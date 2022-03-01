/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.repository.util;

import java.util.ArrayList;
import java.util.List;

import org.openapitools.client.model.AddCollectPlatformMasterRequest;
import org.openapitools.client.model.AddCollectSubPlatformMasterRequest;
import org.openapitools.client.model.AddFilterScopeRequest;
import org.openapitools.client.model.AddFilterScopeResponse;
import org.openapitools.client.model.AddNodeAndAssignScopeFromInstanceRequest;
import org.openapitools.client.model.AddNodeConfigSettingInfoRequest;
import org.openapitools.client.model.AddNodeRequest;
import org.openapitools.client.model.AddScopeRequest;
import org.openapitools.client.model.AgentStatusInfoResponse;
import org.openapitools.client.model.AssignNodeScopeRequest;
import org.openapitools.client.model.CollectorPlatformInfoResponse;
import org.openapitools.client.model.CollectorSubPlatformInfoResponse;
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.FacilityInfoResponseP1;
import org.openapitools.client.model.FacilityPathResponse;
import org.openapitools.client.model.FacilityRelationInfoResponse;
import org.openapitools.client.model.FacilityTreeItemResponseP1;
import org.openapitools.client.model.GetAgentValidManagerFacilityIdsResponse;
import org.openapitools.client.model.GetFacilityTreeResponse;
import org.openapitools.client.model.GetFilterNodeListRequest;
import org.openapitools.client.model.GetNodeFullByTargetDatetimeRequest;
import org.openapitools.client.model.GetNodeListRequest;
import org.openapitools.client.model.GetNodeListResponse;
import org.openapitools.client.model.GetNodesBySNMPRequest;
import org.openapitools.client.model.IsNodeResponse;
import org.openapitools.client.model.MapAssociationInfoResponse;
import org.openapitools.client.model.ModifyNodeConfigSettingInfoRequest;
import org.openapitools.client.model.ModifyNodeRequest;
import org.openapitools.client.model.ModifyScopeRequest;
import org.openapitools.client.model.NodeConfigSettingInfoResponse;
import org.openapitools.client.model.NodeInfoDeviceSearchResponse;
import org.openapitools.client.model.NodeInfoResponse;
import org.openapitools.client.model.NodeInfoResponseP1;
import org.openapitools.client.model.NodeInfoResponseP2;
import org.openapitools.client.model.OperationAgentRequest;
import org.openapitools.client.model.OperationAgentResponse;
import org.openapitools.client.model.PingResultResponse;
import org.openapitools.client.model.ReleaseNodeScopeRequest;
import org.openapitools.client.model.ReplaceNodeVariableRequest;
import org.openapitools.client.model.ReplaceNodeVariableResponse;
import org.openapitools.client.model.RepositoryTableInfoResponse;
import org.openapitools.client.model.RunCollectNodeConfigResponse;
import org.openapitools.client.model.ScopeInfoResponseP1;
import org.openapitools.client.model.SearchNodesBySNMPRequest;
import org.openapitools.client.model.SetStatusNodeConfigSettingRequest;
import org.openapitools.client.model.SetValidRequest;

import com.clustercontrol.bean.RestKind;
import com.clustercontrol.fault.FacilityDuplicate;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NodeConfigSettingDuplicate;
import com.clustercontrol.fault.NodeConfigSettingNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.SnmpResponseError;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.rest.client.DefaultApi;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestConnectUnit;
import com.clustercontrol.util.RestUrlSequentialExecuter;

public class RepositoryRestClientWrapper {

	private RestConnectUnit connectUnit;

	private final RestKind restKind = RestKind.RepositoryRestEndpoints;

	public static RepositoryRestClientWrapper getWrapper(String managerName) {
		return new RepositoryRestClientWrapper(RestConnectManager.getActive(managerName));
	}

	public RepositoryRestClientWrapper(RestConnectUnit endpointUnit) {
		this.connectUnit = endpointUnit;
	}

	public FacilityTreeItemResponse getFacilityTree(String ownerRoleId)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<FacilityTreeItemResponseP1> proxy = new RestUrlSequentialExecuter<FacilityTreeItemResponseP1>(this.connectUnit, this.restKind) {
			@Override
			public FacilityTreeItemResponseP1 executeMethod(DefaultApi apiClient) throws Exception {
				FacilityTreeItemResponseP1 result = null;
				GetFacilityTreeResponse dtoRes = apiClient.repositoryGetFacilityTree(ownerRoleId, null);
				if (dtoRes != null) {
					result = new FacilityTreeItemResponseP1();
					result.setData(dtoRes.getData());
					result.setChildren(dtoRes.getChildren());
				}
				return result;
			}
		};
		try {
			return toFacilityTreeItem(proxy.proxyExecute());
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public FacilityTreeItemResponse getExecTargetFacilityTreeByFacilityId(String targetFacilityId, String ownerRoleId)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<FacilityTreeItemResponseP1> proxy = new RestUrlSequentialExecuter<FacilityTreeItemResponseP1>(this.connectUnit, this.restKind) {
			@Override
			public FacilityTreeItemResponseP1 executeMethod(DefaultApi apiClient) throws Exception {
				FacilityTreeItemResponseP1 result = apiClient
						.repositoryGetExecTargetFacilityTreeByFacilityId(targetFacilityId, ownerRoleId);
				return result;
			}
		};
		try {
			return toFacilityTreeItem(proxy.proxyExecute());
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public FacilityTreeItemResponse getNodeFacilityTree(String ownerRoleId)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<FacilityTreeItemResponseP1> proxy = new RestUrlSequentialExecuter<FacilityTreeItemResponseP1>(this.connectUnit, this.restKind) {
			@Override
			public FacilityTreeItemResponseP1 executeMethod(DefaultApi apiClient) throws Exception {
				FacilityTreeItemResponseP1 result = apiClient.repositoryGetNodeFacilityTree(ownerRoleId);
				return result;
			}
		};
		try {
			return toFacilityTreeItem(proxy.proxyExecute());
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<NodeInfoResponseP2> getFilterNodeList(GetFilterNodeListRequest request)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		RestUrlSequentialExecuter<List<NodeInfoResponseP2>> proxy = new RestUrlSequentialExecuter<List<NodeInfoResponseP2>>(this.connectUnit, this.restKind) {
			@Override
			public List<NodeInfoResponseP2> executeMethod(DefaultApi apiClient) throws Exception {
				List<NodeInfoResponseP2> result = apiClient.repositoryGetFilterNodeList(request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<FacilityInfoResponseP1> getExecTargetFacilityIdList(String facilityId, String ownerRoleId)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<List<FacilityInfoResponseP1>> proxy = new RestUrlSequentialExecuter<List<FacilityInfoResponseP1>>(this.connectUnit, this.restKind) {
			@Override
			public List<FacilityInfoResponseP1> executeMethod(DefaultApi apiClient) throws Exception {
				List<FacilityInfoResponseP1> result = apiClient.repositoryGetExecTargetFacilityIdList(facilityId,
						ownerRoleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public NodeInfoResponseP1 getNode(String facilityId)
			throws RestConnectFailed, FacilityNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<NodeInfoResponseP1> proxy = new RestUrlSequentialExecuter<NodeInfoResponseP1>(this.connectUnit, this.restKind) {
			@Override
			public NodeInfoResponseP1 executeMethod(DefaultApi apiClient) throws Exception {
				NodeInfoResponseP1 result = apiClient.repositoryGetNode(facilityId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | FacilityNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public NodeInfoResponse getNodeFull(String facilityId)
			throws RestConnectFailed, FacilityNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<NodeInfoResponse> proxy = new RestUrlSequentialExecuter<NodeInfoResponse>(this.connectUnit, this.restKind) {
			@Override
			public NodeInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				NodeInfoResponse result = apiClient.repositoryGetNodeFull(facilityId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | FacilityNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public NodeInfoResponse getNodeFullByTargetDatetime(String facilityId, GetNodeFullByTargetDatetimeRequest request)
			throws RestConnectFailed, FacilityNotFound, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		RestUrlSequentialExecuter<NodeInfoResponse> proxy = new RestUrlSequentialExecuter<NodeInfoResponse>(this.connectUnit, this.restKind) {
			@Override
			public NodeInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				NodeInfoResponse result = apiClient.repositoryGetNodeFullByTargetDatetime(facilityId, request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | FacilityNotFound | InvalidUserPass | InvalidRole | HinemosUnknown
				| InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public FacilityPathResponse getFacilityPath(String facilityId, String parentFacilityId)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<FacilityPathResponse> proxy = new RestUrlSequentialExecuter<FacilityPathResponse>(this.connectUnit, this.restKind) {
			@Override
			public FacilityPathResponse executeMethod(DefaultApi apiClient) throws Exception {
				FacilityPathResponse result = apiClient.repositoryGetFacilityPath(facilityId, parentFacilityId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public NodeInfoDeviceSearchResponse getNodePropertyBySNMP(GetNodesBySNMPRequest request)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, SnmpResponseError, InvalidSetting {
		RestUrlSequentialExecuter<NodeInfoDeviceSearchResponse> proxy = new RestUrlSequentialExecuter<NodeInfoDeviceSearchResponse>(this.connectUnit, this.restKind) {
			@Override
			public NodeInfoDeviceSearchResponse executeMethod(DefaultApi apiClient) throws Exception {
				NodeInfoDeviceSearchResponse result = apiClient.repositoryGetNodePropertyBySNMP(request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | SnmpResponseError
				| InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public NodeInfoResponse addNode(AddNodeRequest request)
			throws RestConnectFailed, FacilityDuplicate, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		RestUrlSequentialExecuter<NodeInfoResponse> proxy = new RestUrlSequentialExecuter<NodeInfoResponse>(this.connectUnit, this.restKind) {
			@Override
			public NodeInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				NodeInfoResponse result = apiClient.repositoryAddNode(request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | FacilityDuplicate | InvalidUserPass | InvalidRole | HinemosUnknown
				| InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public NodeInfoResponse modifyNode(String facilityId, ModifyNodeRequest request)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		RestUrlSequentialExecuter<NodeInfoResponse> proxy = new RestUrlSequentialExecuter<NodeInfoResponse>(this.connectUnit, this.restKind) {
			@Override
			public NodeInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				NodeInfoResponse result = apiClient.repositoryModifyNode(facilityId, request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<NodeInfoResponse> deleteNode(String facilityIds)
			throws RestConnectFailed, UsedFacility, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<List<NodeInfoResponse>> proxy = new RestUrlSequentialExecuter<List<NodeInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<NodeInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<NodeInfoResponse> result = apiClient.repositoryDeleteNode(facilityIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | UsedFacility | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<FacilityInfoResponse> getFacilityList(String parentFacilityId)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<List<FacilityInfoResponse>> proxy = new RestUrlSequentialExecuter<List<FacilityInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<FacilityInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<FacilityInfoResponse> result = apiClient.repositoryGetFacilityList(parentFacilityId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public ScopeInfoResponseP1 getScope(String facilityId)
			throws RestConnectFailed, FacilityNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<ScopeInfoResponseP1> proxy = new RestUrlSequentialExecuter<ScopeInfoResponseP1>(this.connectUnit, this.restKind) {
			@Override
			public ScopeInfoResponseP1 executeMethod(DefaultApi apiClient) throws Exception {
				ScopeInfoResponseP1 result = apiClient.repositoryGetScope(facilityId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | FacilityNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public ScopeInfoResponseP1 getScopeDefault()
			throws RestConnectFailed, FacilityNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<ScopeInfoResponseP1> proxy = new RestUrlSequentialExecuter<ScopeInfoResponseP1>(this.connectUnit, this.restKind) {
			@Override
			public ScopeInfoResponseP1 executeMethod(DefaultApi apiClient) throws Exception {
				ScopeInfoResponseP1 result = apiClient.repositoryGetScopeDefault();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | FacilityNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public ScopeInfoResponseP1 addScope(AddScopeRequest request)
			throws RestConnectFailed, FacilityDuplicate, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		RestUrlSequentialExecuter<ScopeInfoResponseP1> proxy = new RestUrlSequentialExecuter<ScopeInfoResponseP1>(this.connectUnit, this.restKind) {
			@Override
			public ScopeInfoResponseP1 executeMethod(DefaultApi apiClient) throws Exception {
				ScopeInfoResponseP1 result = apiClient.repositoryAddScope(request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | FacilityDuplicate | InvalidUserPass | InvalidRole | HinemosUnknown
				| InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public ScopeInfoResponseP1 modifyScope(String facilityId, ModifyScopeRequest request)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		RestUrlSequentialExecuter<ScopeInfoResponseP1> proxy = new RestUrlSequentialExecuter<ScopeInfoResponseP1>(this.connectUnit, this.restKind) {
			@Override
			public ScopeInfoResponseP1 executeMethod(DefaultApi apiClient) throws Exception {
				ScopeInfoResponseP1 result = apiClient.repositoryModifyScope(facilityId, request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<ScopeInfoResponseP1> deleteScope(String facilityIds)
			throws RestConnectFailed, UsedFacility, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<List<ScopeInfoResponseP1>> proxy = new RestUrlSequentialExecuter<List<ScopeInfoResponseP1>>(this.connectUnit, this.restKind) {
			@Override
			public List<ScopeInfoResponseP1> executeMethod(DefaultApi apiClient) throws Exception {
				List<ScopeInfoResponseP1> result = apiClient.repositoryDeleteScope(facilityIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | UsedFacility | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<NodeInfoResponseP2> getNodeList(String parentFacilityId, String level)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<List<NodeInfoResponseP2>> proxy = new RestUrlSequentialExecuter<List<NodeInfoResponseP2>>(this.connectUnit, this.restKind) {
			@Override
			public List<NodeInfoResponseP2> executeMethod(DefaultApi apiClient) throws Exception {
				List<NodeInfoResponseP2> result = null;
				GetNodeListResponse dtoRes = apiClient.repositoryGetNodeList(parentFacilityId, null, level);
				if (dtoRes != null) {
					result = dtoRes.getNodeInfoList();
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	// getNodeListAll APIはgetNodeList(String, String)と統合したため、統合先を呼び出す
	public List<NodeInfoResponseP2> getNodeListAll()
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		return getNodeList(null, null);
	}

	public List<FacilityInfoResponseP1> getNodeScopeList(String facilityId)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<List<FacilityInfoResponseP1>> proxy = new RestUrlSequentialExecuter<List<FacilityInfoResponseP1>>(this.connectUnit, this.restKind) {
			@Override
			public List<FacilityInfoResponseP1> executeMethod(DefaultApi apiClient) throws Exception {
				List<FacilityInfoResponseP1> result = apiClient.repositoryGetNodeScopeList(facilityId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<FacilityInfoResponseP1> getFacilityIdList(String parentFacilityId, String level)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<List<FacilityInfoResponseP1>> proxy = new RestUrlSequentialExecuter<List<FacilityInfoResponseP1>>(this.connectUnit, this.restKind) {
			@Override
			public List<FacilityInfoResponseP1> executeMethod(DefaultApi apiClient) throws Exception {
				List<FacilityInfoResponseP1> result = apiClient.repositoryGetFacilityIdList(parentFacilityId, level);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<FacilityInfoResponseP1> getNodeFacilityIdList(String parentFacilityId, String ownerRoleId, String level)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<List<FacilityInfoResponseP1>> proxy = new RestUrlSequentialExecuter<List<FacilityInfoResponseP1>>(this.connectUnit, this.restKind) {
			@Override
			public List<FacilityInfoResponseP1> executeMethod(DefaultApi apiClient) throws Exception {
				List<FacilityInfoResponseP1> result = apiClient.repositoryGetNodeFacilityIdList(parentFacilityId,
						ownerRoleId, level);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<FacilityRelationInfoResponse> assignNodeScope(String parentFacilityId, AssignNodeScopeRequest request)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		RestUrlSequentialExecuter<List<FacilityRelationInfoResponse>> proxy = new RestUrlSequentialExecuter<List<FacilityRelationInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<FacilityRelationInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<FacilityRelationInfoResponse> result = apiClient.repositoryAssignNodeScope(parentFacilityId,
						request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<FacilityRelationInfoResponse> releaseNodeScope(String parentFacilityId, ReleaseNodeScopeRequest request)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		RestUrlSequentialExecuter<List<FacilityRelationInfoResponse>> proxy = new RestUrlSequentialExecuter<List<FacilityRelationInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<FacilityRelationInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<FacilityRelationInfoResponse> result = apiClient.repositoryReleaseNodeScope(parentFacilityId,
						request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public IsNodeResponse isNode(String facilityId)
			throws RestConnectFailed, FacilityNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<IsNodeResponse> proxy = new RestUrlSequentialExecuter<IsNodeResponse>(this.connectUnit, this.restKind) {
			@Override
			public IsNodeResponse executeMethod(DefaultApi apiClient) throws Exception {
				IsNodeResponse result = apiClient.repositoryIsNode(facilityId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | FacilityNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<RepositoryTableInfoResponse> getPlatformList()
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<RepositoryTableInfoResponse>> proxy = new RestUrlSequentialExecuter<List<RepositoryTableInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<RepositoryTableInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<RepositoryTableInfoResponse> result = apiClient.repositoryGetPlatformList();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<RepositoryTableInfoResponse> getCollectorSubPlatformTableInfoList()
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<RepositoryTableInfoResponse>> proxy = new RestUrlSequentialExecuter<List<RepositoryTableInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<RepositoryTableInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<RepositoryTableInfoResponse> result = apiClient.repositoryGetCollectorSubPlatformTableInfoList();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public NodeInfoResponse setValid(String facilityId, SetValidRequest request)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown, FacilityNotFound, InvalidSetting {
		RestUrlSequentialExecuter<NodeInfoResponse> proxy = new RestUrlSequentialExecuter<NodeInfoResponse>(this.connectUnit, this.restKind) {
			@Override
			public NodeInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				NodeInfoResponse result = apiClient.repositorySetValid(facilityId, request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | FacilityNotFound
				| InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public Long getLastUpdate() throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<Long> proxy = new RestUrlSequentialExecuter<Long>(this.connectUnit, this.restKind) {
			@Override
			public Long executeMethod(DefaultApi apiClient) throws Exception {
				Long result = apiClient.repositoryGetLastUpdate();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<AgentStatusInfoResponse> getAgentStatusList()
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<List<AgentStatusInfoResponse>> proxy = new RestUrlSequentialExecuter<List<AgentStatusInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<AgentStatusInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<AgentStatusInfoResponse> result = apiClient.repositoryGetAgentStatusList();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<OperationAgentResponse> operationAgent(OperationAgentRequest request)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		RestUrlSequentialExecuter<List<OperationAgentResponse>> proxy = new RestUrlSequentialExecuter<List<OperationAgentResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<OperationAgentResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<OperationAgentResponse> result = apiClient.repositoryOperationAgent(request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public ReplaceNodeVariableResponse replaceNodeVariable(ReplaceNodeVariableRequest request)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		RestUrlSequentialExecuter<ReplaceNodeVariableResponse> proxy = new RestUrlSequentialExecuter<ReplaceNodeVariableResponse>(this.connectUnit, this.restKind) {
			@Override
			public ReplaceNodeVariableResponse executeMethod(DefaultApi apiClient) throws Exception {
				ReplaceNodeVariableResponse result = apiClient.repositoryReplaceNodeVariable(request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<NodeInfoDeviceSearchResponse> searchNodesBySNMP(SearchNodesBySNMPRequest request)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, FacilityDuplicate, InvalidSetting {
		RestUrlSequentialExecuter<List<NodeInfoDeviceSearchResponse>> proxy = new RestUrlSequentialExecuter<List<NodeInfoDeviceSearchResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<NodeInfoDeviceSearchResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<NodeInfoDeviceSearchResponse> result = apiClient.repositorySearchNodesBySNMP(request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | FacilityDuplicate
				| InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public NodeConfigSettingInfoResponse addNodeConfigSettingInfo(AddNodeConfigSettingInfoRequest request)
			throws RestConnectFailed, NodeConfigSettingDuplicate, InvalidUserPass, InvalidRole, HinemosUnknown,
			InvalidSetting {
		RestUrlSequentialExecuter<NodeConfigSettingInfoResponse> proxy = new RestUrlSequentialExecuter<NodeConfigSettingInfoResponse>(this.connectUnit, this.restKind) {
			@Override
			public NodeConfigSettingInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				NodeConfigSettingInfoResponse result = apiClient.repositoryAddNodeConfigSettingInfo(request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NodeConfigSettingDuplicate | InvalidUserPass | InvalidRole | HinemosUnknown
				| InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public NodeConfigSettingInfoResponse modifyNodeConfigSettingInfo(String settingId,
			ModifyNodeConfigSettingInfoRequest request)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		RestUrlSequentialExecuter<NodeConfigSettingInfoResponse> proxy = new RestUrlSequentialExecuter<NodeConfigSettingInfoResponse>(this.connectUnit, this.restKind) {
			@Override
			public NodeConfigSettingInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				NodeConfigSettingInfoResponse result = apiClient.repositoryModifyNodeConfigSettingInfo(settingId,
						request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<NodeConfigSettingInfoResponse> deleteNodeConfigSettingInfo(String settingIds)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<List<NodeConfigSettingInfoResponse>> proxy = new RestUrlSequentialExecuter<List<NodeConfigSettingInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<NodeConfigSettingInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<NodeConfigSettingInfoResponse> result = apiClient
						.repositoryDeleteNodeConfigSettingInfo(settingIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<NodeConfigSettingInfoResponse> setStatusNodeConfigSetting(SetStatusNodeConfigSettingRequest request)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<List<NodeConfigSettingInfoResponse>> proxy = new RestUrlSequentialExecuter<List<NodeConfigSettingInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<NodeConfigSettingInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<NodeConfigSettingInfoResponse> result = apiClient.repositorySetStatusNodeConfigSetting(request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public NodeConfigSettingInfoResponse getNodeConfigSettingInfo(String settingId)
			throws RestConnectFailed, NodeConfigSettingNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<NodeConfigSettingInfoResponse> proxy = new RestUrlSequentialExecuter<NodeConfigSettingInfoResponse>(this.connectUnit, this.restKind) {
			@Override
			public NodeConfigSettingInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				NodeConfigSettingInfoResponse result = apiClient.repositoryGetNodeConfigSettingInfo(settingId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | NodeConfigSettingNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<NodeConfigSettingInfoResponse> getNodeConfigSettingList()
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<List<NodeConfigSettingInfoResponse>> proxy = new RestUrlSequentialExecuter<List<NodeConfigSettingInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<NodeConfigSettingInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<NodeConfigSettingInfoResponse> result = apiClient.repositoryGetNodeConfigSettingList();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public RunCollectNodeConfigResponse runCollectNodeConfig(String settingId) throws RestConnectFailed,
			FacilityNotFound, InvalidUserPass, InvalidRole, NodeConfigSettingNotFound, HinemosUnknown {
		RestUrlSequentialExecuter<RunCollectNodeConfigResponse> proxy = new RestUrlSequentialExecuter<RunCollectNodeConfigResponse>(this.connectUnit, this.restKind) {
			@Override
			public RunCollectNodeConfigResponse executeMethod(DefaultApi apiClient) throws Exception {
				RunCollectNodeConfigResponse result = apiClient.repositoryRunCollectNodeConfig(settingId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | FacilityNotFound | InvalidUserPass | InvalidRole | NodeConfigSettingNotFound
				| HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<NodeInfoResponse> searchNode(GetNodeListRequest request) throws RestConnectFailed, FacilityNotFound,
			InvalidUserPass, InvalidRole, InvalidSetting, HinemosDbTimeout, HinemosUnknown {
		RestUrlSequentialExecuter<List<NodeInfoResponse>> proxy = new RestUrlSequentialExecuter<List<NodeInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<NodeInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<NodeInfoResponse> result = apiClient.repositorySearchNode(request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | InvalidSetting | HinemosDbTimeout
				| HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<PingResultResponse> ping(String facilityId)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<List<PingResultResponse>> proxy = new RestUrlSequentialExecuter<List<PingResultResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<PingResultResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<PingResultResponse> result = apiClient.repositoryPing(facilityId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public AddFilterScopeResponse addFilterScope(AddFilterScopeRequest request)
			throws RestConnectFailed, InvalidUserPass, FacilityDuplicate, InvalidSetting, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<AddFilterScopeResponse> proxy = new RestUrlSequentialExecuter<AddFilterScopeResponse>(this.connectUnit, this.restKind) {
			@Override
			public AddFilterScopeResponse executeMethod(DefaultApi apiClient) throws Exception {
				AddFilterScopeResponse result = apiClient.repositoryAddFilterScope(request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | FacilityDuplicate | InvalidSetting | InvalidRole
				| HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public GetAgentValidManagerFacilityIdsResponse getAgentValidManagerFacilityIds()
			throws RestConnectFailed, InvalidSetting, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<GetAgentValidManagerFacilityIdsResponse> proxy = new RestUrlSequentialExecuter<GetAgentValidManagerFacilityIdsResponse>(this.connectUnit, this.restKind) {
			@Override
			public GetAgentValidManagerFacilityIdsResponse executeMethod(DefaultApi apiClient) throws Exception {
				GetAgentValidManagerFacilityIdsResponse result = apiClient.repositoryGetAgentValidManagerFacilityIds();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidSetting | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<FacilityRelationInfoResponse> addNodeAndAssignScopeFromInstance(String cloudScopeId, String locationId,
			String instanceId) throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		RestUrlSequentialExecuter<List<FacilityRelationInfoResponse>> proxy = new RestUrlSequentialExecuter<List<FacilityRelationInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<FacilityRelationInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				AddNodeAndAssignScopeFromInstanceRequest dtoReq = new AddNodeAndAssignScopeFromInstanceRequest();
				dtoReq.setCloudScopeId(cloudScopeId);
				dtoReq.setInstanceId(instanceId);
				dtoReq.setLocationId(locationId);
				List<FacilityRelationInfoResponse> result = apiClient.repositoryAddNodeAndAssignScopeFromInstance(dtoReq);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<MapAssociationInfoResponse> getL2ConnectionMap(String scopeId)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<List<MapAssociationInfoResponse>> proxy = new RestUrlSequentialExecuter<List<MapAssociationInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<MapAssociationInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<MapAssociationInfoResponse> result = apiClient.repositoryGetL2ConnectionMap(scopeId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<MapAssociationInfoResponse> getL3ConnectionMap(String scopeId)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<List<MapAssociationInfoResponse>> proxy = new RestUrlSequentialExecuter<List<MapAssociationInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<MapAssociationInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<MapAssociationInfoResponse> result = apiClient.repositoryGetL3ConnectionMap(scopeId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public CollectorPlatformInfoResponse addCollectPlatformMaster(AddCollectPlatformMasterRequest request)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<CollectorPlatformInfoResponse> proxy = new RestUrlSequentialExecuter<CollectorPlatformInfoResponse>(this.connectUnit, this.restKind) {
			@Override
			public CollectorPlatformInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CollectorPlatformInfoResponse result = apiClient.repositoryAddCollectPlatformMaster(request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public CollectorSubPlatformInfoResponse addCollectSubPlatformMaster(AddCollectSubPlatformMasterRequest request)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<CollectorSubPlatformInfoResponse> proxy = new RestUrlSequentialExecuter<CollectorSubPlatformInfoResponse>(this.connectUnit, this.restKind) {
			@Override
			public CollectorSubPlatformInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CollectorSubPlatformInfoResponse result = apiClient.repositoryAddCollectSubPlatformMaster(request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<CollectorPlatformInfoResponse> deleteCollectPlatformMaster(String platformIds)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<CollectorPlatformInfoResponse>> proxy = new RestUrlSequentialExecuter<List<CollectorPlatformInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<CollectorPlatformInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<CollectorPlatformInfoResponse> result = apiClient
						.repositoryDeleteCollectPlatformMaster(platformIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<CollectorSubPlatformInfoResponse> deleteCollectSubPlatformMaster(String subPlatformIds)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<CollectorSubPlatformInfoResponse>> proxy = new RestUrlSequentialExecuter<List<CollectorSubPlatformInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<CollectorSubPlatformInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<CollectorSubPlatformInfoResponse> result = apiClient
						.repositoryDeleteCollectSubPlatformMaster(subPlatformIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<CollectorPlatformInfoResponse> getCollectPlatformMasterList()
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<CollectorPlatformInfoResponse>> proxy = new RestUrlSequentialExecuter<List<CollectorPlatformInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<CollectorPlatformInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<CollectorPlatformInfoResponse> result = apiClient.repositoryGetCollectPlatformMasterList();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<CollectorSubPlatformInfoResponse> getCollectSubPlatformMasterList()
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<CollectorSubPlatformInfoResponse>> proxy = new RestUrlSequentialExecuter<List<CollectorSubPlatformInfoResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<CollectorSubPlatformInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<CollectorSubPlatformInfoResponse> result = apiClient.repositoryGetCollectSubPlatformMasterList();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public Boolean sendManagerDiscoveryInfo(String facilityId) throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<Boolean> proxy = new RestUrlSequentialExecuter<Boolean>(this.connectUnit, this.restKind) {
			@Override
			public Boolean executeMethod(DefaultApi apiClient) throws Exception {
				Boolean result = apiClient.repositorySendManagerDiscoveryInfo(facilityId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	/**
	 * FacilityTreeItemResponseP1をFacilityTreeItemResponseに変換して返却 <BR>
	 * マネージャのFacilityTreeItemResponseP1にparentが無いための実装
	 * 
	 * @param response
	 * @return
	 */
	private static FacilityTreeItemResponse toFacilityTreeItem(FacilityTreeItemResponseP1 response) throws HinemosUnknown {
		FacilityTreeItemResponse ret = new FacilityTreeItemResponse();
		convertFacilityTreeItem(response, ret);
		return ret;
	}

	/**
	 * FacilityTreeItemResponseP1をFacilityTreeItemResponseに変換（再帰）
	 * 
	 * @param src
	 * @param dest
	 */
	private static void convertFacilityTreeItem(FacilityTreeItemResponseP1 src, FacilityTreeItemResponse dest) throws HinemosUnknown {

		FacilityInfoResponse destData = new FacilityInfoResponse();
		RestClientBeanUtil.convertBean(src.getData(), destData);
		dest.setData(destData);
		String facilityName = dest.getData().getFacilityName();
		dest.getData().setFacilityName(HinemosMessage.replace(facilityName));

		if (src.getChildren() == null) {
			return;
		}

		dest.setChildren(new ArrayList<>());
		for (FacilityTreeItemResponseP1 child : src.getChildren()) {
			FacilityTreeItemResponse destChild = new FacilityTreeItemResponse();
			destChild.setParent(dest);
			dest.getChildren().add(destChild);
			convertFacilityTreeItem(child, destChild);
		}
	}
}
