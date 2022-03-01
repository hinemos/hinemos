/*_############################################################################
  _## 
  _##  SNMP4J - EnumerationIterator.java  
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

import java.util.*;

/**
 * The <code>EnumerationIterator</code> provides an iterator from an
 * {@link Enumeration}.
 *
 * @author Frank Fock
 * @version 2.0
 * @since 1.6.1
 */
public class EnumerationIterator<E> implements Iterator<E> {

  private Enumeration<E> e;

  public EnumerationIterator(Enumeration<E> e) {
    this.e = e;
  }

  /**
   * Returns <tt>true</tt> if the iteration has more elements.
   *
   * @return <tt>true</tt> if the iterator has more elements.
   */
  public boolean hasNext() {
    return e.hasMoreElements();
  }

  /**
   * Returns the next element in the iteration.
   *
   * @return the next element in the iteration.
   */
  public E next() {
    return e.nextElement();
  }

  /**
   * This method is not supported for enumerations.
   */
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
