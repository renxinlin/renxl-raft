package com.renxl.club.raft.core.role;

import com.renxl.club.raft.core.member.NodeId;
import com.renxl.club.raft.core.scheduled.ElectionTaskFuture;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @Author renxl
 * @Date 2020-08-29 13:49
 * @Version 1.0.0
 */

@EqualsAndHashCode
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FollowerRole implements Role{

    private RoleEnum name;
    private int term;
    private  ElectionTaskFuture electionTaskFuture;

    // 当前leader id 有可能为null
    private NodeId leaderId;

    //  投过票的节点 有可能为null 当选举的时候 候选者会决定是否votedFor somebody
    private NodeId votedFor;

    public RoleEnum getName() {
        return name;
    }

    public void setName(RoleEnum name) {
        this.name = name;
    }

    public ElectionTaskFuture getElectionTaskFuture() {
        return electionTaskFuture;
    }

    public void setElectionTaskFuture(ElectionTaskFuture electionTaskFuture) {
        this.electionTaskFuture = electionTaskFuture;
    }

    public NodeId getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(NodeId leaderId) {
        this.leaderId = leaderId;
    }

    public NodeId getVotedFor() {
        return votedFor;
    }

    public void setVotedFor(NodeId votedFor) {
        this.votedFor = votedFor;
    }

    @Override
    public int getTerm() {
        return 0;
    }

    @Override
    public void setTerm(int term) {

    }

    @Override
    public boolean cancelLogOrElection() {
        return electionTaskFuture.cancel(false);
    }



}
