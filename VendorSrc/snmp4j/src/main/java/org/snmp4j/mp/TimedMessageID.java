/*_############################################################################
  _## 
  _##  SNMP4J - TimedMessageID.java  
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

package org.snmp4j.mp;

/**
 * The <code>TimedMessageID</code> adds system time information to the message ID that allows
 * to measure response times and detect lost messages with SNMPv3.
 *
 * @author Frank Fock
 * @since 2.4.3
 */
public class TimedMessageID extends SimpleMessageID {

  private static final long serialVersionUID = 952343921331667512L;

  private long creationNanoTime;

  public TimedMessageID(int messageID) {
    super(messageID);
    this.creationNanoTime = System.nanoTime();
  }

  /**
   * Gets the {@link System#nanoTime()} when this message ID object has been created.
   * @return
   *    the creation time stamp in nano seconds.
   */
  public long getCreationNanoTime() {
    return creationNanoTime;
  }

  @Override
  public String toString() {
    return "TimedMessageID{" +
        "msgID="+getID()+
        ",creationNanoTime=" + creationNanoTime +
        "}";
  }
}
