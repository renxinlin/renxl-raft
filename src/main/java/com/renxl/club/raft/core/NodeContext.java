package com.renxl.club.raft.core;

import com.google.common.eventbus.EventBus;
import com.renxl.club.raft.config.DefaultConfig;
import com.renxl.club.raft.connector.Connector;
import com.renxl.club.raft.core.member.MemberGroup;
import com.renxl.club.raft.core.member.NodeId;
import com.renxl.club.raft.core.scheduled.Scheduler;
import com.renxl.club.raft.log.Log;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author renxl
 * @Date 2020-08-26 19:06
 * @Version 1.0.0
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
public class NodeContext {
    // 集群元信息
    private MemberGroup memberGroup;
    private DefaultConfig config;
    private NodeId selfId;




    // 组件

    private Scheduler defaultScheduler;

    private Connector connector;

    private Log log;

    // 总线

    private EventBus eventBus;

    // 工作线程 采用多线程+ 同步工具包
    private NodeWorker nodeWorker;

}
