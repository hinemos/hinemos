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
import com.clustercontrol.utility.settings.monitor.conv.PacketCapConv;
import com.clustercontrol.utility.settings.monitor.xml.PacketCapMonitor;
import com.clustercontrol.utility.settings.monitor.xml.PacketCapMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorFilterInfo;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

/**
 * パケットキャプチャ 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された パケットキャプチャ 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている パケットキャプチャ 監視情報と重複する場合はスキップされる。
 *
 * @version 6.1.0
 * @since 6.1.0
 */
public class PacketCapAction extends AbstractMonitorAction<PacketCapMonitors> {
	public PacketCapAction() throws ConvertorException {
		super();
	}
	@Override
	public Class<PacketCapMonitors> getDataClass() {
		return PacketCapMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(PacketCapMonitors packetCapMonitors) {
		int res = PacketCapConv.checkSchemaVersion(packetCapMonitors.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				PacketCapConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfo> createMonitorInfoList(PacketCapMonitors packetCapMonitors) throws ConvertorException, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return PacketCapConv.createMonitorInfoList(packetCapMonitors);
	}

	@Override
	protected List<MonitorInfo> getFilterdMonitorList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		MonitorFilterInfo monitorFilterInfo = new MonitorFilterInfo();
		monitorFilterInfo.setMonitorTypeId(com.clustercontrol.bean.HinemosModuleConstant.MONITOR_PCAP_BIN);
		
		List<MonitorInfo> list = MonitorSettingEndpointWrapper
				.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMonitorListByCondition(monitorFilterInfo);
		
		return list;
	}

	@Override
	protected PacketCapMonitors createCastorData(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return PacketCapConv.createPacketCapMonitors(monitorInfoList);
	}

	@Override
	protected void sort(PacketCapMonitors object) {
		PacketCapMonitor[] ms = object.getPacketCapMonitor();
		Arrays.sort(
			ms,
			new Comparator<PacketCapMonitor>() {
				@Override
				public int compare(PacketCapMonitor obj1, PacketCapMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setPacketCapMonitor(ms);
	}
}
