/*_############################################################################
  _## 
  _##  SNMP4J - TcpTransportMapping.java  
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

import java.io.IOException;

import org.snmp4j.SNMP4JSettings;
import org.snmp4j.TransportStateReference;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TcpAddress;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.snmp4j.log.LogFactory;
import org.snmp4j.log.LogAdapter;

/**
 * The <code>TcpTransportMapping</code> is the abstract base class for
 * TCP transport mappings.
 *
 * @author Frank Fock
 * @version 2.8.6
 */
public abstract class TcpTransportMapping
    extends AbstractTransportMapping<TcpAddress>
    implements ConnectionOrientedTransportMapping<TcpAddress>
{

  private static final LogAdapter logger =
      LogFactory.getLogger(TcpTransportMapping.class);

  protected TcpAddress tcpAddress;
  private transient Vector<TransportStateListener> transportStateListeners;
  protected Set<Address> suspendedAddresses = ConcurrentHashMap.newKeySet(5);
  /**
   * Enable or disable automatic (re)opening the communication socket when sending a message
   */
  protected boolean openSocketOnSending = true;

  public TcpTransportMapping(TcpAddress tcpAddress) {
    this.tcpAddress = tcpAddress;
  }

  public Class<? extends Address> getSupportedAddressClass() {
    return TcpAddress.class;
  }

  /**
   * Returns the transport address that is used by this transport mapping for
   * sending and receiving messages.
   * @return
   *    the <code>Address</code> used by this transport mapping. The returned
   *    instance must not be modified!
   */
  public TcpAddress getAddress() {
    return tcpAddress;
  }

  public TcpAddress getListenAddress() {
    return tcpAddress;
  }

  public abstract void sendMessage(TcpAddress address, byte[] message,
                                   TransportStateReference tmStateReference)
      throws IOException;

  public abstract void listen() throws IOException;

  public abstract void close() throws IOException;

  /**
   * Returns the <code>MessageLengthDecoder</code> used by this transport
   * mapping.
   * @return
   *    a MessageLengthDecoder instance.
   * @since 1.7
   */
  public abstract MessageLengthDecoder getMessageLengthDecoder();

  /**
   * Sets the <code>MessageLengthDecoder</code> that decodes the total
   * message length from the header of a message.
   *
   * @param messageLengthDecoder
   *    a MessageLengthDecoder instance.
   * @since 1.7
   */
  public abstract void setMessageLengthDecoder(MessageLengthDecoder messageLengthDecoder);

  /**
   * Sets the connection timeout. This timeout specifies the time a connection
   * may be idle before it is closed.
   * @param connectionTimeout
   *    the idle timeout in milliseconds. A zero or negative value will disable
   *    any timeout and connections opened by this transport mapping will stay
   *    opened until they are explicitly closed.
   * @since 1.7
   */
  public abstract void setConnectionTimeout(long connectionTimeout);

  public synchronized void addTransportStateListener(TransportStateListener l) {
    if (transportStateListeners == null) {
      transportStateListeners = new Vector<TransportStateListener>(2);
    }
    transportStateListeners.add(l);
  }

  public synchronized void removeTransportStateListener(TransportStateListener
      l) {
    if (transportStateListeners != null) {
      transportStateListeners.remove(l);
    }
  }

  protected void fireConnectionStateChanged(TransportStateEvent change) {
    if (logger.isDebugEnabled()) {
      logger.debug("Firing transport state event: "+change);
    }
    final List<TransportStateListener> listenersFinalRef = transportStateListeners;
    if (listenersFinalRef != null) {
      try {
        List<TransportStateListener> listeners;
        synchronized (listenersFinalRef) {
          listeners = new ArrayList<TransportStateListener>(listenersFinalRef);
        }
        for (TransportStateListener listener : listeners) {
          listener.connectionStateChanged(change);
        }
      }
      catch (RuntimeException ex) {
        logger.error("Exception in fireConnectionStateChanged: "+ex.getMessage(), ex);
        if (SNMP4JSettings.isForwardRuntimeExceptions()) {
          throw ex;
        }
      }
    }
  }

  abstract Object removeSocketEntry(TcpAddress incomingAddress);

  /**
   * If {@code true} and method {@link #listen()} has not been called yet or the connection has been closed or reset,
   * then {@link #listen()} will be called to open the communication socket when a message is being sent using
   * {@link #sendMessage(TcpAddress, byte[], TransportStateReference)}.
   *
   * @return
   *     {@code true} if {@link #sendMessage(TcpAddress, byte[], TransportStateReference)} will ensure that
   *     a server socket is there for receiving responses, {@code false} otherwise.
   * @since 2.8.6
   */
  public boolean isOpenSocketOnSending() {
    return openSocketOnSending;
  }

  /**
   * Activate or deactivate auto {@link #listen()} when
   * {@link #sendMessage(TcpAddress, byte[], TransportStateReference)} is called but there is no listening
   * socket.
   *
   * @param openSocketOnSending
   *     {@code true} if {@link #sendMessage(TcpAddress, byte[], TransportStateReference)} should ensure
   *     that server socket is available for communication, {@code false} if {@link #listen()} must be called
   *     explicitly.
   * @since 2.8.6
   */
  public void setOpenSocketOnSending(boolean openSocketOnSending) {
    this.openSocketOnSending = openSocketOnSending;
  }

  /**
   * Suspend sending of messages to the specified address, regardless if a connection is already established or
   * not. To be able to send messages again to the specified address using
   * {@link #sendMessage(TcpAddress, byte[], TransportStateReference)}, call {@link #resumeAddress}.
   * @param addressToSuspendSending
   *    an arbitrary remote address for which any messages send by
   *    {@link #sendMessage(TcpAddress, byte[], TransportStateReference)} should be dropped before sending
   *    and reopening a connection to that address.
   * @since 2.8.6
   */
  public void suspendAddress(TcpAddress addressToSuspendSending) {
    suspendedAddresses.add(addressToSuspendSending);
  }

  /**
   * Resume sending of messages to the specified address.
   * @param addressToResumeSending
   *    an arbitrary remote address for which any messages send by
   *    {@link #sendMessage(TcpAddress, byte[], TransportStateReference)} should be dropped before sending
   *    and reopening a connection to that address.
   * @return
   *    {@code true} if the specified address was previously suspended and is now resumed to allow sending messages,
   *    {@code false} otherwise.
   * @since 2.8.6
   */
  public boolean resumeAddress(TcpAddress addressToResumeSending) {
    return suspendedAddresses.remove(addressToResumeSending);
  }

  /**
   * Handle a message that could not be send to the specified address, because there is no server socket for
   * receiving responses.
   * @param address
   *         an {@code Address} instance denoting the target address.
   * @param message
   *         the whole message as an array of bytes.
   * @param transportStateReference
   *         the (optional) transport model state reference as defined by
   *         RFC 5590 section 6.1.
   * @since 2.8.6
   */
  protected void handleDroppedMessageToSend(TcpAddress address, byte[] message,
                                            TransportStateReference transportStateReference) {
    logger.warn("TCP message to be sent has been dropped, because transport mapping is closed: address="+
            address+", message="+ OctetString.fromByteArray(message).toHexString());
  }

}
