package com.customfile.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.customfile.app.model.entity.CustomFile;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;


/**
 * 分片上传文件服务
 *
 * @author YCJ
 * @date 2023/02/20
 */
public interface CustomFileService extends IService<CustomFile> {


    /**
     * 分片上传文件
     *
     * @param segmentFile    部分文件
     * @param fileName 源文件名称
     * @param fileSize       文件大小
     * @param segmentIndex   段指数
     * @param segmentSize    段大小
     * @param key            md5关键
     * @return {@link CustomFile}
     */
    CustomFile upLoadSegmentFile(HttpServletRequest httpServletRequest, MultipartFile segmentFile, String fileName, Long fileSize, Integer segmentIndex, Long segmentSize, String key);

    /**
     * 检查文件
     * 该文件存在，返回数据
     *
     * @param key md5关键
     * @return {@link CustomFile}
     */
    CustomFile getSegmentFileByKey(String key);
}
