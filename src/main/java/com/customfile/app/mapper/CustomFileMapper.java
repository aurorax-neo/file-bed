package com.customfile.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.customfile.app.model.entity.CustomFile;
import org.apache.ibatis.annotations.Mapper;


/**
 * 图像映射器
 *
 * @author YCJ
 * @date 2023/02/20
 */
@Mapper
public interface CustomFileMapper extends BaseMapper<CustomFile> {
}
