/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.bean;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * バイナリ収集・監視機能で利用する定数管理クラス.<br>
 * <br>
 * 主にBinaryCheckInfoの各フィールドの値.<br>
 * 値が"."(ドット)区切りの定数は基本的にClientの多言語表示(messages_client.properties)対応.
 * 
 * @since 6.1.0
 * @version 6.1.0
 * 
 **/
public final class BinaryConstant {

	// 収集方式.
	/** 収集方式_ファイル全体 **/
	public final static String COLLECT_TYPE_WHOLE_FILE = "binary.collect.type.individual";
	/** 収集方式_増分のみ **/
	public final static String COLLECT_TYPE_ONLY_INCREMENTS = "binary.collect.type.continuous";

	// レコード分割方法.
	/** レコード分割方法_時間区切り **/
	public final static String CUT_TYPE_INTERVAL = "binary.cut.type.interval";
	/** レコード分割方法_レコード長指定 **/
	public final static String CUT_TYPE_LENGTH = "binary.cut.type.length";

	// データ構造.
	/** データ構造種別. */
	public static enum DataArchType {
		/** 時間区切り. */
		INTERVAL,
		/** 手入力(レコード長指定)(空欄). */
		CUSTOMIZE,
		/** プリセットファイルから読込(レコード長指定). */
		PRESET,
		/** データ構造関係なし(ファイル全体等). */
		NONE,
		/** . */
		ERROR
	}

	// レコード長指定方法.
	/** レコード長指定方法_固定長 **/
	public final static String LENGTH_TYPE_FIXED = "binary.length.type.fixed";
	/** レコード長指定方法_可変長 **/
	public final static String LENGTH_TYPE_VARIABLE = "binary.length.type.variable";

	// タグ種別.
	/** タグ種別_pacct **/
	public final static String TAG_TYPE_PACCT = "pacct";
	/** タグ種別_wtmp(固定長ログ) **/
	public final static String TAG_TYPE_WTMP = "wtmp";
	/** タグ種別_pcap(可変長ログ) **/
	public final static String TAG_TYPE_PCAP = "pcap";
	/** タグ種別_汎用(全種別) **/
	public final static String TAG_TYPE_UNIVERSAL = "binary.tag.type.none";

	// タイムスタンプ種類(tsType)
	/** タイムスタンプ種類_協定世界時からの経過秒＋マイクロ秒 **/
	public final static String TS_TYPE_SEC_AND_USEC = "timestamp.seconds.useconds";
	/** タイムスタンプ種類_協定世界時からの経過秒のみ **/
	public final static String TS_TYPE_ONLY_SEC = "timestamp.only.seconds";

	/** タイムスタンプ種別バイト数マップ(key:タイムスタンプ種類 value:バイト数) **/
	public final static Map<String, Integer> TIMESTAMP_BYTE_MAP;

	// 検索種別.
	/** バイナリ検索種別 **/
	public static enum SearchType {
		/** 16進数 **/
		HEX,
		/** 文字列 **/
		STRING,
		/** 検索文字列なし(null/空文字) **/
		EMPTY,
		/** エラー(0xのみ/16進数文字列にマッチしない) **/
		ERROR
	}

	/** バイナリ検索エラー **/
	public static enum SearchError {
		/** 0xのみが入力されている **/
		ONLY_OX,
		/** 16進数以外の文字列([a-fA-F0-9])が入力されている **/
		INVALID_HEX
	}

	// ファイル内レコード位置(任意バイナリファイル向け).
	/** ファイル内レコード位置_先頭 **/
	public final static String FILE_POSISION_TOP = "file.position.top";
	/** ファイル内レコード位置_中間 **/
	public final static String FILE_POSISION_MIDDLE = "file.position.middle";
	/** ファイル内レコード位置_末尾(任意バイナリファイル以外のデフォルト値) **/
	public final static String FILE_POSISION_END = "file.position.end";

	// 定形文字列.
	/** フィルタリングのメッセージ欄用定数 **/
	public final static String BINARY_LINE = "#[BINARY_LINE]";
	/** 16進数を表す先頭文字列 **/
	public final static String HEX_PREFIX = "0x";

	// マップ等の定数初期化.
	static {
		/** タイムスタンプ種類別バイト数マップ **/
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put(TS_TYPE_SEC_AND_USEC, Integer.valueOf(8));
		map.put(TS_TYPE_ONLY_SEC, Integer.valueOf(4));
		TIMESTAMP_BYTE_MAP = Collections.unmodifiableMap(map);
	}

	// インスタンス化防止コンストラクタ.
	private BinaryConstant() {
		throw new IllegalStateException("ConstClass");
	}
}
