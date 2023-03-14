package com.customfile.app.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Date;


/**
 * 保存文件
 *
 * @author YCJ
 * @date 2023/03/08
 */
@TableName("customFile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomFile implements Serializable {
    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = -1145039356017772808L;

    @TableId
    private Long id;
    private String fileName;
    private Long fileSize;
    private String filePath;
    private Integer segmentIndex;
    private Long segmentSize;
    private Integer segmentTotal;
    private Date uploadDate;
    private Integer isMerge;
    private Integer isST;
    private String fileMD5;
    private String fileKey;
    @TableLogic
    private Integer isDelete;
}
