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
import org.openapitools.client.model.MonitorNumericValueInfoResponse.MonitorNumericTypeEnum;
import org.openapitools.client.model.MonitorNumericValueInfoResponse.PriorityEnum;
import org.openapitools.client.model.ProcessCheckInfoResponse;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.constant.YesNoConstant;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.Monitor;
import com.clustercontrol.utility.settings.monitor.xml.NumericChangeAmount;
import com.clustercontrol.utility.settings.monitor.xml.NumericValue;
import com.clustercontrol.utility.settings.monitor.xml.ProcessInfo;
import com.clustercontrol.utility.settings.monitor.xml.ProcessMonitor;
import com.clustercontrol.utility.settings.monitor.xml.ProcessMonitors;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * プロセス監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 * @version 6.1.0
 * @since 1.0.0
 *
 *
 */
public class ProcessConv {
	private final static Log logger = LogFactory.getLog(ProcessConv.class);

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
	 * Castor で作成した構造に格納されている Process 監視設定情報を DTO へ変換する<BR>
	 *
	 * @param
	 * @return
	 * @throws ConvertorException
	 * @throws ParseException 
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	public static List<MonitorInfoResponse> createMonitorInfoList(ProcessMonitors processMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		List<MonitorInfoResponse> monitorInfoList = new LinkedList<MonitorInfoResponse>();

		for (ProcessMonitor processMonitor : processMonitors.getProcessMonitor()) {
			logger.debug("Monitor Id : " + processMonitor.getMonitor().getMonitorId());

			MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(processMonitor.getMonitor());

			for (NumericValue numericValue : processMonitor.getNumericValue()) {
				if(numericValue.getPriority() == PriorityConstant.TYPE_INFO ||
						numericValue.getPriority() == PriorityConstant.TYPE_WARNING){
					monitorInfo.getNumericValueInfo().add(MonitorConv.createMonitorNumericValueInfo(numericValue));
				}
			}
			if(monitorInfo.getNumericValueInfo().size() != 2){
				throw new ConvertorException(
						processMonitor.getMonitor().getMonitorId()
						+ " " + Messages.getString("SettingTools.NumericValueInvalid"));
			}

			for (NumericChangeAmount changeValue : processMonitor.getNumericChangeAmount()){
				if(changeValue.getPriority() == PriorityConstant.TYPE_INFO ||
						changeValue.getPriority() == PriorityConstant.TYPE_WARNING){
					monitorInfo.getNumericValueInfo().add(MonitorConv.createMonitorNumericValueInfo(changeValue));
				}
			}			
			MonitorNumericValueInfoResponse monitorNumericValueInfo = new MonitorNumericValueInfoResponse();

			monitorInfo.setMonitorId(processMonitor.getMonitor().getMonitorId());//MonitorId
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
			if( monitorInfo.getChangeFlg() ==false && processMonitor.getNumericChangeAmount().length == 0 ){
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

			monitorInfo.setProcessCheckInfo(createProcessCheckInfo(processMonitor.getProcessInfo()));
			monitorInfoList.add(monitorInfo);
		}

		return monitorInfoList;
	}

	/**
	 * DTO に格納されている Process 監視設定情報 を Castor で作成した構造へ変換する<BR>
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
	public static ProcessMonitors createProcessMonitors(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, ParseException, RestConnectFailed {
		ProcessMonitors processMonitors = new ProcessMonitors();

		for (MonitorInfoResponse monitorInfo : monitorInfoList) {

			monitorInfo = MonitorsettingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			ProcessMonitor processMonitor = new ProcessMonitor();

			Monitor monitor = MonitorConv.createMonitor(monitorInfo);
			processMonitor.setMonitor(monitor);

			assert monitorInfo.getProcessCheckInfo() != null: "ProcessCheckInfo must not be null.";
			ProcessInfo processInfo = createProcessInfo(monitorInfo);
			processMonitor.setProcessInfo(processInfo);

			for (MonitorNumericValueInfoResponse numericValueInfo: monitorInfo.getNumericValueInfo()) {
				if(numericValueInfo.getPriority() == PriorityEnum.INFO ||
						numericValueInfo.getPriority() == PriorityEnum.WARNING){

					if(numericValueInfo.getMonitorNumericType().name().equals("CHANGE")) {
						processMonitor.addNumericChangeAmount(MonitorConv.createNumericChangeAmount(monitorInfo.getMonitorId(),numericValueInfo));
					}
					else{
						processMonitor.addNumericValue(MonitorConv.createNumericValue(monitorInfo.getMonitorId(),numericValueInfo));
					}
				}
			}

			processMonitors.addProcessMonitor(processMonitor);
		}

		processMonitors.setCommon(MonitorConv.versionDto2Xml());
		processMonitors.setSchemaInfo(getSchemaVersion());

		return processMonitors;
	}

	private static ProcessInfo createProcessInfo(MonitorInfoResponse processCheckInfo) {
		ProcessInfo processInfo = new ProcessInfo();
		processInfo.setCommand(processCheckInfo.getProcessCheckInfo().getCommand());
		processInfo.setMonitorId(processCheckInfo.getMonitorId());
		processInfo.setMonitorTypeId("");

		processInfo.setParam(processCheckInfo.getProcessCheckInfo().getParam());
		processInfo.setCaseSensitivityFlg(YesNoConstant.booleanToType(processCheckInfo.getProcessCheckInfo().getCaseSensitivityFlg()));

		return processInfo;
	}

	private static ProcessCheckInfoResponse  createProcessCheckInfo(ProcessInfo processInfo) {
		ProcessCheckInfoResponse  processCheckInfo = new ProcessCheckInfoResponse ();
		processCheckInfo.setCommand(processInfo.getCommand());

		processCheckInfo.setParam(processInfo.getParam());
		processCheckInfo.setCaseSensitivityFlg(YesNoConstant.typeToBoolean(processInfo.getCaseSensitivityFlg()));

		return processCheckInfo;
	}
}
