/*_############################################################################
  _## 
  _##  SNMP4J - TestAuthMD5.java  
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


package org.snmp4j.security;

import org.snmp4j.log.LogAdapter;
import org.snmp4j.log.LogFactory;
import org.snmp4j.smi.OctetString;
import junit.framework.*;

public class TestAuthMD5 extends TestCase {
    private static LogAdapter cat = LogFactory.getLogger(TestAuthMD5.class);

    public TestAuthMD5(String name) {

        super(name);
    }

    public static String asHex(byte buf[]) {
        return new OctetString(buf).toHexString();
    }


    protected void setUp() {
    }

    protected void tearDown() {
    }

    public void testPasswordToKey1() {
        String password = "maplesyrup";
        byte[] engineId = {
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02};
        byte[] expectedKey = {
                (byte) 0x52, (byte) 0x6f, (byte) 0x5e, (byte) 0xed,
                (byte) 0x9f, (byte) 0xcc, (byte) 0xe2, (byte) 0x6f,
                (byte) 0x89, (byte) 0x64, (byte) 0xc2, (byte) 0x93,
                (byte) 0x07, (byte) 0x87, (byte) 0xd8, (byte) 0x2b};

        AuthMD5 auth = new AuthMD5();

        byte[] key = auth.passwordToKey(new OctetString(password), engineId);
        assertEquals(expectedKey.length, key.length);
        for (int i = 0; i < key.length; i++) {
            assertEquals(key[i], expectedKey[i]);
        }
    }

    public void testPasswordToKey2() {
        String password = "newsyrup";
        byte[] engineId = {
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02};
        byte[] expectedKey = {
                (byte) 0x87, (byte) 0x02, (byte) 0x1d, (byte) 0x7b,
                (byte) 0xd9, (byte) 0xd1, (byte) 0x01, (byte) 0xba,
                (byte) 0x05, (byte) 0xea, (byte) 0x6e, (byte) 0x3b,
                (byte) 0xf9, (byte) 0xd9, (byte) 0xbd, (byte) 0x4a};

        AuthMD5 auth = new AuthMD5();
        byte[] key = auth.passwordToKey(new OctetString(password), engineId);
        assertEquals(expectedKey.length, key.length);
        for (int i = 0; i < key.length; i++) {
            assertEquals(expectedKey[i], key[i]);
        }
    }

    public void testChangeDelta() {
        String oldPass = "maplesyrup";
        String newPass = "newsyrup";
        byte[] oldKey;
        byte[] newKey;
        byte[] random = {
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        byte[] engineId = {
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02};
        byte[] expectedDelta = {
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x88, (byte) 0x05, (byte) 0x61, (byte) 0x51,
                (byte) 0x41, (byte) 0x67, (byte) 0x6c, (byte) 0xc9,
                (byte) 0x19, (byte) 0x61, (byte) 0x74, (byte) 0xe7,
                (byte) 0x42, (byte) 0xa3, (byte) 0x25, (byte) 0x51};

        AuthMD5 auth = new AuthMD5();
        oldKey = auth.passwordToKey(new OctetString(oldPass), engineId);
        newKey = auth.passwordToKey(new OctetString(newPass), engineId);
        byte[] delta = auth.changeDelta(oldKey, newKey, random);
        assertEquals(expectedDelta.length, delta.length);
        for (int i = 0; i < delta.length; i++) {
            assertEquals(delta[i], expectedDelta[i]);
        }
    }

    public void testAuth() {
        byte[] msg = {
                (byte) 0x30, (byte) 0x7A, (byte) 0x02, (byte) 0x01,
                (byte) 0x03, (byte) 0x30, (byte) 0x0F, (byte) 0x02,
                (byte) 0x03, (byte) 0x00, (byte) 0xDE, (byte) 0xAD,
                (byte) 0x02, (byte) 0x02, (byte) 0x10, (byte) 0x00,
                (byte) 0x04, (byte) 0x01, (byte) 0x05, (byte) 0x02,
                (byte) 0x01, (byte) 0x03, (byte) 0x04, (byte) 0x31,
                (byte) 0x30, (byte) 0x2F, (byte) 0x04, (byte) 0x11,
                (byte) 0x80, (byte) 0x00, (byte) 0x13, (byte) 0x70,
                (byte) 0x05, (byte) 0x65, (byte) 0x6E, (byte) 0x74,
                (byte) 0x65, (byte) 0x72, (byte) 0x70, (byte) 0x72,
                (byte) 0x69, (byte) 0x73, (byte) 0x65, (byte) 0x12,
                (byte) 0x5C, (byte) 0x02, (byte) 0x01, (byte) 0x07,
                (byte) 0x02, (byte) 0x02, (byte) 0x04, (byte) 0x9F,
                (byte) 0x04, (byte) 0x03, (byte) 0x4D, (byte) 0x44,
                (byte) 0x35, (byte) 0x04, (byte) 0x0C,
                (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x04,
                (byte) 0x00, (byte) 0x30, (byte) 0x31,
                (byte) 0x04, (byte) 0x11, (byte) 0x80,
                (byte) 0x00, (byte) 0x13, (byte) 0x70,
                (byte) 0x05, (byte) 0x65, (byte) 0x6E,
                (byte) 0x74, (byte) 0x65, (byte) 0x72,
                (byte) 0x70, (byte) 0x72, (byte) 0x69,
                (byte) 0x73, (byte) 0x65, (byte) 0x12,
                (byte) 0x5C, (byte) 0x04, (byte) 0x00,
                (byte) 0xA0, (byte) 0x1A, (byte) 0x02,
                (byte) 0x02, (byte) 0x34, (byte) 0x28,
                (byte) 0x02, (byte) 0x01, (byte) 0x00,
                (byte) 0x02, (byte) 0x01, (byte) 0x00,
                (byte) 0x30, (byte) 0x0E, (byte) 0x30,
                (byte) 0x0C, (byte) 0x06, (byte) 0x08,
                (byte) 0x2B, (byte) 0x06, (byte) 0x01,
                (byte) 0x02, (byte) 0x01, (byte) 0x01,
                (byte) 0x01, (byte) 0x00, (byte) 0x05,
                (byte) 0x00};
        byte[] key = {
                (byte) 0x38, (byte) 0xD0, (byte) 0x2F, (byte) 0x07,
                (byte) 0xF2, (byte) 0xAB, (byte) 0xFA, (byte) 0x10,
                (byte) 0xDE, (byte) 0x13, (byte) 0x66, (byte) 0xB7,
                (byte) 0xAA, (byte) 0xAC, (byte) 0xDE, (byte) 0x70};

        byte[] expectedDigest = {
                (byte) 0xBC, (byte) 0x6B, (byte) 0xC2, (byte) 0xD2,
                (byte) 0x3B, (byte) 0x54, (byte) 0xA2, (byte) 0xEA,
                (byte) 0x6E, (byte) 0xC4, (byte) 0x01, (byte) 0x66,
                (byte) 0x68, (byte) 0x20, (byte) 0xC6, (byte) 0xB0
        };
        AuthMD5 auth = new AuthMD5();
        cat.debug("start authenticate");
        cat.debug("msg before: " + asHex(msg));
        boolean res = auth.authenticate(key, msg, 0,
                msg.length,
                new ByteArrayWindow(msg, 59, 12));
        cat.debug("msg after: " + asHex(msg));

        assertEquals(true, res);
        for (int i = 0; i < 12; i++) {
            cat.debug("" + i);
            assertEquals(expectedDigest[i], msg[59 + i]);
        }
    }

    public void testAuthenticate() {
        byte[] msg = {
                /* Some Dummy values */
                (byte) 0xff, (byte) 0xfe, (byte) 0xfd, (byte) 0xfc, (byte) 0xfb,
                /* Message */
                (byte) 0x30, (byte) 0x82, (byte) 0x00, (byte) 0xA5,
                (byte) 0x02, (byte) 0x01, (byte) 0x03, (byte) 0x30,
                (byte) 0x82, (byte) 0x00, (byte) 0x0F, (byte) 0x02,
                (byte) 0x03, (byte) 0x18, (byte) 0x00, (byte) 0x02,
                (byte) 0x02, (byte) 0x02, (byte) 0x10, (byte) 0x00,
                (byte) 0x04, (byte) 0x01, (byte) 0x01, (byte) 0x02,
                (byte) 0x01, (byte) 0x03, (byte) 0x04, (byte) 0x33,
                (byte) 0x30, (byte) 0x82, (byte) 0x00, (byte) 0x2F,
                (byte) 0x04, (byte) 0x11, (byte) 0x80, (byte) 0x00,
                (byte) 0x13, (byte) 0x70, (byte) 0x05, (byte) 0x65,
                (byte) 0x6E, (byte) 0x74, (byte) 0x65, (byte) 0x72,
                (byte) 0x70, (byte) 0x72, (byte) 0x69, (byte) 0x73,
                (byte) 0x65, (byte) 0x12, (byte) 0x5C, (byte) 0x02,
                (byte) 0x01, (byte) 0x65, (byte) 0x02, (byte) 0x02,
                (byte) 0x02, (byte) 0x4C, (byte) 0x04, (byte) 0x03,
                (byte) 0x4D, (byte) 0x44, (byte) 0x35, (byte) 0x04,
                (byte) 0x0C,
                /* auth beg*/(byte) 0x01, (byte) 0x02, (byte) 0x03,
                (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
                (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b,
                (byte) 0x0c,
                /* end     */(byte) 0x04, (byte) 0x00, (byte) 0x30,
                (byte) 0x82, (byte) 0x00, (byte) 0x56, (byte) 0x04,
                (byte) 0x11, (byte) 0x80, (byte) 0x00, (byte) 0x13,
                (byte) 0x70, (byte) 0x05, (byte) 0x65, (byte) 0x6E,
                (byte) 0x74, (byte) 0x65, (byte) 0x72, (byte) 0x70,
                (byte) 0x72, (byte) 0x69, (byte) 0x73, (byte) 0x65,
                (byte) 0x12, (byte) 0x5C, (byte) 0x04, (byte) 0x00,
                (byte) 0xA2, (byte) 0x3F, (byte) 0x02, (byte) 0x02,
                (byte) 0x34, (byte) 0x28, (byte) 0x02, (byte) 0x01,
                (byte) 0x00, (byte) 0x02, (byte) 0x01, (byte) 0x00,
                (byte) 0x30, (byte) 0x33, (byte) 0x30, (byte) 0x82,
                (byte) 0x00, (byte) 0x2F, (byte) 0x06, (byte) 0x08,
                (byte) 0x2B, (byte) 0x06, (byte) 0x01, (byte) 0x02,
                (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00,
                (byte) 0x04, (byte) 0x23, (byte) 0x41, (byte) 0x47,
                (byte) 0x45, (byte) 0x4E, (byte) 0x54, (byte) 0x2B,
                (byte) 0x2B, (byte) 0x76, (byte) 0x33, (byte) 0x2E,
                (byte) 0x35, (byte) 0x2E, (byte) 0x31, (byte) 0x32,
                (byte) 0x20, (byte) 0x41, (byte) 0x54, (byte) 0x4D,
                (byte) 0x20, (byte) 0x53, (byte) 0x69, (byte) 0x6D,
                (byte) 0x75, (byte) 0x6C, (byte) 0x61, (byte) 0x74,
                (byte) 0x69, (byte) 0x6F, (byte) 0x6E, (byte) 0x20,
                (byte) 0x41, (byte) 0x67, (byte) 0x65, (byte) 0x6E,
                (byte) 0x74,
                /* And again some dummy values */
                (byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef, (byte) 0x00};
        byte[] engineId = {
                (byte) 0x80, (byte) 0x00,
                (byte) 0x13, (byte) 0x70, (byte) 0x05, (byte) 0x65,
                (byte) 0x6E, (byte) 0x74, (byte) 0x65, (byte) 0x72,
                (byte) 0x70, (byte) 0x72, (byte) 0x69, (byte) 0x73,
                (byte) 0x65, (byte) 0x12, (byte) 0x5C};
        byte[] authCode = {
                (byte) 0xC4, (byte) 0x51, (byte) 0x37, (byte) 0xE7,
                (byte) 0xEC, (byte) 0x7A, (byte) 0x7F, (byte) 0x6C,
                (byte) 0x15, (byte) 0x9D, (byte) 0x43, (byte) 0x05};
        int messageOffset = 5;
        int messageLength = msg.length - messageOffset - messageOffset;
        int digestOffset = (16 * 4) + 1;

        byte[] expectedMsg = (byte[]) msg.clone();

        System.arraycopy(authCode, 0, expectedMsg, digestOffset + messageOffset, authCode.length);

        AuthMD5 auth = new AuthMD5();
        byte[] key = auth.passwordToKey(new OctetString("MD5UserAuthPassword"), engineId);

        boolean res = auth.authenticate(key, msg, messageOffset,
                messageLength,
                new ByteArrayWindow(msg,
                        messageOffset + digestOffset,
                        12));

        assertEquals(true, res);

        assertEquals(expectedMsg.length, msg.length);
        for (int i = 0; i < msg.length; i++) {
            assertEquals(msg[i], expectedMsg[i]);
        }

        byte[] tmsg = (byte[]) msg.clone();
        res = auth.isAuthentic(key, tmsg, messageOffset, messageLength,
                new ByteArrayWindow(tmsg, messageOffset + digestOffset, 12));
        assertEquals(true, res);

        tmsg = (byte[]) msg.clone();
        tmsg[33] = (byte) (tmsg[33] + 5);
        res = auth.isAuthentic(key, tmsg, messageOffset, messageLength,
                new ByteArrayWindow(tmsg, messageOffset + digestOffset, 12));
        assertEquals(false, res);

        tmsg = (byte[]) msg.clone();
        res = auth.isAuthentic(key, tmsg, messageOffset, messageLength + 1,
                new ByteArrayWindow(tmsg, messageOffset + digestOffset, 12));
        assertEquals(false, res);

        tmsg = (byte[]) msg.clone();
        res = auth.isAuthentic(key, tmsg, messageOffset, messageLength - 1,
                new ByteArrayWindow(tmsg, messageOffset + digestOffset, 12));
        assertEquals(false, res);

        tmsg = (byte[]) msg.clone();
        res = auth.isAuthentic(key, tmsg, messageOffset + 1, messageLength,
                new ByteArrayWindow(tmsg, messageOffset + 1 + digestOffset, 12));
        assertEquals(false, res);

        tmsg = (byte[]) msg.clone();
        res = auth.isAuthentic(key, tmsg, messageOffset - 1, messageLength,
                new ByteArrayWindow(tmsg, messageOffset - 1 + digestOffset, 12));
        assertEquals(false, res);

        tmsg = (byte[]) msg.clone();
        res = auth.isAuthentic(key, tmsg, messageOffset, messageLength,
                new ByteArrayWindow(tmsg, messageOffset + digestOffset + 1, 12));
        assertEquals(false, res);

        tmsg = (byte[]) msg.clone();
        res = auth.isAuthentic(key, tmsg, messageOffset, messageLength,
                new ByteArrayWindow(tmsg, messageOffset + digestOffset - 1, 12));
        assertEquals(false, res);

        tmsg = (byte[]) msg.clone();
        byte[] shortKey = new byte[2];
        System.arraycopy(key, 0, shortKey, 0, 2);
        res = auth.isAuthentic(shortKey, tmsg, messageOffset, messageLength,
                new ByteArrayWindow(tmsg, messageOffset + digestOffset, 12));
        assertEquals(false, res);


        key[2] = (byte) (key[2] + 1);
        res = auth.isAuthentic(key, msg, messageOffset, messageLength,
                new ByteArrayWindow(msg, messageOffset + digestOffset, 12));
        assertEquals(false, res);
    }
}
