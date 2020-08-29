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

    private ReplicatingState replicatingState;


}
