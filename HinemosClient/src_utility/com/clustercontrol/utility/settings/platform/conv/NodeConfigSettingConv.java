/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.platform.conv;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.repository.bean.NodeConfigSettingItem;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.platform.xml.CustomItemInfo;
import com.clustercontrol.utility.settings.platform.xml.NodeConfigInfo;
import com.clustercontrol.utility.settings.platform.xml.NodeConfigList;
import com.clustercontrol.utility.settings.platform.xml.NotifyId;
import com.clustercontrol.utility.settings.platform.xml.SettingItemList;
import com.clustercontrol.ws.notify.NotifyRelationInfo;
import com.clustercontrol.ws.repository.NodeConfigCustomInfo;
import com.clustercontrol.ws.repository.NodeConfigSettingInfo;
import com.clustercontrol.ws.repository.NodeConfigSettingItemInfo;

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
	

	public NodeConfigInfo[] convDto2Xml(List<NodeConfigSettingInfo> nodeConfigSettingList) {
		List<NodeConfigInfo> list = new ArrayList<>();
		for (NodeConfigSettingInfo nodeConfigSettingInfo : nodeConfigSettingList) {
			NodeConfigInfo nodeConfigInfo = new NodeConfigInfo();
			
			nodeConfigInfo.setSettingId(nodeConfigSettingInfo.getSettingId());
			nodeConfigInfo.setSettingName(nodeConfigSettingInfo.getSettingName());
			nodeConfigInfo.setFacilityId(nodeConfigSettingInfo.getFacilityId());
			nodeConfigInfo.setScope(nodeConfigSettingInfo.getScope());
			nodeConfigInfo.setOwnerRoleId(nodeConfigSettingInfo.getOwnerRoleId());
			nodeConfigInfo.setRunInterval(nodeConfigSettingInfo.getRunInterval());
			
			nodeConfigInfo.setDescription(nodeConfigSettingInfo.getDescription());
			nodeConfigInfo.setCalendarId(nodeConfigSettingInfo.getCalendarId());
			nodeConfigInfo.setValidFlg(nodeConfigSettingInfo.isValidFlg());

			SettingItemList itemList = new SettingItemList();
			for (NodeConfigSettingItemInfo item : nodeConfigSettingInfo.getNodeConfigSettingItemList()) {
				if(item.getSettingItemId().equals(NodeConfigSettingItem.CUSTOM.toString())) {
					continue;
				}
				itemList.addSettingItemId(item.getSettingItemId());
			}
			nodeConfigInfo.setSettingItemList(itemList);

			List<CustomItemInfo> customlist = new ArrayList<>();
			for (NodeConfigCustomInfo custom : nodeConfigSettingInfo.getNodeConfigCustomList()) {
				CustomItemInfo customInfo = new CustomItemInfo();
				customInfo.setSettingCustomId(custom.getSettingCustomId());
				customInfo.setDisplayName(custom.getDisplayName());
				customInfo.setDescription(custom.getDescription());
				customInfo.setCommand(custom.getCommand());
				customInfo.setSpecifyUser(custom.isSpecifyUser());
				customInfo.setEffectiveUser(custom.getEffectiveUser());
				
				customInfo.setValidFlg(custom.isValidFlg());
				customlist.add(customInfo);
			}
			nodeConfigInfo.setCustomItemInfo(customlist.toArray(new CustomItemInfo[0]));
			
			List<NotifyId> notifyIdList = new ArrayList<>();
			for (NotifyRelationInfo notifyInfo: nodeConfigSettingInfo.getNotifyRelationList()) {
				NotifyId notifyId = new NotifyId();
				notifyId.setNotifyId(notifyInfo.getNotifyId());
				notifyId.setNotifyType(notifyInfo.getNotifyType());
				notifyIdList.add(notifyId);
			}
			nodeConfigInfo.setNotifyId(notifyIdList.toArray(new NotifyId[0]));
			
			list.add(nodeConfigInfo);
		}
		return list.toArray(new NodeConfigInfo[0]);
	}

	
	public List<NodeConfigSettingInfo> convXml2Dto(NodeConfigList nodeConfig) {
		List<NodeConfigSettingInfo> list = new ArrayList<>();
		for (NodeConfigInfo nodeConfigInfo : nodeConfig.getNodeConfigInfo()) {
			NodeConfigSettingInfo nodeConfigSettingInfo = new NodeConfigSettingInfo();

			nodeConfigSettingInfo.setSettingId(nodeConfigInfo.getSettingId());
			nodeConfigSettingInfo.setSettingName(nodeConfigInfo.getSettingName());
			nodeConfigSettingInfo.setFacilityId(nodeConfigInfo.getFacilityId());
			nodeConfigSettingInfo.setOwnerRoleId(nodeConfigInfo.getOwnerRoleId());
			nodeConfigSettingInfo.setRunInterval(nodeConfigInfo.getRunInterval());

			nodeConfigSettingInfo.setDescription(nodeConfigInfo.getDescription());
			nodeConfigSettingInfo.setCalendarId(nodeConfigInfo.getCalendarId());
			nodeConfigSettingInfo.setValidFlg(nodeConfigInfo.getValidFlg());

			if (nodeConfigInfo.getSettingItemList() != null) {
				for (String item : nodeConfigInfo.getSettingItemList().getSettingItemId()) {
					NodeConfigSettingItemInfo itemInfo = new NodeConfigSettingItemInfo();
					itemInfo.setSettingItemId(item);
					nodeConfigSettingInfo.getNodeConfigSettingItemList().add(itemInfo);
				}
			}

			boolean customIsValid = false;
			for (CustomItemInfo custom : nodeConfigInfo.getCustomItemInfo()) {
				NodeConfigCustomInfo customInfo = new NodeConfigCustomInfo();
				customInfo.setSettingCustomId(custom.getSettingCustomId());
				customInfo.setDisplayName(custom.getDisplayName());
				customInfo.setDescription(custom.getDescription());
				customInfo.setCommand(custom.getCommand());
				customInfo.setSpecifyUser(custom.getSpecifyUser());
				customInfo.setEffectiveUser(custom.getEffectiveUser());

				customInfo.setValidFlg(custom.getValidFlg());
				if (custom.getValidFlg()) {
					customIsValid = true;
				}
				nodeConfigSettingInfo.getNodeConfigCustomList().add(customInfo);
			}
			if (customIsValid) {
				NodeConfigSettingItemInfo itemInfo = new NodeConfigSettingItemInfo();
				itemInfo.setSettingItemId(NodeConfigSettingItem.CUSTOM.toString());
				nodeConfigSettingInfo.getNodeConfigSettingItemList().add(itemInfo);
			}
			
			for (NotifyId notify : nodeConfigInfo.getNotifyId()) {
				NotifyRelationInfo notifyInfo = new NotifyRelationInfo();
				notifyInfo.setNotifyId(notify.getNotifyId());
				notifyInfo.setNotifyType(notify.getNotifyType());
				nodeConfigSettingInfo.getNotifyRelationList().add(notifyInfo);
			}
			
			list.add(nodeConfigSettingInfo);
		}
		return list;
	}
}
