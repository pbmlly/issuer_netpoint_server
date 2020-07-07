package com.csnt.ins.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author source
 */
public interface RunnableCatchThrowable extends Runnable {
    Logger logger = LoggerFactory.getLogger(RunnableCatchThrowable.class);

    /**
     * 下级须实现run方法
     */
    void runCatch();

    /**
     * 原始Runnable外层实现错误捕获
     */
    @Override
    default void run() {
        try {
            runCatch();
        } catch (Throwable e) {
            logger.error("[RunnableCatchThrowable]:", e);
        }
    }
}
