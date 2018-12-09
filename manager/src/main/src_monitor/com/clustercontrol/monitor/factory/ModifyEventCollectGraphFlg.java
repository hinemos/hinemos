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
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.EventLogNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.bean.EventDataInfo;
import com.clustercontrol.monitor.run.util.EventCacheModifyCallback;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.notify.monitor.util.QueryUtil;


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

		if (list != null && list.size()>0) {
			for(EventDataInfo event : list) {
				if (event != null) {
					this.modifyCollectGraphFlg(
							event.getMonitorId(),
							event.getMonitorDetailId(),
							event.getPluginId(),
							event.getFacilityId(),
							event.getPriority(),
							event.getGenerationDate(),
							event.getOutputDate(),
							collectGraphFlg);
				}
			}
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
	 * @param priority 更新対象の重要度
	 * @param generateDate 更新対象の出力日時
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
			int priority,
			Long generateDate,
			Long outputDate,
			Boolean collectGraphFlg) throws  MonitorNotFound, InvalidRole  {

		m_log.debug("monitorId = " + monitorId + ", monitorDetailId = " + monitorDetailId + ", pluginId = " + pluginId 
				+ ", facilityId = " + facilityId + ", priority = " + priority + ", generateDate = " + generateDate 
				+ ", outputDate = " + outputDate + ", collectGraphFlg = " + collectGraphFlg);
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

			// フラグを変更
			event.setCollectGraphFlg(collectGraphFlg);
			
			jtm.addCallback(new EventCacheModifyCallback(false, event));
		}
	}
}

