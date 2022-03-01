/*_############################################################################
  _## 
  _##  SNMP4J - TLSTMTest.java  
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.log.ConsoleLogAdapter;
import org.snmp4j.log.ConsoleLogFactory;
import org.snmp4j.log.LogFactory;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.TSM;
import org.snmp4j.smi.*;
import org.snmp4j.transport.tls.DefaultTlsTmSecurityCallback;
import org.snmp4j.transport.tls.SecurityNameMapping;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test TLSTM with real networking.
 * @author Frank Fock
 */
public class TLSTMTest {

    static {
        LogFactory.setLogFactory(new ConsoleLogFactory());
        ConsoleLogAdapter.setWarnEnabled(true);
    }

    private static final int TIMEOUT = 5000;

    private TLSTM tlstmCR;
    private TLSTM tlstmCS;
    private static final String SEC_NAME = "localhost";
    private static final OctetString SERVER_FINGER_PRINT =
            OctetString.fromHexString("4a:48:60:20:35:10:97:92:de:62:79:ae:85:b9:49:65:e9:03:6d:5a:f8:f3:70:41:9d:db:50:5a:76:3c:de:b5");
    private static final OctetString CLIENT_FINGER_PRINT = new OctetString("2");
    private static final byte[] MESSAGE = new byte[] { 0,1,2,3,4,5,6,7,8,9,10 };
    private static final byte[] MESSAGE_SCOPED_PDU =
            OctetString.fromHexString("02 01 00 02 01"
                    +" 00 30 46 30 17 06 0F 2B 06 01 04 01 A6 70 0A 01 01 04 01 01 02 00 42 04 05 75 8A 24 30 2B 06 09"
                    +" 2B 06 01 06 03 12 01 04 00 04 1E 70 65 57 61 64 66 6E 61 67 6E 65 72 67 72 44 46 41 41 48 41 72"
                    +" C3 A4 C3 B6 C3 9F 33 39 34", ' ').getValue();

    @Before
    public void setUp() throws Exception {

        tlstmCS = new TLSTM();
        tlstmCR = new TLSTM(new TlsAddress("127.0.0.1/0"));
        tlstmCR.setServerEnabled(true);
        URL keystoreUrl = getClass().getResource("dtls-cert.ks");
        String password = "snmp4j";
        tlstmCS.setKeyStore(keystoreUrl.getFile());
        tlstmCS.setKeyStorePassword(password);
        tlstmCR.setKeyStore(keystoreUrl.getFile());
        tlstmCR.setKeyStorePassword(password);
        tlstmCS.setTrustStore(keystoreUrl.getFile());
        tlstmCS.setTrustStorePassword(password);
        tlstmCR.setTrustStore(keystoreUrl.getFile());
        tlstmCR.setTrustStorePassword(password);

        tlstmCR.setTlsProtocols(new String[]{"TLSv1.2"});
        tlstmCS.setTlsProtocols(new String[]{"TLSv1.2"});
    }

    @After
    public void tearDown() throws Exception {
         tlstmCR.close();
         tlstmCS.close();
    }

    @Test
    public void sendMessage() throws Exception {
        final boolean[] messageReceived = { false };
        CertifiedTarget certifiedTarget = new CertifiedTarget(
                new TlsAddress(tlstmCR.getListenAddress().getInetAddress(), tlstmCR.getListenAddress().getPort()),
                new OctetString(SEC_NAME), SERVER_FINGER_PRINT, CLIENT_FINGER_PRINT);
        TransportStateReference tmStateReference =
                new TransportStateReference(tlstmCS,
                        tlstmCR.getListenAddress(),
                        new OctetString(SEC_NAME),
                        SecurityLevel.authPriv,
                        SecurityLevel.undefined,
                        false, null, certifiedTarget);
        final TransportListener transportListener = new TransportListener() {
            @Override
            public synchronized void processMessage(TransportMapping sourceTransport, Address incomingAddress,
                                                    ByteBuffer wholeMessage, TransportStateReference tmStateReference) {
                byte[] message = new byte[wholeMessage.limit()];
                System.arraycopy(wholeMessage.array(), 0, message, 0, message.length);
                assertArrayEquals(MESSAGE, message);
                messageReceived[0] = true;
                notify();
            }
        };
        tlstmCR.addTransportListener(transportListener);
        tlstmCR.listen();
        synchronized (transportListener) {
            tlstmCS.sendMessage(tlstmCR.getListenAddress(), MESSAGE, tmStateReference);
            transportListener.wait(3200);
        }
        assertTrue(messageReceived[0]);
    }

    @Test
    public void sendMessageWithPDU() throws Exception {
        tlstmCR.listen();
        final boolean[] messageReceived = { false };
        CertifiedTarget certifiedTarget = new CertifiedTarget(
                new TlsAddress(tlstmCR.getListenAddress().getInetAddress(), tlstmCR.getListenAddress().getPort()),
                new OctetString(SEC_NAME), SERVER_FINGER_PRINT, CLIENT_FINGER_PRINT);
        certifiedTarget.setTimeout(100);
        certifiedTarget.setRetries(0);
        certifiedTarget.setSecurityModel(4);
        final CommandResponder commandResponder = new CommandResponder() {

            @Override
            public synchronized void processPdu(CommandResponderEvent event) {
                messageReceived[0] = true;
                notify();
                event.setProcessed(true);
            }

        };
        DefaultTlsTmSecurityCallback securityCallback = new DefaultTlsTmSecurityCallback();
        securityCallback.addAcceptedIssuerDN("CN=www.snmp4j.org, OU=Unit-Test, O=AGENTPP, L=Stuttgart, ST=Baden-Wuerttemberg, C=DE");
        securityCallback.addSecurityNameMapping(
                OctetString.fromHexString("4a:48:60:20:35:10:97:92:de:62:79:ae:85:b9:49:65:e9:03:6d:5a:f8:f3:70:41:9d:db:50:5a:76:3c:de:b5"),
                SecurityNameMapping.CertMappingType.SANIpAddress,
                new OctetString("127.0.0.1"), new OctetString("localhost"));
        tlstmCR.setSecurityCallback(securityCallback);
        Snmp snmpAgent = new Snmp(tlstmCR);
        snmpAgent.addCommandResponder(commandResponder);
        ScopedPDU scopedPDU = new ScopedPDU();
        UnsignedInteger32 value1 = new UnsignedInteger32(91589156l);
        OctetString value2 = new OctetString("peWadfnagnergrDFAAHAräöß394");
        scopedPDU.add(new VariableBinding(SnmpConstants.snmp4jStatsRequestRetries, value1));
        scopedPDU.add(new VariableBinding(SnmpConstants.snmpTrapCommunity, value2));
        OctetString localEngineID = new OctetString(MPv3.createLocalEngineID());
        Snmp snmp = new Snmp(tlstmCS);
        ((MPv3)snmp.getMessageDispatcher().getMessageProcessingModel(MPv3.ID)).setSecurityModels(
                new SecurityModels().addSecurityModel(new TSM()));
        ((MPv3)snmpAgent.getMessageDispatcher().getMessageProcessingModel(MPv3.ID)).setSecurityModels(
                new SecurityModels().addSecurityModel(new TSM(localEngineID, false)));
        scopedPDU.setContextEngineID(localEngineID);
        snmp.listen();
        snmpAgent.setLocalEngine(localEngineID.getValue(), 1, 1);
        snmpAgent.listen();
        synchronized (commandResponder) {
            snmp.send(scopedPDU, certifiedTarget);
            commandResponder.wait(2000);
        }
        assertTrue(messageReceived[0]);
        snmp.close();
        snmpAgent.close();
    }

    @Test
    public void sendMessageWithPDUVeryLong() throws Exception {
        tlstmCS.setMaxInboundMessageSize(65535);
        tlstmCR.setMaxInboundMessageSize(65535);
        tlstmCR.listen();
        final boolean[] messageReceived = { false };
        CertifiedTarget certifiedTarget = new CertifiedTarget(
                new TlsAddress(tlstmCR.getListenAddress().getInetAddress(), tlstmCR.getListenAddress().getPort()),
                new OctetString(SEC_NAME), SERVER_FINGER_PRINT, CLIENT_FINGER_PRINT);
        certifiedTarget.setTimeout(2000);
        certifiedTarget.setRetries(0);
        certifiedTarget.setSecurityModel(4);
        final CommandResponder commandResponder = new CommandResponder() {

            @Override
            public synchronized void processPdu(CommandResponderEvent event) {
                messageReceived[0] = true;
                notify();
                event.setProcessed(true);
            }

        };
        DefaultTlsTmSecurityCallback securityCallback = new DefaultTlsTmSecurityCallback();
        securityCallback.addAcceptedIssuerDN("CN=www.snmp4j.org, OU=Unit-Test, O=AGENTPP, L=Stuttgart, ST=Baden-Wuerttemberg, C=DE");
        securityCallback.addSecurityNameMapping(
                OctetString.fromHexString("4a:48:60:20:35:10:97:92:de:62:79:ae:85:b9:49:65:e9:03:6d:5a:f8:f3:70:41:9d:db:50:5a:76:3c:de:b5"),
                SecurityNameMapping.CertMappingType.SANIpAddress,
                new OctetString("127.0.0.1"), new OctetString("localhost"));
        tlstmCR.setSecurityCallback(securityCallback);
        Snmp snmpAgent = new Snmp(tlstmCR);
        snmpAgent.addCommandResponder(commandResponder);
        ScopedPDU scopedPDU = new ScopedPDU();
        UnsignedInteger32 value1 = new UnsignedInteger32(91589156l);
        OctetString value2 = new OctetString("peWadfnagnergrDFAAHAräöß394");
        scopedPDU.add(new VariableBinding(SnmpConstants.snmp4jStatsRequestRetries, value1));
        scopedPDU.add(new VariableBinding(SnmpConstants.snmpTrapCommunity, value2));
        scopedPDU.add(new VariableBinding(SnmpConstants.sysDescr, new OctetString(ByteBuffer.allocate(40000).array())));
        OctetString localEngineID = new OctetString(MPv3.createLocalEngineID());
        Snmp snmp = new Snmp(tlstmCS);
        ((MPv3)snmp.getMessageDispatcher().getMessageProcessingModel(MPv3.ID)).setSecurityModels(
                new SecurityModels().addSecurityModel(new TSM()));
        ((MPv3)snmpAgent.getMessageDispatcher().getMessageProcessingModel(MPv3.ID)).setSecurityModels(
                new SecurityModels().addSecurityModel(new TSM(localEngineID, false)));
        scopedPDU.setContextEngineID(localEngineID);
        snmp.listen();
        snmpAgent.setLocalEngine(localEngineID.getValue(), 1, 1);
        snmpAgent.listen();
        synchronized (commandResponder) {
            snmp.send(scopedPDU, certifiedTarget);
            commandResponder.wait(2000);
        }
        assertTrue(messageReceived[0]);
        snmp.close();
        snmpAgent.close();
    }

    @Test
    public void sendMessageWithBufferUnderflow() throws Exception {
        TLSTM tlstmCS = new TLSTM() {
            @Override
            void writeNetBuffer(SocketEntry entry, SocketChannel sc) throws IOException {
                entry.getOutNetBuffer().flip();
                // Send SSL/TLS encoded data to peer
                ByteBuffer outNet = entry.getOutNetBuffer().slice();
                while (outNet.hasRemaining()) {
                    for (int start = outNet.position(), end = outNet.limit(),
                        packetLength = 500; start < end; start = outNet.limit()) {
                        int num = sc.write((ByteBuffer)outNet.position(start).limit(start + Math.min(end - start, packetLength)));
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (num == -1) {
                            throw new IOException("TLS connection closed");
                        }
                    }
                }
                entry.getOutNetBuffer().clear();
            }
        };
        tlstmCS.setServerEnabled(false);
        URL keystoreUrl = getClass().getResource("dtls-cert.ks");
        String password = "snmp4j";
        tlstmCS.setKeyStore(keystoreUrl.getFile());
        tlstmCS.setKeyStorePassword(password);
        tlstmCS.setTrustStore(keystoreUrl.getFile());
        tlstmCS.setTrustStorePassword(password);
        tlstmCS.setTlsProtocols(new String[]{"TLSv1.2"});
        tlstmCS.setMaxInboundMessageSize(32768);
        tlstmCR.setMaxInboundMessageSize(32768);
        tlstmCR.listen();
        final boolean[] messageReceived = { false };
        CertifiedTarget certifiedTarget = new CertifiedTarget(
                new TlsAddress(tlstmCR.getListenAddress().getInetAddress(), tlstmCR.getListenAddress().getPort()),
                new OctetString(SEC_NAME), SERVER_FINGER_PRINT, CLIENT_FINGER_PRINT);
        certifiedTarget.setTimeout(10000);
        certifiedTarget.setRetries(0);
        certifiedTarget.setSecurityModel(4);
        final CommandResponder commandResponder = new CommandResponder() {

            @Override
            public synchronized void processPdu(CommandResponderEvent event) {
                messageReceived[0] = true;
                notify();
                event.setProcessed(true);
            }

        };
        DefaultTlsTmSecurityCallback securityCallback = new DefaultTlsTmSecurityCallback();
        securityCallback.addAcceptedIssuerDN("CN=www.snmp4j.org, OU=Unit-Test, O=AGENTPP, L=Stuttgart, ST=Baden-Wuerttemberg, C=DE");
        securityCallback.addSecurityNameMapping(
                OctetString.fromHexString("4a:48:60:20:35:10:97:92:de:62:79:ae:85:b9:49:65:e9:03:6d:5a:f8:f3:70:41:9d:db:50:5a:76:3c:de:b5"),
                SecurityNameMapping.CertMappingType.SANIpAddress,
                new OctetString("127.0.0.1"), new OctetString("localhost"));
        tlstmCR.setSecurityCallback(securityCallback);
        Snmp snmpAgent = new Snmp(tlstmCR);
        snmpAgent.addCommandResponder(commandResponder);
        ScopedPDU scopedPDU = new ScopedPDU();
        //scopedPDU.setType(PDU.INFORM);
        UnsignedInteger32 value1 = new UnsignedInteger32(91589156l);
        OctetString value2 = new OctetString("peWadfnagnergrDFAAHAräöß394");
        scopedPDU.add(new VariableBinding(SnmpConstants.snmp4jStatsRequestRetries, value1));
        scopedPDU.add(new VariableBinding(SnmpConstants.snmpTrapCommunity, value2));
        scopedPDU.add(new VariableBinding(SnmpConstants.sysDescr, new OctetString(ByteBuffer.allocate(1000).array())));
        OctetString localEngineID = new OctetString(MPv3.createLocalEngineID());
        Snmp snmp = new Snmp(tlstmCS);
        ((MPv3)snmp.getMessageDispatcher().getMessageProcessingModel(MPv3.ID)).setSecurityModels(
                new SecurityModels().addSecurityModel(new TSM()));
        ((MPv3)snmpAgent.getMessageDispatcher().getMessageProcessingModel(MPv3.ID)).setSecurityModels(
                new SecurityModels().addSecurityModel(new TSM(localEngineID, false)));
        scopedPDU.setContextEngineID(localEngineID);
        snmp.listen();
        snmpAgent.setLocalEngine(localEngineID.getValue(), 1, 1);
        snmpAgent.listen();
        final ResponseListener responseListener = new ResponseListener() {
            @Override
            public synchronized void onResponse(ResponseEvent event) {
            }
        };
        synchronized (commandResponder) {
            snmp.send(scopedPDU, certifiedTarget, null, responseListener);
            commandResponder.wait(5000);
        }
        assertTrue("PDU not received by command responder", messageReceived[0]);
        snmp.close();
        snmpAgent.close();
    }

    @Test
    public void sendNotifyV3TLSTM() throws Exception {
        DefaultTlsTmSecurityCallback securityCallback = new DefaultTlsTmSecurityCallback();
        securityCallback.addAcceptedIssuerDN("CN=www.snmp4j.org, OU=Unit-Test, O=AGENTPP, L=Stuttgart, ST=Baden-Wuerttemberg, C=DE");
        securityCallback.addSecurityNameMapping(
                OctetString.fromHexString("4a:48:60:20:35:10:97:92:de:62:79:ae:85:b9:49:65:e9:03:6d:5a:f8:f3:70:41:9d:db:50:5a:76:3c:de:b5"),
                SecurityNameMapping.CertMappingType.SANIpAddress,
                new OctetString("127.0.0.1"), new OctetString("localhost"));
        tlstmCR.setSecurityCallback(securityCallback);
        SecurityModels.getInstance().addSecurityModel(new TSM());
        Snmp snmpCommandResponder = new Snmp(tlstmCR);
        snmpCommandResponder.listen();
        CertifiedTarget target = new CertifiedTarget(tlstmCR.getListenAddress(),
                new OctetString(SEC_NAME), SERVER_FINGER_PRINT, CLIENT_FINGER_PRINT);
        target.setTimeout(TIMEOUT);
        target.setVersion(SnmpConstants.version3);
        target.setSecurityModel(4);
        ScopedPDU pdu = new ScopedPDU();
        pdu.setType(PDU.NOTIFICATION);
        pdu.setContextName(new OctetString("myContext"));
        SnmpTest.addTestVariableBindings(pdu, false, false, target.getVersion());
        Snmp snmp = new Snmp(tlstmCS);
        pdu.setRequestID(new Integer32(snmp.getNextRequestID()));
        unconfirmedTest(snmpCommandResponder, snmp, tlstmCS, target, pdu);
        snmp.close();
    }

    private void unconfirmedTest(Snmp snmpCommandResponder, Snmp snmpCommandGenerator,
                                 TransportMapping transportMappingCG, Target target, PDU pdu) throws IOException {
        Map<Integer, SnmpTest.RequestResponse> queue = new HashMap<Integer, SnmpTest.RequestResponse>(2);
        queue.put(pdu.getRequestID().getValue(), new SnmpTest.RequestResponse(pdu, null));
        SnmpTest.TestCommandResponder responder = new SnmpTest.TestCommandResponder(snmpCommandResponder, queue);
        snmpCommandResponder.addCommandResponder(responder);
        ResponseEvent resp = snmpCommandGenerator.send(pdu, target, transportMappingCG);
        assertNull(resp);
        try {
            for (int i=0; i<100 && !queue.isEmpty(); i++) {
                Thread.sleep(50);
            }
        } catch (InterruptedException iex) {
            // ignore
        }
        snmpCommandResponder.removeCommandResponder(responder);
        assertTrue(queue.isEmpty());
    }

}
