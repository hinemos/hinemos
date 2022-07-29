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
import com.clustercontrol.utility.settings.monitor.conv.PacketCapConv;
import com.clustercontrol.utility.settings.monitor.xml.PacketCapMonitor;
import com.clustercontrol.utility.settings.monitor.xml.PacketCapMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * パケットキャプチャ 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された パケットキャプチャ 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている パケットキャプチャ 監視情報と重複した場合はダイアログにて上書き/スキップをユーザに選択させる。
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
	public List<MonitorInfoResponse> createMonitorInfoList(PacketCapMonitors packetCapMonitors) throws ConvertorException, HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, InvalidSetting, ParseException {
		return PacketCapConv.createMonitorInfoList(packetCapMonitors);
	}

	@Override
	protected List<MonitorInfoResponse> getFilterdMonitorList() throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed {
		GetMonitorListRequest monitorFilterInfo = new GetMonitorListRequest();
				
		MonitorFilterInfoRequest monitorFilter = new MonitorFilterInfoRequest();
		monitorFilter.setMonitorTypeId(HinemosModuleConstant.MONITOR_PCAP_BIN);
		
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
	protected PacketCapMonitors createCastorData(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
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
