package com.zegocloud.uikit.prebuilt.liveaudioroom.core.cohost;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.plugin.invitation.components.ZegoCancelInvitationButton;
import com.zegocloud.uikit.prebuilt.liveaudioroom.R;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service.LiveAudioRoomManager;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.utils.Utils;
import java.util.HashMap;
import java.util.Map;

public class ZegoCancelRequestCoHostButton extends ZegoCancelInvitationButton {

    public ZegoCancelRequestCoHostButton(@NonNull Context context) {
        super(context);
    }

    public ZegoCancelRequestCoHostButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private PluginCallbackListener callbackListener;

    @Override
    protected void initView() {
        setBackgroundResource(R.drawable.liveaudioroom_bg_cohost_btn);
        ZegoTranslationText translationText = LiveAudioRoomManager.getInstance().getTranslationText();
        if (translationText != null) {
            setText(translationText.cancelTheTakeSeatApplicationButton);
        }
        setTextColor(Color.WHITE);
        setTextSize(13);
        setGravity(Gravity.CENTER);
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        setPadding(Utils.dp2px(14, displayMetrics), 0, Utils.dp2px(16, displayMetrics), 0);
        setCompoundDrawablePadding(Utils.dp2px(6, displayMetrics));
        setCompoundDrawablesWithIntrinsicBounds(R.drawable.liveaudioroom_bottombar_cohost, 0, 0, 0);
        setOnClickListener(null);
    }

    @Override
    protected void invokedWhenClick() {
        String hostUserID = LiveAudioRoomManager.getInstance().roleService.getHostUserID();
        ZegoUIKitUser hostUser = ZegoUIKit.getUser(hostUserID);
        if (TextUtils.isEmpty(hostUserID) || hostUser == null) {
            if (callbackListener != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("code", -999);
                callbackListener.callback(map);
            }
            return;
        }
        if (!invitees.contains(hostUserID)) {
            invitees.add(hostUserID);
        }
        LiveAudioRoomManager.getInstance().invitationService.cancelInvitation(invitees, new PluginCallbackListener() {
            @Override
            public void callback(Map<String, Object> result) {
                if (callbackListener != null) {
                    callbackListener.callback(result);
                }
            }
        });
    }

    public void setRequestCallbackListener(PluginCallbackListener callbackListener) {
        this.callbackListener = callbackListener;
    }
}
