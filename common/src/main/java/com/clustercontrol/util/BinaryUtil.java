/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Byte型データを扱うUtil<br>
 * <br>
 * ※バイナリ監視特有のデータを扱うUtilはBinaryBeanUtilを参照.<br>
 * 
 * @version 6.1.0
 * @since 6.1.0
 * @see com.clustercontrol.binary.util.BinaryBeanUtil
 */
public class BinaryUtil {

	// クラス共通フィールド.
	/** ロガー */
	private static Log log = LogFactory.getLog(BinaryUtil.class);

	/**
	 * バイトリスト初期化.<br>
	 * <br>
	 * 指定したバイト値で満たして初期化したバイトリストを返却する.<br>
	 * 
	 * @param initByte
	 *            リストを満たすバイト値
	 * @param size
	 *            リストサイズ
	 * 
	 * @return 生成されない場合はnull返却
	 */
	public static List<Byte> initByteList(Byte initByte, int size) {
		List<Byte> byteList = new ArrayList<Byte>();
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				byteList.add(initByte);
			}
		}
		return existList(byteList);
	}

	/**
	 * バイトリストサイズ調整.<br>
	 * <br>
	 * バイトリストを指定サイズにフィットするようカットもしくはパディングする.<br>
	 * 
	 * @param byteist
	 *            対象バイトリスト
	 * 
	 * @param size
	 *            フィットさせたいサイズ
	 * 
	 * @return 引数不正等で生成されない場合はnull返却
	 */
	public static List<Byte> fitSizeByteList(List<Byte> byteist, int size) {
		List<Byte> returnList = new ArrayList<Byte>(byteist);
		return cutByteList(returnList, size, true);
	}

	/**
	 * バイトリスト先頭カット.<br>
	 * <br>
	 * バイトリストの先頭を指定サイズで切り取って返却する.<br>
	 * 
	 * @param cutList
	 *            カット対象バイトリスト
	 * 
	 * @param size
	 *            カット後のリストサイズ
	 * 
	 * @param paddingFlg
	 *            trueの場合リストが指定サイズに満たない場合にパディング実施<br>
	 *            falseの場合リストが指定サイズに満たない場合は引数をそのまま返却.
	 * 
	 * @return 引数不正等で生成されない場合はnull返却
	 */
	public static List<Byte> cutByteList(List<Byte> cutList, int size, boolean paddingFlg) {
		List<Byte> firstPartOfList = new ArrayList<Byte>(cutList);
		if (existList(cutList) == null) {
			return null;
		}

		if (size <= cutList.size()) {
			// カット後のサイズより大きい場合は指定サイズでカット.
			firstPartOfList = firstPartOfList.subList(0, size);
		} else if (paddingFlg) {
			Byte padByte = 0;
			firstPartOfList = paddingByteList(firstPartOfList, padByte, size);
		} else {
			return cutList;
		}

		return existList(firstPartOfList);
	}

	/**
	 * バイトリストパディング.<br>
	 * <br>
	 * 指定サイズの不足分を指定バイト値でパディングして返却する.<br>
	 * 
	 * @param paddingList
	 *            パディング対象のバイトリスト
	 * 
	 * @param padByte
	 *            不足分を満たすバイト値
	 * 
	 * @param size
	 *            パディング後のリストサイズ
	 * 
	 * @return 引数不正等で生成されない場合はnull返却
	 */
	public static List<Byte> paddingByteList(List<Byte> paddingList, Byte padByte, int size) {
		List<Byte> byteList = paddingByteList(paddingList, padByte, size, true);
		return existList(byteList);
	}

	/**
	 * バイトリストパディング.<br>
	 * <br>
	 * 指定サイズの不足分を指定バイト値でパディングして返却する.<br>
	 * 
	 * @param paddingList
	 *            パディング対象のバイトリスト
	 * 
	 * @param padByte
	 *            不足分を満たすバイト値
	 * 
	 * @param size
	 *            パディング後のリストサイズ
	 * @param backpad
	 *            true:後方パディング(XX000) false：前方パディング(000XX)
	 * 
	 * @return 引数不正等で生成されない場合はnull返却、サイズを満たしている場合は引数そのまま返却.
	 */
	public static List<Byte> paddingByteList(List<Byte> paddingList, Byte padByte, int size, boolean backpad) {
		List<Byte> byteList = new ArrayList<Byte>();
		List<Byte> fullList = null;

		// リストが存在しないもしくは指定サイズより大きい場合.
		if (existList(paddingList) == null || size < paddingList.size()) {
			return null;
		}

		// パディング不要な場合.
		if (size == paddingList.size()) {
			return paddingList;
		}

		// パディング実施.
		int paddingSize = size - paddingList.size();
		fullList = initByteList(padByte, paddingSize);

		if (backpad) {
			byteList.addAll(paddingList);
			byteList.addAll(fullList);
		} else {
			byteList.addAll(fullList);
			byteList.addAll(paddingList);
		}

		return existList(byteList);
	}

	/**
	 * byte[]をArrayList{@code <Byte>}に変換.<br>
	 * 
	 * @param bytearray
	 *            変換元のバイト配列
	 * @return リスト生成されない場合はnull返却
	 */
	public static List<Byte> arrayToList(byte[] bytearray) {
		List<Byte> byteList = new ArrayList<Byte>();
		if (bytearray.length > 0) {
			for (int i = 0; i < bytearray.length; i++) {
				byteList.add(Byte.valueOf(bytearray[i]));
			}
		}
		return existList(byteList);
	}

	/**
	 * ArrayList{@code <Byte>}をbyte[]に変換.<br>
	 * 
	 * @param byteList
	 *            変換元のバイトリスト
	 * @return 引数不正の場合はサイズ0の配列返却
	 */
	public static byte[] listToArray(List<Byte> byteList) {
		if (existList(byteList) == null) {
			return new byte[0];
		}
		byte[] byteArray = new byte[byteList.size()];
		Byte[] classByteArray = byteList.toArray(new Byte[byteList.size()]);

		for (int i = 0; i < classByteArray.length; i++) {
			byteArray[i] = classByteArray[i].byteValue();
		}

		return byteArray;
	}

	/**
	 * ArrayList{@code <Byte>}をxml送信用に変換.<br>
	 * <br>
	 * Base64エンコードして文字列変換<br>
	 * .
	 * 
	 * @param byteList
	 *            変換元のバイトリスト
	 * @return 引数nullもしくはsize0の場合は空文字返却.
	 */
	public static String listToBase64(List<Byte> byteList) {
		byte[] sendBinary = BinaryUtil.listToArray(byteList);
		String messageBase64 = Base64.encodeBase64String(sendBinary);
		return messageBase64;
	}

	/**
	 * xml送信用Bas64文字列をArrayList{@code <Byte>}に変換.<br>
	 * 
	 * @param byteList
	 *            変換元のバイトリスト
	 * @return 引数不正はnull返却.
	 */
	public static List<Byte> base64ToList(String base64Str) {
		try {
			byte[] sendBinary = Base64.decodeBase64(base64Str);
			List<Byte> byteList = arrayToList(sendBinary);
			return existList(byteList);
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * バイトリストをString変換(16進数表記).<br>
	 * 
	 * @param byteList
	 *            変換元のバイトリスト
	 * @return 16進数表記で変換した文字列、引数不正は空文字返却
	 */
	public static String listToString(List<Byte> byteList) {
		String byteString = listToString(byteList, 0);
		return byteString;
	}

	/**
	 * バイトリストを指定バイト単位で半角スペースで区切ってString変換(16進数表記).<br>
	 * 
	 * @param byteList
	 *            変換元のバイトリスト
	 * @param unitByte
	 * @return 16進数表記で変換した文字列、引数不正は空文字返却
	 */
	public static String listToString(List<Byte> byteList, int unitByte) {
		String byteString = listToString(byteList, unitByte, " ");
		return byteString;
	}

	/**
	 * バイトリストを指定バイト単位・指定区切り文字で区切ってString変換(16進数表記).<br>
	 * 
	 * @param byteList
	 *            変換元のバイトリスト
	 * @param unitByte
	 *            区切バイト数
	 * @param delimiter
	 *            区切り文字
	 * @return 16進数表記で変換した文字列、引数不正は空文字返却
	 */
	public static String listToString(List<Byte> byteList, int unitByte, String delimiter) {
		String byteString = "";
		if (existList(byteList) != null) {
			StringBuilder sb = new StringBuilder();
			int pointa = 1;
			for (Byte value : byteList) {
				byte[] bytearray = { value.byteValue() };
				sb.append(Hex.encodeHex(bytearray));
				if (pointa == unitByte) {
					sb.append(delimiter);
					pointa = 0;
				}
				pointa++;
			}
			byteString = sb.toString();
		}
		return byteString;
	}

	/**
	 * 16進数文字列をバイトリスト変換.<br>
	 * 
	 * @param byteString
	 *            変換元文字列(区切り文字なしの16進数表記)
	 * @return 変換不可の場合はnull返却
	 */
	public static List<Byte> stirngToList(String bytesString) {
		return stirngToList(bytesString, 1, 0);
	}

	/**
	 * 16進数文字列をバイトリスト変換.<br>
	 * 
	 * @param byteString
	 *            変換元文字列(16進数表記)
	 * @param unitByte
	 *            変換元の区切バイト数
	 * @param delimiterSize
	 *            変換元の区切り文字の文字数
	 * @return 変換不可の場合はnull返却
	 */
	public static List<Byte> stirngToList(String bytesString, int unitByte, int delimiterSize) {
		List<Byte> byteList = new ArrayList<Byte>();
		if (bytesString == null || bytesString.length() <= 0) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		String onlyBytesString = null;
		int pointa = 0;
		int keta = 0;

		// 区切り文字を取り除いてbyteのみの文字列に変換する.
		if (delimiterSize > 0) {
			for (int i = 0; i < bytesString.length(); i++) {
				if (unitByte > 0 && pointa == unitByte * 2) {
					// 区切り文字は飛ばす.
					pointa = 0;
					i = i + delimiterSize - 1;
					continue;
				} else if (keta == 0) {
					// byte文字列1桁目.
					sb.append(bytesString.substring(i, i + 1));
					keta++;
					pointa++;
					continue;
				} else if (keta == 1) {
					// byte文字列2桁目.
					sb.append(bytesString.substring(i, i + 1));
					keta = 0;
					pointa++;
					continue;
				} else {
					// 想定外.
					return null;
				}
			}
			onlyBytesString = sb.toString();
		} else {
			// 区切り文字存在しない場合は引数をbyteのみの文字列とみなす.
			onlyBytesString = bytesString;
		}

		try {
			byte[] bytes = Hex.decodeHex(onlyBytesString.toCharArray());
			byteList = arrayToList(bytes);
		} catch (DecoderException e) {
			log.warn(e.getMessage(), e);
		}
		return existList(byteList);
	}

	/**
	 * バイトリスト指定エンコードで文字列変換.<br>
	 * 
	 * @param byteList
	 *            変換元のバイトリスト
	 * @param charsetName
	 *            指定エンコード(Java文字セット)<br>
	 *            文字セットについては下記参照.<br>
	 * @see http://docs.oracle.com/javase/jp/7/api/java/nio/charset/Charset.html
	 * @return 文字列変換に失敗した場合はnull返却
	 */
	public static String listToEncodeString(List<Byte> byteList, String charsetName) {
		byte[] byteArray = listToArray(byteList);

		String returnStr = null;
		try {
			returnStr = new String(byteArray, charsetName);
		} catch (UnsupportedEncodingException e) {
			log.warn(e.getMessage(), e);
		}

		return returnStr;
	}

	/**
	 * バイトリストが存在しない場合null変換.<br>
	 * 
	 * @param byteList
	 *            判定対象のバイトリスト
	 * @return リスト存在しない場合はnull返却、存在する場合は引数返却
	 */
	public static List<Byte> existList(List<Byte> byteList) {
		if (byteList == null || byteList.size() <= 0) {
			return null;
		}
		return byteList;
	}

	/**
	 * バイトリスト同一チェック.<br>
	 * 
	 * @param compareList1
	 *            比較対象のリスト
	 * @param compareList2
	 *            比較対象のリスト
	 * @return リストが存在しない、もしくはサイズがそもそも異なる場合もfalse
	 */
	public static boolean equals(List<Byte> compareList1, List<Byte> compareList2) {
		if (existList(compareList1) == null || existList(compareList2) == null) {
			return false;
		}
		if (compareList1.size() != compareList2.size()) {
			return false;
		}
		for (int i = 0; i < compareList1.size(); i++) {
			if (!compareList1.get(i).equals(compareList2.get(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * バイトリスト前方一致チェック.<br>
	 * <br>
	 * containerListの先頭がforwardMatchに一致するかチェック.<br>
	 * 
	 * @param containerList
	 *            先頭チェック対象バイナリ
	 * @param forwardMatch
	 *            前方一致対象バイナリ
	 * @return 引数が存在しないリスト、もしくはcontainerListがforwardMatchより短い場合もfalse
	 */
	public static boolean forwardMatch(List<Byte> containerList, List<Byte> forwardMatch) {
		if (existList(containerList) == null || existList(forwardMatch) == null) {
			return false;
		}
		if (containerList.size() < forwardMatch.size()) {
			return false;
		}
		for (int i = 0; i < forwardMatch.size(); i++) {
			if (!containerList.get(i).equals(forwardMatch.get(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * List<Byte>版のindexOfメソッド<br>
	 * <br>
	 * srcListの一部がtagListに一致するかチェック.<br>
	 * ロジックの内容は 
	 * java.lang.String#indexOf 
	 * の実装内容を List<Byte>向けに微調整したもの
	 * 
	 * @param srcList
	 *            走査対象バイナリ
	 * @param tagList
	 *            部分一致条件バイナリ
	 * @return 一致なしなら -1 , 一致ありなら 一致した箇所の先頭
	 */
	public static int byteListIndexOf(List<Byte> srcList, List<Byte> tagList) {
		if (existList(srcList) == null || existList(tagList) == null) {
			return -1;
		}
		int fromIndex = 0 ;
		int sourceOffset = 0 ;
		int targetOffset = 0 ;
		int sourceCount = srcList.size();
		int targetCount = tagList.size();
		
		if (fromIndex >= sourceCount) {
			if(targetCount == 0){
				return sourceCount;
			}else{
				return -1;
			}
		}
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		if (targetCount == 0) {
			return fromIndex;
		}

		Byte first = tagList.get(0);
		int max = sourceOffset + (sourceCount - targetCount);

		for (int i = sourceOffset + fromIndex; i <= max; i++) {
			/* Look for first character. */
			if (srcList.get(i) != first) {
				while (++i <= max && srcList.get(i) != first);
			}

			/* Found first character, now look at the rest of v2 */
			if (i <= max) {
				int j = i + 1;
				int end = j + targetCount - 1;
				for (int k = targetOffset + 1; j < end && srcList.get(j)
						== tagList.get(k); j++, k++);

				if (j == end) {
					/* Found whole string. */
					return i - sourceOffset;
				}
			}
		}
		return -1;
	}

	
	/**
	 * long値のint値変換.<br>
	 * <br>
	 * リストの要素数はintの最大値までしか扱えないため<br>
	 * intの最大値を超えるlong値はintの最大値として返却する.<br>
	 * ※バイナリリストの作成時等に利用する.<br>
	 * 
	 * @param parseValue
	 *            変換対象のlong値
	 * @return
	 */
	public static int longParseInt(long parseValue) {
		Long valueLong = Long.valueOf(parseValue);
		int valueInt = 0;

		if (parseValue > Integer.MAX_VALUE) {
			valueInt = Integer.MAX_VALUE;
		} else {
			try {
				valueInt = Integer.parseInt(valueLong.toString());
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
			}
		}

		return valueInt;
	}

	/**
	 * バイナリリストをLong値変換.<br>
	 * <br>
	 * 
	 * @param byteList
	 *            変換対象のバイナリ(先頭8byteがlong値変換)
	 * @return 引数不正等はnull返却
	 */
	public static Long listToLong(List<Byte> byteList) {

		if (existList(byteList) == null) {
			//
			return null;
		}

		// ByteBufferでlong値変換するために8byte必要.
		byteList = paddingByteList(byteList, Byte.valueOf((byte) 0), 8, false);
		byte[] binaryArray = BinaryUtil.listToArray(byteList);
		Long returnLong = ByteBuffer.wrap(binaryArray).getLong();

		return returnLong;
	}

	/**
	 * バイトリストを元にファイル出力.
	 * 
	 * @param recordBinary
	 *            出力対象のバイトリスト.
	 * @param outputFile
	 *            出力ファイルオブジェクト.
	 * @param addMode
	 *            true:追記モード、false:新規作成.
	 **/
	public static void outputBinary(List<Byte> recordBinary, File outputFile, boolean addMode) {
		FileOutputStream output = null;
		//try (FileOutputStream output = new FileOutputStream(outputFile, addMode)) {
		try {
			output = new FileOutputStream(outputFile, addMode);
			output.write(BinaryUtil.listToArray(recordBinary));
		} catch (FileNotFoundException e) {
			log.warn(e.getMessage(), e);
		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		} finally {
			if(null != output) {
				try {
					output.close();
				} catch (IOException e) {
					log.warn(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * バイトリストを元にファイル出力.
	 * 
	 * @param recordBinary
	 *            出力対象のバイト配列.
	 * @param outputFile
	 *            出力ファイルオブジェクト.
	 * @param addMode
	 *            true:追記モード、false:新規作成.
	 **/
	public static void outputBinary(byte[] recordBinary, File outputFile, boolean addMode) {
		FileOutputStream output = null;
		//try (FileOutputStream output = new FileOutputStream(outputFile, addMode)) {
		try {
			output = new FileOutputStream(outputFile, addMode);
			output.write(recordBinary);
		} catch (FileNotFoundException e) {
			log.warn(e.getMessage(), e);
		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		} finally {
			if(null != output) {
				try {
					output.close();
				} catch (IOException e) {
					log.warn(e.getMessage(), e);
				}
			}
		}
	}

	private BinaryUtil() {
		throw new IllegalStateException("UtilClass");
	}
}
