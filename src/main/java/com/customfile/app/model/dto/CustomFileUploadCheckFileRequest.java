package com.customfile.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author YCJ
 * @date 2023/3/8 0008
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomFileUploadCheckFileRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -1501364428428427044L;
    private String key;
}
