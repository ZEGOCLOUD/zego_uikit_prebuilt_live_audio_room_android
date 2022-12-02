package com.zegocloud.uikit.prebuilt.liveaudioroom.core;

import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class ZegoBottomMenuBarConfig {

    public boolean showInRoomMessageButton = true;
    public int menuBarButtonsMaxCount = 5;

    public List<ZegoMenuBarButtonName> hostButtons = new ArrayList<>();
    public List<ZegoMenuBarButtonName> speakerButtons = new ArrayList<>();
    public List<ZegoMenuBarButtonName> audienceButtons = new ArrayList<>();
//    public transient List<View> hostExtendButtons = new ArrayList<>();
//    public transient List<View> speakerExtendButtons = new ArrayList<>();
//    public transient List<View> audienceExtendButtons = new ArrayList<>();

    public ZegoMemberListConfig memberListConfig = new ZegoMemberListConfig();

    public ZegoBottomMenuBarConfig() {
    }

    public ZegoBottomMenuBarConfig(List<ZegoMenuBarButtonName> hostButtons, List<ZegoMenuBarButtonName> speakerButtons,
        List<ZegoMenuBarButtonName> audienceButtons) {
        this.hostButtons = hostButtons;
        this.speakerButtons = speakerButtons;
        this.audienceButtons = audienceButtons;
    }

}
