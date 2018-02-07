/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.factory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.EventLogNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.bean.ConfirmConstant;
import com.clustercontrol.monitor.bean.EventBatchConfirmInfo;
import com.clustercontrol.monitor.bean.EventDataInfo;
import com.clustercontrol.monitor.run.util.EventCacheModifyCallback;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.notify.monitor.util.QueryUtil;
import com.clustercontrol.repository.bean.FacilityTargetConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;


/**
 * イベント情報の確認を更新するクラス<BR>
 *
 * @version 3.0.0
 * @since 1.0.0
 */
public class ModifyEventConfirm {

	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(ModifyEventConfirm.class);

	/**
	 * 引数で指定されたイベント情報一覧の確認を更新します。<BR>
	 * 確認ユーザとして、操作を実施したユーザを設定します。<BR>
	 * 取得したイベント情報の確認フラグを更新します。確認フラグが済の場合は、確認済み日時も更新します。
	 * 
	 * @param list 更新対象のイベント情報一覧（EventLogDataが格納されたArrayList）
	 * @param confirmType 確認フラグ（未／済）（更新値）
	 * @param confirmUser 確認ユーザ
	 * 
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.monitor.bean.EventTabelDefine
	 * @see com.clustercontrol.bean.ConfirmConstant
	 * @see #modifyConfirm(String, String, String, Date, Date, int, String)
	 */
	public void modifyConfirm(ArrayList<EventDataInfo> list, int confirmType, String confirmUser
			) throws MonitorNotFound, InvalidRole  {

		if (list != null && list.size()>0) {

			// 確認済み日時
			Long confirmDate = null;
			if(confirmType == ConfirmConstant.TYPE_CONFIRMED){
				confirmDate = HinemosTime.currentTimeMillis();
			}

			for(EventDataInfo event : list) {

				if (event != null) {

					this.modifyConfirm(
							event.getMonitorId(),
							event.getMonitorDetailId(),
							event.getPluginId(),
							event.getFacilityId(),
							event.getPriority(),
							event.getGenerationDate(),
							event.getOutputDate(),
							confirmDate,
							confirmType,
							confirmUser);

				}
			}
		}
	}

	/**
	 * 引数で指定されたイベント情報の確認を更新します。<BR>
	 * 確認ユーザとして、操作を実施したユーザを設定します。<BR>
	 * 取得したイベント情報の確認フラグを更新します。確認フラグが済の場合は、確認済み日時も更新します。
	 * 
	 * @param monitorId 更新対象の監視項目ID
	 * @param pluginId 更新対象のプラグインID
	 * @param facilityId 更新対象のファシリティID
	 * @param priority 更新対象の重要度
	 * @param generateDate 更新対象の出力日時
	 * @param outputDate 更新対象の受信日時
	 * @param confirmDate 更新対象の確認済み日時
	 * @param confirmType 確認フラグ（未／済）（更新値）
	 * @param confirmUser 確認ユーザ
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.bean.ConfirmConstant
	 * @see com.clustercontrol.monitor.ejb.entity.EventLogBean#ejbFindByPrimaryKey(EventLogPK)
	 */
	public void modifyConfirm(
			String monitorId,
			String monitorDetailId,
			String pluginId,
			String facilityId,
			int priority,
			Long generateDate,
			Long outputDate,
			Long confirmDate,
			int confirmType,
			String confirmUser
			) throws  MonitorNotFound, InvalidRole  {

		// イベントログ情報を取得
		EventLogEntity event = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {

			try {
				event = QueryUtil.getEventLogPK(monitorId, monitorDetailId, pluginId, outputDate, facilityId, ObjectPrivilegeMode.MODIFY);
			} catch (EventLogNotFound e) {
				throw new MonitorNotFound(e.getMessage(), e);
			} catch (InvalidRole e) {
				throw e;
			}

			// 確認有無を変更
			event.setConfirmFlg(confirmType);

			if(confirmType == ConfirmConstant.TYPE_CONFIRMED){
				if(confirmDate == null){
					confirmDate = HinemosTime.currentTimeMillis();
				}
				event.setConfirmDate(confirmDate);
			}

			// 確認を実施したユーザを設定
			event.setConfirmUser(confirmUser);
			
			jtm.addCallback(new EventCacheModifyCallback(false, event));
		}
	}

	/**
	 * 引数で指定された条件に一致するイベント情報の確認を一括更新します。
	 * <p>
	 * <ol>
	 * <li>引数で指定されたプロパティに格納された更新条件を、プロパティユーティリティ（{@link com.clustercontrol.util.PropertyUtil}）を使用して取得します。</li>
	 * <li>引数で指定されたファシリティ配下のファシリティと更新条件を基に、イベント情報を取得します。</li>
	 * <li>確認ユーザとして、操作を実施したユーザを設定します。</li>
	 * <li>取得したイベント情報の確認フラグを更新します。確認フラグが済の場合は、確認済み日時も更新します。</li>
	 * <li>イベント情報Entity Beanのキャッシュをフラッシュします。</li>
	 * 
	 * @param confirmType 確認フラグ（未／済）（更新値）
	 * @param facilityId 更新対象の親ファシリティID
	 * @param property 更新条件
	 * @param confirmUser 確認ユーザ
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.bean.ConfirmConstant
	 * @see com.clustercontrol.util.PropertyUtil#getPropertyValue(com.clustercontrol.bean.Property, java.lang.String)
	 * @see com.clustercontrol.repository.session.RepositoryControllerBean#getFacilityIdList(String, int)
	 * @see com.clustercontrol.monitor.ejb.entity.EventLogBean#ejbHomeBatchConfirm(String[], Integer, Timestamp, Timestamp, Timestamp, Timestamp, String, String, Integer, Integer)
	 */
	public void modifyBatchConfirm(int confirmType, String facilityId, EventBatchConfirmInfo info, String confirmUser) throws HinemosUnknown {

		Integer[] priorityIds = null;
		Long outputFromDate = null;
		Long outputToDate = null;
		Long generationFromDate = null;
		Long generationToDate = null;
		String monitorId = null;
		String monitorDetailId = null;
		int facilityType = 0;
		String application = null;
		String message = null;
		String comment = null;
		String commentUser = null;
		Boolean collectGraphFlg = null;

		//重要度取得
		if(info.getPriorityList() == null){
			throw new HinemosUnknown("priority is null");
		}
		priorityIds = info.getPriorityList();
		
		//更新日時（自）取得
		if(info.getOutputFromDate() != null){
			outputFromDate = info.getOutputFromDate();
			outputFromDate -= (outputFromDate % 1000);	//ミリ秒の桁を0にする
		}

		//更新日時（至）取得
		if(info.getOutputToDate() != null){
			outputToDate = info.getOutputToDate();
			outputToDate += (999 - (outputToDate % 1000));	//ミリ秒の桁を999にする
		}

		//出力日時（自）取得
		if(info.getGenerationFromDate() != null){
			generationFromDate = info.getGenerationFromDate();
			generationFromDate -= (generationFromDate % 1000);	//ミリ秒の桁を0にする
		}

		//出力日時（至）取得
		if(info.getGenerationToDate() != null){
			generationToDate = info.getGenerationToDate();
			generationToDate += (999 - (generationToDate % 1000));	//ミリ秒の桁を999にする
		}

		//監視項目ID取得
		if(!"".equals(info.getMonitorId())){
			monitorId = info.getMonitorId();
		}

		//監視詳細取得
		if(!"".equals(info.getMonitorDetailId())){
			monitorDetailId = info.getMonitorDetailId();
		}

		//対象ファシリティ種別取得
		if(info.getFacilityType() != null){
			facilityType = info.getFacilityType();
		}

		//アプリケーション取得
		if(!"".equals(info.getApplication())){
			application = info.getApplication();
		}

		//メッセージ取得
		if(!"".equals(info.getMessage())){
			message = info.getMessage();
		}
		//コメント
		if(!"".equals(info.getComment())){
			comment = info.getComment();
		}
		//コメントユーザ
		if(!"".equals(info.getCommentUser())){
			commentUser = info.getCommentUser();
		}
		// 性能グラフ用フラグ
		if (info.getCollectGraphFlg() != null) {
			collectGraphFlg = info.getCollectGraphFlg();
		}

		// 対象ファシリティのファシリティIDを取得
		// ファシリティが指定されない（最上位）場合は、ファシリティIDを指定せずに検索を行う
		String[] facilityIds = null;
		ArrayList<String> facilityIdList = null;
		if(facilityId != null && !"".equals(facilityId)){

			int level = RepositoryControllerBean.ALL;
			if(FacilityTargetConstant.TYPE_BENEATH == facilityType){
				level = RepositoryControllerBean.ONE_LEVEL;
			}

			facilityIdList = new RepositoryControllerBean().getFacilityIdList(facilityId, level);

			// スコープの場合
			if(facilityIdList != null && facilityIdList.size() > 0){
				facilityIds = new String[facilityIdList.size()];
				facilityIdList.toArray(facilityIds);
			}
			// ノードの場合
			else{
				facilityIds = new String[1];
				facilityIds[0] = facilityId;
			}
		}

		// アップデートする設定フラグ
		Long confirmDate = HinemosTime.currentTimeMillis();
		Integer confirmFlg = Integer.valueOf(confirmType);

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			int rtn = QueryUtil.updateEventLogFlgByFilter(
					facilityIds,
					priorityIds,
					outputFromDate,
					outputToDate,
					generationFromDate,
					generationToDate,
					monitorId,
					monitorDetailId,
					application,
					message,
					confirmFlg,
					confirmUser,
					comment,
					commentUser,
					confirmType,
					confirmDate,
					collectGraphFlg);
			m_log.debug("The result of updateEventLogFlgByFilter is: " + rtn);
			
			// イベントキャッシュの更新
			ArrayList<Integer> priorityList = new ArrayList<>();
			for (Integer i : priorityIds) {
				priorityList.add(i);
			}
			String ownerRoleId = null;
			jtm.addCallback(new EventCacheModifyCallback(
					facilityIdList,
					priorityList,
					outputFromDate,
					outputToDate,
					generationFromDate,
					generationToDate,
					monitorId,
					monitorDetailId,
					application,
					message,
					confirmFlg,
					confirmUser,
					comment,
					commentUser,
					confirmType,
					confirmDate,
					collectGraphFlg,
					ownerRoleId));
		}
	}
}

