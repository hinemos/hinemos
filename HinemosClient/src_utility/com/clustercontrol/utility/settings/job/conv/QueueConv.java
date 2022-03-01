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

import org.openapitools.client.model.AddJobQueueRequest;
import org.openapitools.client.model.JobQueueSettingViewInfoListItemResponse;
import org.openapitools.client.model.JobQueueSettingViewInfoResponse;

import com.clustercontrol.utility.settings.job.xml.JobQueueInfo;
import com.clustercontrol.utility.settings.model.BaseConv;

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
	public AddJobQueueRequest queueXml2Dto(JobQueueInfo queue) {
		AddJobQueueRequest info = new AddJobQueueRequest();
		info.setQueueId(queue.getQueueId());
		info.setName(queue.getName());
		info.setConcurrency(queue.getConcurrency());
		info.setOwnerRoleId(queue.getOwnerRoleId());
		return info;
	}
	
	/**
	 * XMLのBeanからHinemosのBeanに変換しします。
	 * @param schedule XMLのBean
	 * @return
	 */
	public JobQueueInfo[] view2queueXML(JobQueueSettingViewInfoResponse view) {
		List<JobQueueInfo> list = new ArrayList<>();
		
		for(JobQueueSettingViewInfoListItemResponse tmp: view.getItems()){
			JobQueueInfo info = new JobQueueInfo();
			info.setQueueId(tmp.getQueueId());
			info.setName(tmp.getName());
			info.setConcurrency(tmp.getConcurrency());
			info.setOwnerRoleId(tmp.getOwnerRoleId());
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
