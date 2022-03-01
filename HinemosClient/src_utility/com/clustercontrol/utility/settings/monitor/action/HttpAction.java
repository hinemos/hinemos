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

import org.openapitools.client.model.HttpNumericMonitorInfoResponse;
import org.openapitools.client.model.HttpStringMonitorInfoResponse;
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
import com.clustercontrol.utility.settings.monitor.conv.HttpConv;
import com.clustercontrol.utility.settings.monitor.xml.HttpMonitor;
import com.clustercontrol.utility.settings.monitor.xml.HttpMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * HTTP 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された HTTP 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている HTTP 監視情報と重複した場合はダイアログにて上書き/スキップをユーザに選択させる。
 *
 * @version 6.1.0
 * @since 1.0.0
 *
 *
 *
 */
public class HttpAction extends AbstractMonitorAction<HttpMonitors> {
	public HttpAction() throws ConvertorException {
		super();
	}

	@Override
	public Class<HttpMonitors> getDataClass() {
		return HttpMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(HttpMonitors object) {
		int res = HttpConv.checkSchemaVersion(object.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				HttpConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfoResponse> createMonitorInfoList(HttpMonitors object) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		return HttpConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfoResponse> getFilterdMonitorList() throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed {
		List<MonitorInfoResponse> tmpList = new ArrayList<MonitorInfoResponse>();
		MonitorInfoResponse monitorInfoResponse = null;
		// HttpNumericMonitorInfoを取得しMonitorInfoResponseに変換
		List<HttpNumericMonitorInfoResponse> numericMonitorInfoList = 
				MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getHttpNumericList(null);
		for(HttpNumericMonitorInfoResponse info: numericMonitorInfoList){
			monitorInfoResponse = new MonitorInfoResponse();
			RestClientBeanUtil.convertBean(info, monitorInfoResponse);
			tmpList.add(monitorInfoResponse);
		}
		// HttpStringMonitorInfoを取得しMonitorInfoResponseに変換
		List<HttpStringMonitorInfoResponse> stringMonitorInfoList =
				MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getHttpStringList(null);
		for(HttpStringMonitorInfoResponse info: stringMonitorInfoList){
			monitorInfoResponse = new MonitorInfoResponse();
			RestClientBeanUtil.convertBean(info, monitorInfoResponse);
			tmpList.add(monitorInfoResponse);
		}
		return tmpList;
	}

	@Override
	protected HttpMonitors createCastorData(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		return HttpConv.createHttpMonitors(monitorInfoList);
	}

	@Override
	protected void sort(HttpMonitors object) {
		HttpMonitor[] ms = object.getHttpMonitor();
		Arrays.sort(
			ms,
			new Comparator<HttpMonitor>() {
				@Override
				public int compare(HttpMonitor obj1, HttpMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setHttpMonitor(ms);

	}
}
