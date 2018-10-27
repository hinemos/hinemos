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
import com.clustercontrol.utility.settings.monitor.conv.BinaryfileConv;
import com.clustercontrol.utility.settings.monitor.xml.BinaryfileMonitor;
import com.clustercontrol.utility.settings.monitor.xml.BinaryfileMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorFilterInfo;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

/**
 * バイナリファイル 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された バイナリファイル 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている バイナリファイル 監視情報と重複する場合はスキップされる。
 *
 * @version 6.1.0
 * @since 6.1.0
 */
public class BinaryfileAction extends AbstractMonitorAction<BinaryfileMonitors> {
	public BinaryfileAction() throws ConvertorException {
		super();
	}
	@Override
	public Class<BinaryfileMonitors> getDataClass() {
		return BinaryfileMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(BinaryfileMonitors binaryfileMonitors) {
		int res = BinaryfileConv.checkSchemaVersion(binaryfileMonitors.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				BinaryfileConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfo> createMonitorInfoList(BinaryfileMonitors logfileMonitors) throws ConvertorException, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return BinaryfileConv.createMonitorInfoList(logfileMonitors);
	}

	@Override
	protected List<MonitorInfo> getFilterdMonitorList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		MonitorFilterInfo monitorFilterInfo = new MonitorFilterInfo();
		monitorFilterInfo.setMonitorTypeId(com.clustercontrol.bean.HinemosModuleConstant.MONITOR_BINARYFILE_BIN);
		
		List<MonitorInfo> list = MonitorSettingEndpointWrapper
				.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMonitorListByCondition(monitorFilterInfo);
		
		return list;
	}

	@Override
	protected BinaryfileMonitors createCastorData(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return BinaryfileConv.createBinaryMonitors(monitorInfoList);
	}

	@Override
	protected void sort(BinaryfileMonitors object) {
		BinaryfileMonitor[] ms = object.getBinaryfileMonitor();
		Arrays.sort(
			ms,
			new Comparator<BinaryfileMonitor>() {
				@Override
				public int compare(BinaryfileMonitor obj1, BinaryfileMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setBinaryfileMonitor(ms);
	}
}
