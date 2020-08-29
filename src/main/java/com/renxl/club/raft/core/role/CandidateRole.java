package com.renxl.club.raft.core.role;

import com.renxl.club.raft.core.scheduled.ElectionTaskFuture;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Author renxl
 * @Date 2020-08-29 13:50
 * @Version 1.0.0
 */

@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class CandidateRole implements Role {


    private ElectionTaskFuture electionTaskFuture;

    private final RoleEnum name;
    private final int term;

    /**
     * 一票制 + 过半制
     */
    private final int votesCount;




}
