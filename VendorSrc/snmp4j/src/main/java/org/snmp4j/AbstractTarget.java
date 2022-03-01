/*_############################################################################
  _## 
  _##  SNMP4J - AbstractTarget.java  
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
package org.snmp4j;

import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.smi.Address;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OctetString;

import java.util.List;

/**
 * A {@code AbstractTarget} class is an abstract representation of a remote
 * SNMP entity. It represents a target with an Address object, as well protocol
 * parameters such as retransmission and timeout policy. Implementers of the
 * {@link Target} interface can subclass {@code AbstractTarget} to
 * take advantage of the implementation of common {@code Target}
 * properties.
 *
 * @author Frank Fock
 * @version 2.0
 * @since 1.2
 */
public abstract class AbstractTarget implements Target {

  private Address address;
  private int version = SnmpConstants.version3;
  private int retries = 0;
  private long timeout = 1000;
  private int maxSizeRequestPDU = 65535;
  private List<TransportMapping<? extends Address>> preferredTransports;

  protected int securityLevel = SecurityLevel.NOAUTH_NOPRIV;
  protected int securityModel = SecurityModel.SECURITY_MODEL_USM;
  protected OctetString securityName = new OctetString();

  /**
   * Default constructor
   */
  protected AbstractTarget() {
  }

  /**
   * Creates a SNMPv3 target with no retries and a timeout of one second.
   * @param address
   *    an {@code Address} instance.
   */
  protected AbstractTarget(Address address) {
    this.address = address;
  }

  protected AbstractTarget(Address address, OctetString securityName) {
    this(address);
    this.securityName = securityName;
  }

  /**
   * Gets the address of this target.
   * @return
   *    an Address instance.
   */
  public Address getAddress() {
    return address;
  }

  /**
   * Sets the address of the target.
   * @param address
   *    an Address instance.
   */
  public void setAddress(Address address) {
    this.address = address;
  }

  /**
   * Sets the SNMP version (thus the SNMP message processing model) of the
   * target.
   * @param version
   *    the message processing model ID.
   * @see org.snmp4j.mp.SnmpConstants#version1
   * @see org.snmp4j.mp.SnmpConstants#version2c
   * @see org.snmp4j.mp.SnmpConstants#version3
   */
  public void setVersion(int version) {
    this.version = version;
  }

  /**
   * Gets the SNMP version (NMP messagen processing model) of the target.
   * @return
   *    the message processing model ID.
   * @see org.snmp4j.mp.SnmpConstants#version1
   * @see org.snmp4j.mp.SnmpConstants#version2c
   * @see org.snmp4j.mp.SnmpConstants#version3
   */
  public int getVersion() {
    return version;
  }

  /**
   * Sets the number of retries to be performed before a request is timed out.
   * @param retries
   *    the number of retries. <em>Note: If the number of retries is set to
   *    0, then the request will be sent out exactly once.</em>
   */
  public void setRetries(int retries) {
    if (retries < 0) {
      throw new IllegalArgumentException("Number of retries < 0");
    }
    this.retries = retries;
  }

  /**
   * Gets the number of retries.
   * @return
   *    an integer &gt;= 0.
   */
  public int getRetries() {
    return retries;
  }

  /**
   * Sets the timeout for a target.
   * @param timeout
   *    timeout in milliseconds before a confirmed request is resent or
   *    timed out.
   */
  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  /**
   * Gets the timeout for a target.
   * @return
   *    the timeout in milliseconds.
   */
  public long getTimeout() {
    return timeout;
  }

  /**
   * Gets the maximum size of request PDUs that this target is able to respond
   * to. The default is 65535.
   * @return
   *    the maximum PDU size of request PDUs for this target. Which is always
   *    greater than 484.
   */
  public int getMaxSizeRequestPDU() {
    return maxSizeRequestPDU;
  }

  /**
   * Sets the maximum size of request PDUs that this target is able to receive.
   * @param maxSizeRequestPDU
   *    the maximum PDU (SNMP message) size this session will be able to
   *    process.
   */
  public void setMaxSizeRequestPDU(int maxSizeRequestPDU) {
    if (maxSizeRequestPDU < SnmpConstants.MIN_PDU_LENGTH) {
      throw new IllegalArgumentException("The minimum PDU length is: "+
                                         SnmpConstants.MIN_PDU_LENGTH);
    }
    this.maxSizeRequestPDU = maxSizeRequestPDU;
  }

  public List<TransportMapping<? extends Address>> getPreferredTransports() {
    return preferredTransports;
  }

  /**
   * Sets the prioritised list of transport mappings to be used for this
   * target. The first mapping in the list that matches the target address
   * will be chosen for sending new requests. If the value is set to
   * {@code null} (default), the appropriate {@link TransportMapping}
   * will be chosen by the supplied {@code address} of the target.
   * If an entity supports more than one {@link TransportMapping} for
   * an {@link Address} class, the the results are not defined.
   * This situation can be controlled by setting this preferredTransports
   * list.
   * @param preferredTransports
   *    a list of transport mappings that are preferred for this target class.
   * @since 2.0
   */
  public void setPreferredTransports(List<TransportMapping<? extends Address>>
                                         preferredTransports) {
    this.preferredTransports = preferredTransports;
  }

  protected String toStringAbstractTarget() {
    return "address="+getAddress()+",version="+version+
        ",timeout="+timeout+",retries="+retries+
        ",securityLevel=" + securityLevel +
        ",securityModel=" + securityModel +
        ",securityName=" + securityName +
        ",preferredTransports="+preferredTransports;
  }

  public String toString() {
    return getClass().getName()+"["+toStringAbstractTarget()+"]";
  }

  public Object clone() {
    try {
      return super.clone();
    }
    catch (CloneNotSupportedException ex) {
      return null;
    }
  }


  public int getSecurityModel() {
    return securityModel;
  }

  public final OctetString getSecurityName() {
    return securityName;
  }

  public int getSecurityLevel() {
    return securityLevel;
  }

  /**
   * Sets the security level for this target. The supplied security level must
   * be supported by the security model dependent information associated with
   * the security name set for this target.
   * @param securityLevel
   *   one of
   *   <UL>
   *   <LI>{@link org.snmp4j.security.SecurityLevel#NOAUTH_NOPRIV}</LI>
   *   <LI>{@link org.snmp4j.security.SecurityLevel#AUTH_NOPRIV}</LI>
   *   <LI>{@link org.snmp4j.security.SecurityLevel#AUTH_PRIV}</LI>
   *   </UL>
   */
  public void setSecurityLevel(int securityLevel) {
    this.securityLevel = securityLevel;
  }

  /**
   * Sets the security model for this target.
   * @param securityModel
   *    an {@code int} value as defined in the {@link org.snmp4j.security.SecurityModel}
   *    interface or any third party subclass thereof.
   */
  public void setSecurityModel(int securityModel) {
    this.securityModel = securityModel;
  }

  /**
   * Sets the security name to be used with this target.
   * @param securityName
   *    an {@code OctetString} instance (must not be {@code null}).
   * @see #getSecurityName()
   */
  public final void setSecurityName(OctetString securityName) {
    this.securityName = securityName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AbstractTarget that = (AbstractTarget) o;

    if (version != that.version) return false;
    if (retries != that.retries) return false;
    if (timeout != that.timeout) return false;
    if (maxSizeRequestPDU != that.maxSizeRequestPDU) return false;
    if (securityLevel != that.securityLevel) return false;
    if (securityModel != that.securityModel) return false;
    if (!address.equals(that.address)) return false;
    if (preferredTransports != null ? !preferredTransports.equals(that.preferredTransports) : that.preferredTransports != null)
      return false;
    if (!securityName.equals(that.securityName)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = address.hashCode();
    result = 31 * result + version;
    result = 31 * result + securityName.hashCode();
    return result;
  }
}

