package com.zegocloud.uikit.prebuilt.liveaudioroom.core;

public enum ZegoLiveAudioRoomLayoutAlignment {
    SPACE_AROUND(0), SPACE_BETWEEN(1), SPACE_EVENLY(2), FLEX_START(3), FLEX_END(4), CENTER(5);


    private int value;

    private ZegoLiveAudioRoomLayoutAlignment(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

}
