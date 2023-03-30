package com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service;

import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.List;

public interface OutgoingInvitationListener {

    void onActionSendInvitation(List<String> invitees, int errorCode, String message,
        List<ZegoUIKitUser> errorInvitees);

    void onActionCancelInvitation(List<String> invitees, int errorCode, String message);

    void onSendInvitationButReceiveResponseTimeout(ZegoUIKitUser invitee, String extendedData);

    void onSendInvitationAndIsAccepted(ZegoUIKitUser invitee, String extendedData);

    void onSendInvitationButIsRejected(ZegoUIKitUser invitee, String extendedData);

}
