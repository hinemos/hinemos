/*_############################################################################
  _## 
  _##  SNMP4J - DefaultSshTransportMapping.java  
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
import org.snmp4j.event.CounterEvent;
import org.snmp4j.log.LogAdapter;
import org.snmp4j.log.LogFactory;
import org.snmp4j.mp.CounterSupport;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.SshAddress;
import org.snmp4j.transport.ssh.SshSession;
import org.snmp4j.transport.ssh.SshTransportAdapter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The <code>DefaultSshTransportMapping</code> implements a TCP transport
 * mapping with TLS according to RFC 5592 (named SSHTM therein).
 *
 * @author Frank Fock
 * @version 2.0
 * @since 2.0
 */
public class DefaultSshTransportMapping extends AbstractTransportMapping<SshAddress> {

  private static final LogAdapter logger =
      LogFactory.getLogger(DefaultSshTransportMapping.class);

  private final Map<SessionID, SshSession> sessions = new HashMap<SessionID, SshSession>();
  private SshTransportAdapter transportAdapter;
  private CounterSupport counterSupport;

  public DefaultSshTransportMapping(SshTransportAdapter transportAdapter) {
    this.transportAdapter = transportAdapter;
    this.counterSupport = CounterSupport.getInstance();
  }

  @Override
  public Class<? extends Address> getSupportedAddressClass() {
    return SshAddress.class;
  }

  @Override
  public SshAddress getListenAddress() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void sendMessage(SshAddress address, byte[] message,
                          TransportStateReference tmStateReference) throws IOException {
    SessionID sessionKey = new SessionID(tmStateReference.getSecurityName(), address);
    SshSession session = null;
    synchronized (sessions) {
      session = sessions.get(sessionKey);
    }
    if (session == null) {
      if (tmStateReference.isSameSecurity()) {
        logger.warn("Cannot (re)open session because tmStateReference requires 'sameSecurity'");
        throw new IOException("Session '"+address+"' for '"+tmStateReference.getSecurityName()+
                              "' closed/unavailable");
      }
      session =  openSession(address, tmStateReference, maxInboundMessageSize);
    }
  }

  protected SshSession openSession(SshAddress address,
                                   TransportStateReference tmStateReference,
                                   int maxMessageSize) {
    // RFC 5592 §5.3:
    // 1. Increment snmpSshtmSessionOpens counter.
    fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpSshtmSessionOpens));
    // 2.
    return transportAdapter.openClientSession(tmStateReference, maxMessageSize);
  }

  @Override
  public void close() throws IOException {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void listen() throws IOException {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean isListening() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  protected void fireIncrementCounter(CounterEvent e) {
    counterSupport.fireIncrementCounter(e);
  }

  protected class SessionID {
    private OctetString tmSecurityName;
    private SshAddress address;

    public SessionID(OctetString tmSecurityName, SshAddress address) {
      this.tmSecurityName = tmSecurityName;
      this.address = address;
    }

    @Override
    public String toString() {
      return "DefaultSshTransportMapping.SessionID[" +
          "tmSecurityName=" + tmSecurityName +
          ", address=" + address +
          ']';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      SessionID sessionID = (SessionID) o;

      if (address != null ? !address.equals(sessionID.address) : sessionID.address != null) return false;
      return !(tmSecurityName != null ?
          !tmSecurityName.equals(sessionID.tmSecurityName) :
          sessionID.tmSecurityName != null);
    }

    @Override
    public int hashCode() {
      int result = tmSecurityName != null ? tmSecurityName.hashCode() : 0;
      result = 31 * result + (address != null ? address.hashCode() : 0);
      return result;
    }
  }
}
