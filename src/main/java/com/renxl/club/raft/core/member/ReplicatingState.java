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

    public boolean advanceIndex(int lastEntryIndex) {
        // 这里nextindex 只有稳定的时候 = matchIndex+1
        // 新的leader会认为 matchIndex = 0 nextIndex = 1
        boolean result = (matchIndex != lastEntryIndex || nextIndex != (lastEntryIndex + 1));

        matchIndex = lastEntryIndex;
        nextIndex = lastEntryIndex + 1;

        return result;
    }

    public boolean backOffNextIndex() {
        // 回退只能慢慢回退
        if (nextIndex > 1) {
            nextIndex--;
            return true;
        }
        return false;

    }

    public void stop() {
        replicating = false;

    }
}
