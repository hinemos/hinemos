/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.monitor.conv.HttpScenarioConv;
import com.clustercontrol.utility.settings.monitor.xml.HttpScenarioMonitor;
import com.clustercontrol.utility.settings.monitor.xml.HttpScenarioMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

/**
 * HTTP 監視(シナリオ)設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された HTTP 監視(シナリオ)情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている HTTP 監視(シナリオ)情報と重複する場合はスキップされる。
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
	public List<MonitorInfo> createMonitorInfoList(HttpScenarioMonitors object) throws ConvertorException {
		return HttpScenarioConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfo> getFilterdMonitorList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		List<MonitorInfo> tmpList = MonitorSettingEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getHttpList();
		for(MonitorInfo info: new ArrayList<>(tmpList)){
			if(info.getMonitorType() != MonitorTypeConstant.TYPE_SCENARIO){
				tmpList.remove(info);
			}
		}
		return tmpList;
	}

	@Override
	protected HttpScenarioMonitors createCastorData(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
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
