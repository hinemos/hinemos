/*_############################################################################
  _## 
  _##  SNMP4J - DHParameters.java  
  _## 
  _##  Copyright (C) 2003-2020  Frank Fock and Jochen Katz (SNMP4J.org)
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

package org.snmp4j.security.dh;

import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.asn1.BEROutputStream;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OctetString;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * The Diffie Hellman Parameter interface provides public the parameters needed for doing a Diffie-Hellman
 * key agreement.
 *
 * @author Frank Fock
 * @since 2.6.0
 */
public class DHParameters implements Serializable {

    private BigInteger prime;
    private BigInteger generator;
    private int privateValueLength;

    /**
     * Default DHParameters as suggested by RFC 2786, usmDHParameters (p = {@link DHGroups#P1}, g = {@link DHGroups#G},
     * and private value length = 16.
     */
    public static final DHParameters DEFAULT = new DHParameters(DHGroups.P1, DHGroups.G, 16);

    public DHParameters(BigInteger prime, BigInteger generator, int privateValueLength) {
        this.prime = prime;
        this.generator = generator;
        this.privateValueLength = privateValueLength;
    }

    public static OctetString encodeBER(BigInteger prime, BigInteger generator, int privateValueLength) {
        int lengthOfPrime = BER.getBigIntegerBERLength(prime);
        int lengthOfGenerator = BER.getBigIntegerBERLength(generator);
        int lengthOfPrivateValueLength = 0;
        if (privateValueLength != 0) {
            lengthOfPrivateValueLength = new Integer32(privateValueLength).getBERLength();
        }
        int sequenceLength = lengthOfGenerator + lengthOfPrime + lengthOfPrivateValueLength;
        int capacity = sequenceLength + BER.getBERLengthOfLength(sequenceLength) + 1;
        ByteBuffer byteBuffer = ByteBuffer.allocate(capacity);
        BEROutputStream outputStream = new BEROutputStream(byteBuffer);
        try {
            BER.encodeSequence(outputStream, BER.ASN_SEQUENCE, sequenceLength);
            BER.encodeBigInteger(outputStream, BER.ASN_INTEGER, prime);
            BER.encodeBigInteger(outputStream, BER.ASN_INTEGER, generator);
            if (privateValueLength != 0) {
                BER.encodeInteger(outputStream, BER.ASN_INTEGER, privateValueLength);
            }
        } catch (IOException e) {
            return null;
        }
        return new OctetString(outputStream.getBuffer().array());
    }

    public static DHParameters getDHParametersFromBER(OctetString berValue) throws IOException {
        BERInputStream inputStream = new BERInputStream(ByteBuffer.wrap(berValue.getValue()));
        BER.MutableByte sequenceType = new BER.MutableByte();
        int sequenceLength = BER.decodeHeader(inputStream, sequenceType);
        long headerOffset = inputStream.getPosition();
        BER.MutableByte integerType = new BER.MutableByte();
        BigInteger prime = BER.decodeBigInteger(inputStream, integerType);
        BigInteger generator = BER.decodeBigInteger(inputStream, integerType);
        int privateValueLength = 0;
        if (inputStream.available() > 0 && inputStream.getPosition() < sequenceLength + headerOffset) {
            privateValueLength = BER.decodeInteger(inputStream, integerType);
        }
        return new DHParameters(prime, generator, privateValueLength);
    }

    public BigInteger getPrime() {
        return prime;
    }

    public BigInteger getGenerator() {
        return generator;
    }

    public int getPrivateValueLength() {
        return privateValueLength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DHParameters that = (DHParameters) o;

        if (getPrivateValueLength() != that.getPrivateValueLength()) return false;
        if (!getPrime().equals(that.getPrime())) return false;
        return getGenerator().equals(that.getGenerator());
    }

    @Override
    public int hashCode() {
        int result = getPrime().hashCode();
        result = 31 * result + getGenerator().hashCode();
        result = 31 * result + getPrivateValueLength();
        return result;
    }

    @Override
    public String toString() {
        return "DHParameters{" +
                "prime=" + prime +
                ", generator=" + generator +
                ", privateValueLength=" + privateValueLength +
                '}';
    }
}
