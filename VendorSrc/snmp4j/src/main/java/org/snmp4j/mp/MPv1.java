/*_############################################################################
  _## 
  _##  SNMP4J - MPv1.java  
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
package org.snmp4j.mp;

import org.snmp4j.*;
import org.snmp4j.smi.Address;
import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OctetString;

import java.io.IOException;

import org.snmp4j.log.*;
import org.snmp4j.asn1.BER;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.asn1.BER.MutableByte;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.asn1.BEROutputStream;
import java.nio.ByteBuffer;
import org.snmp4j.util.PDUFactory;

/**
 * The <code>MPv1</code> is the message processing model for SNMPv1.
 * @author Frank Fock
 * @version 2.2
 */
public class MPv1 implements MessageProcessingModel {

  public static final int ID = MessageProcessingModel.MPv1;
  private static final LogAdapter logger = LogFactory.getLogger(MPv1.class);

  protected PDUFactory incomingPDUFactory = new PDUFactory() {
    public PDU createPDU(Target target) {
      return new PDUv1();
    }
    public PDU createPDU(MessageProcessingModel messageProcessingModel) {
      return new PDUv1();
    }
  };

  /**
   * Creates a SNMPv1 message processing model with a PDU factory for incoming
   * messages that uses {@link PDUv1}.
   */
  public MPv1() {
  }

  /**
   * Creates a SNMPv1 message processing model with a custom PDU factory that
   * must ignore the target parameter when creating a PDU for parsing incoming
   * messages.
   * @param incomingPDUFactory
   *    a {@link PDUFactory}. If <code>null</code> the default factory will be
   *    used which creates {@link ScopedPDU} instances.
   */
  public MPv1(PDUFactory incomingPDUFactory) {
    if (incomingPDUFactory != null) {
      this.incomingPDUFactory = incomingPDUFactory;
    }
  }

  public int getID() {
    return ID;
  }

  public int prepareOutgoingMessage(Address transportAddress,
                                    int maxMessageSize,
                                    int messageProcessingModel,
                                    int securityModel,
                                    byte[] securityName,
                                    int securityLevel,
                                    PDU pdu,
                                    boolean expectResponse,
                                    PduHandle sendPduHandle,
                                    Address destTransportAddress,
                                    BEROutputStream outgoingMessage, TransportStateReference tmStateReference)
      throws IOException
  {
    if ((securityLevel != SecurityLevel.NOAUTH_NOPRIV) ||
        (securityModel != SecurityModel.SECURITY_MODEL_SNMPv1)) {
      logger.error("MPv1 used with unsupported security model");
      return SnmpConstants.SNMP_MP_UNSUPPORTED_SECURITY_MODEL;
    }
    if (pdu instanceof ScopedPDU) {
      String txt = "ScopedPDU must not be used with MPv1";
      logger.error(txt);
      throw new IllegalArgumentException(txt);
    }

    if (!isProtocolVersionSupported(messageProcessingModel)) {
      logger.error("MPv1 used with unsupported SNMP version");
      return SnmpConstants.SNMP_MP_UNSUPPORTED_SECURITY_MODEL;
    }


    OctetString community = new OctetString(securityName);
    Integer32 version = new Integer32(messageProcessingModel);
    // compute total length
    int length = pdu.getBERLength();
    length += community.getBERLength();
    length += version.getBERLength();

    ByteBuffer buf = ByteBuffer.allocate(length +
                                         BER.getBERLengthOfLength(length) + 1);
    // set the buffer of the outgoing message
    outgoingMessage.setBuffer(buf);

    // encode the message
    BER.encodeHeader(outgoingMessage, BER.SEQUENCE, length);
    version.encodeBER(outgoingMessage);

    community.encodeBER(outgoingMessage);
    pdu.encodeBER(outgoingMessage);

    return SnmpConstants.SNMP_MP_OK;
  }

  public int prepareResponseMessage(int messageProcessingModel,
                                    int maxMessageSize,
                                    int securityModel,
                                    byte[] securityName,
                                    int securityLevel,
                                    PDU pdu,
                                    int maxSizeResponseScopedPDU,
                                    StateReference stateReference,
                                    StatusInformation statusInformation,
                                    BEROutputStream outgoingMessage)
      throws IOException
  {
    return prepareOutgoingMessage(stateReference.getAddress(),
                                  maxMessageSize,
                                  messageProcessingModel,
                                  securityModel,
                                  securityName,
                                  securityLevel,
                                  pdu,
                                  false,
                                  stateReference.getPduHandle(),
                                  null,
                                  outgoingMessage, null);
  }

  public int prepareDataElements(MessageDispatcher messageDispatcher,
                                 Address transportAddress,
                                 BERInputStream wholeMsg,
                                 TransportStateReference tmStateReference,
                                 Integer32 messageProcessingModel,
                                 Integer32 securityModel,
                                 OctetString securityName,
                                 Integer32 securityLevel,
                                 MutablePDU pdu,
                                 PduHandle sendPduHandle,
                                 Integer32 maxSizeResponseScopedPDU,
                                 StatusInformation statusInformation,
                                 MutableStateReference mutableStateReference)
      throws IOException
  {
    MutableByte mutableByte = new MutableByte();
    int length = BER.decodeHeader(wholeMsg, mutableByte);
    int startPos = (int)wholeMsg.getPosition();
    if (mutableByte.getValue() != BER.SEQUENCE) {
      String txt = "SNMPv1 PDU must start with a SEQUENCE";
      logger.error(txt);
      throw new IOException(txt);
    }
    Integer32 version = new Integer32();
    version.decodeBER(wholeMsg);

    securityName.decodeBER(wholeMsg);
    securityLevel.setValue(SecurityLevel.NOAUTH_NOPRIV);
    securityModel.setValue(SecurityModel.SECURITY_MODEL_SNMPv1);
    messageProcessingModel.setValue(ID);

    PDU v1PDU = incomingPDUFactory.createPDU(this);
    pdu.setPdu(v1PDU);
    v1PDU.decodeBER(wholeMsg);

    BER.checkSequenceLength(length, (int)wholeMsg.getPosition() - startPos,
                            v1PDU);

    sendPduHandle.setTransactionID(v1PDU.getRequestID().getValue());

    // create state reference
    StateReference stateRef =
        new StateReference(sendPduHandle,
                           transportAddress,
                           null,
                           SecurityModels.getInstance().getSecurityModel(securityModel),
                           securityName.getValue(),
                           SnmpConstants.SNMP_ERROR_SUCCESS);
    mutableStateReference.setStateReference(stateRef);

    return SnmpConstants.SNMP_MP_OK;
  }

  public boolean isProtocolVersionSupported(int snmpProtocolVersion) {
    return (snmpProtocolVersion == SnmpConstants.version1);
  }

  public void releaseStateReference(PduHandle pduHandle) {
    // we do not cache state information -> do nothing
  }

}
