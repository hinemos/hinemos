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
import org.openapitools.client.model.GetPlatformServiceForLoginUserResponse;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorNumericValueInfoResponse;
import org.openapitools.client.model.MonitorPluginStringInfoResponse;
import org.openapitools.client.model.PlatformServicesResponse;
import org.openapitools.client.model.PluginCheckInfoResponse;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.BillingMonitor;
import com.clustercontrol.utility.settings.monitor.xml.BillingMonitors;
import com.clustercontrol.utility.settings.monitor.xml.NumericChangeAmount;
import com.clustercontrol.utility.settings.monitor.xml.NumericValue;
import com.clustercontrol.utility.settings.monitor.xml.PluginStringValue;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.xcloud.plugin.monitor.CreateBillingDetailMonitorDialog.MonitorKind;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.plugin.monitor.PlatformServiceBillingDetailMonitorPlugin;
import com.clustercontrol.xcloud.plugin.monitor.PlatformServiceBillingMonitorPlugin;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;

/**
 * 課金監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 * @version 6.1.0
 * @since 6.0.0
 *
 *
 */
public class BillingConv {
	private final static Log logger = LogFactory.getLog(BillingConv.class);

	private final static String SCHEMA_TYPE = "I";
	private final static String SCHEMA_VERSION = "1";
	private final static String SCHEMA_REVISION = "2";

	private final static int PLUGIN_STRINGVALUE_INFO_SIZE = 2;
	
	private static final String KEY_PLATFORM = "platform";
	private static final String KEY_SERVICE = "service";
	private static final String KEY_FACILITY_TYPE = "FacilityType";
	private static final String KEY_MONITOR_KIND = "MonitorKind";
	
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

	/**
	 * <BR>
	 *
	 * @return
	 */
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
	 * Castor で作成した形式の リソース 監視設定情報を DTO へ変換する<BR>
	 *
	 * @param
	 * @return
	 * @throws ConvertorException
	 * @throws ParseException 
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 * @throws RestConnectFailed 
	 */
	public static List<MonitorInfoResponse> createMonitorInfoList(BillingMonitors billingMonitors)
			throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException, RestConnectFailed {
		List<MonitorInfoResponse> monitorInfoList =
				new LinkedList<MonitorInfoResponse>();
		
		CloudRestClientWrapper endpoint = CloudRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		for (BillingMonitor billingMonitor : billingMonitors.getBillingMonitor()) {
			logger.debug("Monitor Id : " + billingMonitor.getMonitor().getMonitorId());
			boolean matched = false;
			String errMessage = "";
			String value1 = "";
			String value2 = "";
			// PluginStringValue チェック
			if (billingMonitor.getMonitor().getMonitorTypeId().equals(PlatformServiceBillingMonitorPlugin.monitorPluginId)){
				value1 = getPluginStringValue(KEY_PLATFORM, billingMonitor.getBillingInfo().getPluginStringValue());
				value2 = getPluginStringValue(KEY_SERVICE, billingMonitor.getBillingInfo().getPluginStringValue());
				if(!value1.isEmpty() && !value2.isEmpty()){
					try {
						
						GetPlatformServiceForLoginUserResponse billingServices = endpoint.getPlatformServiceForLoginUser(
								billingMonitor.getMonitor().getFacilityId(),
								billingMonitor.getMonitor().getOwnerRoleId());
						
						for (PlatformServicesResponse service : billingServices.getPlatiformServices()) {
							if (value1.equals(service.getPlatformId()) && value2.equals(service.getServiceId())) {
								matched = true;
								break;
							}
						}
						if(!matched){
							errMessage = Messages.getString("SettingTools.EssentialValueInvalid") + " Target = " + value1 + " : " + value2;
						}
					} catch (HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass e) {
						errMessage = Messages.getString(HinemosMessage.replace(e.getMessage()));
					}
				}
				else{
					errMessage = Messages.getString("SettingTools.EssentialValueInvalid") + " Target = " + value1 + " : " + value2;
				}
			}else if (billingMonitor.getMonitor().getMonitorTypeId().equals(PlatformServiceBillingDetailMonitorPlugin.monitorPluginId)){
				value1 = getPluginStringValue(KEY_FACILITY_TYPE, billingMonitor.getBillingInfo().getPluginStringValue());
				value2 = getPluginStringValue(KEY_MONITOR_KIND, billingMonitor.getBillingInfo().getPluginStringValue());
				if(!value1.isEmpty() && !value2.isEmpty()){
					if(value1.equals(FacilityConstant.TYPE_SCOPE_STRING)|| value1.equals(FacilityConstant.TYPE_NODE_STRING))
						if(value2.equals(MonitorKind.sum.name())|| value2.equals(MonitorKind.delta.name()))
							matched = true;
				}
				if(!matched){
					errMessage = Messages.getString("SettingTools.EssentialValueInvalid") + " Target = " + value1 + " : " + value2;
				}
			}
			
			if (!matched){
				MonitorInfoResponse erroMonitor = MonitorConv.makeErrorMonitor(
						billingMonitor.getMonitor().getMonitorId(), errMessage);
				monitorInfoList.add(erroMonitor);
			}
			else{
				MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(billingMonitor.getMonitor());
	
				for (NumericValue numericValue : billingMonitor.getNumericValue()) {
					if(numericValue.getPriority() == PriorityConstant.TYPE_INFO ||
							numericValue.getPriority() == PriorityConstant.TYPE_WARNING){
						monitorInfo.getNumericValueInfo().add(MonitorConv.createMonitorNumericValueInfo(numericValue));
					}
				}
				if(monitorInfo.getNumericValueInfo().size() != 2){
					throw new ConvertorException(
							billingMonitor.getMonitor().getMonitorId()
							+ " " + Messages.getString("SettingTools.NumericValueInvalid"));
				}
				for (NumericChangeAmount changeValue : billingMonitor.getNumericChangeAmount()){
					if(changeValue.getPriority() == PriorityConstant.TYPE_INFO ||
							changeValue.getPriority() == PriorityConstant.TYPE_WARNING){
						monitorInfo.getNumericValueInfo().add(MonitorConv.createMonitorNumericValueInfo(changeValue));
					}
				}
				MonitorNumericValueInfoResponse monitorNumericValueInfo =
						new MonitorNumericValueInfoResponse();
				monitorNumericValueInfo.setMonitorNumericType(MonitorNumericValueInfoResponse.MonitorNumericTypeEnum.BASIC);
				monitorNumericValueInfo.setPriority(MonitorNumericValueInfoResponse.PriorityEnum.CRITICAL);
				monitorNumericValueInfo.setThresholdLowerLimit(0.0);
				monitorNumericValueInfo.setThresholdUpperLimit(0.0);
				monitorInfo.getNumericValueInfo().add(monitorNumericValueInfo);
	
				monitorNumericValueInfo = new MonitorNumericValueInfoResponse();
				monitorNumericValueInfo.setMonitorNumericType(MonitorNumericValueInfoResponse.MonitorNumericTypeEnum.BASIC);
				monitorNumericValueInfo.setPriority(MonitorNumericValueInfoResponse.PriorityEnum.UNKNOWN);
				monitorNumericValueInfo.setThresholdLowerLimit(0.0);
				monitorNumericValueInfo.setThresholdUpperLimit(0.0);
				monitorInfo.getNumericValueInfo().add(monitorNumericValueInfo);
	
				// 変化量監視が無効の場合、関連閾値が未入力なら、画面デフォルト値にて補完
				if( monitorInfo.getChangeFlg() ==false && billingMonitor.getNumericChangeAmount().length == 0 ){
					MonitorConv.setMonitorChangeAmountDefault(monitorInfo);
				}
				
				// 変化量についても閾値判定と同様にTYPE_CRITICALとTYPE_UNKNOWNを定義する
				monitorNumericValueInfo = new MonitorNumericValueInfoResponse();
				monitorNumericValueInfo.setMonitorNumericType(MonitorNumericValueInfoResponse.MonitorNumericTypeEnum.CHANGE);
				monitorNumericValueInfo.setPriority(MonitorNumericValueInfoResponse.PriorityEnum.CRITICAL);
				monitorNumericValueInfo.setThresholdLowerLimit(0.0);
				monitorNumericValueInfo.setThresholdUpperLimit(0.0);
				monitorInfo.getNumericValueInfo().add(monitorNumericValueInfo);
				
				monitorNumericValueInfo = new MonitorNumericValueInfoResponse();
				monitorNumericValueInfo.setMonitorNumericType(MonitorNumericValueInfoResponse.MonitorNumericTypeEnum.CHANGE);
				monitorNumericValueInfo.setPriority(MonitorNumericValueInfoResponse.PriorityEnum.UNKNOWN);
				monitorNumericValueInfo.setThresholdLowerLimit(0.0);
				monitorNumericValueInfo.setThresholdUpperLimit(0.0);
				monitorInfo.getNumericValueInfo().add(monitorNumericValueInfo);
				
				monitorInfo.setPluginCheckInfo
					(createBillingCheckInfo(billingMonitor.getBillingInfo()));
				monitorInfoList.add(monitorInfo);
			}
		}

		return monitorInfoList;
	}

	/**
	 * DTO から、Castor で作成した形式の リソース 監視設定情報へ変換する<BR>
	 *
	 * @param
	 * @return
	 * @throws MonitorNotFound
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws RestConnectFailed 
	 * @throws ParseException 
	 */
	public static com.clustercontrol.utility.settings.monitor.xml.BillingMonitors
		createBillingMonitors(List<MonitorInfoResponse> monitorInfoList)
			throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		
		com.clustercontrol.utility.settings.monitor.xml.BillingMonitors billingMonitors = new com.clustercontrol.utility.settings.monitor.xml.BillingMonitors();

		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorsettingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			BillingMonitor billingMonitor = new BillingMonitor();
			billingMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			for (MonitorNumericValueInfoResponse numericValueInfo : monitorInfo.getNumericValueInfo()) {
				if(numericValueInfo.getPriority() ==  MonitorNumericValueInfoResponse.PriorityEnum.INFO ||
						numericValueInfo.getPriority() == MonitorNumericValueInfoResponse.PriorityEnum.WARNING){
					if(numericValueInfo.getMonitorNumericType().equals(MonitorNumericValueInfoResponse.MonitorNumericTypeEnum.CHANGE)) {
						billingMonitor.addNumericChangeAmount(MonitorConv.createNumericChangeAmount(monitorInfo.getMonitorId(),numericValueInfo));
					}
					else{
						billingMonitor.addNumericValue(MonitorConv.createNumericValue(monitorInfo.getMonitorId(),numericValueInfo));
					}
				}
			}

			billingMonitor.setBillingInfo(createPluginCheckInfo(monitorInfo.getMonitorId(),monitorInfo.getPluginCheckInfo()));
			billingMonitors.addBillingMonitor(billingMonitor);
		}
		billingMonitors.setCommon(MonitorConv.versionDto2Xml());
		billingMonitors.setSchemaInfo(getSchemaVersion());

		return billingMonitors;
	}

	/**
	 * <BR>
	 * WS->XML
	 * @return
	 */
	
	private static com.clustercontrol.utility.settings.monitor.xml.BillingInfo
		createPluginCheckInfo(String monitorId, PluginCheckInfoResponse pluginCheckInfo) {

		com.clustercontrol.utility.settings.monitor.xml.BillingInfo billingInfo =
				new com.clustercontrol.utility.settings.monitor.xml.BillingInfo();
		billingInfo.setMonitorTypeId("");
		billingInfo.setMonitorId(monitorId);

		List<MonitorPluginStringInfoResponse> monitorPluginStringInfo =
				pluginCheckInfo.getMonitorPluginStringInfoList();
		
		com.clustercontrol.utility.settings.monitor.xml.PluginStringValue[] pluginStringValueArray
			= new com.clustercontrol.utility.settings.monitor.xml.PluginStringValue[PLUGIN_STRINGVALUE_INFO_SIZE];
		for (int i=0;i<PLUGIN_STRINGVALUE_INFO_SIZE ;i++){
			pluginStringValueArray[i] = new PluginStringValue();
			pluginStringValueArray[i].setKey(monitorPluginStringInfo.get(i).getKey());
			pluginStringValueArray[i].setMonitorId(monitorId);
			pluginStringValueArray[i].setValue(monitorPluginStringInfo.get(i).getValue());
		}
		billingInfo.setPluginStringValue(pluginStringValueArray);
		return billingInfo;
	}
	
	/**
	 * <BR>
	 *xml->WS
	 * @return
	 */

	private static PluginCheckInfoResponse
			createBillingCheckInfo(com.clustercontrol.utility.settings.monitor.xml.BillingInfo billingInfo) {
		PluginCheckInfoResponse pluginCheckInfo = new PluginCheckInfoResponse();
		
		PluginStringValue[] pluginStringValue = billingInfo.getPluginStringValue();
		MonitorPluginStringInfoResponse info = null;
		for (int i = 0;i< PLUGIN_STRINGVALUE_INFO_SIZE; i++){
			info = new MonitorPluginStringInfoResponse();
			info.setKey(pluginStringValue[i].getKey());
			info.setValue(pluginStringValue[i].getValue());
			pluginCheckInfo.getMonitorPluginStringInfoList().add(info);
		}
		return pluginCheckInfo;
	}
	
	private static String getPluginStringValue(String key, PluginStringValue[] pluginStringValues) {
		if(pluginStringValues == null)
			return "";
		
		for(PluginStringValue pluginStringValue: pluginStringValues){
			if (pluginStringValue.getKey().equals(key))
				return pluginStringValue.getValue();
		}
		return "";
	}
}