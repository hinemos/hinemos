/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.conv;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorPluginStringInfoResponse;
import org.openapitools.client.model.MonitorTruthValueInfoResponse;
import org.openapitools.client.model.PluginCheckInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import org.openapitools.client.model.PlatformServiceConditionResponse;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.CloudServiceInfo;
import com.clustercontrol.utility.settings.monitor.xml.CloudServiceMonitor;
import com.clustercontrol.utility.settings.monitor.xml.CloudServiceMonitors;
import com.clustercontrol.utility.settings.monitor.xml.PluginStringValue;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.settings.monitor.xml.TruthValue;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.version.util.VersionUtil;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;

/**
 * クラウドサービス 監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 * @version 6.1.0
 * @since 6.0.0
 *
 */
public class CloudServiceConv {
	private final static Log logger = LogFactory.getLog(CloudServiceConv.class);

	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	static private String SCHEMA_TYPE = VersionUtil.getSchemaProperty("MONITOR.CLOUDSERVICE.SCHEMATYPE");
	static private String SCHEMA_VERSION = VersionUtil.getSchemaProperty("MONITOR.CLOUDSERVICE.SCHEMAVERSION");
	static private String SCHEMA_REVISION =VersionUtil.getSchemaProperty("MONITOR.CLOUDSERVICE.SCHEMAREVISION");
	
	static private String KEY_TARGETS = "targets";
	static private String REGION = "REGION";
	
	
	/**
	 * <BR>
	 *
	 * @return
	 */
	public static SchemaInfo getSchemaVersion(){
		SchemaInfo schema = new SchemaInfo();

		schema.setSchemaType(SCHEMA_TYPE);
		schema.setSchemaVersion(SCHEMA_VERSION);
		schema.setSchemaRevision(SCHEMA_REVISION);

		return schema;
	}

	/*スキーマのバージョンチェック*/
	public static int checkSchemaVersion(SchemaInfo schemaInfo) {
		return BaseConv.checkSchemaVersion(
				SCHEMA_TYPE,
				SCHEMA_VERSION,
				SCHEMA_REVISION,
				schemaInfo.getSchemaType(),
				schemaInfo.getSchemaVersion(),
				schemaInfo.getSchemaRevision()
				);
	}

	/**
	 * <BR>
	 *
	 * @return
	 * @throws RestConnectFailed 
	 * @throws ParseException 
	 * @throws MonitorNotFound
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	
	public static CloudServiceMonitors
		createCloudServiceMonitors(List<MonitorInfoResponse> monitorInfoList)
				throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		CloudServiceMonitors cloudServiceMonitors = new CloudServiceMonitors();

		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorsettingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			CloudServiceMonitor cloudServiceMonitor = new CloudServiceMonitor();
			cloudServiceMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			for (MonitorTruthValueInfoResponse truthValueInfo : monitorInfo.getTruthValueInfo()) {
				cloudServiceMonitor.addTruthValue(MonitorConv.createTruthValue(monitorInfo.getMonitorId(),truthValueInfo));
			}

			cloudServiceMonitor.setCloudServiceInfo(createCloudServiceInfo(monitorInfo.getMonitorId() ,monitorInfo.getPluginCheckInfo()));
			cloudServiceMonitors.addCloudServiceMonitor(cloudServiceMonitor);
		}

		cloudServiceMonitors.setCommon(MonitorConv.versionDto2Xml());
		cloudServiceMonitors.setSchemaInfo(getSchemaVersion());

		return cloudServiceMonitors;
	}
	

	public static List<MonitorInfoResponse> createMonitorInfoList(CloudServiceMonitors cloudServiceMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException, RestConnectFailed{
		List<MonitorInfoResponse> monitorInfoList = new LinkedList<MonitorInfoResponse>();

		
		List<PlatformServiceConditionResponse> conditions = null;
		
		for (CloudServiceMonitor cloudServiceMonitor : cloudServiceMonitors.getCloudServiceMonitor()) {
			logger.debug("Monitor Id : " + cloudServiceMonitor.getMonitor().getMonitorId());
			boolean matched = false;
			String errMessage = "";
			String value = getPluginStringValue(KEY_TARGETS, cloudServiceMonitor.getCloudServiceInfo().getPluginStringValue());
			if(!value.isEmpty()){
				String[] values = cloudServiceMonitor.getMonitor().getFacilityId().split("_", 0);
				boolean region = REGION.equals(values[2]);
				int cloudIdValStPos =  2;
				int cloudIdValEdPos =  values.length - 1;
				String locationId;
				if (region){
					cloudIdValStPos = 3;
					cloudIdValEdPos -= 1;
					locationId = values[ values.length -1 ];
				} else {
					locationId = null;
				}
				String[] cloudScopeIdEditAry = new String[ cloudIdValEdPos - cloudIdValStPos + 1 ]  ;
				for( int loopCnt = cloudIdValStPos ; loopCnt <= cloudIdValEdPos ;  loopCnt++){
					cloudScopeIdEditAry[ loopCnt - cloudIdValStPos ]=values[loopCnt];
				}
				String cloudScopeId = String.join( "_" , cloudScopeIdEditAry  );
				logger.debug("cloudScopeId : " + cloudScopeId );
				try {
					CloudRestClientWrapper endpoint = CloudRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
					conditions = endpoint.getPlatformServiceConditions( cloudScopeId
							,locationId , cloudServiceMonitor.getMonitor().getOwnerRoleId());
					for (PlatformServiceConditionResponse condition :conditions){
						if (condition.getId().equals(value)){
							matched = true;	
							break;
						}
					}
					if(!matched){
						errMessage = Messages.getString("SettingTools.EssentialValueInvalid") + " Service = " + value;
					}
				} catch (CloudManagerException | InvalidRole | InvalidUserPass e) {
					errMessage = Messages.getString(HinemosMessage.replace(e.getMessage()));
				}
			}
			else{
				errMessage = Messages.getString("SettingTools.EssentialValueInvalid") + " Service = " + value;
			}
			
			if (!matched){
				MonitorInfoResponse erroMonitor = MonitorConv.makeErrorMonitor(
						cloudServiceMonitor.getMonitor().getMonitorId(), errMessage);
				monitorInfoList.add(erroMonitor);
			}
			else{
				MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(cloudServiceMonitor.getMonitor());
				for (TruthValue truthValue : cloudServiceMonitor.getTruthValue()) {
					monitorInfo.getTruthValueInfo().add(MonitorConv.createTruthValue(truthValue));
				}
				monitorInfo.setPluginCheckInfo
					(createCloudServiceCheckInfo(cloudServiceMonitor.getCloudServiceInfo()));
				monitorInfoList.add(monitorInfo);
			}
		}

		return monitorInfoList;
	}
	
	/**
	 * <BR>
	 * WS->XML
	 * @return
	 */
	
	private static CloudServiceInfo createCloudServiceInfo(String monitorId,PluginCheckInfoResponse pluginCheckInfo) {
		CloudServiceInfo cloudServiceInfo = new CloudServiceInfo();
		cloudServiceInfo.setMonitorTypeId("");

		cloudServiceInfo.setMonitorId(monitorId);
		
		List<MonitorPluginStringInfoResponse> monitorPluginStringInfo =
				pluginCheckInfo.getMonitorPluginStringInfoList();
		
		com.clustercontrol.utility.settings.monitor.xml.PluginStringValue pluginStringValueArray
			= new com.clustercontrol.utility.settings.monitor.xml.PluginStringValue();
		
		pluginStringValueArray.setKey(monitorPluginStringInfo.get(0).getKey());
		pluginStringValueArray.setMonitorId(monitorId);
		pluginStringValueArray.setValue(monitorPluginStringInfo.get(0).getValue());

		cloudServiceInfo.setPluginStringValue(pluginStringValueArray);;
		
		return cloudServiceInfo;
	}
	
	/**
	 * <BR>
	 *
	 * @return
	 */
	private static PluginCheckInfoResponse createCloudServiceCheckInfo(CloudServiceInfo cloudServiceInfo) {
		PluginCheckInfoResponse pluginCheckInfo = new PluginCheckInfoResponse();
		PluginStringValue pluginStringValue = cloudServiceInfo.getPluginStringValue();
		MonitorPluginStringInfoResponse info = null;
		info = new MonitorPluginStringInfoResponse();
		info.setKey(pluginStringValue.getKey());
		info.setValue(pluginStringValue.getValue());
		pluginCheckInfo.getMonitorPluginStringInfoList().add(info);
		
		return pluginCheckInfo;
	}
	
	private static String getPluginStringValue(String key, PluginStringValue pluginStringValue) {
		if (pluginStringValue.getKey().equals(key))
			return pluginStringValue.getValue();
		return "";
	}
}