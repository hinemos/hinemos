/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.utility.settings.job.conv;

import java.util.ArrayList;
import java.util.List;

import org.openapitools.client.model.AddJobLinkSendSettingRequest;
import org.openapitools.client.model.AddJobLinkSendSettingRequest.ProcessModeEnum;
import org.openapitools.client.model.AddJobLinkSendSettingRequest.ProtocolEnum;
import org.openapitools.client.model.JobLinkSendSettingResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.utility.settings.AbstractConvertor;
import com.clustercontrol.utility.settings.job.xml.JobLinkSendInfo;
import com.clustercontrol.utility.util.OpenApiEnumConverter;
import com.clustercontrol.version.util.VersionUtil;

public class JobLinkSendConv extends AbstractConvertor {

	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	/** スキーマタイプ */
	private static final String schemaType = VersionUtil.getSchemaProperty("JOB.JOBLINKSEND.SCHEMATYPE");
	/** スキーマバージョン */
	private static final String schemaVersion = VersionUtil.getSchemaProperty("JOB.JOBLINKSEND.SCHEMAVERSION");
	/** スキーマレビジョン */
	private static final String schemaRevision = VersionUtil.getSchemaProperty("JOB.JOBLINKSEND.SCHEMAREVISION");
	
	/**
	 * コンストラクタ
	 */
	public JobLinkSendConv() {
		super.schemaType = JobLinkSendConv.schemaType;
		super.schemaVersion = JobLinkSendConv.schemaVersion;
		super.schemaRevision = JobLinkSendConv.schemaRevision;
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
	public AddJobLinkSendSettingRequest jobLinkSendXml2Dto(JobLinkSendInfo jobLinkSend) throws InvalidSetting, HinemosUnknown {
		AddJobLinkSendSettingRequest info = new AddJobLinkSendSettingRequest();
		info.setJoblinkSendSettingId(jobLinkSend.getJoblinkSendSettingId());
		info.setOwnerRoleId(jobLinkSend.getOwnerRoleId());
		info.setDescription(jobLinkSend.getDescription());
		info.setFacilityId(jobLinkSend.getFacilityId());
		ProcessModeEnum processModeEnum = OpenApiEnumConverter.integerToEnum(jobLinkSend.getProcessMode(), ProcessModeEnum.class);
		info.setProcessMode(processModeEnum);
		ProtocolEnum protocolEnum = OpenApiEnumConverter.stringToEnum(jobLinkSend.getProtocol(), ProtocolEnum.class);
		info.setProtocol(protocolEnum);
		if(jobLinkSend.hasPort()){
			info.setPort(jobLinkSend.getPort());
		}
		info.setHinemosUserId(jobLinkSend.getHinemosUserId());
		info.setHinemosPassword(jobLinkSend.getHinemosPassword());
		info.setProxyFlg(jobLinkSend.getProxyFlg());
		info.setProxyHost(jobLinkSend.getProxyHost());
		if(jobLinkSend.hasProxyPort()){
			info.setProxyPort(jobLinkSend.getProxyPort());
		}
		info.setProxyUser(jobLinkSend.getProxyUser());
		info.setProxyPassword(jobLinkSend.getProxyPassword());
		return info;
	}

	/**
	 * XMLのBeanからHinemosのBeanに変換しします。
	 * @param schedule XMLのBean
	 * @return
	 */
	public JobLinkSendInfo[] jobLinkDto2XML(List<JobLinkSendSettingResponse> jobLinkSendList) {
		List<JobLinkSendInfo> list = new ArrayList<>();
		for(JobLinkSendSettingResponse jobLinkSend: jobLinkSendList){
			JobLinkSendInfo info = new JobLinkSendInfo();
			info.setJoblinkSendSettingId(jobLinkSend.getJoblinkSendSettingId());
			info.setOwnerRoleId(jobLinkSend.getOwnerRoleId());
			info.setDescription(jobLinkSend.getDescription());
			info.setFacilityId(jobLinkSend.getFacilityId());
			int processModeEnum = OpenApiEnumConverter.enumToInteger(jobLinkSend.getProcessMode());
			info.setProcessMode(processModeEnum);
			String protocolEnum = OpenApiEnumConverter.enumToString(jobLinkSend.getProtocol());
			info.setProtocol(protocolEnum);
			if (jobLinkSend.getPort() != null) {
				info.setPort(jobLinkSend.getPort());
			}
			info.setHinemosUserId(jobLinkSend.getHinemosUserId());
			info.setHinemosPassword(jobLinkSend.getHinemosPassword());
			info.setProxyFlg(jobLinkSend.getProxyFlg());
			info.setProxyHost(jobLinkSend.getProxyHost());
			if (jobLinkSend.getProxyPort() != null) {
				info.setProxyPort(jobLinkSend.getProxyPort());
			}
			info.setProxyUser(jobLinkSend.getProxyUser());
			info.setProxyPassword(jobLinkSend.getProxyPassword());
			list.add(info);
		}
		return list.toArray(new JobLinkSendInfo[0]);
	}

}
