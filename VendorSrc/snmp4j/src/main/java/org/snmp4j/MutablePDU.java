/*_############################################################################
  _## 
  _##  SNMP4J - MutablePDU.java  
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

import org.snmp4j.PDU;
import java.io.Serializable;

/**
 * The <code>MutablePDU</code> is a container for a <code>PDU</code>
 * instance.
 * @author Frank Fock
 * @version 1.0
 */
public class MutablePDU implements Serializable {

  private static final long serialVersionUID = 2511133364341663087L;

  private PDU pdu;

  public MutablePDU() {
  }

  public PDU getPdu() {
    return pdu;
  }

  public void setPdu(PDU pdu) {
    this.pdu = pdu;
  }
}
