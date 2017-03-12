package com.clustercontrol.repository.util;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.util.EndpointUnit.EndpointSetting;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.ws.access.UsedOwnerRole_Exception;
import com.clustercontrol.ws.repository.AgentStatusInfo;
import com.clustercontrol.ws.repository.FacilityDuplicate_Exception;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityNotFound_Exception;
import com.clustercontrol.ws.repository.FacilityTreeItem;
import com.clustercontrol.ws.repository.HinemosUnknown_Exception;
import com.clustercontrol.ws.repository.InvalidRole_Exception;
import com.clustercontrol.ws.repository.InvalidSetting_Exception;
import com.clustercontrol.ws.repository.InvalidUserPass_Exception;
import com.clustercontrol.ws.repository.NodeInfo;
import com.clustercontrol.ws.repository.NodeInfoDeviceSearch;
import com.clustercontrol.ws.repository.RepositoryEndpoint;
import com.clustercontrol.ws.repository.RepositoryEndpointService;
import com.clustercontrol.ws.repository.RepositoryTableInfo;
import com.clustercontrol.ws.repository.ScopeInfo;
import com.clustercontrol.ws.repository.SnmpResponseError_Exception;
import com.clustercontrol.ws.repository.UsedFacility_Exception;

/**
 * Hinemosマネージャとの通信をするクラス。
 * HAのような複数マネージャ対応のため、このクラスを実装する。
 *
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる。
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する。
 */
public class RepositoryEndpointWrapper {

	// ログ
	private static Log m_log = LogFactory.getLog( RepositoryEndpointWrapper.class );

	private EndpointUnit endpointUnit;

	public RepositoryEndpointWrapper(EndpointUnit endpointUnit) {
		this.endpointUnit = endpointUnit;
	}

	public static RepositoryEndpointWrapper getWrapper(String managerName) {
		return new RepositoryEndpointWrapper(EndpointManager.get(managerName));
	}

	private static List<EndpointSetting<RepositoryEndpoint>> getRepositoryEndpoint(EndpointUnit endpointUnit) {
		return endpointUnit.getEndpoint(RepositoryEndpointService.class, RepositoryEndpoint.class);
	}

	public List<RepositoryTableInfo> getPlatformList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = (RepositoryEndpoint) endpointSetting.getEndpoint();
				return endpoint.getPlatformList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getPlatformList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public void addNode(NodeInfo nodeInfo)
			throws FacilityDuplicate_Exception, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.addNode(nodeInfo);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("addNode(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public void addScope(String parentId, ScopeInfo scopeInfo)
			throws FacilityDuplicate_Exception, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.addScope(parentId, scopeInfo);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("addScope(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public void assignNodeScope(String scopeId, List<String> nodeId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.assignNodeScope(scopeId, nodeId);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("assignNodeScope(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public void deleteNode(List<String> facilityIdList)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, UsedFacility_Exception, UsedOwnerRole_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = (RepositoryEndpoint) endpointSetting.getEndpoint();
				endpoint.deleteNode(facilityIdList);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("deleteNode(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public void deleteScope(List<String> facilityIdList)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, UsedFacility_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = (RepositoryEndpoint) endpointSetting.getEndpoint();
				endpoint.deleteScope(facilityIdList);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("deleteScope(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public Long getLastUpdate() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getLastUpdate();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getLastUpdate(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<String> getExecTargetFacilityIdList(String facilityId, String ownerRoleId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getExecTargetFacilityIdList(facilityId,  ownerRoleId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getNode(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public NodeInfo getNode(String facilityId)
			throws FacilityNotFound_Exception, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getNode(facilityId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getNode(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public NodeInfoDeviceSearch getNodePropertyBySNMP(String ipAddress,
			int port, String community, int version, String facilityID,
			String user, String securityLevel, String authPassword,
			String privPassword, String authProtocol, String privProtocol)
		throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, SnmpResponseError_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getNodePropertyBySNMP(ipAddress, port,
						community, version, facilityID, user, securityLevel,
						authPassword, privPassword, authProtocol, privProtocol);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getNodePropertyBySNMP(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public List<NodeInfoDeviceSearch> searchNodesBySNMP(String ownerRoleId, String ipAddressFrom,
			String ipAddressTo, int port, String community, int version, String facilityID,
			String user, String securityLevel, String authPassword,
			String privPassword, String authProtocol, String privProtocol)
		throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, SnmpResponseError_Exception, FacilityDuplicate_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.searchNodesBySNMP(ownerRoleId, ipAddressFrom, ipAddressTo, port,
						community, version, facilityID, securityLevel, user,
						authPassword, privPassword, authProtocol, privProtocol);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getNodePropertyBySNMP(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public List<String> getNodeScopeList(String facilityId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getNodeScopeList(facilityId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getNodeScopeList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public List<FacilityInfo> getFacilityList(String facilityId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = (RepositoryEndpoint) endpointSetting.getEndpoint();
				return endpoint.getFacilityList(facilityId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getScopeList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public ScopeInfo getScope(String facilityId)
			throws FacilityNotFound_Exception, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getScope(facilityId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getScope(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public void modifyNode(NodeInfo nodeInfo)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.modifyNode(nodeInfo);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("modifyNode(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public void modifyScope(ScopeInfo scopeInfo)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.modifyScope(scopeInfo);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("modifyScope(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public void releaseNodeScope(String scopeId, List<String> nodeIdList)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.releaseNodeScope(scopeId, nodeIdList);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("releaseNodeScope(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public List<RepositoryTableInfo> getCollectorSubPlatformTableInfoList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = (RepositoryEndpoint) endpointSetting.getEndpoint();
				return endpoint.getCollectorSubPlatformTableInfoList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getCollectorSubPlatformMstList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public String getFacilityPath(String facilityId, String parentFacilityId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getFacilityPath(facilityId, parentFacilityId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getFacilityPath(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public List<NodeInfo> getFilterNodeList(NodeInfo nodeInfo)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getFilterNodeList(nodeInfo);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getFilterNodeList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public List<NodeInfo> getNodeList(String parentFacilityId, int level)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getNodeList(parentFacilityId, level);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getNodeList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public List<NodeInfo> getNodeListAll()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getNodeListAll();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getNodeListAll(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public List<AgentStatusInfo> getAgentStatusList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getAgentStatusList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getAgentStatusList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public FacilityTreeItem getFacilityTree(String ownerRoleId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = endpointSetting.getEndpoint();
				FacilityTreeItem item = endpoint.getFacilityTree(ownerRoleId);
				setTreeParent(item);
				return item;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getFacilityTree(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public FacilityTreeItem getExecTargetFacilityTreeByFacilityId(String facilityId, String ownerRoleId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = (RepositoryEndpoint) endpointSetting.getEndpoint();
				FacilityTreeItem item = endpoint.getExecTargetFacilityTreeByFacilityId(facilityId, ownerRoleId);
				if(item != null)
					setTreeParent(item);
				return item;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getExecTargetFacilityTreeByFacilityId(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public FacilityTreeItem getNodeFacilityTree(String ownerRoleId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = (RepositoryEndpoint) endpointSetting.getEndpoint();
				FacilityTreeItem item = endpoint.getNodeFacilityTree(ownerRoleId);
				if(item != null)
					setTreeParent(item);
				return item;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getNodeFacilityTree(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public static void setTreeParent(FacilityTreeItem item) {
		String facilityName = item.getData().getFacilityName();
		item.getData().setFacilityName(HinemosMessage.replace(facilityName));
		List<FacilityTreeItem> children = item.getChildren();
		for (FacilityTreeItem child : children) {
			child.setParent(item);
			setTreeParent(child);
		}
	}

	public void restartAgent(ArrayList<String> facilityIdList, int agentCommand)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.restartAgent(facilityIdList, agentCommand);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("restartAgent(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}

	public String replaceNodeVariable(String facilityId, String replaceObject)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<RepositoryEndpoint> endpointSetting : getRepositoryEndpoint(endpointUnit)) {
			try {
				RepositoryEndpoint endpoint = (RepositoryEndpoint) endpointSetting.getEndpoint();
				return endpoint.replaceNodeVariable(facilityId, replaceObject);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("replaceNodeVariable(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}

		throw wse;
	}
}
