package com.renxl.club.raft.connector;

import com.google.common.eventbus.EventBus;
import com.renxl.club.raft.connector.handler.ProtoBufferDecoder;
import com.renxl.club.raft.connector.handler.ProtoBufferEncoder;
import com.renxl.club.raft.core.member.Address;
import com.renxl.club.raft.core.member.Endpoint;
import com.renxl.club.raft.core.member.NodeId;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

import static com.renxl.club.raft.connector.message.LengthFieldBasedFrameDecoderConfig.*;

/**
 * @Author renxl
 * @Date 2020-08-06 16:13
 * @Version 1.0.0
 */
@Slf4j
public class DefaultChannelGroup {
    private static ConcurrentHashMap<NodeId, Channel> channels = new ConcurrentHashMap();
    private EventBus eventBus;
    private EventLoopGroup workerGroup = new NioEventLoopGroup(8);

    public DefaultChannelGroup(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public static void add(NodeId nodeId, Channel channel) {

        channels.put(nodeId, channel);

    }

    public static Channel get(NodeId nodeId) {
        Channel channel = channels.get(nodeId);
        return channel;


    }

    public Channel remove(NodeId nodeId) {
        Channel channel = get(nodeId);
        if (channel != null) {
            channels.remove(nodeId);
        }
        return channel;
    }

    public Channel getChannel(Endpoint endpoint) {
        Channel channel = channels.get(endpoint.getNodeId());
        if (channel == null) {
            return connect(endpoint.getAddress(),endpoint.getNodeId());
        }
        return channel;
    }

    @SneakyThrows
    private Channel connect(Address address, NodeId nodeId) {
        Bootstrap bootstrap = new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                // 防止小包延迟
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();

                        pipeline.addLast(new ProtoBufferEncoder(eventBus));
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP, false));
                        pipeline.addLast(new ProtoBufferDecoder(eventBus));
                    }
                });
        ChannelFuture future = bootstrap.connect(address.getIp(), address.getPort()).sync();
        if (!future.isSuccess()) {
            throw new ChannelException("failed to connect", future.cause());
        }
        Channel nettyChannel = future.channel();
        channels.put(nodeId,nettyChannel);
        nettyChannel.closeFuture().addListener((ChannelFutureListener) cf -> {
            channels.remove(nodeId);
        });
        return nettyChannel;
    }


}
