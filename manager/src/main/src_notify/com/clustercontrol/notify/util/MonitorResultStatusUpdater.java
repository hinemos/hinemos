/*

 Copyright (C) 2009 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.notify.util;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.entity.MonitorStatusPK;
import com.clustercontrol.notify.model.MonitorStatusEntity;

public class MonitorResultStatusUpdater {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( MonitorResultStatusUpdater.class );

	// このメソッドを利用する場合は、getLockを使用すること。
	public static Long getCounter (MonitorStatusPK pk) throws NotifyNotFound {
		Long count = null;
		MonitorStatusEntity monitorStatus = MonitorStatusCache.get(pk);
		count = monitorStatus.getCounter();
		if (count == null) {
			m_log.info("getCounter() counter is null. pk=" + pk);
		}
		return count;
	}

	/**
	 * 直前の監視結果と現在の監視結果の重要度を比較し、変更がある場合は、DBで保持している同一重要度カウンタをリセット。
	 * 戻り値として、trueを返す。
	 * 重要度変化がない場合は、同一重要度カウンタをカウントアップし、DB情報を更新。
	 * 戻り値として、falseを返す。
	 *
	 * @param facilityId ファシリティID
	 * @param pluginId プラグインID
	 * @param monitorId 監視項目ID
	 * @param generateDate 監視結果生成時刻
	 * @param currentPriority 現在の監視結果の重要度
	 * @return 重要度変化の有無（有:true / 無:false）
	 */
	private static boolean update(String facilityId, String pluginId, String monitorId,
			String subkey, Long generateDate, int currentPriority) {
		m_log.debug("update() facilityId = " + facilityId +
				", pluginId = " + pluginId +
				", monitorId = " + monitorId +
				", subkey = " + subkey +
				", generateDate = " + generateDate +
				", currentPriority = " + currentPriority);

		MonitorStatusPK pk = new MonitorStatusPK(facilityId, pluginId, monitorId, subkey);

		MonitorStatusEntity monitorStatus = MonitorStatusCache.get(pk);
		if (monitorStatus == null) {
			m_log.debug("create new entity. " + pk);

			// 新規タプルを生成
			try {
				// 同一重要度カウンタは1で生成
				// インスタンス生成
				monitorStatus = new MonitorStatusEntity(facilityId, pluginId, monitorId, subkey);
				// 重複チェック
				monitorStatus.setPriority(currentPriority);
				monitorStatus.setLastUpdate(generateDate);
				monitorStatus.setCounter(1l);
				MonitorStatusCache.add(monitorStatus);
				return true;
			} catch (EntityExistsException e1) {
				m_log.info("update() : "
						+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
				return true;
			}
		}

		// 重要度が変化しているか確認
		if(currentPriority != monitorStatus.getPriority()){
			if(m_log.isDebugEnabled()){
				m_log.debug("prioityChangeFlag = true. " + pk + " ," +
						monitorStatus.getPriority() + " to " +
						currentPriority);
			}

			// 重要度を更新
			monitorStatus.setPriority(currentPriority);

			// 同一重要度カウンタを1にリセット
			monitorStatus.setCounter(1l);
			MonitorStatusCache.update(monitorStatus);

			return true;
		} else {
			// 重要度は変化していない
			// 50行ぐらい下の箇所で、カウンタをアップする。
		}

		// 同一重要度カウンタをアップ
		long oldCount = monitorStatus.getCounter();
		// 同一重要度カウンタの最大値（この値を超えた場合は、以降は更新されない）
		int maxInitialCount = HinemosPropertyUtil.getHinemosPropertyNum("notify.initial.count.max", Long.valueOf(10)).intValue();

		// 最大カウント数を超えると、それ以上は増やさない（DBへのupdateを減らすための方策）
		if(oldCount <= maxInitialCount){
			monitorStatus.setCounter(oldCount+1);
			monitorStatus.setLastUpdate(generateDate);
			MonitorStatusCache.update(monitorStatus);
		}
		return false;
	}

	public static boolean update(OutputBasicInfo output) {
		if(output.getSubKey() == null){
			m_log.info("SubKey is null. PluginId = " + output.getPluginId() +
					", MonitorId = " + output.getMonitorId() +
					", FacilityId = " + output.getFacilityId());
			output.setSubKey("");
		}

		return update(output.getFacilityId(), output.getPluginId(), output.getMonitorId(),
				output.getSubKey(), output.getGenerationDate(), output.getPriority());
	}
}
