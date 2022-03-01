/*_############################################################################
  _## 
  _##  SNMP4J - MessageID.java  
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
 * The <code>MessageID</code> interface defines the characteristics of a SNMP message ID
 * as defined by RFC 3412 §6.2.
 *
 * @author Frank Fock
 * @since 2.4.3
 */
public interface MessageID {

  /**
   * Gets the integer representation of the message ID.
   * @return
   *    the message ID as a value between 0 and 2147483647 (inclusive).
   */
  int getID();

}
