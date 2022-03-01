/*_############################################################################
  _## 
  _##  SNMP4J - StateReference.java  
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

import java.util.*;

import org.snmp4j.*;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;

import java.io.Serializable;


/**
 * The <code>StateReference</code> class represents state information associated with SNMP messages. The state reference
 * is used to send response or report (SNMPv3 only). Depending on the security model not all fields may be filled.
 *
 * @author Frank Fock
 * @version 3.1.0
 */
public class StateReference<A extends Address> implements Serializable {

    private static final long serialVersionUID = 7385215386971310699L;

    private A address;
    private transient TransportMapping<? super A> transportMapping;
    private byte[] contextEngineID;
    private byte[] contextName;
    private SecurityModel securityModel;
    private byte[] securityName;
    private int securityLevel;
    private SecurityStateReference securityStateReference;
    private MessageID msgID;
    private int maxSizeResponseScopedPDU;
    private int msgFlags;
    private PduHandle pduHandle;
    private byte[] securityEngineID;
    private int errorCode = 0;
    protected List<MessageID> retryMsgIDs;
    private int matchedMsgID;
    private long responseRuntimeNanos;

    /**
     * Default constructor.
     */
    public StateReference() {
    }

    /**
     * Creates a state reference for community based security models.
     *
     * @param pduHandle
     *         PduHandle
     * @param peerAddress
     *         Address
     * @param peerTransport
     *         the <code>TransportMapping</code> to be used to communicate with the peer.
     * @param secModel
     *         SecurityModel
     * @param secName
     *         a community string.
     * @param errorCode
     *         an error code associated with the SNMP message.
     */
    public StateReference(PduHandle pduHandle,
                          A peerAddress,
                          TransportMapping<? super A> peerTransport,
                          SecurityModel secModel,
                          byte[] secName,
                          int errorCode) {
        this(0, 0, 65535, pduHandle, peerAddress, peerTransport,
                null, secModel, secName,
                SecurityLevel.NOAUTH_NOPRIV, null, null, null, errorCode);
    }

    /**
     * Creates a state reference for SNMPv3 messages.
     *
     * @param msgID
     *         int
     * @param msgFlags
     *         int
     * @param maxSizeResponseScopedPDU
     *         int
     * @param pduHandle
     *         PduHandle
     * @param peerAddress
     *         Address
     * @param peerTransport
     *         the <code>TransportMapping</code> to be used to communicate with the peer.
     * @param secEngineID
     *         byte[]
     * @param secModel
     *         SecurityModel
     * @param secName
     *         byte[]
     * @param secLevel
     *         int
     * @param contextEngineID
     *         byte[]
     * @param contextName
     *         byte[]
     * @param secStateReference
     *         SecurityStateReference
     * @param errorCode
     *         int
     */
    public StateReference(int msgID,
                          int msgFlags,
                          int maxSizeResponseScopedPDU,
                          PduHandle pduHandle,
                          A peerAddress,
                          TransportMapping<? super A> peerTransport,
                          byte[] secEngineID,
                          SecurityModel secModel,
                          byte[] secName,
                          int secLevel,
                          byte[] contextEngineID,
                          byte[] contextName,
                          SecurityStateReference secStateReference,
                          int errorCode) {
        this.msgID = createMessageID(msgID);
        this.msgFlags = msgFlags;
        this.maxSizeResponseScopedPDU = maxSizeResponseScopedPDU;
        this.pduHandle = pduHandle;
        this.address = peerAddress;
        this.transportMapping = peerTransport;
        this.securityEngineID = secEngineID;
        this.securityModel = secModel;
        this.securityName = secName;
        this.securityLevel = secLevel;
        this.contextEngineID = contextEngineID;
        this.contextName = contextName;
        this.securityStateReference = secStateReference;
        this.errorCode = errorCode;
    }

    public boolean isReportable() {
        return ((msgFlags & 0x04) > 0);
    }

    public A getAddress() {
        return address;
    }

    public void setAddress(A address) {
        this.address = address;
    }

    public void setContextEngineID(byte[] contextEngineID) {
        this.contextEngineID = contextEngineID;
    }

    public byte[] getContextEngineID() {
        return contextEngineID;
    }

    public void setContextName(byte[] contextName) {
        this.contextName = contextName;
    }

    public byte[] getContextName() {
        return contextName;
    }

    public void setSecurityModel(SecurityModel securityModel) {
        this.securityModel = securityModel;
    }

    public SecurityModel getSecurityModel() {
        return securityModel;
    }

    public void setSecurityName(byte[] securityName) {
        this.securityName = securityName;
    }

    public byte[] getSecurityName() {
        return securityName;
    }

    public void setSecurityLevel(int securityLevel) {
        this.securityLevel = securityLevel;
    }

    public int getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityStateReference(SecurityStateReference securityStateReference) {
        this.securityStateReference = securityStateReference;
    }

    public SecurityStateReference getSecurityStateReference() {
        return securityStateReference;
    }

    public void setMsgID(MessageID msgID) {
        this.msgID = msgID;
    }

    public void setMsgID(int msgID) {
        this.msgID = createMessageID(msgID);
    }

    public MessageID getMsgID() {
        return msgID;
    }

    public void setMsgFlags(int msgFlags) {
        this.msgFlags = msgFlags;
    }

    public int getMsgFlags() {
        return msgFlags;
    }

    public void setMaxSizeResponseScopedPDU(int maxSizeResponseScopedPDU) {
        this.maxSizeResponseScopedPDU = maxSizeResponseScopedPDU;
    }

    public int getMaxSizeResponseScopedPDU() {
        return maxSizeResponseScopedPDU;
    }

    public PduHandle getPduHandle() {
        return pduHandle;
    }

    public byte[] getSecurityEngineID() {
        return securityEngineID;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public TransportMapping<? super A> getTransportMapping() {
        return transportMapping;
    }

    public void setPduHandle(PduHandle pduHandle) {
        this.pduHandle = pduHandle;
        updateRequestStatisticsPduHandle(pduHandle);
    }

    protected void updateRequestStatisticsPduHandle(PduHandle pduHandle) {
        if (pduHandle instanceof RequestStatistics) {
            RequestStatistics requestStatistics = (RequestStatistics) pduHandle;
            requestStatistics.setTotalMessagesSent(1 + ((retryMsgIDs != null) ? retryMsgIDs.size() : 0));
            requestStatistics.setResponseRuntimeNanos(responseRuntimeNanos);
            if (msgID.getID() == matchedMsgID) {
                requestStatistics.setIndexOfMessageResponded(0);
            } else if (retryMsgIDs != null) {
                int index = 1;
                for (Iterator<MessageID> it = retryMsgIDs.iterator(); it.hasNext(); index++) {
                    if (it.next().getID() == matchedMsgID) {
                        requestStatistics.setIndexOfMessageResponded(index);
                        break;
                    }
                }
            }
        }
    }

    public void setSecurityEngineID(byte[] securityEngineID) {
        this.securityEngineID = securityEngineID;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public void setTransportMapping(TransportMapping<? super A> transportMapping) {
        this.transportMapping = transportMapping;
    }

    protected boolean isMatchingMessageID(MessageID msgID) {
        return isMatchingMessageID(msgID.getID());
    }

    public boolean isMatchingMessageID(int msgID) {
        if (this.msgID.getID() == msgID) {
            matchedMsgID = msgID;
            if (this.msgID instanceof TimedMessageID) {
                responseRuntimeNanos = System.nanoTime() - ((TimedMessageID) this.msgID).getCreationNanoTime();
            }
        } else if (retryMsgIDs != null) {
            for (MessageID retryMsgID : retryMsgIDs) {
                if (retryMsgID.getID() == msgID) {
                    matchedMsgID = msgID;
                    if (this.msgID instanceof TimedMessageID) {
                        responseRuntimeNanos = System.nanoTime() - ((TimedMessageID) this.msgID).getCreationNanoTime();
                    }
                    break;
                }
            }
        }
        updateRequestStatisticsPduHandle(pduHandle);
        return (matchedMsgID == msgID);
    }

    public boolean equals(Object o) {
        if (o instanceof StateReference) {
            StateReference<?> other = (StateReference) o;
            return ((isMatchingMessageID(other.msgID) ||
                    ((other.retryMsgIDs != null) && (other.retryMsgIDs.contains(msgID)))) &&
                    equalsExceptMsgID(other));
        }
        return false;
    }

    public boolean equalsExceptMsgID(StateReference<?> other) {
        return (((pduHandle == null) && (other.pduHandle == null)) ||
                (pduHandle != null) && (pduHandle.equals(other.getPduHandle())) &&
                        (Arrays.equals(securityEngineID, other.securityEngineID)) &&
                        (securityModel.equals(other.securityModel)) &&
                        (Arrays.equals(securityName, other.securityName)) &&
                        (securityLevel == other.securityLevel) &&
                        (Arrays.equals(contextEngineID, other.contextEngineID)) &&
                        (Arrays.equals(contextName, other.contextName)));
    }

    public int hashCode() {
        return msgID.getID();
    }

    public String toString() {
        return "StateReference[msgID=" + msgID + ",pduHandle=" + pduHandle +
                ",securityEngineID=" + OctetString.fromByteArray(securityEngineID) +
                ",securityModel=" + securityModel +
                ",securityName=" + OctetString.fromByteArray(securityName) +
                ",securityLevel=" + securityLevel +
                ",contextEngineID=" + OctetString.fromByteArray(contextEngineID) +
                ",contextName=" + OctetString.fromByteArray(contextName) +
                ",retryMsgIDs=" + retryMsgIDs + "]";
    }

    public synchronized void addMessageIDs(List<MessageID> msgIDs) {
        if (retryMsgIDs == null) {
            retryMsgIDs = new ArrayList<MessageID>(msgIDs.size());
        }
        retryMsgIDs.addAll(msgIDs);
    }

    public synchronized List<MessageID> getMessageIDs() {
        List<MessageID> msgIDs = new ArrayList<MessageID>(1 + ((retryMsgIDs != null) ? retryMsgIDs.size() : 0));
        msgIDs.add(msgID);
        if (retryMsgIDs != null) {
            msgIDs.addAll(retryMsgIDs);
        }
        return msgIDs;
    }

    public static MessageID createMessageID(int msgID) {
        if (SNMP4JSettings.getSnmp4jStatistics() == SNMP4JSettings.Snmp4jStatistics.extended) {
            return new TimedMessageID(msgID);
        }
        return new SimpleMessageID(msgID);
    }
}
