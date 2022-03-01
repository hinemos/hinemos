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
import org.openapitools.client.model.PingMonitorInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.monitor.conv.PingConv;
import com.clustercontrol.utility.settings.monitor.xml.PingMonitor;
import com.clustercontrol.utility.settings.monitor.xml.PingMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * PING監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義されたPING監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されているPING監視情報と重複した場合はダイアログにて上書き/スキップをユーザに選択させる。
 *
 * @version 6.1.0
 * @since 1.0.0
 *
 */
public class PingAction extends AbstractMonitorAction<PingMonitors> {
	public PingAction() throws ConvertorException {
		super();
		this.targetTitle = Messages.getString("monitor.ping");
	}

	@Override
	public Class<PingMonitors> getDataClass() {
		return PingMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(PingMonitors object) {
		int res = PingConv.checkSchemaVersion(object.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				PingConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfoResponse> createMonitorInfoList(PingMonitors object) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		return PingConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfoResponse> getFilterdMonitorList() throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed {
		List<PingMonitorInfoResponse> dtoList = MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getPingList(null);
		List<MonitorInfoResponse> retList = new ArrayList<MonitorInfoResponse>();
		for(PingMonitorInfoResponse dtoRec :  dtoList){
			MonitorInfoResponse infoRec = new MonitorInfoResponse();
			RestClientBeanUtil.convertBean(dtoRec, infoRec);
			retList.add(infoRec);
		}
		return retList;
	}

	@Override
	protected PingMonitors createCastorData(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		return PingConv.createPingMonitors(monitorInfoList);
	}

	@Override
	protected void sort(PingMonitors object) {
		PingMonitor[] ms = object.getPingMonitor();
		Arrays.sort(
			ms,
			new Comparator<PingMonitor>() {
				@Override
				public int compare(PingMonitor obj1, PingMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setPingMonitor(ms);
	}
}
