package com.customfile.app.model.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Date;


/**
 * 分片文件
 *
 * @author YCJ
 * @date 2023/02/20
 */
@TableName("SegmentFile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomFileVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -1145039356017772808L;

    private String id;
    private String fileName;
    private Long fileSize;
    private String fileUrl;
    private Integer segmentIndex;
    private Long segmentSize;
    private Integer segmentTotal;
    private Integer isMerge;
    private Date uploadDate;
    private Integer isDelete;
}
