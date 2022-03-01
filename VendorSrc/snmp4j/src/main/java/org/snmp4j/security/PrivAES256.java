/*_############################################################################
  _## 
  _##  SNMP4J - PrivAES256.java  
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
package org.snmp4j.security;

import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.nonstandard.NonStandardSecurityProtocol;
import org.snmp4j.smi.OID;

/**
 * Encryption class for AES 256.
 *
 * @author Jochen Katz
 * @version 1.11
 */
public class PrivAES256 extends PrivAES implements NonStandardSecurityProtocol {

  private static final long serialVersionUID = -4678800188622949146L;

  /**
   * Unique ID of this privacy protocol.
   */
  public static OID ID = new OID(SnmpConstants.oosnmpUsmAesCfb256Protocol);

  private OID oid;

  /**
   * Constructor.
   */
  public PrivAES256() {
    super(32);
  }

  /**
   * Gets the OID uniquely identifying the privacy protocol.
   * @return
   *    an <code>OID</code> instance.
   */
  public OID getID() {
    return (oid == null) ? getDefaultID() : oid;
  }

  @Override
  public void setID(OID newID) {
    oid = new OID(newID);
  }

  @Override
  public OID getDefaultID() {
    return (OID)ID.clone();
  }

}
