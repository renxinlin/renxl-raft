package com.renxl.club.raft.connector;

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
}
