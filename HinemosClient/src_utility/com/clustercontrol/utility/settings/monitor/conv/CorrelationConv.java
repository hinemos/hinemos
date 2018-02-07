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
import com.clustercontrol.utility.settings.monitor.xml.CorrelationInfo;
import com.clustercontrol.utility.settings.monitor.xml.CorrelationMonitor;
import com.clustercontrol.utility.settings.monitor.xml.CorrelationMonitors;
import com.clustercontrol.utility.settings.monitor.xml.NumericChangeAmount;
import com.clustercontrol.utility.settings.monitor.xml.NumericValue;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.ws.monitor.CorrelationCheckInfo;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.ws.monitor.MonitorNumericValueInfo;

/**
 * 相関係数監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 * @version 6.1.0
 * @since 6.1.0
 *
 *
 *
 */
public class CorrelationConv {
	private final static Log logger = LogFactory.getLog(CorrelationConv.class);

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
	 * Castor で作成した形式の相関係数監視監視設定情報を DTO へ変換する<BR>
	 *
	 * @param
	 * @return
	 * @throws ConvertorException
	 */
	public static List<MonitorInfo> createMonitorInfoList(CorrelationMonitors correlationMonitors) throws ConvertorException {
		List<MonitorInfo> monitorInfoList = new LinkedList<MonitorInfo>();

		for (CorrelationMonitor correlationMonitor : correlationMonitors.getCorrelationMonitor()) {
			logger.debug("Monitor Id : " + correlationMonitor.getMonitor().getMonitorId());

			MonitorInfo monitorInfo = MonitorConv.createMonitorInfo(correlationMonitor.getMonitor());

			for (NumericValue numericValue : correlationMonitor.getNumericValue()) {
				if(numericValue.getPriority() == PriorityConstant.TYPE_INFO ||
						numericValue.getPriority() == PriorityConstant.TYPE_WARNING){
					monitorInfo.getNumericValueInfo().add(MonitorConv.createMonitorNumericValueInfo(numericValue));
				}
			}
			if(monitorInfo.getNumericValueInfo().size() != 2){
				throw new ConvertorException(
						correlationMonitor.getMonitor().getMonitorId()
						+ " " + Messages.getString("SettingTools.NumericValueInvalid"));
			}
			
			for (NumericChangeAmount changeValue : correlationMonitor.getNumericChangeAmount()){
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
			
			monitorInfo.setCorrelationCheckInfo(createCorrelationCheckInfo(correlationMonitor.getCorrelationInfo()));
			monitorInfoList.add(monitorInfo);
		}

		return monitorInfoList;
	}

	/**
	 * DTO から、Castor で作成した形式の相関係数監視設定情報へ変換する<BR>
	 *
	 * @param
	 * @return
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static CorrelationMonitors createCorrelationMonitors(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		CorrelationMonitors correlationMonitors = new CorrelationMonitors();

		for (MonitorInfo monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorSettingEndpointWrapper
					.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			CorrelationMonitor correlationMonitor = new CorrelationMonitor();
			correlationMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			for (MonitorNumericValueInfo numericValueInfo : monitorInfo.getNumericValueInfo()) {
				if(numericValueInfo.getPriority() == PriorityConstant.TYPE_INFO ||
						numericValueInfo.getPriority() == PriorityConstant.TYPE_WARNING){
					if(numericValueInfo.getMonitorNumericType().contains("CHANGE")) {
						correlationMonitor.addNumericChangeAmount(MonitorConv.createNumericChangeAmount(numericValueInfo));
					}
					else{
						correlationMonitor.addNumericValue(MonitorConv.createNumericValue(numericValueInfo));
					}
				}
			}

			correlationMonitor.setCorrelationInfo(createCorrelationInfo(monitorInfo.getCorrelationCheckInfo()));
			correlationMonitors.addCorrelationMonitor(correlationMonitor);
		}

		correlationMonitors.setCommon(MonitorConv.versionDto2Xml());
		correlationMonitors.setSchemaInfo(getSchemaVersion());

		return correlationMonitors;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static CorrelationInfo createCorrelationInfo(CorrelationCheckInfo correlationCheckInfo) {
		CorrelationInfo correlationInfo = new CorrelationInfo();

		correlationInfo.setMonitorTypeId("");

		correlationInfo.setMonitorId(correlationCheckInfo.getMonitorId());
		correlationInfo.setTargetMonitorId(correlationCheckInfo.getTargetMonitorId());
		correlationInfo.setTargetItemName(correlationCheckInfo.getTargetItemName());
		correlationInfo.setTargetDisplayName(correlationCheckInfo.getTargetDisplayName());
		correlationInfo.setReferMonitorId(correlationCheckInfo.getReferMonitorId());
		correlationInfo.setReferItemName(correlationCheckInfo.getReferItemName());
		correlationInfo.setReferDisplayName(correlationCheckInfo.getReferDisplayName());
		correlationInfo.setReferFacilityId(correlationCheckInfo.getReferFacilityId());
		correlationInfo.setAnalysysRange(correlationCheckInfo.getAnalysysRange());

		return correlationInfo;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static CorrelationCheckInfo createCorrelationCheckInfo(CorrelationInfo correlationInfo) {
		CorrelationCheckInfo correlationCheckInfo = new CorrelationCheckInfo();

		correlationCheckInfo.setMonitorTypeId(correlationInfo.getMonitorTypeId());

		correlationCheckInfo.setMonitorId(correlationInfo.getMonitorId());
		correlationCheckInfo.setTargetMonitorId(correlationInfo.getTargetMonitorId());
		correlationCheckInfo.setTargetItemName(correlationInfo.getTargetItemName());
		correlationCheckInfo.setTargetDisplayName(correlationInfo.getTargetDisplayName());
		correlationCheckInfo.setReferMonitorId(correlationInfo.getReferMonitorId());
		correlationCheckInfo.setReferItemName(correlationInfo.getReferItemName());
		correlationCheckInfo.setReferDisplayName(correlationInfo.getReferDisplayName());
		correlationCheckInfo.setReferFacilityId(correlationInfo.getReferFacilityId());
		correlationCheckInfo.setAnalysysRange(correlationInfo.getAnalysysRange());

		return correlationCheckInfo;
	}
}