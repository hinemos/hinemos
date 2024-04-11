/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.SingletonUtil;
import org.openapitools.client.model.SdmlMonitorTypeMasterResponse;

import com.clustercontrol.util.RestConnectManager;

/**
 * SDML監視種別に関するユーティリティクラス
 *
 */
public class SdmlMonitorTypeUtil {
	private static Log logger = LogFactory.getLog(SdmlMonitorTypeUtil.class);

	private Map<String, Map<String, SdmlMonitorTypeMasterResponse>> sdmlMonitorTypeMstMap;

	/** Private Constructor */
	private SdmlMonitorTypeUtil() {
	}

	/** Get singleton */
	private static SdmlMonitorTypeUtil getInstance() {
		return SingletonUtil.getSessionInstance(SdmlMonitorTypeUtil.class);
	}

	private Map<String, Map<String, SdmlMonitorTypeMasterResponse>> getMap() {
		if (sdmlMonitorTypeMstMap == null || sdmlMonitorTypeMstMap.size() == 0) {
			initialize();
		}
		return sdmlMonitorTypeMstMap;
	}

	/**
	 * キャッシュ情報初期化
	 */
	private void initialize() {
		logger.debug("initialize()");
		// 初期化
		sdmlMonitorTypeMstMap = new ConcurrentHashMap<>();

		// マネージャ毎に初期化する
		for (String managerName : RestConnectManager.getActiveManagerSet()) {
			initialize(managerName);
		}
	}

	/**
	 * キャッシュ情報初期化（マネージャー毎）
	 */
	private void initialize(String managerName) {
		logger.debug("initialize() : managerName=" + managerName);

		try {
			SdmlRestClientWrapper wrapper = SdmlRestClientWrapper.getWrapper(managerName);
			List<SdmlMonitorTypeMasterResponse> list = wrapper.getSdmlMonitorTypeMaster();

			if (list != null && !list.isEmpty()) {
				// 取得した情報をキャッシュにセット
				Map<String, SdmlMonitorTypeMasterResponse> map = new ConcurrentHashMap<>();
				for (SdmlMonitorTypeMasterResponse info : list) {
					map.put(info.getSdmlMonitorTypeId(), info);
				}
				sdmlMonitorTypeMstMap.put(managerName, map);
			} else {
				sdmlMonitorTypeMstMap.put(managerName, new ConcurrentHashMap<>());
			}
		} catch (Exception e) {
			logger.error("initialize() : " + e.getMessage(), e);
		}
	}

	/**
	 * キャッシュ情報を更新する
	 */
	public static void refresh() {
		getInstance().initialize();
	}

	/**
	 * マネージャを指定してマップを取得
	 * 
	 * @param managerName
	 * @return
	 */
	public static Map<String, SdmlMonitorTypeMasterResponse> getMap(String managerName) {
		Map<String, SdmlMonitorTypeMasterResponse> map = getInstance().getMap().get(managerName);
		if (map == null) {
			return new ConcurrentHashMap<>();
		}
		return map;
	}

	/**
	 * マネージャとSDML監視種別IDを指定してプラグインIDを取得
	 * 
	 * @param managerName
	 * @param sdmlMonitorTypeId
	 * @return
	 */
	public static String getPluginId(String managerName, String sdmlMonitorTypeId) {
		SdmlMonitorTypeMasterResponse info = getMap(managerName).get(sdmlMonitorTypeId);
		if (info == null) {
			return null;
		}
		return info.getPluginId();
	}

	/**
	 * 指定したプラグインIDがSDMLのものかどうか
	 * 
	 * @param managerName
	 * @param pluginId
	 * @return
	 */
	public static boolean isSdmlPluginId(String managerName, String pluginId) {
		Map<String, SdmlMonitorTypeMasterResponse> map = getMap(managerName);
		if (map == null || map.isEmpty()) {
			return false;
		}
		for (Entry<String, SdmlMonitorTypeMasterResponse> entry : map.entrySet()) {
			if (pluginId.equals(entry.getValue().getPluginId())) {
				return true;
			}
		}
		return false;
	}
}
