/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.utility.difference.desc.sdml;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.utility.difference.DiffAnnotation;

@DiffAnnotation("{\"type\":\"Element\"}")
public class MonitorNotifyRelation {
	private String sdmlMonitorTypeId;
	private List<NotifyRelationInfo> notifyIds = new ArrayList<>();

	@DiffAnnotation("{\"type\":\"PrimaryKey\"}")
	public String getSdmlMonitorTypeId() {
		return sdmlMonitorTypeId;
	}

	@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"MonitorNotifyRelation_notifyId\"}", "{\"type\":\"Array\"}"})
	public NotifyRelationInfo[] getNotifyIds() {
		return notifyIds.toArray(new NotifyRelationInfo[0]);
	}

	public static MonitorNotifyRelation getCopiedInstance(com.clustercontrol.utility.settings.sdml.xml.MonitorNotifyRelation xmlInfo) {
		MonitorNotifyRelation rtn = new MonitorNotifyRelation();
		rtn.sdmlMonitorTypeId = xmlInfo.getSdmlMonitorTypeId();
		for (com.clustercontrol.utility.settings.sdml.xml.NotifyId notify : xmlInfo.getNotifyId()) {
			rtn.notifyIds.add(NotifyRelationInfo.getCopiedInstance(notify));
		}
		return rtn;
	}
}
