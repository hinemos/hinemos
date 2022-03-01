/*_############################################################################
  _## 
  _##  SNMP4J - TestBER.java  
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


package org.snmp4j.asn1;

import junit.framework.*;
import java.io.*;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.snmp4j.ScopedPDU;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UnsignedInteger32;
import org.snmp4j.smi.Counter64;
import org.snmp4j.asn1.BER.MutableByte;

/**
 * Tests for verifying BER encoding and decoding.
 * @author Frank Fock
 * @since 2.0
 */
public class TestBER extends TestCase {

  public TestBER(String s) {
    super(s);
  }

  protected void setUp() {
  }

  protected void tearDown() {
  }

  public void testEncodeHeader() throws Exception {
    byte[] header = { (byte)0x80, (byte)0x83, (byte)0x73, (byte)0x59, (byte)0xB5 };
    ByteArrayOutputStream os1=  new ByteArrayOutputStream(65535);
    int type2=  0x80;
    int length3=  7559605;
    BER.encodeHeader(os1, type2, length3);
    assertEquals(os1.size(), header.length);
    byte[] result = os1.toByteArray();
    for (int i=0; i<os1.size(); i++)
      assertEquals(result[i], header[i]);
  }


  public void testEncodeInteger() throws Exception {
    byte[] result = { 0x02, 0x02, (byte)0x96, (byte)0xB5 };
    ByteArrayOutputStream os1=  new ByteArrayOutputStream(65535);
    byte type2=  0x02;
    int value3=  -26955;
    BER.encodeInteger(os1, type2, value3);

    assertEquals(os1.size(), result.length);
    byte[] value = os1.toByteArray();
    for (int i=0; i<os1.size(); i++)
      assertEquals(value[i], result[i]);

    Integer32 i32 = new Integer32(value3);
    assertEquals(result.length, i32.getBERLength());
  }

  public void testEncodeOID() throws Exception {
    byte[] result = { 0x06, 0x04, 0x2B, 0x06, (byte)0x99, 0x37 };
    ByteArrayOutputStream os1=  new ByteArrayOutputStream(65535);
    byte type2=  6;
    int[] oid3=  { 1, 3, 6, 3255 };
    BER.encodeOID(os1, type2, oid3);

    assertEquals(os1.size(), result.length);
    byte[] value = os1.toByteArray();
    for (int i=0; i<os1.size(); i++)
      assertEquals(value[i], result[i]);

    OID variable = new OID(oid3);
    assertEquals(result.length, variable.getBERLength());
  }

  public void testEncodeOIDMaxSubID() throws Exception {
    ByteArrayOutputStream os1=  new ByteArrayOutputStream(65535);
    byte type2=  6;
    int[] oid3=  { 1, 3, 6, -1 };
    BER.encodeOID(os1, type2, oid3);

    byte[] value = os1.toByteArray();
    OID variable = new OID(BER.decodeOID(new BERInputStream(ByteBuffer.wrap(value)),
                                         new MutableByte()));
    assertEquals(new OID(oid3), variable);
  }

  public void testEncodeSequence() throws Exception {
    byte[] result = { 0x30, 0x09, 0x02, 0x01, 0x00, 0x04, 0x04,
                      (byte)0xEB, 0x06, (byte)0x99,
                      0x37 };
    ByteArrayOutputStream os1=  new ByteArrayOutputStream(65535);
    byte type2=  0x30;
    int length3=  9;
    byte[] value = null;
    BER.encodeSequence(os1, type2, length3);
    BER.encodeInteger(os1, (byte)0x02, 0);
    BER.encodeString(os1, (byte)0x04, new byte[] { (byte)0xEB, 0x06, (byte)0x99, 0x37 });

    assertEquals(result.length, os1.size());
    value = os1.toByteArray();
    for (int i=0; i<os1.size(); i++)
      assertEquals(value[i], result[i]);

  }


  public void testEncodeString() throws Exception {
    byte[] result = { 0x04, 0x04,
                      (byte)0xEB, 0x06, (byte)0x99,
                      0x37 };
    ByteArrayOutputStream os1=  new ByteArrayOutputStream(65535);
    byte[] value = new byte[] { (byte)0xEB, 0x06, (byte)0x99, 0x37 };
    BER.encodeString(os1, (byte)0x04, value);

    assertEquals(result.length, os1.size());
    byte[] encoded = os1.toByteArray();
    for (int i=0; i<os1.size(); i++)
      assertEquals(encoded[i], result[i]);

    OctetString variable = new OctetString(value);
    assertEquals(result.length, variable.getBERLength());
  }

  public void testEncodeUnsignedInteger() throws Exception {
    byte[] result = { 0x42, 0x05, 0x00, (byte)0x80, 0x00, 0x00, 0x00 };
    ByteArrayOutputStream os1=  new ByteArrayOutputStream(65535);
    byte type2=  0x42;
    long value3=  2147483648l;
    byte[] value = null;
    BER.encodeUnsignedInteger(os1, type2, value3);

    assertEquals(result.length, os1.size());
    value = os1.toByteArray();
    for (int i=0; i<os1.size(); i++)
      assertEquals(result[i], value[i]);

    UnsignedInteger32 variable = new UnsignedInteger32(value3);
    assertEquals(result.length, variable.getBERLength());
  }

  public void testEncodeUnsignedInt64() throws Exception {
    byte[] result = { 0x46, 0x09, 0x00, (byte)0xC9, (byte)0xAC, (byte)0xC1, (byte)0x87,
                      0x4B, (byte)0xB1, (byte)0xE1, (byte)0xB9 };
    byte[] result3 = { 0x46, 0x01, 0x03 };
    byte[] result4 = { 0x46, 0x04, 0x01, 0x00, 0x00, 0x01 };
    ByteArrayOutputStream os1=  new ByteArrayOutputStream(11);
    byte type2=  0x46;
    long value3= -3914541189257109063l;// 14532202884452442553l;
    byte[] value = null;
    BER.encodeUnsignedInt64(os1, type2, value3);

    assertEquals(result.length, os1.size());
    value = os1.toByteArray();
    for (int i=0; i<os1.size(); i++) {
      assertEquals(result[i], value[i]);
    }
    Counter64 variable = new Counter64(value3);
    assertEquals(result.length, variable.getBERLength());

    os1=  new ByteArrayOutputStream(3);
    BER.encodeUnsignedInt64(os1, type2, 3);

    assertEquals(3, os1.size());
    value = os1.toByteArray();
    for (int i=0; i<os1.size(); i++) {
      assertEquals(result3[i], value[i]);
    }

    os1=  new ByteArrayOutputStream(3);
    BER.encodeUnsignedInt64(os1, type2, 16777217);

    assertEquals(6, os1.size());
    value = os1.toByteArray();
    for (int i=0; i<os1.size(); i++) {
      assertEquals(result4[i], value[i]);
    }
  }

  public void testDecodeLength() throws Exception {
    ByteArrayOutputStream os1 = new ByteArrayOutputStream(65535);
    int length = 7559605;
    BER.encodeLength(os1, length);
    byte[] result = os1.toByteArray();
    IOException ex = null;
    try {
      int decodedLength =
          BER.decodeLength(new BERInputStream(ByteBuffer.wrap(result)));
      assertEquals(length, decodedLength);
    }
    catch (IOException iox) {
      ex = iox;
    }
    assertNotNull(ex);
  }

  public void testDecodeInteger() throws Exception {
    ByteArrayOutputStream os1=  new ByteArrayOutputStream(65535);
    int integer =  -1;
    BER.encodeInteger(os1, (byte)0x02, integer);
    byte[] result = os1.toByteArray();
    BER.MutableByte b = new BER.MutableByte();
    int decodedInteger =
        BER.decodeInteger(new BERInputStream(ByteBuffer.wrap(result)), b);
    assertEquals(integer, decodedInteger);
    assertEquals((byte)0x02, b.getValue());

    integer = 0x7FFFFFFF;
    os1=  new ByteArrayOutputStream(65535);
    BER.encodeInteger(os1, (byte)0x02, integer);
    result = os1.toByteArray();
    decodedInteger =
        BER.decodeInteger(new BERInputStream(ByteBuffer.wrap(result)), b);
    assertEquals(integer, decodedInteger);
    assertEquals((byte)0x02, b.getValue());
  }

  public void testDecodeUnsignedInteger() throws Exception {
    ByteArrayOutputStream os1=  new ByteArrayOutputStream(65535);
    long integer =  0xFFFFFFFFl;
    BER.encodeUnsignedInteger(os1, (byte)0x42, integer);
    byte[] result = os1.toByteArray();
    BER.MutableByte b = new BER.MutableByte();
    long decodedInteger =
        BER.decodeUnsignedInteger(new BERInputStream(ByteBuffer.wrap(result)), b);
    assertEquals(integer, decodedInteger);
    assertEquals((byte)0x42, b.getValue());

    integer = 0x7FFFFFFFl;
    os1=  new ByteArrayOutputStream(65535);
    BER.encodeUnsignedInteger(os1, (byte)0x43, integer);
    result = os1.toByteArray();
    decodedInteger =
        BER.decodeUnsignedInteger(new BERInputStream(ByteBuffer.wrap(result)),b);
    assertEquals(integer, decodedInteger);
    assertEquals((byte)0x43, b.getValue());
  }

  public void testDecodeUnsignedInt64() throws Exception {
    ByteArrayOutputStream os1=  new ByteArrayOutputStream(65535);
    long integer = -1;
    BER.encodeUnsignedInt64(os1, (byte)0x46, integer);
    byte[] result = os1.toByteArray();
    BER.MutableByte b = new BER.MutableByte();
    long decodedInteger =
        BER.decodeUnsignedInt64(new BERInputStream(ByteBuffer.wrap(result)), b);
    assertEquals(integer, decodedInteger);
    assertEquals((byte)0x46, b.getValue());

    integer = 0x7FFFFFFFFFFFFFFFl;
    os1=  new ByteArrayOutputStream(65535);
    BER.encodeUnsignedInt64(os1, (byte)0x46, integer);
    result = os1.toByteArray();
    decodedInteger =
        BER.decodeUnsignedInt64(new BERInputStream(ByteBuffer.wrap(result)), b);
    assertEquals(integer, decodedInteger);
    assertEquals((byte)0x46, b.getValue());
  }

  public void testDecodeString() throws Exception {
    ByteArrayOutputStream os1=  new ByteArrayOutputStream(65535);
    String s = "Hello SNMP4J";
    BER.encodeString(os1, (byte)0x04, s.getBytes());
    byte[] result = os1.toByteArray();
    BER.MutableByte b = new BER.MutableByte();
    byte[] decodedString =
        BER.decodeString(new BERInputStream(ByteBuffer.wrap(result)), b);
    for (int i=0; i<decodedString.length; i++)
      assertEquals(s.getBytes()[i], decodedString[i]);
    assertEquals((byte)0x04, b.getValue());
  }

  public void testDecodeOID() throws Exception {
    ByteArrayOutputStream os1=  new ByteArrayOutputStream(65535);
    int[] s = { 1, 3, 6, 1, 4, 4976, 1, 0 };
    BER.encodeOID(os1, (byte)0x06, s);
    byte[] result = os1.toByteArray();
    BER.MutableByte b = new BER.MutableByte();
    int[] decodedOID =
        BER.decodeOID(new BERInputStream(ByteBuffer.wrap(result)), b);
    for (int i=0; i<decodedOID.length; i++)
      assertEquals(s[i], decodedOID[i]);
    assertEquals((byte)0x06, b.getValue());
  }

  public void testDecodeOID0() throws Exception {
    ByteArrayOutputStream os1=  new ByteArrayOutputStream(65535);
    int[] s = { 0, 39, 6, 1, 4, 4976, 1, 0 };
    BER.encodeOID(os1, (byte)0x06, s);
    byte[] result = os1.toByteArray();
    BER.MutableByte b = new BER.MutableByte();
    int[] decodedOID =
        BER.decodeOID(new BERInputStream(ByteBuffer.wrap(result)), b);
    for (int i=0; i<decodedOID.length; i++)
      assertEquals(s[i], decodedOID[i]);
    assertEquals((byte)0x06, b.getValue());
  }

  public void testDecodeOID11() throws Exception {
    ByteArrayOutputStream os1=  new ByteArrayOutputStream(65535);
    int[] s = { 1, 1, 6, 1, 4, 4976, 1, 0 };
    BER.encodeOID(os1, (byte)0x06, s);
    byte[] result = os1.toByteArray();
    BER.MutableByte b = new BER.MutableByte();
    int[] decodedOID =
        BER.decodeOID(new BERInputStream(ByteBuffer.wrap(result)), b);
    for (int i=0; i<decodedOID.length; i++)
      assertEquals(s[i], decodedOID[i]);
    assertEquals((byte)0x06, b.getValue());
  }

  public void testDecodeOID10() throws Exception {
    ByteArrayOutputStream os1=  new ByteArrayOutputStream(65535);
    int[] s = { 1, 0, 6, 1, 4, 4976, 1, 0 };
    BER.encodeOID(os1, (byte)0x06, s);
    byte[] result = os1.toByteArray();
    BER.MutableByte b = new BER.MutableByte();
    int[] decodedOID =
        BER.decodeOID(new BERInputStream(ByteBuffer.wrap(result)), b);
    for (int i=0; i<decodedOID.length; i++)
      assertEquals(s[i], decodedOID[i]);
    assertEquals((byte)0x06, b.getValue());
  }

  public void testDecodeOID139() throws Exception {
    int[] s = { 1, 39, 6, 1, 4, 4976, 1, 0 };
    int oidLength = BER.getOIDLength(s);
    assertEquals(8, oidLength);
    ByteArrayOutputStream os1=  new ByteArrayOutputStream(oidLength);
    BER.encodeOID(os1, (byte)0x06, s);
    byte[] result = os1.toByteArray();
    BER.MutableByte b = new BER.MutableByte();
    int[] decodedOID =
        BER.decodeOID(new BERInputStream(ByteBuffer.wrap(result)), b);
    for (int i=0; i<decodedOID.length; i++)
      assertEquals(s[i], decodedOID[i]);
    assertEquals((byte)0x06, b.getValue());
  }

  public void testDecodeOID2() throws Exception {
    int[] s = { 2, 2205, 6, 1, 4, 4976, 1, 0 };
    int oidLength = BER.getOIDLength(s);
    assertEquals(9, oidLength);
    ByteArrayOutputStream os1=  new ByteArrayOutputStream(oidLength);
    BER.encodeOID(os1, (byte)0x06, s);
    byte[] result = os1.toByteArray();
    BER.MutableByte b = new BER.MutableByte();
    int[] decodedOID =
        BER.decodeOID(new BERInputStream(ByteBuffer.wrap(result)), b);
    for (int i=0; i<decodedOID.length; i++)
      assertEquals(s[i], decodedOID[i]);
    assertEquals((byte)0x06, b.getValue());
  }

  public void testDecodeOID2Big() throws Exception {
    int[] s = { 2, 1073741824, 6, 1, 4, 4976, 1, 0 };
    int oidLength = BER.getOIDLength(s);
    assertEquals(12, oidLength);
    ByteArrayOutputStream os1=  new ByteArrayOutputStream(oidLength);
    BER.encodeOID(os1, (byte)0x06, s);
    byte[] result = os1.toByteArray();
    BER.MutableByte b = new BER.MutableByte();
    int[] decodedOID =
        BER.decodeOID(new BERInputStream(ByteBuffer.wrap(result)), b);
    for (int i=0; i<decodedOID.length; i++)
      assertEquals(s[i], decodedOID[i]);
    assertEquals((byte)0x06, b.getValue());
  }

  /*
  public void testDecodeScopedPDU() throws Exception {
    OctetString scopedPDUString =
        OctetString.fromHexString("30:3f:02:01:03:30:12:02:04:04:44:59:05:02:04:00:00:ff:e2:04:01:04:02:01:03:04:10"+
            ":30:0e:03:00:02:01:00:02:01:00:04:00:04:00:04:00:30:14:04:00:04:00:a0:0e:02:04:04:44:59:05:02:01:00:02:"+
            "01:00:30:00");
    BERInputStream wholeMsg = new BERInputStream(ByteBuffer.wrap(scopedPDUString.getValue()));
    BER.MutableByte type = new BER.MutableByte();
    int length = BER.decodeHeader(wholeMsg, type);
    assertEquals(type.getValue(),BER.SEQUENCE);
    long lengthOfLength = wholeMsg.getPosition();
    wholeMsg.reset();
    wholeMsg.mark(length);
    assertEquals(wholeMsg.skip(lengthOfLength), lengthOfLength);
    Integer32 snmpVersion = new Integer32();
    snmpVersion.decodeBER(wholeMsg);
    assertEquals(snmpVersion.getValue(), SnmpConstants.version3);
    // decode SNMPv3 header
    MPv3.HeaderData header = new MPv3.HeaderData();
    header.decodeBER(wholeMsg);
    ScopedPDU scopedPDU = new ScopedPDU();
    scopedPDU.decodeBER(wholeMsg);
  }
  */

  public void testEncodeBigInteger() throws Exception {
    byte[] result = { 0x02, 0x02, (byte)0x96, (byte)0xB5 };
    ByteArrayOutputStream os1=  new ByteArrayOutputStream(65535);
    byte type2=  0x02;
    BigInteger value3=  BigInteger.valueOf(-26955);
    BER.encodeBigInteger(os1, type2, value3);

    assertEquals(os1.size(), result.length);
    byte[] value = os1.toByteArray();
    for (int i=0; i<os1.size(); i++)
      assertEquals(value[i], result[i]);

    assertEquals(result.length, os1.size());
    int berIntegerLength = (value3.bitLength() + 1)/8;
    assertEquals(berIntegerLength+BER.getBERLengthOfLength(berIntegerLength)+1, result.length);
  }

  public void testDecodeBigInteger() throws Exception {
    ByteArrayOutputStream os1 =  new ByteArrayOutputStream(65535);
    byte type2 = 0x02;
    BigInteger value3 = BigInteger.valueOf(Long.MAX_VALUE).shiftLeft(16);
    BER.encodeBigInteger(os1, type2, value3);

    //System.out.println(new OctetString(os1.toByteArray()).toHexString());
    assertEquals("02:0a:7f:ff:ff:ff:ff:ff:ff:ff:00:00", new OctetString(os1.toByteArray()).toHexString());
    MutableByte mutableByte = new MutableByte();
    BigInteger result = BER.decodeBigInteger(new BERInputStream(ByteBuffer.wrap(os1.toByteArray())), mutableByte);
    assertEquals(type2, mutableByte.getValue());
    assertEquals(value3, result);
  }

}
