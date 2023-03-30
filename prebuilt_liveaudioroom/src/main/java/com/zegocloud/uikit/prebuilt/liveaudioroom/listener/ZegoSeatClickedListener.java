package com.zegocloud.uikit.prebuilt.liveaudioroom.listener;

import android.view.ViewGroup;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

public interface ZegoSeatClickedListener {

    void onSeatClicked(ViewGroup parent, int position, ZegoUIKitUser user);
}
