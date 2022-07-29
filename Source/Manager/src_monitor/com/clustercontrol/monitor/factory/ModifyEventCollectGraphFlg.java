/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.factory;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.EventLogNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.bean.EventDataInfo;
import com.clustercontrol.monitor.bean.EventLogHistoryTypeConstant;
import com.clustercontrol.monitor.run.util.EventCacheModifyCallback;
import com.clustercontrol.monitor.run.util.EventLogOperationHistoryUtil;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.notify.monitor.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;


/**
 * イベント情報の性能グラフ用フラグを更新するクラス<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class ModifyEventCollectGraphFlg {

	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(ModifyEventCollectGraphFlg.class);

	/**
	 * 引数で指定されたイベント情報一覧の性能グラフ用フラグを更新します。<BR>
	 * 
	 * @param list 更新対象のイベント情報一覧（EventLogDataが格納されたArrayList）
	 * @param collectGraphFlg 性能グラフ用フラグ（ON:true、OFF:false）（更新値）
	 * 
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 */
	public void modifyCollectGraphFlg(ArrayList<EventDataInfo> list, Boolean collectGraphFlg) throws MonitorNotFound, InvalidRole  {
		
		if (list == null || list.size() <= 0) {
			return ;
		}
		
		for (EventDataInfo event : list) {
			this.modifyCollectGraphFlg(
					event.getMonitorId(),
					event.getMonitorDetailId(),
					event.getPluginId(),
					event.getFacilityId(),
					event.getOutputDate(),
					collectGraphFlg);
		}
		
	}

	/**
	 * 引数で指定されたイベント情報の性能グラフ用フラグを更新します。<BR>
	 * 取得したイベント情報の性能グラフ用フラグを更新します。
	 * 
	 * @param monitorId 更新対象の監視項目ID
	 * @param monitorDetailId 更新対象の監視項目詳細ID
	 * @param pluginId 更新対象のプラグインID
	 * @param facilityId 更新対象のファシリティID
	 * @param outputDate 更新対象の受信日時
	 * @param collectGraphFlg 更新対象の性能グラフ用フラグ(更新値)
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 */
	public void modifyCollectGraphFlg(
			String monitorId,
			String monitorDetailId,
			String pluginId,
			String facilityId,
			Long outputDate,
			Boolean collectGraphFlg) throws  MonitorNotFound, InvalidRole  {

		m_log.debug("monitorId = " + monitorId + ", monitorDetailId = " + monitorDetailId + ", pluginId = " + pluginId 
				+ ", facilityId = " + facilityId + ", outputDate = " + outputDate + ", collectGraphFlg = " + collectGraphFlg);
		
		if (collectGraphFlg == null) {
			//NOT NULL 列のため、nullの場合は何もしない
			return;
		}
		
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
			
			long now = HinemosTime.currentTimeMillis();
			String user = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
			StringBuilder detail = new StringBuilder();
			
			boolean isChange = ModifyEventInfo.setCollectGraphFlagChange(event, collectGraphFlg, detail);
			
			if (isChange) {
				EventLogOperationHistoryUtil.addEventLogOperationHistory(jtm,
						event, now, user, EventLogHistoryTypeConstant.TYPE_CHANGE_VALUE, detail.toString()
					);
			}
			
			jtm.addCallback(new EventCacheModifyCallback(false, event));
		}
	}
}

