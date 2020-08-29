package com.renxl.club.raft.core.member;

/**
 * @Author renxl
 * @Date 2020-08-28 20:50
 * @Version 1.0.0
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
public class Address {

    private String ip;
    private int port;

}
