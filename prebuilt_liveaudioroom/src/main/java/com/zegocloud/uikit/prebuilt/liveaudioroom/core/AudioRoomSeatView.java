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
import com.zegocloud.uikit.prebuilt.liveaudioroom.R;
import com.zegocloud.uikit.prebuilt.liveaudioroom.databinding.LiveaudioroomItemSeatBinding;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.ZegoAudioVideoForegroundView;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.utils.Utils;

public class AudioRoomSeatView extends FrameLayout {

    private LiveaudioroomItemSeatBinding binding;
    private ZegoAudioVideoView audioVideoView;
    private AudioRoomSeat audioRoomSeat;
    private boolean isLocked = false;
    private ZegoLiveAudioRoomSeatConfig seatConfig;

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

    public void setUser(ZegoUIKitUser uiKitUser) {
        if (uiKitUser != null) {
            addUserToSeat(uiKitUser);
        } else {
            removeUserFromSeat();
        }
    }

    private void addUserToSeat(ZegoUIKitUser uiKitUser) {
        audioVideoView.setUserID(uiKitUser.userID);
        if (binding.seatAudiovideoPlaceHolder.getChildCount() == 0) {
            binding.seatAudiovideoPlaceHolder.addView(audioVideoView, new FrameLayout.LayoutParams(-1, -1));
        }
        binding.seatAvatarContentEmpty.setVisibility(GONE);
    }

    private void removeUserFromSeat() {
        audioVideoView.setUserID("");
        if (binding.seatAudiovideoPlaceHolder.getChildCount() != 0) {
            binding.seatAudiovideoPlaceHolder.removeView(audioVideoView);
        }
        binding.seatAvatarContentEmpty.setVisibility(VISIBLE);
    }

    public void setLock(boolean lock) {
        isLocked = lock;
        setLockInner(lock);
    }

    public boolean isLocked() {
        return isLocked;
    }

    public AudioRoomSeat getAudioRoomSeat() {
        return audioRoomSeat;
    }

    private void setLockInner(boolean lock) {
        if (binding != null) {
            if (lock) {
                if (seatConfig != null && seatConfig.closeIcon != null) {
                    binding.seatStateLock.setImageDrawable(seatConfig.closeIcon);
                } else {
                    binding.seatStateLock.setImageResource(R.drawable.audioroom_icon_lock_seat);
                }
            } else {
                if (seatConfig != null && seatConfig.openIcon != null) {
                    binding.seatStateLock.setImageDrawable(seatConfig.openIcon);
                } else {
                    binding.seatStateLock.setImageResource(R.drawable.audioroom_icon_on_stage);
                }
            }
        }
    }

    public void setSeatConfig(ZegoLiveAudioRoomSeatConfig seatConfig) {
        this.seatConfig = seatConfig;
        if (seatConfig == null) {
            return;
        }
        if (seatConfig.foregroundViewProvider != null) {
            audioVideoView.setForegroundViewProvider((parent, uiKitUser) -> {
                return seatConfig.foregroundViewProvider.getForegroundView(parent, uiKitUser, audioRoomSeat.seatIndex);
            });
        } else {
            audioVideoView.setForegroundViewProvider((parent, uiKitUser) -> {
                return new ZegoAudioVideoForegroundView(parent.getContext(), uiKitUser.userID);
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
