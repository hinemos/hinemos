/*_############################################################################
  _## 
  _##  SNMP4J - CommonTimer.java  
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

import java.util.TimerTask;
import java.util.Date;

/**
 * This <code>CommonTimer</code> defines the subset interface used from
 * {@link java.util.Timer} by SNMP4J.
 *
 * @author Frank Fock
 * @version 1.9
 * @since 1.9
 */
public interface CommonTimer {

  /**
   * Schedules the specified task for execution after the specified delay.
   *
   * @param task  task to be scheduled.
   * @param delay delay in milliseconds before task is to be executed.
   * @throws IllegalArgumentException if <tt>delay</tt> is negative, or
   *         <tt>delay + System.currentTimeMillis()</tt> is negative.
   * @throws IllegalStateException if task was already scheduled or
   *         cancelled, or timer was cancelled.
   */
  void schedule(TimerTask task, long delay);

  /**
   * Schedules the specified task for repeated <i>fixed-delay execution</i>,
   * beginning at the specified time. Subsequent executions take place at
   * approximately regular intervals, separated by the specified period.
   *
   * <p>In fixed-delay execution, each execution is scheduled relative to
   * the actual execution time of the previous execution.  If an execution
   * is delayed for any reason (such as garbage collection or other
   * background activity), subsequent executions will be delayed as well.
   * In the long run, the frequency of execution will generally be slightly
   * lower than the reciprocal of the specified period (assuming the system
   * clock underlying <tt>Object.wait(long)</tt> is accurate).
   *
   * <p>Fixed-delay execution is appropriate for recurring activities
   * that require "smoothness."  In other words, it is appropriate for
   * activities where it is more important to keep the frequency accurate
   * in the short run than in the long run.  This includes most animation
   * tasks, such as blinking a cursor at regular intervals.  It also includes
   * tasks wherein regular activity is performed in response to human
   * input, such as automatically repeating a character as long as a key
   * is held down.
   *
   * @param task   task to be scheduled.
   * @param firstTime First time at which task is to be executed.
   * @param period time in milliseconds between successive task executions.
   * @throws IllegalArgumentException if <tt>time.getTime()</tt> is negative.
   * @throws IllegalStateException if task was already scheduled or
   *         cancelled, timer was cancelled, or timer thread terminated.
   */
  void schedule(TimerTask task, Date firstTime, long period);

  /**
   * Schedules the specified task for repeated <i>fixed-delay execution</i>,
   * beginning after the specified delay.  Subsequent executions take place
   * at approximately regular intervals separated by the specified period.
   *
   * <p>In fixed-delay execution, each execution is scheduled relative to
   * the actual execution time of the previous execution.  If an execution
   * is delayed for any reason (such as garbage collection or other
   * background activity), subsequent executions will be delayed as well.
   * In the long run, the frequency of execution will generally be slightly
   * lower than the reciprocal of the specified period (assuming the system
   * clock underlying <tt>Object.wait(long)</tt> is accurate).
   *
   * <p>Fixed-delay execution is appropriate for recurring activities
   * that require "smoothness."  In other words, it is appropriate for
   * activities where it is more important to keep the frequency accurate
   * in the short run than in the long run.  This includes most animation
   * tasks, such as blinking a cursor at regular intervals.  It also includes
   * tasks wherein regular activity is performed in response to human
   * input, such as automatically repeating a character as long as a key
   * is held down.
   *
   * @param task   task to be scheduled.
   * @param delay  delay in milliseconds before task is to be executed.
   * @param period time in milliseconds between successive task executions.
   * @throws IllegalArgumentException if <tt>delay</tt> is negative, or
   *         <tt>delay + System.currentTimeMillis()</tt> is negative.
   * @throws IllegalStateException if task was already scheduled or
   *         cancelled, timer was cancelled, or timer thread terminated.
   */
  void schedule(TimerTask task, long delay, long period);

  /**
   * Terminates this timer, discarding any currently scheduled tasks.
   * Does not interfere with a currently executing task (if it exists).
   * Once a timer has been terminated, its execution thread terminates
   * gracefully, and no more tasks may be scheduled on it.
   *
   * <p>Note that calling this method from within the run method of a
   * timer task that was invoked by this timer absolutely guarantees that
   * the ongoing task execution is the last task execution that will ever
   * be performed by this timer.
   *
   * <p>This method may be called repeatedly; the second and subsequent
   * calls have no effect.
   */
  public void cancel();

}
