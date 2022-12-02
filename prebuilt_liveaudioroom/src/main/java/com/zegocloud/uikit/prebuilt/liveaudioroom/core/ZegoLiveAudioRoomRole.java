package com.zegocloud.uikit.prebuilt.liveaudioroom.core;

public enum ZegoLiveAudioRoomRole {
    HOST(0), SPEAKER(1), AUDIENCE(2);


    private int value;

    ZegoLiveAudioRoomRole(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    public static ZegoLiveAudioRoomRole get(String value) {
        if (String.valueOf(ZegoLiveAudioRoomRole.HOST.value).equals(value)) {
            return ZegoLiveAudioRoomRole.HOST;
        } else if (String.valueOf(ZegoLiveAudioRoomRole.SPEAKER.value).equals(value)) {
            return ZegoLiveAudioRoomRole.SPEAKER;
        } else {
            return ZegoLiveAudioRoomRole.AUDIENCE;
        }
    }

}
