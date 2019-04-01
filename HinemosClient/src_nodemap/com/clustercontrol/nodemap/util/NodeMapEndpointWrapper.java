/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.util;

import java.util.List;

import javax.activation.DataHandler;
import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.util.EndpointUnit.EndpointSetting;
import com.clustercontrol.ws.nodemap.HinemosDbTimeout_Exception;
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
import com.clustercontrol.ws.nodemap.FacilityDuplicate_Exception;
import com.clustercontrol.ws.nodemap.InvalidSetting_Exception;
import com.clustercontrol.ws.repository.NodeInfo;
import com.clustercontrol.ws.repository.ScopeInfo;

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

	public List<NodeInfo> getNodeList(String parentFacilityId, NodeInfo nodeFilterInfo)
			throws HinemosUnknown_Exception, HinemosDbTimeout_Exception, InvalidSetting_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		m_log.debug("getNodeList:" + parentFacilityId);
		for (EndpointSetting<NodeMapEndpoint> endpointSetting : getNodeMapEndpoint(endpointUnit)) {
			try {
				NodeMapEndpoint endpoint = (NodeMapEndpoint) endpointSetting.getEndpoint();
				return endpoint.getNodeList(parentFacilityId, nodeFilterInfo);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getNodeList(), " + e.getMessage(), e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public String getNodeConfigFileId()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<NodeMapEndpoint> endpointSetting : getNodeMapEndpoint(endpointUnit)) {
			try {
				NodeMapEndpoint endpoint = (NodeMapEndpoint) endpointSetting.getEndpoint();
				return endpoint.getNodeConfigFileId();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getNodeConfigFileId(), " + e.getMessage(), e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public DataHandler downloadNodeConfigFileHeader(String conditionStr, String filename, String language)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<NodeMapEndpoint> endpointSetting : getNodeMapEndpoint(endpointUnit)) {
			try {
				NodeMapEndpoint endpoint = (NodeMapEndpoint) endpointSetting.getEndpoint();
				return endpoint.downloadNodeConfigFileHeader(conditionStr, filename, language);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("downloadNodeConfigFileHeader(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public DataHandler downloadNodeConfigFile(List<String> facilityIdList, Long targetDatetime, String filename, String language, String managerName, List<String> itemList)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<NodeMapEndpoint> endpointSetting : getNodeMapEndpoint(endpointUnit)) {
			try {
				NodeMapEndpoint endpoint = (NodeMapEndpoint) endpointSetting.getEndpoint();
				return endpoint.downloadNodeConfigFile(facilityIdList, targetDatetime, filename, language, managerName, itemList);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("downloadNodeConfigFile(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public int getDownloadNodeConfigCount()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<NodeMapEndpoint> endpointSetting : getNodeMapEndpoint(endpointUnit)) {
			try {
				NodeMapEndpoint endpoint = (NodeMapEndpoint) endpointSetting.getEndpoint();
				return endpoint.getDownloadNodeConfigCount();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getDownloadNodeConfigCount(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void deleteNodeConfigFile(String filename)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<NodeMapEndpoint> endpointSetting : getNodeMapEndpoint(endpointUnit)) {
			try {
				NodeMapEndpoint endpoint = (NodeMapEndpoint) endpointSetting.getEndpoint();
				endpoint.deleteNodeConfigFile(filename);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("deleteEventFile(), " + e.getMessage());
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
	
	public void addFilterScope(ScopeInfo property, List<String> facilityIdList)
			throws InvalidSetting_Exception, HinemosUnknown_Exception, InvalidRole_Exception, 
			InvalidUserPass_Exception, FacilityDuplicate_Exception {
		WebServiceException wse = null;
		m_log.debug("addFilterScope");
		for (EndpointSetting<NodeMapEndpoint> endpointSetting : getNodeMapEndpoint(endpointUnit)) {
			try {
				NodeMapEndpoint endpoint = (NodeMapEndpoint) endpointSetting.getEndpoint();
				endpoint.addFilterScope(property, facilityIdList);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("addFilterScope(), " + e.getMessage(), e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

}
