package com.clustercontrol.maintenance.util;

import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.util.EndpointUnit.EndpointSetting;
import com.clustercontrol.ws.maintenance.InvalidSetting_Exception;
import com.clustercontrol.ws.maintenance.MaintenanceDuplicate_Exception;
import com.clustercontrol.ws.maintenance.MaintenanceEndpoint;
import com.clustercontrol.ws.maintenance.MaintenanceEndpointService;
import com.clustercontrol.ws.maintenance.MaintenanceInfo;
import com.clustercontrol.ws.maintenance.MaintenanceNotFound_Exception;
import com.clustercontrol.ws.maintenance.MaintenanceTypeMst;
import com.clustercontrol.ws.maintenance.NotifyNotFound_Exception;
import com.clustercontrol.ws.maintenance.HinemosUnknown_Exception;
import com.clustercontrol.ws.maintenance.InvalidRole_Exception;
import com.clustercontrol.ws.maintenance.InvalidUserPass_Exception;

/**
 * Hinemosマネージャとの通信をするクラス。
 * HAのような複数マネージャ対応のため、このクラスを実装する。
 *
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる。
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する。
 */
public class MaintenanceEndpointWrapper {

	// ログ
	private static Log m_log = LogFactory.getLog( MaintenanceEndpointWrapper.class );

	private EndpointUnit endpointUnit;

	public MaintenanceEndpointWrapper(EndpointUnit endpointUnit) {
		this.endpointUnit = endpointUnit;
	}

	public static MaintenanceEndpointWrapper getWrapper(String managerName) {
		return new MaintenanceEndpointWrapper(EndpointManager.get(managerName));
	}

	private static List<EndpointSetting<MaintenanceEndpoint>> getMaintenanceEndpoint(EndpointUnit endpointUnit) {
		return endpointUnit.getEndpoint(MaintenanceEndpointService.class, MaintenanceEndpoint.class);
	}

	public MaintenanceInfo getMaintenanceInfo(String maintenanceId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MaintenanceNotFound_Exception, NotifyNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MaintenanceEndpoint> endpointSetting : getMaintenanceEndpoint(endpointUnit)) {
			try {
				MaintenanceEndpoint endpoint = (MaintenanceEndpoint) endpointSetting.getEndpoint();
				return endpoint.getMaintenanceInfo(maintenanceId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getMaintenanceInfo(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<MaintenanceInfo> getMaintenanceList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MaintenanceNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MaintenanceEndpoint> endpointSetting : getMaintenanceEndpoint(endpointUnit)) {
			try {
				MaintenanceEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getMaintenanceList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getMaintenanceList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<MaintenanceTypeMst> getMaintenanceTypeList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MaintenanceNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MaintenanceEndpoint> endpointSetting : getMaintenanceEndpoint(endpointUnit)) {
			try {
				MaintenanceEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getMaintenanceTypeList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getMaintenanceTypeList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void addMaintenance(MaintenanceInfo info)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MaintenanceDuplicate_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MaintenanceEndpoint> endpointSetting : getMaintenanceEndpoint(endpointUnit)) {
			try {
				MaintenanceEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.addMaintenance(info);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("addMaintenance(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void modifyMaintenance(MaintenanceInfo info)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MaintenanceNotFound_Exception, NotifyNotFound_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MaintenanceEndpoint> endpointSetting : getMaintenanceEndpoint(endpointUnit)) {
			try {
				MaintenanceEndpoint endpoint = (MaintenanceEndpoint) endpointSetting.getEndpoint();
				endpoint.modifyMaintenance(info);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("modifyMaintenance(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void deleteMaintenance(String maintenanceId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MaintenanceNotFound_Exception, NotifyNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MaintenanceEndpoint> endpointSetting : getMaintenanceEndpoint(endpointUnit)) {
			try {
				MaintenanceEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.deleteMaintenance(maintenanceId);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("deleteMaintenance(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void setMaintenanceStatus(String maintenanceId, boolean validFlag)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MaintenanceNotFound_Exception, NotifyNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MaintenanceEndpoint> endpointSetting : getMaintenanceEndpoint(endpointUnit)) {
			try {
				MaintenanceEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.setMaintenanceStatus(maintenanceId, validFlag);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("setMaintenanceStatus(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
}
