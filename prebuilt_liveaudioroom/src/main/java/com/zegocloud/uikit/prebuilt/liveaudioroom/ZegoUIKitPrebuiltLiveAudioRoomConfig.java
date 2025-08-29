package com.zegocloud.uikit.prebuilt.liveaudioroom;


import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoPlayStreamBufferIntervalRange;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoBottomMenuBarConfig;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoDialogInfo;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoInRoomMessageViewConfig;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoInnerText;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoLiveAudioRoomLayoutConfig;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoLiveAudioRoomRole;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoLiveAudioRoomSeatConfig;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoMenuBarButtonName;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoTranslationText;
import com.zegocloud.uikit.service.defines.ZegoAudioVideoResourceMode;
import com.zegocloud.uikit.service.defines.ZegoMeRemovedFromRoomListener;
import im.zego.zegoexpress.entity.ZegoAudioConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ZegoUIKitPrebuiltLiveAudioRoomConfig {

    //Character Type
    public ZegoLiveAudioRoomRole role = ZegoLiveAudioRoomRole.AUDIENCE;
    // Sitting position, only effective when the role is host and speaker
    public int takeSeatIndexWhenJoining = -1;
    public boolean turnOnMicrophoneWhenJoining = false;
    public boolean useSpeakerWhenJoining = true;
    public ZegoBottomMenuBarConfig bottomMenuBarConfig = new ZegoBottomMenuBarConfig(new ArrayList<>(
        Arrays.asList(ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON, ZegoMenuBarButtonName.SHOW_MEMBER_LIST_BUTTON,
            ZegoMenuBarButtonName.CLOSE_SEAT_BUTTON)), new ArrayList<>(
        Arrays.asList(ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON, ZegoMenuBarButtonName.SHOW_MEMBER_LIST_BUTTON)),
        new ArrayList<>(Collections.singletonList(ZegoMenuBarButtonName.APPLY_TO_TAKE_SEAT_BUTTON)));

    /**
     * if confirmDialogInfo is not null,a confirm dialog will show when exit button was clicked or back button is
     * pressed. please use {@link ZegoTranslationText#leaveRoomConfirmDialogInfo }  to custom dialog texts.
     */
    @Deprecated
    public ZegoDialogInfo confirmDialogInfo;

    public ZegoTranslationText translationText = new ZegoTranslationText();

    public ZegoInnerText innerText = new ZegoInnerText();
    public ZegoLiveAudioRoomLayoutConfig layoutConfig = new ZegoLiveAudioRoomLayoutConfig();
    public ZegoLiveAudioRoomSeatConfig seatConfig = new ZegoLiveAudioRoomSeatConfig();
    public ZegoInRoomMessageViewConfig inRoomMessageViewConfig = new ZegoInRoomMessageViewConfig();
    public List<Integer> hostSeatIndexes = Collections.singletonList(0);
    public String userAvatarUrl;
    public Map<String, String> userInRoomAttributes;
    public boolean closeSeatsWhenJoin = true;
    public transient ZegoMeRemovedFromRoomListener removedFromRoomListener;
    public ZegoAudioConfig audioConfig;
    public ZegoPlayStreamBufferIntervalRange playStreamBufferIntervalRange;
    public ZegoAudioVideoResourceMode avResourceMode = ZegoAudioVideoResourceMode.RTC_ONLY;

    public static ZegoUIKitPrebuiltLiveAudioRoomConfig host() {
        ZegoUIKitPrebuiltLiveAudioRoomConfig config = new ZegoUIKitPrebuiltLiveAudioRoomConfig();
        config.role = ZegoLiveAudioRoomRole.HOST;
        config.takeSeatIndexWhenJoining = 0;
        config.turnOnMicrophoneWhenJoining = true;
        config.confirmDialogInfo = new ZegoDialogInfo();
        return config;
    }

    public static ZegoUIKitPrebuiltLiveAudioRoomConfig audience() {
        ZegoUIKitPrebuiltLiveAudioRoomConfig config = new ZegoUIKitPrebuiltLiveAudioRoomConfig();
        config.role = ZegoLiveAudioRoomRole.AUDIENCE;
        return config;
    }

}
