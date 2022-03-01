/*_############################################################################
  _## 
  _##  SNMP4J - Target.java  
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

import java.io.Serializable;
import java.util.List;

import org.snmp4j.smi.Address;
// for JavaDoc
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OctetString;

/**
 * A {@code Target} interface defines an abstract representation of a
 * remote SNMP entity. It represents a target with an Address object, as well
 * protocol parameters such as retransmission and timeout policy.
 *
 * @author Frank Fock
 * @version 2.0
 */
public interface Target extends Serializable, Cloneable {

  /**
   * Gets the address of this target.
   * @return
   *    an Address instance.
   */
  Address getAddress();

  /**
   * Sets the address of the target.
   * @param address
   *    an Address instance.
   */
  void setAddress(Address address);

  /**
   * Sets the SNMP version (thus the SNMP message processing model) of the
   * target.
   * @param version
   *    the message processing model ID.
   * @see org.snmp4j.mp.SnmpConstants#version1
   * @see org.snmp4j.mp.SnmpConstants#version2c
   * @see org.snmp4j.mp.SnmpConstants#version3
   */
  void setVersion(int version);

  /**
   * Gets the SNMP version (NMP messagen processing model) of the target.
   * @return
   *    the message processing model ID.
   * @see org.snmp4j.mp.SnmpConstants#version1
   * @see org.snmp4j.mp.SnmpConstants#version2c
   * @see org.snmp4j.mp.SnmpConstants#version3
   */
  int getVersion();

  /**
   * Sets the number of retries to be performed before a request is timed out.
   * @param retries
   *    the number of retries. <em>Note: If the number of retries is set to
   *    0, then the request will be sent out exactly once.</em>
   */
  void setRetries(int retries);

  /**
   * Gets the number of retries.
   * @return
   *    an integer &gt;= 0.
   */
  int getRetries();

  /**
   * Sets the timeout for a target.
   * @param timeout
   *    timeout in milliseconds before a confirmed request is resent or
   *    timed out.
   */
  void setTimeout(long timeout);

  /**
   * Gets the timeout for a target.
   * @return
   *    the timeout in milliseconds.
   */
  long getTimeout();

  /**
   * Gets the maximum size of request PDUs that this target is able to respond
   * to. The default is 65535.
   * @return
   *    the maximum PDU size of request PDUs for this target. Which is always
   *    greater than 484.
   */
  int getMaxSizeRequestPDU();

  /**
   * Sets the maximum size of request PDUs that this target is able to receive.
   * @param maxSizeRequestPDU
   *    the maximum PDU (SNMP message) size this session will be able to
   *    process.
   */
  void setMaxSizeRequestPDU(int maxSizeRequestPDU);

  /**
   * Gets the prioritised list of transport mappings to be used for this
   * target. The first mapping in the list that matches the target address
   * is chosen for sending new requests.
   *
   * @return
   *    an ordered list of {@link TransportMapping} instances.
   * @since 2.0
   */
  List<TransportMapping<? extends Address>> getPreferredTransports();

  Object clone();

  /**
   * Creates a new copy of this target with the same address type.
   * @return  a copy of this target with the same address type.
   * @since 2.8.0
   */
  Target duplicate();



  /**
   * Gets the security model associated with this target.
   * @return
   *    an {@code int} value as defined in the {@link org.snmp4j.security.SecurityModel}
   *    interface or any third party subclass thereof.
   */
  int getSecurityModel();

  /**
   * Gets the security name associated with this target. The security name
   * is used by the security model to lookup further parameters like
   * authentication and privacy protocol settings from the security model
   * dependent internal storage.
   * @return
   *   an {@code OctetString} instance (never {@code null}).
   */
  OctetString getSecurityName();

  /**
   * Gets the security level associated with this target.
   * @return
   *   one of
   *   <UL>
   *   <LI>{@link org.snmp4j.security.SecurityLevel#NOAUTH_NOPRIV}</LI>
   *   <LI>{@link org.snmp4j.security.SecurityLevel#AUTH_NOPRIV}</LI>
   *   <LI>{@link org.snmp4j.security.SecurityLevel#AUTH_PRIV}</LI>
   *   </UL>
   */
  int getSecurityLevel();

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
  void setSecurityLevel(int securityLevel);

  /**
   * Sets the security model for this target.
   * @param securityModel
   *    an {@code int} value as defined in the {@link org.snmp4j.security.SecurityModel}
   *    interface or any third party subclass thereof.
   */
  void setSecurityModel(int securityModel);

  /**
   * Sets the security name to be used with this target.
   * @param securityName
   *    an {@code OctetString} instance (must not be {@code null}).
   * @see #getSecurityName()
   */
  void setSecurityName(OctetString securityName);
}

