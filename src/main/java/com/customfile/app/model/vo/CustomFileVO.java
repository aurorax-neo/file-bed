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

    private Long id;
    private String name;
    private String suffix;
    private Long size;
    private String url;
    private Integer segmentIndex;
    private Long segmentSize;
    private Integer segmentTotal;
    private Date uploadDate;
    private Integer isDelete;
}
