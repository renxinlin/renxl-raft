package com.renxl.club.raft.log;

import com.google.common.eventbus.EventBus;
import com.renxl.club.raft.log.sequence.Sequence;
import com.renxl.club.raft.log.snapshot.Snapshot;
import com.renxl.club.raft.log.statemachine.StateMachine;

/**
 * @Author renxl
 * @Date 2020-08-26 19:18
 * @Version 1.0.0
 */
public abstract class AbstractLog implements Log{

    protected final EventBus            eventBus;

    protected       int                 commitIndex = 0;

    protected       StateMachine        stateMachine;

    private         Snapshot            snapshot;

    protected Sequence entrySequence;




    public AbstractLog(EventBus eventBus) {
        this.eventBus = eventBus;
    }
}
