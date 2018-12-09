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
import com.clustercontrol.utility.settings.monitor.conv.WinServiceConv;
import com.clustercontrol.utility.settings.monitor.xml.WinServiceMonitor;
import com.clustercontrol.utility.settings.monitor.xml.WinServiceMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

/**
 * Windows サービス 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された Windows サービス 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている Windows サービス 監視情報と重複する場合はスキップされる。
 *
 * @version 6.1.0
 * @since 2.0.0
 *
 */
public class WinServiceAction extends AbstractMonitorAction<WinServiceMonitors> {
	public WinServiceAction() throws ConvertorException {
		super();
	}

	@Override
	public Class<WinServiceMonitors> getDataClass() {
		return WinServiceMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(WinServiceMonitors winServiceMonitors) {
		int res = WinServiceConv.checkSchemaVersion(winServiceMonitors.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				WinServiceConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfo> createMonitorInfoList(WinServiceMonitors winServiceMonitors) throws ConvertorException {
		return WinServiceConv.createMonitorInfoList(winServiceMonitors);
	}

	@Override
	protected List<MonitorInfo> getFilterdMonitorList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return MonitorSettingEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getWinServiceList();
	}

	@Override
	protected WinServiceMonitors createCastorData(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return WinServiceConv.createWinServiceMonitors(monitorInfoList);
	}

	@Override
	protected void sort(WinServiceMonitors object) {
		WinServiceMonitor[] ms = object.getWinServiceMonitor();
		Arrays.sort(
			ms,
			new Comparator<WinServiceMonitor>() {
				@Override
				public int compare(WinServiceMonitor obj1, WinServiceMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setWinServiceMonitor(ms);
	}
}
