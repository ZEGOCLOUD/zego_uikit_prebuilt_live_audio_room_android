package com.zegocloud.uikit.prebuilt.liveaudioroom.internal;

import android.Manifest.permission;
import android.content.Context;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.FragmentActivity;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.uikit.components.audiovideo.ZegoToggleMicrophoneButton;
import com.zegocloud.uikit.prebuilt.liveaudioroom.R;
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
        PermissionX.init((FragmentActivity) getContext()).permissions(permission.RECORD_AUDIO)
            .onExplainRequestReason((scope, deniedList) -> {
                String message = getContext().getString(R.string.permission_explain_mic);
                scope.showRequestReasonDialog(deniedList, message, getContext().getString(R.string.ok));
            }).onForwardToSettings((scope, deniedList) -> {
                String message = getContext().getString(R.string.settings_mic);
                scope.showForwardToSettingsDialog(deniedList, message, getContext().getString(R.string.settings),
                    getContext().getString(R.string.cancel));
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
