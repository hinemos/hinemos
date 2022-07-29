/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.util;

import com.clustercontrol.notify.bean.NotifyTypeConstant;
import com.clustercontrol.util.Messages;

/**
 * 通知に関するUtilityクラス<br/>
 */
public class NotifyTypeUtil {
	/** ステータス（Enum判定用のName）。 */
	private static final String ENUM_NAME_STATUS = "STATUS";

	/** イベント（Enum判定用のName）。 */
	private static final String ENUM_NAME_EVENT = "EVENT";

	/** メール（Enum判定用のName）。 */
	private static final String ENUM_NAME_MAIL = "MAIL";

	/** ジョブ（Enum判定用のName）。 */
	private static final String ENUM_NAME_JOB = "JOB";

	/** ログエスカレーション（Enum判定用のName）。 */
	private static final String ENUM_NAME_LOG_ESCALATE = "LOG_ESCALATE";

	/** コマンド（Enum判定用のName）。 */
	private static final String ENUM_NAME_COMMAND = "COMMAND";

	/** 環境構築（Enum判定用のName）。 */
	private static final String ENUM_NAME_INFRA = "INFRA";

	/** REST（Enum判定用のName）。 */
	private static final String ENUM_NAME_REST = "REST";
	
		/** クラウド（Enum判定用のName）。 */
	private static final String ENUM_NAME_CLOUD = "CLOUD";

	/** メッセージ（Enum判定用のName）。 */
	private static final String ENUM_NAME_MESSAGE = "MESSAGE";

	public static String typeToString(int notifyType) {
		switch (notifyType) {
		case NotifyTypeConstant.TYPE_STATUS:
			return Messages.getString("notifies.status");
		case NotifyTypeConstant.TYPE_EVENT:
			return Messages.getString("notifies.event");
		case NotifyTypeConstant.TYPE_MAIL:
			return Messages.getString("notifies.mail");
		case NotifyTypeConstant.TYPE_JOB:
			return Messages.getString("notifies.job");
		case NotifyTypeConstant.TYPE_LOG_ESCALATE:
			return Messages.getString("notifies.log.escalate");
		case NotifyTypeConstant.TYPE_COMMAND:
			return Messages.getString("notifies.command");
		case NotifyTypeConstant.TYPE_INFRA:
			return Messages.getString("notifies.infra");
		case NotifyTypeConstant.TYPE_REST:
			return Messages.getString("notifies.rest");
		case NotifyTypeConstant.TYPE_CLOUD:
			return Messages.getString("notifies.cloud");
		case NotifyTypeConstant.TYPE_MESSAGE:
			return Messages.getString("notifies.message");
		default:
			return "";
		}
	}

	/**
	 * 種別からEnumに変換します。<BR>
	 * ※Enumの型は引数で指定できますが、列挙子のNameは統一されている必要があります。<BR>
	 * 
	 * @param type 種別
	 * @param enumType Enumの型
	 * @return Enum
	 */
	public static <T extends Enum<T>> T typeToEnum(int type, Class<T> enumType) {
		String name = "";
		switch (type) {
		case NotifyTypeConstant.TYPE_STATUS:
			name = ENUM_NAME_STATUS;
			break;
		case NotifyTypeConstant.TYPE_EVENT:
			name = ENUM_NAME_EVENT;
			break;
		case NotifyTypeConstant.TYPE_MAIL:
			name = ENUM_NAME_MAIL;
			break;
		case NotifyTypeConstant.TYPE_JOB:
			name = ENUM_NAME_JOB;
			break;
		case NotifyTypeConstant.TYPE_LOG_ESCALATE:
			name = ENUM_NAME_LOG_ESCALATE;
			break;
		case NotifyTypeConstant.TYPE_COMMAND:
			name = ENUM_NAME_COMMAND;
			break;
		case NotifyTypeConstant.TYPE_INFRA:
			name = ENUM_NAME_INFRA;
			break;
		case NotifyTypeConstant.TYPE_REST :
			name = ENUM_NAME_REST;
			break;
		case NotifyTypeConstant.TYPE_CLOUD:
			name = ENUM_NAME_CLOUD;
			break;
		case NotifyTypeConstant.TYPE_MESSAGE:
			name = ENUM_NAME_MESSAGE;
			break;
		default:
			return null;
		}
		return Enum.valueOf(enumType, name);
	}

	/**
	 * Enumから種別に変換します。<BR>
	 * ※Enumの型は引数で指定できますが、列挙子のNameは統一されている必要があります。<BR>
	 * 
	 * @param value 変換するEnum
	 * @param enumType Enumの型
	 * @return 種別
	 */
	public static <T extends Enum<T>> int enumToType(T value, Class<T> enumType) {
		String name = value.name();
		if (name.equals(ENUM_NAME_STATUS)) {
			return NotifyTypeConstant.TYPE_STATUS;
		} else if (name.equals(ENUM_NAME_EVENT)) {
			return NotifyTypeConstant.TYPE_EVENT;
		} else if (name.equals(ENUM_NAME_MAIL)) {
			return NotifyTypeConstant.TYPE_MAIL;
		} else if (name.equals(ENUM_NAME_JOB)) {
			return NotifyTypeConstant.TYPE_JOB;
		} else if (name.equals(ENUM_NAME_LOG_ESCALATE)) {
			return NotifyTypeConstant.TYPE_LOG_ESCALATE;
		} else if (name.equals(ENUM_NAME_COMMAND)) {
			return NotifyTypeConstant.TYPE_COMMAND;
		} else if (name.equals(ENUM_NAME_INFRA)) {
			return NotifyTypeConstant.TYPE_INFRA;
		} else if (name.equals(ENUM_NAME_REST)) {
			return NotifyTypeConstant.TYPE_REST;
		}else if (name.equals(ENUM_NAME_CLOUD)) {
			return NotifyTypeConstant.TYPE_CLOUD;
		} else if (name.equals(ENUM_NAME_MESSAGE)) {
			return NotifyTypeConstant.TYPE_MESSAGE;
		}
		return -1;
	}
}
