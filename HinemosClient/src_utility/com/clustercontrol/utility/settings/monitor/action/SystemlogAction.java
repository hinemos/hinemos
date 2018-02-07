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
import com.clustercontrol.utility.settings.monitor.conv.SystemlogConv;
import com.clustercontrol.utility.settings.monitor.xml.SyslogMonitor;
import com.clustercontrol.utility.settings.monitor.xml.SyslogMonitors;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

/**
 * システムログ 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された システムログ 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている システムログ 監視情報と重複する場合はスキップされる。
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
	public List<MonitorInfo> createMonitorInfoList(SyslogMonitors syslogMonitors) throws ConvertorException {
		return SystemlogConv.createMonitorInfoList(syslogMonitors);
	}

	@Override
	protected List<MonitorInfo> getFilterdMonitorList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return MonitorSettingEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getSystemlogList();
	}

	@Override
	protected SyslogMonitors createCastorData(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
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
