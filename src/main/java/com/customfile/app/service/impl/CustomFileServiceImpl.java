package com.customfile.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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

    @Resource
    private CustomFileMapper customFileMapper;

    /**
     * 上传分片文件
     *
     * @param segmentFile  文件
     * @param fileName     完整文件名称
     * @param segmentIndex 分片索引
     * @param segmentSize  分片大小
     * @param key          key
     * @return {@link CustomFile}
     */
    @Override
    public CustomFile upLoadCustomFile(HttpServletRequest httpServletRequest, String fileName, MultipartFile segmentFile, Integer segmentIndex, Long segmentSize, Integer segmentTotal, String key) {
        final String savePath = PathConstant.getFileSavePath().concat(key).concat(File.separator);
        // 查找是否存在，不存在添加在数据库
        CustomFile customFile = this.getCustomFileByKey(key);
        if (customFile == null) {
            boolean writeSuccess = this.createFile(fileName, segmentFile, savePath, segmentSize, segmentTotal, key);
            if (!writeSuccess) {
                // 写入失败，返回错误信息
                throw new BusinessException(StateCode.SYSTEM_ERROR, "文件数据库记录创建失败");
            }
        }

        // 将当前分片存入
        boolean isSegmentSaveSuccess = this.saveSegment(fileName, segmentFile, segmentIndex, savePath, key);
        if (!isSegmentSaveSuccess) {
            // 分片存储失败
            throw new BusinessException(StateCode.SYSTEM_ERROR, "分片文件存储失败");
        }

        // 判断是否分片齐全，齐全则合并生成完整文件
        // 其实考虑这会不会失败应该在数据库再加一个值，再说吧
        customFile = this.getCustomFileByKey(key);
        String fileName_ = customFile.getFileName();
        String key_ = customFile.getFileKey();
        Integer segmentTotal_ = customFile.getSegmentTotal();
        String filePath_ = customFile.getFilePath();
        if (segmentIndex.equals(customFile.getSegmentTotal())) {
            // 合并分片
            boolean mergeSuccess = this.mergeSegment(fileName_, savePath, segmentTotal_, filePath_, key_);
            if (!mergeSuccess) {
                throw new BusinessException(StateCode.SYSTEM_ERROR, "文件分片合并失败");
            }
            // 另开线程去自旋删除
            DeleteSegments deleteSegments = new DeleteSegments(fileName_, key_, savePath, segmentTotal_);
            deleteSegments.start();
        }
        return customFile;
    }

    /**
     * 被键自定义文件
     *
     * @param key md5关键
     * @return {@link CustomFile}
     */
    @Override
    public CustomFile getCustomFileByKey(String key) {
        LambdaQueryWrapper<CustomFile> query = new LambdaQueryWrapper<>();
        query.eq(key != null, CustomFile::getFileKey, key);
        return customFileMapper.selectOne(query);
    }

    /**
     * 创建文件
     *
     * @param path        保存路径
     * @param segmentSize 段大小
     * @param key         md5关键
     * @return boolean
     */
    private boolean createFile(String fileName, MultipartFile file, String path, Long segmentSize, Integer segmentTotal, String key) {

        //文件后缀
        String suffix = FileUtil.getFileSuffix(fileName);
        //文件重命名（含后缀）
        String saveFileName = FileUtil.getFileNameWithSuffix(key, suffix);

        synchronized (key.intern()) {
            CustomFile customFile = new CustomFile();
            customFile.setFileName(fileName);
            customFile.setFileSize(file.getSize());
            customFile.setFilePath(path.concat(saveFileName));
            customFile.setSegmentIndex(0);
            customFile.setSegmentSize(segmentSize);
            customFile.setSegmentTotal(segmentTotal);
            customFile.setFileKey(key);
            return customFileMapper.insert(customFile) > 0;
        }
    }

    /**
     * 保存分片
     *
     * @param file     文件
     * @param filePath 保存路径
     * @param fileKey  关键
     * @return boolean
     */
    private boolean saveSegment(String fileName, MultipartFile file, Integer segmentIndex, String filePath, String fileKey) {

        // 存储分片
        // 如果已上传
        if (segmentIndex <= this.getCustomFileByKey(fileKey).getSegmentIndex()) {
            return false;
        }


        // 如果未上传
        String segmentName = FileUtil.getSegmentName(fileName, fileKey, segmentIndex);
        boolean saveSuccess = saveFile(file, filePath.concat(segmentName));
        if (saveSuccess) {
            // 更新分片索引
            LambdaUpdateWrapper<CustomFile> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(segmentIndex > 0, CustomFile::getSegmentIndex, segmentIndex)
                    .eq(fileKey != null, CustomFile::getFileKey, fileKey);
            int row = customFileMapper.update(new CustomFile(), updateWrapper);
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
     * @return boolean
     */
    private boolean mergeSegment(String fileName, String segmentPath, int segmentTotal, String filePath, String fileKey) {

        boolean debug = false;

        FileInputStream fileInputStream = null;
        FileOutputStream outputStream = null;
        byte[] byt = new byte[10 * 1024 * 1024];
        try {
            // 整合结果文件
            File newFile = new File(filePath);
            if (!newFile.getParentFile().exists()) {
                boolean b = newFile.getParentFile().mkdir();
                if (!b) {
                    return false;
                }
            }
            outputStream = new FileOutputStream(newFile, true);
            int len;

            for (int i = 0; i < segmentTotal; i++) {
                String segmentFile = segmentPath + FileUtil.getSegmentName(fileName, fileKey, i + 1);
                fileInputStream = new FileInputStream(segmentFile);
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
     * 保存文件
     *
     * @param file     文件
     * @param filePath 路径
     * @return boolean
     */
    private boolean saveFile(MultipartFile file, String filePath) {
        File dest = new File(filePath);
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
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

/**
 * 删除部分
 *
 * @author YCJ
 * @date 2023/03/11
 */
class DeleteSegments extends Thread {

    private final String fileName;
    private final String fileKey;
    private final String savePath;
    private final Integer segmentTotal;

    DeleteSegments(String fileName, String fileKey, String segmentPath, Integer segmentTotal) {
        this.fileName = fileName;
        this.fileKey = fileKey;
        this.savePath = segmentPath;
        this.segmentTotal = segmentTotal;
    }


    @Override
    public void run() {
        this.deleteSegments();
    }

    private void deleteSegments() {

        boolean debug = false;

        // 为了保证不被占用，先回收数据流对象
        System.gc();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        int firstFinished = 0;
        int[] visited = new int[this.segmentTotal];
        for (int i = 0; i < this.segmentTotal; i++) {
            String segmentFile = this.savePath + FileUtil.getSegmentName(this.fileName, this.fileKey, i + 1);
            File file = new File(segmentFile);
            boolean result = file.delete();
            if (result) {
                firstFinished++;
                visited[i] = 1;
            }
            if (debug) System.out.println("分片文件:" + segmentFile + "删除" + (result ? "成功" : "失败"));
        }
        // visited数组，然后完成了再去除，直到count到达总数
        while (firstFinished < this.segmentTotal) {
            System.gc();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (int i = 0; i < this.segmentTotal; i++) {
                if (visited[i] == 0) {
                    String segmentFile = this.savePath + FileUtil.getSegmentName(this.fileName, this.fileKey, i + 1);
                    File file = new File(segmentFile);
                    boolean result = file.delete();
                    if (result) {
                        visited[i] = 1;
                        firstFinished++;
                    }
                    if (debug) System.out.println("分片文件:" + segmentFile + "删除" + (result ? "成功" : "失败"));
                }
            }
        }
    }
}
