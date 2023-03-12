package com.customfile.app.common.utils;

import org.springframework.boot.system.ApplicationHome;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.UUID;

import static com.customfile.app.common.constant.ApiMapping.CUSTOM_FILE_DOWNLOAD_CONTROLLER_MAPPING;

/**
 * 文件工具类 文件名生成
 *
 * @author YCJ
 * @date 2023/02/20
 */
public class FileUtil {

    public static String getApplicationHomePath() {
        ApplicationHome ah = new ApplicationHome(FileUtil.class);
        File file = ah.getSource();
        String path = file.getParentFile().toString();
        return path.concat(File.separator);
    }

    public static String getFileNameWithSuffix(String name, String suffix) {
        return name + "." + suffix;
    }

    public static String getFileNameWithoutSuffix(String fullFileName) {
        int suffixIndex = fullFileName.lastIndexOf('.');
        if (suffixIndex < 0)
            return fullFileName;
        return fullFileName.substring(0, suffixIndex);
    }

    public static String getFileSuffix(String fileName) {
        int suffixIndex = fileName.lastIndexOf('.');
        if (suffixIndex < 0)
            return "";
        return fileName.substring(suffixIndex + 1);
    }

    public static String getSegmentName(String fileName, String reName, int segmentIndex) {
        String suffix = FileUtil.getFileSuffix(fileName);
        String reFileName = FileUtil.getFileNameWithSuffix(reName, suffix);
        return reFileName.concat("#") + segmentIndex;
    }

    public static String getUUIDFileNameWithSuffix(String fullFileName) {
        String suffix = getFileSuffix(fullFileName);
        String name = UUID.randomUUID().toString();
        return name.concat(".").concat(suffix);
    }

    public static String getFileUrl(HttpServletRequest httpServletRequest, String key) {
        String basePath = httpServletRequest.getScheme()
                .concat("://")
                .concat(httpServletRequest.getServerName())
                .concat(":")
                .concat(String.valueOf(httpServletRequest.getServerPort()))
                .concat(httpServletRequest.getContextPath());
        return basePath
                .concat(CUSTOM_FILE_DOWNLOAD_CONTROLLER_MAPPING + "/")
                .concat(key);
    }
}
