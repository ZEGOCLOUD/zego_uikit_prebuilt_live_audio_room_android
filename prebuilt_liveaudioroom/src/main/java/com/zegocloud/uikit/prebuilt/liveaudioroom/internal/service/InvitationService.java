package com.zegocloud.uikit.prebuilt.liveaudioroom.internal.service;

import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.prebuilt.liveaudioroom.core.ZegoInnerText;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InvitationService {

    private Map<String, LiveAudioRoomInvitation> zegoInvitationMap = new HashMap<>();
    private List<OutgoingInvitationListener> outgoingInvitationListenerList = new ArrayList<>();
    private List<IncomingInvitationListener> incomingInvitationListenerList = new ArrayList<>();

    public void clearInvitations() {
        zegoInvitationMap.clear();
    }

    public List<String> getTakeSeatRequestUserIDs() {
        List<String> userIDs = new ArrayList<>();
        for (LiveAudioRoomInvitation invitation : zegoInvitationMap.values()) {
            boolean isTakeSeatRequest = invitation.type == LiveAudioRoomInvitationType.REQUEST_TAKE_SEAT.getValue();
            if (!invitation.isFinished() && isTakeSeatRequest) {
                userIDs.add(invitation.inviter);
            }
        }
        return userIDs;
    }

    public boolean isUserTakeSeatRequestExisted(String userID) {
        boolean existed = false;
        for (LiveAudioRoomInvitation invitation : zegoInvitationMap.values()) {
            boolean isTakeSeatRequest = invitation.type == LiveAudioRoomInvitationType.REQUEST_TAKE_SEAT.getValue();
            boolean isUserRequest = Objects.equals(invitation.inviter, userID);
            if (!invitation.isFinished() && isTakeSeatRequest && isUserRequest) {
                existed = true;
                break;
            }
        }
        return existed;
    }

    public void onInvitationReceived(ZegoUIKitUser inviter, int type, String data) {
        String myUserID = ZegoUIKit.getLocalUser().userID;

        if (type == LiveAudioRoomInvitationType.INVITE_TO_SEAT.getValue()
            || type == LiveAudioRoomInvitationType.REQUEST_TAKE_SEAT.getValue()) {
            if (type == LiveAudioRoomInvitationType.INVITE_TO_SEAT.getValue()) {
                //if i already send request to take seat,and receive invite then cancel request.
                LiveAudioRoomInvitation myInvite = getInvitation(myUserID, inviter.userID);
                if (myInvite != null && myInvite.type == LiveAudioRoomInvitationType.REQUEST_TAKE_SEAT.getValue()) {
                    cancelInvitation(inviter.userID, null);
                }
            }
            LiveAudioRoomInvitation invitation = new LiveAudioRoomInvitation();
            invitation.inviter = inviter.userID;
            invitation.inviteExtendedData = data;
            invitation.invitee = myUserID;
            invitation.type = type;
            invitation.setState(LiveAudioRoomInviteState.RECV_NEW);
            zegoInvitationMap.put(invitation.getInvitationID(), invitation);
        }
        for (IncomingInvitationListener incomingInvitationListener : incomingInvitationListenerList) {
            incomingInvitationListener.onReceiveNewInvitation(inviter, type, data);
        }
    }

    public void onInvitationTimeout(ZegoUIKitUser inviter, String data) {
        ZegoUIKitUser invitee = ZegoUIKit.getLocalUser();
        LiveAudioRoomInvitation liveAudioRoomInvitation = getInvitation(inviter.userID, invitee.userID);
        if (liveAudioRoomInvitation != null) {
            liveAudioRoomInvitation.setState(LiveAudioRoomInviteState.RECV_TIME_OUT);

            for (IncomingInvitationListener inComingInvitationListener : incomingInvitationListenerList) {
                inComingInvitationListener.onReceiveInvitationButResponseTimeout(inviter);
            }
            zegoInvitationMap.remove(liveAudioRoomInvitation.getInvitationID());
        }
    }

    public void onInvitationResponseTimeout(List<ZegoUIKitUser> invitees, String data) {
        LiveAudioRoomInvitation liveAudioRoomInvitation = getInvitation(ZegoUIKit.getLocalUser().userID,
            invitees.get(0).userID);
        if (liveAudioRoomInvitation != null) {
            liveAudioRoomInvitation.setState(LiveAudioRoomInviteState.SEND_TIME_OUT);

            for (OutgoingInvitationListener outgoingInvitationListener : outgoingInvitationListenerList) {
                outgoingInvitationListener.onSendInvitationButReceiveResponseTimeout(invitees.get(0), data);
            }
            zegoInvitationMap.remove(liveAudioRoomInvitation.getInvitationID());
        }
    }

    public void onInvitationAccepted(ZegoUIKitUser invitee, String data) {
        LiveAudioRoomInvitation liveAudioRoomInvitation = getInvitation(ZegoUIKit.getLocalUser().userID,
            invitee.userID);
        if (liveAudioRoomInvitation != null) {
            liveAudioRoomInvitation.setState(LiveAudioRoomInviteState.SEND_IS_ACCEPTED);

            for (OutgoingInvitationListener outgoingInvitationListener : outgoingInvitationListenerList) {
                outgoingInvitationListener.onSendInvitationAndIsAccepted(invitee, data);
            }
            zegoInvitationMap.remove(liveAudioRoomInvitation.getInvitationID());
        }
    }

    public void onInvitationRefused(ZegoUIKitUser invitee, String data) {
        LiveAudioRoomInvitation liveAudioRoomInvitation = getInvitation(ZegoUIKit.getLocalUser().userID,
            invitee.userID);
        if (liveAudioRoomInvitation != null) {
            liveAudioRoomInvitation.setState(LiveAudioRoomInviteState.SEND_IS_REJECTED);
            for (OutgoingInvitationListener outgoingInvitationListener : outgoingInvitationListenerList) {
                outgoingInvitationListener.onSendInvitationButIsRejected(invitee, data);
            }
            zegoInvitationMap.remove(liveAudioRoomInvitation.getInvitationID());
        }
    }

    public void onInvitationCanceled(ZegoUIKitUser inviter, String data) {
        LiveAudioRoomInvitation liveAudioRoomInvitation = getInvitation(inviter.userID,
            ZegoUIKit.getLocalUser().userID);
        if (liveAudioRoomInvitation != null) {
            liveAudioRoomInvitation.setState(LiveAudioRoomInviteState.RECV_IS_CANCELLED);
            for (IncomingInvitationListener invitationListener : incomingInvitationListenerList) {
                invitationListener.onReceiveInvitationButIsCancelled(inviter, data);
            }
            zegoInvitationMap.remove(liveAudioRoomInvitation.getInvitationID());
        }
    }

    public LiveAudioRoomInvitation getInvitation(String inviterID, String inviteeID) {
        String invitationID = LiveAudioRoomInvitation.getInvitationID(inviterID, inviteeID);
        return zegoInvitationMap.get(invitationID);
    }

    public void sendInvitation(String invitee, int timeout, int type, String data,
        PluginCallbackListener callbackListener) {
        sendInvitation(Collections.singletonList(invitee), timeout, type, data, callbackListener);
    }

    public void sendInvitation(List<String> invitees, int timeout, int type, String data,
        PluginCallbackListener callbackListener) {
        if (ZegoUIKit.getLocalUser() == null) {
            return;
        }
        String myUserID = ZegoUIKit.getLocalUser().userID;
        List<String> filterList = new ArrayList<>();
        for (String invitee : invitees) {
            LiveAudioRoomInvitation hisInvitation = getInvitation(invitee, myUserID);
            if (hisInvitation == null || hisInvitation.isFinished()
                || hisInvitation.type != LiveAudioRoomInvitationType.INVITE_TO_SEAT.getValue()) {
                filterList.add(invitee);
            }
        }
        if (filterList.isEmpty()) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", -1);
            if (callbackListener != null) {
                callbackListener.callback(map);
            }
            return;
        }
        ZegoUIKit.getSignalingPlugin().sendInvitation(filterList, timeout, type, data, null, result -> {
            int code = (int) result.get("code");
            if (code == 0) {
                for (String userID : filterList) {
                    LiveAudioRoomInvitation invitation = new LiveAudioRoomInvitation();
                    invitation.inviter = myUserID;
                    invitation.inviteExtendedData = data;
                    invitation.invitee = userID;
                    invitation.type = type;
                    invitation.setState(LiveAudioRoomInviteState.SEND_NEW);
                    zegoInvitationMap.put(invitation.getInvitationID(), invitation);
                }
                ZegoInnerText translationText = LiveAudioRoomManager.getInstance().getInnerText();
                if (translationText != null && translationText.sendRequestTakeSeatToast != null) {
                    result.put("message", translationText.sendRequestTakeSeatToast);
                }

                List<ZegoUIKitUser> errorInvitees = (List<ZegoUIKitUser>) result.get("errorInvitees");
                for (ZegoUIKitUser errorInvitee : errorInvitees) {
                    filterList.remove(errorInvitee.userID);
                }

                // if send finished but find already receive invite,then cancel my invitation
                List<String> needCancelUserIDs = new ArrayList<>();
                for (String userID : filterList) {
                    LiveAudioRoomInvitation hisInvitation = getInvitation(userID, myUserID);
                    if (hisInvitation != null && !hisInvitation.isFinished()
                        && hisInvitation.type == LiveAudioRoomInvitationType.INVITE_TO_SEAT.getValue()) {
                        needCancelUserIDs.add(userID);
                    }
                }
                if (!needCancelUserIDs.isEmpty()) {
                    cancelInvitation(needCancelUserIDs, null);
                }
            }
            if (callbackListener != null) {
                callbackListener.callback(result);
            }
            for (OutgoingInvitationListener outgoingInvitationListener : outgoingInvitationListenerList) {
                outgoingInvitationListener.onActionSendInvitation(invitees, code, (String) result.get("message"),
                    (List<ZegoUIKitUser>) result.get("errorInvitees"));
            }
        });
    }

    public void acceptInvitation(ZegoUIKitUser inviter, PluginCallbackListener callbackListener) {
        if (ZegoUIKit.getLocalUser() == null) {
            return;
        }
        String myUserID = ZegoUIKit.getLocalUser().userID;
        ZegoUIKit.getSignalingPlugin().acceptInvitation(inviter.userID, "", new PluginCallbackListener() {
            @Override
            public void callback(Map<String, Object> result) {
                int code = (int) result.get("code");
                LiveAudioRoomInvitation hisInvitation = getInvitation(inviter.userID, myUserID);
                if (code == 0) {
                    if (hisInvitation != null) {
                        hisInvitation.setState(LiveAudioRoomInviteState.RECV_ACCEPT);
                    }
                }
                if (callbackListener != null) {
                    callbackListener.callback(result);
                }
                for (IncomingInvitationListener incomingInvitationListener : incomingInvitationListenerList) {
                    incomingInvitationListener.onActionAcceptInvitation(inviter, code, (String) result.get("message"));
                }
                if (hisInvitation != null) {
                    zegoInvitationMap.remove(hisInvitation.getInvitationID());
                }
            }
        });
    }

    public void refuseInvitation(ZegoUIKitUser inviter, PluginCallbackListener callbackListener) {
        if (ZegoUIKit.getLocalUser() == null) {
            return;
        }
        String myUserID = ZegoUIKit.getLocalUser().userID;
        ZegoUIKit.getSignalingPlugin().refuseInvitation(inviter.userID, "", new PluginCallbackListener() {
            @Override
            public void callback(Map<String, Object> result) {
                int code = (int) result.get("code");
                LiveAudioRoomInvitation hisInvitation = getInvitation(inviter.userID, myUserID);
                if (code == 0) {
                    if (hisInvitation != null) {
                        hisInvitation.setState(LiveAudioRoomInviteState.RECV_REJECT);
                    }
                }
                if (callbackListener != null) {
                    callbackListener.callback(result);
                }
                for (IncomingInvitationListener incomingInvitationListener : incomingInvitationListenerList) {
                    incomingInvitationListener.onActionRejectInvitation(inviter, code, (String) result.get("message"));
                }
                if (hisInvitation != null) {
                    zegoInvitationMap.remove(hisInvitation.getInvitationID());
                }
            }
        });
    }

    public void cancelInvitation(String invitee, PluginCallbackListener callbackListener) {
        cancelInvitation(Collections.singletonList(invitee), callbackListener);
    }

    public void cancelInvitation(List<String> invitees, PluginCallbackListener callbackListener) {
        if (ZegoUIKit.getLocalUser() == null) {
            return;
        }
        String myUserID = ZegoUIKit.getLocalUser().userID;
        ZegoUIKit.getSignalingPlugin().cancelInvitation(invitees, "", new PluginCallbackListener() {
            @Override
            public void callback(Map<String, Object> result) {
                int code = (int) result.get("code");
                if (code == 0) {
                    for (String invitee : invitees) {
                        LiveAudioRoomInvitation myInvitation = getInvitation(myUserID, invitee);
                        if (myInvitation != null) {
                            myInvitation.setState(LiveAudioRoomInviteState.SEND_CANCEL);
                        }
                    }
                }
                if (callbackListener != null) {
                    callbackListener.callback(result);
                }
                for (OutgoingInvitationListener outgoingInvitationListener : outgoingInvitationListenerList) {
                    outgoingInvitationListener.onActionCancelInvitation(invitees, code, (String) result.get("message"));
                }
                for (String invitee : invitees) {
                    LiveAudioRoomInvitation myInvitation = getInvitation(myUserID, invitee);
                    if (myInvitation != null) {
                        zegoInvitationMap.remove(myInvitation.getInvitationID());
                    }
                }

            }
        });
    }

    public void cancelMyInvitations() {
        for (LiveAudioRoomInvitation audioRoomInvitation : zegoInvitationMap.values()) {
            if (ZegoUIKit.getLocalUser().userID.equals(audioRoomInvitation.inviter)) {
                if (!audioRoomInvitation.isFinished()) {
                    cancelInvitation(audioRoomInvitation.invitee, null);
                }
            }
        }
    }

    public void refuseAllRequest() {
        for (LiveAudioRoomInvitation audioRoomInvitation : zegoInvitationMap.values()) {
            if (ZegoUIKit.getLocalUser().userID.equals(audioRoomInvitation.invitee)) {
                if (!audioRoomInvitation.isFinished()) {
                    refuseInvitation(ZegoUIKit.getUser(audioRoomInvitation.inviter), null);
                }
            }
        }
    }

    public void leaveRoom() {
        cancelMyInvitations();
        clearInvitations();
        outgoingInvitationListenerList.clear();
        incomingInvitationListenerList.clear();
    }

    public void addOutgoingInvitationListener(OutgoingInvitationListener listener) {
        outgoingInvitationListenerList.add(listener);
    }

    public void removeOutgoingInvitationListener(OutgoingInvitationListener listener) {
        outgoingInvitationListenerList.remove(listener);
    }

    public void addIncomingInvitationListener(IncomingInvitationListener listener) {
        incomingInvitationListenerList.add(listener);
    }

    public void removeIncomingInvitationListener(IncomingInvitationListener listener) {
        incomingInvitationListenerList.remove(listener);
    }
}
