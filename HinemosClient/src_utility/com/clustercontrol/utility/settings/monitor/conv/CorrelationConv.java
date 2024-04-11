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
import org.openapitools.client.model.CorrelationCheckInfoResponse;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorNumericValueInfoResponse;
import org.openapitools.client.model.MonitorNumericValueInfoResponse.MonitorNumericTypeEnum;
import org.openapitools.client.model.MonitorNumericValueInfoResponse.PriorityEnum;

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
import com.clustercontrol.utility.settings.monitor.xml.CorrelationInfo;
import com.clustercontrol.utility.settings.monitor.xml.CorrelationMonitor;
import com.clustercontrol.utility.settings.monitor.xml.CorrelationMonitors;
import com.clustercontrol.utility.settings.monitor.xml.NumericChangeAmount;
import com.clustercontrol.utility.settings.monitor.xml.NumericValue;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.version.util.VersionUtil;

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

	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	private final static String SCHEMA_TYPE = VersionUtil.getSchemaProperty("MONITOR.CORRELATION.SCHEMATYPE");
	private final static String SCHEMA_VERSION = VersionUtil.getSchemaProperty("MONITOR.CORRELATION.SCHEMAVERSION");
	private final static String SCHEMA_REVISION =VersionUtil.getSchemaProperty("MONITOR.CORRELATION.SCHEMAREVISION");

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
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 * @throws ParseException 
	 */
	public static List<MonitorInfoResponse> createMonitorInfoList(CorrelationMonitors correlationMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		List<MonitorInfoResponse> monitorInfoList = new LinkedList<MonitorInfoResponse>();

		for (CorrelationMonitor correlationMonitor : correlationMonitors.getCorrelationMonitor()) {
			logger.debug("Monitor Id : " + correlationMonitor.getMonitor().getMonitorId());

			MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(correlationMonitor.getMonitor());

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
			if( monitorInfo.getChangeFlg() ==false && correlationMonitor.getNumericChangeAmount().length == 0 ){
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
	 * @throws RestConnectFailed 
	 * @throws ParseException 
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static CorrelationMonitors createCorrelationMonitors(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		CorrelationMonitors correlationMonitors = new CorrelationMonitors();

		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorsettingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			CorrelationMonitor correlationMonitor = new CorrelationMonitor();
			correlationMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			for (MonitorNumericValueInfoResponse numericValueInfo : monitorInfo.getNumericValueInfo()) {
				if(numericValueInfo.getPriority() == PriorityEnum.INFO ||
						numericValueInfo.getPriority() == PriorityEnum.WARNING){
					if(numericValueInfo.getMonitorNumericType().name().equals("CHANGE")) {
						correlationMonitor.addNumericChangeAmount(MonitorConv.createNumericChangeAmount(monitorInfo.getMonitorId(),numericValueInfo));
					}
					else{
						correlationMonitor.addNumericValue(MonitorConv.createNumericValue(monitorInfo.getMonitorId(),numericValueInfo));
					}
				}
			}

			correlationMonitor.setCorrelationInfo(createCorrelationInfo(monitorInfo));
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
	private static CorrelationInfo createCorrelationInfo(MonitorInfoResponse monitorInfo) {
		CorrelationInfo correlationInfo = new CorrelationInfo();

		correlationInfo.setMonitorTypeId("");

		correlationInfo.setMonitorId(monitorInfo.getMonitorId());
		correlationInfo.setTargetMonitorId(monitorInfo.getCorrelationCheckInfo().getTargetMonitorId());
		correlationInfo.setTargetItemName(monitorInfo.getCorrelationCheckInfo().getTargetItemName());
		correlationInfo.setTargetDisplayName(monitorInfo.getCorrelationCheckInfo().getTargetDisplayName());
		correlationInfo.setReferMonitorId(monitorInfo.getCorrelationCheckInfo().getReferMonitorId());
		correlationInfo.setReferItemName(monitorInfo.getCorrelationCheckInfo().getReferItemName());
		correlationInfo.setReferDisplayName(monitorInfo.getCorrelationCheckInfo().getReferDisplayName());
		correlationInfo.setReferFacilityId(monitorInfo.getCorrelationCheckInfo().getReferFacilityId());
		correlationInfo.setAnalysysRange(monitorInfo.getCorrelationCheckInfo().getAnalysysRange());

		return correlationInfo;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static CorrelationCheckInfoResponse createCorrelationCheckInfo(CorrelationInfo correlationInfo) {
		CorrelationCheckInfoResponse correlationCheckInfo = new CorrelationCheckInfoResponse();

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