/*_############################################################################
  _## 
  _##  SNMP4J - PDUFactory.java  
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

import org.snmp4j.Target;
import org.snmp4j.PDU;
import org.snmp4j.mp.MessageProcessingModel;

/**
 * <code>PDUFactory</code> defines the interface for PDU factories.
 *
 * @author Frank Fock
 * @version 2.2
 * @since 1.0.2
 */
public interface PDUFactory {

  /**
   * Creates a {@link PDU} instance for the supplied target. The created
   * PDU has to be compliant to the SNMP version defined by the supplied target.
   * For example, a SNMPv3 target requires a ScopedPDU instance.
   *
   * @param target
   *    the <code>Target</code> where the PDU to be created will be sent.
   * @return PDU
   *    a PDU instance that is compatible with the supplied target.
   */
  PDU createPDU(Target target);

  /**
   * Creates a {@link PDU} instance that is compatible with the given SNMP version
   * (message processing model).
   * @param messageProcessingModel
   *    a {@link org.snmp4j.mp.MessageProcessingModel} instance.
   * @return
   *    a {@link PDU} instance that is compatible with the given SNMP version
   *   (message processing model).
   * @since 2.2
   */
  PDU createPDU(MessageProcessingModel messageProcessingModel);

}
