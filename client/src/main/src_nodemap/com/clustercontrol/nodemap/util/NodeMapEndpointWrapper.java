/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.util;

import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.util.EndpointUnit.EndpointSetting;
import com.clustercontrol.ws.nodemap.Association;
import com.clustercontrol.ws.nodemap.BgFileNotFound_Exception;
import com.clustercontrol.ws.nodemap.HinemosUnknown_Exception;
import com.clustercontrol.ws.nodemap.IconFileNotFound_Exception;
import com.clustercontrol.ws.nodemap.InvalidRole_Exception;
import com.clustercontrol.ws.nodemap.InvalidUserPass_Exception;
import com.clustercontrol.ws.nodemap.NodeMapEndpoint;
import com.clustercontrol.ws.nodemap.NodeMapEndpointService;
import com.clustercontrol.ws.nodemap.NodeMapException_Exception;
import com.clustercontrol.ws.nodemap.NodeMapModel;

/**
 * Hinemosマネージャとの通信をするクラス。
 * HAのような複数マネージャ対応のため、このクラスを実装する。
 * 
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる。
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する。
 */
public class NodeMapEndpointWrapper {

	// ログ
	private static Log m_log = LogFactory.getLog( NodeMapEndpointWrapper.class );

	private EndpointUnit endpointUnit;

	public NodeMapEndpointWrapper(EndpointUnit endpointUnit) {
		this.endpointUnit = endpointUnit;
	}

	public static NodeMapEndpointWrapper getWrapper(String managerName) {
		return new NodeMapEndpointWrapper(EndpointManager.get(managerName));
	}

	private static List<EndpointSetting<NodeMapEndpoint>> getNodeMapEndpoint(EndpointUnit endpointUnit) {
		return endpointUnit.getEndpoint(NodeMapEndpointService.class, NodeMapEndpoint.class);
	}
	
	public List<String> getIconImagePK() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		m_log.debug("getIconImagePK");
		for (EndpointSetting<NodeMapEndpoint> endpointSetting : getNodeMapEndpoint(endpointUnit)) {
			try {
				NodeMapEndpoint endpoint = (NodeMapEndpoint) endpointSetting.getEndpoint();
				return endpoint.getIconImagePK();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getIconImagePK(), " + e.getMessage(), e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<String> getBgImagePK() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		m_log.debug("getBgImagePK");
		for (EndpointSetting<NodeMapEndpoint> endpointSetting : getNodeMapEndpoint(endpointUnit)) {
			try {
				NodeMapEndpoint endpoint = (NodeMapEndpoint) endpointSetting.getEndpoint();
				return endpoint.getBgImagePK();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getBgImagePK(), " + e.getMessage(), e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public NodeMapModel getNodeMapModel(String facilityId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, NodeMapException_Exception {
		WebServiceException wse = null;
		m_log.debug("getNodeMapModel:" + facilityId);
		for (EndpointSetting<NodeMapEndpoint> endpointSetting : getNodeMapEndpoint(endpointUnit)) {
			try {
				NodeMapEndpoint endpoint = (NodeMapEndpoint) endpointSetting.getEndpoint();
				return endpoint.getNodeMapModel(facilityId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getNodeMapModel(), " + e.getMessage(), e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void registerNodeMapModel(NodeMapModel map) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		m_log.debug("registerNodeMapModel");
		for (EndpointSetting<NodeMapEndpoint> endpointSetting : getNodeMapEndpoint(endpointUnit)) {
			try {
				NodeMapEndpoint endpoint = (NodeMapEndpoint) endpointSetting.getEndpoint();
				endpoint.registerNodeMapModel(map);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("registerNodeMapModel(), " + e.getMessage(), e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void setBgImage(String filename, byte[] filedata) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, NodeMapException_Exception {
		WebServiceException wse = null;
		m_log.debug("setBgImage");
		for (EndpointSetting<NodeMapEndpoint> endpointSetting : getNodeMapEndpoint(endpointUnit)) {
			try {
				NodeMapEndpoint endpoint = (NodeMapEndpoint) endpointSetting.getEndpoint();
				endpoint.setBgImage(filename, filedata);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("setBgImage(), " + e.getMessage(), e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void setIconImage(String filename, byte[] filedata) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, NodeMapException_Exception {
		WebServiceException wse = null;
		m_log.debug("setIconImage");
		for (EndpointSetting<NodeMapEndpoint> endpointSetting : getNodeMapEndpoint(endpointUnit)) {
			try {
				NodeMapEndpoint endpoint = (NodeMapEndpoint) endpointSetting.getEndpoint();
				endpoint.setIconImage(filename, filedata);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("setIconImage(), " + e.getMessage(), e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public byte[] getIconImage(String filename) throws HinemosUnknown_Exception, IconFileNotFound_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		m_log.debug("getIconImage");
		for (EndpointSetting<NodeMapEndpoint> endpointSetting : getNodeMapEndpoint(endpointUnit)) {
			try {
				NodeMapEndpoint endpoint = (NodeMapEndpoint) endpointSetting.getEndpoint();
				return endpoint.getIconImage(filename);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getIconImage(), " + e.getMessage(), e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public byte[] getBgImage(String filename) throws BgFileNotFound_Exception, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		m_log.debug("getBgImage");
		for (EndpointSetting<NodeMapEndpoint> endpointSetting : getNodeMapEndpoint(endpointUnit)) {
			try {
				NodeMapEndpoint endpoint = (NodeMapEndpoint) endpointSetting.getEndpoint();
				return endpoint.getBgImage(filename);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getBgImage(), " + e.getMessage(), e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public boolean isBgImage(String filename) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		m_log.debug("isBgImage");
		for (EndpointSetting<NodeMapEndpoint> endpointSetting : getNodeMapEndpoint(endpointUnit)) {
			try {
				NodeMapEndpoint endpoint = (NodeMapEndpoint) endpointSetting.getEndpoint();
				return endpoint.isBgImage(filename);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("isBgImage(), " + e.getMessage(), e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public boolean isIconImage(String filename) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		m_log.debug("isIconImage");
		for (EndpointSetting<NodeMapEndpoint> endpointSetting : getNodeMapEndpoint(endpointUnit)) {
			try {
				NodeMapEndpoint endpoint = (NodeMapEndpoint) endpointSetting.getEndpoint();
				return endpoint.isIconImage(filename);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("isIconImage(), " + e.getMessage(), e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public List<Association> getL2ConnectionMap(String scopeId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		m_log.debug("getL2ConnectionMap");
		for (EndpointSetting<NodeMapEndpoint> endpointSetting : getNodeMapEndpoint(endpointUnit)) {
			try {
				NodeMapEndpoint endpoint = (NodeMapEndpoint) endpointSetting.getEndpoint();
				return endpoint.getL2ConnectionMap(scopeId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getL2ConnectionMap(), " + e.getMessage(), e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public List<Association> getL3ConnectionMap(String scopeId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		m_log.debug("getL3ConnectionMap");
		for (EndpointSetting<NodeMapEndpoint> endpointSetting : getNodeMapEndpoint(endpointUnit)) {
			try {
				NodeMapEndpoint endpoint = (NodeMapEndpoint) endpointSetting.getEndpoint();
				return endpoint.getL3ConnectionMap(scopeId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getL3ConnectionMap(), " + e.getMessage(), e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	/**
	 * 指定されたfacilityに対してpingを実施します。
	 * 
	 * @param facilityId
	 * @return
	 * @throws HinemosUnknown_Exception
	 * @throws InvalidRole_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws NodeMapException_Exception
	 */
	public List<String> ping(String facilityId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, NodeMapException_Exception {
		WebServiceException wse = null;
		m_log.debug("ping");
		for (EndpointSetting<NodeMapEndpoint> endpointSetting : getNodeMapEndpoint(endpointUnit)) {
			try {
				NodeMapEndpoint endpoint = (NodeMapEndpoint) endpointSetting.getEndpoint();
				return endpoint.ping(facilityId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("ping(), " + e.getMessage(), e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	

	public String getVersion() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		
		for (EndpointSetting<NodeMapEndpoint> endpointSetting : getNodeMapEndpoint(endpointUnit)) {
			try {
				NodeMapEndpoint endpoint = (NodeMapEndpoint) endpointSetting.getEndpoint();
				return endpoint.getVersion();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getVersion(), " + e.getMessage(), e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

}
