package com.zegocloud.uikit.prebuilt.liveaudioroom.internal;

import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.service.defines.ZegoUIKitPluginCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitSignalingPluginRoomAttributesOperatedCallback;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SeatService {

    private boolean batchOperation = false;
    private SeatChangedListener seatChangedListener;

    public void takeSeat(int seatIndex, ZegoUIKitSignalingPluginRoomAttributesOperatedCallback callback) {
        if (ZegoUIKit.getLocalUser() == null) {
            return;
        }
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
        if (ZegoUIKit.getLocalUser() == null) {
            return;
        }
        if (!batchOperation) {
            ZegoUIKit.getSignalingPlugin().beginRoomPropertiesBatchOperation(true, false, false);
            batchOperation = true;
            tryTakeSeat(toSeatIndex, null);
            removeUserFromSeat(fromSeatIndex, null);
            ZegoUIKit.getSignalingPlugin().endRoomPropertiesBatchOperation(new ZegoUIKitPluginCallback() {
                @Override
                public void onResult(int errorCode, String errorMessage) {
                    batchOperation = false;
                }
            });
        }
    }

    public void tryTakeSeat(int seatIndex, ZegoUIKitSignalingPluginRoomAttributesOperatedCallback callback) {
        if (ZegoUIKit.getLocalUser() == null) {
            return;
        }
        String key = String.valueOf(seatIndex);
        String value = ZegoUIKit.getLocalUser().userID;
        ZegoUIKit.getSignalingPlugin()
            .updateRoomProperty(key, value, true, false, false, (errorCode, errorMessage, errorKeys) -> {
                if (callback != null) {
                    callback.onSignalingPluginRoomAttributesOperated(errorCode, errorMessage, errorKeys);
                }
            });
    }

    public void leaveSeat(int seatIndex, ZegoUIKitSignalingPluginRoomAttributesOperatedCallback callback) {
        if (ZegoUIKit.getLocalUser() == null) {
            return;
        }
        List<String> list = Collections.singletonList(String.valueOf(seatIndex));
        ZegoUIKit.getSignalingPlugin().deleteRoomProperties(list, true, (errorCode, errorMessage, errorKeys) -> {
            if (callback != null) {
                callback.onSignalingPluginRoomAttributesOperated(errorCode, errorMessage, errorKeys);
            }
        });
    }

    public void removeUserFromSeat(int seatIndex, ZegoUIKitSignalingPluginRoomAttributesOperatedCallback callback) {
        if (ZegoUIKit.getLocalUser() == null) {
            return;
        }
        List<String> list = Collections.singletonList(String.valueOf(seatIndex));
        ZegoUIKit.getSignalingPlugin().deleteRoomProperties(list, true, (errorCode, errorMessage, errorKeys) -> {
            if (callback != null) {
                callback.onSignalingPluginRoomAttributesOperated(errorCode, errorMessage, errorKeys);
            }
        });
    }

    public void setSeatChangedListener(SeatChangedListener seatChangedListener) {
        this.seatChangedListener = seatChangedListener;
    }

    public void onRoomPropertiesFullUpdated(List<String> updateKeys, HashMap<String, String> oldProperties,
        HashMap<String, String> properties) {
        if (seatChangedListener != null) {
            seatChangedListener.onRoomPropertiesFullUpdated(updateKeys, oldProperties, properties);
        }
    }

    public interface SeatChangedListener {

        void onRoomPropertiesFullUpdated(List<String> updateKeys, HashMap<String, String> oldProperties,
            HashMap<String, String> properties);
    }

    public void leaveRoom() {
        batchOperation = false;
    }
}
