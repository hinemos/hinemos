/*_############################################################################
  _## 
  _##  SNMP4J - Address.java  
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

package org.snmp4j.smi;

/**
 * The <code>Address</code> interface serves as a base class for all SNMP
 * transport addresses.
 * <p>
 * Note: This class should be moved to package <code>org.snmp4j</code>
 * in SNMP4J 2.0.
 * </p>
 *
 * @author Frank Fock
 * @version 2.0
 */
public interface Address extends AssignableFromString, AssignableFromByteArray {

  /**
   * Checks whether this <code>Address</code> is a valid transport address.
   * @return
   *    <code>true</code> if the address is valid, <code>false</code> otherwise.
   */
  boolean isValid();

  /**
   * Parses the address from the supplied string representation.
   * @param address
   *    a String representation of this address.
   * @return
   *    <code>true</code> if <code>address</code> could be successfully
   *    parsed and has been assigned to this address object, <code>false</code>
   *    otherwise.
   */
  boolean parseAddress(String address);

  /**
   * Sets the address value from the supplied String. The string must match
   * the format required for the Address instance implementing this interface.
   * Otherwise an {@link IllegalArgumentException} runtime exception is thrown.
   * @param address
   *    an address String.
   * @since 1.7
   */
  void setValue(String address);
}

