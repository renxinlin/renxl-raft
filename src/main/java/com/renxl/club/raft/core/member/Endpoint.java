package com.renxl.club.raft.core.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author renxl
 * @Date 2020-08-28 20:47
 * @Version 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
public class Endpoint {
    private NodeId nodeId;
    private Address address;

}
