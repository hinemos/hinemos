/*_############################################################################
  _## 
  _##  SNMP4J - UsmUserEvent.java  
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

import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.UsmUserEntry;
// needed for JavaDoc
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUserTable;

/**
 * This Event is issued whenever a user of the {@link USM} is created
 * modified or deleted.
 *
 * @author Frank Fock
 * @version 1.0
 */
public class UsmUserEvent extends EventObject {

  private static final long serialVersionUID = -2650579887988635391L;

  /**
   * Constant: a new user was created.
   */
  public static final int USER_ADDED = 1;

  /**
   * Constant: a user was deleted.
   */
  public static final int USER_REMOVED = 2;

  /**
   * Constant: a user was changed (but not deleted).
   */
  public static final int USER_CHANGED = 3;

  private org.snmp4j.security.UsmUserEntry user;
  private int type;

  /**
   * Construct a UsmUserEvent.
   *
   * @param source
   *    the object that emitts this event
   * @param changedEntry
   *    the changed entry
   * @param type
   *    can be USER_ADDED, USER_REMOVED or USER_CHANGED.
   */
  public UsmUserEvent(SecurityModel source, UsmUserEntry changedEntry, int type) {
    super(source);
    this.user = changedEntry;
    this.type = type;
  }

  /**
   * Get the modified entry of the {@link UsmUserTable}.
   *
   * @return the entry <ul>
   *     <li> after the modification if the user was added or modified
   *     <li> before the modification if the user was deleted </ul>
   */
  public org.snmp4j.security.UsmUserEntry getUser() {
    return user;
  }

  /**
   * Return the type of operation that triggered this event.
   *
   * @return One of USER_ADDED, USER_REMOVED or USER_CHANGED.
   */
  public int getType() {
    return type;
  }
}
