package com.zegocloud.uikit.prebuilt.liveaudioroom.core;

public class LanguageTextCHS extends LanguageBaseText {

    public LanguageTextCHS() {
        removeSpeakerMenuDialogButton = "将 %s 移下麦位";
        muteSpeakerMenuDialogButton = "静音 %s";
        takeSeatMenuDialogButton = "上麦";
        leaveSeatMenuDialogButton = "下麦";
        cancelMenuDialogButton = "取消";
        memberListTitle = "听众";
        removeSpeakerFailedToast = "无法将 %s 移下麦位";
        applyToTakeSeatButton = "申请上麦";
        cancelTheTakeSeatApplicationButton = "取消";
        memberListAgreeButton = "同意";
        memberListDisagreeButton = "不同意";
        inviteToTakeSeatMenuDialogButton = "邀请 %s 上麦";
        sendRequestTakeSeatToast = "您正在申请上麦，请等待确认。";

        you = "您";
        speaker = "连麦中";
        host = "房主";

        explainMic = "需要麦克风访问权限才能开始直播";
        settingsMic = "请前往系统设置允许访问麦克风。";
        ok = "确定";
        cancel = "取消";
        settings = "设置";
        confirm = "确认";

        leaveSeatDialogInfo = new ZegoDialogInfo("下麦", "您确定要下麦吗？",
            "取消", "确定");
        removeSpeakerFromSeatDialogInfo = new ZegoDialogInfo("从麦位上移除",
            "您确定要将 %s 从麦位上移除吗？", "取消", "确定");
        receivedCoHostInvitationDialogInfo = new ZegoDialogInfo("邀请",
            "房主邀请您上麦", "不同意", "同意");
        leaveRoomConfirmDialogInfo = new ZegoDialogInfo("离开房间",
            "您确定要离开房间吗", "取消", "确认");
    }
}
