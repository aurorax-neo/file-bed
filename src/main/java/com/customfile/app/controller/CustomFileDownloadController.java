package com.customfile.app.controller;

import com.customfile.app.common.constant.ApiMapping;
import com.customfile.app.common.exception.BusinessException;
import com.customfile.app.common.response.StateCode;
import com.customfile.app.model.entity.CustomFile;
import com.customfile.app.service.CustomFileService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


/**
 * 下载控制器
 *
 * @author YCJ
 * @date 2023/02/21
 */
@Controller
@RequestMapping(ApiMapping.CUSTOM_FILE_DOWNLOAD_CONTROLLER_MAPPING)
public class CustomFileDownloadController {

    @Resource
    private CustomFileService customFileService;

    @GetMapping("/{key}")
    public ResponseEntity<FileSystemResource> sendFile(HttpServletRequest request, @PathVariable String key) {
        CustomFile customFile = customFileService.getSegmentFileByKey(key);

        if (customFile == null) {
            throw new BusinessException(StateCode.NOT_FOUND_ERROR);
        }

        File returnFile = new File(customFile.getPath());
        if (!returnFile.exists()) {
            throw new BusinessException(StateCode.NOT_FOUND_ERROR, "请求文件丢失");
        }


        String fileName = customFile.getName();
        String fileSuffix = customFile.getSuffix();
        fileName = fileSuffix != null ? fileName.concat(".").concat(fileSuffix) : fileName;
        String userAgent = request.getHeader("USER-AGENT");//获取浏览器版本
        if (StringUtils.contains(userAgent, "MSIE")) {//IE浏览器
            fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        } else if (StringUtils.contains(userAgent, "Mozilla")) {//google,火狐浏览器
            fileName = new String(fileName.getBytes(), StandardCharsets.ISO_8859_1);
        } else {
            fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);//其他浏览器
        }

        //响应头
        HttpHeaders headers = new HttpHeaders();
        headers.add("Expires", "0");
        headers.add("Pragma", "no-cache");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("ETag", String.valueOf(System.currentTimeMillis()));
        headers.add("Content-Disposition", "attachment;filename=".concat(fileName));
        //内容长度
        long contentLength = returnFile.length();
        //媒体类型
        MediaType mediaType = MediaType.parseMediaType("application/octet-stream");
        //响应体
        FileSystemResource body = new FileSystemResource(returnFile);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(contentLength)
                .contentType(mediaType)
                .body(body);
    }

}
