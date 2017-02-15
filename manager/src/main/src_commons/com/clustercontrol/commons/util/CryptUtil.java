/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.commons.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CryptUtil {
	
	private static final Log m_log = LogFactory.getLog( CryptUtil.class );
	
	// 使用する暗号化アルゴリズム
	private static String algorithm = "BLOWFISH";
	
	private static String cryptKey = "hinemos";
	
	static {
		String etcdir = System.getProperty("hinemos.manager.etc.dir");
		String keyFile = "db_crypt.key";
		String keyPath = etcdir + File.separator + keyFile;
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		
		try {
			fileReader = new FileReader(keyPath);
			bufferedReader = new BufferedReader(fileReader);
			cryptKey = bufferedReader.readLine();
		} catch (Exception e){
			m_log.warn("file not readable. (" + keyFile + ") : " + e.getMessage(), e);
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (IOException e) {
			}
			try {
				if (fileReader != null) {
					fileReader.close();
				}
			} catch (IOException e) {
			}
		}
		// m_log.info("key=[" + cryptKey + "]"); // TODO この行は、コメントアウトすること！（パスワードがログに出力されてしまうので。）
	}
	
	public static String encrypt(String word) {
		return encrypt(cryptKey, word);
	}
	
	private static String encrypt(String key, String word) {
		if (word == null) {
			return null;
		}
		// 暗号化
		SecretKeySpec sksSpec = new SecretKeySpec(key.getBytes(), algorithm);
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance(algorithm);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			m_log.warn("encrypt : " + (e.getClass().getName()) + "," + e.getMessage(), e);
			return null;
		}
		try {
			cipher.init(Cipher.ENCRYPT_MODE, sksSpec);
		} catch (InvalidKeyException e) {
			m_log.warn("encrypt : " + (e.getClass().getName()) + "," + e.getMessage(), e);
			return null;
		}
		
		byte[] encrypted = null;
		try {
			encrypted = cipher.doFinal(word.getBytes());
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			m_log.warn("encrypt : " + (e.getClass().getName()) + "," + e.getMessage(), e);
			return null;
		}
		
		return Base64.encodeBase64String(encrypted);
	}

	public static String decrypt(String word) {
		return decrypt(cryptKey, word);
	}
	
	public static String decrypt(String key, String word) {
		if (word == null) {
			return null;
		}
		
		byte[] encrypted = Base64.decodeBase64(word);
		// 複合化
		SecretKeySpec sksSpec = new SecretKeySpec(key.getBytes(), algorithm);
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance(algorithm);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			m_log.warn("encrypt : " + (e.getClass().getName()) + "," + e.getMessage(), e);
			return null;
		}
		
		try {
			cipher.init(Cipher.DECRYPT_MODE, sksSpec);
		} catch (InvalidKeyException e) {
			m_log.warn("encrypt : " + (e.getClass().getName()) + "," + e.getMessage(), e);
			return null;
		}
		
		byte[] decrypted;
		try {
			decrypted = cipher.doFinal(encrypted);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			m_log.warn("encrypt : " + (e.getClass().getName()) + "," + e.getMessage(), e);
			return null;
		}
		
		return new String(decrypted);
	}
	
	/**
	 * バージョンアップツールから利用する
	 * @param args
	 */
	public static void main(String args[]) {
		if (args.length != 3) {
			System.out.println("usage CryptUtil encrypt <key> <word>");
			System.out.println("usage CryptUtil decrypt <key> <word>");
				System.exit(1);
		}
		String mode = args[0];
		String key = args[1];
		String word = args[2];
		// System.out.println("mode=" + mode + ", key=" + key + ", word=" + word);
		if ("decrypt".equals(mode)) {
		//	System.out.println("decrypt");
			System.out.println(decrypt(key, word));
		} else {
		//	System.out.println("encrypt");
			System.out.println(encrypt(key, word));
		}
	}
}
