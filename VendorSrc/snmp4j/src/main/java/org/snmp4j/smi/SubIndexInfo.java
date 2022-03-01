/*_############################################################################
  _## 
  _##  SNMP4J - SubIndexInfo.java  
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
 * The {@link SubIndexInfo} interface represents the meta information of a SMI INDEX clause element (= sub-index)
 * which are relevant for converting an OID index value to an INDEX object and vice versa.
 *
 * @author Frank Fock
 * @since 2.5.0
 */
public interface SubIndexInfo {
  /**
   * Checks if the sub-index represented by this index info has an implied length or not.
   *
   * @return <code>true</code> if the length of this variable length sub-index is implied (i.e., the sub-index
   * is the last in the index).
   */
  boolean hasImpliedLength();

  /**
   * Gets the minimum length in bytes of the sub-index. If min and max length are equal, then this sub-index
   * is a fixed length length sub-index.
   *
   * @return the minimum length of the (sub-index) in bytes.
   */
  int getMinLength();

  /**
   * Gets the maximum length in bytes of the sub-index. If min and max length are equal, then this sub-index
   * is a fixed length length sub-index.
   *
   * @return the maximum length of the (sub-index) in bytes.
   */
  int getMaxLength();

  /**
   * Gets the SNMP syntax value of the sub-index' base syntax.
   *
   * @return the SNMP4J (BER) syntax identifier.
   */
  int getSnmpSyntax();
}
