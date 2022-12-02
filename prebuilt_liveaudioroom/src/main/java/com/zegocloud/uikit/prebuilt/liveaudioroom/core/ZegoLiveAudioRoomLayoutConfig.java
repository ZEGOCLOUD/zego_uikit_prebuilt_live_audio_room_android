package com.zegocloud.uikit.prebuilt.liveaudioroom.core;

import java.util.Arrays;
import java.util.List;

public class ZegoLiveAudioRoomLayoutConfig {

    public List<ZegoLiveAudioRoomLayoutRowConfig> rowConfigs = Arrays.asList(
        new ZegoLiveAudioRoomLayoutRowConfig(4, ZegoLiveAudioRoomLayoutAlignment.SPACE_AROUND),
        new ZegoLiveAudioRoomLayoutRowConfig(4, ZegoLiveAudioRoomLayoutAlignment.SPACE_AROUND));
    public int rowSpacing = 0;
}
