/*_############################################################################
  _## 
  _##  SNMP4J - AuthMD5.java  
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
import org.snmp4j.smi.OID;

/**
 * The AuthMD5 class implements the MD5 authentication protocol.
 *
 * @author Frank Fock
 * @author Jochen Katz
 * @version 1.0
 */
public class AuthMD5
    extends AuthGeneric {

  private static final long serialVersionUID = -5972274836195217352L;

  public static final OID ID = new OID(SnmpConstants.usmHMACMD5AuthProtocol);

  public static final int KEY_LENGTH = 16;

  public AuthMD5() {
    super("MD5", KEY_LENGTH);
  }

  public OID getID() {
    return (OID) ID.clone();
  }
}
