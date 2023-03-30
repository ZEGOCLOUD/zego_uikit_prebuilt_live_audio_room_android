package com.zegocloud.uikit.prebuilt.liveaudioroom.core.cohost;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.adapter.utils.GenericUtils;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.plugin.invitation.components.ZegoStartInvitationButton;
import com.zegocloud.uikit.prebuilt.liveaudioroom.R;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoInnerText;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service.LiveAudioRoomInvitationType;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service.LiveAudioRoomManager;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.utils.Utils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZegoRequestTakeSeatButton extends ZegoStartInvitationButton {

    public ZegoRequestTakeSeatButton(@NonNull Context context) {
        super(context);
        initView();
    }

    public ZegoRequestTakeSeatButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private PluginCallbackListener callbackListener;

    @Override
    protected void initView() {
        super.initView();
        type = LiveAudioRoomInvitationType.REQUEST_TAKE_SEAT.getValue();
        setBackgroundResource(R.drawable.liveaudioroom_bg_cohost_btn);
        ZegoInnerText translationText = LiveAudioRoomManager.getInstance().getInnerText();
        if (translationText != null) {
            setText(translationText.applyToTakeSeatButton);
        }
        setTextColor(Color.WHITE);
        setTextSize(13);
        setGravity(Gravity.CENTER);
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        setPadding(Utils.dp2px(14, displayMetrics), 0, Utils.dp2px(16, displayMetrics), 0);
        setCompoundDrawablePadding(Utils.dp2px(6, displayMetrics));
        setCompoundDrawablesWithIntrinsicBounds(R.drawable.liveaudioroom_bottombar_cohost, 0, 0, 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    protected void invokedWhenClick() {
        String hostUserID = LiveAudioRoomManager.getInstance().roleService.getHostUserID();
        ZegoUIKitUser hostUser = ZegoUIKit.getUser(hostUserID);
        if (TextUtils.isEmpty(hostUserID) || hostUser == null) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", -1);
            if (callbackListener != null) {
                callbackListener.callback(map);
            }
            return;
        }
        if (!invitees.contains(hostUser)) {
            invitees.add(hostUser);
        }
        List<String> idList = GenericUtils.map(invitees, zegoUIKitUser -> zegoUIKitUser.userID);
        LiveAudioRoomManager.getInstance().invitationService.sendInvitation(idList, timeout, type, data,
            callbackListener);
    }

    public void setRequestCallbackListener(PluginCallbackListener callbackListener) {
        this.callbackListener = callbackListener;
    }
}
