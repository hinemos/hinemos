/*_############################################################################
  _## 
  _##  SNMP4J - MessageException.java  
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

import java.io.IOException;
import org.snmp4j.mp.StatusInformation;

/**
 * The <code>MessageException</code> represents information about an exception
 * occurred during message processing. The associated
 * <code>StatusInformation</code> object provides (if present) detailed
 * information about the error that occurred and the status of the processed
 * message.
 * @author Frank Fock
 * @version 1.0.1
 */
public class MessageException extends IOException {

  private static final long serialVersionUID = 7129156393920783825L;

  private StatusInformation statusInformation;
  private int snmp4jErrorStatus;

  public MessageException() {
  }

  /**
   * Creates a <code>MessageException</code> from a
   * <code>StatusInformation</code> object.
   * @param status
   *   a <code>StatusInformation</code> instance.
   */
  public MessageException(StatusInformation status) {
    super(""+status.getErrorIndication());
    setStatusInformation(status);
  }

  public MessageException(String message) {
    super(message);
  }

  /**
   * Creates a <code>MessageException</code> with error message and
   * SNMP4J specific error status (see {@link #getSnmp4jErrorStatus()}
   * for details.
   *
   * @param message
   *    an error message.
   * @param snmp4jErrorStatus
   *    a {@link org.snmp4j.mp.MessageProcessingModel} or {@link org.snmp4j.security.SecurityModel}
   *    specific error status as defined by {@link org.snmp4j.mp.SnmpConstants}.
   * @since 2.2
   */
  public MessageException(String message, int snmp4jErrorStatus) {
    super(message);
    this.snmp4jErrorStatus = snmp4jErrorStatus;
  }

  /**
   * Creates a <code>MessageException</code> with error message and
   * SNMP4J specific error status (see {@link #getSnmp4jErrorStatus()}
   * for details.
   *
   * @param message
   *    an error message.
   * @param snmp4jErrorStatus
   *    a {@link org.snmp4j.mp.MessageProcessingModel} or {@link org.snmp4j.security.SecurityModel}
   *    specific error status as defined by {@link org.snmp4j.mp.SnmpConstants}.
   * @param rootCause
   *    the root cause represented by a Throwable.
   * @since 2.2.6
   */
  public MessageException(String message, int snmp4jErrorStatus, Throwable rootCause) {
    super(message, rootCause);
    this.snmp4jErrorStatus = snmp4jErrorStatus;
  }

  public StatusInformation getStatusInformation() {
    return statusInformation;
  }

  public void setStatusInformation(StatusInformation statusInformation) {
    this.statusInformation = statusInformation;
  }

  /**
   * Gets the SNMP4J specific error status associated with this exception.
   *
   * @return
   *    a {@link org.snmp4j.mp.MessageProcessingModel} or {@link org.snmp4j.security.SecurityModel}
   *    specific error status as defined by {@link org.snmp4j.mp.SnmpConstants}.
   * @since 2.2
   */
  public int getSnmp4jErrorStatus() {
    return snmp4jErrorStatus;
  }
}

