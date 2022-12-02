package com.zegocloud.uikit.prebuilt.liveaudioroom.internal;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoLiveAudioRoomRole;
import com.zegocloud.uikit.prebuilt.liveaudioroom.databinding.LayoutSeatForegroundBinding;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.LiveAudioRoomManager.RoleChangedListener;
import com.zegocloud.uikit.service.defines.ZegoMicrophoneStateChangeListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.Objects;

public class ZegoAudioForegroundView extends FrameLayout {

    private ZegoMicrophoneStateChangeListener microphoneStateChangeListener;
    private LayoutSeatForegroundBinding binding;
    private ZegoUIKitUser userInfo;
    private RoleChangedListener roleChangedListener;

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
        microphoneStateChangeListener = new ZegoMicrophoneStateChangeListener() {
            @Override
            public void onMicrophoneOn(ZegoUIKitUser uiKitUser, boolean isOn) {
                if (Objects.equals(uiKitUser, userInfo)) {
                    if (isOn) {
                        showMicrophone(false);
                    } else {
                        showMicrophone(true);
                    }
                }
            }
        };
        roleChangedListener = new RoleChangedListener() {
            @Override
            public void onRoleChanged(String userID, ZegoLiveAudioRoomRole after) {
                if (Objects.equals(userInfo.userID, userID)) {
                    showHostTag(after == ZegoLiveAudioRoomRole.HOST);
                }
            }
        };
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ZegoUIKit.addMicrophoneStateListener(microphoneStateChangeListener);
        LiveAudioRoomManager.getInstance().addRoleChangedListener(roleChangedListener);
        if (userInfo != null) {
            showHostTag(LiveAudioRoomManager.getInstance().isUserHost(userInfo.userID));
        }
        if (userInfo != null && !ZegoUIKit.isMicrophoneOn(userInfo.userID)) {
            showMicrophone(true);
        } else {
            showMicrophone(false);
        }

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ZegoUIKit.removeMicrophoneStateListener(microphoneStateChangeListener);
        LiveAudioRoomManager.getInstance().removeRoleChangedListener(roleChangedListener);
    }

    public void setUserInfo(ZegoUIKitUser userInfo) {
        this.userInfo = userInfo;
        if (userInfo != null) {
            boolean isHost = Objects.equals(ZegoUIKit.getRoomProperties().get("host"), userInfo.userID);
            showHostTag(isHost);
            showMicrophone(!userInfo.isMicOpen);
            if (binding != null) {
                binding.seatName.setText(userInfo.userName);
            }
        }
    }

    public void showMicrophone(boolean showMicStatusOnView) {
        if (binding != null) {
            binding.seatIconMic.setVisibility(showMicStatusOnView ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public void showHostTag(boolean showHostSignage) {
        if (binding != null) {
            binding.seatIconHost.setVisibility(showHostSignage ? View.VISIBLE : View.GONE);
        }
    }
}
