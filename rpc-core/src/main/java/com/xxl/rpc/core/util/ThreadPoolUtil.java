package com.xxl.rpc.core.util;

import java.util.concurrent.*;

/**
 * @author mzj 2019-02-18
 */
public class ThreadPoolUtil {

    private static final long DEFAULT_KEEP_ALIVE_TIME = 60L;
    private static final int DEFAULT_QUEUE_CAPACITY = 1000;

    /**
     * make server thread pool
     *
     * @param serverType
     * @return
     */
    public static ThreadPoolExecutor makeServerThreadPool(final String serverType, int corePoolSize, int maxPoolSize) {
        ThreadPoolExecutor serverHandlerPool = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                DEFAULT_KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(DEFAULT_QUEUE_CAPACITY),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        String name = String.format("rpc, %s-serverHandlerPool-%d", serverType, r.hashCode());
                        return new Thread(r, name);
                    }
                },
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        String msg = String.format("rpc %s Thread pool is EXHAUSTED!", serverType);
                        throw new RpcException(msg);
                    }
                });// default maxThreads 300, minThreads 60

        return serverHandlerPool;
    }

}
