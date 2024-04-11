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
import org.openapitools.client.model.CustomTrapCheckInfoResponse;
import org.openapitools.client.model.CustomTrapCheckInfoResponse.ConvertFlgEnum;
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
import com.clustercontrol.utility.settings.monitor.xml.CustomTrapInfo;
import com.clustercontrol.utility.settings.monitor.xml.CustomTrapMonitor;
import com.clustercontrol.utility.settings.monitor.xml.CustomTrapMonitors;
import com.clustercontrol.utility.settings.monitor.xml.NumericChangeAmount;
import com.clustercontrol.utility.settings.monitor.xml.NumericValue;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.settings.monitor.xml.StringValue;
import com.clustercontrol.utility.util.OpenApiEnumConverter;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.version.util.VersionUtil;

/**
 * カスタムトラップ監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 * 
 * @version 6.1.0
 * @since 6.0.0
 * 
 */
public class CustomTrapConv {
	private final static Log logger = LogFactory.getLog(CustomTrapConv.class);
	
	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	static private String SCHEMA_TYPE = VersionUtil.getSchemaProperty("MONITOR.CUSTOMTRAP.SCHEMATYPE");
	static private String SCHEMA_VERSION = VersionUtil.getSchemaProperty("MONITOR.CUSTOMTRAP.SCHEMAVERSION");
	static private String SCHEMA_REVISION =VersionUtil.getSchemaProperty("MONITOR.CUSTOMTRAP.SCHEMAREVISION");
	
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
	 * @throws ParseException 
	 * @throws RestConnectFailed 
	 * @throws MonitorNotFound_Exception 
	 * @throws InvalidUserPass_Exception 
	 * @throws InvalidRole_Exception 
	 * @throws HinemosUnknown_Exception 
	 */
	public static CustomTrapMonitors createCustomTrapMonitors(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, ParseException, RestConnectFailed {
		CustomTrapMonitors customTrapMonitors = new CustomTrapMonitors();
		
		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo =  MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMonitor(monitorInfo.getMonitorId());

			CustomTrapMonitor customTrapMonitor = new CustomTrapMonitor();
			customTrapMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));
			
			// カスタムトラップ監視（数値）を設定する。
			for (MonitorNumericValueInfoResponse numericValueInfo : monitorInfo.getNumericValueInfo()) {
				if(numericValueInfo.getPriority() == PriorityEnum.INFO ||
						numericValueInfo.getPriority() == PriorityEnum.WARNING){
					if(numericValueInfo.getMonitorNumericType().name().equals("CHANGE")) {
						customTrapMonitor.addNumericChangeAmount(MonitorConv.createNumericChangeAmount(monitorInfo.getMonitorId(),numericValueInfo));
					}
					else{
						customTrapMonitor.addNumericValue(MonitorConv.createNumericValue(monitorInfo.getMonitorId(),numericValueInfo));
					}
				}
			}
			// カスタムトラップ監視（文字列）を設定する。
			int orderNo = 0;
			for (MonitorStringValueInfoResponse monitorStringValueInfo : monitorInfo.getStringValueInfo()) {
				customTrapMonitor.addStringValue(MonitorConv.createStringValue(monitorInfo.getMonitorId(),monitorStringValueInfo, ++orderNo));
			}
			
			customTrapMonitor.setCustomTrapInfo(createCustomTrapInfo(monitorInfo));
			customTrapMonitors.addCustomTrapMonitor(customTrapMonitor);
		}

		customTrapMonitors.setCommon(MonitorConv.versionDto2Xml());
		customTrapMonitors.setSchemaInfo(getSchemaVersion());
		
		return customTrapMonitors;
	}
	
	public static List<MonitorInfoResponse> createMonitorInfoList(CustomTrapMonitors customTrapMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		List<MonitorInfoResponse> monitorInfoList = new LinkedList<MonitorInfoResponse>();
		
		for (CustomTrapMonitor customTrapMonitor : customTrapMonitors.getCustomTrapMonitor()) {
			logger.debug("Monitor Id : " + customTrapMonitor.getMonitor().getMonitorId());

			MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(customTrapMonitor.getMonitor());

			if (monitorInfo.getMonitorType() == MonitorTypeEnum.NUMERIC) {
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
				if( monitorInfo.getChangeFlg() ==false && customTrapMonitor.getNumericChangeAmount().length == 0 ){
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
	private static CustomTrapInfo createCustomTrapInfo(MonitorInfoResponse monitorInfo) {
		CustomTrapInfo customTrapInfo = new CustomTrapInfo();
		customTrapInfo.setMonitorTypeId("");
		customTrapInfo.setMonitorId(monitorInfo.getMonitorId());
		int convertFlgEnumInt = OpenApiEnumConverter.enumToInteger(monitorInfo.getCustomTrapCheckInfo().getConvertFlg());
		customTrapInfo.setConvertFlg(convertFlgEnumInt);
		customTrapInfo.setTargetKey(monitorInfo.getCustomTrapCheckInfo().getTargetKey());
		
		return customTrapInfo;
	}
	
	/**
	 * <BR>
	 * 
	 * @return 
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	private static CustomTrapCheckInfoResponse createCustomTrapCheckInfo(CustomTrapInfo customTrapInfo) throws InvalidSetting, HinemosUnknown {
		CustomTrapCheckInfoResponse customTrapCheckInfo = new CustomTrapCheckInfoResponse();
		customTrapCheckInfo.setTargetKey(customTrapInfo.getTargetKey());
		ConvertFlgEnum convertFlgEnum = OpenApiEnumConverter.integerToEnum(customTrapInfo.getConvertFlg(), ConvertFlgEnum.class);
		customTrapCheckInfo.setConvertFlg(convertFlgEnum);
		
		return customTrapCheckInfo;
	}
}