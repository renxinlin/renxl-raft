package com.renxl.club.raft.core;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.renxl.club.raft.config.DefaultConfig;
import com.renxl.club.raft.connector.Connector;
import com.renxl.club.raft.connector.NioConnector;
import com.renxl.club.raft.core.member.Endpoint;
import com.renxl.club.raft.core.member.MemberGroup;
import com.renxl.club.raft.core.member.NodeId;
import com.renxl.club.raft.core.scheduled.DefaultScheduler;
import com.renxl.club.raft.core.scheduled.NodeWorker;
import com.renxl.club.raft.core.scheduled.Scheduler;
import com.renxl.club.raft.core.store.NodeStore;
import com.renxl.club.raft.core.store.NodeStoreImpl;
import com.renxl.club.raft.log.Log;
import lombok.Data;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @Author renxl
 * @Date 2020-08-28 20:42
 * @Version 1.0.0
 */
@Data
public class NodeBuilder {


    /**
     * Group.
     * 集群元信息
     */
    private final MemberGroup group;

    /**
     * Self id.
     */
    private final NodeId selfId;

    /**
     * Event bus, INTERNAL.
     * 总线
     */
    private final EventBus eventBus;

    /**
     * Node configuration.
     * 配置信息
     */
    private DefaultConfig config = new DefaultConfig();

    private NodeStore nodeStore;

    /**
     * Starts as standby or not.
     * <p>
     * 重要: 这个属性在standby启动的时候 作用于catch up阶段
     */
    private boolean standby = false;


    ////////////////////////////////////////////////////////////////////////////
    /**
     * 一致性组件上下文属性构建 start
     */

    private Log log;


    /**
     * Scheduler, INTERNAL.
     */
    private Scheduler scheduler = null;

    /**
     * Connector, component to communicate between nodes, INTERNAL.
     */
    private Connector connector = null;

    /**
     * Task executor for node, INTERNAL.
     */
    private NodeWorker nodeWorker;


    /**
     * 这里的配置来自于 命令行输入  也就是common-cli组件解析的结果
     */
    public NodeBuilder(@Nonnull List<Endpoint> endpoints, @Nonnull NodeId selfId) {
        Preconditions.checkNotNull(endpoints);
        Preconditions.checkNotNull(selfId);
        this.group = new MemberGroup(endpoints, selfId);
        this.selfId = selfId;
        this.eventBus = new EventBus("eventBus:" + selfId.getId());
    }


    /**
     * Set task executor.
     * 测试使用
     *
     * @param nodeWorker task executor
     * @return this
     */
    NodeBuilder setTaskExecutor(@Nonnull NodeWorker nodeWorker) {
        Preconditions.checkNotNull(nodeWorker);
        this.nodeWorker = nodeWorker;
        return this;
    }


    /**
     * Build node.
     *
     * @return node
     */
    @Nonnull
    public Node build() {
        return new NodeImpl(buildContext());
    }

    /**
     * 设置一致性组件的上下文关联的 各种属性
     * Build context for node.
     *
     * @return node context
     */
    @Nonnull
    private NodeContext buildContext() {

        NodeContext context = new NodeContext();
        context.setConfig(config == null ? new DefaultConfig() : config);
        context.setMemberGroup(group);
        context.setSelfId(selfId);


        context.setDefaultScheduler(scheduler == null ? new DefaultScheduler(
                config.getMinElectionTimeout(),
                config.getMaxElectionTimeout(),
                config.getLogReplicationInterval()
        ) : scheduler);
        context.setEventBus(eventBus == null ? new EventBus() : eventBus);


        // TODO 选举完毕 在做日志持久化
        context.setLog(null);
        context.setNodeWorker(new NodeWorker());
        context.setConnector(createNioConnector(context.getConfig().getRaftPort()));
        context.setNodeStore(nodeStore == null ? new NodeStoreImpl(0, null) : nodeStore);
        return context;
    }

    /**
     * 创建rpc组件
     * Create nio connector.
     *
     * @return nio connector
     */
    private Connector createNioConnector(int port) {
        return new NioConnector(eventBus, port);
    }


}
