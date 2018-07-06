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

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.CustomInfo;
import com.clustercontrol.utility.settings.monitor.xml.CustomMonitor;
import com.clustercontrol.utility.settings.monitor.xml.CustomMonitors;
import com.clustercontrol.utility.settings.monitor.xml.NumericChangeAmount;
import com.clustercontrol.utility.settings.monitor.xml.NumericValue;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.settings.monitor.xml.StringValue;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.monitor.CommandExecType;
import com.clustercontrol.ws.monitor.CustomCheckInfo;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.ws.monitor.MonitorNumericValueInfo;
import com.clustercontrol.ws.monitor.MonitorStringValueInfo;

/**
 * カスタム 監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 * 
 * @version 6.1.0
 * @since 2.0.0
 * 
 */
public class CustomConv {
	private final static Log logger = LogFactory.getLog(CustomConv.class);
	
	static private String SCHEMA_TYPE = "I";
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
	 * DTO から、Castor で作成した形式の カスタム 監視設定情報へ変換する<BR>
	 * 
	 * @param 
	 * @return 
	 * @throws MonitorNotFound_Exception 
	 * @throws InvalidUserPass_Exception 
	 * @throws InvalidRole_Exception 
	 * @throws HinemosUnknown_Exception 
	 */
	public static CustomMonitors createCustomMonitors(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		CustomMonitors customMonitors = new CustomMonitors();

		for (MonitorInfo monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorSettingEndpointWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			CustomMonitor customMonitor = new CustomMonitor();
			customMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			for (MonitorNumericValueInfo numericValueInfo : monitorInfo.getNumericValueInfo()) {
				if(numericValueInfo.getPriority() == PriorityConstant.TYPE_INFO ||
						numericValueInfo.getPriority() == PriorityConstant.TYPE_WARNING){
					if(numericValueInfo.getMonitorNumericType().contains("CHANGE")) {
						customMonitor.addNumericChangeAmount(MonitorConv.createNumericChangeAmount(numericValueInfo));
					}
					else{
						customMonitor.addNumericValue(MonitorConv.createNumericValue(numericValueInfo));
					}
				}
			}
			// カスタム監視（文字列）を設定する。
			int orderNo = 0;
			for (MonitorStringValueInfo monitorStringValueInfo : monitorInfo.getStringValueInfo()) {
				customMonitor.addStringValue(MonitorConv.createStringValue(monitorStringValueInfo, ++orderNo));
			}
			customMonitor.setCustomInfo(createCustomInfo(monitorInfo.getCustomCheckInfo()));
			customMonitors.addCustomMonitor(customMonitor);
		}

		customMonitors.setCommon(MonitorConv.versionDto2Xml());
		customMonitors.setSchemaInfo(getSchemaVersion());

		return customMonitors;
	}

	public static List<MonitorInfo> createMonitorInfoList(CustomMonitors customMonitors) throws ConvertorException {
		List<MonitorInfo> monitorInfoList = new LinkedList<MonitorInfo>();
		
		for (CustomMonitor customMonitor : customMonitors.getCustomMonitor()) {
			logger.debug("Monitor Id : " + customMonitor.getMonitor().getMonitorId());

			MonitorInfo monitorInfo = MonitorConv.createMonitorInfo(customMonitor.getMonitor());

			if (monitorInfo.getMonitorType() == MonitorTypeConstant.TYPE_NUMERIC) {

				for (NumericValue numericValue : customMonitor.getNumericValue()) {
					if (numericValue.getPriority() == PriorityConstant.TYPE_INFO
							|| numericValue.getPriority() == PriorityConstant.TYPE_WARNING) {
						monitorInfo.getNumericValueInfo().add(MonitorConv.createMonitorNumericValueInfo(numericValue));
					}
				}
				if (monitorInfo.getNumericValueInfo().size() != 2) {
					throw new ConvertorException(customMonitor.getMonitor().getMonitorId() + " "
							+ Messages.getString("SettingTools.NumericValueInvalid"));
				}
				for (NumericChangeAmount changeValue : customMonitor.getNumericChangeAmount()){
					if(changeValue.getPriority() == PriorityConstant.TYPE_INFO ||
							changeValue.getPriority() == PriorityConstant.TYPE_WARNING){
						monitorInfo.getNumericValueInfo().add(MonitorConv.createMonitorNumericValueInfo(changeValue));
					}
				}		
				MonitorNumericValueInfo monitorNumericValueInfo = new MonitorNumericValueInfo();
				monitorNumericValueInfo.setMonitorId(monitorInfo.getMonitorId());
				monitorNumericValueInfo.setMonitorNumericType("");
				monitorNumericValueInfo.setPriority(PriorityConstant.TYPE_CRITICAL);
				monitorNumericValueInfo.setThresholdLowerLimit(0.0);
				monitorNumericValueInfo.setThresholdUpperLimit(0.0);
				monitorInfo.getNumericValueInfo().add(monitorNumericValueInfo);

				monitorNumericValueInfo = new MonitorNumericValueInfo();
				monitorNumericValueInfo.setMonitorId(monitorInfo.getMonitorId());
				monitorNumericValueInfo.setMonitorNumericType("");
				monitorNumericValueInfo.setPriority(PriorityConstant.TYPE_UNKNOWN);
				monitorNumericValueInfo.setThresholdLowerLimit(0.0);
				monitorNumericValueInfo.setThresholdUpperLimit(0.0);
				monitorInfo.getNumericValueInfo().add(monitorNumericValueInfo);

				// 変化量についても閾値判定と同様にTYPE_CRITICALとTYPE_UNKNOWNを定義する
				monitorNumericValueInfo = new MonitorNumericValueInfo();
				monitorNumericValueInfo.setMonitorId(monitorInfo.getMonitorId());
				monitorNumericValueInfo.setMonitorNumericType("CHANGE");
				monitorNumericValueInfo.setPriority(PriorityConstant.TYPE_CRITICAL);
				monitorNumericValueInfo.setThresholdLowerLimit(0.0);
				monitorNumericValueInfo.setThresholdUpperLimit(0.0);
				monitorInfo.getNumericValueInfo().add(monitorNumericValueInfo);
				
				monitorNumericValueInfo = new MonitorNumericValueInfo();
				monitorNumericValueInfo.setMonitorId(monitorInfo.getMonitorId());
				monitorNumericValueInfo.setMonitorNumericType("CHANGE");
				monitorNumericValueInfo.setPriority(PriorityConstant.TYPE_UNKNOWN);
				monitorNumericValueInfo.setThresholdLowerLimit(0.0);
				monitorNumericValueInfo.setThresholdUpperLimit(0.0);
				monitorInfo.getNumericValueInfo().add(monitorNumericValueInfo);
			}
			else{
				// カスタム監視（文字列）を設定する。
				StringValue[] values = customMonitor.getStringValue();
				MonitorConv.sort(values);
				for (StringValue stringValue : values) {
					monitorInfo.getStringValueInfo().add(MonitorConv.createMonitorStringValueInfo(stringValue));
				}
			}
			monitorInfo.setCustomCheckInfo(createCustomCheckInfo(customMonitor.getCustomInfo()));
			monitorInfoList.add(monitorInfo);
		}
		
		return monitorInfoList;
	}
	
	/**
	 * <BR>
	 * 
	 * @return 
	 */
	private static CustomInfo createCustomInfo(CustomCheckInfo customCheckInfo) {
		CustomInfo customInfo = new CustomInfo();
		customInfo.setMonitorTypeId("");
		customInfo.setMonitorId(customCheckInfo.getMonitorId());
		customInfo.setCommand(customCheckInfo.getCommand());
		customInfo.setSpecifyUser(customCheckInfo.isSpecifyUser());
		customInfo.setEffectiveUser(customCheckInfo.getEffectiveUser());
		customInfo.setExecuteType(customCheckInfo.getCommandExecType().ordinal());
		customInfo.setSelectedFacilityId(customCheckInfo.getSelectedFacilityId());
		customInfo.setTimeout(customCheckInfo.getTimeout());
		customInfo.setConvertFlg(customCheckInfo.getConvertFlg());
		
		return customInfo;
	}
	
	/**
	 * <BR>
	 * 
	 * @return 
	 */
	private static CustomCheckInfo createCustomCheckInfo(CustomInfo customInfo) {
		CustomCheckInfo customCheckInfo = new CustomCheckInfo();
		customCheckInfo.setMonitorTypeId("");
		customCheckInfo.setMonitorId(customInfo.getMonitorId());
		customCheckInfo.setCommand(customInfo.getCommand());
		customCheckInfo.setSpecifyUser(customInfo.getSpecifyUser());
		customCheckInfo.setEffectiveUser(customInfo.getEffectiveUser());

		CommandExecType returnType = CommandExecType.INDIVIDUAL;
		for (CommandExecType commandExecType : CommandExecType.values()) {
			if (customInfo.getExecuteType() == commandExecType.ordinal()) {
				returnType = commandExecType;
				break;
			}
		}
		customCheckInfo.setCommandExecType(returnType);

		customCheckInfo.setSelectedFacilityId(customInfo.getSelectedFacilityId());
		customCheckInfo.setTimeout(customInfo.getTimeout());
		customCheckInfo.setConvertFlg(customInfo.getConvertFlg());
		return customCheckInfo;
	}
}