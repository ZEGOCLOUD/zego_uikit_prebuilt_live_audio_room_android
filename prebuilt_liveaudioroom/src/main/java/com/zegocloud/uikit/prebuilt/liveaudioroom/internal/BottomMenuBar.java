package com.zegocloud.uikit.prebuilt.liveaudioroom.internal;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView.ScaleType;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import com.zegocloud.uikit.components.audiovideo.ZegoLeaveButton;
import com.zegocloud.uikit.components.audiovideo.ZegoToggleMicrophoneButton;
import com.zegocloud.uikit.prebuilt.liveaudioroom.R;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoBottomMenuBarConfig;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoInRoomMessageButton;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoLiveAudioRoomRole;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoMenuBarButtonName;
import com.zegocloud.uikit.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BottomMenuBar extends LinearLayout {

    private List<View> showList = new ArrayList<>();
    private List<View> hideList = new ArrayList<>();
    private Map<ZegoLiveAudioRoomRole, List<View>> extendedButtons = new HashMap<>();
    private BottomMenuBarMoreDialog moreDialog;
    private LinearLayout childLinearLayout;
    private ZegoInRoomMessageButton messageButton;
    private ZegoLiveAudioRoomRole currentRole;
    private LiveMemberList livememberList;
    private ZegoBottomMenuBarConfig menuBarConfig = new ZegoBottomMenuBarConfig();

    public BottomMenuBar(@NonNull Context context) {
        super(context);
        initView();
    }

    public BottomMenuBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public BottomMenuBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setOrientation(LinearLayout.HORIZONTAL);
        setLayoutParams(new LayoutParams(-1, -2));
        setGravity(Gravity.CENTER_HORIZONTAL);

        messageButton = new ZegoInRoomMessageButton(getContext());
        messageButton.setImageResource(R.drawable.audioroom_icon_im);
        messageButton.setScaleType(ScaleType.FIT_XY);
        LinearLayout.LayoutParams btnParam = new LayoutParams(-2, -2);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int marginStart = Utils.dp2px(16, displayMetrics);
        int marginTop = Utils.dp2px(10, displayMetrics);
        btnParam.setMargins(marginStart, marginTop, 0, marginStart);
        addView(messageButton, btnParam);

        childLinearLayout = new LinearLayout(getContext());
        childLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        childLinearLayout.setGravity(Gravity.END);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -2, 1);
        addView(childLinearLayout, params);
        int paddingEnd = Utils.dp2px(8, getResources().getDisplayMetrics());
        childLinearLayout.setPadding(0, 0, paddingEnd, 0);

    }

    private List<View> getMenuBarViews(List<ZegoMenuBarButtonName> list) {
        List<View> viewList = new ArrayList<>();
        for (ZegoMenuBarButtonName zegoMenuBarButton : list) {
            View viewFromType = getViewFromType(zegoMenuBarButton);
            viewList.add(viewFromType);
        }
        return viewList;
    }

    private void addChildView(View view) {
        childLinearLayout.addView(view);
    }

    private void removeAllChildViews() {
        childLinearLayout.removeAllViews();
    }

    private LayoutParams generateChildLayoutParams() {
        int size = Utils.dp2px(36f, getResources().getDisplayMetrics());
        int marginTop = Utils.dp2px(10f, getResources().getDisplayMetrics());
        int marginBottom = Utils.dp2px(16f, getResources().getDisplayMetrics());
        int marginEnd = Utils.dp2px(8, getResources().getDisplayMetrics());
        LayoutParams layoutParams = new LayoutParams(size, size);
        layoutParams.topMargin = marginTop;
        layoutParams.bottomMargin = marginBottom;
        layoutParams.rightMargin = marginEnd;
        return layoutParams;
    }

    private View getViewFromType(ZegoMenuBarButtonName name) {
        View view = null;
        switch (name) {
            case TOGGLE_MICROPHONE_BUTTON: {
                view = new PermissionMicrophoneButton(getContext());
                ((ZegoToggleMicrophoneButton) view).setIcon(R.drawable.audioroom_icon_mic_on,
                    R.drawable.audioroom_icon_mic_off);
                LayoutParams params = generateChildLayoutParams();
                view.setLayoutParams(params);
            }
            break;
            case SHOW_MEMBER_LIST_BUTTON: {
                view = new ImageView(getContext());
                ((ImageView) view).setImageResource(R.drawable.audioroom_icon_member);
                LayoutParams params = generateChildLayoutParams();
                view.setLayoutParams(params);
                view.setOnClickListener(v -> {
                    livememberList = new LiveMemberList(getContext());
                    if (menuBarConfig != null && menuBarConfig.memberListConfig != null) {
                        livememberList.setMemberListItemViewProvider(
                            menuBarConfig.memberListConfig.memberListItemViewProvider);
                    }
                    livememberList.show();
                });
            }
            break;
            case LEAVE_BUTTON: {
                view = new ZegoLeaveButton(getContext());
                ((ZegoLeaveButton) view).setIcon(R.drawable.audioroom_icon_close);
                LayoutParams params = generateChildLayoutParams();
                view.setLayoutParams(params);
            }
            break;
        }
        if (view != null) {
            view.setTag(name);
        }
        return view;
    }

    public void addExtendedButtons(List<View> viewList, ZegoLiveAudioRoomRole role) {
        extendedButtons.put(role, viewList);
        if (role == currentRole) {
            notifyListChanged();
        }
    }

    public void clearExtendedButtons(ZegoLiveAudioRoomRole role) {
        extendedButtons.remove(role);
        if (role == currentRole) {
            notifyListChanged();
        }
    }

    private void showMoreDialog() {
        if (moreDialog == null) {
            moreDialog = new BottomMenuBarMoreDialog(getContext());
        }
        if (!moreDialog.isShowing()) {
            moreDialog.show();
        }
        moreDialog.setHideChildren(hideList);
    }

    private void notifyListChanged() {
        removeAllChildViews();
        showList.clear();
        hideList.clear();

        List<View> buildInViews = new ArrayList<>();
        List<View> extendedViews = new ArrayList<>();
        if (currentRole == ZegoLiveAudioRoomRole.HOST) {
            buildInViews = getMenuBarViews(menuBarConfig.hostButtons);
            extendedViews = extendedButtons.get(ZegoLiveAudioRoomRole.HOST);
        } else if (currentRole == ZegoLiveAudioRoomRole.SPEAKER) {
            buildInViews = getMenuBarViews(menuBarConfig.speakerButtons);
            extendedViews = extendedButtons.get(ZegoLiveAudioRoomRole.SPEAKER);
        } else if (currentRole == ZegoLiveAudioRoomRole.AUDIENCE) {
            buildInViews = getMenuBarViews(menuBarConfig.audienceButtons);
            extendedViews = extendedButtons.get(ZegoLiveAudioRoomRole.AUDIENCE);
        }
        List<View> menuBarViews = new ArrayList<>(buildInViews);
        if (extendedViews != null && !extendedViews.isEmpty()) {
            menuBarViews.addAll(extendedViews);
        }

        if (menuBarViews.size() <= menuBarConfig.menuBarButtonsMaxCount) {
            showList.addAll(menuBarViews);
        } else {
            int showChildCount = menuBarConfig.menuBarButtonsMaxCount - 1;
            if (showChildCount > 0) {
                showList.addAll(menuBarViews.subList(0, showChildCount));
                hideList = menuBarViews.subList(showChildCount, menuBarViews.size());
            }
            showList.add(new MoreButton(getContext()));
        }

        for (int i = 0; i < showList.size(); i++) {
            addChildView(showList.get(i));
        }
        if (moreDialog != null) {
            moreDialog.setHideChildren(hideList);
        }
    }

    private void showInRoomMessageButton(boolean show) {
        messageButton.setVisibility(show ? VISIBLE : GONE);
    }

    public void showHostButtons() {
        currentRole = ZegoLiveAudioRoomRole.HOST;
        notifyListChanged();
    }

    public void showSpeakerButtons() {
        currentRole = ZegoLiveAudioRoomRole.SPEAKER;
        notifyListChanged();
    }

    public void showAudienceButtons() {
        currentRole = ZegoLiveAudioRoomRole.AUDIENCE;
        notifyListChanged();
    }

    public void setConfig(ZegoBottomMenuBarConfig menuBarConfig) {
        if (menuBarConfig == null) {
            menuBarConfig = new ZegoBottomMenuBarConfig();
        }
        this.menuBarConfig = menuBarConfig;
        showInRoomMessageButton(menuBarConfig.showInRoomMessageButton);
    }

    public class MoreButton extends AppCompatImageView {

        public MoreButton(@NonNull Context context) {
            super(context);
            initView();
        }

        public MoreButton(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            initView();
        }

        private void initView() {
            StateListDrawable sld = new StateListDrawable();
            sld.addState(new int[]{android.R.attr.state_pressed},
                ContextCompat.getDrawable(getContext(), R.drawable.audioroom_icon_more));
            sld.addState(new int[]{}, ContextCompat.getDrawable(getContext(), R.drawable.audioroom_icon_more));
            setImageDrawable(sld);
            LayoutParams params = generateChildLayoutParams();
            setLayoutParams(params);
            setOnClickListener(v -> showMoreDialog());
        }
    }
}
