package com.zegocloud.uikit.prebuilt.liveaudioroom.internal;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.components.internal.RippleIconView;
import com.zegocloud.uikit.components.memberlist.ZegoMemberList;
import com.zegocloud.uikit.components.memberlist.ZegoMemberListComparator;
import com.zegocloud.uikit.components.memberlist.ZegoMemberListItemViewProvider;
import com.zegocloud.uikit.prebuilt.liveaudioroom.R;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoMemberListConfig;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.liveaudioroom.databinding.LiveaudioroomLayoutMemberlistBinding;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service.InvitationService;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service.LiveAudioRoomInvitation;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service.LiveAudioRoomInvitationType;
import com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service.LiveAudioRoomManager;
import com.zegocloud.uikit.prebuilt.liveaudioroom.listener.ZegoSeatsClosedListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.service.defines.ZegoUserUpdateListener;
import com.zegocloud.uikit.utils.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class LiveAudioRoomMemberList extends BottomSheetDialog {

    private LiveaudioroomLayoutMemberlistBinding binding;
    private ZegoMemberListConfig memberListConfig;
    private ZegoUserUpdateListener userUpdateListener;
    private BottomActionDialog moreOperationDialog;
    private TextView liveMemberListTitle;
    private ZegoMemberList liveMemberList;
    private TextView liveMemberListCount;
    private ConstraintLayout liveMemberListLayout;

    public LiveAudioRoomMemberList(@NonNull Context context) {
        super(context, R.style.TransparentDialog);
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
    }

    public LiveAudioRoomMemberList(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = LiveaudioroomLayoutMemberlistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        liveMemberListTitle = binding.liveMemberListTitle;
        liveMemberList = binding.liveMemberList;
        liveMemberListCount = binding.liveMemberListCount;
        liveMemberListLayout = binding.liveMemberListLayout;

        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.dimAmount = 0.1f;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);
        setCanceledOnTouchOutside(true);
        window.setBackgroundDrawable(new ColorDrawable());

        ZegoTranslationText translationText = LiveAudioRoomManager.getInstance().getTranslationText();
        if (translationText != null && translationText.memberListTitle != null) {
            liveMemberListTitle.setText(translationText.memberListTitle);
        }

        liveMemberList.setMemberListComparator(new ZegoMemberListComparator() {
            @Override
            public List<ZegoUIKitUser> sortUserList(List<ZegoUIKitUser> userList) {
                List<ZegoUIKitUser> result = new ArrayList<>();
                List<ZegoUIKitUser> speaker = new ArrayList<>();
                List<ZegoUIKitUser> audience = new ArrayList<>();
                List<ZegoUIKitUser> requested = new ArrayList<>();
                List<ZegoUIKitUser> host = new ArrayList<>();
                ZegoUIKitUser you = null;

                for (ZegoUIKitUser uiKitUser : userList) {
                    boolean isHost = LiveAudioRoomManager.getInstance().roleService.isUserHost(uiKitUser.userID);
                    boolean isYou = Objects.equals(uiKitUser, ZegoUIKit.getLocalUser());
                    if (isHost) {
                        host.add(uiKitUser);
                    } else {
                        if (isYou) {
                            you = uiKitUser;
                        } else {
                            boolean isSpeaker = LiveAudioRoomManager.getInstance().roleService.isUserSpeaker(
                                uiKitUser.userID);
                            if (isSpeaker) {
                                speaker.add(uiKitUser);
                            } else {
                                boolean isRequested = LiveAudioRoomManager.getInstance().invitationService.isUserTakeSeatRequestExisted(
                                    uiKitUser.userID);
                                if (isRequested) {
                                    requested.add(uiKitUser);
                                } else {
                                    audience.add(uiKitUser);
                                }
                            }
                        }
                    }
                }
                if (you != null) {
                    if (!host.contains(you)) {
                        if (!host.isEmpty()) {
                            result.addAll(host);
                        }
                        result.add(you);
                    } else {
                        host.remove(you);
                        host.add(0, you);
                        result.addAll(host);
                    }
                } else {
                    result.addAll(host);
                }
                result.addAll(speaker);
                result.addAll(requested);
                result.addAll(audience);
                return result;
            }
        });

        if (memberListConfig != null && memberListConfig.memberListItemViewProvider != null) {
            liveMemberList.setItemViewProvider(memberListConfig.memberListItemViewProvider);
        } else {
            liveMemberList.setItemViewProvider(new ZegoMemberListItemViewProvider() {
                @Override
                public View onCreateView(ViewGroup parent) {
                    View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.liveaudioroom_item_member, parent, false);
                    int height = Utils.dp2px(70, parent.getContext().getResources().getDisplayMetrics());
                    view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
                    return view;
                }

                @Override
                public void onBindView(View view, ZegoUIKitUser uiKitUser, int position) {
                    RippleIconView rippleIconView = view.findViewById(R.id.live_member_item_icon);
                    ImageView customAvatar = view.findViewById(R.id.live_member_item_custom);
                    TextView memberName = view.findViewById(R.id.live_member_item_name);
                    TextView tag = view.findViewById(R.id.live_member_item_tag);
                    TextView agree = view.findViewById(R.id.live_member_item_agree);
                    TextView disagree = view.findViewById(R.id.live_member_item_disagree);
                    TextView more = view.findViewById(R.id.live_member_item_more);
                    rippleIconView.setText(uiKitUser.userName, false);
                    memberName.setText(uiKitUser.userName);
                    ZegoUIKitUser localUser = ZegoUIKit.getLocalUser();

                    if (uiKitUser.inRoomAttributes != null) {
                        String avatarUrl = uiKitUser.avatar;
                        if (!TextUtils.isEmpty(avatarUrl)) {
                            RequestOptions requestOptions = new RequestOptions().circleCrop();
                            Glide.with(view.getContext()).load(avatarUrl).apply(requestOptions).into(customAvatar);
                        }
                    }

                    boolean isYou = Objects.equals(uiKitUser, localUser);
                    boolean isUserHost = LiveAudioRoomManager.getInstance().roleService.isUserHost(uiKitUser.userID);
                    boolean isUserSpeaker = LiveAudioRoomManager.getInstance().roleService.isUserSpeaker(
                        uiKitUser.userID);
                    ZegoTranslationText translationText = LiveAudioRoomManager.getInstance().getTranslationText();

                    StringBuilder builder = new StringBuilder();
                    if (isYou || isUserHost || isUserSpeaker) {
                        builder.append("(");
                    }
                    if (isYou) {
                        if (translationText != null) {
                            builder.append(translationText.you);
                        } else {

                        }
                    }
                    if (isUserHost) {
                        if (isYou) {
                            builder.append(",");
                        }
                        if (translationText != null) {
                            builder.append(translationText.host);
                        }
                    } else {
                        if (isUserSpeaker) {
                            if (isYou) {
                                builder.append(",");
                            }
                            if (translationText != null) {
                                builder.append(translationText.speaker);
                            }
                        }
                    }

                    if (isYou || isUserHost || isUserSpeaker) {
                        builder.append(")");
                    }
                    tag.setText(builder.toString());

                    InvitationService invitationService = LiveAudioRoomManager.getInstance().invitationService;
                    boolean userCoHostRequestExisted = invitationService.isUserTakeSeatRequestExisted(uiKitUser.userID);
                    boolean isSelfHost = LiveAudioRoomManager.getInstance().roleService.isLocalUserHost();
                    if (isYou) {
                        agree.setVisibility(View.GONE);
                        disagree.setVisibility(View.GONE);
                        more.setVisibility(View.GONE);
                    } else {
                        if (isSelfHost) {
                            if (isUserHost) {
                                agree.setVisibility(View.GONE);
                                disagree.setVisibility(View.GONE);
                                more.setVisibility(View.GONE);
                            } else {
                                if (userCoHostRequestExisted) {
                                    agree.setVisibility(View.VISIBLE);
                                    disagree.setVisibility(View.VISIBLE);
                                    more.setVisibility(View.GONE);
                                } else {
                                    agree.setVisibility(View.GONE);
                                    disagree.setVisibility(View.GONE);
                                    if(isUserSpeaker){
                                        more.setVisibility(View.GONE);
                                    }else {
                                        more.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        } else {
                            agree.setVisibility(View.GONE);
                            disagree.setVisibility(View.GONE);
                            more.setVisibility(View.GONE);
                        }
                    }
                    boolean seatLocked = LiveAudioRoomManager.getInstance().seatService.isSeatLocked();
                    if (!seatLocked) {
                        agree.setVisibility(View.GONE);
                        disagree.setVisibility(View.GONE);
                    }

                    if (translationText != null) {
                        agree.setText(translationText.memberListAgreeButton);
                        disagree.setText(translationText.memberListDisagreeButton);
                    }

                    agree.setOnClickListener(v -> {
                        invitationService.acceptInvitation(uiKitUser, null);
                        dismiss();
                    });
                    disagree.setOnClickListener(v -> {
                        invitationService.refuseInvitation(uiKitUser, null);
                        dismiss();
                    });
                    more.setOnClickListener(v -> {
                        if (memberListConfig != null && memberListConfig.memberListMoreButtonPressedListener != null) {
                            memberListConfig.memberListMoreButtonPressedListener.onMemberListMoreButtonPressed(
                                (ViewGroup) view, uiKitUser);
                        } else {
                            if (seatLocked) {
                                if (!isUserSpeaker) {
                                    showMoreOperationDialog(uiKitUser);
                                    dismiss();
                                }
                            }
                        }
                    });
                }
            });
        }

        liveMemberListCount.setText(String.valueOf(ZegoUIKit.getAllUsers().size()));
        userUpdateListener = new ZegoUserUpdateListener() {
            @Override
            public void onUserJoined(List<ZegoUIKitUser> userInfoList) {
                liveMemberListCount.setText(String.valueOf(ZegoUIKit.getAllUsers().size()));
            }

            @Override
            public void onUserLeft(List<ZegoUIKitUser> userInfoList) {
                liveMemberListCount.setText(String.valueOf(ZegoUIKit.getAllUsers().size()));
            }
        };

        setOnDismissListener(dialog -> {
            ZegoUIKit.removeUserUpdateListener(userUpdateListener);
        });

        // both need setPeekHeight & setLayoutParams
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int height = (int) (displayMetrics.heightPixels * 0.6f);
        getBehavior().setPeekHeight(height);
        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(-1, height);
        liveMemberListLayout.setLayoutParams(params);

        LiveAudioRoomManager.getInstance().seatService.addSeatsLockedListener(new ZegoSeatsClosedListener() {
            @Override
            public void onSeatsClosed() {
            }

            @Override
            public void onSeatsOpened() {
                updateList();
            }
        });
    }

    private void showMoreOperationDialog(ZegoUIKitUser uiKitUser) {
        List<String> stringList = new ArrayList<>();
        ZegoTranslationText translationText = LiveAudioRoomManager.getInstance().getTranslationText();
        if (translationText != null && translationText.inviteToTakeSeatMenuDialogButton != null) {
            stringList.add(String.format(translationText.inviteToTakeSeatMenuDialogButton, uiKitUser.userName));
        }
        if (translationText != null && translationText.cancelMenuDialogButton != null) {
            stringList.add(translationText.cancelMenuDialogButton);
        }

        moreOperationDialog = new BottomActionDialog(getContext(), stringList);
        moreOperationDialog.show();
        moreOperationDialog.setOnDialogClickListener((dialog, which) -> {
            if (which == 0) {
                LiveAudioRoomInvitation myInvitation = LiveAudioRoomManager.getInstance().invitationService.getInvitation(
                    ZegoUIKit.getLocalUser().userID, uiKitUser.userID);
                if (myInvitation != null && !myInvitation.isFinished() && myInvitation.isTakeSeatInvite()) {
                } else {
                    LiveAudioRoomManager.getInstance().invitationService.sendInvitation(
                        Collections.singletonList(uiKitUser.userID), 60,
                        LiveAudioRoomInvitationType.INVITE_TO_SEAT.getValue(), "", null);
                }
            }
            dialog.dismiss();
        });
    }

    @Override
    public void show() {
        super.show();
        ZegoUIKit.addUserUpdateListener(userUpdateListener);
        updateList();
    }

    public void updateList() {
        if (liveMemberListCount != null) {
            liveMemberListCount.setText(String.valueOf(ZegoUIKit.getAllUsers().size()));
        }
        if (liveMemberList != null) {
            liveMemberList.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
            dismiss();
            return true;
        }
        return false;
    }

    public void setMemberListItemConfig(ZegoMemberListConfig memberListConfig) {
        this.memberListConfig = memberListConfig;
    }
}
