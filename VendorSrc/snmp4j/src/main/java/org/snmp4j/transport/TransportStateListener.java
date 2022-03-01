/*_############################################################################
  _## 
  _##  SNMP4J - TransportStateListener.java  
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

package org.snmp4j.transport;

import java.util.EventListener;

/**
 * The <code>TransportStateListener</code> interface can be implemented
 * to monitor the connection state for connection oriented transport mappings.
 *
 * @author Frank Fock
 * @version 1.7
 * @since 1.7
 */
public interface TransportStateListener extends EventListener {

  /**
   * The connection state of a transport protocol connection has been changed.
   * @param change
   *    a <code>TransportStateEvent</code> instance.
   */
  void connectionStateChanged(TransportStateEvent change);
}
