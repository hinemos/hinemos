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

import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.monitor.conv.LogfileConv;
import com.clustercontrol.utility.settings.monitor.xml.LogfileMonitor;
import com.clustercontrol.utility.settings.monitor.xml.LogfileMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

/**
 * ログファイル 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された ログファイル 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている ログファイル 監視情報と重複する場合はスキップされる。
 *
 * @version 6.1.0
 * @since 2.0.0
 */
public class LogfileAction extends AbstractMonitorAction<LogfileMonitors> {
	public LogfileAction() throws ConvertorException {
		super();
	}
	@Override
	public Class<LogfileMonitors> getDataClass() {
		return LogfileMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(LogfileMonitors logfileMonitors) {
		int res = LogfileConv.checkSchemaVersion(logfileMonitors.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				LogfileConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfo> createMonitorInfoList(LogfileMonitors logfileMonitors) throws ConvertorException, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return LogfileConv.createMonitorInfoList(logfileMonitors);
	}

	@Override
	protected List<MonitorInfo> getFilterdMonitorList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return MonitorSettingEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getLogfileList();
	}

	@Override
	protected LogfileMonitors createCastorData(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return LogfileConv.createLogfileMonitors(monitorInfoList);
	}

	@Override
	protected void sort(LogfileMonitors object) {
		LogfileMonitor[] ms = object.getLogfileMonitor();
		Arrays.sort(
			ms,
			new Comparator<LogfileMonitor>() {
				@Override
				public int compare(LogfileMonitor obj1, LogfileMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setLogfileMonitor(ms);
	}
}
