/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.factory;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.FunctionPrefixEnum;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.NodeConfigSettingNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.notify.factory.ModifyNotifyRelation;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.repository.model.NodeConfigCustomInfo;
import com.clustercontrol.repository.model.NodeConfigCustomInfoPK;
import com.clustercontrol.repository.model.NodeConfigSettingInfo;
import com.clustercontrol.repository.model.NodeConfigSettingItemInfo;
import com.clustercontrol.repository.model.NodeConfigSettingItemInfoPK;
import com.clustercontrol.repository.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;

import jakarta.persistence.EntityExistsException;

/**
 * 対象構成情報の更新処理を実装したクラス<BR>
 */
public class NodeConfigSettingModifier {

	private static Log m_log = LogFactory.getLog(NodeConfigSettingModifier.class);

	/**
	 * 対象構成情報を追加する。<BR>
	 *
	 * @param info 追加する対象構成情報
	 * @param modifyUserId 作業ユーザID
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 */
	public static void addNodeConfigSettingInfo(NodeConfigSettingInfo info, String modifyUserId)
			throws EntityExistsException, HinemosUnknown {

		String settingId = "";
		long now = HinemosTime.currentTimeMillis();

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// 重複チェック
			jtm.checkEntityExists(NodeConfigSettingInfo.class, info.getSettingId());

			info.setNotifyGroupId(NotifyGroupIdGenerator.generate(info));
			info.setRegDate(now);
			info.setRegUser(modifyUserId);
			info.setUpdateDate(now);
			info.setUpdateUser(modifyUserId);

			// 新規登録
			em.persist(info);

			// 通知情報の登録
			if (info.getNotifyRelationList() != null
					&& info.getNotifyRelationList().size() > 0) {
				for (NotifyRelationInfo notifyRelationInfo : info.getNotifyRelationList()) {
					notifyRelationInfo.setNotifyGroupId(info.getNotifyGroupId());
					notifyRelationInfo.setFunctionPrefix(FunctionPrefixEnum.NODE_CONFIG_SETTING.name());
				}
				// 通知情報を登録
				new ModifyNotifyRelation().add(info.getNotifyRelationList(), info.getOwnerRoleId());
			}
			
			// 対象情報を設定
			List<NodeConfigSettingItemInfo> list = info.getNodeConfigSettingItemList();
			if (list != null) {
				for(int index = 0; index < list.size(); index++){
					NodeConfigSettingItemInfo itemInfo = list.get(index);
					itemInfo.setSettingId(info.getSettingId());
					em.persist(itemInfo);
					itemInfo.relateToNodeConfigSettingInfo(info);
				}
			}
			
			// ユーザ任意情報の登録
			settingId = info.getSettingId();
			List<NodeConfigCustomInfo> customList = info.getNodeConfigCustomList();
			if (customList != null) {
				for(int index = 0; index < customList.size(); index++){
					NodeConfigCustomInfo customInfo = customList.get(index);
					customInfo.setSettingId(settingId);
					em.persist(customInfo);
					customInfo.relateToNodeConfigSettingInfo(info);
				}
			}
			

		} catch (EntityExistsException e) {
			m_log.info("addNodeConfigSettingInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		} catch (HinemosUnknown e) {
			throw e;
		}

		m_log.debug("addNodeConfigSettingInfo() successful in adding a nodeConfigSettingInfo. (settingId = " + settingId + ")");
	}

	/**
	 * 対象構成情報を変更する。<BR>
	 *
	 * @param info 変更後の対象構成情報
	 * @param modifyUserId 作業ユーザID
	 * @throws NodeConfigSettingNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public static void modifyNodeConfigSettingInfo(NodeConfigSettingInfo info, String modifyUserId)
			throws NodeConfigSettingNotFound, InvalidRole, HinemosUnknown {

		String settingId = "";
		long now = HinemosTime.currentTimeMillis();

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			NodeConfigSettingInfo entity = QueryUtil.getNodeConfigSettingInfoPK(info.getSettingId(), ObjectPrivilegeMode.MODIFY);

			entity.setSettingName(info.getSettingName());
			entity.setDescription(info.getDescription());
			entity.setFacilityId(info.getFacilityId());
			entity.setRunInterval(info.getRunInterval());
			entity.setCalendarId(info.getCalendarId());
			entity.setValidFlg(info.getValidFlg());
			entity.setUpdateDate(now);
			entity.setUpdateUser(modifyUserId);

			// 通知情報を更新
			if (info.getNotifyRelationList() != null
					&& info.getNotifyRelationList().size() > 0) {
				for (NotifyRelationInfo notifyRelationInfo : info.getNotifyRelationList()) {
					notifyRelationInfo.setNotifyGroupId(entity.getNotifyGroupId());
					notifyRelationInfo.setFunctionPrefix(FunctionPrefixEnum.NODE_CONFIG_SETTING.name());
				}
			}
			new NotifyControllerBean().modifyNotifyRelation(
					info.getNotifyRelationList(), entity.getNotifyGroupId(), entity.getOwnerRoleId());

			// 収集対象の更新
			if (info.getNodeConfigSettingItemList() != null) {
				NodeConfigSettingItemInfo itemInfoEntity = null;
				List<NodeConfigSettingItemInfoPK> itemInfoPkList = new ArrayList<>();
				NodeConfigSettingItemInfoPK itemInfoPk = null;
				for (NodeConfigSettingItemInfo itemInfo : info.getNodeConfigSettingItemList()) {
					itemInfoPk = new NodeConfigSettingItemInfoPK(info.getSettingId(), itemInfo.getSettingItemId());
					try {
						itemInfoEntity = QueryUtil.getNodeConfigSettingItemInfoPK(itemInfoPk);
					} catch (NodeConfigSettingNotFound e) {
						itemInfoEntity = new NodeConfigSettingItemInfo(itemInfoPk);
						em.persist(itemInfoEntity);
						itemInfoEntity.relateToNodeConfigSettingInfo(entity);
					}
					itemInfoPkList.add(itemInfoPk);
				}
				// 不要なCalDetailInfoEntityを削除
				entity.deleteNodeConfigSettingItemList(itemInfoPkList);
			}
			

			// ユーザ任意情報の更新
			if (info.getNodeConfigCustomList() != null) {
				NodeConfigCustomInfo customInfoEntity = null;
				List<NodeConfigCustomInfoPK> customInfoPkList = new ArrayList<>();
				NodeConfigCustomInfoPK customInfoPk = null;
				for (NodeConfigCustomInfo customInfo : info.getNodeConfigCustomList()) {
					customInfoPk = new NodeConfigCustomInfoPK(info.getSettingId(), customInfo.getSettingCustomId());
					try {
						customInfoEntity = QueryUtil.getNodeConfigCustomInfoPK(customInfoPk);
					} catch (NodeConfigSettingNotFound e) {
						customInfoEntity = new NodeConfigCustomInfo(customInfoPk);
					}
					customInfoEntity.setDisplayName(customInfo.getDisplayName());
					customInfoEntity.setDescription(customInfo.getDescription());
					customInfoEntity.setCommand(customInfo.getCommand());
					customInfoEntity.setSpecifyUser(customInfo.isSpecifyUser());
					customInfoEntity.setEffectiveUser(customInfo.getEffectiveUser());
					customInfoEntity.setValidFlg(customInfo.isValidFlg());
					em.persist(customInfoEntity);
					customInfoEntity.relateToNodeConfigSettingInfo(entity);
					customInfoPkList.add(customInfoPk);
				}
				// 不要なCalDetailInfoEntityを削除
				entity.deleteNodeConfigSettingCustomList(customInfoPkList);
			}

		} catch (NotifyNotFound e) {
			throw new NodeConfigSettingNotFound(e.getMessage(), e);
		} catch (NodeConfigSettingNotFound e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		}

		m_log.debug("modifyNodeConfigSettingInfo() successful in modifing a nodeConfigSettingInfo. (settingId = " + settingId + ")");
	}

	/**
	 * 対象構成情報を削除する。<BR>
	 *
	 * @param settingId 削除する対象構成情報のsettingId
	 * @param modifyUserId 作業ユーザID
	 * @throws NodeConfigSettingNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public static void deleteNodeConfigSettingInfo(String settingId, String modifyUserId)
			throws NodeConfigSettingNotFound, InvalidRole, HinemosUnknown {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// 該当する対象構成情報を取得
			NodeConfigSettingInfo entity = QueryUtil.getNodeConfigSettingInfoPK(settingId, ObjectPrivilegeMode.MODIFY);

			// 通知グループ情報を削除
			new NotifyControllerBean().deleteNotifyRelation(entity.getNotifyGroupId());

			// 対象構成情報を削除
			em.remove(entity);

			// 通知履歴情報を削除
			new NotifyControllerBean().deleteNotifyHistory(HinemosModuleConstant.NODE_CONFIG_SETTING, settingId);
		}
	}
}
