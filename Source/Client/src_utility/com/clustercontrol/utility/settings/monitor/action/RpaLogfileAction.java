/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
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
import org.openapitools.client.model.RpaLogfileMonitorInfoResponse;

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
import com.clustercontrol.utility.settings.monitor.conv.RpaLogfileConv;
import com.clustercontrol.utility.settings.monitor.xml.RpaLogfileMonitor;
import com.clustercontrol.utility.settings.monitor.xml.RpaLogfileMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * RPAログファイル 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された RPAログファイル 監視情報をマネージャに反映させるクラス<br>
 *
 */
public class RpaLogfileAction extends AbstractMonitorAction<RpaLogfileMonitors> {
	public RpaLogfileAction() throws ConvertorException {
		super();
	}
	@Override
	public Class<RpaLogfileMonitors> getDataClass() {
		return RpaLogfileMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(RpaLogfileMonitors logfileMonitors) {
		int res = RpaLogfileConv.checkSchemaVersion(logfileMonitors.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				RpaLogfileConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfoResponse> createMonitorInfoList(RpaLogfileMonitors logfileMonitors) throws ConvertorException, HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, InvalidSetting, ParseException {
		return RpaLogfileConv.createMonitorInfoList(logfileMonitors);
	}

	@Override
	protected List<MonitorInfoResponse> getFilterdMonitorList() throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed {
		List<MonitorInfoResponse> monitorInfoList = new ArrayList<MonitorInfoResponse>();
		
		List<RpaLogfileMonitorInfoResponse> logfileMonitorInfoList =
				MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getRpaLogfileList();
		
		for(RpaLogfileMonitorInfoResponse logfile:logfileMonitorInfoList){
			MonitorInfoResponse monitorInfo = new MonitorInfoResponse();
			RestClientBeanUtil.convertBeanSimple(logfile, monitorInfo);
			monitorInfoList.add(monitorInfo);
		}
		
		return monitorInfoList;
	}

	@Override
	protected RpaLogfileMonitors createCastorData(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		return RpaLogfileConv.createRpaLogfileMonitors(monitorInfoList);
	}

	@Override
	protected void sort(RpaLogfileMonitors object) {
		RpaLogfileMonitor[] ms = object.getRpaLogfileMonitor();
		Arrays.sort(
			ms,
			new Comparator<RpaLogfileMonitor>() {
				@Override
				public int compare(RpaLogfileMonitor obj1, RpaLogfileMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setRpaLogfileMonitor(ms);
	}
}
