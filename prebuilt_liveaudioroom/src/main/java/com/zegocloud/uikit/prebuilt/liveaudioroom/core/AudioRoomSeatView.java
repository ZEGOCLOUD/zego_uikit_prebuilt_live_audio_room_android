package com.zegocloud.uikit.prebuilt.liveaudioroom.core;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import com.google.android.flexbox.FlexboxLayout;
import com.zegocloud.uikit.components.audiovideo.ZegoAudioVideoView;
import com.zegocloud.uikit.components.audiovideo.ZegoAvatarAlignment;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoAudioVideoViewConfig;
import com.zegocloud.uikit.prebuilt.liveaudioroom.databinding.LiveaudioroomItemSeatBinding;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.ZegoAudioForegroundView;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.utils.Utils;

public class AudioRoomSeatView extends FrameLayout {

    private LiveaudioroomItemSeatBinding binding;
    private ZegoAudioVideoView audioVideoView;
    private AudioRoomSeat audioRoomSeat;

    public AudioRoomSeatView(@NonNull Context context, AudioRoomSeat audioRoomSeat) {
        super(context);
        this.audioRoomSeat = audioRoomSeat;
        initView();
    }

    private void initView() {
        binding = LiveaudioroomItemSeatBinding.inflate(LayoutInflater.from(getContext()), this, true);
        int width = Utils.dp2px(80, getContext().getResources().getDisplayMetrics());
        int height = Utils.dp2px(80, getContext().getResources().getDisplayMetrics());
        FlexboxLayout.LayoutParams flexChildParams = new FlexboxLayout.LayoutParams(width, height);
        setLayoutParams(flexChildParams);

        audioVideoView = new ZegoAudioVideoView(getContext());
        int avatarSize = Utils.dp2px(66, getContext().getResources().getDisplayMetrics());
        int contentSize = Utils.dp2px(54, getContext().getResources().getDisplayMetrics());
        audioVideoView.setAvatarSize(avatarSize, contentSize);
        audioVideoView.setAvatarAlignment(ZegoAvatarAlignment.START);
        audioVideoView.setSoundWaveColor(Color.parseColor("#0055FF"));
        binding.seatAvatarContentEmpty.setVisibility(VISIBLE);
    }

    public void addUserToSeat(ZegoUIKitUser uiKitUser) {
        audioVideoView.setUserID(uiKitUser.userID);
        binding.seatAudiovideoPlaceHolder.addView(audioVideoView, new FrameLayout.LayoutParams(-1, -1));
        binding.seatAvatarContentEmpty.setVisibility(GONE);
    }

    public void removeUserFromSeat() {
        audioVideoView.setUserID("");
        binding.seatAudiovideoPlaceHolder.removeView(audioVideoView);
        binding.seatAvatarContentEmpty.setVisibility(VISIBLE);
    }

    public void setSeatConfig(ZegoLiveAudioRoomSeatConfig seatConfig) {
        if (seatConfig == null) {
            return;
        }
        if (seatConfig.foregroundViewProvider != null) {
            audioVideoView.setForegroundViewProvider((parent, uiKitUser) -> {
                return seatConfig.foregroundViewProvider.getForegroundView(parent, uiKitUser, audioRoomSeat.seatIndex);
            });
        } else {
            audioVideoView.setForegroundViewProvider((parent, uiKitUser) -> {
                return new ZegoAudioForegroundView(parent.getContext(), uiKitUser);
            });
        }
        ZegoAudioVideoViewConfig audioVideoViewConfig = new ZegoAudioVideoViewConfig();
        audioVideoViewConfig.showSoundWavesInAudioMode = seatConfig.showSoundWaveInAudioMode;
        audioVideoView.setAudioVideoConfig(audioVideoViewConfig);
        if (seatConfig.backgroundColor != 0) {
            setBackgroundColor(seatConfig.backgroundColor);
        }
        if (seatConfig.backgroundImage != null) {
            setBackground(seatConfig.backgroundImage);
        }
    }

    public void updateUser() {
        String userID = audioVideoView.getUserID();
        audioVideoView.setUserID("");
        audioVideoView.setUserID(userID);
    }
}
