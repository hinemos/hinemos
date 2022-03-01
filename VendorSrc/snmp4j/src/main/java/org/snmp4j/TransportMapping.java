/*_############################################################################
  _## 
  _##  SNMP4J - TransportMapping.java  
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

import java.io.IOException;
import org.snmp4j.smi.Address;
import org.snmp4j.transport.TransportListener;

/**
 * The {@code TransportMapping} defines the common interface for SNMP
 * transport mappings. A transport mapping can only support a single
 * transport protocol.
 *
 * @author Frank Fock
 * @version 2.0
 */
public interface TransportMapping<A extends Address> {

  /**
   * Gets the {@code Address} class that is supported by this transport mapping.
   * @return
   *    a subclass of {@link Address}.
   */
  Class<? extends Address> getSupportedAddressClass();

  /**
   * Returns the address that represents the actual incoming address this transport
   * mapping uses to listen for incoming packets.
   *
   * @return
   *    the address for incoming packets or {@code null} this transport
   *    mapping is not configured to listen for incoming packets.
   * @since 1.6
   */
  A getListenAddress();

  /**
   * Sends a message to the supplied address using this transport.
   * @param address
   *    an {@code Address} instance denoting the target address.
   * @param message
   *    the whole message as an array of bytes.
   * @param tmStateReference
   *    the (optional) transport model state reference as defined by
   *    RFC 5590 section 6.1.
   * @throws IOException
   *    if any underlying IO operation fails.
   */
  void sendMessage(A address, byte[] message,
                   TransportStateReference tmStateReference) throws IOException;

  /**
   * Adds a transport listener to the transport. Normally, at least one
   * transport listener needs to be added to process incoming messages.
   * @param transportListener
   *    a {@code TransportListener} instance.
   * @since 1.6
   */
  void addTransportListener(TransportListener transportListener);

  /**
   * Removes a transport listener. Incoming messages will no longer be
   * propagated to the supplied {@code TransportListener}.
   * @param transportListener
   *    a {@code TransportListener} instance.
   * @since 1.6
   */
  void removeTransportListener(TransportListener transportListener);

  /**
   * Closes the transport an releases all bound resources synchronously.
   * @throws IOException
   *    if any IO operation for the close fails.
   */
  void close() throws IOException;

  /**
   * Listen for incoming messages. For connection oriented transports, this
   * method needs to be called before {@link #sendMessage} is called for the
   * first time.
   * @throws IOException
   *    if an IO operation exception occurs while starting the listener.
   */
  void listen() throws IOException;

  /**
   * Returns {@code true} if the transport mapping is listening for
   * incoming messages. For connection oriented transport mappings this
   * is a prerequisite to be able to send SNMP messages. For connectionless
   * transport mappings it is a prerequisite to be able to receive responses.
   * @return
   *    {@code true} if this transport mapping is listening for messages.
   * @since 1.1
   */
  boolean isListening();

  /**
   * Gets the maximum length of an incoming message that can be successfully
   * processed by this transport mapping implementation.
   * @return
   *    an integer &gt; 484.
   */
  int getMaxInboundMessageSize();
}

