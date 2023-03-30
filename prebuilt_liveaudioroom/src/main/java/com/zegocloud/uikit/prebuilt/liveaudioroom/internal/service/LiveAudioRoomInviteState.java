package com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service;

public enum LiveAudioRoomInviteState {
    /**
     * send new invitation
     */
    SEND_NEW,
    /**
     * send invitation and cancelled by self
     */
    SEND_CANCEL,
    /**
     * send invitation and is accepted by other
     */
    SEND_IS_ACCEPTED,
    /**
     * send invitation and is rejected by other
     */
    SEND_IS_REJECTED,
    /**
     * send invitation but no response from other
     */
    SEND_TIME_OUT,
    /**
     * receive new invitation
     */
    RECV_NEW,
    /**
     * receive a invitation and is cancelled by inviter
     */
    RECV_IS_CANCELLED,
    /**
     * receive a invitation and is accept by self
     */
    RECV_ACCEPT,
    /**
     * receive a invitation and is rejected by self
     */
    RECV_REJECT,
    /**
     * receive invitation but no reply
     */
    RECV_TIME_OUT
}
