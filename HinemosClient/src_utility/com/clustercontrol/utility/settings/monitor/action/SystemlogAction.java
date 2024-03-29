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
import org.openapitools.client.model.SystemlogMonitorInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.monitor.conv.SystemlogConv;
import com.clustercontrol.utility.settings.monitor.xml.SyslogMonitor;
import com.clustercontrol.utility.settings.monitor.xml.SyslogMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * システムログ 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された システムログ 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている システムログ 監視情報と重複した場合はダイアログにて上書き/スキップをユーザに選択させる。
 *
 * @version 6.1.0
 * @since 2.0.0
 */
public class SystemlogAction extends AbstractMonitorAction<SyslogMonitors> {
	public SystemlogAction() throws ConvertorException {
		super();
	}

	@Override
	public Class<SyslogMonitors> getDataClass() {
		return SyslogMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(SyslogMonitors syslogMonitors) {
		int res = SystemlogConv.checkSchemaVersion(syslogMonitors.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				SystemlogConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfoResponse> createMonitorInfoList(SyslogMonitors syslogMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		return SystemlogConv.createMonitorInfoList(syslogMonitors);
	}

	@Override
	protected List<MonitorInfoResponse> getFilterdMonitorList() throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed {
		List<SystemlogMonitorInfoResponse> systemLogMonitorInfoList =
				MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getSystemlogList(null);
		
		List<MonitorInfoResponse> monitorInfoList = new ArrayList<MonitorInfoResponse>();
		
		for(SystemlogMonitorInfoResponse systemLogMonitorInfo:systemLogMonitorInfoList){
			MonitorInfoResponse monitorInfo = new MonitorInfoResponse();
			RestClientBeanUtil.convertBeanSimple(systemLogMonitorInfo, monitorInfo);
			monitorInfoList.add(monitorInfo);
		}
		
		return monitorInfoList;
	}

	@Override
	protected SyslogMonitors createCastorData(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		return SystemlogConv.createSyslogMonitors(monitorInfoList);
	}

	@Override
	protected void sort(SyslogMonitors object) {
		SyslogMonitor[] ms = object.getSyslogMonitor();
		Arrays.sort(
			ms,
			new Comparator<SyslogMonitor>() {
				@Override
				public int compare(SyslogMonitor obj1, SyslogMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setSyslogMonitor(ms);
	}
}
