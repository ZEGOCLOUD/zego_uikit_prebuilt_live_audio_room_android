package com.zegocloud.uikit.prebuilt.liveaudioroom.internal;

import android.text.TextUtils;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.common.IZegoUIKitSignalingPlugin;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoLiveAudioRoomRole;
import com.zegocloud.uikit.service.defines.ZegoUIKitPluginCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitSignalingPluginUsersInRoomAttributesUpdateListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.service.defines.ZegoUserInRoomAttributesInfo;
import com.zegocloud.uikit.service.defines.ZegoUsersInRoomAttributesQueriedCallback;
import com.zegocloud.uikit.service.defines.ZegoUsersInRoomAttributesQueryConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RoleService {

    private List<RoleChangedListener> roleChangedListenerList = new ArrayList<>();
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
                if (errorCode == 0) {
                    for (ZegoUserInRoomAttributesInfo attribute : attributes) {
                        ZegoLiveAudioRoomRole userRole = ZegoLiveAudioRoomRole.get(
                            attribute.getAttributes().get("role"));
                        userAttrs.put(attribute.getUserID(), attribute.getAttributes());
                        for (RoleChangedListener roleChangedListener : roleChangedListenerList) {
                            roleChangedListener.onRoleChanged(attribute.getUserID(), userRole);
                        }
                    }
                }
            }
        });
        signalingPlugin.addUsersInRoomAttributesUpdateListener(
            new ZegoUIKitSignalingPluginUsersInRoomAttributesUpdateListener() {
                @Override
                public void onUsersInRoomAttributesUpdated(List<String> updateKeys,
                    List<ZegoUserInRoomAttributesInfo> oldAttributes, List<ZegoUserInRoomAttributesInfo> attributes,
                    ZegoUIKitUser editor) {

                    userAttrs.clear();
                    for (ZegoUserInRoomAttributesInfo attribute : attributes) {
                        ZegoLiveAudioRoomRole newUserRole = ZegoLiveAudioRoomRole.get(
                            attribute.getAttributes().get("role"));
                        userAttrs.put(attribute.getUserID(), attribute.getAttributes());
                        for (RoleChangedListener roleChangedListener : roleChangedListenerList) {
                            roleChangedListener.onRoleChanged(attribute.getUserID(), newUserRole);
                        }
                    }
                    // attribute delete.
                    for (ZegoUserInRoomAttributesInfo oldAttribute : oldAttributes) {
                        if (!userAttrs.containsKey(oldAttribute.getUserID())) {
                            for (RoleChangedListener roleChangedListener : roleChangedListenerList) {
                                roleChangedListener.onRoleChanged(oldAttribute.getUserID(),
                                    ZegoLiveAudioRoomRole.AUDIENCE);
                            }
                        }
                    }
                }
            });
    }

    public void onRoomPropertiesFullUpdated(List<String> updateKeys, HashMap<String, String> oldProperties,
        HashMap<String, String> properties) {
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
                                removeLocalUserHost((errorCode, errorMessage) -> {
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
    }

    public boolean isLocalUserHost() {
        if (ZegoUIKit.getLocalUser() == null) {
            return false;
        }
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

    public void setLocalUserHost(ZegoUIKitPluginCallback callback) {
        if (ZegoUIKit.getLocalUser() == null) {
            return;
        }
        List<String> userIDs = Collections.singletonList(ZegoUIKit.getLocalUser().userID);
        setUserRole(userIDs, String.valueOf(ZegoLiveAudioRoomRole.HOST.value()), callback);
    }

    public void removeLocalUserHost(ZegoUIKitPluginCallback callback) {
        if (ZegoUIKit.getLocalUser() == null) {
            return;
        }
        List<String> userIDs = Collections.singletonList(ZegoUIKit.getLocalUser().userID);
        setUserRole(userIDs, "", callback);
    }

    private void setUserRole(List<String> userIDs, String value, ZegoUIKitPluginCallback callback) {
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

    void leaveRoom() {
        roleChangedListenerList.clear();
        userAttrs.clear();
    }

    public interface RoleChangedListener {

        void onRoleChanged(String userID, ZegoLiveAudioRoomRole after);
    }
}
