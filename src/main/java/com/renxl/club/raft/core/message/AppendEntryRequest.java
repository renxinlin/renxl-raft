package com.renxl.club.raft.core.message;

import com.renxl.club.raft.connector.message.RpcMessage;
import com.renxl.club.raft.core.member.NodeId;
import com.renxl.club.raft.log.entry.Entry;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;

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
public class AppendEntryRequest extends RpcMessage {




    private int term;

    private NodeId leaderId;

    private int prevLogIndex;

    private int prevLogTerm;

    private List<Entry> entries = Collections.emptyList();

    private int leaderCommit;



    private Channel channel;


    public int getLastEntryIndex() {
        return this.entries.isEmpty() ? this.prevLogIndex : this.entries.get(this.entries.size() - 1).getIndex();
    }
}
