/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.util.OptionManager;
import com.clustercontrol.bean.ActivationKeyConstant;
import com.clustercontrol.fault.HinemosUnknown;

public class KeyCheck {
	
	// ログ
	private static Log m_log = LogFactory.getLog( KeyCheck.class );
	
	private static final String ALGORITHM = "RSA";
	public static final String PUBLIC_KEY_STR = 
			"30819?300=06092:864886?70=010101050003818=0030818902818100;53399;<0><38<6809;2:?65;424>94?1087:6<;779>>==:90766<?<=625112:<1>909;680=?207:7>0:===7<38893=:01389:88?818>65<46?2=765:561>01:8=8;863;;:7?26435:;;866>;:0;??6:>73;=16>19?93397?774928?24974<0349:49<4=>25722851:858>7<9904;=0?;2;64561>=81?===8==27><?32>>;><30203010001";
	private static Map<String, Integer> expireDateMap = new ConcurrentHashMap<>();
	private static Map<String, String> filenameTypeMap = new ConcurrentHashMap<>();
	
	/**
	 * キーの最終年月を取得します。
	 * 
	 * @param type キータイプ
	 * @return キーの最終年月
	 */
	private static synchronized Integer getLastestDate(String type){
		Integer lastestDate =Integer.valueOf(0);
		String filenameType = null;
		
		String etcdir = System.getProperty("hinemos.manager.etc.dir");

		File[] files = null;
		PublicKey publicKey = null;
		try {
			publicKey = getPublicKey(PUBLIC_KEY_STR);
			m_log.info("etcdir=" + etcdir);
			File directory = new File(etcdir);
			// キーのファイル名はYYYYMM_XXX_Option形式
			files = directory.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.matches("\\d{6}_\\d{3}_.+") && name.endsWith(type);
				}
			});
			if (files == null) {
				m_log.warn(etcdir + " does not exist");
				return Integer.valueOf(0);
			}
			m_log.info("Found key files=" + files.length);
		} catch (Exception e) {
			m_log.warn(e.getMessage(), e);
			return Integer.valueOf(0);
		}

		Map<String, Integer> latestDateMap = new HashMap<>();
		for (File file : files) {
			FileReader fileReader = null;
			try {
				String filename = file.getName();
				Integer filenamePre = Integer.parseInt(filename.substring(0, 6));

				/*
				 * チェック0:
				 * ファイル名のチェックと、
				 * チェック対象ファイルが対象ファイルかどうか。
				 */
				String[] fileTypeArr = filename.split("_");
				if (fileTypeArr.length != 3 || !fileTypeArr[2].equals(type)) {
					m_log.debug("file type different. fileName:" + filename + ", targetType:" + type);
					continue;
				}

				/*
				 * チェック1:
				 * ファイルの中身を複合化したものが、ファイル名と一致していることを確認する。
				 */
				fileReader = new FileReader(file);
				int charLength = 256;
				char[] cbuf = new char[charLength];
				int readN = fileReader.read(cbuf, 0, charLength);
				String str = decrypt(new String(cbuf), publicKey);
				m_log.debug("filename=" + filename + ", read-char=" + readN + ", contents=" + str);
				if (!filename.equals(str)) {
					m_log.warn("Invalid key!");
					continue;
				}

				// typeにより日付を取得
				lastestDate = latestDateMap.get(type);
				filenameType = filenameTypeMap.get(type);
				
				/*
				 * ファイル名より日付を抽出する。
				 * 最終的に最新のファイルの日付のみ残る。
				 */
				if (null == lastestDate) {
					lastestDate = filenamePre;
					filenameType = fileTypeArr[1];
				} else if (lastestDate < filenamePre) {
					lastestDate = filenamePre;
					filenameType = fileTypeArr[1];
				}

				// typeにより日付を設定
				m_log.info("setLatestDate(" + type + ")=" + lastestDate);
				latestDateMap.put(type, lastestDate);
				filenameTypeMap.put(type, filenameType);
				
			} catch (Exception e) {
				if (e instanceof NumberFormatException) {
					m_log.info(e.getMessage());
				} else {
					m_log.info(e.getMessage(), e);
				}
			} finally {
				if (fileReader != null) {
					try {
						fileReader.close();
					} catch (IOException e) {
						// nop
					}
				}
			}
		}
		return lastestDate;
	}
	
	public static String getActivationKeyFilename(LocalDate expireDate, String type) {
		if (filenameTypeMap.get(type) != null && expireDate != null) {
			StringBuilder activationKeyFilename = new StringBuilder();
			activationKeyFilename.append(expireDate.format(DateTimeFormatter.ofPattern("yyyyMM")))
				.append("_").append(filenameTypeMap.get(type))
				.append("_").append(type);
			return activationKeyFilename.toString();
		}
		return null;
	}
	
	/**
	 * キーチェック
	 * 
	 * @param type キータイプ
	 * @return 妥当なキーが存在する場合true; それ以外false
	 */
	public static synchronized boolean checkKey(String type) {
		// If an Option already checked
		if(OptionManager.has(type)){
			m_log.debug(type + " has been checked");
			return OptionManager.has(type);
		}
	
		boolean keyCheck = false;
	
		Integer lastestDate = getLastestDate(type);
		expireDateMap.put(type, lastestDate);
		
		// Compare key with current timestamp
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		int nowYM = cal.get(Calendar.YEAR) * 100 + cal.get(Calendar.MONTH) + 1;
		m_log.debug("nowYM = " + nowYM);
		if(!lastestDate.equals(0) && ((int)lastestDate) >= nowYM){
			keyCheck = true;
			// Add to option list
			OptionManager.add(type);
			// 評価版かどうか判定
			if(!lastestDate.equals(ActivationKeyConstant.ACTIVATION_KEY_YYYYMM)){
				OptionManager.add(type + "_" + lastestDate + ActivationKeyConstant.EVALUATION_SUFFIX);
			}
		}else{
			keyCheck = false;
			// 評価版かつ期限切れ
			if(!lastestDate.equals(0) && !lastestDate.equals(ActivationKeyConstant.ACTIVATION_KEY_YYYYMM)){
				OptionManager.add(type + "_" + lastestDate + ActivationKeyConstant.EVALUATION_EXPIRED_SUFFIX);
			}
			m_log.warn("The key is either invalid or expired! lastestDate=" + lastestDate + ", type=" + type);
		}
	
		return keyCheck;
	}
	
	/**
	 * キーチェックで作成したキーの最終日時のキャッシュを返す。
	 * 
	 * @param type キータイプ
	 * @return キータイプでキャッシュしている最終日付
	 */
	public static LocalDate getExpireDate(String type){
		Integer expireDate = expireDateMap.get(type);
		if (expireDate != null) {
			Pattern p = Pattern.compile("(\\d{4})(\\d{2})");
			Matcher m = p.matcher(expireDate.toString());
			if (m.find()) {
				int year = Integer.parseInt(m.group(1));
				int month = Integer.parseInt(m.group(2));
				int day = LocalDate.of(year, month, 1).lengthOfMonth();
				
				return LocalDate.of(year, month, day);
			}
		}
		return null;
	}

	/**
	 * 公開鍵
	 * com.clustercontrol.key.KeyGeneratorから参照されているため、publicにする。
	 * @param str
	 * @return
	 * @throws HinemosUnknown
	 */
	public static PublicKey getPublicKey(String str) throws HinemosUnknown {
		try {
			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(string2Byte(str));
			KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
			return keyFactory.generatePublic(publicKeySpec);
		} catch (InvalidKeySpecException e) {
			throw new HinemosUnknown("getPublicKey fail " + e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			throw new HinemosUnknown("getPublicKey fail " + e.getMessage(), e);
		}
	}

	/**
	 * StringからByte配列に変換
	 * com.clustercontrol.key.KeyGeneratorから参照されているため、publicにする。
	 * @param str
	 * @return byte[]
	 */
	public static byte[] string2Byte(String str) {
		if(str.length() % 2 != 0) {
			str = "0" + str;
		}
		byte[] bytes = new byte[str.length() / 2];
		for(int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte)(((str.charAt(2 * i) - '0') << 4) + str.charAt(2 * i + 1) - '0');
		}
		return bytes;
	}
	
	/**
	 * 公開鍵で復号
	 * com.clustercontrol.key.KeyGeneratorから参照されているため、publicにする。
	 * @param source
	 * @param publicKey
	 * @return
	 * @throws HinemosUnknown
	 */
	public static String decrypt(String source, PublicKey publicKey) throws HinemosUnknown{
		return new String(decrypt(string2Byte(source), publicKey));
	}
	private static byte[] decrypt(byte[] source, PublicKey publicKey) throws HinemosUnknown{
		m_log.trace("decrypt=" + source.length);
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, publicKey);
			return cipher.doFinal(source);
		} catch (IllegalBlockSizeException ex) {
			m_log.warn(ex.getMessage(), ex);
		} catch (BadPaddingException ex) {
			m_log.warn(ex.getMessage(), ex);
		} catch (InvalidKeyException ex) {
			m_log.warn(ex.getMessage(), ex);
		} catch (NoSuchAlgorithmException ex) {
			m_log.warn(ex.getMessage(), ex);
		} catch (NoSuchPaddingException ex) {
			m_log.warn(ex.getMessage(), ex);
		}
		throw new HinemosUnknown("decrypt error");
	}

	/**
	 * Hinemos時刻と起動時にチェックしたキーファイルの時間の比較結果を返却する
	 */
	@Deprecated
	public static String getResult(String type) {
		return getNowYM() + "_true";
	}

	/**
	 * Hinemos時刻と起動時にチェックしたキーファイルの時間の比較結果を返却する
	 */
	@Deprecated
	public static String getResultEnterprise() {
		return getResult(ActivationKeyConstant.TYPE_ENTERPRISE);
	}

	/**
	 * Hinemos時刻と起動時にチェックしたキーファイルの時間の比較結果を返却する
	 */
	@Deprecated
	public static String getResultXcloud() {
		return getResult(ActivationKeyConstant.TYPE_XCLOUD);
	}

	/**
	 * Hinemos時刻より現在年月を取得する
	 */
	@Deprecated
	private static String getNowYM() {
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		String nowYM = "";
		String y = String.valueOf(cal.get(Calendar.YEAR));
		String m = String.valueOf(cal.get(Calendar.MONTH) + 1);
		
		switch (m.length()) {
		case 1 :
			nowYM = y + "0" + m;
			break;
		case 2 :
			nowYM = y + m;
			break;
		default :
			break;
		}
		return nowYM;
	}

}
