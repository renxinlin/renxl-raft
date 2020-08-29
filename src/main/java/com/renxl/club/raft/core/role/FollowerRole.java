package com.renxl.club.raft.core.role;

import com.renxl.club.raft.core.member.NodeId;
import com.renxl.club.raft.core.scheduled.ElectionTaskFuture;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Author renxl
 * @Date 2020-08-29 13:49
 * @Version 1.0.0
 */

@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class FollowerRole {

    private final RoleEnum name;
    private final int term;
    private  ElectionTaskFuture electionTaskFuture;

    // 当前leader id  有可能为null
    private NodeId leaderId;

    //  投过票的节点 有可能为null
    private final NodeId votedFor;



    public FollowerRole(RoleEnum name, int term, ElectionTaskFuture electionTaskFuture, NodeId leaderId, NodeId votedFor) {
        this.name = name;
        this.term = term;
        this.electionTaskFuture = electionTaskFuture;
        this.leaderId = leaderId;
        this.votedFor = votedFor;
    }
}
