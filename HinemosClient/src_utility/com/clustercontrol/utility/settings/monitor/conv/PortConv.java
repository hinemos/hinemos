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
import org.openapitools.client.model.PortCheckInfoResponse;
import org.openapitools.client.model.PortCheckInfoResponse.ServiceIdEnum;

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
import com.clustercontrol.utility.settings.monitor.xml.PortInfo;
import com.clustercontrol.utility.settings.monitor.xml.PortMonitor;
import com.clustercontrol.utility.settings.monitor.xml.PortMonitors;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.util.OpenApiEnumConverter;
import com.clustercontrol.utility.util.UtilityManagerUtil;

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
	 * Castor で作成した形式の リソース 監視設定情報を DTO へ変換する<BR>
	 *
	 * @param
	 * @return
	 * @throws ConvertorException
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 * @throws ParseException 
	 */
	public static List<MonitorInfoResponse> createMonitorInfoList(PortMonitors portMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		List<MonitorInfoResponse> monitorInfoList = new LinkedList<MonitorInfoResponse>();

		for (PortMonitor portMonitor : portMonitors.getPortMonitor()) {
			logger.debug("Monitor Id : " + portMonitor.getMonitor().getMonitorId());

			MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(portMonitor.getMonitor());

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
			if( monitorInfo.getChangeFlg() ==false && portMonitor.getNumericChangeAmount().length == 0 ){
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
	 * @throws ParseException 
	 * @throws RestConnectFailed 
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static PortMonitors createPortMonitors(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, ParseException, RestConnectFailed {
		PortMonitors portMonitorList = new PortMonitors();

		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorsettingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			PortMonitor portMonitor = new PortMonitor();
			portMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			for (MonitorNumericValueInfoResponse numericValueInfo : monitorInfo.getNumericValueInfo()) {
				if(numericValueInfo.getPriority() == PriorityEnum.INFO ||
						numericValueInfo.getPriority() == PriorityEnum.WARNING){
					if(numericValueInfo.getMonitorNumericType().name().equals("CHANGE")) {
						portMonitor.addNumericChangeAmount(MonitorConv.createNumericChangeAmount(monitorInfo.getMonitorId(),numericValueInfo));
					}
					else{
						portMonitor.addNumericValue(MonitorConv.createNumericValue(monitorInfo.getMonitorId(),numericValueInfo));
					}
				}
			}

			portMonitor.setPortInfo(createPortInfo(monitorInfo));
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
	private static PortInfo createPortInfo(MonitorInfoResponse monitorInfo) {
		PortInfo portInfo = new PortInfo();
		portInfo.setMonitorTypeId("");
		portInfo.setMonitorId(monitorInfo.getMonitorId());
		portInfo.setPortNo(monitorInfo.getPortCheckInfo().getPortNo());
		portInfo.setRunCount(monitorInfo.getPortCheckInfo().getRunCount());
		portInfo.setRunInterval(monitorInfo.getPortCheckInfo().getRunInterval());
		String serviceIdString = OpenApiEnumConverter.enumToString(monitorInfo.getPortCheckInfo().getServiceId());
		portInfo.setServiceId(serviceIdString);
		portInfo.setTimeout(monitorInfo.getPortCheckInfo().getTimeout());

		return portInfo;
	}

	/**
	 * <BR>
	 *
	 * @return
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	private static PortCheckInfoResponse createPortCheckInfo(PortInfo portInfo) throws InvalidSetting, HinemosUnknown {
		PortCheckInfoResponse portCheckInfo = new PortCheckInfoResponse();

		portCheckInfo.setPortNo(portInfo.getPortNo());
		portCheckInfo.setRunCount(portInfo.getRunCount());
		portCheckInfo.setRunInterval(portInfo.getRunInterval());
		ServiceIdEnum serviceIdEnum = OpenApiEnumConverter.stringToEnum(portInfo.getServiceId(), ServiceIdEnum.class);
		portCheckInfo.setServiceId(serviceIdEnum);
		portCheckInfo.setTimeout(portInfo.getTimeout());

		return portCheckInfo;
	}
}