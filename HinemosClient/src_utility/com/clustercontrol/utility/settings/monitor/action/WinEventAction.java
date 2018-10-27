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
import com.clustercontrol.utility.settings.monitor.conv.WinEventConv;
import com.clustercontrol.utility.settings.monitor.xml.WinEventMonitor;
import com.clustercontrol.utility.settings.monitor.xml.WinEventMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;
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
 * @since 2.2.0
 *
 */
public class WinEventAction extends AbstractMonitorAction<WinEventMonitors> {
	public WinEventAction() throws ConvertorException {
		super();
	}

	@Override
	public Class<WinEventMonitors> getDataClass() {
		return WinEventMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(WinEventMonitors winEventMonitors) {
		int res = WinEventConv.checkSchemaVersion(winEventMonitors.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				WinEventConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfo> createMonitorInfoList(WinEventMonitors winEventMonitors) throws ConvertorException {
		return WinEventConv.createMonitorInfoList(winEventMonitors);
	}

	@Override
	protected List<MonitorInfo> getFilterdMonitorList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return MonitorSettingEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getWinEventList();
	}

	@Override
	protected WinEventMonitors createCastorData(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return WinEventConv.createWinEventMonitors(monitorInfoList);
	}

	@Override
	protected void sort(WinEventMonitors object) {
		WinEventMonitor[] ms = object.getWinEventMonitor();
		Arrays.sort(
			ms,
			new Comparator<WinEventMonitor>() {
				@Override
				public int compare(WinEventMonitor obj1, WinEventMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setWinEventMonitor(ms);
	}
}
