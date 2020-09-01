package com.renxl.club.raft.core.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author renxl
 * @Date 2020-08-29 14:15
 * @Version 1.0.0
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class AppendEntryResponse {

    private Boolean append;



}
