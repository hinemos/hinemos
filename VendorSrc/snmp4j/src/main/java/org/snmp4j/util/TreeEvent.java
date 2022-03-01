/*_############################################################################
  _## 
  _##  SNMP4J - TreeEvent.java  
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
package org.snmp4j.util;

import org.snmp4j.smi.VariableBinding;
import org.snmp4j.PDU;

/**
 * The <code>TreeEvent</code> class reports events in a tree retrieval
 * operation.
 *
 * @author Frank Fock
 * @version 1.8
 * @since 1.8
 * @see TreeUtils
 */
public class TreeEvent extends RetrievalEvent {

  private static final long serialVersionUID = 5660517240029018420L;

  public TreeEvent(TreeUtils.TreeRequest source, Object userObject, VariableBinding[] vbs) {
    super(source, userObject, vbs);
  }

  public TreeEvent(TreeUtils.TreeRequest source, Object userObject, int status) {
    super(source, userObject, status);
  }

  public TreeEvent(TreeUtils.TreeRequest source, Object userObject, PDU report) {
    super(source, userObject, report);
  }

  public TreeEvent(TreeUtils.TreeRequest source, Object userObject, Exception exception) {
    super(source, userObject, exception);
  }

  /**
   * Gets the variable bindings retrieved in depth first order from the
   * (sub-)tree.
   *
   * @return VariableBinding[]
   *    a possibly empty or <code>null</code> array of
   *    <code>VariableBindings</code>.
   */
  public VariableBinding[] getVariableBindings() {
    return vbs;
  }

}
