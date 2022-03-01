/*_############################################################################
  _## 
  _##  SNMP4J - Null.java  
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
import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.asn1.BER.MutableByte;

/**
 * The <code>Null</code> class represents SMI Null and the derived
 * SMIv2 exception syntaxes.
 *
 * @author Frank Fock
 * @version 1.8
 */
public class Null extends AbstractVariable {

  private static final long serialVersionUID = 6907924131098190092L;

  private int syntax = SMIConstants.SYNTAX_NULL;

  public static final Null noSuchObject =
      new Null(SMIConstants.EXCEPTION_NO_SUCH_OBJECT);
  public static final Null noSuchInstance =
      new Null(SMIConstants.EXCEPTION_NO_SUCH_INSTANCE);
  public static final Null endOfMibView =
      new Null(SMIConstants.EXCEPTION_END_OF_MIB_VIEW);
  public static final Null instance =
      new Null(SMIConstants.SYNTAX_NULL);

  public Null() {
  }

  public Null(int exceptionSyntax) {
   setSyntax(exceptionSyntax);
  }

  public void decodeBER(BERInputStream inputStream) throws java.io.IOException {
    MutableByte type = new BER.MutableByte();
    BER.decodeNull(inputStream, type);
    this.syntax = type.getValue() & 0xFF;
  }

  public int getSyntax() {
    return syntax;
  }

  public int hashCode() {
    return getSyntax();
  }

  public int getBERLength() {
    return 2;
  }

  public boolean equals(Object o) {
    return (o instanceof Null) && (((Null) o).getSyntax() == getSyntax());
  }

  public int compareTo(Variable o) {
    return (getSyntax() - o.getSyntax());
  }

  public String toString() {
    switch (getSyntax()) {
      case SMIConstants.EXCEPTION_NO_SUCH_OBJECT:
        return "noSuchObject";
      case SMIConstants.EXCEPTION_NO_SUCH_INSTANCE:
        return "noSuchInstance";
      case SMIConstants.EXCEPTION_END_OF_MIB_VIEW:
        return "endOfMibView";
    }
    return "Null";
  }

  public void encodeBER(OutputStream outputStream) throws java.io.IOException {
    BER.encodeHeader(outputStream, (byte)getSyntax(), 0);
  }

  public void setSyntax(int syntax) {
    if ((syntax != SMIConstants.SYNTAX_NULL) &&  (!isExceptionSyntax(syntax))) {
      throw new IllegalArgumentException("Syntax " + syntax +
                                         " is incompatible with Null type");
    }
    this.syntax = syntax;
  }

  public Object clone() {
    return new Null(this.syntax);
  }

  public static boolean isExceptionSyntax(int syntax) {
    switch (syntax) {
      case SMIConstants.EXCEPTION_NO_SUCH_OBJECT:
      case SMIConstants.EXCEPTION_NO_SUCH_INSTANCE:
      case SMIConstants.EXCEPTION_END_OF_MIB_VIEW:
        return true;
    }
    return false;
  }

  /**
   * Returns the syntax of this Null variable.
   * @return
   *    {@link SMIConstants#SYNTAX_NULL} or one of the exception syntaxes
   *    {@link SMIConstants#EXCEPTION_NO_SUCH_OBJECT},
   *    {@link SMIConstants#EXCEPTION_NO_SUCH_INSTANCE}, or
   *    {@link SMIConstants#EXCEPTION_END_OF_MIB_VIEW}
   * @since 1.7
   */
  public final int toInt() {
    return getSyntax();
  }

  /**
   * Returns the syntax of this Null variable.
   * @return
   *    {@link SMIConstants#SYNTAX_NULL} or one of the exception syntaxes
   *    {@link SMIConstants#EXCEPTION_NO_SUCH_OBJECT},
   *    {@link SMIConstants#EXCEPTION_NO_SUCH_INSTANCE}, or
   *    {@link SMIConstants#EXCEPTION_END_OF_MIB_VIEW}
   * @since 1.7
   */
  public final long toLong() {
    return getSyntax();
  }

  public OID toSubIndex(boolean impliedLength) {
    throw new UnsupportedOperationException();
  }

  public void fromSubIndex(OID subIndex, boolean impliedLength) {
    throw new UnsupportedOperationException();
  }
}

