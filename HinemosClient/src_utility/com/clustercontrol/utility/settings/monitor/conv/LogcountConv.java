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
import org.openapitools.client.model.LogcountCheckInfoResponse;
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
import com.clustercontrol.utility.settings.monitor.xml.LogcountInfo;
import com.clustercontrol.utility.settings.monitor.xml.LogcountMonitor;
import com.clustercontrol.utility.settings.monitor.xml.LogcountMonitors;
import com.clustercontrol.utility.settings.monitor.xml.NumericChangeAmount;
import com.clustercontrol.utility.settings.monitor.xml.NumericValue;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * ログ件数 監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 * @version 6.1.0
 * @since 6.1.0
 *
 *
 */
public class LogcountConv {
	private final static Log logger = LogFactory.getLog(LogcountConv.class);

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
	 * Castor で作成した形式の ログ件数 監視設定情報を DTO へ変換する<BR>
	 *
	 * @param
	 * @return
	 * @throws ConvertorException
	 * @throws ParseException 
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	public static List<MonitorInfoResponse> createMonitorInfoList(LogcountMonitors logcountMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		List<MonitorInfoResponse> monitorInfoList = new LinkedList<MonitorInfoResponse>();

		for (LogcountMonitor logcountMonitor : logcountMonitors.getLogcountMonitor()) {
			logger.debug("Monitor Id : " + logcountMonitor.getMonitor().getMonitorId());

			MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(logcountMonitor.getMonitor());

			for (NumericValue numericValue : logcountMonitor.getNumericValue()) {
				if(numericValue.getPriority() == PriorityConstant.TYPE_INFO ||
						numericValue.getPriority() == PriorityConstant.TYPE_WARNING){
					monitorInfo.getNumericValueInfo().add(MonitorConv.createMonitorNumericValueInfo(numericValue));
				}
			}
			if (monitorInfo.getNumericValueInfo().size() != 2) {
				throw new ConvertorException(
						logcountMonitor.getMonitor().getMonitorId()
						+ " " + Messages.getString("SettingTools.NumericValueInvalid"));
			}
			
			for (NumericChangeAmount changeValue : logcountMonitor.getNumericChangeAmount()){
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
			if( monitorInfo.getChangeFlg() ==false && logcountMonitor.getNumericChangeAmount().length == 0 ){
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

			monitorInfo.setLogcountCheckInfo(createLogcountCheckInfo(logcountMonitor.getLogcountInfo()));
			monitorInfoList.add(monitorInfo);
		}

		return monitorInfoList;
	}

	/**
	 * DTO から、Castor で作成した形式の ログ件数 監視設定情報へ変換する<BR>
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
	public static LogcountMonitors createLogcountMonitors(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, ParseException, RestConnectFailed {
		LogcountMonitors logcountMonitors = new LogcountMonitors();

		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorsettingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			LogcountMonitor logcountMonitor = new LogcountMonitor();
			logcountMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			for (MonitorNumericValueInfoResponse numericValueInfo : monitorInfo.getNumericValueInfo()) {
				if(numericValueInfo.getPriority() == MonitorNumericValueInfoResponse.PriorityEnum .INFO ||
						numericValueInfo.getPriority() == MonitorNumericValueInfoResponse.PriorityEnum .WARNING){
					if(numericValueInfo.getMonitorNumericType().name().equals("CHANGE")) {
						logcountMonitor.addNumericChangeAmount(MonitorConv.createNumericChangeAmount(monitorInfo.getMonitorId(),numericValueInfo));
					}
					else{
						logcountMonitor.addNumericValue(MonitorConv.createNumericValue(monitorInfo.getMonitorId(),numericValueInfo));
					}
				}
			}

			logcountMonitor.setLogcountInfo(createLogcountInfo(monitorInfo));
			logcountMonitors.addLogcountMonitor(logcountMonitor);
		}

		logcountMonitors.setCommon(MonitorConv.versionDto2Xml());
		logcountMonitors.setSchemaInfo(getSchemaVersion());

		return logcountMonitors;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static LogcountInfo createLogcountInfo(MonitorInfoResponse monitorInfo) {
		LogcountInfo logcountInfo = new LogcountInfo();
		logcountInfo.setMonitorTypeId("");
		logcountInfo.setMonitorId(monitorInfo.getMonitorId());
		
		logcountInfo.setIsAnd(monitorInfo.getLogcountCheckInfo().getIsAnd());
		logcountInfo.setKeyword(monitorInfo.getLogcountCheckInfo().getKeyword());
		logcountInfo.setTag(monitorInfo.getLogcountCheckInfo().getTag());
		logcountInfo.setTargetMonitorId(monitorInfo.getLogcountCheckInfo().getTargetMonitorId());

		return logcountInfo;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static LogcountCheckInfoResponse createLogcountCheckInfo(LogcountInfo logcountInfo) {
		LogcountCheckInfoResponse logcountCheckInfo = new LogcountCheckInfoResponse();
		
		logcountCheckInfo.setIsAnd(logcountInfo.getIsAnd());
		logcountCheckInfo.setKeyword(logcountInfo.getKeyword());
		logcountCheckInfo.setTag(logcountInfo.getTag());
		logcountCheckInfo.setTargetMonitorId(logcountInfo.getTargetMonitorId());

		return logcountCheckInfo;
	}
}