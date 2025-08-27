package com.zegocloud.uikit.prebuilt.liveaudioroom.core;

public class ZegoPlayStreamBufferIntervalRange {

    public int minBufferInterval;
    public int maxBufferInterval;

    public ZegoPlayStreamBufferIntervalRange() {
        minBufferInterval = 0;
        maxBufferInterval = 4000;
    }

    public ZegoPlayStreamBufferIntervalRange(int minBufferInterval, int maxBufferInterval) {
        this.minBufferInterval = minBufferInterval;
        this.maxBufferInterval = maxBufferInterval;
    }
}
