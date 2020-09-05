package com.renxl.club.raft.connector.handler;

import com.google.common.eventbus.EventBus;
import com.renxl.club.raft.connector.message.RpcMessage;
import com.renxl.club.raft.core.message.AppendEntryRequest;
import com.renxl.club.raft.core.message.AppendEntryResponse;
import com.renxl.club.raft.core.message.ElectionRequest;
import com.renxl.club.raft.core.message.ElectionResponse;
import com.renxl.club.raft.support.ProtoStuffUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import static com.renxl.club.raft.connector.message.MessageType.*;

/**
 * @Author renxl
 * @Date 2020-08-29 14:30
 * @Version 1.0.0
 */
@Slf4j
public class ProtoBufferEncoder extends MessageToByteEncoder<RpcMessage> {
    private EventBus eventBus;

    public ProtoBufferEncoder(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * @param ctx
     * @param msg
     * @param out
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage msg, ByteBuf out) throws Exception {
        byte[] serialize = ProtoStuffUtil.serialize(msg);
        if(msg instanceof ElectionRequest){
            out.writeByte(ELECTION_REQUEST);
        }
        if(msg instanceof ElectionResponse){
            out.writeByte(ELECTION_RESPONSE);
        }
        if(msg instanceof AppendEntryResponse){
            out.writeByte(APPEND_ENTRY_RESPONSE);
        }
        if(msg instanceof AppendEntryRequest){
            out.writeByte(APPEND_ENTRY_REQUEST);
        }

        out.writeInt(serialize.length);
        out.writeBytes(serialize);
        // TODO 期望 替换掉默认的write   尤其是日志复制的时候 更不能有延迟 否则会导致集群稳定性受到影响 但是refcnt出现问题 跟进给refcnt ++
        log.info("receive out bound [{}] msg....,[{}]",msg.getType(), msg);
//        ctx.writeAndFlush(out);
    }
}
