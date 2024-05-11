package com.zegocloud.uikit.prebuilt.liveaudioroom.internal;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.FragmentActivity;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.uikit.components.audiovideo.ZegoToggleMicrophoneButton;
import com.zegocloud.uikit.prebuilt.liveaudioroom.R;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service.LiveAudioRoomManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionMicrophoneButton extends ZegoToggleMicrophoneButton {

    private GestureDetectorCompat gestureDetectorCompat;

    public PermissionMicrophoneButton(@NonNull Context context) {
        super(context);
        gestureDetectorCompat = new GestureDetectorCompat(context, new SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (getContext() instanceof FragmentActivity) {
                    requestPermissionIfNeeded((allGranted, grantedList, deniedList) -> {
                        if (allGranted) {
                            callOnClick();
                        }
                    });
                }
                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetectorCompat.onTouchEvent(event);
    }

    private void requestPermissionIfNeeded(RequestCallback requestCallback) {
        List<String> permissions = Arrays.asList(permission.RECORD_AUDIO);

        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
            }
        }
        if (allGranted) {
            requestCallback.onResult(true, permissions, new ArrayList<>());
            return;
        }
        ZegoTranslationText translationText = LiveAudioRoomManager.getInstance().getTranslationText();
        PermissionX.init((FragmentActivity) getContext()).permissions(permission.RECORD_AUDIO)
            .onExplainRequestReason((scope, deniedList) -> {
                String ok = "";
                String settings = "";
                String cancel = "";
                String explainMic = "";
                String settingsMic = "";
                if (translationText != null) {
                    ok = translationText.ok;
                    settings = translationText.settings;
                    cancel = translationText.cancel;
                    explainMic = translationText.explainMic;
                    settingsMic = translationText.settingsMic;
                }
                scope.showRequestReasonDialog(deniedList, explainMic, ok);
            }).onForwardToSettings((scope, deniedList) -> {
                String ok = "";
                String settings = "";
                String cancel = "";
                String explainMic = "";
                String settingsMic = "";
                if (translationText != null) {
                    ok = translationText.ok;
                    settings = translationText.settings;
                    cancel = translationText.cancel;
                    explainMic = translationText.explainMic;
                    settingsMic = translationText.settingsMic;
                }
                scope.showForwardToSettingsDialog(deniedList, settingsMic, settings, cancel);
            }).request(new RequestCallback() {
                @Override
                public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                    @NonNull List<String> deniedList) {
                    if (requestCallback != null) {
                        requestCallback.onResult(allGranted, grantedList, deniedList);
                    }
                }
            });
    }
}
