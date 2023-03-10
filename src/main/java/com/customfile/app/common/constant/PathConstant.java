package com.customfile.app.common.constant;

import com.customfile.app.common.utils.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;


/**
 * 路径常量
 *
 * @author YCJ
 * @date 2023/02/21
 */
@Component
public class PathConstant {

    private static String FILE_SAVE_PATH;
    private static String FILE_TEMP_PATH;

    public static String getFileSavePath() {
        return FILE_SAVE_PATH;
    }

    @Value("${file.save-path:DEFAULT}")
    private void setFileSavePath(String fileSavePath) {
        String path = FileUtil.getApplicationHomePath()
                .concat("data")
                .concat(File.separator)
                .concat("file")
                .concat(File.separator);
        PathConstant.FILE_SAVE_PATH = "DEFAULT".equals(fileSavePath) ? path : fileSavePath;
    }

    public static String getFileTempPath() {
        return FILE_TEMP_PATH;
    }

    @Value("${file.temp-path:DEFAULT}")
    private void setFileTempPath(String fileTempPath) {
        String path = FileUtil.getApplicationHomePath()
                .concat("data")
                .concat(File.separator)
                .concat("file")
                .concat(File.separator)
                .concat("temp")
                .concat(File.separator);
        PathConstant.FILE_TEMP_PATH = "DEFAULT".equals(fileTempPath) ? path : fileTempPath;
    }
}
