/*_############################################################################
  _## 
  _##  SNMP4J - ThreadFactory.java  
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
 * The <code>ThreadFactory</code> describes a factory for threads of execution
 * modeled as <code>WorkerTask</code>s.
 *
 * @author Frank Fock
 * @version 1.9
 * @since 1.9
 */
public interface ThreadFactory {

  /**
   * Creates a new thread of execution for the supplied task. The returned
   * <code>WorkerTask</code> is a symmetric wrapper for the supplied one.
   * When the returned task is being run, the supplied one will be executed
   * in a new thread of execution until it either terminates or the
   * {@link WorkerTask#terminate()} method has been called.
   *
   * @param name
   *    the name of the execution thread.
   * @param task
   *    the task to be executed in the new thread.
   * @param daemon
   *    indicates whether the new thread is a daemon (<code>true</code> or an
   *    user thread (<code>false</code>).
   * @return
   *    the <code>WorkerTask</code> wrapper to control start and termination of
   *    the thread.
   */
  WorkerTask createWorkerThread(String name, WorkerTask task, boolean daemon);

}
