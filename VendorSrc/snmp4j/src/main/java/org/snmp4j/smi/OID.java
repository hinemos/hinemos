/*_############################################################################
  _## 
  _##  SNMP4J - OID.java  
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
import java.util.*;
import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.SNMP4JSettings;
import org.snmp4j.util.OIDTextFormat;
import org.snmp4j.util.SimpleOIDTextFormat;

import java.text.ParseException;

/**
 * The Object Identifier Class.
 *
 * The Object Identifier (OID) class is the encapsulation of an
 * SMI object identifier. The SMI object is a data identifier for a
 * data element found in a Management Information Base (MIB), as
 * defined by a MIB definition. The {@code OID} class allows definition and
 * manipulation of object identifiers.
 *
 * @author Frank Fock
 * @version 2.3.1
 */
public class OID extends AbstractVariable
    implements AssignableFromString, AssignableFromIntArray
{

  private static final long serialVersionUID = 7521667239352941172L;

  private static OIDTextFormat oidTextFormat = new SimpleOIDTextFormat();

  public static final int MAX_OID_LEN = 128;
  public static final int MAX_SUBID_VALUE = 0xFFFFFFFF;

  private static final int[] NULL_OID = new int[0];

  private int[] value = NULL_OID;

  /**
   * Constructs a zero length OID.
   */
  public OID() {
  }

  /**
   * Constructs an {@code OID} from a dotted string. The string can contain
   * embedded strings enclosed by a single quote (') that are converted to
   * the corresponding OIO value. For example the following OID pairs are equal:
   * <pre>
   *     OID a = new OID("1.3.6.2.1.5.'hallo'.1");
   *     OID b = new OID("1.3.6.2.1.5.104.97.108.108.111.1");
   *     assertEquals(a, b);
   *     a = new OID("1.3.6.2.1.5.'hal.lo'.1");
   *     b = new OID("1.3.6.2.1.5.104.97.108.46.108.111.1");
   *     assertEquals(a, b);
   *     a = new OID("1.3.6.2.1.5.'hal.'.'''.'lo'.1");
   *     b = new OID("1.3.6.2.1.5.104.97.108.46.39.108.111.1");
   * </pre>
   * @param oid
   *    a dotted OID String, for example "1.3.6.1.2.2.1.0"
   */
  public OID(String oid) {
    value = parseDottedString(oid);
  }

  /**
   * Constructs an {@code OID} from an array of integer values.
   * @param rawOID
   *    an array of {@code int} values. The array
   *    is copied. Later changes to {@code rawOID} will therefore not
   *    affect the OID's value.
   */
  public OID(int[] rawOID) {
    this(rawOID, 0, rawOID.length);
  }

  /**
   * Constructs an {@code OID} from two arrays of integer values where
   * the first represents the OID prefix (i.e., the object class ID) and
   * the second one represents the OID suffix (i.e., the instance identifier).
   * @param prefixOID
   *    an array of {@code int} values. The array
   *    is copied. Later changes to {@code prefixOID} will therefore not
   *    affect the OID's value.
   * @param suffixOID
   *    an array of {@code int} values which will be appended to the
   *    {@code prefixOID} OID. The array is copied. Later changes to
   *    {@code suffixOID} will therefore not affect the OID's value.
   * @since 1.8
   */
  public OID(int[] prefixOID, int[] suffixOID) {
    this.value = new int[prefixOID.length+suffixOID.length];
    System.arraycopy(prefixOID, 0, value, 0, prefixOID.length);
    System.arraycopy(suffixOID, 0, value, prefixOID.length, suffixOID.length);
  }

  /**
   * Constructs an {@code OID} from two arrays of integer values where
   * the first represents the OID prefix (i.e., the object class ID) and
   * the second one represents the OID suffix (i.e., the instance identifier).
   * @param prefixOID
   *    an array of {@code int} values. The array
   *    is copied. Later changes to {@code prefixOID} will therefore not
   *    affect the OID's value.
   * @param suffixID
   *    an {@code int} value that will be appended to the
   *    {@code prefixOID} OID. The array is copied. Later changes to
   *    {@code prefixOID} will therefore not affect the OID's value.
   * @since 2.2.6
   */
  public OID(int[] prefixOID, int suffixID) {
    this.value = new int[prefixOID.length+1];
    System.arraycopy(prefixOID, 0, value, 0, prefixOID.length);
    this.value[prefixOID.length] = suffixID;
  }

  /**
   * Constructs an {@code OID} from an array of integer values.
   * @param rawOID
   *    an array of {@code int} values. The array
   *    is copied. Later changes to {@code rawOID} will therefore not
   *    affect the OID's value.
   * @param offset
   *    the zero based offset into the {@code rawOID} that points to the
   *    first sub-identifier of the new OID.
   * @param length
   *    the length of the new OID, where {@code offset + length} must be
   *    less or equal the length of {@code rawOID}. Otherwise an
   *    {@code IndexOutOfBoundsException} is thrown.
   */
  public OID(int[] rawOID, int offset, int length) {
    setValue(rawOID, offset, length);
  }

  /**
   * Copy constructor.
   * @param other OID
   */
  public OID(OID other) {
    this(other.getValue());
  }

  private static int[] parseDottedString(String oid) {
    try {
      return SNMP4JSettings.getOIDTextFormat().parse(oid);
    }
    catch (ParseException ex) {
      throw new RuntimeException("OID '"+oid+"' cannot be parsed", ex);
    }
  }


  public final int getSyntax() {
    return SMIConstants.SYNTAX_OBJECT_IDENTIFIER;
  }

  @Override
  public int hashCode() {
      return Arrays.hashCode(getValue());
  }

  public final boolean equals(Object o) {
    if (o instanceof OID) {
      OID other = (OID)o;
      if (other.value.length != value.length) {
        return false;
      }
      for (int i=0; i<value.length; i++) {
        if (value[i] != other.value[i]) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Returns a copy of this OID where sub-identifiers have been set to zero
   * for all n-th sub-identifier where the n-th bit of mask is zero.
   * @param mask
   *    a mask where the n-th bit corresponds to the n-th sub-identifier.
   * @return
   *    the masked OID.
   * @since 1.5
   */
  public OID mask(OctetString mask) {
    int[] masked = new int[value.length];
    System.arraycopy(value, 0, masked, 0, value.length);
    for (int i=0; (i<mask.length()*8) && (i<masked.length); i++) {
      byte b = (byte) (0x80 >> (i%8));
      if ((mask.get(i/8) & b) == 0) {
        masked[i] = 0;
      }
    }
    return new OID(masked);
  }

  public final int compareTo(Variable o) {
    if (o instanceof OID) {
      OID other = (OID)o;
      int min = Math.min(value.length, other.value.length);
      int result = leftMostCompare(min, other);
      if (result == 0) {
        return (value.length - other.value.length);
      }
      return result;
    }
    throw new ClassCastException(o.getClass().getName());
  }

  /**
   * Return a string representation that can be parsed again to this {@link OID}
   * by {@link #OID(String)}.
   * @return
   *    a formatted string representation of this OID (e.g. "ifDescr.1") that
   *    can be parsed again as defined by {@link OIDTextFormat#formatForRoundTrip(int[])}
   *    in {@link SNMP4JSettings}.
   */
  public String toString() {
    return SNMP4JSettings.getOIDTextFormat().formatForRoundTrip(value);
  }

  /**
   * Format the OID as text. This could return to same result as {@link #toString()}
   * but also fully converted index-to-text values like
   * {@code snmp4jLogLoggerIndex.org.snmp4j.MessageDispatcherImpl}.

   * @return
   *    a string representation of this OID as defined by the
   *    {@link OIDTextFormat#format(int[])} in {@link SNMP4JSettings}.
   * @since 2.2
   */
  public String format() {
    return SNMP4JSettings.getOIDTextFormat().format(value);
  }

  /**
   * Returns the OID as dotted string (e.g., "1.3.6.1.4.1") regardless of what
   * {@link OIDTextFormat} instance is set in {@link SNMP4JSettings}.
   * @return
   *    a dotted string representation of this OID value.
   * @since 2.2
   */
  public String toDottedString() {
    return oidTextFormat.format(value);
  }

  /**
   * Returns the content of the as a byte array. This method can be used
   * to convert an index value to an {@code OctetString} or
   * {@code IpAddress} instance.
   *
   * @return
   *    the sub-identifies of this {@code OID} as a byte array. Each
   *    sub-identifier value is masked with 0xFF to form a byte value.
   * @since 1.2
   */
  public byte[] toByteArray() {
    byte[] b = new byte[value.length];
    for (int i=0; i<value.length; i++) {
      b[i] = (byte) (value[i] & 0xFF);
    }
    return b;
  }

  public void encodeBER(OutputStream outputStream) throws java.io.IOException {
    BER.encodeOID(outputStream, BER.OID, value);
  }

  public int getBERLength() {
    int length = BER.getOIDLength(value);
    return length + BER.getBERLengthOfLength(length) + 1;
  }

  public void decodeBER(BERInputStream inputStream) throws java.io.IOException {
    BER.MutableByte type = new BER.MutableByte();
    int[] v = BER.decodeOID(inputStream, type);
    if (type.getValue() != BER.OID) {
      throw new IOException("Wrong type encountered when decoding OID: "+
                            type.getValue());
    }
    setValue(v);
  }

  public void setValue(String value) {
    this.value = parseDottedString(value);
  }

  /**
   * Sets the value from an array of integer values.
   *
   * @param value
   *    The new value
   * @throws IllegalArgumentException
   *    if value == null.
   */
  public final void setValue(int[] value) {
    if (value == null) {
      throw new IllegalArgumentException("OID value must not be set to null");
    }
    this.value = value;
  }

  private void setValue(int[] rawOID, int offset, int length) {
    value = new int[length];
    System.arraycopy(rawOID, offset, value, 0, length);
  }

  /**
   * Gets all sub-identifiers as an int array.
   *
   * @return int arry of all sub-identifiers
   */
  public final int[] getValue() {
    return value;
  }

  /**
   * Gets the sub-identifier value at the specified position.
   * @param index
   *    a zero-based index into the {@code OID}.
   * @throws ArrayIndexOutOfBoundsException
   *    if the index is out of range (index &lt; 0 || index &gt;= size()).
   * @return
   *    the sub-indentifier value at <code>index</code>. NOTE: The returned
   *    value may be negative if the sub-identifier value is greater than
   *    {@code 2^31}.
   */
  public final int get(int index) {
    return value[index];
  }

  /**
   * Gets the unsigned sub-identifier value at the specified position.
   * @param index int
   * @return
   *    the sub-identifier value at {@code index} as an unsigned long
   *    value.
   */
  public final long getUnsigned(int index) {
    return value[index] & 0xFFFFFFFFL;
  }

  /**
   * Sets the sub-identifier at the specified position.
   * @param index
   *    a zero-based index into the {@code OID}.
   * @param value
   *    a 32bit unsigned integer value.
   * @throws ArrayIndexOutOfBoundsException
   *    if the index is out of range (index &lt; 0 || index &gt;= size()).
   */
  public final void set(int index, int value) {
    this.value[index] = value;
  }

  /**
   * Appends a dotted String OID to this {@code OID}.
   * @param oid
   *    a dotted String with numerical sub-identifiers.
   * @return
   *    a pointer to this OID instance (useful for chaining).
   */
  public final OID append(String oid) {
    OID suffix = new OID(oid);
    return append(suffix);
  }

  /**
   * Appends an {@code OID} to this OID.
   * @param oid
   *    an {@code OID} instance.
   * @return
   *    a pointer to this OID instance (useful for chaining).
   */
  public final OID append(OID oid) {
    int[] newValue = new int[value.length+oid.value.length];
    System.arraycopy(value, 0, newValue, 0, value.length);
    System.arraycopy(oid.value, 0, newValue, value.length, oid.value.length);
    value = newValue;
    return this;
  }

  /**
   * Appends a sub-identifier to this OID.
   * @param subID
   *    an integer value.
   * @return
   *    a pointer to this OID instance (useful for chaining).
   */
  public final OID append(int subID) {
    int[] newValue = new int[value.length+1];
    System.arraycopy(value, 0, newValue, 0, value.length);
    newValue[value.length] = subID;
    value = newValue;
    return this;
  }

  /**
   * Appends an unsigned long sub-identifier value to this OID.
   * @param subID
   *    an unsigned long value less or equal to 2^32-1.
   * @return
   *    a pointer to this OID instance (useful for chaining).
   * @since 1.2
   */
  public final OID appendUnsigned(long subID) {
    return append((int)(subID & 0xFFFFFFFFL));
  }

  /**
   * Checks whether this {@code OID} can be BER encoded.
   * @return
   *    {@code true} if size() &gt;= 2 and size() &lt;= 128 and if the first
   *    two sub-identifiers are less than 3 and 40 respectively.
   */
  public boolean isValid() {
    return ((size() >= 2) && (size() <= 128) &&
            ((value[0] & 0xFFFFFFFFL) <= 2l) &&
            ((value[1] & 0xFFFFFFFFL) < 40l));
  }

  /**
   * Returns the number of sub-identifiers in this {@code OID}.
   * @return
   *    an integer value between 0 and 128.
   */
  public final int size() {
    return value.length;
  }

  /**
   * Compares the n leftmost sub-identifiers with the given {@code OID}
   * in left-to-right direction.
   * @param n
   *    the number of sub-identifiers to compare.
   * @param other
   *    an {@code OID} to compare with this {@code OID}.
   * @return
   *    <UL>
   *    <LI>0 if the first {@code n} sub-identifiers are the same.
   *    <LI>&lt;0 if the first {@code n} sub-identifiers of this
   *    {@code OID} are lexicographic less than those of the comparand.
   *    <LI>&gt;0 if the first {@code n} sub-identifiers of this
   *    {@code OID} are lexicographic greater than those of the comparand.
   *    </UL>
   */
  public int leftMostCompare(int n, OID other) {
    for (int i=0; i<n && i < value.length && i < other.size(); i++) {
      if (value[i] != other.value[i]) {
        if ((value[i] & 0xFFFFFFFFL) <
            (other.value[i] & 0xFFFFFFFFL)) {
          return -1;
        }
        else {
          return 1;
        }
      }
    }
    if (n > value.length) {
      return -1;
    }
    else if (n > other.size()) {
      return 1;
    }
    return 0;
  }

  /**
   * Compares the n rightmost sub-identifiers in direction right-to-left
   * with those of the given {@code OID}.
   * @param n
   *    the number of sub-identifiers to compare.
   * @param other
   *    an {@code OID} to compare with this {@code OID}.
   * @return
   *    <UL>
   *    <LI>0 if the first {@code n} sub-identifiers are the same.
   *    <LI>&lt;0 if the first {@code n} sub-identifiers of this
   *    {@code OID} are lexicographic less than those of the comparand.
   *    <LI>&gt;0 if the first {@code n} sub-identifiers of this
   *    {@code OID} are lexicographic greater than those of the comparand.
   *    </UL>
   */
  public int rightMostCompare(int n, OID other) {
    int cursorA = value.length-1;
    int cursorB = other.value.length-1;
    for (int i=n-1; i>=0; i--,cursorA--,cursorB--) {
      if (value[cursorA] != other.value[cursorB]) {
        if (value[cursorA] < other.value[cursorB]) {
          return -1;
        }
        else {
          return 1;
        }
      }
    }
    return 0;
  }

  /**
   * Check if the OID starts with the given OID.
   *
   * @param other
   *    the OID to compare to
   * @return
   *    false if the sub-identifiers do not match.
   */
  public boolean startsWith(OID other) {
    if (other.value.length > value.length) {
      return false;
    }
    int min = Math.min(value.length, other.value.length);
    return (leftMostCompare(min, other) == 0);
  }

  public Object clone() {
    return new OID(value);
  }

  /**
   * Returns the last sub-identifier as an integer value. If this OID is
   * empty (i.e. has no sub-identifiers) then a
   * {@link NoSuchElementException} is thrown
   * @return
   *    the value of the last sub-identifier of this OID as an integer value.
   *    Sub-identifier values greater than 2^31-1 will be returned as negative
   *    values!
   * @since 1.2
   */
  public final int last() {
    if (value.length > 0) {
      return value[value.length-1];
    }
    throw new NoSuchElementException();
  }

  /**
   * Returns the last sub-identifier as an unsigned long value. If this OID is
   * empty (i.e. has no sub-identifiers) then a
   * {@link NoSuchElementException} is thrown
   * @return
   *    the value of the last sub-identifier of this OID as an unsigned long.
   * @since 1.2
   */
  public final long lastUnsigned() {
    if (value.length > 0) {
      return value[value.length-1] & 0xFFFFFFFFL;
    }
    throw new NoSuchElementException();
  }

  /**
   * Removes the last sub-identifier (if available) from this {@code OID}
   * and returns it.
   * @return
   *    the last sub-identifier or -1 if there is no sub-identifier left in
   *    this {@code OID}.
   */
  public int removeLast() {
    if (value.length == 0) {
      return -1;
    }
    int[] newValue = new int[value.length-1];
    System.arraycopy(value, 0, newValue, 0, value.length-1);
    int retValue = value[value.length-1];
    value = newValue;
    return retValue;
  }

  /**
   * Remove the n rightmost subidentifiers from this OID.
   * @param n
   *    the number of subidentifiers to remove. If {@code n} is zero or
   *    negative then this OID will not be changed. If {@code n} is greater
   *    than {@link #size()} all subidentifiers will be removed from this OID.
   */
  public void trim(int n) {
    if (n > 0) {
      if (n > value.length) {
        n = value.length;
      }
      int[] newValue = new int[value.length-n];
      System.arraycopy(value, 0, newValue, 0, value.length-n);
      value = newValue;
    }
  }

  /**
   * Returns a new copy of this OID with the last sub-indentifier removed.
   * @return
   *    a copy of this OID with {@code n-1} sub-identifiers where
   *    {@code n} is the size of this OID and greater than zero, otherwise
   *    a zero length OID is returned.
   * @since 1.11
   */
  public OID trim() {
    return new OID(value, 0, Math.max(value.length-1, 0));
  }

  public int toInt() {
    throw new UnsupportedOperationException();
  }

  public long toLong() {
    throw new UnsupportedOperationException();
  }

  public final OID toSubIndex(boolean impliedLength) {
    if (impliedLength) {
      return new OID(value);
    }
    OID subIndex = new OID(new int[] { size() });
    subIndex.append(this);
    return subIndex;
  }

  public final void fromSubIndex(OID subIndex, boolean impliedLength) {
    int offset = 1;
    if (impliedLength) {
      offset = 0;
    }
    setValue(subIndex.getValue(), offset, subIndex.size()-offset);
  }

  /**
   * Returns the successor OID for this OID.
   * @return
   *    an OID clone of this OID with a zero sub-identifier appended.
   * @since 1.7
   */
  public final OID successor() {
    if (value.length == MAX_OID_LEN) {
      for (int i=MAX_OID_LEN-1; i>=0; i--) {
        if (value[i] != MAX_SUBID_VALUE) {
          int[] succ = new int[i+1];
          System.arraycopy(value, 0, succ, 0, i+1);
          succ[i]++;
          return new OID(succ);
        }
      }
      return new OID();
    }
    else {
      int[] succ = new int[value.length + 1];
      System.arraycopy(value, 0, succ, 0, value.length);
      succ[value.length] = 0;
      return new OID(succ);
    }
  }

  /**
   * Returns the predecessor OID for this OID.
   * @return
   *    if this OID ends on 0, then a {@link #MAX_OID_LEN}
   *    sub-identifier OID is returned where each sub-ID for index greater
   *    or equal to {@link #size()} is set to {@link #MAX_SUBID_VALUE}.
   * @since 1.7
   */
  public final OID predecessor() {
    if (last() != 0) {
      int[] pval = new int[MAX_OID_LEN];
      System.arraycopy(value, 0, pval, 0, value.length);
      Arrays.fill(pval, value.length, pval.length, MAX_SUBID_VALUE);
      OID pred = new OID(pval);
      pred.set(size()-1, last()-1);
      return pred;
    }
    else {
      OID pred = new OID(this);
      pred.removeLast();
      return pred;
    }
  }

  /**
   * Returns the next following OID with the same or lesser size (length).
   * @return OID
   *    the next OID on the same or upper level or a clone of this OID, if
   *    it has a zero length or is 2^32-1.
   * @since 1.7
   */
  public final OID nextPeer() {
    OID next = new OID(this);
    if ((next.size() > 0) && (last() != MAX_SUBID_VALUE)) {
      next.set(next.size()-1, last()+1);
    }
    else if (next.size() > 1) {
      next.trim(1);
      next = next.nextPeer();
    }
    return next;
  }

  /**
   * Returns the greater of the two OID values.
   * @param a
   *    an OID.
   * @param b
   *    an OID.
   * @return
   *    {@code a} if a &gt;= b, {@code b} otherwise.
   * @since 1.7
   */
  public static OID max(OID a, OID b) {
    if (a.compareTo(b) >= 0) {
      return a;
    }
    return b;
  }

  /**
   * Returns the lesser of the two OID values.
   * @param a
   *    an OID.
   * @param b
   *    an OID.
   * @return
   *    {@code a} if a &lt;= b, {@code b} otherwise.
   * @since 1.7
   */
  public static OID min(OID a, OID b) {
    if (a.compareTo(b) <= 0) {
      return a;
    }
    return b;
  }

  public int[] toIntArray() {
    return value;
  }

}

