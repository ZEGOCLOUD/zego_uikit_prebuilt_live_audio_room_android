package com.zegocloud.uikit.prebuilt.liveaudioroom.core;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import com.zegocloud.uikit.components.audiovideo.ZegoAudioVideoView;
import com.zegocloud.uikit.components.audiovideocontainer.Size;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoAudioVideoViewConfig;
import com.zegocloud.uikit.components.audiovideo.ZegoAvatarAlignment;
import com.zegocloud.uikit.prebuilt.liveaudioroom.databinding.ItemSeatBinding;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.ZegoAudioForegroundView;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.utils.Utils;

public class AudioRoomSeatView extends FrameLayout {

    private ItemSeatBinding binding;
    private AudioRoomSeat audioRoomSeat;
    private ZegoAudioVideoView audioVideoView;

    public AudioRoomSeatView(@NonNull Context context, AudioRoomSeat audioRoomSeat) {
        super(context);
        this.audioRoomSeat = audioRoomSeat;
        initView();
    }

    private void initView() {
        binding = ItemSeatBinding.inflate(LayoutInflater.from(getContext()), this, true);
        audioVideoView = new ZegoAudioVideoView(getContext());
        int width = Utils.dp2px(54, getContext().getResources().getDisplayMetrics());
        int height = Utils.dp2px(54, getContext().getResources().getDisplayMetrics());
        audioVideoView.setAvatarSize(new Size(width, height));
        audioVideoView.setAvatarAlignment(ZegoAvatarAlignment.START);
        binding.seatEmpty.setVisibility(VISIBLE);
    }

    public void addUserToSeat(ZegoUIKitUser uiKitUser) {
        audioVideoView.setUserID(uiKitUser.userID);
        addView(audioVideoView, new FrameLayout.LayoutParams(-1, -1));
        binding.seatEmpty.setVisibility(GONE);
    }

    public void removeUserFromSeat() {
        audioVideoView.setUserID("");
        removeView(audioVideoView);
        binding.seatEmpty.setVisibility(VISIBLE);
    }

    public void setSeatConfig(ZegoLiveAudioRoomSeatConfig seatConfig) {
        if (seatConfig == null) {
            return;
        }
        if (seatConfig.foregroundViewProvider != null) {
            audioVideoView.setForegroundViewProvider(seatConfig.foregroundViewProvider);
        } else {
            audioVideoView.setForegroundViewProvider((parent, uiKitUser) -> {
                return new ZegoAudioForegroundView(parent.getContext(), uiKitUser);
            });
        }
        ZegoAudioVideoViewConfig audioVideoViewConfig = new ZegoAudioVideoViewConfig();
        audioVideoViewConfig.showSoundWavesInAudioMode = seatConfig.showSoundWavesInAudioMode;
        audioVideoView.setAudioVideoConfig(audioVideoViewConfig);
        if (seatConfig.backgroundColor != 0) {
            setBackgroundColor(seatConfig.backgroundColor);
        }
        if (seatConfig.backgroundImage != null) {
            setBackground(seatConfig.backgroundImage);
        }
    }
}
