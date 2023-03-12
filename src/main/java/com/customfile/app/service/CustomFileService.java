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
     * 加载自定义文件
     *
     * @param httpServletRequest http servlet请求
     * @param fileName           文件名称
     * @param segmentFile        部分文件
     * @param segmentIndex       段指数
     * @param segmentSize        段大小
     * @param segmentTotal       段总
     * @param fileKey                关键
     * @return {@link CustomFile}
     */
    CustomFile upLoadCustomFile(HttpServletRequest httpServletRequest, String fileName,Long fileSize, MultipartFile segmentFile, Integer segmentIndex, Long segmentSize, Integer segmentTotal, String fileKey, String fileMD5);

    /**
     * 检查文件
     * 该文件存在，返回数据
     *
     * @param key md5关键
     * @return {@link CustomFile}
     */
    CustomFile getCustomFileByKey(String key);
}
