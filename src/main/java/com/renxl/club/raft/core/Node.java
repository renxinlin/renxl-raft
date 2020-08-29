package com.renxl.club.raft.core;

import com.renxl.club.raft.log.statemachine.StateMachine;

import javax.annotation.Nonnull;

/**
 * @Author renxl
 * @Date 2020-08-26 10:49
 * @Version 1.0.0
 */
public interface Node {


    void start();

    void registerStateMachine(@Nonnull StateMachine stateMachine);

    void appendLog(@Nonnull byte[] commandBytes);

    void stop() throws InterruptedException;


}
