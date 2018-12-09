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
		default:
			return "";
		}
	}
}
