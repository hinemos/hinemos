package com.clustercontrol.monitor.util;

import java.util.List;

import javax.activation.DataHandler;
import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.util.EndpointUnit.EndpointSetting;
import com.clustercontrol.ws.monitor.EventBatchConfirmInfo;
import com.clustercontrol.ws.monitor.EventDataInfo;
import com.clustercontrol.ws.monitor.EventFilterInfo;
import com.clustercontrol.ws.monitor.EventLogNotFound_Exception;
import com.clustercontrol.ws.monitor.FacilityNotFound_Exception;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidSetting_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorEndpoint;
import com.clustercontrol.ws.monitor.MonitorEndpointService;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.ws.monitor.ScopeDataInfo;
import com.clustercontrol.ws.monitor.StatusDataInfo;
import com.clustercontrol.ws.monitor.StatusFilterInfo;
import com.clustercontrol.ws.monitor.ViewListInfo;

/**
 * Hinemosマネージャとの通信をするクラス。
 * HAのような複数マネージャ対応のため、このクラスを実装する。
 *
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる。
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する。
 */
public class MonitorEndpointWrapper {

	// ログ
	private static Log m_log = LogFactory.getLog( MonitorEndpointWrapper.class );

	private EndpointUnit endpointUnit;

	public MonitorEndpointWrapper(EndpointUnit endpointUnit) {
		this.endpointUnit = endpointUnit;
	}

	public static MonitorEndpointWrapper getWrapper(String managerName) {
		return new MonitorEndpointWrapper(EndpointManager.get(managerName));
	}

	public static List<EndpointSetting<MonitorEndpoint>> getMonitorEndpoint(EndpointUnit endpointUnit) {
		return endpointUnit.getEndpoint(MonitorEndpointService.class, MonitorEndpoint.class);
	}

	public EndpointUnit getEndpointUnit() {
		return this.endpointUnit;
	}

	public EventDataInfo getEventInfo(String monitorId, String monitorDetailId, String pluginId, String facilityId, Long outputDate)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorEndpoint> endpointSetting : getMonitorEndpoint(endpointUnit)) {
			try {
				MonitorEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getEventInfo(monitorId, monitorDetailId, pluginId, facilityId, outputDate);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getEventInfo(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public ViewListInfo getEventList(String facilityId, EventFilterInfo filter, int messages)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorEndpoint> endpointSetting : getMonitorEndpoint(endpointUnit)) {
			try {
				MonitorEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getEventList(facilityId, filter, messages);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getEventList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<ScopeDataInfo> getScopeList(String facilityId, boolean statusFlag, boolean eventFlag, boolean orderFlg)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception, FacilityNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorEndpoint> endpointSetting : getMonitorEndpoint(endpointUnit)) {
			try {
				MonitorEndpoint endpoint = (MonitorEndpoint) endpointSetting.getEndpoint();
				return endpoint.getScopeList(facilityId, statusFlag, eventFlag, orderFlg);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getScopeList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<StatusDataInfo> getStatusList(String facilityId, StatusFilterInfo filter)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorEndpoint> endpointSetting : getMonitorEndpoint(endpointUnit)) {
			try {
				MonitorEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getStatusList(facilityId, filter);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getStatusList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void modifyComment(String monitorId, String monitorDetailId, String pluginId, String facilityId, Long outputDate, String comment, Long commentDate, String commentUser)
			throws EventLogNotFound_Exception, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorEndpoint> endpointSetting : getMonitorEndpoint(endpointUnit)) {
			try {
				MonitorEndpoint endpoint = (MonitorEndpoint) endpointSetting.getEndpoint();
				endpoint.modifyComment(monitorId, monitorDetailId, pluginId, facilityId, outputDate, comment, commentDate, commentUser);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("modifyComment(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public DataHandler downloadEventFile(String facilityId, EventFilterInfo filter, String filename, String language)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorEndpoint> endpointSetting : getMonitorEndpoint(endpointUnit)) {
			try {
				MonitorEndpoint endpoint = (MonitorEndpoint) endpointSetting.getEndpoint();
				return endpoint.downloadEventFile(facilityId, filter, filename, language);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("downloadEventFile(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void deleteEventFile(String filename)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorEndpoint> endpointSetting : getMonitorEndpoint(endpointUnit)) {
			try {
				MonitorEndpoint endpoint = (MonitorEndpoint) endpointSetting.getEndpoint();
				endpoint.deleteEventFile(filename);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("deleteEventFile(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void modifyConfirm(List<EventDataInfo> list, int confirmType)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorEndpoint> endpointSetting : getMonitorEndpoint(endpointUnit)) {
			try {
				MonitorEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.modifyConfirm(list, confirmType);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("modifyConfirmMultiple(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void modifyBatchConfirm(int confirmType, String facilityId, EventBatchConfirmInfo info)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorEndpoint> endpointSetting : getMonitorEndpoint(endpointUnit)) {
			try {
				MonitorEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.modifyBatchConfirm(confirmType, facilityId, info);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("modifyBatchConfirm(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	/**
	 * 選択されたイベントの蓄積グラフ用フラグを変更します。
	 * 
	 * @param list
	 * @param flgType
	 * @throws HinemosUnknown_Exception
	 * @throws InvalidRole_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws MonitorNotFound_Exception
	 */
	public void modifyCollectGraphFlg(List<EventDataInfo> list, Boolean flgType)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorEndpoint> endpointSetting : getMonitorEndpoint(endpointUnit)) {
			try {
				MonitorEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.modifyCollectGraphFlg(list, flgType);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("modifyCollectGraphFlg(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public boolean deleteStatus(List<StatusDataInfo> statusDataInfoList)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorEndpoint> endpointSetting : getMonitorEndpoint(endpointUnit)) {
			try {
				MonitorEndpoint endpoint = (MonitorEndpoint) endpointSetting.getEndpoint();
				return endpoint.deleteStatus(statusDataInfoList);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("deleteStatus(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
}
