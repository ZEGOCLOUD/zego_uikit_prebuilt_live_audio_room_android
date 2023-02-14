package com.zegocloud.uikit.prebuilt.liveaudioroom.internal;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoLiveAudioRoomRole;
import com.zegocloud.uikit.prebuilt.liveaudioroom.databinding.LiveaudioroomLayoutSeatForegroundBinding;
import com.zegocloud.uikit.components.audiovideo.ZegoBaseAudioVideoForegroundView;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.HashMap;

public class ZegoAudioVideoForegroundView extends ZegoBaseAudioVideoForegroundView {

    private LiveaudioroomLayoutSeatForegroundBinding binding;

    public ZegoAudioVideoForegroundView(@NonNull Context context, String userID) {
        super(context, userID);
    }

    public ZegoAudioVideoForegroundView(@NonNull Context context, @Nullable AttributeSet attrs, String userID) {
        super(context, attrs, userID);
    }

    @Override
    protected void onForegroundViewCreated(ZegoUIKitUser uiKitUser) {
        super.onForegroundViewCreated(uiKitUser);

        binding = LiveaudioroomLayoutSeatForegroundBinding.inflate(LayoutInflater.from(getContext()), this, true);
        update(uiKitUser);
    }

    @Override
    protected void onMicrophoneStateChanged(boolean isMicrophoneOn) {
        super.onMicrophoneStateChanged(isMicrophoneOn);
        updateUserMicrophone(isMicrophoneOn);
    }

    @Override
    protected void onInRoomAttributesUpdated(HashMap<String, String> inRoomAttributes) {
        super.onInRoomAttributesUpdated(inRoomAttributes);
        updateUserInRoomAttributes(inRoomAttributes);
    }

    private void update(ZegoUIKitUser uiKitUser) {
        if (uiKitUser != null) {
            updateUserInRoomAttributes(uiKitUser.inRoomAttributes);
            updateUserMicrophone(uiKitUser.isMicrophoneOn);
            if (binding != null) {
                binding.foregroundUserName.setText(uiKitUser.userName);
            }
        }
    }

    private void updateUserMicrophone(boolean isMicrophoneOn) {
        if (binding != null) {
            binding.foregroundAvatarContentMic.setVisibility(isMicrophoneOn ? GONE : VISIBLE);
        }
    }

    private void updateUserInRoomAttributes(HashMap<String, String> inRoomAttributes) {
        boolean isHost;
        if (inRoomAttributes != null) {
            isHost = ZegoLiveAudioRoomRole.get(inRoomAttributes.get("role")) == ZegoLiveAudioRoomRole.HOST;
        } else {
            isHost = false;
        }
        if (binding != null) {
            binding.foregroundIconHost.setVisibility(isHost ? View.VISIBLE : View.GONE);
        }
    }
}
