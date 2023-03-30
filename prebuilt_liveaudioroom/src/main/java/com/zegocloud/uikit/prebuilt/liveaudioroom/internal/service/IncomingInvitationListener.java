package com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service;

import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

public interface IncomingInvitationListener {

    void onReceiveNewInvitation(ZegoUIKitUser inviter, int type, String data);

    void onReceiveInvitationButResponseTimeout(ZegoUIKitUser inviter);

    void onReceiveInvitationButIsCancelled(ZegoUIKitUser inviter, String extendedData);

    void onActionAcceptInvitation(ZegoUIKitUser inviter, int errorCode, String message);

    void onActionRejectInvitation(ZegoUIKitUser inviter, int errorCode, String message);
}
