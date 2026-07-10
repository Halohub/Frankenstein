package com.halohub.frankenstein.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.halohub.frankenstein.entity.BizOrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BizOrderItemMapper extends BaseMapper<BizOrderItem> {

    @Select("SELECT * FROM biz_order_item WHERE order_id = #{orderId} ORDER BY id ASC")
    List<BizOrderItem> listByOrderId(@Param("orderId") Long orderId);
}
