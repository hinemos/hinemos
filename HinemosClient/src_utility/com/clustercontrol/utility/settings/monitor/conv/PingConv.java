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
import org.openapitools.client.model.MonitorNumericValueInfoResponse;
import org.openapitools.client.model.PingCheckInfoResponse;

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
import com.clustercontrol.utility.settings.monitor.xml.PingInfo;
import com.clustercontrol.utility.settings.monitor.xml.PingMonitor;
import com.clustercontrol.utility.settings.monitor.xml.PingMonitors;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.util.UtilityManagerUtil;

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
	private final static String SCHEMA_REVISION = "2";

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
	 * @throws ParseException 
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	public static List<MonitorInfoResponse> createMonitorInfoList(PingMonitors pingMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		List<MonitorInfoResponse> monitorInfoList = new LinkedList<MonitorInfoResponse>();

		for (PingMonitor pingMonitor : pingMonitors.getPingMonitor()) {
			logger.debug("Monitor Id : " + pingMonitor.getMonitor().getMonitorId());

			MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(pingMonitor.getMonitor());

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
			MonitorNumericValueInfoResponse monitorNumericValueInfo = new MonitorNumericValueInfoResponse();
			monitorNumericValueInfo.setMonitorNumericType(MonitorNumericValueInfoResponse.MonitorNumericTypeEnum.BASIC);
			monitorNumericValueInfo.setPriority(MonitorNumericValueInfoResponse.PriorityEnum.CRITICAL);
			monitorNumericValueInfo.setThresholdLowerLimit(0.0);
			monitorNumericValueInfo.setThresholdUpperLimit(0.0);
			monitorInfo.getNumericValueInfo().add(monitorNumericValueInfo);

			monitorNumericValueInfo = new MonitorNumericValueInfoResponse();
			monitorNumericValueInfo.setMonitorNumericType(MonitorNumericValueInfoResponse.MonitorNumericTypeEnum.BASIC);
			monitorNumericValueInfo.setPriority(MonitorNumericValueInfoResponse.PriorityEnum.UNKNOWN);
			monitorNumericValueInfo.setThresholdLowerLimit(0.0);
			monitorNumericValueInfo.setThresholdUpperLimit(0.0);
			monitorInfo.getNumericValueInfo().add(monitorNumericValueInfo);

			// 変化量監視が無効の場合、関連閾値が未入力なら、画面デフォルト値にて補完
			if( monitorInfo.getChangeFlg() ==false && pingMonitor.getNumericChangeAmount().length == 0 ){
				MonitorConv.setMonitorChangeAmountDefault(monitorInfo);
			}
			
			// 変化量についても閾値判定と同様にTYPE_CRITICALとTYPE_UNKNOWNを定義する
			monitorNumericValueInfo = new MonitorNumericValueInfoResponse();
			monitorNumericValueInfo.setMonitorNumericType(MonitorNumericValueInfoResponse.MonitorNumericTypeEnum.CHANGE);
			monitorNumericValueInfo.setPriority(MonitorNumericValueInfoResponse.PriorityEnum.CRITICAL);
			monitorNumericValueInfo.setThresholdLowerLimit(0.0);
			monitorNumericValueInfo.setThresholdUpperLimit(0.0);
			monitorInfo.getNumericValueInfo().add(monitorNumericValueInfo);
			
			monitorNumericValueInfo = new MonitorNumericValueInfoResponse();
			monitorNumericValueInfo.setMonitorNumericType(MonitorNumericValueInfoResponse.MonitorNumericTypeEnum.CHANGE);
			monitorNumericValueInfo.setPriority(MonitorNumericValueInfoResponse.PriorityEnum.UNKNOWN);
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
	 * @throws RestConnectFailed 
	 * @throws ParseException 
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static PingMonitors createPingMonitors(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		PingMonitors pingMonitors = new PingMonitors();

		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorsettingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			PingMonitor pingMonitor = new PingMonitor();
			pingMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			for (MonitorNumericValueInfoResponse numericValueInfo : monitorInfo.getNumericValueInfo() ) {
				if(numericValueInfo.getPriority() == MonitorNumericValueInfoResponse.PriorityEnum.INFO ||
						numericValueInfo.getPriority() == MonitorNumericValueInfoResponse.PriorityEnum.WARNING){
					if(numericValueInfo.getMonitorNumericType().equals(MonitorNumericValueInfoResponse.MonitorNumericTypeEnum.CHANGE )) {
						pingMonitor.addNumericChangeAmount(MonitorConv.createNumericChangeAmount(monitorInfo.getMonitorId(), numericValueInfo));
					}
					else{
						pingMonitor.addNumericValue(MonitorConv.createNumericValue(monitorInfo.getMonitorId(),numericValueInfo));
					}
				}
			}

			pingMonitor.setPingInfo(createPingInfo(monitorInfo.getMonitorId(), monitorInfo.getPingCheckInfo()));
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
	private static PingInfo createPingInfo(String monitorId ,PingCheckInfoResponse pingCheckInfo) {
		PingInfo pingInfo = new PingInfo();

		pingInfo.setMonitorTypeId("");

		pingInfo.setMonitorId(monitorId);
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
	private static PingCheckInfoResponse createPingCheckInfo(PingInfo pingInfo) {
		PingCheckInfoResponse pingCheckInfo = new PingCheckInfoResponse();

		pingCheckInfo.setRunCount(pingInfo.getRunCount());
		pingCheckInfo.setRunInterval(pingInfo.getRunInterval());
		pingCheckInfo.setTimeout(pingInfo.getTimeout());

		return pingCheckInfo;
	}
}