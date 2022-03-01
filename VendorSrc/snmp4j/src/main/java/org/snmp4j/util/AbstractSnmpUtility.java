/*_############################################################################
  _## 
  _##  SNMP4J - AbstractSnmpUtility.java  
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

import org.snmp4j.Session;

/**
 * The <code>AbstractSnmpUtility</code> is an abstract base class for
 * convenience utility classes to retrieve SNMP data.
 *
 * @author Frank Fock
 * @version 1.8
 */
public abstract class AbstractSnmpUtility {

  protected Session session;
  protected PDUFactory pduFactory;

  /**
   * Creates a <code>AbstractSnmpUtility</code> instance. The created instance
   * is thread safe as long as the supplied <code>Session</code> and
   * <code>PDUFactory</code> are thread safe.
   *
   * @param snmpSession
   *    a SNMP <code>Session</code> instance.
   * @param pduFactory
   *    a <code>PDUFactory</code> instance that creates the PDU that are used
   *    by this instance.
   */
  public AbstractSnmpUtility(Session snmpSession, PDUFactory pduFactory) {
    this.session = snmpSession;
    this.pduFactory = pduFactory;
  }
}
