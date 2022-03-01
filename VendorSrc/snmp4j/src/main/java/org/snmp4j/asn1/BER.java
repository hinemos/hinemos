/*_############################################################################
  _## 
  _##  SNMP4J - BER.java  
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

import java.io.OutputStream;
import java.io.IOException;
import java.math.BigInteger;

/**
 * The BER class provides utility methods for the BER encoding and decoding.
 *
 * @author Frank Fock
 * @author Jochen Katz
 * @version 2.5.8
 */
public class BER {

    public static final byte ASN_BOOLEAN = 0x01;
    public static final byte ASN_INTEGER = 0x02;
    public static final byte ASN_BIT_STR = 0x03;
    public static final byte ASN_OCTET_STR = 0x04;
    public static final byte ASN_NULL = 0x05;
    public static final byte ASN_OBJECT_ID = 0x06;
    public static final byte ASN_SEQUENCE = 0x10;
    public static final byte ASN_SET = 0x11;
    public static final byte ASN_UNIVERSAL = 0x00;
    public static final byte ASN_APPLICATION = 0x40;
    public static final byte ASN_CONTEXT = (byte)0x80;
    public static final byte ASN_PRIVATE = (byte)0xC0;
    public static final byte ASN_PRIMITIVE = (byte)0x00;
    public static final byte ASN_CONSTRUCTOR = (byte)0x20;

    public static final byte ASN_LONG_LEN = (byte)0x80;
    public static final byte ASN_EXTENSION_ID = (byte)0x1F;
    public static final byte ASN_BIT8 = (byte)0x80;

    public static final byte INTEGER = ASN_UNIVERSAL | 0x02;
    public static final byte INTEGER32 = ASN_UNIVERSAL | 0x02;
    public static final byte BITSTRING = ASN_UNIVERSAL | 0x03;
    public static final byte OCTETSTRING = ASN_UNIVERSAL | 0x04;
    public static final byte NULL = ASN_UNIVERSAL | 0x05;
    public static final byte OID = ASN_UNIVERSAL | 0x06;
    public static final byte SEQUENCE = ASN_CONSTRUCTOR | 0x10;

    public static final byte IPADDRESS = ASN_APPLICATION | 0x00;
    public static final byte COUNTER = ASN_APPLICATION | 0x01;
    public static final byte COUNTER32 = ASN_APPLICATION | 0x01;
    public static final byte GAUGE = ASN_APPLICATION | 0x02;
    public static final byte GAUGE32 = ASN_APPLICATION | 0x02;
    public static final byte TIMETICKS = ASN_APPLICATION | 0x03;
    public static final byte OPAQUE = ASN_APPLICATION | 0x04;
    public static final byte COUNTER64 = ASN_APPLICATION | 0x06;

    public static final int NOSUCHOBJECT = 0x80;
    public static final int NOSUCHINSTANCE = 0x81;
    public static final int ENDOFMIBVIEW = 0x82;

    private static final int LENMASK = 0x0ff;
    public static final int MAX_OID_LENGTH = 127;

    private static boolean checkSequenceLength = true;
    private static boolean checkValueLength = true;
    private static boolean checkFirstSubID012 = true;

    /**
     * The {@code MutableByte} class serves for exchanging type information
     * from the various decode* methods.
     *
     * @author Frank Fock
     * @version 1.0
     */
    public static class MutableByte {
        byte value = 0;

        public MutableByte() { }

        public MutableByte(byte value) {
            setValue(value);
        }

        public void setValue(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    /**
     * Encodes an ASN.1 header for an object with the ID and
     * length specified.
     * @param os
     *    an {@code OutputStream} to which the header is encoded.
     * @param type
     *    the type of the ASN.1 object. Must be &lt; 30, i.e. no extension octets.
     * @param length
     *    the length of the object. The maximum length is 0xFFFFFFFF;
     * @throws IOException
     *   if the output stream fails to store the encoded header.
     */
    public static void encodeHeader(OutputStream os, int type, int length)
            throws IOException
    {
        os.write(type);
        encodeLength(os, length);
    }

    /**
     * Encodes an ASN.1 header for an object with the ID and
     * length specified with a fixed length of the encoded length as supplied.
     * @param os
     *    an {@code OutputStream} to which the header is encoded.
     * @param type
     *    the type of the ASN.1 object. Must be &lt; 30, i.e. no extension octets.
     * @param length
     *    the length of the object. The maximum length is 0xFFFFFFFF;
     * @param numBytesLength
     *    the number of bytes used to encode the length of the length.
     * @throws IOException
     *   if the output stream fails to store the encoded header.
     */
    public static void encodeHeader(OutputStream os, int type, int length,
                                    int numBytesLength)
            throws IOException
    {
        os.write(type);
        encodeLength(os, length, numBytesLength);
    }

    /**
     * Compute the space needed to encode the length.
     *
     * @param length
     *    Length to encode
     * @return
     *    the count of bytes needed to encode the value {@code length}
     */
    public static int getBERLengthOfLength(int length) {
        if (length < 0) {
            return 5;
        }
        else if (length < 0x80){
            return 1;
        }
        else if (length <= 0xFF){
            return 2;
        }
        else if (length <= 0xFFFF) { /* 0xFF < length <= 0xFFFF */
            return 3;
        }
        else if (length <= 0xFFFFFF) { /* 0xFFFF < length <= 0xFFFFFF */
            return 4;
        }
        return 5;
    }

    /**
     * Encodes the length of an ASN.1 object.
     * @param os
     *   an {@code OutputStream} to which the length is encoded.
     * @param length
     *    the length of the object. The maximum length is 0xFFFFFFFF;
     * @throws IOException
     *   if the output stream fails to store the encoded length.
     */
    public static void encodeLength(OutputStream os, int length)
            throws IOException
    {
        if (length < 0) {
            os.write(0x04 | ASN_LONG_LEN);
            os.write((length >> 24) & 0xFF);
            os.write((length >> 16) & 0xFF);
            os.write((length >> 8) & 0xFF);
            os.write(length & 0xFF);
        }
        else if (length < 0x80){
            os.write(length);
        }
        else if (length <= 0xFF){
            os.write((0x01 | ASN_LONG_LEN));
            os.write(length);
        }
        else if (length <= 0xFFFF) { /* 0xFF < length <= 0xFFFF */
            os.write(0x02 | ASN_LONG_LEN);
            os.write((length >> 8) & 0xFF);
            os.write(length & 0xFF);
        }
        else if (length <= 0xFFFFFF) { /* 0xFFFF < length <= 0xFFFFFF */
            os.write(0x03 | ASN_LONG_LEN);
            os.write((length >> 16) & 0xFF);
            os.write((length >> 8) & 0xFF);
            os.write(length & 0xFF);
        }
        else {
            os.write(0x04 | ASN_LONG_LEN);
            os.write((length >> 24) & 0xFF);
            os.write((length >> 16) & 0xFF);
            os.write((length >> 8) & 0xFF);
            os.write(length & 0xFF);
        }
    }

    /**
     * Encodes the length of an ASN.1 object.
     * @param os
     *   an {@code OutputStream} to which the length is encoded.
     * @param length
     *    the length of the object. The maximum length is 0xFFFFFFFF;
     * @param numLengthBytes
     *    the number of bytes to be used to encode the length using the long
     *    form.
     * @throws IOException
     *   if the output stream fails to store the encoded length.
     */
    public static void encodeLength(OutputStream os, int length,
                                    int numLengthBytes)
            throws IOException
    {
        os.write((numLengthBytes | ASN_LONG_LEN));
        for (int i=(numLengthBytes-1)*8; i>=0; i-=8) {
            os.write(((length >> i) & 0xFF));
        }
    }

    /**
     * Encode a signed integer.
     * @param os
     *    an {@code OutputStream} to which the length is encoded.
     * @param type
     *    the tag type for the integer (typically 0x02)
     * @param value
     *    the integer value to encode.
     * @throws IOException
     *   if the output stream fails to store the encoded integer.
     */
    public static void encodeInteger(OutputStream os, byte type, int value)
            throws IOException
    {
        int integer = value;
        int mask;
        int intsize = 4;

        /*
         * Truncate "unnecessary" bytes off of the most significant end of this
         * 2's complement integer.  There should be no sequence of 9
         * consecutive 1's or 0's at the most significant end of the
         * integer.
         */
        mask = 0x1FF << ((8 * 3) - 1);
        /* mask is 0xFF800000 on a big-endian machine */
        while((((integer & mask) == 0) || ((integer & mask) == mask))
                && intsize > 1){
            intsize--;
            integer <<= 8;
        }
        encodeHeader(os, type, intsize);
        mask = 0xFF << (8 * 3);
        /* mask is 0xFF000000 on a big-endian machine */
        while ((intsize--) > 0){
            os.write(((integer & mask) >> (8 * 3)));
            integer <<= 8;
        }
    }

    /**
     * Encode a signed integer.
     * @param os
     *    an {@code OutputStream} to which the length is encoded.
     * @param type
     *    the tag type for the integer (typically 0x02)
     * @param value
     *    the integer value to encode.
     * @throws IOException
     *   if the output stream fails to store the encoded integer.
     */
    public static void encodeBigInteger(OutputStream os, byte type, BigInteger value)
            throws IOException
    {
        byte[] bytes = value.toByteArray();
        encodeHeader(os, type, bytes.length);
        os.write(bytes);
    }

    /**
     * Get the BER encoded length of a BigInteger value.
     * @param value
     *    a BigInteger value with a length that is less 2^31.
     * @return
     *    the length of the BER encoding of the supplied BigInteger as INTEGER value.
     */
    public static int getBigIntegerBERLength(BigInteger value) {
        int length = value.toByteArray().length;
        return length + getBERLengthOfLength(length) + 1;
    }


    /**
     * Encode an unsigned integer.
     * ASN.1 integer ::= 0x02 asnlength byte {byte}*
     * @param os
     *    an {@code OutputStream} to which the length is encoded.
     * @param type
     *    the tag type for the integer (typically 0x02)
     * @param value
     *    the integer value to encode.
     * @throws IOException
     *   if the output stream fails to store the encoded value.
     */
    public static void encodeUnsignedInteger(OutputStream os, byte type, long value)
            throws IOException
    {
        // figure out the len
        int len = 1;
        if ((( value >> 24) & LENMASK) != 0) {
            len = 4;
        }
        else if ((( value >> 16) & LENMASK) !=0) {
            len = 3;
        }
        else if ((( value >> 8) & LENMASK) !=0) {
            len = 2;
        }
        // check for 5 byte len where first byte will be
        // a null
        if ((( value >> (8 * (len -1))) & 0x080) !=0)	{
            len++;
        }

        // build up the header
        encodeHeader(os, type, len);  // length of BER encoded item

        // special case, add a null byte for len of 5
        if (len == 5) {
            os.write(0);
            for (int x=1; x<len; x++) {
                os.write((int) (value >> (8 * (4 - x) & LENMASK)));
            }
        }
        else
        {
            for (int x=0; x<len; x++) {
                os.write((int) (value >> (8 * ((len - 1) - x) & LENMASK)));
            }
        }
    }

    /**
     * Encode an ASN.1 octet string filled with the supplied input string.
     * @param os
     *    an {@code OutputStream} to which the length is encoded.
     * @param type
     *    the tag type for the integer (typically 0x02)
     * @param string
     *    the {@code byte} array containing the octet string value.
     * @throws IOException
     *   if the output stream fails to store the encoded value.
     */
    public static void encodeString(OutputStream os, byte type, byte[] string)
            throws IOException
    {
        /*
         * ASN.1 octet string ::= primstring | cmpdstring
         * primstring ::= 0x04 asnlength byte {byte}*
         * cmpdstring ::= 0x24 asnlength string {string}*
         * This code will never send a compound string.
         */
        encodeHeader(os, type, string.length);
        // fixed
        os.write(string);
    }

    /**
     * Encode an ASN.1 header for a sequence with the ID and length specified.
     * This only works on data types &lt; 30, i.e. no extension octets.
     * The maximum length is 0xFFFF;
     *
     * @param os
     *    an {@code OutputStream} to which the length is encoded.
     * @param type
     *    the tag type for the integer (typically 0x02)
     * @param length
     *    the length of the sequence to encode.
     * @throws IOException
     *   if the output stream fails to store the encoded value.
     */
    public static void encodeSequence(OutputStream os, byte type, int length)
            throws IOException
    {
        os.write(type);
        encodeLength(os, length);
    }

    /**
     * Gets the payload length in bytes of the BER encoded OID value.
     * @param value
     *    an array of unsigned integer values representing an object identifier.
     * @return
     *    the BER encoded length of the OID without header and length.
     */
    public static int getOIDLength(int[] value) {
        int length = 1;
        if (value.length > 1) {  // for first 2 subids, one sub-id is saved by special encoding
            length = getSubIDLength((value[0] * 40) + value[1]);
        }
        for (int i = 2; i < value.length; i++) {
            length += getSubIDLength(value[i]);
        }
        return length;
    }

    private static int getSubIDLength(int subID) {
        int length;
        long v = subID & 0xFFFFFFFFL;
        if (v < 0x80) { //  7 bits long subid
            length = 1;
        }
        else if (v < 0x4000) {  // 14 bits long subid
            length = 2;
        }
        else if (v < 0x200000) { // 21 bits long subid
            length = 3;
        }
        else if (v < 0x10000000) { // 28 bits long subid
            length = 4;
        }
        else {                     // 32 bits long subid
            length = 5;
        }
        return length;
    }

    /**
     * Encode an ASN.1 oid filled with the supplied oid value.
     *
     * @param os
     *    an {@code OutputStream} to which the length is encoded.
     * @param type
     *    the tag type for the integer (typically 0x06)
     * @param oid
     *    the {@code int} array containing the OID value.
     * @throws IOException
     *   if the output stream fails to store the encoded value.
     */
    public static void encodeOID(OutputStream os, byte type, int[] oid)
            throws IOException
    {
        /*
         * ASN.1 objid ::= 0x06 asnlength subidentifier {subidentifier}*
         * subidentifier ::= {leadingbyte}* lastbyte
         * leadingbyte ::= 1 7bitvalue
         * lastbyte ::= 0 7bitvalue
         */
        encodeHeader(os, type, getOIDLength(oid));

        int encodedLength = oid.length;
        int rpos = 0;

        if (oid.length < 2){
            os.write(0);
            encodedLength = 0;
        }
        else {
            int firstSubID = oid[0];
            if (checkFirstSubID012 && (firstSubID < 0 || firstSubID > 2)) {
                throw new IOException("Invalid first sub-identifier (must be 0, 1, or 2)");
            }
            encodeSubID(os, oid[1] + (firstSubID * 40));
            encodedLength -= 2;
            rpos = 2;
        }

        while (encodedLength-- > 0){
            encodeSubID(os, oid[rpos++]);
        }
    }

    private static void encodeSubID(OutputStream os, int subID) throws IOException {
        long subid = (subID & 0xFFFFFFFFL);
        if (subid < 127) {
            os.write((int)subid & 0xFF);
        }
        else {
            long mask = 0x7F; /* handle subid == 0 case */
            long bits = 0;

            /* testmask *MUST* !!!! be of an unsigned type */
            for (long testmask = 0x7F, testbits = 0; testmask != 0;
                 testmask <<= 7, testbits += 7) {
                if ((subid & testmask) > 0) {	/* if any bits set */
                    mask = testmask;
                    bits = testbits;
                }
            }
            /* mask can't be zero here */
            for (; mask != 0x7F; mask >>= 7, bits -= 7){
                /* fix a mask that got truncated above */
                if (mask == 0x1E00000) {
                    mask = 0xFE00000;
                }
                os.write((int)(((subid & mask) >> bits) | ASN_BIT8));
            }
            os.write((int)(subid & mask));
        }
    }


    public static void encodeUnsignedInt64(OutputStream os, byte type, long value)
            throws IOException
    {
        int len;
        /*
         * Truncate "unnecessary" bytes off of the most significant end of this
         * 2's complement integer.  There should be no sequence of 9
         * consecutive 1's or 0's at the most significant end of the
         * integer.
         */
        for (len = 8; len > 1; len--) {
            if (((value >> (8 * (len - 1))) & 0xFF) != 0) {
                break;
            }
        }
        if ((( value >> (8 * (len -1))) & 0x080) !=0) {
            len++;
        }
        encodeHeader(os, type, len);
        if (len == 9) {
            os.write(0);
            len--;
        }
        for (int x=0; x<len; x++) {
            os.write((int) (value >> (8 * ((len - 1) - x) & LENMASK)));
        }
    }

    /**
     * Decodes a ASN.1 length.
     * @param is
     *    an {@code InputStream}
     * @return
     *    the decoded length.
     * @throws IOException
     *   if the input stream contains an invalid BER encoding or an IO
     *   exception occurred while reading from the stream.
     */
    public static int decodeLength(BERInputStream is)
            throws IOException
    {
        return decodeLength(is, true);
    }

    /**
     * Decodes a ASN.1 length.
     * @param is
     *    an {@code InputStream}
     * @param checkLength
     *    if {@code false} length check is always suppressed.
     * @return
     *    the decoded length.
     * @throws IOException
     *   if the input stream contains an invalid BER encoding or an IO
     *   exception occurred while reading from the stream.
     */
    public static int decodeLength(BERInputStream is, boolean checkLength)
            throws IOException
    {
        int length = 0;
        int lengthbyte = is.read();

        if ((lengthbyte & ASN_LONG_LEN) > 0) {
            lengthbyte &= ~ASN_LONG_LEN;	/* turn MSb off */
            if (lengthbyte == 0){
                throw new IOException("Indefinite lengths are not supported");
            }
            if (lengthbyte > 4){
                throw new IOException("Data length > 4 bytes are not supported!");
            }
            for (int i=0; i<lengthbyte; i++) {
                int l = is.read() & 0xFF;
                length |= (l << (8*((lengthbyte-1)-i)));
            }
            if (length < 0) {
                throw new IOException("SNMP does not support data lengths > 2^31");
            }
        }
        else { /* short asnlength */
            length = lengthbyte & 0xFF;
        }
        /*
         * If activated we do a length check here: length > is.available() then throw
         * exception
         */
        if (checkLength) {
            checkLength(is, length);
        }
        return length;
    }

    /**
     * Decodes an ASN.1 header for an object with the ID and
     * length specified.
     *  On entry, datalength is input as the number of valid bytes following
     *   "data".  On exit, it is returned as the number of valid bytes
     *   in this object following the id and length.
     *
     *  This only works on data types &lt; 30, i.e. no extension octets.
     *  The maximum length is 0xFFFF;
     *
     * @param is
     *   the BERInputStream to decode.
     * @param type
     *   returns the type of the object at the current position in the input
     *   stream.
     * @param checkLength
     *    if {@code false} length check is always suppressed.
     * @return
     *   the decoded length of the object.
     * @throws IOException
     *   if the input stream contains an invalid BER encoding or an IO
     *   exception occurred while reading from the stream.
     */
    public static int decodeHeader(BERInputStream is, MutableByte type,
                                   boolean checkLength)
            throws IOException
    {
        /* this only works on data types &lt; 30, i.e. no extension octets */
        byte t = (byte)is.read();
        if ((t & ASN_EXTENSION_ID) == ASN_EXTENSION_ID) {
            throw new IOException("Cannot process extension IDs"+
                    getPositionMessage(is));
        }
        type.setValue(t);
        return decodeLength(is, checkLength);
    }

    /**
     * Decodes an ASN.1 header for an object with the ID and
     * length specified.
     *  On entry, datalength is input as the number of valid bytes following
     *   "data".  On exit, it is returned as the number of valid bytes
     *   in this object following the id and length.
     *
     *  This only works on data types &lt; 30, i.e. no extension octets.
     *  The maximum length is 0xFFFF;
     *
     * @param is
     *   the BERInputStream to decode.
     * @param type
     *   returns the type of the object at the current position in the input
     *   stream.
     * @return
     *   the decoded length of the object.
     * @throws IOException
     *   if the input stream contains an invalid BER encoding or an IO
     *   exception occurred while reading from the stream.
     */
    public static int decodeHeader(BERInputStream is, MutableByte type)
            throws IOException
    {
        return decodeHeader(is, type, true);
    }

    public static int decodeInteger(BERInputStream is, MutableByte type)
            throws IOException
    {
        int length;
        int value = 0;

        type.setValue((byte)is.read());

        if ((type.value != 0x02) && (type.value != 0x43) &&
                (type.value != 0x41)) {
            throw new IOException("Wrong ASN.1 type. Not an integer: "+type.value+
                    getPositionMessage(is));
        }
        length = decodeLength(is);
        if (length > 4) {
            throw new IOException("Length greater than 32bit are not supported "+
                    " for integers: "+getPositionMessage(is));
        }
        int b = is.read() & 0xFF;
        if ((b & 0x80) > 0) {
            value = -1; /* integer is negative */
        }
        while (length-- > 0) {
            value = (value << 8) | b;
            if (length > 0) {
                b = is.read();
            }
        }
        return value;
    }

    public static BigInteger decodeBigInteger(BERInputStream is, MutableByte type)
            throws IOException
    {
        int length;
        type.setValue((byte)is.read());

        if (type.value != 0x02) {
            throw new IOException("Wrong ASN.1 type. Not an INTEGER: "+type.value+
                    getPositionMessage(is));
        }
        length = decodeLength(is);
        if (length < 0) {
            throw new IOException("Length greater than "+Integer.MAX_VALUE+" are not supported "+
                    " for integers: "+getPositionMessage(is));
        }
        byte[] bytes = new byte[length];
        int actualRead = is.read(bytes);
        if (actualRead != length) {
            throw new IOException("Length of INTEGER ("+length+") is greater than number of bytes left in BER stream: "+
                    actualRead);
        }
        return new BigInteger(bytes);
    }


    private static String getPositionMessage(BERInputStream is) {
        return " at position "+is.getPosition();
    }

    public static long decodeUnsignedInteger(BERInputStream is, MutableByte type)
            throws IOException
    {
        int	length;
        long value = 0;

        // get the type
        type.setValue((byte)is.read());
        if ((type.value != 0x02) && (type.value != 0x43) &&
                (type.value != 0x41) && (type.value != 0x42) &&
                (type.value != 0x47)) {
            throw new IOException("Wrong ASN.1 type. Not an unsigned integer: "+
                    type.value+
                    getPositionMessage(is));
        }
        // pick up the len
        length = decodeLength(is);

        // check for legal uint size
        int b = is.read();
        if ((length > 5) || ((length > 4) && (b != 0x00))) {
            throw new IOException("Only 32bit unsigned integers are supported"+
                    getPositionMessage(is));
        }

        // check for leading  0 octet
        if (b == 0x00) {
            if (length > 1) {
                b = is.read();
            }
            length--;
        }

        // calculate the value
        for (int i=0; i<length; i++) {
            value = (value << 8) | (b & 0xFF);
            if (i+1<length) {
                b = is.read();
            }
        }
        return value;
    }

    public static byte[] decodeString(BERInputStream is, MutableByte type)
            throws IOException
    {
        /*
         * ASN.1 octet string ::= primstring | cmpdstring
         * primstring ::= 0x04 asnlength byte {byte}*
         * cmpdstring ::= 0x24 asnlength string {string}*
         * ipaddress  ::= 0x40 4 byte byte byte byte
         */
        // get the type
        type.setValue((byte)is.read());
        if ((type.value != BER.OCTETSTRING) && (type.value != 0x24) &&
                (type.value != BER.IPADDRESS) && (type.value != BER.OPAQUE) &&
                (type.value != BER.BITSTRING) &&
                (type.value != 0x45)) {
            throw new IOException("Wrong ASN.1 type. Not a string: "+type.value+getPositionMessage(is));
        }
        int length = decodeLength(is);

        byte[] value = new byte[length];

        if (length > 0) {
            int read = is.read(value, 0, length);
            if ((read < 0) || (read < length)) {
                throw new IOException("Wrong string length " + read + " < " + length);
            }
        }
        return value;
    }


    public static int[] decodeOID(BERInputStream is, MutableByte type)
            throws IOException
    {
        /*
         * ASN.1 objid ::= 0x06 asnlength subidentifier {subidentifier}*
         * subidentifier ::= {leadingbyte}* lastbyte
         * leadingbyte ::= 1 7bitvalue
         * lastbyte ::= 0 7bitvalue
         */
        int subidentifier;
        int length;

        // get the type
        type.setValue((byte)is.read());
        if (type.value != 0x06) {
            throw new IOException("Wrong type. Not an OID: "+type.value+
                    getPositionMessage(is));
        }
        length = decodeLength(is);

        int[] oid = new int[length+2];
        /* Handle invalid object identifier encodings of the form 06 00 robustly */
        if (length == 0) {
            oid[0] = oid[1] = 0;
        }
        int pos = 1;
        while (length > 0){
            subidentifier = 0;
            int b;
            do {	/* shift and add in low order 7 bits */
                int next = is.read();
                if (next < 0) {
                    throw new IOException("Unexpected end of input stream" +
                            getPositionMessage(is));
                }
                b = next & 0xFF;
                subidentifier = (subidentifier << 7) + (b & ~ASN_BIT8);
                length--;
            } while ((length > 0) && ((b & ASN_BIT8) != 0));	/* last byte has high bit clear */
            oid[pos++] = subidentifier;
        }

        /*
         * The first two subidentifiers are encoded into the first component
         * with the value (X * 40) + Y, where:
         * X is the value of the first subidentifier.
         * Y is the value of the second subidentifier.
         */
        subidentifier = oid[1];
        if (subidentifier == 0x2B){
            oid[0] = 1;
            oid[1] = 3;
        }
        else if (subidentifier >= 0 && subidentifier < 80) {
            if (subidentifier < 40) {
                oid[0] = 0;
                oid[1] = subidentifier;
            }
            else {
                oid[0] = 1;
                oid[1] = subidentifier - 40;
            }
        }
        else {
            oid[0] = 2;
            oid[1] = subidentifier - 80;
        }
        if (pos < 2) {
            pos = 2;
        }
        int[] value = new int[pos];
        System.arraycopy(oid, 0, value, 0, pos);
        return value;
    }

    public static void decodeNull(BERInputStream is, MutableByte type)
            throws IOException
    {
        // get the type
        type.setValue((byte)(is.read() & 0xFF));
        if ((type.value != (byte)0x05) && (type.value != (byte)0x80) &&
                (type.value != (byte)0x81) && (type.value != (byte)0x82)) {
            throw new IOException("Wrong ASN.1 type. Is not null: " + type.value+
                    getPositionMessage(is));
        }
        int length = decodeLength(is);
        if (length != 0) {
            throw new IOException("Invalid Null encoding, length is not zero: "+
                    length+getPositionMessage(is));
        }
    }

    public static long decodeUnsignedInt64(BERInputStream is, MutableByte type)
            throws IOException
    {
        // get the type
        type.setValue((byte)is.read());
        if ((type.value != 0x02) && (type.value != 0x46)) {
            throw new IOException("Wrong type. Not an integer 64: "+type.value+
                    getPositionMessage(is));
        }
        int length = decodeLength(is);
        int b = is.read() & 0xFF;
        if (length > 9) {
            throw new IOException("Invalid 64bit unsigned integer length: "+length+
                    getPositionMessage(is));
        }
        // check for leading  0 octet
        if (b == 0x00) {
            if (length > 1) {
                b = is.read();
            }
            length--;
        }
        long value = 0;
        // calculate the value
        for (int i=0; i<length; i++) {
            value = (value << 8) | (b & 0xFF);
            if (i+1<length) {
                b = is.read();
            }
        }
        return value;
    }

    /**
     * Gets the SEQUENCE length checking mode.
     * @return
     *    {@code true} if the length of a parsed SEQUENCE should be checked
     *    against the real length of the objects parsed.
     */
    public static boolean isCheckSequenceLength() {
        return checkSequenceLength;
    }

    /**
     * Sets the application wide SEQUENCE length checking mode.
     * @param checkSequenceLen
     *    specifies whether he length of a parsed SEQUENCE should be checked
     *    against the real length of the objects parsed.
     */
    public static void setCheckSequenceLength(boolean checkSequenceLen) {
        checkSequenceLength = checkSequenceLen;
    }

    /**
     * Checks the length of a sequence, by computing the expected payload length and comparing it with the expected
     * length as given in the header of the BER sequence. If {@link #isCheckSequenceLength()} and the length differ
     * an {@link IOException} is thrown.
     * @param expectedLength
     *     the expected length as encoded in the header of the BER sequence to check.
     * @param sequence
     *     the BER sequence to validate.
     * @throws IOException
     *     is thrown if {@link #isCheckSequenceLength()} and the length differ.
     * @deprecated Use {@link #checkSequenceLength(int, int, BERSerializable)} because it is immune against longer
     *     than minimum numeric value encodings.
     */
    public static void checkSequenceLength(int expectedLength, BERSerializable sequence)
            throws IOException
    {
        if ((isCheckSequenceLength()) && (expectedLength != sequence.getBERPayloadLength())) {
            throw new IOException("The actual length of the SEQUENCE object "+
                    sequence.getClass().getName()+
                    " is "+sequence.getBERPayloadLength()+", but "+
                    expectedLength+" was expected");
        }
    }

    /**
     * Checks the length of a sequence, by computing the expected payload length and comparing it with the actual
     * length as given. If {@link #isCheckSequenceLength()} and the length differ an {@link IOException} is thrown.
     * @param expectedLength
     *     the expected length as encoded in the header of the BER sequence to check.
     * @param actualLength
     *     the number of bytes actually decoded from the BER stream for this sequence.
     * @param sequence
     *     the BER sequence to validate.
     * @throws IOException
     *     is thrown if {@link #isCheckSequenceLength()} and the length differ.
     */
    public static void checkSequenceLength(int expectedLength, int actualLength, BERSerializable sequence)
            throws IOException
    {
        if ((isCheckSequenceLength()) && (expectedLength != actualLength)) {
            throw new IOException("The actual length of the SEQUENCE object "+
                    sequence.getClass().getName()+
                    " is "+actualLength+", but "+
                    expectedLength+" was expected");
        }
    }

    /**
     * Checks whether the length of that was encoded is also available from the
     * stream.
     *
     * @param is InputStream
     * @param length int
     * @throws IOException
     *    if the bytes that are given in length cannot be read from the input
     *    stream (without blocking).
     */
    private static void checkLength(BERInputStream is, int length) throws
            IOException {
        if (!checkValueLength) {
            return;
        }
        if ((length < 0) || (length > is.getAvailableBytes())) {
            throw new IOException("The encoded length "+
                    length+
                    " exceeds the number of bytes left in input"+
                    getPositionMessage(is)+
                    " which actually is "+is.getAvailableBytes());
        }
    }

    public boolean isCheckValueLength() {
        return checkValueLength;
    }

    public void setCheckValueLength(boolean checkValueLength) {
        BER.checkValueLength = checkValueLength;
    }

    public static boolean isCheckFirstSubID012() {
        return checkFirstSubID012;
    }

    public static void setCheckFirstSubID012(boolean checkFirstSubID012) {
        BER.checkFirstSubID012 = checkFirstSubID012;
    }
}


