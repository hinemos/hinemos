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
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.NumericChangeAmount;
import com.clustercontrol.utility.settings.monitor.xml.NumericValue;
import com.clustercontrol.utility.settings.monitor.xml.PortInfo;
import com.clustercontrol.utility.settings.monitor.xml.PortMonitor;
import com.clustercontrol.utility.settings.monitor.xml.PortMonitors;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.ws.monitor.MonitorNumericValueInfo;
import com.clustercontrol.ws.monitor.PortCheckInfo;

/**
 * ポート監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 * @version 6.1.0
 * @since 1.0.0
 *
 *
 */
public class PortConv {
	private final static Log logger = LogFactory.getLog(PortConv.class);

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
	public static List<MonitorInfo> createMonitorInfoList(PortMonitors portMonitors) throws ConvertorException {
		List<MonitorInfo> monitorInfoList = new LinkedList<MonitorInfo>();

		for (PortMonitor portMonitor : portMonitors.getPortMonitor()) {
			logger.debug("Monitor Id : " + portMonitor.getMonitor().getMonitorId());

			MonitorInfo monitorInfo = MonitorConv.createMonitorInfo(portMonitor.getMonitor());

			for (NumericValue numericValue : portMonitor.getNumericValue()) {
				if(numericValue.getPriority() == PriorityConstant.TYPE_INFO ||
						numericValue.getPriority() == PriorityConstant.TYPE_WARNING){
					monitorInfo.getNumericValueInfo().add(MonitorConv.createMonitorNumericValueInfo(numericValue));
				}
			}
			if(monitorInfo.getNumericValueInfo().size() != 2){
				throw new ConvertorException(
						portMonitor.getMonitor().getMonitorId()
						+ " " + Messages.getString("SettingTools.NumericValueInvalid"));
			}
			
			for (NumericChangeAmount changeValue : portMonitor.getNumericChangeAmount()){
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

			monitorInfo.setPortCheckInfo(createPortCheckInfo(portMonitor.getPortInfo()));
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
	public static PortMonitors createPortMonitors(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		PortMonitors portMonitorList = new PortMonitors();

		for (MonitorInfo monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorSettingEndpointWrapper
					.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			PortMonitor portMonitor = new PortMonitor();
			portMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			for (MonitorNumericValueInfo numericValueInfo : monitorInfo.getNumericValueInfo()) {
				if(numericValueInfo.getPriority() == PriorityConstant.TYPE_INFO ||
						numericValueInfo.getPriority() == PriorityConstant.TYPE_WARNING){
					if(numericValueInfo.getMonitorNumericType().contains("CHANGE")) {
						portMonitor.addNumericChangeAmount(MonitorConv.createNumericChangeAmount(numericValueInfo));
					}
					else{
						portMonitor.addNumericValue(MonitorConv.createNumericValue(numericValueInfo));
					}
				}
			}

			portMonitor.setPortInfo(createPortInfo(monitorInfo.getPortCheckInfo()));
			portMonitorList.addPortMonitor(portMonitor);
		}

		portMonitorList.setCommon(MonitorConv.versionDto2Xml());
		portMonitorList.setSchemaInfo(getSchemaVersion());

		return portMonitorList;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static PortInfo createPortInfo(PortCheckInfo portCheckInfo) {
		PortInfo portInfo = new PortInfo();
		portInfo.setMonitorTypeId("");
		portInfo.setMonitorId(portCheckInfo.getMonitorId());
		portInfo.setPortNo(portCheckInfo.getPortNo());
		portInfo.setRunCount(portCheckInfo.getRunCount());
		portInfo.setRunInterval(portCheckInfo.getRunInterval());
		portInfo.setServiceId(portCheckInfo.getServiceId());
		portInfo.setTimeout(portCheckInfo.getTimeout());

		return portInfo;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static PortCheckInfo createPortCheckInfo(PortInfo portInfo) {
		PortCheckInfo portCheckInfo = new PortCheckInfo();

		portCheckInfo.setMonitorTypeId(portInfo.getMonitorTypeId());
		portCheckInfo.setMonitorId(portInfo.getMonitorId());
		portCheckInfo.setPortNo(portInfo.getPortNo());
		portCheckInfo.setRunCount(portInfo.getRunCount());
		portCheckInfo.setRunInterval(portInfo.getRunInterval());
		portCheckInfo.setServiceId(portInfo.getServiceId());
		portCheckInfo.setTimeout(portInfo.getTimeout());

		return portCheckInfo;
	}
}