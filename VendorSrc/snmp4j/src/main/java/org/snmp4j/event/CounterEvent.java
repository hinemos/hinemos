/*_############################################################################
  _## 
  _##  SNMP4J - CounterEvent.java  
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
package org.snmp4j.event;

import java.util.EventObject;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.Counter32;
// for JavaDoc
import org.snmp4j.smi.Counter64;

/**
 * <code>CounterEvent</code> is an event object that indicates that a specific
 * counter needs to be incremented.
 * <p>
 * At the same time a <code>CounterEvent</code>
 * can be used by the event originator to retrieve the actual value of the
 * specified counter. Listeners that maintain the specified counter value,
 * must set the new value when receiving the <code>CounterEvent</code> by using
 * the {@link #setCurrentValue(Variable currentValue)} method.
 *
 * @author Frank Fock
 * @version 2.4.2
 */
public class CounterEvent extends EventObject {

  private static final long serialVersionUID = 7916507798848195425L;

  private OID oid;
  private Variable currentValue = new Counter32();
  private long increment = 1;
  private Object index;

  /**
   * Creates a <code>CounterEvent</code> for the specified counter.
   * @param source
   *    the source of the event.
   * @param oid
   *    the OID of the counter instance (typically, the counter is a scalar and
   *    thus the OID has to end on zero).
   */
  public CounterEvent(Object source, OID oid) {
    super(source);
    this.oid = oid;
  }

  /**
   * Creates a <code>CounterEvent</code> for the specified counter.
   * @param source
   *    the source of the event.
   * @param oid
   *    the OID of the counter instance (typically, the counter is a scalar and
   *    thus the OID has to end on zero).
   * @param increment
   *    a positive natural number (default is 1) that defines the increment
   *    that needs to be added to the counter on behalf of this event.
   */
  public CounterEvent(Object source, OID oid, long increment) {
    this(source, oid);
    this.increment = increment;
  }

  /**
   * Creates a <code>CounterEvent</code> for the specified counter.
   * @param source
   *    the source of the event.
   * @param oid
   *    the OID of the counter instance (typically, the counter is a scalar and
   *    thus the OID has to end on zero).
   * @param index
   *    an counter defined object that identifies the counter row within a table of counters.
   * @param increment
   *    a positive natural number (default is 1) that defines the increment
   *    that needs to be added to the counter on behalf of this event.
   * @since 2.4.2
   */
  public CounterEvent(Object source, OID oid, Object index, long increment) {
    this(source, oid, increment);
    this.index = index;
  }

  /**
   * Gets the instance object identifier of the counter.
   * @return
   *    an <code>OID</code>.
   */
  public OID getOid() {
    return oid;
  }

  /**
   * Gets the current value of the counter, as set by the maintainer of the
   * counter (one of the event listeners).
   * @return
   *    a {@link Counter32} or {@link Counter64} instance.
   */
  public Variable getCurrentValue() {
    return currentValue;
  }

  /**
   * Sets the current value of the counter. This method has to be called by
   * the maintainer of the counter's value.
   *
   * @param currentValue
   *    a {@link Counter32} or {@link Counter64} instance.
   */
  public void setCurrentValue(Variable currentValue) {
    this.currentValue = currentValue;
  }

  /**
   * The increment to be added to the counter value on behalf of this event.
   * The default is 1.
   * @return
   *    the counter increment of this event.
   * @since 2.4.2
   */
  public long getIncrement() {
    return increment;
  }

  /**
   * Sets the increment of the event. This has to be done before the event is fired to have an effect!
   * @param increment
   *    the counter increment (must be a positive value for Counter32 counters!). For Counter64 counters,
   *    the value might be negative but is then interpreted as an unsinged long value.
   */
  public void setIncrement(long increment) {
    this.increment = increment;
  }

  /**
   * The index identifier of the counter value (if the counter belongs to a table of counters).
   * @return
   *    the row index identifier for this counter event or <code>null</code> if the counter is a scalar value.
   * @since 2.4.2
   */
  public Object getIndex() {
    return index;
  }

  @Override
  public String toString() {
    return "CounterEvent{" +
        "oid=" + oid +
        ", currentValue=" + currentValue +
        ", increment=" + increment +
        ", index=" + index +
        "} " + super.toString();
  }
}
