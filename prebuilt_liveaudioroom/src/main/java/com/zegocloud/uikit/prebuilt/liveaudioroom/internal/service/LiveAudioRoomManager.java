package com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service;

import android.app.Application;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.prebuilt.liveaudioroom.ZegoUIKitPrebuiltLiveAudioRoomConfig;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.PrebuiltUICallBack;
import com.zegocloud.uikit.service.defines.ZegoAudioVideoResourceMode;
import com.zegocloud.uikit.service.defines.ZegoRoomPropertyUpdateListener;
import com.zegocloud.uikit.service.defines.ZegoScenario;
import com.zegocloud.uikit.service.defines.ZegoUIKitCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitPluginCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitSignalingPluginInvitationListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitSignalingPluginRoomPropertyUpdateListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitSignalingPluginUsersInRoomAttributesUpdateListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.service.defines.ZegoUserUpdateListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import timber.log.Timber;

public class LiveAudioRoomManager {

    private ZegoUIKitSignalingPluginRoomPropertyUpdateListener signalRoomPropertyListener;
    private ZegoRoomPropertyUpdateListener rtcRoomPropertyUpdateListener;
    private ZegoUIKitSignalingPluginUsersInRoomAttributesUpdateListener signalUsersInRoomAttributesListener;
    private ZegoUIKitSignalingPluginInvitationListener invitationListener;

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
    private ZegoUserUpdateListener rtcUserUpdateListener;
    private ZegoUIKitPrebuiltLiveAudioRoomConfig prebuiltLiveAudioRoomConfig;

    public void setPrebuiltConfig(ZegoUIKitPrebuiltLiveAudioRoomConfig config) {
        this.prebuiltLiveAudioRoomConfig = config;
        if (config.translationText != null) {
            ZegoUIKit.setLanguage(config.translationText.getLanguage());
        }
    }

    public ZegoUIKitPrebuiltLiveAudioRoomConfig getPrebuiltConfig() {
        return prebuiltLiveAudioRoomConfig;
    }

    private static final String TAG = "LiveAudioRoomManager";

    public void init(Application application, long appID, String appSign) {
        ZegoUIKit.init(application, appID, appSign, ZegoScenario.GENERAL);
        invitationListener = new ZegoUIKitSignalingPluginInvitationListener() {

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
                invitationService.onInvitationCanceled(inviter, data);
            }
        };
    }

    public void loginUser(String userID, String userName) {
        ZegoUIKit.login(userID, userName);

        prebuiltLiveAudioRoomConfig.translationText.copyFromInnerTextIfNotCustomized(
            prebuiltLiveAudioRoomConfig.innerText);

        seatService.init(prebuiltLiveAudioRoomConfig.layoutConfig);
        roleService.init();
    }

    public ZegoTranslationText getTranslationText() {
        return prebuiltLiveAudioRoomConfig.translationText;
    }

    public void joinRoom(String userID, String userName, String roomID, JoinRoomCallback callback) {
        ZegoUIKit.setAudioVideoResourceMode(ZegoAudioVideoResourceMode.RTC_ONLY);
        signalRoomPropertyListener = new ZegoUIKitSignalingPluginRoomPropertyUpdateListener() {
            @Override
            public void onRoomPropertyUpdated(String key, String oldValue, String newValue) {
                Timber.d("onRoomPropertyUpdated() called with: key = [" + key + "], oldValue = [" + oldValue
                    + "], newValue = [" + newValue + "]");
            }

            @Override
            public void onRoomPropertiesFullUpdated(List<String> updateKeys, HashMap<String, String> oldProperties,
                HashMap<String, String> properties) {
                Timber.d(
                    "onRoomPropertiesFullUpdated() called with: updateKeys = [" + updateKeys + "], oldProperties = ["
                        + oldProperties + "], properties = [" + properties + "]");
                seatService.onSignalRoomPropertiesFullUpdated(updateKeys, oldProperties, properties);
                roleService.onSignalRoomPropertiesFullUpdated(updateKeys, oldProperties, properties);

            }
        };
        ZegoUIKit.getSignalingPlugin().addRoomPropertyUpdateListener(signalRoomPropertyListener);

        signalUsersInRoomAttributesListener = (updateKeys, oldAttributes, attributes, editor) -> {
            roleService.onSignalUsersInRoomAttributesUpdated(updateKeys, oldAttributes, attributes, editor);
        };
        ZegoUIKit.getSignalingPlugin().addUsersInRoomAttributesUpdateListener(signalUsersInRoomAttributesListener);

        rtcRoomPropertyUpdateListener = new ZegoRoomPropertyUpdateListener() {
            @Override
            public void onRoomPropertyUpdated(String key, String oldValue, String newValue) {
                seatService.onRTCRoomPropertyUpdated(key, oldValue, newValue);
            }

            @Override
            public void onRoomPropertiesFullUpdated(List<String> updateKeys, Map<String, String> oldProperties,
                Map<String, String> properties) {

            }
        };
        ZegoUIKit.addRoomPropertyUpdateListener(rtcRoomPropertyUpdateListener);

        rtcUserUpdateListener = new ZegoUserUpdateListener() {
            @Override
            public void onUserJoined(List<ZegoUIKitUser> userInfoList) {

            }

            @Override
            public void onUserLeft(List<ZegoUIKitUser> userInfoList) {
                for (ZegoUIKitUser zegoUIKitUser : userInfoList) {
                    LiveAudioRoomManager.getInstance().roleService.onUserLeaveRoom(zegoUIKitUser.userID);
                }
            }
        };
        ZegoUIKit.addUserUpdateListener(rtcUserUpdateListener);
        ZegoUIKit.getSignalingPlugin().addInvitationListener(invitationListener);
        ZegoUIKit.joinRoom(roomID, new ZegoUIKitCallback() {
            @Override
            public void onResult(int errorCode) {
                if (errorCode == 0) {
                    ZegoUIKit.getSignalingPlugin().login(userID, userName, new ZegoUIKitPluginCallback() {
                        @Override
                        public void onResult(int errorCode, String message) {
                            if (errorCode == 0) {
                                ZegoUIKit.getSignalingPlugin().joinRoom(roomID, new ZegoUIKitPluginCallback() {
                                    @Override
                                    public void onResult(int errorCode, String message) {
                                        if (errorCode == 0) {
                                            if (callback != null) {
                                                callback.onJoinRoomSuccess();
                                            }
                                        } else {
                                            if (callback != null) {
                                                callback.onJoinRoomFail();
                                            }
                                            leaveRoom();
                                        }
                                    }
                                });
                            } else {
                                if (callback != null) {
                                    callback.onJoinRoomFail();
                                }
                                leaveRoom();
                            }
                        }
                    });
                } else {
                    if (callback != null) {
                        callback.onJoinRoomFail();
                    }
                    leaveRoom();
                }

            }
        });
    }

    public void leaveRoom() {
        ZegoUIKit.getSignalingPlugin().removeInvitationListener(invitationListener);
        seatService.leaveRoom();
        roleService.leaveRoom();
        invitationService.leaveRoom();
        ZegoUIKit.setAudioVideoResourceMode(ZegoAudioVideoResourceMode.DEFAULT);
        ZegoUIKit.getSignalingPlugin().removeRoomPropertyUpdateListener(signalRoomPropertyListener);
        ZegoUIKit.getSignalingPlugin().removeUsersInRoomAttributesUpdateListener(signalUsersInRoomAttributesListener);
        ZegoUIKit.removeRoomPropertyUpdateListener(rtcRoomPropertyUpdateListener);
        ZegoUIKit.removeUserUpdateListener(rtcUserUpdateListener);

        ZegoUIKit.getSignalingPlugin().leaveRoom(null);
        ZegoUIKit.leaveRoom();
        //        ZegoUIKit.getSignalingPlugin().logout();
        //        ZegoUIKit.logout();
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

