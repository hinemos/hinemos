/*_############################################################################
  _## 
  _##  SNMP4J - MultiThreadedTrapReceiver.java  
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

package org.snmp4j.test;

import org.snmp4j.Snmp;
import org.snmp4j.smi.Address;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.TransportMapping;
import org.snmp4j.smi.OctetString;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.mp.MPv3;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.security.USM;
import org.snmp4j.smi.GenericAddress;
import java.net.UnknownHostException;
import java.io.IOException;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;

public class MultiThreadedTrapReceiver implements CommandResponder {

  // initialize Log4J logging
/*
  static {
    LogFactory.setLogFactory(new Log4jLogFactory());
    BER.setCheckSequenceLength(false);
  }
*/
  private MultiThreadedMessageDispatcher dispatcher;
  private Snmp snmp = null;
  private Address listenAddress;
  private ThreadPool threadPool;

  private int n = 0;
  private long start = -1;


  public MultiThreadedTrapReceiver() {
//    BasicConfigurator.configure();
  }

  private void init() throws IOException {
    threadPool = ThreadPool.create("Trap", 2);
    dispatcher =
        new MultiThreadedMessageDispatcher(threadPool,
                                           new MessageDispatcherImpl());
    listenAddress =
        GenericAddress.parse(System.getProperty("snmp4j.listenAddress",
                                                "udp:0.0.0.0/162"));
    TransportMapping<? extends Address> transport;
    if (listenAddress instanceof UdpAddress) {
      transport = new DefaultUdpTransportMapping((UdpAddress)listenAddress);
    }
    else {
      transport = new DefaultTcpTransportMapping((TcpAddress)listenAddress);
    }
    snmp = new Snmp(dispatcher, transport);
    snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
    snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
    snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3());
    USM usm = new USM(SecurityProtocols.getInstance(),
                      new OctetString(MPv3.createLocalEngineID()), 0);
    SecurityModels.getInstance().addSecurityModel(usm);
    snmp.listen();
  }

  public void run() {
    try {
      init();
      snmp.addCommandResponder(this);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public static void main(String[] args) {
    MultiThreadedTrapReceiver multithreadedtrapreceiver = new
        MultiThreadedTrapReceiver();
    multithreadedtrapreceiver.run();
  }

  public void processPdu(CommandResponderEvent event) {
    if (start < 0) {
      start = System.currentTimeMillis()-1;
    }
//    System.out.println(event.toString());
    n++;
    if ((n % 100 == 1)) {
      System.out.println("Processed " +
                         (n / (double)(System.currentTimeMillis() - start)) * 1000 +
                         "/s, total="+n);
    }
  }
}
