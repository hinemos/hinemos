/*_############################################################################
  _## 
  _##  SNMP4J - PDU.java  
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

import org.snmp4j.smi.*;
import org.snmp4j.asn1.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import org.snmp4j.smi.Integer32;
import org.snmp4j.mp.SnmpConstants;
import java.io.Serializable;

/**
 * The {@code PDU} class represents a SNMP protocol data unit. The PDU
 * version supported by the BER decoding and encoding methods of this class
 * is v2.
 * <p>
 * The default PDU type is GET.
 *
 * @author Frank Fock
 * @version 2.0
 * @see PDUv1
 * @see ScopedPDU
 */
public class PDU implements BERSerializable, Serializable {

  private static final long serialVersionUID = 7607672475629607472L;

  /**
   * Denotes a get PDU.
   */
  public static final int GET      = (BER.ASN_CONTEXT | BER.ASN_CONSTRUCTOR);
  /**
   * Denotes a getnext (search) PDU.
   */
  public static final int GETNEXT  = (BER.ASN_CONTEXT | BER.ASN_CONSTRUCTOR | 0x1);
  /**
   * Denotes a response PDU.
   */
  public static final int RESPONSE = (BER.ASN_CONTEXT | BER.ASN_CONSTRUCTOR | 0x2);
  /**
   * Denotes a set PDU.
   */
  public static final int SET      = (BER.ASN_CONTEXT | BER.ASN_CONSTRUCTOR | 0x3);
  /**
   * Denotes a SNMPv1 trap PDU. This type can only be used with instances of the
   * {@link PDUv1} class.
   */
  public static final int V1TRAP   = (BER.ASN_CONTEXT | BER.ASN_CONSTRUCTOR | 0x4);
  /**
   * Denotes a SNMPv2c/v3 getbulk PDU.
   */
  public static final int GETBULK  = (BER.ASN_CONTEXT | BER.ASN_CONSTRUCTOR | 0x5);
  /**
   * Denotes a SNMPv2c/v3 inform PDU (unprecisely also known as a confirmed
   * notification).
   */
  public static final int INFORM   = (BER.ASN_CONTEXT | BER.ASN_CONSTRUCTOR | 0x6);
  /**
   * Denotes a SNMPv2c/v3 notification PDU (undistinguishable from
   * {@code #TRAP}).
   */
  public static final int TRAP     = (BER.ASN_CONTEXT | BER.ASN_CONSTRUCTOR | 0x7);
  /**
   * Denotes a SNMPv2c/v3 notification PDU (undistinguishable from
   * {@code #NOTIFICATION}).
   */
  public static final int NOTIFICATION = TRAP;
  /**
   * Denotes a SNMPv3 report PDU.
   */
  public static final int REPORT   = (BER.ASN_CONTEXT | BER.ASN_CONSTRUCTOR | 0x8);


  // Error status constants

  /**
   * Operation success (no error).
   */
  public static final int noError = SnmpConstants.SNMP_ERROR_SUCCESS;

  /**
   * PDU encoding is too big for the transport used.
   */
  public static final int tooBig = SnmpConstants.SNMP_ERROR_TOO_BIG;

  /**
   * No such variable binding name, see error index.
   */
  public static final int noSuchName = SnmpConstants.SNMP_ERROR_NO_SUCH_NAME;

  /**
   * Bad value in variable binding, see error index.
   */
  public static final int badValue = SnmpConstants.SNMP_ERROR_BAD_VALUE;

  /**
   * The variable binding is read-only, see error index.
   */
  public static final int readOnly = SnmpConstants.SNMP_ERROR_READ_ONLY;

  /**
   * An unspecific error caused by a variable binding, see error index.
   */
  public static final int genErr = SnmpConstants.SNMP_ERROR_GENERAL_ERROR;

  /**
   * The variable binding is not accessible by the current MIB view, see error
   * index.
   */
  public static final int noAccess = SnmpConstants.SNMP_ERROR_NO_ACCESS;

  /**
   * The variable binding's value has the wrong type, see error index.
   */
  public static final int wrongType = SnmpConstants.SNMP_ERROR_WRONG_TYPE;

  /**
   * The variable binding's value has the wrong length, see error index.
   */
  public static final int wrongLength = SnmpConstants.SNMP_ERROR_WRONG_LENGTH;

  /**
   * The variable binding's value has a value that could under no circumstances
   * be assigned, see error index.
   */
  public static final int wrongValue = SnmpConstants.SNMP_ERROR_WRONG_VALUE;

  /**
   * The variable binding's value has the wrong encoding, see error index.
   */
  public static final int wrongEncoding =
      SnmpConstants.SNMP_ERROR_WRONG_ENCODING;

  /**
   * The specified object does not exists and cannot be created,
   * see error index.
   */
  public static final int noCreation = SnmpConstants.SNMP_ERROR_NO_CREATION;

  /**
   * The variable binding's value is presently inconsistent with the current
   * state of the target object, see error index.
   */
  public static final int inconsistentValue =
      SnmpConstants.SNMP_ERROR_INCONSISTENT_VALUE;

  /**
   * The resource needed to assign a variable binding's value is presently
   * unavailable, see error index.
   */
  public static final int resourceUnavailable =
      SnmpConstants.SNMP_ERROR_RESOURCE_UNAVAILABLE;

  /**
   * Unable to commit a value, see error index.
   */
  public static final int commitFailed = SnmpConstants.SNMP_ERROR_COMMIT_FAILED;

  /**
   * Unable to undo a committed value, see error index.
   */
  public static final int undoFailed = SnmpConstants.SNMP_ERROR_UNDO_FAILED;

  /**
   * Unauthorized access, see error index.
   */
  public static final int authorizationError =
      SnmpConstants.SNMP_ERROR_AUTHORIZATION_ERROR;

  /**
   * The variable's value cannot be modified, see error index.
   */
  public static final int notWritable = SnmpConstants.SNMP_ERROR_NOT_WRITEABLE;

  /**
   * The specified object does not exists and presently it cannot be created,
   * see error index.
   */
  public static final int inconsistentName =
      SnmpConstants.SNMP_ERROR_INCONSISTENT_NAME;

  protected Vector<VariableBinding> variableBindings = new Vector<VariableBinding>();
  protected Integer32 errorStatus = new Integer32();
  protected Integer32 errorIndex = new Integer32();
  protected Integer32 requestID = new Integer32();
  protected int type = GET;

  /**
   * Default constructor.
   */
  public PDU() {
  }

  /**
   * Copy constructor which creates a deep copy (clone) of the
   * other PDU.
   * @param other
   *    the {@code PDU} to copy from.
   */
  public PDU(PDU other) {
    variableBindings = new Vector<VariableBinding>(other.size());
    for (VariableBinding vb : other.variableBindings) {
      variableBindings.add((VariableBinding) vb.clone());
    }
    errorIndex = (Integer32) other.errorIndex.clone();
    errorStatus = (Integer32) other.errorStatus.clone();
    type = other.type;
    if (other.requestID != null) {
      requestID = (Integer32) other.requestID.clone();
    }
  }

  /**
   * Constructs a new PDU from a type and a list of {@link VariableBinding} instances.
   * The list will not be referenced, instead a deep copy of the variable bindings
   * is executed (each variable binding will be cloned).
   *
   * @param pduType
   *    the PDU type.
   * @param vbs
   *    the variable bindings.
   * @since 2.2.4
   */
  public PDU(int pduType, List<? extends VariableBinding> vbs) {
    this.type = pduType;
    variableBindings = new Vector<VariableBinding>(vbs.size());
    for (VariableBinding vb : vbs) {
      variableBindings.add((VariableBinding) vb.clone());
    }
  }

  /**
   * Adds a variable binding to this PDU. A {@code NullPointerException}
   * is thrown if {@code VariableBinding} or its {@code Variable} is
   * {@code null}.
   * @param vb
   *   a {@code VariableBinding} instance.
   */
  public void add(VariableBinding vb) {
    variableBindings.add(vb);
  }

  /**
   * Adds a new variable binding to this PDU by using the OID of the supplied
   * {@code VariableBinding}. The value portion is thus set to
   * {@code null}.
   *
   * This method should be used for GET type requests. For SET, TRAP and INFORM
   * requests, the {@link #add} method should be used instead.
   * @param vb
   *   a {@code VariableBinding} instance.
   * @since 1.8
   */
  public void addOID(VariableBinding vb) {
    VariableBinding cvb = new VariableBinding(vb.getOid());
    variableBindings.add(cvb);
  }

  /**
   * Adds an array of variable bindings to this PDU (see
   * {@link #add(VariableBinding vb)}).
   * @param vbs
   *   an array of {@code VariableBinding} instances. The instances in the
   *   array will be appended to the current list of variable bindings in the
   *   PDU.
   */
  public void addAll(VariableBinding[] vbs) {
    variableBindings.ensureCapacity(variableBindings.size()+vbs.length);
    for (VariableBinding vb : vbs) {
      add(vb);
    }
  }

  /**
   * Adds a list of variable bindings to this PDU (see
   * {@link #add(VariableBinding vb)}).
   * @param vbs
   *   a list of {@code VariableBinding} instances. The instances in the
   *   list will be appended to the current list of variable bindings in the
   *   PDU.
   * @since 2.2.4
   */
  public void addAll(List<? extends VariableBinding> vbs) {
    variableBindings.addAll(vbs);
  }

  /**
   * Adds new {@code VariableBindings} each with the OID of the
   * corresponding variable binding of the supplied array to this PDU (see
   * {@link #addOID(VariableBinding vb)}).
   * @param vbs
   *   an array of {@code VariableBinding} instances. For each instance
   *   in the supplied array, a new VariableBinding created by
   *   {@code new VariableBinding(OID)} will be appended to the current
   *   list of variable bindings in the PDU.
   * @since 1.8
   */
  public void addAllOIDs(VariableBinding[] vbs) {
    variableBindings.ensureCapacity(variableBindings.size() + vbs.length);
    for (VariableBinding vb : vbs) {
      addOID(vb);
    }
  }

  /**
   * Gets the variable binding at the specified position.
   * @param index
   *    a zero based positive integer ({@code 0 &lt;= index &lt; {@link #size()}})
   * @return
   *    a VariableBinding instance. If {@code index} is out of bounds
   *    an exception is thrown.
   */
  public VariableBinding get(int index) {
    return variableBindings.get(index);
  }

  /**
   * Gets the first variable whose OID starts with the specified OID.
   * @param prefix
   *    the search {@link OID}.
   * @return
   *    the {@link Variable} of the first {@link VariableBinding}
   *    whose prefix matches {@code oid}. If no such element
   *    could be found, {@code null} is returned.
   * @since 2.0
   */
  public Variable getVariable(OID prefix) {
    for (VariableBinding vb : variableBindings) {
      if (vb.getOid().startsWith(prefix)) {
        return vb.getVariable();
      }
    }
    return null;
  }

  /**
   * Gets a list of {@link VariableBinding}s whose OID prefix
   * matches the supplied prefix.
   * @param prefix
   *    the search {@link OID}.
   * @return
   *    a List of all {@link VariableBinding}s
   *    whose prefix matches {@code oid}. If no such element
   *    could be found, an empty List is returned.
   */
  public List<VariableBinding> getBindingList(OID prefix) {
    List<VariableBinding> list = new ArrayList<VariableBinding>(variableBindings.size());
    for (VariableBinding vb : variableBindings) {
      if (vb.getOid().startsWith(prefix)) {
        list.add(vb);
      }
    }
    return list;
  }

  /**
   * Sets the variable binding at the specified position.
   * @param index
   *    a zero based positive integer ({@code 0 &lt;= index &lt;} {@link #size()})
   *    If {@code index} is out of bounds
   *    an exception is thrown.
   * @param vb
   *    a VariableBinding instance ({@code null} is not allowed).
   * @return
   *    the variable binding that has been replaced.
   */
  public VariableBinding set(int index, VariableBinding vb) {
    if (vb == null) {
      throw new NullPointerException("Variable binding must not be null");
    }
    return variableBindings.set(index, vb);
  }

  /**
   * Removes the variable binding at the supplied position.
   * @param index
   *    a position &gt;= 0 and &lt; {@link #size()}.
   */
  public void remove(int index) {
    variableBindings.remove(index);
  }

  /**
   * Gets the number of variable bindings in the PDU.
   * @return
   *    the size of the PDU.
   */
  public int size() {
    return variableBindings.size();
  }

  /**
   * Gets the variable binding vector.
   * @return
   *    the internal {@code Vector} containing the PDU's variable bindings.
   */
  public Vector<? extends VariableBinding> getVariableBindings() {
    return variableBindings;
  }

  /**
   * Sets the {@link VariableBinding}s for this PDU.
   * @param vbs
   *    a list of {@link VariableBinding} instances which must
   *    not be null.
   * @since 2.1
   */
  public void setVariableBindings(List<? extends VariableBinding> vbs) {
    if (vbs == null) {
      throw new NullPointerException();
    }
    this.variableBindings = new Vector<VariableBinding>(vbs);
  }

  /**
   * Remove the last variable binding from the PDU, if such an element exists.
   */
  public void trim() {
    if (variableBindings.size() > 0) {
      variableBindings.remove(variableBindings.size() - 1);
    }
  }

  /**
   * Sets the error status of the PDU.
   * @param errorStatus
   *    a SNMP error status.
   * @see SnmpConstants
   */
  public void setErrorStatus(int errorStatus) {
    this.errorStatus.setValue(errorStatus);
  }

  /**
   * Gets the error status of the PDU.
   * @return
   *    a SNMP error status.
   * @see SnmpConstants
   */
  public int getErrorStatus() {
    return errorStatus.getValue();
  }

  /**
   * Gets a textual description of the error status.
   * @return
   *    a String containing an element of the
   *    {@link SnmpConstants#SNMP_ERROR_MESSAGES} array for a valid error status.
   *    "Unknown error: &lt;errorStatusNumber&gt;" is returned for any other value.
   */
  public String getErrorStatusText() {
    return toErrorStatusText(errorStatus.getValue());
  }

  /**
   * Returns textual description for the supplied error status value.
   * @param errorStatus
   *    an error status.
   * @return
   *    a String containing an element of the
   *    {@link SnmpConstants#SNMP_ERROR_MESSAGES} array for a valid error status.
   *    "Unknown error: &lt;errorStatusNumber&gt;" is returned for any other value.
   * @since 1.7
   */
  public static String toErrorStatusText(int errorStatus) {
    try {
      if (errorStatus < 0) {
        return SnmpConstants.SNMP_TP_ERROR_MESSAGES[Math.abs(errorStatus)-1];
      }
      return SnmpConstants.SNMP_ERROR_MESSAGES[errorStatus];
    }
    catch (ArrayIndexOutOfBoundsException iobex) {
      return "Unknown error: "+errorStatus;
    }
  }

  /**
   * Sets the error index.
   * @param errorIndex
   *    an integer value &gt;= 0 where 1 denotes the first variable binding.
   */
  public void setErrorIndex(int errorIndex) {
    this.errorIndex.setValue(errorIndex);
  }

  /**
   * Gets the error index.
   * @return
   *   an integer value &gt;= 0 where 1 denotes the first variable binding.
   */
  public int getErrorIndex() {
    return errorIndex.getValue();
  }

  /**
   * Checks whether this PDU is a confirmed class PDU.
   * @return boolean
   */
  public boolean isConfirmedPdu() {
    return ((type != PDU.REPORT) && (type != PDU.RESPONSE) &&
            (type != PDU.TRAP) && (type != PDU.V1TRAP));
  }

  /**
   * Checks whether this PDU is a {@link PDU#RESPONSE} or [@link PDU#REPORT}.
   * @return
   *    {@code true} if {@link #getType()} returns {@link PDU#RESPONSE} or [@link PDU#REPORT} and
   *    {@code false} otherwise.
   * @since 2.4.1
   */
  public boolean isResponsePdu() {
    return ((type == PDU.RESPONSE) || (type == PDU.REPORT));
  }

  public int getBERLength() {
    // header for data_pdu
    int length = getBERPayloadLengthPDU();
    length += BER.getBERLengthOfLength(length) + 1;
    // assume maximum length here
    return length;
  }

  public int getBERPayloadLength() {
    return getBERPayloadLengthPDU();
  }

  public void decodeBER(BERInputStream inputStream) throws IOException {
    BER.MutableByte pduType = new BER.MutableByte();
    int length = BER.decodeHeader(inputStream, pduType);
    int pduStartPos = (int)inputStream.getPosition();
    switch (pduType.getValue()) {
      case PDU.SET:
      case PDU.GET:
      case PDU.GETNEXT:
      case PDU.GETBULK:
      case PDU.INFORM:
      case PDU.REPORT:
      case PDU.TRAP:
      case PDU.RESPONSE:
        break;
      default:
        throw new IOException("Unsupported PDU type: "+pduType.getValue());
    }
    this.type = pduType.getValue();
    requestID.decodeBER(inputStream);
    errorStatus.decodeBER(inputStream);
    errorIndex.decodeBER(inputStream);

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
      variableBindings.add(vb);
    }
    if (inputStream.getPosition() - startPos != vbLength) {
      throw new IOException("Length of VB sequence ("+vbLength+
                            ") does not match real length: "+
                            ((int)inputStream.getPosition()-startPos));
    }
    if (BER.isCheckSequenceLength()) {
      BER.checkSequenceLength(length,
                              (int) inputStream.getPosition() - pduStartPos,
                              this);
    }
  }

  /**
   * Computes the length in bytes of the BER encoded variable bindings without
   * including the length of BER sequence length.
   * @param variableBindings
   *    a list of variable bindings.
   * @return
   *    the length in bytes of the BER encoded VB list.
   */
  public static int getBERLength(List<? extends VariableBinding> variableBindings) {
    int length = 0;
    // length for all vbs
    for (VariableBinding variableBinding : variableBindings) {
      length += variableBinding.getBERLength();
    }
    return length;
  }

  protected int getBERPayloadLengthPDU() {
    int length = getBERLength(variableBindings);
    length += BER.getBERLengthOfLength(length) + 1;

    // req id, error status, error index
    Integer32 i32 = new Integer32(requestID.getValue());
    length += i32.getBERLength();
    i32 = errorStatus;
    length += i32.getBERLength();
    i32 = errorIndex;
    length += i32.getBERLength();
    i32 = null;
    return length;
  }

  public void encodeBER(OutputStream outputStream) throws IOException {
    BER.encodeHeader(outputStream, type, getBERPayloadLengthPDU());

    requestID.encodeBER(outputStream);
    errorStatus.encodeBER(outputStream);
    errorIndex.encodeBER(outputStream);

    int vbLength = 0;
    for (VariableBinding vb : variableBindings) {
      vbLength += vb.getBERLength();
    }
    BER.encodeHeader(outputStream, BER.SEQUENCE, vbLength);
    for (VariableBinding vb : variableBindings) {
      vb.encodeBER(outputStream);
    }
  }

  /**
   * Removes all variable bindings from the PDU and sets the request ID to zero.
   * This can be used to reuse a PDU for another request.
   */
  public void clear() {
    variableBindings.clear();
    setRequestID(new Integer32(0));
  }

  /**
   * Sets the PDU type.
   * @param type
   *    the type of the PDU (e.g. GETNEXT, SET, etc.)
   */
  public void setType(int type) {
    this.type = type;
  }

  /**
   * Gets the PDU type. The default is {@link PDU#GETNEXT}.
   * @return
   *    the PDU's type.
   */
  public int getType() {
    return type;
  }

  public Object clone() {
    return new PDU(this);
  }

  /**
   * Gets the request ID associated with this PDU.
   * @return
   *    an {@code Integer32} instance.
   */
  public Integer32 getRequestID() {
    return requestID;
  }

  /**
   * Sets the request ID for this PDU. When the request ID is not set or set to
   * zero, the message processing model will generate a unique request ID for
   * the {@code PDU} when sent.
   * @param requestID
   *    a unique request ID.
   */
  public void setRequestID(Integer32 requestID) {
    this.requestID = requestID;
  }

  /**
   * Gets a string representation of the supplied PDU type.
   * @param type
   *    a PDU type.
   * @return
   *    a string representation of {@code type}, for example "GET".
   */
  public static String getTypeString(int type) {
    switch (type) {
      case PDU.GET:
        return "GET";
      case PDU.SET:
        return "SET";
      case PDU.GETNEXT:
        return "GETNEXT";
      case PDU.GETBULK:
        return "GETBULK";
      case PDU.INFORM:
        return "INFORM";
      case PDU.RESPONSE:
        return "RESPONSE";
      case PDU.REPORT:
        return "REPORT";
      case PDU.TRAP:
        return "TRAP";
      case PDU.V1TRAP:
        return "V1TRAP";
    }
    return "unknown";
  }

  /**
   * Gets the PDU type identifier for a string representation of the type.
   * @param type
   *    the string representation of a PDU type: {@code GET, GETNEXT, GETBULK,
   *    SET, INFORM, RESPONSE, REPORT, TRAP, V1TRAP)}.
   * @return
   *    the corresponding PDU type constant, or {@code Integer.MIN_VALUE}
   *    of the supplied type is unknown.
   */
  public static int getTypeFromString(String type) {
    if (type.equals("GET")) {
      return PDU.GET;
    }
    else if (type.equals("SET")) {
      return PDU.SET;
    }
    else if (type.equals("GETNEXT")) {
      return PDU.GETNEXT;
    }
    else if (type.equals("GETBULK")) {
      return PDU.GETBULK;
    }
    else if (type.equals("INFORM")) {
      return PDU.INFORM;
    }
    else if (type.equals("RESPONSE")) {
      return PDU.RESPONSE;
    }
    else if (type.equals("TRAP")) {
      return PDU.TRAP;
    }
    else if (type.equals("V1TRAP")) {
      return PDU.V1TRAP;
    }
    else if (type.equals("REPORT")) {
      return PDU.REPORT;
    }
    return Integer.MIN_VALUE;
  }

  /**
   * Returns a string representation of the object.
   *
   * @return a string representation of the object.
   */
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append(getTypeString(type));
    buf.append("[requestID=");
    buf.append(requestID);
    buf.append(", errorStatus=");
    buf.append(getErrorStatusText()).append("(").append(errorStatus).append(")");
    buf.append(", errorIndex=");
    buf.append(errorIndex);
    buf.append(", VBS[");
    for (int i=0; i<variableBindings.size(); i++) {
      buf.append(variableBindings.get(i));
      if (i+1 < variableBindings.size()) {
        buf.append("; ");
      }
    }
    buf.append("]]");
    return buf.toString();
  }

  /**
   * Gets the maximum repetitions of repeatable variable bindings in GETBULK
   * requests.
   * @return
   *    an integer value &gt;= 0.
   */
  public int getMaxRepetitions() {
    return errorIndex.getValue();
  }

  /**
   * Sets the maximum repetitions of repeatable variable bindings in GETBULK
   * requests.
   * @param maxRepetitions
   *    an integer value &gt;= 0.
   */
  public void setMaxRepetitions(int maxRepetitions) {
    this.errorIndex.setValue(maxRepetitions);
  }

  /**
   * Gets the number of non repeater variable bindings in a GETBULK PDU.
   * @return
   *    an integer value &gt;= 0 and &lt;= {@link #size()}
   */
  public int getNonRepeaters() {
    return errorStatus.getValue();
  }

  /**
   * Sets the number of non repeater variable bindings in a GETBULK PDU.
   * @param nonRepeaters
   *    an integer value &gt;= 0 and &lt;= {@link #size()}
   */
  public void setNonRepeaters(int nonRepeaters) {
    this.errorStatus.setValue(nonRepeaters);
  }

  /**
   * Returns an array with the variable bindings of this PDU.
   * @return
   *    an array of {@code VariableBinding} instances of this PDU in the
   *    same order as in the PDU.
   */
  public VariableBinding[] toArray() {
    VariableBinding[] vbs = new VariableBinding[this.variableBindings.size()];
    this.variableBindings.toArray(vbs);
    return vbs;
  }

  @Override
  public int hashCode() {
    // Returning the hasCode() of the request ID is not a good idea, as
    // this might change during sending a request.
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PDU) {
      PDU o = (PDU) obj;
      return (type == o.type) &&
          (AbstractVariable.equal(requestID, o.requestID)) &&
          (AbstractVariable.equal(errorStatus,o.errorStatus)) &&
          (AbstractVariable.equal(errorIndex, o.errorIndex)) &&
          variableBindings.equals(o.variableBindings);
    }
    return false;
  }
}

