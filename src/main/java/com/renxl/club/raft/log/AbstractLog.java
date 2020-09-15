package com.renxl.club.raft.log;

import com.google.common.eventbus.EventBus;
import com.renxl.club.raft.core.member.NodeId;
import com.renxl.club.raft.core.message.AppendEntryRequest;
import com.renxl.club.raft.log.entry.Entry;
import com.renxl.club.raft.log.entry.EntryBuilder;
import com.renxl.club.raft.log.sequence.Sequence;
import com.renxl.club.raft.log.snapshot.Snapshot;
import com.renxl.club.raft.log.statemachine.StateMachine;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.renxl.club.raft.log.entry.Entry.KIND_NO_HEART;

/**
 * @Author renxl
 * @Date 2020-08-26 19:18
 * @Version 1.0.0
 */
@Slf4j
public class AbstractLog implements Log {

    protected final EventBus eventBus;

    protected int commitIndex = 0;

    protected StateMachine stateMachine;
    protected Sequence entrySequence;
    private Snapshot snapshot;


    public AbstractLog(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void appendNoop(int term) {
        int nextIndex = entrySequence.getNextLogIndex();

        Entry entry = EntryBuilder.create(KIND_NO_HEART, nextIndex, term, new byte[0]);
        entrySequence.append(entry);


    }

    @Override
    public int getNextIndex() {
        return entrySequence.getNextLogIndex();
    }

    @Override
    public int getCommitIndex() {
        return commitIndex;
    }

    @Override
    public List<Entry> getAppendEntrys(int nextIndex) {
        // todo 添加一个变量来控制每次传输的量 目前是全量


        return null;
    }

    /**
     *
     * @param term leader 当前term
     * @param leaderId leader信息
     * @param nextIndex leader存储的follower nextindex   第一次选举时 leader初始化其为自身的nextindex
     * @return
     */
    @Override
    public AppendEntryRequest createAppendEntries(int term, NodeId leaderId, int nextIndex) {
        int nextLogIndex = entrySequence.getNextLogIndex();
        if (nextIndex > nextLogIndex) {
            throw new IllegalStateException(" member  nextIndex bigger than leader nextIndex ");
        }
        Entry entry = entrySequence.getEntry(nextIndex - 1);
        AppendEntryRequest appendEntryRequest = new AppendEntryRequest(
                term, // 当前term
                leaderId,
                entry.getIndex(),
                entry.getTerm(),
                // 获取需要传输的日志信息
                getAppendEntrys(nextIndex),
                commitIndex,
                null  // channel会在rpc环节补充
        );
        return appendEntryRequest;
    }


}
