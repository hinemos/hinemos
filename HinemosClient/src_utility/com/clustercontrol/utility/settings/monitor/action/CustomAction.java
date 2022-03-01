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

import org.openapitools.client.model.CustomNumericMonitorInfoResponse;
import org.openapitools.client.model.CustomStringMonitorInfoResponse;
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
import com.clustercontrol.utility.settings.monitor.conv.CustomConv;
import com.clustercontrol.utility.settings.monitor.xml.CustomMonitor;
import com.clustercontrol.utility.settings.monitor.xml.CustomMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * カスタム 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された コマンド 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている コマンド 監視情報と重複した場合はダイアログにて上書き/スキップをユーザに選択させる。
 *
 * @version 6.1.0
 * @since 2.0.0
 *
 *
 */
public class CustomAction extends AbstractMonitorAction<CustomMonitors> {

	public CustomAction() throws ConvertorException {
		super();
	}

	@Override
	public Class<CustomMonitors> getDataClass() {
		return CustomMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(CustomMonitors customMonitors) {
		int res = CustomConv.checkSchemaVersion(customMonitors.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				CustomConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfoResponse> createMonitorInfoList(CustomMonitors customMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		return CustomConv.createMonitorInfoList(customMonitors);
	}

	@Override
	protected List<MonitorInfoResponse> getFilterdMonitorList() throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed {
		List<MonitorInfoResponse> monitorInfoList = new ArrayList<MonitorInfoResponse>();
		List<CustomNumericMonitorInfoResponse> numericList = MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getCustomNumericList(null);
		List<CustomStringMonitorInfoResponse> stringList = MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getCustomStringList(null);
		
		for(CustomNumericMonitorInfoResponse numeric:numericList){
			MonitorInfoResponse monitorInfo = new MonitorInfoResponse();
			RestClientBeanUtil.convertBean(numeric, monitorInfo);
			monitorInfoList.add(monitorInfo);
		}
		for(CustomStringMonitorInfoResponse string:stringList){
			MonitorInfoResponse monitorInfo = new MonitorInfoResponse();
			RestClientBeanUtil.convertBean(string, monitorInfo);
			monitorInfoList.add(monitorInfo);
		}
		
		return monitorInfoList;
	}

	@Override
	protected CustomMonitors createCastorData(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		return CustomConv.createCustomMonitors(monitorInfoList);
	}

	@Override
	protected void sort(CustomMonitors object) {
		CustomMonitor[] ms = object.getCustomMonitor();
		Arrays.sort(
			ms,
			new Comparator<CustomMonitor>() {
				@Override
				public int compare(CustomMonitor obj1, CustomMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setCustomMonitor(ms);
	}
}
