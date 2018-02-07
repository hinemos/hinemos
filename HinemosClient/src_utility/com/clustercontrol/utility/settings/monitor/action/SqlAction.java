/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.action;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.monitor.conv.SqlConv;
import com.clustercontrol.utility.settings.monitor.xml.SqlMonitor;
import com.clustercontrol.utility.settings.monitor.xml.SqlMonitors;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

/**
 * SQL 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された SQL 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている SQL 監視情報と重複する場合はスキップされる。
 *
 * @version 6.1.0
 * @since 1.0.0
 *
 */
public class SqlAction extends AbstractMonitorAction<SqlMonitors> {
	public SqlAction() throws ConvertorException {
		super();
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
	protected List<MonitorInfo> createMonitorInfoList(SqlMonitors object) throws ConvertorException {
		return SqlConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfo> getFilterdMonitorList()
			throws HinemosUnknown_Exception, InvalidRole_Exception,
			InvalidUserPass_Exception, MonitorNotFound_Exception {
		return MonitorSettingEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getSqlList();
	}

	@Override
	protected SqlMonitors createCastorData(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
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