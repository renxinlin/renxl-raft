package com.renxl.club.raft.core;

import com.renxl.club.raft.core.member.NodeId;

import javax.annotation.Nullable;

/**
 * todo 持久化这份数据
 */
public interface NodeStore {

    int getTerm();

    void setTerm(int term);

    @Nullable
    NodeId getVotedFor();

    void setVotedFor(@Nullable NodeId votedFor);



}
