/*_############################################################################
  _## 
  _##  SNMP4J - PduHandle.java  
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

import java.io.Serializable;

/**
 * The <code>PduHandle</code> class represents an unique key for a SNMP PDU.
 * It uses an unique transaction ID (request ID) to identify the PDUs.
 *
 * @author Frank Fock
 * @version 1.0.3
 * @since 1.0
 */
public class PduHandle implements Serializable {

  private static final long serialVersionUID = -6365764428974409942L;

  public static final int NONE = Integer.MIN_VALUE;
  private int transactionID = NONE;

  /**
   * Creates a <code>PduHandle</code> with a transaction ID set to {@link #NONE}.
   */
  public PduHandle() {
  }

  /**
   * Creates a <code>PduHandle</code> for the supplied transaction ID.
   * @param transactionID
   *    an unqiue transaction ID.
   */
  public PduHandle(int transactionID) {
    setTransactionID(transactionID);
  }

  /**
   * Gets the transaction ID of this handle.
   * @return
   *    the transaction ID.
   */
  public int getTransactionID() {
    return transactionID;
  }

  /**
   * Sets the transaction ID which is typically the request ID of the PDU.
   * @param transactionID
   *    an unqiue transaction ID.
   */
  public void setTransactionID(int transactionID) {
    this.transactionID = transactionID;
  }

  /**
   * Copy all members from the supplied <code>PduHandle</code>.
   * @param other
   *    a PduHandle.
   */
  public void copyFrom(PduHandle other) {
    setTransactionID(other.transactionID);
  }

  /**
   * Indicates whether some other object is "equal to" this one.
   *
   * @param obj the reference object with which to compare.
   * @return <code>true</code> if this object is the same as the obj argument;
   *   <code>false</code> otherwise.
   */
  public boolean equals(Object obj) {
    return (obj instanceof PduHandle) && (transactionID == ((PduHandle) obj).transactionID);
  }

  /**
   * Returns a hash code value for the object.
   *
   * @return a hash code value for this object.
   */
  public int hashCode() {
    return transactionID;
  }

  public String toString() {
    return "PduHandle["+transactionID+"]";
  }

}

