/*_############################################################################
  _## 
  _##  SNMP4J - GenericAddress.java  
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

import java.io.*;
import java.util.*;
import org.snmp4j.log.*;
import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.SNMP4JSettings;

/**
 * The <code>GenericAddress</code> implements the decorator and factory
 * design pattern to provide a generic address type.
 * <p>
 * To register address types other than the default, set the system property
 * {@link #ADDRESS_TYPES_PROPERTIES} before calling the {@link #parse} method
 * for the first time.
 *
 * @author Frank Fock
 * @version 1.8
 */
public class GenericAddress extends SMIAddress {

  static final long serialVersionUID = -6102594326202231511L;

  /**
   * Default address type identifier for an UpdAddress.
   */
  public static final String TYPE_UDP = "udp";
  /**
   * Default address type identifier for a TcpAddress.
   */
  public static final String TYPE_TCP = "tcp";
  /**
   * Default address type identifier for an IpAddress.
   */
  public static final String TYPE_IP  = "ip";

  /**
   * Default address type identifier for an TlsAddress.
   */
  public static final String TYPE_TLS = "tls";

  public static final String ADDRESS_TYPES_PROPERTIES =
      "org.snmp4j.addresses";
  private static final String ADDRESS_TYPES_PROPERTIES_DEFAULT =
      "address.properties";

  private static final LogAdapter logger = LogFactory.getLogger(GenericAddress.class);

  private SMIAddress address;
  private static Map<String, Class<? extends Address>> knownAddressTypes = null;

  public GenericAddress() {
  }

  public GenericAddress(SMIAddress address) {
    this.address = address;
  }

  public int getSyntax() {
    return address.getSyntax();
  }

  public boolean isValid() {
    return (address != null) && address.isValid();
  }

  public String toString() {
    return address.toString();
  }

  public int hashCode() {
    return address.hashCode();
  }

  public int compareTo(Variable o) {
    return address.compareTo(o);
  }

  public boolean equals(Object o) {
    return address.equals(o);
  }

  public void decodeBER(BERInputStream inputStream) throws java.io.IOException {
    throw new UnsupportedOperationException();
  }
  public void encodeBER(OutputStream outputStream) throws java.io.IOException {
    address.encodeBER(outputStream);
  }

  public int getBERLength() {
    return address.getBERLength();
  }

  public void setAddress(SMIAddress address) {
    this.address = address;
  }

  public Address getAddress() {
    return address;
  }

  /**
   * Register Address classes from a properties file. The registered
   * address types are used by the {@link GenericAddress#parse(String)}
   * method to type-safe instantiate sub-classes from <code>Address</code>
   * from a <code>String</code>.
   */
  @SuppressWarnings("unchecked")
  private synchronized static void registerAddressTypes() {
    if (SNMP4JSettings.isExtensibilityEnabled()) {
      String addresses = System.getProperty(ADDRESS_TYPES_PROPERTIES,
                                            ADDRESS_TYPES_PROPERTIES_DEFAULT);
      InputStream is = Variable.class.getResourceAsStream(addresses);
      if (is == null) {
        throw new InternalError("Could not read '" + addresses +
                                "' from classpath!");
      }
      Properties props = new Properties();
      try {
        props.load(is);
        Map<String, Class<? extends Address>> h = new TreeMap<String, Class<? extends Address>>();
        for (Enumeration en = props.propertyNames(); en.hasMoreElements(); ) {
          String id = en.nextElement().toString();
          String className = props.getProperty(id);
          try {
            Class<? extends Address> c = (Class<? extends Address>)Class.forName(className);
            h.put(id, c);
          }
          catch (ClassNotFoundException cnfe) {
            logger.error(cnfe);
          }
          catch (ClassCastException ccex) {
            logger.error("Class name '"+className+"' is not a subclass of "+Address.class.getName());
          }
        }
        knownAddressTypes = h;
      }
      catch (IOException iox) {
        String txt = "Could not read '" + addresses + "': " + iox.getMessage();
        logger.error(txt);
        throw new InternalError(txt);
      }
      finally {
        try {
          is.close();
        }
        catch (IOException ex) {
          // ignore
          logger.warn(ex);
        }
      }
    }
    else {
      Map<String, Class<? extends Address>> h = new TreeMap<String, Class<? extends Address>>();
      h.put(TYPE_UDP, UdpAddress.class);
      h.put(TYPE_TCP, TcpAddress.class);
      h.put(TYPE_IP, IpAddress.class);
      h.put(TYPE_TLS, TlsAddress.class);
      knownAddressTypes = h;
    }
  }

  /**
   * Parses a given transport protocol dependent address string into an
   * <code>Address</code> instance that is subsumed by this
   * <code>GenericAddress</code> object.
   *
   * @param address
   *    an address string with a leading type specifier as defined in the
   *    "address.properties". The format is <code>"type:address"</code> where
   *    the format of <code>address</code> depends on <code>type</code>.
   *    Valid values for <code>type</code> are, for example, "udp" and "tcp".
   * @return
   *    a <code>Address</code> instance of the address classes specified
   *    in "address.properties" whose type ID matched the specified ID in
   *    <code>address</code>. If <code>address</code> cannot be parsed,
   *    <code>null</code> is returned.
   * @throws IllegalArgumentException
   *    if the address type indicator supplied is not know.
   */
  public static Address parse(String address) {
    if (knownAddressTypes == null) {
      registerAddressTypes();
    }
    String type = TYPE_UDP;
    int sep = address.indexOf(':');
    if (sep > 0) {
      type = address.substring(0, sep);
      address = address.substring(sep+1);
    }
    type = type.toLowerCase();
    Class<? extends Address> c = knownAddressTypes.get(type);
    if (c == null) {
      throw new IllegalArgumentException("Address type " + type + " unknown");
    }
    try {
      Address addr = c.newInstance();
      if (addr.parseAddress(address)) {
        return addr;
      }
      return null;
    }
    catch (Exception ex) {
      logger.warn(ex);
    }
    return null;
  }

  /**
   * Parse an address form the supplied string.
   * @param address
   *    an address string known by the GenericAddress.
   * @return boolean
   * @see #parse(String address)
   */
  public boolean parseAddress(String address) {
    Address addr = parse(address);
    if (addr instanceof SMIAddress) {
      setAddress((SMIAddress)addr);
      return true;
    }
    return false;
  }

  public void setValue(byte[] rawAddress) {
    address.setValue(rawAddress);
  }

  public Object clone() {
    return new GenericAddress(address);
  }

  public int toInt() {
    throw new UnsupportedOperationException();
  }

  public long toLong() {
    throw new UnsupportedOperationException();
  }

  public OID toSubIndex(boolean impliedLength) {
    throw new UnsupportedOperationException();
  }

  public void fromSubIndex(OID subIndex, boolean impliedLength) {
    throw new UnsupportedOperationException();
  }

  public byte[] toByteArray() {
    return address.toByteArray();
  }

  public void setValue(String value) {
    if (!parseAddress(value)) {
      throw new IllegalArgumentException(value+" cannot be parsed by "+
                                         getClass().getName());
    }
  }

  /**
   * Gets the transport domain prefix string (lowercase) for a supplied
   * {@link Address} class.
   * @param addressClass
   *    an implementation class of {@link Address}.
   * @return
   *    the corresponding transport domain prefix as defined by the
   *    IANA registry "SNMP Transport Domains" if the <code>addressClass</code>
   *    has been registered with a domain prefix, <code>null</code> otherwise.
   * @since 2.0
   */
  public static String getTDomainPrefix(Class<? extends Address> addressClass) {
    if (knownAddressTypes == null) {
      registerAddressTypes();
    }
    for (Map.Entry<String,Class<? extends Address>> entry : knownAddressTypes.entrySet()) {
      if (entry.getValue().equals(addressClass)) {
        return entry.getKey();
      }
    }
    return null;
  }
}

