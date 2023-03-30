package com.zegocloud.uikit.prebuilt.liveaudioroom;


import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoBottomMenuBarConfig;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoDialogInfo;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoInRoomMessageViewConfig;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoLiveAudioRoomLayoutConfig;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoLiveAudioRoomRole;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoLiveAudioRoomSeatConfig;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoMenuBarButtonName;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoInnerText;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoTranslationText;
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
    public ZegoBottomMenuBarConfig bottomMenuBarConfig = new ZegoBottomMenuBarConfig(
        Arrays.asList(ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON, ZegoMenuBarButtonName.SHOW_MEMBER_LIST_BUTTON,
            ZegoMenuBarButtonName.CLOSE_SEAT_BUTTON),
        Arrays.asList(ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON, ZegoMenuBarButtonName.SHOW_MEMBER_LIST_BUTTON),
        Collections.singletonList(ZegoMenuBarButtonName.APPLY_TO_TAKE_SEAT_BUTTON));
    public ZegoDialogInfo confirmDialogInfo;


    /**
     * @deprecated  use {@link ZegoUIKitPrebuiltLiveAudioRoomConfig#innerText} instead
     */
    @Deprecated
    public ZegoTranslationText translationText = new ZegoTranslationText();
    public ZegoInnerText innerText = new ZegoInnerText();
    public ZegoLiveAudioRoomLayoutConfig layoutConfig = new ZegoLiveAudioRoomLayoutConfig();
    public ZegoLiveAudioRoomSeatConfig seatConfig = new ZegoLiveAudioRoomSeatConfig();
    public ZegoInRoomMessageViewConfig inRoomMessageViewConfig = new ZegoInRoomMessageViewConfig();
    public List<Integer> hostSeatIndexes = Collections.singletonList(0);
    public String userAvatarUrl;
    public Map<String, String> userInRoomAttributes;
    public boolean closeSeatsWhenJoin = true;

    public static ZegoUIKitPrebuiltLiveAudioRoomConfig host() {
        ZegoUIKitPrebuiltLiveAudioRoomConfig config = new ZegoUIKitPrebuiltLiveAudioRoomConfig();
        config.role = ZegoLiveAudioRoomRole.HOST;
        config.takeSeatIndexWhenJoining = 0;
        config.turnOnMicrophoneWhenJoining = true;
        config.confirmDialogInfo = new ZegoDialogInfo("Leave the room", "Are you sure to leave the room?", "Cancel",
            "OK");
        return config;
    }

    public static ZegoUIKitPrebuiltLiveAudioRoomConfig audience() {
        ZegoUIKitPrebuiltLiveAudioRoomConfig config = new ZegoUIKitPrebuiltLiveAudioRoomConfig();
        config.role = ZegoLiveAudioRoomRole.AUDIENCE;
        return config;
    }

}
