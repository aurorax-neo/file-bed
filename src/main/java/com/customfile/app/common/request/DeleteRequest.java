package com.customfile.app.common.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 删除请求
 *
 * @author YCJ
 * @date 2022/12/18
 */
@Data
public class DeleteRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 2018465367535488680L;
    /**
     * id
     */
    private Long id;
}