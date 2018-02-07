/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.conv;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.cloud.action.CloudTools;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.CloudServiceInfo;
import com.clustercontrol.utility.settings.monitor.xml.CloudServiceMonitor;
import com.clustercontrol.utility.settings.monitor.xml.CloudServiceMonitors;
import com.clustercontrol.utility.settings.monitor.xml.PluginStringValue;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.settings.monitor.xml.TruthValue;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.ws.xcloud.CloudManagerException;
import com.clustercontrol.ws.xcloud.PlatformServiceCondition;

/**
 * クラウドサービス 監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 * @version 6.1.0
 * @since 6.0.0
 *
 */
public class CloudServiceConv {
	private final static Log logger = LogFactory.getLog(CloudServiceConv.class);

	static private String SCHEMA_TYPE = "H";
	static private String SCHEMA_VERSION = "1";
	static private String SCHEMA_REVISION = "2";
	
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
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	
	public static CloudServiceMonitors
		createCloudServiceMonitors(List<com.clustercontrol.ws.monitor.MonitorInfo> monitorInfoList)
				throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		CloudServiceMonitors cloudServiceMonitors = new CloudServiceMonitors();

		for (com.clustercontrol.ws.monitor.MonitorInfo monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorSettingEndpointWrapper
					.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			CloudServiceMonitor cloudServiceMonitor = new CloudServiceMonitor();
			cloudServiceMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			for (com.clustercontrol.ws.monitor.MonitorTruthValueInfo truthValueInfo : monitorInfo.getTruthValueInfo()) {
				cloudServiceMonitor.addTruthValue(MonitorConv.createTruthValue(truthValueInfo));
			}

			cloudServiceMonitor.setCloudServiceInfo(createCloudServiceInfo(monitorInfo.getPluginCheckInfo()));
			cloudServiceMonitors.addCloudServiceMonitor(cloudServiceMonitor);
		}

		cloudServiceMonitors.setCommon(MonitorConv.versionDto2Xml());
		cloudServiceMonitors.setSchemaInfo(getSchemaVersion());

		return cloudServiceMonitors;
	}
	

	public static List<com.clustercontrol.ws.monitor.MonitorInfo> createMonitorInfoList(CloudServiceMonitors cloudServiceMonitors) throws ConvertorException {
		List<com.clustercontrol.ws.monitor.MonitorInfo> monitorInfoList = new LinkedList<com.clustercontrol.ws.monitor.MonitorInfo>();

		
		com.clustercontrol.ws.xcloud.CloudEndpoint endpoint = CloudTools.getEndpoint(com.clustercontrol.ws.xcloud.CloudEndpoint.class);
		List<PlatformServiceCondition> conditions = null;
		
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
				if (region){
					cloudIdValStPos = 3;
					cloudIdValEdPos -= 1;
				}
				String[] cloudScopeIdEditAry = new String[ cloudIdValEdPos - cloudIdValStPos + 1 ]  ;
				for( int loopCnt = cloudIdValStPos ; loopCnt <= cloudIdValEdPos ;  loopCnt++){
					cloudScopeIdEditAry[ loopCnt - cloudIdValStPos ]=values[loopCnt];
				}
				String cloudScopeId = String.join( "_" , cloudScopeIdEditAry  );
				logger.debug("cloudScopeId : " + cloudScopeId );
				try {
					if (region){
						String locationId = values[ values.length -1 ]; 
						logger.debug("locationId :" + locationId);
						conditions = endpoint.getPlatformServiceConditionsByLocationAndRole( cloudScopeId
								,locationId , cloudServiceMonitor.getMonitor().getOwnerRoleId());
					}else{
						conditions = endpoint.getPlatformServiceConditionsByRole(cloudScopeId,
								cloudServiceMonitor.getMonitor().getOwnerRoleId());
					}
					for (PlatformServiceCondition condition :conditions){
						if (condition.getId().equals(value)){
							matched = true;	
							break;
						}
					}
					if(!matched){
						errMessage = Messages.getString("SettingTools.EssentialValueInvalid") + " Service = " + value;
					}
				} catch (CloudManagerException | com.clustercontrol.ws.xcloud.InvalidRole_Exception
						| com.clustercontrol.ws.xcloud.InvalidUserPass_Exception e) {
					errMessage = Messages.getString(HinemosMessage.replace(e.getMessage()));
				}
			}
			else{
				errMessage = Messages.getString("SettingTools.EssentialValueInvalid") + " Service = " + value;
			}
			
			if (!matched){
				MonitorInfo erroMonitor = MonitorConv.makeErrorMonitor(
						cloudServiceMonitor.getMonitor().getMonitorId(), errMessage);
				monitorInfoList.add(erroMonitor);
			}
			else{
				MonitorInfo monitorInfo = MonitorConv.createMonitorInfo(cloudServiceMonitor.getMonitor());
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
	
	private static CloudServiceInfo
		createCloudServiceInfo(com.clustercontrol.ws.monitor.PluginCheckInfo pluginCheckInfo) {
		CloudServiceInfo cloudServiceInfo = new CloudServiceInfo();
		cloudServiceInfo.setMonitorTypeId("");

		cloudServiceInfo.setMonitorId(pluginCheckInfo.getMonitorId());
		
		List<com.clustercontrol.ws.monitor.MonitorPluginStringInfo> monitorPluginStringInfo =
				pluginCheckInfo.getMonitorPluginStringInfoList();
		
		com.clustercontrol.utility.settings.monitor.xml.PluginStringValue pluginStringValueArray
			= new com.clustercontrol.utility.settings.monitor.xml.PluginStringValue();
		
		pluginStringValueArray.setKey(monitorPluginStringInfo.get(0).getKey());
		pluginStringValueArray.setMonitorId(monitorPluginStringInfo.get(0).getMonitorId());
		pluginStringValueArray.setValue(monitorPluginStringInfo.get(0).getValue());

		cloudServiceInfo.setPluginStringValue(pluginStringValueArray);;
		
		return cloudServiceInfo;
	}
	
	/**
	 * <BR>
	 *
	 * @return
	 */
	private static com.clustercontrol.ws.monitor.PluginCheckInfo createCloudServiceCheckInfo(CloudServiceInfo cloudServiceInfo) {
		com.clustercontrol.ws.monitor.PluginCheckInfo pluginCheckInfo = new com.clustercontrol.ws.monitor.PluginCheckInfo();
		pluginCheckInfo.setMonitorTypeId(cloudServiceInfo.getMonitorTypeId());
		pluginCheckInfo.setMonitorId(cloudServiceInfo.getMonitorId());
		
		PluginStringValue pluginStringValue = cloudServiceInfo.getPluginStringValue();
		com.clustercontrol.ws.monitor.MonitorPluginStringInfo info = null;
		info = new com.clustercontrol.ws.monitor.MonitorPluginStringInfo();
		info.setKey(pluginStringValue.getKey());
		info.setMonitorId(pluginStringValue.getMonitorId());
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