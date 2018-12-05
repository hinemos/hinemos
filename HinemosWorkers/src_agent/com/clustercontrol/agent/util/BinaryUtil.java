/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import com.clustercontrol.fault.HinemosUnknown;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Byte型データを扱うUtil<br>
 * <br>
 * 6.1.1 より 必要箇所のみ抜粋して移植   <br>
 * また  commons-codec.jarに含まれる <br>  
 * org.apache.commons.codec.binary.Hex#decodeHex <br>
 * org.apache.commons.codec.binary.Hex#encodeHex <br>
 * について  6.0.3エージェントではcommons-codec.jarが参照できないので抜粋の上で取り込み   <br>
 * 
 * @version 6.0.3
 * @since HinemosUnknown
 * @see com.clustercontrol.binary.util.BinaryBeanUtil
 */
public class BinaryUtil {

	// クラス共通フィールド.
	/** ロガー */
	private static Log log = LogFactory.getLog(BinaryUtil.class);

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
				sb.append(encodeHex(bytearray));
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
			byte[] bytes = decodeHex(onlyBytesString.toCharArray());
			byteList = arrayToList(bytes);
		} catch (HinemosUnknown e) {
			log.warn(e.getMessage(), e);
		}
		return existList(byteList);
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
	
	//---------------------------------------------------------------------
	// 以下 末尾まで commons-codec.1.10 における  org.apache.commons.codec.binary.Hex.java より メソッド単位にて 引用
	// ただし DecoderException のみ HinemosUnknown に置換 
	//  ソース取得先  https://archive.apache.org/dist/commons/codec/source/
	//---------------------------------------------------------------------
	
    /**
     * Used to build output as Hex
     */
    private static final char[] DIGITS_LOWER =
        {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Used to build output as Hex
     */
    private static final char[] DIGITS_UPPER =
        {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * Converts an array of characters representing hexadecimal values into an array of bytes of those same values. The
     * returned array will be half the length of the passed array, as it takes two characters to represent any given
     * byte. An exception is thrown if the passed char array has an odd number of elements.
     *
     * @param data
     *            An array of characters containing hexadecimal digits
     * @return A byte array containing binary data decoded from the supplied char array.
     * @throws DecoderException
     *             Thrown if an odd number or illegal of characters is supplied
     */
    public static byte[] decodeHex(final char[] data) throws HinemosUnknown {

        final int len = data.length;

        if ((len & 0x01) != 0) {
            throw new HinemosUnknown("Odd number of characters.");
        }

        final byte[] out = new byte[len >> 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j], j) << 4;
            j++;
            f = f | toDigit(data[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }    /**
     * Converts a hexadecimal character to an integer.
     *
     * @param ch
     *            A character to convert to an integer digit
     * @param index
     *            The index of the character in the source
     * @return An integer
     * @throws DecoderException
     *             Thrown if ch is an illegal hex character
     */
    protected static int toDigit(final char ch, final int index) throws HinemosUnknown {
        final int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new HinemosUnknown("Illegal hexadecimal character " + ch + " at index " + index);
        }
        return digit;
    }

    /**
     * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
     * The returned array will be double the length of the passed array, as it takes two characters to represent any
     * given byte.
     *
     * @param data
     *            a byte[] to convert to Hex characters
     * @return A char[] containing hexadecimal characters
     */
    public static char[] encodeHex(final byte[] data) {
        return encodeHex(data, true);
    }

    /**
     * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
     * The returned array will be double the length of the passed array, as it takes two characters to represent any
     * given byte.
     *
     * @param data
     *            a byte[] to convert to Hex characters
     * @param toLowerCase
     *            <code>true</code> converts to lowercase, <code>false</code> to uppercase
     * @return A char[] containing hexadecimal characters
     * @since 1.4
     */
    public static char[] encodeHex(final byte[] data, final boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    /**
     * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
     * The returned array will be double the length of the passed array, as it takes two characters to represent any
     * given byte.
     *
     * @param data
     *            a byte[] to convert to Hex characters
     * @param toDigits
     *            the output alphabet (must contain at least 16 chars)
     * @return A char[] containing the appropriate characters from the alphabet
     *         For best results, this should be either upper- or lower-case hex.
     * @since 1.4
     */
    protected static char[] encodeHex(final byte[] data, final char[] toDigits) {
        final int l = data.length;
        final char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    /**
     * Converts a byte buffer into an array of characters representing the hexadecimal values of each byte in order.
     * The returned array will be double the length of the passed array, as it takes two characters to represent any
     * given byte.
     *
     * @param data
     *            a byte buffer to convert to Hex characters
     * @param toDigits
     *            the output alphabet (must be at least 16 characters)
     * @return A char[] containing the appropriate characters from the alphabet
     *         For best results, this should be either upper- or lower-case hex.
     * @since 1.11
     */
    protected static char[] encodeHex(final ByteBuffer data, final char[] toDigits) {
        return encodeHex(data.array(), toDigits);
    }

    /**
     * Converts an array of bytes into a String representing the hexadecimal values of each byte in order. The returned
     * String will be double the length of the passed array, as it takes two characters to represent any given byte.
     *
     * @param data
     *            a byte[] to convert to Hex characters
     * @return A String containing lower-case hexadecimal characters
     * @since 1.4
     */
    public static String encodeHexString(final byte[] data) {
        return new String(encodeHex(data));
    }

}
