package com.halohub.frankenstein.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.halohub.frankenstein.entity.BizCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BizCategoryMapper extends BaseMapper<BizCategory> {

    @Select("SELECT COUNT(1) FROM biz_category WHERE parent_id = #{categoryId} AND deleted = 0")
    long countChildren(@Param("categoryId") Long categoryId);

    @Select("SELECT COUNT(1) FROM biz_spu WHERE category_id = #{categoryId} AND deleted = 0")
    long countSpuByCategory(@Param("categoryId") Long categoryId);
}
