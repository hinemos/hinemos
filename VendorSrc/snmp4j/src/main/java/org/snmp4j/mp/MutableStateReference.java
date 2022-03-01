/*_############################################################################
  _## 
  _##  SNMP4J - MutableStateReference.java  
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
 * The <code>MutableStateReference</code> encapsulates a {@link StateReference}
 * for read/write access.
 * @author Frank Fock
 * @version 1.0
 */
public class MutableStateReference {

  private StateReference stateReference;

  public MutableStateReference() {
  }

  public StateReference getStateReference() {
    return stateReference;
  }
  public void setStateReference(StateReference stateReference) {
    this.stateReference = stateReference;
  }

}
