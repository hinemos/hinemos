/*_############################################################################
  _## 
  _##  SNMP4J - SubIndexInfoImpl.java  
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
 * The {@link SubIndexInfoImpl} class represents the meta information of a SMI INDEX clause element (= sub-index)
 * which are relevant for converting an OID index value to an INDEX object and vice versa.
 *
 * @author Frank Fock
 * @since 2.5.0
 */
public class SubIndexInfoImpl implements SubIndexInfo {

  private boolean impliedLength;
  private int minLength;
  private int maxLength;
  private int snmpSyntax;

  /**
   * Create a sub index information object.
   * @param impliedLength
   *    indicates if the sub-index value has an implied variable length (must apply to the last variable length
   *    sub-index only).
   * @param minLength
   *    the minimum length in bytes of the sub-index value.
   * @param maxLength
 *      the maximum length in bytes of the sub-index value.
   * @param snmpSyntax
   *    the BER syntax of the sub-index object type's base syntax.
   */
  public SubIndexInfoImpl(boolean impliedLength, int minLength, int maxLength, int snmpSyntax) {
    this.impliedLength = impliedLength;
    this.maxLength = maxLength;
    this.minLength = minLength;
    this.snmpSyntax = snmpSyntax;
  }

  @Override
  public boolean hasImpliedLength() {
    return impliedLength;
  }

  @Override
  public int getMinLength() {
    return minLength;
  }

  @Override
  public int getMaxLength() {
    return maxLength;
  }

  @Override
  public int getSnmpSyntax() {
    return snmpSyntax;
  }

}
