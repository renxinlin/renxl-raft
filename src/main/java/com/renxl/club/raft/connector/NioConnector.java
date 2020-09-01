package com.renxl.club.raft.connector;


import com.google.common.eventbus.EventBus;
import com.renxl.club.raft.connector.handler.ProtoBufferDecoder;
import com.renxl.club.raft.connector.handler.ProtoBufferEncoder;
import com.renxl.club.raft.core.member.Endpoint;
import com.renxl.club.raft.core.message.AppendEntryRequest;
import com.renxl.club.raft.core.message.ElectionRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.ThreadSafe;
import java.util.List;

import static com.renxl.club.raft.connector.message.LengthFieldBasedFrameDecoderConfig.*;

/**
 * 处理 选举 日志 -压缩-》 快照
 * @author mac
 */
@ThreadSafe
@Slf4j
public class NioConnector implements Connector {


    /**
     * Connector
     * <p>
     * NioConnector [workgroup ]
     * <p>
     * <p>
     * inboundChannelGroup[通过nodeId addchannel]     outboundChannelGroup[生成时候 addchannel]
     * <p>
     * encoder decoder                                encoder decoder
     * FromRemoteHandler                            ToRemoteHandler
     * <p>
     * AbstractHandler[from和to都是交给抽象类处理，from自己处理添加到group的nodeid消息]
     */


    private final NioEventLoopGroup bossNioEventLoopGroup = new NioEventLoopGroup(1);

    // 有可能从NOdeBuilder外部传入从而作为共享线程 目前是不共享的，线程数量从配置中获取
    private final NioEventLoopGroup workerNioEventLoopGroup;
    private final EventBus eventBus;
    private final int port;

    /**
     * 为什么要分成两个
     * 设计成双向的目的如下:
     * 假设按照节点id字典顺序建立连接
     * <p>
     * 在xraft中
     * 作者channel的建立实在发送消息的时候
     * 而不是在初始化的时候[初始化的时候只是建立了socketserver的监听]
     * 初始化的时候其他节点不一定启动完毕，所以延迟也有一定的道理
     * <p>
     * 既然在延迟的时候建立连接就存在以下问题
     * 假设按字典顺序A->B->C建立三角矩阵连接
     * <p>
     * <p>
     * 则B这个时候成为候选者,B向A发送消息的时候则会出现无法获取连接的现象
     * <p>
     * 为解决这个问题 这里临时建立逆三角矩阵连接
     * 但这样就出现了重复连接
     * <p>
     * raft在稳定的时候消息的发起方应该都是leader
     * 所以这时候在成为leader的时候在关闭 逆三角矩阵的连接
     * <p>
     * =================
     * 假设a b c 三个节点
     * 但是存在以下弊端
     * a b 两个节点同时成为候选者
     * <p>
     * a b 同时广播选举消息
     * <p>
     * a b  c 连接如下
     * <p>
     * a先广播
     * a       b         c
     * a[in]     a[in]
     * b[out]
     * <p>
     * c[out]
     * <p>
     * b广播
     * <p>
     * a       b         c
     * b[out]  a[in]
     * c[out]          a[in]
     * c[in]           a[out]
     * c[in]   b[out]
     * <p>
     * <p>
     * =================
     * 假设此时 c 成为leader
     * 则关闭a[in]
     * <p>
     * a       b         c
     * b[out]  a[in]
     * c[in]           a[out]
     * c[in]   b[out]
     * <p>
     * 但是实际上b[out]  a[in]这对连接只有在重新选举的时候才会被用到 此时资源处于闲置浪费状态
     * <p>
     * ！！！！！！！！！！！
     * 解决方案心跳关闭
     * 字典排序的连接实际上是个三角矩阵
     * 按照之前作者的设计 在getC
     * <p>
     * <p>
     * <p>
     * 备注 三角矩阵结构
     * a b  c  d
     * a   ~  ~ ~
     * b 。   ~ ~
     * c 。。   ~
     * d 。。 。
     */
    private DefaultChannelGroup outchannels;


    public NioConnector( EventBus eventBus, int port) {
        // 0表示采用系统配置

        this.workerNioEventLoopGroup = new NioEventLoopGroup();
        this.eventBus = eventBus;
        this.port = port;
        outchannels = new DefaultChannelGroup(eventBus);


    }


    @SneakyThrows
    @Override
    public void initialize() {
        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(bossNioEventLoopGroup, workerNioEventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new ProtoBufferEncoder(eventBus));
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP, false));
                        pipeline.addLast(new ProtoBufferDecoder(eventBus));
                    }
                });
        log.debug("node listen on port {}", port);
        serverBootstrap.bind(port).sync();
    }

    @Override
    public void close() {
        log.debug("close connector");
        bossNioEventLoopGroup.shutdownGracefully();
        workerNioEventLoopGroup.shutdownGracefully();
    }


    /////////////////////////////////////////////////
    //
    //
    //  以下为消息发送部分
    //
    //
    /////////////////////////////////////////////////


    @Override
    public void sendElectionRequest(ElectionRequest electionRequest, List<Endpoint> endpoints) {
        for (Endpoint endpoint:endpoints) {
            ChannelFuture channelFuture = outchannels.getChannel(endpoint).writeAndFlush(electionRequest);
            channelFuture.addListener(listener->{
                if (!listener.isSuccess()){
                    log.info("send election error");
                }
            });
        }
    }

    @Override
    public void sendAppendEntryRequest(AppendEntryRequest appendEntryRequest, List<Endpoint> endpoints) {
        for (Endpoint endpoint:endpoints) {
            ChannelFuture channelFuture = outchannels.getChannel(endpoint).writeAndFlush(appendEntryRequest);
            channelFuture.addListener(listener->{
                if (!listener.isSuccess()){
                    log.info("send election error");
                }
            });
        }
    }


}
