/*_############################################################################
  _## 
  _##  SNMP4J - AuthenticationFailureEvent.java  
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
import org.snmp4j.smi.Address;
import org.snmp4j.TransportMapping;
import org.snmp4j.asn1.BERInputStream;
// For JavaDoc
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.transport.TransportListener;

/**
 * The <code>AuthenticationFailureEvent</code> class describes the source
 * and type of an authentication failure as well as the message that caused
 * the error.
 *
 * @author Frank Fock
 * @version 1.5
 * @since 1.5
 */
public class AuthenticationFailureEvent extends EventObject {

  private static final long serialVersionUID = -8623553792794471405L;

  private Address address;
  private transient TransportMapping transport;
  private BERInputStream message;
  private int error;

  /**
   * Creates an authentication failure event.
   * @param source
   *    the instance that generated the event.
   * @param sourceAddress
   *    the address from where the failed message has been received.
   * @param transport
   *    the <code>TransportMapping</code> with which the message has been
   *    received.
   * @param error
   *    the SNMP4J MP error status caused by the message
   *    (see {@link SnmpConstants}).
   * @param message
   *    the message as received at the position where processing the message
   *    has stopped.
   */
  public AuthenticationFailureEvent(TransportListener source,
                                    Address sourceAddress,
                                    TransportMapping transport,
                                    int error,
                                    BERInputStream message) {
    super(source);
    this.address = sourceAddress;
    this.transport = transport;
    this.error = error;
    this.message = message;
  }

  /**
   * Returns the transport mapping over which the message has bee received.
   * @return
   *    a <code>TransportMapping</code> instance.
   */
  public TransportMapping getTransport() {
    return transport;
  }

  /**
   * Returns the message received.
   * @return
   *    a <code>BERInputStream</code> at the position where processing of the
   *    message has stopped.
   */
  public BERInputStream getMessage() {
    return message;
  }

  /**
   * Returns the SNMP4J internal error status caused by the message.
   * @return
   *    the error status.
   */
  public int getError() {
    return error;
  }

  /**
   * Returns the source address from which the message has been received.
   * @return
   *    the source <code>Address</code>.
   */
  public Address getAddress() {
    return address;
  }

}
