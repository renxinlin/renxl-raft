package com.renxl.club.raft.core.role;

import com.renxl.club.raft.core.scheduled.ElectionTaskFuture;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Author renxl
 * @Date 2020-08-29 13:50
 * @Version 1.0.0
 */
@AllArgsConstructor
@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class CandidateRole implements Role {


    private ElectionTaskFuture electionTaskFuture;

    private   RoleEnum name;

    private   int term;

    /**
     * 一票制 + 过半制
     */
    private  int votesCount;




    @Override
    public boolean cancelLogOrElection() {
        return electionTaskFuture.cancel(false);
    }
}
