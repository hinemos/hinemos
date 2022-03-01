/*_############################################################################
  _## 
  _##  SNMP4J - TableListener.java  
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

import java.util.EventListener;

/**
 * The <code>TableListener</code> interface is implemented by objects
 * listening for table events. Table events typically contain row data.
 *
 * @author Frank Fock
 * @version 1.10
 * @since 1.0.2
 * @see TableUtils
 */
public interface TableListener extends EventListener {

  /**
   * Consumes the next table event, which is typically the next row in a
   * table retrieval operation.
   *
   * @param event
   *    a <code>TableEvent</code> instance.
   * @return
   *    <code>true</code> if this listener wants to receive more events,
   *    otherwise return <code>false</code>. For example, a
   *    <code>TableListener</code> can return <code>false</code> to stop
   *    table retrieval.
   */
  boolean next(TableEvent event);

  /**
   * Indicates in a series of table events that no more events will follow.
   * @param event
   *    a <code>TableEvent</code> instance that will either indicate an error
   *    ({@link TableEvent#isError()} returns <code>true</code>) or success
   *    of the table operation.
   */
  void finished(TableEvent event);

  /**
   * Indicates whether the tree walk is complete or not.
   * @return
   *    <code>true</code> if it is complete, <code>false</code> otherwise.
   * @since 1.10
   */
  boolean isFinished();

}
