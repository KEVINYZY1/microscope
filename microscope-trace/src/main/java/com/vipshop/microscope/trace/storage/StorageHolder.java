package com.vipshop.microscope.trace.storage;

import com.vipshop.microscope.trace.Tracer;

/**
 * Storage holder.
 * <p/>
 * 1 ArrayBlockingQueue
 * 2 Disruptor
 * 3 Log4j2
 *
 * @author Xu Fei
 * @version 1.0
 */
public class StorageHolder {

    private static final int key = Tracer.DEFAULT_STORAGE;

    public static Storage getStorage() {
        return getStorage(key);
    }

    public static Storage getStorage(int key) {
        switch (key) {
            case 1:
                return getArrayBlockingQueueStorage();
            case 2:
                return getDisruptorQueueStorage();
            case 3:
                return getLog4j2FileStorage();
            default:
                return getArrayBlockingQueueStorage();
        }
    }

    private static Storage getArrayBlockingQueueStorage() {
        return ArrayBlockingQueueStorageHolder.storage;
    }

    private static Storage getDisruptorQueueStorage() {
        return DisruptorQueueStorageHolder.storage;
    }

    private static Storage getLog4j2FileStorage() {
        return Log4j2FileStorageHolder.storage;
    }

    private static class ArrayBlockingQueueStorageHolder {
        private static final Storage storage = new ArrayBlockingQueueStorage();
    }

    private static class DisruptorQueueStorageHolder {
        private static final Storage storage = new DisruptorQueueStorage();
    }

    private static class Log4j2FileStorageHolder {
        private static final Storage storage = new Log4j2FileStorage();
    }

}
