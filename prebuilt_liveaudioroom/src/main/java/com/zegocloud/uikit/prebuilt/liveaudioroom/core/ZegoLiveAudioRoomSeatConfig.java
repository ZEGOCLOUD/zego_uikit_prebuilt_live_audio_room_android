package com.zegocloud.uikit.prebuilt.liveaudioroom.core;

import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import com.zegocloud.uikit.prebuilt.liveaudioroom.listener.ZegoSeatClickedListener;

public class ZegoLiveAudioRoomSeatConfig {

    public boolean showSoundWaveInAudioMode = true;
    public transient ZegoLiveAudioRoomSeatForegroundViewProvider foregroundViewProvider;
    public @ColorInt int backgroundColor;
    public Drawable backgroundImage;
    public Drawable openIcon;
    public Drawable closeIcon;
    public transient ZegoSeatClickedListener seatClickedListener;
}
