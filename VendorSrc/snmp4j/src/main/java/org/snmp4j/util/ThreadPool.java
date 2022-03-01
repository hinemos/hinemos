/*_############################################################################
  _## 
  _##  SNMP4J - ThreadPool.java  
  _## 
  _##  Copyright (C) 2003-2020  Frank Fock (SNMP4J.org)
  _##  
  _##  Licensed under the Apache License, Version 2.0 (the "License");
  _##  you may not use this file except in compliance with the License.
  _##  You may obtain a copy of the License at
  _##  
  _##      http://www.apache.org/licenses/LICENSE-2.0
  _##  
  _##  Unless required by applicable law or agreed to in writing, software
  _##  distributed under the License is distributed on an "AS IS" BASIS,
  _##  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  _##  See the License for the specific language governing permissions and
  _##  limitations under the License.
  _##  
  _##########################################################################*/
package org.snmp4j.util;

import org.snmp4j.SNMP4JSettings;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The <code>ThreadPool</code> provides a pool of a fixed number of threads
 * that are capable to execute tasks that implement the <code>Runnable</code>
 * interface concurrently. The ThreadPool blocks when all threads are busy
 * with tasks and an additional task is added.
 *
 * @author Frank Fock
 * @version 2.6.1
 * @since 1.0.2
 */
public class ThreadPool implements WorkerPool {

    private static final int DEFAULT_TASK_MANAGER_BUSY_TIMEOUT_MILLIS = 20;

    protected List<TaskManager> taskManagers;
    protected String name = "ThreadPool";
    protected volatile boolean stop = false;
    protected boolean respawnThreads = false;
    protected int taskManagersBusyTimeoutMillis = DEFAULT_TASK_MANAGER_BUSY_TIMEOUT_MILLIS;

    protected ThreadPool() {
    }

    protected String getTaskManagerName(String prefix, int index) {
        return prefix + "." + index;
    }

    protected void setup(String name, int size) {
        this.name = name;
        taskManagers = new CopyOnWriteArrayList<>();
        for (int i = 0; i < size; i++) {
            TaskManager tm = new TaskManager(getTaskManagerName(name, i));
            taskManagers.add(tm);
            tm.start();
        }
    }

    /**
     * Creates a thread pool with the supplied name and size.
     *
     * @param name
     *         the name prefix for the threads in this pool.
     * @param size
     *         the number of threads in this pool. This number also specifies the
     *         number of concurrent tasks that can be executed with this pool.
     *
     * @return a <code>ThreadPool</code> instance.
     */
    public static ThreadPool create(String name, int size) {
        ThreadPool pool = new ThreadPool();
        pool.setup(name, size);
        return pool;
    }

    /**
     * Executes a task on behalf of this thread pool. If all threads are currently
     * busy, this method call blocks until a thread gets idle again which is when
     * the call returns immediately.
     *
     * @param task
     *         a <code>Runnable</code> to execute.
     */
    public void execute(WorkerTask task) {
        while (true) {
            for (int i = 0; i < taskManagers.size(); i++) {
                TaskManager tm = taskManagers.get(i);
                if ((respawnThreads) && (!tm.isAlive())) {
                    tm = new TaskManager(getTaskManagerName(name, i));
                    taskManagers.set(i, tm);
                }
                if (tm.isIdle()) {
                    synchronized (this) {
                        if (tm.isIdle()) {
                            try {
                                tm.execute(task);
                                return;
                            } catch (IllegalStateException isex) {
                                // ignore
                            }
                        }
                    }
                }
            }
            synchronized (this) {
                // check again to avoid race conditions with the notify of the task manager
                boolean waitForNotify = true;
                for (TaskManager tm : taskManagers) {
                    if (tm.isIdle()) {
                        waitForNotify = false;
                        break;
                    }
                }
                if (waitForNotify) {
                    try {
                        wait(taskManagersBusyTimeoutMillis);
                    } catch (InterruptedException ex) {
                        handleInterruptedExceptionOnExecute(ex, task);
                    }
                }
            }
        }
    }

    /**
     * Handle a interrupted exception on the execution attempt of {@link org.snmp4j.util.WorkerTask}.
     * If the body is void, execution continues and the interrupted exception is ignored.
     * To stop the execution, a {@link java.lang.RuntimeException} has to be thrown.
     * The default behavior is to rethrow the interrupted exception wrapped in a {@link java.lang.RuntimeException}
     * if {@link org.snmp4j.SNMP4JSettings#forwardRuntimeExceptions} is <code>true</code>. Otherwise, the
     * interrupted exception is ignored.
     *
     * @param interruptedException
     *         the caught InterruptedException.
     * @param task
     *         the task to should have been executed, but failed to execute (until now) because of a busy pool.
     *
     * @since 2.3.3
     */
    protected void handleInterruptedExceptionOnExecute(InterruptedException interruptedException, WorkerTask task) {
        if (SNMP4JSettings.isForwardRuntimeExceptions()) {
            throw new RuntimeException(interruptedException);
        }
    }

    /**
     * Tries to execute a task on behalf of this thread pool. If all threads are
     * currently busy, this method returns <code>false</code>. Otherwise the task
     * is executed in background.
     *
     * @param task
     *         a <code>Runnable</code> to execute.
     *
     * @return <code>true</code> if the task is executing.
     * @since 1.6
     */
    public boolean tryToExecute(WorkerTask task) {
        for (int i = 0; i < taskManagers.size(); i++) {
            TaskManager tm = taskManagers.get(i);
            if ((respawnThreads) && (!tm.isAlive())) {
                tm = new TaskManager(getTaskManagerName(name, i));
            }
            if (tm.isIdle()) {
                try {
                    tm.execute(task);
                    return true;
                } catch (IllegalStateException isex) {
                    // ignore
                }
            }
        }
        return false;
    }

    /**
     * Tests if the threads are respawn (recreates) when they have been stopped
     * or canceled.
     *
     * @return <code>true</code> if threads are respawn.
     */
    public boolean isRespawnThreads() {
        return respawnThreads;
    }

    /**
     * Specifies whether threads are respawned by this thread pool after they
     * have been stopped or not. Default is no respawning.
     *
     * @param respawnThreads
     *         if <code>true</code> then threads will be respawn.
     */
    public void setRespawnThreads(boolean respawnThreads) {
        this.respawnThreads = respawnThreads;
    }

    /**
     * Returns the name of the thread pool.
     *
     * @return the name of this thread pool.
     */
    public String getName() {
        return name;
    }

    /**
     * Stops all threads in this thread pool gracefully. This method will not
     * return until all threads have been terminated and joined successfully.
     */
    @SuppressWarnings("unchecked")
    public void stop() {
        List<? extends TaskManager> tms;
        synchronized (this) {
            stop = true;
            tms = taskManagers;
        }
        for (TaskManager tm : tms) {
            tm.terminate();
            synchronized (tm) {
                tm.notify();
            }
            try {
                tm.join();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Cancels all threads non-blocking by interrupting them.
     */
    public void cancel() {
        stop = true;
        for (TaskManager tm : taskManagers) {
            tm.terminate();
            tm.interrupt();
        }
    }

    /**
     * Interrupts all threads in the pool.
     *
     * @since 1.6
     */
    public void interrupt() {
        for (TaskManager tm : taskManagers) {
            tm.interrupt();
        }
    }

    /**
     * Checks if all threads of the pool are idle.
     *
     * @return <code>true</code> if all threads are idle.
     * @since 1.6
     */
    public boolean isIdle() {
        for (TaskManager tm : taskManagers) {
            if (!tm.isIdle()) {
                return false;
            }
        }
        return true;
    }

    public int getTaskManagersBusyTimeoutMillis() {
        return taskManagersBusyTimeoutMillis;
    }

    /**
     * Sets the timeout value in milliseconds the pool waits when all task managers are busy for a notification of
     * them to check again for idle task managers. In normal (non-error) operation, this timeout could be a large value
     * to save some CPU cycles. For most use cases the default {@link #DEFAULT_TASK_MANAGER_BUSY_TIMEOUT_MILLIS} should
     * be optimal. A zero value will disable the timeout.
     *
     * @param taskManagersBusyTimeoutMillis
     *         the timeout value (see {@link Object#wait(long)}).
     *
     * @since 2.6.1
     */
    public void setTaskManagersBusyTimeoutMillis(int taskManagersBusyTimeoutMillis) {
        this.taskManagersBusyTimeoutMillis = taskManagersBusyTimeoutMillis;
    }

    /**
     * The <code>TaskManager</code> executes tasks in a thread.
     *
     * @author Frank Fock
     * @version 2.7.0
     * @since 1.0.2
     */
    class TaskManager extends Thread {

        private WorkerTask task = null;
        private volatile boolean run = true;

        public TaskManager(String name) {
            super(name);
        }

        public void run() {
            while ((!stop) && run) {
                if (task != null) {
                    synchronized (this) {
                        task.run();
                        task = null;
                    }
                    synchronized (ThreadPool.this) {
                        ThreadPool.this.notify();
                    }
                } else synchronized (this) {
                    try {
                        if (task == null) {
                            wait();
                        }
                    } catch (InterruptedException ex) {
                        run = respawnThreads;
                        break;
                    }
                }
            }
        }

        public boolean isIdle() {
            return ((task == null) && run);
        }

        public boolean isStopped() {
            return stop;
        }

        public void terminate() {
            stop = true;
            WorkerTask t;
            if ((t = task) != null) {
                t.terminate();
            }
        }

        public synchronized void execute(WorkerTask task) {
            if (this.task == null) {
                this.task = task;
                notify();
            } else {
                throw new IllegalStateException("TaskManager is not idle");
            }
        }
    }
}
