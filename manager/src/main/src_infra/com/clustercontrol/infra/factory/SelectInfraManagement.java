/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraManagementInvalid;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.infra.bean.AccessInfo;
import com.clustercontrol.infra.bean.InfraNodeInputConstant;
import com.clustercontrol.infra.bean.InfraParameterConstant;
import com.clustercontrol.infra.model.InfraManagementInfo;
import com.clustercontrol.infra.model.InfraManagementParamInfo;
import com.clustercontrol.infra.model.InfraModuleInfo;
import com.clustercontrol.infra.model.ReferManagementModuleInfo;
import com.clustercontrol.infra.util.QueryUtil;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.repository.factory.NodeProperty;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;

/**
 * 環境構築情報を取得する。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class SelectInfraManagement {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( SelectInfraManagement.class );

	/**
	 * 環境構築情報を取得します。
	 * <p>
	 */
	public InfraManagementInfo getInfraManagement(String infraManagementId, String ownerRoleId, ObjectPrivilegeMode mode) throws InfraManagementNotFound, InvalidRole, HinemosUnknown {
		InfraManagementInfo info = null;
		if (ownerRoleId == null) {
			info = QueryUtil.getInfraManagementInfoPK(infraManagementId, mode);
		} else {
			info = QueryUtil.getInfraManagementInfoPK_OR(infraManagementId, ownerRoleId, mode);
		}

		// モジュール情報を取得する
		if (info.getModuleList() != null) {
			for (InfraModuleInfo<?> infraModuleInfo : info.getModuleList()) {
				infraModuleInfo = QueryUtil.getInfraModuleInfoPK(infraModuleInfo.getId());
			}
		}
		
		return info;
	}

	/**
	 * 環境構築情報を取得します。
	 * <p>
	 */
	public InfraManagementInfo get(String infraManagementId, String ownerRoleId, ObjectPrivilegeMode mode) throws InfraManagementNotFound, InvalidRole, HinemosUnknown {
		InfraManagementInfo info = null;
		if (ownerRoleId == null) {
			info = QueryUtil.getInfraManagementInfoPK(infraManagementId, mode);
		} else {
			info = QueryUtil.getInfraManagementInfoPK_OR(infraManagementId, ownerRoleId, mode);
		}

		// モジュール情報を取得する
		if (info.getModuleList() != null) {
			for (InfraModuleInfo<?> infraModuleInfo : info.getModuleList()) {
				infraModuleInfo = QueryUtil.getInfraModuleInfoPK(infraModuleInfo.getId());
			}
		}
		
		// 変数情報を取得する
		if (info.getInfraManagementParamList() != null) {
			for (InfraManagementParamInfo paramInfo : info.getInfraManagementParamList()) {
				paramInfo = QueryUtil.getInfraManagementParamInfoPK(paramInfo.getId());
			}
		}
		
		// 通知情報を設定する
		// FIXME: NotifyRelationCacheの適用
		info.setNotifyRelationList(
			new NotifyControllerBean().getNotifyRelation(info.getNotifyGroupId()));
		
		// FIXME:
		Collections.sort(info.getModuleList(), new Comparator<InfraModuleInfo<?>>() {
			@Override
			public int compare(InfraModuleInfo<?> o1, InfraModuleInfo<?> o2) {
				return o1.getOrderNo().compareTo(o2.getOrderNo());
			}
		});
		
		return info;
	}
	
	public List<InfraManagementInfo> getList() throws InvalidRole, HinemosUnknown {
		List<InfraManagementInfo> list = QueryUtil.getAllInfraManagementInfo();
		for (InfraManagementInfo info: list) {
			// 通知情報を設定する
			// FIXME: NotifyRelationCacheの適用
			info.setNotifyRelationList(
				new NotifyControllerBean().getNotifyRelation(info.getNotifyGroupId()));

			// FIXME:
			Collections.sort(info.getModuleList(), new Comparator<InfraModuleInfo<?>>() {
				@Override
				public int compare(InfraModuleInfo<?> o1, InfraModuleInfo<?> o2) {
					return o1.getOrderNo().compareTo(o2.getOrderNo());
				}
			});
		}
		return list;
	}

	public List<InfraManagementInfo> getListByOwnerRole(String ownerRoleId) throws InvalidRole, HinemosUnknown {
		m_log.debug("getList() : start");

		List<InfraManagementInfo> list = QueryUtil.getAllInfraManagementInfoOrderByInfraManagementId_OR(ownerRoleId);
		for (InfraManagementInfo info: list) {
			// 通知情報を設定する
			// FIXME: NotifyRelationCacheの適用
			info.setNotifyRelationList(
				new NotifyControllerBean().getNotifyRelation(info.getNotifyGroupId()));
			// FIXME:
			Collections.sort(info.getModuleList(), new Comparator<InfraModuleInfo<?>>() {
				@Override
				public int compare(InfraModuleInfo<?> o1, InfraModuleInfo<?> o2) {
					return o1.getOrderNo().compareTo(o2.getOrderNo());
				}
			});
		}
		return list;
	}

	/**
	 * 参照環境構築モジュールの選択対象一覧を返す
	 * 
	 * @param ownerRoleId オーナーロールID
	 * @return 参照環境構築モジュールの選択対象の環境構築IDリスト
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<String> getReferManagementIdList(String ownerRoleId) throws InvalidRole, HinemosUnknown {
		m_log.debug("getReferManagementIdListByOwnerRole() : start");
		List<String> rtnList = new ArrayList<>();
		List<InfraManagementInfo> list = QueryUtil.getAllInfraManagementInfoOrderByInfraManagementId_OR(ownerRoleId);
		for (InfraManagementInfo info: list) {
			rtnList.add(info.getManagementId());
		}
		return rtnList;
	}

	/**
	 * 環境構築設定内のモジュールが最大件数を超えていないか確認
	 * ネストが指定された深さより深い場合はfalseを返す
	 * 
	 * @param managementId 環境構築ID
	 * @param moduleIdList 環境構築モジュールIDリスト（初回用）
	 */
	public void checkInfraModuleCount(String managementId, List<String> moduleIdList)
		throws InfraManagementNotFound, InfraManagementInvalid, InvalidRole, HinemosUnknown {
		// モジュール件数
		int count = 0;
		// モジュール最大件数
		int maxCount = HinemosPropertyCommon.infra_management_module_maxcount.getIntegerValue();

		InfraManagementInfo managementInfo = getInfraManagement(managementId, null, ObjectPrivilegeMode.EXEC);
		if (managementInfo.getModuleList() == null) {
			throw new HinemosUnknown("module is empty. managerId=" + managementId);
		}
		for (InfraModuleInfo<?> moduleInfo : managementInfo.getModuleList()) {
			if (moduleIdList != null && moduleIdList.size() > 0 && !moduleIdList.contains(moduleInfo.getModuleId())) {
				continue;
			}
			count++;
			m_log.debug("checkInfraModuleCount() : count=" + count);
			if (count > maxCount) {
				// 値が超過したらエラー
				throw new InfraManagementInvalid("Infra Management Module Count is out of range. : maxCount=" + maxCount);
			}
			if (moduleInfo instanceof ReferManagementModuleInfo) {
				count = getInfraModuleCount(((ReferManagementModuleInfo)moduleInfo).getReferManagementId(), count, maxCount);
			}
		}
	}

	/**
	 * 環境構築設定内のモジュールが最大件数を超えていないか確認
	 * ネストが指定された深さより深い場合はfalseを返す
	 * 
	 * @param managementId 環境構築ID
	 * @param count モジュールの件数
	 * @param maxCount モジュールの最大件数
	 * @return モジュールの件数
	 */
	private int getInfraModuleCount(String managementId, int count, int maxCount)
			throws InfraManagementNotFound, InfraManagementInvalid, InvalidRole, HinemosUnknown {
		InfraManagementInfo managementInfo = getInfraManagement(managementId, null, ObjectPrivilegeMode.EXEC);
		if (managementInfo.getModuleList() == null) {
			return count;
		}
		for (InfraModuleInfo<?> moduleInfo : managementInfo.getModuleList()) {
			count++;
			m_log.debug("getInfraModuleCount() : count=" + count);
			if (count > maxCount) {
				// 値が超過したらエラー
				throw new InfraManagementInvalid("Infra Management Module Count is out of range. : maxCount=" + maxCount);
			}
			if (moduleInfo instanceof ReferManagementModuleInfo) {
				count = getInfraModuleCount(((ReferManagementModuleInfo)moduleInfo).getReferManagementId(), count, maxCount);
			}
		}
		return count;
	}

	/**
	 * アクセス情報を作成する
	 * 
	 * @param managementId 環境構築ID
	 * @param nodeInputType アクセス情報の設定方法（InfraNodeInputConstant）
	 * @param parentFacilityIdList ファシリティIDリスト（通知用）
	 * @param moduleIdList モジュールIDリスト（初回用）
	 * @return アクセス情報のリスト
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InfraManagementNotFound
	 */
	public List<AccessInfo> createAccessInfoList(String managementId, Integer nodeInputType, List<String> parentFacilityIdList, List<String> moduleIdList)
			throws InvalidRole, HinemosUnknown, InfraManagementNotFound, FacilityNotFound {
		m_log.debug("createAccessInfoList() : start");
		List<AccessInfo> accessInfoList = new ArrayList<>();
		Map<String, AccessInfo> accessInfoMap = new HashMap<>();
		accessInfoList = createAccessInfoList(accessInfoList, accessInfoMap, managementId, "", nodeInputType, parentFacilityIdList, moduleIdList);
		return accessInfoList;
	}

	/**
	 * 環境設定のアクセス情報を作成する
	 * 
	 * @param accessInfoList アクセス情報リスト
	 * @param accessInfoMap 上位のアクセス情報リストを保持するマップ
	 * @param managementId 環境構築ID
	 * @param moduleId モジュールID（"上位モジュールID#モジュールID"の形式）
	 * @param nodeInputType アクセス情報の設定方法（InfraNodeInputConstant）
	 * @param parentAccessInfoList ファシリティIDリスト（通知、もしくは上位環境構築設定より引き継ぐ）
	 * @param moduleIdList モジュールIDリスト（初回用）
	 * @return アクセス情報リスト
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InfraManagementNotFound
	 * @throws FacilityNotFound
	 */
	private List<AccessInfo> createAccessInfoList(
			List<AccessInfo> accessInfoList,
			Map<String, AccessInfo> accessInfoMap,
			String managementId, 
			String moduleId, 
			Integer nodeInputType,
			List<String> parentFacilityIdList,
			List<String> moduleIdList)
			throws InvalidRole, HinemosUnknown, InfraManagementNotFound, FacilityNotFound {
		m_log.debug("createAccessInfoList() : start");
		Map<String, AccessInfo> localAccessInfoMap = new HashMap<>();

		// 環境構築設定情報を取得
		InfraManagementInfo managementInfo = get(managementId, null, ObjectPrivilegeMode.EXEC);

		// 対象の環境構築設定のログイン情報を取得
		List<String> nodeIdList = new ArrayList<>();
		if (managementInfo.getFacilityId() != null && !managementInfo.getFacilityId().isEmpty()) {
			nodeIdList = new RepositoryControllerBean().getExecTargetFacilityIdList(
					managementInfo.getFacilityId(), managementInfo.getOwnerRoleId());
		} else if (parentFacilityIdList != null) {
			for (String parentFacilityId : parentFacilityIdList) {
				nodeIdList.add(parentFacilityId);
			}
		}
		if (nodeInputType == InfraNodeInputConstant.TYPE_INFRA_PARAM) {
			localAccessInfoMap.putAll(accessInfoMap);
			localAccessInfoMap.putAll(getParamAccessInfoMap(managementId));
			for(String nodeId : nodeIdList) {
				AccessInfo accessInfo = new AccessInfo();
				accessInfo.setFacilityId(nodeId);
				accessInfo.setModuleId(moduleId);
				AccessInfo referAccessInfo = new AccessInfo();
				if (localAccessInfoMap.containsKey(nodeId)) {
					referAccessInfo = localAccessInfoMap.get(nodeId);
				} else if (localAccessInfoMap.containsKey("")) {
					referAccessInfo = localAccessInfoMap.get("");
				}
				if (referAccessInfo.getSshUser() != null) {
					accessInfo.setSshUser(referAccessInfo.getSshUser());
				} else {
					accessInfo.setSshUser("");
				}
				if (referAccessInfo.getSshPassword() != null) {
					accessInfo.setSshPassword(referAccessInfo.getSshPassword());
				} else {
					accessInfo.setSshPassword("");
				}
				if (referAccessInfo.getSshPrivateKeyFilepath() != null) {
					accessInfo.setSshPrivateKeyFilepath(referAccessInfo.getSshPrivateKeyFilepath());
				} else {
					accessInfo.setSshPrivateKeyFilepath("");
				}
				if (referAccessInfo.getSshPrivateKeyPassphrase() != null) {
					accessInfo.setSshPrivateKeyPassphrase(referAccessInfo.getSshPrivateKeyPassphrase());
				} else {
					accessInfo.setSshPrivateKeyPassphrase("");
				}
				if (referAccessInfo.getWinRmUser() != null) {
					accessInfo.setWinRmUser(referAccessInfo.getWinRmUser());
				} else {
					accessInfo.setWinRmUser("");
				}
				if (referAccessInfo.getWinRmPassword() != null) {
					accessInfo.setWinRmPassword(referAccessInfo.getWinRmPassword());
				} else {
					accessInfo.setWinRmPassword("");
				}
				accessInfoList.add(accessInfo);
			}
		} else if (nodeInputType == InfraNodeInputConstant.TYPE_NODE_PARAM) {
			for(String nodeId : nodeIdList) {
				NodeInfo node = NodeProperty.getProperty(nodeId);
				AccessInfo accessInfo = new AccessInfo();
				accessInfo.setFacilityId(nodeId);
				accessInfo.setModuleId(moduleId);
				if (node.getSshUser() != null) {
					accessInfo.setSshUser(node.getSshUser());
				} else {
					accessInfo.setSshUser("");
				}
				if (node.getSshUserPassword() != null) {
					accessInfo.setSshPassword(node.getSshUserPassword());
				} else {
					accessInfo.setSshPassword("");
				}
				if (node.getSshPrivateKeyFilepath() != null) {
					accessInfo.setSshPrivateKeyFilepath(node.getSshPrivateKeyFilepath());
				} else {
					accessInfo.setSshPrivateKeyFilepath("");
				}
				if (node.getSshPrivateKeyPassphrase() != null) {
					accessInfo.setSshPrivateKeyPassphrase(node.getSshPrivateKeyPassphrase());
				} else {
					accessInfo.setSshPrivateKeyPassphrase("");
				}
				if (node.getWinrmUser() != null) {
					accessInfo.setWinRmUser(node.getWinrmUser());
				} else {
					accessInfo.setWinRmUser("");
				}
				if (node.getWinrmUserPassword() != null) {
					accessInfo.setWinRmPassword(node.getWinrmUserPassword());
				} else {
					accessInfo.setWinRmPassword("");
				}
				accessInfoList.add(accessInfo);
			}
		} else if (nodeInputType == InfraNodeInputConstant.TYPE_DIALOG) {
			for(String nodeId : nodeIdList) {
				AccessInfo accessInfo = new AccessInfo();
				accessInfo.setFacilityId(nodeId);
				accessInfo.setModuleId(moduleId);
				accessInfoList.add(accessInfo);
			}
		}

		for (InfraModuleInfo<?> moduleInfo : managementInfo.getModuleList()) {
			if ((moduleIdList != null && moduleIdList.size() > 0 && !moduleIdList.contains(moduleInfo.getModuleId())) 
					|| !(moduleInfo instanceof ReferManagementModuleInfo) 
					|| !moduleInfo.getValidFlg()) {
				continue;
			}
			if (moduleId.equals("")) {
				moduleId = moduleInfo.getModuleId();
			} else {
				moduleId = String.format("%s" + AccessInfo.MODULEID_DELIMITER + "%s", moduleId, moduleInfo.getModuleId());
			}
			accessInfoList = createAccessInfoList(
					accessInfoList, 
					localAccessInfoMap, 
					((ReferManagementModuleInfo)moduleInfo).getReferManagementId(),
					moduleId,
					nodeInputType,
					nodeIdList,
					null);
		}
		return accessInfoList;
	}

	/**
	 * 環境変数情報からアクセス情報のマップを取得する
	 * 
	 * @param managementId 環境構築ID
	 * @return アクセス情報マップ
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	private Map<String, AccessInfo> getParamAccessInfoMap(String managementId) throws InvalidRole, HinemosUnknown {
		m_log.debug("getParamAccessInfoMap() : start");

		Map<String, AccessInfo> rtnAccessInfoMap = new HashMap<>();

		// パラメータが設定されていない場合は処理終了
		if (managementId == null || managementId.isEmpty()) {
			return rtnAccessInfoMap;
		}

		// 環境構築変数情報を取得する
		List<InfraManagementParamInfo> paramList = QueryUtil.getInfraManagementParamListFindByManagementId(managementId);
		if (paramList.size() == 0) {
			return rtnAccessInfoMap;
		}
		
		for (InfraManagementParamInfo paramInfo : paramList) {
			String nodeId = "";
			String key = "";
			String value = "";
			if (paramInfo.getParamId().equals(InfraParameterConstant.SSH_USER)
					|| paramInfo.getParamId().equals(InfraParameterConstant.SSH_PASSWORD)
					|| paramInfo.getParamId().equals(InfraParameterConstant.SSH_PRIVATE_KEY_FILEPATH)
					|| paramInfo.getParamId().equals(InfraParameterConstant.SSH_PRIVATE_KEY_PASSPHRASE)
					|| paramInfo.getParamId().equals(InfraParameterConstant.WINRM_USER)
					|| paramInfo.getParamId().equals(InfraParameterConstant.WINRM_PASSWORD)) {
				key = paramInfo.getParamId();
				value = paramInfo.getValue();
				nodeId = "";
			} else if ((paramInfo.getParamId().startsWith(InfraParameterConstant.SSH_USER + InfraParameterConstant.PARAMETER_DELIMITER)
					|| paramInfo.getParamId().startsWith(InfraParameterConstant.SSH_PASSWORD + InfraParameterConstant.PARAMETER_DELIMITER)
					|| paramInfo.getParamId().startsWith(InfraParameterConstant.SSH_PRIVATE_KEY_FILEPATH + InfraParameterConstant.PARAMETER_DELIMITER)
					|| paramInfo.getParamId().startsWith(InfraParameterConstant.SSH_PRIVATE_KEY_PASSPHRASE + InfraParameterConstant.PARAMETER_DELIMITER)
					|| paramInfo.getParamId().startsWith(InfraParameterConstant.WINRM_USER + InfraParameterConstant.PARAMETER_DELIMITER)
					|| paramInfo.getParamId().startsWith(InfraParameterConstant.WINRM_PASSWORD + InfraParameterConstant.PARAMETER_DELIMITER))
					&& paramInfo.getParamId().split(InfraParameterConstant.PARAMETER_DELIMITER).length == 2) {
				String[] strArgs = paramInfo.getParamId().split(InfraParameterConstant.PARAMETER_DELIMITER);
				key = strArgs[0];
				value = paramInfo.getValue();
				nodeId = strArgs[1];
			}
			if (key.equals("")) {
				continue;
			}
			if (!rtnAccessInfoMap.containsKey(nodeId)) {
				rtnAccessInfoMap.put(nodeId, new AccessInfo());
			}
			if (key.equals(InfraParameterConstant.SSH_USER)) {
				rtnAccessInfoMap.get(nodeId).setSshUser(value);
			} else if (key.equals(InfraParameterConstant.SSH_PASSWORD)) {
				rtnAccessInfoMap.get(nodeId).setSshPassword(value);
			} else if (key.equals(InfraParameterConstant.SSH_PRIVATE_KEY_FILEPATH)) {
				rtnAccessInfoMap.get(nodeId).setSshPrivateKeyFilepath(value);
			} else if (key.equals(InfraParameterConstant.SSH_PRIVATE_KEY_PASSPHRASE)) {
				rtnAccessInfoMap.get(nodeId).setSshPrivateKeyPassphrase(value);
			} else if (key.equals(InfraParameterConstant.WINRM_USER)) {
				rtnAccessInfoMap.get(nodeId).setWinRmUser(value);
			} else if (key.equals(InfraParameterConstant.WINRM_PASSWORD)) {
				rtnAccessInfoMap.get(nodeId).setWinRmPassword(value);
			} else {
				continue;
			}
		}
		return rtnAccessInfoMap;
	}
}
