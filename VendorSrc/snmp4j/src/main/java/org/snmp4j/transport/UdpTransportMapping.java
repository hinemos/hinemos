/*_############################################################################
  _## 
  _##  SNMP4J - UdpTransportMapping.java  
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
import java.io.IOException;
import org.snmp4j.smi.UdpAddress;

/**
 * The <code>UdpTransportMapping</code> is the abstract base class for
 * UDP transport mappings.
 * @author Frank Fock
 * @version 1.0
 */

public abstract class UdpTransportMapping
    extends AbstractTransportMapping<UdpAddress> {

  protected UdpAddress udpAddress;

  public UdpTransportMapping(UdpAddress udpAddress) {
    this.udpAddress = udpAddress;
  }

  public Class<? extends Address> getSupportedAddressClass() {
    return UdpAddress.class;
  }

  /**
   * Returns the transport address that is configured for this transport mapping for
   * sending and receiving messages.
   * @return
   *    the <code>Address</code> used by this transport mapping. The returned
   *    instance must not be modified!
   */
  public UdpAddress getAddress() {
    return udpAddress;
  }

  public UdpAddress getListenAddress() {
    return udpAddress;
  }

  public abstract void listen() throws IOException;

  public abstract void close() throws IOException;

  public abstract void sendMessage(UdpAddress address, byte[] message,
                                   TransportStateReference tmStateReference)
      throws IOException;

}
