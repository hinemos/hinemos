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
import com.clustercontrol.utility.settings.monitor.conv.HttpConv;
import com.clustercontrol.utility.settings.monitor.xml.HttpMonitor;
import com.clustercontrol.utility.settings.monitor.xml.HttpMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

/**
 * HTTP 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された HTTP 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている HTTP 監視情報と重複する場合はスキップされる。
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
	public List<MonitorInfo> createMonitorInfoList(HttpMonitors object) throws ConvertorException {
		return HttpConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfo> getFilterdMonitorList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		List<MonitorInfo> tmpList = MonitorSettingEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getHttpList();
		for(MonitorInfo info: new ArrayList<>(tmpList)){
			if(info.getMonitorType() != MonitorTypeConstant.TYPE_NUMERIC && info.getMonitorType() != MonitorTypeConstant.TYPE_STRING){
				tmpList.remove(info);
			}
		}
		return tmpList;
	}

	@Override
	protected HttpMonitors createCastorData(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
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
