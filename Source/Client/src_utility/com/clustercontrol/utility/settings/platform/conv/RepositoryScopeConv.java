/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.platform.conv;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.ScopeInfoResponseP1;

import com.clustercontrol.utility.settings.platform.xml.RepositoryScope;
import com.clustercontrol.utility.settings.platform.xml.RepositoryScopeNode;
import com.clustercontrol.utility.settings.platform.xml.ScopeNodeInfo;
import com.clustercontrol.utility.util.Config;

/**
 * リポジトリスコープ情報を取得、設定、削除するコマンドラインインターフェース<BR>
 * 
 * @version 6.1.0
 * @since 2.0.0
 * 
 * 
 */
public class RepositoryScopeConv {
	
	// ロガー
	private static Log logger = LogFactory.getLog(RepositoryScopeConv.class);

	/**
	 * Facility ツリーを表すインターフェース。<BR>
	 * 
	 */
	public interface IFacilityTreeItem {
	    public List<IFacilityTreeItem> getChildren() throws Exception;
	    public FacilityInfoResponse getData() throws Exception;
	    public IFacilityTreeItem getParent();
	}
	
	/**
	 * Caster のスコープ情報に変換<BR>
	 * 
	 * @param
	 * @return
	 */
	public static RepositoryScope createRepositoryScope(IFacilityTreeItem rootItem) throws Exception {
		logger.debug("enter RepositlyScopeConv.createRepositoryScope method");

		RepositoryScope repositoryScope = new RepositoryScope();

		recursiveCreateRepositoryScope(rootItem, repositoryScope, null);
		
		repositoryScope.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
		repositoryScope.setSchemaInfo(RepositoryConv.getSchemaVersionScope());

		logger.debug("leave RepositlyScopeConv.createRepositoryScope method");

		return repositoryScope;
	}
	
	private static void recursiveCreateRepositoryScope (
			IFacilityTreeItem treeItem,
			RepositoryScope repositoryScope,
			com.clustercontrol.utility.settings.platform.xml.ScopeInfo parentScopeInfo
			) throws Exception {
		FacilityInfoResponse scopeInfo_ws = treeItem.getData();
		if (
			// ルートか確認。
			scopeInfo_ws.getFacilityType() == FacilityInfoResponse.FacilityTypeEnum.COMPOSITE ||
			// インターナル以外のスコープか確認。
			(scopeInfo_ws.getFacilityType() == FacilityInfoResponse.FacilityTypeEnum.SCOPE && !RepositoryConv.checkInternalScope(scopeInfo_ws.getFacilityId()))
			) {
			// コンポジット以外は、変換。
			if (scopeInfo_ws.getFacilityType() != FacilityInfoResponse.FacilityTypeEnum.COMPOSITE) {
				logger.debug("ScopeID : " + scopeInfo_ws.getFacilityId());
				
				com.clustercontrol.utility.settings.platform.xml.ScopeInfo scopeInfo_cas = new com.clustercontrol.utility.settings.platform.xml.ScopeInfo();
	
				scopeInfo_cas.setFacilityId(scopeInfo_ws.getFacilityId());
				scopeInfo_cas.setFacilityName(scopeInfo_ws.getFacilityName());
				scopeInfo_cas.setDescription(scopeInfo_ws.getDescription());
				scopeInfo_cas.setOwnerRoleId(scopeInfo_ws.getOwnerRoleId());
				scopeInfo_cas.setIconImage(scopeInfo_ws.getIconImage());
				scopeInfo_cas.setParentFacilityId(treeItem.getParent() != null ? treeItem.getParent().getData().getFacilityId(): "");
				
				// 親が null という状況は、親が FacilityConstant.TYPE_COMPOSITE です。
				if (parentScopeInfo != null) {
					parentScopeInfo.addScopeInfo(scopeInfo_cas);
				}
				else {
					repositoryScope.addScopeInfo(scopeInfo_cas);
				}
				
				parentScopeInfo = scopeInfo_cas;
			}
			else {
				parentScopeInfo = null;
			}

			// 子供を探索。
			for (IFacilityTreeItem childItem: treeItem.getChildren()) {
				recursiveCreateRepositoryScope(childItem, repositoryScope, parentScopeInfo);
			}

			Arrays.sort(parentScopeInfo != null ? parentScopeInfo.getScopeInfo() : repositoryScope.getScopeInfo(),
					new Comparator<com.clustercontrol.utility.settings.platform.xml.ScopeInfo>() {
						/**
						 * FacilityId を比較する。
						 */
						@Override
						public int compare(
							com.clustercontrol.utility.settings.platform.xml.ScopeInfo scopeInfo1,
							com.clustercontrol.utility.settings.platform.xml.ScopeInfo scopeInfo2
							) {
							return scopeInfo1.getFacilityId().compareTo(scopeInfo2.getFacilityId());
						}
					});
		}
	}
	
	/**
	 * Caster のスコープノード情報に変換<BR>
	 * 
	 * @param
	 * @param nodeInfoList に格納されている NodeInfo と対応しており、同じ順番で NodeInfo が所属するスコープのリストが格納されている。
	 * @param
	 * @return
	 * @throws Exception
	 */
	public static RepositoryScopeNode createRepositoryScopeNode(IFacilityTreeItem rootItem) throws Exception {
		logger.debug("enter RepositlyScopeConv.createRepositoryScopeNode method");

		RepositoryScopeNode repositoryScopeNode = new RepositoryScopeNode();

		recursiveRepositoryScopeNode(rootItem, repositoryScopeNode);
		Arrays.sort(repositoryScopeNode.getScopeNodeInfo(),
				new Comparator<com.clustercontrol.utility.settings.platform.xml.ScopeNodeInfo>() {
				/**
				 * FacilityId を比較する。
				 */
				/**
				 * スコープのファシリティIDを比較する。
				 * スコープのファシリティIDが同じ場合は、ノードのファシリティIDを比較する。
				 */
				@Override
				public int compare(
					com.clustercontrol.utility.settings.platform.xml.ScopeNodeInfo info1,
					com.clustercontrol.utility.settings.platform.xml.ScopeNodeInfo info2
					) {
					
					String scopeFacilityId1 = info1.getScopeFacilityId();
					String scopeFacilityId2 = info2.getScopeFacilityId();
					
					String nodeFacilityId1 = info1.getNodeFacilityId();
					String nodeFacilityId2 = info2.getNodeFacilityId();
					
					int ret = scopeFacilityId1.compareTo(scopeFacilityId2);
					
					if (ret == 0) {
						ret = nodeFacilityId1.compareTo(nodeFacilityId2);
					}
					
					return ret;
				}
			});

		repositoryScopeNode.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
		repositoryScopeNode.setSchemaInfo(RepositoryConv.getSchemaVersionScope());

		logger.debug("leave RepositlyScopeConv.createRepositoryScopeNode method");

		return repositoryScopeNode;
	}

	private static void recursiveRepositoryScopeNode(
			IFacilityTreeItem treeItem,
			RepositoryScopeNode repositoryScopeNode
			) throws Exception {
		FacilityInfoResponse facilityInfo = treeItem.getData();
		
		if (!RepositoryConv.checkInternalScope(treeItem.getData().getFacilityId())) {
			// ノードが調査対象。
			if (facilityInfo.getFacilityType() == FacilityInfoResponse.FacilityTypeEnum.NODE) {
				logger.debug("NodeID ScopeID : " + treeItem.getParent().getData().getFacilityId() + " " +  facilityInfo.getFacilityId());
				
				ScopeNodeInfo scopeNodeInfo = new com.clustercontrol.utility.settings.platform.xml.ScopeNodeInfo();

				scopeNodeInfo.setNodeFacilityId(facilityInfo.getFacilityId());
				scopeNodeInfo.setNodeFacilityName(facilityInfo.getFacilityName());
				scopeNodeInfo.setScopeFacilityId(treeItem.getParent().getData().getFacilityId());
				scopeNodeInfo.setScopeFacilityName(treeItem.getParent().getData().getFacilityName());
				
				repositoryScopeNode.addScopeNodeInfo(scopeNodeInfo);
			}
			else {
				for (IFacilityTreeItem childItem: treeItem.getChildren()) {
					recursiveRepositoryScopeNode(childItem, repositoryScopeNode);
				}
			}
		}
	}
	
	/**
	 * Web サービス のスコープ情報に変換<BR>
	 * 既定値は、com.clustercontrol.repository.util.ScopePropertyUtil.property2scope を参考。
	 * 
	 * @param
	 * @return
	 */
	public static ScopeInfoResponseP1 createScopeInfo_ws(
			com.clustercontrol.utility.settings.platform.xml.ScopeInfo scopeInfo_cas
			) {
		ScopeInfoResponseP1 scopeInfo_ws = new ScopeInfoResponseP1();

		scopeInfo_ws.setFacilityId(scopeInfo_cas.getFacilityId());
		scopeInfo_ws.setFacilityName(scopeInfo_cas.getFacilityName());
		scopeInfo_ws.setDescription(scopeInfo_cas.getDescription());
		scopeInfo_ws.setOwnerRoleId(scopeInfo_cas.getOwnerRoleId());
		scopeInfo_ws.setIconImage(scopeInfo_cas.getIconImage());
		
		return scopeInfo_ws;
	}
}