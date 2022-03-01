/*_############################################################################
  _## 
  _##  SNMP4J - TaskScheduler.java  
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

import java.util.LinkedList;
import org.snmp4j.log.LogFactory;
import org.snmp4j.log.LogAdapter;

/**
 * The <code>TaskScheduler</code> uses a <code>ThreadPool</code> to recurrent
 * execute <code>SchedulerTask</code>s.
 *
 * @author Frank Fock
 * @version 1.6
 * @since 1.6
 */
public class TaskScheduler implements Runnable {

  private LogAdapter logger = LogFactory.getLogger(TaskScheduler.class);

  private static final long DEFAULT_SCHEDULER_TIMEOUT = 5;

  private LinkedList<SchedulerTask> tasks = new LinkedList<SchedulerTask>();
  private ThreadPool threadPool;
  private boolean stop;
  protected long schedulerTimeout = DEFAULT_SCHEDULER_TIMEOUT;

  /**
   * Creates a <code>TaskScheduler</code> that uses the supplied
   * <code>ThreadPool</code> to execute tasks.
   *
   * @param threadPool
   *    a <code>ThreadPool</code>.
   */
  public TaskScheduler(ThreadPool threadPool) {
    this.threadPool = threadPool;
  }

  /**
   * Adds a task to the scheduler.
   * @param task
   *    a <code>SchedulerTask</code>.
   */
  public synchronized void addTask(SchedulerTask task) {
    tasks.addLast(task);
    notify();
  }

  /**
   * Removes a task from the scheduler.
   * @param task
   *    the <code>SchedulerTask</code> to be removed from the scheduler
   * @return
   *    <code>true</code> if the task could be removed.
   */
  public synchronized boolean removeTask(SchedulerTask task) {
    return tasks.remove(task);
  }

  /**
   * Removes all tasks.
   */
  public synchronized void clear() {
    tasks.clear();
  }

  /**
   * Runs the scheduler. While in this method tasks are scheduled on the
   * internal thread pool. The scheduler tries to schedule task fairly.
   */
  public void run() {
    while (!stop) {
      boolean readyToRun = false;
      synchronized (this) {
        for (int i=0; i<tasks.size(); i++) {
          SchedulerTask task = tasks.get(i);
          if (task.isDone()) {
            if (logger.isDebugEnabled()) {
              logger.debug("Task '" + task + "' is done");
            }
            tasks.removeFirst();
            continue;
          }
          else if (task.isReadyToRun()) {
            readyToRun = true;
            while (!threadPool.tryToExecute(task)) {
              try {
                synchronized (threadPool) {
                  threadPool.wait(schedulerTimeout);
                }
              }
              catch (InterruptedException ex) {
                logger.warn("Scheduler interrupted, aborting...");
                stop = true;
                break;
              }
            }
            tasks.addLast(tasks.removeFirst());
            i--;
          }
        }
      }
      if (!readyToRun) {
        try {
          if (threadPool.isIdle()) {
            synchronized (this) {
              wait(schedulerTimeout);
            }
          }
          else {
            synchronized (threadPool) {
              threadPool.wait(schedulerTimeout);
            }
          }
        }
        catch (InterruptedException ex1) {
          logger.warn("Scheduler interrupted, aborting...");
          stop = true;
        }
      }
    }
    logger.info("Scheduler stopped.");
  }

  /**
   * Stops the schedulers run method.
   * @param stop
   *    <code>true</code> to stop the scheduler.
   */
  public void setStop(boolean stop) {
    this.stop = stop;
  }

  /**
   * Checks if the scheduler is (to be) stopped.
   * @return
   *    <code>true</code> if the scheduler has been stopped or is being stopped.
   */
  public boolean isStop() {
    return stop;
  }
}
