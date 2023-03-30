package com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service;

public class LiveAudioRoomInvitation {

    //    public String invitationID;
    public String invitee;
    public String inviter;
    public String inviteExtendedData;
    private LiveAudioRoomInviteState lastState;
    private LiveAudioRoomInviteState state;
    public int type;

    public String getInvitationID() {
        return inviter + invitee;
    }

    public boolean isFinished() {
        return state != LiveAudioRoomInviteState.SEND_NEW && state != LiveAudioRoomInviteState.RECV_NEW;
    }

    public void setState(LiveAudioRoomInviteState state) {
        lastState = this.state;
        this.state = state;
    }

    public boolean isTakeSeatInvite() {
        return type == LiveAudioRoomInvitationType.INVITE_TO_SEAT.getValue();
    }

    public boolean isTakeSeatRequest() {
        return type == LiveAudioRoomInvitationType.REQUEST_TAKE_SEAT.getValue();
    }

    public static String getInvitationID(String inviter, String invitee) {
        return inviter + invitee;
    }
}
