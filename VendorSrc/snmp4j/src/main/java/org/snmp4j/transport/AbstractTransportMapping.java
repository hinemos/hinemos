/*_############################################################################
  _## 
  _##  SNMP4J - AbstractTransportMapping.java  
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

import org.snmp4j.TransportMapping;
import org.snmp4j.MessageDispatcher;
import java.io.IOException;

import org.snmp4j.TransportStateReference;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.TcpAddress;

import java.util.ArrayList;
import java.util.List;
import java.nio.ByteBuffer;

/**
 * The <code>AbstractTransportMapping</code> provides an abstract
 * implementation for the message dispatcher list and the maximum inbound
 * message size.
 *
 * @author Frank Fock
 * @version 2.0
 */
public abstract class AbstractTransportMapping<A extends Address>
    implements TransportMapping<A> {

  protected List<TransportListener> transportListener = new ArrayList<TransportListener>(1);
  protected int maxInboundMessageSize = (1 << 16) - 1;
  protected boolean asyncMsgProcessingSupported = true;

  public abstract Class<? extends Address> getSupportedAddressClass();

  public abstract void sendMessage(A address, byte[] message,
                                   TransportStateReference tmStateReference)
          throws IOException;

  public synchronized void addTransportListener(TransportListener l) {
    if (!transportListener.contains(l)) {
      List<TransportListener> tlCopy =
              new ArrayList<TransportListener>(transportListener);
      tlCopy.add(l);
      transportListener = tlCopy;
    }
  }

  public synchronized void removeTransportListener(TransportListener l) {
    if (transportListener != null && transportListener.contains(l)) {
      List<TransportListener> tlCopy = new ArrayList<TransportListener>(transportListener);
      tlCopy.remove(l);
      transportListener = tlCopy;
    }
  }

  protected void fireProcessMessage(Address address, ByteBuffer buf,
                                    TransportStateReference tmStateReference) {
    if (transportListener != null) {
      for (TransportListener aTransportListener : transportListener) {
        aTransportListener.processMessage(this, address, buf, tmStateReference);
      }
    }
  }


  public abstract void close() throws IOException;

  public abstract void listen() throws IOException;

  public int getMaxInboundMessageSize() {
    return maxInboundMessageSize;
  }

  /**
   * Returns <code>true</code> if asynchronous (multi-threaded) message
   * processing may be implemented. The default is <code>true</code>.
   *
   * @return if <code>false</code> is returned the
   * {@link MessageDispatcher#processMessage(org.snmp4j.TransportMapping, org.snmp4j.smi.Address, java.nio.ByteBuffer, org.snmp4j.TransportStateReference)}
   * method must not return before the message has been entirely processed.
   */
  public boolean isAsyncMsgProcessingSupported() {
    return asyncMsgProcessingSupported;
  }

  /**
   * Specifies whether this transport mapping has to support asynchronous
   * messages processing or not.
   *
   * @param asyncMsgProcessingSupported if <code>false</code> the {@link MessageDispatcher#processMessage(org.snmp4j.TransportMapping, org.snmp4j.smi.Address, java.nio.ByteBuffer, org.snmp4j.TransportStateReference)}
   *                                    method must not return before the message has been entirely processed,
   *                                    because the incoming message buffer is not copied before the message
   *                                    is being processed. If <code>true</code> the message buffer is copied
   *                                    for each call, so that the message processing can be implemented
   *                                    asynchronously.
   */
  public void setAsyncMsgProcessingSupported(boolean asyncMsgProcessingSupported) {
    this.asyncMsgProcessingSupported = asyncMsgProcessingSupported;
  }
}
