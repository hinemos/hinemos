/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.platform.conv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openapitools.client.model.AddNodeConfigSettingInfoRequest;
import org.openapitools.client.model.AddNodeConfigSettingInfoRequest.RunIntervalEnum;
import org.openapitools.client.model.NodeConfigCustomInfoRequest;
import org.openapitools.client.model.NodeConfigCustomInfoResponse;
import org.openapitools.client.model.NodeConfigSettingInfoResponse;
import org.openapitools.client.model.NodeConfigSettingItemInfoResponse;
import org.openapitools.client.model.NotifyRelationInfoRequest;
import org.openapitools.client.model.NotifyRelationInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.repository.bean.NodeConfigSettingItem;
import org.openapitools.client.model.NodeConfigSettingItemInfoRequest;
import org.openapitools.client.model.NodeConfigSettingItemInfoRequest.SettingItemIdEnum;

import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.platform.xml.CustomItemInfo;
import com.clustercontrol.utility.settings.platform.xml.NodeConfigInfo;
import com.clustercontrol.utility.settings.platform.xml.NotifyId;
import com.clustercontrol.utility.settings.platform.xml.SettingItemList;
import com.clustercontrol.utility.util.OpenApiEnumConverter;

/**
 * 構成情報収集設定JavaBeanとXML(Bean)のbindingとのやりとりを
 * 行うクラス<BR>
 * 
 * @version 6.2.0
 * @since 6.2.0
 * 
 */
public class NodeConfigSettingConv {
	static final private String schemaType="I";
	static final private String schemaVersion="1";
	static final private String schemaRevision="1";
	
	public NodeConfigSettingConv() {
	}
	
	/**
	 * XMLとツールの対応バージョンをチェック */
	public int checkSchemaVersion(String type, String version ,String revision) {
		return BaseConv.checkSchemaVersion(schemaType, schemaVersion, schemaRevision,
				type, version, revision);
	}

	/**
	 * スキーマのバージョンを返します。
	 * @return
	 */
	public com.clustercontrol.utility.settings.platform.xml.SchemaInfo getSchemaVersion(){
		com.clustercontrol.utility.settings.platform.xml.SchemaInfo schema = new com.clustercontrol.utility.settings.platform.xml.SchemaInfo();

		schema.setSchemaType(schemaType);
		schema.setSchemaVersion(schemaVersion);
		schema.setSchemaRevision(schemaRevision);
		return schema;
	}
	

	public NodeConfigInfo[] convDto2Xml(List<NodeConfigSettingInfoResponse> nodeConfigSettingList) {
		List<NodeConfigInfo> list = new ArrayList<>();
		for (NodeConfigSettingInfoResponse nodeConfigSettingInfo : nodeConfigSettingList) {
			NodeConfigInfo nodeConfigInfo = new NodeConfigInfo();
			
			nodeConfigInfo.setSettingId(nodeConfigSettingInfo.getSettingId());
			nodeConfigInfo.setSettingName(nodeConfigSettingInfo.getSettingName());
			nodeConfigInfo.setFacilityId(nodeConfigSettingInfo.getFacilityId());
			nodeConfigInfo.setScope(nodeConfigSettingInfo.getScope());
			nodeConfigInfo.setOwnerRoleId(nodeConfigSettingInfo.getOwnerRoleId());
			int runIntervalEnumInt = OpenApiEnumConverter.enumToInteger(nodeConfigSettingInfo.getRunInterval());
			nodeConfigInfo.setRunInterval(runIntervalEnumInt);
			
			nodeConfigInfo.setDescription(nodeConfigSettingInfo.getDescription());
			nodeConfigInfo.setCalendarId(nodeConfigSettingInfo.getCalendarId());
			nodeConfigInfo.setValidFlg(nodeConfigSettingInfo.getValidFlg());

			SettingItemList itemList = new SettingItemList();
			for (NodeConfigSettingItemInfoResponse item : nodeConfigSettingInfo.getNodeConfigSettingItemList()) {
				if(item.getSettingItemId().toString().equals(NodeConfigSettingItem.CUSTOM.toString())) {
					continue;
				}
				itemList.addSettingItemId(item.getSettingItemId().name());
			}
			nodeConfigInfo.setSettingItemList(itemList);

			List<CustomItemInfo> customlist = new ArrayList<>();
			for (NodeConfigCustomInfoResponse custom : nodeConfigSettingInfo.getNodeConfigCustomList()) {
				CustomItemInfo customInfo = new CustomItemInfo();
				customInfo.setSettingCustomId(custom.getSettingCustomId());
				customInfo.setDisplayName(custom.getDisplayName());
				customInfo.setDescription(custom.getDescription());
				customInfo.setCommand(custom.getCommand());
				customInfo.setSpecifyUser(custom.getSpecifyUser());
				customInfo.setEffectiveUser(custom.getEffectiveUser());
				
				customInfo.setValidFlg(custom.getValidFlg());
				customlist.add(customInfo);
			}
			nodeConfigInfo.setCustomItemInfo(customlist.toArray(new CustomItemInfo[0]));
			
			List<NotifyId> notifyIdList = new ArrayList<>();
			for (NotifyRelationInfoResponse notifyInfo: nodeConfigSettingInfo.getNotifyRelationList()) {
				NotifyId notifyId = new NotifyId();
				notifyId.setNotifyId(notifyInfo.getNotifyId());
				int notifyTypeEnumInt = OpenApiEnumConverter.enumToInteger(notifyInfo.getNotifyType());
				notifyId.setNotifyType(notifyTypeEnumInt);
				notifyIdList.add(notifyId);
			}
			nodeConfigInfo.setNotifyId(notifyIdList.toArray(new NotifyId[0]));
			
			list.add(nodeConfigInfo);
		}
		return list.toArray(new NodeConfigInfo[0]);
	}

	
	public AddNodeConfigSettingInfoRequest convXml2Dto(NodeConfigInfo nodeConfig) throws InvalidSetting, HinemosUnknown {
		AddNodeConfigSettingInfoRequest nodeConfigSettingInfo = null;
		for (NodeConfigInfo nodeConfigInfo : Arrays.asList(nodeConfig) ) {
			nodeConfigSettingInfo = new AddNodeConfigSettingInfoRequest();

			nodeConfigSettingInfo.setSettingId(nodeConfigInfo.getSettingId());
			nodeConfigSettingInfo.setSettingName(nodeConfigInfo.getSettingName());
			nodeConfigSettingInfo.setFacilityId(nodeConfigInfo.getFacilityId());
			nodeConfigSettingInfo.setOwnerRoleId(nodeConfigInfo.getOwnerRoleId());
			RunIntervalEnum runIntervalEnum = 
					OpenApiEnumConverter.integerToEnum(nodeConfigInfo.getRunInterval(), RunIntervalEnum.class);
			nodeConfigSettingInfo.setRunInterval(runIntervalEnum);

			nodeConfigSettingInfo.setDescription(nodeConfigInfo.getDescription());
			nodeConfigSettingInfo.setCalendarId(nodeConfigInfo.getCalendarId());
			nodeConfigSettingInfo.setValidFlg(nodeConfigInfo.getValidFlg());

			if (nodeConfigInfo.getSettingItemList() != null) {
				for (String item : nodeConfigInfo.getSettingItemList().getSettingItemId()) {
					NodeConfigSettingItemInfoRequest itemInfo = new NodeConfigSettingItemInfoRequest();
					itemInfo.setSettingItemId(SettingItemIdEnum.valueOf(item));
					nodeConfigSettingInfo.getNodeConfigSettingItemList().add(itemInfo);
				}
			}

			boolean customIsValid = false;
			for (CustomItemInfo custom : nodeConfigInfo.getCustomItemInfo()) {
				NodeConfigCustomInfoRequest customInfo = new NodeConfigCustomInfoRequest();
				customInfo.setSettingCustomId(custom.getSettingCustomId());
				customInfo.setDisplayName(custom.getDisplayName());
				customInfo.setDescription(custom.getDescription());
				customInfo.setCommand(custom.getCommand());
				customInfo.setSpecifyUser(custom.getSpecifyUser());
				if (custom.getSpecifyUser()) {
					customInfo.setEffectiveUser(custom.getEffectiveUser());
				}

				customInfo.setValidFlg(custom.getValidFlg());
				if (custom.getValidFlg()) {
					customIsValid = true;
				}
				nodeConfigSettingInfo.getNodeConfigCustomList().add(customInfo);
			}
			if (customIsValid) {
				NodeConfigSettingItemInfoRequest itemInfo = new NodeConfigSettingItemInfoRequest();
				itemInfo.setSettingItemId(SettingItemIdEnum.valueOf(NodeConfigSettingItem.CUSTOM.name()));
				nodeConfigSettingInfo.getNodeConfigSettingItemList().add(itemInfo);
			}
			
			for (NotifyId notify : nodeConfigInfo.getNotifyId()) {
				NotifyRelationInfoRequest notifyInfo = new NotifyRelationInfoRequest();
				notifyInfo.setNotifyId(notify.getNotifyId());
				nodeConfigSettingInfo.getNotifyRelationList().add(notifyInfo);
			}
			
		}
		return nodeConfigSettingInfo;
	}
}
