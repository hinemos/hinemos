/*_############################################################################
  _## 
  _##  SNMP4J - VariantVariable.java  
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

import org.snmp4j.asn1.BERInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The <code>VariantVariable</code> provides a decorator for any type
 * of Variable instance, to be able to intercept or monitor variable
 * value modification by using a {@link VariantVariableCallback}.
 * <p>
 * This class will work for implementations that use {@link #getSyntax()}
 * method to determine the variables syntax. However "instanceof" will not
 * work.
 * <p>
 * In contrast to the native <code>Variable</code> implementations,
 * <code>VariantVariable</code> can be modified dynamically (i.e. while
 * a PDU is being BER encoded where this variable has been added to) without
 * causing BER encoding errors.
 *
 * @author Frank Fock
 * @version 1.8
 * @since 1.7
 */
public class VariantVariable extends AbstractVariable implements
    AssignableFromInteger,
    AssignableFromLong,
    AssignableFromString,
    AssignableFromByteArray {

  private static final long serialVersionUID = -3678564678835871188L;

  private Variable variable;
  private VariantVariableCallback callback;

  /**
   * Creates a variant variable wrapping the specified value.
   * @param initialVariable
   *    a <code>Variable</code>.
   */
  public VariantVariable(Variable initialVariable) {
    if (initialVariable == null) {
      throw new NullPointerException();
    }
    this.variable = initialVariable;
  }

  /**
   * Creates a variant variable wrapping the specified value and a callback
   * that monitors value modifications.
   * @param initialVariable
   *    a <code>Variable</code>.
   * @param callback
   *    a callback handler that is called before the value is to be modified
   *    and after it has been modified.
   */
  public VariantVariable(Variable initialVariable,
                         VariantVariableCallback callback) {
    this(initialVariable);
    this.callback = callback;
  }

  public synchronized int compareTo(Variable o) {
    updateVariable();
    return variable.compareTo(o);
  }

  protected void updateVariable() {
    if (callback != null) {
      callback.updateVariable(this);
    }
  }

  protected void variableUpdated() {
    if (callback != null) {
      callback.variableUpdated(this);
    }
  }

  public synchronized void decodeBER(BERInputStream inputStream) throws IOException {
    variable.decodeBER(inputStream);
    variableUpdated();
  }

  public synchronized void encodeBER(OutputStream outputStream) throws IOException {
    updateVariable();
    variable.encodeBER(outputStream);
  }

  public synchronized void fromSubIndex(OID subIndex, boolean impliedLength) {
    variable.fromSubIndex(subIndex, impliedLength);
    variableUpdated();
  }

  public synchronized int getBERLength() {
    updateVariable();
    return variable.getBERLength();
  }

  public int getSyntax() {
    return variable.getSyntax();
  }

  public synchronized int toInt() {
    updateVariable();
    return variable.toInt();
  }

  public synchronized long toLong() {
    updateVariable();
    return variable.toLong();
  }

  public synchronized byte[] toByteArray() {
    updateVariable();
    if (variable instanceof AssignableFromByteArray) {
      return ((AssignableFromByteArray)variable).toByteArray();
    }
    throw new UnsupportedOperationException();
  }

  public synchronized OID toSubIndex(boolean impliedLength) {
    updateVariable();
    return variable.toSubIndex(impliedLength);
  }

  public synchronized boolean equals(Object o) {
    updateVariable();
    return variable.equals(o);
  }

  public synchronized int hashCode() {
    updateVariable();
    return variable.hashCode();
  }

  public synchronized String toString() {
    updateVariable();
    return variable.toString();
  }

  public Object clone() {
    updateVariable();
    return new VariantVariable((Variable)variable.clone());
  }

  public synchronized void setValue(int value) {
    if (variable instanceof AssignableFromInteger) {
      ((AssignableFromInteger)variable).setValue(value);
    }
    else {
      throw new ClassCastException("An integer value cannot be assigned to "+
                                   variable);
    }
  }

  public synchronized void setValue(long value) {
    if (variable instanceof AssignableFromLong) {
      ((AssignableFromLong)variable).setValue(value);
    }
    else {
      throw new ClassCastException("A long value cannot be assigned to "+
                                   variable);
    }
  }

  public synchronized void setValue(OctetString value) {
    if (variable instanceof AssignableFromByteArray) {
      ((AssignableFromByteArray)variable).setValue(value.getValue());
    }
    else {
      throw new ClassCastException("An OctetString value cannot be assigned to "+
                                   variable);
    }
  }

  public synchronized void setValue(byte[] value) {
    if (variable instanceof AssignableFromByteArray) {
      ((AssignableFromByteArray)variable).setValue(value);
    }
    else {
      throw new ClassCastException("A byte array value cannot be assigned to "+
                                   variable);
    }
  }

  public synchronized void setValue(String value) {
    if (variable instanceof AssignableFromString) {
      ((AssignableFromString)variable).setValue(value);
    }
    else {
      throw new ClassCastException("A string value cannot be assigned to "+
                                   variable);
    }
  }

  /**
   * Returns the typed variable that holds the wrapped value.
   * @return
   *    a Variable instance.
   */
  public Variable getVariable() {
    return variable;
  }

  public boolean isDynamic() {
    return true;
  }

}
