/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.utility.difference.desc.sdml;

import com.clustercontrol.utility.difference.DiffAnnotation;

@DiffAnnotation("{\"type\":\"Element\"}")
public class NotifyRelationInfo {
	private String notifyId;

	@DiffAnnotation("{\"type\":\"PrimaryKey\"}")
	public String getNotifyId() {
		return notifyId;
	}

	public static NotifyRelationInfo getCopiedInstance(com.clustercontrol.utility.settings.sdml.xml.NotifyRelationInfo xmlInfo) {
		NotifyRelationInfo rtn = new NotifyRelationInfo();
		rtn.notifyId = xmlInfo.getNotifyId();
		return rtn;
	}
}
