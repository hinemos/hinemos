/*_############################################################################
  _## 
  _##  SNMP4J - TransportIpAddress.java  
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

import java.util.StringTokenizer;
import java.io.IOException;
import org.snmp4j.asn1.BERInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.snmp4j.log.LogFactory;
import org.snmp4j.log.LogAdapter;
import java.net.Inet6Address;
import java.lang.reflect.Method;

/**
 * The <code>TransportIpAddress</code> is the abstract base class for all
 * transport addresses on top of IP network addresses.
 *
 * @author Frank Fock
 * @version 1.5
 */
public abstract class TransportIpAddress extends IpAddress {

  private static final LogAdapter logger =
      LogFactory.getLogger(TransportIpAddress.class);

  static final long serialVersionUID = 695596530250216972L;

  protected int port = 0;

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    if ((port < 0) || (port > 65535)) {
      throw new IllegalArgumentException("Illegal port specified: " + port);
    }
    this.port = port;
  }

  public boolean isValid() {
    return super.isValid() && (port >= 0) && (port <= 65535);
  }

  public int compareTo(Variable o) {
    int result = super.compareTo(o);
    if (result == 0) {
      return (port - ((TransportIpAddress) o).getPort());
    }
    return result;
  }

  public boolean equals(Object o) {
    return (o instanceof TransportIpAddress) && (super.equals(o) && ((TransportIpAddress)o).getPort() == port);
  }

  public boolean parseAddress(String address) {
    try {
      StringTokenizer st = new StringTokenizer(address, "/");
      String addr = st.nextToken();
      String port = st.nextToken();
      if (super.parseAddress(addr)) {
        this.port = Integer.parseInt(port);
        return true;
      }
      return false;
    }
    catch (Exception ex) {
      return false;
    }
  }

  public static Address parse(String address) {
    UdpAddress a = new UdpAddress();
    if (a.parseAddress(address)) {
      return a;
    }
    return null;
  }

  public String toString() {
    return super.toString()+"/"+port;
  }

  public int hashCode() {
    return super.hashCode()^2 + port;
  }

  /**
   * Sets this transport address from an OcetString containing the address
   * value in format as specified by the TRANSPORT-ADDRESS-MIB.
   * @param transportAddress
   *    an OctetString containing the IP address bytes and the two port bytes
   *    in network byte order.
   * @throws UnknownHostException
   *    if the address is invalid.
   */
  public void setTransportAddress(OctetString transportAddress) throws
      UnknownHostException {
    OctetString inetAddr =
        transportAddress.substring(0, transportAddress.length()-2);
    byte[] addr = inetAddr.getValue();
    if ((addr.length == 8) || (addr.length == 20)) {
      // address with zone (scope) index
      byte[] ipaddr = new byte[addr.length-4];
      System.arraycopy(addr, 0, ipaddr, 0, ipaddr.length);
      int sz = ipaddr.length;
      int scope = ((addr[sz] << 24) +
                   ((addr[sz+1] & 0xff) << 16) +
                   ((addr[sz+2] & 0xFF) << 8) +
                   (addr[sz+3] & 0xFF));
      try {
        Class[] params = new Class[] { String.class, byte[].class, int.class };
        Method m = Inet6Address.class.getMethod("getByAddress", params);
        Object[] args = new Object[] { null, ipaddr, scope };
        Object o = m.invoke(Inet6Address.class, args);
        setInetAddress((InetAddress)o);
      }
      catch (Exception ex) {
        logger.warn("Java < 1.5 does not support scoped IPv6 addresses, "+
                    "ignoring scope ID for " + transportAddress);
        setInetAddress(InetAddress.getByAddress(ipaddr));
      }
    }
    else {
      setInetAddress(InetAddress.getByAddress(addr));
    }
    port = ((transportAddress.get(transportAddress.length()-2) & 0xFF) << 8);
    port += (transportAddress.get(transportAddress.length()-1) & 0xFF);
  }

  /**
   * Returns the address value as a byte array.
   * @return
   *    a byte array with IP address bytes and two additional bytes containing
   *    the port in network byte order. If the address is a zoned (scoped) IP
   *    address, four additional bytes with the scope ID are returned between
   *    address and port bytes.
   * @since 1.5
   */
  public byte[] getValue() {
    byte[] addr = getInetAddress().getAddress();
    int scopeSize = 0;
    int scopeID = 0;
    if (getInetAddress() instanceof Inet6Address) {
      try {
        Inet6Address ip6Addr = (Inet6Address) getInetAddress();
        Method m = Inet6Address.class.getMethod("getScopeId");
        Object scope = m.invoke(ip6Addr);
        scopeID = ((Number)scope).intValue();
        scopeSize = 4;
      }
      catch (Exception ex) {
        // ignore
      }
    }
    byte[] retval = new byte[addr.length+2+scopeSize];
    System.arraycopy(addr, 0, retval, 0, addr.length);
    int offset = addr.length;
    if (scopeSize > 0) {
      retval[offset++] = (byte)((scopeID & 0xFF000000) >> 24);
      retval[offset++] = (byte)((scopeID & 0x00FF0000) >> 16);
      retval[offset++] = (byte)((scopeID & 0x0000FF00) >> 8);
      retval[offset++] = (byte) (scopeID & 0x000000FF);
    }
    retval[offset++] = (byte)((port >> 8) & 0xFF);
    retval[offset] = (byte)(port & 0xFF);
    return retval;
  }

  public void decodeBER(BERInputStream inputStream) throws IOException {
    OctetString os = new OctetString();
    os.decodeBER(inputStream);
    try {
      setTransportAddress(os);
    }
    catch (Exception ex) {
      String txt = "Wrong encoding of TransportAddress";
      logger.error(txt);
      throw new IOException(txt+": "+ex.getMessage());
    }
  }

  public void encodeBER(OutputStream outputStream) throws IOException {
    OctetString os = new OctetString(getValue());
    os.encodeBER(outputStream);
  }

  public int getBERLength() {
    return getValue().length;
  }

  public int getBERPayloadLength() {
    return getBERLength();
  }

  public int getSyntax() {
    return SMIConstants.SYNTAX_OCTET_STRING;
  }

}
