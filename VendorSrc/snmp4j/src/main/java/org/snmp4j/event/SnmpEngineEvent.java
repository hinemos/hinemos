/*_############################################################################
  _## 
  _##  SNMP4J - SnmpEngineEvent.java  
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

import java.util.EventObject;

import org.snmp4j.mp.MPv3;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Address;

/**
 * The <code>SnmpEngineEvent</code> describes events generated on behalf of
 * the engine ID cache of the SNMPv3 message processing model (MPv3).
 *
 * @author Frank Fock
 * @version 1.6
 * @since 1.6
 */
public class SnmpEngineEvent extends EventObject {

  private static final long serialVersionUID = -7287039511083410591L;

  public static final int ADDED_ENGINE_ID = 1;
  public static final int REMOVED_ENGINE_ID = 2;
  public static final int IGNORED_ENGINE_ID = 3;

  private OctetString engineID;
  private Address engineAddress;
  private int type;

  public SnmpEngineEvent(MPv3 source, int type,
                         OctetString engineID, Address engineAddress) {
    super(source);
    this.engineID = engineID;
    this.type = type;
    this.engineAddress = engineAddress;
  }

  /**
   * Returns the type of the engine event.
   * @return
   *    one of the integer constants defined by this class.
   */
  public int getType() {
    return type;
  }

  /**
   * Returns the engine ID associated with this event.
   * @return
   *    the engine ID.
   */
  public OctetString getEngineID() {
    return engineID;
  }

  /**
   * Returns the transport address of the engine.
   * @return
   *    the transport address.
   */
  public Address getEngineAddress() {
    return engineAddress;
  }

}
