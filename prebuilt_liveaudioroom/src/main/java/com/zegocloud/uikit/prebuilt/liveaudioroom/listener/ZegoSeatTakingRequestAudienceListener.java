package com.zegocloud.uikit.prebuilt.liveaudioroom.listener;

import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

public interface ZegoSeatTakingRequestAudienceListener {

    void onSeatTakingRequestRejected(ZegoUIKitUser invitee, String extendedData);

    void onHostSeatTakingInviteSent();
}
