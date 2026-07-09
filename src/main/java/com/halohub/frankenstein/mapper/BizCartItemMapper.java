package com.halohub.frankenstein.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.halohub.frankenstein.entity.BizCartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BizCartItemMapper extends BaseMapper<BizCartItem> {

    @Select("SELECT * FROM biz_cart_item WHERE member_id = #{memberId} ORDER BY id DESC")
    List<BizCartItem> listByMemberId(@Param("memberId") Long memberId);

    @Select("SELECT * FROM biz_cart_item WHERE member_id = #{memberId} AND sku_id = #{skuId} LIMIT 1")
    BizCartItem findByMemberAndSku(@Param("memberId") Long memberId, @Param("skuId") Long skuId);
}
