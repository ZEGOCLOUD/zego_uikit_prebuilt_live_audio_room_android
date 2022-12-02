package com.zegocloud.uikit.prebuilt.liveaudioroom.internal;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.zegocloud.uikit.prebuilt.liveaudioroom.R;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoDialogInfo;

public class ZegoExitRoomButton extends AppCompatImageView {


    private ZegoDialogInfo confirmDialogInfo;
    private LeaveLiveAudioRoomListener leaveLiveListener;

    public ZegoExitRoomButton(@NonNull Context context) {
        super(context);
        initView();
    }

    public ZegoExitRoomButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public void initView() {
        setOnClickListener(null);
        setImageResource(R.drawable.audioroom_icon_close);
        setOnClickListener(v -> {
            invokeWhenClick();
        });
    }

    public void setConfirmDialogInfo(ZegoDialogInfo info) {
        confirmDialogInfo = info;
    }

    private void invokeWhenClick() {
        boolean isActivity = getContext() instanceof Activity;
        if (isActivity && confirmDialogInfo != null) {
            showQuitDialog(confirmDialogInfo);
        } else {
            if (leaveLiveListener != null) {
                leaveLiveListener.onLeaveLiveAudioRoom();
            }
        }
    }

    public void setLeaveLiveListener(LeaveLiveAudioRoomListener listener) {
        this.leaveLiveListener = listener;
    }

    private void showQuitDialog(ZegoDialogInfo dialogInfo) {
        new ConfirmDialog.Builder(getContext()).setTitle(dialogInfo.title).setMessage(dialogInfo.message)
                .setPositiveButton(dialogInfo.confirmButtonName, (dialog, which) -> {
                    if (leaveLiveListener != null) {
                        leaveLiveListener.onLeaveLiveAudioRoom();
                    }
                    dialog.dismiss();
                }).setNegativeButton(dialogInfo.cancelButtonName, (dialog, which) -> {
            dialog.dismiss();
        }).build().show();
    }

}
