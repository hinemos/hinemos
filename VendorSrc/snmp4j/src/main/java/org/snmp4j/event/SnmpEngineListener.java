/*_############################################################################
  _## 
  _##  SNMP4J - SnmpEngineListener.java  
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


package org.snmp4j.event;

import java.util.EventListener;

/**
 * The <code>SnmpEngineListener</code> interface can be implemented by classes
 * that need to be informed about changes to the SNMP engine ID cache.
 *
 * @author Frank Fock
 * @version 1.6
 * @since 1.6
 */
public interface SnmpEngineListener extends EventListener {

  /**
   * An SNMP engine has been added to or removed from the engine cache.
   * @param engineEvent
   *    the SnmpEngineEvent object describing the engine that has been added.
   */
  void engineChanged(SnmpEngineEvent engineEvent);

}
