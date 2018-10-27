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
import com.clustercontrol.utility.settings.monitor.conv.CorrelationConv;
import com.clustercontrol.utility.settings.monitor.xml.CorrelationMonitor;
import com.clustercontrol.utility.settings.monitor.xml.CorrelationMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorFilterInfo;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

/**
 * 相関係数監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された相関係数監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている相関係数監視情報と重複する場合はスキップされる。
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
	public List<MonitorInfo> createMonitorInfoList(CorrelationMonitors object) throws ConvertorException {
		return CorrelationConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfo> getFilterdMonitorList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		//TODO 
		MonitorFilterInfo monitorFilterInfo = new MonitorFilterInfo();
		monitorFilterInfo.setMonitorTypeId(com.clustercontrol.bean.HinemosModuleConstant.MONITOR_CORRELATION);
		
		List<MonitorInfo> list = MonitorSettingEndpointWrapper
				.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMonitorListByCondition(monitorFilterInfo);
		
		return list;
	}

	@Override
	protected CorrelationMonitors createCastorData(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
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
