package com.zegocloud.uikit.prebuilt.liveaudioroom.core;

public class ZegoInnerText {

    public String removeSpeakerMenuDialogButton = "Remove %s from seat";
    public String muteSpeakerMenuDialogButton = "Mute %s";
    public String takeSeatMenuDialogButton = "Take the seat";
    public String leaveSeatMenuDialogButton = "Leave the seat";
    public String cancelMenuDialogButton = "Cancel";
    public String memberListTitle = "Audience";
    public String removeSpeakerFailedToast = "Failed to remove %s from seat";

    public ZegoDialogInfo leaveSeatDialogInfo = new ZegoDialogInfo("Leave the seat", "Are you sure to leave the seat?",
        "Cancel", "OK");
    public ZegoDialogInfo removeSpeakerFromSeatDialogInfo = new ZegoDialogInfo("Remove the speaker",
        "Are you sure to remove %s from the seat?", "Cancel", "OK");


    public String applyToTakeSeatButton = "Apply to take seat";
    public String cancelTheTakeSeatApplicationButton = "Cancel";
    public String memberListAgreeButton = "Agree";
    public String memberListDisagreeButton = "Disagree";
    public String inviteToTakeSeatMenuDialogButton = "Invite %s to take seat";
    public String sendRequestTakeSeatToast = "You are applying to take a seat, please wait for confirmation.";
    public ZegoDialogInfo receivedCoHostInvitationDialogInfo = new ZegoDialogInfo("Invitation",
        "The host is inviting you to take seat", "Disagree", "Agree");
}
