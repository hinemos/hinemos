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
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.NumericChangeAmount;
import com.clustercontrol.utility.settings.monitor.xml.NumericValue;
import com.clustercontrol.utility.settings.monitor.xml.PingInfo;
import com.clustercontrol.utility.settings.monitor.xml.PingMonitor;
import com.clustercontrol.utility.settings.monitor.xml.PingMonitors;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.ws.monitor.MonitorNumericValueInfo;
import com.clustercontrol.ws.monitor.PingCheckInfo;

/**
 * Ping 監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 * @version 6.1.0
 * @since 1.0.0
 *
 *
 *
 */
public class PingConv {
	private final static Log logger = LogFactory.getLog(PingConv.class);

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
	 * Castor で作成した形式の PING 監視設定情報を DTO へ変換する<BR>
	 *
	 * @param
	 * @return
	 * @throws ConvertorException
	 */
	public static List<MonitorInfo> createMonitorInfoList(PingMonitors pingMonitors) throws ConvertorException {
		List<MonitorInfo> monitorInfoList = new LinkedList<MonitorInfo>();

		for (PingMonitor pingMonitor : pingMonitors.getPingMonitor()) {
			logger.debug("Monitor Id : " + pingMonitor.getMonitor().getMonitorId());

			MonitorInfo monitorInfo = MonitorConv.createMonitorInfo(pingMonitor.getMonitor());

			for (NumericValue numericValue : pingMonitor.getNumericValue()) {
				if(numericValue.getPriority() == PriorityConstant.TYPE_INFO ||
						numericValue.getPriority() == PriorityConstant.TYPE_WARNING){
					monitorInfo.getNumericValueInfo().add(MonitorConv.createMonitorNumericValueInfo(numericValue));
				}
			}
			if(monitorInfo.getNumericValueInfo().size() != 2){
				throw new ConvertorException(
						pingMonitor.getMonitor().getMonitorId()
						+ " " + Messages.getString("SettingTools.NumericValueInvalid"));
			}
			
			for (NumericChangeAmount changeValue : pingMonitor.getNumericChangeAmount()){
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
			
			monitorInfo.setPingCheckInfo(createPingCheckInfo(pingMonitor.getPingInfo()));
			monitorInfoList.add(monitorInfo);
		}

		return monitorInfoList;
	}

	/**
	 * DTO から、Castor で作成した形式の PING 監視設定情報へ変換する<BR>
	 *
	 * @param
	 * @return
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static PingMonitors createPingMonitors(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		PingMonitors pingMonitors = new PingMonitors();

		for (MonitorInfo monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorSettingEndpointWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			PingMonitor pingMonitor = new PingMonitor();
			pingMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			for (MonitorNumericValueInfo numericValueInfo : monitorInfo.getNumericValueInfo()) {
				if(numericValueInfo.getPriority() == PriorityConstant.TYPE_INFO ||
						numericValueInfo.getPriority() == PriorityConstant.TYPE_WARNING){
					if(numericValueInfo.getMonitorNumericType().contains("CHANGE")) {
						pingMonitor.addNumericChangeAmount(MonitorConv.createNumericChangeAmount(numericValueInfo));
					}
					else{
						pingMonitor.addNumericValue(MonitorConv.createNumericValue(numericValueInfo));
					}
				}
			}

			pingMonitor.setPingInfo(createPingInfo(monitorInfo.getPingCheckInfo()));
			pingMonitors.addPingMonitor(pingMonitor);
		}

		pingMonitors.setCommon(MonitorConv.versionDto2Xml());
		pingMonitors.setSchemaInfo(getSchemaVersion());

		return pingMonitors;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static PingInfo createPingInfo(PingCheckInfo pingCheckInfo) {
		PingInfo pingInfo = new PingInfo();

		pingInfo.setMonitorTypeId("");

		pingInfo.setMonitorId(pingCheckInfo.getMonitorId());
		pingInfo.setRunCount(pingCheckInfo.getRunCount());
		pingInfo.setRunInterval(pingCheckInfo.getRunInterval());
		pingInfo.setTimeout(pingCheckInfo.getTimeout());

		return pingInfo;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static PingCheckInfo createPingCheckInfo(PingInfo pingInfo) {
		PingCheckInfo pingCheckInfo = new PingCheckInfo();

		pingCheckInfo.setMonitorTypeId(pingInfo.getMonitorTypeId());

		pingCheckInfo.setMonitorId(pingInfo.getMonitorId());
		pingCheckInfo.setRunCount(pingInfo.getRunCount());
		pingCheckInfo.setRunInterval(pingInfo.getRunInterval());
		pingCheckInfo.setTimeout(pingInfo.getTimeout());

		return pingCheckInfo;
	}
}