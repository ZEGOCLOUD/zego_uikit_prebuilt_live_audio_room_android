package com.zegocloud.uikit.prebuilt.liveaudioroom.components;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.JustifyContent;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.prebuilt.liveaudioroom.R;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.AudioRoomSeat;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.AudioRoomSeatView;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoDialogInfo;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoLiveAudioRoomLayoutAlignment;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoLiveAudioRoomLayoutConfig;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoLiveAudioRoomLayoutRowConfig;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoLiveAudioRoomSeatConfig;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.BottomActionDialog;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.ConfirmDialog;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.LiveAudioRoomManager;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.service.defines.ZegoUserUpdateListener;
import com.zegocloud.uikit.utils.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public class ZegoAudioRoomContainer extends LinearLayout {

    private static final String TAG = "ZegoAudioRoomContainer";

    private List<AudioRoomSeat> audioRoomSeatList = new ArrayList<>();
    private ZegoUserUpdateListener userUpdateListener;
    private ZegoLiveAudioRoomLayoutConfig layoutConfig;
    private ZegoLiveAudioRoomSeatConfig seatConfig;
    private List<Integer> lockSeatIndexesForHost;

    public ZegoAudioRoomContainer(@NonNull Context context) {
        super(context);
        initView();
    }

    public ZegoAudioRoomContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ZegoAudioRoomContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setOrientation(LinearLayout.VERTICAL);
        userUpdateListener = new ZegoUserUpdateListener() {
            @Override
            public void onUserJoined(List<ZegoUIKitUser> userInfoList) {
            }

            @Override
            public void onUserLeft(List<ZegoUIKitUser> userInfoList) {
                for (ZegoUIKitUser uiKitUser : userInfoList) {
                    removeUserFromSeat(uiKitUser.userID);
                }
            }
        };
    }

    private void removeUserFromSeat(int seatIndex) {
        if (seatIndex < 0 || seatIndex >= audioRoomSeatList.size()) {
            return;
        }
        AudioRoomSeat audioRoomSeat = audioRoomSeatList.get(seatIndex);
        removeUserFromSeat(audioRoomSeat);
    }

    private void removeUserFromSeat(String userID) {
        AudioRoomSeat audioRoomSeat = getAudioRoomSeat(userID);
        if (audioRoomSeat != null) {
            removeUserFromSeat(audioRoomSeat);
        }
    }

    private void removeUserFromSeat(AudioRoomSeat audioRoomSeat) {
        if (audioRoomSeat.uiKitUser != null) {
            audioRoomSeat.uiKitUser = null;
            AudioRoomSeatView seatView = getAudioRoomSeatView(audioRoomSeat);
            seatView.removeUserFromSeat();
        }
    }

    private AudioRoomSeat getAudioRoomSeat(String userID) {
        AudioRoomSeat seat = null;
        for (int i = 0; i < audioRoomSeatList.size(); i++) {
            AudioRoomSeat audioRoomSeat = audioRoomSeatList.get(i);
            if (audioRoomSeat.uiKitUser != null && Objects.equals(userID, audioRoomSeat.uiKitUser.userID)) {
                seat = audioRoomSeat;
                break;
            }
        }
        return seat;
    }

    public void addUserToSeat(int seatIndex, ZegoUIKitUser uiKitUser) {
        if (seatIndex < 0 || seatIndex >= audioRoomSeatList.size()) {
            return;
        }
        if (uiKitUser == null) {
            return;
        }
        AudioRoomSeat audioRoomSeat = audioRoomSeatList.get(seatIndex);
        if (audioRoomSeat.uiKitUser == uiKitUser) {
            return;
        }
        if (audioRoomSeat.uiKitUser == null) {
            audioRoomSeat.uiKitUser = uiKitUser;
            AudioRoomSeatView seatView = getAudioRoomSeatView(audioRoomSeat);
            seatView.addUserToSeat(uiKitUser);
        }
    }

    public void addUserToSeat(ZegoUIKitUser uiKitUser) {
        int firstEmptyIndex = -1;
        for (int i = 0; i < audioRoomSeatList.size(); i++) {
            if (audioRoomSeatList.get(i).uiKitUser == null) {
                firstEmptyIndex = i;
                break;
            }
        }
        if (firstEmptyIndex != -1) {
            addUserToSeat(firstEmptyIndex, uiKitUser);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ZegoUIKit.addUserUpdateListener(userUpdateListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ZegoUIKit.removeUserUpdateListener(userUpdateListener);
    }

    private AudioRoomSeatView getAudioRoomSeatView(AudioRoomSeat audioRoomSeat) {
        FlexboxLayout flexboxLayout = (FlexboxLayout) getChildAt(audioRoomSeat.rowIndex);
        return ((AudioRoomSeatView) flexboxLayout.getChildAt(audioRoomSeat.columnIndex));
    }


    public void setLayoutConfig(ZegoLiveAudioRoomLayoutConfig layoutConfig) {
        this.layoutConfig = layoutConfig;
        audioRoomSeatList.clear();

        for (int rowIndex = 0; rowIndex < layoutConfig.rowConfigs.size(); rowIndex++) {
            ZegoLiveAudioRoomLayoutRowConfig rowConfig = layoutConfig.rowConfigs.get(rowIndex);
            FlexboxLayout flexboxLayout = new FlexboxLayout(getContext());
            LayoutParams params = new LayoutParams(-1, -2);
            params.bottomMargin = layoutConfig.rowSpacing;
            addView(flexboxLayout, params);
            for (int columnIndex = 0; columnIndex < rowConfig.count; columnIndex++) {
                AudioRoomSeat audioRoomSeat = new AudioRoomSeat();
                audioRoomSeat.rowIndex = rowIndex;
                audioRoomSeat.columnIndex = columnIndex;
                audioRoomSeat.seatIndex = audioRoomSeatList.size();
                audioRoomSeatList.add(audioRoomSeat);
                AudioRoomSeatView seatView = new AudioRoomSeatView(getContext(), audioRoomSeat);
                seatView.setSeatConfig(seatConfig);
                seatView.setOnClickListener(v -> {
                    onSeatViewClicked(audioRoomSeat);
                });
                int width = Utils.dp2px(74, getContext().getResources().getDisplayMetrics());
                int height = Utils.dp2px(74, getContext().getResources().getDisplayMetrics());
                FlexboxLayout.LayoutParams flexChildParams = new FlexboxLayout.LayoutParams(width, height);
                flexboxLayout.addView(seatView, flexChildParams);
            }

            if (rowConfig.alignment == ZegoLiveAudioRoomLayoutAlignment.SPACE_EVENLY) {
                flexboxLayout.setJustifyContent(JustifyContent.SPACE_EVENLY);
            } else if (rowConfig.alignment == ZegoLiveAudioRoomLayoutAlignment.SPACE_BETWEEN) {
                flexboxLayout.setJustifyContent(JustifyContent.SPACE_BETWEEN);
            } else if (rowConfig.alignment == ZegoLiveAudioRoomLayoutAlignment.SPACE_AROUND) {
                flexboxLayout.setJustifyContent(JustifyContent.SPACE_AROUND);
            } else if (rowConfig.alignment == ZegoLiveAudioRoomLayoutAlignment.CENTER) {
                flexboxLayout.setJustifyContent(JustifyContent.CENTER);
            } else if (rowConfig.alignment == ZegoLiveAudioRoomLayoutAlignment.FLEX_START) {
                flexboxLayout.setJustifyContent(JustifyContent.FLEX_START);
            } else if (rowConfig.alignment == ZegoLiveAudioRoomLayoutAlignment.FLEX_END) {
                flexboxLayout.setJustifyContent(JustifyContent.FLEX_END);
            }
        }
    }

    private int findMySeatIndex() {
        for (AudioRoomSeat audioRoomSeat : audioRoomSeatList) {
            if (Objects.equals(audioRoomSeat.uiKitUser, ZegoUIKit.getLocalUser())) {
                return audioRoomSeat.seatIndex;
            }
        }
        return -1;
    }

    private void onSeatViewClicked(AudioRoomSeat audioRoomSeat) {
        boolean isLocalUserHost = LiveAudioRoomManager.getInstance().isLocalUserHost();
        if (audioRoomSeat.uiKitUser == null) {
            if (isLocalUserHost || lockSeatIndexesForHost.contains(audioRoomSeat.seatIndex)) {
                return;
            }
            int mySeatIndex = findMySeatIndex();
            if (mySeatIndex == -1) {
                showTakeSeatActionDialog(audioRoomSeat);
            } else {
                LiveAudioRoomManager.getInstance().seatService.switchSeat(mySeatIndex, audioRoomSeat.seatIndex);
            }
        } else {
            if (Objects.equals(audioRoomSeat.uiKitUser, ZegoUIKit.getLocalUser())) {
                if (!isLocalUserHost) {
                    showLeaveSeatActionDialog(audioRoomSeat);
                }
            } else {
                if (isLocalUserHost) {
                    showRemoveSeatUserActionDialog(audioRoomSeat);
                }
            }
        }
    }

    public int getSeatCount() {
        return audioRoomSeatList.size();
    }

    private void showTakeSeatActionDialog(AudioRoomSeat audioRoomSeat) {
        List<String> stringList = new ArrayList<>();
        ZegoTranslationText translationText = LiveAudioRoomManager.getInstance().getTranslationText();
        if (translationText != null && translationText.takeSeatMenuDialogButton != null) {
            stringList.add(translationText.takeSeatMenuDialogButton);
        } else {
            stringList.add(getContext().getString(R.string.take_the_seat));
        }
        if (translationText != null && translationText.cancelMenuDialogButton != null) {
            stringList.add(translationText.cancelMenuDialogButton);
        } else {
            stringList.add(getContext().getString(R.string.cancel));
        }
        BottomActionDialog bottomActionDialog = new BottomActionDialog(getContext(), stringList);
        bottomActionDialog.show();
        bottomActionDialog.setOnDialogClickListener((dialog, which) -> {
            if (which == 0) {
                LiveAudioRoomManager.getInstance().seatService.tryTakeSeat(audioRoomSeat.seatIndex,
                    (errorCode, errorMessage, errorKeys) -> {
                        if (errorCode == 0) {
                        }
                    });
            }
            dialog.dismiss();
        });
    }

    private void showLeaveSeatActionDialog(AudioRoomSeat audioRoomSeat) {
        List<String> stringList = new ArrayList<>();
        ZegoTranslationText translationText = LiveAudioRoomManager.getInstance().getTranslationText();
        if (translationText != null && translationText.leaveSeatMenuDialogButton != null) {
            stringList.add(translationText.leaveSeatMenuDialogButton);
        } else {
            stringList.add(getContext().getString(R.string.leave_the_seat));
        }
        if (translationText != null && translationText.cancelMenuDialogButton != null) {
            stringList.add(translationText.cancelMenuDialogButton);
        } else {
            stringList.add(getContext().getString(R.string.cancel));
        }
        BottomActionDialog bottomActionDialog = new BottomActionDialog(getContext(), stringList);
        bottomActionDialog.show();
        bottomActionDialog.setOnDialogClickListener((dialog, which) -> {
            if (which == 0) {
                showLeaveSeatConfirmDialog(audioRoomSeat);
            }
            dialog.dismiss();
        });
    }

    private void showLeaveSeatConfirmDialog(AudioRoomSeat audioRoomSeat) {
        String title = getContext().getString(R.string.leave_the_seat);
        String message = getContext().getString(R.string.leave_the_seat_message);
        String cancelButtonName = getContext().getString(R.string.cancel);
        String confirmButtonName = getContext().getString(R.string.ok);
        ZegoTranslationText translationText = LiveAudioRoomManager.getInstance().getTranslationText();
        if (translationText != null) {
            ZegoDialogInfo dialogInfo = translationText.leaveSeatDialogInfo;
            if (dialogInfo != null && dialogInfo.title != null) {
                title = dialogInfo.title;
            }
            if (dialogInfo != null && dialogInfo.message != null) {
                message = dialogInfo.message;
            }
            if (dialogInfo != null && dialogInfo.cancelButtonName != null) {
                cancelButtonName = dialogInfo.cancelButtonName;
            }
            if (dialogInfo != null && dialogInfo.confirmButtonName != null) {
                confirmButtonName = dialogInfo.confirmButtonName;
            }
        }
        new ConfirmDialog.Builder(getContext()).setTitle(title).setMessage(message)
            .setPositiveButton(confirmButtonName, (dialog, which) -> {
                LiveAudioRoomManager.getInstance().seatService.leaveSeat(audioRoomSeat.seatIndex,
                    (errorCode, errorMessage, errorKeys) -> {
                        if (errorCode == 0) {
                        }
                    });
                dialog.dismiss();
            }).setNegativeButton(cancelButtonName, (dialog, which) -> {
                dialog.dismiss();
            }).build().show();
    }

    private void showRemoveSeatUserActionDialog(AudioRoomSeat audioRoomSeat) {
        List<String> stringList = new ArrayList<>();
        ZegoTranslationText translationText = LiveAudioRoomManager.getInstance().getTranslationText();
        if (translationText != null && translationText.removeSpeakerMenuDialogButton != null) {
            stringList.add(
                String.format(translationText.removeSpeakerMenuDialogButton, audioRoomSeat.uiKitUser.userName));
        } else {
            stringList.add(getContext().getString(R.string.remove_the_seat, audioRoomSeat.uiKitUser.userName));
        }
        if (translationText != null && translationText.cancelMenuDialogButton != null) {
            stringList.add(translationText.cancelMenuDialogButton);
        } else {
            stringList.add(getContext().getString(R.string.cancel));
        }
        BottomActionDialog bottomActionDialog = new BottomActionDialog(getContext(), stringList);
        bottomActionDialog.show();
        bottomActionDialog.setOnDialogClickListener((dialog, which) -> {
            if (which == 0) {
                showRemoveSeatUserConfirmDialog(audioRoomSeat);
            }
            dialog.dismiss();
        });

    }

    private void showRemoveSeatUserConfirmDialog(AudioRoomSeat audioRoomSeat) {
        String userName = audioRoomSeat.uiKitUser.userName;
        String title = getContext().getString(R.string.remove_the_seat_title);
        String message = getContext().getString(R.string.remove_the_seat_message, userName);
        String cancelButtonName = getContext().getString(R.string.cancel);
        String confirmButtonName = getContext().getString(R.string.ok);
        ZegoTranslationText translationText = LiveAudioRoomManager.getInstance().getTranslationText();
        if (translationText != null) {
            ZegoDialogInfo dialogInfo = translationText.removeSpeakerFromSeatDialogInfo;
            if (dialogInfo != null && dialogInfo.title != null) {
                title = dialogInfo.title;
            }
            if (dialogInfo != null && dialogInfo.message != null) {
                message = String.format(dialogInfo.message, userName);
            }
            if (dialogInfo != null && dialogInfo.cancelButtonName != null) {
                cancelButtonName = dialogInfo.cancelButtonName;
            }
            if (dialogInfo != null && dialogInfo.confirmButtonName != null) {
                confirmButtonName = dialogInfo.confirmButtonName;
            }
        }
        new ConfirmDialog.Builder(getContext()).setTitle(title).setMessage(message)
            .setPositiveButton(confirmButtonName, (dialog, which) -> {
                dialog.dismiss();
                LiveAudioRoomManager.getInstance().seatService.removeUserFromSeat(audioRoomSeat.seatIndex,
                    (errorCode, errorMessage, errorKeys) -> {
                        if (errorCode == 0) {

                        } else {
                            if (translationText != null && translationText.removeSpeakerFailedToast != null) {
                                String toast = String.format(translationText.removeSpeakerFailedToast, userName);
                                Toast.makeText(getContext(), toast, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            }).setNegativeButton(cancelButtonName, (dialog2, which) -> {
                dialog2.dismiss();
            }).build().show();
    }


    public void onRoomPropertiesFullUpdated(List<String> updateKeys, HashMap<String, String> oldProperties,
        HashMap<String, String> properties) {
        for (String key : updateKeys) {
            String oldValue = oldProperties.get(key);
            String newValue = properties.get(key);
            if (!TextUtils.isEmpty(oldValue) && TextUtils.isEmpty(newValue)) {
                removeUserFromSeat(Integer.parseInt(key));
            }
        }
        for (String key : updateKeys) {
            String oldValue = oldProperties.get(key);
            String newValue = properties.get(key);
            if (!TextUtils.isEmpty(newValue)) {
                if (!TextUtils.isEmpty(oldValue)) {
                    removeUserFromSeat(Integer.parseInt(key));
                }
                addUserToSeat(Integer.parseInt(key), ZegoUIKit.getUser(newValue));
            }
        }
    }

    public void onSeatChanged(Map<String, String> map) {
        for (int i = 0; i < audioRoomSeatList.size(); i++) {
            removeUserFromSeat(i);
        }
        for (Entry<String, String> entry : map.entrySet()) {
            int seatIndex = Integer.parseInt(entry.getKey());
            addUserToSeat(seatIndex, ZegoUIKit.getUser(entry.getValue()));
        }
    }

    public void setSeatConfig(ZegoLiveAudioRoomSeatConfig seatConfig) {
        this.seatConfig = seatConfig;
    }

    public void setLockSeatList(List<Integer> lockSeatIndexesForHost) {
        this.lockSeatIndexesForHost = lockSeatIndexesForHost;
    }
}
