/*_############################################################################
  _## 
  _##  SNMP4J - CommandResponder.java  
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


package org.snmp4j;

import java.util.EventListener;

/**
 * {@code CommandResponder} process incoming request, report and
 * notification PDUs. An event may only processed once. A command responder
 * must therefore set the {@code processed} member of the supplied
 * {@code CommandResponderEvent} object to {@code true} when it has
 * processed the PDU.
 *
 * @author Jochen Katz
 * @author Frank Fock
 * @version 1.0
 */
public interface CommandResponder extends EventListener {

  /**
   * Process an incoming request, report or notification PDU.
   * @param event
   *    a {@code CommandResponderEvent} instance containing the PDU to
   *    process and some additional information returned by the message
   *    processing model that decoded the SNMP message.
   */
  void processPdu(CommandResponderEvent event);

}
