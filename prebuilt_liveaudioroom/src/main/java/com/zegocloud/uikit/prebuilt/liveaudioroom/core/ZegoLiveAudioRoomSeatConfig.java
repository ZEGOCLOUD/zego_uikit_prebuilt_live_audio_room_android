package com.zegocloud.uikit.prebuilt.liveaudioroom.core;

import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import com.zegocloud.uikit.components.audiovideo.ZegoForegroundViewProvider;

public class ZegoLiveAudioRoomSeatConfig {

    public boolean showSoundWavesInAudioMode = true;
    public transient ZegoForegroundViewProvider foregroundViewProvider;
    public @ColorInt
    int backgroundColor;
    public Drawable backgroundImage;
}
