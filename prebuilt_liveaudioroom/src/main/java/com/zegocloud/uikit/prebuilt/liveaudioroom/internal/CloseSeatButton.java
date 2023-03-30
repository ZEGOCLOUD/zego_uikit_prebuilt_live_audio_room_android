package com.zegocloud.uikit.prebuilt.liveaudioroom.internal;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.components.common.ZegoButton;
import com.zegocloud.uikit.prebuilt.liveaudioroom.R;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service.LiveAudioRoomManager;
import com.zegocloud.uikit.service.defines.ZegoRoomPropertyUpdateListener;
import java.util.List;
import java.util.Map;

public class CloseSeatButton extends ZegoButton {

    private ZegoRoomPropertyUpdateListener roomPropertyUpdateListener;

    public CloseSeatButton(@NonNull Context context) {
        super(context);
    }

    public CloseSeatButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CloseSeatButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView() {
        super.initView();
        setImageResource(R.drawable.liveaudioroom_btn_lock_seat, R.drawable.liveaudioroom_btn_unlock_seat);

        roomPropertyUpdateListener = new ZegoRoomPropertyUpdateListener() {
            @Override
            public void onRoomPropertyUpdated(String key, String oldValue, String newValue) {
                if ("lockseat".equals(key)) {
                    if ("1".equals(newValue)) {
                        open();
                    } else if ("0".equals(newValue)) {
                        close();
                    }
                }
            }

            @Override
            public void onRoomPropertiesFullUpdated(List<String> updateKeys, Map<String, String> oldProperties,
                Map<String, String> properties) {

            }
        };

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ZegoUIKit.addRoomPropertyUpdateListener(roomPropertyUpdateListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ZegoUIKit.removeRoomPropertyUpdateListener(roomPropertyUpdateListener);
    }


    @Override
    protected void afterClick() {
        super.afterClick();
        toggle();
        LiveAudioRoomManager.getInstance().seatService.lockSeat(isOpen());
    }
}
