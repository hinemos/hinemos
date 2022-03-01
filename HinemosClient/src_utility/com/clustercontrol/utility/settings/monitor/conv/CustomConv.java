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
import org.openapitools.client.model.CustomCheckInfoResponse;
import org.openapitools.client.model.CustomCheckInfoResponse.CommandExecTypeCodeEnum;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorInfoResponse.MonitorTypeEnum;
import org.openapitools.client.model.MonitorNumericValueInfoResponse;
import org.openapitools.client.model.MonitorNumericValueInfoResponse.MonitorNumericTypeEnum;
import org.openapitools.client.model.MonitorNumericValueInfoResponse.PriorityEnum;
import org.openapitools.client.model.MonitorStringValueInfoResponse;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
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
import com.clustercontrol.utility.util.OpenApiEnumConverter;
import com.clustercontrol.utility.util.UtilityManagerUtil;

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
	static private String SCHEMA_REVISION = "2";
	
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
	 * @throws RestConnectFailed 
	 * @throws ParseException 
	 * @throws MonitorNotFound_Exception 
	 * @throws InvalidUserPass_Exception 
	 * @throws InvalidRole_Exception 
	 * @throws HinemosUnknown_Exception 
	 */
	public static CustomMonitors createCustomMonitors(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		CustomMonitors customMonitors = new CustomMonitors();

		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorsettingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			CustomMonitor customMonitor = new CustomMonitor();
			customMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			for (MonitorNumericValueInfoResponse numericValueInfo : monitorInfo.getNumericValueInfo()) {
				if(numericValueInfo.getPriority() == PriorityEnum.INFO ||
						numericValueInfo.getPriority() == PriorityEnum.WARNING){
					if(numericValueInfo.getMonitorNumericType().name().equals("CHANGE")) {
						customMonitor.addNumericChangeAmount(MonitorConv.createNumericChangeAmount(monitorInfo.getMonitorId(),numericValueInfo));
					}
					else{
						customMonitor.addNumericValue(MonitorConv.createNumericValue(monitorInfo.getMonitorId(),numericValueInfo));
					}
				}
			}
			// カスタム監視（文字列）を設定する。
			int orderNo = 0;
			for (MonitorStringValueInfoResponse monitorStringValueInfo : monitorInfo.getStringValueInfo()) {
				customMonitor.addStringValue(MonitorConv.createStringValue(monitorInfo.getMonitorId(),monitorStringValueInfo, ++orderNo));
			}
			customMonitor.setCustomInfo(createCustomInfo(monitorInfo));
			customMonitors.addCustomMonitor(customMonitor);
		}

		customMonitors.setCommon(MonitorConv.versionDto2Xml());
		customMonitors.setSchemaInfo(getSchemaVersion());

		return customMonitors;
	}

	public static List<MonitorInfoResponse> createMonitorInfoList(CustomMonitors customMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		List<MonitorInfoResponse> monitorInfoList = new LinkedList<MonitorInfoResponse>();
		
		for (CustomMonitor customMonitor : customMonitors.getCustomMonitor()) {
			logger.debug("Monitor Id : " + customMonitor.getMonitor().getMonitorId());

			MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(customMonitor.getMonitor());

			if (monitorInfo.getMonitorType() == MonitorTypeEnum.NUMERIC) {

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
				MonitorNumericValueInfoResponse monitorNumericValueInfo = new MonitorNumericValueInfoResponse();
				monitorNumericValueInfo.setMonitorNumericType(null);
				monitorNumericValueInfo.setPriority(PriorityEnum.CRITICAL);
				monitorNumericValueInfo.setThresholdLowerLimit(0.0);
				monitorNumericValueInfo.setThresholdUpperLimit(0.0);
				monitorInfo.getNumericValueInfo().add(monitorNumericValueInfo);

				monitorNumericValueInfo = new MonitorNumericValueInfoResponse();
				monitorNumericValueInfo.setMonitorNumericType(null);
				monitorNumericValueInfo.setPriority(PriorityEnum.UNKNOWN);
				monitorNumericValueInfo.setThresholdLowerLimit(0.0);
				monitorNumericValueInfo.setThresholdUpperLimit(0.0);
				monitorInfo.getNumericValueInfo().add(monitorNumericValueInfo);

				// 変化量監視が無効の場合、関連閾値が未入力なら、画面デフォルト値にて補完
				if( monitorInfo.getChangeFlg() ==false && customMonitor.getNumericChangeAmount().length == 0 ){
					MonitorConv.setMonitorChangeAmountDefault(monitorInfo);
				}
				
				// 変化量についても閾値判定と同様にTYPE_CRITICALとTYPE_UNKNOWNを定義する
				monitorNumericValueInfo = new MonitorNumericValueInfoResponse();
				monitorNumericValueInfo.setMonitorNumericType(MonitorNumericTypeEnum.CHANGE);
				monitorNumericValueInfo.setPriority(PriorityEnum.CRITICAL);
				monitorNumericValueInfo.setThresholdLowerLimit(0.0);
				monitorNumericValueInfo.setThresholdUpperLimit(0.0);
				monitorInfo.getNumericValueInfo().add(monitorNumericValueInfo);
				
				monitorNumericValueInfo = new MonitorNumericValueInfoResponse();
				monitorNumericValueInfo.setMonitorNumericType(MonitorNumericTypeEnum.CHANGE);
				monitorNumericValueInfo.setPriority(PriorityEnum.UNKNOWN);
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
	private static CustomInfo createCustomInfo(MonitorInfoResponse monitorInfo) {
		CustomInfo customInfo = new CustomInfo();
		customInfo.setMonitorTypeId("");
		customInfo.setMonitorId(monitorInfo.getMonitorId());
		customInfo.setCommand(monitorInfo.getCustomCheckInfo().getCommand());
		customInfo.setSpecifyUser(monitorInfo.getCustomCheckInfo().getSpecifyUser());
		customInfo.setEffectiveUser(monitorInfo.getCustomCheckInfo().getEffectiveUser());
		customInfo.setExecuteType(monitorInfo.getCustomCheckInfo().getCommandExecTypeCode().ordinal());
		customInfo.setSelectedFacilityId(monitorInfo.getCustomCheckInfo().getSelectedFacilityId());
		customInfo.setTimeout(monitorInfo.getCustomCheckInfo().getTimeout());
		int convertFlgEnumInt = OpenApiEnumConverter.enumToInteger(monitorInfo.getCustomCheckInfo().getConvertFlg());
		customInfo.setConvertFlg(convertFlgEnumInt);
		
		return customInfo;
	}
	
	/**
	 * <BR>
	 * 
	 * @return 
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	private static CustomCheckInfoResponse createCustomCheckInfo(CustomInfo customInfo) throws InvalidSetting, HinemosUnknown {
		CustomCheckInfoResponse customCheckInfo = new CustomCheckInfoResponse();

		customCheckInfo.setCommand(customInfo.getCommand());
		customCheckInfo.setSpecifyUser(customInfo.getSpecifyUser());
		customCheckInfo.setEffectiveUser(customInfo.getEffectiveUser());

		CommandExecTypeCodeEnum returnType = CommandExecTypeCodeEnum.INDIVIDUAL;
		for (CommandExecTypeCodeEnum commandExecType : CommandExecTypeCodeEnum.values()) {
			if (customInfo.getExecuteType() == commandExecType.ordinal()) {
				returnType = commandExecType;
				break;
			}
		}
		customCheckInfo.setCommandExecTypeCode(returnType);

		customCheckInfo.setSelectedFacilityId(customInfo.getSelectedFacilityId());
		customCheckInfo.setTimeout(customInfo.getTimeout());
		CustomCheckInfoResponse.ConvertFlgEnum convertFlgEnum = OpenApiEnumConverter.integerToEnum(customInfo.getConvertFlg(), CustomCheckInfoResponse.ConvertFlgEnum.class);
		customCheckInfo.setConvertFlg(convertFlgEnum);
		return customCheckInfo;
	}
}