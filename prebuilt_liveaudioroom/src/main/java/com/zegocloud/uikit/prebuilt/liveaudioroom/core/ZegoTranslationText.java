package com.zegocloud.uikit.prebuilt.liveaudioroom.core;

import com.zegocloud.uikit.internal.ZegoUIKitLanguage;

public class ZegoTranslationText {

    public String removeSpeakerMenuDialogButton;
    public String muteSpeakerMenuDialogButton;
    public String takeSeatMenuDialogButton;
    public String leaveSeatMenuDialogButton;
    public String cancelMenuDialogButton;
    public String memberListTitle;
    public String removeSpeakerFailedToast;

    public String applyToTakeSeatButton;
    public String cancelTheTakeSeatApplicationButton;
    public String memberListAgreeButton;
    public String memberListDisagreeButton;
    public String inviteToTakeSeatMenuDialogButton;
    public String sendRequestTakeSeatToast;

    public ZegoDialogInfo leaveSeatDialogInfo;
    public ZegoDialogInfo removeSpeakerFromSeatDialogInfo;
    public ZegoDialogInfo receivedCoHostInvitationDialogInfo;
    public ZegoDialogInfo leaveRoomConfirmDialogInfo;

    public String you;
    public String speaker;
    public String host;
    public String explainMic;
    public String settingsMic;
    public String ok;
    public String cancel;
    public String settings;
    public String confirm;

    private LanguageBaseText languageText = new LanguageTextEnglish();

    public LanguageBaseText getLanguageText() {
        return languageText;
    }

    public ZegoTranslationText() {
        this(ZegoUIKitLanguage.ENGLISH);
    }


    public ZegoTranslationText(ZegoUIKitLanguage language) {
        if (language == ZegoUIKitLanguage.CHS) {
            languageText = new LanguageTextCHS();
        }

        removeSpeakerMenuDialogButton = languageText.removeSpeakerMenuDialogButton;
        muteSpeakerMenuDialogButton = languageText.muteSpeakerMenuDialogButton;
        takeSeatMenuDialogButton = languageText.takeSeatMenuDialogButton;
        leaveSeatMenuDialogButton = languageText.leaveSeatMenuDialogButton;
        cancelMenuDialogButton = languageText.cancelMenuDialogButton;
        memberListTitle = languageText.memberListTitle;
        removeSpeakerFailedToast = languageText.removeSpeakerFailedToast;

        applyToTakeSeatButton = languageText.applyToTakeSeatButton;
        cancelTheTakeSeatApplicationButton = languageText.cancelTheTakeSeatApplicationButton;
        memberListAgreeButton = languageText.memberListAgreeButton;
        memberListDisagreeButton = languageText.memberListDisagreeButton;
        inviteToTakeSeatMenuDialogButton = languageText.inviteToTakeSeatMenuDialogButton;
        sendRequestTakeSeatToast = languageText.sendRequestTakeSeatToast;

        leaveSeatDialogInfo = languageText.leaveSeatDialogInfo;
        removeSpeakerFromSeatDialogInfo = languageText.removeSpeakerFromSeatDialogInfo;
        receivedCoHostInvitationDialogInfo = languageText.receivedCoHostInvitationDialogInfo;
        leaveRoomConfirmDialogInfo = languageText.leaveRoomConfirmDialogInfo;

        you = languageText.you;
        speaker = languageText.speaker;
        host = languageText.host;
        explainMic = languageText.explainMic;
        settingsMic = languageText.settingsMic;
        ok = languageText.ok;
        cancel = languageText.cancel;
        settings = languageText.settings;
        confirm = languageText.confirm;
    }

    /**
     * if user not set custom translationText，then copy from innerText. Only for English.
     *
     * @param innerText
     */
    public void copyFromInnerTextIfNotCustomized(ZegoInnerText innerText) {
        if (innerText == null) {
            // innerText is null, no need to copy from
            return;
        }
        if (languageText instanceof LanguageTextEnglish) {
            LanguageTextEnglish english = new LanguageTextEnglish();
            if (english.removeSpeakerMenuDialogButton.equals(removeSpeakerMenuDialogButton)) { // not changed
                removeSpeakerMenuDialogButton = innerText.removeSpeakerMenuDialogButton;
            }
            if (english.muteSpeakerMenuDialogButton.equals(muteSpeakerMenuDialogButton)) {
                muteSpeakerMenuDialogButton = innerText.muteSpeakerMenuDialogButton;
            }
            if (english.takeSeatMenuDialogButton.equals(takeSeatMenuDialogButton)) {
                takeSeatMenuDialogButton = innerText.takeSeatMenuDialogButton;
            }
            if (english.leaveSeatMenuDialogButton.equals(leaveSeatMenuDialogButton)) {
                leaveSeatMenuDialogButton = innerText.leaveSeatMenuDialogButton;
            }
            if (english.cancelMenuDialogButton.equals(cancelMenuDialogButton)) {
                cancelMenuDialogButton = innerText.cancelMenuDialogButton;
            }
            if (english.memberListTitle.equals(memberListTitle)) {
                memberListTitle = innerText.memberListTitle;
            }
            if (english.removeSpeakerFailedToast.equals(removeSpeakerFailedToast)) {
                removeSpeakerFailedToast = innerText.removeSpeakerFailedToast;
            }
            if (english.applyToTakeSeatButton.equals(applyToTakeSeatButton)) {
                applyToTakeSeatButton = innerText.applyToTakeSeatButton;
            }
            if (english.cancelTheTakeSeatApplicationButton.equals(cancelTheTakeSeatApplicationButton)) {
                cancelTheTakeSeatApplicationButton = innerText.cancelTheTakeSeatApplicationButton;
            }
            if (english.memberListAgreeButton.equals(memberListAgreeButton)) {
                memberListAgreeButton = innerText.memberListAgreeButton;
            }
            if (english.memberListDisagreeButton.equals(memberListDisagreeButton)) {
                memberListDisagreeButton = innerText.memberListDisagreeButton;
            }
            if (english.inviteToTakeSeatMenuDialogButton.equals(inviteToTakeSeatMenuDialogButton)) {
                inviteToTakeSeatMenuDialogButton = innerText.inviteToTakeSeatMenuDialogButton;
            }
            if (english.sendRequestTakeSeatToast.equals(sendRequestTakeSeatToast)) {
                sendRequestTakeSeatToast = innerText.sendRequestTakeSeatToast;
            }
        }
    }

    public ZegoUIKitLanguage getLanguage() {
        if (languageText instanceof LanguageTextCHS) {
            return ZegoUIKitLanguage.CHS;
        }
        return ZegoUIKitLanguage.ENGLISH;
    }
}
