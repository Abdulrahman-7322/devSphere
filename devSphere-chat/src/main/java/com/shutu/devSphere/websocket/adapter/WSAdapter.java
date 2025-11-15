package com.shutu.devSphere.websocket.adapter;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shutu.commons.security.cache.TokenStoreCache;
import com.shutu.commons.security.user.UserDetail;
import com.shutu.commons.tools.utils.Result;
import com.shutu.devSphere.model.dto.ws.GroupMessageDTO;
import com.shutu.devSphere.model.dto.ws.PrivateMessageDTO;
import com.shutu.devSphere.model.entity.Message;
import com.shutu.devSphere.model.entity.RoomFriend;
import com.shutu.devSphere.model.enums.ws.WSReqTypeEnum;
import com.shutu.devSphere.model.vo.ws.response.ChatMessageResp;
import com.shutu.devSphere.model.vo.ws.response.WSBaseResp;
import com.shutu.devSphere.service.RoomFriendService;
import com.shutu.feign.UserFeignClient;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Date;


/**
 * ws适配器
 * 将后端接收到的私聊 / 群聊消息封装成统一的响应对象 WSBaseResp<ChatMessageResp> 发送给前端。
 */
@Component
public class WSAdapter {
    @Resource
    private RoomFriendService roomFriendService;
    @Resource
    private UserFeignClient userFeignClient;


    public TokenStoreCache getTokenStoreCache() {
        return SpringUtil.getBean(TokenStoreCache.class);
    }

    /**
     * 构建私聊消息响应对象
     * @param privateMessageDTO
     * @return
     */
    public WSBaseResp<ChatMessageResp> buildPrivateMessageResp(PrivateMessageDTO privateMessageDTO) {
        Long loginUserId = privateMessageDTO.getFromUserId();
        Message message = new Message();
        message.setFromUid(loginUserId);// 设置信息的发送者
        message.setContent(privateMessageDTO.getContent());
        message.setCreateTime(new Date());
        ChatMessageResp chatMessageResp = getMessageVo(message);

        // 创建WSBaseResp对象
        WSBaseResp<ChatMessageResp> wsBaseResp = new WSBaseResp<>();
        // 设置房间ID （好友私聊房间）
        Long toUserId = privateMessageDTO.getToUserId();
        long uid1 = loginUserId > toUserId ? toUserId : loginUserId;
        long uid2 = loginUserId > toUserId ? loginUserId : toUserId;
        RoomFriend roomFriend = roomFriendService.getOne(new LambdaQueryWrapper<RoomFriend>()
                .eq(RoomFriend::getUid1, uid1).eq(RoomFriend::getUid2, uid2));
        if (roomFriend != null) {
            chatMessageResp.setRoomId(roomFriend.getRoomId());
        }
        // 设置数据和类型
        wsBaseResp.setData(chatMessageResp);
        wsBaseResp.setType(WSReqTypeEnum.CHAT.getType());
        return wsBaseResp;
    }

    /**
     * 构建群聊消息响应对象
     * @param groupMessageDTO
     * @return
     */
    public WSBaseResp<ChatMessageResp> buildGroupMessageResp(GroupMessageDTO groupMessageDTO) {
        Message message = new Message();
        message.setRoomId(groupMessageDTO.getToRoomId());
        message.setFromUid(groupMessageDTO.getFromUserId());// 设置信息的发送者
        message.setContent(groupMessageDTO.getContent());
        message.setCreateTime(new Date());
        ChatMessageResp chatMessageResp = getMessageVo(message);

        // 创建WSBaseResp对象
        WSBaseResp<ChatMessageResp> wsBaseResp = new WSBaseResp<>();
        // 设置房间ID
        chatMessageResp.setRoomId(groupMessageDTO.getToRoomId());
        // 设置数据和类型
        wsBaseResp.setData(chatMessageResp);
        wsBaseResp.setType(WSReqTypeEnum.CHAT.getType());
        return wsBaseResp;
    }

    /**
     * 构建消息内容及用户信息结构体
     * @param UserId 发送消息的用户id
     * @param content 消息内容
     * @return ChatMessageResp
     */
    @NotNull
    public ChatMessageResp getMessageVo(Message message) {
        // 创建ChatMessageResp对象
        ChatMessageResp chatMessageResp = new ChatMessageResp();
        // 获取登录用户的信息
        Result<UserDetail> result = userFeignClient.getById(message.getFromUid());
        UserDetail user = result.getData();
        // 创建UserInfo对象
        ChatMessageResp.UserInfo userInfo = new ChatMessageResp.UserInfo();
        // 设置用户名、ID和头像
        userInfo.setUsername(user.getUsername());
        userInfo.setUid(user.getId());
        userInfo.setAvatar(user.getHeadUrl());
        // 和发送者信息
        chatMessageResp.setFromUser(userInfo);
        // 创建Message对象
        ChatMessageResp.Message messageVO = new ChatMessageResp.Message();
        // 设置私信内容
        messageVO.setContent(message.getContent());
        messageVO.setSendTime(message.getCreateTime());
        // 设置消息对象
        chatMessageResp.setMessage(messageVO);

        return chatMessageResp;
    }


}
