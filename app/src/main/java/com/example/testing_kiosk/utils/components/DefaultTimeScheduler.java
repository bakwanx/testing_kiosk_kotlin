package com.example.testing_kiosk.utils.components;

import androidx.annotation.NonNull;

import com.example.testing_kiosk.utils.TimeScheduler;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


/**
 * Implementation of {@link TimeScheduler} by extending
 * {@link ScheduledThreadPoolExecutor} to keep tasks sorted. Tasks will get executed in order
 * of execution time (by using a {@link DelayQueue} internally.
 */
public class DefaultTimeScheduler extends ScheduledThreadPoolExecutor implements TimeScheduler {

    /**
     * How many core threads
     */
    private static final int TIMER_DEFAULT_NUM_THREADS = 2;
    private static final long THREADPOOL_SHUTDOWN_WAIT_TIME = 3000;


    /**
     * Create a scheduler that executes tasks in dynamically adjustable intervals
     */
    @SuppressWarnings("unused")
    public DefaultTimeScheduler() {
        this(TIMER_DEFAULT_NUM_THREADS);
    }


    public DefaultTimeScheduler(ThreadFactory factory) {
        this(factory, TIMER_DEFAULT_NUM_THREADS);
    }

    public DefaultTimeScheduler(ThreadFactory factory, int max_threads) {
        super(max_threads, factory);
        setRejectedExecutionHandler(new ShutdownRejectedExecutionHandler(getRejectedExecutionHandler()));
    }

    public DefaultTimeScheduler(int corePoolSize) {
        super(corePoolSize);
        setRejectedExecutionHandler(new ShutdownRejectedExecutionHandler(getRejectedExecutionHandler()));
    }

    public void setThreadFactory(ThreadFactory factory) {
        super.setThreadFactory(factory);
    }

    public String dumpTimerTasks() {
        return getQueue().toString();
    }


    public int getCurrentThreads() {
        return super.getPoolSize();
    }

    public int getMinThreads() {
        return super.getCorePoolSize();
    }

    public void setMinThreads(int size) {
        super.setCorePoolSize(size);
    }

    public int getMaxThreads() {
        return super.getMaximumPoolSize();
    }

    public void setMaxThreads(int size) {
        super.setMaximumPoolSize(size);
    }

    public long getKeepAliveTime() {
        return super.getKeepAliveTime(TimeUnit.MILLISECONDS);
    }

    public void setKeepAliveTime(long time) {
        super.setKeepAliveTime(time, TimeUnit.MILLISECONDS);
    }

    /**
     * Schedule a task for execution at varying intervals. After execution, the task will get rescheduled after
     * {@link Task#nextInterval()} milliseconds. The task is neve done until nextInterval()
     * return a value <= 0 or the task is cancelled.
     *
     * @param task the task to execute
     */
    public Future<?> scheduleWithDynamicInterval(Task task) {
        if (task == null) throw new NullPointerException();

        if (isShutdown()) return null;

        TaskWrapper task_wrapper = new TaskWrapper(task);
        task_wrapper.doSchedule(); // calls schedule() in ScheduledThreadPoolExecutor
        return task_wrapper;
    }


    @NonNull
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return super.scheduleWithFixedDelay(new RobustRunnable(command), initialDelay, delay, unit);
    }

    /**
     * Answers the number of tasks currently in the queue.
     *
     * @return The number of tasks currently in the queue.
     */
    public int size() {
        return getQueue().size();
    }


    /**
     * Stop the scheduler if it's running. Switch to stopped, if it's
     * suspended. Clear the task queue, cancelling all un-executed tasks
     *
     * @throws InterruptedException if interrupted while waiting for thread
     *                              to return
     */
    public void stop() {
        java.util.List<Runnable> tasks = shutdownNow();
        for (Runnable task : tasks) {
            if (task instanceof Future) {
                Future future = (Future) task;
                future.cancel(true);
            }
        }
        getQueue().clear();
        try {
            awaitTermination(THREADPOOL_SHUTDOWN_WAIT_TIME, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
        }
    }


    @NonNull
    public String toString() {
        return getClass().getSimpleName();
    }

    /**
     * Class which catches exceptions in run() - https://jira.jboss.org/jira/browse/JGRP-1062
     */
    static class RobustRunnable implements Runnable {
        final Runnable command;

        public RobustRunnable(Runnable command) {
            this.command = command;
        }

        public void run() {
            if (command != null) {
                try {
                    command.run();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }


    private class TaskWrapper<V> implements Runnable, Future<V> {
        private final Task task;
        private volatile Future<?> future; // cannot be NULL !
        private volatile boolean cancelled = false;


        public TaskWrapper(Task task) {
            this.task = task;
        }

        @SuppressWarnings("unused")
        public Future<?> getFuture() {
            return future;
        }

        public void run() {
            try {
                if (cancelled) {
                    if (future != null) future.cancel(true);
                    return;
                }
                if (future != null && future.isCancelled()) return;
                task.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }

            if (cancelled) {
                if (future != null) future.cancel(true);
                return;
            }
            if (future != null && future.isCancelled()) return;

            doSchedule();
        }


        public void doSchedule() {
            long next_interval = task.nextInterval();
            if (next_interval > 0) {
                future = schedule(this, next_interval, TimeUnit.MILLISECONDS);
                if (cancelled) future.cancel(true);
            }// else ==> task will not get rescheduled as interval < 0 !!
        }


        public boolean cancel(boolean mayInterruptIfRunning) {
            boolean retval = !isDone();
            cancelled = true;
            if (future != null) future.cancel(mayInterruptIfRunning);
            return retval;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public boolean isDone() {
            return cancelled || (future == null || future.isDone());
        }

        public V get() /*throws InterruptedException, ExecutionException*/ {
            return null;
        }

        public V get(long timeout, TimeUnit unit) /*throws InterruptedException, ExecutionException, TimeoutException*/ {
            return null;
        }

    }


}
