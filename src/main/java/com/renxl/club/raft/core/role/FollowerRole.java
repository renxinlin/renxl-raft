package com.renxl.club.raft.core.role;

import com.renxl.club.raft.core.member.NodeId;
import com.renxl.club.raft.core.scheduled.ElectionTaskFuture;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author renxl
 * @Date 2020-08-29 13:49
 * @Version 1.0.0
 */

@Data
@EqualsAndHashCode
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor

public class FollowerRole implements Role{

    private RoleEnum name;
    private int term;
    private  ElectionTaskFuture electionTaskFuture;

    // 当前leader id  有可能为null
    private NodeId leaderId;

    //  投过票的节点 有可能为null
    private NodeId votedFor;


    @Override
    public boolean cancelLogOrElection() {
        return electionTaskFuture.cancel(false);
    }
}
