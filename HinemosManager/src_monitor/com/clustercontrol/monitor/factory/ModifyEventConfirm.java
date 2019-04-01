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
import com.clustercontrol.monitor.bean.UpdateEventFilterInternal;
import com.clustercontrol.monitor.run.util.EventCacheModifyCallback;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.notify.monitor.util.QueryUtil;
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
	 * 取得したイベント情報の確認フラグを更新します。確認フラグの更新値が確認中、確認済の場合は、確認日時も更新します。
	 * 
	 * @param list 更新対象のイベント情報一覧（EventLogDataが格納されたArrayList）
	 * @param confirmType 確認（未確認／確認中／確認済）（更新値）
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
			if (confirmType == ConfirmConstant.TYPE_CONFIRMED
					|| confirmType == ConfirmConstant.TYPE_CONFIRMING){
				confirmDate = HinemosTime.currentTimeMillis();
			}

			for(EventDataInfo event : list) {

				if (event != null) {

					this.modifyConfirm(
							event.getMonitorId(),
							event.getMonitorDetailId(),
							event.getPluginId(),
							event.getFacilityId(),
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
	 * @param monitorDetailId 更新対象の監視詳細
	 * @param pluginId 更新対象のプラグインID
	 * @param facilityId 更新対象のファシリティID
	 * @param outputDate 更新対象の受信日時
	 * @param confirmDate 確認日時
	 * @param confirmType 確認（未確認／確認中／確認済）（更新値）
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
			
			long date = HinemosTime.currentTimeMillis();
			
			ModifyEventInfo.setConfirmFlgChange(jtm, event, confirmType, date, confirmUser);
			
			jtm.addCallback(new EventCacheModifyCallback(false, event));
		}
	}

	/**
	 * 引数で指定された条件に一致するイベント情報の確認を一括更新します。
	 * <p>
	 * <ol>
	 * <li>引数で指定されたファシリティ配下のファシリティと更新条件を基に、イベント情報を取得します。</li>
	 * <li>確認ユーザとして、操作を実施したユーザを設定します。</li>
	 * <li>取得したイベント情報の確認フラグを更新します。確認フラグが確認中／確認済の場合は、確認日時も更新します。</li>
	 * <li>イベント情報Entity Beanのキャッシュをフラッシュします。</li>
	 * 
	 * @param confirmType 確認フラグ（未確認／確認中／確認済）（更新値）
	 * @param facilityId 更新対象の親ファシリティID
	 * @param info 更新条件
	 * @param confirmUser 確認ユーザ
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.bean.ConfirmConstant
	 * @see com.clustercontrol.util.PropertyUtil#getPropertyValue(com.clustercontrol.bean.Property, java.lang.String)
	 * @see com.clustercontrol.repository.session.RepositoryControllerBean#getFacilityIdList(String, int)
	 * @see com.clustercontrol.monitor.ejb.entity.EventLogBean#ejbHomeBatchConfirm(String[], Integer, Timestamp, Timestamp, Timestamp, Timestamp, String, String, Integer, Integer)
	 */
	public void modifyBatchConfirm(int confirmType, String facilityId, EventBatchConfirmInfo info, String confirmUser) throws HinemosUnknown {

		if (info.getPriorityList() == null){
			throw new HinemosUnknown("priority is null");
		}
		
		//フィルタ条件を変換
		UpdateEventFilterInternal filterIntenal = new UpdateEventFilterInternal();
		filterIntenal.setFilter(facilityId, info);
		
		// アップデートする設定フラグ
		Long confirmDate = HinemosTime.currentTimeMillis();
		Integer confirmFlg = Integer.valueOf(confirmType);
		
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			
			int rtn = QueryUtil.updateEventLogFlgByFilter(
					filterIntenal,
					confirmFlg,
					confirmUser,
					confirmDate);
			m_log.debug("The result of updateEventLogFlgByFilter is: " + rtn);
			
			// イベントキャッシュの更新
			jtm.addCallback(new EventCacheModifyCallback(
					filterIntenal,
					confirmFlg,
					confirmUser,
					confirmDate));
		}
	}
}

