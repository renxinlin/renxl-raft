package com.renxl.club.raft.core.role;

import com.renxl.club.raft.core.scheduled.LogReplicationFuture;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Author renxl
 * @Date 2020-08-29 13:49
 * @Version 1.0.0
 */

@EqualsAndHashCode
@Accessors(chain = true)
public class LeaderRole implements Role{


    private LogReplicationFuture logReplicationFuture;


    private  RoleEnum name;
    private  int term;

    public LeaderRole(LogReplicationFuture logReplicationFuture, RoleEnum name, int term) {
        this.logReplicationFuture = logReplicationFuture;
        this.name = name;
        this.term = term;
    }

    public LogReplicationFuture getLogReplicationFuture() {
        return logReplicationFuture;
    }

    public void setLogReplicationFuture(LogReplicationFuture logReplicationFuture) {
        this.logReplicationFuture = logReplicationFuture;
    }

    public RoleEnum getName() {
        return name;
    }

    public void setName(RoleEnum name) {
        this.name = name;
    }

    @Override
    public int getTerm() {
        return term;
    }

    @Override
    public void setTerm(int term) {
        this.term = term;
    }

    @Override
    public boolean cancelLogOrElection() {
        return logReplicationFuture.cancel(false);
    }

    @Override
    public String toString() {
        return "LeaderRole{" +
                "logReplicationFuture=" + logReplicationFuture +
                ", name=" + name +
                ", term=" + term +
                '}';
    }
}
