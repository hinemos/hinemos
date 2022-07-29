/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.bean;

import java.util.ArrayList;
import java.util.List;

public class NotifyTypeConstant {

	/*通知タイプとコード*/
	public static final int TYPE_STATUS = 0;
	public static final int TYPE_EVENT = 1;
	public static final int TYPE_MAIL = 2;
	public static final int TYPE_JOB = 3;
	public static final int TYPE_LOG_ESCALATE=4;
	public static final int TYPE_COMMAND=5;
	public static final int TYPE_INFRA = 6;
	public static final int TYPE_REST = 7;
	public static final int TYPE_CLOUD = 8;
	public static final int TYPE_MESSAGE = 9;

	/** 通知ID種別 */
	public static final int NOTIFY_ID_TYPE_DEFAULT = 0;

	/**
	 * 通知タイプからその優先度を取得します。
	 * セルフチェック機能など、DBが使えない状態での通知を考慮し、
	 * 障害耐久性の高い通知方法のプライオリティが高く設定されます。
	 * @param notifyType 通知タイプ
	 * @return 通知の優先度（値が小さいほうが優先度が高い）
	 */
	public static int getPriority(int notifyType){
		switch (notifyType) {
		case NotifyTypeConstant.TYPE_MAIL:
			return 1;
		case NotifyTypeConstant.TYPE_LOG_ESCALATE:
			return 2;
		case NotifyTypeConstant.TYPE_COMMAND:
			return 3;
		case NotifyTypeConstant.TYPE_EVENT:
			return 4;
		case NotifyTypeConstant.TYPE_STATUS:
			return 5;
		case NotifyTypeConstant.TYPE_JOB:
			return 6;
		case NotifyTypeConstant.TYPE_INFRA:
			return 7;
		case NotifyTypeConstant.TYPE_REST:
			return 8;
		case NotifyTypeConstant.TYPE_CLOUD:
			return 9;
		case NotifyTypeConstant.TYPE_MESSAGE:
			return 10;
		default:
			return 100;
		}
	}

	public static List<Integer> getList() {
		List<Integer> types = new ArrayList<Integer>();

		types.add(TYPE_STATUS);
		types.add(TYPE_EVENT);
		types.add(TYPE_MAIL);
		types.add(TYPE_JOB);
		types.add(TYPE_LOG_ESCALATE);
		types.add(TYPE_COMMAND);
		types.add(TYPE_INFRA);
		types.add(TYPE_REST);
		types.add(TYPE_CLOUD);
		types.add(TYPE_MESSAGE);

		return types;
	}

}
