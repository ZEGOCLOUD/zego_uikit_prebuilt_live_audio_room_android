package com.zegocloud.uikit.prebuilt.liveaudioroom.core;

import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.Objects;

public class AudioRoomSeat {

    public int seatIndex = 0;
    public int rowIndex = 0;
    public int columnIndex = 0;
    private ZegoUIKitUser lastUser;
    private ZegoUIKitUser currentUser;

    public boolean isNotEmpty() {
        return currentUser != null;
    }

    public boolean isEmpty() {
        return currentUser == null;
    }

    public boolean isSeatChanged() {
        return !Objects.equals(currentUser, lastUser);
    }

    public boolean isTakenByUser(ZegoUIKitUser uiKitUser) {
        return uiKitUser.equals(currentUser);
    }

    public ZegoUIKitUser getUser() {
        return currentUser;
    }

    public ZegoUIKitUser getLastUser() {
        return lastUser;
    }

    public void setUser(ZegoUIKitUser uiKitUser) {
        lastUser = currentUser;
        currentUser = uiKitUser;
    }
}
