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
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
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
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.HttpCheckInfo;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.ws.monitor.MonitorNumericValueInfo;
import com.clustercontrol.ws.monitor.MonitorStringValueInfo;

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
	static private String SCHEMA_REVISION = "1";
	
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
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static HttpMonitors createHttpMonitors(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		HttpMonitors httpMonitors = new HttpMonitors();
		
		for (MonitorInfo monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo =  MonitorSettingEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMonitor(monitorInfo.getMonitorId());

			HttpMonitor httpMonitor = new HttpMonitor();
			httpMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));
			
			for (MonitorNumericValueInfo numericValueInfo : monitorInfo.getNumericValueInfo()) {
				if(numericValueInfo.getPriority() == PriorityConstant.TYPE_INFO ||
						numericValueInfo.getPriority() == PriorityConstant.TYPE_WARNING){
					if(numericValueInfo.getMonitorNumericType().contains("CHANGE")) {
						httpMonitor.addNumericChangeAmount(MonitorConv.createNumericChangeAmount(numericValueInfo));
					}
					else{
						httpMonitor.addNumericValue(MonitorConv.createNumericValue(numericValueInfo));
					}
				}
			}
			
			int orderNo = 0;
			for (MonitorStringValueInfo monitorStringValueInfo : monitorInfo.getStringValueInfo()) {
				httpMonitor.addStringValue(MonitorConv.createStringValue(monitorStringValueInfo, ++orderNo));
			}

			httpMonitor.setHttpInfo(createHttpInfo(monitorInfo.getHttpCheckInfo()));
			httpMonitors.addHttpMonitor(httpMonitor);
		}
		
		httpMonitors.setCommon(MonitorConv.versionDto2Xml());
		httpMonitors.setSchemaInfo(getSchemaVersion());
		
		return httpMonitors;
	}
	
	public static List<MonitorInfo> createMonitorInfoList(HttpMonitors httpMonitors) throws ConvertorException {
		List<MonitorInfo> monitorInfoList = new LinkedList<MonitorInfo>();
		
		for (HttpMonitor httpMonitor : httpMonitors.getHttpMonitor()) {
			logger.debug("Monitor Id : " + httpMonitor.getMonitor().getMonitorId());

			MonitorInfo monitorInfo = MonitorConv.createMonitorInfo(httpMonitor.getMonitor());
			
			if(monitorInfo.getMonitorType() == MonitorTypeConstant.TYPE_NUMERIC){
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
				if( monitorInfo.isChangeFlg() ==false && httpMonitor.getNumericChangeAmount().length == 0 ){
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
	private static HttpInfo createHttpInfo(HttpCheckInfo httpCheckInfo) {
		HttpInfo httpInfo = new HttpInfo();
		httpInfo.setMonitorTypeId("");
		httpInfo.setMonitorId(httpCheckInfo.getMonitorId());
		httpInfo.setProxyHost(httpCheckInfo.getProxyHost());

		httpInfo.setProxyPort(Objects.isNull(httpCheckInfo.getProxyPort())?0:httpCheckInfo.getProxyPort());
		httpInfo.setProxySet(Objects.isNull(httpCheckInfo.isProxySet())?false:httpCheckInfo.isProxySet());
		httpInfo.setRequestUrl(httpCheckInfo.getRequestUrl());
		httpInfo.setTimeout(httpCheckInfo.getTimeout());

		httpInfo.setUrlReplace(Objects.isNull(httpCheckInfo.isUrlReplace())?false:httpCheckInfo.isUrlReplace());

		return httpInfo;
	}
	
	/**
	 * <BR>
	 * 
	 * @return
	 */
	private static HttpCheckInfo createHttpCheckInfo(HttpInfo httpInfo) {
		HttpCheckInfo httpCheckInfo = new HttpCheckInfo();
		httpCheckInfo.setMonitorId(httpInfo.getMonitorId());
		httpCheckInfo.setMonitorTypeId(httpInfo.getMonitorTypeId());
		httpCheckInfo.setProxyHost(httpInfo.getProxyHost());
		httpCheckInfo.setProxyPort(httpInfo.getProxyPort());
		httpCheckInfo.setProxySet(httpInfo.getProxySet());
		httpCheckInfo.setRequestUrl(httpInfo.getRequestUrl());
		httpCheckInfo.setTimeout(httpInfo.getTimeout());
		httpCheckInfo.setUrlReplace(httpInfo.getUrlReplace());
		
		return httpCheckInfo;
	}
}