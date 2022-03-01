/*_############################################################################
  _## 
  _##  SNMP4J - DummyTransport.java  
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

import org.snmp4j.SNMP4JSettings;
import org.snmp4j.TransportStateReference;
import org.snmp4j.log.LogAdapter;
import org.snmp4j.log.LogFactory;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OctetString;
import org.snmp4j.util.WorkerTask;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * The <code>DummyTransport</code> is a test TransportMapping for Command Generators
 * which actually does not sent messages over the network. Instead it provides the message
 * transparently as incoming message over the {@link DummyTransportResponder} on a virtual
 * listen address, regardless to which outbound address the message was sent. The messages
 * are returned even if the <code>listenAddress</code> is left <code>null</code>.
 *
 * @author Frank Fock
 * @since 2.0
 */
public class DummyTransport<A extends IpAddress> extends AbstractTransportMapping<A> {

  private static final LogAdapter logger = LogFactory.getLogger(DummyTransport.class);

  private final Queue<MessageContainer> requests = new ConcurrentLinkedQueue<MessageContainer>();
  private final Queue<MessageContainer> responses = new ConcurrentLinkedQueue<MessageContainer>();
  private boolean listening;
  private A listenAddress;
  private A receiverAddress;
  private WorkerTask listenThread;
  private long sessionID = 0;

  public DummyTransport() {
    this.listening = false;
  }

  public DummyTransport(A senderAddress) {
    this.listenAddress = senderAddress;
  }

  public DummyTransport(A senderAddress, A receiverAddress) {
    this.listenAddress = senderAddress;
    this.receiverAddress = receiverAddress;
  }

  @Override
  public Class<? extends Address> getSupportedAddressClass() {
    return IpAddress.class;
  }

  @Override
  public A getListenAddress() {
    return listenAddress;
  }

  public void setListenAddress(A listenAddress) {
    this.listenAddress = listenAddress;
  }

  @Override
  public void sendMessage(A address, byte[] message, TransportStateReference tmStateReference) throws IOException {
    synchronized (requests) {
      if (logger.isDebugEnabled()) {
        logger.debug("Send request message to '"+address+"': "+ new OctetString(message).toHexString());
      }
      requests.add(new MessageContainer(address, new OctetString(message)));
    }
  }

  @Override
  public void close() throws IOException {
    if (listening) {
      listening = false;
      listenThread.terminate();
      try {
        listenThread.join();
      } catch (InterruptedException e) {
        // ignore
      }
    }
    responses.clear();
  }

  @Override
  public void listen() throws IOException {
    listening = true;
    sessionID++;
    QueueProcessor listener = new QueueProcessor(responses, this);
    this.listenThread = SNMP4JSettings.getThreadFactory().createWorkerThread(
        "DummyTransportMapping_"+getListenAddress(), listener, true);
    this.listenThread.run();
  }

  @Override
  public boolean isListening() {
    return listening;
  }

  public AbstractTransportMapping<A> getResponder(A receiverAddress) {
    this.receiverAddress = receiverAddress;
    return new DummyTransportResponder();
  }

  private class QueueProcessor implements WorkerTask {

    private volatile boolean stop;
    private Queue<MessageContainer> queue;
    private AbstractTransportMapping tm;

    public QueueProcessor(Queue<MessageContainer> queue, AbstractTransportMapping tm) {
      this.queue = queue;
      this.tm = tm;
    }

    @Override
    public void run() {
      while (!stop) {
        MessageContainer nextMessage = null;
        nextMessage = queue.poll();
        if (nextMessage != null) {
          TransportStateReference stateReference =
            new TransportStateReference(DummyTransport.this, listenAddress, null,
                                        SecurityLevel.undefined, SecurityLevel.undefined,
                                        false, sessionID);
          tm.fireProcessMessage(listenAddress,
              ByteBuffer.wrap(nextMessage.getPayload().getValue()), stateReference);
        }
        else {
          try {
            Thread.sleep(50);
          } catch (InterruptedException e) {
            logger.warn("Interrupted QueueProcessor: "+e.getMessage());
          }
        }
      }
    }

    @Override
    public void terminate() {
      stop = true;
    }

    @Override
    public void join() throws InterruptedException {
      stop = true;
      synchronized (this) {
        // wait until run is stopped
      }
    }

    @Override
    public void interrupt() {
      stop = true;
    }
  }

  public class DummyTransportResponder extends AbstractTransportMapping<A> {

    private boolean listening;
    private WorkerTask listenThread;

    @Override
    public Class<? extends Address> getSupportedAddressClass() {
      return DummyTransport.this.getSupportedAddressClass();
    }

    @Override
    public A getListenAddress() {
      return receiverAddress;
    }

    @Override
    public void sendMessage(A address, byte[] message, TransportStateReference tmStateReference) throws IOException {
      if (logger.isDebugEnabled()) {
        logger.debug("Send response message to '"+address+"': "+ new OctetString(message).toHexString());
      }
      responses.add(new MessageContainer(address, new OctetString(message)));
    }

    @Override
    public void close() throws IOException {
      this.listening = false;
      if (listenThread != null) {
        this.listenThread.terminate();
        try {
          this.listenThread.join();
        } catch (InterruptedException e) {
          // ignore
        }
      }
      requests.clear();
    }

    @Override
    public void listen() throws IOException {
      this.listening = true;
      sessionID++;
      QueueProcessor listener = new QueueProcessor(requests, this);
      this.listenThread = SNMP4JSettings.getThreadFactory().createWorkerThread(
          "DummyResponseTransportMapping_"+getListenAddress(), listener, true);
      this.listenThread.run();
    }

    @Override
    public boolean isListening() {
      return this.listening;
    }
  }

  @Override
  public String toString() {
    return "DummyTransport{" +
        "requests=" + requests +
        ", responses=" + responses +
        ", listening=" + listening +
        ", listenAddress=" + listenAddress +
        ", receiverAddress=" + receiverAddress +
        ", listenThread=" + listenThread +
        ", sessionID=" + sessionID +
        '}';
  }

  private class MessageContainer {
    private Address sourceAddress;
    private OctetString payload;

    public MessageContainer(Address sourceAddress, OctetString payload) {
      this.payload = payload;
      this.sourceAddress = sourceAddress;
    }

    public OctetString getPayload() {
      return payload;
    }

    public Address getSourceAddress() {
      return sourceAddress;
    }
  }
}
