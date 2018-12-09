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
import com.clustercontrol.utility.settings.monitor.conv.PerfConv;
import com.clustercontrol.utility.settings.monitor.xml.PerfMonitor;
import com.clustercontrol.utility.settings.monitor.xml.PerfMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

/**
 * リソース 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された リソース  監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている リソース 監視情報と重複する場合はスキップされる。
 *
 * @version 6.1.0
 * @since 1.0.0
 *
 */
public class PerfAction extends AbstractMonitorAction<PerfMonitors> {
	public PerfAction() throws ConvertorException {
		super();
	}

	@Override
	public Class<PerfMonitors> getDataClass() {
		return PerfMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(PerfMonitors object) {
		int res = PerfConv.checkSchemaVersion(object.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				PerfConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfo> createMonitorInfoList(PerfMonitors object) throws ConvertorException {
		return PerfConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfo> getFilterdMonitorList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return MonitorSettingEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getPerformanceList();
	}

	@Override
	protected PerfMonitors createCastorData(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return PerfConv.createPerfMonitors(monitorInfoList);
	}

	@Override
	protected void sort(PerfMonitors object) {
		PerfMonitor[] ms = object.getPerfMonitor();
		Arrays.sort(
			ms,
			new Comparator<PerfMonitor>() {
				@Override
				public int compare(PerfMonitor obj1, PerfMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setPerfMonitor(ms);
	}
}
