package com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service;

import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.common.IZegoUIKitSignalingPlugin;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.AudioRoomSeat;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoLiveAudioRoomRole;
import com.zegocloud.uikit.service.defines.ZegoUIKitPluginCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.service.defines.ZegoUserInRoomAttributesInfo;
import com.zegocloud.uikit.service.defines.ZegoUsersInRoomAttributesQueriedCallback;
import com.zegocloud.uikit.service.defines.ZegoUsersInRoomAttributesQueryConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RoleService {

    private List<RoleChangedListener> roleChangedListenerList = new ArrayList<>();
    private Map<String, ZegoLiveAudioRoomRole> userRoleMap = new HashMap<>();

    public void init() {
        saveUserAttribute(ZegoUIKit.getLocalUser().userID, new HashMap<>());
    }

    public void queryUserInRoomAttribute() {
        IZegoUIKitSignalingPlugin signalingPlugin = ZegoUIKit.getSignalingPlugin();
        ZegoUsersInRoomAttributesQueryConfig queryConfig = new ZegoUsersInRoomAttributesQueryConfig();
        queryConfig.setCount(100);
        signalingPlugin.queryUsersInRoomAttributes(queryConfig, new ZegoUsersInRoomAttributesQueriedCallback() {
            @Override
            public void onUsersInRoomAttributesQueried(List<ZegoUserInRoomAttributesInfo> attributes, String nextFlag,
                int errorCode, String errorMessage) {
                if (errorCode == 0) {
                    for (ZegoUserInRoomAttributesInfo attribute : attributes) {
                        saveUserAttribute(attribute.getUserID(), attribute.getAttributes());
                    }
                }
            }
        });
    }

    private void saveUserAttribute(String userID, HashMap<String, String> attribute) {
        if (attribute != null && attribute.containsKey("role")) {
            ZegoLiveAudioRoomRole userNewRole = ZegoLiveAudioRoomRole.get(attribute.get("role"));
            setUserRoleAndNotify(userID, userNewRole);
        }
    }

    private void setUserRoleAndNotify(String userID, ZegoLiveAudioRoomRole audioRoomRole) {
        ZegoLiveAudioRoomRole userOldRole = getUserRole(userID);
        if (userOldRole == ZegoLiveAudioRoomRole.HOST && audioRoomRole == ZegoLiveAudioRoomRole.SPEAKER) {
            return;
        }

        userRoleMap.put(userID, audioRoomRole);
        if (userOldRole != audioRoomRole) {
            if (ZegoUIKit.getUser(userID) != null) {
                for (RoleChangedListener roleChangedListener : roleChangedListenerList) {
                    roleChangedListener.onRoleChanged(userID, userOldRole, audioRoomRole);
                }
            }
        }
    }

    private ZegoLiveAudioRoomRole getUserRole(String userID) {
        ZegoLiveAudioRoomRole userRole = userRoleMap.get(userID);
        if (userRole == null) {
            userRole = ZegoLiveAudioRoomRole.AUDIENCE;
            userRoleMap.put(userID, userRole);
        }
        return userRole;
    }

    void onSignalRoomPropertiesFullUpdated(List<String> updateKeys, HashMap<String, String> oldProperties,
        HashMap<String, String> properties) {

        SeatService seatService = LiveAudioRoomManager.getInstance().seatService;
        List<AudioRoomSeat> audioRoomSeatList = seatService.getAudioRoomSeatList();
        for (AudioRoomSeat audioRoomSeat : audioRoomSeatList) {
            if (audioRoomSeat.isSeatChanged()) {
                ZegoUIKitUser seatUser = audioRoomSeat.getUser();
                if (seatUser != null) {
                    if (!isUserHost(seatUser.userID)) {
                        setUserRoleAndNotify(seatUser.userID, ZegoLiveAudioRoomRole.SPEAKER);
                    }
                } else {
                    ZegoUIKitUser lastUser = audioRoomSeat.getLastUser();
                    if (lastUser != null) {
                        if (seatService.findUserRoomSeat(lastUser.userID) == null) {
                            if (ZegoUIKit.getUser(lastUser.userID) != null) {
                                setUserRoleAndNotify(lastUser.userID, ZegoLiveAudioRoomRole.AUDIENCE);
                            }
                        }

                    }
                }
            }
        }
    }

    void onSignalUsersInRoomAttributesUpdated(List<String> updateKeys, List<ZegoUserInRoomAttributesInfo> oldAttributes,
        List<ZegoUserInRoomAttributesInfo> attributes, ZegoUIKitUser editor) {
        for (ZegoUserInRoomAttributesInfo attribute : attributes) {
            saveUserAttribute(attribute.getUserID(), attribute.getAttributes());
        }
    }

    public boolean isUserSpeaker(String userID) {
        SeatService seatService = LiveAudioRoomManager.getInstance().seatService;
        AudioRoomSeat userRoomSeat = seatService.findUserRoomSeat(userID);
        if (userRoomSeat == null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isUserHost(String userID) {
        return getUserRole(userID) == ZegoLiveAudioRoomRole.HOST;
    }

    public boolean isLocalUserHost() {
        if (ZegoUIKit.getLocalUser() == null) {
            return false;
        }
        return isUserHost(ZegoUIKit.getLocalUser().userID);
    }

    public String getHostUserID() {
        for (Entry<String, ZegoLiveAudioRoomRole> entry : userRoleMap.entrySet()) {
            if (entry.getValue() == ZegoLiveAudioRoomRole.HOST) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void onUserLeaveRoom(String userID) {
        userRoleMap.remove(userID);
    }

    public void setLocalUserHost(ZegoUIKitPluginCallback callback) {
        if (ZegoUIKit.getLocalUser() == null) {
            return;
        }
        List<String> userIDs = Collections.singletonList(ZegoUIKit.getLocalUser().userID);
        setUserRoleToServer(userIDs, String.valueOf(ZegoLiveAudioRoomRole.HOST.value()), callback);
    }

    public void removeLocalUserHost(ZegoUIKitPluginCallback callback) {
        if (ZegoUIKit.getLocalUser() == null) {
            return;
        }
        List<String> userIDs = Collections.singletonList(ZegoUIKit.getLocalUser().userID);
        setUserRoleToServer(userIDs, "", callback);
    }

    private void setUserRoleToServer(List<String> userIDs, String value, ZegoUIKitPluginCallback callback) {
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
        userRoleMap.clear();
    }

    public interface RoleChangedListener {

        void onRoleChanged(String userID, ZegoLiveAudioRoomRole before, ZegoLiveAudioRoomRole after);
    }
}
