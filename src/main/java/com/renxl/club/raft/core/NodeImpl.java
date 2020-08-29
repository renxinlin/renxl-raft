package com.renxl.club.raft.core;

import com.renxl.club.raft.core.role.FollowerRole;
import com.renxl.club.raft.core.role.Role;
import com.renxl.club.raft.core.role.RoleEnum;
import com.renxl.club.raft.core.scheduled.ElectionTaskFuture;
import com.renxl.club.raft.log.statemachine.StateMachine;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;

/**
 * @Author renxl
 * @Date 2020-08-26 10:49
 * @Version 1.0.0
 */
@Slf4j
public class NodeImpl implements Node {
    private NodeContext nodeContext;

    private Role role;

    public NodeImpl(NodeContext buildContext) {
        this.nodeContext = buildContext;
    }

    @Override
    public void start() {
        nodeContext.getConnector().initialize();
        becomeFollower(new FollowerRole(RoleEnum.FOLLOWER, nodeContext.getNodeStore().getTerm(), startElectionDelayTask(), null, null));


    }

    private void becomeFollower(FollowerRole followerRole) {
        if (true) {
            // 将必要的变更持久化
            log.info("");
        }

        role = followerRole;


    }

    private ElectionTaskFuture startElectionDelayTask() {
        return nodeContext.getDefaultScheduler().electionTask(this::electionTask);
    }

    private void electionTask() {
        /**
         * 选举核心:
         */



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
