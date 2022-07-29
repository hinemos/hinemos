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

import org.openapitools.client.model.LogfileMonitorInfoResponse;
import org.openapitools.client.model.MonitorInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.sdml.util.SdmlClientUtil;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.monitor.conv.LogfileConv;
import com.clustercontrol.utility.settings.monitor.xml.LogfileMonitor;
import com.clustercontrol.utility.settings.monitor.xml.LogfileMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;

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
	public List<MonitorInfoResponse> createMonitorInfoList(LogfileMonitors logfileMonitors) throws ConvertorException, HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, InvalidSetting, ParseException {
		return LogfileConv.createMonitorInfoList(logfileMonitors);
	}

	@Override
	protected List<MonitorInfoResponse> getFilterdMonitorList() throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed {
		List<MonitorInfoResponse> monitorInfoList = new ArrayList<MonitorInfoResponse>();
		
		List<LogfileMonitorInfoResponse> logfileMonitorInfoList =
				MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getLogfileList(null);
		
		for(LogfileMonitorInfoResponse logfile:logfileMonitorInfoList){
			MonitorInfoResponse monitorInfo = new MonitorInfoResponse();
			RestClientBeanUtil.convertBeanSimple(logfile, monitorInfo);
			if (SdmlClientUtil.isCreatedBySdml(monitorInfo)) {
				// SDMLで自動作成された監視は除外する
				continue;
			}
			monitorInfoList.add(monitorInfo);
		}
		
		return monitorInfoList;
	}

	@Override
	protected LogfileMonitors createCastorData(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
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
