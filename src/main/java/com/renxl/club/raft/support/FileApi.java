package com.renxl.club.raft.support;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.nio.ch.DirectBuffer;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * 指定一个索引文件为100M
 * 指定一个索引数据文件为500M
 * 一个快照为500M
 * todo 一个快照集合每个个体为1G
 *
 * @Author renxl
 * @Date 2020-08-26 19:24
 * @Version 1.0.0
 */
public class FileApi {
    /**
     * 4KB pagecache 的大小
     */
    private static final int OS_PAGE_SIZE = 4096;

    // 索引文件大小   50M
    //  数据文件的大小 1G
    private int dataFileSize = 1024*1024*50;


    private final File file;

    protected final AtomicInteger wrotePosition = new AtomicInteger(0);
    //
    //
    //
    //
    //
    private final RandomAccessFile randomAccessFile;
    //
    //
    //
    //
    //
    private final FileChannel fileChannel;
    //
    //
    //
    //
    private final MappedByteBuffer mappedByteBuffer;
    Logger log = LoggerFactory.getLogger(FileApi.class);
    private Integer fileName;


    /**
     * 正向新建
     * @param dir
     * @param filename
     * @param filesize
     */
    @SneakyThrows
    public FileApi(String dir,Integer filename ,int filesize) {
        this.dataFileSize = filesize;
        this.fileName = fileName;
        this.file = new File(dir+filename);
        ensureDirOK(this.file.getParent());
        this.randomAccessFile = new RandomAccessFile(file, "rw");
        this.fileChannel = new RandomAccessFile(file, "rw").getChannel();
        mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, dataFileSize);
        // 每1M空间刷盘一次
        warmMappedFile(FlushDiskType.SYNC_FLUSH,2);
    }

    /**
     * 反向load
     * @param existDir
     * @param existFileName
     */
    @SneakyThrows
    public FileApi(String existDir, Integer existFileName) {
        this.file = new File(existDir+existFileName.toString());
        // TODO 验证dataFileSize
        this.dataFileSize = (int)file.getTotalSpace();
        this.fileName = existFileName;

        this.randomAccessFile = new RandomAccessFile(file, "rw");
        this.fileChannel = new RandomAccessFile(file, "rw").getChannel();
        mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, dataFileSize);
        // TODO 验证此时能不能写入假值
//        warmMappedFile(FlushDiskType.SYNC_FLUSH,2);


    }

    /**
     *
     * @param newDir
     * @param newFile
     * @param fileSize
     * @param index
     */
    @SneakyThrows
    public FileApi(String newDir, int newFile, int fileSize, int index) {
        this.file = new File(newDir+newFile);
        // TODO 验证dataFileSize
        this.dataFileSize = (int)file.getTotalSpace();
        this.fileName = newFile;

        this.randomAccessFile = new RandomAccessFile(file, "rw");
        this.fileChannel = new RandomAccessFile(file, "rw").getChannel();
        mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, dataFileSize);
        // 文件写满后 新文件的初始index为即将写入的index
        mappedByteBuffer.putInt(index);
        mappedByteBuffer.putInt(index);

    }


    public static void ensureDirOK(final String dirName) {
        if (dirName != null) {
            File f = new File(dirName);
            if (!f.exists()) {
                boolean result = f.mkdirs();
            }
        }
    }


    /**
     * todo 释放资源
     *
     * @param buffer
     */
    public static void clean(final ByteBuffer buffer) {
        if (buffer == null || !buffer.isDirect() || buffer.capacity() == 0)
            return;
        invoke(invoke(viewed(buffer), "cleaner"), "clean");
    }

    private static ByteBuffer viewed(ByteBuffer buffer) {
        String methodName = "viewedBuffer";
        Method[] methods = buffer.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals("attachment")) {
                methodName = "attachment";
                break;
            }
        }

        ByteBuffer viewedBuffer = (ByteBuffer) invoke(buffer, methodName);
        if (viewedBuffer == null) {
            return buffer;
        } else {
            return viewed(viewedBuffer);
        }
    }

    private static Method method(Object target, String methodName, Class<?>[] args)
            throws NoSuchMethodException {
        try {
            return target.getClass().getMethod(methodName, args);
        } catch (NoSuchMethodException e) {
            return target.getClass().getDeclaredMethod(methodName, args);
        }
    }

    private static Object invoke(final Object target, final String methodName, final Class<?>... args) {
        return AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                try {
                    Method method = method(target, methodName, args);
                    method.setAccessible(true);
                    return method.invoke(target);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        });
    }


    /**
     * 隔几个pagecache 进行物理内存到虚拟内存的映射建立
     * @param type
     * @param pages
     */
    public void warmMappedFile(FlushDiskType type, int pages) {
        long beginTime = System.currentTimeMillis();
        // 搞一套新指针 共享缓冲 但独立指针
        ByteBuffer byteBuffer = this.mappedByteBuffer.slice();
        int flush = 0;
        long time = System.currentTimeMillis();
        for (int i = 0, j = 0; i < this.dataFileSize; i += OS_PAGE_SIZE, j++) {
            byteBuffer.put(i, (byte) 0);
            // force flush when flush disk type is sync
            if (type == FlushDiskType.SYNC_FLUSH) {
                if ((i / OS_PAGE_SIZE) - (flush / OS_PAGE_SIZE) >= pages) {
                    flush = i;
                    mappedByteBuffer.force();
                }
            }

            // prevent gc
            if (j % 1000 == 0) {
                log.info("j={}, costTime={}", j, System.currentTimeMillis() - time);
                time = System.currentTimeMillis();
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    log.error("Interrupted", e);
                }
            }
        }

        // force flush when prepare load finished
        if (type == FlushDiskType.SYNC_FLUSH) {
            log.info("mapped file warm-up done, force to disk, mappedFile={}, costTime={}",
                    this.fileName, System.currentTimeMillis() - beginTime);
            mappedByteBuffer.force();
        }
        log.info("mapped file warm-up done. mappedFile={}, costTime={}", this.fileName,
                System.currentTimeMillis() - beginTime);
        // 把持住内存 不被swap
        this.mlock();
    }

    /**
     * 防止swap
     */
    public void mlock() {
        final long beginTime = System.currentTimeMillis();
        final long address = ((DirectBuffer) (this.mappedByteBuffer)).address();
        Pointer pointer = new Pointer(address);
        {
            int ret = LibC.INSTANCE.mlock(pointer, new NativeLong(this.dataFileSize));
            log.info("mlock {} {} {} ret = {} time consuming = {}", address, this.fileName, this.dataFileSize, ret, System.currentTimeMillis() - beginTime);
        }

        {
            int ret = LibC.INSTANCE.madvise(pointer, new NativeLong(this.dataFileSize), LibC.MADV_WILLNEED);
            log.info("madvise {} {} {} ret = {} time consuming = {}", address, this.fileName, this.dataFileSize, ret, System.currentTimeMillis() - beginTime);
        }
    }

    public void munlock() {
        final long beginTime = System.currentTimeMillis();
        final long address = ((DirectBuffer) (this.mappedByteBuffer)).address();
        Pointer pointer = new Pointer(address);
        int ret = LibC.INSTANCE.munlock(pointer, new NativeLong(this.dataFileSize));
        log.info("munlock {} {} {} ret = {} time consuming = {}", address, this.fileName, this.dataFileSize, ret, System.currentTimeMillis() - beginTime);
    }



    public void write(int offset,byte[] commandBytes) {
        mappedByteBuffer.put(commandBytes,offset,commandBytes.length);
    }

    public void writeInt(int offset,int data) {
        mappedByteBuffer.putInt(offset,data);

    }


    public void read(byte[] dst, int offset,int length) {
        mappedByteBuffer.get(dst,offset,length);

    }

    public int readInt(int offset) {
       return mappedByteBuffer.getInt(offset);

    }




    public Integer getFileName() {
        return fileName;
    }

    public int getDataFileSize() {
        return dataFileSize;
    }


}
