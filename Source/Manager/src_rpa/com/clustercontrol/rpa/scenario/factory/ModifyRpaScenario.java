/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.scenario.factory;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.RpaScenarioDuplicate;
import com.clustercontrol.fault.RpaScenarioNotFound;
import com.clustercontrol.fault.RpaScenarioOperationResultCreateSettingDuplicate;
import com.clustercontrol.fault.RpaScenarioOperationResultCreateSettingNotFound;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.rpa.scenario.model.RpaScenario;
import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResult;
import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResultCreateSetting;
import com.clustercontrol.rpa.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;

import jakarta.persistence.EntityExistsException;

/**
 * RPAシナリオ情報を更新するクラス
 */
public class ModifyRpaScenario {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( ModifyRpaScenario.class );

	/**
	 * RPAシナリオ情報を作成します。
	 */
	public void add(RpaScenario data, String name) throws RpaScenarioDuplicate {
		long now = HinemosTime.currentTimeMillis();

		//エンティティBeanを作る
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			
			// 重複チェック
			jtm.checkEntityExists(RpaScenario.class, data.getScenarioId());
			data.setRegDate(now);
			data.setRegUser(name);
			data.setUpdateDate(now);
			data.setUpdateUser(name);
			
			em.persist(data);
			jtm.flush();
		} catch (EntityExistsException e) {
			m_log.info("add() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new RpaScenarioDuplicate(e.getMessage(),e);
		}
	}
	
	/**
	 * RPAシナリオ情報を変更します。
	 */
	public void modify(RpaScenario data, String name) throws RpaScenarioNotFound, InvalidRole {

		//RPAシナリオ情報を取得
		RpaScenario rpaScenario = QueryUtil.getRpaScenarioPK(data.getScenarioId(), ObjectPrivilegeMode.MODIFY);
		
		//RPAシナリオ情報を更新
		rpaScenario.setScenarioName(data.getScenarioName());
		rpaScenario.setDescription(data.getDescription());
		rpaScenario.setManualTime(data.getManualTime());
		rpaScenario.setManualTimeCulcType(data.getManualTimeCulcType());
		rpaScenario.setOpeStartDate(data.getOpeStartDate());
		rpaScenario.setCommonNodeScenario(data.getCommonNodeScenario());
		rpaScenario.setTagRelationList(data.getTagRelationList());
		
		rpaScenario.setUpdateDate(HinemosTime.currentTimeMillis());
		rpaScenario.setUpdateUser(name);
	}
	
	/**
	 * RPAシナリオの実行ノード情報を変更します。
	 */
	public void modifyExecNode(RpaScenario data, String userId) throws RpaScenarioNotFound, InvalidRole {
		//RPAシナリオ情報を取得
		RpaScenario rpaScenario = QueryUtil.getRpaScenarioPK(data.getScenarioId(), ObjectPrivilegeMode.MODIFY);
		// 実行ノード情報を更新
		rpaScenario.setExecNodes(data.getExecNodes());
		
		rpaScenario.setUpdateDate(HinemosTime.currentTimeMillis());
		rpaScenario.setUpdateUser(userId);
	}
	
	/**
	 * RPAシナリオ情報を削除します。
	 */
	public void delete(String scenarioId) throws InvalidRole, HinemosUnknown {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// RPAシナリオ情報を検索し取得
			RpaScenario entity = null;
			try {
				entity = QueryUtil.getRpaScenarioPK(scenarioId, ObjectPrivilegeMode.MODIFY);
			} catch (RpaScenarioNotFound e) {
				throw new HinemosUnknown(e.getMessage(), e);
			}

			//RPAシナリオ情報を削除
			m_log.debug(String.format("delete() delete RPA scenario. scenarioId=%s", entity.getScenarioId())); 
			em.remove(entity);
		}
	}
	
	/**
	 * RPAシナリオ実績を削除します。
	 * @param scenarioId 削除対象のRPAシナリオID
	 */
	public void deleteOperationResult(String scenarioId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<RpaScenarioOperationResult> entities = QueryUtil.getRpaScenarioOperationResultByScenarioId(scenarioId);
			if (entities != null && !entities.isEmpty()) {
				m_log.debug(String.format("deleteOperationResult() delete RPA scenarioOperationResult. scenarioId=%s", scenarioId)); 
				for (RpaScenarioOperationResult entity : entities) {
					em.remove(entity);
				}
			}
		}
	}
	
	/**
	 * RPAシナリオ実績作成設定を作成します。
	 */
	public void addCreateSetting(RpaScenarioOperationResultCreateSetting data, String userId) throws RpaScenarioOperationResultCreateSettingDuplicate, InvalidRole, HinemosUnknown {
		long now = HinemosTime.currentTimeMillis();

		//エンティティBeanを作成
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			
			// 重複チェック
			jtm.checkEntityExists(RpaScenarioOperationResultCreateSetting.class, data.getScenarioOperationResultCreateSettingId());
			
			data.setRegDate(now);
			data.setRegUser(userId);
			data.setUpdateDate(now);
			data.setUpdateUser(userId);
			
			em.persist(data);
			// 通知グループ情報を更新
			new NotifyControllerBean().addNotifyRelation(data.getNotifyId(), data.getOwnerRoleId());

			jtm.flush();
		} catch (EntityExistsException e) {
			m_log.info("addCreateSetting() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new RpaScenarioOperationResultCreateSettingDuplicate(data.getScenarioOperationResultCreateSettingId());
		}
	}
	/**
	 * RPAシナリオ実績作成設定を変更します。
	 */
	public void modifyCreateSetting(RpaScenarioOperationResultCreateSetting data, String userId) throws RpaScenarioOperationResultCreateSettingNotFound, InvalidRole, HinemosUnknown, NotifyNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			//RPAシナリオ実績作成設定を取得
			RpaScenarioOperationResultCreateSetting entity
				= QueryUtil.getRpaScenarioCreateSettingPK(data.getScenarioOperationResultCreateSettingId(), ObjectPrivilegeMode.MODIFY);
			
			//RPAシナリオ実績作成設定を更新
			entity.setCalendarId(data.getCalendarId());
			entity.setDescription(data.getDescription());
			entity.setFacilityId(data.getFacilityId());
			entity.setInterval(data.getInterval());
			entity.setApplication(data.getApplication());
			entity.setValidFlg(data.getValidFlg());
	
			entity.setUpdateDate(HinemosTime.currentTimeMillis());
			entity.setUpdateUser(userId);
			
			// 通知グループ情報を更新
			entity.setNotifyGroupId(data.getNotifyGroupId());
			new NotifyControllerBean().modifyNotifyRelation(data.getNotifyId(), entity.getNotifyGroupId(), entity.getOwnerRoleId());
		}
	}
	
	/**
	 * RPAシナリオ実績作成設定を削除します。
	 */
	public void deleteCreateSetting(String settingId) throws RpaScenarioOperationResultCreateSettingNotFound, InvalidRole, HinemosUnknown {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			RpaScenarioOperationResultCreateSetting entity = QueryUtil.getRpaScenarioCreateSettingPK(settingId);

			NotifyControllerBean notifyControllerBean = new NotifyControllerBean();
			if (entity.getNotifyGroupId() != null) {
				// 通知グループ情報を削除
				notifyControllerBean.deleteNotifyRelation(entity.getNotifyGroupId());
			}
	
			// 通知履歴情報を削除する
			notifyControllerBean.deleteNotifyHistory(HinemosModuleConstant.RPA_SCENARIO_CREATE, settingId);

			//RPAシナリオ情報を削除
			m_log.debug(String.format("delete() delete RPA Scenario Operation Result Create Setting. ScenarioOperationResultCreateSettingId=%s", entity.getScenarioOperationResultCreateSettingId())); 
			em.remove(entity);
		} catch (RpaScenarioOperationResultCreateSettingNotFound | InvalidRole e) {
			m_log.warn("delete() failed to delete.", e);
			throw e;
		}
	}

}