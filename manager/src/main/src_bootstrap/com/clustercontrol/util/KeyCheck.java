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
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.util.OptionManager;
import com.clustercontrol.fault.HinemosUnknown;

public class KeyCheck {
	
	// ログ
	private static Log m_log = LogFactory.getLog( KeyCheck.class );
	
	private static final String ALGORITHM = "RSA";
	public static final String PUBLIC_KEY_STR = 
			"30819?300=06092:864886?70=010101050003818=00308189028181008?8=8?0037062==696>189>=09>404??810<4<2?>>9<52:5?2<97072438320?=1718>;4>9?140368:4>18425657:>94>7<;1<<>63>;75445<>;?4=>063>18;971747028>8:<<;1?1<579:921?5<??:>9><4>:9?;?8??;61:303?0394<351=79;36338><124;;38:>0220;37<66=6>2>?9>>41<4=2;0;833616?8===:09;0=<1=0203010001";
	
	/**
	 * ライセンスキーのファイル名(YYYY_MM_enterprise)
	 */
	public static final String TYPE_ENTERPRISE = "enterprise";
	
	/**
	 * ライセンスキーのファイル名(YYYY_MM_xcloud)
	 */
	public static final String TYPE_XCLOUD = "xcloud";

	/**
	 * ライセンスキーで有効となるファイル名の最新日付および年月比較結果
	 * key(String)はオプション名、value(Integer)は先頭6桁の数値(e.g. 201812)
	 */
	private static Map<String, Integer> latestDateMap= new HashMap<>();

	/**
	 * 秘密鍵と公開鍵のチェックを行います。
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		PrivateKey privateKey = null;
		PublicKey publicKey = null;
		
		/// 秘密鍵と公開鍵を生成済みの場合 true
		/// 秘密鍵と公開鍵を生成したい場合 false (動作確認用)
		boolean flag = false;
		if (flag) {
			try {
				// 秘密鍵
				privateKey = getPrivateKey("ここに秘密鍵の文字列を書く。privateKey.txtのファイルの中身。");
		
				// 公開鍵
				publicKey = getPublicKey("ここに公開鍵の文字列を書く。");
				// publicKey = getPublicKey(publicKeyStr);
			} catch (Exception e) {
				System.out.println("hoge" + e.getMessage());
			}
		} else {
			KeyPairGenerator generator;
			try {
				generator = KeyPairGenerator.getInstance(ALGORITHM);
				SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
				// 公開鍵ビット長は 1024
				generator.initialize(1024, random);
				KeyPair keyPair = generator.generateKeyPair();
				privateKey = keyPair.getPrivate();
				publicKey = keyPair.getPublic();
			} catch (NoSuchAlgorithmException ex) {
				System.out.println(ex.getMessage());
			}
		}
		
		//
		// 使用するキーペア
		System.out.println("秘密鍵");
		if (privateKey != null) {
			System.out.println(byte2String(privateKey.getEncoded()));
		}
		System.out.println("公開鍵");
		if (publicKey != null) {
			System.out.println(byte2String(publicKey.getEncoded()));
		}

		// 暗号化したいデータをバイト列で用意
		String string = "20140701_nttdata";
		byte[] src = string.getBytes();
		System.out.println("暗号化するデータ（String）");
		System.out.println(string);
		System.out.println("暗号化するデータ（byte）");
		System.out.println(byte2String(src));

		// 暗号化
		try {
			String encStr = encrypt(string, privateKey);
			System.out.println("暗号化後データ");
			System.out.println(encStr);
	
			// 復号
			String decStr = decrypt(encStr, publicKey);
			System.out.println("復号後データ");
			System.out.println(decStr);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

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
				return false;
			}
			m_log.info("Found key files=" + files.length);
		} catch (Exception e) {
			m_log.warn(e.getMessage(), e);
			return false;
		}
		
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
				Integer latestDate = latestDateMap.get(type);

				/*
				 * ファイル名より日付を抽出する。
				 * 最終的に最新のファイルの日付のみ残る。
				 */
				if (null == latestDate) {
					latestDate = filenamePre;
				} else if (latestDate < filenamePre) {
					latestDate = filenamePre;
				}

				// typeにより日付を設定
				setLatestDate(type, latestDate);

				// Compare key with current timestamp
				Calendar cal = Calendar.getInstance(TimeZone.getDefault());
				int nowYM = cal.get(Calendar.YEAR) * 100 + cal.get(Calendar.MONTH);
				m_log.debug("nowYM = " + nowYM);
				if(!latestDate.equals(0) && ((int)latestDate) >= nowYM){
					keyCheck = true;
					// Add to option list
					OptionManager.add(type);
					break;
				}else{
					keyCheck = false;
					m_log.warn("The key is either invalid or expired! filename=" + filename);
				}
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

		return keyCheck;
	}

	/**
	 * 秘密鍵
	 * com.clustercontrol.key.KeyGeneratorから参照されているため、publicにする。
	 * @param str
	 * @return
	 * @throws HinemosUnknown
	 */
	public static PrivateKey getPrivateKey(String str) throws HinemosUnknown {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(string2Byte(str));
			return keyFactory.generatePrivate(privateKeySpec);
		} catch (InvalidKeySpecException e) {
			throw new HinemosUnknown("getPrivateKey fail " + e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			throw new HinemosUnknown("getPrivateKey fail " + e.getMessage(), e);
		}
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
	
	private static byte[] string2Byte(String str) {
		if(str.length() % 2 != 0) {
			str = "0" + str;
		}
		byte[] bytes = new byte[str.length() / 2];
		for(int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte)(((str.charAt(2 * i) - '0') << 4) + str.charAt(2 * i + 1) - '0');
		}
		return bytes;
	}

	private static String byte2String(byte[] bytes) {
		int len = bytes.length;
		byte[] data = new byte[len << 1];
		for (int i = 0, j = 0; i < len; i++) {
			int c = bytes[i];
			data[j++] = (byte)(((c >> 4) & 0x0000000f) + '0');
			data[j++] = (byte)((c & 0x0000000f) + '0');
		}
		return new String(data);
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
	 * 秘密鍵で暗号化
	 * com.clustercontrol.key.KeyGeneratorから参照されているため、publicにする。
	 * @param source
	 * @param privateKey
	 * @return
	 * @throws HinemosUnknown
	 */
	public static String encrypt(String source, PrivateKey privateKey) throws HinemosUnknown {
		return byte2String(encrypt(source.getBytes(), privateKey));
	}
	private static byte[] encrypt(byte[] source, PrivateKey privateKey) throws HinemosUnknown{
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, privateKey);
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
		throw new HinemosUnknown("encrypt error");
	}

	private static void setLatestDate(String type, Integer latestDate) {
		m_log.info("setLatestDate(" + type + ")=" + latestDate);
		latestDateMap.put(type, latestDate);
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
		return getResult(TYPE_ENTERPRISE);
	}

	/**
	 * Hinemos時刻と起動時にチェックしたキーファイルの時間の比較結果を返却する
	 */
	@Deprecated
	public static String getResultXcloud() {
		return getResult(TYPE_XCLOUD);
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
