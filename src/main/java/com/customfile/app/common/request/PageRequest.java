package com.customfile.app.common.request;

import com.customfile.app.common.constant.CommonConstant;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


@Data
public class PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -8989132333688527597L;
    /**
     * 当前页号
     */
    private long current = 1;

    /**
     * 页面大小
     */
    private long pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder = CommonConstant.SORT_ORDER_ASC;
}
