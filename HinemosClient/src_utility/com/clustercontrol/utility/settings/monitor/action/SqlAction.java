/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.action;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.SqlNumericMonitorInfoResponse;
import org.openapitools.client.model.SqlStringMonitorInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.monitor.conv.SqlConv;
import com.clustercontrol.utility.settings.monitor.xml.SqlMonitor;
import com.clustercontrol.utility.settings.monitor.xml.SqlMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * SQL 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された SQL 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている SQL 監視情報と重複した場合はダイアログにて上書き/スキップをユーザに選択させる。
 *
 * @version 6.1.0
 * @since 1.0.0
 *
 */
public class SqlAction extends AbstractMonitorAction<SqlMonitors> {
	public SqlAction() throws ConvertorException {
		super();
		this.targetTitle = Messages.getString("monitor.sql");
	}

	@Override
	protected boolean checkSchemaVersionScope(SqlMonitors object) {
		int res = SqlConv.checkSchemaVersion(object.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				SqlConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	protected Class<SqlMonitors> getDataClass() {
		return SqlMonitors.class;
	}

	@Override
	protected List<MonitorInfoResponse> createMonitorInfoList(SqlMonitors object) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		return SqlConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfoResponse> getFilterdMonitorList()
			throws HinemosUnknown, InvalidRole,
			InvalidUserPass, MonitorNotFound, RestConnectFailed {
		List<MonitorInfoResponse> retList = new ArrayList<MonitorInfoResponse>();
		List<SqlStringMonitorInfoResponse> resDtoListString = MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getSqlStringList(null) ;
		for(SqlStringMonitorInfoResponse recSrc :resDtoListString){
			MonitorInfoResponse recDest = new MonitorInfoResponse();
			RestClientBeanUtil.convertBean(recSrc, recDest);
			retList.add(recDest);
		}
		List<SqlNumericMonitorInfoResponse> resDtoListNumeric = MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getSqlNumericList(null);
		for(SqlNumericMonitorInfoResponse recSrc :resDtoListNumeric){
			MonitorInfoResponse recDest = new MonitorInfoResponse();
			RestClientBeanUtil.convertBean(recSrc, recDest);
			retList.add(recDest);
		}
		return retList;
	}

	@Override
	protected SqlMonitors createCastorData(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		return SqlConv.createSqlMonitors(monitorInfoList);
	}

	@Override
	protected void sort(SqlMonitors object) {
		SqlMonitor[] ms = object.getSqlMonitor();
		Arrays.sort(
			ms,
			new Comparator<SqlMonitor>() {
				@Override
				public int compare(SqlMonitor obj1, SqlMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setSqlMonitor(ms);
	}
}