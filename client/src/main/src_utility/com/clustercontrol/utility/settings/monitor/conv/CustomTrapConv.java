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
import com.clustercontrol.utility.settings.monitor.xml.CustomTrapInfo;
import com.clustercontrol.utility.settings.monitor.xml.CustomTrapMonitor;
import com.clustercontrol.utility.settings.monitor.xml.CustomTrapMonitors;
import com.clustercontrol.utility.settings.monitor.xml.NumericChangeAmount;
import com.clustercontrol.utility.settings.monitor.xml.NumericValue;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.settings.monitor.xml.StringValue;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.monitor.CustomTrapCheckInfo;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.ws.monitor.MonitorNumericValueInfo;
import com.clustercontrol.ws.monitor.MonitorStringValueInfo;

/**
 * カスタムトラップ監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 * 
 * @version 6.1.0
 * @since 6.0.0
 * 
 */
public class CustomTrapConv {
	private final static Log logger = LogFactory.getLog(CustomTrapConv.class);
	
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
	public static CustomTrapMonitors createCustomTrapMonitors(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		CustomTrapMonitors customTrapMonitors = new CustomTrapMonitors();
		
		for (MonitorInfo monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo =  MonitorSettingEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMonitor(monitorInfo.getMonitorId());

			CustomTrapMonitor customTrapMonitor = new CustomTrapMonitor();
			customTrapMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));
			
			// カスタムトラップ監視（数値）を設定する。
			for (MonitorNumericValueInfo numericValueInfo : monitorInfo.getNumericValueInfo()) {
				if(numericValueInfo.getPriority() == PriorityConstant.TYPE_INFO ||
						numericValueInfo.getPriority() == PriorityConstant.TYPE_WARNING){
					if(numericValueInfo.getMonitorNumericType().contains("CHANGE")) {
						customTrapMonitor.addNumericChangeAmount(MonitorConv.createNumericChangeAmount(numericValueInfo));
					}
					else{
						customTrapMonitor.addNumericValue(MonitorConv.createNumericValue(numericValueInfo));
					}
				}
			}
			// カスタムトラップ監視（文字列）を設定する。
			int orderNo = 0;
			for (MonitorStringValueInfo monitorStringValueInfo : monitorInfo.getStringValueInfo()     ) {
				customTrapMonitor.addStringValue(MonitorConv.createStringValue(monitorStringValueInfo, ++orderNo));
			}
			
			customTrapMonitor.setCustomTrapInfo(createCustomTrapInfo(monitorInfo.getCustomTrapCheckInfo()));
			customTrapMonitors.addCustomTrapMonitor(customTrapMonitor);
		}

		customTrapMonitors.setCommon(MonitorConv.versionDto2Xml());
		customTrapMonitors.setSchemaInfo(getSchemaVersion());
		
		return customTrapMonitors;
	}
	
	public static List<MonitorInfo> createMonitorInfoList(CustomTrapMonitors customTrapMonitors) throws ConvertorException {
		List<MonitorInfo> monitorInfoList = new LinkedList<MonitorInfo>();
		
		for (CustomTrapMonitor customTrapMonitor : customTrapMonitors.getCustomTrapMonitor()) {
			logger.debug("Monitor Id : " + customTrapMonitor.getMonitor().getMonitorId());

			MonitorInfo monitorInfo = MonitorConv.createMonitorInfo(customTrapMonitor.getMonitor());

			if (monitorInfo.getMonitorType() == MonitorTypeConstant.TYPE_NUMERIC) {
				for (NumericValue numericValue : customTrapMonitor.getNumericValue()) {
					if (numericValue.getPriority() == PriorityConstant.TYPE_INFO
							|| numericValue.getPriority() == PriorityConstant.TYPE_WARNING) {
						monitorInfo.getNumericValueInfo().add(MonitorConv.createMonitorNumericValueInfo(numericValue));
					}
				}
				if (monitorInfo.getNumericValueInfo().size() != 2) {
					throw new ConvertorException(customTrapMonitor.getMonitor().getMonitorId() + " "
							+ Messages.getString("SettingTools.NumericValueInvalid"));
				}
				
				for (NumericChangeAmount changeValue : customTrapMonitor.getNumericChangeAmount()){
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
				StringValue[] values = customTrapMonitor.getStringValue();
				MonitorConv.sort(values);
				for (StringValue stringValue : values) {
					monitorInfo.getStringValueInfo().add(MonitorConv.createMonitorStringValueInfo(stringValue));
				}
			}

			monitorInfo.setCustomTrapCheckInfo(createCustomTrapCheckInfo(customTrapMonitor.getCustomTrapInfo()));
			monitorInfoList.add(monitorInfo);
		}
		
		return monitorInfoList;
	}
	
	/**
	 * <BR>
	 * 
	 * @return 
	 */
	private static CustomTrapInfo createCustomTrapInfo(CustomTrapCheckInfo customTrapCheckInfo) {
		CustomTrapInfo customTrapInfo = new CustomTrapInfo();
		customTrapInfo.setMonitorTypeId("");
		customTrapInfo.setMonitorId(customTrapCheckInfo.getMonitorId());
		customTrapInfo.setConvertFlg(customTrapCheckInfo.getConvertFlg());
		customTrapInfo.setTargetKey(customTrapCheckInfo.getTargetKey());
		
		return customTrapInfo;
	}
	
	/**
	 * <BR>
	 * 
	 * @return 
	 */
	private static CustomTrapCheckInfo createCustomTrapCheckInfo(CustomTrapInfo customTrapInfo) {
		CustomTrapCheckInfo customTrapCheckInfo = new CustomTrapCheckInfo();
		customTrapCheckInfo.setMonitorId(customTrapInfo.getMonitorId());
		customTrapCheckInfo.setTargetKey(customTrapInfo.getTargetKey());
		customTrapCheckInfo.setConvertFlg(customTrapInfo.getConvertFlg());
		
		return customTrapCheckInfo;
	}
}