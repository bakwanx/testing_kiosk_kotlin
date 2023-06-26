package com.example.testing_kiosk.utils.components;

import java.util.concurrent.ThreadPoolExecutor;


/**
 * ShutdownRejectedExecutionHandler is a decorator RejectedExecutionHandler used
 * in all ThreadPoolExecutor(s). Default RejectedExecutionHandler raises
 * RuntimeException when a task is submitted to ThreadPoolExecutor that has been
 * shutdown. ShutdownRejectedExecutionHandler instead logs only a warning
 * message.
 * 
 * @see ThreadPoolExecutor
 * @see java.util.concurrent.RejectedExecutionHandler
 */
public class ShutdownRejectedExecutionHandler implements java.util.concurrent.RejectedExecutionHandler {

    java.util.concurrent.RejectedExecutionHandler handler;

    public ShutdownRejectedExecutionHandler(java.util.concurrent.RejectedExecutionHandler handler) {
        super();
        if(handler == null)
            throw new NullPointerException("RejectedExecutionHandler cannot be NULL");
        this.handler=handler;
    }

    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

        if(!executor.isShutdown()) {
            handler.rejectedExecution(r, executor);
        }
    }
}
