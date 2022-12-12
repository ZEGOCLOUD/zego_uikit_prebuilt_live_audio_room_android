package com.zegocloud.uikit.prebuilt.liveaudioroom.core;

import android.view.View;
import android.view.ViewGroup;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

public interface ZegoLiveAudioRoomSeatForegroundViewProvider {

    View getForegroundView(ViewGroup parent, ZegoUIKitUser uiKitUser, int seatIndex);
}
