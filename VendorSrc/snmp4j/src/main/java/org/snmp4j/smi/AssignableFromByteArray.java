/*_############################################################################
  _## 
  _##  SNMP4J - AssignableFromByteArray.java  
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
 * The <code>AssignableFromByteArray</code> interface describes objects whose
 * value can be set from a byte array and converted back to a byte array.
 *
 * @author Frank Fock
 * @version 1.8
 * @since 1.7
 */
public interface AssignableFromByteArray {

  /**
   * Sets the value of this object from the supplied byte array.
   * @param value
   *    a byte array.
   */
  void setValue(byte[] value);

  /**
   * Returns the value of this object as a byte array.
   * @return
   *    a byte array.
   * @since 1.8
   */
  byte[] toByteArray();
}
