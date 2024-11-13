package test.demo.activity.net;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程池管理工具类
 */
public final class ThreadPoolManager {
    private ExecutorService cachePool = Executors.newCachedThreadPool();
    private ExecutorService singlePool = Executors.newSingleThreadExecutor();

    private static final class SingletonHolder {
        private static final ThreadPoolManager INSTANCE = new ThreadPoolManager();
    }

    private ThreadPoolManager() {
    }

    public static void executeInSinglePool(Runnable r) {
        if (r != null) {
            SingletonHolder.INSTANCE.singlePool.execute(r);
        }
    }

    public static void executeInCachePool(Runnable r) {
        if (r != null) {
            SingletonHolder.INSTANCE.cachePool.execute(r);
        }
    }

}
