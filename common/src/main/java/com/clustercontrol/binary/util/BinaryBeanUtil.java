/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.util;

import java.util.HashMap;
import java.util.Map;

import com.clustercontrol.binary.bean.BinaryConstant;
import com.clustercontrol.binary.bean.BinarySearchBean;

/**
 * バイナリ監視特有のデータの持ち方に関するUtil.<br>
 * <br>
 * ※Byte型データを扱うUtilはBinaryUtilを参照.<br>
 * 
 * @version 6.1.0
 * @since 6.1.0
 * @see com.clustercontrol.util.BinaryUtil
 */
public class BinaryBeanUtil {

	/**
	 * バイナリデータタグマップの文字列変換.<br>
	 * <br>
	 * バイナリデータ用のタグを格納しているMap
	 * <tagName,tagValue>を"tagName1=tagValue1;tagName2=tagValue2;..."の文字列に変換.
	 * 
	 * @param tagMap
	 *            変換元のタグマップ
	 * @return 変換後の文字列、引数不正等はnull返却.
	 */
	public static String tagMapToStr(Map<String, String> tagMap) {
		// 引数チェック.
		if (tagMap == null || tagMap.isEmpty()) {
			return null;
		}

		// 文字列変換処理.
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> tagSet : tagMap.entrySet()) {
			sb.append(tagSet.getKey());
			sb.append("=");
			if (tagSet.getValue() != null) {
				sb.append(tagSet.getValue());
			} else {
				sb.append("");
			}
			sb.append(";");
		}
		return sb.toString();
	}

	/**
	 * タグ文字列のマップ変換.<br>
	 * <br>
	 * バイナリデータ用のタグを格納している文字列"tagName1=tagValue1;tagName2=tagValue2;..."を<br>
	 * Map<tagName,tagValue>に変換.
	 * 
	 * @param tagStr
	 *            変換元のタグストリング
	 * @return 変換後のマップ、引数不正等はnull返却.
	 */
	public static Map<String, String> tagStrToMap(String tagStr) {
		// 引数チェック.
		if (tagStr == null || tagStr.isEmpty()) {
			return null;
		}

		// マップ変換用の変数初期化.
		Map<String, String> tagMap = new HashMap<String, String>();
		StringBuilder tmpSb = new StringBuilder();
		String key = "";
		String value = "";

		// 文字列のマップ変換処理.
		for (int i = 0; i < tagStr.length(); i++) {
			if (tagStr.charAt(i) == '=') {
				// それまで連結した文字列をキーとして格納.
				key = tmpSb.toString();
				tmpSb = new StringBuilder();
			} else if (tagStr.charAt(i) == ';') {
				// それまで連結した文字列を値として格納.
				value = tmpSb.toString();
				if (!key.isEmpty()) {
					// キーが存在する場合、マップに格納.
					tagMap.put(key, value);
				}
				// マップに格納したので初期化.
				key = "";
				value = "";
				tmpSb = new StringBuilder();
			} else {
				tmpSb.append(tagStr.charAt(i));
			}
		}

		// 生成されていない場合はnull返却.
		if (tagMap.isEmpty()) {
			return null;
		}

		// 生成マップ返却.
		return tagMap;
	}

	/**
	 * 16進数検索判定.<br>
	 * <br>
	 * 検索文字列をチェックして、16進数か判定する.
	 * 
	 * @param searchString
	 *            検索文字列
	 * @return true 16進数検索、false それ以外.
	 * @see com.clustercontrol.binary.bean.BinaryConstant
	 */
	public static boolean isSearchHex(String searchString) {
		BinaryConstant.SearchType searchType = getSearchType(searchString);

		if (BinaryConstant.SearchType.HEX.equals(searchType)) {
			return true;
		}
		return false;
	}

	/**
	 * 検索種別取得.<br>
	 * <br>
	 * 検索文字列をチェックの上、検索種別取得.
	 * 
	 * @param searchString
	 *            検索文字列
	 * @return 検索種別.
	 * @see com.clustercontrol.binary.bean.BinaryConstant
	 */
	public static BinaryConstant.SearchType getSearchType(String searchString) {
		BinarySearchBean binarySearchBean = getSearchBean(searchString);
		return binarySearchBean.getSearchType();
	}

	/**
	 * 16進数検索文字列取得.<br>
	 * <br>
	 * 検索文字列をチェックの上、16進数検索文字列のみを取得.
	 * 
	 * @param searchString
	 *            検索文字列(先頭0xはじまり前提)
	 * @return 0xを取り除いた16進数文字列(文字列不正はnull返却).
	 * @see com.clustercontrol.binary.bean.BinaryConstant
	 */
	public static String getOnlyHex(String searchString) {
		BinarySearchBean binarySearchBean = getSearchBean(searchString);
		return binarySearchBean.getOnlyHexString();
	}

	/**
	 * 検索種別と16進数検索文字列を取得.<br>
	 * <br>
	 * 検索文字列をチェックの上、検索種別と16進数のみの検索文字列取得.
	 * 
	 * @param searchString
	 *            検索文字列
	 * @return 検索種別.
	 * @see com.clustercontrol.binary.bean.BinaryConstant
	 */
	public static BinarySearchBean getSearchBean(String searchString) {

		BinarySearchBean binarySearchBean = new BinarySearchBean();

		// 検索文字列がnullもしくは空文字の場合.
		if (searchString == null || searchString.isEmpty()) {
			binarySearchBean.setSearchType(BinaryConstant.SearchType.EMPTY);
			return binarySearchBean;
		}

		// 2文字以下は文字列検索.
		if (searchString.length() < 2) {
			binarySearchBean.setSearchType(BinaryConstant.SearchType.STRING);
			return binarySearchBean;
		}

		// 先頭2文字が"0x"でない場合は文字列として判定
		String prefix = searchString.substring(0, 2);
		if (!BinaryConstant.HEX_PREFIX.equals(prefix)) {
			binarySearchBean.setSearchType(BinaryConstant.SearchType.STRING);
			return binarySearchBean;
		}

		// 0xのみの場合.
		if (BinaryConstant.HEX_PREFIX.equals(searchString)) {
			binarySearchBean.setSearchType(BinaryConstant.SearchType.ERROR);
			binarySearchBean.setSearchError(BinaryConstant.SearchError.ONLY_OX);
			return binarySearchBean;
		}

		// 16進数の文字列以外が入力されている場合はエラー.
		String hexStr = searchString.substring(2);
		if (!hexStr.matches("^[a-fA-F0-9]+$")) {
			binarySearchBean.setSearchType(BinaryConstant.SearchType.ERROR);
			binarySearchBean.setSearchError(BinaryConstant.SearchError.INVALID_HEX);
			return binarySearchBean;
		}

		// 16進数検索.
		binarySearchBean.setSearchType(BinaryConstant.SearchType.HEX);
		binarySearchBean.setOnlyHexString(hexStr);
		return binarySearchBean;
	}

	/**
	 * データ構造の処理種別を判定して返却.<BR>
	 *
	 * @param collectType
	 *            収集方式.
	 * @param cutType
	 *            レコード分割方法.
	 * @param tagType
	 *            タグ種類(プリセット名).
	 * 
	 * @return データ構造の処理種別、引数データ不正の場合はNONEを返却.
	 * @see com.clustercontrol.binary.bean.BinaryConstant
	 */
	public static BinaryConstant.DataArchType getDataArchType(String collectType, String cutType, String tagType) {

		// 収集方式:ファイル全体の場合.
		if (BinaryConstant.COLLECT_TYPE_WHOLE_FILE.equals(collectType)) {
			return BinaryConstant.DataArchType.NONE;
		}

		// 時間区切りの場合.
		if (BinaryConstant.CUT_TYPE_INTERVAL.equals(cutType)) {
			return BinaryConstant.DataArchType.INTERVAL;
		}

		// データ構造手入力の場合.
		if (BinaryConstant.TAG_TYPE_UNIVERSAL.equals(tagType)) {
			return BinaryConstant.DataArchType.CUSTOMIZE;
		}

		// データ構造プリセットファイルから読込の場合.
		if (tagType != null && !tagType.isEmpty()) {
			return BinaryConstant.DataArchType.PRESET;
		}

		// 判定に必要な情報が取得不可の場合(引数データ不正).
		return BinaryConstant.DataArchType.ERROR;

	}

	private BinaryBeanUtil() {
		throw new IllegalStateException("UtilClass");
	}
}
