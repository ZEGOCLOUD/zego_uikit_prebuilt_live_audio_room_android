package com.zegocloud.uikit.prebuilt.liveaudioroom.core;

import android.view.ViewGroup;
import com.zegocloud.uikit.components.audiovideo.ZegoBaseAudioVideoForegroundView;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

public interface ZegoLiveAudioRoomSeatForegroundViewProvider {

    ZegoBaseAudioVideoForegroundView getForegroundView(ViewGroup parent, ZegoUIKitUser uiKitUser, int seatIndex);
}
