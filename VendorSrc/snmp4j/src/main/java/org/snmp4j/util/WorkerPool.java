/*_############################################################################
  _## 
  _##  SNMP4J - WorkerPool.java  
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

/**
 * The <code>WorkerPool</code> interface models an abstract pool of workers
 * (threads) which can execute {@link WorkerTask}s concurrently.
 *
 * @author Frank Fock
 * @version 1.9
 */
public interface WorkerPool {

  /**
   * Executes a task on behalf of this worker pool. If all threads are currently
   * busy, this method call blocks until a worker gets idle again which is when
   * the call returns immediately.
   * @param task
   *    a <code>Runnable</code> to execute.
   */
  void execute(WorkerTask task);

  /**
   * Tries to execute a task on behalf of this worker pool. If all threads are
   * currently busy, this method returns <code>false</code>. Otherwise the task
   * is executed in background.
   * @param task
   *    a <code>Runnable</code> to execute.
   * @return
   *    <code>true</code> if the task is executing.
   */
  boolean tryToExecute(WorkerTask task);

  /**
   * Stops all threads in this worker pool gracefully. This method will not
   * return until all threads have been terminated and joined successfully.
   */
  void stop();

  /**
   * Cancels all threads non-blocking by interrupting them.
   */
  void cancel();

  /**
   * Checks if all workers of the pool are idle.
   * @return
   *    <code>true</code> if all workers are idle.
   */
  boolean isIdle();

}
