/*_############################################################################
  _## 
  _##  SNMP4J - CounterListener.java  
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
// for JavaDoc
import org.snmp4j.security.SecurityModel;
import org.snmp4j.mp.MessageProcessingModel;

/**
 * The <code>CounterListener</code> interface has to be implemented by listener
 * for {@link CounterEvent} events. By implementing this method, an object is
 * able to be informed by a {@link MessageProcessingModel},
 * {@link SecurityModel}, or other objects about conditions causing
 * certain counters to be incremented.
 *
 * @author Frank Fock
 * @version 1.0
 */
public interface CounterListener extends EventListener {

  /**
   * Increment the supplied counter instance and return the current value
   * (after incrementation) in the event object if the event receiver is the
   * maintainer of the counter value.
   * @param event
   *   a <code>CounterEvent</code> instance.
   */
  void incrementCounter(CounterEvent event);

}
