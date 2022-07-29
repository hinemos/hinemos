/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa.factory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.IpAddressInfo;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.CollectionComparator;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityDuplicate;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.RpaManagementToolMasterNotFound;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.PlatformConstant;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.NodeHostnameInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.ScopeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.rpa.factory.bean.RpaResourceInfo;
import com.clustercontrol.rpa.model.RpaManagementToolAccount;
import com.clustercontrol.rpa.model.RpaManagementToolMst;
import com.clustercontrol.rpa.session.CacheRpaResourceManagement;
import com.clustercontrol.rpa.util.QueryUtil;
import com.clustercontrol.rpa.util.RpaConstants;
import com.clustercontrol.rpa.util.RpaUtil;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.NetworkInterfaceUtil;
import com.clustercontrol.util.apllog.AplLogger;

public class RepositoryUpdater {
	private static Log m_log = LogFactory.getLog( RepositoryUpdater.class );

	private RepositoryControllerBean bean;

	public RepositoryUpdater() {
		this.bean = new RepositoryControllerBean();
	}

	/**
	 * RPA管理ツールアカウントを表すスコープを登録、およびその配下にRPAリソースノードを登録する。
	 */
	public void update(RpaManagementToolAccount account) throws HinemosUnknown, IOException {
		m_log.info(String.format("update() update RPA Resources. RpaScopeId=%s", account.getRpaScopeId()));
		CacheRpaResourceManagement cacheRm = new CacheRpaResourceManagement(account);
		// RPA管理ツールからリソース情報を取得
		try {
			m_log.debug(String.format("update() collect RPA Resources. RpaScopeId=%s", account.getRpaScopeId()));
			cacheRm.update();
		} catch (RpaManagementToolMasterNotFound e) {
			m_log.warn(String.format("update() collecting RPA Resources failure. RpaScopeId=%s", account.getRpaScopeId()), e);
			throw new HinemosUnknown(e);
		}

		// RPA管理ツールアカウントスコープの登録、または更新
		m_log.debug(String.format("update() update RPA Management Scope. RpaScopeId=%s", account.getRpaScopeId()));
		ScopeInfo scopeInfo = updateRpaManagementScope(cacheRm.getRpaManagementToolAccount(), cacheRm.getRpaManagementToolMst());


		// RPA管理ツールのリソース自動検知が有効な場合、スコープ配下のノードを更新
		// スコープをキャッシュツリーに反映させておく必要がある都合上、スコープの更新とは別トランザクションで行う。
		if (!HinemosPropertyCommon.rpa_autoupdate_enable.getBooleanValue()) {
			// プロパティで自動検知が無効な場合は実施しない。
			return;
		}	
		updateRpaNodes(cacheRm, scopeInfo.getFacilityId());
	}
	
	// 引数のRPA管理ツールアカウントのリソースを削除する。
	public void remove(RpaManagementToolAccount account) throws HinemosUnknown, UsedFacility, InvalidRole {
		m_log.info(String.format("remove() remove RPA Resources RpaScopeId=%s", account.getRpaScopeId()));
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			jtm.begin();

			// RPA管理ツールマスタを取得
			RpaManagementToolMst master = QueryUtil.getRpaManagementToolMstPK(account.getRpaManagementToolId());
			
			// RPA管理ツールアカウントスコープのファシリティIDを取得
			String parentFacilityId = RpaUtil.generateRpaManagementScopeId(account, master);
			
			m_log.trace(String.format("remove() remove RPA Management Scope. facilityId=%s", parentFacilityId));
			
			// RPA管理ツールアカウントスコープ直下のノード一覧を取得
			List<FacilityInfo> facilityList = bean.getFacilityList(parentFacilityId);
			m_log.trace(String.format("remove() remove Facilities. facilityList=%s", facilityList));
			List<String> nodeIdList = facilityList
					.stream()
					.filter(facility -> facility.getFacilityType() == FacilityConstant.TYPE_NODE)
					.map(FacilityInfo::getFacilityId)
					.collect(Collectors.toList());
			m_log.trace(String.format("remove() remove Nodes. nodeIdList=%s", nodeIdList));
			// 配下ノードを削除
			bean.deleteNode(nodeIdList.toArray(new String[nodeIdList.size()]));
			
			// RPA管理ツールアカウントスコープを削除
			m_log.trace(String.format("remove() remove Scope. facilityId=%s", parentFacilityId));
			bean.deleteScope(new String[]{parentFacilityId});
			
			jtm.commit();
		} catch (RpaManagementToolMasterNotFound | FacilityNotFound e) {
			// 想定外エラー
			m_log.warn(e.getMessage(), e);
			throw new HinemosUnknown(e);
		}
	}
	
	// RPA管理ツールアカウントのスコープを更新
	private ScopeInfo updateRpaManagementScope(RpaManagementToolAccount account, RpaManagementToolMst master) throws HinemosUnknown {
		m_log.debug(String.format("update() update RPA Management Scope RpaScopeId=%s", account.getRpaScopeId()));
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			jtm.begin();


			String facilityId = RpaUtil.generateRpaManagementScopeId(account, master);
			ScopeInfo rpaManagementScope = getScope(facilityId);
			
			// RPA管理製品スコープが存在しない場合は新規にスコープを登録する。
			if (rpaManagementScope == null) {
				ScopeInfo newScope = new ScopeInfo(facilityId);
				newScope.setFacilityName(account.getRpaScopeName());
				newScope.setOwnerRoleId(account.getOwnerRoleId());
				newScope.setDescription(account.getDescription());
				try {
					rpaManagementScope = bean.addScope(RpaConstants.RPA, newScope);
				} catch (Exception e) {
					throw new HinemosUnknown(e);
				}
				
				m_log.info(String.format("add RPA Management Scope. facilityId=%s",facilityId));
			} else {
				// アカウント情報が更新されていたらスコープ情報を更新
				if (!rpaManagementScope.getFacilityName().equals(account.getRpaScopeName()) ||
						!rpaManagementScope.getDescription().equals(account.getDescription())) {
					m_log.info(String.format("update RPA Management Scope. facilityId=%s",facilityId));
					rpaManagementScope.setFacilityName(account.getRpaScopeName());
					rpaManagementScope.setDescription(account.getDescription());
					
					try {
						rpaManagementScope = bean.modifyScope(rpaManagementScope);
					} catch (Exception e) {
						throw new HinemosUnknown(e);
					}
				}
			}

			jtm.commit();
			return rpaManagementScope;
		}
	}
	
	// RPAリソースノードの更新を行う。
	// ノード1件毎にコミットしないとツリーキャッシュに反映されないため、それぞれ別トランザクションで更新する。
	private void updateRpaNodes(CacheRpaResourceManagement cacheRm, String parentFacilityId) throws HinemosUnknown {
		// RPA管理ツールアカウントスコープ直下のファシリティを取得
		List<FacilityInfo> facilityList = bean.getFacilityList(parentFacilityId);
		
		// RPA管理ツールアカウントスコープ配下のノード情報を取得
		List<NodeInfo> nodeList = new ArrayList<>();
		for (FacilityInfo facility : facilityList) {
			switch (facility.getFacilityType()) {
			case FacilityConstant.TYPE_NODE:
				// ノード情報を取得
				try {
					nodeList.add(bean.getNode(facility.getFacilityId()));
				} catch (FacilityNotFound e) {
					m_log.warn(e.getMessage(), e);
				}
				break;
			case FacilityConstant.TYPE_SCOPE:
			default:
				// スコープの場合は削除する。
				try {
					bean.deleteScope(new String[] {facility.getFacilityId()});
				} catch (UsedFacility | InvalidRole | FacilityNotFound e) {
					m_log.warn("update(): failed to delete scope, facilityId = " + facility.getFacilityId() + ", " + e.getMessage(), e);
				}
				break;
			}
		}
		
		// ノード情報とRPAリソースを比較し、更新を行う。
		List<RpaResourceInfo> resourceInfoList = cacheRm.getResourceInfos();
		m_log.info(String.format("update() detecting nodes. rpaScopeId=%s", cacheRm.getRpaManagementToolAccount().getRpaScopeId()));
		m_log.debug(String.format("nodeList=%s, resourceInfoList=%s", nodeList, resourceInfoList));
		
		// 追加、削除したノードをカウント
		List<String> addNodes = new ArrayList<>();
		List<String> deleteNodes = new ArrayList<>();
		
		CollectionComparator.compare(nodeList, resourceInfoList, 
			new CollectionComparator.Comparator<NodeInfo, RpaResourceInfo>() {
				@Override
				public boolean match(NodeInfo o1, RpaResourceInfo o2) throws HinemosUnknown {
					return o1.getRpaExecEnvId().equals(o2.getRpaExecEnvId());
				}
	
				@Override
				public void matched(NodeInfo o1, RpaResourceInfo o2) throws HinemosUnknown {
					try (JpaTransactionManager jtm = new JpaTransactionManager()) {
						jtm.begin();
						updateNode(o1, o2);
						jtm.commit();
					} catch (Exception e) {
						m_log.warn("update(): failed to update node, facilityId = " + o1.getFacilityId() + ", " + e.getMessage(), e);
					}
				}
	
				@Override
				public void afterO1(NodeInfo o1) throws HinemosUnknown {
					// RPAリソースに含まれない(削除された)ノードを削除
					try (JpaTransactionManager jtm = new JpaTransactionManager()) {
						jtm.begin();
						bean.deleteNode(new String[]{o1.getFacilityId()});
						m_log.info(String.format("update(): delete RPA Node. facilityId=%s", o1.getFacilityId()));
						jtm.commit();
						deleteNodes.add(o1.getFacilityId());
					} catch (Exception e) {
						m_log.warn("update(): failed to delete node, facilityId=" + o1.getFacilityId() + ", " + e.getMessage(), e);
					}
				}
	
				@Override
				public void afterO2(RpaResourceInfo o2) throws HinemosUnknown {
					// RPAリソースでノードを登録
					try (JpaTransactionManager jtm = new JpaTransactionManager()) {
						jtm.begin();
						NodeInfo result = registNode(o2, cacheRm.getRpaManagementToolAccount(), cacheRm.getRpaManagementToolMst());
						m_log.info(String.format("update(): add RPA Resource Node. RpaExecEnvId=%s", o2.getRpaExecEnvId()));
						// スコープに割当
						bean.assignNodeScope(parentFacilityId, new String[]{result.getFacilityId()});
						jtm.commit();
						addNodes.add(result.getFacilityId());
					} catch (Exception e) {
						m_log.warn("update(): failed to add RPA Resource node, rpaExecEnvId = " + o2.getRpaExecEnvId() + ", " + e.getMessage(), e);
					}
				}
			}
		);
		m_log.info(String.format("update(): finish detecting. rpaScopeId = %s", cacheRm.getRpaManagementToolAccount().getRpaScopeId()));
		
		// 追加されたノードがある場合、INTERNAL通知を行う。
		if (HinemosPropertyCommon.rpa_autoupdate_notify_add_node.getBooleanValue() && addNodes.size() != 0) {
			AplLogger.put(InternalIdCommon.RPA_SYS_001, PriorityConstant.TYPE_INFO, new String[]{String.valueOf(addNodes.size())} ,
					MessageConstant.MESSAGE_RPA_AUTO_DETECT_ADD_NODE_ORG.getMessage(String.join(", ", addNodes)));
		}
		
		// 削除されたノードがある場合、INTERNAL通知を行う。
		if (HinemosPropertyCommon.rpa_autoupdate_notify_delete_node.getBooleanValue() && deleteNodes.size() != 0) {
			AplLogger.put(InternalIdCommon.RPA_SYS_003, PriorityConstant.TYPE_INFO, new String[]{String.valueOf(deleteNodes.size())} ,
					MessageConstant.MESSAGE_RPA_AUTO_DETECT_DELETE_NODE_ORG.getMessage(String.join(", ", deleteNodes)));
		}
	}
		
	private NodeInfo registNode(RpaResourceInfo resourceInfo, RpaManagementToolAccount account, RpaManagementToolMst master) throws FacilityDuplicate, InvalidSetting, HinemosUnknown {

		NodeInfo nodeInfo = new NodeInfo(RpaUtil.generateRpaResourceNodeId(resourceInfo, account, master));
		
		// Hinemosプロパティで定義されるデフォルト値をセット
		RpaUtil.setDefaultNodeInfoValue(nodeInfo);

		// オーナーロールID
		nodeInfo.setOwnerRoleId(account.getOwnerRoleId());
		
		// ファシリティ名
		if (!resourceInfo.getHostName().isEmpty()) {
			nodeInfo.setFacilityName(resourceInfo.getHostName());
		} else {
			nodeInfo.setFacilityName(resourceInfo.getIpAddress());
		}
		
		// ノード名
		if (!resourceInfo.getNodeName().isEmpty()) {
			nodeInfo.setNodeName(resourceInfo.getNodeName());
		} else {
			nodeInfo.setNodeName(resourceInfo.getIpAddress());			
		}

		// 説明
		nodeInfo.setDescription(RpaConstants.autoRegistNodeDescription);
		
		// 管理対象フラグ:ON
		nodeInfo.setValid(true);
		
		// 自動デバイスサーチ:OFF
		nodeInfo.setAutoDeviceSearch(false);
		
		// プラットフォーム:WINDOWS
		nodeInfo.setPlatformFamily(PlatformConstant.WINDOWS);
		
		// サブプラットフォーム:ブランク
		nodeInfo.setSubPlatformFamily("");
		
		// IPアドレス
		setIp(nodeInfo, resourceInfo.getIpAddress());
		
		// ホスト名
		// 未登録の場合はIPアドレスをセット
		m_log.info("set Hostname");
		if (!resourceInfo.getHostName().isEmpty()) {
			NodeHostnameInfo newHostname = new NodeHostnameInfo(nodeInfo.getFacilityId(), resourceInfo.getHostName());
			nodeInfo.getNodeHostnameInfo().add(newHostname);
		} else {
			NodeHostnameInfo newHostname = new NodeHostnameInfo(nodeInfo.getFacilityId(), resourceInfo.getIpAddress());
			nodeInfo.getNodeHostnameInfo().add(newHostname);
		}
		
		// RPA管理ツールタイプ
		nodeInfo.setRpaManagementToolType(master.getRpaManagementToolName());

		// RPAリソースID
		nodeInfo.setRpaResourceId(account.getRpaScopeId());

		// RPAユーザ
		nodeInfo.setRpaUser(resourceInfo.getRpaUser());

		// RPA実行環境ID
		nodeInfo.setRpaExecEnvId(resourceInfo.getRpaExecEnvId());

		// ノードを登録
		NodeInfo ret = bean.addNode(nodeInfo); 
		return ret;
	}

	// リソース情報に応じてノード情報を更新
	private void updateNode(NodeInfo nodeInfo, RpaResourceInfo resourceInfo) throws InvalidSetting, InvalidRole, FacilityNotFound, HinemosUnknown {
		StringBuilder changeLog = new StringBuilder();
		// INTERNAL通知のオリジナルメッセージ
		StringBuilder messageOrg = new StringBuilder();
		
		// 各リソース情報に基づいてノード情報を更新
		// リソース情報が有効な場合のみ更新する(空の情報では更新しない)
		
		// ファシリティ名
		if (!nodeInfo.getFacilityName().equals(resourceInfo.getFacilityName()) && !resourceInfo.getFacilityName().isEmpty()) {
			changeLog.append("FacilityName:").append(nodeInfo.getFacilityName()).append("->").append(resourceInfo.getFacilityName()).append(";");
			messageOrg.append(
					MessageConstant.MESSAGE_RPA_AUTO_DETECT_CHANGE_NODE_ORG.getMessage(
							MessageConstant.FACILITY_NAME.getMessage(), 
							nodeInfo.getFacilityName(), 
							resourceInfo.getFacilityName()))
			.append("\n");
			nodeInfo.setFacilityName(resourceInfo.getFacilityName());
		}
		
		// IPアドレス
		String oldIp;
		if (nodeInfo.getIpAddressVersion() == 4) {
			oldIp = nodeInfo.getIpAddressV4();
		} else {
			oldIp = nodeInfo.getIpAddressV6();
		}
		
		if (!oldIp.equals(resourceInfo.getIpAddress()) && !resourceInfo.getIpAddress().isEmpty()) {
			setIp(nodeInfo, resourceInfo.getIpAddress());
			String newIp;
			if (nodeInfo.getIpAddressVersion() == 4) {
				newIp = nodeInfo.getIpAddressV4();
			} else {
				newIp = nodeInfo.getIpAddressV6();
			}
			changeLog.append("IPAddress:").append(oldIp).append("->").append(newIp).append(";");
			messageOrg.append(
					MessageConstant.MESSAGE_RPA_AUTO_DETECT_CHANGE_NODE_ORG.getMessage(
							MessageConstant.IP_ADDRESS.getMessage(), 
							oldIp, 
							newIp))
			.append("\n");
		}

		// ホスト名
		if (!nodeInfo.getNodeHostnameInfo().get(0).getHostname().equals(resourceInfo.getHostName()) && !resourceInfo.getHostName().isEmpty()) {
			changeLog.append("HostName:").append(nodeInfo.getNodeHostnameInfo().get(0).getHostname()).append("->").append(resourceInfo.getHostName()).append(";");
			messageOrg.append(
					MessageConstant.MESSAGE_RPA_AUTO_DETECT_CHANGE_NODE_ORG.getMessage(
							MessageConstant.HOST_NAME.getMessage(), 
							nodeInfo.getNodeHostnameInfo().get(0).getHostname(),
							resourceInfo.getHostName()))
					.append("\n");
			NodeHostnameInfo newHostname = new NodeHostnameInfo(nodeInfo.getFacilityId(), resourceInfo.getHostName());
			nodeInfo.getNodeHostnameInfo().set(0, newHostname);
		}
		
		// ノード名
		if (!nodeInfo.getNodeName().equals(resourceInfo.getNodeName()) && !resourceInfo.getNodeName().isEmpty()) {
			changeLog.append("NodeName:").append(nodeInfo.getNodeName()).append("->").append(resourceInfo.getNodeName()).append(";");
			messageOrg.append(
					MessageConstant.MESSAGE_RPA_AUTO_DETECT_CHANGE_NODE_ORG.getMessage(
							MessageConstant.NODE_NAME.getMessage(), 
							nodeInfo.getNodeName(),
							resourceInfo.getNodeName()))
					.append("\n");
			nodeInfo.setNodeName(resourceInfo.getNodeName());
		}
		
		// ユーザ名
		if (!nodeInfo.getRpaUser().equals(resourceInfo.getRpaUser()) && !resourceInfo.getRpaUser().isEmpty()) {
			changeLog.append("RpaUser:").append(nodeInfo.getRpaUser()).append("->").append(resourceInfo.getRpaUser()).append(";");
			messageOrg.append(
					MessageConstant.MESSAGE_RPA_AUTO_DETECT_CHANGE_NODE_ORG.getMessage(
							MessageConstant.RPA_USER.getMessage(), 
							nodeInfo.getRpaUser(),
							resourceInfo.getRpaUser()))
					.append("\n");
			nodeInfo.setRpaUser(resourceInfo.getRpaUser());
		}
		
		if (0 < changeLog.length()) {
			bean.modifyNode(nodeInfo);
			// INTERNAL通知を行う。
			
			if (HinemosPropertyCommon.rpa_autoupdate_notify_change_node.getBooleanValue()) {
				AplLogger.put(InternalIdCommon.RPA_SYS_002, PriorityConstant.TYPE_INFO, new String[]{nodeInfo.getFacilityId()} ,
						messageOrg.toString());
			}
			m_log.info(String.format("Update node properties. FacilityID=%s, RpaExecEnvId=%s, log=%s", nodeInfo.getFacilityId(),
					resourceInfo.getRpaExecEnvId(), changeLog.toString()));
		}
	}

	private ScopeInfo getScope(String facilityId) throws HinemosUnknown {
		try {
			return bean.getScope(facilityId);
		} catch (FacilityNotFound e) {
			// ファシリティIDが存在しない場合はnullを返す。
			return null;
		} catch (Exception e) {
			throw new HinemosUnknown(e);
		}
	}
	
	/**
	 * RPAリソースのノードにIPアドレス情報をセットする。
	 */
	private void setIp(NodeInfo nodeInfo, String ipAddress) {
		// IPアドレス
		if (ipAddress.isEmpty()) {
			// IPアドレス情報が空の場合、Hinemosプロパティで定義されたデフォルト値を登録
			nodeInfo.setIpAddressVersion(4);
			nodeInfo.setIpAddressV4(HinemosPropertyCommon.rpa_ipaddress_notavailable.getStringValue());
			nodeInfo.setIpAddressV6("");
		} else {
			IpAddressInfo ipInfo = new IpAddressInfo();
			try {
				ipInfo = NetworkInterfaceUtil.getIpAddressInfo(ipAddress);
			} catch (InvalidSetting | HinemosUnknown e) {
				m_log.warn(e.getMessage(), e);
			}
			
			switch (ipInfo.getVersion()) {
			case IPV6:
				nodeInfo.setIpAddressVersion(6);
				nodeInfo.setIpAddressV6(ipAddress);
				nodeInfo.setIpAddressV4("");
				break;
			case IPV4:
			default:
				nodeInfo.setIpAddressVersion(4);
				nodeInfo.setIpAddressV4(ipAddress);
				nodeInfo.setIpAddressV6("");
				break;
			}
		}
	}
}
