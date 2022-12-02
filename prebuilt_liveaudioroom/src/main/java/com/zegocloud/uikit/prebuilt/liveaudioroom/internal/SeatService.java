package com.zegocloud.uikit.prebuilt.liveaudioroom.internal;

import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoLiveAudioRoomRole;
import com.zegocloud.uikit.service.defines.ZegoUIKitPluginCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitSignalingPluginRoomAttributesOperatedCallback;
import java.util.Collections;
import java.util.List;

public class SeatService {

    public void takeSeat(int seatIndex, ZegoUIKitSignalingPluginRoomAttributesOperatedCallback callback) {
        String key = String.valueOf(seatIndex);
        String value = ZegoUIKit.getLocalUser().userID;
        ZegoUIKit.getSignalingPlugin()
            .updateRoomProperty(key, value, true, true, true, (errorCode, errorMessage, errorKeys) -> {
                if (callback != null) {
                    callback.onSignalingPluginRoomAttributesOperated(errorCode, errorMessage, errorKeys);
                }
            });
    }

    public void switchSeat(int fromSeatIndex, int toSeatIndex) {
        ZegoUIKit.getSignalingPlugin().beginRoomPropertiesBatchOperation(true, false, false);
        tryTakeSeat(toSeatIndex, null);
        removeUserFromSeat(fromSeatIndex, null);
        ZegoUIKit.getSignalingPlugin().endRoomPropertiesBatchOperation(new ZegoUIKitPluginCallback() {
            @Override
            public void onResult(int errorCode, String errorMessage) {

            }
        });
    }

    public void tryTakeSeat(int seatIndex, ZegoUIKitSignalingPluginRoomAttributesOperatedCallback callback) {
        String key = String.valueOf(seatIndex);
        String value = ZegoUIKit.getLocalUser().userID;
        ZegoUIKit.getSignalingPlugin().updateRoomProperty(key, value, true, false, false, (errorCode, errorMessage, errorKeys) -> {
                if (callback != null) {
                    callback.onSignalingPluginRoomAttributesOperated(errorCode, errorMessage, errorKeys);
                }
            });
    }

    public void leaveSeat(int seatIndex, ZegoUIKitSignalingPluginRoomAttributesOperatedCallback callback) {
        List<String> list = Collections.singletonList(String.valueOf(seatIndex));
        ZegoUIKit.getSignalingPlugin().deleteRoomProperties(list, true, (errorCode, errorMessage, errorKeys) -> {
            if (errorCode == 0) {
                LiveAudioRoomManager.getInstance().setLocalUserRole(ZegoLiveAudioRoomRole.AUDIENCE);
            }
            if (callback != null) {
                callback.onSignalingPluginRoomAttributesOperated(errorCode, errorMessage, errorKeys);
            }
        });
    }

    public void removeUserFromSeat(int seatIndex, ZegoUIKitSignalingPluginRoomAttributesOperatedCallback callback) {
        List<String> list = Collections.singletonList(String.valueOf(seatIndex));
        ZegoUIKit.getSignalingPlugin().deleteRoomProperties(list, true, (errorCode, errorMessage, errorKeys) -> {
            if (callback != null) {
                callback.onSignalingPluginRoomAttributesOperated(errorCode, errorMessage, errorKeys);
            }
        });
    }
}
