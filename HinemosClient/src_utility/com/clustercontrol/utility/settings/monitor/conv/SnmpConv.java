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
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
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
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.ws.monitor.MonitorNumericValueInfo;
import com.clustercontrol.ws.monitor.MonitorStringValueInfo;
import com.clustercontrol.ws.monitor.SnmpCheckInfo;

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

	private final static String SCHEMA_TYPE = "I";
	private final static String SCHEMA_VERSION = "1";
	private final static String SCHEMA_REVISION = "1";
	
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
	 */
	public static List<MonitorInfo> createMonitorInfoList(SnmpMonitors snmpMonitors) throws ConvertorException {
		List<MonitorInfo> monitorInfoList = new LinkedList<MonitorInfo>();
		
		for (SnmpMonitor snmpMonitor : snmpMonitors.getSnmpMonitor()) {
			logger.debug("Monitor Id : " + snmpMonitor.getMonitor().getMonitorId());

			MonitorInfo monitorInfo = MonitorConv.createMonitorInfo(snmpMonitor.getMonitor());

			if(monitorInfo.getMonitorType() == MonitorTypeConstant.TYPE_NUMERIC){
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
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static SnmpMonitors createSnmpMonitors(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		SnmpMonitors snmpMonitors = new SnmpMonitors();
		
		for (MonitorInfo monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo =  MonitorSettingEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getMonitor(monitorInfo.getMonitorId());

			SnmpMonitor snmpMonitor = new SnmpMonitor();
			snmpMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));
			
			for (MonitorNumericValueInfo numericValueInfo : monitorInfo.getNumericValueInfo()) {
				if(numericValueInfo.getPriority() == PriorityConstant.TYPE_INFO ||
						numericValueInfo.getPriority() == PriorityConstant.TYPE_WARNING){
					if(numericValueInfo.getMonitorNumericType().contains("CHANGE")) {
						snmpMonitor.addNumericChangeAmount(MonitorConv.createNumericChangeAmount(numericValueInfo));
					}
					else{
						snmpMonitor.addNumericValue(MonitorConv.createNumericValue(numericValueInfo));
					}
				}
			}
			
			int orderNo = 0;
			for (MonitorStringValueInfo monitorStringValueInfo : monitorInfo.getStringValueInfo()) {
				snmpMonitor.addStringValue(MonitorConv.createStringValue(monitorStringValueInfo, ++orderNo));
			}

			snmpMonitor.setSnmpInfo(createSnmpInfo(monitorInfo.getSnmpCheckInfo()));
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
	private static SnmpInfo createSnmpInfo(SnmpCheckInfo snmpCheckInfo) {
		SnmpInfo snmpInfo = new SnmpInfo();
		// DBからMonitorTypeIdがなくなっているのでこのままだとnullが設定されてしまう。
		// nullが設定されるとエクスポート時にValidedにひっかかる為、空文字を設定しておく。
		//snmpInfo.setMonitorTypeId(snmpCheckInfo.getMonitorTypeId());
		snmpInfo.setMonitorTypeId("");

		snmpInfo.setMonitorId(snmpCheckInfo.getMonitorId());
		snmpInfo.setConvertFlg(snmpCheckInfo.getConvertFlg());
		snmpInfo.setSnmpOid(snmpCheckInfo.getSnmpOid());
		
		return snmpInfo;
	}
	
	/**
	 * <BR>
	 * 
	 * @return
	 */
	private static SnmpCheckInfo createSnmpCheckInfo(SnmpInfo snmpInfo) {
		SnmpCheckInfo snmpCheckInfo = new SnmpCheckInfo();
		// DBからMonitorTypeIdがなくなっているのでこのままだとnullが設定されてしまう。
		// nullが設定されるとエクスポート時にValidedにひっかかる為、空文字を設定しておく。
		//snmpCheckInfo.setMonitorTypeId(snmpInfo.getMonitorTypeId());
		snmpCheckInfo.setMonitorTypeId("");
		snmpCheckInfo.setMonitorId(snmpInfo.getMonitorId());
		snmpCheckInfo.setConvertFlg(snmpInfo.getConvertFlg());
		snmpCheckInfo.setSnmpOid(snmpInfo.getSnmpOid());

//		snmpCheckInfo.setCommunityName();
//		snmpCheckInfo.setSnmpPort();
//		snmpCheckInfo.setSnmpVersion();
		
		return snmpCheckInfo;
	}
}
