package com.zegocloud.uikit.prebuilt.liveaudioroom.core;

import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

public class AudioRoomSeat {

    public int seatIndex = 0;
    public int rowIndex = 0;
    public int columnIndex = 0;
    public ZegoUIKitUser uiKitUser;

    @Override
    public String toString() {
        return "AudioRoomSeat{" +
            "seatIndex=" + seatIndex +
            ", rowIndex=" + rowIndex +
            ", columnIndex=" + columnIndex +
            ", uiKitUser=" + uiKitUser +
            '}';
    }
}
