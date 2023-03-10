package com.customfile.app.model.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomFileUploadSegmentFileRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 5234429507977777052L;

    private MultipartFile segmentFile;
    private String fileName;
    private Long fileSize;
    private Integer segmentIndex;
    private Long segmentSize;
    private String key;
}
