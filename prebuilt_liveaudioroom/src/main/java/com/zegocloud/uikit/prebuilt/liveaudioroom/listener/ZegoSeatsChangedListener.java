package com.zegocloud.uikit.prebuilt.liveaudioroom.listener;

import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.List;
import java.util.Map;

public interface ZegoSeatsChangedListener {

    void onSeatsChanged(Map<Integer, ZegoUIKitUser> takenSeats, List<Integer> untakenSeats);
}
