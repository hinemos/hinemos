/*_############################################################################
  _## 
  _##  SNMP4J - VariableBinding.java  
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

import java.io.Serializable;
import org.snmp4j.asn1.*;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;

import org.snmp4j.SNMP4JSettings;
import org.snmp4j.util.VariableTextFormat;

/**
 * A <code>VariableBinding</code> is an association of a object instance
 * identifier ({@link OID}) and the instance's value ({@link Variable}).
 *
 * @author Frank Fock
 * @version 1.9
 */
public class VariableBinding
    implements Serializable, BERSerializable, Cloneable {

  private static final long serialVersionUID = 1032709950031514113L;

  private OID oid;
  private Variable variable;


  /**
   * Creates a variable binding with a zero length OID and a {@link Null} value.
   */
  public VariableBinding() {
    oid = new OID();
    this.variable = Null.instance;
  }

  /**
   * Creates a variable binding with the supplied object instance identifier
   * and a {@link Null} value.
   * @param oid
   *    the OID for the new variable binding.
   */
  public VariableBinding(OID oid) {
    setOid(oid);
    this.variable = Null.instance;
  }

  /**
   * Creates a variable binding with the supplied OID and value.
   * @param oid
   *    the OID for the new variable binding (must not be <code>null</code>).
   * @param variable
   *    the value for the new variable binding (must not be <code>null</code>).
   */
  public VariableBinding(OID oid, Variable variable) {
    setOid(oid);
    setVariable(variable);
  }

  /**
   * Creates a variable binding with the supplied OID and a text value.
   * The text value is parsed based on MIB information to a Variable
   * using the {@link VariableTextFormat} set by
   * {@link SNMP4JSettings#setVariableTextFormat(org.snmp4j.util.VariableTextFormat)}.
   * The default {@link org.snmp4j.util.SimpleVariableTextFormat} does not support
   * this operation. To be able to use this constructor, use the <code>SmiManager</code>
   * of SNMP4J-SMI instead.
   *
   * @param oid
   *    the {@link OID} of the new variable binding.
   * @param variableText
   *    the textual representation of a {@link Variable} of the syntax defined for <code>oid</code>
   *    by a MIB specification.
   * @throws ParseException
   *    if the <code>variableText</code> cannot be parsed or <code>oid</code>'s syntax is unknown.
   * @since 2.2
   */
  public VariableBinding(OID oid, String variableText) throws ParseException {
    this(oid, SNMP4JSettings.getVariableTextFormat().parse(oid, variableText));
  }

  /**
   * Gets the object instance identifier of the variable binding.
   * @return
   *    an <code>OID</code>.
   */
  public OID getOid() {
    return oid;
  }

  /**
   * Sets the object instance identifier for the variable binding.
   * @param oid
   *    an OID (must not be <code>null</code>) that is cloned when added to
   *    this binding.
   */
  public void setOid(OID oid) {
    if (oid == null) {
      throw new IllegalArgumentException(
          "OID of a VariableBinding must not be null");
    }
    this.oid = (OID) oid.clone();
  }

  /**
   * Sets the value of the variable binding.
   *
   * @param variable
   *    a <code>Variable</code> (must not be <code>null</code>) that is cloned
   *    when added to this binding.
   */
  public void setVariable(Variable variable) {
    if (variable == null) {
      throw new IllegalArgumentException(
          "Variable of a VariableBinding must not be null");
    }
    this.variable = (Variable) variable.clone();
  }

  /**
   * Gets the value of the variable binding.
   * @return
   *   a <code>Variable</code> instance.
   */
  public Variable getVariable() {
    return variable;
  }

  /**
   * Gets the syntax of the variable bindings value.
   * @return
   *   a SMI syntax identifier (see {@link SMIConstants}).
   */
  public final int getSyntax() {
    return variable.getSyntax();
  }

  /**
   * Returns whether the variable bindings value has an exception syntax.
   * @see Variable
   * @return
   *    <code>true</code> if the syntax of this variable is an instance of
   *    <code>Null</code> and its syntax equals one of the following:
   *    <UL>
   *    <LI>{@link SMIConstants#EXCEPTION_NO_SUCH_OBJECT}</LI>
   *    <LI>{@link SMIConstants#EXCEPTION_NO_SUCH_INSTANCE}</LI>
   *    <LI>{@link SMIConstants#EXCEPTION_END_OF_MIB_VIEW}</LI>
   *    </UL>
   */
  public boolean isException() {
    return variable.isException();
  }

  public final int getBERPayloadLength() {
    return oid.getBERLength() + variable.getBERLength();
  }

  public final int getBERLength() {
    int length = getBERPayloadLength();
    // add type byte and length of length
    length += BER.getBERLengthOfLength(length) + 1;
    return length;
  }

  public final void decodeBER(BERInputStream inputStream) throws IOException {
    BER.MutableByte type = new BER.MutableByte();
    int length = BER.decodeHeader(inputStream, type);
    long startPos = inputStream.getPosition();
    if (type.getValue() != BER.SEQUENCE) {
      throw new IOException("Invalid sequence encoding: " + type.getValue());
    }
    oid.decodeBER(inputStream);
    variable = AbstractVariable.createFromBER(inputStream);
    if (BER.isCheckSequenceLength()) {
      BER.checkSequenceLength(length,
                              (int) (inputStream.getPosition() - startPos),
                              this);
    }
  }

  public final void encodeBER(OutputStream outputStream) throws IOException {
    int length = getBERPayloadLength();
    BER.encodeHeader(outputStream, BER.SEQUENCE,
                     length);
    oid.encodeBER(outputStream);
    variable.encodeBER(outputStream);
  }

  /**
   * Gets a string representation of this variable binding using the
   * {@link VariableTextFormat} configured by {@link SNMP4JSettings}.
   * @return
   *    a string of the form <code>&lt;OID&gt; = &lt;Variable&gt;</code>.
   */
  public String toString() {
    VariableTextFormat varFormat = SNMP4JSettings.getVariableTextFormat();
    return varFormat.format(oid, variable, true);
  }

  /**
   * Gets a string representation of this variable binding's value using the
   * {@link VariableTextFormat} configured by {@link SNMP4JSettings}.
   * @return
   *    a string of the form <code>&lt;Variable&gt;</code>.
   * @since 1.10
   */
  public String toValueString() {
    VariableTextFormat varFormat = SNMP4JSettings.getVariableTextFormat();
    return varFormat.format(oid, variable, false);
  }

  public Object clone() {
    return new VariableBinding(oid, variable);
  }

  public int hashCode() {
    return oid.hashCode();
  }

  public boolean equals(Object o) {
    if (o instanceof VariableBinding) {
      VariableBinding other = (VariableBinding)o;
      return oid.equals(other.getOid()) &&
          variable.equals(other.getVariable());
    }
    return false;
  }

  /**
   * Create an array of {@link VariableBinding}s based on the
   * provided OIDs.
   * @param oids
   *    an array of OIDs (must not be null).
   * @return
   *    an array of {@link VariableBinding}s where each
   *    the n-th binding's OID is the n-th OID out of
   *    <code>oids</code>.
   * @since 2.1
   */
  public static VariableBinding[] createFromOIDs(OID[] oids) {
    VariableBinding[] variableBindings = new VariableBinding[oids.length];
    for (int i=0; i<oids.length; i++) {
      variableBindings[i] = new VariableBinding(oids[i]);
    }
    return variableBindings;
  }
}

