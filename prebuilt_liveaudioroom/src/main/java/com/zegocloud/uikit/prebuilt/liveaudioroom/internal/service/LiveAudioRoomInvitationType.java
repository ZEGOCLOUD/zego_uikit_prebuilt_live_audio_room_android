package com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service;

public enum LiveAudioRoomInvitationType {
    REQUEST_TAKE_SEAT(2), INVITE_TO_SEAT(3);

    private int value;

    LiveAudioRoomInvitationType(int var3) {
        this.value = var3;
    }

    public int getValue() {
        return this.value;
    }

    public static LiveAudioRoomInvitationType getInvitationType(int type) {
        try {
            if (REQUEST_TAKE_SEAT.value == type) {
                return REQUEST_TAKE_SEAT;
            } else if (INVITE_TO_SEAT.value == type) {
                return INVITE_TO_SEAT;
            } else {
                return REQUEST_TAKE_SEAT;
            }
        } catch (Exception var2) {
            throw new RuntimeException("The enumeration cannot be found");
        }
    }

}
