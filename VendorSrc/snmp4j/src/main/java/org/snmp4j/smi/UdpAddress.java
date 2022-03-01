/*_############################################################################
  _## 
  _##  SNMP4J - UdpAddress.java  
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
package org.snmp4j.smi;

import java.net.InetAddress;

/**
 * The <code>UdpAddress</code> represents UDP/IP transport addresses.
 * @author Frank Fock
 * @version 1.8.3
 */
public class UdpAddress extends TransportIpAddress {

  static final long serialVersionUID = -4390734262648716203L;

  public UdpAddress() {
  }

  public UdpAddress(InetAddress inetAddress, int port) {
    setInetAddress(inetAddress);
    setPort(port);
  }

  public UdpAddress(int port) {
    setPort(port);
  }

  public UdpAddress(String address) {
    if (!parseAddress(address)) {
      throw new IllegalArgumentException(address);
    }
  }

  public static Address parse(String address) {
    UdpAddress a = new UdpAddress();
    if (a.parseAddress(address)) {
      return a;
    }
    return null;
  }

  public boolean equals(Object o) {
    return (o instanceof UdpAddress) && super.equals(o);
  }

}

