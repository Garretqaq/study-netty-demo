package com.example.demo.java.reactor.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池工具类
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class ThreadPoolUtil {
    /**
     * 线程池核心线程数(当前用到的多线程地方较少 核心数先设置为2)
     */
    private static final int CORE_POOL_SIZE = 2;

    /**
     * 线程池最大线程数
     */
    private static final int MAX_POOL_SIZE = 10;

    /**
     * 任务队列  此处使用ArrayBlockingQueue有界队列，防止队列无限膨胀导致内存溢出
     */
    private static final BlockingQueue<Runnable> WORK_QUEUE = new ArrayBlockingQueue<>(20);

    /**
     * 超出核心线程的额外线程空状态生存时间  此处是秒
     */
    private static final int KEEP_ALIVE_TIME = 60;

    /**
     * 线程工厂
     */
    private static final ThreadFactory THREAD_FACTORY = new MyThreadFactory();

    /**
     * 拒绝策略 CallerRunsPolicy：不在新线程中执行任务，而是由调用者所在的线程来执行
     */
    private static final RejectedExecutionHandler REJECTED_HANDLER = new ThreadPoolExecutor.CallerRunsPolicy();

    /**
     * 线程池
     */
    private static final ThreadPoolExecutor threadPool;

    static {
        threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                WORK_QUEUE,
                THREAD_FACTORY,
                REJECTED_HANDLER);
    }

    public static void execute(Runnable runnable) {
        getCommonThreadPoolInfo();
        threadPool.execute(runnable);
    }

    public static <T> void execute(FutureTask<T> futureTask) {
        getCommonThreadPoolInfo();
        threadPool.execute(futureTask);
    }

    public static <T> void cancel(FutureTask<T> futureTask) {
        getCommonThreadPoolInfo();
        futureTask.cancel(true);
    }

    /**
     * 线程池监控
     */
    private static void getCommonThreadPoolInfo() {
        log.info("CommonThreadPoolInfo========>当前线程总数：{}，正在执行任务线程数：{}，已执行完成任务数：{}",
                threadPool.getPoolSize(), threadPool.getActiveCount(), threadPool.getCompletedTaskCount());
    }

    /**
     * 自定义线程工厂
     */
    static class MyThreadFactory implements ThreadFactory {
        private final AtomicInteger threadId = new AtomicInteger();

        /**
         * 设置线程名称
         */
        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "ThreadPoolUtil:" + threadId.getAndIncrement());
        }
    }
}
 
