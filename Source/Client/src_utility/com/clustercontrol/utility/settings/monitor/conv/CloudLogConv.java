/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
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
import org.openapitools.client.model.MonitorStringValueInfoResponse;
import org.openapitools.client.model.PluginCheckInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.CloudLogInfo;
import com.clustercontrol.utility.settings.monitor.xml.CloudLogMonitor;
import com.clustercontrol.utility.settings.monitor.xml.CloudLogMonitors;
import com.clustercontrol.utility.settings.monitor.xml.PluginStringValue;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.settings.monitor.xml.StringValue;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * クラウドログ 監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 */
public class CloudLogConv {
	private final static Log logger = LogFactory.getLog(CloudLogConv.class);

	static private String SCHEMA_TYPE = "K";
	static private String SCHEMA_VERSION = "1";
	static private String SCHEMA_REVISION = "1";
	
	
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
	
	public static CloudLogMonitors
		createCloudLogMonitors(List<MonitorInfoResponse> monitorInfoList)
				throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		CloudLogMonitors cloudLogMonitors = new CloudLogMonitors();

		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorsettingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			CloudLogMonitor cloudLogMonitor = new CloudLogMonitor();
			cloudLogMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			int orderNo = 0;
			for (MonitorStringValueInfoResponse monitorStringValueInfo : monitorInfo.getStringValueInfo()) {
				cloudLogMonitor.addStringValue(MonitorConv.createStringValue(monitorInfo.getMonitorId(),monitorStringValueInfo, ++orderNo));
			}

			cloudLogMonitor.setCloudLogInfo(createCloudLogInfo(monitorInfo));
			cloudLogMonitors.addCloudLogMonitor(cloudLogMonitor);
		}

		cloudLogMonitors.setCommon(MonitorConv.versionDto2Xml());
		cloudLogMonitors.setSchemaInfo(getSchemaVersion());

		return cloudLogMonitors;
	}
	

	public static List<MonitorInfoResponse> createMonitorInfoList(CloudLogMonitors cloudLogMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException, RestConnectFailed{
		List<MonitorInfoResponse> monitorInfoList = new LinkedList<MonitorInfoResponse>();

		for (CloudLogMonitor cloudLogMonitor : cloudLogMonitors.getCloudLogMonitor()) {
			logger.debug("Monitor Id : " + cloudLogMonitor.getMonitor().getMonitorId());
			MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(cloudLogMonitor.getMonitor());

			StringValue[] values = cloudLogMonitor.getStringValue();
			MonitorConv.sort(values);
			for (StringValue stringValue : values) {
				monitorInfo.getStringValueInfo().add(MonitorConv.createMonitorStringValueInfo(stringValue));
			}

			monitorInfo.setPluginCheckInfo(createCloudLogCheckInfo(cloudLogMonitor.getCloudLogInfo()));
			monitorInfoList.add(monitorInfo);
		}

		return monitorInfoList;
	}
	
	/**
	 * <BR>
	 * WS->XML
	 * @return
	 */
	private static CloudLogInfo createCloudLogInfo(MonitorInfoResponse monitorInfo) {
		CloudLogInfo cloudLogInfo = new CloudLogInfo();

		cloudLogInfo.setMonitorId(monitorInfo.getMonitorId());
		cloudLogInfo.setMonitorTypeId(monitorInfo.getMonitorTypeId());
		
		List<MonitorPluginStringInfoResponse> monitorPluginStringInfo =
				monitorInfo.getPluginCheckInfo().getMonitorPluginStringInfoList();
		
		for(MonitorPluginStringInfoResponse stringInfo : monitorPluginStringInfo){
			com.clustercontrol.utility.settings.monitor.xml.PluginStringValue pluginStringValue
			= new com.clustercontrol.utility.settings.monitor.xml.PluginStringValue();
		
			pluginStringValue.setKey(stringInfo.getKey());
			pluginStringValue.setMonitorId(monitorInfo.getMonitorId());
			pluginStringValue.setValue(stringInfo.getValue());
			
			// valueがnullの場合、マーシャル時にエラーとなる為、空白をセットする。
			if(stringInfo.getValue() == null){
				pluginStringValue.setValue("");
			}
			
			cloudLogInfo.addPluginStringValue(pluginStringValue);;
		}
		
		return cloudLogInfo;
	}
	
	/**
	 * <BR>
	 *
	 * @return
	 */
	private static PluginCheckInfoResponse createCloudLogCheckInfo(CloudLogInfo cloudLogInfo) {
		PluginCheckInfoResponse pluginCheckInfo = new PluginCheckInfoResponse();
		for(PluginStringValue pluginStringValue : cloudLogInfo.getPluginStringValue()){
			MonitorPluginStringInfoResponse info = new MonitorPluginStringInfoResponse();
			info.setKey(pluginStringValue.getKey());
			info.setValue(pluginStringValue.getValue());
			pluginCheckInfo.getMonitorPluginStringInfoList().add(info);
		}
		
		return pluginCheckInfo;
	}
	
}