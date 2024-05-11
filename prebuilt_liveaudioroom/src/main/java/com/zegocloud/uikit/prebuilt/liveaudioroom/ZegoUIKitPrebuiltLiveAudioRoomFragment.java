package com.zegocloud.uikit.prebuilt.liveaudioroom;

import android.Manifest.permission;
import android.app.Application;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.adapter.utils.GenericUtils;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoDialogInfo;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoLiveAudioRoomRole;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.liveaudioroom.databinding.LiveaudioroomFragmentLiveaudioroomBinding;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.ConfirmDialog;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.PrebuiltUICallBack;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service.IncomingInvitationListener;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service.InvitationService;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service.JoinRoomCallback;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service.LiveAudioRoomInvitation;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service.LiveAudioRoomInvitationType;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service.LiveAudioRoomManager;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service.OutgoingInvitationListener;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service.RoleService.RoleChangedListener;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service.SeatService;
import com.zegocloud.uikit.prebuilt.liveaudioroom.listener.ZegoSeatTakingRequestAudienceListener;
import com.zegocloud.uikit.prebuilt.liveaudioroom.listener.ZegoSeatTakingRequestHostListener;
import com.zegocloud.uikit.prebuilt.liveaudioroom.listener.ZegoSeatsChangedListener;
import com.zegocloud.uikit.prebuilt.liveaudioroom.listener.ZegoSeatsClosedListener;
import com.zegocloud.uikit.service.defines.ZegoMeRemovedFromRoomListener;
import com.zegocloud.uikit.service.defines.ZegoSetUsersInRoomAttributesCallback;
import com.zegocloud.uikit.service.defines.ZegoTurnOnYourMicrophoneRequestListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitPluginCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitSignalingPluginRoomAttributesOperatedCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.service.defines.ZegoUserCountOrPropertyChangedListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import timber.log.Timber;

public class ZegoUIKitPrebuiltLiveAudioRoomFragment extends Fragment {

    //Current page exit
    private OnBackPressedCallback onBackPressedCallback;
    private LiveaudioroomFragmentLiveaudioroomBinding binding;
    //Additional controls list
    private Map<ZegoLiveAudioRoomRole, List<View>> bottomMenuBarExtendedButtons = new HashMap<>();
    private View liveAudioRoomBackgroundView;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable hideTipsRunnable = new Runnable() {
        @Override
        public void run() {
            binding.liveToast.setVisibility(View.GONE);
        }
    };
    private ListenerInfo mListenerInfo;
    private LiveAudioRoomViewModel liveAudioRoomViewModel;
    private ConfirmDialog receiveTakeSeatInviteDialog;

    private ZegoUIKitPrebuiltLiveAudioRoomFragment() {
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

        LiveAudioRoomManager.getInstance().setPrebuiltConfig(config);
        return fragment;
    }

    public static ZegoUIKitPrebuiltLiveAudioRoomFragment newInstanceWithToken(long appID, String token, String userID,
        String userName, String roomID, ZegoUIKitPrebuiltLiveAudioRoomConfig config) {
        ZegoUIKitPrebuiltLiveAudioRoomFragment fragment = new ZegoUIKitPrebuiltLiveAudioRoomFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("appID", appID);
        bundle.putString("appToken", token);
        bundle.putString("userID", userID);
        bundle.putString("userName", userName);
        bundle.putString("roomID", roomID);
        fragment.setArguments(bundle);

        LiveAudioRoomManager.getInstance().setPrebuiltConfig(config);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        liveAudioRoomViewModel = new ViewModelProvider(requireActivity()).get(LiveAudioRoomViewModel.class);

        Bundle arguments = getArguments();
        long appID = arguments.getLong("appID");
        String appSign = arguments.getString("appSign");
        String userName = arguments.getString("userName");
        String userID = arguments.getString("userID");
        String token = arguments.getString("appToken");

        if (appID != 0) {
            Application application = requireActivity().getApplication();
            LiveAudioRoomManager.getInstance().init(application, appID, appSign);
            if (!TextUtils.isEmpty(token)) {
                ZegoUIKit.renewToken(token);
            }
            LiveAudioRoomManager.getInstance().loginUser(userID, userName);
        }
        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                ZegoUIKitPrebuiltLiveAudioRoomConfig prebuiltConfig = LiveAudioRoomManager.getInstance()
                    .getPrebuiltConfig();
                if (prebuiltConfig.confirmDialogInfo != null) {
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
        binding = LiveaudioroomFragmentLiveaudioroomBinding.inflate(inflater, container, false);
        initLiveAudioRoomWidgetsBaseConfig();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String userID = getArguments().getString("userID");
        String userName = getArguments().getString("userName");
        String roomID = getArguments().getString("roomID");
        LiveAudioRoomManager.getInstance().joinRoom(userID, userName, roomID, new JoinRoomCallback() {
            @Override
            public void onJoinRoomSuccess() {
                onRoomJoinSucceed();
            }

            @Override
            public void onJoinRoomFail() {
                onRoomJoinFailed();
            }
        });
    }

    private void onRoomJoinFailed() {
        Timber.w("onRoomJoinFailed() called");
    }

    private void onRoomJoinSucceed() {
        Timber.d("onRoomJoinSucceed() called");
        initListeners();

        ZegoUIKit.addUserCountOrPropertyChangedListener(new ZegoUserCountOrPropertyChangedListener() {
            @Override
            public void onUserCountOrPropertyChanged(List<ZegoUIKitUser> userList) {
                ZegoUserCountOrPropertyChangedListener listener = getListenerInfo().userCountOrPropertyChangedListener;
                if (listener != null) {
                    listener.onUserCountOrPropertyChanged(userList);
                }
            }
        });

        LiveAudioRoomManager.getInstance().roleService.queryUserInRoomAttribute();

        ZegoUIKitPrebuiltLiveAudioRoomConfig config = LiveAudioRoomManager.getInstance().getPrebuiltConfig();
        String selfUserID = getArguments().getString("userID");
        if (!TextUtils.isEmpty(config.userAvatarUrl)) {
            ZegoUIKit.getSignalingPlugin()
                .setUsersInRoomAttributes("avatar", config.userAvatarUrl, Collections.singletonList(selfUserID),
                    new ZegoSetUsersInRoomAttributesCallback() {
                        @Override
                        public void onSetUsersInRoomAttributes(int errorCode, String errorMessage) {
                        }
                    });
        }
        if (config.userInRoomAttributes != null) {
            for (Entry<String, String> entry : config.userInRoomAttributes.entrySet()) {
                ZegoUIKit.getSignalingPlugin()
                    .setUsersInRoomAttributes(entry.getKey(), entry.getValue(), Collections.singletonList(selfUserID),
                        null);
            }
        }

        if (config.role == ZegoLiveAudioRoomRole.HOST || config.role == ZegoLiveAudioRoomRole.SPEAKER) {
            boolean invalid = config.takeSeatIndexWhenJoining < 0
                || config.takeSeatIndexWhenJoining > LiveAudioRoomManager.getInstance().seatService.getSeatCount();
            boolean locked = config.role == ZegoLiveAudioRoomRole.SPEAKER && config.hostSeatIndexes.contains(
                config.takeSeatIndexWhenJoining);
            if (invalid || locked) {
                config.takeSeatIndexWhenJoining = -1;
            } else {
                if (config.turnOnMicrophoneWhenJoining) {
                    requestPermissionIfNeeded((allGranted, grantedList, deniedList) -> {
                        String userID = ZegoUIKit.getLocalUser().userID;
                        if (config.turnOnMicrophoneWhenJoining) {
                            if (grantedList.contains(permission.RECORD_AUDIO)) {
                                ZegoUIKit.turnMicrophoneOn(userID, true);
                            }
                        } else {
                            ZegoUIKit.turnMicrophoneOn(userID, false);
                        }
                    });
                }
                SeatService seatService = LiveAudioRoomManager.getInstance().seatService;
                if (config.role == ZegoLiveAudioRoomRole.HOST) {
                    seatService.takeSeat(config.takeSeatIndexWhenJoining, (errorCode, errorMessage, errorKeys) -> {
                        if (errorCode == 0) {
                            LiveAudioRoomManager.getInstance().roleService.setLocalUserHost(
                                (errorCode2, errorMessage2) -> {
                                    if (errorCode2 != 0) {
                                        LiveAudioRoomManager.getInstance().seatService.makeSeatEmpty(
                                            config.takeSeatIndexWhenJoining, null);
                                    }
                                });
                        } else {
                        }
                    });
                } else if (config.role == ZegoLiveAudioRoomRole.SPEAKER) {
                    seatService.tryTakeSeat(config.takeSeatIndexWhenJoining, (errorCode, errorMessage, errorKeys) -> {
                        if (errorCode != 0) {
                        }
                    });
                }

            }
        }
        ZegoUIKit.setAudioOutputToSpeaker(config.useSpeakerWhenJoining);

        SeatService seatService = LiveAudioRoomManager.getInstance().seatService;
        if (config.role == ZegoLiveAudioRoomRole.HOST) {
            if (!seatService.hasLockSeat()) {
                seatService.lockSeat(config.closeSeatsWhenJoin);
            }
        }
        if (seatService.isSeatLocked()) {
            onSeatLocked();
        } else {
            onSeatUnLocked();
        }

        ZegoUIKit.addTurnOnYourMicrophoneRequestListener(new ZegoTurnOnYourMicrophoneRequestListener() {
            @Override
            public void onTurnOnYourMicrophoneRequest(ZegoUIKitUser fromUser) {
                ZegoTurnOnYourMicrophoneRequestListener listener = getListenerInfo().turnOnYourMicrophoneRequestListener;
                if (listener != null) {
                    listener.onTurnOnYourMicrophoneRequest(fromUser);
                }
            }
        });

        ZegoUIKit.addOnMeRemovedFromRoomListener(new ZegoMeRemovedFromRoomListener() {
            @Override
            public void onMeRemovedFromRoom() {
                ZegoUIKitPrebuiltLiveAudioRoomConfig prebuiltConfig = LiveAudioRoomManager.getInstance()
                    .getPrebuiltConfig();
                if (prebuiltConfig.removedFromRoomListener == null) {
                    leaveRoom();
                    requireActivity().finish();
                }
            }
        });
    }

    private void initListeners() {
        LiveAudioRoomManager.getInstance().setPrebuiltUICallBack(new PrebuiltUICallBack() {
            @Override
            public void showTopTips(String tips, boolean green) {
                showTopTipsOnFragment(tips, green);
            }
        });
        LiveAudioRoomManager.getInstance().roleService.addRoleChangedListener(new RoleChangedListener() {
            @Override
            public void onRoleChanged(String userID, ZegoLiveAudioRoomRole before, ZegoLiveAudioRoomRole after) {
                String selfUserID = getArguments().getString("userID");
                if (Objects.equals(userID, selfUserID)) {
                    onUserRoleChanged(after);
                }
                binding.roomBottomMenuBar.updateMemberList();
            }
        });

        LiveAudioRoomManager.getInstance().seatService.setSeatChangedListener(new ZegoSeatsChangedListener() {
            @Override
            public void onSeatsChanged(Map<Integer, ZegoUIKitUser> takenSeats, List<Integer> untakenSeats) {
                binding.liveAudioRoomContainer.onSeatsChanged(takenSeats, untakenSeats);
                ZegoSeatsChangedListener seatsChangedListener = getListenerInfo().seatsChangedListener;
                if (seatsChangedListener != null) {
                    seatsChangedListener.onSeatsChanged(takenSeats, untakenSeats);
                }
            }
        });
        LiveAudioRoomManager.getInstance().seatService.addSeatsLockedListener(new ZegoSeatsClosedListener() {
            @Override
            public void onSeatsClosed() {
                onSeatLocked();
                ZegoSeatsClosedListener seatsLockedListener = getListenerInfo().seatsLockedListener;
                if (seatsLockedListener != null) {
                    seatsLockedListener.onSeatsClosed();
                }
            }

            @Override
            public void onSeatsOpened() {
                onSeatUnLocked();
                ZegoSeatsClosedListener seatsLockedListener = getListenerInfo().seatsLockedListener;
                if (seatsLockedListener != null) {
                    seatsLockedListener.onSeatsOpened();
                }
            }
        });
        LiveAudioRoomManager.getInstance().invitationService.addIncomingInvitationListener(
            new IncomingInvitationListener() {
                @Override
                public void onReceiveNewInvitation(ZegoUIKitUser inviter, int type, String data) {
                    LiveAudioRoomInvitation invitation = LiveAudioRoomManager.getInstance().invitationService.getInvitation(
                        inviter.userID, ZegoUIKit.getLocalUser().userID);
                    if (invitation != null && invitation.isTakeSeatInvite()) {
                        ZegoTranslationText translationText = LiveAudioRoomManager.getInstance().getTranslationText();
                        if (translationText != null) {
                            ZegoDialogInfo dialogInfo = translationText.receivedCoHostInvitationDialogInfo;
                            if (dialogInfo != null) {
                                if (receiveTakeSeatInviteDialog != null && receiveTakeSeatInviteDialog.isShowing()) {
                                    return;
                                }
                                InvitationService invitationService = LiveAudioRoomManager.getInstance().invitationService;
                                SeatService seatService = LiveAudioRoomManager.getInstance().seatService;
                                receiveTakeSeatInviteDialog = new ConfirmDialog.Builder(getContext()).setTitle(
                                        dialogInfo.title).setMessage(dialogInfo.message)
                                    .setPositiveButton(dialogInfo.confirmButtonName, (dialog, which) -> {
                                        invitationService.acceptInvitation(inviter, null);
                                        int availableSeatIndex = seatService.findFirstAvailableSeatIndex();
                                        if (availableSeatIndex != -1) {
                                            seatService.tryTakeSeat(availableSeatIndex, null);
                                        }
                                        dialog.dismiss();
                                    }).setNegativeButton(dialogInfo.cancelButtonName, (dialog, which) -> {
                                        invitationService.refuseInvitation(inviter, null);
                                        dialog.dismiss();
                                    }).build();
                                receiveTakeSeatInviteDialog.show();
                            }
                        }
                        if (getListenerInfo().audienceListener != null) {
                            getListenerInfo().audienceListener.onHostSeatTakingInviteSent();
                        }
                    } else if (invitation != null && invitation.isTakeSeatRequest()) {
                        if (getListenerInfo().hostListener != null) {
                            getListenerInfo().hostListener.onSeatTakingRequested(inviter);
                        }
                    }
                    checkRedPoint();
                }

                @Override
                public void onReceiveInvitationButResponseTimeout(ZegoUIKitUser inviter) {
                    checkRedPoint();
                    LiveAudioRoomInvitation invitation = LiveAudioRoomManager.getInstance().invitationService.getInvitation(
                        inviter.userID, ZegoUIKit.getLocalUser().userID);
                    if (invitation != null && invitation.isTakeSeatInvite()) {
                        if (receiveTakeSeatInviteDialog != null) {
                            receiveTakeSeatInviteDialog.dismiss();
                        }
                    }
                }

                @Override
                public void onReceiveInvitationButIsCancelled(ZegoUIKitUser inviter, String extendedData) {
                    checkRedPoint();
                    LiveAudioRoomInvitation invitation = LiveAudioRoomManager.getInstance().invitationService.getInvitation(
                        inviter.userID, ZegoUIKit.getLocalUser().userID);
                    if (invitation != null && invitation.isTakeSeatInvite()) {
                        if (receiveTakeSeatInviteDialog != null) {
                            receiveTakeSeatInviteDialog.dismiss();
                        }
                    } else if (invitation != null && invitation.isTakeSeatRequest()) {
                        if (getListenerInfo().hostListener != null) {
                            getListenerInfo().hostListener.onSeatTakingRequestCancelled(inviter);
                        }
                    }
                }

                @Override
                public void onActionAcceptInvitation(ZegoUIKitUser inviter, int errorCode, String message) {
                    checkRedPoint();
                }

                @Override
                public void onActionRejectInvitation(ZegoUIKitUser inviter, int errorCode, String message) {
                    checkRedPoint();
                }
            });
        LiveAudioRoomManager.getInstance().invitationService.addOutgoingInvitationListener(
            new OutgoingInvitationListener() {
                @Override
                public void onActionSendInvitation(List<String> invitees, int errorCode, String message,
                    List<ZegoUIKitUser> errorInvitees) {

                }

                @Override
                public void onActionCancelInvitation(List<String> invitees, int errorCode, String message) {

                }

                @Override
                public void onSendInvitationButReceiveResponseTimeout(ZegoUIKitUser invitee, String extendedData) {
                }

                @Override
                public void onSendInvitationAndIsAccepted(ZegoUIKitUser invitee, String extendedData) {
                    LiveAudioRoomInvitation invitation = LiveAudioRoomManager.getInstance().invitationService.getInvitation(
                        ZegoUIKit.getLocalUser().userID, invitee.userID);
                    if (invitation != null && invitation.isTakeSeatRequest()) {
                        SeatService seatService = LiveAudioRoomManager.getInstance().seatService;
                        int availableSeatIndex = LiveAudioRoomManager.getInstance().seatService.findFirstAvailableSeatIndex();
                        if (availableSeatIndex != -1) {
                            seatService.tryTakeSeat(availableSeatIndex, null);
                        }
                    }
                }

                @Override
                public void onSendInvitationButIsRejected(ZegoUIKitUser invitee, String extendedData) {
                    LiveAudioRoomInvitation invitation = LiveAudioRoomManager.getInstance().invitationService.getInvitation(
                        ZegoUIKit.getLocalUser().userID, invitee.userID);
                    if (invitation != null && invitation.isTakeSeatRequest()) {
                        if (getListenerInfo().audienceListener != null) {
                            getListenerInfo().audienceListener.onSeatTakingRequestRejected();
                        }
                    } else if (invitation != null && invitation.isTakeSeatInvite()) {
                        if (getListenerInfo().hostListener != null) {
                            getListenerInfo().hostListener.onSeatTakingInviteRejected();
                        }
                    }
                }
            });
    }

    private void onUserRoleChanged(ZegoLiveAudioRoomRole userRole) {
        ZegoUIKitUser localUser = ZegoUIKit.getLocalUser();
        if (userRole == ZegoLiveAudioRoomRole.HOST) {
            binding.roomBottomMenuBar.showHostButtons();
        } else if (userRole == ZegoLiveAudioRoomRole.SPEAKER) {
            binding.roomBottomMenuBar.showSpeakerButtons();
            requestPermissionIfNeeded((allGranted, grantedList, deniedList) -> {
                if (grantedList.contains(permission.RECORD_AUDIO)) {
                    if (localUser != null) {
                        ZegoUIKit.turnMicrophoneOn(localUser.userID, true);
                    }
                }
            });
        } else {
            if (localUser != null) {
                ZegoUIKit.turnMicrophoneOn(localUser.userID, false);
            }
            binding.roomBottomMenuBar.showAudienceButtons();
        }
    }

    private void onSeatUnLocked() {
        binding.liveAudioRoomContainer.lockSeat(false);
        binding.roomBottomMenuBar.hideTakeSeatButton();
        binding.roomBottomMenuBar.hideRedPoint();
        if (receiveTakeSeatInviteDialog != null && receiveTakeSeatInviteDialog.isShowing()) {
            receiveTakeSeatInviteDialog.dismiss();
        }
    }

    private void onSeatLocked() {
        binding.liveAudioRoomContainer.lockSeat(true);
        if (!LiveAudioRoomManager.getInstance().roleService.isLocalUserHost()) {
            binding.roomBottomMenuBar.showTakeSeatButton();
        }
        checkRedPoint();
    }

    private void checkRedPoint() {
        boolean seatLocked = LiveAudioRoomManager.getInstance().seatService.isSeatLocked();
        boolean localUserHost = LiveAudioRoomManager.getInstance().roleService.isLocalUserHost();
        List<String> userIDs = LiveAudioRoomManager.getInstance().invitationService.getTakeSeatRequestUserIDs();
        if (localUserHost && seatLocked && !userIDs.isEmpty()) {
            binding.roomBottomMenuBar.showRedPoint();
        } else {
            binding.roomBottomMenuBar.hideRedPoint();
        }
    }

    private void showTopTipsOnFragment(String tips, boolean green) {
        binding.liveToast.setText(tips);
        binding.liveToast.setVisibility(View.VISIBLE);
        if (green) {
            binding.liveToast.setBackgroundColor(Color.parseColor("#55BC9E"));
        } else {
            binding.liveToast.setBackgroundColor(Color.parseColor("#BD5454"));
        }
        handler.removeCallbacks(hideTipsRunnable);
        handler.postDelayed(hideTipsRunnable, 2000);
    }

    private void initLiveAudioRoomWidgetsBaseConfig() {
        ZegoUIKitPrebuiltLiveAudioRoomConfig prebuiltConfig = LiveAudioRoomManager.getInstance().getPrebuiltConfig();
        if (liveAudioRoomBackgroundView != null) {
            binding.liveAudioRoomBackgroundContainer.addView(liveAudioRoomBackgroundView);
        }
        binding.liveAudioRoomContainer.setSeatConfig(prebuiltConfig.seatConfig);
        binding.liveAudioRoomContainer.setLayoutConfig(prebuiltConfig.layoutConfig);
        binding.liveAudioRoomContainer.setLockSeatList(prebuiltConfig.hostSeatIndexes);

        if (prebuiltConfig.confirmDialogInfo != null) {
            binding.liveRoomExit.setConfirmDialogInfo(getDialogInfo());
        }

        binding.liveRoomExit.setLeaveLiveListener(() -> {
            leaveRoom();
            requireActivity().finish();
        });

        binding.roomBottomMenuBar.setConfig(prebuiltConfig.bottomMenuBarConfig);
        for (Map.Entry<ZegoLiveAudioRoomRole, List<View>> entry : bottomMenuBarExtendedButtons.entrySet()) {
            binding.roomBottomMenuBar.addExtendedButtons(entry.getValue(), entry.getKey());
        }

        onUserRoleChanged(prebuiltConfig.role);

        if (prebuiltConfig.inRoomMessageViewConfig != null) {
            binding.liveMessageView.setVisibility(
                prebuiltConfig.inRoomMessageViewConfig.visible ? View.VISIBLE : View.GONE);
            binding.liveMessageView.setItemViewProvider(
                prebuiltConfig.inRoomMessageViewConfig.inRoomMessageItemViewProvider);
        }
    }

    public void addButtonToBottomMenuBar(List<View> widgets, ZegoLiveAudioRoomRole role) {
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
        LiveAudioRoomManager.getInstance().leaveRoom();
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
        ZegoUIKitPrebuiltLiveAudioRoomConfig config = LiveAudioRoomManager.getInstance().getPrebuiltConfig();
        ZegoTranslationText translationText = LiveAudioRoomManager.getInstance().getTranslationText();
        if (TextUtils.isEmpty(config.confirmDialogInfo.title)) {
            config.confirmDialogInfo.title = translationText.leaveRoomConfirmDialogInfo.title;
        }
        if (TextUtils.isEmpty(config.confirmDialogInfo.message)) {
            config.confirmDialogInfo.message = translationText.leaveRoomConfirmDialogInfo.message;
        }
        if (TextUtils.isEmpty(config.confirmDialogInfo.confirmButtonName)) {
            config.confirmDialogInfo.confirmButtonName = translationText.leaveRoomConfirmDialogInfo.confirmButtonName;
        }
        if (TextUtils.isEmpty(config.confirmDialogInfo.cancelButtonName)) {
            config.confirmDialogInfo.cancelButtonName = translationText.leaveRoomConfirmDialogInfo.cancelButtonName;
        }
        return config.confirmDialogInfo;
    }

    private void requestPermissionIfNeeded(RequestCallback requestCallback) {
        List<String> permissions = Collections.singletonList(permission.RECORD_AUDIO);

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

        PermissionX.init(requireActivity()).permissions(permission.RECORD_AUDIO)
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


    @Override
    public void onPause() {
        super.onPause();
        if (requireActivity().isFinishing()) {
            leaveRoom();
        }
    }

    static class ListenerInfo {

        protected ZegoTurnOnYourMicrophoneRequestListener turnOnYourMicrophoneRequestListener;
        protected ZegoSeatTakingRequestAudienceListener audienceListener;
        protected ZegoSeatTakingRequestHostListener hostListener;
        protected ZegoSeatsClosedListener seatsLockedListener;
        protected ZegoSeatsChangedListener seatsChangedListener;
        protected ZegoUserCountOrPropertyChangedListener userCountOrPropertyChangedListener;
    }

    ListenerInfo getListenerInfo() {
        if (mListenerInfo != null) {
            return mListenerInfo;
        }
        mListenerInfo = new ListenerInfo();
        return mListenerInfo;
    }

    public void setTurnOnYourMicrophoneRequestListener(ZegoTurnOnYourMicrophoneRequestListener l) {
        getListenerInfo().turnOnYourMicrophoneRequestListener = l;
    }

    public void setSeatTakingRequestAudienceListener(ZegoSeatTakingRequestAudienceListener l) {
        getListenerInfo().audienceListener = l;
    }

    public void setUserCountOrPropertyChangedListener(ZegoUserCountOrPropertyChangedListener l) {
        getListenerInfo().userCountOrPropertyChangedListener = l;
    }

    public void setSeatTakingRequestHostListener(ZegoSeatTakingRequestHostListener l) {
        getListenerInfo().hostListener = l;
    }

    public void setSeatsChangedListener(ZegoSeatsChangedListener l) {
        getListenerInfo().seatsChangedListener = l;
    }

    public void setSeatsClosedListener(ZegoSeatsClosedListener l) {
        getListenerInfo().seatsLockedListener = l;
    }

    public void turnMicrophoneOn(String userID, boolean isOn) {
        ZegoUIKit.turnMicrophoneOn(userID, isOn);
    }

    public void removeSpeakerFromSeat(String userID) {
        LiveAudioRoomManager.getInstance().seatService.removeSpeakerFromSeat(userID, null);
    }

    public void applyToTakeSeat(ZegoUIKitPluginCallback callback) {
        String hostUserID = LiveAudioRoomManager.getInstance().roleService.getHostUserID();
        ZegoUIKitUser hostUser = ZegoUIKit.getUser(hostUserID);
        if (TextUtils.isEmpty(hostUserID) || hostUser == null) {
            return;
        }
        List<String> idList = GenericUtils.map(Collections.singletonList(hostUser),
            zegoUIKitUser -> zegoUIKitUser.userID);
        LiveAudioRoomManager.getInstance().invitationService.sendInvitation(idList, 60,
            LiveAudioRoomInvitationType.REQUEST_TAKE_SEAT.getValue(), "", new PluginCallbackListener() {
                @Override
                public void callback(Map<String, Object> result) {
                    if (callback != null) {
                        int code = (int) result.get("code");
                        String message = (String) result.get("message");
                        callback.onResult(code, message);
                    }
                }
            });
    }

    public void cancelSeatTakingRequest() {
        String hostUserID = LiveAudioRoomManager.getInstance().roleService.getHostUserID();
        ZegoUIKitUser uiKitUser = ZegoUIKit.getUser(hostUserID);
        if (TextUtils.isEmpty(hostUserID) || uiKitUser == null) {
            return;
        }
        LiveAudioRoomManager.getInstance().invitationService.cancelInvitation(Collections.singletonList(hostUserID),
            new PluginCallbackListener() {
                @Override
                public void callback(Map<String, Object> result) {

                }
            });
    }

    public void takeSeat(int index) {
        SeatService seatService = LiveAudioRoomManager.getInstance().seatService;
        seatService.tryTakeSeat(index, new ZegoUIKitSignalingPluginRoomAttributesOperatedCallback() {
            @Override
            public void onSignalingPluginRoomAttributesOperated(int errorCode, String errorMessage,
                List<String> errorKeys) {

            }
        });

    }

    public void leaveSeat() {
        SeatService seatService = LiveAudioRoomManager.getInstance().seatService;
        int mySeatIndex = seatService.findMyRoomSeatIndex();
        if (mySeatIndex != -1) {
            seatService.makeSeatEmpty(mySeatIndex, new ZegoUIKitSignalingPluginRoomAttributesOperatedCallback() {
                @Override
                public void onSignalingPluginRoomAttributesOperated(int errorCode, String errorMessage,
                    List<String> errorKeys) {

                }
            });
        }
    }

    public void acceptSeatTakingRequest(String audienceUserID) {
        InvitationService invitationService = LiveAudioRoomManager.getInstance().invitationService;
        invitationService.acceptInvitation(ZegoUIKit.getUser(audienceUserID), new PluginCallbackListener() {
            @Override
            public void callback(Map<String, Object> result) {

            }
        });
    }

    public void rejectSeatTakingRequest(String audienceUserID) {
        InvitationService invitationService = LiveAudioRoomManager.getInstance().invitationService;
        invitationService.refuseInvitation(ZegoUIKit.getUser(audienceUserID), new PluginCallbackListener() {
            @Override
            public void callback(Map<String, Object> result) {

            }
        });
    }

    public void inviteAudienceToTakeSeat(String audienceUserID) {
        InvitationService invitationService = LiveAudioRoomManager.getInstance().invitationService;
        invitationService.sendInvitation(Collections.singletonList(audienceUserID), 60,
            LiveAudioRoomInvitationType.INVITE_TO_SEAT.getValue(), "", new PluginCallbackListener() {
                @Override
                public void callback(Map<String, Object> result) {

                }
            });
    }

    public void acceptHostTakeSeatInvitation() {
        String hostUserID = LiveAudioRoomManager.getInstance().roleService.getHostUserID();
        ZegoUIKitUser uiKitUser = ZegoUIKit.getUser(hostUserID);
        if (TextUtils.isEmpty(hostUserID) || uiKitUser == null) {
            return;
        }
        InvitationService invitationService = LiveAudioRoomManager.getInstance().invitationService;
        SeatService seatService = LiveAudioRoomManager.getInstance().seatService;
        invitationService.acceptInvitation(uiKitUser, new PluginCallbackListener() {
            @Override
            public void callback(Map<String, Object> result) {
                int firstAvailableSeatIndex = seatService.findFirstAvailableSeatIndex();
                seatService.tryTakeSeat(firstAvailableSeatIndex,
                    new ZegoUIKitSignalingPluginRoomAttributesOperatedCallback() {
                        @Override
                        public void onSignalingPluginRoomAttributesOperated(int errorCode, String errorMessage,
                            List<String> errorKeys) {

                        }
                    });
            }
        });
    }

    public void openSeats() {
        LiveAudioRoomManager.getInstance().seatService.lockSeat(false);
    }

    public void closeSeats() {
        LiveAudioRoomManager.getInstance().seatService.lockSeat(true);
    }
}
