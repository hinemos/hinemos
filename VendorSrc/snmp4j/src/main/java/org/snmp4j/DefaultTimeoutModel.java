/*_############################################################################
  _## 
  _##  SNMP4J - DefaultTimeoutModel.java  
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
 * The <code>DefaultTimeoutModel</code> implements a timeout model that uses
 * constant timeouts between retries.
 * <p>
 * The total time waited before a request is timed out is therefore:
 * <code>(totalNumberOfRetries + 1) * targetTimeout</code> where each (re)try
 * is timed out after <code>targetTimeout</code> milliseconds.
 *
 * @author Frank Fock
 * @version 1.0
 */
public class DefaultTimeoutModel implements TimeoutModel {

  public DefaultTimeoutModel() {
  }

  public long getRetryTimeout(int retryCount,
                              int totalNumberOfRetries, long targetTimeout) {
    return targetTimeout;
  }

  public long getRequestTimeout(int totalNumberOfRetries, long targetTimeout) {
    return (totalNumberOfRetries+1)*targetTimeout;
  }
}
