/*_############################################################################
  _## 
  _##  SNMP4J - PduHandleCallback.java  
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
 * The <code>PduHandleCallback</code> can be used to get informed about a
 * <code>PduHandle</code> creation before a request is actually sent out.
 *
 * @author Frank Fock
 * @version 1.8
 * @since 1.8
 */
public interface PduHandleCallback<P> {

  /**
   * A new PduHandle has been created for a PDU. This event callback
   * notification can be used to get informed about a new PduHandle
   * (just) before a PDU has been sent out.
   *
   * @param handle
   *   a <code>PduHandle</code> instance that uniquely identifies a request -
   *   thus in most cases the request ID.
   * @param pdu
   *    the request PDU for which the handle has been created.
   */
  void pduHandleAssigned(PduHandle handle, P pdu);

}
