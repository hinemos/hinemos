/*_############################################################################
  _## 
  _##  SNMP4J - DefaultThreadFactory.java  
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

import org.snmp4j.util.*;
import org.snmp4j.SNMP4JSettings;

/**
 * The <code>DefaultThreadFactory</code> creates {@link WorkerTask} instances
 * that allow concurrent execution of tasks. By default it uses a timeout
 * of 60 seconds (1 min.) when joining threads on behalf of an call of the
 * {@link WorkerTask#join()} method. By setting
 *
 * @author Frank Fock
 * @version 1.10.2
 * @since 1.8
 */
public class DefaultThreadFactory implements ThreadFactory {

  private long joinTimeout;

  public DefaultThreadFactory() {
    joinTimeout = SNMP4JSettings.getThreadJoinTimeout();
  }

  /**
   * Creates a new thread of execution for the supplied task.
   *
   * @param name the name of the execution thread.
   * @param task the task to be executed in the new thread.
   * @return the <code>WorkerTask</code> wrapper to control start and
   *   termination of the thread.
   */
  public WorkerTask createWorkerThread(String name, WorkerTask task,
                                       boolean daemon) {
    WorkerThread wt = new WorkerThread(name, task);
    wt.thread.setDaemon(daemon);
    return wt;
  }

  /**
   * Sets the maximum time to wait when joining a worker task thread.
   * @param millis
   *    the time to wait. 0 waits forever.
   * @since 1.10.2
   */
  public void setThreadJoinTimeout(long millis) {
    this.joinTimeout = millis;
  }

  public class WorkerThread implements WorkerTask {

    private Thread thread;
    private WorkerTask task;
    private boolean started = false;

    public WorkerThread(String name, WorkerTask task) {
      this.thread = new Thread(task, name);
      this.task = task;
    }

    public void terminate() {
      task.terminate();
    }

    public void join() throws InterruptedException {
      task.join();
      thread.join(joinTimeout);
    }

    public void run() {
      if (!started) {
        started = true;
        thread.start();
      }
      else {
        thread.run();
      }
    }

    public void interrupt() {
      task.interrupt();
      thread.interrupt();
    }
  }
}
