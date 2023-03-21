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
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Synchronized;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


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

    private static final Cache<Object, Object> CACHE = CacheBuilder.newBuilder()
            .maximumSize(1024)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();

    /**
     * 上传分片文件
     *
     * @param segmentFile  文件
     * @param fileName     完整文件名称
     * @param segmentIndex 分片索引
     * @param segmentSize  分片大小
     * @param fileKey      key
     * @return {@link CustomFile}
     */
    @Override
    @Synchronized
    public CustomFile upLoadCustomFile(HttpServletRequest httpServletRequest, String fileName, Long fileSize, MultipartFile segmentFile, Integer segmentIndex, Long segmentSize, Integer segmentTotal, String fileKey, String fileMD5) {
        // 存储路径
        final String savePath = PathConstant.getFileSavePath().concat(fileMD5).concat(File.separator);

        if (CACHE.getIfPresent(fileKey + segmentIndex) != null) {
            throw new BusinessException(StateCode.SYSTEM_ERROR, "请勿重复请求");
        } else {
            CACHE.put(fileKey + segmentIndex, 0);
        }

        // 秒传(0索引用来秒传校验)
        if (segmentIndex == 0) {
            // 查找是否存在，不存在添加在数据库
            CustomFile customFile = this.getCustomFileByKey(fileKey);
            if (customFile == null) {
                this.createFile(fileName, fileSize, savePath, segmentSize, segmentTotal, fileKey, fileMD5);
            } else if (customFile.getIsMerge() > 0) {
                return customFile;
            } else if (customFile.getIsMerge().equals(0) && customFile.getSegmentIndex().equals(customFile.getSegmentTotal())) {
                this.mergeSegment(fileName, savePath, segmentTotal, savePath, fileKey);
            }
            // 查询是否存在非秒传文件记录
            LambdaQueryWrapper<CustomFile> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(CustomFile::getFilePath, CustomFile::getSegmentIndex, CustomFile::getSegmentTotal)
                    .eq(true, CustomFile::getIsST, 0)
                    .eq(true, CustomFile::getIsMerge, 1)
                    .eq(true, CustomFile::getFileMD5, fileMD5);
            CustomFile existFile = customFileMapper.selectOne(queryWrapper);
            // 如果存在
            if (existFile != null) {
                String filePath_ = existFile.getFilePath();
                Integer segmentIndex_ = existFile.getSegmentIndex();
                LambdaUpdateWrapper<CustomFile> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(filePath_ != null, CustomFile::getFilePath, filePath_)
                        .set(segmentIndex_ > 0, CustomFile::getSegmentIndex, segmentIndex_)
                        .set(true, CustomFile::getIsST, 1)
                        .eq(true, CustomFile::getFileKey, fileKey);
                customFileMapper.update(new CustomFile(), updateWrapper);
            }
            return this.getCustomFileByKey(fileKey);
        }

        // 将当前分片存入
        this.saveSegment(fileName, segmentFile, segmentIndex, savePath, fileKey);

        // 分片上传完成，合并和删除分片
        if (segmentIndex.equals(segmentTotal)) {
            // 合并分片
            this.mergeSegment(fileName, savePath, segmentTotal, savePath, fileKey);
            // 更新合并状态
            LambdaUpdateWrapper<CustomFile> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(true, CustomFile::getIsMerge, 1)
                    .eq(true, CustomFile::getFileKey, fileKey);
            customFileMapper.update(new CustomFile(), updateWrapper);

            // 另开线程去自旋删除
            DeleteSegments deleteSegments = new DeleteSegments(fileName, fileKey, savePath, segmentTotal);
            deleteSegments.start();
        }
        return this.getCustomFileByKey(fileKey);
    }

    /**
     * 被键自定义文件
     *
     * @param key md5关键
     * @return {@link CustomFile}
     */
    @Override
    public CustomFile getCustomFileByKey(String key) {
        LambdaQueryWrapper<CustomFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(key != null, CustomFile::getFileKey, key);
        return customFileMapper.selectOne(queryWrapper);
    }

    /**
     * 创建文件
     *
     * @param path        保存路径
     * @param segmentSize 段大小
     * @param key         md5关键
     */
    private void createFile(String fileName, Long fileSize, String path, Long segmentSize, Integer segmentTotal, String key, String fileMD5) {

        //文件后缀
        String suffix = FileUtil.getFileSuffix(fileName);
        //文件重命名（含后缀）
        String completeFileName = FileUtil.getFileNameWithSuffix(key, suffix);

        synchronized (key.intern()) {
            CustomFile customFile = new CustomFile();
            customFile.setFileName(fileName);
            customFile.setFileSize(fileSize);
            customFile.setFilePath(path.concat(completeFileName));
            customFile.setSegmentIndex(0);
            customFile.setSegmentSize(segmentSize);
            customFile.setSegmentTotal(segmentTotal);
            customFile.setIsMerge(0);
            customFile.setIsST(0);
            customFile.setFileKey(key);
            customFile.setFileMD5(fileMD5);
            customFile.setIsDelete(0);

            boolean isSuccess = customFileMapper.insert(customFile) > 0;
            if (!isSuccess) {
                // 写入失败，返回错误信息
                throw new BusinessException(StateCode.SYSTEM_ERROR, "文件数据库记录创建失败");
            }
        }
    }

    /**
     * 保存分片
     *
     * @param file     文件
     * @param filePath 保存路径
     * @param fileKey  关键
     */
    private void saveSegment(String fileName, MultipartFile file, Integer segmentIndex, String filePath, String fileKey) {
        String segmentName = FileUtil.getSegmentName(fileName, fileKey, segmentIndex);
        this.saveFile(file, filePath.concat(segmentName));
        // 更新分片索引
        LambdaUpdateWrapper<CustomFile> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(segmentIndex > 0, CustomFile::getSegmentIndex, segmentIndex)
                .eq(fileKey != null, CustomFile::getFileKey, fileKey);
        int row = customFileMapper.update(new CustomFile(), updateWrapper);
        if (row <= 0) {
            throw new BusinessException(StateCode.SYSTEM_ERROR, "分片索引更新失败");
        }
    }

    /**
     * 合并分片
     */
    private void mergeSegment(String fileName, String segmentPath, int segmentTotal, String filePath, String fileKey) {

        boolean debug = false;

        FileInputStream fileInputStream = null;
        FileOutputStream outputStream = null;
        byte[] byt = new byte[10 * 1024 * 1024];
        try {
            // 合并文件 全字串
            filePath = filePath.concat(FileUtil.getFileNameWithSuffix(fileKey, FileUtil.getFileSuffix(fileName)));
            // 整合结果文件
            File newFile = new File(filePath);
            if (!newFile.getParentFile().exists()) {
                if (!newFile.getParentFile().mkdirs()) {
                    throw new BusinessException(StateCode.SYSTEM_ERROR, "文件夹创建失败");
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
            throw new BusinessException(StateCode.SYSTEM_ERROR, "文件分片合并失败");
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
    }


    /**
     * 保存文件
     *
     * @param file     文件
     * @param filePath 路径
     */
    private void saveFile(MultipartFile file, String filePath) {
        File dest = new File(filePath);
        //判断文件父目录是否存在
        if (!dest.getParentFile().exists()) {
            if (!dest.getParentFile().mkdirs()) {
                // 文件夹创建失败
                throw new BusinessException(StateCode.SYSTEM_ERROR, dest.getParentFile() + "文件夹创建失败");
            }
        }
        //保存文件
        try {
            file.transferTo(dest);
        } catch (Exception e) {
            e.printStackTrace();
            // 文件存储失败
            throw new BusinessException(StateCode.SYSTEM_ERROR, "分片文件存储失败");
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
