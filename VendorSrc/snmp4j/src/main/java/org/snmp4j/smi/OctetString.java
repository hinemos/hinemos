/*_############################################################################
  _## 
  _##  SNMP4J - OctetString.java  
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

/**
 * The {@code OctetString} class represents the SMI type OCTET STRING.
 *
 * @author Frank Fock
 * @version 1.8
 * @since 1.0
 */
public class OctetString extends AbstractVariable
    implements AssignableFromByteArray, AssignableFromString {

  private static final long serialVersionUID = 4125661211046256289L;

  private static final char DEFAULT_HEX_DELIMITER = ':';

  private byte[] value = new byte[0];

  /**
   * Creates a zero length octet string.
   */
  public OctetString() {
  }

  /**
   * Creates an octet string from an byte array.
   * @param rawValue
   *    an array of bytes.
   */
  public OctetString(byte[] rawValue) {
    this(rawValue, 0, rawValue.length);
  }

  /**
   * Creates an octet string from an byte array.
   * @param rawValue
   *    an array of bytes.
   * @param offset
   *    the position (zero based) of the first byte to be copied from
   *    {@code rawValue}into the new {@code OctetString}.
   * @param length
   *    the number of bytes to be copied.
   */
  public OctetString(byte[] rawValue, int offset, int length) {
    value = new byte[length];
    System.arraycopy(rawValue, offset, value, 0, length);
  }

  /**
   * Creates a concatenated octet string from two byte arrays.
   * @param rawValuePrefix
   *    an array of bytes.
   * @param rawValueSuffix
   *    an array of bytes which will appended to rawValuePrefix to form this new OctetString. If rawValueSuffix is
   *    {@code null} then the result will be the same as with a zero length suffix array.
   * @since 2.6.0
   */
  public OctetString(byte[] rawValuePrefix, byte[] rawValueSuffix) {
    if (rawValueSuffix == null) {
      value = new byte[rawValuePrefix.length];
    }
    else {
      value = new byte[rawValuePrefix.length + rawValueSuffix.length];
    }
    System.arraycopy(rawValuePrefix, 0, value, 0, rawValuePrefix.length);
    if (rawValueSuffix != null) {
      System.arraycopy(rawValueSuffix, 0, value, rawValuePrefix.length, rawValueSuffix.length);
    }
  }

  /**
   * Creates an octet string from a java string.
   *
   * @param stringValue
   *    a Java string.
   */
  public OctetString(String stringValue) {
    this.value = stringValue.getBytes();
  }

  /**
   * Creates an octet string from another OctetString by cloning its value.
   *
   * @param other
   *    an {@code OctetString} instance.
   */
  public OctetString(OctetString other) {
    this.value = new byte[0];
    append(other);
  }

  /**
   * Appends a single byte to this octet string.
   * @param b
   *    a byte value.
   */
  public void append(byte b) {
    byte[] newValue = new byte[value.length+1];
    System.arraycopy(value, 0, newValue, 0, value.length);
    newValue[value.length] = b;
    value = newValue;
  }

  /**
   * Appends an array of bytes to this octet string.
   * @param bytes
   *    an array of bytes.
   */
  public void append(byte[] bytes) {
    byte[] newValue = new byte[value.length + bytes.length];
    System.arraycopy(value, 0, newValue, 0, value.length);
    System.arraycopy(bytes, 0, newValue, value.length, bytes.length);
    value = newValue;
  }

  /**
   * Appends an octet string.
   * @param octetString
   *   an {@code OctetString} to append to this octet string.
   */
  public void append(OctetString octetString) {
    append(octetString.getValue());
  }

  /**
   * Appends the supplied string to this {@code OctetString}. Calling this
   * method is identical to <I>append(string.getBytes())</I>.
   * @param string
   *    a String instance.
   */
  public void append(String string) {
    append(string.getBytes());
  }

  /**
   * Sets the value of the octet string to a zero length string.
   */
  public void clear() {
    value = new byte[0];
  }

  public void encodeBER(OutputStream outputStream) throws java.io.IOException {
    BER.encodeString(outputStream, BER.OCTETSTRING, getValue());
  }

  public void decodeBER(BERInputStream inputStream) throws java.io.IOException {
    BER.MutableByte type = new BER.MutableByte();
    byte[] v = BER.decodeString(inputStream, type);
    if (type.getValue() != BER.OCTETSTRING) {
      throw new IOException("Wrong type encountered when decoding OctetString: "+
                            type.getValue());
    }
    setValue(v);
  }

  public int getBERLength() {
    return value.length + BER.getBERLengthOfLength(value.length) + 1;
  }

  public int getSyntax() {
    return SMIConstants.SYNTAX_OCTET_STRING;
  }

  /**
   * Gets the byte at the specified index.
   * @param index
   *    a zero-based index into the octet string.
   * @return
   *    the byte value at the specified index.
   * @throws ArrayIndexOutOfBoundsException
   *    if {@code index} &lt; 0 or &gt; {@link #length()}.
   */
  public final byte get(int index) {
    return value[index];
  }

  /**
   * Sets the byte value at the specified index.
   * @param index
   *    an index value greater or equal 0 and less than {@link #length()}.
   * @param b
   *    the byte value to set.
   * @since v1.2
   */
  public final void set(int index, byte b) {
    value[index] = b;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(getValue());
  }

  public boolean equals(Object o) {
    if (o instanceof OctetString) {
      OctetString other = (OctetString)o;
      return Arrays.equals(value, other.value);
    }
    return false;
  }

  /**
   * Checks if the value of this OctetString equals the argument.
   * @param v
   *    the byte array to compare with this OctetStrings value member.
   * @return
   *    {@code Arrays.equals(value, (byte[])v)}
   * @since 2.0
   */
  public boolean equalsValue(byte[] v) {
    return Arrays.equals(value, v);
  }

  public int compareTo(Variable o) {
    if (o instanceof OctetString) {
      OctetString other = (OctetString)o;
      int maxlen = Math.min(value.length, other.value.length);
      for (int i=0; i<maxlen; i++) {
        if (value[i] != other.value[i]) {
          if ((value[i] & 0xFF) < (other.value[i] & 0xFF)) {
            return -1;
          }
          else {
            return 1;
          }
        }
      }
      return (value.length - other.value.length);
    }
    throw new ClassCastException(o.getClass().getName());
  }

  /**
   * Returns a new string that is a substring of this string. The substring
   * begins at the specified {@code beginIndex} and extends to the
   * character at index {@code endIndex - 1}.
   * Thus the length of the substring is {@code endIndex-beginIndex}.
   * @param beginIndex
   *    the beginning index, inclusive.
   * @param endIndex
   *    the ending index, exclusive.
   * @return
   *    the specified substring.
   * @since 1.3
   */
  public OctetString substring(int beginIndex, int endIndex) {
    if ((beginIndex < 0) || (endIndex > length())) {
      throw new IndexOutOfBoundsException();
    }
    byte[] substring = new byte[endIndex - beginIndex];
    System.arraycopy(value, beginIndex, substring, 0, substring.length);
    return new OctetString(substring);
  }

  /**
   * Tests if this octet string starts with the specified prefix.
   * @param prefix
   *    the prefix.
   * @return
   *    {@code true} if the bytes of this octet string up to the length
   *    of {@code prefix} equal those of {@code prefix}.
   * @since 1.2
   */
  public boolean startsWith(OctetString prefix) {
    if ((prefix == null) || prefix.length() > length()) {
      return false;
    }
    for (int i=0; i<prefix.length(); i++) {
      if (prefix.get(i) != value[i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Determines whether this octet string contains non ISO control characters
   * only.
   * @return
   *    {@code false} if this octet string contains any ISO control
   *    characters as defined by {@link Character#isISOControl(char)}
   *    except if these ISO control characters are all whitespace characters
   *    as defined by {@link Character#isWhitespace(char)} and not
   *    {@code '&#92;u001C'}-{@code '&#92;u001F'}.
   */
  public boolean isPrintable() {
    for (byte aValue : value) {
      char c = (char) aValue;
      if ((Character.isISOControl(c) || ((c & 0xFF) >= 0x80)) &&
          ((!Character.isWhitespace(c)) ||
              (((c & 0xFF) >= 0x1C)) && ((c & 0xFF) <= 0x1F))) {
        return false;
      }
    }
    return true;
  }

  public String toString() {
    if (isPrintable()) {
      return new String(value);
    }
    return toHexString();
  }

  public String toHexString() {
    return toHexString(DEFAULT_HEX_DELIMITER);
  }

  public String toHexString(char separator) {
    return toString(separator, 16);
  }

  public static OctetString fromHexString(String hexString) {
    return fromHexString(hexString, DEFAULT_HEX_DELIMITER);
  }

  public static OctetString fromHexString(String hexString, char delimiter) {
    return OctetString.fromString(hexString, delimiter, 16);
  }


  public static OctetString fromString(String string, char delimiter, int radix) {
    String delim = "";
    delim += delimiter;
    StringTokenizer st = new StringTokenizer(string, delim);
    byte[] value = new byte[st.countTokens()];
    for (int n=0; st.hasMoreTokens(); n++) {
      String s = st.nextToken();
      value[n] = (byte)Integer.parseInt(s, radix);
    }
    return new OctetString(value);
  }

  /**
   * Create an OctetString from a hexadecimal string of 2-byte pairs without
   * delimiter. For example: 08A69E
   * @param hexString
   *    a string of characters a-f,A-F,0-9 with length 2*b, where b is the length
   *    of the string in bytes.
   * @return
   *    an OctetString instance with the length {@code hexString.length()/2}.
   * @since 2.1
   */
  public static OctetString fromHexStringPairs(String hexString) {
    byte[] value = new byte[hexString.length()/2];
    for (int i=0; i<value.length; i++) {
      int h = i*2;
      value[i] = (byte)Integer.parseInt(hexString.substring(h, h+2), 16);
    }
    return new OctetString(value);
  }

  /**
   * Creates an OctetString from a string representation in the specified
   * radix.
   * @param string
   *    the string representation of an octet string.
   * @param radix
   *    the radix of the string representation.
   * @return
   *    the OctetString instance.
   * @since 1.6
   */
  public static OctetString fromString(String string, int radix) {
    int digits = (int)(Math.round((float)Math.log(256)/Math.log(radix)));
    byte[] value = new byte[string.length()/digits];
    for (int n=0; n<string.length(); n+=digits) {
      String s = string.substring(n, n+digits);
      value[n/digits] = (byte)Integer.parseInt(s, radix);
    }
    return new OctetString(value);
  }

  public String toString(char separator, int radix) {
    int digits = (int)(Math.round((float)Math.log(256)/Math.log(radix)));
    StringBuilder buf = new StringBuilder(value.length*(digits+1));
    for (int i=0; i<value.length; i++) {
      if (i > 0) {
        buf.append(separator);
      }
      int v = (value[i] & 0xFF);
      String val = Integer.toString(v, radix);
      for (int j=0; j < digits - val.length(); j++) {
        buf.append('0');
      }
      buf.append(val);
    }
    return buf.toString();
  }

  /**
   * Returns a string representation of this octet string in the radix
   * specified. There will be no separation characters, but each byte will
   * be represented by {@code round(log(256)/log(radix))} digits.
   *
   * @param radix
   *    the radix to use in the string representation.
   * @return
   *    a string representation of this ocetet string in the specified radix.
   * @since 1.6
   */
  public String toString(int radix) {
    int digits = (int)(Math.round((float)Math.log(256)/Math.log(radix)));
    StringBuilder buf = new StringBuilder(value.length*(digits+1));
    for (byte aValue : value) {
      int v = (aValue & 0xFF);
      String val = Integer.toString(v, radix);
      for (int j = 0; j < digits - val.length(); j++) {
        buf.append('0');
      }
      buf.append(val);
    }
    return buf.toString();
  }


  /**
   * Formats the content into a ASCII string. Non-printable characters are
   * replaced by the supplied placeholder character.
   * @param placeholder
   *    a placeholder character, for example '.'.
   * @return
   *    the contents of this octet string as ASCII formatted string.
   * @since 1.6
   */
  public String toASCII(char placeholder) {
    StringBuilder buf = new StringBuilder(value.length);
    for (byte aValue : value) {
      if ((Character.isISOControl((char) aValue)) ||
          ((aValue & 0xFF) >= 0x80)) {
        buf.append(placeholder);
      } else {
        buf.append((char) aValue);
      }
    }
    return buf.toString();
  }

  public void setValue(String value) {
    setValue(value.getBytes());
  }

  public void setValue(byte[] value) {
    if (value == null) {
      throw new IllegalArgumentException(
          "OctetString must not be assigned a null value");
    }
    this.value = value;
  }

  public byte[] getValue() {
    return value;
  }

  /**
   * Gets the length of the byte string.
   * @return
   *    a zero or positive integer value.
   */
  public final int length() {
    return value.length;
  }

  public Object clone() {
    return new OctetString(value);
  }

  /**
   * Returns the length of the payload of this {@code BERSerializable}
   * object in bytes when encoded according to the Basic Encoding Rules (BER).
   *
   * @return the BER encoded length of this variable.
   */
  public int getBERPayloadLength() {
    return value.length;
  }

  public int toInt() {
    throw new UnsupportedOperationException();
  }

  public long toLong() {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns a copy of this OctetString where each bit not set in the supplied
   * mask zeros the corresponding bit in the returned OctetString.
   * @param mask
   *    a mask where the n-th bit corresponds to the n-th bit in the returned
   *    OctetString.
   * @return
   *    the masked OctetString.
   * @since 1.7
   */
  public OctetString mask(OctetString mask) {
    byte[] masked = new byte[value.length];
    System.arraycopy(value, 0, masked, 0, value.length);
    for (int i=0; (i<mask.length()) && (i<masked.length); i++) {
      masked[i] = (byte)(masked[i] & mask.get(i));
    }
    return new OctetString(masked);
  }

  public OID toSubIndex(boolean impliedLength) {
    int[] subIndex;
    int offset = 0;
    if (!impliedLength) {
      subIndex = new int[length()+1];
      subIndex[offset++] = length();
    }
    else {
      subIndex = new int[length()];
    }
    for (int i=0; i<length(); i++) {
      subIndex[offset+i] = get(i) & 0xFF;
    }
    return new OID(subIndex);
  }

  public void fromSubIndex(OID subIndex, boolean impliedLength) {
    if (impliedLength) {
      setValue(subIndex.toByteArray());
    }
    else {
      OID suffix = new OID(subIndex.getValue(), 1, subIndex.size() - 1);
      setValue(suffix.toByteArray());
    }
  }

  /**
   * Splits an {@code OctetString} using a set of delimiter characters
   * similar to how a StringTokenizer would do it.
   * @param octetString
   *    the input string to tokenize.
   * @param delimOctets
   *    a set of delimiter octets.
   * @return
   *    a Collection of OctetString instances that contain the tokens.
   */
  public static Collection<OctetString> split(OctetString octetString,
                                              OctetString delimOctets) {
    List<OctetString> parts = new LinkedList<OctetString>();
    int maxDelim = -1;
    for (int i = 0; i<delimOctets.length(); i++) {
      int delim = delimOctets.get(i) & 0xFF;
      if (delim > maxDelim) {
        maxDelim = delim;
      }
    }
    int startPos = 0;
    for (int i = 0; i<octetString.length(); i++) {
      int c = octetString.value[i] & 0xFF;
      boolean isDelim = false;
      if (c <= maxDelim) {
        for (int j=0; j<delimOctets.length(); j++) {
          if (c == (delimOctets.get(j) & 0xFF)) {
            if ((startPos >= 0) && (i > startPos)) {
              parts.add(new OctetString(octetString.value,
                                        startPos, i - startPos));
            }
            startPos = -1;
            isDelim = true;
          }
        }
      }
      if (!isDelim && (startPos < 0)) {
        startPos = i;
      }
    }
    if (startPos >= 0) {
      parts.add(new OctetString(octetString.value, startPos,
                                octetString.length() - startPos));
    }
    return parts;
  }

  /**
   * Creates an {@code OctetString} from an byte array.
   * @param value
   *    a byte array that is copied into the value of the created
   *     {@code OctetString} or {@code null}.
   * @return
   *    an OctetString or {@code null} if {@code value}
   *    is {@code null}.
   * @since 1.7
   */
  public static OctetString fromByteArray(byte[] value) {
    if (value == null) {
      return null;
    }
    return new OctetString(value);
  }

  public byte[] toByteArray() {
    return getValue();
  }
}


