/*_############################################################################
  _## 
  _##  SNMP4J - TableEvent.java  
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

import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.PDU;
import java.util.Arrays;

/**
 * The <code>TableEvent</code> class reports events in a table retrieval
 * operation.
 *
 * @author Frank Fock
 * @version 1.8
 * @since 1.0.2
 * @see TableUtils
 */
public class TableEvent extends RetrievalEvent {

  private static final long serialVersionUID = 3340523737695933621L;

  private OID index;

  protected TableEvent(TableUtils.TableRequest source, Object userObject) {
    super(source, userObject);
    this.userObject = userObject;
  }

  /**
   * Creates a table event with a status.
   * @param source
   *    the source of the event.
   * @param userObject
   *    the user object or <code>null</code>.
   * @param status
   *    one of the status constants defined for this object.
   */
  public TableEvent(TableUtils.TableRequest source, Object userObject, int status) {
    this(source, userObject);
    this.status = status;
  }

  /**
   * Creates a table event with an exception.
   * @param source
   *    the source of the event.
   * @param userObject
   *    the user object or <code>null</code>.
   * @param exception
   *    an exception instance.
   */
  public TableEvent(TableUtils.TableRequest source, Object userObject, Exception exception) {
    this(source, userObject);
    this.exception = exception;
    this.status = STATUS_EXCEPTION;
  }

  /**
   * Creates a table event with a report PDU.
   * @param source
   *    the source of the event.
   * @param userObject
   *    the user object or <code>null</code>.
   * @param report
   *    a PDU of type {@link PDU#REPORT}.
   */
  public TableEvent(TableUtils.TableRequest source, Object userObject, PDU report) {
    this(source, userObject);
    this.reportPDU = report;
    this.status = STATUS_REPORT;
  }

  /**
   * Creates a table event with row data.
   *
   * @param source
   *    the source of the event.
   * @param userObject
   *    the user object or <code>null</code>.
   * @param index
   *    the index OID of the row.
   * @param cols
   *    an array of <code>VariableBinding</code> instances containing the
   *    elements of the row. The array may contain <code>null</code> elements
   *    which indicates that the agent does not return an instance for that
   *    column and row index. If an element is not <code>null</code>, then
   *    the <code>OID</code> of the variable binding contains the full instance
   *    <code>OID</code> (i.e., table OID + column ID + row index) of the variable.
   */
  public TableEvent(TableUtils.TableRequest source, Object userObject,
                    OID index, VariableBinding[] cols) {
    super(source, userObject, cols);
    this.index = index;
  }

  /**
   * Gets the row index OID.
   * @return
   *    the row's index OID or <code>null</code> if {@link #isError()} returns
   *    <code>true</code>.
   */
  public OID getIndex() {
    return index;
  }

  /**
   * Gets the columnar objects of the row.
   * @return
   *    an array of <code>VariableBinding</code> instances containing the
   *    elements of the row. The array may contain <code>null</code> elements
   *    which indicates that the agent does not return an instance for that
   *    column and row index. If an element is not <code>null</code>, then
   *    the <code>OID</code> of the variable binding contains the full instance
   *    <code>OID</code> of the variable.<p>
   *    If {@link #isError()} returns <code>true</code>, <code>null</code>
   *    will be returned.
   */
  public VariableBinding[] getColumns() {
    return vbs;
  }

  public String toString() {
    return getClass().getName()+"[index="+index+",vbs="+
        ((vbs == null) ? "null" : ""+Arrays.asList(vbs))+
        ",status="+status+",exception="+
        exception+",report="+reportPDU+"]";
  }
}
