package com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service;

import android.text.TextUtils;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.AudioRoomSeat;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoLiveAudioRoomLayoutConfig;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoLiveAudioRoomLayoutRowConfig;
import com.zegocloud.uikit.prebuilt.liveaudioroom.listener.ZegoSeatsChangedListener;
import com.zegocloud.uikit.prebuilt.liveaudioroom.listener.ZegoSeatsClosedListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitSignalingPluginRoomAttributesOperatedCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitSignalingPluginRoomPropertyOperatedCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public class SeatService {

    private boolean batchOperation = false;
    private ZegoSeatsChangedListener seatChangedListener;
    private List<ZegoSeatsClosedListener> seatsLockedListenerList = new ArrayList<>();
    private Map<String, String> uikitRoomProperties = new HashMap<>();

    private List<AudioRoomSeat> audioRoomSeatList = new ArrayList<>();

    public void init(ZegoLiveAudioRoomLayoutConfig layoutConfig) {
        audioRoomSeatList.clear();
        for (int rowIndex = 0; rowIndex < layoutConfig.rowConfigs.size(); rowIndex++) {
            ZegoLiveAudioRoomLayoutRowConfig rowConfig = layoutConfig.rowConfigs.get(rowIndex);
            for (int columnIndex = 0; columnIndex < rowConfig.count; columnIndex++) {
                AudioRoomSeat audioRoomSeat = new AudioRoomSeat();
                audioRoomSeat.rowIndex = rowIndex;
                audioRoomSeat.columnIndex = columnIndex;
                audioRoomSeat.seatIndex = audioRoomSeatList.size();
                audioRoomSeatList.add(audioRoomSeat);
            }
        }
    }

    public List<AudioRoomSeat> getAudioRoomSeatList() {
        return audioRoomSeatList;
    }

    public int getSeatCount() {
        return audioRoomSeatList.size();
    }

    public int findFirstAvailableSeatIndex() {
        int firstEmptyIndex = -1;
        for (int i = 0; i < audioRoomSeatList.size(); i++) {
            if (audioRoomSeatList.get(i).isEmpty()) {
                firstEmptyIndex = i;
                break;
            }
        }
        return firstEmptyIndex;
    }

    public AudioRoomSeat findUserRoomSeat(String userID) {
        AudioRoomSeat seat = null;
        for (int i = 0; i < audioRoomSeatList.size(); i++) {
            AudioRoomSeat audioRoomSeat = audioRoomSeatList.get(i);
            if (audioRoomSeat.isNotEmpty() && Objects.equals(userID, audioRoomSeat.getUser().userID)) {
                seat = audioRoomSeat;
                break;
            }
        }
        return seat;
    }

    public int findMyRoomSeatIndex() {
        ZegoUIKitUser localUser = ZegoUIKit.getLocalUser();
        AudioRoomSeat userRoomSeat = findUserRoomSeat(localUser.userID);
        if (userRoomSeat != null) {
            return userRoomSeat.seatIndex;
        } else {
            return -1;
        }
    }

    public AudioRoomSeat tryGetAudioRoomSeat(int seatIndex) {
        if (seatIndex < 0 || seatIndex >= audioRoomSeatList.size()) {
            return null;
        }
        return audioRoomSeatList.get(seatIndex);
    }

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
            ZegoUIKit.getSignalingPlugin()
                .endRoomPropertiesBatchOperation(new ZegoUIKitSignalingPluginRoomPropertyOperatedCallback() {
                    @Override
                    public void onSignalingPluginRoomPropertyOperated(int errorCode, String errorMessage,
                        List<String> errorKeys) {
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

    public void makeSeatEmpty(int seatIndex, ZegoUIKitSignalingPluginRoomAttributesOperatedCallback callback) {
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

    public void removeSpeakerFromSeat(String userID, ZegoUIKitSignalingPluginRoomAttributesOperatedCallback callback) {
        if (ZegoUIKit.getLocalUser() == null) {
            return;
        }
        for (Entry<String, String> entry : ZegoUIKit.getSignalingPlugin().getRoomProperties().entrySet()) {
            String seatIndex = entry.getKey();
            String seatUser = entry.getValue();
            if (Objects.equals(userID, seatUser)) {
                List<String> list = Collections.singletonList(String.valueOf(seatIndex));
                ZegoUIKit.getSignalingPlugin()
                    .deleteRoomProperties(list, true, (errorCode, errorMessage, errorKeys) -> {
                        if (callback != null) {
                            callback.onSignalingPluginRoomAttributesOperated(errorCode, errorMessage, errorKeys);
                        }
                    });
            }
        }
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

    public void setSeatChangedListener(ZegoSeatsChangedListener seatChangedListener) {
        this.seatChangedListener = seatChangedListener;
    }

    public void onSignalRoomPropertiesFullUpdated(List<String> updateKeys, HashMap<String, String> oldProperties,
        HashMap<String, String> properties) {

        for (AudioRoomSeat audioRoomSeat : audioRoomSeatList) {
            String userID = properties.get(String.valueOf(audioRoomSeat.seatIndex));
            if (TextUtils.isEmpty(userID)) {
                audioRoomSeat.setUser(null);
            } else {
                ZegoUIKitUser uiKitUser = ZegoUIKit.getUser(userID);
                if (uiKitUser == null) {
                    uiKitUser = new ZegoUIKitUser(userID);
                }
                audioRoomSeat.setUser(uiKitUser);
            }
        }

        Map<Integer, ZegoUIKitUser> takenSeats = new HashMap<>();
        List<Integer> unTakenSeats = new ArrayList<>();
        for (AudioRoomSeat audioRoomSeat : audioRoomSeatList) {
            if (audioRoomSeat.isEmpty()) {
                unTakenSeats.add(audioRoomSeat.seatIndex);
            } else {
                takenSeats.put(audioRoomSeat.seatIndex, audioRoomSeat.getUser());
            }
        }

        if (seatChangedListener != null) {
            seatChangedListener.onSeatsChanged(takenSeats, unTakenSeats);
        }
    }

    public void leaveRoom() {
        batchOperation = false;
        audioRoomSeatList.clear();
        seatChangedListener = null;
        uikitRoomProperties.clear();
        seatsLockedListenerList.clear();
    }

    public void addSeatsLockedListener(ZegoSeatsClosedListener seatsLockedListener) {
        this.seatsLockedListenerList.add(seatsLockedListener);
    }

    public void removeSeatsLockedListener(ZegoSeatsClosedListener seatsLockedListener) {
        seatsLockedListenerList.remove(seatsLockedListener);
    }

    public boolean isSeatLocked() {
        return "1".equals(uikitRoomProperties.get("lockseat"));
    }

    public void lockSeat(boolean lock) {
        if (lock) {
            ZegoUIKit.setRoomProperty("lockseat", "1");
        } else {
            ZegoUIKit.setRoomProperty("lockseat", "0");
            InvitationService invitationService = LiveAudioRoomManager.getInstance().invitationService;
            invitationService.refuseAllRequest();
            invitationService.cancelMyInvitations();
        }
    }

    public boolean hasLockSeat() {
        return uikitRoomProperties.containsKey("lockseat");
    }

    private static final String TAG = "SeatService";

    public void onRTCRoomPropertyUpdated(String key, String oldValue, String newValue) {
        uikitRoomProperties.put(key, newValue);
        if ("lockseat".equals(key)) {
            if ("1".equals(newValue)) {
                for (ZegoSeatsClosedListener seatsLockedListener : seatsLockedListenerList) {
                    if (seatsLockedListener != null) {
                        seatsLockedListener.onSeatsClosed();
                    }
                }
            } else if ("0".equals(newValue)) {
                for (ZegoSeatsClosedListener seatsLockedListener : seatsLockedListenerList) {
                    if (seatsLockedListener != null) {
                        seatsLockedListener.onSeatsOpened();
                    }
                }
            }
        }
    }
}
