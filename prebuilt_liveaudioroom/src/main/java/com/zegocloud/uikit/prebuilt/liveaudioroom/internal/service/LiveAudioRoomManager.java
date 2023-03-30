package com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service;

import android.content.Context;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.common.IZegoUIKitSignalingPlugin;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoInnerText;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoLiveAudioRoomLayoutConfig;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.PrebuiltUICallBack;
import com.zegocloud.uikit.service.defines.ZegoRoomPropertyUpdateListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitSignalingPluginInvitationListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitSignalingPluginRoomPropertyUpdateListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LiveAudioRoomManager {

    private LiveAudioRoomManager() {
    }

    private static final class Holder {

        private static final LiveAudioRoomManager INSTANCE = new LiveAudioRoomManager();
    }

    public static LiveAudioRoomManager getInstance() {
        return LiveAudioRoomManager.Holder.INSTANCE;
    }

    public SeatService seatService = new SeatService();
    public RoleService roleService = new RoleService();
    public InvitationService invitationService = new InvitationService();
    private PrebuiltUICallBack uiCallBack;
    private ZegoInnerText innerText;
    Context context;

    public void init(Context context, ZegoLiveAudioRoomLayoutConfig layoutConfig, ZegoInnerText innerText) {
        this.context = context.getApplicationContext();
        this.innerText = innerText;
        seatService.init(layoutConfig);
        roleService.init();
        IZegoUIKitSignalingPlugin signalingPlugin = ZegoUIKit.getSignalingPlugin();
        signalingPlugin.addRoomPropertyUpdateListener(new ZegoUIKitSignalingPluginRoomPropertyUpdateListener() {
            @Override
            public void onRoomPropertyUpdated(String key, String oldValue, String newValue) {

            }

            @Override
            public void onRoomPropertiesFullUpdated(List<String> updateKeys, HashMap<String, String> oldProperties,
                HashMap<String, String> properties) {
                seatService.onSignalRoomPropertiesFullUpdated(updateKeys, oldProperties, properties);
                roleService.onSignalRoomPropertiesFullUpdated(updateKeys, oldProperties, properties);

            }
        });
        signalingPlugin.addUsersInRoomAttributesUpdateListener((updateKeys, oldAttributes, attributes, editor) -> {
            roleService.onSignalUsersInRoomAttributesUpdated(updateKeys, oldAttributes, attributes, editor);
        });
        signalingPlugin.addInvitationListener(new ZegoUIKitSignalingPluginInvitationListener() {

            @Override
            public void onInvitationReceived(ZegoUIKitUser inviter, int type, String data) {
                invitationService.onInvitationReceived(inviter, type, data);
            }

            @Override
            public void onInvitationTimeout(ZegoUIKitUser inviter, String data) {
                invitationService.onInvitationTimeout(inviter, data);
            }

            @Override
            public void onInvitationResponseTimeout(List<ZegoUIKitUser> invitees, String data) {
                invitationService.onInvitationResponseTimeout(invitees, data);
            }

            @Override
            public void onInvitationAccepted(ZegoUIKitUser invitee, String data) {
                invitationService.onInvitationAccepted(invitee, data);
            }

            @Override
            public void onInvitationRefused(ZegoUIKitUser invitee, String data) {
                invitationService.onInvitationRefused(invitee, data);
            }

            @Override
            public void onInvitationCanceled(ZegoUIKitUser inviter, String data) {
                invitationService.onInvitationCancelled(inviter, data);
            }
        });
        ZegoUIKit.addRoomPropertyUpdateListener(new ZegoRoomPropertyUpdateListener() {
            @Override
            public void onRoomPropertyUpdated(String key, String oldValue, String newValue) {
                seatService.onRTCRoomPropertyUpdated(key, oldValue, newValue);
            }

            @Override
            public void onRoomPropertiesFullUpdated(List<String> updateKeys, Map<String, String> oldProperties,
                Map<String, String> properties) {

            }
        });
    }

    public ZegoInnerText getInnerText() {
        return innerText;
    }

    public void leaveRoom() {
        seatService.leaveRoom();
        roleService.leaveRoom();
        invitationService.leaveRoom();
        innerText = null;
    }

    public void setPrebuiltUICallBack(PrebuiltUICallBack uiCallBack) {
        this.uiCallBack = uiCallBack;
    }

    public void showTopTips(String tips, boolean green) {
        if (uiCallBack != null) {
            uiCallBack.showTopTips(tips, green);
        }
    }
}

