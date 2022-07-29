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

import org.openapitools.client.model.HttpScenarioMonitorInfoResponse;
import org.openapitools.client.model.MonitorInfoResponse;

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
import com.clustercontrol.utility.settings.monitor.conv.HttpScenarioConv;
import com.clustercontrol.utility.settings.monitor.xml.HttpScenarioMonitor;
import com.clustercontrol.utility.settings.monitor.xml.HttpScenarioMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * HTTP 監視(シナリオ)設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された HTTP 監視(シナリオ)情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている HTTP 監視(シナリオ)情報と重複した場合はダイアログにて上書き/スキップをユーザに選択させる。
 *
 * @version 6.1.0
 * @since 5.0.a
 *
 *
 *
 */
public class HttpScenarioAction extends AbstractMonitorAction<HttpScenarioMonitors> {
	public HttpScenarioAction() throws ConvertorException {
		super();
	}

	@Override
	public Class<HttpScenarioMonitors> getDataClass() {
		return HttpScenarioMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(HttpScenarioMonitors object) {
		int res = HttpScenarioConv.checkSchemaVersion(object.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				HttpScenarioConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfoResponse> createMonitorInfoList(HttpScenarioMonitors object) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		return HttpScenarioConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfoResponse> getFilterdMonitorList() throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed {
		List<HttpScenarioMonitorInfoResponse>  httpScenarioList = MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getHttpScenarioList(null);
		List<MonitorInfoResponse>  tmpList = new ArrayList<MonitorInfoResponse>();
		MonitorInfoResponse monitorInfoResponse = null;
		for(HttpScenarioMonitorInfoResponse info:httpScenarioList){
			monitorInfoResponse = new MonitorInfoResponse();
			RestClientBeanUtil.convertBean(info, monitorInfoResponse);
			tmpList.add(monitorInfoResponse);
		}
		return tmpList;
	}

	@Override
	protected HttpScenarioMonitors createCastorData(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		return HttpScenarioConv.createHttpScenarioMonitors(monitorInfoList);
	}

	@Override
	protected void sort(HttpScenarioMonitors object) {
		HttpScenarioMonitor[] ms = object.getHttpScenarioMonitor();
		Arrays.sort(
			ms,
			new Comparator<HttpScenarioMonitor>() {
				@Override
				public int compare(HttpScenarioMonitor obj1, HttpScenarioMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setHttpScenarioMonitor(ms);
	}
}
