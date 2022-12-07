package com.zegocloud.uikit.prebuilt.liveaudioroom.internal;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.components.internal.RippleIconView;
import com.zegocloud.uikit.components.memberlist.ZegoMemberListComparator;
import com.zegocloud.uikit.components.memberlist.ZegoMemberListItemViewProvider;
import com.zegocloud.uikit.prebuilt.liveaudioroom.R;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.liveaudioroom.databinding.LayoutMemberlistBinding;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.service.defines.ZegoUserUpdateListener;
import com.zegocloud.uikit.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LiveMemberList extends BottomSheetDialog {

    private LayoutMemberlistBinding binding;
    private ZegoMemberListItemViewProvider memberListItemProvider;
    private ZegoUserUpdateListener userUpdateListener;

    public LiveMemberList(@NonNull Context context) {
        super(context, R.style.TransparentDialog);
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
    }

    public LiveMemberList(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = LayoutMemberlistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
            binding.liveMemberListTitle.setText(translationText.memberListTitle);
        }

        binding.liveMemberList.setMemberListComparator(new ZegoMemberListComparator() {
            @Override
            public List<ZegoUIKitUser> sortUserList(List<ZegoUIKitUser> userList) {
                List<ZegoUIKitUser> result = new ArrayList<>();
                List<ZegoUIKitUser> speaker = new ArrayList<>();
                List<ZegoUIKitUser> audience = new ArrayList<>();
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
                            boolean isSpeaker = LiveAudioRoomManager.getInstance().roleService.isUserSpeaker(uiKitUser.userID);
                            if (isSpeaker) {
                                speaker.add(uiKitUser);
                            } else {
                                audience.add(uiKitUser);
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
                result.addAll(audience);
                return result;
            }
        });

        if (memberListItemProvider != null) {
            binding.liveMemberList.setItemViewProvider(memberListItemProvider);
        } else {
            binding.liveMemberList.setItemViewProvider(new ZegoMemberListItemViewProvider() {
                @Override
                public View onCreateView(ViewGroup parent) {
                    View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_liveroom_member, parent, false);
                    int height = Utils.dp2px(70, parent.getContext().getResources().getDisplayMetrics());
                    view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
                    return view;
                }

                @Override
                public void onBindView(View view, ZegoUIKitUser uiKitUser, int position) {
                    RippleIconView rippleIconView = view.findViewById(R.id.live_member_item_icon);
                    TextView memberName = view.findViewById(R.id.live_member_item_name);
                    TextView tag = view.findViewById(R.id.live_member_item_tag);
                    rippleIconView.setText(uiKitUser.userName, false);
                    memberName.setText(uiKitUser.userName);
                    ZegoUIKitUser localUser = ZegoUIKit.getLocalUser();

                    boolean isYou = Objects.equals(uiKitUser, localUser);
                    boolean isHost = LiveAudioRoomManager.getInstance().roleService.isUserHost(uiKitUser.userID);
                    boolean isSpeaker = LiveAudioRoomManager.getInstance().roleService.isUserSpeaker(uiKitUser.userID);
                    StringBuilder builder = new StringBuilder();
                    if (isYou || isHost || isSpeaker) {
                        builder.append("(");
                    }
                    if (isYou) {
                        builder.append(getContext().getString(R.string.you));
                    }
                    if (isHost) {
                        if (isYou) {
                            builder.append(",");
                        }
                        builder.append(getContext().getString(R.string.host));
                    } else {
                        if (isSpeaker) {
                            if (isYou) {
                                builder.append(",");
                            }
                            builder.append(getContext().getString(R.string.speaker));
                        }
                    }

                    if (isYou || isHost || isSpeaker) {
                        builder.append(")");
                    }
                    tag.setText(builder.toString());
                }
            });
        }

        binding.liveMemberListListCount.setText(String.valueOf(ZegoUIKit.getAllUsers().size()));
        userUpdateListener = new ZegoUserUpdateListener() {
            @Override
            public void onUserJoined(List<ZegoUIKitUser> userInfoList) {
                binding.liveMemberListListCount.setText(String.valueOf(ZegoUIKit.getAllUsers().size()));
            }

            @Override
            public void onUserLeft(List<ZegoUIKitUser> userInfoList) {
                binding.liveMemberListListCount.setText(String.valueOf(ZegoUIKit.getAllUsers().size()));
            }
        };
        ZegoUIKit.addUserUpdateListener(userUpdateListener);

        setOnDismissListener(dialog -> {
            ZegoUIKit.removeUserUpdateListener(userUpdateListener);
        });

        // both need setPeekHeight & setLayoutParams
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int height = (int) (displayMetrics.heightPixels * 0.6f);
        getBehavior().setPeekHeight(height);
        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(-1, height);
        binding.liveMemberListLayout.setLayoutParams(params);
    }

    public void updateList() {
        if (binding != null) {
            binding.liveMemberList.notifyDataSetChanged();
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

    public void setMemberListItemViewProvider(ZegoMemberListItemViewProvider memberListItemProvider) {
        this.memberListItemProvider = memberListItemProvider;
    }

}
