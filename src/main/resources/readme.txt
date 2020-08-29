序列化采用Protostuff

日志文件采用 内存映射

采用多线程
日志状态机采用 hash + 多线程 【数据的一致性 实际上是key相等的数据一致性 可以将hash[key] 归一化后分配到同一个线程 从而保障状态机的正确性】

日志缓冲区采用单线程？？？
leader其实可以异步但follower必须提交才能回复

log

indexfile 000100   当索引文件不够则新建索引文件
indexfile 000100
indexfile 000100

commitlog 001000   当数据文件不够则新建数据文件
commitlog 002000


每次新建数据文件则生成快照
快照还是基于数据块传输 放弃transferTo的原因
1 transferto 虽然适合大文件 且性能很好 但是很容易堵死带宽
2 这么大的数据量 必然出现分包问题，假如出现传输过程宕掉 则需要类似redis aof那样修复  所以最终抛弃这种方案
2 NioSocketChannel channel = (NioSocketChannel)ctx.channel();SelectableChannel ch = channel.unsafe().ch();







