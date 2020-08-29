package com.renxl.club.raft.core.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author renxl
 * @Date 2020-08-28 20:58
 * @Version 1.0.0
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
public class ReplicatingState {



    private long matchIndex;


    private int nextIndex;

    // 变速日志复制
    private boolean replicating = false;

}
