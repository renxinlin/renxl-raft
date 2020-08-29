package com.renxl.club.raft.core.role;

import com.renxl.club.raft.core.scheduled.LogReplicationFuture;
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
public class LeaderRole {


    private LogReplicationFuture logReplicationFuture;


    private final RoleEnum name;
    private final int term;

    public LeaderRole(LogReplicationFuture logReplicationFuture, RoleEnum name, int term) {
        this.logReplicationFuture = logReplicationFuture;
        this.name = name;
        this.term = term;
    }


    public void cancel(){
        logReplicationFuture.cancel(false);
    }
}
