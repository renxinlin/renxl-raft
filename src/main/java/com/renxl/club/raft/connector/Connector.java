package com.renxl.club.raft.connector;

import com.renxl.club.raft.core.member.Endpoint;
import com.renxl.club.raft.core.message.AppendEntryRequest;
import com.renxl.club.raft.core.message.ElectionRequest;

import java.util.List;

/**
 *
 * 设计实现主要为nioconnector
 * 封装通信功能
 * 与一致性组件进行通信
 * Connector.
 */
public interface Connector {


    void initialize();

    void close();

    void sendElectionRequest(ElectionRequest electionRequest, List<Endpoint> endpoints);

    void sendAppendEntryRequest(AppendEntryRequest appendEntryRequest,Endpoint endpoint);

}
