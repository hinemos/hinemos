/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.conv;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.IntegrationCheckInfoResponse;
import org.openapitools.client.model.IntegrationConditionInfoResponse;
import org.openapitools.client.model.IntegrationConditionInfoResponse.TargetMonitorTypeEnum;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorTruthValueInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.ConditionValue;
import com.clustercontrol.utility.settings.monitor.xml.IntegrationInfo;
import com.clustercontrol.utility.settings.monitor.xml.IntegrationMonitor;
import com.clustercontrol.utility.settings.monitor.xml.IntegrationMonitors;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.settings.monitor.xml.TruthValue;
import com.clustercontrol.utility.util.OpenApiEnumConverter;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.version.util.VersionUtil;

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

	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	private final static String SCHEMA_TYPE = VersionUtil.getSchemaProperty("MONITOR.INTEGRATION.SCHEMATYPE");
	private final static String SCHEMA_VERSION = VersionUtil.getSchemaProperty("MONITOR.INTEGRATION.SCHEMAVERSION");
	private final static String SCHEMA_REVISION =VersionUtil.getSchemaProperty("MONITOR.INTEGRATION.SCHEMAREVISION");

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
	 * @throws ParseException 
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	public static List<MonitorInfoResponse> createMonitorInfoList(IntegrationMonitors integrationMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		List<MonitorInfoResponse> monitorInfoList = new LinkedList<MonitorInfoResponse>();

		for (IntegrationMonitor integrationMonitor : integrationMonitors.getIntegrationMonitor()) {
			logger.debug("Monitor Id : " + integrationMonitor.getMonitor().getMonitorId());

			MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(integrationMonitor.getMonitor());
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
	 * @throws RestConnectFailed 
	 * @throws ParseException 
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static IntegrationMonitors createIntegrationMonitors(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		IntegrationMonitors integrationMonitors = new IntegrationMonitors();

		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorsettingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			IntegrationMonitor integrationMonitor = new IntegrationMonitor();
			integrationMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			for (MonitorTruthValueInfoResponse truthValueInfo : monitorInfo.getTruthValueInfo()) {
				integrationMonitor.addTruthValue(MonitorConv.createTruthValue(monitorInfo.getMonitorId(),truthValueInfo));
			}

			integrationMonitor.setIntegrationInfo(createIntegrationInfo(monitorInfo));
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
	private static IntegrationInfo createIntegrationInfo(MonitorInfoResponse monitorInfo) {
		IntegrationInfo integrationInfo = new IntegrationInfo();
		integrationInfo.setMonitorTypeId("");
		integrationInfo.setMonitorId(monitorInfo.getMonitorId());
		
		List<ConditionValue> infos = new ArrayList<>();
		int orderNo = 0;
		for (IntegrationConditionInfoResponse info : monitorInfo.getIntegrationCheckInfo().getConditionList()) {
			ConditionValue condition = new ConditionValue();
			condition.setDescription(info.getDescription());
			condition.setComparisonMethod(info.getComparisonMethod());
			condition.setComparisonValue(info.getComparisonValue());
			condition.setTargetDisplayName(info.getTargetDisplayName());
			condition.setTargetItemName(info.getTargetItemName());
			condition.setTargetMonitorId(info.getTargetMonitorId());
			int targetMonitroId = OpenApiEnumConverter.enumToInteger(info.getTargetMonitorType());
			condition.setTargetMonitorType(targetMonitroId);
			if (info.getTargetMonitorType() == TargetMonitorTypeEnum.STRING)
				condition.setIsAnd(info.getIsAnd());

			condition.setMonitorNode(info.getMonitorNode());
			if (!info.getMonitorNode())
				condition.setTargetFacilityId(info.getTargetFacilityId());
			
			condition.setOrderNo(++orderNo);
			
			infos.add(condition);
		}
		integrationInfo.setConditionValue(infos.toArray(new ConditionValue[0]));
		
		integrationInfo.setMessageOk(monitorInfo.getIntegrationCheckInfo().getMessageOk());
		integrationInfo.setMessageNg(monitorInfo.getIntegrationCheckInfo().getMessageNg());
		integrationInfo.setNotOrder(monitorInfo.getIntegrationCheckInfo().getNotOrder());
		integrationInfo.setTimeout(monitorInfo.getIntegrationCheckInfo().getTimeout());

		return integrationInfo;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static IntegrationCheckInfoResponse createIntegrationCheckInfo(IntegrationInfo integrationInfo) {
		IntegrationCheckInfoResponse integrationCheckInfo = new IntegrationCheckInfoResponse();
		
		ConditionValue[] conditions = integrationInfo.getConditionValue();
		sort(conditions);
		for (ConditionValue condition : conditions) {
			IntegrationConditionInfoResponse info = new IntegrationConditionInfoResponse();
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