/*_############################################################################
  _## 
  _##  SNMP4J - DefaultTcpTransportMappingTest.java  
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

import junit.framework.TestCase;
import org.junit.Before;
import org.snmp4j.PDU;
import org.snmp4j.TransportMapping;
import org.snmp4j.TransportStateReference;
import org.snmp4j.asn1.BEROutputStream;
import org.snmp4j.log.ConsoleLogAdapter;
import org.snmp4j.log.ConsoleLogFactory;
import org.snmp4j.log.LogAdapter;
import org.snmp4j.log.LogFactory;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.util.ThreadPool;
import org.snmp4j.util.WorkerTask;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class DefaultTcpTransportMappingTest extends TestCase {

    public static final int MAX_MESSAGE_FOR_LOAD_TEST = 100;

    static {
        LogFactory.setLogFactory(new ConsoleLogFactory());
        ConsoleLogAdapter.setWarnEnabled(true);
    }

    private static final LogAdapter LOGGER = LogFactory.getLogger(DefaultTcpTransportMappingTest.class);

    public void testSendMessage() throws Exception {
        DefaultTcpTransportMapping serverTransportMapping = new DefaultTcpTransportMapping();
        final List<OctetString> bytesReceivedList = new ArrayList<OctetString>();
        final Object sync = new Object();
        serverTransportMapping.addTransportListener(new TransportListener() {
            @Override
            public void processMessage(TransportMapping sourceTransport, Address incomingAddress,
                                       ByteBuffer wholeMessage, TransportStateReference tmStateReference) {
                OctetString bytesReceived = new OctetString(wholeMessage.array());
                System.out.println("Received from "+incomingAddress+": "+bytesReceived.toHexString());
                bytesReceivedList.add(bytesReceived);
                synchronized (sync) {
                    sync.notify();
                }
            }
        });
        serverTransportMapping.setServerEnabled(true);
        serverTransportMapping.listen();
        TcpAddress serverAddress = serverTransportMapping.getListenAddress();
        DefaultTcpTransportMapping clientTransportMapping = new DefaultTcpTransportMapping();
        clientTransportMapping.listen();
        TransportStateReference transportStateReference =
                new TransportStateReference(clientTransportMapping, null, null,
                        null, null, false, new Object());
        PDU v2cPDU = new PDU();
        v2cPDU.add(new VariableBinding(new OID(SnmpConstants.sysDescr), new OctetString("hello World")));
        BEROutputStream berOutputStream = new BEROutputStream(ByteBuffer.allocate(v2cPDU.getBERLength()));
        v2cPDU.encodeBER(berOutputStream);
        byte[] bytes2Send = berOutputStream.getBuffer().array();
        synchronized (sync) {
            clientTransportMapping.sendMessage(serverAddress, bytes2Send, transportStateReference);
            sync.wait(2000);
        }
        assertEquals(1, bytesReceivedList.size());
        assertEquals(new OctetString(bytes2Send).toHexString(), bytesReceivedList.get(0).toHexString());
        clientTransportMapping.close();
        serverTransportMapping.close();
    }

    public void testSendMessageAfterReconnect() throws Exception {
        DefaultTcpTransportMapping serverTransportMapping = new DefaultTcpTransportMapping();
        final List<OctetString> bytesReceivedList = new ArrayList<OctetString>();
        final Object sync = new Object();
        final Object sync2 = new Object();
        serverTransportMapping.addTransportListener(new TransportListener() {
            @Override
            public void processMessage(TransportMapping sourceTransport, Address incomingAddress,
                                       ByteBuffer wholeMessage, TransportStateReference tmStateReference) {
                OctetString bytesReceived = new OctetString(wholeMessage.array());
                System.out.println("Received from "+incomingAddress+": "+bytesReceived.toHexString());
                bytesReceivedList.add(bytesReceived);
                synchronized (tmStateReference.getSessionID()) {
                    tmStateReference.getSessionID().notify();
                }
            }
        });
        serverTransportMapping.setServerEnabled(true);
        serverTransportMapping.listen();

        TcpAddress serverAddress = serverTransportMapping.getListenAddress();
        DefaultTcpTransportMapping clientTransportMapping = new DefaultTcpTransportMapping();
        clientTransportMapping.listen();
        TransportStateReference transportStateReference =
                new TransportStateReference(clientTransportMapping, null, null,
                        null, null, false, sync);
        PDU v2cPDU = new PDU();
        v2cPDU.add(new VariableBinding(new OID(SnmpConstants.sysDescr), new OctetString("hello World")));
        BEROutputStream berOutputStream = new BEROutputStream(ByteBuffer.allocate(v2cPDU.getBERLength()));
        v2cPDU.encodeBER(berOutputStream);
        byte[] bytes2Send = berOutputStream.getBuffer().array();
        synchronized (sync) {
            clientTransportMapping.sendMessage(serverAddress, bytes2Send, transportStateReference);
            sync.wait(2000);
        }
        assertEquals(1, bytesReceivedList.size());
        assertEquals(new OctetString(bytes2Send).toHexString(), bytesReceivedList.get(0).toHexString());
        serverTransportMapping.close();

        // Second message
        bytesReceivedList.clear();
        serverTransportMapping.tcpAddress.setPort(serverAddress.getPort());
        serverTransportMapping.listen();
        v2cPDU.add(new VariableBinding(SnmpConstants.sysUpTime, new Integer32(1234)));
        berOutputStream = new BEROutputStream(ByteBuffer.allocate(v2cPDU.getBERLength()));
        v2cPDU.encodeBER(berOutputStream);
        bytes2Send = berOutputStream.getBuffer().array();
        transportStateReference =
                new TransportStateReference(clientTransportMapping, null, null,
                        null, null, false, sync2);
        synchronized (sync2) {
            clientTransportMapping.sendMessage(serverAddress, bytes2Send, transportStateReference);
            sync2.wait(2000);
        }
        assertEquals(1, bytesReceivedList.size());
        assertEquals(new OctetString(bytes2Send).toHexString(), bytesReceivedList.get(0).toHexString());
        clientTransportMapping.close();
        serverTransportMapping.close();
    }

    public void testSendMessageAfterDisconnect() throws Exception {
        DefaultTcpTransportMapping serverTransportMapping = new DefaultTcpTransportMapping();
        final List<OctetString> bytesReceivedList = new ArrayList<>();
        final Object sync = new Object();
        final Object sync2 = new Object();
        final Object sync3 = new Object();
        serverTransportMapping.addTransportListener(new TransportListener() {
            boolean second = false;
            public void processMessage(TransportMapping sourceTransport,
                                       Address incomingAddress, ByteBuffer wholeMessage,
                                       TransportStateReference tmStateReference) {
                OctetString bytesReceived = new OctetString(wholeMessage.array());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Received from " + incomingAddress + ": " + bytesReceived.toHexString() +
                            " with session = " + tmStateReference.getSessionID());
                }
                bytesReceivedList.add(bytesReceived);
                synchronized (sync2) {
                    if (second) {
                        sync2.notify();
                    }
                    else {
                        second = true;
                    }
                }
                synchronized (sync) {
                    sync.notify();
                }
            }
        });
        serverTransportMapping.setServerEnabled(true);
        serverTransportMapping.listen();

        final ArrayList<Object> droppedMessage = new ArrayList<>();
        TcpAddress serverAddress = serverTransportMapping.getListenAddress();
        DefaultTcpTransportMapping clientTransportMapping = new DefaultTcpTransportMapping() {
            @Override
            protected void handleDroppedMessageToSend(TcpAddress address, byte[] message,
                                                      TransportStateReference transportStateReference) {
                droppedMessage.add(message);
                synchronized (sync3) {
                    sync3.notify();
                }
            }
        };
        clientTransportMapping.listen();
        TransportStateReference transportStateReference =
                new TransportStateReference(clientTransportMapping, null, null,
                        null, null, false, sync);
        PDU v2cPDU = new PDU();
        v2cPDU.add(new VariableBinding(new OID(SnmpConstants.sysDescr), new OctetString("hello World")));
        BEROutputStream berOutputStream = new BEROutputStream(ByteBuffer.allocate(v2cPDU.getBERLength()));
        v2cPDU.encodeBER(berOutputStream);
        byte[] bytes2Send = berOutputStream.getBuffer().array();
        synchronized (sync) {
            clientTransportMapping.sendMessage(serverAddress, bytes2Send, transportStateReference);
            sync.wait(2000);
        }
        assertEquals(1, bytesReceivedList.size());
        assertEquals(new OctetString(bytes2Send).toHexString(), bytesReceivedList.get(0).toHexString());
        clientTransportMapping.close(serverAddress);
        assertEquals(0, clientTransportMapping.sockets.size());

        // Second message
        bytesReceivedList.clear();
        v2cPDU.add(new VariableBinding(SnmpConstants.sysUpTime, new Integer32(1234)));
        berOutputStream = new BEROutputStream(ByteBuffer.allocate(v2cPDU.getBERLength()));
        v2cPDU.encodeBER(berOutputStream);
        bytes2Send = berOutputStream.getBuffer().array();
        transportStateReference =
                new TransportStateReference(clientTransportMapping, null, null,
                        null, null, false, sync2);
        synchronized (sync2) {
            clientTransportMapping.sendMessage(serverAddress, bytes2Send, transportStateReference);
            sync2.wait(2000);
        }
        assertEquals(1, bytesReceivedList.size());
        assertEquals(new OctetString(bytes2Send).toHexString(), bytesReceivedList.get(0).toHexString());
        //clientTransportMapping.setOpenSocketOnSending(false);
        clientTransportMapping.suspendAddress(serverAddress);
        clientTransportMapping.close(serverAddress);
        assertEquals(0, clientTransportMapping.sockets.size());

        // Third message
        bytesReceivedList.clear();
        v2cPDU.add(new VariableBinding(SnmpConstants.sysUpTime, new Integer32(1234)));
        berOutputStream = new BEROutputStream(ByteBuffer.allocate(v2cPDU.getBERLength()));
        v2cPDU.encodeBER(berOutputStream);
        bytes2Send = berOutputStream.getBuffer().array();
        transportStateReference =
                new TransportStateReference(clientTransportMapping, null, null,
                        null, null, false, sync3);
        synchronized (sync3) {
            clientTransportMapping.sendMessage(serverAddress, bytes2Send, transportStateReference);
            sync3.wait(2000);
        }
        // no message sent:
        assertEquals(0, bytesReceivedList.size());
        // message dropped:
        assertEquals(1, droppedMessage.size());
        clientTransportMapping.resumeAddress(serverAddress);
        assertEquals(0, clientTransportMapping.suspendedAddresses.size());

        // Fourth message
        v2cPDU.add(new VariableBinding(SnmpConstants.sysUpTime, new Integer32(1234)));
        berOutputStream = new BEROutputStream(ByteBuffer.allocate(v2cPDU.getBERLength()));
        v2cPDU.encodeBER(berOutputStream);
        bytes2Send = berOutputStream.getBuffer().array();
        transportStateReference =
                new TransportStateReference(clientTransportMapping, null, null,
                        null, null, false, sync3);
        synchronized (sync2) {
            clientTransportMapping.sendMessage(serverAddress, bytes2Send, transportStateReference);
            sync2.wait(2000);
        }
        assertEquals(1, bytesReceivedList.size());
        assertEquals(new OctetString(bytes2Send).toHexString(), bytesReceivedList.get(0).toHexString());

        serverTransportMapping.close();
        assertNull(serverTransportMapping.server);
        assertEquals(0, serverTransportMapping.sockets.size());
    }

    public void testReconnectUnderLoad() throws Exception {

        TcpAddress serverAddress = new TcpAddress();
        DefaultTcpTransportMapping serverTransportMapping = new DefaultTcpTransportMapping(serverAddress);
        final List<OctetString> bytesReceivedList = Collections.synchronizedList(new ArrayList<>());
        final List<OctetString> bytesSentList = Collections.synchronizedList(new ArrayList<>());
        serverTransportMapping.addTransportListener(new TransportListener() {
            public void processMessage(TransportMapping sourceTransport,
                                       Address incomingAddress, ByteBuffer wholeMessage,
                                       TransportStateReference tmStateReference) {
                OctetString bytesReceived = new OctetString(wholeMessage.array());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Received from " + incomingAddress + ": " + bytesReceived.toHexString() +
                            " with session = " + tmStateReference.getSessionID());
                }
                bytesReceivedList.add(bytesReceived);
            }
        });
        serverTransportMapping.setServerEnabled(true);
        serverTransportMapping.listen();

        // Pin server port for reconnects
        TcpAddress serverAddressOS = serverTransportMapping.getListenAddress();
        serverAddress.setPort(serverAddressOS.getPort());

        int numTransportMappings = 1;
        final Object[] sessionIDs = new Object[numTransportMappings];
        final DefaultTcpTransportMapping[] clientTransportMappings =
                new DefaultTcpTransportMapping[numTransportMappings];
        final TransportStateReference[] transportStateReferences = new TransportStateReference[numTransportMappings];
        for (int i=0; i<clientTransportMappings.length; i++) {
            clientTransportMappings[i] = new DefaultTcpTransportMapping();
            clientTransportMappings[i].listen();
            transportStateReferences[i] =
                    new TransportStateReference(clientTransportMappings[i], null, null,
                            null, null, false, sessionIDs[i]);
        }
        ThreadPool clientPool = ThreadPool.create("TCPclientTM", numTransportMappings);
        for (TransportStateReference tmRef : transportStateReferences) {
            TransportMapping<TcpAddress> clientTransport = (TransportMapping<TcpAddress>) tmRef.getTransport();
            clientPool.execute(new WorkerTask() {
                boolean stop = false;
                @Override
                public void terminate() {
                    stop = true;
                }

                @Override
                public void join() throws InterruptedException {

                }

                @Override
                public void interrupt() {

                }

                @Override
                public void run() {
                    for (int i = 0; (!stop) && i< MAX_MESSAGE_FOR_LOAD_TEST; i++) {
                        try {
                            PDU v2cPDU = new PDU();
                            v2cPDU.add(new VariableBinding(new OID(SnmpConstants.sysDescr), new OctetString("hello World"+i)));
                            BEROutputStream berOutputStream = new BEROutputStream(ByteBuffer.allocate(v2cPDU.getBERLength()));
                            v2cPDU.encodeBER(berOutputStream);
                            byte[] bytes2Send = berOutputStream.getBuffer().array();
                            bytesSentList.add(OctetString.fromByteArray(bytes2Send));
                            clientTransport.sendMessage(serverAddress, bytes2Send, tmRef);
                            Thread.sleep(7);
                        } catch (IOException | InterruptedException iox) {
                            iox.printStackTrace();
                        }
                    }
                }
            });
        }
        Thread.sleep(500);
        serverTransportMapping.close();
        assertEquals(0, serverTransportMapping.sockets.size());
        serverTransportMapping.listen();
        assertNotNull(serverTransportMapping.serverThread);
        Thread.sleep(500);
        clientPool.stop();
        for (DefaultTcpTransportMapping tcpTransportMapping : clientTransportMappings) {
            assertEquals(1, tcpTransportMapping.sockets.size());
        }
        assertTrue(bytesSentList.size() - bytesReceivedList.size() <= 1);
        int count = bytesReceivedList.size();
        assertEquals(bytesSentList.subList(0, count), bytesReceivedList.subList(0, count));
    }

}
