package com.zegocloud.uikit.prebuilt.liveaudioroom;

import android.Manifest.permission;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.signaling.ZegoSignalingPlugin;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoDialogInfo;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoLiveAudioRoomRole;
import com.zegocloud.uikit.prebuilt.liveaudioroom.databinding.FragmentZegouikitPrebuiltLiveaudioroomBinding;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.ConfirmDialog;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.LiveAudioRoomManager;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.LiveAudioRoomManager.RoleChangedListener;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.LiveAudioRoomManager.SeatChangedListener;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.SeatService;
import com.zegocloud.uikit.service.defines.ZegoScenario;
import com.zegocloud.uikit.service.defines.ZegoUIKitCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitPluginCallback;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ZegoUIKitPrebuiltLiveAudioRoomFragment extends Fragment {

    //Current page exit
    private OnBackPressedCallback onBackPressedCallback;
    private ZegoUIKitPrebuiltLiveAudioRoomConfig config;
    private FragmentZegouikitPrebuiltLiveaudioroomBinding binding;
    //Additional controls list
    private Map<ZegoLiveAudioRoomRole, List<View>> bottomMenuBarExtendedButtons = new HashMap<>();
    private Boolean joinRTCSuccess = null;
    private Boolean joinPluginSuccess = null;
    private View liveAudioRoomBackgroundView;

    public ZegoUIKitPrebuiltLiveAudioRoomFragment() {
        // Required empty public constructor
    }

    public static ZegoUIKitPrebuiltLiveAudioRoomFragment newInstance(long appID, String appSign, String userID,
        String userName, String roomID, ZegoUIKitPrebuiltLiveAudioRoomConfig config) {
        ZegoUIKitPrebuiltLiveAudioRoomFragment fragment = new ZegoUIKitPrebuiltLiveAudioRoomFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("appID", appID);
        bundle.putString("appSign", appSign);
        bundle.putString("userID", userID);
        bundle.putString("userName", userName);
        bundle.putString("roomID", roomID);
        fragment.setArguments(bundle);
        fragment.setPrebuiltLiveStreamingConfig(config);
        return fragment;
    }

    public void setPrebuiltLiveStreamingConfig(ZegoUIKitPrebuiltLiveAudioRoomConfig prebuiltLiveStreamingConfig) {
        config = prebuiltLiveStreamingConfig;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        long appID = arguments.getLong("appID");
        String appSign = arguments.getString("appSign");
        String userName = arguments.getString("userName");
        String userID = arguments.getString("userID");
        String roomID = getArguments().getString("roomID");
        if (appID != 0) {
            ZegoUIKit.installPlugins(Collections.singletonList(ZegoSignalingPlugin.getInstance()));
            ZegoUIKit.init(requireActivity().getApplication(), appID, appSign, ZegoScenario.STANDARD_CHATROOM);

            ZegoUIKit.login(userID, userName, new ZegoUIKitCallback() {
                @Override
                public void onResult(int errorCode) {
                    if (errorCode == 0) {
                        ZegoUIKit.joinRoom(roomID, new ZegoUIKitCallback() {
                            @Override
                            public void onResult(int errorCode) {
                                if (errorCode == 0) {
                                    ZegoUIKit.getSignalingPlugin()
                                        .login(userID, userName, new ZegoUIKitPluginCallback() {
                                            @Override
                                            public void onResult(int errorCode, String errorMessage) {
                                                if (errorCode == 0) {
                                                    ZegoUIKit.getSignalingPlugin()
                                                        .joinRoom(roomID, new ZegoUIKitPluginCallback() {
                                                            @Override
                                                            public void onResult(int errorCode, String errorMessage) {
                                                                if (errorCode == 0) {
                                                                    onRoomJoinSucceed();
                                                                } else {
                                                                    onRoomJoinFailed();
                                                                }
                                                            }
                                                        });
                                                }
                                            }
                                        });
                                }
                            }
                        });
                    }
                }
            });
        }
        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (config.confirmDialogInfo != null) {
                    showQuitDialog(getDialogInfo());
                } else {
                    leaveRoom();
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentZegouikitPrebuiltLiveaudioroomBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (liveAudioRoomBackgroundView != null) {
            binding.liveAudioRoomBackgroundContainer.addView(liveAudioRoomBackgroundView);
        }
        binding.liveAudioRoomContainer.setSeatConfig(config.seatConfig);
        binding.liveAudioRoomContainer.setLayoutConfig(config.layoutConfig);
        binding.liveAudioRoomContainer.setLockSeatList(config.hostSeatIndexes);
        LiveAudioRoomManager.getInstance().setTranslationText(config.translationText);

    }

    private void onRoomJoinFailed() {
        Log.d(ZegoUIKit.TAG, "onRoomJoinFailed() called");
    }

    private void onRoomJoinSucceed() {
        Log.d(ZegoUIKit.TAG, "onRoomJoinSucceed() called");
        LiveAudioRoomManager.getInstance().init();
        LiveAudioRoomManager.getInstance().addRoleChangedListener(new RoleChangedListener() {
            @Override
            public void onRoleChanged(String userID, ZegoLiveAudioRoomRole after) {
                Log.d(ZegoUIKit.TAG, "onRoleChanged() called with: userID = [" + userID + "], after = [" + after + "]");
                String selfUserID = getArguments().getString("userID");
                if (Objects.equals(userID, selfUserID)) {
                    if (after == ZegoLiveAudioRoomRole.HOST) {
                        binding.roomBottomMenuBar.showHostButtons();
                    } else {
                        if (after == ZegoLiveAudioRoomRole.SPEAKER) {
                            binding.roomBottomMenuBar.showSpeakerButtons();
                            requestPermissionIfNeeded((allGranted, grantedList, deniedList) -> {
                                if (grantedList.contains(permission.RECORD_AUDIO)) {
                                    ZegoUIKit.turnMicrophoneOn(selfUserID, true);
                                }
                            });
                        } else {
                            ZegoUIKit.turnMicrophoneOn(selfUserID, false);
                            binding.roomBottomMenuBar.showAudienceButtons();
                        }
                    }
                }
            }
        });

        LiveAudioRoomManager.getInstance().setSeatChangedListener(new SeatChangedListener() {
            @Override
            public void onRoomPropertiesFullUpdated(List<String> updateKeys, HashMap<String, String> oldProperties,
                HashMap<String, String> properties) {
                binding.liveAudioRoomContainer.onRoomPropertiesFullUpdated(updateKeys, oldProperties, properties);
            }
        });

        initLiveAudioRoomWidgets();

        if (config.role == ZegoLiveAudioRoomRole.HOST || config.role == ZegoLiveAudioRoomRole.SPEAKER) {
            boolean invalid = config.takeSeatIndexWhenJoining < 0 || config.takeSeatIndexWhenJoining > binding.liveAudioRoomContainer.getSeatCount();
            boolean locked = config.role == ZegoLiveAudioRoomRole.SPEAKER && config.hostSeatIndexes.contains(
                config.takeSeatIndexWhenJoining);
            if (invalid || locked) {
                config.takeSeatIndexWhenJoining = -1;
                LiveAudioRoomManager.getInstance().setLocalUserRole(ZegoLiveAudioRoomRole.AUDIENCE);
            } else {
                if (config.turnOnMicrophoneWhenJoining) {
                    requestPermissionIfNeeded((allGranted, grantedList, deniedList) -> {
                        if (grantedList.contains(permission.RECORD_AUDIO)) {
                            if (config.turnOnMicrophoneWhenJoining) {
                                String userID = ZegoUIKit.getLocalUser().userID;
                                ZegoUIKit.turnMicrophoneOn(userID, true);
                            }
                        }
                    });
                }
                SeatService seatService = LiveAudioRoomManager.getInstance().seatService;
                if (config.role == ZegoLiveAudioRoomRole.HOST) {
                    seatService.takeSeat(config.takeSeatIndexWhenJoining, (errorCode, errorMessage, errorKeys) -> {
                        if (errorCode == 0) {
                            LiveAudioRoomManager.getInstance().setUserHost((errorCode2, errorMessage2) -> {
                                if (errorCode2 != 0) {
                                    LiveAudioRoomManager.getInstance().seatService.leaveSeat(config.takeSeatIndexWhenJoining, null);
                                    LiveAudioRoomManager.getInstance().setLocalUserRole(ZegoLiveAudioRoomRole.AUDIENCE);
                                }
                            });
                        } else {
                            LiveAudioRoomManager.getInstance().setLocalUserRole(ZegoLiveAudioRoomRole.AUDIENCE);
                        }
                    });
                } else if (config.role == ZegoLiveAudioRoomRole.SPEAKER) {
                    seatService.tryTakeSeat(config.takeSeatIndexWhenJoining, (errorCode, errorMessage, errorKeys) -> {
                        if (errorCode != 0) {
                            LiveAudioRoomManager.getInstance().setLocalUserRole(ZegoLiveAudioRoomRole.AUDIENCE);
                        }
                    });
                }

            }
        }
        ZegoUIKit.setAudioOutputToSpeaker(config.useSpeakerWhenJoining);

    }

    private void initLiveAudioRoomWidgets() {
        binding.roomName.setText(config.translationText.prebuiltTitle);
        binding.roomId.setText("ID: " + getArguments().getString("roomID"));

        if (config.confirmDialogInfo != null) {
            binding.liveRoomExit.setConfirmDialogInfo(getDialogInfo());
        }

        binding.liveRoomExit.setLeaveLiveListener(() -> {
            leaveRoom();
            requireActivity().finish();
        });

        binding.roomBottomMenuBar.setConfig(config.bottomMenuBarConfig);
        for (Map.Entry<ZegoLiveAudioRoomRole, List<View>> entry : bottomMenuBarExtendedButtons.entrySet()) {
            binding.roomBottomMenuBar.addExtendedButtons(entry.getValue(), entry.getKey());
        }
        binding.roomBottomMenuBar.showAudienceButtons();
    }

    public void addButtonToMenuBar(List<View> widgets, ZegoLiveAudioRoomRole role) {
        bottomMenuBarExtendedButtons.put(role, widgets);
        if (binding != null) {
            binding.roomBottomMenuBar.addExtendedButtons(widgets, role);
        }
    }

    public void clearBottomBarExtendButtons(ZegoLiveAudioRoomRole role) {
        bottomMenuBarExtendedButtons.remove(role);
        if (binding != null) {
            binding.roomBottomMenuBar.clearExtendedButtons(role);
        }
    }

    public void setBackgroundView(View view) {
        liveAudioRoomBackgroundView = view;
        if (binding != null) {
            binding.liveAudioRoomBackgroundContainer.addView(view);
        }
    }

    /**
     * Leaving the room
     */
    private void leaveRoom() {
        ZegoUIKit.leaveRoom();
        ZegoUIKit.logout();
        ZegoUIKit.getSignalingPlugin().leaveRoom(null);
        LiveAudioRoomManager.getInstance().clear();
    }

    private void showQuitDialog(ZegoDialogInfo dialogInfo) {
        new ConfirmDialog.Builder(getContext()).setTitle(dialogInfo.title).setMessage(dialogInfo.message)
            .setPositiveButton(dialogInfo.confirmButtonName, (dialog, which) -> {
                if (onBackPressedCallback != null) {
                    onBackPressedCallback.setEnabled(false);
                }
                dialog.dismiss();
                leaveRoom();
                requireActivity().onBackPressed();
            }).setNegativeButton(dialogInfo.cancelButtonName, (dialog, which) -> {
                dialog.dismiss();
            }).build().show();
    }

    private ZegoDialogInfo getDialogInfo() {
        ZegoDialogInfo dialogInfo = new ZegoDialogInfo();
        if (config.confirmDialogInfo.title == null) {
            dialogInfo.title = getString(R.string.stop_room_title);
        } else {
            dialogInfo.title = config.confirmDialogInfo.title;
        }
        if (config.confirmDialogInfo.message == null) {
            dialogInfo.message = getString(R.string.stop_room_message);
        } else {
            dialogInfo.message = config.confirmDialogInfo.message;
        }
        if (config.confirmDialogInfo.confirmButtonName == null) {
            dialogInfo.confirmButtonName = getString(R.string.stop_room_ok);
        } else {
            dialogInfo.confirmButtonName = config.confirmDialogInfo.confirmButtonName;
        }
        if (config.confirmDialogInfo.cancelButtonName == null) {
            dialogInfo.cancelButtonName = getString(R.string.stop_room_cancel);
        } else {
            dialogInfo.cancelButtonName = config.confirmDialogInfo.cancelButtonName;
        }
        return dialogInfo;
    }

    private void requestPermissionIfNeeded(RequestCallback requestCallback) {
        PermissionX.init(requireActivity()).permissions(permission.RECORD_AUDIO)
            .onExplainRequestReason((scope, deniedList) -> {
                String message = getContext().getString(R.string.permission_explain_mic);
                scope.showRequestReasonDialog(deniedList, message, getString(R.string.ok));
            }).onForwardToSettings((scope, deniedList) -> {
                String message = getContext().getString(R.string.settings_mic);
                scope.showForwardToSettingsDialog(deniedList, message, getString(R.string.settings),
                    getString(R.string.cancel));
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        leaveRoom();
    }

}
