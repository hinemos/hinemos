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
import org.openapitools.client.model.ProcessMonitorInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.sdml.util.SdmlClientUtil;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.monitor.conv.ProcessConv;
import com.clustercontrol.utility.settings.monitor.xml.ProcessMonitor;
import com.clustercontrol.utility.settings.monitor.xml.ProcessMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * PROCESS監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義されたPROCESS監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されているPROCESS監視情報と重複した場合はダイアログにて上書き/スキップをユーザに選択させる。
 *
 * @version 6.1.0
 * @since 1.0.0
 *
 */
public class ProcessAction extends AbstractMonitorAction<ProcessMonitors> {
	public ProcessAction() throws ConvertorException {
		super();
	}

	@Override
	protected boolean checkSchemaVersionScope(ProcessMonitors object) {
		int res = ProcessConv.checkSchemaVersion(object.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				ProcessConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	protected Class<ProcessMonitors> getDataClass() {
		return ProcessMonitors.class;
	}

	@Override
	protected List<MonitorInfoResponse> createMonitorInfoList(ProcessMonitors object) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		return ProcessConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfoResponse> getFilterdMonitorList()
			throws HinemosUnknown, InvalidRole,
			InvalidUserPass, MonitorNotFound, RestConnectFailed {
		List<MonitorInfoResponse> monitorInfoResponseList = new ArrayList<MonitorInfoResponse>();

		 List<ProcessMonitorInfoResponse> dtoResList 
		 = MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getProcessList(null);
		
		for(ProcessMonitorInfoResponse dtoRes:dtoResList){
			MonitorInfoResponse monitorInfo = new MonitorInfoResponse();
			RestClientBeanUtil.convertBean(dtoRes, monitorInfo);
			if (SdmlClientUtil.isCreatedBySdml(monitorInfo)) {
				// SDMLで自動作成された監視は除外する
				continue;
			}
			monitorInfoResponseList.add(monitorInfo);
		}
		
		return monitorInfoResponseList;
	}

	@Override
	protected ProcessMonitors createCastorData(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		return ProcessConv.createProcessMonitors(monitorInfoList);
	}

	@Override
	protected void sort(ProcessMonitors object) {
		ProcessMonitor[] ms = object.getProcessMonitor();
		Arrays.sort(
			ms,
			new Comparator<ProcessMonitor>() {
				@Override
				public int compare(ProcessMonitor obj1, ProcessMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setProcessMonitor(ms);
	}
}
