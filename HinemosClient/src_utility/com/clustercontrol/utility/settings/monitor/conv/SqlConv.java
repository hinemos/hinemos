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
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
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
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.ws.monitor.MonitorNumericValueInfo;
import com.clustercontrol.ws.monitor.MonitorStringValueInfo;
import com.clustercontrol.ws.monitor.SqlCheckInfo;

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
	 * Castor で作成した形式の リソース 監視設定情報を DTO へ変換する<BR>
	 *
	 * @param
	 * @return
	 * @throws ConvertorException
	 */
	public static List<MonitorInfo> createMonitorInfoList(SqlMonitors sqlMonitors) throws ConvertorException {
		List<MonitorInfo> monitorInfoList = new LinkedList<MonitorInfo>();

		for (SqlMonitor sqlMonitor : sqlMonitors.getSqlMonitor()) {
			logger.debug("Monitor Id : " + sqlMonitor.getMonitor().getMonitorId());

			MonitorInfo monitorInfo = MonitorConv.createMonitorInfo(sqlMonitor.getMonitor());

			if(monitorInfo.getMonitorType() == MonitorTypeConstant.TYPE_NUMERIC){
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
				if( monitorInfo.isChangeFlg() ==false && sqlMonitor.getNumericChangeAmount().length == 0 ){
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
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static SqlMonitors createSqlMonitors(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		SqlMonitors sqlMonitors = new SqlMonitors();

		for (MonitorInfo monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorSettingEndpointWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			SqlMonitor sqlMonitor = new SqlMonitor();
			sqlMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			for (MonitorNumericValueInfo numericValueInfo : monitorInfo.getNumericValueInfo()) {
				if(numericValueInfo.getPriority() == PriorityConstant.TYPE_INFO ||
						numericValueInfo.getPriority() == PriorityConstant.TYPE_WARNING){
					if(numericValueInfo.getMonitorNumericType().contains("CHANGE")) {
						sqlMonitor.addNumericChangeAmount(MonitorConv.createNumericChangeAmount(numericValueInfo));
					}
					else{
						sqlMonitor.addNumericValue(MonitorConv.createNumericValue(numericValueInfo));
					}
				}
			}

			int orderNo = 0;
			for (MonitorStringValueInfo monitorStringValueInfo : monitorInfo.getStringValueInfo()) {
				sqlMonitor.addStringValue(MonitorConv.createStringValue(monitorStringValueInfo, ++orderNo));
			}

			sqlMonitor.setSqlInfo(createSqlInfo(monitorInfo.getSqlCheckInfo()));
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
	private static SqlInfo createSqlInfo(SqlCheckInfo sqlCheckInfo) {
		SqlInfo sqlInfo = new SqlInfo();
		sqlInfo.setMonitorTypeId("");

		sqlInfo.setMonitorId(sqlCheckInfo.getMonitorId());
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
	private static SqlCheckInfo createSqlCheckInfo(SqlInfo sqlInfo) {
		SqlCheckInfo sqlCheckInfo = new SqlCheckInfo();
		sqlCheckInfo.setMonitorTypeId("");

		sqlCheckInfo.setMonitorId(sqlInfo.getMonitorId());
		sqlCheckInfo.setConnectionUrl(sqlInfo.getConnectionUrl());
		sqlCheckInfo.setJdbcDriver(sqlInfo.getJdbcDriver());
		sqlCheckInfo.setPassword(sqlInfo.getPassword());
		sqlCheckInfo.setQuery(sqlInfo.getQuery());
		sqlCheckInfo.setUser(sqlInfo.getUser());

		return sqlCheckInfo;
	}
}
