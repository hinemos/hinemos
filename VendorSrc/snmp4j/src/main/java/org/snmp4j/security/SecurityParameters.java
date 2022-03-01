/*_############################################################################
  _## 
  _##  SNMP4J - SecurityParameters.java  
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

import org.snmp4j.asn1.BERSerializable;

/**
 * The {@code SecurityParameters} interface represents the security
 * parameters in a SNMPv3 message.
 *
 * @author Frank Fock
 * @version 1.0
 */
public interface SecurityParameters extends BERSerializable {

  /**
   * Gets the byte position of the first byte (counted from zero) of the
   * security parameters in the whole message.
   * @return
   *    the position of the first byte (counted from zero) of the security
   *    parameters in the whole SNMP message. -1 is returned, when the position
   *    is unknown (not set).
   */
  int getSecurityParametersPosition();

  /**
   * Sets the position of the first byte (counted from zero) of the security
   * parameters in the whole SNMP message.
   * @param pos
   *    an integer value greater or equal than zero.
   */
  void setSecurityParametersPosition(int pos);

  /**
   * Gets the maximum length of the BER encoded representation of this
   * {@code SecurityParameters} instance.
   * @param securityLevel
   *    the security level to be used.
   * @return
   *    the maximum BER encoded length in bytes.
   */
  int getBERMaxLength(int securityLevel);
}
