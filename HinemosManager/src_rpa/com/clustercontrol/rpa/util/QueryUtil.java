/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.jpql.compile.QueryPreparator;
import com.clustercontrol.accesscontrol.util.UserRoleCache;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.QueryDivergence;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.fault.RpaManagementToolAccountNotFound;
import com.clustercontrol.fault.RpaManagementToolEndStatusMasterNotFound;
import com.clustercontrol.fault.RpaManagementToolMasterNotFound;
import com.clustercontrol.fault.RpaManagementToolRunParamMasterNotFound;
import com.clustercontrol.fault.RpaManagementToolRunTypeMasterNotFound;
import com.clustercontrol.fault.RpaManagementToolStopModeMasterNotFound;
import com.clustercontrol.fault.RpaScenarioNotFound;
import com.clustercontrol.fault.RpaScenarioOperationResultCreateSettingNotFound;
import com.clustercontrol.fault.RpaScenarioOperationResultNotFound;
import com.clustercontrol.fault.RpaScenarioTagNotFound;
import com.clustercontrol.fault.RpaToolMasterNotFound;
import com.clustercontrol.hub.model.CollectStringKeyInfo;
import com.clustercontrol.hub.model.CollectStringKeyInfoPK;
import com.clustercontrol.rpa.model.RpaManagementToolAccount;
import com.clustercontrol.rpa.model.RpaManagementToolEndStatusMst;
import com.clustercontrol.rpa.model.RpaManagementToolMst;
import com.clustercontrol.rpa.model.RpaManagementToolRunParamMst;
import com.clustercontrol.rpa.model.RpaManagementToolRunTypeMst;
import com.clustercontrol.rpa.model.RpaManagementToolRunTypeMstPK;
import com.clustercontrol.rpa.model.RpaManagementToolStopModeMst;
import com.clustercontrol.rpa.model.RpaManagementToolStopModeMstPK;
import com.clustercontrol.rpa.model.RpaManagementToolTypeMst;
import com.clustercontrol.rpa.model.RpaScenarioCoefficientPattern;
import com.clustercontrol.rpa.model.RpaToolEnvMst;
import com.clustercontrol.rpa.model.RpaToolMst;
import com.clustercontrol.rpa.model.RpaToolRunCommandMst;
import com.clustercontrol.rpa.monitor.model.RpaLogFileCheckInfo;
import com.clustercontrol.rpa.monitor.model.RpaManagementToolServiceCheckInfo;
import com.clustercontrol.rpa.scenario.model.RpaScenario;
import com.clustercontrol.rpa.scenario.model.RpaScenario.CulcType;
import com.clustercontrol.rpa.scenario.model.RpaScenarioExecNode;
import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResult;
import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResult.OperationResultStatus;
import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResultCreateSetting;
import com.clustercontrol.rpa.scenario.model.RpaScenarioTag;
import com.clustercontrol.rpa.scenario.model.RpaScenarioTagRelation;
import com.clustercontrol.rpa.scenario.model.UpdateRpaScenarioOperationResultInfo;
import com.clustercontrol.util.MessageConstant;

import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import net.sf.jpasecurity.jpql.parser.JpqlBrackets;
import net.sf.jpasecurity.jpql.parser.JpqlFrom;
import net.sf.jpasecurity.jpql.parser.JpqlParser;
import net.sf.jpasecurity.jpql.parser.JpqlSelect;
import net.sf.jpasecurity.jpql.parser.JpqlStatement;
import net.sf.jpasecurity.jpql.parser.JpqlUpdate;
import net.sf.jpasecurity.jpql.parser.JpqlWhere;
import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.jpql.parser.ToStringVisitor;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	/**
	 * RPA管理ツールアカウントを取得する。
	 * @param rpaScopeId
	 * @param userId
	 * @return RPA管理ツールアカウント
	 * @throws RpaScopeNotFound
	 * @throws InvalidRole
	 */
	public static RpaManagementToolAccount getRpaAccountPK(String rpaScopeId) throws RpaManagementToolAccountNotFound, InvalidRole {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// オブジェクト権限が無いためオーナーロールのみで権限判定
			RpaManagementToolAccount entity = em.find(RpaManagementToolAccount.class, rpaScopeId, ObjectPrivilegeMode.READ);
			
			if (entity == null) {
				RpaManagementToolAccountNotFound e = new RpaManagementToolAccountNotFound("RpaManagementToolAccount.find"
						+ ", rpaScopeId = " + rpaScopeId);
				throw e;
			}			
			return entity;
		} catch (ObjectPrivilege_InvalidRole e) {
			throw new InvalidRole(MessageConstant.MESSAGE_DO_NOT_HAVE_ENOUGH_PERMISSION.getMessage());
		}
	}

	/**
	 * RPA管理ツールアカウントを取得する。
	 * @param rpaScopeId
	 * @param mode
	 * @return RPA管理ツールアカウント
	 * @throws RpaManagementToolAccountNotFound
	 * @throws InvalidRole
	 */
	public static RpaManagementToolAccount getRpaAccountPK(String rpaScopeId, ObjectPrivilegeMode mode)
			throws RpaManagementToolAccountNotFound, InvalidRole {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			RpaManagementToolAccount entity = em.find(RpaManagementToolAccount.class, rpaScopeId, mode);
			if (entity == null) {
				RpaManagementToolAccountNotFound e = new RpaManagementToolAccountNotFound("RpaManagementToolAccount.find"
						+ ", rpaScopeId = " + rpaScopeId);
				throw e;
			}			
			return entity;
		} catch (ObjectPrivilege_InvalidRole e) {
			throw new InvalidRole(MessageConstant.MESSAGE_DO_NOT_HAVE_ENOUGH_PERMISSION.getMessage());
		}
	}

	/**
	 * RPA管理ツールアカウント一覧を取得する。
	 * @param userId
	 * @return RPAスコープ
	 */
	public static List<RpaManagementToolAccount> getRpaAccountList() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<RpaManagementToolAccount> entities = em.createNamedQuery("RpaManagementToolAccount.findAll", RpaManagementToolAccount.class, ObjectPrivilegeMode.READ)
					.getResultList();

			return entities;
		}
	}

	/**
	 * RPA管理ツールアカウント一覧を取得する。(権限チェックなし)
	 * @param userId
	 * @return RPAスコープ
	 */
	public static List<RpaManagementToolAccount> getRpaAccountList_NONE() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<RpaManagementToolAccount> entities = em.createNamedQuery("RpaManagementToolAccount.findAll", RpaManagementToolAccount.class, ObjectPrivilegeMode.NONE)
					.getResultList();

			return entities;
		}
	}

	/**
	 * 引数のアカウントIDとURLに一致するRPA管理アカウントを取得する。
	 * @throws RpaManagementToolAccountNotFound
	 */
	public static RpaManagementToolAccount getRpaAccountByAccountIdAndUrl(String accountId, String url) throws RpaManagementToolAccountNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("RpaManagementToolAccount.findByAccountIdAndUrl", RpaManagementToolAccount.class)
					.setParameter("accountId", accountId)
					.setParameter("url", url)
					.getSingleResult();
		} catch (NoResultException e) {
			throw new RpaManagementToolAccountNotFound(e);
		}
	}

	/**
	 * 引数のアカウントIDとURLに一致するRPA管理アカウントを取得する。
	 * @throws RpaManagementToolAccountNotFound
	 */
	public static RpaManagementToolAccount getRpaAccountByAccountIdAndUrlAndTenantName(String accountId, String url, String tenantName) throws RpaManagementToolAccountNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("RpaManagementToolAccount.findByAccountIdAndUrlAndTenantName", RpaManagementToolAccount.class)
					.setParameter("accountId", accountId)
					.setParameter("url", url)
					.setParameter("tenantName", tenantName)
					.getSingleResult();
		} catch (NoResultException e) {
			throw new RpaManagementToolAccountNotFound(e);
		}
	}

	public static RpaScenario getRpaScenarioPK(String scenarioId) throws RpaScenarioNotFound, InvalidRole {
		return getRpaScenarioPK(scenarioId, ObjectPrivilegeMode.READ);
	}
	
	/**
	 * RPA管理ツールマスタを検索する。
	 */
	public static RpaManagementToolMst getRpaManagementToolMstPK(String rpaManagementToolId) throws RpaManagementToolMasterNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			RpaManagementToolMst entity = em.find(RpaManagementToolMst.class, rpaManagementToolId, ObjectPrivilegeMode.NONE);
			if (entity == null) {
				RpaManagementToolMasterNotFound e = new RpaManagementToolMasterNotFound("RpaToolMst.findByPrimaryKey"
						+ ", rpaManagementToolId = " + rpaManagementToolId);
				m_log.info("getRpaManagementToolMstPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}
	
	/**
	 * RPA管理ツールマスタ一覧を取得する
	 */
	public static List<RpaManagementToolMst> getRpaManagementToolMstList() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			List<RpaManagementToolMst> entities = em.createNamedQuery("RpaManagementToolMst.findAll", RpaManagementToolMst.class).getResultList();
			return entities;
		}
	}

	/**
	 * RPA管理ツールタイプマスタ一覧を取得する
	 */
	public static List<RpaManagementToolTypeMst> getRpaManagementToolTypeMstList() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			List<RpaManagementToolTypeMst> entities = em.createNamedQuery("RpaManagementToolTypeMst.findAll", RpaManagementToolTypeMst.class).getResultList();
			return entities;
		}
	}

	/**
	 * RPA管理ツール実行種別マスタ一覧を取得する
	 */
	public static List<RpaManagementToolRunTypeMst> getRpaManagementToolRunTypeMstList(String rpaManagementToolId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			List<RpaManagementToolRunTypeMst> entities = em.createNamedQuery("RpaManagementToolRunTypeMst.findByRpaManagementToolId", RpaManagementToolRunTypeMst.class)
				.setParameter("rpaManagementToolId", rpaManagementToolId)
				.getResultList();
			return entities;
		}
	}

	/**
	 * RPA管理ツール実行種別マスタを検索する
	 * @throws RpaManagementToolRunTypeMasterNotFound 
	 */
	public static RpaManagementToolRunTypeMst getRpaManagementToolRunTypeMstPK(String rpaManagementToolId, Integer runType) 
			throws RpaManagementToolRunTypeMasterNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			RpaManagementToolRunTypeMst entity = em.find(RpaManagementToolRunTypeMst.class, 
					new RpaManagementToolRunTypeMstPK(rpaManagementToolId, runType), ObjectPrivilegeMode.NONE);
			if (entity == null) {
				RpaManagementToolRunTypeMasterNotFound e = new RpaManagementToolRunTypeMasterNotFound("RpaToolRunTypeMst.findByPrimaryKey"
						+ ", rpaManagementToolId = " + rpaManagementToolId
						+ ", runType = " + runType);
				m_log.info("getRpaManagementToolRunTypeMstPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	/**
	 * RPA管理ツール停止方法マスタ一覧を取得する
	 */
	public static List<RpaManagementToolStopModeMst> getRpaManagementToolStopModeMstList(String rpaManagementToolId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			List<RpaManagementToolStopModeMst> entities = em.createNamedQuery("RpaManagementToolStopModeMst.findByRpaManagementToolId", RpaManagementToolStopModeMst.class)
				.setParameter("rpaManagementToolId", rpaManagementToolId)
				.getResultList();
			return entities;
		}
	}

	/**
	 * RPA管理ツール停止方法マスタを検索する
	 * @throws RpaManagementToolStopModeMasterNotFound 
	 */
	public static RpaManagementToolStopModeMst getRpaManagementToolStopModeMstPK(String rpaManagementToolId, Integer stopMode) 
			throws RpaManagementToolStopModeMasterNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			RpaManagementToolStopModeMst entity = em.find(RpaManagementToolStopModeMst.class, 
					new RpaManagementToolStopModeMstPK(rpaManagementToolId, stopMode), ObjectPrivilegeMode.NONE);
			if (entity == null) {
				RpaManagementToolStopModeMasterNotFound e = new RpaManagementToolStopModeMasterNotFound("RpaToolStopModeMst.findByPrimaryKey"
						+ ", rpaManagementToolId = " + rpaManagementToolId
						+ ", stopMode = " + stopMode);
				m_log.info("getRpaManagementToolStopModeMstPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	/**
	 * RPA管理ツール起動パラメータマスタを検索する
	 * @throws RpaManagementToolRunTypeMasterNotFound 
	 */
	public static RpaManagementToolRunParamMst getRpaManagementToolRunParamMstPK(Integer paramId) 
			throws RpaManagementToolRunParamMasterNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			RpaManagementToolRunParamMst entity = em.find(RpaManagementToolRunParamMst.class, 
					paramId, ObjectPrivilegeMode.NONE);
			if (entity == null) {
				RpaManagementToolRunParamMasterNotFound e = new RpaManagementToolRunParamMasterNotFound("RpaToolRunParamMst.findByPrimaryKey"
						+ ", paramId = " + paramId);
				m_log.info("getRpaManagementToolRunParamMstPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	/**
	 * RPA管理ツール終了状態マスタを検索する
	 * @throws RpaManagementToolRunTypeMasterNotFound 
	 */
	public static RpaManagementToolEndStatusMst getRpaManagementToolEndStatusMstPK(Integer endStatusId) 
			throws RpaManagementToolEndStatusMasterNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			RpaManagementToolEndStatusMst entity = em.find(RpaManagementToolEndStatusMst.class, 
					endStatusId, ObjectPrivilegeMode.NONE);
			if (entity == null) {
				RpaManagementToolEndStatusMasterNotFound e = new RpaManagementToolEndStatusMasterNotFound("RpaToolEndStatusMst.findByPrimaryKey"
						+ ", endStatusId = " + endStatusId);
				m_log.info("getRpaManagementToolEndStatusMstPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	/**
	 * RPA管理ツール実行パラメータマスタ一覧を取得する
	 * シナリオ入力パラメータ以外を取得します。
	 */
	public static List<RpaManagementToolRunParamMst> getRpaManagementToolRunParamMstList(String rpaManagementToolId, Integer runType) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			List<RpaManagementToolRunParamMst> entities = em.createNamedQuery("RpaManagementToolRunParamMst.findByRpaManagementToolIdAndRunType", RpaManagementToolRunParamMst.class)
				.setParameter("rpaManagementToolId", rpaManagementToolId)
				.setParameter("runType", runType)
				.getResultList();
			return entities;
		}
	}

	/**
	 * RPA管理ツールの入力必須の実行パラメータマスタ一覧を取得する
	 * シナリオ入力パラメータ以外を取得します。
	 */
	public static List<RpaManagementToolRunParamMst> getRpaManagementToolRequiredRunParamMstList(String rpaManagementToolId, Integer runType) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			List<RpaManagementToolRunParamMst> entities = em.createNamedQuery("RpaManagementToolRunParamMst.findRequiredParamByRpaManagementToolIdAndRunType", RpaManagementToolRunParamMst.class)
				.setParameter("rpaManagementToolId", rpaManagementToolId)
				.setParameter("runType", runType)
				.getResultList();
			return entities;
		}
	}

	/**
	 * RPA管理ツールの変更不可の実行パラメータマスタ一覧を取得する
	 * シナリオ入力パラメータ以外を取得します。
	 */
	public static List<RpaManagementToolRunParamMst> getRpaManagementToolFixedRunParamMstList(String rpaManagementToolId, Integer runType) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			List<RpaManagementToolRunParamMst> entities = em.createNamedQuery("RpaManagementToolRunParamMst.findFixedParamByRpaManagementToolIdAndRunType", RpaManagementToolRunParamMst.class)
				.setParameter("rpaManagementToolId", rpaManagementToolId)
				.setParameter("runType", runType)
				.getResultList();
			return entities;
		}
	}

	/**
	 * RPA管理ツール終了状態マスタ一覧を取得する
	 */
	public static List<RpaManagementToolEndStatusMst> getRpaManagementToolEndStatusMstList(String rpaManagementToolId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			List<RpaManagementToolEndStatusMst> entities = em.createNamedQuery("RpaManagementToolEndStatusMst.findByRpaManagementToolId", RpaManagementToolEndStatusMst.class)
				.setParameter("rpaManagementToolId", rpaManagementToolId)
				.getResultList();
			return entities;
		}
	}
	
	/**
	 * RPA管理ツールIDからRPA管理ツールの実行終了を表すステータスを取得する。
	 */
	public static List<String> getEndStatusListByRpaManagementToolId(String rpaManagementToolId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("RpaManagementToolEndStatusMst.findEndStatusByRpaManagementToolId", String.class)
					.setParameter("rpaManagementToolId", rpaManagementToolId)
					.getResultList();
		}
	}

	/**
	 * RPAシナリオ実績作成設定一覧を取得する
	 */
	public static List<RpaScenarioOperationResultCreateSetting> getRpaScenarioCreateSettingList() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("RpaScenarioOperationResultCreateSetting.findAll", RpaScenarioOperationResultCreateSetting.class, ObjectPrivilegeMode.READ)
				.getResultList();
		}
	}

	public static RpaScenarioOperationResultCreateSetting getRpaScenarioCreateSettingPK_NONE(String settingId) throws RpaScenarioOperationResultCreateSettingNotFound, InvalidRole {
		return getRpaScenarioCreateSettingPK(settingId, ObjectPrivilegeMode.NONE);
	}

	public static RpaScenarioOperationResultCreateSetting getRpaScenarioCreateSettingPK(String settingId) throws RpaScenarioOperationResultCreateSettingNotFound, InvalidRole {
		return getRpaScenarioCreateSettingPK(settingId, ObjectPrivilegeMode.READ);
	}

	/**
	 * RPAシナリオ実績作成設定を取得する。
	 */
	public static RpaScenarioOperationResultCreateSetting getRpaScenarioCreateSettingPK(String settingId, ObjectPrivilegeMode mode)
			throws RpaScenarioOperationResultCreateSettingNotFound, InvalidRole {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			RpaScenarioOperationResultCreateSetting entity = em.find(RpaScenarioOperationResultCreateSetting.class, settingId, mode);
			if (entity == null) {
				RpaScenarioOperationResultCreateSettingNotFound e = new RpaScenarioOperationResultCreateSettingNotFound(settingId);
				m_log.info("getRpaScenarioCreateSettingPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getRpaScenarioCreateSettingPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(MessageConstant.MESSAGE_DO_NOT_HAVE_ENOUGH_PERMISSION.getMessage());
		}
	}

	/**
	 * シナリオID一覧を取得する
	 */
	public static List<String> getScenarioIdList_NONE() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<String> list = em.createNamedQuery("RpaScenario.findIdAll", String.class).getResultList();
			return list;
		}
	}

	/**
	 * RPAシナリオ一覧を取得する
	 */
	public static List<RpaScenario> getRpaScenarioList(ObjectPrivilegeMode mode) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<RpaScenario> list = em.createNamedQuery("RpaScenario.findAll", RpaScenario.class, mode).getResultList();
			return list;
		}
	}

	/**
	 * RPAシナリオ一覧を取得する。(権限チェックなし)
	 */
	public static List<RpaScenario> getRpaScenarioList_NONE() {
		return getRpaScenarioList(ObjectPrivilegeMode.NONE);
	}

	/**
	 * RPAシナリオを取得する
	 */
	public static RpaScenario getRpaScenarioPK(String scenarioId, ObjectPrivilegeMode mode) throws RpaScenarioNotFound, InvalidRole {
		RpaScenario entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find(RpaScenario.class, scenarioId, mode);
			if (entity == null) {
				RpaScenarioNotFound e = new RpaScenarioNotFound("RpaScenario.findByPrimaryKey"
						+ ", scenarioId = " + scenarioId);
				m_log.info("getRpaScenarioPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getRpaScenarioPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	/**
	 * RPAシナリオ一覧を取得する
	 */
	public static List<RpaScenario> getRpaScenarioByFilter(
			String scenarioOperationResultCreateSettingId,
			String rpaToolId,
			String scenarioId,
			String scenarioName,
			String scenarioIdentifyString,
			String ownerRoleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// 「含まない」検索を行うかの判断に使う値
			String notInclude = "NOT:";
		
			StringBuffer sbJpql = new StringBuffer();
			sbJpql.append("SELECT DISTINCT a FROM RpaScenario a WHERE true = true");
			// scenarioOperationResultCreateSettingId
			if(scenarioOperationResultCreateSettingId != null && !"".equals(scenarioOperationResultCreateSettingId)) {
				if(!scenarioOperationResultCreateSettingId.startsWith(notInclude)) {
					sbJpql.append(" AND a.scenarioOperationResultCreateSettingId like :scenarioOperationResultCreateSettingId");
				}else{
					sbJpql.append(" AND a.scenarioOperationResultCreateSettingId not like :scenarioOperationResultCreateSettingId");
				}
			}
			// rpaToolId
			if(rpaToolId != null && !"".equals(rpaToolId)) {
				if(!rpaToolId.startsWith(notInclude)) {
					sbJpql.append(" AND a.rpaToolId like :rpaToolId");
				}else{
					sbJpql.append(" AND a.rpaToolId not like :rpaToolId");
				}
			}
			// scenarioId
			if(scenarioId != null && !"".equals(scenarioId)) {
				if(!scenarioId.startsWith(notInclude)) {
					sbJpql.append(" AND a.scenarioId like :scenarioId");
				}else{
					sbJpql.append(" AND a.scenarioId not like :scenarioId");
				}
			}
			// scenarioName
			if(scenarioName != null && !"".equals(scenarioName)) {
				if(!scenarioName.startsWith(notInclude)) {
					sbJpql.append(" AND a.scenarioName like :scenarioName");
				}else{
					sbJpql.append(" AND a.scenarioName not like :scenarioName");
				}
			}
			// scenarioIdentifyString
			if(scenarioIdentifyString != null && !"".equals(scenarioIdentifyString)) {
				if (!scenarioIdentifyString.startsWith(notInclude)) {
					sbJpql.append(" AND a.scenarioIdentifyString like :scenarioIdentifyString");
				}else{
					sbJpql.append(" AND a.scenarioIdentifyString not like :scenarioIdentifyString");
				}
			}
			// ownerRoleId
			if(ownerRoleId != null && !"".equals(ownerRoleId)) {
				if (!ownerRoleId.startsWith(notInclude)) {
					sbJpql.append(" AND a.ownerRoleId like :ownerRoleId");
				}else{
					sbJpql.append(" AND a.ownerRoleId not like :ownerRoleId");
				}
			}
			TypedQuery<RpaScenario> typedQuery = em.createQuery(sbJpql.toString(), RpaScenario.class);

			// scenarioOperationResultCreateSettingId
			if(scenarioOperationResultCreateSettingId != null && !"".equals(scenarioOperationResultCreateSettingId)) {
				if(!scenarioOperationResultCreateSettingId.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("scenarioOperationResultCreateSettingId",
							QueryDivergence.escapeLikeCondition(scenarioOperationResultCreateSettingId));
				}else{
					typedQuery = typedQuery.setParameter("scenarioOperationResultCreateSettingId",
							QueryDivergence.escapeLikeCondition(scenarioOperationResultCreateSettingId.substring(notInclude.length())));
				}
			}
			// rpaToolId
			if(rpaToolId != null && !"".equals(rpaToolId)) {
				if(!rpaToolId.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("rpaToolId",
							QueryDivergence.escapeLikeCondition(rpaToolId));
				}else{
					typedQuery = typedQuery.setParameter("rpaToolId",
							QueryDivergence.escapeLikeCondition(rpaToolId.substring(notInclude.length())));
				}
			}
			// scenarioId
			if(scenarioId != null && !"".equals(scenarioId)) {
				if(!scenarioId.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("scenarioId",
							QueryDivergence.escapeLikeCondition(scenarioId));
				}else{
					typedQuery = typedQuery.setParameter("scenarioId",
							QueryDivergence.escapeLikeCondition(scenarioId.substring(notInclude.length())));
				}
			}
			// scenarioName
			if(scenarioName != null && !"".equals(scenarioName)) {
				if(!scenarioName.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("scenarioName",
							QueryDivergence.escapeLikeCondition(scenarioName));
				}else{
					typedQuery = typedQuery.setParameter("scenarioName",
							QueryDivergence.escapeLikeCondition(scenarioName.substring(notInclude.length())));
				}
			}
			// scenarioIdentifyString
			if(scenarioIdentifyString != null && !"".equals(scenarioIdentifyString)) {
				if(!scenarioIdentifyString.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("scenarioIdentifyString",
							QueryDivergence.escapeLikeCondition(scenarioIdentifyString));
				}else{
					typedQuery = typedQuery.setParameter("scenarioIdentifyString",
							QueryDivergence.escapeLikeCondition(scenarioIdentifyString.substring(notInclude.length())));
				}
			}
			// ownerRoleId
			if(ownerRoleId != null && !"".equals(ownerRoleId)) {
				if(!ownerRoleId.startsWith(notInclude)) {
					typedQuery = typedQuery.setParameter("ownerRoleId",
							QueryDivergence.escapeLikeCondition(ownerRoleId));
				}else{
					typedQuery = typedQuery.setParameter("ownerRoleId",
							QueryDivergence.escapeLikeCondition(ownerRoleId.substring(notInclude.length())));
				}
			}
			return typedQuery.getResultList();
		}
	}

	/**
	 * 監視項目IDからRPAログファイル監視情報を取得する
	 */
	public static RpaLogFileCheckInfo getMonitorRpaLogfileInfoPK_NONE(String monitorId) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			RpaLogFileCheckInfo entity = em.find(RpaLogFileCheckInfo.class, monitorId, ObjectPrivilegeMode.NONE);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("MonitorRpaLogFileCheckInfoEntity.findByPrimaryKey"
						+ ", monitorId = " + monitorId);
				m_log.info("getMonitorRpaLogfileInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	/**
	 * 監視項目IDからRPA管理ツールサービス監視情報を取得する
	 */
	public static RpaManagementToolServiceCheckInfo getMonitorRpaManagementToolServiceInfoPK_NONE(String monitorId) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			RpaManagementToolServiceCheckInfo entity = em.find(RpaManagementToolServiceCheckInfo.class, monitorId, ObjectPrivilegeMode.NONE);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("MonitorRpaManagementToolServiceCheckInfoEntity.findByPrimaryKey"
						+ ", monitorId = " + monitorId);
				m_log.info("getMonitorRpaManagementToolServiceInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	/**
	 * RPAツールマスタを検索する。
	 */
	public static RpaToolMst getRpaToolMstPK(String rpaToolId) throws RpaToolMasterNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			RpaToolMst entity = em.find(RpaToolMst.class, rpaToolId, ObjectPrivilegeMode.NONE);
			if (entity == null) {
				RpaToolMasterNotFound e = new RpaToolMasterNotFound("RpaToolMst.findByPrimaryKey"
						+ ", rpaToolId = " + rpaToolId);
				m_log.info("geRpaToolMstPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}
	
	/**
	 * RPAツールマスタ一覧を取得する
	 */
	public static List<RpaToolMst> getRpaToolMstList() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			List<RpaToolMst> entities = em.createNamedQuery("RpaToolMst.findAll", RpaToolMst.class).getResultList();
			return entities;
		}
	}

	/**
	 * 環境毎のRPAツールマスタを検索する。
	 */
	public static RpaToolEnvMst getRpaToolEnvMstPK(String rpaToolEnvId) throws RpaToolMasterNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			RpaToolEnvMst entity = em.find(RpaToolEnvMst.class, rpaToolEnvId, ObjectPrivilegeMode.NONE);
			if (entity == null) {
				RpaToolMasterNotFound e = new RpaToolMasterNotFound("RpaToolEnvMst.findByPrimaryKey"
						+ ", rpaToolEnvId = " + rpaToolEnvId);
				m_log.info("geRpaToolEnvMstPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}
	/**
	 * 環境毎のRPAツールマスタ一覧を取得する
	 */
	public static List<RpaToolEnvMst> getRpaToolEnvMstList() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			List<RpaToolEnvMst> entities = em.createNamedQuery("RpaToolEnvMst.findAll", RpaToolEnvMst.class).getResultList();
			return entities;
		}
	}

	/**
	 * RPAツール実行コマンドマスタを検索する。
	 */
	public static RpaToolRunCommandMst getRpaToolRunCommandMstPK(String rpaToolId) throws RpaToolMasterNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			RpaToolRunCommandMst entity = em.find(RpaToolRunCommandMst.class, rpaToolId, ObjectPrivilegeMode.NONE);
			if (entity == null) {
				RpaToolMasterNotFound e = new RpaToolMasterNotFound("RpaToolRunCommandMst.findByPrimaryKey"
						+ ", rpaToolId = " + rpaToolId);
				m_log.info("geRpaToolRunCommandMstPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}
	
	/**
	 * RPAツール実行コマンドマスタ一覧を取得する
	 */
	public static List<RpaToolRunCommandMst> getRpaToolRunCommandMstList() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			List<RpaToolRunCommandMst> entities = em.createNamedQuery("RpaToolRunCommandMst.findAll", RpaToolRunCommandMst.class).getResultList();
			return entities;
		}
	}

	/**
	 * シナリオIDからシナリオ実績を検索する。
	 * @param scenarioId
	 * @return　List<RpaScenarioOperationResult>
	 */
	public static List<RpaScenarioOperationResult> getRpaScenarioOperationResultByScenarioId(String scenarioId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<RpaScenarioOperationResult> entities = em.createNamedQuery("RpaScenarioOperationResult.findByScenarioId",RpaScenarioOperationResult.class)
					.setParameter("scenarioId", scenarioId)
					.getResultList();
			
			return entities;			
		}
	}

	/**
	 * 作成設定ID、ファシリティID、シナリオ識別文字列でシナリオを検索する。<BR>
	 * 上記パラメータでシナリオは一意に特定できる。
	 * @param scenarioOperationResultCreateSettingId
	 * @param facilityId
	 * @param scenarioIdentifyString
	 * @return シナリオ
	 * @throws RpaScenarioNotFound 
	 */
	public static RpaScenario getScenario(String scenarioOperationResultCreateSettingId, String facilityId, String scenarioIdentifyString, String ownerRoleId) throws RpaScenarioNotFound{
		return getScenario(scenarioOperationResultCreateSettingId, facilityId, scenarioIdentifyString, ObjectPrivilegeMode.READ, ownerRoleId);
	}

	/**
	 * 作成設定ID、ファシリティID、シナリオ識別文字列でシナリオを検索する。<BR>
	 * 上記パラメータでシナリオは一意に特定できる。
	 * @param scenarioOperationResultCreateSettingId
	 * @param facilityId
	 * @param scenarioIdentifyString
	 * @return シナリオ
	 * @throws RpaScenarioNotFound 
	 */
	public static RpaScenario getScenario(String scenarioOperationResultCreateSettingId, String facilityId, String scenarioIdentifyString, ObjectPrivilegeMode mode, String ownerRoleId) throws RpaScenarioNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery_OR("RpaScenario.findScenario", RpaScenario.class, mode, ownerRoleId)
					.setParameter("scenarioOperationResultCreateSettingId", scenarioOperationResultCreateSettingId)
					.setParameter("facilityId", facilityId)
					.setParameter("scenarioIdentifyString", scenarioIdentifyString)
					.getSingleResult();
		} catch (NoResultException e) {
			m_log.debug("getScenario : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new RpaScenarioNotFound(e);
		}
	}

	/**
	 * 複数ノードで共通のシナリオを検索する。<BR>
	 * 同一監視項目IDおよびシナリオ識別文字列のシナリオ間では、「複数ノードで共通のシナリオ」は一個のみ
	 * @param scenarioOperationResultCreateSettingId
	 * @param scenarioIdentifyString
	 * @return 複数ノードで共通のシナリオまたはnull
	 * @throws RpaScenarioNotFound 
	 */
	public static RpaScenario getCommonScenario(String scenarioOperationResultCreateSettingId, String scenarioIdentifyString) throws RpaScenarioNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("RpaScenario.findCommonScenario", RpaScenario.class)
					.setParameter("scenarioOperationResultCreateSettingId", scenarioOperationResultCreateSettingId)
					.setParameter("scenarioIdentifyString", scenarioIdentifyString)
					.getSingleResult();
		} catch (NoResultException e) {
			m_log.debug("getCommonScenario : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new RpaScenarioNotFound(e);
		}
	}
	
	/**
	 * 対象のシナリオタグに紐づくシナリオが存在する場合、紐づいているタグのIDを取得する
	 */
	public static List<String> getRpaScenarioTagIdFindByScenarioTagRelation(List<String> tagIds){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("RpaScenario.relationScenarioTagId", String.class)
					.setParameter("tagIds", tagIds).getResultList();
		}
	}
	
	public static RpaScenarioTag getRpaScenarioTagPK(String tagId) throws RpaScenarioTagNotFound, InvalidRole {
		return getRpaScenarioTagPK(tagId, ObjectPrivilegeMode.READ);
	}
	
	/**
	 * RPAシナリオタグを取得する
	 */
	public static RpaScenarioTag getRpaScenarioTagPK(String tagId, ObjectPrivilegeMode mode) throws RpaScenarioTagNotFound, InvalidRole {
		RpaScenarioTag entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find(RpaScenarioTag.class, tagId, mode);
			if (entity == null) {
				RpaScenarioTagNotFound e = new RpaScenarioTagNotFound("RpaScenarioTag.findByPrimaryKey"
						+ ", tagId = " + tagId);
				m_log.info("getRpaScenarioTagPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getRpaScenarioTagPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}
	
	/**
	 * RPAシナリオタグを全検索
	 */
	public static List<RpaScenarioTag> getAllRpaScenarioTag() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<RpaScenarioTag> list
			= em.createNamedQuery("RpaScenarioTag.findIdAll", RpaScenarioTag.class).getResultList();
			return list;
		}
	}
	
	/**
	 * オーナーロールIDを条件としてRPAシナリオタグを全検索
	 */
	public static List<RpaScenarioTag> getAllRpaScenarioTag_OR(String ownerRoleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<RpaScenarioTag> list
			= em.createNamedQuery_OR("RpaScenarioTag.findIdAll", RpaScenarioTag.class, ownerRoleId).getResultList();
			return list;
		}
	}
	
	/**
	 * RPAシナリオタグのリレーションを全検索
	 */
	public static List<RpaScenarioTagRelation> getAllRpaScenarioTagRelation() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<RpaScenarioTagRelation> list
			= em.createNamedQuery("RpaScenarioTagRelation.findAll", RpaScenarioTagRelation.class).getResultList();
			return list;
		}
	}
	
	/**
	 * RPAシナリオ実績一覧を取得する
	 */
	public static List<RpaScenarioOperationResult> getRpaScenarioOperationResultByFilter(
			Long startDateFrom,
			Long startDateTo,
			String scenarioId,
			List<String> tagIdList,
			List<OperationResultStatus> statusList,
			List<String> facilityIds,
			Integer firstResult,
			Integer maxResults) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
		
			StringBuffer sbJpql = new StringBuffer();
			sbJpql.append("SELECT DISTINCT b FROM RpaScenarioOperationResult b "
					+ "INNER JOIN RpaScenario c on b.scenarioId = c.scenarioId "
					+ "WHERE true = true AND ");
			
			StringBuffer scenarioSbJpql = new StringBuffer();
			scenarioSbJpql.append("SELECT DISTINCT a.scenarioId FROM RpaScenario a WHERE true = true");
			
			// シナリオの権限チェックを追加
			List<String> roleIds = checkObjectPrivilege(RpaScenario.class);
			String scenarioJpql = null;
			if(roleIds != null){
				scenarioJpql = getQuery(scenarioSbJpql.toString(), RpaScenario.class, ObjectPrivilegeMode.READ, roleIds);
			} else {
				scenarioJpql = scenarioSbJpql.toString();
			}
			sbJpql.append("b.scenarioId IN (" + scenarioJpql + ")");
			
			// startDateFrom
			if(startDateFrom != null) {
				sbJpql.append(" AND b.startDate >= :startDateFrom");
			}
			// startDateTo
			if(startDateTo != null) {
				sbJpql.append(" AND b.startDate < :startDateTo");
			}
			// scenarioId
			if(scenarioId != null && !"".equals(scenarioId)) {
				sbJpql.append(" AND b.scenarioId like :scenarioId");
			}
			// tagId
			int tagIndex = 0;
			if (tagIdList != null && !tagIdList.isEmpty()){
				String tagJpql = "";
				for (String tagId : tagIdList){
					if(tagId == null || "".equals(tagId)) {
						continue;
					}
					
					if (tagJpql.isEmpty()){
						tagJpql = " AND (:tagId_" + tagIndex + " MEMBER OF c.tagRelationList ";
					} else {
						tagJpql = tagJpql + "OR :tagId_" + tagIndex + " MEMBER OF c.tagRelationList ";
					}
					tagIndex++;
				}
				tagJpql = tagJpql + ")";
				sbJpql.append(tagJpql);
			}
			// status
			if(statusList != null && !statusList.isEmpty()) {
				sbJpql.append(" AND b.status IN :statusList");
			}
			// facilityId
			if(facilityIds != null && !facilityIds.isEmpty()) {
				sbJpql.append(" AND b.facilityId IN :facilityIds");
			}
			sbJpql.append(" ORDER BY b.startDate DESC");
			
			TypedQuery<RpaScenarioOperationResult> typedQuery = em.createQuery(sbJpql.toString(), RpaScenarioOperationResult.class);

			// startDateFrom
			if(startDateFrom != null) {
				typedQuery = typedQuery.setParameter("startDateFrom", startDateFrom);
			}
			// startDateTo
			if(startDateTo != null) {
				typedQuery = typedQuery.setParameter("startDateTo", startDateTo);
			}
			// scenarioId
			if(scenarioId != null && !"".equals(scenarioId)) {
				typedQuery = typedQuery.setParameter("scenarioId",
						QueryDivergence.escapeLikeCondition(scenarioId));
			}
			// tagId
			if (tagIdList != null){
				for (int i = 0; i < tagIndex; i++){
					if(tagIdList.get(i) != null && !"".equals(tagIdList.get(i))) {
						typedQuery = typedQuery.setParameter("tagId_" + i, tagIdList.get(i));
					}
				}
			}
			// status
			if(statusList != null && !statusList.isEmpty()) {
				typedQuery = typedQuery.setParameter("statusList", statusList);
			}
			// facilityId
			if(facilityIds != null && !facilityIds.isEmpty()) {
				typedQuery = typedQuery.setParameter("facilityIds", facilityIds);
			}
			
			// ページング処理
			if (firstResult != null) {
				typedQuery.setFirstResult(firstResult);
			}
			if (maxResults != null) {
				typedQuery.setMaxResults(maxResults);
			}
			
			// roleIds
			if (roleIds != null){
				HinemosEntityManager.appendParam(typedQuery, "roleId", roleIds.toArray(new String[roleIds.size()]));
			}
			
			return typedQuery.getResultList();
		}
	}
	
	/**
	 * ユーザの権限情報を取得する
	 * 
	 */
	private static List<String> checkObjectPrivilege(Class<?> entityClass){
		HinemosObjectPrivilege hinemosObjectPrivilege = entityClass.getAnnotation(HinemosObjectPrivilege.class);
		// エンティティにAnnotationが設定されていない場合はそのままの実装を返す
		if (hinemosObjectPrivilege == null) {
			return null;
		}
		
		String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		// ユーザ情報が取得できない場合はそのままの実装を返す
		if (loginUser == null || "".equals(loginUser.trim())) {
			return null;
		}

		// ADMINISTRATORSロールに所属している場合はnullを返す
		Boolean isAdministrator = (Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR);
		if (isAdministrator != null && isAdministrator) {
			return null;
		}
		
		return UserRoleCache.getRoleIdList(loginUser);
	}
	
	/**
	 * ユーザ情報を取得し、JPQLにオブジェクト権限チェックを追加する
	 * 
	 */
	private static <T> String getQuery(String sbJpql, Class<?> entityClass, ObjectPrivilegeMode mode, List<String> roleIds){
		Boolean isAdministrator = (Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR);
		
		if (isAdministrator != null && isAdministrator) {
			// ADMINISTRATORSロールの場合はオブジェクト権限チェックを行わない。
			return sbJpql;
		}
		
		// オブジェクト権限チェックを含むJPQLに変換
		String afterJpql = getObjectPrivilegeJPQL(sbJpql, entityClass, mode, null);
		afterJpql = afterJpql.replaceAll(":roleIds", HinemosEntityManager.getParamNameString("roleId", roleIds.toArray(new String[roleIds.size()])));
		
		return afterJpql;
	}
	
	/**
	 * JPQLにオブジェクト権限チェックを入れて返す
	 * com.clustercontrol.commons.util.HinemosEntityManager.getObjectPrivilegeJPQLを流用
	 * 
	 */
	private static String getObjectPrivilegeJPQL(String jpqlString, Class<?> entityClass, ObjectPrivilegeMode mode, String ownerRoleId) {

		String rtnString = "";
		try {
			HinemosObjectPrivilege hinemosObjectPrivilege = entityClass.getAnnotation(HinemosObjectPrivilege.class);
			String objectType = hinemosObjectPrivilege.objectType();

			// JPQLの構文解析
			JpqlParser jpqlParser = new JpqlParser();
			JpqlFrom jpqlFrom = null;
			JpqlWhere jpqlWhere = null;
			JpqlStatement statement = jpqlParser.parseQuery(jpqlString);

			if (statement.jjtGetChild(0) instanceof JpqlSelect
					|| statement.jjtGetChild(0) instanceof JpqlUpdate) {

				if (statement.jjtGetChild(0) instanceof JpqlSelect) {
					JpqlSelect jpqlSelect = (JpqlSelect)statement.jjtGetChild(0);
					for(int i=0 ; i<jpqlSelect.jjtGetNumChildren() ; i++ ) {
						if (jpqlSelect.jjtGetChild(i) instanceof JpqlFrom) {
							jpqlFrom = (JpqlFrom)jpqlSelect.jjtGetChild(i);
						} else if (jpqlSelect.jjtGetChild(i) instanceof JpqlWhere) {
							jpqlWhere = (JpqlWhere)jpqlSelect.jjtGetChild(i);
							break;
						}
					}
				}
				else if (statement.jjtGetChild(0) instanceof JpqlUpdate) {
					JpqlUpdate jpqlUpdate = (JpqlUpdate)statement.jjtGetChild(0);
					for(int i=0 ; i<jpqlUpdate.jjtGetNumChildren() ; i++ ) {
						if (jpqlUpdate.jjtGetChild(i) instanceof JpqlFrom) {
							jpqlFrom = (JpqlFrom)jpqlUpdate.jjtGetChild(i);
						} else if (jpqlUpdate.jjtGetChild(i) instanceof JpqlWhere) {
							jpqlWhere = (JpqlWhere)jpqlUpdate.jjtGetChild(i);
							break;
						}
					}
				}
				// オブジェクト権限チェックのJPQLを挿入
				Node jpqlExists = null;
				if (ownerRoleId == null) {
					jpqlExists = QueryPreparator.createObjectPrivilegeExists(objectType, mode);
				} else {
					jpqlExists = QueryPreparator.createObjectPrivilegeExists(objectType, mode, ownerRoleId);
				}
				if (jpqlWhere == null) {
					jpqlWhere = QueryPreparator.createWhere(jpqlExists);
					Node parent = jpqlFrom.jjtGetParent();
					for (int i = parent.jjtGetNumChildren(); i > 2; i--) {
						parent.jjtAddChild(parent.jjtGetChild(i - 1), i);
					}
					parent.jjtAddChild(jpqlWhere, 2);
				} else {
					Node condition = jpqlWhere.jjtGetChild(0);
					if (!(condition instanceof JpqlBrackets)) {
						condition = QueryPreparator.createBrackets(condition);
					}
					Node and = QueryPreparator.createAnd(condition, jpqlExists);
					and.jjtSetParent(jpqlWhere);
					jpqlWhere.jjtSetChild(and, 0);
				}
			}

			ToStringVisitor v = new ToStringVisitor();
			statement.jjtAccept(v, null);
			rtnString = statement.toString();
			m_log.debug("getObjectPrivilegeJPQL() jpql = " + rtnString);
		} catch (Exception e) {
			m_log.warn("getObjectPrivilegeJPQL() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}
		return rtnString;
	}
	
	/**
	 * RPAシナリオ実績合計数を取得する
	 */
	public static Long getRpaScenarioOperationResultCountByFilter(
			Long startDateFrom,
			Long startDateTo,
			String scenarioId,
			List<String> tagIdList,
			List<OperationResultStatus> statusList,
			List<String> facilityIds) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			
			StringBuffer sbJpql = new StringBuffer();
			sbJpql.append("SELECT COUNT (DISTINCT b) FROM RpaScenarioOperationResult b "
					+ "INNER JOIN RpaScenario c on b.scenarioId = c.scenarioId "
					+ "WHERE true = true AND ");
			
			StringBuffer scenarioSbJpql = new StringBuffer();
			scenarioSbJpql.append("SELECT DISTINCT a.scenarioId FROM RpaScenario a WHERE true = true");
			
			// シナリオの権限チェックを追加
			List<String> roleIds = checkObjectPrivilege(RpaScenario.class);
			String scenarioJpql = null;
			if(roleIds != null){
				scenarioJpql = getQuery(scenarioSbJpql.toString(), RpaScenario.class, ObjectPrivilegeMode.READ, roleIds);
			} else {
				scenarioJpql = scenarioSbJpql.toString();
			}
			sbJpql.append("b.scenarioId IN (" + scenarioJpql + ")");
			
			// startDateFrom
			if(startDateFrom != null) {
				sbJpql.append(" AND b.startDate >= :startDateFrom");
			}
			// startDateTo
			if(startDateTo != null) {
				sbJpql.append(" AND b.startDate < :startDateTo");
			}
			// scenarioId
			if(scenarioId != null && !"".equals(scenarioId)) {
				sbJpql.append(" AND b.scenarioId like :scenarioId");
			}
			// tagId
			int tagIndex = 0;
			if (tagIdList != null && !tagIdList.isEmpty()){
				String tagJpql = "";
				for (String tagId : tagIdList){
					if(tagId == null || "".equals(tagId)) {
						continue;
					}
					
					if (tagJpql.isEmpty()){
						tagJpql = " AND (:tagId_" + tagIndex + " MEMBER OF c.tagRelationList ";
					} else {
						tagJpql = tagJpql + "OR :tagId_" + tagIndex + " MEMBER OF c.tagRelationList ";
					}
					tagIndex++;
				}
				tagJpql = tagJpql + ")";
				sbJpql.append(tagJpql);
			}
			// status
			if(statusList != null && !statusList.isEmpty()) {
				sbJpql.append(" AND b.status IN :statusList");
			}
			// facilityId
			if(facilityIds != null && !facilityIds.isEmpty()) {
				sbJpql.append(" AND b.facilityId IN :facilityIds");
			}

			TypedQuery<Long> typedQuery = em.createQuery(sbJpql.toString(), Long.class);

			// startDateFrom
			if(startDateFrom != null) {
				typedQuery = typedQuery.setParameter("startDateFrom", startDateFrom);
			}
			// startDateTo
			if(startDateTo != null) {
				typedQuery = typedQuery.setParameter("startDateTo", startDateTo);
			}
			// scenarioId
			if(scenarioId != null && !"".equals(scenarioId)) {
				typedQuery = typedQuery.setParameter("scenarioId",
						QueryDivergence.escapeLikeCondition(scenarioId));
			}
			// tagId
			if (tagIdList != null){
				for (int i = 0; i < tagIndex; i++){
					if(tagIdList.get(i) != null && !"".equals(tagIdList.get(i))) {
						typedQuery = typedQuery.setParameter("tagId_" + i, tagIdList.get(i));
					}
				}
			}
			// status
			if(statusList != null && !statusList.isEmpty()) {
				typedQuery = typedQuery.setParameter("statusList", statusList);
			}
			// facilityId
			if(facilityIds != null && !facilityIds.isEmpty()) {
				typedQuery = typedQuery.setParameter("facilityIds", facilityIds);
			}
			
			// roleIds
			if (roleIds != null){
				HinemosEntityManager.appendParam(typedQuery, "roleId", roleIds.toArray(new String[roleIds.size()]));
			}
			
			return typedQuery.getSingleResult();
		}
	}
	
	public static RpaScenarioOperationResult getRpaScenarioOperationResultPK(Long resultId) throws RpaScenarioOperationResultNotFound, InvalidRole {
		return getRpaScenarioOperationResultPK(resultId, ObjectPrivilegeMode.READ);
	}
	
	/**
	 * 実績IDに基づいてRPAシナリオ実績を取得する
	 */
	public static RpaScenarioOperationResult getRpaScenarioOperationResultPK(Long resultId, ObjectPrivilegeMode mode) throws RpaScenarioOperationResultNotFound, InvalidRole {
		RpaScenarioOperationResult entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find(RpaScenarioOperationResult.class, resultId, mode);
			if (entity == null) {
				RpaScenarioOperationResultNotFound e = 
						new RpaScenarioOperationResultNotFound("RpaScenarioOperationResult.findByPrimaryKey" + ", resultId = " + resultId);
				m_log.info("getRpaScenarioOperationResultPK() : "+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getRpaScenarioOperationResultPK() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	/**
	 * 指定したシナリオ識別子およびシナリオ実績作成設定と一致する実行ノード一覧を取得する
	 */
	public static List<RpaScenarioExecNode> getRpaScenarioExecNodeList(String scenarioIdentifyString, String scenarioOperationResultCreateSettingId) {
		return getRpaScenarioExecNodeList(scenarioIdentifyString, scenarioOperationResultCreateSettingId, null);
	}

	/**
	 * 指定したシナリオ識別子およびシナリオ実績作成設定と一致する実行ノード一覧を取得する
	 */
	public static List<RpaScenarioExecNode> getRpaScenarioExecNodeList(String scenarioIdentifyString, String scenarioOperationResultCreateSettingId, String ownerRoleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			List<RpaScenarioExecNode> entities = em.createNamedQuery_OR("RpaScenarioExecNode.findByScenarioIdentifyStringAndCreateSettingId", RpaScenarioExecNode.class, ObjectPrivilegeMode.READ, ownerRoleId)
					.setParameter("scenarioIdentifyString", scenarioIdentifyString)
					.setParameter("scenarioOperationResultCreateSettingId", scenarioOperationResultCreateSettingId)
					.getResultList();
			return entities;
		}
	}
	
	/**
	 * collectIdをキーにしたCollectStringKeyInfo(PK)のマップを取得する。
	 */
	public static Map<Long, CollectStringKeyInfoPK> getCollectStringKeyInfoMapFindByMonitorIdsAndFacilityIds(List<String> monitorIds, List<String> facilityIds) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("CollectStringKeyInfo.findByMonitorIdsAndFacilityIds", CollectStringKeyInfo.class)
					.setParameter("monitorIds", monitorIds)
					.setParameter("facilityIds", facilityIds)
					.getResultStream()
					.collect(Collectors.toMap(CollectStringKeyInfo::getCollectId, CollectStringKeyInfo::getId));
		}
	}
	
	/**
	 * fromDate(基準時刻)からCollectStringDataの最初のIDを取得する。
	 */
	public static long getStartPosition(long fromDate) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			Long result = em.createNamedQuery("CollectStringData.findStartPosition", Long.class)
					.setParameter("fromDate", fromDate)
					.getSingleResult(); 
			if (result != null) {
				return result;
			} else {
				return 0L;
			}
		} catch (NoResultException e) {
			// 取得できなかった場合(基準時刻以前のデータが無い場合)、0を返す。
			return 0L;
		}
	}
	
	/**
	 * RPAシナリオ実績情報の削減工数をカウントして返します。
	 */
	public static List<Object[]> getRpaScenarioOperationResultReductionCount(
			String queryName, Long startDateFrom, Long startDateTo, List<String> facilityIds, CulcType culcType) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			
			String name;
			String culcTypeParamName;
			switch (culcType) {
			case AUTO:
				name = queryName + "Auto";
				culcTypeParamName = "AUTO";
				break;
			case FIX_TIME:
			default:
				name = queryName + "FixTime";
				culcTypeParamName = "FIX_TIME";
				break;
			}
			
			List<String> roleIds = checkObjectPrivilege(RpaScenario.class);
			TypedQuery<Object[]> query;
			if(roleIds != null){
				query = em.createNamedQuery(name + "ByObjectPrivilege", Object[].class)
						.setParameter("roleIds", roleIds)
						;
			} else {
				query = em.createNamedQuery(name, Object[].class)
						;
			}
			
			return query
					.setParameter(culcTypeParamName, culcType)
					.setParameter("NORMAL_END", OperationResultStatus.NORMAL_END)
					.setParameter("ERROR_END", OperationResultStatus.ERROR_END)
					.setParameter("startDateFrom", startDateFrom)
					.setParameter("startDateTo", startDateTo)
					.setParameter("facilityIds", facilityIds)
					.getResultList();
		}
	}
	/**
	 * RPAシナリオ実績情報の削減工数を集計して返します。
	 */
	public static List<Object[]> getRpaScenarioOperationResultReductionCount(
			String queryName, Long startDateFrom, Long startDateTo, List<String> facilityIds,int manualTimeIdx, int runTimeIdx) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			// 手動操作時間が自動算出及び固定のシナリオでそれぞれカウントする。
			List<Object[]> autoCount = getRpaScenarioOperationResultReductionCount(queryName, startDateFrom, startDateTo, facilityIds, CulcType.AUTO);
			List<Object[]> fixedCount = getRpaScenarioOperationResultReductionCount(queryName, startDateFrom, startDateTo, facilityIds, CulcType.FIX_TIME);
			
			// 手動操作時間固定のシナリオのデータは、再計算を行う。
			reculcForFixManualTime(fixedCount, manualTimeIdx, runTimeIdx);
			
			List<Object[]> ret = new ArrayList<>();
			ret.addAll(autoCount);
			ret.addAll(fixedCount);
			
			return ret;
		}
	}
	
	/**
	 * 手動操作時間固定のシナリオデータ向けの再計算を行う。<br>
	 * 削減時間:手動操作時間 - 実行時間
	 */
	private static void reculcForFixManualTime(List<Object[]> data, int manualTimeIdx, int runTimeIdx) {
		for (Object[] objects : data) {
			Number manualTimeData = (Number) objects[manualTimeIdx];
			if (manualTimeData == null) { manualTimeData = 0;}
			Number runTimeData = (Number) objects[runTimeIdx];
			if (runTimeData == null) { runTimeData = 0;}
			
			objects[manualTimeIdx] = manualTimeData.doubleValue() - runTimeData.doubleValue();
		}
	}


	/**
	 * RPAシナリオ実績情報の日別シナリオ実施件数をカウントして返します。
	 */
	public static List<Object[]> getRpaScenarioOperationResultDailyErrorsCount(
			Long startDateFrom, Long startDateTo, List<String> facilityIds) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			List<String> roleIds = checkObjectPrivilege(RpaScenario.class);
			if(roleIds != null){
				return em.createNamedQuery("RpaScenarioOperationResult.dailyErrorsCountByObjectPrivilege", Object[].class)
						.setParameter("NORMAL_END", OperationResultStatus.NORMAL_END)
						.setParameter("ERROR_END", OperationResultStatus.ERROR_END)
						.setParameter("startDateFrom", startDateFrom)
						.setParameter("startDateTo", startDateTo)
						.setParameter("facilityIds", facilityIds)
						.setParameter("roleIds", roleIds)
						.getResultList();
			} else {
				return em.createNamedQuery("RpaScenarioOperationResult.dailyErrorsCount", Object[].class)
						.setParameter("NORMAL_END", OperationResultStatus.NORMAL_END)
						.setParameter("ERROR_END", OperationResultStatus.ERROR_END)
						.setParameter("startDateFrom", startDateFrom)
						.setParameter("startDateTo", startDateTo)
						.setParameter("facilityIds", facilityIds)
						.getResultList();
			}
		}
	}
	
	/**
	 * RPAシナリオ実績情報の時間帯別削減工数をカウントして返します。
	 */
	public static List<Object[]> getRpaScenarioOperationResultHourlyReductionCount(
			Long startDateFrom, Long startDateTo, List<String> facilityIds) {
		return getRpaScenarioOperationResultReductionCount("RpaScenarioOperationResult.hourlyReductionCount", startDateFrom, startDateTo, facilityIds, 1, 2);
	}

	/**
	 * RPAシナリオ実績情報のシナリオ別エラー数をカウントして返します。
	 */
	public static List<Object[]> getRpaScenarioOperationResultsSenarioErrorsCount(
			Long startDateFrom, Long startDateTo, List<String> facilityIds) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			List<String> roleIds = checkObjectPrivilege(RpaScenario.class);
			if(roleIds != null){
				return em.createNamedQuery("RpaScenarioOperationResult.scenarioErrorsCountByObjectPrivilege", Object[].class)
						.setParameter("NORMAL_END", OperationResultStatus.NORMAL_END)
						.setParameter("ERROR_END", OperationResultStatus.ERROR_END)
						.setParameter("startDateFrom", startDateFrom)
						.setParameter("startDateTo", startDateTo)
						.setParameter("facilityIds", facilityIds)
						.setParameter("roleIds", roleIds)
						.getResultList();
			} else {
				return em.createNamedQuery("RpaScenarioOperationResult.scenarioErrorsCount", Object[].class)
						.setParameter("NORMAL_END", OperationResultStatus.NORMAL_END)
						.setParameter("ERROR_END", OperationResultStatus.ERROR_END)
						.setParameter("startDateFrom", startDateFrom)
						.setParameter("startDateTo", startDateTo)
						.setParameter("facilityIds", facilityIds)
						.getResultList();
			}
		}
	}
	
	/**
	 * RPAシナリオ実績情報のノード別エラー数をカウントして返します。
	 */
	public static List<Object[]> getRpaScenarioOperationResultsNodeErrorsCount(
			Long startDateFrom, Long startDateTo, List<String> facilityIds) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			List<String> roleIds = checkObjectPrivilege(RpaScenario.class);
			if(roleIds != null){
				return em.createNamedQuery("RpaScenarioOperationResult.nodeErrorsCountByObjectPrivilege", Object[].class)
						.setParameter("NORMAL_END", OperationResultStatus.NORMAL_END)
						.setParameter("ERROR_END", OperationResultStatus.ERROR_END)
						.setParameter("startDateFrom", startDateFrom)
						.setParameter("startDateTo", startDateTo)
						.setParameter("facilityIds", facilityIds)
						.setParameter("roleIds", roleIds)
						.getResultList();
			} else {
				return em.createNamedQuery("RpaScenarioOperationResult.nodeErrorsCount", Object[].class)
						.setParameter("NORMAL_END", OperationResultStatus.NORMAL_END)
						.setParameter("ERROR_END", OperationResultStatus.ERROR_END)
						.setParameter("startDateFrom", startDateFrom)
						.setParameter("startDateTo", startDateTo)
						.setParameter("facilityIds", facilityIds)
						.getResultList();
			}
		}
	}
	
	/**
	 * RPAシナリオ実績情報のシナリオ別削減工数をカウントして返します。
	 */
	public static List<Object[]> getRpaScenarioOperationResultScenarioReductionCount(
			Long startDateFrom, Long startDateTo, List<String> facilityIds) {
		return getRpaScenarioOperationResultReductionCount("RpaScenarioOperationResult.scenarioReductionCount", startDateFrom, startDateTo, facilityIds, 1, 2);
	}
	
	/**
	 * RPAシナリオ実績情報のノード別削減工数をカウントして返します。
	 */
	public static List<Object[]> getRpaScenarioOperationResultNodeReductionCount(
			Long startDateFrom, Long startDateTo, List<String> facilityIds) {
		return getRpaScenarioOperationResultReductionCount("RpaScenarioOperationResult.nodeReductionCount", startDateFrom, startDateTo, facilityIds, 1, 2);
	}

	/**
	 * RPAシナリオ実績情報のエラー数をカウントして返します。
	 */
	public static List<Object[]> getRpaScenarioOperationResultErrorsCount(
			Long startDateFrom, Long startDateTo, List<String> facilityIds) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			List<String> roleIds = checkObjectPrivilege(RpaScenario.class);
			if(roleIds != null){
				return em.createNamedQuery("RpaScenarioOperationResult.errorsCountByObjectPrivilege", Object[].class)
						.setParameter("NORMAL_END", OperationResultStatus.NORMAL_END)
						.setParameter("ERROR_END", OperationResultStatus.ERROR_END)
						.setParameter("startDateFrom", startDateFrom)
						.setParameter("startDateTo", startDateTo)
						.setParameter("facilityIds", facilityIds)
						.setParameter("roleIds", roleIds)
						.getResultList();
			} else {
				return em.createNamedQuery("RpaScenarioOperationResult.errorsCount", Object[].class)
						.setParameter("NORMAL_END", OperationResultStatus.NORMAL_END)
						.setParameter("ERROR_END", OperationResultStatus.ERROR_END)
						.setParameter("startDateFrom", startDateFrom)
						.setParameter("startDateTo", startDateTo)
						.setParameter("facilityIds", facilityIds)
						.getResultList();
			}
		}
	}
	
	/**
	 * RPAシナリオ実績情報の削減工数をカウントして返します。
	 */
	public static List<Object[]> getRpaScenarioOperationResultReductionCount(
			Long startDateFrom, Long startDateTo, List<String> facilityIds) {
		return getRpaScenarioOperationResultReductionCount("RpaScenarioOperationResult.reductionCount", startDateFrom, startDateTo, facilityIds, 0, 1);
	}
	
	/**
	 * 実行ノード訂正の対象となるシナリオ実績のレコード数を取得する。
	 */
	public static long countUpdateTargetResults(long fromTime, long toTime, String scenarioIdentifyString, String scenarioOperationResultCreateSettingId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			// ユーザ所属のロールを取得
			List<String> roleIds = checkObjectPrivilege(RpaScenario.class);
			
			TypedQuery<Long> query;
			if (roleIds != null) {
				query = em.createNamedQuery("RpaScenarioOperationResult.countUpdateTargetResultsByObjectPrivilege", long.class)
						.setParameter("roleIds", roleIds)
						.setParameter("RPA_SCENARIO", HinemosModuleConstant.RPA_SCENARIO)
						.setParameter("MODIFY", ObjectPrivilegeMode.MODIFY.name());
			} else {
				query = em.createNamedQuery("RpaScenarioOperationResult.countUpdateTargetResults", long.class); 				
			}
			query.setParameter("fromTime", fromTime)
			.setParameter("toTime", toTime)
			.setParameter("scenarioIdentifyString", scenarioIdentifyString)
			.setParameter("scenarioOperationResultCreateSettingId", scenarioOperationResultCreateSettingId)
			;
			
			return query.getSingleResult();
		} catch (NoResultException e) {
			return 0;
		}
	}

	/**
	 * 実行ノード訂正の対象となるシナリオ実績を取得する。
	 */
	public static List<RpaScenarioOperationResult> findUpdateTargetResults(long fromTime, long toTime, String scenarioIdentifyString, String scenarioOperationResultCreateSettingId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			// ユーザ所属のロールを取得
			List<String> roleIds = checkObjectPrivilege(RpaScenario.class);
			
			TypedQuery<RpaScenarioOperationResult> query;
			if (roleIds != null) {
				query = em.createNamedQuery("RpaScenarioOperationResult.findUpdateTargetResultsByObjectPrivilege", RpaScenarioOperationResult.class)
						.setParameter("roleIds", roleIds)
						.setParameter("RPA_SCENARIO", HinemosModuleConstant.RPA_SCENARIO)
						.setParameter("MODIFY", ObjectPrivilegeMode.MODIFY.name());
				
			} else {
				query = em.createNamedQuery("RpaScenarioOperationResult.findUpdateTargetResults", RpaScenarioOperationResult.class); 				
			}
			query.setParameter("fromTime", fromTime)
			.setParameter("toTime", toTime)
			.setParameter("scenarioIdentifyString", scenarioIdentifyString)
			.setParameter("scenarioOperationResultCreateSettingId", scenarioOperationResultCreateSettingId)
			;

			return query.getResultList();
		}
	}
	
	/**
	 * IDを指定してシナリオ実績更新情報を取得する。
	 */
	public static UpdateRpaScenarioOperationResultInfo getUpdateRpaScenarioOperationResultInfoPK(long updateId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			return em.find(UpdateRpaScenarioOperationResultInfo.class, updateId, ObjectPrivilegeMode.NONE);
		}
	}

	/**
	 * 実行予定のシナリオ実績更新情報一覧を取得する。
	 */
	public static List<UpdateRpaScenarioOperationResultInfo> getUpdateRpaScenarioOperationResultInfoList() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("UpdateRpaScenarioOperationResultInfo.findAll", UpdateRpaScenarioOperationResultInfo.class)
					.getResultList();
		}
	}

	/**
	 * 更新実行中(スケジュール中)のシナリオ実績のレコード数を取得する。
	 */
	public static long countTotalUpdatingOperationResults() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("UpdateRpaScenarioOperationResultInfo.countTotalUpdatingOperationResults", BigDecimal.class)
					.getSingleResult()
					.longValue();
		} catch (NoResultException e) {
			return 0;
		}
	}

	/**
	 * RPAログ解析用データ検索用IDの一時格納テーブルを作成する。
	 */
	public static int createTargetCollectorIdsTable() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			int ret = em.createNamedQuery("TargetCollectorIds.createTable").executeUpdate();
			return ret;
		}
	}

	/**
	 * RPAログ解析用データ検索用IDの一時格納テーブルにID一覧を格納する。
	 */
	public static int insertTargetCollectIds(List<Long> collectIds) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// INSERTではリストをパラメータとして渡せないため、値から直接SQL文を作成。
			String query = "INSERT INTO cc_target_collector_ids(collect_id) VALUES "
					+ collectIds.stream()
					.map(String::valueOf)
					.collect(Collectors.joining("'),('", "('", "')"));

			int ret = em.createNativeQuery(query)
					.executeUpdate();
			return ret;
		}
	}
	
	/**
	 * RPAログ解析用データ検索用IDの一時格納テーブルを削除する。
	 */
	public static int dropTargetCollectorIdsTable() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			int ret = em.createNamedQuery("TargetCollectorIds.dropTable").executeUpdate();
			return ret;
		}
	}

	// for debug
	public static void main(String[] args) {
		m_log.info("start main().");
	
		// 大量データのcollectIds(>32767:JDBCドライバのパラメータ数上限)リスト作成
		// @see com.clustercontrol.commons.util.QueryDivergence.getQueryWhereInParamThreashold()
		// 以下では、100001件のリストを作成
		List<Long> collectIds = LongStream.rangeClosed(0, 100000).boxed().collect(Collectors.toList());
		
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			jtm.begin();
			// 一時テーブルを作成
			m_log.info("create temporary table.");
			createTargetCollectorIdsTable();
			// 大量データをINSERT
			m_log.info(String.format("insert collectIds of %d.", collectIds.size()));
			insertTargetCollectIds(collectIds);
			// 一時テーブルを破棄
			m_log.info("inserted successfully. drop temporary table.");
			dropTargetCollectorIdsTable();
			jtm.commit();
		}
	}
	
	/**
	 * RPA管理ツールIDからRPA管理ツールのシナリオ入力パラメータ名を取得する。
	 */
	public static String getScenarioParamNameByRpaManagementToolId(String rpaManagementToolId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("RpaManagementToolRunParamMst.findScenarioParamNameByRpaManagementToolId", String.class)
					.setParameter("rpaManagementToolId", rpaManagementToolId)
					.getSingleResult();
		}
	}
	
	/**
	 * 自動化効果計算マスタ一覧を取得する
	 */
	public static List<RpaScenarioCoefficientPattern> getRpaScenarioCoefficientPatternList() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()){
			HinemosEntityManager em = jtm.getEntityManager();
			List<RpaScenarioCoefficientPattern> entities = em.createNamedQuery("RpaScenarioCoefficientPattern.findAll", RpaScenarioCoefficientPattern.class).getResultList();
			return entities;
		}
	}
	
	/**
	 * 指定したPKの自動化効果計算マスタを取得する
	 */
	public static RpaScenarioCoefficientPattern getRpaScenarioCoefficientPattern(String rpaToolEnvId, int orderNo){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("RpaScenarioCoefficientPattern.findByPK", RpaScenarioCoefficientPattern.class)
					.setParameter("rpaToolEnvId", rpaToolEnvId)
					.setParameter("orderNo", orderNo)
					.getSingleResult();
		}
	}
}