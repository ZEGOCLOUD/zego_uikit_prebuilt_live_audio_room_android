package com.zegocloud.uikit.prebuilt.liveaudioroom.core;

public class ZegoLiveAudioRoomLayoutRowConfig {
    public int count;
    public int seatSpacing;
    public ZegoLiveAudioRoomLayoutAlignment alignment;

    public ZegoLiveAudioRoomLayoutRowConfig(int count,
        ZegoLiveAudioRoomLayoutAlignment alignment) {
        this.count = count;
        this.alignment = alignment;
    }

    public ZegoLiveAudioRoomLayoutRowConfig(int count, int seatSpacing,
        ZegoLiveAudioRoomLayoutAlignment alignment) {
        this.count = count;
        this.seatSpacing = seatSpacing;
        this.alignment = alignment;
    }

    public ZegoLiveAudioRoomLayoutRowConfig() {
    }
}
