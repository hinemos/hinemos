/*_############################################################################
  _## 
  _##  SNMP4J - SecureTarget.java  
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

import org.snmp4j.smi.OctetString;
import java.io.*;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.smi.Address;

/**
 * The {@code SecureTarget} is an security model independent abstract class
 * for all targets supporting secure SNMP communication.
 *
 * @author Jochen Katz
 * @author Frank Fock
 * @version 2.0
 */
public abstract class SecureTarget
    extends AbstractTarget implements Serializable {

  private static final long serialVersionUID = 3864834593299255038L;

  /**
   * Default constructor.
   */
  protected SecureTarget() {
  }

  /**
   * Creates a SNMPv3 secure target with an address and security name.
   * @param address
   *    an {@code Address} instance denoting the transport address of the
   *    target.
   * @param securityName
   *    a {@code OctetString} instance representing the security name
   *    of the USM user used to access the target.
   */
  protected SecureTarget(Address address, OctetString securityName) {
    super(address, securityName);
  }

  @Override
  public String toString() {
    return "SecureTarget[" + toStringAbstractTarget() + ']';
  }
}
