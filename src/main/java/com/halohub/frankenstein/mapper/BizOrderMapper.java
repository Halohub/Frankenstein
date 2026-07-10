package com.halohub.frankenstein.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.halohub.frankenstein.entity.BizOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BizOrderMapper extends BaseMapper<BizOrder> {

    @Select("SELECT * FROM biz_order WHERE member_id = #{memberId} AND status = 'PENDING_PAY' " +
            "AND expire_time IS NOT NULL AND expire_time < #{now} AND deleted = 0")
    List<BizOrder> listExpiredPendingOrders(@Param("memberId") Long memberId,
                                            @Param("now") LocalDateTime now);

    @Select("SELECT * FROM biz_order WHERE status = 'PENDING_PAY' " +
            "AND expire_time IS NOT NULL AND expire_time < #{now} AND deleted = 0")
    List<BizOrder> listAllExpiredPendingOrders(@Param("now") LocalDateTime now);
}
