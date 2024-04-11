/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.session;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.ObjectPrivilegeFilterInfo;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.UserIdConstant;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfoPK;
import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.accesscontrol.util.RoleValidator;
import com.clustercontrol.bean.FunctionPrefixEnum;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.collect.util.ZipCompressor;
import com.clustercontrol.commons.session.CheckFacility;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.fault.PrivilegeDuplicate;
import com.clustercontrol.fault.RpaManagementToolAccountDuplicate;
import com.clustercontrol.fault.RpaManagementToolAccountNotFound;
import com.clustercontrol.fault.RpaScenarioCoefficientPatternDuplicate;
import com.clustercontrol.fault.RpaScenarioDuplicate;
import com.clustercontrol.fault.RpaScenarioNotFound;
import com.clustercontrol.fault.RpaScenarioOperationResultCreateSettingDuplicate;
import com.clustercontrol.fault.RpaScenarioOperationResultCreateSettingNotFound;
import com.clustercontrol.fault.RpaScenarioOperationResultNotFound;
import com.clustercontrol.fault.RpaScenarioTagDuplicate;
import com.clustercontrol.fault.RpaScenarioTagNotFound;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.fault.UsedObjectPrivilege;
import com.clustercontrol.notify.factory.ModifyNotifyRelation;
import com.clustercontrol.notify.factory.SelectNotify;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.notify.util.NotifyCallback;
import com.clustercontrol.notify.util.NotifyRelationCacheRefreshCallback;
import com.clustercontrol.platform.HinemosPropertyDefault;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.FacilityRelationEntity;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.rest.endpoint.rpa.dto.CorrectExecNodeDetailRequest;
import com.clustercontrol.rest.endpoint.rpa.dto.DownloadRpaScenarioOperationResultRecordsRequest;
import com.clustercontrol.rest.endpoint.rpa.dto.GetRpaScenarioCorrectExecNodeResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.GetRpaScenarioExecNodeDataResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.GetRpaScenarioListResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.GetRpaScenarioOperationResultSummaryDataResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.GetRpaScenarioOperationResultSummaryForBarResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.GetRpaScenarioOperationResultSummaryForPieResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.GetRpaScenarioOperationResultSummaryStructureResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.GetRpaScenarioResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.RpaScenarioExecNodeResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.RpaScenarioOperationResultWithDetailResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.RpaScenarioResponseP1;
import com.clustercontrol.rest.endpoint.rpa.dto.RpaScenarioTagResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.SearchRpaScenarioOperationResultDataResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.SearchRpaScenarioOperationResultResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.SummaryTypeEnum;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestDownloadFile;
import com.clustercontrol.rpa.bean.SummaryTypeConstant;
import com.clustercontrol.rpa.factory.ModifyRpaAccount;
import com.clustercontrol.rpa.factory.SelectRpaAccount;
import com.clustercontrol.rpa.model.RpaManagementToolAccount;
import com.clustercontrol.rpa.model.RpaManagementToolEndStatusMst;
import com.clustercontrol.rpa.model.RpaManagementToolMst;
import com.clustercontrol.rpa.model.RpaManagementToolRunParamMst;
import com.clustercontrol.rpa.model.RpaManagementToolRunTypeMst;
import com.clustercontrol.rpa.model.RpaManagementToolStopModeMst;
import com.clustercontrol.rpa.model.RpaScenarioCoefficientPattern;
import com.clustercontrol.rpa.model.RpaToolEnvMst;
import com.clustercontrol.rpa.model.RpaToolMst;
import com.clustercontrol.rpa.model.RpaToolRunCommandMst;
import com.clustercontrol.rpa.scenario.bean.RpaScenarioFilterInfo;
import com.clustercontrol.rpa.scenario.bean.RpaScenarioOperationResultFilterInfo;
import com.clustercontrol.rpa.scenario.factory.DownloadRpaScenarioOperationResult;
import com.clustercontrol.rpa.scenario.factory.ModifyRpaScenario;
import com.clustercontrol.rpa.scenario.factory.ModifyRpaScenarioCoefficientPattern;
import com.clustercontrol.rpa.scenario.factory.ModifyRpaScenarioTag;
import com.clustercontrol.rpa.scenario.factory.ModifySchedule;
import com.clustercontrol.rpa.scenario.factory.SelectRpaScenario;
import com.clustercontrol.rpa.scenario.factory.SelectRpaScenarioCoefficientPattern;
import com.clustercontrol.rpa.scenario.factory.SelectRpaScenarioExecNode;
import com.clustercontrol.rpa.scenario.factory.SelectRpaScenarioOperationResult;
import com.clustercontrol.rpa.scenario.factory.SelectRpaScenarioTag;
import com.clustercontrol.rpa.scenario.model.RpaScenario;
import com.clustercontrol.rpa.scenario.model.RpaScenarioExecNode;
import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResult;
import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResult.OperationResultStatus;
import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResultCreateSetting;
import com.clustercontrol.rpa.scenario.model.RpaScenarioTag;
import com.clustercontrol.rpa.scenario.model.RpaScenarioTagRelation;
import com.clustercontrol.rpa.scenario.model.UpdateRpaScenarioOperationResultInfo;
import com.clustercontrol.rpa.scenario.session.ScenarioOperationResultUpdater;
import com.clustercontrol.rpa.util.QueryUtil;
import com.clustercontrol.rpa.util.RpaResourceDetectCallback;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * RPA機能の管理を行う Session Bean
 */
public class RpaControllerBean implements CheckFacility{

	private static Log m_log = LogFactory.getLog( RpaControllerBean.class );
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";
	
		private ILock getAddScenarioLock (String rpaScenarioOperationResultCreateSettingId) {
		// シナリオIDはシナリオ実績作成設定に基づいて自動採番するため、シナリオ実績作成設定
		// 登録する際はID単位でロックを取得する
		ILockManager lm = LockManagerFactory.instance().create();
		return lm.create(getLockKey(rpaScenarioOperationResultCreateSettingId));
	}

	private String getLockKey(String id) {
		return String.format("%s [%s]", RpaControllerBean.class.getName(), id);
	}
	
	/**
	 * シナリオ設定にシナリオ実績作成設定情報を反映する。
	 * シナリオID:「シナリオ実績作成設定ID_連番」
	 * オーナーロールID:シナリオ実績作成設定と同一
	 */
	private void inheritanceScenarioCreateSetting(RpaScenario scenario) throws RpaScenarioOperationResultCreateSettingNotFound, InvalidRole, HinemosUnknown {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			// シナリオ実績作成設定を取得
			RpaScenarioOperationResultCreateSetting createSetting = getRpaScenarioOperationResultCreateSetting(scenario.getScenarioOperationResultCreateSettingId());
			
			// シナリオIDを生成
			String id = createSetting.getScenarioOperationResultCreateSettingId();
			long num = createSetting.getScenarioNumber();
			createSetting.setScenarioNumber(num + 1);
			scenario.setScenarioId(String.format("%s_%04d", id, num));
			
			// オーナーロールIDを継承
			scenario.setOwnerRoleId(createSetting.getOwnerRoleId());
		}
	}
	
	/**
	 * シナリオ設定にシナリオ実績作成設定のオブジェクト権限を継承する。
	 * @throws HinemosUnknown 
	 */
	private static void inheritanceOnjectPrivilege(String scenarioId, String createSettingId) throws HinemosUnknown, InvalidRole, InvalidSetting {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			// 作成設定のオブジェクト権限を取得
			ObjectPrivilegeFilterInfo filter = new ObjectPrivilegeFilterInfo();
			filter.setObjectType(HinemosModuleConstant.RPA_SCENARIO_CREATE);
			filter.setObjectId(createSettingId);
			AccessControllerBean bean = new AccessControllerBean();
			List<ObjectPrivilegeInfo> createSettingPrivilegeList = bean.getObjectPrivilegeInfoList(filter);
			
			// シナリオのオブジェクト権限を作成
			List<ObjectPrivilegeInfo> scenarioPrivilegeList = new ArrayList<>();
			for (ObjectPrivilegeInfo createSettingPrivilege : createSettingPrivilegeList) {
				scenarioPrivilegeList.add(
						new ObjectPrivilegeInfo(
								new ObjectPrivilegeInfoPK(
										HinemosModuleConstant.RPA_SCENARIO,
										scenarioId, 
										createSettingPrivilege.getRoleId(),
										createSettingPrivilege.getObjectPrivilege())));
			}
			
			if (!scenarioPrivilegeList.isEmpty()) {
				// 登録ユーザが空(自動登録時)、またはシナリオのオーナーロールに所属していない場合があるため、
				// オブジェクト権限の登録は管理者ユーザで行う。
				// (オブジェクト権限の登録・変更ユーザはユーザからは見えない)
				HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, UserIdConstant.HINEMOS);
				HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, true);
				
				// オブジェクト権限登録
				new AccessControllerBean().replaceObjectPrivilegeInfo(HinemosModuleConstant.RPA_SCENARIO, scenarioId, scenarioPrivilegeList);
			}
		} catch (PrivilegeDuplicate | UsedObjectPrivilege | JobMasterNotFound e) {
			// 想定外エラー
			m_log.warn("inheritanceOnjectPrivilege(): failed to add scenario:" + e.getMessage(), e);
			throw new HinemosUnknown(e);
		}
	}


	/**
	 * 指定したRPAスコープIDのRPA管理ツールアカウントを取得します。
	 * 
	 * @param id
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public RpaManagementToolAccount getRpaAccount(String id) throws RpaManagementToolAccountNotFound, HinemosUnknown {
		JpaTransactionManager jtm = null;
		RpaManagementToolAccount rpaAccount = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			rpaAccount = new SelectRpaAccount().getRpaAccount(id);
			jtm.commit();
		} catch (RpaManagementToolAccountNotFound e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("getRpaAccount() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return rpaAccount;
	}

	/**
	 * RPA管理ツールアカウント一覧を取得します。
	 * 
	 * @param id
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<RpaManagementToolAccount> getRpaAccountList() throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			List<RpaManagementToolAccount> rpaAccountList = new SelectRpaAccount().getRpaAccountList();
			jtm.commit();
			return rpaAccountList;
		} catch (Exception e) {
			m_log.warn("getRpaAccount() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}
	
	/**
	 * RPA管理ツールアカウントをマネージャに登録します。<BR>
	 * 
	 * @param rpaAccount
	 * @return rpaAccount
	 * @throws HinemosUnknown
	 * @throws RpaManagementToolAccountDuplicate
	 * @throws InvalidSetting
	 * 
	 */
	public RpaManagementToolAccount addRpaAccount(RpaManagementToolAccount rpaAccount) throws HinemosUnknown, RpaManagementToolAccountDuplicate, InvalidSetting {
		JpaTransactionManager jtm = null;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
			//ユーザがオーナーロールIDに所属しているかチェック
			RoleValidator.validateUserBelongRole(rpaAccount.getOwnerRoleId(), userId,
					(Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR));

			ModifyRpaAccount modifier = new ModifyRpaAccount();
			modifier.add(rpaAccount, userId);

			// アカウント追加後、コールバックメソッドでリソースの検知を実施
			jtm.addCallback(new RpaResourceDetectCallback(rpaAccount.getRpaScopeId()));
			
			jtm.commit();
			return new SelectRpaAccount().getRpaAccount(rpaAccount.getRpaScopeId());
		} catch (RpaManagementToolAccountDuplicate | InvalidSetting e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("addRpaAccount() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}


	/**
	 * RPA管理ツールアカウント情報を変更します。
	 * 
	 * @param rpaAccount
	 * @return rpaAccount
	 * @throws HinemosUnknown
	 * @throws RpaManagementToolAccountNotFound
	 * @throws InvalidRole 
	 * 
	 */
	public RpaManagementToolAccount modifyRpaAccount(RpaManagementToolAccount rpaAccount) throws HinemosUnknown, RpaManagementToolAccountNotFound, InvalidRole {
		JpaTransactionManager jtm = null;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

			ModifyRpaAccount modifier = new ModifyRpaAccount();
			modifier.modify(rpaAccount, userId);
			// アカウント追加後、コールバックメソッドでリソースの検知を実施
			jtm.addCallback(new RpaResourceDetectCallback(rpaAccount.getRpaScopeId()));

			jtm.commit();
			return new SelectRpaAccount().getRpaAccount(rpaAccount.getRpaScopeId());
		} catch (RpaManagementToolAccountNotFound e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (InvalidRole e){
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("modifyRpaAccount() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * RPA管理ツールアカウントをマネージャから削除します。
	 * 
	 * @param scenarioId 削除対象のRPAシナリオID
	 * @return List<RpaScenario>
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws UsedFacility 
	 * 
	 */
	public List<RpaManagementToolAccount> deleteRpaAccount(List<String> rpaScopeIdList) throws RpaManagementToolAccountNotFound, HinemosUnknown, InvalidRole, UsedFacility {
		m_log.debug("deleteRpaScenario");

		JpaTransactionManager jtm = null;
		List<RpaManagementToolAccount> retList =  new ArrayList<>();
		String id = "";

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			// RPA管理ツールアカウントを削除
			ModifyRpaAccount modifier = new ModifyRpaAccount();
			for(String rpaScopeId : rpaScopeIdList) {
				id = rpaScopeId;
				RpaManagementToolAccount ret = new SelectRpaAccount().getRpaAccount(rpaScopeId);
				// アカウント及び自動検知リソース削除
				RpaResourceMonitor.internalResourceRemove(ret);
				modifier.delete(rpaScopeId);
			}

			jtm.commit();
		} catch (RpaManagementToolAccountNotFound e){
			if (jtm != null){
				jtm.rollback();
			}
			throw new RpaManagementToolAccountNotFound(
					MessageConstant.MESSAGE_FAILED_TO_DELETE.getMessage(MessageConstant.RPA_MANAGEMENT_TOOL_ACCOUNT.getMessage()) 
					+ MessageConstant.MESSAGE_NOT_FOUND.getMessage(
							MessageConstant.RPA_SCOPE_ID.getMessage(),
							id));
		} catch (InvalidRole e){
			if (jtm != null){
				jtm.rollback();
			}
			throw new InvalidRole(
					MessageConstant.MESSAGE_DO_NOT_HAVE_ENOUGH_PERMISSION.getMessage()
					);
		} catch (UsedFacility e){
			if (jtm != null){
				jtm.rollback();
			}
			throw new UsedFacility(
					MessageConstant.MESSAGE_FAILED_TO_DELETE.getMessage(MessageConstant.RPA_MANAGEMENT_TOOL_ACCOUNT.getMessage()) 
					+ MessageConstant.MESSAGE_PLEASE_CHECK_TO_BE_USED.getMessage(MessageConstant.RPA_SCOPE.getMessage(),e.getMessage())
					);
		} catch (Exception e) {
			m_log.warn("deleteRpaScenario() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return retList;
	}
	
	/**
	 * RPA管理ツールマスタ一覧を取得する。
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<RpaManagementToolMst> getRpaManagementToolMstList() throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			List<RpaManagementToolMst> list = QueryUtil.getRpaManagementToolMstList();
			jtm.commit();
			return list;
		} catch (Exception e){
			m_log.warn("getRpaManagementToolMstList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}
	
	/**
	 * RPA管理ツール実行種別マスタ一覧を取得する。
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<RpaManagementToolRunTypeMst> getRpaManagementToolRunTypeMstList(String rpaManagementToolId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			List<RpaManagementToolRunTypeMst> list = QueryUtil.getRpaManagementToolRunTypeMstList(rpaManagementToolId);
			jtm.commit();
			return list;
		} catch (Exception e){
			m_log.warn("getRpaManagementToolRunTypeMstList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}
	
	/**
	 * RPA管理ツール停止方法マスタ一覧を取得する。
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<RpaManagementToolStopModeMst> getRpaManagementToolStopModeMstList(String rpaManagementToolId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			List<RpaManagementToolStopModeMst> list = QueryUtil.getRpaManagementToolStopModeMstList(rpaManagementToolId);
			jtm.commit();
			return list;
		} catch (Exception e){
			m_log.warn("getRpaManagementToolStopModeMstList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}
	
	/**
	 * 指定したRPA管理ツールIDのRPA管理ツール実行パラメータマスタ一覧を取得する。
	 * @param rpaManagementToolId
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<RpaManagementToolRunParamMst> getRpaManagementToolRunParamMstList(String rpaManagementToolId, Integer runType) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			List<RpaManagementToolRunParamMst> list = QueryUtil.getRpaManagementToolRunParamMstList(rpaManagementToolId, runType);
			jtm.commit();
			return list;
		} catch (Exception e){
			m_log.warn("getRpaManagementToolRunParamMstList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}
	
	/**
	 * 指定したRPA管理ツールIDのRPA管理ツール終了状態マスタ一覧を取得する。
	 * @param rpaManagementToolId
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<RpaManagementToolEndStatusMst> getRpaManagementToolEndStatusMstList(String rpaManagementToolId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			List<RpaManagementToolEndStatusMst> list = QueryUtil.getRpaManagementToolEndStatusMstList(rpaManagementToolId);
			jtm.commit();
			return list;
		} catch (Exception e){
			m_log.warn("getRpaManagementToolEndStatusMstList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	/**
	 * RPAシナリオ情報の登録
	 */
	public RpaScenario addRpaScenario(RpaScenario data) throws HinemosUnknown, RpaScenarioDuplicate, InvalidSetting, InvalidRole {
		RpaScenario ret = null;
		JpaTransactionManager jtm = null;
		SelectRpaScenario selectRpaScenario = new SelectRpaScenario();

		// シナリオ作成設定ID単位でロックを取得
		ILock lock = getAddScenarioLock(data.getScenarioOperationResultCreateSettingId());
		lock.writeLock();

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 共通のシナリオを登録する場合、共通のシナリオが既に存在するかチェック
			if (data.getCommonNodeScenario() && selectRpaScenario.checkCommonNodeScenario(data.getScenarioId(),
					data.getScenarioOperationResultCreateSettingId(), data.getScenarioIdentifyString())){
				throw new InvalidSetting(MessageConstant.MESSAGE_RPA_SCENARIO_DUPLICATION_COMMON.getMessage());
			}
			
			// シナリオ作成設定から、シナリオID、オーナーロールIDをセット
			inheritanceScenarioCreateSetting(data);

			ModifyRpaScenario rpaScenario = new ModifyRpaScenario();
			rpaScenario.add(data, (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));
			
			// シナリオ設定にシナリオ作成設定のオブジェクト権限を継承
			inheritanceOnjectPrivilege(data.getScenarioId(), data.getScenarioOperationResultCreateSettingId());

			jtm.commit();
			ret = selectRpaScenario.getRpaScenario(data.getScenarioId());
		} catch (RpaScenarioDuplicate | InvalidSetting e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw e;
		} catch (RpaScenarioOperationResultCreateSettingNotFound e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidSetting(
					MessageConstant.MESSAGE_NOT_FOUND.getMessage(
							MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CREATE_SETTING_ID.getMessage(), 
							data.getScenarioOperationResultCreateSettingId()));		
		} catch (Exception e) {
			m_log.warn("addRpaScenario() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
			lock.writeUnlock();
		}

		m_log.info("add RPA Scenario. scenarioId=" + ret.getScenarioId());
		return ret;
	}

	/**
	 * RPAシナリオ情報の変更
	 */
	public RpaScenario modifyRpaScenario(RpaScenario data) throws HinemosUnknown, RpaScenarioNotFound,InvalidSetting, InvalidRole {
		RpaScenario ret = null;
		JpaTransactionManager jtm = null;
		SelectRpaScenario selectRpaScenario = new SelectRpaScenario();

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			// 共通のシナリオを登録する場合、共通のシナリオが既に存在するかチェック
			RpaScenario rpaScenario = selectRpaScenario.getRpaScenario(data.getScenarioId());
			if (data.getCommonNodeScenario() && selectRpaScenario.checkCommonNodeScenario(rpaScenario.getScenarioId(),
					rpaScenario.getScenarioOperationResultCreateSettingId(), rpaScenario.getScenarioIdentifyString())){
				throw new InvalidSetting(MessageConstant.MESSAGE_RPA_SCENARIO_DUPLICATION_COMMON.getMessage());
			}
			
			ModifyRpaScenario modifyRpaScenario = new ModifyRpaScenario();
			modifyRpaScenario.modify(data, (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

			jtm.commit();
			ret = selectRpaScenario.getRpaScenario(data.getScenarioId());
		} catch (RpaScenarioNotFound | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("modifyRpaScenario() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		m_log.info("modify RPA Scenario. scenarioId=" + ret.getScenarioId());
		return ret;
	}

	/**
	 * RPAシナリオ情報の削除
	 */
	public List<RpaScenario> deleteRpaScenario(List<String> scenarioIdList) throws InvalidRole, HinemosUnknown {
		m_log.debug("deleteRpaScenario");

		JpaTransactionManager jtm = null;
		List<RpaScenario> retList =  new ArrayList<>();

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			// RPAシナリオ情報を削除
			ModifyRpaScenario rpaScenario = new ModifyRpaScenario();
			for(String scenarioId : scenarioIdList) {
				retList.add(new SelectRpaScenario().getRpaScenario(scenarioId));
				rpaScenario.delete(scenarioId);
			}

			jtm.commit();
		} catch (InvalidRole | HinemosUnknown e){
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("deleteRpaScenario() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		m_log.info("delete RPA Scenario. scenarioId=" + String.join(", ", scenarioIdList.toArray(new String[scenarioIdList.size()])));
		return retList;
	}
	
	/**
	 * RPAシナリオ一覧を取得(権限チェックなし)
	 */
	public List<RpaScenario> getRpaScenarioList() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			jtm.begin();
			List<RpaScenario> ret =  new SelectRpaScenario().getRpaScenarioList();
			jtm.commit();
			return ret;
		}
	}

	/**
	 * RPAシナリオ一覧の取得
	 */
	public ArrayList<GetRpaScenarioListResponse> getRpaScenarioList(RpaScenarioFilterInfo condition) throws InvalidRole, HinemosUnknown {
		m_log.debug("getRpaScenarioList(RpaScenarioFilterInfo) : start");

		JpaTransactionManager jtm = null;

		ArrayList<RpaScenario> scenarioList = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			scenarioList = new SelectRpaScenario().getRpaScenarioList(condition);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getRpaScenarioList(condition) : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		ArrayList<GetRpaScenarioListResponse> resList = new ArrayList<>();
		
		for (RpaScenario info : scenarioList){
			GetRpaScenarioListResponse dto = new GetRpaScenarioListResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			resList.add(dto);
		}
		
		m_log.debug("getRpaScenarioList(condition) : end");
		return resList;
	}
	
	/**
	 * シナリオIDからRPAシナリオ情報の取得
	 */
	public GetRpaScenarioResponse getRpaScenario(String scenarioId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		RpaScenario scenarioInfo = null;
		GetRpaScenarioResponse dtoRes = new GetRpaScenarioResponse();
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			// RPAシナリオを取得
			SelectRpaScenario select = new SelectRpaScenario();
			scenarioInfo = select.getRpaScenario(scenarioId);
			
			RestBeanUtil.convertBeanNoInvalid(scenarioInfo, dtoRes);
			
			// RPAシナリオタグを取得
			SelectRpaScenarioTag selectTag = new SelectRpaScenarioTag();
			for (String tagId : scenarioInfo.getTagRelationList()){
				RpaScenarioTag tagInfo = selectTag.getRpaScenarioTag(tagId);
				RpaScenarioTagResponse tagRes = new RpaScenarioTagResponse();
				RestBeanUtil.convertBeanNoInvalid(tagInfo, tagRes);
				dtoRes.addTagList(tagRes);
			}
			
			// 実行ノードは自動変換されないので別個に変換
			setExecNodeDto(dtoRes, scenarioInfo.getExecNodes());
			
			// RPAシナリオノードからノード名を取得
			for (GetRpaScenarioExecNodeDataResponse node : dtoRes.getExecNodeList()){
				String nodeName = new RepositoryControllerBean().getFacilityPath(node.getExecNode(), null);
				node.setExecNodeName(nodeName);
			}
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getRpaScenario() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return dtoRes;
	}

	/**
	 * シナリオ実行ノードのEntityをレスポンスDTOにセットする
	 * @param scenarioDto
	 * @param execNodesInfo
	 */
	private void setExecNodeDto(GetRpaScenarioResponse scenarioDto, List<RpaScenarioExecNode> execNodesInfo) {
		scenarioDto.setExecNodeList(new ArrayList<>());
		for (RpaScenarioExecNode execNodeInfo : execNodesInfo) {
			GetRpaScenarioExecNodeDataResponse data = new GetRpaScenarioExecNodeDataResponse();
			data.setExecNode(execNodeInfo.getId().getFacilityId());
			scenarioDto.addExecNodeList(data);
		}
	}
	
	/**
	 * RPAツールマスタ一覧を取得する。
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<RpaToolMst> getRpaToolMstList() throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			List<RpaToolMst> list = QueryUtil.getRpaToolMstList();
			jtm.commit();
			return list;
		} catch (Exception e){
			m_log.warn("getRpaToolMstList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	/**
	 * RPAツールマスタ一覧を取得する。
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<RpaToolEnvMst> getRpaToolEnvMstList() throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			List<RpaToolEnvMst> list = QueryUtil.getRpaToolEnvMstList();
			jtm.commit();
			return list;
		} catch (Exception e){
			m_log.warn("getRpaToolEnvMstList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}
	
	/**
	 * RPAツールシナリオ実行コマンドマスタ一覧を取得する。
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<RpaToolRunCommandMst> getRpaToolRunCommandMstList() throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			List<RpaToolRunCommandMst> list = QueryUtil.getRpaToolRunCommandMstList();
			jtm.commit();
			return list;
		} catch (Exception e){
			m_log.warn("getRpaToolRunCommandMstList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	/**
	 * RPAシナリオタグ情報の登録
	 */
	public RpaScenarioTag addRpaScenarioTag(RpaScenarioTag data) throws HinemosUnknown, RpaScenarioTagDuplicate, InvalidSetting, InvalidRole {
		RpaScenarioTag ret = null;
		JpaTransactionManager jtm = null;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			//ユーザがオーナーロールIDに所属しているかチェック
			RoleValidator.validateUserBelongRole(data.getOwnerRoleId(),
					(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),
					(Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR));

			ModifyRpaScenarioTag rpaScenarioTag = new ModifyRpaScenarioTag();
			rpaScenarioTag.add(data, (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

			jtm.commit();
			ret = new SelectRpaScenarioTag().getRpaScenarioTag(data.getTagId());
		} catch (RpaScenarioTagDuplicate | InvalidSetting e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("addRpaScenarioTag() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		m_log.info("add RPA Scenario Tag. tagId=" + ret.getTagId());
		return ret;
	}

	/**
	 * RPAシナリオタグ情報の変更
	 */
	public RpaScenarioTag modifyRpaScenarioTag(RpaScenarioTag data) throws HinemosUnknown, RpaScenarioTagNotFound,InvalidSetting, InvalidRole {
		RpaScenarioTag ret = null;
		JpaTransactionManager jtm = null;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			ModifyRpaScenarioTag rpaScenarioTag = new ModifyRpaScenarioTag();
			rpaScenarioTag.modify(data, (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

			jtm.commit();
			ret = new SelectRpaScenarioTag().getRpaScenarioTag(data.getTagId());
		} catch (RpaScenarioTagNotFound | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("modifyRpaScenarioTag() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		m_log.info("modify RPA Scenario Tag. tagId=" + ret.getTagId());
		return ret;
	}

	/**
	 * RPAシナリオタグ情報の削除
	 */
	public List<RpaScenarioTag> deleteRpaScenarioTag(List<String> tagIdList) throws InvalidRole, HinemosUnknown {
		m_log.debug("deleteRpaScenarioTag");

		JpaTransactionManager jtm = null;
		List<RpaScenarioTag> retList =  new ArrayList<>();

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			// タグがシナリオにセットされている場合はすべてエラーとする
			List<String> checkResult = new SelectRpaScenarioTag().checkRpaScenarioTagRelation(tagIdList);
			if(!checkResult.isEmpty()){
				throw new InvalidSetting(MessageConstant.MESSAGE_RPA_SCENARIO_TAG_USED.getMessage(String.join(",", checkResult)));
			}
			
			// RPAシナリオタグ情報を削除
			ModifyRpaScenarioTag rpaScenarioTag = new ModifyRpaScenarioTag();
			for(String tagId : tagIdList) {
				retList.add(new SelectRpaScenarioTag().getRpaScenarioTag(tagId));
				rpaScenarioTag.delete(tagId);
			}

			jtm.commit();
		} catch (InvalidRole | HinemosUnknown e){
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("deleteRpaScenarioTag() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		m_log.info("delete RPA Scenario Tag. tagId=" + String.join(", ", tagIdList.toArray(new String[tagIdList.size()])));
		return retList;
	}
	
	/**
	 * RPAシナリオタグ一覧の取得
	 */
	public ArrayList<RpaScenarioTag> getRpaScenarioTagListByOwnerRole(String ownerRoleId) throws InvalidRole, HinemosUnknown {
		m_log.debug("getRpaScenarioTagByOwnerRole(ownerRoleId)");

		JpaTransactionManager jtm = null;
		SelectRpaScenarioTag select = new SelectRpaScenarioTag();
		ArrayList<RpaScenarioTag> list;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = select.getRpaScenarioTagList(ownerRoleId);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getRpaScenarioTagListByOwnerRole() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}

		return list;

	}

	/**
	 * タグIDからRPAシナリオタグ情報の取得
	 */
	public RpaScenarioTag getRpaScenarioTag(String tagId) throws InvalidRole, HinemosUnknown, RpaScenarioTagNotFound {
		JpaTransactionManager jtm = null;
		RpaScenarioTag info = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			// RPAシナリオタグを取得
			SelectRpaScenarioTag select = new SelectRpaScenarioTag();
			info = select.getRpaScenarioTag(tagId);
			
			jtm.commit();
		} catch (RpaScenarioTagNotFound e) {
			if (jtm != null)
				jtm.rollback();
			throw new RpaScenarioTagNotFound(
					MessageConstant.MESSAGE_NOT_FOUND.getMessage(
							MessageConstant.RPA_SCENARIO_TAG_ID.getMessage(), 
							tagId));
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getRpaScenarioTag() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return info;
	}
	
	/**
	 * 指定されたタグIDを親に含む子タグを抽出する。
	 */
	public List<RpaScenarioTag> getChildrenScenarioTagList(String parentTagId) throws InvalidRole, HinemosUnknown {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			List <RpaScenarioTag> scenarioTagList = getRpaScenarioTagListByOwnerRole(null);
			// タグのパスが「\親タグID\」を含む、または「\親タグ」で終わるタグを抽出する。
			return scenarioTagList.stream().filter(tag -> tag.getTagPath().matches("(.*\\\\" + parentTagId + "\\\\.*|.*\\\\" + parentTagId + "$)")).collect(Collectors.toList());
		}
	}
	
	/**
	 * RPAシナリオ実績一覧の取得
	 */
	public SearchRpaScenarioOperationResultResponse getRpaScenarioOperationResultList(RpaScenarioOperationResultFilterInfo condition) 
			throws InvalidRole, HinemosUnknown, InvalidSetting {
		long start = System.currentTimeMillis();
		m_log.debug("getRpaScenarioOperationResultList(RpaScenarioOperationResultFilterInfo) : start");

		JpaTransactionManager jtm = null;

		SearchRpaScenarioOperationResultResponse dtoRes = new SearchRpaScenarioOperationResultResponse();
		ArrayList<RpaScenarioOperationResult> list = null;
		Map<String,String> scenarioNameMap = null;
		Map<String,String> facilityNameMap = null;
		Long count = (long) 0;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			List<String> facilityIds = new SelectRpaScenarioOperationResult().checkFacilityId(condition.getFacilityId());
			count = new SelectRpaScenarioOperationResult().getRpaScenarioOperationResultCount(condition, facilityIds);
			list = new SelectRpaScenarioOperationResult().getRpaScenarioOperationResultList(condition, facilityIds);
			scenarioNameMap = new SelectRpaScenario().getRpaScenarioNameMap();
			facilityNameMap = getFacilityNameMap();
			jtm.commit();
		} catch (HinemosUnknown | InvalidRole | InvalidSetting e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getRpaScenarioOperatinResultList(condition) : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		
		List<SearchRpaScenarioOperationResultDataResponse> dtoList = new ArrayList<>();
		for (RpaScenarioOperationResult info : list){
			SearchRpaScenarioOperationResultDataResponse dto = new SearchRpaScenarioOperationResultDataResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dto.setScenarioName(scenarioNameMap.get(dto.getScenarioId()));
			dto.setFacilityName(facilityNameMap.get(dto.getFacilityId()));
			dtoList.add(dto);
		}
		
		dtoRes.setResultList(dtoList);
		
		dtoRes.setCount(Integer.valueOf(count.toString()));
		dtoRes.setOffset(condition.getOffset());
		dtoRes.setSize(dtoList.size());
		dtoRes.setTime(System.currentTimeMillis() - start);

		m_log.debug("getRpaScenarioOperatinResultList(condition) : end");
		return dtoRes;
	}
	
	/**
	 * 実績IDからRPAシナリオ実績詳細情報の取得
	 */
	public RpaScenarioOperationResultWithDetailResponse getRpaScenarioOperationResult(Long resultId) throws 
			RpaScenarioOperationResultNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		RpaScenarioOperationResult info = null;
		Map<String, String> scenarioNameMap = null;
		Map<String, String> facilityNameMap = null;
		Map<String, RpaToolMst> rpaToolMap = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			// RPAシナリオ実績一覧を取得
			SelectRpaScenarioOperationResult select = new SelectRpaScenarioOperationResult();
			info = select.getRpaScenarioOperationResult(resultId);
			scenarioNameMap = new SelectRpaScenario().getRpaScenarioNameMap();
			facilityNameMap = getFacilityNameMap();
			rpaToolMap = new SelectRpaScenario().getRpaToolNameMap();
			RpaScenarioOperationResultWithDetailResponse dto = new RpaScenarioOperationResultWithDetailResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dto.setScenarioName(scenarioNameMap.get(dto.getScenarioId()));
			dto.setFacilityName(facilityNameMap.get(dto.getFacilityId()));
			dto.setRpaToolId(rpaToolMap.get(dto.getScenarioId()).getRpaToolId());
			
			jtm.commit();
			return dto;
		} catch (RpaScenarioOperationResultNotFound | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getRpaScenarioOperationResultDetail() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		
	}	
	
	/**
	 * シナリオIDから同一実績作成設定、同一識別子のRPAシナリオ情報の取得
	 */
	public GetRpaScenarioCorrectExecNodeResponse getRpaScenarioModifyExecNode(String scenarioId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		RpaScenario scenarioInfo = null;
		List<RpaScenarioExecNode> execNodeList = null;
		ArrayList<RpaScenario> scenarioList = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			// RPAシナリオを取得
			SelectRpaScenario select = new SelectRpaScenario();
			scenarioInfo = select.getRpaScenario(scenarioId);
			
			SelectRpaScenarioExecNode selectExecNode = new SelectRpaScenarioExecNode();
			execNodeList = selectExecNode.getRpaScenarioExecNode(scenarioInfo.getScenarioIdentifyString(), scenarioInfo.getScenarioOperationResultCreateSettingId());
			
			RpaScenarioFilterInfo condition = new RpaScenarioFilterInfo();
			condition.setScenarioIdentifyString(scenarioInfo.getScenarioIdentifyString());
			condition.setScenarioOperationResultCreateSettingId(scenarioInfo.getScenarioOperationResultCreateSettingId());
			scenarioList = new SelectRpaScenario().getRpaScenarioList(condition);
			
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getRpaScenarioModifyExecNode() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		GetRpaScenarioCorrectExecNodeResponse dtoRes = new GetRpaScenarioCorrectExecNodeResponse();
		RestBeanUtil.convertBeanNoInvalid(scenarioInfo, dtoRes);
		
		List<RpaScenarioExecNodeResponse> execNodeResponseList = new ArrayList<>();
		for (RpaScenarioExecNode execNode : execNodeList){
			RpaScenarioExecNodeResponse nodeRes = new RpaScenarioExecNodeResponse();
			RestBeanUtil.convertBeanNoInvalid(execNode, nodeRes);
			execNodeResponseList.add(nodeRes);
		}
		dtoRes.setExecNodeList(execNodeResponseList);
		
		List<RpaScenarioResponseP1> scenarioResList = new ArrayList<>();
		for (RpaScenario scenario : scenarioList){
			RpaScenarioResponseP1 scenarioRes = new RpaScenarioResponseP1();
			RestBeanUtil.convertBeanNoInvalid(scenario, scenarioRes);
			scenarioResList.add(scenarioRes);
		}
		dtoRes.setScenarioList(scenarioResList);
		
		return dtoRes;
	}
	
	/**
	 * 集計グラフ(棒)用の情報の取得
	 */
	public GetRpaScenarioOperationResultSummaryForBarResponse getRpaScenarioOperationResultSummaryForBar(List<String> facilityIds, 
			Long targetMonth, SummaryTypeEnum dataType, int limit) throws InvalidRole, HinemosUnknown {
		
		// 検索年月を設定
		Date startDateFrom = new Date(targetMonth);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDateFrom);
		
		int max = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		
		calendar.add(Calendar.MONTH, 1);
		Date startDateTo = calendar.getTime();
		
		GetRpaScenarioOperationResultSummaryForBarResponse dtoRes = new GetRpaScenarioOperationResultSummaryForBarResponse();
		
		switch(dataType.getCode()){
			case SummaryTypeConstant.TYPE_DAILY_COUNT: {
				setDailyCount(dtoRes, max,
						getRpaScenarioOperationResultListForGraph(startDateFrom.getTime(), startDateTo.getTime(), facilityIds, dataType));
				
				break;
			}
			case SummaryTypeConstant.TYPE_HOURLY_REDUCTION: {
				setHourlyReduction(dtoRes, 
						getRpaScenarioOperationResultListForGraph(startDateFrom.getTime(), startDateTo.getTime(), facilityIds, dataType));
				
				break;
			}
			case SummaryTypeConstant.TYPE_SCENARIO_ERRORS: {
				setScenarioErrors(dtoRes, limit, 
						getRpaScenarioOperationResultListForGraph(startDateFrom.getTime(), startDateTo.getTime(), facilityIds, dataType));
				
				break;
			}
			case SummaryTypeConstant.TYPE_NODE_ERRORS: {
				setNodeErrors(dtoRes, limit, 
						getRpaScenarioOperationResultListForGraph(startDateFrom.getTime(), startDateTo.getTime(), facilityIds, dataType));
				
				break;
			}
			case SummaryTypeConstant.TYPE_SCENARIO_REDUCTION: {
				setScenarioReduction(dtoRes,  limit, 
						getRpaScenarioOperationResultListForGraph(startDateFrom.getTime(), startDateTo.getTime(), facilityIds, dataType));
				
				break;
			}
			case SummaryTypeConstant.TYPE_NODE_REDUCTION: {
				setNodeReduction(dtoRes,  limit, 
						getRpaScenarioOperationResultListForGraph(startDateFrom.getTime(), startDateTo.getTime(), facilityIds, dataType));
				
				break;
			}
			default: {
				break;
			}
		}
		
		return dtoRes;
	}
	
	/**
	 * 集計グラフ(円)用の情報の取得
	 */
	public GetRpaScenarioOperationResultSummaryForPieResponse getRpaScenarioOperationResultSummaryForPie(List<String> facilityIds, 
			Long targetMonth, SummaryTypeEnum dataType) throws InvalidRole, HinemosUnknown {
		
		// 検索年月を設定
		Date startDateFrom = new Date(targetMonth);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDateFrom);
		
		calendar.add(Calendar.MONTH, 1);
		Date startDateTo = calendar.getTime();
		
		GetRpaScenarioOperationResultSummaryForPieResponse dtoRes = new GetRpaScenarioOperationResultSummaryForPieResponse();
		
		switch(dataType.getCode()){
			case SummaryTypeConstant.TYPE_ERRORS: {
				setErrorsForPie(dtoRes, 
						getRpaScenarioOperationResultListForGraph(startDateFrom.getTime(), startDateTo.getTime(), facilityIds, dataType));
				
				break;
			}
			case SummaryTypeConstant.TYPE_REDUCTION: {
				setReductionForPie(dtoRes, 
						getRpaScenarioOperationResultListForGraph(startDateFrom.getTime(), startDateTo.getTime(), facilityIds, dataType));
				
				break;
			}
			default: {
				break;
			}
		}
		
		return dtoRes;
	}
	
	/**
	 * 集計グラフ用の情報の取得
	 */
	private List<Object[]> getRpaScenarioOperationResultListForGraph(
			Long startDateFrom, Long startDateTo, List<String> facilityIds, SummaryTypeEnum dataType) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<Object[]> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			switch(dataType.getCode()){
				case SummaryTypeConstant.TYPE_DAILY_COUNT: {
					list = new SelectRpaScenarioOperationResult()
							.getRpaScenarioOperationResultDailyErrorsCount(startDateFrom, startDateTo, facilityIds);
					
					break;
				}
				case SummaryTypeConstant.TYPE_HOURLY_REDUCTION: {
					list = new SelectRpaScenarioOperationResult()
							.getRpaScenarioOperationResultHourlyReductionCount(startDateFrom, startDateTo, facilityIds);
					
					break;
				}
				case SummaryTypeConstant.TYPE_SCENARIO_ERRORS: {
					list = new SelectRpaScenarioOperationResult()
							.getRpaScenarioOperationResultScenarioErrorsCount(startDateFrom, startDateTo, facilityIds);
					
					break;
				}
				case SummaryTypeConstant.TYPE_NODE_ERRORS: {
					list = new SelectRpaScenarioOperationResult()
							.getRpaScenarioOperationResultNodeErrorsCount(startDateFrom, startDateTo, facilityIds);
					
					break;
				}
				case SummaryTypeConstant.TYPE_SCENARIO_REDUCTION: {
					list = new SelectRpaScenarioOperationResult()
							.getRpaScenarioOperationResultScenarioReductionCount(startDateFrom, startDateTo, facilityIds);
					
					break;
				}
				case SummaryTypeConstant.TYPE_NODE_REDUCTION: {
					list = new SelectRpaScenarioOperationResult()
							.getRpaScenarioOperationResultNodeReductionCount(startDateFrom, startDateTo, facilityIds);
					
					break;
				}
				case SummaryTypeConstant.TYPE_ERRORS: {
					list = new SelectRpaScenarioOperationResult()
							.getRpaScenarioOperationResultErrorsCount(startDateFrom, startDateTo, facilityIds);
					
					break;
				}
				case SummaryTypeConstant.TYPE_REDUCTION: {
					list = new SelectRpaScenarioOperationResult()
							.getRpaScenarioOperationResultReductionCount(startDateFrom, startDateTo, facilityIds);
					
					break;
				}
				default: {
					break;
				}
			}
			
			jtm.commit();
		} catch (HinemosUnknown | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getRpaScenarioOperationResultListForGraph : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		
		return list;
	}

	/**
	 * グラフにおけるノード名情報を返します。 
	 */
	private Map<String,String> getFacilityNameMap() throws HinemosUnknown {
		Map<String,String> facilityNameMap = new HashMap<>();
		ArrayList<NodeInfo> entityList = new RepositoryControllerBean().getNodeList();
		
		for (NodeInfo entity : entityList){
			facilityNameMap.put(entity.getFacilityId(), entity.getFacilityName());
		}
		
		return facilityNameMap;
	}
	
	/**
	 * 日別シナリオ実施件数の集計結果をセット
	 */
	private void setDailyCount(GetRpaScenarioOperationResultSummaryForBarResponse dtoRes, 
			int max, List<Object[]> countList){
		dtoRes.setName(MessageConstant.RPA_SCENARIO_OPERATION_RESULT_DAYLY_COUNT_NAME.getMessage());

		List<String> stacks = Arrays.asList(MessageConstant.SUCCESS.getMessage(), MessageConstant.FAILED.getMessage());
		dtoRes.setStructure(getStructure(MessageConstant.DAY.getMessage(), 
				MessageConstant.COUNT.getMessage(), stacks));
		
		List<GetRpaScenarioOperationResultSummaryDataResponse> datas = new ArrayList<>();
		
		for (int i = 1; i <= max; i++){
			GetRpaScenarioOperationResultSummaryDataResponse data = new GetRpaScenarioOperationResultSummaryDataResponse();
			Double[] summaryArray = {0.0, 0.0};
			
			Iterator<Object[]> it = countList.iterator();
			while(it.hasNext()){
				Object[] obj = it.next();
				
				SimpleDateFormat sdf = new SimpleDateFormat("d");
				String date = sdf.format(obj[0]);
				
				if (Integer.parseInt(date) != i){
					continue;
				}
				
				if (obj[1] == OperationResultStatus.NORMAL_END){
					summaryArray[0] = ((Number) obj[2]).doubleValue();
				} else {
					summaryArray[1] = ((Number) obj[2]).doubleValue();
				}
				it.remove();
			}
			
			data.setItem(i + MessageConstant.DAY.getMessage());
			
			List<Double> values = new ArrayList<>();
			values.add(summaryArray[0]);
			values.add(summaryArray[1]);
			data.setValues(values);
			
			datas.add(data);
		}
		dtoRes.setDatas(datas);
	}
	
	/**
	 * 時間帯別削減工数の集計結果をセット
	 */
	private void setHourlyReduction(GetRpaScenarioOperationResultSummaryForBarResponse dtoRes, 
			List<Object[]> countList) {
		dtoRes.setName(MessageConstant.RPA_SCENARIO_OPERATION_RESULT_HOURLY_REDUCTION_NAME.getMessage());

		List<String> stacks = Arrays.asList(MessageConstant.REDUCTION_TIME.getMessage(), MessageConstant.RUN_TIME.getMessage());
		dtoRes.setStructure(getStructure(MessageConstant.HOUR.getMessage(), 
				MessageConstant.MINUTE.getMessage(), stacks));
		
		// 月,表示名のMap
		Map<String, String> hoursMap = new LinkedHashMap<>();
		for (int i = 0; i <= 24; i++){
			String hourString = String.valueOf(i); 
			hoursMap.put(hourString, MessageConstant.IN_HOUR.getMessage(hourString));
		}
		List<GetRpaScenarioOperationResultSummaryDataResponse> datas = setReductionDatas(countList, 24, hoursMap, true, true);
		// setReductionDatasで反転しているので、戻す。
		Collections.reverse(datas);
		dtoRes.setDatas(datas);
	}
	
	/**
	 * シナリオ別エラー数の集計結果をセット
	 */
	private void setScenarioErrors(GetRpaScenarioOperationResultSummaryForBarResponse dtoRes, 
			int limit, List<Object[]> countList) {
		dtoRes.setName(MessageConstant.RPA_SCENARIO_OPERATION_RESULT_SCENARIO_ERRORS_NAME.getMessage());
		List<String> stacks = Arrays.asList(MessageConstant.SUCCESS.getMessage(), MessageConstant.FAILED.getMessage());
		dtoRes.setStructure(getStructure(MessageConstant.RPA_SCENARIO_OPERATION_RESULT_SCENARIO.getMessage(), 
				MessageConstant.COUNT.getMessage(), stacks));
		
		Map<String,String> scenarioNameMap = new SelectRpaScenario().getRpaScenarioNameMap();
		dtoRes.setDatas(sortErrorsDatas(setErrorsDatas(countList), limit, scenarioNameMap));
	}
	
	/**
	 * ノード別エラー数の集計結果をセット
	 */
	private void setNodeErrors(GetRpaScenarioOperationResultSummaryForBarResponse dtoRes, 
			int limit, List<Object[]> countList) throws HinemosUnknown {
		dtoRes.setName(MessageConstant.RPA_SCENARIO_OPERATION_RESULT_NODE_ERRORS_NAME.getMessage());
		List<String> stacks = Arrays.asList(MessageConstant.SUCCESS.getMessage(), MessageConstant.FAILED.getMessage());
		dtoRes.setStructure(getStructure(MessageConstant.RPA_SCENARIO_OPERATION_RESULT_NODE.getMessage(), 
				MessageConstant.COUNT.getMessage(), stacks));
		
		Map<String, String> facilityNameMap = getFacilityNameMap();
		dtoRes.setDatas(sortErrorsDatas(setErrorsDatas(countList), limit, facilityNameMap));
	}
	
	/**
	 * シナリオ別削減工数の集計結果をセット
	 */
	private void setScenarioReduction(GetRpaScenarioOperationResultSummaryForBarResponse dtoRes, 
			int limit, List<Object[]> countList) {
		dtoRes.setName(MessageConstant.RPA_SCENARIO_OPERATION_RESULT_SCENARIO_REDUCTION_NAME.getMessage());
		List<String> stacks = Arrays.asList(MessageConstant.REDUCTION_TIME.getMessage(), MessageConstant.RUN_TIME.getMessage());
		dtoRes.setStructure(getStructure(MessageConstant.RPA_SCENARIO_OPERATION_RESULT_SCENARIO.getMessage(), 
				MessageConstant.MINUTE.getMessage(), stacks));
		
		Map<String,String> scenarioNameMap = new SelectRpaScenario().getRpaScenarioNameMap();
		dtoRes.setDatas(sortErrorsDatas(setReductionDatas(countList, limit, scenarioNameMap, false, false),limit,scenarioNameMap));
	}
	
	/**
	 * ノード別削減工数の集計結果をセット
	 */
	private void setNodeReduction(GetRpaScenarioOperationResultSummaryForBarResponse dtoRes, 
			int limit, List<Object[]> countList) throws HinemosUnknown {
		dtoRes.setName(MessageConstant.RPA_SCENARIO_OPERATION_RESULT_NODE_REDUCTION_NAME.getMessage());
		List<String> stacks = Arrays.asList(MessageConstant.REDUCTION_TIME.getMessage(), MessageConstant.RUN_TIME.getMessage());
		dtoRes.setStructure(getStructure(MessageConstant.RPA_SCENARIO_OPERATION_RESULT_NODE.getMessage(), 
				MessageConstant.MINUTE.getMessage(), stacks));
		
		Map<String, String> facilityNameMap = getFacilityNameMap();
		dtoRes.setDatas(sortErrorsDatas(setReductionDatas(countList, limit, facilityNameMap, false, false),limit,facilityNameMap));
	}
	
	/**
	 * 円グラフ用エラー割合の集計結果をセット
	 */
	private void setErrorsForPie(GetRpaScenarioOperationResultSummaryForPieResponse dtoRes, 
			List<Object[]> countList) {
		dtoRes.setName(MessageConstant.RPA_SCENARIO_OPERATION_RESULT_ERRORS_NAME.getMessage());

		List<String> stacks = Arrays.asList(MessageConstant.SUCCESS.getMessage(), MessageConstant.FAILED.getMessage());
		dtoRes.setStructure(getStructure(MessageConstant.DAY.getMessage(), 
				MessageConstant.COUNT.getMessage(), stacks));
		
		List<Double> datas = new ArrayList<>();
		Double success = 0.0;
		Double error = 0.0;
		for ( Object[] count : countList){
			if (count[0] == OperationResultStatus.NORMAL_END){
				success = ((Number) count[1]).doubleValue();
			} else {
				error = ((Number) count[1]).doubleValue();
			}
		}
		datas.add(success);
		datas.add(error);
		
		dtoRes.setDatas(datas);
	}
	
	/**
	 * 円グラフ用削減工数の集計結果をセット
	 */
	private void setReductionForPie(GetRpaScenarioOperationResultSummaryForPieResponse dtoRes, 
			List<Object[]> countList) {
		dtoRes.setName(MessageConstant.RPA_SCENARIO_OPERATION_RESULT_REDUCTION_NAME.getMessage());
		List<String> stacks = Arrays.asList(MessageConstant.REDUCTION_TIME.getMessage(), MessageConstant.RUN_TIME.getMessage());
		dtoRes.setStructure(getStructure(MessageConstant.MINUTE.getMessage(), 
				MessageConstant.MINUTE.getMessage(), stacks));
		
		List<Double> datas = new ArrayList<>(Arrays.asList(0.0, 0.0));
		
		for (Object[] count : countList) {
			for (int i=0; i<2 ;i++) {
				// 削減時間、実行時間はnullの場合がある(異常終了時)ため、nullチェック
				Number valueData = (Number) count[i];
				if (valueData == null) { valueData = 0; }
				double value = valueData.doubleValue() / (60 * 1000);
				datas.set(i, datas.get(i) + value);
			}
		}

		dtoRes.setDatas(datas);
	}
	
	/**
	 * グラフ用の単位データをセット
	 */
	private GetRpaScenarioOperationResultSummaryStructureResponse getStructure(String item, String mesure, List<String> stacks){
		GetRpaScenarioOperationResultSummaryStructureResponse structure = new GetRpaScenarioOperationResultSummaryStructureResponse();
		structure.setItem(item);
		structure.setMesure(mesure);
		structure.setStacks(stacks);
		return structure;
	}
	
	/**
	 * エラー数グラフ描画用のデータをセット
	 */
	private List<GetRpaScenarioOperationResultSummaryDataResponse> setErrorsDatas(List<Object[]> countList){
		List<GetRpaScenarioOperationResultSummaryDataResponse> datas = new ArrayList<>();
		
		Map<String, Double[]> countMap = new HashMap<>();
		for (Object[] it : countList){ 
			if (countMap.get(it[0].toString()) == null){
				Double[] doubleArray = {0.0, 0.0};
				countMap.put(it[0].toString(), doubleArray);
			}
			
			if(OperationResultStatus.NORMAL_END.toString().equals(it[1].toString())){
				countMap.get(it[0].toString())[0] = ((Number) it[2]).doubleValue();
			} else if(OperationResultStatus.ERROR_END.toString().equals(it[1].toString())){
				countMap.get(it[0].toString())[1] = ((Number) it[2]).doubleValue();
			}
		}
		
		for ( Entry<String, Double[]> entry : countMap.entrySet()){
			GetRpaScenarioOperationResultSummaryDataResponse data = new GetRpaScenarioOperationResultSummaryDataResponse();
			
			data.setItem(entry.getKey());
			
			List<Double> values = new ArrayList<>();
			values.add(entry.getValue()[0]);
			values.add(entry.getValue()[1]);
			data.setValues(values);
			
			datas.add(data);
		}
		
		return datas;
	}
	
	/**
	 * 削減工数グラフ描画用のデータをセット
	 */
	private List<GetRpaScenarioOperationResultSummaryDataResponse> setReductionDatas(List<Object[]> countList,
			int limit, Map<String, String> nameMap, boolean padding, boolean useNameAndIdAsItem) {
		List<GetRpaScenarioOperationResultSummaryDataResponse> datas = new ArrayList<>();
		
		// 最大表示件数に対応する為、一度取得データを逆順にする
		Collections.reverse(countList);
		
		// データ格納マップ(順序を維持するためLinkedHashMap)
		Map<String, Double> reductionTimes = new LinkedHashMap<>();
		Map<String, Double> runTimes = new LinkedHashMap<>();
		
		if (padding) {
			// nameMapのitemを全て格納する場合、最初にデータ初期値を格納する。
			for (String itemKey : nameMap.keySet()) {
				runTimes.put(itemKey, 0.0);
				reductionTimes.put(itemKey, 0.0);				
			}
		}
		
		// データをMapに格納
		for (Object[] it : countList){
			String itemKey = it[0].toString();
			
			if (nameMap.get(itemKey) == null) {
				String itemName = it[0].toString();
				nameMap.put(itemKey, itemName);
			}
			
			if (runTimes.get(itemKey) == null) {
				runTimes.put(itemKey, 0.0);
			}

			if (reductionTimes.get(itemKey) == null) {
				reductionTimes.put(itemKey, 0.0);
			}
			
			// 削減時間、実行時間はnullの場合がある(異常終了時)ため、nullチェック
			Number reductionTimeData = (Number) it[1];
			if (reductionTimeData == null) { reductionTimeData = 0;}
			Number runTimeData = (Number) it[2];
			if (runTimeData == null) { runTimeData = 0;}

			Double reductionTime = reductionTimeData.doubleValue() / (60 * 1000);
			Double runTime = runTimeData.doubleValue() / (60 * 1000);
			
			// 同一キーがある場合は加算する。(手動操作時間自動と固定で別々に取得している場合があるため)
			reductionTimes.put(itemKey, reductionTimes.get(itemKey) + reductionTime);
			runTimes.put(itemKey, runTimes.get(itemKey) + runTime);
		}
		
		// responseに格納
		// findbus対応 map参照を Entry化
		for (Entry<String, Double> rec: reductionTimes.entrySet()) {
			String itemKey = rec.getKey();
			if (limit <= datas.size()){
				break;
			}
			if (nameMap.get(itemKey) == null) {
				continue;
			}
			
			GetRpaScenarioOperationResultSummaryDataResponse data = new GetRpaScenarioOperationResultSummaryDataResponse();
			if (useNameAndIdAsItem) {
				//時間帯別削減工数(分)の場合は時間の表示名をそのままセット
				data.setItem(nameMap.get(itemKey));
			} else {
				// シナリオ別削減工数(分)およびノード別削減工数(分)の場合は名前とIDをセット
				data.setItem(nameMap.get(itemKey) + "(" + itemKey + ")");
			}

			double reductionTime = rec.getValue();
			double runTime = runTimes.get(itemKey);

			List<Double> values = new ArrayList<>();
			values.add(reductionTime);
			values.add(runTime);
			data.setValues(values);
			
			datas.add(data);
		}
		
		// 最大表示件数に対応する為に逆順にしてセットしたデータを元の順番に戻す
		Collections.reverse(datas);
		return datas;
	}
	
	/**
	 * エラー数グラフに表示する項目のソート処理
	 */
	private List<GetRpaScenarioOperationResultSummaryDataResponse> sortErrorsDatas(
			List<GetRpaScenarioOperationResultSummaryDataResponse> datas, int limit, Map<String, String> nameMap){
		Map<String, Double> sortMap = new HashMap<>();
		for (GetRpaScenarioOperationResultSummaryDataResponse data: datas){
			Double rate = data.getValues().get(1) / (data.getValues().get(0) + data.getValues().get(1));
			sortMap.put(data.getItem(), rate);
		}
		
		return sortDatas(datas, sortMap, nameMap, limit);
	}
	
	/**
	 * グラフに表示する項目のソート処理の本体
	 */
	private List<GetRpaScenarioOperationResultSummaryDataResponse> sortDatas(
			List<GetRpaScenarioOperationResultSummaryDataResponse> datas, 
			Map<String, Double> sortMap, Map<String, String> nameMap, int limit){
		List<Entry<String, Double>> listEntries = new ArrayList<Entry<String, Double>>(sortMap.entrySet());
		
		Comparator<Entry<String, Double>> entryComparator =
				Comparator.comparing(Entry<String, Double>::getValue, Comparator.reverseOrder())
				.thenComparing(Entry<String, Double>::getKey);
		listEntries.sort(entryComparator);
		
		List<GetRpaScenarioOperationResultSummaryDataResponse> sortDatas = new ArrayList<>();
		for (Entry<String, Double> entry : listEntries){
			if (limit <= sortDatas.size()){
				break;
			}
			
			for (GetRpaScenarioOperationResultSummaryDataResponse data: datas){
				if (entry.getKey() == null || data.getItem() == null) {
					continue;
				}
				if ( !entry.getKey().equals(data.getItem()) ){
					continue;
				}
				
				String item = nameMap.get(data.getItem());
				if (item != null){
					data.setItem(item + "(" + data.getItem() + ")");
				} else {
					data.setItem(data.getItem());
				}
				sortDatas.add(data);
			}
		}
		
		// グラフを描画する際に逆順になるため、ここでは想定の順番とは逆にしておく
		Collections.reverse(sortDatas);
		return sortDatas;
	}

	/**
	 * IDからシナリオ実績作成設定を取得する。
	 */
	public RpaScenarioOperationResultCreateSetting getRpaScenarioOperationResultCreateSetting(String settingId) throws RpaScenarioOperationResultCreateSettingNotFound, InvalidRole, HinemosUnknown {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			jtm.begin();
			RpaScenarioOperationResultCreateSetting ret = QueryUtil.getRpaScenarioCreateSettingPK(settingId);
			
			// 通知IDをセット
			ret.setNotifyId(new NotifyControllerBean().getNotifyRelation(ret.getNotifyGroupId()));
			
			jtm.commit();
			return ret;
		} catch (RpaScenarioOperationResultCreateSettingNotFound e) {
			throw new RpaScenarioOperationResultCreateSettingNotFound(
					MessageConstant.MESSAGE_NOT_FOUND.getMessage(
							MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CREATE_SETTING_ID.getMessage(), 
							settingId));
		} catch (InvalidRole e) {
			throw new InvalidRole(MessageConstant.MESSAGE_DO_NOT_HAVE_ENOUGH_PERMISSION.getMessage());
		}
	}
	
	/**
	 * シナリオ実績作成設定一覧を取得する。
	 */
	public List<RpaScenarioOperationResultCreateSetting> getRpaScenarioOperationResultCreateSettingList() throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			List<RpaScenarioOperationResultCreateSetting> list = QueryUtil.getRpaScenarioCreateSettingList();
			for (RpaScenarioOperationResultCreateSetting info : list) {
				// 通知IDをセット
				info.setNotifyId(new NotifyControllerBean().getNotifyRelation(info.getNotifyGroupId()));				
			}
			jtm.commit();
			return list;
		} catch (Exception e){
			m_log.warn("getRpaScenarioOperationResultCreateSettingList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	/**
	 * シナリオ実績作成設定を登録する。
	 * 
	 * @param data 登録情報
	 * @param isImport true:設定インポートエクスポートから実行、false:それ以外
	 * @return
	 * @throws InvalidSetting
	 * @throws RpaScenarioOperationResultCreateSettingDuplicate
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public RpaScenarioOperationResultCreateSetting addRpaScenarioOperationResultCreateSetting(RpaScenarioOperationResultCreateSetting data, boolean isImport)
			throws InvalidSetting , RpaScenarioOperationResultCreateSettingDuplicate, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID); 
			//ユーザがオーナーロールIDに所属しているかチェック
			RoleValidator.validateUserBelongRole(data.getOwnerRoleId(),
					userId,
					(Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR));
			
			// ファシリティIDの存在チェック、オーナーロールからの参照チェック
			FacilityTreeCache.validateFacilityId(data.getFacilityId(), data.getOwnerRoleId(), false);
			// カレンダIDの存在チェック、オーナーロールからの参照チェック
			CommonValidator.validateCalenderId(data.getCalendarId(), false, data.getOwnerRoleId());
			
			// 通知IDの存在チェック、設定
			String notifyGroupId = NotifyGroupIdGenerator.generate(data);
			data.setNotifyGroupId(notifyGroupId);
			for(NotifyRelationInfo notifyInfo : data.getNotifyId()){
				// 通知IDの存在チェック、オーナーロールからの参照チェック
				CommonValidator.validateNotifyId(notifyInfo.getNotifyId(), true, data.getOwnerRoleId());
				notifyInfo.setNotifyGroupId(notifyGroupId);
				notifyInfo.setFunctionPrefix(FunctionPrefixEnum.RPA_SCENARIO_CREATE.name());
			}

			// 登録
			new ModifyRpaScenario().addCreateSetting(data, userId);
			
			// スケジュール登録
			ModifySchedule.updateAnalyzeSchedule(data);			

			// コールバックメソッド設定
			if (!isImport) {
				addImportRpaScenarioOperationResultCreateSettingCallback(jtm);
			}
			jtm.commit();

			m_log.info("add RPA Scenario Operation Result Create Setting. settingId=" + data.getScenarioOperationResultCreateSettingId());
			return getRpaScenarioOperationResultCreateSetting(data.getScenarioOperationResultCreateSettingId());
		} catch (InvalidSetting e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidSetting(
					String.format("%s %s", 
					MessageConstant.MESSAGE_FAILED_TO_MODIFY.getMessage()
					, e.getMessage()));
		} catch (RpaScenarioOperationResultCreateSettingDuplicate e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new RpaScenarioOperationResultCreateSettingDuplicate(
					String.format("%s %s",
					MessageConstant.MESSAGE_FAILED_TO_CREATE.getMessage(
							MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CREATE_SETTING.getMessage()),
					MessageConstant.MESSAGE_DUPLICATED.getMessage(
							MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CREATE_SETTING_ID.getMessage(), 
							data.getScenarioOperationResultCreateSettingId())));
		} catch (InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(MessageConstant.MESSAGE_DO_NOT_HAVE_ENOUGH_PERMISSION.getMessage());
		} catch (FacilityNotFound e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidSetting(
					String.format("%s %s", 
							MessageConstant.MESSAGE_FAILED_TO_CREATE.getMessage(
									MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CREATE_SETTING.getMessage()),
					MessageConstant.MESSAGE_NOT_FOUND.getMessage(
							MessageConstant.SCOPE.getMessage(), 
							data.getFacilityId())));
		} catch (Exception e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(MessageConstant.MESSAGE_UNEXPECTED_ERR_OCCURRED.getMessage(e.getMessage()));
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}
	
	/**
	 * シナリオ実績作成設定の変更。
	 * 
	 * @param data 登録情報
	 * @param isImport true:設定インポートエクスポートから実行、false:それ以外
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws RpaScenarioOperationResultCreateSettingNotFound
	 * @throws InvalidSetting
	 */
	public RpaScenarioOperationResultCreateSetting modifyRpaScenarioOperationResultCreateSetting(RpaScenarioOperationResultCreateSetting data, boolean isImport)
			throws HinemosUnknown, InvalidRole, RpaScenarioOperationResultCreateSettingNotFound, InvalidSetting {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID); 

			//RPAシナリオ実績作成設定を取得
			RpaScenarioOperationResultCreateSetting entity
				= QueryUtil.getRpaScenarioCreateSettingPK(data.getScenarioOperationResultCreateSettingId(), ObjectPrivilegeMode.MODIFY);
			// ファシリティIDの存在チェック、オーナーロールからの参照チェック
			FacilityTreeCache.validateFacilityId(data.getFacilityId(), entity.getOwnerRoleId(), false);
			// カレンダIDの存在チェック、オーナーロールからの参照チェック
			CommonValidator.validateCalenderId(data.getCalendarId(), false, entity.getOwnerRoleId());
			// 通知IDの存在チェック、設定
			String notifyGroupId = NotifyGroupIdGenerator.generate(data);
			data.setNotifyGroupId(notifyGroupId);
			for(NotifyRelationInfo notifyInfo : data.getNotifyId()){
				CommonValidator.validateNotifyId(notifyInfo.getNotifyId(), true, getRpaScenarioOperationResultCreateSetting(data.getScenarioOperationResultCreateSettingId()).getOwnerRoleId());
				notifyInfo.setNotifyGroupId(notifyGroupId);
				notifyInfo.setFunctionPrefix(FunctionPrefixEnum.RPA_SCENARIO_CREATE.name());
			}

				// 設定変更
			new ModifyRpaScenario().modifyCreateSetting(data, userId);
			// スケジュール更新
			ModifySchedule.updateAnalyzeSchedule(data);		
			
			// コールバックメソッド設定
			if (!isImport) {
				addImportRpaScenarioOperationResultCreateSettingCallback(jtm);
			}

			jtm.commit();

			m_log.info("modify RPA Scenario Operation Result Create Setting. settingId=" + data.getScenarioOperationResultCreateSettingId());
			return getRpaScenarioOperationResultCreateSetting(data.getScenarioOperationResultCreateSettingId());
		} catch (RpaScenarioOperationResultCreateSettingNotFound e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new RpaScenarioOperationResultCreateSettingNotFound(
					String.format("%s %s",
					MessageConstant.MESSAGE_FAILED_TO_MODIFY.getMessage(
							MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CREATE_SETTING.getMessage()),
					MessageConstant.MESSAGE_NOT_FOUND.getMessage(
							MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CREATE_SETTING_ID.getMessage(), 
							data.getScenarioOperationResultCreateSettingId())));
		} catch (InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(MessageConstant.MESSAGE_DO_NOT_HAVE_ENOUGH_PERMISSION.getMessage());
		} catch (FacilityNotFound e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidSetting(
					String.format("%s %s", 
					MessageConstant.MESSAGE_FAILED_TO_MODIFY.getMessage(
							MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CREATE_SETTING.getMessage()),
					MessageConstant.MESSAGE_NOT_FOUND.getMessage(
							MessageConstant.SCOPE.getMessage(), 
							data.getFacilityId())));
		} catch (InvalidSetting e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidSetting(
					String.format("%s %s", 
					MessageConstant.MESSAGE_FAILED_TO_MODIFY.getMessage()
					, e.getMessage()));
		} catch (Exception e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(MessageConstant.MESSAGE_UNEXPECTED_ERR_OCCURRED.getMessage(e.getMessage()));
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}
	
	/**
	 * シナリオ実績作成設定の新規登録／変更時に呼び出すコールバックメソッドを設定
	 * 
	 * 設定インポートエクスポートでCommit後に呼び出すものだけ定義
	 * 
	 * @param jtm JpaTransactionManager
	 */
	public void addImportRpaScenarioOperationResultCreateSettingCallback(JpaTransactionManager jtm) {
		// 通知リレーション情報のキャッシュ更新
		jtm.addCallback(new NotifyRelationCacheRefreshCallback());
	}

	/**
	 * シナリオ作成設定の削除
	 */
	public List<RpaScenarioOperationResultCreateSetting> deleteRpaScenarioOperationResultCreateSetting(List<String> settingIdList)
			throws RpaScenarioOperationResultCreateSettingNotFound, InvalidRole, HinemosUnknown, InvalidSetting {
		JpaTransactionManager jtm = null;
		String id = "";
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			List<RpaScenarioOperationResultCreateSetting> ret = new ArrayList<>();
			for (String settingId : settingIdList) {
				// 紐づくシナリオ実績があれば設定削除不可
				List<RpaScenario> ScenarioList = QueryUtil.getRpaScenarioByFilter(settingId,null,null,null,null,null);
				if(ScenarioList != null && ScenarioList.size() > 0){
					StringBuilder Idlist = new StringBuilder();
					for(RpaScenario rec : ScenarioList){
						if( Idlist.length() > 0  ){
							Idlist.append(",");
						}
						Idlist.append(rec.getScenarioId());
					}
					String[] args = { Idlist.toString() };
					String message = MessageConstant.MESSAGE_RPA_SCENARIO_OPERATION_RESULT_LINKED.getMessage(args);
					throw new InvalidSetting(message);
				}
				id = settingId;
				RpaScenarioOperationResultCreateSetting entity = getRpaScenarioOperationResultCreateSetting(settingId);
				new ModifyRpaScenario().deleteCreateSetting(settingId);
				// スケジュール削除
				ModifySchedule.deleteAnalyzeSchedule(settingId);
				ret.add(entity);
			}

			// コミット後にリフレッシュする
			jtm.addCallback(new NotifyRelationCacheRefreshCallback());
			jtm.commit();

			m_log.info("delete RPA Scenario Operation Result Create Setting. settingId=" + String.join(", ", settingIdList.toArray(new String[settingIdList.size()])));
			return ret;
		} catch (RpaScenarioOperationResultCreateSettingNotFound e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new RpaScenarioOperationResultCreateSettingNotFound(
					String.format("%s %s",
					MessageConstant.MESSAGE_FAILED_TO_DELETE.getMessage(
							MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CREATE_SETTING.getMessage()),
					MessageConstant.MESSAGE_NOT_FOUND.getMessage(
							MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CREATE_SETTING_ID.getMessage(), 
							id)));
		} catch (InvalidRole | ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(MessageConstant.MESSAGE_DO_NOT_HAVE_ENOUGH_PERMISSION.getMessage());
		} catch ( InvalidSetting e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch ( Exception e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(MessageConstant.MESSAGE_UNEXPECTED_ERR_OCCURRED.getMessage(e.getMessage()));
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}
	
	/**
	 * シナリオ実績作成設定の有効/無効を設定する。
	 */
	public List<RpaScenarioOperationResultCreateSetting> setRpaScenarioOperationResultCreateSettingValid(List<String> settingIdList, boolean validflg) throws HinemosUnknown, InvalidRole, RpaScenarioOperationResultCreateSettingNotFound, InvalidSetting {
		JpaTransactionManager jtm = null;
		String settingId = "";
		List<RpaScenarioOperationResultCreateSetting> ret = new ArrayList<>();
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
			
			for (String id : settingIdList) {
				settingId = id;
				RpaScenarioOperationResultCreateSetting setting = getRpaScenarioOperationResultCreateSetting(settingId);
				
				// 設定変更
				setting.setValidFlg(validflg);
				new ModifyRpaScenario().modifyCreateSetting(setting, userId);
				// スケジュール更新
				ModifySchedule.updateAnalyzeSchedule(setting);
				ret.add(setting);
			}

			jtm.commit();
			m_log.info("set validflg RPA Scenario Operation Result Create Setting. settingIdList=" + settingIdList + ", validflg=" + validflg);
			return ret;
		} catch (RpaScenarioOperationResultCreateSettingNotFound e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new RpaScenarioOperationResultCreateSettingNotFound(
					String.format("%s %s",
					MessageConstant.MESSAGE_FAILED_TO_MODIFY.getMessage(
							MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CREATE_SETTING.getMessage()),
					MessageConstant.MESSAGE_NOT_FOUND.getMessage(
							MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CREATE_SETTING_ID.getMessage(), 
							settingId)));
		} catch (InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(MessageConstant.MESSAGE_DO_NOT_HAVE_ENOUGH_PERMISSION.getMessage());
		} catch (Exception e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(MessageConstant.MESSAGE_UNEXPECTED_ERR_OCCURRED.getMessage(e.getMessage()));
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	/**
	 * シナリオ実績レコードをマネージャに一時ファイルとして出力
	 * 
	 * レコードをZIPにまとめるため一時ファイルとして出力
	 */
	public ArrayList<String> createTmpRecords(DownloadRpaScenarioOperationResultRecordsRequest dtoReq, Locale locale)
			throws HinemosUnknown, InvalidSetting, InvalidRole {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		
		ArrayList<String> csvPathList = new ArrayList<>();

		JpaTransactionManager jtm = null;
		DownloadRpaScenarioOperationResult download = new DownloadRpaScenarioOperationResult(dtoReq);
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			RpaScenarioOperationResultFilterInfo filterInfo = new RpaScenarioOperationResultFilterInfo();
			RestBeanUtil.convertBean(dtoReq.getSearchRpaScenarioOperationResultRequest(), filterInfo);
			
			// ファイルを一時保存する為のフォルダを作成
			download.createTemporaryFile(null, dtoReq.getClientName());
			
			if (dtoReq.getScenarioOperationResultFlg()){
				SelectRpaScenarioOperationResult selectResult = new SelectRpaScenarioOperationResult();
				List<String> facilityIds = selectResult.checkFacilityId(filterInfo.getFacilityId());
				
				// 画面側のページングに関わらず、検索条件に一致する全件を取得する。
				filterInfo.setOffset(null);
				filterInfo.setSize(null);
				List<RpaScenarioOperationResult> resultList = selectResult.getRpaScenarioOperationResultList(filterInfo, facilityIds);
				
				String startDateFrom = "";
				String startDateTo = "";
				if(filterInfo.getStartDateFrom() != null){
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
					Date from = new Date(filterInfo.getStartDateFrom());
					startDateFrom = sdf.format(from);
					Date to = new Date(filterInfo.getStartDateTo());
					startDateTo = sdf.format(to);
				}
				
				csvPathList.addAll(exportResultCsv(resultList, download.getDirName(), startDateFrom, startDateTo, locale));
			}
			
			if (dtoReq.getScopeFlg()) {
				List<FacilityInfo> facilityList = new RepositoryControllerBean().getFacilityList();
				csvPathList.add(exportFacilityCsv(facilityList, download.getDirName(), locale));
				List<FacilityRelationEntity> facilityRelationList = com.clustercontrol.repository.util.QueryUtil.getAllFacilityRelations_NONE();
				// 参照可能なファシリティのリレーションのみ出力
				facilityRelationList = facilityRelationList.stream()
						.filter(relation ->	facilityList.stream().anyMatch(facility -> facility.getFacilityId().equals(relation.getChildFacilityId())))
						.filter(relation ->	facilityList.stream().anyMatch(facility -> facility.getFacilityId().equals(relation.getParentFacilityId())))
						.collect(Collectors.toList());
				csvPathList.add(exportFacilityRelationCsv(facilityRelationList, download.getDirName(), locale));
			}
			
			if (dtoReq.getScenarioTagFlg()) {
				List<RpaScenarioTag> tagList = new SelectRpaScenarioTag().getRpaScenarioTagList(null);
				csvPathList.add(exportScenarioTagCsv(tagList, download.getDirName(), locale));
				List<RpaScenarioTagRelation> tagRelationList = new SelectRpaScenarioTag().getRpaScenarioTagRelationList();
				// 参照可能なタグのリレーションのみ出力
				tagRelationList = tagRelationList.stream()
						.filter(relation -> tagList.stream().anyMatch(tag -> tag.getTagId().equals(relation.getId().getTagId())))
						.collect(Collectors.toList());
				csvPathList.add(exportScenarioTagRelationCsv(tagRelationList, download.getDirName(), locale));
			}
			
			if (dtoReq.getScenarioFlg()) {
				List<RpaScenario> scenarioList = new SelectRpaScenario().getRpaScenarioList(new RpaScenarioFilterInfo());
				csvPathList.add(exportScenarioCsv(scenarioList, download.getDirName(), locale));
			}
			
			jtm.commit();
		} catch (InvalidSetting e) {
			if (jtm != null)
				jtm.rollback();
			throw e;
		} catch (IOException e) {
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (HinemosUnknown e) {
			m_log.warn(methodName + DELIMITER + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw e;
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		
		return csvPathList;
	}

	/**
	 * シナリオ実績データをCSVに出力
	 */
	private ArrayList<String> exportResultCsv(List<RpaScenarioOperationResult> resultList, 
			String filePath, String startDateFrom, String startDateTo, Locale locale) throws IOException, HinemosUnknown{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		Map<String, List<RpaScenarioOperationResult>> groupByFacility = 
				resultList.stream().collect(Collectors.groupingBy(RpaScenarioOperationResult::getFacilityId));
		
		ArrayList<String> filePathList = new ArrayList<>();
		
		String exportLineSeparatorStr = 
				HinemosPropertyCommon.rpa_scenario_operation_result_download_csv_line_separator.getStringValue();
		String exportLineSeparator = setSeparator(exportLineSeparatorStr);
		
		for(Entry<String, List<RpaScenarioOperationResult>> entry : groupByFacility.entrySet()){
			String fileName;
			if ("".equals(startDateFrom) || "".equals(startDateTo)){
				fileName = MessageConstant.RPA_SCENARIO_OPERATION_RESULT_DOWNLOAD_CSV_RESULT_ALL
						.getMessage(entry.getKey());
			} else {
				fileName = MessageConstant.RPA_SCENARIO_OPERATION_RESULT_DOWNLOAD_CSV_RESULT
						.getMessage(entry.getKey(), startDateFrom, startDateTo);
			}
			File file = new File(filePath + HinemosMessage.replace(fileName, locale));
			
			FileOutputStream fos = new FileOutputStream(file);
			
			OutputStreamWriter osw = new OutputStreamWriter(fos,
					HinemosPropertyCommon.rpa_scenario_operation_result_download_csv_charset.getStringValue());
			PrintWriter pw = new PrintWriter(new BufferedWriter(osw));
			
			setUtf8Bom(fos);
			
			// ヘッダー出力
			pw.print(HinemosMessage.replace(
					MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CSV_HEADER_OPERATION_RESULT.getMessage(), locale));
			pw.print(exportLineSeparator);
			
			// カラム出力
			for (RpaScenarioOperationResult result : entry.getValue()){
				pw.print(escapeNumber(result.getResultId()) + ",");
				pw.print(escapeString(result.getScenarioId()) + ",");
				pw.print(escapeString(result.getFacilityId()) + ",");
				pw.print(escapeString(result.getRpaToolEnvId()) + ",");
				pw.print(escapeNumber(result.getStartDate()) + ",");
				pw.print(escapeNumber(result.getEndDate()) + ",");
				pw.print(escapeString(result.getStatus().toString()) + ",");
				pw.print(escapeNumber(result.getStep()) + ",");
				pw.print(escapeNumber(result.getRunTime()) + ",");
				pw.print(escapeNumber(result.getManualTime()) + ",");
				pw.print(escapeNumber(result.getCoefficientCost()) + ",");
				pw.print(escapeNumber(result.getReductionTime()) + ",");
				pw.print(escapeNumber(result.getReductionRate()) + ",");
				pw.print(escapeString(result.getScenarioOperationResultCreateSettingId()) + ",");
				pw.print(escapeString(sdf.format(result.getStartDateOnly())) + ",");
				pw.print(escapeNumber(result.getStartHour()));
				pw.print(exportLineSeparator);
			}
			
			pw.close();
			fos.close();
			
			filePathList.add(file.getAbsolutePath());
		}
		
		return filePathList;
	}
	
	/**
	 * ファシリティデータをCSVに出力
	 */
	private String exportFacilityCsv(List<FacilityInfo> facilityList, String filePath, Locale locale) throws IOException, HinemosUnknown{
		String fileName = MessageConstant.RPA_SCENARIO_OPERATION_RESULT_DOWNLOAD_CSV_FACILITY.getMessage();
		File file = new File(filePath + HinemosMessage.replace(fileName, locale));
		
		String exportLineSeparatorStr = 
				HinemosPropertyCommon.rpa_scenario_operation_result_download_csv_line_separator.getStringValue();
		String exportLineSeparator = setSeparator(exportLineSeparatorStr);
		
		FileOutputStream fos = new FileOutputStream(file);
		
		OutputStreamWriter osw = new OutputStreamWriter(fos,
				HinemosPropertyCommon.rpa_scenario_operation_result_download_csv_charset.getStringValue());
		PrintWriter pw = new PrintWriter(new BufferedWriter(osw));
		
		setUtf8Bom(fos);
		
		// ヘッダー出力
		pw.print(HinemosMessage.replace(
				MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CSV_HEADER_FACILITY.getMessage(), locale));
		pw.print(exportLineSeparator);
		
		// カラム出力
		for (FacilityInfo node : facilityList){
			pw.print(escapeString(node.getFacilityId()) + ",");
			pw.print(escapeString(HinemosMessage.replace(node.getFacilityName(), locale)) + ",");
			pw.print(escapeNumber(node.getFacilityType()) + ",");
			pw.print(escapeString(node.getDescription()) + ",");
			pw.print(escapeNumber(node.getDisplaySortOrder()) + ",");
			pw.print(escapeString(node.getIconImage()) + ",");
			pw.print(node.getValid() + ",");
			pw.print(escapeString(node.getOwnerRoleId()) + ",");
			pw.print(escapeString(node.getCreateUserId()) + ",");
			pw.print(escapeNumber(node.getCreateDatetime()) + ",");
			pw.print(escapeString(node.getModifyUserId()) + ",");
			pw.print(escapeNumber(node.getModifyDatetime()));
			pw.print(exportLineSeparator);
		}
		
		pw.close();
		fos.close();
		
		return file.getAbsolutePath();
	}
	
	/**
	 * ファシリティリレーションデータをCSVに出力
	 */
	private String exportFacilityRelationCsv(List<FacilityRelationEntity> facilityRelationList, String filePath, Locale locale) throws IOException, HinemosUnknown{
		String fileName = MessageConstant.RPA_SCENARIO_OPERATION_RESULT_DOWNLOAD_CSV_SCOPE_RELATION.getMessage();
		File file = new File(filePath + HinemosMessage.replace(fileName, locale));
		
		String exportLineSeparatorStr = 
				HinemosPropertyCommon.rpa_scenario_operation_result_download_csv_line_separator.getStringValue();
		String exportLineSeparator = setSeparator(exportLineSeparatorStr);
		
		FileOutputStream fos = new FileOutputStream(file);
		
		OutputStreamWriter osw = new OutputStreamWriter(fos,
				HinemosPropertyCommon.rpa_scenario_operation_result_download_csv_charset.getStringValue());
		PrintWriter pw = new PrintWriter(new BufferedWriter(osw));
		
		setUtf8Bom(fos);
		
		// ヘッダー出力
		pw.print(HinemosMessage.replace(
				MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CSV_HEADER_SCOPE_RELATION.getMessage(), locale));
		pw.print(exportLineSeparator);
		
		// カラム出力
		for (FacilityRelationEntity facilityRelation : facilityRelationList){
			pw.print(escapeString(facilityRelation.getParentFacilityId()) + ",");
			pw.print(escapeString(facilityRelation.getChildFacilityId()));
			pw.print(exportLineSeparator);
		}
		
		pw.close();
		fos.close();
		
		return file.getAbsolutePath();
	}
	
	/**
	 * シナリオデータをCSVに出力
	 */
	private String exportScenarioCsv(List<RpaScenario> scenarioList, String filePath, Locale locale) throws IOException, HinemosUnknown{
		String fileName = MessageConstant.RPA_SCENARIO_OPERATION_RESULT_DOWNLOAD_CSV_SCENARIO.getMessage();
		File file = new File(filePath + HinemosMessage.replace(fileName, locale));
		
		String exportLineSeparatorStr = 
				HinemosPropertyCommon.rpa_scenario_operation_result_download_csv_line_separator.getStringValue();
		String exportLineSeparator = setSeparator(exportLineSeparatorStr);
		
		FileOutputStream fos = new FileOutputStream(file);
		
		OutputStreamWriter osw = new OutputStreamWriter(fos,
				HinemosPropertyCommon.rpa_scenario_operation_result_download_csv_charset.getStringValue());
		PrintWriter pw = new PrintWriter(new BufferedWriter(osw));
		
		setUtf8Bom(fos);
		
		// ヘッダー出力
		pw.print(HinemosMessage.replace(
				MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CSV_HEADER_SCENARIO.getMessage(), locale));
		pw.print(exportLineSeparator);
		
		// カラム出力
		for (RpaScenario scenario : scenarioList){
			pw.print(escapeString(scenario.getScenarioId()) + ",");
			pw.print(escapeString(scenario.getScenarioName()) + ",");
			pw.print(escapeString(scenario.getRpaToolId()) + ",");
			pw.print(escapeString(scenario.getDescription()) + ",");
			pw.print(escapeString(scenario.getOwnerRoleId()) + ",");
			pw.print(escapeString(scenario.getScenarioOperationResultCreateSettingId()) + ",");
			pw.print(escapeString(scenario.getScenarioIdentifyString()) + ",");
			pw.print(escapeNumber(scenario.getManualTime()) + ",");
			pw.print(escapeString(scenario.getManualTimeCulcType().toString()) + ",");
			pw.print(scenario.getCommonNodeScenario() + ",");
			pw.print(escapeNumber(scenario.getOpeStartDate()) + ",");
			pw.print(escapeNumber(scenario.getRegDate()) + ",");
			pw.print(escapeNumber(scenario.getUpdateDate()) + ",");
			pw.print(escapeString(scenario.getRegUser()) + ",");
			pw.print(escapeString(scenario.getUpdateUser()));
			pw.print(exportLineSeparator);
		}
		
		pw.close();
		fos.close();
		
		return file.getAbsolutePath();
	}
	
	/**
	 * タグデータをCSVに出力
	 */
	private String exportScenarioTagCsv(List<RpaScenarioTag> tagList, String filePath, Locale locale) throws IOException, HinemosUnknown{
		String fileName = MessageConstant.RPA_SCENARIO_OPERATION_RESULT_DOWNLOAD_CSV_SCENARIO_TAG.getMessage();
		File file = new File(filePath + HinemosMessage.replace(fileName, locale));
		
		String exportLineSeparatorStr = 
				HinemosPropertyCommon.rpa_scenario_operation_result_download_csv_line_separator.getStringValue();
		String exportLineSeparator = setSeparator(exportLineSeparatorStr);
		
		FileOutputStream fos = new FileOutputStream(file);
		
		OutputStreamWriter osw = new OutputStreamWriter(fos,
				HinemosPropertyCommon.rpa_scenario_operation_result_download_csv_charset.getStringValue());
		PrintWriter pw = new PrintWriter(new BufferedWriter(osw));
		
		setUtf8Bom(fos);
		
		// ヘッダー出力
		pw.print(HinemosMessage.replace(
				MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CSV_HEADER_TAG.getMessage(), locale));
		pw.print(exportLineSeparator);
		
		// カラム出力
		for (RpaScenarioTag tag : tagList){
			pw.print(escapeString(tag.getTagId()) + ",");
			pw.print(escapeString(tag.getTagName()) + ",");
			pw.print(escapeString(tag.getDescription()) + ",");
			pw.print(escapeString(tag.getOwnerRoleId()) + ",");
			pw.print(escapeString(tag.getTagPath()) + ",");
			pw.print(escapeNumber(tag.getRegDate()) + ",");
			pw.print(escapeNumber(tag.getUpdateDate()) + ",");
			pw.print(escapeString(tag.getRegUser()) + ",");
			pw.print(escapeString(tag.getUpdateUser()));
			pw.print(exportLineSeparator);
		}
		
		pw.close();
		fos.close();
		
		return file.getAbsolutePath();
	}
	
	/**
	 * タグリレーションデータをCSVに出力
	 */
	private String exportScenarioTagRelationCsv(List<RpaScenarioTagRelation> tagRelationList, 
			String filePath, Locale locale) throws IOException, HinemosUnknown{
		String fileName = MessageConstant.RPA_SCENARIO_OPERATION_RESULT_DOWNLOAD_CSV_SCENARIO_TAG_RELATION.getMessage();
		File file = new File(filePath + HinemosMessage.replace(fileName, locale));
		
		String exportLineSeparatorStr = 
				HinemosPropertyCommon.rpa_scenario_operation_result_download_csv_line_separator.getStringValue();
		String exportLineSeparator = setSeparator(exportLineSeparatorStr);
		
		FileOutputStream fos = new FileOutputStream(file);
		
		OutputStreamWriter osw = new OutputStreamWriter(fos,
				HinemosPropertyCommon.rpa_scenario_operation_result_download_csv_charset.getStringValue());
		PrintWriter pw = new PrintWriter(new BufferedWriter(osw));
		
		setUtf8Bom(fos);
		
		// ヘッダー出力
		pw.print(HinemosMessage.replace(
				MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CSV_HEADER_TAG_RELATION.getMessage(), locale));
		pw.print(exportLineSeparator);
		
		// カラム出力
		for (RpaScenarioTagRelation tagRelation : tagRelationList){
			pw.print(escapeString(tagRelation.getId().getScenarioId()) + ",");
			pw.print(escapeString(tagRelation.getId().getTagId()));
			pw.print(exportLineSeparator);
		}
		
		pw.close();
		
		return file.getAbsolutePath();
	}
	
	/**
	 * CSVの改行コードを設定
	 */
	private String setSeparator(String exportLineSeparatorStr){
		if ("CRLF".equals(exportLineSeparatorStr)) {
			return "\r\n";
		} else if ("LF".equals(exportLineSeparatorStr)) {
			return "\n";
		} else if ("CR".equals(exportLineSeparatorStr)) {
			return "\r";
		}
		return "\r\n";
	}
	
	/**
	 * CSVにBOMを設定
	 */
	private void setUtf8Bom(FileOutputStream fos) throws IOException {
		boolean UTF8_BOM = HinemosPropertyCommon.rpa_scenario_operation_result_download_csv_bom.getBooleanValue();
		if (UTF8_BOM) {
			fos.write( 0xef );
			fos.write( 0xbb );
			fos.write( 0xbf );
		}
	}
	
	/**
	 * CSVに出力する数値に対するエスケープ処理
	 */
	private Object escapeNumber(Number value){
		if(value == null){
			return "";
		}
		
		return value;
	}
	
	/**
	 * CSVに出力する文字列に対するエスケープ処理
	 */
	private String escapeString(String value){
		if(value == null){
			return "";
		}
		
		String ret = value.replaceAll("\"", "\"\"");
		return "\"" + ret + "\"";
	}
	
	/**
	 * ZIPファイルにまとめてクライアント送信用添付ファイル返却
	 */
	public RestDownloadFile createZipHandler(ArrayList<String> intoZipList, String ouputZipName, String clientName)
			throws HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");

		// マネージャーの出力先ファイルを生成
		String dirName = HinemosPropertyDefault.rpa_scenario_operation_result_export_dir.getStringValue();
		File directory = new File(dirName, clientName);
		dirName = directory.getAbsolutePath();
		File ouputZip = new File(dirName, ouputZipName);
		m_log.debug(methodName + DELIMITER
				+ String.format("create file object to outpu zip. directory=[%s], file=[%s]", dirName, ouputZipName));

		try {
			// ZIP形式に圧縮
			ZipCompressor.archive(intoZipList, ouputZip.getAbsolutePath());
			m_log.debug(
					methodName + DELIMITER + String.format("create the zip. file=[%s]", ouputZip.getAbsolutePath()));
		} catch (HinemosUnknown e) {
			if (ouputZip.exists()) {
				// zipファイル生成済の場合削除
				this.deleteDownloadedRecord(ouputZipName, clientName);
			}
			m_log.warn(methodName + DELIMITER + String.format("faild to create the zip. file=[%s], message=%s",
					ouputZip.getAbsolutePath(), e.getMessage()), e);
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			// 圧縮前のファイルを削除
			for (String intoZipFile : intoZipList) {
				String fileName = new File(intoZipFile).getName();
				this.deleteDownloadedRecord(fileName, clientName);
			}
		}

		m_log.debug(
				methodName + DELIMITER + String.format("return the DataHandler for [%s].", ouputZip.getAbsolutePath()));

		return new RestDownloadFile(ouputZip, ouputZipName);
	}
	
	/**
	 * CSVファイルのダウンロード済レコードを削除
	 */
	public void deleteDownloadedRecord(String fileName, String clientName) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		String dirName = HinemosPropertyDefault.rpa_scenario_operation_result_export_dir.getStringValue();
		File directory = new File(dirName, clientName);
		dirName = directory.getAbsolutePath();
		File file = new File(dirName, fileName);
		if (!file.delete()) {
			m_log.warn(methodName + DELIMITER + String.format("Fail to delete file[%s].", file.getAbsolutePath()));
		} else {
			m_log.debug(methodName + DELIMITER + String.format("Success to delete file[%s].", file.getAbsolutePath()));
		}
		try {
			String[] fileDirArray = directory.list();
			if (fileDirArray == null || fileDirArray.length == 0) {
				if (!directory.delete()) {
					m_log.warn(methodName + DELIMITER + String.format("Fail to delete directory[%s].", dirName));
				} else {
					m_log.debug(methodName + DELIMITER + String.format("Success to delete directory[%s].", dirName));
				}
			} else {
				m_log.debug(methodName + DELIMITER + String.format("Skip to delete directory[%s].", dirName));
			}
		} catch (Exception e) {
			m_log.warn(methodName + DELIMITER + String.format("Fail to delete directory[%s].", dirName), e);
		}
	}
	
	/**
	 * 設定インポート機能によるRPAシナリオタグ情報の変更
	 * 既存の子タグのタグ階層を更新する
	 * 
	 */
	public void importRpaScenarioTagPath(RpaScenarioTag data) throws HinemosUnknown, RpaScenarioTagNotFound, InvalidRole {
		try{
			ModifyRpaScenarioTag rpaScenarioTag = new ModifyRpaScenarioTag();
			
			// タグ階層更新処理
			String userName = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
			List<String> retIdList = rpaScenarioTag.modifyChildTagsPath(data, userName);
			m_log.info("modify RPA Scenario TagPath. tagId=" + String.join(",", retIdList));
		} catch (RpaScenarioTagNotFound | InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("importRpaScenarioTag() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}
	
	/**
	 * 自動化効果計算マスタ一覧を取得する。
	 * 
	 */
	public List<RpaScenarioCoefficientPattern> getRpaScenarioCoefficientPatternList() throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			List<RpaScenarioCoefficientPattern> list = 
					new SelectRpaScenarioCoefficientPattern().getRpaScenarioTagList();
			jtm.commit();
			return list;
		} catch (Exception e){
			m_log.warn("getRpaScenarioCoefficientPatternList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}
	
	/**
	 * 自動化効果計算マスタをマネージャに登録する。
	 * 
	 */
	public RpaScenarioCoefficientPattern addRpaScenarioCoefficientPattern(RpaScenarioCoefficientPattern pattern) 
			throws HinemosUnknown, RpaScenarioCoefficientPatternDuplicate {
		JpaTransactionManager jtm = null;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			ModifyRpaScenarioCoefficientPattern modifier = new ModifyRpaScenarioCoefficientPattern();
			modifier.add(pattern);
			
			jtm.commit();
			return new SelectRpaScenarioCoefficientPattern().getRpaScenarioCoefficientPattern(pattern.getId().getRpaToolEnvId(), pattern.getId().getOrderNo());
		} catch (RpaScenarioCoefficientPatternDuplicate e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("addRpaScenarioCoefficientPattern() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}
	
	/**
	 * 自動化効果計算マスタ情報を削除する
	 */
	public RpaScenarioCoefficientPattern deleteRpaScenarioCoefficientPattern(String rpaToolEnvId, Integer orderNo) throws InvalidRole, HinemosUnknown {
		m_log.debug("deleteRpaScenarioCoefficientPattern");

		JpaTransactionManager jtm = null;
		RpaScenarioCoefficientPattern ret = null;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 自動化効果計算マスタ情報を削除
			ModifyRpaScenarioCoefficientPattern pattern = new ModifyRpaScenarioCoefficientPattern();
			ret = new SelectRpaScenarioCoefficientPattern().getRpaScenarioCoefficientPattern(rpaToolEnvId, orderNo);
			pattern.delete(rpaToolEnvId, orderNo);

			jtm.commit();
		} catch (InvalidRole | HinemosUnknown e){
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("deleteRpaScenarioCoefficientPattern() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		m_log.info("delete RPA Scenario Coefficient Pattern. rptToolEnvId=" + ret.getRpaToolEnvId());
		return ret;
	}
	
	/**
	 * シナリオ設定の実行ノード訂正を行う。
	 * @throws InvalidRole 
	 * @throws RpaScenarioNotFound 
	 * @throws InvalidSetting 
	 * @throws FacilityNotFound 
	 * @throws HinemosUnknown 
	 */
	public void correctExecNode(String scenarioOperationResultCreateSettingId, String scenarioIdentifyString, List<CorrectExecNodeDetailRequest> execNodes) throws InvalidRole, InvalidSetting, HinemosUnknown {
		String facilityId = null;
		String scenarioId = null;
		String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			jtm.begin();
			ModifyRpaScenario modifier = new ModifyRpaScenario();
			SelectRpaScenario selector = new SelectRpaScenario();
			
			for (CorrectExecNodeDetailRequest node : execNodes) {
				facilityId = node.getFacilityId();
				scenarioId = node.getScenarioId();
				// 作成設定ID、ファシリティID、シナリオ識別文字列でシナリオを検索する(変更元のシナリオ)
				RpaScenario oldScenario = QueryUtil.getScenario(scenarioOperationResultCreateSettingId, facilityId, scenarioIdentifyString, null);
				m_log.debug("facilityId=" + facilityId + ", scenarioId=" + scenarioId + ", oldScenario=" + oldScenario);
				if (!scenarioId.isEmpty()) {
					// 変更先のシナリオを検索する。
					RpaScenario newScenario = selector.getRpaScenario(scenarioId);
					// 変更元のシナリオ≠変更先のシナリオの場合、ノード訂正を実行
					if (!newScenario.getScenarioId().equals(oldScenario.getScenarioId())) {
						// ファシリティIDが変更先シナリオのオーナーロールから参照可能かチェック、ノードかどうかチェック
						FacilityTreeCache.validateFacilityId(facilityId, newScenario.getOwnerRoleId(), true);
						// ノード訂正
						oldScenario.removeExecNode(facilityId);
						newScenario.addExecNode(facilityId);
						
						modifier.modifyExecNode(oldScenario, userId);
						modifier.modifyExecNode(newScenario, userId);
					}
					m_log.debug("correct Scenario. oldScenario=" + oldScenario + ", newScenario=" + newScenario);
				} else {
					// シナリオIDが空の場合、変更元のシナリオからノードの削除のみ行う。
					oldScenario.removeExecNode(facilityId);
					modifier.modifyExecNode(oldScenario, userId);
					m_log.debug("correct Scenario. oldScenario=" + oldScenario);
				}
				jtm.flush();
			}
			
			jtm.commit();
		} catch (RpaScenarioNotFound e) {
			throw new InvalidSetting(String.format("%s %s",
					MessageConstant.MESSAGE_FAILED_TO_CORRECT_RPA_SCENARIO_EXECUTION_NODE.getMessage(),
					MessageConstant.MESSAGE_NOT_FOUND.getMessage(MessageConstant.RPA_SCENARIO.getMessage(), scenarioId)
					));
		} catch (InvalidRole e) {
			throw new InvalidRole(MessageConstant.MESSAGE_DO_NOT_HAVE_ENOUGH_PERMISSION.getMessage());
		} catch (FacilityNotFound e) {
			throw new InvalidSetting(String.format("%s %s",
					MessageConstant.MESSAGE_FAILED_TO_CORRECT_RPA_SCENARIO_EXECUTION_NODE.getMessage(),
					MessageConstant.MESSAGE_NOT_FOUND.getMessage(MessageConstant.FACILITY_ID.getMessage(), facilityId)
					));
		}
	}
	
	/**
	 * シナリオ実績更新をスケジュールする。
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	public void updateRpaScenarioOperationResult(UpdateRpaScenarioOperationResultInfo updateInfo) throws InvalidRole, HinemosUnknown, InvalidSetting {
		String notifyId = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			jtm.begin();
			HinemosEntityManager em = jtm.getEntityManager();
			
			// From < Toのチェック
			if (updateInfo.getFromDate() >= updateInfo.getToDate()) {
				String[] args = { MessageConstant.TARGET_PERIOD_TO.getMessage(),
						MessageConstant.TARGET_PERIOD_FROM.getMessage()};
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_LATER_DATE_AND_TIME.getMessage(args));
			}
			
			// 対象時刻が現在時刻以前であることのチェック
			if (updateInfo.getToDate() > HinemosTime.currentTimeMillis()) {
				String[] args = { MessageConstant.TARGET_PERIOD_TO.getMessage(),
						MessageConstant.CURRENT_DATETIME.getMessage()};
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_EARLIER_DATE_AND_TIME.getMessage(args));
			}

			// 操作ユーザが実績作成設定IDうを参照可能かチェック
			getRpaScenarioOperationResultCreateSetting(updateInfo.getScenarioOperationResultCreateSettingId());
			
			// 通知IDの存在チェック、設定
			if (!updateInfo.getNotifyId().isEmpty()) {
				String notifyGroupId = NotifyGroupIdGenerator.generate(updateInfo);
				updateInfo.setNotifyGroupId(notifyGroupId);

				for(NotifyRelationInfo notifyInfo : updateInfo.getNotifyId()){
					// 操作ユーザが通知IDを参照可能かチェック
					notifyId = notifyInfo.getNotifyId();
					new SelectNotify().getNotify(notifyId);
					notifyInfo.setNotifyGroupId(notifyGroupId);
					notifyInfo.setFunctionPrefix(FunctionPrefixEnum.RPA_SCENARIO_CORRECT.name());
				}
				// 通知情報を登録
				new ModifyNotifyRelation().add(updateInfo.getNotifyId());
			}

			// レコード数チェック
			long maxNum = HinemosPropertyCommon.rpa_update_scenario_operation_result_maxsize.getNumericValue();
			// 対象レコード数を取得
			long numberOfTargetResults = QueryUtil.countUpdateTargetResults(
					updateInfo.getFromDate(),
					updateInfo.getToDate(),
					updateInfo.getScenarioIdentifyString(), 
					updateInfo.getScenarioOperationResultCreateSettingId());
			
			// 更新実行中(スケジュール中)のシナリオ実績レコード数を取得
			long numberOfTotalUpdatingResults = QueryUtil.countTotalUpdatingOperationResults();
			
			m_log.debug("numberOfTargetResults = " + numberOfTargetResults);
			if (numberOfTargetResults == 0) {
				// 対象レコードが0件の場合はエラー
				throw new InvalidSetting(MessageConstant.MESSAGE_UPDATE_NUMBER_OF_OPERATION_RESULTS_IS_ZERO.getMessage());
			} else if (numberOfTargetResults > maxNum) {
				// 対象レコード数が最大サイズを超えていたらエラー
				throw new InvalidSetting(MessageConstant.MESSAGE_UPDATE_NUMBER_OF_OPERATION_RESULTS_IS_OVER.getMessage(
						String.valueOf(maxNum)
						, String.valueOf(numberOfTargetResults)
						));
			} else if (numberOfTotalUpdatingResults + numberOfTargetResults > maxNum) {
				// スケジュール済のシナリオ実績更新の合計更新レコードが最大サイズを超えたらエラー
				throw new InvalidSetting(MessageConstant.MESSAGE_UPDATE_TOTAL_NUMBER_OF_OPERATION_RESULTS_IS_OVER.getMessage(
						String.valueOf(maxNum)
						, String.valueOf(numberOfTargetResults)
						, String.valueOf(numberOfTotalUpdatingResults)
						));
			} else {
				updateInfo.setNumberOfTargetRecords(numberOfTargetResults);
			}
			updateInfo.setModifyUserId((String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));
			// 登録
			em.persist(updateInfo);
			// コミット後にキャッシュをリフレッシュ
			jtm.addCallback(new NotifyRelationCacheRefreshCallback());
			jtm.commit();
		} catch (NotifyNotFound e) {
			throw new InvalidSetting(String.format("%s %s",
					MessageConstant.MESSAGE_FAILED_TO_UPDATE_RPA_SCENARIO_OPERATION_RESULT.getMessage(),
					MessageConstant.MESSAGE_NOT_FOUND.getMessage(MessageConstant.NOTIFY.getMessage(), notifyId)
					));
		} catch (InvalidRole e) {
			throw new InvalidRole(MessageConstant.MESSAGE_DO_NOT_HAVE_ENOUGH_PERMISSION.getMessage());
		} catch (HinemosUnknown e) {
			throw new HinemosUnknown(MessageConstant.MESSAGE_UNEXPECTED_ERR_OCCURRED.getMessage(e.getMessage()));
		} catch (RpaScenarioOperationResultCreateSettingNotFound e) {
			throw new InvalidSetting(String.format("%s %s",
					MessageConstant.MESSAGE_FAILED_TO_UPDATE_RPA_SCENARIO_OPERATION_RESULT.getMessage(),
					MessageConstant.MESSAGE_NOT_FOUND.getMessage(MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CREATE_SETTING_ID.getMessage(), updateInfo.getScenarioOperationResultCreateSettingId())
					));
		}
	}
	
	/**
	 * スケジュール済のシナリオ実績更新情報を返す。
	 */
	public List<UpdateRpaScenarioOperationResultInfo> getUpdateRpaScenarioOperationResultInfoList() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			jtm.begin();
			List<UpdateRpaScenarioOperationResultInfo> ret = QueryUtil.getUpdateRpaScenarioOperationResultInfoList(); 
			jtm.commit();
			return ret;
		}
	}
	
	/**
	 * シナリオ実績更新情報を元に、現在のシナリオ設定の実行ノード情報を更新対象のシナリオ実績に反映する。
	 */
	public void updateUpdateRpaScenarioOperationResults(UpdateRpaScenarioOperationResultInfo updateInfo) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			jtm.begin();

			// 更新対象のシナリオ実績を取得
			List<RpaScenarioOperationResult> targetResultList = QueryUtil.findUpdateTargetResults(
					updateInfo.getFromDate(),
					updateInfo.getToDate(),
					updateInfo.getScenarioIdentifyString(), 
					updateInfo.getScenarioOperationResultCreateSettingId());
			
			// 現在のシナリオ設定の実行ノード一覧を取得
			List<RpaScenarioExecNode> execNodes = QueryUtil.getRpaScenarioExecNodeList(
					updateInfo.getScenarioIdentifyString(),
					updateInfo.getScenarioOperationResultCreateSettingId());
			// <ファシリティID, シナリオID>のマップを作成
			Map<String, String> execNodeMap = execNodes.stream().collect(
					Collectors.toMap(RpaScenarioExecNode::getFacilityId, RpaScenarioExecNode::getScenarioId));
			
			// シナリオ実績更新
			for(RpaScenarioOperationResult targetResult: targetResultList) {
				targetResult.setScenarioId(execNodeMap.get(targetResult.getFacilityId()));
			}

			// 更新完了を通知
			if (updateInfo.getNotifyGroupId() != null) {
				jtm.addCallback(new NotifyCallback(ScenarioOperationResultUpdater.notify(updateInfo, targetResultList.size())));
			}
			jtm.commit();
		}
	}

	/**
	 * シナリオ実績更新情報を削除する。
	 * @throws HinemosUnknown 
	 * @throws InvalidRole 
	 */
	public UpdateRpaScenarioOperationResultInfo deleteUpdateRpaScenarioOperationResultInfo(long updateId) throws InvalidRole, HinemosUnknown {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			jtm.begin();
			HinemosEntityManager em = jtm.getEntityManager();
			
			UpdateRpaScenarioOperationResultInfo entity = QueryUtil.getUpdateRpaScenarioOperationResultInfoPK(updateId);
			NotifyControllerBean notifyControllerBean = new NotifyControllerBean();
			// 通知グループ情報を削除
			if (entity.getNotifyGroupId() != null) {
				notifyControllerBean.deleteNotifyRelation(entity.getNotifyGroupId());
			}
			
			// 通知履歴情報を削除する
			notifyControllerBean.deleteNotifyHistory(HinemosModuleConstant.RPA_SCENARIO_CORRECT, entity.getScenarioOperationResultCreateSettingId());

			// シナリオ実績更新情報を削除する。
			em.remove(entity);

			jtm.commit();
			return entity;
		}
		
	}
	
	/**
	 * ファシリティが利用されているか確認する。
	 * 
	 * @throws UsedFacility
	 * @throws InvalidRole
	 */
	@Override
	public void isUseFacilityId(String facilityId) throws UsedFacility {
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			List<RpaScenarioOperationResultCreateSetting> infoCollection
					= QueryUtil.getRpaScenarioCreateSettingListFindByFacilityId_NONE(facilityId);
			if (infoCollection != null && infoCollection.size() > 0) {
				// ID名を取得する
				StringBuilder sb = new StringBuilder();
				sb.append(MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CREATE_SETTING.getMessage() + " : ");
				for(RpaScenarioOperationResultCreateSetting info : infoCollection) {
					sb.append(info.getScenarioOperationResultCreateSettingId());
					sb.append(", ");
				}
				UsedFacility e = new UsedFacility(sb.toString());
				m_log.info("isUseFacilityId() : " + e.getClass().getSimpleName() +
						", " + facilityId + ", " + e.getMessage());
				throw e;
			}
			jtm.commit();
		} catch (UsedFacility e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("isUseFacilityId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}
}
