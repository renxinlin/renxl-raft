package com.renxl.club.raft.core;

import com.renxl.club.raft.core.role.Role;
import com.renxl.club.raft.log.statemachine.StateMachine;

import javax.annotation.Nonnull;

/**
 * @Author renxl
 * @Date 2020-08-26 10:49
 * @Version 1.0.0
 */
public class NodeImpl implements Node {
    private NodeContext nodeContext;

    private Role role;

    public NodeImpl(NodeContext buildContext) {
        this.nodeContext  = buildContext;
    }

    @Override
    public void start() {
        nodeContext.getConnector().initialize();


    }

    @Override
    public void registerStateMachine(@Nonnull StateMachine stateMachine) {

    }

    @Override
    public void appendLog(@Nonnull byte[] commandBytes) {

    }

    @Override
    public void stop() throws InterruptedException {

    }
}
