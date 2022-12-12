package com.zegocloud.uikit.prebuilt.liveaudioroom.internal;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.prebuilt.liveaudioroom.databinding.LayoutSeatForegroundBinding;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.RoleService.RoleChangedListener;
import com.zegocloud.uikit.service.defines.ZegoMicrophoneStateChangeListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.Objects;

public class ZegoAudioForegroundView extends FrameLayout {

    private LayoutSeatForegroundBinding binding;
    private ZegoUIKitUser userInfo;
    private RoleChangedListener roleChangedListener;
    private ZegoMicrophoneStateChangeListener microphoneStateChangeListener;

    public ZegoAudioForegroundView(@NonNull Context context, ZegoUIKitUser userInfo) {
        super(context);
        this.userInfo = userInfo;
        initView();
    }

    public ZegoAudioForegroundView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ZegoAudioForegroundView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        binding = LayoutSeatForegroundBinding.inflate(LayoutInflater.from(getContext()), this, true);
        setUserInfo(userInfo);
        roleChangedListener = (userID, after) -> {
            if (Objects.equals(userInfo.userID, userID)) {
                update(userInfo);
            }
        };
        microphoneStateChangeListener = (uiKitUser, isOn) -> {
            if (Objects.equals(userInfo.userID, uiKitUser.userID)) {
                userInfo.isMicOpen = isOn;
                update(userInfo);
            }
        };
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ZegoUIKit.addMicrophoneStateListener(microphoneStateChangeListener);
        LiveAudioRoomManager.getInstance().roleService.addRoleChangedListener(roleChangedListener);
        update(userInfo);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ZegoUIKit.removeMicrophoneStateListener(microphoneStateChangeListener);
        LiveAudioRoomManager.getInstance().roleService.removeRoleChangedListener(roleChangedListener);
    }

    public void setUserInfo(ZegoUIKitUser userInfo) {
        this.userInfo = userInfo;
        update(userInfo);
    }

    private void update(ZegoUIKitUser userInfo) {
        if (userInfo != null) {
            boolean isHost = LiveAudioRoomManager.getInstance().roleService.isUserHost(userInfo.userID);
            if (binding != null) {
                binding.foregroundIconHost.setVisibility(isHost ? View.VISIBLE : View.GONE);
            }
            if (binding != null) {
                binding.foregroundUserName.setText(userInfo.userName);
            }
            if (binding != null) {
                binding.foregroundAvatarContentMic.setVisibility(userInfo.isMicOpen ? GONE : VISIBLE);
            }
        }
    }
}
