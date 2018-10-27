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
import com.clustercontrol.utility.settings.monitor.conv.LogcountConv;
import com.clustercontrol.utility.settings.monitor.xml.LogcountMonitor;
import com.clustercontrol.utility.settings.monitor.xml.LogcountMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorFilterInfo;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

/**
 * ログ件数 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された ログ件数 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている ログ件数 監視情報と重複する場合はスキップされる。
 *
 * @version 6.1.0
 * @since 6.1.0
 *
 */
public class LogcountAction extends AbstractMonitorAction<LogcountMonitors> {
	public LogcountAction() throws ConvertorException {
		super();
	}

	@Override
	public Class<LogcountMonitors> getDataClass() {
		return LogcountMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(LogcountMonitors object) {
		int res = LogcountConv.checkSchemaVersion(object.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				LogcountConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfo> createMonitorInfoList(LogcountMonitors object) throws ConvertorException {
		return LogcountConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfo> getFilterdMonitorList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		MonitorFilterInfo monitorFilterInfo = new MonitorFilterInfo();
		monitorFilterInfo.setMonitorTypeId(com.clustercontrol.bean.HinemosModuleConstant.MONITOR_LOGCOUNT);
		
		List<MonitorInfo> list = MonitorSettingEndpointWrapper
				.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMonitorListByCondition(monitorFilterInfo);
		
		return list;
	}

	@Override
	protected LogcountMonitors createCastorData(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return LogcountConv.createLogcountMonitors(monitorInfoList);
	}

	@Override
	protected void sort(LogcountMonitors object) {
		LogcountMonitor[] ms = object.getLogcountMonitor();
		Arrays.sort(
			ms,
			new Comparator<LogcountMonitor>() {
				@Override
				public int compare(LogcountMonitor obj1, LogcountMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setLogcountMonitor(ms);
	}
}