package com.zegocloud.uikit.prebuilt.liveaudioroom.core.cohost;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service.LiveAudioRoomInvitation;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service.LiveAudioRoomInvitationType;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service.LiveAudioRoomManager;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service.OutgoingInvitationListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.List;

public class ZegoTakeSeatButton extends FrameLayout {

    private ZegoRequestTakeSeatButton requestCoHostButton;
    private ZegoCancelRequestCoHostButton cancelRequestCoHostButton;

    public ZegoTakeSeatButton(@NonNull Context context) {
        super(context);
        initView();
    }

    public ZegoTakeSeatButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ZegoTakeSeatButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        requestCoHostButton = new ZegoRequestTakeSeatButton(getContext());
        addView(requestCoHostButton);
        cancelRequestCoHostButton = new ZegoCancelRequestCoHostButton(getContext());
        addView(cancelRequestCoHostButton);

        showRequestButton();

        String hostUserID = LiveAudioRoomManager.getInstance().roleService.getHostUserID();
        LiveAudioRoomInvitation invitation = LiveAudioRoomManager.getInstance().invitationService.getInvitation(
            ZegoUIKit.getLocalUser().userID, hostUserID);
        if (invitation != null && invitation.type == LiveAudioRoomInvitationType.REQUEST_TAKE_SEAT.getValue()) {
            showCancelButton();
        } else {
            showRequestButton();
        }

        LiveAudioRoomManager.getInstance().invitationService.addOutgoingInvitationListener(
            new OutgoingInvitationListener() {
                @Override
                public void onActionSendInvitation(List<String> invitees, int errorCode, String message,
                    List<ZegoUIKitUser> errorInvitees) {
                    LiveAudioRoomInvitation invitation = LiveAudioRoomManager.getInstance().invitationService.getInvitation(
                        ZegoUIKit.getLocalUser().userID, invitees.get(0));
                    if (invitation != null && invitation.isTakeSeatRequest()) {
                        if (errorCode == 0) {
                            showCancelButton();
                        }
                    }

                }

                @Override
                public void onActionCancelInvitation(List<String> invitees, int errorCode, String message) {
                    LiveAudioRoomInvitation invitation = LiveAudioRoomManager.getInstance().invitationService.getInvitation(
                        ZegoUIKit.getLocalUser().userID, invitees.get(0));
                    if (invitation != null && invitation.isTakeSeatRequest()) {
                        if (errorCode == 0) {
                            showRequestButton();
                        }
                    }
                }

                @Override
                public void onSendInvitationButReceiveResponseTimeout(ZegoUIKitUser invitee, String extendedData) {
                    LiveAudioRoomInvitation invitation = LiveAudioRoomManager.getInstance().invitationService.getInvitation(
                        ZegoUIKit.getLocalUser().userID, invitee.userID);
                    if (invitation != null && invitation.isTakeSeatRequest()) {
                        showRequestButton();
                    }
                }

                @Override
                public void onSendInvitationAndIsAccepted(ZegoUIKitUser invitee, String extendedData) {
                    LiveAudioRoomInvitation invitation = LiveAudioRoomManager.getInstance().invitationService.getInvitation(
                        ZegoUIKit.getLocalUser().userID, invitee.userID);
                    if (invitation != null && invitation.isTakeSeatRequest()) {
                        showRequestButton();
                    }
                }

                @Override
                public void onSendInvitationButIsRejected(ZegoUIKitUser invitee, String extendedData) {
                    LiveAudioRoomInvitation invitation = LiveAudioRoomManager.getInstance().invitationService.getInvitation(
                        ZegoUIKit.getLocalUser().userID, invitee.userID);
                    if (invitation != null && invitation.isTakeSeatRequest()) {
                        showRequestButton();
                    }
                }
            });
    }

    private void showRequestButton() {
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setVisibility(GONE);
        }
        requestCoHostButton.setVisibility(VISIBLE);

    }

    private void showCancelButton() {
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setVisibility(GONE);
        }
        cancelRequestCoHostButton.setVisibility(VISIBLE);
    }
}
