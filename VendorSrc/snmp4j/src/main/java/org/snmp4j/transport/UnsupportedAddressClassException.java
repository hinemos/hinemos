/*_############################################################################
  _## 
  _##  SNMP4J - UnsupportedAddressClassException.java  
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

import org.snmp4j.MessageException;

/**
 * The <code>UnsupportedAddressClassException</code> indicates a message
 * exception caused by unsupported address class. When this exception is
 * thrown, the target address class is not supported by the entity that
 * is to sent the message and that operation will be canceled.
 *
 * @author Frank Fock
 * @version 1.6
 */
public class UnsupportedAddressClassException extends MessageException {

  private static final long serialVersionUID = -864696255672171900L;

  private Class addressClass;

  public UnsupportedAddressClassException(String message, Class addressClass) {
    super(message);
    this.addressClass = addressClass;
  }

  /**
   * Returns the class of the address class that is not supported.
   * @return
   *    a Class.
   */
  public Class getAddressClass() {
    return addressClass;
  }
}
