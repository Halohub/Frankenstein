package com.halohub.frankenstein.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.halohub.frankenstein.entity.BizSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BizSkuMapper extends BaseMapper<BizSku> {

    @Select("SELECT * FROM biz_sku WHERE spu_id = #{spuId} AND deleted = 0 ORDER BY id ASC")
    List<BizSku> listBySpuId(@Param("spuId") Long spuId);

    @Select("SELECT COUNT(1) FROM biz_sku WHERE sku_code = #{skuCode} AND deleted = 0 " +
            "AND (#{excludeId} IS NULL OR id <> #{excludeId})")
    long countBySkuCode(@Param("skuCode") String skuCode, @Param("excludeId") Long excludeId);
}
