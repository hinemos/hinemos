/*_############################################################################
  _## 
  _##  SNMP4J - TSM.java  
  _## 
  _##  Copyright (C) 2003-2020  Frank Fock and Jochen Katz (SNMP4J.org)
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

package org.snmp4j.security;

import org.snmp4j.TransportStateReference;
import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.asn1.BEROutputStream;
import org.snmp4j.event.CounterEvent;
import org.snmp4j.log.LogAdapter;
import org.snmp4j.log.LogFactory;
import org.snmp4j.mp.CounterSupport;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.mp.StatusInformation;
import org.snmp4j.smi.*;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * The <code>TSM</code> (Transport Security Model) implements a
 * {@link SecurityModel} which uses transport security mechanisms
 * as defined in RFC 5591.
 *
 * @author Frank Fock
 * @version 2.0
 * @since 2.0
 */
public class TSM extends SNMPv3SecurityModel  {

  private static final int MAX_PREFIX_LENGTH = 4;
  private static final byte PREFIX_SEPARATOR = 0x3a;

  private static final LogAdapter logger = LogFactory.getLogger(TSM.class);

  /**
   * The snmpTsmConfigurationUsePrefix flag as defined in RFC 5591.
   */
  private boolean usePrefix;
  private CounterSupport counterSupport;

  public TSM() {
    this(new OctetString(MPv3.createLocalEngineID()), false);
  }

  public TSM(OctetString localEngineID, boolean usePrefix) {
    this.counterSupport = CounterSupport.getInstance();
    this.localEngineID = localEngineID;
    this.usePrefix = usePrefix;
  }

  public void setLocalEngineID(OctetString localEngineID) {
     this.localEngineID = localEngineID;
  }

  protected void fireIncrementCounter(CounterEvent e) {
    counterSupport.fireIncrementCounter(e);
  }

  @Override
  public int getID() {
    return SECURITY_MODEL_TSM;
  }

  @Override
  public boolean supportsEngineIdDiscovery() {
    // RFC 5343 section 3.2
    return false;
  }

  @Override
  public boolean hasAuthoritativeEngineID() {
    return false;
  }

  @Override
  public SecurityParameters newSecurityParametersInstance() {
    return new TsmSecurityParameters();
  }

  @Override
  public SecurityStateReference newSecurityStateReference() {
    return new TsmSecurityStateReference();
  }

  @Override
  public int generateRequestMessage(int messageProcessingModel,
                                    byte[] globalData,
                                    int maxMessageSize,
                                    int securityModel,
                                    byte[] securityEngineID,
                                    byte[] securityName,
                                    int securityLevel,
                                    BERInputStream scopedPDU,
                                    SecurityParameters securityParameters,
                                    BEROutputStream wholeMsg,
                                    TransportStateReference tmStateReference) throws IOException {
    return generateMessage(messageProcessingModel,
        globalData,
        maxMessageSize,
        securityModel,
        securityEngineID,
        securityName,
        securityLevel,
        scopedPDU,
        null,
        securityParameters,
        wholeMsg,
        tmStateReference);
  }

  public CounterSupport getCounterSupport() {
    return counterSupport;
  }

  private int generateMessage(int messageProcessingModel,
                              byte[] globalData,
                              int maxMessageSize,
                              int securityModel,
                              byte[] securityEngineID,
                              byte[] securityName,
                              int securityLevel,
                              BERInputStream scopedPDU,
                              SecurityStateReference securityStateReference,
                              SecurityParameters securityParameters,
                              BEROutputStream wholeMsg,
                              TransportStateReference tmStateReference) throws IOException {
    TransportStateReference activeTmStateReference = tmStateReference;
    TsmSecurityStateReference tsmSecurityStateReference =
        (TsmSecurityStateReference) securityStateReference;
    if ((tsmSecurityStateReference != null) &&
        (tsmSecurityStateReference.getTmStateReference() != null)) {
      activeTmStateReference = tsmSecurityStateReference.getTmStateReference();
      activeTmStateReference.setRequestedSecurityLevel(
          activeTmStateReference.getTransportSecurityLevel());
      // TSM uses same security for responses/reports as defined in RFC 5591 §4.2.1
      activeTmStateReference.setSameSecurity(true);
    }
    else {
      // TSM does not use same security for requests as defined in RFC 5591 §4.2.2
      activeTmStateReference.setSameSecurity(false);
      if (usePrefix) {
        String prefix = getTransportDomainPrefix(tmStateReference.getAddress());
        if (prefix == null) {
          CounterEvent event = new CounterEvent(this,
                                                SnmpConstants.snmpTsmUnknownPrefixes);
          fireIncrementCounter(event);
          return SnmpConstants.SNMPv3_TSM_UNKNOWN_PREFIXES;
        }
        else {
          String secNamePrefix = getSecurityNamePrefix(securityName);
          if ((secNamePrefix == null) || (!secNamePrefix.equals(prefix))) {
            CounterEvent event = new CounterEvent(this,
                                                  SnmpConstants.snmpTsmInvalidPrefixes);
            fireIncrementCounter(event);
            return SnmpConstants.SNMPv3_TSM_UNKNOWN_PREFIXES;
          }
          // remove prefix and assign tmSecurityName
          activeTmStateReference.setSecurityName(
              new OctetString(new String(securityName).substring(secNamePrefix.length()+1)));
        }
      }
      else {
        activeTmStateReference.setSecurityName(new OctetString(securityName));
      }
    }
    // SecurityParameters already set to zero length OctetString by MPv3.
    // Build Message without authentication
    byte[] scopedPduBytes = buildMessageBuffer(scopedPDU);
    byte[] wholeMessage =
      buildWholeMessage(new Integer32(messageProcessingModel),
                        scopedPduBytes, globalData, securityParameters);
    ByteBuffer buf =
        (ByteBuffer)ByteBuffer.wrap(wholeMessage).position(wholeMessage.length);
    wholeMsg.setBuffer(buf);
    return SnmpConstants.SNMPv3_TSM_OK;
  }

  protected String getSecurityNamePrefix(byte[] securityName) {
    OctetString secName = new OctetString(securityName);
    String prefix = new String(secName.getValue());
    int colonPos = prefix.indexOf(':');
    if ((colonPos <= 0) || (colonPos > MAX_PREFIX_LENGTH)) {
      return null;
    }
    return prefix.substring(0, colonPos);
  }

  protected String getTransportDomainPrefix(Address address) {
    return GenericAddress.getTDomainPrefix(address.getClass());
  }

  @Override
  public int generateResponseMessage(int messageProcessingModel,
                                     byte[] globalData,
                                     int maxMessageSize,
                                     int securityModel,
                                     byte[] securityEngineID,
                                     byte[] securityName,
                                     int securityLevel,
                                     BERInputStream scopedPDU,
                                     SecurityStateReference securityStateReference,
                                     SecurityParameters securityParameters,
                                     BEROutputStream wholeMsg) throws IOException {
    return generateMessage(messageProcessingModel,
        globalData, maxMessageSize, securityModel, securityEngineID, securityName,
        securityLevel, scopedPDU, securityStateReference, securityParameters,
        wholeMsg, null);
  }

  @Override
  public int processIncomingMsg(int messageProcessingModel,
                                int maxMessageSize,
                                SecurityParameters securityParameters,
                                SecurityModel securityModel,
                                int securityLevel,
                                BERInputStream wholeMsg,
                                TransportStateReference tmStateReference,
                                OctetString securityEngineID,
                                OctetString securityName,
                                BEROutputStream scopedPDU,
                                Integer32 maxSizeResponseScopedPDU,
                                SecurityStateReference securityStateReference,
                                StatusInformation statusInfo) throws IOException {
    // 1. Set the securityEngineID to the local snmpEngineID.
    securityEngineID.setValue(localEngineID.getValue());
    // 2. Check tmStateReference
    if ((tmStateReference == null) ||
        (!tmStateReference.isTransportSecurityValid())) {
      CounterEvent event = new CounterEvent(this,
                                            SnmpConstants.snmpTsmInvalidCaches);
      fireIncrementCounter(event);
      return SnmpConstants.SNMPv3_TSM_INVALID_CACHES;
    }
    // 3. Copy the tmSecurityName to securityName.
    if (usePrefix) {
      String prefix =
          GenericAddress.getTDomainPrefix(tmStateReference.getAddress().getClass());
      if (prefix == null) {
        CounterEvent event = new CounterEvent(this,
                                              SnmpConstants.snmpTsmUnknownPrefixes);
        fireIncrementCounter(event);
        updateStatusInfo(securityLevel, statusInfo, event);
        return SnmpConstants.SNMPv3_TSM_UNKNOWN_PREFIXES;
      }
      else if ((prefix.length() <= 0) || (prefix.length() > 4)) {
        CounterEvent event = new CounterEvent(this,
                                              SnmpConstants.snmpTsmInvalidPrefixes);
        fireIncrementCounter(event);
        updateStatusInfo(securityLevel, statusInfo, event);
        return SnmpConstants.SNMPv3_TSM_UNKNOWN_PREFIXES;
      }
      else {
        OctetString secName = new OctetString(prefix);
        secName.append(PREFIX_SEPARATOR);
        secName.append(tmStateReference.getSecurityName());
        securityName.setValue(secName.getValue());
      }
    }
    else {
      securityName.setValue(tmStateReference.getSecurityName().getValue());
    }
    // 4. Compare the value of tmTransportSecurityLevel:
    if (securityLevel > tmStateReference.getTransportSecurityLevel().getSnmpValue()) {
      CounterEvent event = new CounterEvent(this,
                                            SnmpConstants.snmpTsmInadequateSecurityLevels);
      fireIncrementCounter(event);
      updateStatusInfo(securityLevel, statusInfo, event);
      return SnmpConstants.SNMPv3_TSM_INADEQUATE_SECURITY_LEVELS;
    }
    // 5. The tmStateReference is cached as cachedSecurityData:
    ((TsmSecurityStateReference)securityStateReference).setTmStateReference(tmStateReference);
    // 6. The scopedPDU component is extracted from the wholeMsg.
    TsmSecurityParameters tsmSecurityParameters =
        (TsmSecurityParameters) securityParameters;
    int scopedPDUPosition = tsmSecurityParameters.getScopedPduPosition();
    byte[] message = buildMessageBuffer(wholeMsg);
    int scopedPduLength = message.length - scopedPDUPosition;
    ByteBuffer buf =
        ByteBuffer.wrap(message, scopedPDUPosition, scopedPduLength);
    scopedPDU.setFilledBuffer(buf);
    // 7. The maxSizeResponseScopedPDU is calculated.
    //    Compute real max size response pdu according  to RFC3414 §3.2.9
    int maxSecParamsOverhead =
        tsmSecurityParameters.getBERMaxLength(securityLevel);
    maxSizeResponseScopedPDU.setValue(maxMessageSize -
                                      maxSecParamsOverhead);
    // 8. The statusInformation is set to success.
    return SnmpConstants.SNMPv3_TSM_OK;
  }

  private void updateStatusInfo(int securityLevel, StatusInformation statusInfo, CounterEvent event) {
    if (statusInfo != null) {
      statusInfo.setSecurityLevel(new Integer32(securityLevel));
      statusInfo.setErrorIndication(new VariableBinding(event.getOid(),
          event.getCurrentValue()));
    }
  }

  /**
   * Returns whether the transport domain prefix is prepended to the securityName.
   * @return
   *    <code>true</code> if the transport domain prefix is prepended to the securityName.
   */
  public boolean isUsePrefix() {
    return usePrefix;
  }

  /**
   * Sets the flag that controls whether the transport domain prefix is prepended to the securityName.
   * @param usePrefix
   *     if <code>true</code> the transport domain prefix is prepended to the securityName.
   */
  public void setUsePrefix(boolean usePrefix) {
    this.usePrefix = usePrefix;
  }

}
