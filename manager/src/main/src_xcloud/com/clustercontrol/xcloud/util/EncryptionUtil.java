/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class EncryptionUtil {
    // 使用する暗号化アルゴリズム
	public static String algorithm = "BLOWFISH";
	
	public static String crypt(String word, String key) throws Exception {
        // 暗号化
        SecretKeySpec sksSpec = new SecretKeySpec(key.getBytes(), algorithm);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, sksSpec);
        
        byte[] encrypted = cipher.doFinal(word.getBytes());    
        
        return Base64.encodeBase64String(encrypted);
    }

    public static String decrypt(String word, String key) throws Exception {
        byte[] encrypted = Base64.decodeBase64(word);
        
        // 複合化
        SecretKeySpec sksSpec = new SecretKeySpec(key.getBytes(), algorithm);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, sksSpec);
        
        byte[] decrypted = cipher.doFinal(encrypted);
    
        return new String(decrypted);
    }
}
