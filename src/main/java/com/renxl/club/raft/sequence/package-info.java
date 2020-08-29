/**
 *
 * 采用 COW arraylist来提高程序的并发能力
 * 采用 MapperdFileBuffer 来提高 IO能力 由于command基本都是小数据 非常适合采用MapperdFileBuffer
 */
package com.renxl.club.raft.sequence;