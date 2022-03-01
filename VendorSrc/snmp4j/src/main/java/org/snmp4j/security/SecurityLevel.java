/*_############################################################################
  _## 
  _##  SNMP4J - SecurityLevel.java  
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

/**
 * The <code>SecurityLevel</code> interface contains enumerated values
 * for the different security levels.
 *
 * @author Frank Fock
 * @version 2.0
 */
public enum SecurityLevel {
  undefined(0),
  noAuthNoPriv(1),
  authNoPriv(2),
  authPriv(3);

  /**
   * No authentication and no encryption.
   * Anyone can create and read messages with this security level
   */
  public static final int NOAUTH_NOPRIV = 1;

  /**
   * Authentication and no encryption.
   * Only the one with the right authentication key can create messages
   * with this security level, but anyone can read the contents of
   * the message.
   */
  public static final int AUTH_NOPRIV = 2;

  /**
   * Authentication and encryption.
   * Only the one with the right authentication key can create messages
   * with this security level, and only the one with the right
   * encryption/decryption key can read the contents of the message.
   */
  public static final int AUTH_PRIV = 3;

  private int snmpValue;

  private SecurityLevel(int snmpValue) {
    this.snmpValue = snmpValue;
  }

  /**
   * Gets the SNMP value of this security level.
   * @return
   *    1 for noAuthNoPriv
   *    2 for authNoPriv
   *    3 for authPriv
   */
  public int getSnmpValue() {
    return snmpValue;
  }

  public static SecurityLevel get(int snmpValue) {
    switch (snmpValue) {
      case NOAUTH_NOPRIV: {
        return SecurityLevel.noAuthNoPriv;
      }
      case AUTH_NOPRIV: {
        return SecurityLevel.authNoPriv;
      }
      case AUTH_PRIV: {
        return SecurityLevel.authPriv;
      }
    }
    return SecurityLevel.undefined;
  }
}
