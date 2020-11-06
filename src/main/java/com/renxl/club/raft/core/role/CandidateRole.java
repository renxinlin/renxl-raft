package com.renxl.club.raft.core.role;

import com.renxl.club.raft.core.scheduled.ElectionTaskFuture;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @Author renxl
 * @Date 2020-08-29 13:50
 * @Version 1.0.0
 */
@AllArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
@ToString
public class CandidateRole implements Role {


    private ElectionTaskFuture electionTaskFuture;

    private   RoleEnum name;

    private   int term;

    /**
     * 一票制 + 过半制
     */
    private  int votesCount;


    public ElectionTaskFuture getElectionTaskFuture() {
        return electionTaskFuture;
    }

    public void setElectionTaskFuture(ElectionTaskFuture electionTaskFuture) {
        this.electionTaskFuture = electionTaskFuture;
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

    public int getVotesCount() {
        return votesCount;
    }

    public void setVotesCount(int votesCount) {
        this.votesCount = votesCount;
    }

    @Override
    public boolean cancelLogOrElection() {
        return electionTaskFuture.cancel(false);
    }
}
