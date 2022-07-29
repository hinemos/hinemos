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
import org.openapitools.client.model.MonitorStringValueInfoResponse;
import org.openapitools.client.model.SqlCheckInfoResponse;

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
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.settings.monitor.xml.SqlInfo;
import com.clustercontrol.utility.settings.monitor.xml.SqlMonitor;
import com.clustercontrol.utility.settings.monitor.xml.SqlMonitors;
import com.clustercontrol.utility.settings.monitor.xml.StringValue;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * SQL 監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 * @version 6.1.0
 * @since 1.0.0
 *
 *
 */
public class SqlConv {
	private final static Log logger = LogFactory.getLog(SqlConv.class);

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
	public static List<MonitorInfoResponse> createMonitorInfoList(SqlMonitors sqlMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		List<MonitorInfoResponse> monitorInfoList = new LinkedList<MonitorInfoResponse>();

		for (SqlMonitor sqlMonitor : sqlMonitors.getSqlMonitor()) {
			logger.debug("Monitor Id : " + sqlMonitor.getMonitor().getMonitorId());

			MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(sqlMonitor.getMonitor());

			if(monitorInfo.getMonitorType() == MonitorInfoResponse.MonitorTypeEnum.NUMERIC){
				for (NumericValue numericValue : sqlMonitor.getNumericValue()) {
					if(numericValue.getPriority() == PriorityConstant.TYPE_INFO ||
							numericValue.getPriority() == PriorityConstant.TYPE_WARNING){
						monitorInfo.getNumericValueInfo().add(MonitorConv.createMonitorNumericValueInfo(numericValue));
					}
				}
				if(monitorInfo.getNumericValueInfo().size() != 2){
					throw new ConvertorException(
							sqlMonitor.getMonitor().getMonitorId()
							+ " " + Messages.getString("SettingTools.NumericValueInvalid"));
				}

				for (NumericChangeAmount changeValue : sqlMonitor.getNumericChangeAmount()){
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
				if( monitorInfo.getChangeFlg() ==false && sqlMonitor.getNumericChangeAmount().length == 0 ){
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
			}
			else{
				StringValue[] values = sqlMonitor.getStringValue();
				MonitorConv.sort(values);
				for (StringValue stringValue : values) {
					monitorInfo.getStringValueInfo().add(MonitorConv.createMonitorStringValueInfo(stringValue));
				}
			}

			monitorInfo.setSqlCheckInfo(createSqlCheckInfo(sqlMonitor.getSqlInfo()));
			monitorInfoList.add(monitorInfo);
		}

		return monitorInfoList;
	}

	/**
	 * DTO から、Castor で作成した形式の リソース 監視設定情報へ変換する<BR>
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
	public static SqlMonitors createSqlMonitors(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		SqlMonitors sqlMonitors = new SqlMonitors();

		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorsettingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			SqlMonitor sqlMonitor = new SqlMonitor();
			sqlMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			for (MonitorNumericValueInfoResponse numericValueInfo : monitorInfo.getNumericValueInfo() ) {
				if(numericValueInfo.getPriority() == MonitorNumericValueInfoResponse.PriorityEnum.INFO ||
						numericValueInfo.getPriority() == MonitorNumericValueInfoResponse.PriorityEnum.WARNING){
					if(numericValueInfo.getMonitorNumericType().equals(MonitorNumericValueInfoResponse.MonitorNumericTypeEnum.CHANGE)) {
						sqlMonitor.addNumericChangeAmount(MonitorConv.createNumericChangeAmount(monitorInfo.getMonitorId(), numericValueInfo));
					}
					else{
						sqlMonitor.addNumericValue(MonitorConv.createNumericValue(monitorInfo.getMonitorId(), numericValueInfo));
					}
				}
			}

			int orderNo = 0;
			for (MonitorStringValueInfoResponse monitorStringValueInfo : monitorInfo.getStringValueInfo()) {
				sqlMonitor.addStringValue(MonitorConv.createStringValue(monitorInfo.getMonitorId(),monitorStringValueInfo, ++orderNo));
			}

			sqlMonitor.setSqlInfo(createSqlInfo(monitorInfo.getMonitorId(),monitorInfo.getSqlCheckInfo()));
			sqlMonitors.addSqlMonitor(sqlMonitor);
		}

		sqlMonitors.setCommon(MonitorConv.versionDto2Xml());
		sqlMonitors.setSchemaInfo(getSchemaVersion());

		return sqlMonitors;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static SqlInfo createSqlInfo(String monitorId, SqlCheckInfoResponse sqlCheckInfo) {
		SqlInfo sqlInfo = new SqlInfo();
		sqlInfo.setMonitorTypeId("");

		sqlInfo.setMonitorId(monitorId);
		sqlInfo.setConnectionUrl(sqlCheckInfo.getConnectionUrl());
		sqlInfo.setJdbcDriver(sqlCheckInfo.getJdbcDriver());
		sqlInfo.setPassword(sqlCheckInfo.getPassword());
		sqlInfo.setQuery(sqlCheckInfo.getQuery());
		sqlInfo.setUser(sqlCheckInfo.getUser());

		return sqlInfo;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static SqlCheckInfoResponse createSqlCheckInfo(SqlInfo sqlInfo) {
		SqlCheckInfoResponse sqlCheckInfo = new SqlCheckInfoResponse();
		sqlCheckInfo.setConnectionUrl(sqlInfo.getConnectionUrl());
		sqlCheckInfo.setJdbcDriver(sqlInfo.getJdbcDriver());
		sqlCheckInfo.setPassword(sqlInfo.getPassword());
		sqlCheckInfo.setQuery(sqlInfo.getQuery());
		sqlCheckInfo.setUser(sqlInfo.getUser());

		return sqlCheckInfo;
	}
}
