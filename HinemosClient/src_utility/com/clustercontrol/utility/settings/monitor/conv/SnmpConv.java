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
import org.openapitools.client.model.MonitorInfoResponse.MonitorTypeEnum;
import org.openapitools.client.model.MonitorNumericValueInfoResponse;
import org.openapitools.client.model.MonitorNumericValueInfoResponse.PriorityEnum;
import org.openapitools.client.model.MonitorStringValueInfoResponse;
import org.openapitools.client.model.SnmpCheckInfoResponse;
import org.openapitools.client.model.SnmpCheckInfoResponse.ConvertFlgEnum;

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
import com.clustercontrol.utility.settings.monitor.xml.NumericChangeAmount;
import com.clustercontrol.utility.settings.monitor.xml.NumericValue;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.settings.monitor.xml.SnmpInfo;
import com.clustercontrol.utility.settings.monitor.xml.SnmpMonitor;
import com.clustercontrol.utility.settings.monitor.xml.SnmpMonitors;
import com.clustercontrol.utility.settings.monitor.xml.StringValue;
import com.clustercontrol.utility.util.OpenApiEnumConverter;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.version.util.VersionUtil;

/**
 * SNMP 監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 * 
 * @version 6.1.0
 * @since 1.0.0
 * 
 * 
 */
public class SnmpConv {
	private final static Log logger = LogFactory.getLog(SnmpConv.class);

	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	private final static String SCHEMA_TYPE = VersionUtil.getSchemaProperty("MONITOR.SNMP.SCHEMATYPE");
	private final static String SCHEMA_VERSION = VersionUtil.getSchemaProperty("MONITOR.SNMP.SCHEMAVERSION");
	private final static String SCHEMA_REVISION =VersionUtil.getSchemaProperty("MONITOR.SNMP.SCHEMAREVISION");
	
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
	 */
	public static List<MonitorInfoResponse> createMonitorInfoList(SnmpMonitors snmpMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		List<MonitorInfoResponse> monitorInfoList = new LinkedList<MonitorInfoResponse>();
		
		for (SnmpMonitor snmpMonitor : snmpMonitors.getSnmpMonitor()) {
			logger.debug("Monitor Id : " + snmpMonitor.getMonitor().getMonitorId());

			MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(snmpMonitor.getMonitor());

			if(monitorInfo.getMonitorType() == MonitorTypeEnum.NUMERIC){
				for (NumericValue numericValue : snmpMonitor.getNumericValue()) {
					if(numericValue.getPriority() == PriorityConstant.TYPE_INFO ||
							numericValue.getPriority() == PriorityConstant.TYPE_WARNING){
						monitorInfo.getNumericValueInfo().add(MonitorConv.createMonitorNumericValueInfo(numericValue));
					}
				}
				if(monitorInfo.getNumericValueInfo().size() != 2){
					throw new ConvertorException(
							snmpMonitor.getMonitor().getMonitorId()
							+ " " + Messages.getString("SettingTools.NumericValueInvalid"));
				}

				for (NumericChangeAmount changeValue : snmpMonitor.getNumericChangeAmount()){
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
				if( monitorInfo.getChangeFlg() ==false && snmpMonitor.getNumericChangeAmount().length == 0 ){
					MonitorConv.setMonitorChangeAmountDefault(monitorInfo);
				}
				
				// 変化量についても閾値判定と同様にTYPE_CRITICALとTYPE_UNKNOWNを定義する
				monitorNumericValueInfo = new MonitorNumericValueInfoResponse();
				monitorNumericValueInfo.setMonitorNumericType(MonitorNumericValueInfoResponse.MonitorNumericTypeEnum.CHANGE);
				monitorNumericValueInfo.setPriority(PriorityEnum.CRITICAL);
				monitorNumericValueInfo.setThresholdLowerLimit(0.0);
				monitorNumericValueInfo.setThresholdUpperLimit(0.0);
				monitorInfo.getNumericValueInfo().add(monitorNumericValueInfo);

				monitorNumericValueInfo = new MonitorNumericValueInfoResponse();
				monitorNumericValueInfo.setMonitorNumericType(MonitorNumericValueInfoResponse.MonitorNumericTypeEnum.CHANGE);
				monitorNumericValueInfo.setPriority(PriorityEnum.UNKNOWN);
				monitorNumericValueInfo.setThresholdLowerLimit(0.0);
				monitorNumericValueInfo.setThresholdUpperLimit(0.0);
				monitorInfo.getNumericValueInfo().add(monitorNumericValueInfo);
			}
			else{
				StringValue[] values = snmpMonitor.getStringValue();
				MonitorConv.sort(values);
				for (StringValue stringValue : values) {
					monitorInfo.getStringValueInfo().add(MonitorConv.createMonitorStringValueInfo(stringValue));
				}
			}

			monitorInfo.setSnmpCheckInfo(createSnmpCheckInfo(snmpMonitor.getSnmpInfo()));
			monitorInfoList.add(monitorInfo);
		}
		
		return monitorInfoList;
	}

	/**
	 * DTO から、Castor で作成した形式の リソース 監視設定情報へ変換する<BR>
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
	public static SnmpMonitors createSnmpMonitors(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		SnmpMonitors snmpMonitors = new SnmpMonitors();
		
		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo =  MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMonitor(monitorInfo.getMonitorId());

			SnmpMonitor snmpMonitor = new SnmpMonitor();
			snmpMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));
			
			for (MonitorNumericValueInfoResponse numericValueInfo : monitorInfo.getNumericValueInfo()) {
				if(numericValueInfo.getPriority() == PriorityEnum.INFO ||
						numericValueInfo.getPriority() == PriorityEnum.WARNING){
					if(numericValueInfo.getMonitorNumericType().name().equals("CHANGE")) {
						snmpMonitor.addNumericChangeAmount(MonitorConv.createNumericChangeAmount(monitorInfo.getMonitorId(),numericValueInfo));
					}
					else{
						snmpMonitor.addNumericValue(MonitorConv.createNumericValue(monitorInfo.getMonitorId(),numericValueInfo));
					}
				}
			}
			
			int orderNo = 0;
			for (MonitorStringValueInfoResponse monitorStringValueInfo : monitorInfo.getStringValueInfo()) {
				snmpMonitor.addStringValue(MonitorConv.createStringValue(monitorInfo.getMonitorId(),monitorStringValueInfo, ++orderNo));
			}

			snmpMonitor.setSnmpInfo(createSnmpInfo(monitorInfo));
			snmpMonitors.addSnmpMonitor(snmpMonitor);
		}
		
		snmpMonitors.setCommon(MonitorConv.versionDto2Xml());
		snmpMonitors.setSchemaInfo(getSchemaVersion());
		
		return snmpMonitors;
	}
	
	/**
	 * <BR>
	 * 
	 * @return
	 */
	private static SnmpInfo createSnmpInfo(MonitorInfoResponse monitorInfo) {
		SnmpInfo snmpInfo = new SnmpInfo();
		// DBからMonitorTypeIdがなくなっているのでこのままだとnullが設定されてしまう。
		// nullが設定されるとエクスポート時にValidedにひっかかる為、空文字を設定しておく。
		snmpInfo.setMonitorTypeId("");
		snmpInfo.setMonitorId(monitorInfo.getMonitorId());
		int convertFlgInt = OpenApiEnumConverter.enumToInteger(monitorInfo.getSnmpCheckInfo().getConvertFlg());
		snmpInfo.setConvertFlg(convertFlgInt);
		snmpInfo.setSnmpOid(monitorInfo.getSnmpCheckInfo().getSnmpOid());
		
		return snmpInfo;
	}
	
	/**
	 * <BR>
	 * 
	 * @return
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	private static SnmpCheckInfoResponse createSnmpCheckInfo(SnmpInfo snmpInfo) throws InvalidSetting, HinemosUnknown {
		SnmpCheckInfoResponse snmpCheckInfo = new SnmpCheckInfoResponse();
		ConvertFlgEnum convertFlgEnum =  OpenApiEnumConverter.integerToEnum(snmpInfo.getConvertFlg(), ConvertFlgEnum.class);
		snmpCheckInfo.setConvertFlg(convertFlgEnum);
		snmpCheckInfo.setSnmpOid(snmpInfo.getSnmpOid());
		
		return snmpCheckInfo;
	}
}
