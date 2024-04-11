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
import com.clustercontrol.version.util.VersionUtil;

public class QueueConv {

	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	/** スキーマタイプ */
	private static final String schemaType = VersionUtil.getSchemaProperty("JOB.QUEUE.SCHEMATYPE");
	/** スキーマバージョン */
	private static final String schemaVersion = VersionUtil.getSchemaProperty("JOB.QUEUE.SCHEMAVERSION");
	/** スキーマレビジョン */
	private static final String schemaRevision = VersionUtil.getSchemaProperty("JOB.QUEUE.SCHEMAREVISION");
	
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
