/*_############################################################################
  _## 
  _##  SNMP4J - TlsAddress.java  
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

import org.snmp4j.log.LogAdapter;
import org.snmp4j.log.LogFactory;

import java.net.InetAddress;

/**
 * The <code>SshAddress</code> represents a SSH transport addresses as defined
 * by RFC 5953 SnmpTSLAddress textual convention.
 *
 * @author Frank Fock
 * @version 2.0
 * @since 2.0
 */
public class TlsAddress extends TcpAddress {

  static final long serialVersionUID = 0L;

  private static final LogAdapter logger = LogFactory.getLogger(SshAddress.class);

  public TlsAddress() {
    super();
  }

  public TlsAddress(InetAddress inetAddress, int port) {
    super(inetAddress, port);
  }

  public TlsAddress(String address) {
    if (!parseAddress(address)) {
      throw new IllegalArgumentException(address);
    }
  }

  public static Address parse(String address) {
    try {
      TlsAddress a = new TlsAddress();
      if (a.parseAddress(address)) {
        return a;
      }
    }
    catch (Exception ex) {
      logger.error(ex);
    }
    return null;
  }

  public boolean equals(Object o) {
    return (o instanceof TlsAddress) && super.equals(o);
  }

}

