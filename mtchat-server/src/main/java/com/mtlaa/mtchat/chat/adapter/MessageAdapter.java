package com.mtlaa.mtchat.chat.adapter;

import cn.hutool.core.bean.BeanUtil;
import com.mtlaa.mtchat.strategy.msghandler.AbstractMsgHandler;
import com.mtlaa.mtchat.strategy.msghandler.MsgHandlerFactory;
import com.mtlaa.mtchat.domain.chat.entity.Message;
import com.mtlaa.mtchat.domain.chat.entity.MessageMark;
import com.mtlaa.mtchat.domain.chat.enums.MessageMarkTypeEnum;
import com.mtlaa.mtchat.domain.chat.enums.MessageStatusEnum;
import com.mtlaa.mtchat.domain.chat.enums.MessageTypeEnum;
import com.mtlaa.mtchat.domain.chat.vo.request.ChatMessageReq;
import com.mtlaa.mtchat.domain.chat.vo.request.msg.TextMsgReq;
import com.mtlaa.mtchat.domain.chat.vo.response.ChatMessageResp;
import com.mtlaa.mtchat.domain.common.enums.YesOrNoEnum;


import java.util.*;
import java.util.stream.Collectors;

/**
 * Description: 消息适配器
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-03-26
 */
public class MessageAdapter {
    public static final int CAN_CALLBACK_GAP_COUNT = 100;

    /**
     * 构建消息保存体---只构建一些各种消息类型通用的字段（发送者、房间号、消息类型、状态、创建修改时间）
     * @param request 消息请求
     * @param uid 发送者
     * @return 只包含消息通用信息的消息体
     */
    public static Message buildMsgSave(ChatMessageReq request, Long uid) {
        return Message.builder()
                .fromUid(uid)
                .roomId(request.getRoomId())
                .type(request.getMsgType())
                .status(MessageStatusEnum.NORMAL.getStatus())
                .createTime(new Date())
                .updateTime(new Date())
                .build();
    }

//    public static ChatMessageResp buildMsgRespNoMark(Message message, Long receivedUid) {
//        ChatMessageResp chatMessageResp = new ChatMessageResp();
//        chatMessageResp.setFromUser(buildFromUser(message.getFromUid()));
//        chatMessageResp.setMessage(buildMessageNoMark(message, receivedUid));
//        return chatMessageResp;
//    }
//
//    private static ChatMessageResp.Message buildMessageNoMark(Message message, Long receiveUid) {
//        ChatMessageResp.Message messageVO = new ChatMessageResp.Message();
//        BeanUtil.copyProperties(message, messageVO);
//        messageVO.setSendTime(message.getCreateTime());
//        AbstractMsgHandler<?> msgHandler = MsgHandlerFactory.getStrategyNoNull(message.getType());
//        if (Objects.nonNull(msgHandler)) {
//            messageVO.setBody(msgHandler.showMsg(message));
//        }
//        return messageVO;
//    }
//
//    public static List<ChatMessageResp> buildMsgRespNoMark(List<Message> messages, Long receiveUid) {
//        return messages.stream().map(message -> {
//                    ChatMessageResp resp = new ChatMessageResp();
//                    resp.setFromUser(buildFromUser(message.getFromUid()));
//                    resp.setMessage(buildMessageNoMark(message, receiveUid));
//                    return resp;
//                })
//                .sorted(Comparator.comparing(a -> a.getMessage().getSendTime()))//帮前端排好序，更方便它展示
//                .collect(Collectors.toList());
//    }

    /**
     * 批量构建消息的前端展示体，聚合消息标记（点赞点踩），返回前端用于展示
     * @param messages 消息体列表
     * @param msgMark 消息对应的标记列表
     * @param receiveUid 消息的接受人
     * @return 消息的前端展示体列表
     */
    public static List<ChatMessageResp> buildMsgResp(List<Message> messages, List<MessageMark> msgMark, Long receiveUid) {
        Map<Long, List<MessageMark>> markMap = msgMark.stream().collect(Collectors.groupingBy(MessageMark::getMsgId));
        return messages.stream().map(a -> {
                    ChatMessageResp resp = new ChatMessageResp();
                    resp.setFromUser(buildFromUser(a.getFromUid()));
                    resp.setMessage(buildMessage(a, markMap.getOrDefault(a.getId(), new ArrayList<>()), receiveUid));
                    return resp;
                })
                .sorted(Comparator.comparing(a -> a.getMessage().getSendTime()))//帮前端排好序，更方便它展示
                .collect(Collectors.toList());
    }

    /**
     * 聚合消息对应的消息标记列表，构建当个消息展示体中的消息部分 ChatMessageResp.Message
     * @param message 消息体
     * @param marks 该消息对应的消息标记列表
     * @param receiveUid 消息接收人
     * @return ChatMessageResp.Message
     */
    private static ChatMessageResp.Message buildMessage(Message message, List<MessageMark> marks, Long receiveUid) {
        ChatMessageResp.Message messageVO = new ChatMessageResp.Message();
        BeanUtil.copyProperties(message, messageVO);
        messageVO.setSendTime(message.getCreateTime());
        AbstractMsgHandler<?> msgHandler = MsgHandlerFactory.getStrategyNoNull(message.getType());
        if (Objects.nonNull(msgHandler)) {
            messageVO.setBody(msgHandler.showMsg(message));
        }
        // 设置消息标记
        messageVO.setMessageMark(buildMsgMark(marks, receiveUid));
        return messageVO;
    }

    /**
     * 聚合构建消息对应的消息标记展示体
     * @param marks 消息标记列表
     * @param receiveUid 接收人
     * @return 消息标记展示体 ChatMessageResp.MessageMark
     */
    private static ChatMessageResp.MessageMark buildMsgMark(List<MessageMark> marks, Long receiveUid) {
        Map<Integer, List<MessageMark>> typeMap = marks.stream().collect(Collectors.groupingBy(MessageMark::getType));
        List<MessageMark> likeMarks = typeMap.getOrDefault(MessageMarkTypeEnum.LIKE.getType(), new ArrayList<>());
        List<MessageMark> dislikeMarks = typeMap.getOrDefault(MessageMarkTypeEnum.DISLIKE.getType(), new ArrayList<>());
        ChatMessageResp.MessageMark mark = new ChatMessageResp.MessageMark();
        mark.setLikeCount(likeMarks.size());
        // 设置消息的接收者是否点赞
        mark.setUserLike(Optional.ofNullable(receiveUid)
                .filter(uid -> likeMarks.stream().anyMatch(a -> Objects.equals(a.getUid(), uid)))
                .map(a -> YesOrNoEnum.YES.getStatus()).orElse(YesOrNoEnum.NO.getStatus()));
        mark.setDislikeCount(dislikeMarks.size());
        // 设置消息的接收者是否点踩
        mark.setUserDislike(Optional.ofNullable(receiveUid)
                .filter(uid -> dislikeMarks.stream().anyMatch(a -> Objects.equals(a.getUid(), uid)))
                .map(a -> YesOrNoEnum.YES.getStatus()).orElse(YesOrNoEnum.NO.getStatus()));
        return mark;
    }

    /**
     * 构建消息展示体的用户信息，就是设置一个uid
     * @param fromUid 消息发送者uid
     * @return ChatMessageResp.UserInfo
     */
    private static ChatMessageResp.UserInfo buildFromUser(Long fromUid) {
        ChatMessageResp.UserInfo userInfo = new ChatMessageResp.UserInfo();
        userInfo.setUid(fromUid);
        return userInfo;
    }

    public static ChatMessageReq buildAgreeMsg(Long roomId) {
        ChatMessageReq chatMessageReq = new ChatMessageReq();
        chatMessageReq.setRoomId(roomId);
        chatMessageReq.setMsgType(MessageTypeEnum.TEXT.getType());
        TextMsgReq textMsgReq = new TextMsgReq();
        textMsgReq.setContent("我们已经成为好友了，开始聊天吧");
        chatMessageReq.setBody(textMsgReq);
        return chatMessageReq;
    }


}
