/*_############################################################################
  _## 
  _##  SNMP4J - NonStandardSecurityProtocol.java  
  _## 
  _##  Copyright (C) 2003-2020  Frank Fock and Jochen Katz (SNMP4J.org)
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
package org.snmp4j.security.nonstandard;

import org.snmp4j.smi.OID;

/**
 * With the <code>NonStandardSecurityProtocol</code> interface you can modify
 * the ID of a non-standard security protocol to match the ID that is used
 * by that protocol in your environment.
 *
 * @author Frank Fock
 * @since 2.5.0
 */
public interface NonStandardSecurityProtocol {

  /**
   * Assign a new ID to a non-standard security protocol instance.
   *
   * @param newOID
   *    the new security protcol ID for the security protocol class called.
   */
  void setID(OID newOID);

  /**
   * Gets the default ID for this non-standard privacy protocol.
   * @return
   *    the default ID as defined by the OOSNMP-USM-MIB.
   */
  OID getDefaultID();
}
