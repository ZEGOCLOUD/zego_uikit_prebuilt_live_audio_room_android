package com.zegocloud.uikit.prebuilt.liveaudioroom.core;

public class ZegoTranslationText {

    public String prebuiltTitle = "Live Audio Room";
    public String removeSpeakerMenuDialogButton = "Remove %s from seat";
    public String takeSeatMenuDialogButton = "Take the seat";
    public String leaveSeatMenuDialogButton = "Leave the seat";
    public String cancelMenuDialogButton = "Cancel";
    public String memberListTitle = "Attendance";
    public String removeSpeakerFailedToast = "Failed to remove %s from seat";

    public ZegoDialogInfo leaveSeatDialogInfo = new ZegoDialogInfo("Leave the seat", "Are you sure to leave the seat?",
        "Cancel", "OK");
    public ZegoDialogInfo removeSpeakerFromSeatDialogInfo = new ZegoDialogInfo("Remove the speaker",
        "Are you sure to remove %s from seat?", "Cancel", "OK");

}
