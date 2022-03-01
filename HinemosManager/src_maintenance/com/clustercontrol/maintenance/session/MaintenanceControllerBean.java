/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.session;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.util.RoleValidator;
import com.clustercontrol.bean.FunctionPrefixEnum;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.MaintenanceDuplicate;
import com.clustercontrol.fault.MaintenanceNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.maintenance.factory.MaintenanceCollectBinaryData;
import com.clustercontrol.maintenance.factory.MaintenanceCollectDataRaw;
import com.clustercontrol.maintenance.factory.MaintenanceCollectStringData;
import com.clustercontrol.maintenance.factory.MaintenanceEvent;
import com.clustercontrol.maintenance.factory.MaintenanceJob;
import com.clustercontrol.maintenance.factory.MaintenanceJobLinkMessage;
import com.clustercontrol.maintenance.factory.MaintenanceNodeConfigSettingHistory;
import com.clustercontrol.maintenance.factory.MaintenanceRpaScenarioOperationResult;
import com.clustercontrol.maintenance.factory.MaintenanceSummaryDay;
import com.clustercontrol.maintenance.factory.MaintenanceSummaryHour;
import com.clustercontrol.maintenance.factory.MaintenanceSummaryMonth;
import com.clustercontrol.maintenance.factory.ModifyMaintenance;
import com.clustercontrol.maintenance.factory.ModifySchedule;
import com.clustercontrol.maintenance.factory.OperationMaintenance;
import com.clustercontrol.maintenance.factory.SelectMaintenanceInfo;
import com.clustercontrol.maintenance.factory.SelectMaintenanceTypeMst;
import com.clustercontrol.maintenance.model.MaintenanceInfo;
import com.clustercontrol.maintenance.model.MaintenanceTypeMst;
import com.clustercontrol.maintenance.util.MaintenanceValidator;
import com.clustercontrol.maintenance.util.QueryUtil;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.notify.util.NotifyRelationCacheRefreshCallback;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.apllog.AplLogger;

import jakarta.persistence.EntityExistsException;

/**
 * 
 * メンテナンス機能を管理する Session Bean です。<BR>
 * 
 * @version 6.1.0 バイナリ収集データ削除を追加.
 */
public class MaintenanceControllerBean {

	private static Log m_log = LogFactory.getLog( MaintenanceControllerBean.class );

	/**
	 * メンテナンス情報を追加します。
	 * 
	 * @throws HinemosUnknown
	 * @throws MaintenanceDuplicate
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public MaintenanceInfo addMaintenance(MaintenanceInfo maintenanceInfo)
			throws HinemosUnknown, MaintenanceDuplicate, InvalidSetting, InvalidRole {
		m_log.debug("addMaintenance");

		JpaTransactionManager jtm = null;
		String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

		// メイン処理
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			maintenanceInfo.setNotifyGroupId(NotifyGroupIdGenerator.generate(maintenanceInfo));
			for (NotifyRelationInfo notify : maintenanceInfo.getNotifyId()) {
				notify.setNotifyGroupId(NotifyGroupIdGenerator.generate(maintenanceInfo));
				notify.setFunctionPrefix(FunctionPrefixEnum.MAINTENANCE.name());
			}
			// 入力チェック
			MaintenanceValidator.validateMaintenanceInfo(maintenanceInfo, false);
			
			//ユーザがオーナーロールIDに所属しているかチェック
			RoleValidator.validateUserBelongRole(maintenanceInfo.getOwnerRoleId(),
					(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),
					(Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR));

			// メンテナンス情報を登録
			ModifyMaintenance maintenance = new ModifyMaintenance();
			maintenance.addMaintenance(maintenanceInfo, loginUser);

			// Quartzへ登録
			ModifySchedule modify = new ModifySchedule();
			modify.addSchedule(maintenanceInfo, loginUser);

			// コミット後にキャッシュクリアを行うコールバック
			jtm.addCallback(new NotifyRelationCacheRefreshCallback());
			jtm.commit();

			return getMaintenanceInfo(maintenanceInfo.getMaintenanceId());
		} catch (HinemosUnknown | InvalidSetting | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (EntityExistsException e) {
			if (jtm != null)
				jtm.rollback();
			throw new MaintenanceDuplicate(e.getMessage(), e);
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("addMaintenance() : "
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
	 * メンテナンス情報を変更します。
	 * 
	 * @throws HinemosUnknown
	 * @throws NotifyNotFound
	 * @throws MaintenanceNotFound
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public MaintenanceInfo modifyMaintenance(MaintenanceInfo maintenanceInfo) throws HinemosUnknown, NotifyNotFound, MaintenanceNotFound, InvalidSetting, InvalidRole {
		m_log.debug("modifyMaintenance");

		JpaTransactionManager jtm = null;
		String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

		// メイン処理
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			maintenanceInfo.setNotifyGroupId(NotifyGroupIdGenerator.generate(maintenanceInfo));

			for (NotifyRelationInfo notify : maintenanceInfo.getNotifyId()) {
				notify.setNotifyGroupId(NotifyGroupIdGenerator.generate(maintenanceInfo));
				notify.setFunctionPrefix(FunctionPrefixEnum.MAINTENANCE.name());
			}
			
			MaintenanceInfo entity = QueryUtil.getMaintenanceInfoPK(maintenanceInfo.getMaintenanceId());
			maintenanceInfo.setOwnerRoleId(entity.getOwnerRoleId());
			
			// 入力チェック
			MaintenanceValidator.validateMaintenanceInfo(maintenanceInfo, true);

			// メンテナンス情報を登録
			ModifyMaintenance maintenance = new ModifyMaintenance();
			maintenance.modifyMaintenance(maintenanceInfo, loginUser);

			// Quartzへ登録
			ModifySchedule modifySchedule = new ModifySchedule();
			modifySchedule.addSchedule(maintenanceInfo, loginUser);

			// コミット後にキャッシュクリアを行うコールバック
			jtm.addCallback(new NotifyRelationCacheRefreshCallback());
			jtm.commit();

			
			return getMaintenanceInfo(maintenanceInfo.getMaintenanceId());
		} catch (InvalidSetting | MaintenanceNotFound | NotifyNotFound | InvalidRole | HinemosUnknown e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("modifyMaintenance() : "
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
	 * メンテナンス情報を削除します。
	 * @throws HinemosUnknown
	 * @throws MaintenanceNotFound
	 * @throws InvalidRole
	 * 
	 */
	public List<MaintenanceInfo> deleteMaintenance(List<String> maintenanceIdList) throws HinemosUnknown, MaintenanceNotFound, InvalidRole {
		m_log.debug("deleteMaintenance");

		JpaTransactionManager jtm = null;
		List<MaintenanceInfo> rtn = new ArrayList<>();
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			
			
			for(String maintenanceId : maintenanceIdList) {
				rtn.add(getMaintenanceInfo(maintenanceId));
				// メンテナンス情報を削除
				ModifyMaintenance maintenance = new ModifyMaintenance();
				maintenance.deleteMaintenance(maintenanceId);

				ModifySchedule modify = new ModifySchedule();
				modify.deleteSchedule(maintenanceId);
			}
			
			// コミット後にキャッシュクリアを行うコールバック
			jtm.addCallback(new NotifyRelationCacheRefreshCallback());
			jtm.commit();

			
		} catch (MaintenanceNotFound | InvalidRole | HinemosUnknown e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("deleteMaintenance() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return rtn;
	}


	/**
	 * メンテナンス情報を取得します。
	 *
	 * @return
	 * @throws MaintenanceNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 */
	public MaintenanceInfo getMaintenanceInfo(String maintenanceId) throws MaintenanceNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("getMaintenanceInfo()");

		JpaTransactionManager jtm = null;
		SelectMaintenanceInfo select = new SelectMaintenanceInfo();
		MaintenanceInfo info;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			info = select.getMaintenanceInfo(maintenanceId);
			jtm.commit();
		} catch (MaintenanceNotFound | InvalidRole | HinemosUnknown e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getMaintenanceInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return info;
	}

	/**
	 * メンテナンス情報の一覧を取得します。<BR>
	 * 
	 * @return メンテナンス情報の一覧を保持するArrayList
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 */
	public ArrayList<MaintenanceInfo> getMaintenanceList() throws InvalidRole, HinemosUnknown {
		m_log.debug("getMaintenanceList()");

		JpaTransactionManager jtm = null;
		SelectMaintenanceInfo select = new SelectMaintenanceInfo();
		ArrayList<MaintenanceInfo> list;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = select.getMaintenanceList();
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
			m_log.warn("getMaintenanceList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return list;

	}

	/**
	 * メンテナンス種別の一覧を取得します。<BR>
	 * 下記のようにして生成されるArrayListを、要素として持つArrayListが一覧として返されます。
	 * 
	 * <p>
	 * MaintenanceTypeMstEntity mst = (MaintenanceTypeMstEntity)itr.next();
	 * ArrayList info = new ArrayList();
	 * info.add(mst.getType_id());
	 * info.add(mst.getName_id());
	 * info.add(mst.getOrder_no());
	 * ist.add(info);
	 * </p>
	 * 
	 * @return メンテナンス種別の一覧を保持するArrayList
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public ArrayList<MaintenanceTypeMst> getMaintenanceTypeList() throws InvalidRole, HinemosUnknown {
		m_log.debug("getMaintenanceTypeList()");

		JpaTransactionManager jtm = null;
		SelectMaintenanceTypeMst select = new SelectMaintenanceTypeMst();
		ArrayList<MaintenanceTypeMst> list;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = select.getMaintenanceTypeList();
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getMaintenanceTypeList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**
	 * 
	 * メンテナンスの有効、無効を変更するメソッドです。
	 * @throws HinemosUnknown
	 * @throws MaintenanceNotFound
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 */
	public MaintenanceInfo setMaintenanceStatus(String maintenanceId, boolean validFlag) throws NotifyNotFound, MaintenanceNotFound, InvalidRole, HinemosUnknown{
		m_log.debug("setMaintenanceStatus() : maintenanceId=" + maintenanceId + ", validFlag=" + validFlag);
		// null check
		if(maintenanceId == null || "".equals(maintenanceId)){
			HinemosUnknown e = new HinemosUnknown("target maintenanceId is null or empty.");
			m_log.info("setMaintenanceStatus() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		MaintenanceInfo info = this.getMaintenanceInfo(maintenanceId);
		info.setValidFlg(validFlag);

		try{
			this.modifyMaintenance(info);
			return getMaintenanceInfo(maintenanceId);
		} catch (InvalidSetting  e) {
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * イベントログを削除します。
	 * 
	 * @param dataRetentionPeriod 保存期間(日)
	 * @param status 削除対象のステータス(true=全イベント、false=確認済みイベント)
	 * @param ownerRoleId オーナーロールID
	 * @param maintenanceId メンテナンスID
	 * @return 削除件数
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * 時間を要する処理のため、NotSupportedを採用してJTAの管理下から除外する
	 */
	public int deleteEventLog(int dataRetentionPeriod, boolean status, String ownerRoleId, String maintenanceId) throws InvalidRole, HinemosUnknown {
		m_log.debug("deleteEventLog() : dataRetentionPeriod = " + dataRetentionPeriod + ", status = " + status + ", ownerRoleId = " + ownerRoleId);

		MaintenanceEvent event = new MaintenanceEvent();
		int ret = 0;
		try{
			ret = event.delete(dataRetentionPeriod, status, ownerRoleId, maintenanceId);
		} catch (ObjectPrivilege_InvalidRole e) {
			throw new InvalidRole(e.getMessage(), e);
		} catch(Exception e){
			m_log.warn("deleteEventLog() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		return ret;
	}


	/**
	 * ジョブ実行履歴を削除します。
	 * 
	 * @param dataRetentionPeriod 保存期間(日)
	 * @param status 削除対象のステータス(true=全履歴、false=実行状態が「終了」または「変更済み」の履歴)
	 * @param ownerRoleId オーナーロールID
	 * @param maintenanceId メンテナンスID
	 * @return 削除件数
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * 時間を要する処理のため、NotSupportedを採用してJTAの管理下から除外する
	 */
	public int deleteJobHistory(int dataRetentionPeriod, boolean status, String ownerRoleId, String maintenanceId) throws InvalidRole, HinemosUnknown {
		m_log.debug("deleteJobHistory() : dataRetentionPeriod = " + dataRetentionPeriod + ", status = " + status + ", ownerRoleId = " + ownerRoleId);

		MaintenanceJob job = new MaintenanceJob();
		int ret = 0;

		try{
			ret = job.delete(dataRetentionPeriod, status, ownerRoleId, maintenanceId);

		} catch (ObjectPrivilege_InvalidRole e) {
			throw new InvalidRole(e.getMessage(), e);
		} catch(Exception e){
			m_log.warn("deleteJobHistory() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		return ret;
	}

	/**
	 * 収集データ(raw)を削除します。
	 * 
	 * @param dataRetentionPeriod 保存期間(日)
	 * @param status 削除対象のステータス(性能実績は常にtrue=全履歴)
	 * @param ownerRoleId オーナーロールID
	 * @param maintenanceId メンテナンスID
	 * @return 削除件数
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * 時間を要する処理のため、NotSupportedを採用してJTAの管理下から除外する
	 * 
	 */
	public int deleteCollectData(int dataRetentionPeriod, boolean status, String ownerRoleId, String maintenanceId) throws InvalidRole, HinemosUnknown {
		m_log.debug("deleteCollectData() : dataRetentionPeriod = " + dataRetentionPeriod + ", status = " + status + ", ownerRoleId = " + ownerRoleId);
		MaintenanceCollectDataRaw collectdata = new MaintenanceCollectDataRaw();
		int ret = 0;
		try{
			ret = collectdata.delete(dataRetentionPeriod, status, ownerRoleId, maintenanceId);
		} catch (ObjectPrivilege_InvalidRole e) {
			throw new InvalidRole(e.getMessage(), e);
		} catch(Exception e){
			m_log.warn("deleteCollectData() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		return ret;
	}
	
	/**
	 * サマリデータ(時間)を削除します。
	 * 
	 * @param dataRetentionPeriod 保存期間(日)
	 * @param status 削除対象のステータス(性能実績は常にtrue=全履歴)
	 * @param ownerRoleId オーナーロールID
	 * @param maintenanceId メンテナンスID
	 * @return 削除件数
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * 時間を要する処理のため、NotSupportedを採用してJTAの管理下から除外する
	 * 
	 */
	public int deleteSummaryHour(int dataRetentionPeriod, boolean status, String ownerRoleId, String maintenanceId) throws InvalidRole, HinemosUnknown {
		m_log.debug("deleteSummaryHour() : dataRetentionPeriod = " + dataRetentionPeriod + ", status = " + status + ", ownerRoleId = " + ownerRoleId);
		MaintenanceSummaryHour summaryhour = new MaintenanceSummaryHour();
		int ret = 0;
		try{
			ret = summaryhour.delete(dataRetentionPeriod, status, ownerRoleId, maintenanceId);
		} catch (ObjectPrivilege_InvalidRole e) {
			throw new InvalidRole(e.getMessage(), e);
		} catch(Exception e){
			m_log.warn("deleteSummaryHour() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		return ret;
	}
	
	
	/**
	 * サマリデータ(日)を削除します。
	 * 
	 * @param dataRetentionPeriod 保存期間(日)
	 * @param status 削除対象のステータス(性能実績は常にtrue=全履歴)
	 * @param ownerRoleId オーナーロールID
	 * @param maintenanceId メンテナンスID
	 * @return 削除件数
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * 時間を要する処理のため、NotSupportedを採用してJTAの管理下から除外する
	 * 
	 */
	public int deleteSummaryDay(int dataRetentionPeriod, boolean status, String ownerRoleId, String maintenanceId) throws InvalidRole, HinemosUnknown {
		m_log.debug("deleteSummaryDay() : dataRetentionPeriod = " + dataRetentionPeriod + ", status = " + status + ", ownerRoleId = " + ownerRoleId);
		MaintenanceSummaryDay summaryday = new MaintenanceSummaryDay();
		int ret = 0;
		try{
			ret = summaryday.delete(dataRetentionPeriod, status, ownerRoleId, maintenanceId);
		} catch (ObjectPrivilege_InvalidRole e) {
			throw new InvalidRole(e.getMessage(), e);
		} catch(Exception e){
			m_log.warn("deleteSummaryDay() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		return ret;
	}
	
	
	/**
	 * サマリデータ(月)を削除します。
	 * 
	 * @param dataRetentionPeriod 保存期間(日)
	 * @param status 削除対象のステータス(性能実績は常にtrue=全履歴)
	 * @param ownerRoleId オーナーロールID
	 * @param maintenanceId メンテナンスID
	 * @return 削除件数
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * 時間を要する処理のため、NotSupportedを採用してJTAの管理下から除外する
	 * 
	 */
	public int deleteSummaryMonth(int dataRetentionPeriod, boolean status, String ownerRoleId, String maintenanceId) throws InvalidRole, HinemosUnknown {
		m_log.debug("deleteSummaryMonth() : dataRetentionPeriod = " + dataRetentionPeriod + ", status = " + status + ", ownerRoleId = " + ownerRoleId);
		MaintenanceSummaryMonth summarymonth = new MaintenanceSummaryMonth();
		int ret = 0;
		try{
			ret = summarymonth.delete(dataRetentionPeriod, status, ownerRoleId, maintenanceId);
		} catch (ObjectPrivilege_InvalidRole e) {
			throw new InvalidRole(e.getMessage(), e);
		} catch(Exception e){
			m_log.warn("deleteSummaryMonth() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		return ret;
	}
	
	/**
	 * RPAシナリオ実績を削除します。
	 * 
	 * @param dataRetentionPeriod 保存期間(日)
	 * @param status 削除対象のステータス(シナリオ実績は常にtrue=全履歴)
	 * @param ownerRoleId オーナーロールID
	 * @param maintenanceId メンテナンスID
	 * @return 削除件数
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * 時間を要する処理のため、NotSupportedを採用してJTAの管理下から除外する
	 */
	public int deleteRpaScenarioOperationResult(int dataRetentionPeriod, boolean status, String ownerRoleId, String maintenanceId) throws InvalidRole, HinemosUnknown {
		m_log.debug("deleteEventLog() : dataRetentionPeriod = " + dataRetentionPeriod + ", status = " + status + ", ownerRoleId = " + ownerRoleId);

		MaintenanceRpaScenarioOperationResult result = new MaintenanceRpaScenarioOperationResult();
		int ret = 0;
		try{
			ret = result.delete(dataRetentionPeriod, status, ownerRoleId, maintenanceId);
		} catch (ObjectPrivilege_InvalidRole e) {
			throw new InvalidRole(e.getMessage(), e);
		} catch(Exception e){
			m_log.warn("deleteEventLog() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		return ret;
	}

	
	


	/**
	 * 
	 * メンテナンス機能をスケジュール実行します。<BR>
	 * Quartzからスケジュール実行時に呼び出されます。
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * トランザクション開始はユーザが制御する。
	 * また、追加実装により、トランザクションの入れ子が予期せず生じることを避けるため、Neverを採用する。
	 */
	public void scheduleRunMaintenance(String maintenanceId, String calendarId) throws InvalidRole, HinemosUnknown {
		m_log.debug("scheduleRunMaintenance() : maintenanceId=" + maintenanceId + ", calendarId=" + calendarId);

		JpaTransactionManager jtm = null;
		OutputBasicInfo notifyInfo = null;
		try {
			// トランザクションがすでに開始されている場合は処理終了
			jtm = new JpaTransactionManager();
			if (jtm.isNestedEm()) {
				m_log.error("Transaction has already started.");
				return;
			}

			//カレンダをチェック
			boolean check = false;
			if(calendarId != null && calendarId.length() > 0){
				//カレンダによる実行可/不可のチェック
				if(new CalendarControllerBean().isRun(calendarId, HinemosTime.getDateInstance().getTime()).booleanValue()){
					check = true;
				}
			}
			else{
				check = true;
			}

			if(!check)
				return;

			//メンテナンス実行
			notifyInfo = runMaintenance(maintenanceId);

			if (notifyInfo != null) {
				// 通知設定
				List<OutputBasicInfo> notifyInfoList = new ArrayList<>();
				notifyInfoList.add(notifyInfo);
				NotifyControllerBean.notify(notifyInfoList);
			}
		} catch (CalendarNotFound e) {
			throw new HinemosUnknown(e.getMessage(),e);
		} catch (ObjectPrivilege_InvalidRole e) {
			throw new InvalidRole(e.getMessage(), e);
		} catch (HinemosUnknown e) {
			throw e;
		} catch(Exception e){
			m_log.warn("scheduleRunMaintenance() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
			if (notifyInfo == null) {
				// AplLoggerによるINTERNALイベント
				String[] args = {maintenanceId};
				AplLogger.put(InternalIdCommon.MAINTENANCE_SYS_001, args);
			}
		}
	}

	/**
	 * 
	 * メンテナンス機能を実行するメソッドです。
	 * 
	 * @param maintenanceId
	 * @return 通知情報
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public OutputBasicInfo runMaintenance(String maintenanceId) throws InvalidRole, HinemosUnknown {
		m_log.debug("runMaintenance() : maintenanceId=" + maintenanceId);

		OperationMaintenance operation = new OperationMaintenance();
		OutputBasicInfo rtn = null;

		try {

			rtn = operation.runMaintenance(maintenanceId);

		} catch (ObjectPrivilege_InvalidRole e) {
			throw new InvalidRole(e.getMessage(), e);
		} catch(Exception e){
			m_log.warn("runMaintenance() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
		return rtn;
	}
	
	
	
	/**
	 * 収集蓄積情報を削除します。
	 * 
	 * @param dataRetentionPeriod 保存期間(日)
	 * @param status 削除対象のステータス(性能実績は常にtrue=全履歴)
	 * @param ownerRoleId オーナーロールID
	 * @param maintenanceId メンテナンスID
	 * @return 削除件数
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * 時間を要する処理のため、NotSupportedを採用してJTAの管理下から除外する
	 * 
	 */
	public int deleteCollectStringData(int dataRetentionPeriod, boolean status, String ownerRoleId, String maintenanceId) throws InvalidRole, HinemosUnknown {
		m_log.debug("deleteCollectStringData() : dataRetentionPeriod = " + dataRetentionPeriod + ", status = " + status + ", ownerRoleId = " + ownerRoleId);
		MaintenanceCollectStringData stringData = new MaintenanceCollectStringData();
		int ret = 0;
		try{
			ret = stringData.delete(dataRetentionPeriod, status, ownerRoleId, maintenanceId);
		} catch (ObjectPrivilege_InvalidRole e) {
			throw new InvalidRole(e.getMessage(), e);
		} catch(Exception e){
			m_log.warn("deleteCollectStringData() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		return ret;
	}

	/**
	 * 収集蓄積情報(バイナリ)を削除します。
	 * 
	 * @param dataRetentionPeriod 保存期間(日)
	 * @param status 削除対象のステータス(性能実績は常にtrue=全履歴)
	 * @param ownerRoleId オーナーロールID
	 * @param maintenanceId メンテナンスID
	 * @return 削除件数
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * 時間を要する処理のため、NotSupportedを採用してJTAの管理下から除外する
	 * 
	 */
	public int deleteCollectBinaryData(int dataRetentionPeriod, boolean status, String ownerRoleId, String maintenanceId) throws InvalidRole, HinemosUnknown {
		return deleteCollectBinaryData(dataRetentionPeriod, status, ownerRoleId, maintenanceId, null);
	}

	/**
	 * 収集蓄積情報(バイナリ)を削除します。
	 * 
	 * @param dataRetentionPeriod 保存期間(日)
	 * @param status 削除対象のステータス(性能実績は常にtrue=全履歴)
	 * @param ownerRoleId オーナーロールID
	 * @param maintenanceId メンテナンスID
	 * @param monitorTypeId 監視種別ID
	 * @return 削除件数
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * 時間を要する処理のため、NotSupportedを採用してJTAの管理下から除外する
	 * 
	 */
	public int deleteCollectBinaryData(int dataRetentionPeriod, boolean status, String ownerRoleId, String maintenanceId, String monitorTypeId) throws InvalidRole, HinemosUnknown {
		m_log.debug("deleteCollectBinaryData() : dataRetentionPeriod = " + dataRetentionPeriod + ", status = " + status + ", ownerRoleId = " + ownerRoleId + ", monitorTypeId = " + monitorTypeId);
		MaintenanceCollectBinaryData binData = null;
		if (monitorTypeId == null) {
			binData = new MaintenanceCollectBinaryData();
		} else {
			binData = new MaintenanceCollectBinaryData(monitorTypeId);
		}
		int ret = 0;
		try{
			ret = binData.delete(dataRetentionPeriod, status, ownerRoleId, maintenanceId);
		} catch (ObjectPrivilege_InvalidRole e) {
			throw new InvalidRole(e.getMessage(), e);
		} catch(Exception e){
			m_log.warn("deleteCollectStringData() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		return ret;
	}

	/**
	 * 構成情報履歴を削除します。
	 * 
	 * @param dataRetentionPeriod 保存期間(日)
	 * @param status 削除対象のステータス(性能実績は常にtrue=全履歴)
	 * @param ownerRoleId オーナーロールID
	 * @param maintenanceId メンテナンスID
	 * @return 削除件数
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * 時間を要する処理のため、NotSupportedを採用してJTAの管理下から除外する
	 * 
	 */
	public int deleteNodeConfigSettingHistory(int dataRetentionPeriod, boolean status, String ownerRoleId, String maintenanceId) throws InvalidRole, HinemosUnknown {
		m_log.debug("deleteNodeConfigSettingHistory() : dataRetentionPeriod = " + dataRetentionPeriod + ", status = " + status + ", ownerRoleId = " + ownerRoleId);
		int ret = 0;
		try{
			ret = new MaintenanceNodeConfigSettingHistory().delete(dataRetentionPeriod, status, ownerRoleId, maintenanceId);
		} catch (ObjectPrivilege_InvalidRole e) {
			throw new InvalidRole(e.getMessage(), e);
		} catch(Exception e){
			m_log.warn("deleteNodeConfigSettingHistory() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		return ret;
	}

	/**
	 * ジョブ連携メッセージ情報を削除します。
	 * 
	 * @param dataRetentionPeriod 保存期間(日)
	 * @param status 削除対象のステータス(性能実績は常にtrue=全履歴)
	 * @param ownerRoleId オーナーロールID
	 * @param maintenanceId メンテナンスID
	 * @return 削除件数
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * 時間を要する処理のため、NotSupportedを採用してJTAの管理下から除外する
	 * 
	 */
	public int deleteJobLinkMessage(int dataRetentionPeriod, boolean status, String ownerRoleId, String maintenanceId) throws InvalidRole, HinemosUnknown {
		m_log.debug("deleteJobLinkMessage() : dataRetentionPeriod = " + dataRetentionPeriod + ", status = " + status + ", ownerRoleId = " + ownerRoleId);
		int ret = 0;
		try{
			ret = new MaintenanceJobLinkMessage().delete(dataRetentionPeriod, status, ownerRoleId, maintenanceId);
		} catch (ObjectPrivilege_InvalidRole e) {
			throw new InvalidRole(e.getMessage(), e);
		} catch(Exception e){
			m_log.warn("deleteJobLinkMessage() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		return ret;
	}
}
