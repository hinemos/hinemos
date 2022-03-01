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
import org.openapitools.client.model.ServiceportMonitorInfoResponse;

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
import com.clustercontrol.utility.settings.monitor.conv.PortConv;
import com.clustercontrol.utility.settings.monitor.xml.PortMonitor;
import com.clustercontrol.utility.settings.monitor.xml.PortMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * ポート 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された ポート 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている ポート 監視情報と重複した場合はダイアログにて上書き/スキップをユーザに選択させる。
 *
 * @version 6.1.0
 * @since 1.0.0
 *
 */
public class PortAction extends AbstractMonitorAction<PortMonitors> {
	public PortAction() throws ConvertorException {
		super();
	}

	@Override
	public Class<PortMonitors> getDataClass() {
		return PortMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(PortMonitors object) {
		int res = PortConv.checkSchemaVersion(object.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				PortConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfoResponse> createMonitorInfoList(PortMonitors object) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		return PortConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfoResponse> getFilterdMonitorList() throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed {
		List<MonitorInfoResponse> monitorInfoList = new ArrayList<MonitorInfoResponse>();
		List<ServiceportMonitorInfoResponse> portMonitorInfoList = MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getPortList(null);
		
		for(ServiceportMonitorInfoResponse port:portMonitorInfoList){
			MonitorInfoResponse monitorInfo = new MonitorInfoResponse();
			RestClientBeanUtil.convertBeanSimple(port, monitorInfo);
			monitorInfoList.add(monitorInfo);
		}
		
		return monitorInfoList;
	}

	@Override
	protected PortMonitors createCastorData(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		return PortConv.createPortMonitors(monitorInfoList);
	}

	@Override
	protected void sort(PortMonitors object) {
		PortMonitor[] ms = object.getPortMonitor();
		Arrays.sort(
			ms,
			new Comparator<PortMonitor>() {
				@Override
				public int compare(PortMonitor obj1, PortMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setPortMonitor(ms);
	}
}