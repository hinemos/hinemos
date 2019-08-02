/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.factory;

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.Ipv6Util;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.ObjectValidator;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.nodemap.session.NodeMapControllerBean;
import com.clustercontrol.platform.repository.FacilityModifierUtil;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.bean.NodeRegisterFlagConstant;
import com.clustercontrol.repository.entity.CollectorPlatformMstData;
import com.clustercontrol.repository.entity.CollectorSubPlatformMstData;
import com.clustercontrol.repository.model.CollectorPlatformMstEntity;
import com.clustercontrol.repository.model.CollectorSubPlatformMstEntity;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.FacilityRelationEntity;
import com.clustercontrol.repository.model.NodeGeneralDeviceInfo;
import com.clustercontrol.repository.model.NodeHistory;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.NodeNoteInfo;
import com.clustercontrol.repository.model.ScopeInfo;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.repository.util.FacilityUtil;
import com.clustercontrol.repository.util.NodeConfigRegisterUtil;
import com.clustercontrol.repository.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;

/**
 * ファシリティの更新処理を実装したクラス<BR>
 */
public class FacilityModifier {

	private static Log m_log = LogFactory.getLog(FacilityModifier.class);

	/** プラットフォーム定義情報を登録する。<BR>
	 *
	 * @param CollectorPlatformMstData プラットフォーム定義情報
	 * @throws CollectorPlatformMstData
	 * @throws EntityExistsException
	 */
	public static void addCollectorPratformMst(CollectorPlatformMstData data) throws EntityExistsException {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// インスタンス生成
			CollectorPlatformMstEntity entity = new CollectorPlatformMstEntity(data.getPlatformId());
			// 重複チェック
			jtm.checkEntityExists(CollectorPlatformMstEntity.class, entity.getPlatformId());
			entity.setOrderNo(data.getOrderNo().intValue());
			entity.setPlatformName(data.getPlatformName());
			// 登録
			em.persist(entity);
		} catch (EntityExistsException e) {
			m_log.info("addCollectorPratformMst() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * 指定されたプラットフォームIDのプラットフォーム定義情報を削除する。<BR>
	 *
	 * @param platformId プラットフォームID
	 * @throws FacilityNotFound
	 */
	public static void deleteCollectorPratformMst(String platformId) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
	
			// 該当するプラットフォーム定義情報を取得
			CollectorPlatformMstEntity entity = QueryUtil.getCollectorPlatformMstPK(platformId);
			// 削除
			em.remove(entity);
		}
	}

	/** サブプラットフォーム定義情報を登録する。<BR>
	 *
	 * @param addCollectorSubPratformMst サブプラットフォーム定義情報
	 * @throws addCollectorSubPratformMst
	 * @throws EntityExistsException
	 */
	public static void addCollectorSubPratformMst(CollectorSubPlatformMstData data) throws EntityExistsException {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// インスタンス生成
			CollectorSubPlatformMstEntity entity = new CollectorSubPlatformMstEntity(data.getSubPlatformId());
			// 重複チェック
			jtm.checkEntityExists(CollectorSubPlatformMstEntity.class, entity.getSubPlatformId());
			entity.setSubPlatformName(data.getSubPlatformName());
			entity.setType(data.getType());
			entity.setOrderNo(data.getOrderNo());
			// 登録
			em.persist(entity);
		} catch (EntityExistsException e) {
			m_log.info("addCollectorSubPratformMst() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * 指定されたサブプラットフォームIDのサブプラットフォーム定義情報を削除する。<BR>
	 *
	 * @param subPlatformId サブプラットフォームID
	 * @throws FacilityNotFound
	 */
	public static void deleteCollectorSubPratformMst(String subPlatformId) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// 該当するプラットフォーム定義情報を取得
			CollectorSubPlatformMstEntity entity = QueryUtil.getCollectorSubPlatformMstPK(subPlatformId);
			// 削除
			em.remove(entity);
		}
	}

	/** スコープを追加する。<BR>
	 *
	 * @param parentFacilityId スコープを配置する親スコープのファシリティID（空文字の場合はルートスコープとなる）
	 * @param property 追加するスコープ情報
	 * @param modifyUserId 作業ユーザID
	 * @param displaySortOrder 表示ソート順位
	 * @throws FacilityNotFound
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 */
	public static void addScope(String parentFacilityId, ScopeInfo property, String modifyUserId, int displaySortOrder)
			throws FacilityNotFound, EntityExistsException, HinemosUnknown {

		/** ローカル変数 */
		FacilityInfo parentFacility = null;
		ScopeInfo facility = null;
		String facilityId = null;

		/** メイン処理 */
		m_log.debug("adding a scope...");

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// 入力値（ファシリティID）の格納
			facilityId = property.getFacilityId();

			// 親ファシリティがスコープかどうかを確認する
			if (! ObjectValidator.isEmptyString(parentFacilityId)) {
				parentFacility = QueryUtil.getFacilityPK_NONE(parentFacilityId);
				if (!FacilityUtil.isScope(parentFacility)) {
					HinemosUnknown e = new HinemosUnknown("a parent's facility is not a scope. (parentFacilityId = "
							+ parentFacilityId + ", facilityId = " + facilityId + ")");
					m_log.info("addScope() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}

			// ファシリティインスタンスの生成
			facility = new ScopeInfo(facilityId);
			// 重複チェック
			jtm.checkEntityExists(FacilityInfo.class, facilityId);
			facility.setDisplaySortOrder(displaySortOrder);
			facility.setFacilityType(FacilityConstant.TYPE_SCOPE);
			facility.setValid(true);
			setFacilityEntityProperties(facility, property, modifyUserId);
			
			facility.persistSelf();
			em.persist(facility);
			
			em.flush();

			// ファシリティ関連インスタンスの生成
			if (! ObjectValidator.isEmptyString(parentFacilityId)) {
				assignFacilityToScope(parentFacilityId, facilityId);
			}
		} catch (FacilityNotFound e) {
			throw e;
		} catch (EntityExistsException e) {
			m_log.info("addScope() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		} catch (HinemosUnknown e) {
			throw e;
		}

		m_log.info("addScope() successful in adding a scope . (parentFacilityId = " + parentFacilityId + ", facilityId = " + facilityId + ")");
	}

	/**
	 * オーナーロールスコープを変更する。<BR>
	 * 
	 * @param property 変更後のスコープ情報
	 * @param modifyUserId 作業ユーザID
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 */
	public static void modifyOwnerRoleScope(ScopeInfo property, String modifyUserId)
			throws FacilityNotFound, InvalidRole {

		/** ローカル変数 */
		FacilityInfo facility = null;
		String facilityId = null;

		/** メイン処理 */
		m_log.debug("modifing an owner role scope...");

		// 入力値（ファシリティID）の格納
		facilityId = property.getFacilityId();

		// ファシリティインスタンスを取得（オブジェクト権限のチェックなし）
		facility = QueryUtil.getFacilityPK_NONE(facilityId);

		// ファシリティがスコープかどうかを確認する
		if (!FacilityUtil.isScope(facility)) {
			FacilityNotFound e = new FacilityNotFound("this facility is not a scope. (facilityId = " + facilityId + ")");
			m_log.info("modifyOwnerRoleScope() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		// 変更後の値を格納する
		setFacilityEntityProperties(facility, property, modifyUserId);
		m_log.info("modifyOwnerRoleScope() successful in modifing a scope. (facilityId = " + facilityId + ")");
	}

	/**
	 * スコープを変更する。<BR>
	 *
	 * @param property 変更後のスコープ情報
	 * @param modifyUserId 作業ユーザID
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 */
	public static void modifyScope(ScopeInfo property, String modifyUserId) throws FacilityNotFound, InvalidRole {

		/** ローカル変数 */
		FacilityInfo facility = null;
		String facilityId = null;

		/** メイン処理 */
		m_log.debug("modifing a scope...");

		// 入力値（ファシリティID）の格納
		facilityId = property.getFacilityId();

		// ファシリティインスタンスを取得
		facility = QueryUtil.getFacilityPK(facilityId, ObjectPrivilegeMode.MODIFY);
		
		// ファシリティがスコープかどうかを確認する
		if (!FacilityUtil.isScope(facility)) {
			FacilityNotFound e = new FacilityNotFound("this facility is not a scope. (facilityId = " + facilityId + ")");
			m_log.info("modifyScope() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// 変更後の値を格納する
		setFacilityEntityProperties(facility, property, modifyUserId);

		m_log.info("modifyScope() successful in modifing a scope. (facilityId = " + facilityId + ")");
	}

	/**
	 * オーナーロールスコープを削除する。<BR>
	 *
	 * @param facilityId 削除するスコープのファシリティID
	 * @param modifyUserId 作業ユーザID
	 * @throws UsedFacility
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public static void deleteOwnerRoleScope(String facilityId, String modifyUserId) throws UsedFacility, FacilityNotFound, InvalidRole, HinemosUnknown {

		/** メイン処理 */
		m_log.debug("deleting a owner role scope with sub scopes...");


		// 該当するファシリティインスタンスを取得（オブジェクト権限チェックなし）
		FacilityInfo facility = QueryUtil.getFacilityPK_NONE(facilityId);

		// 関連インスタンス、ファシリティインスタンスを削除する
		deleteScopeRecursive(facility);
		FacilityModifierUtil.deleteFacilityRelation(facilityId);

		m_log.info("deleteScope() successful in deleting a owner role scope with sub scopes. (facilityId = " + facilityId + ")");
	}

	/**
	 * スコープを削除する。<BR>
	 *
	 * @param facilityId 削除するスコープのファシリティID
	 * @param modifyUserId 作業ユーザID
	 * @throws UsedFacility
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public static void deleteScope(String facilityId, String modifyUserId) throws UsedFacility, FacilityNotFound, InvalidRole, HinemosUnknown {

		/** メイン処理 */
		m_log.debug("deleting a scope with sub scopes...");

		// 該当するファシリティインスタンスを取得
		FacilityInfo facility = QueryUtil.getFacilityPK(facilityId, ObjectPrivilegeMode.MODIFY);

		// 関連インスタンス、ファシリティインスタンスを削除する
		deleteScopeRecursive(facility);
		FacilityModifierUtil.deleteFacilityRelation(facilityId);

		m_log.info("deleteScope() successful in deleting a scope with sub scopes. (facilityId = " + facilityId + ")");
	}

	/**
	 * サブスコープを含めて、スコープを削除する。<BR>
	 *
	 * @param scope 削除するスコープインスタンス
	 * @throws UsedFacility
	 * @throws HinemosUnknown
	 */
	private static void deleteScopeRecursive(FacilityInfo scope) throws UsedFacility, HinemosUnknown, FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			/** ローカル変数 */
			String facilityId = null;

			/** メイン処理 */
			facilityId = scope.getFacilityId();

			// スコープでない場合はエラーとする
			if (!FacilityUtil.isScope(scope)) {
				HinemosUnknown e = new HinemosUnknown("this facility is not a scope. (facilityId = " + facilityId + ")");
				m_log.info("deleteScopeRecursive() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}

			// 直下にスコープを存在する場合、そのスコープおよびサブスコープを削除する
			List<FacilityInfo> childEntities = QueryUtil.getChildFacilityEntity(scope.getFacilityId());
			if (childEntities != null && childEntities.size() > 0) {
				Iterator<FacilityInfo> iter = childEntities.iterator();
				while(iter.hasNext()) {
					FacilityInfo childEntity = iter.next();
					if (FacilityUtil.isScope(childEntity)) {
						childEntity.tranSetUncheckFlg(true);
						// リレーションを削除する
						FacilityRelationEntity facilityRelationEntity
						= QueryUtil.getFacilityRelationPk(scope.getFacilityId(), childEntity.getFacilityId());
						em.remove(facilityRelationEntity);
						deleteScopeRecursive(childEntity);
					}
				}
			}
			em.remove(scope);
			new NodeMapControllerBean().deleteMapInfo(null, scope.getFacilityId());

			m_log.info("deleteScopeRecursive() successful in deleting a scope. (facilityId = " + facilityId + ")");
		}
	}

	/**
	 * ノードを追加する。<BR>
	 *
	 * @param nodeInfo 追加するノード情報
	 * @param modifyUserId 作業ユーザID
	 * @param displaySortOrder 表示ソート順位
	 * @throws EntityExistsException
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 */
	public static void addNode(NodeInfo nodeInfo, String modifyUserId, int displaySortOrder)
			throws EntityExistsException, FacilityNotFound, HinemosUnknown {
		m_log.debug("adding a node...");

		String facilityId = nodeInfo.getFacilityId();
		Boolean valid = nodeInfo.getValid();

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// 重複チェック
			jtm.checkEntityExists(FacilityInfo.class, nodeInfo.getFacilityId());
			nodeInfo.setFacilityType(FacilityConstant.TYPE_NODE);
			nodeInfo.setDisplaySortOrder(displaySortOrder);
			nodeInfo.setValid(valid);
			
			Long regDate = setFacilityEntityProperties(nodeInfo, nodeInfo, modifyUserId);

			nodeInfo.persistSelf();

			setNodeEntityProperties(nodeInfo, nodeInfo);

			em.persist(nodeInfo);

			em.flush();

			// cc_node_historyテーブルへの登録 
			NodeHistory history = new NodeHistory(facilityId, regDate);
			history.setRegUser(modifyUserId);

			// 構成情報収集
			if (nodeInfo.getNodeOsRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodeOsInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodeOsInfo(), false);
				history.setOsFlag(true);
			}
			if (nodeInfo.getNodeCpuRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodeCpuInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodeCpuInfo(), false);
				history.setCpuFlag(true);
			}
			if (nodeInfo.getNodeDiskRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodeDiskInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodeDiskInfo(), false);
				history.setDiskFlag(true);
			}
			if (nodeInfo.getNodeFilesystemRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodeFilesystemInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodeFilesystemInfo(), false);
				history.setFilesystemFlag(true);
			}
			if (nodeInfo.getNodeVariableRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodeVariableInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodeVariableInfo(), false);
				history.setNodeVariableFlag(true);
			}
			if (nodeInfo.getNodeHostnameRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodeHostnameInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodeHostnameInfo(), false);
				history.setHostnameFlag(true);
			}
			if (nodeInfo.getNodeMemoryRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodeMemoryInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodeMemoryInfo(), false);
				history.setMemoryFlag(true);
			}
			if (nodeInfo.getNodeNetworkInterfaceRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodeNetworkInterfaceInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodeNetworkInterfaceInfo(), false);
				history.setNetworkInterfaceFlag(true);
			}
			if (nodeInfo.getNodeNetstatRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodeNetstatInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodeNetstatInfo(), false);
				history.setNetstatFlag(true);
			}
			if (nodeInfo.getNodeProcessRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodeProcessInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodeProcessInfo());
			}
			if (nodeInfo.getNodePackageRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodePackageInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodePackageInfo(), false);
				history.setPackageFlag(true);
			}
			if (nodeInfo.getNodeProductRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodeProductInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodeProductInfo(), false);
				history.setProductFlag(true);
			}
			if (nodeInfo.getNodeLicenseRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodeLicenseInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodeLicenseInfo(), false);
				history.setLicenseFlag(true);
			}

			// cc_node_historyテーブルへの登録 
			em.persist(history);

			// 付随する情報の登録
			addNodeOptionalInfo(nodeInfo, regDate, modifyUserId, true);

			em.flush();

			// ファシリティ関連インスタンスの生成
			long startTime = HinemosTime.currentTimeMillis();
			assignFacilityToScope(FacilityTreeAttributeConstant.REGISTERED_SCOPE, facilityId);
			m_log.info(String.format("assignFacilityToScope FacilityTreeAttributeConstant.REGISTEREFD_SCOPE: %dms", HinemosTime.currentTimeMillis() - startTime));

			// オーナー別スコープへの登録
			startTime = HinemosTime.currentTimeMillis();
			assignFacilityToScope(nodeInfo.getOwnerRoleId(), facilityId);
			m_log.info(String.format("assignFacilityToScope facilityEntity.getOwnerRoleId(): %dms", HinemosTime.currentTimeMillis() - startTime));

			// OS別スコープへの登録
			startTime = HinemosTime.currentTimeMillis();
			String platformFamily = nodeInfo.getPlatformFamily();
			assignFacilityToScope(platformFamily, facilityId);
			m_log.info(String.format("assignFacilityToScope %s: %dms", platformFamily, HinemosTime.currentTimeMillis() - startTime));
		} catch (FacilityNotFound e) {
			throw e;
		} catch (EntityExistsException e) {
			m_log.info("addNode() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		} catch (HinemosUnknown e) {
			throw e;
		}

		m_log.info("addNode() successful in adding a node. (facilityId = " + facilityId + ")");
	}

	/**
	 * ノードを変更する。<BR>
	 *
	 * @param nodeInfo 変更後のノード情報
	 * @param modifyUserId 作業ユーザID
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public static void modifyNode(NodeInfo nodeInfo, String modifyUserId)
			throws FacilityNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("modifing a node...");

		String facilityId = nodeInfo.getFacilityId();
		Boolean valid = nodeInfo.getValid();


		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			NodeInfo nodeEntity = QueryUtil.getNodePK(facilityId, ObjectPrivilegeMode.MODIFY);
			if (valid == null) {
				HinemosUnknown e = new HinemosUnknown("node's valid is invalid . (valid = null)");
				m_log.info("modifyNode() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			} else {
				nodeEntity.setValid(valid);
			}
	
			Long regDate = setFacilityEntityProperties(nodeEntity, nodeInfo, modifyUserId);

			// 変更情報の反映
			String oldPlatformFamily = nodeEntity.getPlatformFamily();
			setNodeEntityProperties(nodeEntity, nodeInfo);

			em.flush();

			// cc_node_historyテーブルへの登録 
			NodeHistory history = new NodeHistory(facilityId, regDate);
			history.setRegUser(modifyUserId);

			// 構成情報収集
			if (nodeInfo.getNodeOsRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodeOsInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodeOsInfo(), false);
				history.setOsFlag(true);
			}
			if (nodeInfo.getNodeCpuRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodeCpuInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodeCpuInfo(), false);
				history.setCpuFlag(true);
			}
			if (nodeInfo.getNodeDiskRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodeDiskInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodeDiskInfo(), false);
				history.setDiskFlag(true);
			}
			if (nodeInfo.getNodeFilesystemRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodeFilesystemInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodeFilesystemInfo(), false);
				history.setFilesystemFlag(true);
			}
			if (nodeInfo.getNodeVariableRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodeVariableInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodeVariableInfo(), false);
				history.setNodeVariableFlag(true);
			}
			if (nodeInfo.getNodeHostnameRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodeHostnameInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodeHostnameInfo(), false);
				history.setHostnameFlag(true);
			}
			if (nodeInfo.getNodeMemoryRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodeMemoryInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodeMemoryInfo(), false);
				history.setMemoryFlag(true);
			}
			if (nodeInfo.getNodeNetworkInterfaceRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodeNetworkInterfaceInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodeNetworkInterfaceInfo(), false);
				history.setNetworkInterfaceFlag(true);
			}
			if (nodeInfo.getNodeNetstatRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodeNetstatInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodeNetstatInfo(), false);
				history.setNetstatFlag(true);
			}
			if (nodeInfo.getNodeProcessRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodeProcessInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodeProcessInfo());
			}
			if (nodeInfo.getNodePackageRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodePackageInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodePackageInfo(), false);
				history.setPackageFlag(true);
			}
			if (nodeInfo.getNodeProductRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodeProductInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodeProductInfo(), false);
				history.setProductFlag(true);
			}
			if (nodeInfo.getNodeLicenseRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
				NodeConfigRegisterUtil.registerNodeLicenseInfo(regDate, modifyUserId, nodeInfo.getFacilityId(), nodeInfo.getNodeLicenseInfo(), false);
				history.setLicenseFlag(true);
			}

			// cc_node_historyテーブルへの登録 
			em.persist(history);
		
			// 付随する情報の登録
			modifyNodeOptionalInfo(nodeInfo, regDate, modifyUserId);

			em.flush();
	
			//OS別スコープの更新
			String platformFamily = nodeEntity.getPlatformFamily();
			if (!oldPlatformFamily.equals(platformFamily)) {//OSスコープが変わる場合
				//旧OSスコープから削除
				// なお、システムによる自動操作なので、操作ユーザーの権限に基づいたオブジェクト権限チェックは不要とする
				String[] facilityIds = {facilityId};
				releaseNodeFromScope(oldPlatformFamily, facilityIds, modifyUserId, false);
	
				//新OSスコープに割り当て
				assignFacilityToScope(platformFamily, facilityId);
			}
		}

		m_log.info("modifyNode() successful in modifing a node. (facilityId = " + facilityId + ")");
	}

	/**
	 * ノードを削除する。<BR>
	 *
	 * @param facilityId 削除するノードのファシリティID
	 * @param modifyUserId 作業ユーザID
	 * @throws InvalidRole
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 */
	public static void deleteNode(String facilityId, String modifyUserId) throws FacilityNotFound, InvalidRole, HinemosUnknown {

		// 現在日時を取得
		long regDate = HinemosTime.currentTimeMillis();

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			m_log.debug("deleteNode() start");

			// 該当するファシリティインスタンスを取得
			FacilityInfo facility = QueryUtil.getFacilityPK(facilityId, ObjectPrivilegeMode.MODIFY);

			// ノードでない場合はエラーとする
			if (!FacilityUtil.isNode(facility)) {
				FacilityNotFound e = new FacilityNotFound("this facility is not a node. (facilityId = " + facilityId + ")");
				m_log.info("deleteNode() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}

			m_log.debug("deleteNode() isNode success");

			// 構成情報収集
			NodeConfigRegisterUtil.deleteNodeHistoryDetailInfo(regDate, modifyUserId, facilityId);

			m_log.debug("deleteNode() register history detail success");

			// cc_node_historyテーブルへの登録 
			NodeHistory history = new NodeHistory(facilityId, regDate);
			history.setOsFlag(true);
			history.setCpuFlag(true);
			history.setDiskFlag(true);
			history.setFilesystemFlag(true);
			history.setNodeVariableFlag(true);
			history.setHostnameFlag(true);
			history.setMemoryFlag(true);
			history.setNetworkInterfaceFlag(true);
			history.setNetstatFlag(true);
			history.setPackageFlag(true);
			history.setProductFlag(true);
			history.setLicenseFlag(true);
			history.setRegUser(modifyUserId);
			em.persist(history);

			m_log.debug("deleteNode() register history success");

			em.remove(facility);
			em.flush();

			m_log.debug("deleteNode() delete node info success");

			// 付随する情報の登録
			deleteNodeOptionalInfo(facility.getFacilityId());

			m_log.debug("deleteNode() delete node optional success");

			FacilityModifierUtil.deleteFacilityRelation(facilityId);
			m_log.info("deleteNode() successful in deleting a node. (facilityId = " + facilityId + ")");
		}
	}

	/**
	 * スコープにノードを割り当てる。<BR>
	 *
	 * @param scopeFacilityId スコープのファシリティID
	 * @param facilityId ノードのファシリティID
	 * @throws FacilityNotFound
	 */
	private static void assignFacilityToScope(String scopeFacilityId, String facilityId) throws FacilityNotFound, HinemosUnknown {
		String[] facilityIds = { facilityId };
		assignFacilitiesToScope(scopeFacilityId, facilityIds);
	}

	/**
	 * スコープにノードを割り当てる。<BR>
	 *
	 * @param scopeFacilityId スコープのファシリティID
	 * @param facilityIds ノードのファシリティID配列
	 * @throws FacilityNotFound
	 */
	public static void assignFacilitiesToScope(String scopeFacilityId, String[] facilityIds) throws FacilityNotFound {
		m_log.debug("assigning facilities to a scope...");

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// スコープのファシリティインスタンスの取得
			FacilityInfo scope = QueryUtil.getFacilityPK_NONE(scopeFacilityId);
			scope.tranSetUncheckFlg(true);

			if (!FacilityUtil.isScope(scope)) {
				FacilityNotFound e = new FacilityNotFound("parent's facility is not a scope. (facilityId = " + scopeFacilityId + ")");
				m_log.info("assignFacilitiesToScope() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}

			for (String facilityId : facilityIds) {
				if (doesFacilityRelationEntityExist(scopeFacilityId, facilityId)) {
					m_log.info("assignFacilitiesToScope() skipped assinging a facility to a scope. (parentFacilityId = " + scopeFacilityId + ", facilityId = " + facilityId + ")");
				} else {
					FacilityRelationEntity relation = new FacilityRelationEntity(scopeFacilityId, facilityId);
					em.persist(relation);
					m_log.info("assignFacilitiesToScope() successful in assinging a facility to a scope. (parentFacilityId = " + scopeFacilityId + ", facilityId = " + facilityId + ")");
				}
			}
		} catch (FacilityNotFound e) {
			throw e;
		}

		m_log.info("assignFacilitiesToScope() successful in assigning facilities to a scope.");
	}

	private static boolean doesFacilityRelationEntityExist(
			String parentFacilityId, String childFacilityId) {
		return FacilityTreeCache.getChildFacilityIdSet(parentFacilityId).contains(childFacilityId);
	}

	/**
	 * スコープからノードの割り当てを解除する。<BR>
	 *
	 * @param parentFacilityId スコープのファシリティID
	 * @param facilityIds ノードのファシリティID
	 * @param modifyUserId 作業ユーザID
	 * @param isAuthCheckTarget 操作ユーザーの親スコープへのオブジェクト権限を確認する場合は true ,確認しない場合はfalse
	 *
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 */
	public static void releaseNodeFromScope(String parentFacilityId, String[] facilityIds, String modifyUserId, boolean isAuthCheckTarget)
			throws FacilityNotFound, InvalidRole {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			/** ローカル変数 */
			FacilityInfo facility = null;
			String facilityId = null;

			/** メイン処理 */
			if (m_log.isDebugEnabled()) {
				m_log.debug("releasing nodes from a scope... parentFacilityId=" + parentFacilityId + " , isAuthCheckTarget=" + isAuthCheckTarget);
			}

			// 該当するファシリティインスタンスを取得（オプジェクト権限チェックの要否を考慮）
			if (isAuthCheckTarget) {
				facility = QueryUtil.getFacilityPK(parentFacilityId);
			} else {
				facility = QueryUtil.getFacilityPK_NONE(parentFacilityId);
			}

			if (!FacilityUtil.isScope(facility)) {
				FacilityNotFound e = new FacilityNotFound("parent's facility is not a scope. (parentFacilityId = " + parentFacilityId + ")");
				m_log.info("releaseNodeFromScope() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}

			for (int i = 0; i < facilityIds.length; i++) {
				facilityId = facilityIds[i];
				FacilityRelationEntity relation
				= QueryUtil.getFacilityRelationPk(parentFacilityId, facilityId);
				em.remove(relation);
				m_log.info("releaseNodeFromScope() successful in releaseing a node. (parentFacilityId = " + parentFacilityId + ", facilityId = " + facilityId + ")");
			}

			m_log.info("releaseNodeFromScope() successful in releasing nodes from a scope.");
		}
	}

	/**
	 * facilityInfoインスタンスに格納された値をファシリティインスタンスに格納する。<BR>
	 *
	 * @param facilityEntity 格納先となるファシリティインスタンス
	 * @param facilityInfo 格納する情報
	 * @param modifyUserId 作業ユーザID
	 * @return 更新日時
	 * @throws FacilityNotFound
	 */
	private static Long setFacilityEntityProperties(FacilityInfo facilityEntity, FacilityInfo facilityInfo, String modifyUserId) throws FacilityNotFound {

		/** メイン処理 */
		// 現在日時を取得
		long now = HinemosTime.currentTimeMillis();

		if (!(FacilityUtil.isScope(facilityEntity) && FacilityUtil.isNode(facilityEntity))) {
		} else {
			FacilityNotFound e = new FacilityNotFound("this facility's type is invalid. (facilityType = " + facilityEntity.getFacilityType() + ")");
			m_log.info("setFacilityEntityProperties() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// ファシリティインスタンスへの入力値の格納
		facilityEntity.setFacilityName(facilityInfo.getFacilityName());
		facilityEntity.setDescription(facilityInfo.getDescription());

		// アイコン名の格納
		facilityEntity.setIconImage(facilityInfo.getIconImage());
		// 入力値（オーナーロールID）の格納
		facilityEntity.setOwnerRoleId(facilityInfo.getOwnerRoleId());
		if (ObjectValidator.isEmptyString(facilityEntity.getCreateUserId())) {
			facilityEntity.setCreateUserId(modifyUserId);
			facilityEntity.setCreateDatetime(now);
		}
		facilityEntity.setModifyUserId(modifyUserId);
		facilityEntity.setModifyDatetime(now);

		return now;
	}

	/**
	 * nodeInfoインスタンス上のノード情報をノードインスタンスに格納する。<BR>
	 *
	 * @param nodeEntity 格納先となるノードインスタンス
	 * @param nodeInfo ノード情報
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 */
	private static void setNodeEntityProperties(NodeInfo nodeEntity, NodeInfo nodeInfo) throws EntityExistsException, HinemosUnknown {
		m_log.debug("setNodeEntityProperties() : facilityId = " + nodeInfo.getFacilityId());

		// オートデバイスサーチ
		nodeEntity.setAutoDeviceSearch(
				nodeInfo.getAutoDeviceSearch() == null || nodeInfo.getAutoDeviceSearch());

		// HW
		nodeEntity.setPlatformFamily(nodeInfo.getPlatformFamily());
		nodeEntity.setSubPlatformFamily(nodeInfo.getSubPlatformFamily());
		nodeEntity.setHardwareType(nodeInfo.getHardwareType());

		// IPアドレス関連
		nodeEntity.setIpAddressVersion(nodeInfo.getIpAddressVersion());
		nodeEntity.setIpAddressV4(nodeInfo.getIpAddressV4());
		nodeEntity.setIpAddressV6(Ipv6Util.expand(nodeInfo.getIpAddressV6()));

		// OS関連
		nodeEntity.setNodeName(nodeInfo.getNodeName());

		// Hinemosエージェント
		nodeEntity.setAgentAwakePort(nodeInfo.getAgentAwakePort());

		// JOB
		nodeEntity.setJobPriority(nodeInfo.getJobPriority());
		nodeEntity.setJobMultiplicity(nodeInfo.getJobMultiplicity());

		// SNMP関連
		nodeEntity.setSnmpUser(nodeInfo.getSnmpUser());
		nodeEntity.setSnmpAuthPassword(nodeInfo.getSnmpAuthPassword());
		nodeEntity.setSnmpPrivPassword(nodeInfo.getSnmpPrivPassword());
		nodeEntity.setSnmpPort(nodeInfo.getSnmpPort());
		nodeEntity.setSnmpCommunity(nodeInfo.getSnmpCommunity());
		nodeEntity.setSnmpVersion(nodeInfo.getSnmpVersion());
		nodeEntity.setSnmpSecurityLevel(nodeInfo.getSnmpSecurityLevel());
		nodeEntity.setSnmpAuthProtocol(nodeInfo.getSnmpAuthProtocol());
		nodeEntity.setSnmpPrivProtocol(nodeInfo.getSnmpPrivProtocol());
		nodeEntity.setSnmpTimeout(nodeInfo.getSnmpTimeout());
		nodeEntity.setSnmpRetryCount(nodeInfo.getSnmpRetryCount());

		// WBEM関連
		nodeEntity.setWbemUser(nodeInfo.getWbemUser());
		nodeEntity.setWbemUserPassword(nodeInfo.getWbemUserPassword());
		nodeEntity.setWbemPort(nodeInfo.getWbemPort());
		nodeEntity.setWbemProtocol(nodeInfo.getWbemProtocol());
		nodeEntity.setWbemTimeout(nodeInfo.getWbemTimeout());
		nodeEntity.setWbemRetryCount(nodeInfo.getWbemRetryCount());

		// IPMI関連
		nodeEntity.setIpmiIpAddress(nodeInfo.getIpmiIpAddress());
		nodeEntity.setIpmiPort(nodeInfo.getIpmiPort());
		nodeEntity.setIpmiUser(nodeInfo.getIpmiUser());
		nodeEntity.setIpmiUserPassword(nodeInfo.getIpmiUserPassword());
		nodeEntity.setIpmiTimeout(nodeInfo.getIpmiTimeout());
		nodeEntity.setIpmiRetries(nodeInfo.getIpmiRetries());
		nodeEntity.setIpmiProtocol(nodeInfo.getIpmiProtocol());
		nodeEntity.setIpmiLevel(nodeInfo.getIpmiLevel());

		// WinRM関連
		nodeEntity.setWinrmUser(nodeInfo.getWinrmUser());
		nodeEntity.setWinrmUserPassword(nodeInfo.getWinrmUserPassword());
		nodeEntity.setWinrmVersion(nodeInfo.getWinrmVersion());
		nodeEntity.setWinrmPort(nodeInfo.getWinrmPort());
		nodeEntity.setWinrmProtocol(nodeInfo.getWinrmProtocol());
		nodeEntity.setWinrmTimeout(nodeInfo.getWinrmTimeout());
		nodeEntity.setWinrmRetries(nodeInfo.getWinrmRetries());
		
		// SSH関連
		nodeEntity.setSshUser(nodeInfo.getSshUser());
		nodeEntity.setSshUserPassword(nodeInfo.getSshUserPassword());
		nodeEntity.setSshPrivateKeyFilepath(nodeInfo.getSshPrivateKeyFilepath());
		nodeEntity.setSshPrivateKeyPassphrase(nodeInfo.getSshPrivateKeyPassphrase());
		nodeEntity.setSshPort(nodeInfo.getSshPort());
		nodeEntity.setSshTimeout(nodeInfo.getSshTimeout());

		// クラウド・仮想化管理関連
		nodeEntity.setCloudService(nodeInfo.getCloudService());
		nodeEntity.setCloudScope(nodeInfo.getCloudScope());
		nodeEntity.setCloudResourceType(nodeInfo.getCloudResourceType());
		nodeEntity.setCloudResourceId(nodeInfo.getCloudResourceId());
		nodeEntity.setCloudResourceName(nodeInfo.getCloudResourceName());
		nodeEntity.setCloudResourceId(nodeInfo.getCloudResourceId());
		nodeEntity.setCloudLocation(nodeInfo.getCloudLocation());

		// 管理情報関連
		nodeEntity.setAdministrator(nodeInfo.getAdministrator());
		nodeEntity.setContact(nodeInfo.getContact());
	}

	/**
	 * ノードに付随する情報を登録します。
	 * 
	 * @param nodeInfo 登録対象のノード情報
	 * @param regDate 登録日時
	 * @param regUser 登録ユーザ
	 * @param addFlg true:新規登録、false：modifyNodeOptionalInfoから呼び出されている
	 * @throws HinemosUnknown
	 */
	private static void addNodeOptionalInfo(NodeInfo nodeInfo, Long regDate, String regUser, boolean addFlg) throws HinemosUnknown {
		if (nodeInfo == null) {
			return;
		}
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// 汎用デバイス関連
			List<NodeGeneralDeviceInfo> deviceList = nodeInfo.getNodeDeviceInfo();
			if (deviceList != null && deviceList.size() > 0) {
				for (NodeGeneralDeviceInfo info : deviceList) {
					if (info == null) {
						continue;
					}
					// 入力チェック
					if (info.getDeviceIndex() != -1
							&& ! ObjectValidator.isEmptyString(info.getDeviceType())
							&& ! ObjectValidator.isEmptyString(info.getDeviceName())) {
						NodeGeneralDeviceInfo entity = new NodeGeneralDeviceInfo(
								nodeInfo.getFacilityId(),
								info.getDeviceIndex(),
								info.getDeviceType(),
								info.getDeviceName());
						entity.setDeviceDisplayName(info.getDeviceDisplayName());
						entity.setDeviceSize(info.getDeviceSize());
						entity.setDeviceSizeUnit(info.getDeviceSizeUnit());
						entity.setDeviceDescription(info.getDeviceDescription());
						em.persist(entity);
					} else {
						HinemosUnknown e = new HinemosUnknown("both type and index of device are required. " +
								"(facilityId = " + nodeInfo.getFacilityId() 
								+ ", deviceType = " + info.getDeviceType() 
								+ ", deviceIndex = " + info.getDeviceIndex() 
								+ ", deviceName = " + info.getDeviceName() + ")");
						m_log.info("addNodeOptionalInfo() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e;						
					}
				}
			}

			// 備考
			List<NodeNoteInfo> noteList = nodeInfo.getNodeNoteInfo();
			if (noteList != null && noteList.size() > 0) {
				for (NodeNoteInfo info : noteList) {
					if (info == null) {
						continue;
					}
					NodeNoteInfo entity = new NodeNoteInfo(nodeInfo.getFacilityId(), info.getNoteId());
					entity.setNote(info.getNote());
					em.persist(entity);
				}
			}
		} catch (HinemosUnknown e) {
			throw e;
		}
	}
	
	/**
	 * ノードに付随する情報を登録します。
	 * 
	 * @param nodeInfo 登録対象のノード情報
	 * @param regDate 登録日時
	 * @param regUser 登録ユーザ
	 * @throws HinemosUnknown
	 */
	private static void modifyNodeOptionalInfo(NodeInfo nodeInfo, Long regDate, String regUser) throws HinemosUnknown {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			
			// 既存の情報を削除する
			deleteNodeOptionalInfo(nodeInfo.getFacilityId());

			// 新しい情報を登録
			addNodeOptionalInfo(nodeInfo, regDate, regUser, false);

		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("modifyNodeOptionalInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}
	
	/**
	 * ノードに付随する情報を削除します。
	 * 
	 * @param facilityId 削除対象のファシリティID
	 * @throws HinemosUnknown
	 */
	private static void deleteNodeOptionalInfo(String facilityId) throws HinemosUnknown {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			List<NodeGeneralDeviceInfo> nodeGeneralDeviceList = QueryUtil.getNodeGeneralDeviceInfoByFacilityId(facilityId);
			if (nodeGeneralDeviceList != null && nodeGeneralDeviceList.size() > 0) {
				Iterator<NodeGeneralDeviceInfo> it = nodeGeneralDeviceList.iterator();
				while(it.hasNext()){
					NodeGeneralDeviceInfo info = it.next();
					em.remove(info);
				}
			}
			List<NodeNoteInfo> nodeNoteList = QueryUtil.getNodeNoteInfoByFacilityId(facilityId);
			if (nodeNoteList != null && nodeNoteList.size() > 0) {
				Iterator<NodeNoteInfo> it = nodeNoteList.iterator();
				while(it.hasNext()){
					NodeNoteInfo info = it.next();
					em.remove(info);
				}
			}

			// JPAではDML処理順序が保障されないため、フラッシュ実行
			jtm.flush();

		} catch (Exception e) {
			m_log.warn("deleteNodeOptionalInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

}
