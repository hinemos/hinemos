package com.clustercontrol.util;

import java.io.File;
import java.io.FileReader;
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
import java.util.TimeZone;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
	private static final String TYPE_ENTERPRISE = "enterprise";
	
	/**
	 * ライセンスキーのファイル名(YYYY_MM_xcloud)
	 */
	private static final String TYPE_XCLOUD = "xcloud";

	/**
	 * ライセンスキーで有効となるファイル名の最新日付および年月比較結果
	 */
	private static String latestDateEnterprise = "";

	/**
	 * ライセンスキーで有効となるファイル名の最新日付および年月比較結果
	 */
	private static String latestDateXCloud = "";

	/**
	 * 以下のオプション使用可否チェック<br>
	 *  ・jobmap<br>
	 *  ・nodemap<br>
	 *  ・reporting<br>
	 *  ・utility<br>
	 * @return
	 */
	public static boolean checkEnterprise() {
		
		return checkCommon(TYPE_ENTERPRISE);
	}
	
	/**
	 * 以下のオプション使用可否チェック<br>
	 *  ・vmcloud<br>
	 * @return
	 */
	public static boolean checkXcloud() {
		return checkCommon(TYPE_XCLOUD);
	}
	
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
		System.out.println(byte2String(privateKey.getEncoded()));
		System.out.println("公開鍵");
		System.out.println(byte2String(publicKey.getEncoded()));

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
	 * 
	 * @param type
	 * @return
	 */
	private static boolean checkCommon(String type) {
		
		boolean keyCheck = false;

		String etcdir = System.getProperty("hinemos.manager.etc.dir");

		File[] files = null;
		PublicKey publicKey = null;
		try {
			publicKey = getPublicKey(PUBLIC_KEY_STR);
			m_log.info("etcdir=" + etcdir);
			File directory = new File(etcdir); // TODO ここを修正する必要ある？
			files = directory.listFiles();
			if (files == null) {
				m_log.warn(etcdir + " does not exist");
				return false;
			}
			m_log.info("key files=" + files.length);
		} catch (Exception e) {
			m_log.warn(e.getMessage(), e);
			return false;
		}
		
		for (File file : files) {
			FileReader fileReader = null;
			try {
				String filename = file.getName();
				String filenamePre = filename.substring(0, 6);
				
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
				fileReader.read(cbuf, 0, charLength);
				String str = decrypt(new String(cbuf), publicKey);
				m_log.trace("filename=" + filename + ", contents=" + str);
				if (filename.equals(str)) {
					m_log.debug("OK valid file, filename=" + filename);
					keyCheck = true;
				} else {
					m_log.debug("NG valid file, filename=" + filename);
					continue;
				}

				// typeにより日付を取得
				String latestDate = "";
				if (type.equals(TYPE_ENTERPRISE)) {
					latestDate = getLatestDateEnterprise();
				} else {
					latestDate = getLatestDateXcloud();
				}
				

				/*
				 * ファイル名より日付を抽出する。
				 * 最終的に最新のファイルの日付のみ残る。
				 */				
				if (latestDate.equals("")) {
					latestDate = filenamePre;
				} else {
					if (Integer.parseInt(latestDate) < Integer.parseInt(filenamePre)) {
						latestDate = filenamePre;
					}
				}

				// typeにより日付を設定
				if (type.equals(TYPE_ENTERPRISE)) {
					setLatestDateEnterprise(latestDate);
					m_log.debug("LatestDateEnterprise=" + latestDate);
				} else {
					setLatestDateXcloud(latestDate);
					m_log.debug("LatestDateXcloud=" + latestDate);
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
		m_log.info("license check result:" + keyCheck);
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

	public static String getLatestDateEnterprise() {
		return latestDateEnterprise;
	}

	private static void setLatestDateEnterprise(String latestDate) {
		latestDateEnterprise = latestDate;
	}

	public static String getLatestDateXcloud() {
		return latestDateXCloud;
	}

	private static void setLatestDateXcloud(String latestDate) {
		latestDateXCloud = latestDate;
	}

	/**
	 * Hinemos時刻と起動時にチェックしたキーファイルの時間の比較結果を返却する
	 * @return result
	 */
	public static String getResultEnterprise() {

		String result = "";
		String nowYM = getNowYM();
		if (!nowYM.equals("") && 
				Integer.parseInt(getLatestDateEnterprise()) >= Integer.parseInt(nowYM)) {
			result = getLatestDateEnterprise() + "_true";
		} else {
			result = getLatestDateEnterprise() + "_false";
		}
		return result;
	}

	/**
	 * Hinemos時刻と起動時にチェックしたキーファイルの時間の比較結果を返却する
	 * @return result
	 */
	public static String getResultXcloud() {

		String result = "";
		String nowYM = getNowYM();
		if (!nowYM.equals("") && 
				Integer.parseInt(getLatestDateXcloud()) >= Integer.parseInt(nowYM)) {
			result = getLatestDateXcloud() + "_true";
		} else {
			result = getLatestDateXcloud() + "_false";
		}
		return result;
	}
	
	
	/**
	 * Hinemos時刻より現在年月を取得する
	 * @return nowYM
	 */
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
