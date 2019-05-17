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
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.ws.monitor.MonitorNumericValueInfo;
import com.clustercontrol.ws.monitor.ProcessCheckInfo;

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
	 * Castor で作成した構造に格納されている Process 監視設定情報を DTO へ変換する<BR>
	 *
	 * @param
	 * @return
	 * @throws ConvertorException
	 */
	public static List<MonitorInfo> createMonitorInfoList(ProcessMonitors processMonitors) throws ConvertorException {
		List<MonitorInfo> monitorInfoList = new LinkedList<MonitorInfo>();

		for (ProcessMonitor processMonitor : processMonitors.getProcessMonitor()) {
			logger.debug("Monitor Id : " + processMonitor.getMonitor().getMonitorId());

			MonitorInfo monitorInfo = MonitorConv.createMonitorInfo(processMonitor.getMonitor());

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

			// 変化量監視が無効の場合、関連閾値が未入力なら、画面デフォルト値にて補完
			if( monitorInfo.isChangeFlg() ==false && processMonitor.getNumericChangeAmount().length == 0 ){
				MonitorConv.setMonitorChangeAmountDefault(monitorInfo);
			}
			
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
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static ProcessMonitors createProcessMonitors(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		ProcessMonitors processMonitors = new ProcessMonitors();

		for (MonitorInfo monitorInfo : monitorInfoList) {

			monitorInfo = MonitorSettingEndpointWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			ProcessMonitor processMonitor = new ProcessMonitor();

			Monitor monitor = MonitorConv.createMonitor(monitorInfo);
			processMonitor.setMonitor(monitor);

			assert monitorInfo.getProcessCheckInfo() != null: "ProcessCheckInfo must not be null.";
			ProcessInfo processInfo = createProcessInfo(monitorInfo.getProcessCheckInfo());
			processMonitor.setProcessInfo(processInfo);

			for (MonitorNumericValueInfo numericValueInfo: monitorInfo.getNumericValueInfo()) {
				if(numericValueInfo.getPriority() == PriorityConstant.TYPE_INFO ||
						numericValueInfo.getPriority() == PriorityConstant.TYPE_WARNING){

					if(numericValueInfo.getMonitorNumericType().contains("CHANGE")) {
						processMonitor.addNumericChangeAmount(MonitorConv.createNumericChangeAmount(numericValueInfo));
					}
					else{
						processMonitor.addNumericValue(MonitorConv.createNumericValue(numericValueInfo));
					}
				}
			}

			processMonitors.addProcessMonitor(processMonitor);
		}

		processMonitors.setCommon(MonitorConv.versionDto2Xml());
		processMonitors.setSchemaInfo(getSchemaVersion());

		return processMonitors;
	}

	private static ProcessInfo createProcessInfo(ProcessCheckInfo processCheckInfo) {
		ProcessInfo processInfo = new ProcessInfo();
		processInfo.setCommand(processCheckInfo.getCommand());
		processInfo.setMonitorId(processCheckInfo.getMonitorId());
		processInfo.setMonitorTypeId("");

		processInfo.setParam(processCheckInfo.getParam());
		processInfo.setCaseSensitivityFlg(YesNoConstant.booleanToType(processCheckInfo.isCaseSensitivityFlg()));

		return processInfo;
	}

	private static ProcessCheckInfo createProcessCheckInfo(ProcessInfo processInfo) {
		ProcessCheckInfo processCheckInfo = new ProcessCheckInfo();
		processCheckInfo.setCommand(processInfo.getCommand());
		processCheckInfo.setMonitorId(processInfo.getMonitorId());
		processCheckInfo.setMonitorTypeId("");

		processCheckInfo.setParam(processInfo.getParam());
		processCheckInfo.setCaseSensitivityFlg(YesNoConstant.typeToBoolean(processInfo.getCaseSensitivityFlg()));

		return processCheckInfo;
	}
}
