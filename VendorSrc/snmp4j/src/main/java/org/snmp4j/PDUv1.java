/*_############################################################################
  _## 
  _##  SNMP4J - PDUv1.java  
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

import java.io.*;
import java.util.*;
import org.snmp4j.asn1.*;
import org.snmp4j.asn1.BER.*;
import org.snmp4j.smi.*;
import org.snmp4j.smi.OID;
// for JavaDoc
import org.snmp4j.mp.SnmpConstants;

/**
 * The {@code PDUv1} represents SNMPv1 PDUs. The behavior of this class
 * is identical to its superclass {@link PDU} for the PDU type {@link PDU#GET},
 * {@link PDU#GETNEXT}, and {@link PDU#SET}. The other SNMPv2 PDU types
 * implemented by {@code PDU} are not supported. In contrast to its super
 * class, {@code PDUv1} implements the {@link PDU#V1TRAP} type.
 *
 * To support this type, access methods are provided to get and set the
 * enterprise {@code OID}, generic, specific, and timestamp of a SNMPv1
 * trap PDU.
 *
 * The constants defined for generic SNMPv1 traps are included in this class.
 * The descriptions are taken from the SNMPv2-MIB (RFC 3418). The corresponding
 * OIDs are defined in {@link SnmpConstants}.
 *
 * @author Frank Fock
 * @version 1.7.3
 */
public class PDUv1 extends PDU {

  private static final long serialVersionUID = -6478805117911347898L;

  /**
   * A coldStart(0) trap signifies that the SNMP entity,
   * supporting a notification originator application, is
   * reinitializing itself and that its configuration may
   * have been altered.
   */
  public static final int COLDSTART = 0;

  /**
   * A warmStart(1) trap signifies that the SNMP entity,
   * supporting a notification originator application,
   * is reinitializing itself such that its configuration
   * is unaltered.
   */
  public static final int WARMSTART = 1;

  /**
   * A linkDown(2) trap signifies that the SNMP entity, acting in
   * an agent role, has detected that the ifOperStatus object for
   * one of its communication links is about to enter the down
   * state from some other state (but not from the notPresent
   * state).  This other state is indicated by the included value
   * of ifOperStatus.
   */
  public static final int LINKDOWN = 2;

  /**
   * A linkUp(3) trap signifies that the SNMP entity, acting in an
   * agent role, has detected that the ifOperStatus object for
   * one of its communication links left the down state and
   * transitioned into some other state (but not into the
   * notPresent state).  This other state is indicated by the
   * included value of ifOperStatus.
   */
  public static final int LINKUP = 3;

  /**
   * An authenticationFailure(4) trap signifies that the SNMP
   * entity has received a protocol message that is not
   * properly authenticated.  While all implementations
   * of SNMP entities MAY be capable of generating this
   * trap, the snmpEnableAuthenTraps object indicates
   * whether this trap will be generated.
   */
  public static final int AUTHENTICATIONFAILURE = 4;

  /**
   * If the generic trap identifier is {@code ENTERPRISE_SPECIFIC}(6), then
   * the enterprise specific trap ID is given by the specificTrap member field.
   */
  public static final int ENTERPRISE_SPECIFIC = 6;

  private static final String OPERATION_NOT_SUPPORTED =
      "Operation not supported for SNMPv1 PDUs";

  private OID enterprise = new OID();
  private IpAddress agentAddress = new IpAddress("0.0.0.0");
  private Integer32 genericTrap = new Integer32(0);
  private Integer32 specificTrap = new Integer32(0);
  private TimeTicks timestamp = new TimeTicks(0);


  public PDUv1() {
    setType(V1TRAP);
  }

  /**
   * Copy constructor.
   * @param other
   *    the {@code PDUv1} to copy from.
   * @since 1.9.1c
   */
  public PDUv1(PDUv1 other) {
    super(other);
    enterprise = (OID) other.enterprise.clone();
    agentAddress = (IpAddress) other.agentAddress.clone();
    genericTrap = (Integer32) other.genericTrap.clone();
    specificTrap = (Integer32) other.specificTrap.clone();
    timestamp = (TimeTicks) other.timestamp.clone();
  }

  public Object clone() {
    return new PDUv1(this);
  }

  /**
   * Decodes a {@code Variable} from an {@link BERInputStream}.
   *
   * @param inputStream an {@code InputStream} containing a BER encoded
   *   byte stream.
   * @throws IOException
   *   if there is an encoding error in the BER stream.
   */
  public void decodeBER(BERInputStream inputStream) throws IOException {
    MutableByte pduType = new MutableByte();
    int length = BER.decodeHeader(inputStream, pduType);
    int pduStartPos = (int)inputStream.getPosition();

    switch (pduType.getValue()) {
      case PDU.SET:
      case PDU.GET:
      case PDU.GETNEXT:
      case PDU.V1TRAP:
      case PDU.RESPONSE:
        break;
      // The following PDU types are not supported by the SNMPv1 standard!
      case PDU.NOTIFICATION:
      case PDU.INFORM:
        if (SNMP4JSettings.isAllowSNMPv2InV1()) {
          break;
        }
        // fall through
      default:
        throw new IOException("Unsupported PDU type: "+pduType.getValue());
    }
    this.setType(pduType.getValue());
    if (getType() == PDU.V1TRAP) {
      enterprise.decodeBER(inputStream);
      agentAddress.decodeBER(inputStream);
      genericTrap.decodeBER(inputStream);
      specificTrap.decodeBER(inputStream);
      timestamp.decodeBER(inputStream);
    }
    else {
      requestID.decodeBER(inputStream);
      errorStatus.decodeBER(inputStream);
      errorIndex.decodeBER(inputStream);
    }
    // reusing pduType here to save memory ;-)
    pduType = new BER.MutableByte();
    int vbLength = BER.decodeHeader(inputStream, pduType);
    if (pduType.getValue() != BER.SEQUENCE) {
      throw new IOException("Encountered invalid tag, SEQUENCE expected: "+
                            pduType.getValue());
    }
    // rest read count
    int startPos = (int)inputStream.getPosition();
    variableBindings = new Vector<VariableBinding>();
    while (inputStream.getPosition() - startPos < vbLength) {
      VariableBinding vb = new VariableBinding();
      vb.decodeBER(inputStream);
      if (!isVariableV1(vb.getVariable())) {
        throw new MessageException("Counter64 encountered in SNMPv1 PDU "+
                                   "(RFC 2576 §4.1.2.1)");
      }
      variableBindings.add(vb);
    }
    if (BER.isCheckSequenceLength()) {
      BER.checkSequenceLength(vbLength,
                              (int) inputStream.getPosition() - startPos, this);
      BER.checkSequenceLength(length,
                              (int) inputStream.getPosition() - pduStartPos, this);
    }
  }

  /**
   * Encodes a {@code Variable} to an {@code OutputStream}.
   *
   * @param outputStream an {@code OutputStream}.
   * @throws IOException if an error occurs while writing to the stream.
   */
  public void encodeBER(OutputStream outputStream) throws IOException {
    BER.encodeHeader(outputStream, type, getBERPayloadLength());

    if (type == PDU.V1TRAP) {
      enterprise.encodeBER(outputStream);
      agentAddress.encodeBER(outputStream);
      genericTrap.encodeBER(outputStream);
      specificTrap.encodeBER(outputStream);
      timestamp.encodeBER(outputStream);
    }
    else {
      requestID.encodeBER(outputStream);
      errorStatus.encodeBER(outputStream);
      errorIndex.encodeBER(outputStream);
    }
    int vbLength = 0;
      for (VariableBinding variableBinding : variableBindings) {
          vbLength += variableBinding.getBERLength();
      }
    BER.encodeHeader(outputStream, BER.SEQUENCE, vbLength);
      for (VariableBinding vb : variableBindings) {
          if (!isVariableV1(vb.getVariable())) {
              throw new IOException("Cannot encode Counter64 into a SNMPv1 PDU");
          }
          vb.encodeBER(outputStream);
      }
  }

  /**
   * Check if the given variable can be encoded into a SNMPv1 PDU.
   * @param v
   *    a variable value (must not be {@code null}).
   * @return
   *    {@code true} if the variable is SNMPv1 compatible (or
   *    {@link org.snmp4j.SNMP4JSettings#isAllowSNMPv2InV1()} is true),
   *    {@code false} otherwise, i.e. if {@code v} is an instance of
   *    {@link Counter64}.
   * @since 1.9.1c
   */
  protected boolean isVariableV1(Variable v) {
    return !(v instanceof Counter64) || SNMP4JSettings.isAllowSNMPv2InV1();
  }

  protected int getBERPayloadLengthPDU() {
    if (getType() != PDU.V1TRAP) {
      return super.getBERPayloadLengthPDU();
    }
    else {
      int length = 0;
      // length for all vbs
        for (VariableBinding variableBinding : variableBindings) {
            length += variableBinding.getBERLength();
        }
      length += BER.getBERLengthOfLength(length) + 1;
      length += agentAddress.getBERLength();
      length += enterprise.getBERLength();
      length += genericTrap.getBERLength();
      length += specificTrap.getBERLength();
      length += timestamp.getBERLength();
      return length;
    }
  }

  /**
   * This method is not supported for SNMPv1 PDUs and will throw a
   * {@link java.lang.UnsupportedOperationException}
   * @return
   *    nothing
   * @throws UnsupportedOperationException
   *   is always thrown.
   */
  public int getMaxRepetitions() {
    throw new UnsupportedOperationException(OPERATION_NOT_SUPPORTED);
  }

  /**
   * This method is not supported for SNMPv1 PDUs and will throw a
   * {@link java.lang.UnsupportedOperationException}
   * @param maxRepetitions the number of repetitions for SNMPv2c or later SNMP version. Ignored by this PDUv1.
   * @throws UnsupportedOperationException
   *   is always thrown.
   */
  public void setMaxRepetitions(int maxRepetitions) {
    throw new UnsupportedOperationException(OPERATION_NOT_SUPPORTED);
  }

  /**
   * This method is not supported for SNMPv1 PDUs and will throw a
   * {@link java.lang.UnsupportedOperationException}
   *
   * @param maxSizeScopedPDU int
   * @throws UnsupportedOperationException
   *   is always thrown.
   */
  public void setMaxSizeScopedPDU(int maxSizeScopedPDU) {
    throw new UnsupportedOperationException(OPERATION_NOT_SUPPORTED);
  }

  /**
   * This method is not supported for SNMPv1 PDUs and will throw a
   * {@link java.lang.UnsupportedOperationException}
   *
   * @param nonRepeaters int
   * @throws UnsupportedOperationException
   *   is always thrown.
   */
  public void setNonRepeaters(int nonRepeaters) {
    throw new UnsupportedOperationException(OPERATION_NOT_SUPPORTED);
  }

  private void checkV1TRAP() {
    if (getType() != PDU.V1TRAP) {
      throw new UnsupportedOperationException(
          "Operation is only supported for SNMPv1 trap PDUs (V1TRAP)");
    }
  }

  /**
   * Gets the "enterprise" OID of the SNMPv1 trap. The enterprise OID could be
   * any OID although the name could lead to the assumption that the
   * enterprise OID has to be an OID under the
   * iso(1).org(3).dod(6).internet(1).private(4).enterprises(1) node, but that's
   * not true.
   *
   * @return
   *    an OID instance.
   * @throws UnsupportedOperationException if the type of this PDU is not
   *    {@link PDU#V1TRAP}.
   */
  public OID getEnterprise() {
    checkV1TRAP();
    return enterprise;
  }

  /**
   * Sets the "enterprise" OID of the SNMPv1 trap. The enterprise OID could be
   * any OID although the name could lead to the assumption that the
   * enterprise OID has to be an OID under the
   * iso(1).org(3).dod(6).internet(1).private(4).enterprises(1) node, but that's
   * not true.
   *
   * @param enterprise
   *    an OID instance.
   * @throws UnsupportedOperationException if the type of this PDU is not
   *    {@link PDU#V1TRAP}.
   */
  public void setEnterprise(org.snmp4j.smi.OID enterprise) {
    checkV1TRAP();
    checkNull(enterprise);
    this.enterprise = (OID) enterprise.clone();
  }

  /**
   * Gets the IP address of the originator system of this SNMPv1 trap.
   * If this value is 0.0.0.0 (the recommended default), then the address
   * of the peer SNMP entity should be extracted from the {@link Target}
   * object associated with this PDU.
   *
   * @return
   *    an IpAddress instance.
   * @throws UnsupportedOperationException if the type of this PDU is not
   *    {@link PDU#V1TRAP}.
   */
  public org.snmp4j.smi.IpAddress getAgentAddress() {
    checkV1TRAP();
    return agentAddress;
  }

  /**
   * Sets the IP address of the originator system of this SNMPv1 trap.
   * The default value is 0.0.0.0, which should be only overriden in special
   * cases, for example when forwarding SNMPv1 traps through a SNMP proxy.
   * @param agentAddress
   *    a {@code IpAddress} instance.
   * @throws UnsupportedOperationException if the type of this PDU is not
   *    {@link PDU#V1TRAP}.
   */
  public void setAgentAddress(org.snmp4j.smi.IpAddress agentAddress) {
    checkV1TRAP();
    checkNull(agentAddress);
    this.agentAddress = agentAddress;
  }

  /**
   * Gets the generic trap ID. If this value is ENTERPRISE_SPECIFIC(6), then
   * {@link #getSpecificTrap()} will return the trap ID of the enterprise
   * specific trap.
   * @return
   *    an Integer32 instance with a value between 0 and 6.
   * @throws UnsupportedOperationException if the type of this PDU is not
   *    {@link PDU#V1TRAP}.
   */
  public int getGenericTrap() {
    checkV1TRAP();
    return genericTrap.getValue();
  }

  /**
   * Sets the generic trap ID. If this value is ENTERPRISE_SPECIFIC(6), then
   * {@link #setSpecificTrap} must be used to set the trap ID of the enterprise
   * specific trap.
   * @param genericTrap
   *    an integer value &gt;= 0 and &lt;= 6.
   * @throws UnsupportedOperationException if the type of this PDU is not
   *    {@link PDU#V1TRAP}.
   */
  public void setGenericTrap(int genericTrap) {
    checkV1TRAP();
    this.genericTrap.setValue(genericTrap);
  }

  /**
   * Gets the specific trap ID. If this value is set,
   * {@link #getGenericTrap()} must return ENTERPRISE_SPECIFIC(6).
   * @return
   *    an integer value &gt; 0.
   * @throws UnsupportedOperationException if the type of this PDU is not
   *    {@link PDU#V1TRAP}.
   */
  public int getSpecificTrap() {
    checkV1TRAP();
    return specificTrap.getValue();
  }
  /**
   * Sets the specific trap ID. If this value is set,
   * {@link #setGenericTrap(int genericTrap)} must be called with value
   * {@link #ENTERPRISE_SPECIFIC}.
   *
   * @param specificTrap
   *    an integer value &gt; 0.
   * @throws UnsupportedOperationException if the type of this PDU is not
   *    {@link PDU#V1TRAP}.
   */
  public void setSpecificTrap(int specificTrap) {
    checkV1TRAP();
    this.specificTrap.setValue(specificTrap);
  }

  /**
   * Gets the {@code TimeTicks} value of the trap sender's notion of
   * its sysUpTime value when this trap has been generated.
   *
   * @return
   *    a long value.
   * @throws UnsupportedOperationException if the type of this PDU is not
   *    {@link PDU#V1TRAP}.
   */
  public long getTimestamp() {
    checkV1TRAP();
    return timestamp.getValue();
  }

  /**
   * Sets the {@code TimeTicks} value of the trap sender's notion of
   * its sysUpTime value when this trap has been generated.
   *
   * @param timeStamp
   *    a long value.
   */
  public void setTimestamp(long timeStamp) {
    checkV1TRAP();
    this.timestamp.setValue(timeStamp);
  }

  /**
   * Checks for null parameters.
   * @param parameter
   *    an Object instance.
   * @throws NullPointerException if {@code parameter} is null.
   */
  protected void checkNull(Variable parameter) {
    if (parameter == null) {
      throw new NullPointerException("Members of PDUv1 must not be null");
    }
  }

  public String toString() {
    if (type == PDU.V1TRAP) {
      StringBuilder buf = new StringBuilder();
      buf.append(getTypeString(type));
      buf.append("[reqestID=");
      buf.append(requestID);
      buf.append(",timestamp=");
      buf.append(timestamp);
      buf.append(",enterprise=");
      buf.append(enterprise);
      buf.append(",genericTrap=");
      buf.append(genericTrap);
      buf.append(",specificTrap=");
      buf.append(specificTrap);
      buf.append(", VBS[");
      for (int i = 0; i < variableBindings.size(); i++) {
        buf.append(variableBindings.get(i));
        if (i + 1 < variableBindings.size()) {
          buf.append("; ");
        }
      }
      buf.append("]]");
      return buf.toString();
    }
    return super.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PDUv1) {
      PDUv1 o = (PDUv1)obj;
      return super.equals(obj) &&
        AbstractVariable.equal(enterprise, o.enterprise) &&
        AbstractVariable.equal(agentAddress, o.agentAddress) &&
        AbstractVariable.equal(genericTrap, o.genericTrap) &&
        AbstractVariable.equal(specificTrap, o.specificTrap) &&
        AbstractVariable.equal(timestamp, o.timestamp);
    }
    return super.equals(obj);    //To change body of overridden methods use File | Settings | File Templates.
  }
}
