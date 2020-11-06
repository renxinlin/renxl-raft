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


    public AbstractLog(EventBus eventBus, Sequence fileSequence) {
        this.entrySequence = fileSequence;
        this.eventBus = eventBus;
    }

    @Override
    public void appendNoop(int term) {
        int nextIndex = entrySequence.getNextLogIndex();

        Entry entry = EntryBuilder.create(KIND_NO_HEART, nextIndex, term, new byte[0]);
        entrySequence.appendNoop(entry);


    }

    @Override
    public int getNextIndex() {
        return entrySequence.getNextLogIndex();
    }

    @Override
    public int getCommitIndex() {
        return commitIndex;
    }


    /**
     * @param term      leader 当前term
     * @param leaderId  leader信息
     * @param nextIndex leader存储的follower nextindex   第一次选举时 leader初始化其为自身的nextindex
     * @return
     */
    @Override
    public AppendEntryRequest createAppendEntries(int term, NodeId leaderId, int nextIndex) {
        // 包含缓冲区
        int nextLogIndex = entrySequence.getNextLogIndex();
        if (nextIndex > nextLogIndex) {
            throw new IllegalStateException(" member  nextIndex bigger than leader nextIndex ");
        }
        Entry entry = entrySequence.getEntry(nextIndex - 1);
        AppendEntryRequest appendEntryRequest = new AppendEntryRequest(
                term, // 当前term
                leaderId,
                entry.getIndex(),
                entry.getTerm(), // todo 如果日志分代则entry可能获取不到 这里的index 和term都有问题
                // 获取需要传输的日志信息
                null,// 需要复制的日志
                commitIndex,
                null  // channel会在rpc环节补充
        );
        if (!entrySequence.isEmpty()) {
            // 全量传送未给子节点的日志还是批量 默认全量
            // 这些日志需要发送给follower进行持久化
            appendEntryRequest.setEntries(entrySequence.subList(nextIndex, nextLogIndex));

        }
        return appendEntryRequest;
    }


    /**
     * @param prevLogTerm
     * @param prevLogIndex 来自leader节点存储的nextindex-1   prevLogIndex == entries.get(0).getIndex() - 1
     * @param entries      来自leader的需要提交的日志条目
     * @return
     */
    @Override
    public boolean appendFromLeader(int prevLogTerm, int prevLogIndex, List<Entry> entries) {
        // leader   [1y] [1y] [2x] [2] [3] leader 决定是否创建快照
        // follower [1y] [1y] [x]
        if (!isLastEntryMatched(prevLogTerm, prevLogIndex)) {
            return false;
        }
        // 心跳 等
        if (entries.isEmpty()) {
            // 说明只是leader心跳功能 取消follower的选举期望
            return true;
        }

        // 如果是有k-v命令或者新的选举 需要持久化日志
        //   开始服从leader节点日志 判断是否需要回退
        List<Entry> newEntries = removeUnmatchedLog(entries);
        appendEntriesFromLeader(newEntries);
        return false;
    }

    @Override
    public void commitIndex(int newCommitIndex, int newTerm) {
        // 验证是不是


        if (!validateNewCommitIndex(newCommitIndex, newTerm)) {
            // 正常来说 不可能走到这个逻辑 走到则说明存在问题 因为append成功 意味着前置的index term校验都过了
            log.warn("want to advance commitindex and validate newCommitIndex error ");
            return;
        }
        log.info("advance commit index from {} to {}", commitIndex, newCommitIndex);
        // todo 快照
        entrySequence.commit(newCommitIndex);
        commitIndex = newCommitIndex;
        // todo 推进commitindex的时候需要处理k-v服务
    }

    private boolean validateNewCommitIndex(int newCommitIndex, int newTerm) {

//        if (newCommitIndex <= commitIndex) {
//            return false;
//        }
        Entry entry = entrySequence.getEntry(newCommitIndex);
        if (entry == null) {
            return false;
        }
        if (entry.getTerm() != newTerm) {
            return false;
        }
        return true;
    }

    /**
     * 真正需要追加到缓冲区的日志
     *
     * @param entries
     */
    private void appendEntriesFromLeader(List<Entry> entries) {
        if (entries.isEmpty()) {
            return;
        }

        // 添加到日志条目缓冲区 ，leader节点需要过半提交 follower节点直接提交
        // follower节点直接提交也就造成了 append之前的存在回退的原因 ，可能瞬间leader挂掉 这个条目还没有过半提交 将来新的leader就会对这些双分叉或者单分叉的信息进行回退
        log.info("append entry to  entry buffer  by leader [{}]", entries);
        entrySequence.append(entries);

    }

    private List<Entry> removeUnmatchedLog(List<Entry> entries) {
        /**
         *
         * 根据raft算法 可以知道 follower节点的日志是无条件服从于leader日志 但由于分布式服务的不稳定性 follower节点的日志可能与新的leader日志
         * 从在日志双分叉或者单分叉现象  此时则丢弃分叉点到结尾的follower日志 重新装上leader的日志
         *
         *
         *
         *
         */


        // 先查 后删
        int lastMacthedIndex = -1;
        for (Entry entry : entries) {
            int logIndex = entry.getIndex();
            /**
             * term机制的存在即防止了幽灵复制，这里也防止了不同节点term之差大于2【也就是跨代现象】
             * 数字代表term  数字的下标代表index
             *  leader   1 2 3 3 3 4
             *
             *  follower 1 2 2            这种情况check的时候就通不过 则从prevLogIndex = 4的位置不断回退 回退到index=2 index=1 term =2 匹配上了，但是 index=2,term =2 不匹配leader 需要被leader替代
             *
             *  此时
             *
             *  prevLogTerm = 3
             *  prevLogIndex = 4
             *
             */
            Entry followerEntryMeta = entrySequence.getEntry(logIndex);
            if (followerEntryMeta == null || followerEntryMeta.getTerm() != entry.getTerm()) {
                lastMacthedIndex = logIndex - 1;
            }
        }
        if (lastMacthedIndex == -1) {
            lastMacthedIndex = entries.get(entries.size() - 1).getIndex();
        }


        // 移除 unmatchedIndexStart之后的日志条目 , unmatchedIndexStart本身还是匹配leader的
        removeEntriesAfter(lastMacthedIndex);
        // 从不匹配的节点到结尾需要追加日志

        //leader   follower
        //6 7 8     6 7
        //0 1 2     0 1
        // 将index=8的数据添加上去 | lastMacthedIndex = 7 |  7+1-6 = 2 | 从 下标为2 到结束的数据 append
        // attention: 只需要append follower没有的数据 否则会造成缓冲区重复导致日志序列不正常
        return entries.subList(lastMacthedIndex + 1 - entries.get(0).getIndex(), entries.size());


    }

    /**
     * 如果没有快照和k-v数据 我们直接进行覆盖就行  根本没必要判断
     * 但由于状态数据必须要回滚  所以这里需要remove add  而不是直接覆盖
     *
     * @param lastMatchedIndex
     */
    private void removeEntriesAfter(int lastMatchedIndex) {
        if (entrySequence.isEmpty() || lastMatchedIndex >= entrySequence.getLastLogIndex()) {
            return;
        }

        // 快照处理
        entrySequence.removeAfter(lastMatchedIndex);
        if (lastMatchedIndex < commitIndex) {
            commitIndex = lastMatchedIndex;
        }


    }

    private boolean isLastEntryMatched(int prevLogTerm, int prevLogIndex) {
        Entry entry = entrySequence.getEntry(prevLogIndex);
        // TODO  快照
        if (entry == null) {
            return false;
        }

        if (entry.getTerm() != prevLogTerm) {
            return false;
        }
        return true;
    }


}
