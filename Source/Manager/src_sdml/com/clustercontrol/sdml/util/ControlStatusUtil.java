/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.SdmlControlSettingNotFound;
import com.clustercontrol.sdml.model.SdmlControlStatus;
import com.clustercontrol.sdml.model.SdmlControlStatusPK;
import com.clustercontrol.util.HinemosTime;

public class ControlStatusUtil {
	private static Log logger = LogFactory.getLog(ControlStatusUtil.class);

	private static final Object _deleteLock = new Object();

	/**
	 * アプリケーションIDに紐づく自動制御の状態を全てクリアする
	 * 
	 * @param applicationId
	 */
	public static void clearAll(String applicationId) {
		try {
			synchronized (_deleteLock) {
				int result = QueryUtil.deleteSdmlControlStatusByApplicationId(applicationId);
				logger.info("clearAll() : " + "count=" + result + ", applicationId=" + applicationId);
			}
		} catch (Exception e) {
			logger.warn("clearAll() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}
	}

	/**
	 * 引数で受け取った自動制御状態のエンティティをDBに登録する
	 * 
	 * @param statusEntity
	 */
	public static void refresh(SdmlControlStatus statusEntity) {
		if (statusEntity == null) {
			logger.error("refresh() : entity is null.");
			return;
		}
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			SdmlControlStatus dstEntity = null;
			try {
				dstEntity = QueryUtil.getSdmlControlStatusPK(
						new SdmlControlStatusPK(statusEntity.getApplicationId(), statusEntity.getFacilityId()));
			} catch (SdmlControlSettingNotFound e) {
				// 初回のみ到達する
				logger.debug("refresh() : control status is not found." + statusEntity.getId());
				HinemosEntityManager em = jtm.getEntityManager();
				// 初回の場合は受け取ったエンティティを登録
				em.persist(statusEntity);
				return;
			}
			// 取得できた場合は受け取ったエンティティから値を更新（初回以降）
			copy(statusEntity, dstEntity);
		} catch (Exception e) {
			logger.warn("refresh() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}
	}

	/**
	 * 登録されている全ての自動制御の状態を取得する
	 * 
	 * @return
	 */
	public static List<SdmlControlStatus> getAll() {
		return QueryUtil.getAllSdmlControlStatus();
	}

	/**
	 * SDML制御ログの受信状態を確認し、設定時間以上受信がないかどうかチェックする
	 * 
	 * @param statusEntity
	 * @return true:正常（受信あり） false:異常（受信なし）
	 */
	public static boolean checkControlLogLastUpdate(SdmlControlStatus statusEntity) {
		if (statusEntity.getInternalCheckInterval() == null) {
			// 開始前または停止後はnullとなるのでチェックは通過させる
			return true;
		}
		if (statusEntity.getLastUpdateDate() == null) {
			// 制御ログが送られてきてからステータスが作られるため通常到達しない（チェックは通過させる）
			logger.error("checkControlLogLastUpdate() : last update date is null." + " statusEntity="
					+ statusEntity.toString());
			return true;
		}
		Long buffer = HinemosPropertyCommon.sdml_control_log_reception_max_period_buffer.getNumericValue();
		Long addedDate = statusEntity.getLastUpdateDate()
				+ ((statusEntity.getInternalCheckInterval() + buffer) * 60 * 1000);
		// 現在時刻が間隔＋バッファを超過している場合はfalse
		if (addedDate < HinemosTime.currentTimeMillis()) {
			return false;
		}
		return true;
	}

	/**
	 * SdmlControlStatusのフィールドをコピーする
	 * 
	 * @param src
	 * @param dst
	 */
	public static void copy(SdmlControlStatus src, SdmlControlStatus dst) {
		dst.setApplicationStartupDate(src.getApplicationStartupDate());
		dst.setInternalCheckInterval(src.getInternalCheckInterval());
		dst.setLastControlCode(src.getLastControlCode());
		dst.setLastUpdateDate(src.getLastUpdateDate());
		dst.setStatus(src.getStatus());
	}
}
