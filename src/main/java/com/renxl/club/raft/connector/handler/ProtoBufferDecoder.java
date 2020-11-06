package com.renxl.club.raft.connector.handler;

import com.google.common.eventbus.EventBus;
import com.renxl.club.raft.core.message.AppendEntryRequest;
import com.renxl.club.raft.core.message.AppendEntryResponse;
import com.renxl.club.raft.core.message.ElectionRequest;
import com.renxl.club.raft.core.message.ElectionResponse;
import com.renxl.club.raft.support.ProtoStuffUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.renxl.club.raft.connector.message.MessageType.*;

/**
 * @Author renxl
 * @Date 2020-08-29 14:30
 * @Version 1.0.0
 */
@Slf4j
public class ProtoBufferDecoder extends ByteToMessageDecoder {
    private EventBus eventBus;

    public ProtoBufferDecoder(EventBus eventBus) {
        this.eventBus = eventBus;
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        //读取type字段 根据type区分消息类型
        byte type = in.readByte();
        int length = in.readInt();
        //读取body
        int size = in.readableBytes();

        byte[] bytes = new byte[size];
        in.readBytes(bytes);

        // 根据type进行序列化
        switch (type) {
            case APPEND_ENTRY_REQUEST:
                AppendEntryRequest appendEntryRequest = ProtoStuffUtil.deserialize(bytes, AppendEntryRequest.class);
                appendEntryRequest.setChannel(ctx.channel());
                log.info("收到消息[日志复制请求] .... [{}]",appendEntryRequest);
                eventBus.post(appendEntryRequest);

                break;
            case APPEND_ENTRY_RESPONSE:
                AppendEntryResponse appendEntryResponse = ProtoStuffUtil.deserialize(bytes, AppendEntryResponse.class);
                log.info("收到消息[日志复制响应] .... [{}]",appendEntryResponse);
                eventBus.post(appendEntryResponse);
                break;
            case ELECTION_REQUEST:
                ElectionRequest electionRequest = ProtoStuffUtil.deserialize(bytes, ElectionRequest.class);
                electionRequest.setChannel(ctx.channel());
                log.info("收到消息[选举请求] .... [{}]",electionRequest);
                eventBus.post(electionRequest);
                break;
            case ELECTION_RESPONSE:
                ElectionResponse electionResponse = ProtoStuffUtil.deserialize(bytes, ElectionResponse.class);
                log.info("收到消息[选举响应] .... [{}]",electionResponse);
                eventBus.post(electionResponse);
                break;
            default:
                log.info(" un expect messgae received ");

        }
    }

}
