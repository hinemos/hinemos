/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.sdml.model.SdmlInitializeData;

import jakarta.persistence.EntityExistsException;

public class InitializeDataUtil {
	private static Log logger = LogFactory.getLog(InitializeDataUtil.class);

	private static final Object _deleteLock = new Object();

	/**
	 * アプリケーションIDに紐づく初期化情報を全てクリアする
	 * 
	 * @param applicationId
	 */
	public static void clearAll(String applicationId) {
		clear(applicationId, null);
	}

	/**
	 * アプリケーションIDおよびファシリティIDに紐づく初期化情報をすべてクリアする
	 * 
	 * @param applicationId
	 * @param facilityId
	 */
	public static void clear(String applicationId, String facilityId) {
		if (applicationId == null || applicationId.isEmpty()) {
			logger.error("clear() : applicationId is null.");
			return;
		}
		try {
			synchronized (_deleteLock) {
				if (facilityId == null || facilityId.isEmpty()) {
					int result = QueryUtil.deleteSdmlInitializeDataByApplicationId(applicationId);
					logger.info("clear() : " + "count=" + result + ", applicationId=" + applicationId);
				} else {
					int result = QueryUtil.deleteSdmlInitializeDataByApplicationIdAndFacilityId(applicationId,
							facilityId);
					logger.info("clear() : " + "count=" + result + ", applicationId=" + applicationId + ", facilityId="
							+ facilityId);
				}
			}
		} catch (Exception e) {
			logger.warn("clear() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}
	}

	/**
	 * 監視設定の初期化情報を1件登録する
	 * 
	 * @param data
	 * @throws HinemosUnknown
	 */
	public static void add(SdmlInitializeData data) throws HinemosUnknown {
		logger.debug("add() : " + "data=" + data.toString());

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// 重複チェック
			jtm.checkEntityExists(SdmlInitializeData.class, data.getId());

			em.persist(data);

		} catch (EntityExistsException e) {
			logger.warn("add() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			// 重複受信の場合などすでに存在している場合は無視する
		} catch (Exception e) {
			logger.warn("add() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}
}
