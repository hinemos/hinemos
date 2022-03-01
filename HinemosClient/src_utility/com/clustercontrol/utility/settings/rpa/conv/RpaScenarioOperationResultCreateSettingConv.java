/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.rpa.conv;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openapitools.client.model.NotifyRelationInfoResponse;
import org.openapitools.client.model.RpaScenarioOperationResultCreateSettingResponse;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RpaScenarioOperationResultCreateSettingNotFound;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.rpa.xml.NotifyId;
import com.clustercontrol.utility.settings.rpa.xml.RpaScenarioOperationResultCreateSetting;
import com.clustercontrol.utility.settings.rpa.xml.RpaScenarioOperationResultCreateSettingInfo;
import com.clustercontrol.utility.settings.rpa.xml.RpaScenarioOperationResultCreateSettings;
import com.clustercontrol.utility.util.OpenApiEnumConverter;

public class RpaScenarioOperationResultCreateSettingConv {
	
	static private final String schemaType="K";
	static private final String schemaVersion="1";
	static private final String schemaRevision="1" ;
	
	/* ロガー */
	private static Logger log = Logger.getLogger(RpaScenarioOperationResultCreateSettingConv.class);
	/**
	 * Versionなどの共通部分について、DTOからXMLのBeanに変換します。
	 * 
	 * @param ver　Version情報などのハッシュテーブル。
	 * @return
	 */
	public static com.clustercontrol.utility.settings.rpa.xml.Common
			versionRpaDto2Xml(Hashtable<String,String> ver){
	
		com.clustercontrol.utility.settings.rpa.xml.Common com =
				new com.clustercontrol.utility.settings.rpa.xml.Common();
				
		com.setHinemosVersion(ver.get("hinemosVersion"));
		com.setToolVersion(ver.get("toolVersion"));
		com.setGenerator(ver.get("generator"));
		com.setAuthor(System.getProperty("user.name"));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		com.setGenerateDate(dateFormat.format(new Date()));
		com.setRuntimeHost(ver.get("runtimeHost"));
		com.setConnectedManager(ver.get("connectedManager"));
		
		return com;
	}
	
	/**
	 * XMLとツールの対応バージョンをチェック */
	static public int checkSchemaVersion(String type, String version ,String revision){
		return BaseConv.checkSchemaVersion(
				schemaType,schemaVersion,schemaRevision,
				type,version,revision);
	}
	
	
	/**
	 * スキーマのバージョンを返します。
	 * @return
	 */
	static public com.clustercontrol.utility.settings.rpa.xml.SchemaInfo getSchemaVersion(){
	
		com.clustercontrol.utility.settings.rpa.xml.SchemaInfo schema =
			new com.clustercontrol.utility.settings.rpa.xml.SchemaInfo();
		
		schema.setSchemaType(schemaType);
		schema.setSchemaVersion(schemaVersion);
		schema.setSchemaRevision(schemaRevision);
		
		return schema;
	}
	
	public static RpaScenarioOperationResultCreateSetting 
		getRpaScenarioOperationResultCreateSetting(RpaScenarioOperationResultCreateSettingResponse settingRes)
			throws IndexOutOfBoundsException, ParseException {

		RpaScenarioOperationResultCreateSetting setting = new RpaScenarioOperationResultCreateSetting();
		
		RpaScenarioOperationResultCreateSettingInfo settingInfo = new RpaScenarioOperationResultCreateSettingInfo();
		settingInfo.setRpaScenarioOperationResultCreateSettingId(settingRes.getScenarioOperationResultCreateSettingId());
		settingInfo.setOwnerRoleId(settingRes.getOwnerRoleId());
		settingInfo.setCalendarId(settingRes.getCalendarId());
		settingInfo.setCreateFromDate(settingRes.getCreateFromDate());
		settingInfo.setDescription(settingRes.getDescription());
		settingInfo.setFacilityId(settingRes.getFacilityId());
		settingInfo.setInterval(OpenApiEnumConverter.enumToInteger(settingRes.getInterval()));
		settingInfo.setValidFlg(settingRes.getValidFlg());
		settingInfo.setApplication(settingRes.getApplication());
		
		for (NotifyRelationInfoResponse notifyRelationInfo: settingRes.getNotifyId()) {
			settingInfo.addNotifyId(createNotifyId(notifyRelationInfo));
		}
		
		if(settingInfo.getNotifyId().length == 0){
			NotifyId[] notify = new NotifyId[0];
			settingInfo.setNotifyId(notify);
		}
		
		setting.setRpaScenarioOperationResultCreateSettingInfo(settingInfo);

		return setting;
	}
	
	/**
	 * DTO の NotifyRelationInfo から、castor の NotifyId を作成する
	 * 
	 */
	private static NotifyId createNotifyId(NotifyRelationInfoResponse notifyRelationInfo) {
		NotifyId notifyId = new NotifyId();
		
		notifyId.setNotifyId(notifyRelationInfo.getNotifyId());
		notifyId.setNotifyType(OpenApiEnumConverter.enumToInteger(notifyRelationInfo.getNotifyType()));
		
		return notifyId;
	}
	
	/**
	 * Castor で作成した形式の RPAシナリオ実績作成設定情報を DTO へ変換する<BR>
	 *
	 */
	public static List<RpaScenarioOperationResultCreateSettingResponse> 
		createRpaScenarioOperationResultCreateSettingList(RpaScenarioOperationResultCreateSettings settings) 
			throws ConvertorException, HinemosUnknown, InvalidRole, InvalidUserPass, 
				RpaScenarioOperationResultCreateSettingNotFound, InvalidSetting, ParseException {
		List<RpaScenarioOperationResultCreateSettingResponse> settingList = new LinkedList<RpaScenarioOperationResultCreateSettingResponse>();

		for (RpaScenarioOperationResultCreateSetting setting : settings.getRpaScenarioOperationResultCreateSetting()) {
			log.debug("Setting Id : " + setting.getRpaScenarioOperationResultCreateSettingInfo().getRpaScenarioOperationResultCreateSettingId());
			RpaScenarioOperationResultCreateSettingResponse settingInfo = 
					createRpaScenarioOperationResultCreateSetting
					(setting.getRpaScenarioOperationResultCreateSettingInfo());

			settingList.add(settingInfo);
		}

		return settingList;
	}
	
	public static RpaScenarioOperationResultCreateSettingResponse 
		createRpaScenarioOperationResultCreateSetting(RpaScenarioOperationResultCreateSettingInfo info) {
	
		RpaScenarioOperationResultCreateSettingResponse ret =new RpaScenarioOperationResultCreateSettingResponse();
		
		try {
			ret.setScenarioOperationResultCreateSettingId(info.getRpaScenarioOperationResultCreateSettingId());
			ret.setOwnerRoleId(info.getOwnerRoleId());
			ret.calendarId(info.getCalendarId());
			ret.setCreateFromDate(info.getCreateFromDate());
			ret.setDescription(info.getDescription());
			ret.setFacilityId(info.getFacilityId());
			ret.setInterval(OpenApiEnumConverter.integerToEnum((int)info.getInterval(), RpaScenarioOperationResultCreateSettingResponse.IntervalEnum.class));
			ret.setValidFlg(info.getValidFlg());
			ret.setApplication(info.getApplication());
			
			for (NotifyId notifyId: info.getNotifyId()) {
				ret.getNotifyId().add(createNotifyRelationInfo(notifyId, null));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	
		return ret;
	}
	
	private static NotifyRelationInfoResponse createNotifyRelationInfo(NotifyId notifyId, String groupId) throws InvalidSetting, HinemosUnknown {
		NotifyRelationInfoResponse notifyRelationInfo = new NotifyRelationInfoResponse();
		
		notifyRelationInfo.setNotifyId(notifyId.getNotifyId());
		notifyRelationInfo.setNotifyType(OpenApiEnumConverter.integerToEnum((int)notifyId.getNotifyType() , NotifyRelationInfoResponse.NotifyTypeEnum.class));
		
		return notifyRelationInfo;
	}
}
