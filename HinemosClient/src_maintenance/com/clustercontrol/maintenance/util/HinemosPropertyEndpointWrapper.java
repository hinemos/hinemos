package com.clustercontrol.maintenance.util;

import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.util.EndpointUnit.EndpointSetting;
import com.clustercontrol.ws.maintenance.HinemosPropertyDuplicate_Exception;
import com.clustercontrol.ws.maintenance.HinemosPropertyEndpoint;
import com.clustercontrol.ws.maintenance.HinemosPropertyEndpointService;
import com.clustercontrol.ws.maintenance.HinemosPropertyInfo;
import com.clustercontrol.ws.maintenance.HinemosPropertyNotFound_Exception;
import com.clustercontrol.ws.maintenance.HinemosUnknown_Exception;
import com.clustercontrol.ws.maintenance.InvalidRole_Exception;
import com.clustercontrol.ws.maintenance.InvalidSetting_Exception;
import com.clustercontrol.ws.maintenance.InvalidUserPass_Exception;
import com.clustercontrol.ws.maintenance.MaintenanceNotFound_Exception;
import com.clustercontrol.ws.maintenance.NotifyNotFound_Exception;

/**
 * Hinemosマネージャとの通信をするクラス。
 * HAのような複数マネージャ対応のため、このクラスを実装する。
 *
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる。
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する。
 */
public class HinemosPropertyEndpointWrapper {

	// ログ
	private static Log m_log = LogFactory.getLog( HinemosPropertyEndpointWrapper.class );

	private EndpointUnit endpointUnit;

	public HinemosPropertyEndpointWrapper(EndpointUnit endpointUnit) {
		this.endpointUnit = endpointUnit;
	}

	public static HinemosPropertyEndpointWrapper getWrapper(String managerName) {
		return new HinemosPropertyEndpointWrapper(EndpointManager.get(managerName));
	}

	private static List<EndpointSetting<HinemosPropertyEndpoint>> getHinemosPropertyEndpoint(EndpointUnit endpointUnit) {
		return endpointUnit.getEndpoint(HinemosPropertyEndpointService.class, HinemosPropertyEndpoint.class);
	}

	/**
	 * 共通設定情報リストを取得します。
	 * @return 共通設定情報リスト
	 * @throws HinemosUnknown_Exception
	 * @throws InvalidRole_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws MaintenanceNotFound_Exception
	 */
	public List<HinemosPropertyInfo> getHinemosPropertyList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception  {
		WebServiceException wse = null;

		for (EndpointSetting<HinemosPropertyEndpoint> endpointSetting : getHinemosPropertyEndpoint(endpointUnit)) {
			try {
				HinemosPropertyEndpoint endpoint = endpointSetting.getEndpoint();
				List<HinemosPropertyInfo> list = endpoint.getHinemosPropertyList();

				return list;

			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getHinemosPropertyList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void addHinemosProperty(HinemosPropertyInfo info)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, HinemosPropertyDuplicate_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<HinemosPropertyEndpoint> endpointSetting : getHinemosPropertyEndpoint(endpointUnit)) {
			try {
				HinemosPropertyEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.addHinemosProperty(info);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("addHinemosProperty(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void modifyHinemosProperty(HinemosPropertyInfo info)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, HinemosPropertyNotFound_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<HinemosPropertyEndpoint> endpointSetting : getHinemosPropertyEndpoint(endpointUnit)) {
			try {
				HinemosPropertyEndpoint endpoint = (HinemosPropertyEndpoint)endpointSetting.getEndpoint();
				endpoint.modifyHinemosProperty(info);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("modifyHinemosProperty(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void deleteHinemosProperty(String key)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, HinemosPropertyNotFound_Exception, NotifyNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<HinemosPropertyEndpoint> endpointSetting : getHinemosPropertyEndpoint(endpointUnit)) {
			try {
				HinemosPropertyEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.deleteHinemosProperty(key);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("deleteHinemosProperty(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public HinemosPropertyInfo getHinemosProperty(String key)
			throws HinemosPropertyNotFound_Exception, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception  {
		WebServiceException wse = null;

		for (EndpointSetting<HinemosPropertyEndpoint> endpointSetting : getHinemosPropertyEndpoint(endpointUnit)) {
			try {
				HinemosPropertyEndpoint endpoint = endpointSetting.getEndpoint();
				HinemosPropertyInfo info = endpoint.getHinemosProperty(key);

				return info;

			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getHinemosProperty(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
}
