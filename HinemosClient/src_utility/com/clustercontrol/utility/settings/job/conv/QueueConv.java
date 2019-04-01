/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.job.conv;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.utility.settings.job.xml.JobQueueInfo;
import com.clustercontrol.utility.settings.job.xml.JobQueueList;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.ws.jobmanagement.JobQueueSetting;
import com.clustercontrol.ws.jobmanagement.JobQueueSettingViewInfo;
import com.clustercontrol.ws.jobmanagement.JobQueueSettingViewInfoListItem;

public class QueueConv {

	/** スキーマタイプ */
	private static final String schemaType = "J";
	/** スキーマバージョン */
	private static final String schemaVersion = "1";
	/** スキーマレビジョン */
	private static final String schemaRevision = "1";
	
	/**
	 * コンストラクタ
	 */
	public QueueConv() {
	}

	/**
	 * スキーマのバージョンを返します。
	 * @return
	 */
	public com.clustercontrol.utility.settings.job.xml.SchemaInfo getSchemaVersion() {
		com.clustercontrol.utility.settings.job.xml.SchemaInfo schema = new com.clustercontrol.utility.settings.job.xml.SchemaInfo();
		schema.setSchemaType(schemaType);
		schema.setSchemaVersion(schemaVersion);
		schema.setSchemaRevision(schemaRevision);
		return schema;
	}

	/**
	 * XMLのBeanからHinemosのBeanに変換しします。
	 * @param schedule XMLのBean
	 * @return
	 */
	public List<JobQueueSetting> queueXml2Dto(JobQueueList queueList) {
		List<JobQueueSetting> queueSettingList = new ArrayList<>();
		for (int index = 0; index < queueList.getJobQueueInfo().length; index++) {
			JobQueueInfo queue = queueList.getJobQueueInfo()[index];
			JobQueueSetting info = new JobQueueSetting();
			info.setQueueId(queue.getQueueId());
			info.setName(queue.getName());
			info.setConcurrency(queue.getConcurrency());
			info.setOwnerRoleId(queue.getOwnerRoleId());
			queueSettingList.add(info);
		}
		return queueSettingList;
	}
	
	/**
	 * XMLのBeanからHinemosのBeanに変換しします。
	 * @param schedule XMLのBean
	 * @return
	 */
	public JobQueueInfo[] view2queueXML(JobQueueSettingViewInfo view) {
		List<JobQueueInfo> list = new ArrayList<>();
		
		for (JobQueueSettingViewInfoListItem item : view.getItems()) {
			JobQueueInfo info = new JobQueueInfo();
			info.setQueueId(item.getQueueId());
			info.setName(item.getName());
			info.setConcurrency(item.getConcurrency());
			info.setOwnerRoleId(item.getOwnerRoleId());
			list.add(info);
		}
		return list.toArray(new JobQueueInfo[0]);
	}
	
	/*スキーマのバージョンチェック*/
	public int checkSchemaVersion(String type, String version, String revision) {
		return BaseConv.checkSchemaVersion(
				schemaType,schemaVersion,schemaRevision,
				type,version,revision);
	}
}
