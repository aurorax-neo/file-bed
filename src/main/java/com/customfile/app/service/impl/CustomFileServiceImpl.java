package com.customfile.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.customfile.app.common.constant.PathConstant;
import com.customfile.app.common.exception.BusinessException;
import com.customfile.app.common.response.StateCode;
import com.customfile.app.common.utils.FileUtil;
import com.customfile.app.mapper.CustomFileMapper;
import com.customfile.app.model.entity.CustomFile;
import com.customfile.app.service.CustomFileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * 部分文件服务impl
 *
 * @author YCJ
 * @date 2023/02/20
 */
@Service
public class CustomFileServiceImpl extends ServiceImpl<CustomFileMapper, CustomFile> implements CustomFileService {

    private final static String savePath = PathConstant.getFileSavePath();
    private final static String tempPath = PathConstant.getFileTempPath();
    @Resource
    private CustomFileMapper customFileMapper;

    /**
     * 上传分片文件
     *
     * @param file         文件
     * @param fileName     完整文件名称
     * @param fileSize     文件大小
     * @param segmentIndex 分片索引
     * @param segmentSize  分片大小
     * @param key          md5关键
     * @return {@link CustomFile}
     */
    @Override
    public CustomFile upLoadSegmentFile(HttpServletRequest httpServletRequest, MultipartFile file, String fileName, Long fileSize, Integer segmentIndex, Long segmentSize, String key) {
        // 查找是否存在，不存在就写入
        CustomFile customFile = this.getSegmentFileByKey(key);
        if (customFile == null) {
            boolean writeSuccess = this.createFile(fileName, savePath, fileSize, segmentSize, key);
            if (!writeSuccess) {
                // 写入失败，返回错误信息
                throw new BusinessException(StateCode.SYSTEM_ERROR, "文件数据库记录创建失败");
            }
        }

        customFile = this.getSegmentFileByKey(key);
        // 将当前分片存入
        boolean isSegmentSaveSuccess = this.saveSegment(customFile, file, tempPath, key);
        if (!isSegmentSaveSuccess) {
            // 分片存储失败
            throw new BusinessException(StateCode.SYSTEM_ERROR, "分片文件存储失败");
        }

        //删除分片线程
        CustomFile finalCustomFile = customFile;
        class deleteThread extends Thread {
            @Override
            public void run() {
                try {
                    deleteSegments(finalCustomFile, key, tempPath);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // 判断是否分片齐全，齐全则合并生成完整文件
        // 其实考虑这步会不会失败应该在数据库再加一个值，再说吧
        if (segmentIndex.equals(customFile.getSegmentTotal())) {
            boolean mergeSuccess = this.mergeSegment(customFile, key, tempPath);
            if (!mergeSuccess) {
                throw new BusinessException(StateCode.SYSTEM_ERROR, "文件分片合并失败");
            }
            // 另开线程去自旋删除
            new deleteThread().start();
        }
        return customFile;
    }

    /**
     * 检查部分文件
     *
     * @param key md5关键
     * @return {@link CustomFile}
     */
    @Override
    public CustomFile getSegmentFileByKey(String key) {
        LambdaQueryWrapper<CustomFile> query = new LambdaQueryWrapper<>();
        query.eq(key != null, CustomFile::getCustomKey, key);
        return customFileMapper.selectOne(query);
    }

    /**
     * 创建文件
     *
     * @param fullFileName 源文件名称
     * @param savePath     保存路径
     * @param size         大小
     * @param segmentSize  段大小
     * @param key          md5关键
     * @return boolean
     */
    private boolean createFile(String fullFileName, String savePath, Long size, Long segmentSize, String key) {
        //文件原名（不含后缀）
        String name = FileUtil.getFileNameWithoutSuffix(fullFileName);
        //文件后缀
        String suffix = FileUtil.getFileSuffix(fullFileName);
        //文件重命名（含后缀）
        String saveFileName = FileUtil.getFileNameWithSuffix(key, suffix);

        CustomFile customFile = new CustomFile();
        customFile.setName(name);
        customFile.setSuffix(suffix);
        customFile.setSize(size);
        customFile.setPath(savePath.concat(saveFileName));
        customFile.setSegmentIndex(0);
        customFile.setSegmentSize(segmentSize);

        int total = (int) (size / segmentSize);
        if (size % segmentSize != 0) {
            total++;
        }
        customFile.setSegmentTotal(total);
        customFile.setCustomKey(key);

        return customFileMapper.insert(customFile) > 0;
    }

    /**
     * 保存分片
     *
     * @param file       文件
     * @param savePath   保存路径
     * @param key        md5关键
     * @param customFile 部分文件
     * @return boolean
     */
    private boolean saveSegment(CustomFile customFile, MultipartFile file, String savePath, String key) {

        //分片索引+1
        int segmentIndex = customFile.getSegmentIndex() + 1;

        String fullFileName = FileUtil.getFileNameWithSuffix(key, customFile.getSuffix());
        String segmentFileName = FileUtil.getSegmentName(fullFileName, segmentIndex);
        // 存储分片
        boolean saveSuccess = saveFile(file, savePath + segmentFileName);
        if (saveSuccess) {
            // 更新分片索引
            customFile.setSegmentIndex(segmentIndex);
            int row = customFileMapper.updateById(customFile);
            if (row <= 0) {
                throw new BusinessException(StateCode.SYSTEM_ERROR, "分片索引更新失败");
            }
            return true;
        }
        return false;
    }

    /**
     * 合并分片
     *
     * @param key        md5关键
     * @param tempPath   临时路径
     * @param customFile 分片文件
     * @return boolean
     */
    private boolean mergeSegment(CustomFile customFile, String key, String tempPath) {

        boolean debug = false;

        int segmentCount = customFile.getSegmentTotal();
        FileInputStream fileInputStream = null;
        FileOutputStream outputStream = null;
        byte[] byt = new byte[10 * 1024 * 1024];
        try {
            // 整合结果文件
            File newFile = new File(customFile.getPath());
            outputStream = new FileOutputStream(newFile, true);
            int len;
            for (int i = 0; i < segmentCount; i++) {
                String segmentFileName = FileUtil.getFileNameWithSuffix(key, customFile.getSuffix());
                String segmentFilePath = tempPath + FileUtil.getSegmentName(segmentFileName, i + 1);
                fileInputStream = new FileInputStream(segmentFilePath);
                while ((len = fileInputStream.read(byt)) != -1) {
                    outputStream.write(byt, 0, len);
                }
            }
        } catch (IOException e) {
            if (debug) System.out.println("分片合并异常");
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                if (debug) System.out.println("IO流关闭");
            } catch (Exception e) {
                if (debug) System.out.println("IO流异常关闭");
                e.printStackTrace();
            }
        }
        if (debug) System.out.println("分片合并成功");
        return true;
    }

    /**
     * 删除分片文件
     *
     * @param key        md5关键
     * @param customFile 部分文件
     * @param tempPath   临时道路
     * @throws InterruptedException 中断异常
     */
    private void deleteSegments(CustomFile customFile, String key, String tempPath) throws InterruptedException {

        boolean debug = false;

        // 为了保证不被占用，先回收数据流对象
        System.gc();
        Thread.sleep(1000);
        int segmentCount = customFile.getSegmentTotal();
        int firstFinished = 0;
        int[] visited = new int[segmentCount];
        for (int i = 0; i < segmentCount; i++) {
            String segmentFileName = FileUtil.getFileNameWithSuffix(key, customFile.getSuffix());
            String segmentFilePath = tempPath + FileUtil.getSegmentName(segmentFileName, i + 1);
            File file = new File(segmentFilePath);
            boolean result = file.delete();
            if (result) {
                firstFinished++;
                visited[i] = 1;
            }
            if (debug) System.out.println("分片文件:" + segmentFilePath + "删除" + (result ? "成功" : "失败"));
        }
        // visited数组，然后完成了再去除，直到count到达总数
        while (firstFinished < segmentCount) {
            System.gc();
            Thread.sleep(1000);
            for (int i = 0; i < segmentCount; i++) {
                if (visited[i] == 0) {
                    String segmentFileName = FileUtil.getFileNameWithSuffix(key, customFile.getSuffix());
                    String segmentFilePath = tempPath + FileUtil.getSegmentName(segmentFileName, i + 1);
                    File file = new File(segmentFilePath);
                    boolean result = file.delete();
                    if (result) {
                        visited[i] = 1;
                        firstFinished++;
                    }
                    if (debug) System.out.println("分片文件:" + segmentFilePath + "删除" + (result ? "成功" : "失败"));
                }
            }
        }
    }


    /**
     * 保存文件
     *
     * @param file 文件
     * @param path 路径
     * @return boolean
     */
    private boolean saveFile(MultipartFile file, String path) {
        File dest = new File(path);
        //判断文件父目录是否存在

        if (!dest.getParentFile().exists()) {
            boolean b = dest.getParentFile().mkdir();
            if (!b) {
                return false;
            }
        }
        //保存文件
        try {
            file.transferTo(dest);
            return true;
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
