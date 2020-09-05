package com.renxl.club.raft.core.store;

import com.renxl.club.raft.core.member.NodeId;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;

/**
 * @Author renxl
 * @Date 2020-08-29 16:59
 * @Version 1.0.0
 */
@AllArgsConstructor
@Accessors(chain = true)
public class NodeStoreImpl implements NodeStore {
    private int term = 0;
    private NodeId votedFor;

    @Override
    public int getTerm() {
        return term;
    }

    @Override
    public void setTerm(int term) {
        this.term = term;
    }

    @Nullable
    @Override
    public NodeId getVotedFor() {
        return votedFor;
    }

    @Override
    public void setVotedFor(NodeId votedFor) {
        this.votedFor = votedFor;
    }
}
