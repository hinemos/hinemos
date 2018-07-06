/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.action;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.monitor.conv.IntegrationConv;
import com.clustercontrol.utility.settings.monitor.xml.IntegrationMonitor;
import com.clustercontrol.utility.settings.monitor.xml.IntegrationMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorFilterInfo;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

/**
 * 収集値統合 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された 収集値統合 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている 収集値統合 監視情報と重複する場合はスキップされる。
 *
 * @version 6.1.0
 * @since 6.1.0
 *
 */
public class IntegrationAction extends AbstractMonitorAction<IntegrationMonitors> {
	public IntegrationAction() throws ConvertorException {
		super();
	}

	@Override
	public Class<IntegrationMonitors> getDataClass() {
		return IntegrationMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(IntegrationMonitors object) {
		int res = IntegrationConv.checkSchemaVersion(object.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				IntegrationConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfo> createMonitorInfoList(IntegrationMonitors object) throws ConvertorException {
		return IntegrationConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfo> getFilterdMonitorList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		MonitorFilterInfo monitorFilterInfo = new MonitorFilterInfo();
		monitorFilterInfo.setMonitorTypeId(com.clustercontrol.bean.HinemosModuleConstant.MONITOR_INTEGRATION);
		
		List<MonitorInfo> list = MonitorSettingEndpointWrapper
				.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMonitorListByCondition(monitorFilterInfo);
		
		return list;
	}

	@Override
	protected IntegrationMonitors createCastorData(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return IntegrationConv.createIntegrationMonitors(monitorInfoList);
	}

	@Override
	protected void sort(IntegrationMonitors object) {
		IntegrationMonitor[] ms = object.getIntegrationMonitor();
		Arrays.sort(
			ms,
			new Comparator<IntegrationMonitor>() {
				@Override
				public int compare(IntegrationMonitor obj1, IntegrationMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setIntegrationMonitor(ms);
	}
}