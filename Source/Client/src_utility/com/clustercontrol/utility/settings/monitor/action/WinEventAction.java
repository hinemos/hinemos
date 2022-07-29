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
import org.openapitools.client.model.WineventMonitorInfoResponse;

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
import com.clustercontrol.utility.settings.monitor.conv.WinEventConv;
import com.clustercontrol.utility.settings.monitor.xml.WinEventMonitor;
import com.clustercontrol.utility.settings.monitor.xml.WinEventMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * システムログ 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された システムログ 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている システムログ 監視情報と重複した場合はダイアログにて上書き/スキップをユーザに選択させる。
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
	public List<MonitorInfoResponse> createMonitorInfoList(WinEventMonitors winEventMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		return WinEventConv.createMonitorInfoList(winEventMonitors);
	}

	@Override
	protected List<MonitorInfoResponse> getFilterdMonitorList() throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed {
		List<MonitorInfoResponse> monitorInfoList = new ArrayList<MonitorInfoResponse>();
		List<WineventMonitorInfoResponse> winEventMonitorInfoList =  MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getWinEventList(null);
		
		for(WineventMonitorInfoResponse winEvent: winEventMonitorInfoList){
			MonitorInfoResponse monitorInfo = new MonitorInfoResponse();
			RestClientBeanUtil.convertBeanSimple(winEvent, monitorInfo);
			monitorInfoList.add(monitorInfo);
		}
		
		return monitorInfoList;
	}

	@Override
	protected WinEventMonitors createCastorData(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
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
