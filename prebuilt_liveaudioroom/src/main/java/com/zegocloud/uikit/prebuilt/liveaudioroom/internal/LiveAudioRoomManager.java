package com.zegocloud.uikit.prebuilt.liveaudioroom.internal;

import android.content.Context;
import android.util.Log;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.common.IZegoUIKitSignalingPlugin;
import com.zegocloud.uikit.prebuilt.liveaudioroom.ZegoUIKitPrebuiltLiveAudioRoomFragment;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoTranslationText;
import com.zegocloud.uikit.service.defines.ZegoUIKitSignalingPluginRoomPropertyUpdateListener;
import java.util.HashMap;
import java.util.List;

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
    Context context;
    private ZegoTranslationText translationText;
    private PrebuiltUICallBack uiCallBack;

    public void init(Context context) {
        this.context = context.getApplicationContext();
        roleService.init();
        IZegoUIKitSignalingPlugin signalingPlugin = ZegoUIKit.getSignalingPlugin();
        signalingPlugin.addRoomPropertyUpdateListener(new ZegoUIKitSignalingPluginRoomPropertyUpdateListener() {
            @Override
            public void onRoomPropertyUpdated(String key, String oldValue, String newValue) {
            }

            @Override
            public void onRoomPropertiesFullUpdated(List<String> updateKeys, HashMap<String, String> oldProperties,
                HashMap<String, String> properties) {
                Log.d(ZegoUIKit.TAG,
                    "onRoomPropertiesFullUpdated() called with: updateKeys = [" + updateKeys + "], oldProperties = ["
                        + oldProperties + "], properties = [" + properties + "]");
                roleService.onRoomPropertiesFullUpdated(updateKeys, oldProperties, properties);
                seatService.onRoomPropertiesFullUpdated(updateKeys, oldProperties, properties);

            }
        });
    }

    public void setTranslationText(ZegoTranslationText translationText) {
        this.translationText = translationText;
    }

    public ZegoTranslationText getTranslationText() {
        return translationText;
    }

    public void leaveRoom() {
        seatService.leaveRoom();
        roleService.leaveRoom();
        translationText = null;
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

