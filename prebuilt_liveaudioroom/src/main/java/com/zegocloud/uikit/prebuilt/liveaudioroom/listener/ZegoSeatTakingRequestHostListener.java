package com.zegocloud.uikit.prebuilt.liveaudioroom.listener;

import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

public interface ZegoSeatTakingRequestHostListener {
    void onSeatTakingRequested(ZegoUIKitUser audience);

    void onSeatTakingRequestCancelled(ZegoUIKitUser audience);

    void onSeatTakingInviteRejected();
}
