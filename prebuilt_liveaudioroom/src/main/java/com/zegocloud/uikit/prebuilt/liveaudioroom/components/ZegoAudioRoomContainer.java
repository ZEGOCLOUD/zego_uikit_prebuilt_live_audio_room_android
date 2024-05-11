package com.zegocloud.uikit.prebuilt.liveaudioroom.components;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.JustifyContent;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.signaling.ZegoSignalingPlugin;
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
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service.LiveAudioRoomManager;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service.SeatService;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.service.defines.ZegoUserUpdateListener;
import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMEventHandler;
import im.zego.zim.entity.ZIMUserInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public class ZegoAudioRoomContainer extends LinearLayout {

    private ZegoUserUpdateListener userUpdateListener;
    private ZegoLiveAudioRoomSeatConfig seatConfig;
    private List<Integer> lockSeatIndexesForHost;
    private long lastClickTime = 0;
    private BottomActionDialog leaveSeatActionDialog;
    private ConfirmDialog leaveSeatConfirmDialog;
    private BottomActionDialog removeSeatActionDialog;
    private ConfirmDialog removeSeatUserConfirmDialog;
    private BottomActionDialog takeSeatActionDialog;
    private AudioRoomSeat clickedSeat;
    private Dialog currentDialog;
    private boolean hasNoNameUserOnSeat = false;
    private ZIMEventHandler handler;

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
                if (hasNoNameUserOnSeat) {
                    List<AudioRoomSeat> audioRoomSeatList = LiveAudioRoomManager.getInstance().seatService.getAudioRoomSeatList();
                    for (ZegoUIKitUser uiKitUser : userInfoList) {
                        for (AudioRoomSeat audioRoomSeat : audioRoomSeatList) {
                            if (audioRoomSeat.isNotEmpty()) {
                                boolean emptyName = TextUtils.isEmpty(audioRoomSeat.getUser().userName);
                                if (emptyName && audioRoomSeat.isTakenByUser(uiKitUser)) {
                                    audioRoomSeat.getUser().userName = uiKitUser.userName;
                                    AudioRoomSeatView seatView = getAudioRoomSeatView(audioRoomSeat);
                                    seatView.updateUser();
                                }
                            }
                        }
                    }
                    boolean hasEmptyName = false;
                    for (AudioRoomSeat audioRoomSeat : audioRoomSeatList) {
                        if (audioRoomSeat.isNotEmpty()) {
                            if (TextUtils.isEmpty(audioRoomSeat.getUser().userName)) {
                                hasEmptyName = true;
                                break;
                            }
                        }
                    }
                    if (!hasEmptyName) {
                        hasNoNameUserOnSeat = false;
                    }
                }
            }

            @Override
            public void onUserLeft(List<ZegoUIKitUser> userInfoList) {

            }
        };

        handler = new ZIMEventHandler() {
            @Override
            public void onRoomMemberLeft(ZIM zim, ArrayList<ZIMUserInfo> memberList, String roomID) {
                super.onRoomMemberLeft(zim, memberList, roomID);
                for (ZIMUserInfo uiKitUser : memberList) {
                    removeUserFromSeat(uiKitUser.userID);
                }
            }
        };
    }

    private void removeUserFromSeat(int seatIndex) {
        AudioRoomSeat audioRoomSeat = LiveAudioRoomManager.getInstance().seatService.tryGetAudioRoomSeat(seatIndex);
        if (audioRoomSeat != null) {
            removeUserFromSeat(audioRoomSeat);
        }
    }

    private void removeUserFromSeat(String userID) {
        AudioRoomSeat audioRoomSeat = LiveAudioRoomManager.getInstance().seatService.findUserRoomSeat(userID);
        if (audioRoomSeat != null) {
            removeUserFromSeat(audioRoomSeat);
        }
    }

    private void removeUserFromSeat(AudioRoomSeat audioRoomSeat) {
        AudioRoomSeatView seatView = getAudioRoomSeatView(audioRoomSeat);
        seatView.setUser(null);
    }


    public void addUserToSeat(int seatIndex, ZegoUIKitUser uiKitUser) {
        if (uiKitUser == null) {
            return;
        }
        AudioRoomSeat audioRoomSeat = LiveAudioRoomManager.getInstance().seatService.tryGetAudioRoomSeat(seatIndex);
        AudioRoomSeatView seatView = getAudioRoomSeatView(audioRoomSeat);
        seatView.setUser(uiKitUser);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ZegoUIKit.addUserUpdateListener(userUpdateListener);
        ZegoSignalingPlugin.getInstance().registerZIMEventHandler(handler);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ZegoUIKit.removeUserUpdateListener(userUpdateListener);
        ZegoSignalingPlugin.getInstance().unregisterZIMEventHandler(handler);
    }

    private AudioRoomSeatView getAudioRoomSeatView(AudioRoomSeat audioRoomSeat) {
        FlexboxLayout flexboxLayout = (FlexboxLayout) getChildAt(audioRoomSeat.rowIndex);
        return ((AudioRoomSeatView) flexboxLayout.getChildAt(audioRoomSeat.columnIndex));
    }


    public void setLayoutConfig(ZegoLiveAudioRoomLayoutConfig layoutConfig) {
        List<AudioRoomSeat> audioRoomSeatList = LiveAudioRoomManager.getInstance().seatService.getAudioRoomSeatList();

        int seatIndex = 0;
        for (int rowIndex = 0; rowIndex < layoutConfig.rowConfigs.size(); rowIndex++) {
            ZegoLiveAudioRoomLayoutRowConfig rowConfig = layoutConfig.rowConfigs.get(rowIndex);
            FlexboxLayout flexboxLayout = new FlexboxLayout(getContext());
            LayoutParams params = new LayoutParams(-1, -2);
            params.bottomMargin = layoutConfig.rowSpacing;
            addView(flexboxLayout, params);
            for (int columnIndex = 0; columnIndex < rowConfig.count; columnIndex++) {
                AudioRoomSeat audioRoomSeat = audioRoomSeatList.get(seatIndex);
                seatIndex = seatIndex + 1;
                AudioRoomSeatView seatView = new AudioRoomSeatView(getContext(), audioRoomSeat);
                seatView.setSeatConfig(seatConfig);
                seatView.setOnClickListener(v -> {
                    if (System.currentTimeMillis() - lastClickTime < 500) {
                        return;
                    }
                    onSeatViewClicked(seatView);
                    lastClickTime = System.currentTimeMillis();
                });
                flexboxLayout.addView(seatView);

                FlexboxLayout.LayoutParams layoutParams = (FlexboxLayout.LayoutParams) seatView.getLayoutParams();
                if (rowConfig.alignment == ZegoLiveAudioRoomLayoutAlignment.SPACE_EVENLY
                    || rowConfig.alignment == ZegoLiveAudioRoomLayoutAlignment.SPACE_BETWEEN
                    || rowConfig.alignment == ZegoLiveAudioRoomLayoutAlignment.SPACE_AROUND) {
                    layoutParams.rightMargin = 0;
                } else {
                    layoutParams.rightMargin = rowConfig.seatSpacing;
                }
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

    private void onSeatViewClicked(AudioRoomSeatView seatView) {
        if (ZegoUIKit.getLocalUser() == null) {
            return;
        }
        AudioRoomSeat audioRoomSeat = seatView.getAudioRoomSeat();
        if (seatConfig != null && seatConfig.seatClickedListener != null) {
            ViewGroup parent = (ViewGroup) seatView.getParent();
            seatConfig.seatClickedListener.onSeatClicked(parent, audioRoomSeat.seatIndex, audioRoomSeat.getUser());
            return;
        }

        boolean isLocalUserHost = LiveAudioRoomManager.getInstance().roleService.isLocalUserHost();
        if (audioRoomSeat.isEmpty()) {
            if (isLocalUserHost || lockSeatIndexesForHost.contains(audioRoomSeat.seatIndex) || seatView.isLocked()) {
                return;
            }
            int mySeatIndex = LiveAudioRoomManager.getInstance().seatService.findMyRoomSeatIndex();
            if (mySeatIndex == -1) {
                showTakeSeatActionDialog(audioRoomSeat);
            } else {
                LiveAudioRoomManager.getInstance().seatService.switchSeat(mySeatIndex, audioRoomSeat.seatIndex);
            }
        } else {
            if (Objects.equals(audioRoomSeat.getUser(), ZegoUIKit.getLocalUser())) {
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

    private void showTakeSeatActionDialog(AudioRoomSeat audioRoomSeat) {
        clickedSeat = audioRoomSeat;
        List<String> stringList = new ArrayList<>();
        ZegoTranslationText translationText = LiveAudioRoomManager.getInstance().getTranslationText();
        if (translationText != null && translationText.takeSeatMenuDialogButton != null) {
            stringList.add(translationText.takeSeatMenuDialogButton);
        }
        if (translationText != null && translationText.cancelMenuDialogButton != null) {
            stringList.add(translationText.cancelMenuDialogButton);
        }

        takeSeatActionDialog = new BottomActionDialog(getContext(), stringList);
        takeSeatActionDialog.show();
        takeSeatActionDialog.setOnDialogClickListener((dialog, which) -> {
            if (which == 0) {
                SeatService seatService = LiveAudioRoomManager.getInstance().seatService;
                if (!seatService.isSeatLocked()) {
                    seatService.tryTakeSeat(audioRoomSeat.seatIndex, (errorCode, errorMessage, errorKeys) -> {
                        if (errorCode == 0) {
                        }
                    });
                }
            }
            dialog.dismiss();
        });
        takeSeatActionDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                clickedSeat = null;
            }
        });
        currentDialog = takeSeatActionDialog;
        clickedSeat = audioRoomSeat;
    }

    private void showLeaveSeatActionDialog(AudioRoomSeat audioRoomSeat) {
        List<String> stringList = new ArrayList<>();
        ZegoTranslationText translationText = LiveAudioRoomManager.getInstance().getTranslationText();
        if (translationText != null && translationText.leaveSeatMenuDialogButton != null) {
            stringList.add(translationText.leaveSeatMenuDialogButton);
        }
        if (translationText != null && translationText.cancelMenuDialogButton != null) {
            stringList.add(translationText.cancelMenuDialogButton);
        }
        leaveSeatActionDialog = new BottomActionDialog(getContext(), stringList);
        leaveSeatActionDialog.show();
        leaveSeatActionDialog.setOnDialogClickListener((dialog, which) -> {
            if (which == 0) {
                showLeaveSeatConfirmDialog(audioRoomSeat);
            }
            dialog.dismiss();
        });
        currentDialog = leaveSeatActionDialog;
        clickedSeat = audioRoomSeat;
    }

    private void showLeaveSeatConfirmDialog(AudioRoomSeat audioRoomSeat) {
        if (audioRoomSeat.isEmpty()) {
            return;
        }
        String title = "";
        String message = "";
        String cancelButtonName = "";
        String confirmButtonName = "";
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
        leaveSeatConfirmDialog = new ConfirmDialog.Builder(getContext()).setTitle(title).setMessage(message)
            .setPositiveButton(confirmButtonName, (dialog, which) -> {
                LiveAudioRoomManager.getInstance().seatService.makeSeatEmpty(audioRoomSeat.seatIndex,
                    (errorCode, errorMessage, errorKeys) -> {
                        if (errorCode == 0) {
                        }
                    });
                dialog.dismiss();
            }).setNegativeButton(cancelButtonName, (dialog, which) -> {
                dialog.dismiss();
            }).build();
        leaveSeatConfirmDialog.show();
        currentDialog = leaveSeatConfirmDialog;
        clickedSeat = audioRoomSeat;
    }

    private void showRemoveSeatUserActionDialog(AudioRoomSeat audioRoomSeat) {
        List<String> stringList = new ArrayList<>();
        ZegoTranslationText translationText = LiveAudioRoomManager.getInstance().getTranslationText();
        if (translationText != null && translationText.muteSpeakerMenuDialogButton != null) {
            stringList.add(
                String.format(translationText.muteSpeakerMenuDialogButton, audioRoomSeat.getUser().userName));
        }
        if (translationText != null && translationText.removeSpeakerMenuDialogButton != null) {
            stringList.add(
                String.format(translationText.removeSpeakerMenuDialogButton, audioRoomSeat.getUser().userName));
        }
        if (translationText != null && translationText.cancelMenuDialogButton != null) {
            stringList.add(translationText.cancelMenuDialogButton);
        }
        removeSeatActionDialog = new BottomActionDialog(getContext(), stringList);
        removeSeatActionDialog.show();
        removeSeatActionDialog.setOnDialogClickListener((dialog, which) -> {
            if (which == 0) {
                ZegoUIKit.turnMicrophoneOn(audioRoomSeat.getUser().userID, false);
            } else if (which == 1) {
                showRemoveSeatUserConfirmDialog(audioRoomSeat);
            }
            dialog.dismiss();
        });
        currentDialog = removeSeatActionDialog;
        clickedSeat = audioRoomSeat;
    }

    private void showRemoveSeatUserConfirmDialog(AudioRoomSeat audioRoomSeat) {
        if (audioRoomSeat.isEmpty()) {
            return;
        }
        String userName = audioRoomSeat.getUser().userName;
        String title = "";
        String message = "";
        String cancelButtonName = "";
        String confirmButtonName = "";
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
        removeSeatUserConfirmDialog = new ConfirmDialog.Builder(getContext()).setTitle(title).setMessage(message)
            .setPositiveButton(confirmButtonName, (dialog, which) -> {
                dialog.dismiss();
                LiveAudioRoomManager.getInstance().seatService.removeUserFromSeat(audioRoomSeat.seatIndex,
                    (errorCode, errorMessage, errorKeys) -> {
                        if (errorCode == 0) {

                        } else {
                            String errorTips = "";
                            if (translationText != null && translationText.removeSpeakerFailedToast != null) {
                                errorTips = String.format(translationText.removeSpeakerFailedToast, userName);
                            }
                            LiveAudioRoomManager.getInstance().showTopTips(errorTips, false);
                        }
                    });
            }).setNegativeButton(cancelButtonName, (dialog2, which) -> {
                dialog2.dismiss();
            }).build();
        removeSeatUserConfirmDialog.show();
        currentDialog = removeSeatUserConfirmDialog;
        clickedSeat = audioRoomSeat;
    }

    public void setSeatConfig(ZegoLiveAudioRoomSeatConfig seatConfig) {
        this.seatConfig = seatConfig;
    }

    public void setLockSeatList(List<Integer> lockSeatIndexesForHost) {
        this.lockSeatIndexesForHost = lockSeatIndexesForHost;
    }

    public void onSeatsChanged(Map<Integer, ZegoUIKitUser> takenSeats, List<Integer> untakenSeats) {
        for (Integer untakenSeat : untakenSeats) {
            removeUserFromSeat(untakenSeat);
        }
        for (Entry<Integer, ZegoUIKitUser> entry : takenSeats.entrySet()) {
            Integer seatIndex = entry.getKey();
            ZegoUIKitUser uiKitUser = entry.getValue();
            addUserToSeat(seatIndex, uiKitUser);
        }
        for (Entry<Integer, ZegoUIKitUser> entry : takenSeats.entrySet()) {
            if (TextUtils.isEmpty(entry.getValue().userName)) {
                hasNoNameUserOnSeat = true;
                break;
            }
        }
        SeatService seatService = LiveAudioRoomManager.getInstance().seatService;
        if (clickedSeat != null) {
            AudioRoomSeat audioRoomSeat = seatService.tryGetAudioRoomSeat(clickedSeat.seatIndex);
            if (audioRoomSeat.isSeatChanged()) {
                if (currentDialog != null) {
                    currentDialog.dismiss();
                }
            }

        }
    }

    public void lockSeat(boolean lock) {
        SeatService seatService = LiveAudioRoomManager.getInstance().seatService;
        for (AudioRoomSeat audioRoomSeat : seatService.getAudioRoomSeatList()) {
            AudioRoomSeatView seatView = getAudioRoomSeatView(audioRoomSeat);
            seatView.setLock(lock);
        }
    }
}
