/*_############################################################################
  _## 
  _##  SNMP4J - VariantVariableCallback.java  
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
 * The <code>VariantVariableCallback</code> can be implemented by
 * objects that want to intercept/monitor reading and writing of
 * a <code>VariantVariable</code>'s value.
 *
 * @author Frank Fock
 * @version 1.7
 */
public interface VariantVariableCallback {

  /**
   * The supplied variable's value has been updated.
   * @param variable
   *    the <code>VariantVariable</code> that has been updated.
   */
  void variableUpdated(VariantVariable variable);

  /**
   * The supplied variable needs to be updated because it is about
   * to be read.
   * @param variable
   *    the <code>VariantVariable</code> that will be read.
   */
  void updateVariable(VariantVariable variable);

}
