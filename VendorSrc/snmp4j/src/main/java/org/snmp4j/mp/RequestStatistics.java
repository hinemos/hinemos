/*_############################################################################
  _## 
  _##  SNMP4J - RequestStatistics.java  
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

package org.snmp4j.mp;

/**
 * The <code>RequestStatistics</code> interface defines statistic values about request processing.
 *
 * @author Frank Fock
 * @since 2.4.3
 */
public interface RequestStatistics {

  /**
   * Gets the total number of messages that have been sent on behalf of this request.
   * @return
   *    the number of messages sent (number of retries plus one).
   */
  int getTotalMessagesSent();

  /**
   * Sets the total number of messages that have been sent on behalf of this request.
   * @param totalMessagesSent
   *    the total message count for this request.
   */
  void setTotalMessagesSent(int totalMessagesSent);

  /**
   * Gets the index of the message that has been responded.
   * @return
   *    0 if the initial message has been responded by the command responder.
   *    A value greater than zero indicates, that a retry message has been responded.
   */
  int getIndexOfMessageResponded();

  /**
   * Sets the index of the message that has been responded.
   * @param indexOfMessageResponded
   *    the zero-based index of the message for which the response had been received.
   */
  void setIndexOfMessageResponded(int indexOfMessageResponded);

  /**
   * Gets the time elapsed between the sending of the message and receiving its response.
   * @return
   *    the runtime of the successful request and response message pair in nanoseconds.
   */
  long getResponseRuntimeNanos();

  /**
   * Sets the time elapsed between the sending of the message and receiving its response.
   * @param responseRuntimeNanos
   *    the runtime of the successful request and response message pair in nanoseconds.
   */
  void setResponseRuntimeNanos(long responseRuntimeNanos);

}
