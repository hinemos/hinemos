/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.util;

import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.util.EndpointUnit.EndpointSetting;
import com.clustercontrol.ws.monitor.CollectorItemInfo;
import com.clustercontrol.ws.monitor.HashMapInfo;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidSetting_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.JdbcDriverInfo;
import com.clustercontrol.ws.monitor.MonitorDuplicate_Exception;
import com.clustercontrol.ws.monitor.MonitorFilterInfo;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorInfoBean;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.ws.monitor.MonitorSettingEndpoint;
import com.clustercontrol.ws.monitor.MonitorSettingEndpointService;

/**
 * Hinemosマネージャとの通信をするクラス。 HAのような複数マネージャ対応のため、このクラスを実装する。
 *
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる。
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する。
 */
public class MonitorSettingEndpointWrapper{

	// ログ
	private static Log m_log = LogFactory.getLog( MonitorSettingEndpointWrapper.class );

	private EndpointUnit endpointUnit;

	public MonitorSettingEndpointWrapper(EndpointUnit endpointUnit) {
		this.endpointUnit = endpointUnit;
	}

	public static MonitorSettingEndpointWrapper getWrapper(String managerName) {
		return new MonitorSettingEndpointWrapper(EndpointManager.getActive(managerName));
	}

	private static List<EndpointSetting<MonitorSettingEndpoint>> getMonitorSettingEndpoint(EndpointUnit endpointUnit) {
		return endpointUnit.getEndpoint(MonitorSettingEndpointService.class, MonitorSettingEndpoint.class);
	}

	public List<MonitorInfo> getMonitorList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getMonitorList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getMonitorList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<MonitorInfo> getMonitorListByCondition( MonitorFilterInfo condition )
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getMonitorListByCondition(condition);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getMonitorListByCondition(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<MonitorInfoBean> getMonitorBeanList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getMonitorBeanList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getMonitorBeanList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<MonitorInfoBean> getMonitorBeanListByCondition( MonitorFilterInfo condition )
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getMonitorBeanListByCondition(condition);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getMonitorBeanListByCondition(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public boolean addMonitor(MonitorInfo monitorInfo)
			throws HinemosUnknown_Exception,
			InvalidRole_Exception, InvalidUserPass_Exception,
			MonitorDuplicate_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		
		
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.addMonitor(monitorInfo);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("addMonitor(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public boolean modifyMonitor(MonitorInfo monitorInfo)
			throws HinemosUnknown_Exception,
			InvalidRole_Exception, InvalidUserPass_Exception,
			MonitorNotFound_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.modifyMonitor(monitorInfo);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("modifyMonitor(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public boolean deleteMonitor(List<String> monitorIdList)
			throws HinemosUnknown_Exception,
			InvalidRole_Exception, InvalidUserPass_Exception,
			MonitorNotFound_Exception,
			InvalidSetting_Exception{
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.deleteMonitor(monitorIdList);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("deleteMonitor(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<MonitorInfo> getStringMonitoInfoListForAnalytics(String facilityId, String ownerRoleId)
			throws HinemosUnknown_Exception,
			InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getStringMonitoInfoListForAnalytics(facilityId, ownerRoleId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getStringMonitoInfoListForAnalytics(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public MonitorInfo getMonitor(String monitorId)
			throws HinemosUnknown_Exception,
			InvalidRole_Exception, InvalidUserPass_Exception,
			MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getMonitor(monitorId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getMonitor(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void setStatusMonitor(String monitorId, String monitorTypeId, boolean validFlag)
			throws HinemosUnknown_Exception,
			InvalidRole_Exception, InvalidUserPass_Exception,
			MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.setStatusMonitor(monitorId, monitorTypeId, validFlag);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("setStatusMonitor(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void setStatusCollector(String monitorId, String monitorTypeId, boolean validFlag)
			throws HinemosUnknown_Exception,
			InvalidRole_Exception, InvalidUserPass_Exception,
			MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.setStatusCollector(monitorId, monitorTypeId, validFlag);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("setStatusCollector(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//
	// 各監視機能用のメソッド
	//
	//////////////////////////////////////////////////////////////////////////////////////////

	public List<JdbcDriverInfo> getJdbcDriverList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = (MonitorSettingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getJdbcDriverList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJdbcDriverList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//
	// 各監視機能用のリスト取得メソッド
	//
	//////////////////////////////////////////////////////////////////////////////////////////

	public List<MonitorInfo> getAgentList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = (MonitorSettingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getAgentList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getAgentList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<MonitorInfo> getHttpList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = (MonitorSettingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getHttpList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getHttpList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<MonitorInfo> getLogfileList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = (MonitorSettingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getLogfileList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getLogfileList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<MonitorInfo> getPerformanceList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = (MonitorSettingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getPerformanceList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getPerformanceList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<MonitorInfo> getPingList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = (MonitorSettingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getPingList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getPingList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<MonitorInfo> getPortList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = (MonitorSettingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getPortList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getPortList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<MonitorInfo> getProcessList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = (MonitorSettingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getProcessList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getProcessList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<MonitorInfo> getTrapList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = (MonitorSettingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getTrapList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getTrapList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<MonitorInfo> getSnmpList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = (MonitorSettingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getSnmpList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getSnmpList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<MonitorInfo> getSqlList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = (MonitorSettingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getSqlList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getSqlList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<MonitorInfo> getSystemlogList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = (MonitorSettingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getSystemlogList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getSystemlogList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<MonitorInfo> getCommandList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = (MonitorSettingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getCustomList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getCommandList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<MonitorInfo> getWinServiceList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = (MonitorSettingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getWinServiceList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getWinServiceList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<MonitorInfo> getWinEventList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = (MonitorSettingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getWinEventList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getWinEventList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<MonitorInfo> getCustomTrapList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception	{
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = (MonitorSettingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getCustomTrapList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getCommandList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<MonitorInfo> getMonitorListForLogcount(String facilityId, String ownerRoleId)
			throws HinemosUnknown_Exception, InvalidUserPass_Exception, InvalidRole_Exception	{
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = (MonitorSettingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getMonitorListForLogcount(facilityId, ownerRoleId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getMonitorListForLogcount(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public HashMapInfo getItemCodeMap()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = (MonitorSettingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getItemCodeMap();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getItemCodeMap(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public List<CollectorItemInfo> getAvailableCollectorItemList(String facilityId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = (MonitorSettingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getAvailableCollectorItemList(facilityId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getAvailableCollectorItemList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public List<String> getMonitorStringTagList(String monitorId, String ownerRoleId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = (MonitorSettingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getMonitorStringTagList(monitorId, ownerRoleId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getMonitorStringTagList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public void runSummaryLogcount(String monitorId, Long startDate)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : getMonitorSettingEndpoint(endpointUnit)) {
			try {
				MonitorSettingEndpoint endpoint = (MonitorSettingEndpoint) endpointSetting.getEndpoint();
				endpoint.runSummaryLogcount(monitorId, startDate);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("runSummaryLogcount(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
}
