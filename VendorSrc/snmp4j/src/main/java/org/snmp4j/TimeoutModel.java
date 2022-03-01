/*_############################################################################
  _## 
  _##  SNMP4J - TimeoutModel.java  
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


package org.snmp4j;

/**
 * The {@code TimeoutModel} is the common interface for all models
 * of timing out a SNMP request. The default model is a linear model, thus
 * each retry has the same delay as specified by the {@link Target#getTimeout()}
 * value.
 *
 * @author Frank Fock
 * @version 1.0
 */

public interface TimeoutModel {

  /**
   * Gets the timeout for the specified retry (a zero value for
   * {@code retryCount} specifies the first request).
   * @param retryCount
   *    the number of retries already performed for the target.
   * @param totalNumberOfRetries
   *    the total number of retries configured for the target.
   * @param targetTimeout
   *    the timeout as specified for the target in milliseconds.
   * @return long
   *    the timeout duration in milliseconds for the supplied retry.
   */
  public long getRetryTimeout(int retryCount,
                              int totalNumberOfRetries, long targetTimeout);

  /**
   * Gets the timeout for all retries, which is defined as the sum of
   * {@link #getRetryTimeout(int retryCount, int totalNumberOfRetries,
   * long targetTimeout)}
   * for all {@code retryCount} in
   * {@code 0 &lz;= retryCount &lt; totalNumberOfRetries}.
   *
   * @param totalNumberOfRetries
   *    the total number of retries configured for the target.
   * @param targetTimeout
   *    the timeout as specified for the target in milliseconds.
   * @return
   *    the time in milliseconds when the request will be timed out finally.
   */
  public long getRequestTimeout(int totalNumberOfRetries, long targetTimeout);
}
