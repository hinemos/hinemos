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
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.HttpCheckInfoResponse;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorInfoResponse.MonitorTypeEnum;
import org.openapitools.client.model.MonitorNumericValueInfoResponse;
import org.openapitools.client.model.MonitorNumericValueInfoResponse.MonitorNumericTypeEnum;
import org.openapitools.client.model.MonitorNumericValueInfoResponse.PriorityEnum;
import org.openapitools.client.model.MonitorStringValueInfoResponse;

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
import com.clustercontrol.utility.settings.monitor.xml.HttpInfo;
import com.clustercontrol.utility.settings.monitor.xml.HttpMonitor;
import com.clustercontrol.utility.settings.monitor.xml.HttpMonitors;
import com.clustercontrol.utility.settings.monitor.xml.NumericChangeAmount;
import com.clustercontrol.utility.settings.monitor.xml.NumericValue;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.settings.monitor.xml.StringValue;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * HTTP 監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 * 
 * @version 6.1.0
 * @since 1.0.0
 * 
 * 
 */
public class HttpConv {
	private final static Log logger = LogFactory.getLog(HttpConv.class);
	
	static private String SCHEMA_TYPE = "I";
	static private String SCHEMA_VERSION = "1";
	static private String SCHEMA_REVISION = "2";
	
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
	
	/*スキーマのバージョンチェック*/
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
	 * <BR>
	 * 
	 * @return
	 * @throws RestConnectFailed 
	 * @throws ParseException 
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static HttpMonitors createHttpMonitors(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		HttpMonitors httpMonitors = new HttpMonitors();
		
		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo =  MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMonitor(monitorInfo.getMonitorId());

			HttpMonitor httpMonitor = new HttpMonitor();
			httpMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));
			
			for (MonitorNumericValueInfoResponse numericValueInfo : monitorInfo.getNumericValueInfo()) {
				if(numericValueInfo.getPriority() == PriorityEnum.INFO ||
						numericValueInfo.getPriority() == PriorityEnum.WARNING){
					if(numericValueInfo.getMonitorNumericType().name().equals("CHANGE")) {
						httpMonitor.addNumericChangeAmount(MonitorConv.createNumericChangeAmount(monitorInfo.getMonitorId(),numericValueInfo));
					}
					else{
						httpMonitor.addNumericValue(MonitorConv.createNumericValue(monitorInfo.getMonitorId(),numericValueInfo));
					}
				}
			}
			
			int orderNo = 0;
			for (MonitorStringValueInfoResponse monitorStringValueInfo : monitorInfo.getStringValueInfo()) {
				httpMonitor.addStringValue(MonitorConv.createStringValue(monitorInfo.getMonitorId(),monitorStringValueInfo, ++orderNo));
			}

			httpMonitor.setHttpInfo(createHttpInfo(monitorInfo));
			httpMonitors.addHttpMonitor(httpMonitor);
		}
		
		httpMonitors.setCommon(MonitorConv.versionDto2Xml());
		httpMonitors.setSchemaInfo(getSchemaVersion());
		
		return httpMonitors;
	}
	
	public static List<MonitorInfoResponse> createMonitorInfoList(HttpMonitors httpMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		List<MonitorInfoResponse> monitorInfoList = new LinkedList<MonitorInfoResponse>();
		
		for (HttpMonitor httpMonitor : httpMonitors.getHttpMonitor()) {
			logger.debug("Monitor Id : " + httpMonitor.getMonitor().getMonitorId());

			MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(httpMonitor.getMonitor());
			
			if(monitorInfo.getMonitorType() == MonitorTypeEnum.NUMERIC){
				for (NumericValue numericValue : httpMonitor.getNumericValue()) {
					if(numericValue.getPriority() == PriorityConstant.TYPE_INFO ||
							numericValue.getPriority() == PriorityConstant.TYPE_WARNING){
						monitorInfo.getNumericValueInfo().add(MonitorConv.createMonitorNumericValueInfo(numericValue));
					}
				}
				if(monitorInfo.getNumericValueInfo().size() != 2){
					throw new ConvertorException(
							httpMonitor.getMonitor().getMonitorId()
							+ " " + Messages.getString("SettingTools.NumericValueInvalid"));
				}

				for (NumericChangeAmount changeValue : httpMonitor.getNumericChangeAmount()){
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
				if( monitorInfo.getChangeFlg() ==false && httpMonitor.getNumericChangeAmount().length == 0 ){
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
			}
			else{
				StringValue[] values = httpMonitor.getStringValue();
				MonitorConv.sort(values);
				for (StringValue stringValue : values) {
					monitorInfo.getStringValueInfo().add(MonitorConv.createMonitorStringValueInfo(stringValue));
				}
			}

			monitorInfo.setHttpCheckInfo(createHttpCheckInfo(httpMonitor.getHttpInfo()));
			monitorInfoList.add(monitorInfo);
		}
		
		return monitorInfoList;
	}
	
	/**
	 * <BR>
	 * 
	 * @return
	 */
	private static HttpInfo createHttpInfo(MonitorInfoResponse httpCheckInfo) {
		HttpInfo httpInfo = new HttpInfo();
		httpInfo.setMonitorTypeId(httpCheckInfo.getMonitorType().getValue());
		httpInfo.setMonitorId(httpCheckInfo.getMonitorId());
		httpInfo.setProxyHost(httpCheckInfo.getHttpCheckInfo().getProxyHost());

		httpInfo.setProxyPort(Objects.isNull(httpCheckInfo.getHttpCheckInfo().getProxyPort())?0:httpCheckInfo.getHttpCheckInfo().getProxyPort());
		httpInfo.setProxySet(Objects.isNull(httpCheckInfo.getHttpCheckInfo().getProxySet())?false:httpCheckInfo.getHttpCheckInfo().getProxySet());
		httpInfo.setRequestUrl(httpCheckInfo.getHttpCheckInfo().getRequestUrl());
		httpInfo.setTimeout(httpCheckInfo.getHttpCheckInfo().getTimeout());

		httpInfo.setUrlReplace(Objects.isNull(httpCheckInfo.getHttpCheckInfo().getUrlReplace())?false:httpCheckInfo.getHttpCheckInfo().getUrlReplace());

		return httpInfo;
	}
	
	/**
	 * <BR>
	 * 
	 * @return
	 */
	private static HttpCheckInfoResponse createHttpCheckInfo(HttpInfo httpInfo) {
		HttpCheckInfoResponse httpCheckInfo = new HttpCheckInfoResponse();
		httpCheckInfo.setProxySet(httpInfo.getProxySet());
		if(httpInfo.getProxySet()){
			httpCheckInfo.setProxyHost(httpInfo.getProxyHost());
			httpCheckInfo.setProxyPort(httpInfo.getProxyPort());
		}
		httpCheckInfo.setRequestUrl(httpInfo.getRequestUrl());
		httpCheckInfo.setTimeout(httpInfo.getTimeout());
		httpCheckInfo.setUrlReplace(httpInfo.getUrlReplace());
		
		return httpCheckInfo;
	}
}