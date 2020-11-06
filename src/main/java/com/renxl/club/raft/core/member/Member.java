package com.renxl.club.raft.core.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 节点成员
 *
 * @Author renxl
 * @Date 2020-08-26 19:15
 * @Version 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
public class Member {


    private Endpoint endpoint;
    /**
     * 从节点状态
     */
    private ReplicatingState replicatingState;

    /**
     * 只有commitindex 与 matchindex和nextindex偏移对正，leader才认为完成子节点的正常化
     * @param lastEntryIndex
     * @return
     */
    public boolean advanceIndex(int lastEntryIndex) {

      return   replicatingState.advanceIndex(lastEntryIndex);
    }

    public void stopReplicating() {
        replicatingState.stop();
    }

    public boolean backOffNextIndex() {
        return replicatingState.backOffNextIndex();
    }
}
