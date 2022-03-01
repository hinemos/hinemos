/*_############################################################################
  _## 
  _##  SNMP4J - VariableTextFormat.java  
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
package org.snmp4j.util;

import java.text.ParseException;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

/**
 * The <code>VariableTextFormat</code> provides a textual representation
 * of SNMP {@link Variable}s, in dependence of their associated (instance) OID.
 *
 * @author Frank Fock
 * @version 1.10
 * @since 1.10
 */
public interface VariableTextFormat {

  /**
   * Returns a textual representation of the supplied variable against the
   * optionally supplied instance OID.
   *
   * @param instanceOID
   *    the instance OID <code>variable</code> is associated with.
   *    If <code>null</code> the formatting cannot take any MIB specification
   *    of the variable into account and has to format it based on its type
   *    only.
   * @param variable
   *    the variable to format.
   * @param withOID
   *    if <code>true</code> the <code>instanceOID</code> should be included
   *    in the textual representation to form a {@link VariableBinding}
   *    representation.
   * @return
   *    the textual representation.
   */
  String format(OID instanceOID, Variable variable, boolean withOID);

  /**
   * Parses a textual representation of a variable binding.
   *
   * @param text
   *    a textual representation of the variable binding.
   * @return
   *    the new <code>VariableBinding</code> instance.
   * @throws ParseException
   *    if the variable binding cannot be parsed successfully.
   */
  VariableBinding parseVariableBinding(String text) throws ParseException;

  /**
   * Parses a textual representation of a variable against its associated
   * OBJECT-TYPE OID.
   *
   * @param classOrInstanceOID
   *    the instance OID <code>variable</code> is associated with. Must not
   *    be <code>null</code>.
   * @param text
   *    a textual representation of the variable.
   * @return
   *    the new <code>Variable</code> instance.
   * @throws ParseException
   *    if the variable cannot be parsed successfully.
   */
  Variable parse(OID classOrInstanceOID, String text) throws ParseException;

  /**
   * Parses a textual representation of a variable against a SMI type.
   * @param smiSyntax
   *    the SMI syntax identifier identifying the target <code>Variable</code>.
   * @param text
   *    a textual representation of the variable.
   * @return
   *    the new <code>Variable</code> instance.
   * @throws ParseException
   *    if the variable cannot be parsed successfully.
   */
  Variable parse(int smiSyntax, String text) throws ParseException;
}
