/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.action;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.openapitools.client.model.GetMonitorListRequest;
import org.openapitools.client.model.MonitorFilterInfoRequest;
import org.openapitools.client.model.MonitorInfoResponse;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.monitor.conv.CorrelationConv;
import com.clustercontrol.utility.settings.monitor.xml.CorrelationMonitor;
import com.clustercontrol.utility.settings.monitor.xml.CorrelationMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * 相関係数監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された相関係数監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている相関係数監視情報と重複した場合はダイアログにて上書き/スキップをユーザに選択させる。
 *
 * @version 6.1.0
 * @since 6.1.0
 *
 */
public class CorrelationAction extends AbstractMonitorAction<CorrelationMonitors> {
	public CorrelationAction() throws ConvertorException {
		super();
	}

	@Override
	public Class<CorrelationMonitors> getDataClass() {
		return CorrelationMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(CorrelationMonitors object) {
		int res = CorrelationConv.checkSchemaVersion(object.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				CorrelationConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfoResponse> createMonitorInfoList(CorrelationMonitors object) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		return CorrelationConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfoResponse> getFilterdMonitorList() throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed {
		GetMonitorListRequest monitorFilterInfo = new GetMonitorListRequest();
		
		MonitorFilterInfoRequest monitorFilter = new MonitorFilterInfoRequest();
		monitorFilter.setMonitorTypeId(HinemosModuleConstant.MONITOR_CORRELATION);
		
		monitorFilterInfo.setMonitorFilterInfo(monitorFilter);
		
		List<MonitorInfoResponse> list;
		try {
			list = MonitorsettingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMonitorListByCondition(monitorFilterInfo);
		} catch (InvalidSetting e) {
			throw new HinemosUnknown(e);
		}
		
		return list;
	}

	@Override
	protected CorrelationMonitors createCastorData(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		return CorrelationConv.createCorrelationMonitors(monitorInfoList);
	}

	@Override
	protected void sort(CorrelationMonitors object) {
		CorrelationMonitor[] ms = object.getCorrelationMonitor();
		Arrays.sort(
			ms,
			new Comparator<CorrelationMonitor>() {
				@Override
				public int compare(CorrelationMonitor obj1, CorrelationMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setCorrelationMonitor(ms);
	}
}
