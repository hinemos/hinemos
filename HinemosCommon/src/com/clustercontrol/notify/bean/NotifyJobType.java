/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.bean;


/**
 * ジョブ通知実行種別を定義するクラス<BR>
 * 
 */
public class NotifyJobType {

	/** 直接実行 */
	public static final String STRING_DIRECT = "DIRECT";

	/** ジョブ連携メッセージ送信 */
	public static final String STRING_JOB_LINK_SEND = "JOB_LINK_SEND";

	/** 直接実行(Type) */
	public static final int TYPE_DIRECT = 0;

	/** ジョブ連携メッセージ送信(Type) */
	public static final int TYPE_JOB_LINK_SEND = 1;

	/** 直接実行(Enum) */
	public static final String ENUM_NAME_DIRECT = "DIRECT";

	/** ジョブ連携メッセージ送信(Enum) */
	public static final String ENUM_NAME_JOB_LINK_SEND = "JOB_LINK_SEND";

	/**
	 * メッセージから種別を返す
	 */
	public static Integer stringToType(String message) {
		if (STRING_DIRECT.equals(message)) {
			return TYPE_DIRECT;
		} else if (STRING_JOB_LINK_SEND.equals(message)) {
			return TYPE_JOB_LINK_SEND;
		} else {
			return null;
		}
	}

	/**
	 * Enumから文字列に変換します。<BR>
	 * ※Enumの型は引数で指定できますが、列挙子のNameは統一されている必要があります。<BR>
	 * 
	 * @param value 変換するEnum
	 * @param enumType Enumの型
	 * @return 文字列
	 */
	public static <T extends Enum<T>> String enumToString(T value, Class<T> enumType) {
		if (value != null) {
			String name = value.name();
	
			if (name.equals(ENUM_NAME_DIRECT)) {
				return STRING_DIRECT;
			} else if (name.equals(ENUM_NAME_JOB_LINK_SEND)) {
				return STRING_JOB_LINK_SEND;
			} else {
				return "";
			}
		}
		return "";
	}
}