package com.halohub.frankenstein.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.halohub.frankenstein.entity.BizPayment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BizPaymentMapper extends BaseMapper<BizPayment> {

    @Select("SELECT * FROM biz_payment WHERE payment_no = #{paymentNo} LIMIT 1")
    BizPayment findByPaymentNo(@Param("paymentNo") String paymentNo);

    @Select("SELECT * FROM biz_payment WHERE order_id = #{orderId} AND status = 'PENDING' " +
            "ORDER BY id DESC LIMIT 1")
    BizPayment findPendingByOrderId(@Param("orderId") Long orderId);
}
