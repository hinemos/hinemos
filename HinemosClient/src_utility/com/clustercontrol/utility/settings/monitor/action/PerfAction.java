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
import org.openapitools.client.model.PerformanceMonitorInfoResponse;

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
import com.clustercontrol.utility.settings.monitor.conv.PerfConv;
import com.clustercontrol.utility.settings.monitor.xml.PerfMonitor;
import com.clustercontrol.utility.settings.monitor.xml.PerfMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * リソース 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された リソース  監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている リソース 監視情報と重複した場合はダイアログにて上書き/スキップをユーザに選択させる。
 *
 * @version 6.1.0
 * @since 1.0.0
 *
 */
public class PerfAction extends AbstractMonitorAction<PerfMonitors> {
	public PerfAction() throws ConvertorException {
		super();
	}

	@Override
	public Class<PerfMonitors> getDataClass() {
		return PerfMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(PerfMonitors object) {
		int res = PerfConv.checkSchemaVersion(object.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				PerfConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfoResponse> createMonitorInfoList(PerfMonitors object) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		return PerfConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfoResponse> getFilterdMonitorList() throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed {
		List<MonitorInfoResponse> monitorInfoList = new ArrayList<MonitorInfoResponse>();
		List<PerformanceMonitorInfoResponse> perfMonitorInfo = MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getPerformanceList(null);
		
		for(PerformanceMonitorInfoResponse perf:perfMonitorInfo){
			MonitorInfoResponse monitorInfo = new MonitorInfoResponse();
			RestClientBeanUtil.convertBeanSimple(perf, monitorInfo);
			monitorInfoList.add(monitorInfo);
		}
		
		return monitorInfoList;
	}

	@Override
	protected PerfMonitors createCastorData(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		return PerfConv.createPerfMonitors(monitorInfoList);
	}

	@Override
	protected void sort(PerfMonitors object) {
		PerfMonitor[] ms = object.getPerfMonitor();
		Arrays.sort(
			ms,
			new Comparator<PerfMonitor>() {
				@Override
				public int compare(PerfMonitor obj1, PerfMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setPerfMonitor(ms);
	}
}
