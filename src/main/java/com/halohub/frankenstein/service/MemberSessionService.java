package com.halohub.frankenstein.service;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpLogic;
import com.halohub.frankenstein.satoken.StpMemberUtil;
import org.springframework.stereotype.Service;

@Service
public class MemberSessionService {

    /**
     * Kicks oldest sessions when active device count exceeds the allowed limit.
     */
    public void enforceDeviceLimit(long memberId, int maxDevices) {
        StpLogic logic = StpMemberUtil.getStpLogic();
        SaSession session = logic.getSessionByLoginId(memberId, false);
        if (session == null) {
            return;
        }
        logic.logoutByMaxLoginCount(memberId, session, "", maxDevices);
    }
}
