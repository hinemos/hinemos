package com.clustercontrol.notify.util;

import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.util.EndpointUnit.EndpointSetting;
import com.clustercontrol.ws.notify.HinemosUnknown_Exception;
import com.clustercontrol.ws.notify.InvalidRole_Exception;
import com.clustercontrol.ws.notify.InvalidSetting_Exception;
import com.clustercontrol.ws.notify.InvalidUserPass_Exception;
import com.clustercontrol.ws.notify.NotifyCheckIdResultInfo;
import com.clustercontrol.ws.notify.NotifyDuplicate_Exception;
import com.clustercontrol.ws.notify.NotifyEndpoint;
import com.clustercontrol.ws.notify.NotifyEndpointService;
import com.clustercontrol.ws.notify.NotifyInfo;
import com.clustercontrol.ws.notify.NotifyNotFound_Exception;

/**
 * Hinemosマネージャとの通信をするクラス。
 * HAのような複数マネージャ対応のため、このクラスを実装する。
 *
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる。
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する。
 */
public class NotifyEndpointWrapper {

	// ログ
	private static Log m_log = LogFactory.getLog( NotifyEndpointWrapper.class );

	private EndpointUnit endpointUnit;

	public NotifyEndpointWrapper(EndpointUnit endpointUnit) {
		this.endpointUnit = endpointUnit;
	}

	public static NotifyEndpointWrapper getWrapper(String managerName) {
		return new NotifyEndpointWrapper(EndpointManager.get(managerName));
	}

	private static List<EndpointSetting<NotifyEndpoint>> getNotifyEndpoint(EndpointUnit endpointUnit) {
		return endpointUnit.getEndpoint(NotifyEndpointService.class, NotifyEndpoint.class);
	}

	public boolean addNotify(NotifyInfo notifyInfo) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, NotifyDuplicate_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<NotifyEndpoint> endpointSetting : getNotifyEndpoint(endpointUnit)) {
			try {
				NotifyEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.addNotify(notifyInfo);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("addNotify(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public boolean modifyNotify(NotifyInfo notifyInfo) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, NotifyDuplicate_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<NotifyEndpoint> endpointSetting : getNotifyEndpoint(endpointUnit)) {
			try {
				NotifyEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.modifyNotify(notifyInfo);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("modifyNotify(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public boolean deleteNotify(List<String> notifyIdList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, NotifyNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<NotifyEndpoint> endpointSetting : getNotifyEndpoint(endpointUnit)) {
			try {
				NotifyEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.deleteNotify(notifyIdList);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("deleteNotify(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<NotifyCheckIdResultInfo> checkNotifyId(List<String> notifyIdList) throws HinemosUnknown_Exception, NotifyNotFound_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<NotifyEndpoint> endpointSetting : getNotifyEndpoint(endpointUnit)) {
			try {
				NotifyEndpoint endpoint = (NotifyEndpoint) endpointSetting.getEndpoint();
				return endpoint.checkNotifyId(notifyIdList);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("checkNotifyId(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public NotifyInfo getNotify(String notifyId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, NotifyNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<NotifyEndpoint> endpointSetting : getNotifyEndpoint(endpointUnit)) {
			try {
				NotifyEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getNotify(notifyId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getNotify(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<NotifyInfo> getNotifyList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, NotifyNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<NotifyEndpoint> endpointSetting : getNotifyEndpoint(endpointUnit)) {
			try {
				NotifyEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getNotifyList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getNotifyList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<NotifyInfo> getNotifyListByOwnerRole(String ownerRoleId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, NotifyNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<NotifyEndpoint> endpointSetting : getNotifyEndpoint(endpointUnit)) {
			try {
				NotifyEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getNotifyListByOwnerRole(ownerRoleId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getNotifyListByOwnerRole(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void setNotifyStatus(String notifyId, boolean validFlag) throws NotifyNotFound_Exception, NotifyDuplicate_Exception, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception{
		WebServiceException wse = null;
		for (EndpointSetting<NotifyEndpoint> endpointSetting : getNotifyEndpoint(endpointUnit)) {
			try {
				NotifyEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.setNotifyStatus(notifyId, validFlag);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("setNotifyStatus(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
}
