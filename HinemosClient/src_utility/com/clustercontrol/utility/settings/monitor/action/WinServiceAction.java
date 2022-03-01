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
import org.openapitools.client.model.WinserviceMonitorInfoResponse;

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
import com.clustercontrol.utility.settings.monitor.conv.WinServiceConv;
import com.clustercontrol.utility.settings.monitor.xml.WinServiceMonitor;
import com.clustercontrol.utility.settings.monitor.xml.WinServiceMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * Windows サービス 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された Windows サービス 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている Windows サービス 監視情報と重複した場合はダイアログにて上書き/スキップをユーザに選択させる。
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
	public List<MonitorInfoResponse> createMonitorInfoList(WinServiceMonitors winServiceMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		return WinServiceConv.createMonitorInfoList(winServiceMonitors);
	}

	@Override
	protected List<MonitorInfoResponse> getFilterdMonitorList() throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed {
		List<MonitorInfoResponse> monitorInfoList = new ArrayList<MonitorInfoResponse>();
		List<WinserviceMonitorInfoResponse> winServiceList = MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getWinServiceList(null);
		
		for(WinserviceMonitorInfoResponse winservice:winServiceList){
			MonitorInfoResponse monitorInfo = new MonitorInfoResponse();
			RestClientBeanUtil.convertBeanSimple(winservice, monitorInfo);
			monitorInfoList.add(monitorInfo);
		}
		
		return monitorInfoList;
	}

	@Override
	protected WinServiceMonitors createCastorData(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
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
