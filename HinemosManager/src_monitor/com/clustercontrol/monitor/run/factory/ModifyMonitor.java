/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.monitor.run.factory;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.scheduler.TriggerSchedulerException;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.notify.factory.ModifyNotifyRelation;
import com.clustercontrol.notify.model.MonitorStatusEntity;
import com.clustercontrol.notify.model.NotifyHistoryEntity;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.notify.util.MonitorStatusCache;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;
import com.clustercontrol.util.HinemosTime;


/**
 * 監視情報を変更する抽象クラスです。
 * <p>
 * 監視種別（真偽値，数値，文字列）の各クラスで継承してください。
 *
 * @version 4.0.0
 * @since 2.0.0
 */
abstract public class ModifyMonitor {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( ModifyMonitor.class );

	/** 監視情報のエンティティ */
	protected MonitorInfo m_monitor;

	/** 監視情報。 */
	protected MonitorInfo m_monitorInfo;

	/** ファシリティ変更フラグ。 */
	protected boolean m_isModifyFacilityId = false;

	/** 実行間隔変更フラグ。 */
	private boolean m_isModifyRunInterval = false;

	/** 有効への変更フラグ。 */
	private boolean m_isModifyEnableFlg = false;

	/** 監視対象ID */
	private String m_monitorTypeId;

	/** 監視項目ID */
	private String m_monitorId;

	/**
	 * スケジュール実行種別を返します。
	 */
	protected abstract TriggerType getTriggerType();

	/**
	 * スケジュール実行の遅延時間を返します。
	 */
	protected abstract int getDelayTime();
	
	/**
	 * トランザクションを開始し、引数で指定された監視情報を作成します。
	 * 
	 * @param info 監視情報
	 * @param user 新規作成ユーザ
	 * @return 作成に成功した場合、</code> true </code>
	 * @throws MonitorNotFound
	 * @throws MonitorDuplicate
	 * @throws TriggerSchedulerException
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * 
	 * @see #addMonitorInfo(String)
	 */
	public boolean add(MonitorInfo info, String user) throws MonitorNotFound, MonitorDuplicate, TriggerSchedulerException, HinemosUnknown, InvalidRole {

		m_monitorInfo = info;

		boolean result = false;

		try{
			// 監視情報を登録
			result = addMonitorInfo(user);
		} catch (EntityExistsException e) {
			throw new MonitorDuplicate(e.getMessage(),e);
		} catch (MonitorNotFound e) {
			throw e;
		} catch (TriggerSchedulerException e) {
			throw e;
		} catch (HinemosUnknown e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		}
		return result;
	}
	
	/**
	 * 監視情報を作成します。
	 * <p>
	 * <ol>
	 * <li>監視情報を、引数で指定されたユーザで作成します。</li>
	 * <li>判定情報を作成し、監視情報に設定します。各監視種別（真偽値，数値，文字列）のサブクラスで実装します（{@link #addJudgementInfo()}）。</li>
	 * <li>チェック条件情報を作成し、監視情報に設定します。各監視管理のサブクラスで実装します（{@link #addCheckInfo()}）。</li>
	 * <li>Quartzに、スケージュールと監視情報の有効/無効を登録します。</li>
	 * </ol>
	 * 
	 * @param user 新規作成ユーザ
	 * @return 作成に成功した場合、</code> true </code>
	 * @throws MonitorNotFound
	 * @throws TriggerSchedulerException
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorInfoBean
	 * @see #addJudgementInfo()
	 * @see #addCheckInfo()
	 * @see com.clustercontrol.monitor.run.factory.ModifySchedule#addSchedule(MonitorInfo, String, Calendar)
	 */
	protected boolean addMonitorInfo(String user) throws MonitorNotFound, TriggerSchedulerException, EntityExistsException, HinemosUnknown, InvalidRole {
		long now = HinemosTime.currentTimeMillis();
		JpaTransactionManager jtm = new JpaTransactionManager();

		try{
			// 重複チェック
			jtm.checkEntityExists(MonitorInfo.class, m_monitorInfo.getMonitorId());
			
			m_monitorInfo.setDelayTime(getDelayTime());
			m_monitorInfo.setNotifyGroupId(NotifyGroupIdGenerator.generate(m_monitorInfo));
			m_monitorInfo.setRegDate(now);
			m_monitorInfo.setRegUser(user);
			m_monitorInfo.setTriggerType(getTriggerType().name());
			m_monitorInfo.setUpdateDate(now);
			m_monitorInfo.setUpdateUser(user);
			
			jtm.getEntityManager().persist(m_monitorInfo);

			// 通知情報の登録
			String notifyGroupId = NotifyGroupIdGenerator.generate(m_monitorInfo);
			m_monitorInfo.setNotifyGroupId(notifyGroupId);
			if (m_monitorInfo.getNotifyRelationList() != null
					&& m_monitorInfo.getNotifyRelationList().size() > 0) {
				for (NotifyRelationInfo notifyRelationInfo : m_monitorInfo.getNotifyRelationList()) {
					notifyRelationInfo.setNotifyGroupId(notifyGroupId);
				}
				// 通知情報を登録
				new ModifyNotifyRelation().add(m_monitorInfo.getNotifyRelationList());
			}

			// 判定情報を設定
			if(addJudgementInfo()){
				// チェック条件情報を設定
				if(addCheckInfo()){
					// Quartzに登録(runInterval = 0 -> スケジュール起動を行わない監視)
					if(m_monitorInfo.getRunInterval() > 0){
						ModifySchedule quartz = new ModifySchedule();
						quartz.updateSchedule(m_monitorInfo.getMonitorId());
					}
					return true;
				}
			}
			return false;

		} catch (EntityExistsException e) {
			m_log.info("addMonitorInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		} catch (MonitorNotFound e) {
			throw e;
		} catch (HinemosUnknown e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		}
	}

	/**
	 * 判定情報を作成し、監視情報に設定します。
	 * <p>
	 * 各監視種別（真偽値，数値，文字列）のサブクラスで実装します。
	 * 
	 * @return 作成に成功した場合、</code> true </code>
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 */
	protected abstract boolean addJudgementInfo() throws MonitorNotFound, InvalidRole;

	/**
	 * チェック条件情報を作成し、監視情報に設定します。
	 * <p>
	 * 各監視管理のサブクラスで実装します。
	 * 
	 * @return 作成に成功した場合、</code> true </code>
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	protected abstract boolean addCheckInfo() throws MonitorNotFound, HinemosUnknown, InvalidRole;

	/**
	 * トランザクションを開始し、引数で指定された監視情報を変更します。
	 * 
	 * @param info 監視情報
	 * @param user 最終変更ユーザ
	 * @return 変更に成功した場合、</code> true </code>
	 * @throws MonitorNotFound
	 * @throws TriggerSchedulerException
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * 
	 */
	public boolean modify(MonitorInfo info, String user) throws MonitorNotFound, TriggerSchedulerException, HinemosUnknown, InvalidRole {

		m_monitorInfo = info;

		boolean result = false;

		// 監視情報を変更
		result = modifyMonitorInfo(user);

		return result;
	}

	/**
	 * 判定情報を変更し、監視情報に設定します。
	 * <p>
	 * 各監視種別（真偽値，数値，文字列）のサブクラスで実装します。
	 * 
	 * @return 変更に成功した場合、</code> true </code>
	 * @throws MonitorNotFound
	 * @throws EntityExistsException
	 * @throws InvalidRole
	 */
	protected abstract boolean modifyJudgementInfo() throws MonitorNotFound, EntityExistsException, InvalidRole;

	/**
	 * チェック条件情報を変更し、監視情報に設定します。
	 * <p>
	 * 各監視管理のサブクラスで実装します。
	 * 
	 * @return 変更に成功した場合、</code> true </code>
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	protected abstract boolean modifyCheckInfo() throws MonitorNotFound, InvalidRole, HinemosUnknown;

	/**
	 * 監視情報を作成します。
	 * <p>
	 * <ol>
	 * <li>監視対象IDと監視項目IDより、監視情報を取得します。</li>
	 * <li>監視情報を、引数で指定されたユーザで変更します。</li>
	 * <li>判定情報を変更し、監視情報に設定します。各監視種別（真偽値，数値，文字列）のサブクラスで実装します（{@link #modifyJudgementInfo()}）。</li>
	 * <li>チェック条件情報を変更し、監視情報に設定します。各監視管理のサブクラスで実装します（{@link #modifyCheckInfo()}）。</li>
	 * <li>実行間隔 もしくは 有効/無効が変更されている場合は、Quartzの登録を変更します。</li>
	 * </ol>
	 * 
	 * @param user ユーザ
	 * @return 更新に成功した場合、true
	 * @throws MonitorNotFound
	 * @throws TriggerSchedulerException
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	protected boolean modifyMonitorInfo(String user) throws MonitorNotFound, TriggerSchedulerException, HinemosUnknown, InvalidRole {
		long now = HinemosTime.currentTimeMillis();
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		try{
			// 監視情報を設定
			m_monitor = QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId(), ObjectPrivilegeMode.MODIFY);

			// ファシリティIDが変更されているか
			if(!m_monitorInfo.getFacilityId().equals(m_monitor.getFacilityId())){
				m_isModifyFacilityId = true;
			}
			// 監視/収集間隔が変更されているか
			if(m_monitorInfo.getRunInterval() != m_monitor.getRunInterval().intValue()){
				m_isModifyRunInterval = true;
			}
			// 監視/収集無効から有効(監視or収集のいずれか1つでも)に変更されているか
			if(!m_monitor.getMonitorFlg() &&
					!m_monitor.getCollectorFlg() &&
					(m_monitorInfo.getMonitorFlg() || m_monitorInfo.getCollectorFlg())){
				m_isModifyEnableFlg = true;
			}
			m_log.debug("modifyMonitorInfo() m_isModifyFacilityId = " + m_isModifyFacilityId
					+ ", m_isModifyRunInterval = " + m_isModifyRunInterval
					+ ", m_isModifyEnableFlg = " + m_isModifyEnableFlg);

			m_monitor.setDescription(m_monitorInfo.getDescription());
			if(m_isModifyFacilityId)
				m_monitor.setFacilityId(m_monitorInfo.getFacilityId());
			if(m_isModifyRunInterval)
				m_monitor.setRunInterval(m_monitorInfo.getRunInterval());
			m_monitor.setDelayTime(getDelayTime());
			m_monitor.setTriggerType(getTriggerType().name());
			m_monitor.setCalendarId(m_monitorInfo.getCalendarId());
			m_monitor.setFailurePriority(m_monitorInfo.getFailurePriority());
			m_monitor.setApplication(m_monitorInfo.getApplication());

			// 通知情報を更新
			if (m_monitorInfo.getNotifyRelationList() != null
					&& m_monitorInfo.getNotifyRelationList().size() > 0) {
				for (NotifyRelationInfo notifyRelationInfo : m_monitorInfo.getNotifyRelationList()) {
					notifyRelationInfo.setNotifyGroupId(m_monitor.getNotifyGroupId());
				}
			}
			new NotifyControllerBean().modifyNotifyRelation(
					m_monitorInfo.getNotifyRelationList(), m_monitor.getNotifyGroupId());

			m_monitor.setMonitorFlg(m_monitorInfo.getMonitorFlg());
			m_monitor.setCollectorFlg(m_monitorInfo.getCollectorFlg());
			m_monitor.setLogFormatId(m_monitorInfo.getLogFormatId());
			m_monitor.setItemName(m_monitorInfo.getItemName());
			m_monitor.setMeasure(m_monitorInfo.getMeasure());
			m_monitor.setOwnerRoleId(m_monitorInfo.getOwnerRoleId());
			m_monitor.setUpdateDate(now);
			m_monitor.setUpdateUser(user);

			// 判定情報を設定
			if(modifyJudgementInfo()){

				// チェック条件情報を設定
				if(modifyCheckInfo()){

					// Quartzの登録情報を変更
					new ModifySchedule().updateSchedule(m_monitorInfo.getMonitorId());

					// この監視設定の監視結果状態を削除する
					List<MonitorStatusEntity> statusList
						= MonitorStatusCache.getByPluginIdAndMonitorId(m_monitor.getMonitorTypeId(), m_monitor.getMonitorId());
					for(MonitorStatusEntity status : statusList){
						MonitorStatusCache.remove(status);
					}

					// この監視設定の結果として通知された通知履歴を削除する
					List<NotifyHistoryEntity> historyList
					= com.clustercontrol.notify.util.QueryUtil.getNotifyHistoryByPluginIdAndMonitorId(m_monitor.getMonitorTypeId(), m_monitor.getMonitorId());
					for(NotifyHistoryEntity history : historyList){
						em.remove(history);
					}

					return true;
				}
			}
			return false;

		} catch (NotifyNotFound e) {
			throw new MonitorNotFound(e.getMessage(), e);
		} catch (MonitorNotFound e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		}
	}
	
	/**
	 * トランザクションを開始し、監視情報を削除します。
	 *
	 * @param monitorTypeId 監視対象ID
	 * @param monitorId 監視項目ID
	 * @return 削除に成功した場合、</code> true </code>
	 * @throws MonitorNotFound
	 * @throws TriggerSchedulerException
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 *
	 * @see #deleteMonitorInfo()
	 */
	public boolean delete(String monitorTypeId, String monitorId) throws MonitorNotFound, TriggerSchedulerException, HinemosUnknown, InvalidRole {

		m_monitorTypeId = monitorTypeId;
		m_monitorId = monitorId;

		boolean result = false;

		try
		{
			// 監視情報を削除
			result = deleteMonitorInfo();
		} catch (MonitorNotFound e) {
			throw e;
		} catch (TriggerSchedulerException e) {
			throw e;
		} catch (HinemosUnknown e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		}
		return result;
	}
	
	/**
	 * 監視情報を削除します。
	 * <p>
	 * <ol>
	 * <li>監視対象IDと監視項目IDより、監視情報を取得します。</li>
	 * <li>チェック条件情報を削除します。各監視管理のサブクラスで実装します（{@link #deleteCheckInfo()}）。</li>
	 * <li>判定情報を削除します。各監視種別（真偽値，数値，文字列）のサブクラスで実装します（{@link #deleteJudgementInfo()}）。</li>
	 * <li>監視情報を削除します。</li>
	 * <li>Quartzから監視情報を削除します。</li>
	 * </ol>
	 *
	 * @return 削除に成功した場合、</code> true </code>
	 * @throws MonitorNotFound
	 * @throws TriggerSchedulerException
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 *
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorInfoBean
	 * @see #deleteCheckInfo()
	 * @see com.clustercontrol.monitor.run.factory.ModifySchedule#deleteSchedule(String, String)
	 */
	private boolean deleteMonitorInfo() throws MonitorNotFound, TriggerSchedulerException, HinemosUnknown, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		try
		{
			// 監視情報を取得
			m_monitor = QueryUtil.getMonitorInfoPK(m_monitorId, ObjectPrivilegeMode.MODIFY);

			// 監視グループ情報を削除
			new NotifyControllerBean().deleteNotifyRelation(m_monitor.getNotifyGroupId());

			// チェック条件情報を削除
			if(deleteCheckInfo()){
				// Quartzから削除
				deleteSchedule();

				// 監視情報を削除
				em.remove(m_monitor);

				// この監視設定の監視結果状態を削除する
				List<MonitorStatusEntity> statusList =
						MonitorStatusCache.getByPluginIdAndMonitorId(m_monitorTypeId, m_monitorId);

				for(MonitorStatusEntity status : statusList){
					MonitorStatusCache.remove(status);
				}

				// この監視設定の結果として通知された通知履歴を削除する
				List<NotifyHistoryEntity> historyList =
						com.clustercontrol.notify.util.QueryUtil.getNotifyHistoryByPluginIdAndMonitorId(m_monitorTypeId, m_monitorId);
				for(NotifyHistoryEntity history : historyList){
					em.remove(history);
				}

				return true;
			}
		} catch (MonitorNotFound e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		} catch (HinemosUnknown e) {
			throw e;
		}
		return false;
	}
	
	/**
	 * スケジューラから削除する
	 * @throws TriggerSchedulerException
	 */
	protected void deleteSchedule() throws HinemosUnknown {
		// Quartzに登録(runInterval = 0 -> スケジュール起動を行わない監視)
		if(m_monitor.getRunInterval() > 0){
			new ModifySchedule().deleteSchedule(m_monitorTypeId, m_monitorId);
		}
	}

	/**
	 * チェック条件情報を削除します。
	 * <p>
	 * 各監視管理のサブクラスで実装します。
	 *
	 * @return 削除に成功した場合、</code> true </code>
	 */
	protected boolean deleteCheckInfo() {
		return true;
	}
	
	/**
	 * 監視項目IDをベースにスケジュール実行の遅延時間を生成して返します。
	 */
	public static int getDelayTimeBasic(MonitorInfo monitorInfo){
		// 再起動時も常に同じタイミングでQuartzのTriggerが起動されるように収集種別と収集項目IDから、DelayTimeを生成する

		// 収集種別と収集項目IDを結合した文字列のhashを求める
		int hashCode = (monitorInfo.getMonitorId() + monitorInfo.getMonitorType()).hashCode();

		// hashをシードとして乱数を作成する。このとき乱数の範囲は、0～(monitorInfo-1)とする
		int offsetSecond = new Random(hashCode).nextInt(monitorInfo.getRunInterval());
		m_log.debug("MonitorID : " + monitorInfo.getMonitorId()
				+ ", MonitorType : " + monitorInfo.getMonitorType()
				+ ", offset : " + offsetSecond);

		return offsetSecond;
	}
}
