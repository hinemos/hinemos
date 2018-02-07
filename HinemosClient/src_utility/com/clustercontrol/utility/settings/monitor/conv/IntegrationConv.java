/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.conv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.ConditionValue;
import com.clustercontrol.utility.settings.monitor.xml.IntegrationInfo;
import com.clustercontrol.utility.settings.monitor.xml.IntegrationMonitor;
import com.clustercontrol.utility.settings.monitor.xml.IntegrationMonitors;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.settings.monitor.xml.TruthValue;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.IntegrationCheckInfo;
import com.clustercontrol.ws.monitor.IntegrationConditionInfo;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.ws.monitor.MonitorTruthValueInfo;

/**
 * 収集値統合 監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 * @version 6.1.0
 * @since 6.1.0
 *
 *
 */
public class IntegrationConv {
	private final static Log logger = LogFactory.getLog(IntegrationConv.class);

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
	 * Castor で作成した形式の 収集値統合 監視設定情報を DTO へ変換する<BR>
	 *
	 * @param
	 * @return
	 * @throws ConvertorException
	 */
	public static List<MonitorInfo> createMonitorInfoList(IntegrationMonitors integrationMonitors) throws ConvertorException {
		List<MonitorInfo> monitorInfoList = new LinkedList<MonitorInfo>();

		for (IntegrationMonitor integrationMonitor : integrationMonitors.getIntegrationMonitor()) {
			logger.debug("Monitor Id : " + integrationMonitor.getMonitor().getMonitorId());

			MonitorInfo monitorInfo = MonitorConv.createMonitorInfo(integrationMonitor.getMonitor());
			for (TruthValue truthValue : integrationMonitor.getTruthValue()) {
				monitorInfo.getTruthValueInfo().add(MonitorConv.createTruthValue(truthValue));
			}

			monitorInfo.setIntegrationCheckInfo(createIntegrationCheckInfo(integrationMonitor.getIntegrationInfo()));
			monitorInfoList.add(monitorInfo);
		}

		return monitorInfoList;
	}

	/**
	 * DTO から、Castor で作成した形式の 収集値統合 監視設定情報へ変換する<BR>
	 *
	 * @param
	 * @return
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static IntegrationMonitors createIntegrationMonitors(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		IntegrationMonitors integrationMonitors = new IntegrationMonitors();

		for (MonitorInfo monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorSettingEndpointWrapper
					.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			IntegrationMonitor integrationMonitor = new IntegrationMonitor();
			integrationMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			for (MonitorTruthValueInfo truthValueInfo : monitorInfo.getTruthValueInfo()) {
				integrationMonitor.addTruthValue(MonitorConv.createTruthValue(truthValueInfo));
			}

			integrationMonitor.setIntegrationInfo(createIntegrationInfo(monitorInfo.getIntegrationCheckInfo()));
			integrationMonitors.addIntegrationMonitor(integrationMonitor);
		}

		integrationMonitors.setCommon(MonitorConv.versionDto2Xml());
		integrationMonitors.setSchemaInfo(getSchemaVersion());

		return integrationMonitors;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static IntegrationInfo createIntegrationInfo(IntegrationCheckInfo integrationCheckInfo) {
		IntegrationInfo integrationInfo = new IntegrationInfo();
		integrationInfo.setMonitorTypeId("");
		integrationInfo.setMonitorId(integrationCheckInfo.getMonitorId());
		
		List<ConditionValue> infos = new ArrayList<>();
		int orderNo = 0;
		for (IntegrationConditionInfo info : integrationCheckInfo.getConditionList()) {
			ConditionValue condition = new ConditionValue();
			condition.setDescription(info.getDescription());
			condition.setComparisonMethod(info.getComparisonMethod());
			condition.setComparisonValue(info.getComparisonValue());
			condition.setTargetDisplayName(info.getTargetDisplayName());
			condition.setTargetItemName(info.getTargetItemName());
			condition.setTargetMonitorId(info.getTargetMonitorId());
			condition.setTargetMonitorType(info.getTargetMonitorType());
			if (info.getTargetMonitorType() == MonitorTypeConstant.TYPE_STRING)
				condition.setIsAnd(info.isIsAnd());

			condition.setMonitorNode(info.isMonitorNode());
			if (!info.isMonitorNode())
				condition.setTargetFacilityId(info.getTargetFacilityId());
			
			condition.setOrderNo(++orderNo);
			
			infos.add(condition);
		}
		integrationInfo.setConditionValue(infos.toArray(new ConditionValue[0]));
		
		integrationInfo.setMessageOk(integrationCheckInfo.getMessageOk());
		integrationInfo.setMessageNg(integrationCheckInfo.getMessageNg());
		integrationInfo.setNotOrder(integrationCheckInfo.isNotOrder());
		integrationInfo.setTimeout(integrationCheckInfo.getTimeout());

		return integrationInfo;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static IntegrationCheckInfo createIntegrationCheckInfo(IntegrationInfo integrationInfo) {
		IntegrationCheckInfo integrationCheckInfo = new IntegrationCheckInfo();
		integrationCheckInfo.setMonitorTypeId(integrationInfo.getMonitorTypeId());
		integrationCheckInfo.setMonitorId(integrationInfo.getMonitorId());
		
		ConditionValue[] conditions = integrationInfo.getConditionValue();
		sort(conditions);
		for (ConditionValue condition : conditions) {
			IntegrationConditionInfo info = new IntegrationConditionInfo();
			info.setDescription(condition.getDescription());
			info.setComparisonMethod(condition.getComparisonMethod());
			info.setComparisonValue(condition.getComparisonValue());
			info.setIsAnd(condition.hasIsAnd() ? condition.getIsAnd() : true);
			
			info.setMonitorNode(condition.getMonitorNode());
			info.setTargetFacilityId(condition.getTargetFacilityId() != null ? condition.getTargetFacilityId() : "");
			
			info.setTargetDisplayName(condition.getTargetDisplayName());
			info.setTargetItemName(condition.getTargetItemName());
			info.setTargetMonitorId(condition.getTargetMonitorId());
			
			integrationCheckInfo.getConditionList().add(info);
		}
		
		integrationCheckInfo.setMessageNg(integrationInfo.getMessageNg());
		integrationCheckInfo.setMessageOk(integrationInfo.getMessageOk());
		integrationCheckInfo.setNotOrder(integrationInfo.getNotOrder());
		integrationCheckInfo.setTimeout(integrationInfo.getTimeout());
		return integrationCheckInfo;
	}
	
	/**
	 * ConditionValueを指定された順序(_orderNo)でソートする。
	 * @param objects
	 */
	private static void sort(ConditionValue[] objects) {
		Arrays.sort(
				objects,
				new Comparator<ConditionValue>() {
					@Override
					public int compare(ConditionValue obj1, ConditionValue obj2) {
						return obj1.getOrderNo() - obj2.getOrderNo();
					}
				});
	}
}