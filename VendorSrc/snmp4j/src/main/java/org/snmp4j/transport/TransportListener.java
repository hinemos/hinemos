/*_############################################################################
  _## 
  _##  SNMP4J - TransportListener.java  
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

import org.snmp4j.TransportStateReference;
import org.snmp4j.smi.Address;
import org.snmp4j.TransportMapping;
import java.nio.ByteBuffer;
// JavaDoc import
import org.snmp4j.MessageDispatcher;

/**
 * The <code>TransportListener</code> interface is implemented by objects
 * that process incoming messages from <code>TransportMapping</code>s, for
 * example {@link MessageDispatcher}.
 *
 * @author Frank Fock
 * @version 2.0
 * @since 1.6
 */
public interface TransportListener {

  /**
   * Processes an incoming message.
   * @param sourceTransport
   *    a <code>TransportMapping</code> instance denoting the transport that
   *    received the message and that will be used to send any responses to
   *    this message. The <code>sourceTransport</code> has to support the
   *    <code>incomingAddress</code>'s implementation class.
   * @param incomingAddress
   *    the <code>Address</code> from which the message has been received.
   * @param wholeMessage
   *    an <code>ByteBuffer</code> containing the received message.
   * @param tmStateReference
   *    the transport model state reference as defined by RFC 5590.
   * @since 1.6
   */
  void processMessage(TransportMapping sourceTransport,
                      Address incomingAddress,
                      ByteBuffer wholeMessage,
                      TransportStateReference tmStateReference);
}
