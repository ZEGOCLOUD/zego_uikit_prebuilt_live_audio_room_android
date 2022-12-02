package com.zegocloud.uikit.prebuilt.liveaudioroom.internal;

import android.text.TextUtils;
import android.util.Log;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.common.IZegoUIKitSignalingPlugin;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoLiveAudioRoomRole;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoTranslationText;
import com.zegocloud.uikit.service.defines.ZegoUIKitPluginCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitSignalingPluginRoomPropertyUpdateListener;
import com.zegocloud.uikit.service.defines.ZegoUserInRoomAttributesInfo;
import com.zegocloud.uikit.service.defines.ZegoUsersInRoomAttributesQueriedCallback;
import com.zegocloud.uikit.service.defines.ZegoUsersInRoomAttributesQueryConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    private ZegoTranslationText translationText;
    private List<RoleChangedListener> roleChangedListenerList = new ArrayList<>();
    private SeatChangedListener seatChangedListener;
    private Map<String, HashMap<String, String>> userAttrs = new HashMap<>();

    public void init() {
        userAttrs.put(ZegoUIKit.getLocalUser().userID, new HashMap<>());
        IZegoUIKitSignalingPlugin signalingPlugin = ZegoUIKit.getSignalingPlugin();
        ZegoUsersInRoomAttributesQueryConfig queryConfig = new ZegoUsersInRoomAttributesQueryConfig();
        queryConfig.setCount(100);
        signalingPlugin.queryUsersInRoomAttributes(queryConfig, new ZegoUsersInRoomAttributesQueriedCallback() {
            @Override
            public void onUsersInRoomAttributesQueried(List<ZegoUserInRoomAttributesInfo> attributes, String nextFlag,
                int errorCode, String errorMessage) {
                Log.d(ZegoUIKit.TAG,
                    "queryUsersInRoomAttributes() called with: attributes = [" + attributes + "], nextFlag = ["
                        + nextFlag + "], errorCode = [" + errorCode + "], errorMessage = [" + errorMessage + "]");
                if (errorCode == 0) {
                    for (ZegoUserInRoomAttributesInfo attribute : attributes) {
                        ZegoLiveAudioRoomRole userRole = ZegoLiveAudioRoomRole.get(
                            attribute.getAttributes().get("role"));
                        if (Objects.equals(attribute.getUserID(), ZegoUIKit.getLocalUser().userID)) {
                            setLocalUserRole(userRole);
                        } else {
                            userAttrs.put(attribute.getUserID(), attribute.getAttributes());
                            for (RoleChangedListener roleChangedListener : roleChangedListenerList) {
                                roleChangedListener.onRoleChanged(attribute.getUserID(), userRole);
                            }
                        }

                    }
                }
            }
        });
        signalingPlugin.addUsersInRoomAttributesUpdateListener((updateKeys, oldAttributes, attributes, editor) -> {
            Log.d(ZegoUIKit.TAG,
                "onUsersInRoomAttributesUpdated() called with: updateKeys = [" + updateKeys + "], oldAttributes = ["
                    + oldAttributes + "], attributes = [" + attributes + "], editor = [" + editor + "]");
            for (ZegoUserInRoomAttributesInfo attribute : attributes) {
                ZegoLiveAudioRoomRole userRole = ZegoLiveAudioRoomRole.get(attribute.getAttributes().get("role"));
                if (Objects.equals(attribute.getUserID(), ZegoUIKit.getLocalUser().userID)) {
                    setLocalUserRole(userRole);
                } else {
                    userAttrs.put(attribute.getUserID(), attribute.getAttributes());
                    for (RoleChangedListener roleChangedListener : roleChangedListenerList) {
                        roleChangedListener.onRoleChanged(attribute.getUserID(), userRole);
                    }
                }
            }
        });
        signalingPlugin.addRoomPropertyUpdateListener(new ZegoUIKitSignalingPluginRoomPropertyUpdateListener() {
            @Override
            public void onRoomPropertyUpdated(String key, String oldValue, String newValue) {
                Log.d(ZegoUIKit.TAG, "onRoomPropertyUpdated() called with: key = [" + key + "], oldValue = [" + oldValue
                    + "], newValue = [" + newValue + "]");
            }

            @Override
            public void onRoomPropertiesFullUpdated(List<String> updateKeys, HashMap<String, String> oldProperties,
                HashMap<String, String> properties) {
                Log.d(ZegoUIKit.TAG,
                    "onRoomPropertiesFullUpdated() called with: updateKeys = [" + updateKeys + "], oldProperties = ["
                        + oldProperties + "], properties = [" + properties + "]");
                for (String key : updateKeys) {
                    String oldValue = oldProperties.get(key);
                    String newValue = properties.get(key);
                    if (!TextUtils.isEmpty(oldValue)) {
                        if (!properties.containsValue(oldValue)) {
                            for (RoleChangedListener roleChangedListener : roleChangedListenerList) {
                                if (!isUserHost(oldValue)) {
                                    roleChangedListener.onRoleChanged(oldValue, ZegoLiveAudioRoomRole.AUDIENCE);
                                } else {
                                    if (Objects.equals(oldValue, ZegoUIKit.getLocalUser().userID)) {
                                        LiveAudioRoomManager.getInstance().removeUserHost((errorCode, errorMessage) -> {
                                        });
                                    }
                                }
                            }
                        }
                    }
                    if (!TextUtils.isEmpty(newValue)) {
                        if (!oldProperties.containsValue(newValue)) {
                            for (RoleChangedListener roleChangedListener : roleChangedListenerList) {
                                if (!isUserHost(newValue)) {
                                    roleChangedListener.onRoleChanged(newValue, ZegoLiveAudioRoomRole.SPEAKER);
                                }
                            }
                        }
                    }
                }
                if (seatChangedListener != null) {
                    seatChangedListener.onRoomPropertiesFullUpdated(updateKeys, oldProperties, properties);
                }
            }
        });
    }

    public void setLocalUserRole(ZegoLiveAudioRoomRole role) {
        HashMap<String, String> hashMap = userAttrs.get(ZegoUIKit.getLocalUser().userID);
        ZegoLiveAudioRoomRole before = ZegoLiveAudioRoomRole.get(hashMap.get("role"));
        hashMap.put("role", String.valueOf(role.value()));
        if (before != role) {
            for (RoleChangedListener roleChangedListener : roleChangedListenerList) {
                roleChangedListener.onRoleChanged(ZegoUIKit.getLocalUser().userID, role);
            }
        }
    }

    public boolean isLocalUserHost() {
        return isUserHost(ZegoUIKit.getLocalUser().userID);
    }

    public boolean isUserSpeaker(String userID) {
        if (isUserHost(userID)) {
            return false;
        }
        HashMap<String, String> roomProperties = ZegoUIKit.getSignalingPlugin().getRoomProperties();
        return roomProperties.containsValue(userID);
    }

    public boolean isUserHost(String userID) {
        HashMap<String, String> hashMap = userAttrs.get(userID);
        if (hashMap == null) {
            return false;
        }
        return ZegoLiveAudioRoomRole.get(hashMap.get("role")) == ZegoLiveAudioRoomRole.HOST;
    }

    public void setTranslationText(ZegoTranslationText translationText) {
        this.translationText = translationText;
    }

    public ZegoTranslationText getTranslationText() {
        return translationText;
    }

    public void setUserHost(ZegoUIKitPluginCallback callback) {
        List<String> userIDs = Collections.singletonList(ZegoUIKit.getLocalUser().userID);
        setUserRole(userIDs, String.valueOf(ZegoLiveAudioRoomRole.HOST.value()), callback);
    }

    public void removeUserHost(ZegoUIKitPluginCallback callback) {
        List<String> userIDs = Collections.singletonList(ZegoUIKit.getLocalUser().userID);
        setUserRole(userIDs, "", callback);
    }

    public void setUserRole(List<String> userIDs, String value, ZegoUIKitPluginCallback callback) {
        ZegoUIKit.getSignalingPlugin().setUsersInRoomAttributes("role", value, userIDs, (errorCode, errorMessage) -> {
            if (callback != null) {
                callback.onResult(errorCode, errorMessage);
            }
        });
    }

    public void addRoleChangedListener(RoleChangedListener roleChangedListener) {
        roleChangedListenerList.add(roleChangedListener);
    }

    public void removeRoleChangedListener(RoleChangedListener roleChangedListener) {
        roleChangedListenerList.remove(roleChangedListener);
    }

    public void setSeatChangedListener(SeatChangedListener seatChangedListener) {
        this.seatChangedListener = seatChangedListener;
    }

    public void clear() {
        roleChangedListenerList.clear();
        userAttrs.clear();
        translationText = null;
    }

    public interface RoleChangedListener {

        void onRoleChanged(String userID, ZegoLiveAudioRoomRole after);
    }

    public interface SeatChangedListener {

        void onRoomPropertiesFullUpdated(List<String> updateKeys, HashMap<String, String> oldProperties,
            HashMap<String, String> properties);
    }
}

