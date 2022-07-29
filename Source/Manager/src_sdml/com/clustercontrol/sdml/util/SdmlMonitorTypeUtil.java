/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.QueryDivergence;
import com.clustercontrol.sdml.model.SdmlMonitorTypeMasterInfo;
import com.clustercontrol.util.FilterConstant;
import com.clustercontrol.util.Singletons;

/**
 * SDML監視種別に関するユーティリティクラス
 *
 */
public class SdmlMonitorTypeUtil {
	private static Log logger = LogFactory.getLog(SdmlMonitorTypeUtil.class);

	private Map<String, SdmlMonitorTypeMasterInfo> sdmlMonitorTypeMstMap = new ConcurrentHashMap<>();

	/**
	 * コンストラクタではなく、{@link Singletons#get(Class)}を使用してください。
	 */
	public SdmlMonitorTypeUtil() {
		// SDML監視種別マスタは運用中に変更する想定ではないので初回のみ更新
		refresh();
	}

	private Map<String, SdmlMonitorTypeMasterInfo> getMap() {
		if (sdmlMonitorTypeMstMap == null || sdmlMonitorTypeMstMap.size() == 0) {
			refresh();
		}
		return sdmlMonitorTypeMstMap;
	}

	/**
	 * 情報更新
	 */
	private void refresh() {
		logger.debug("refresh()");
		// 初期化
		sdmlMonitorTypeMstMap.clear();

		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			List<SdmlMonitorTypeMasterInfo> list = QueryUtil.getAllSdmlMonitorTypeMst();
			if (list != null && !list.isEmpty()) {
				for (SdmlMonitorTypeMasterInfo info : list) {
					sdmlMonitorTypeMstMap.put(info.getSdmlMonitorTypeId(), info);
				}
			}

			jtm.commit();
		} catch (Exception e) {
			logger.error("refresh() : " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	/**
	 * SDML監視種別IDを指定してプラグインIDを取得
	 * 
	 * @param sdmlMonitorTypeId
	 * @return
	 */
	public String getPluginId(String sdmlMonitorTypeId) {
		SdmlMonitorTypeMasterInfo info = getMap().get(sdmlMonitorTypeId);
		if (info == null) {
			return "";
		}
		return info.getPluginId();
	}

	/**
	 * 指定したSDML監視種別IDがマスタに含まれているかどうか
	 * 
	 * @param sdmlMonitorTypeId
	 * @return
	 */
	public boolean isContained(String sdmlMonitorTypeId) {
		SdmlMonitorTypeMasterInfo info = getMap().get(sdmlMonitorTypeId);
		if (info == null) {
			return false;
		}
		return true;
	}

	/**
	 * 指定されたプラグインIDから一致するSDML監視種別IDの一覧を取得する<BR>
	 * 先頭に「NOT:」がある場合は一致しない一覧を返す
	 * 
	 * @param pluginId
	 * @return
	 */
	public static List<String> getSdmlMonTypeIdList(String pluginId) {
		if (pluginId == null || pluginId.isEmpty()) {
			return null;
		}

		boolean negate = false;
		String param = pluginId;
		// 「NOT:」の場合は一致しないリストを返す
		if (pluginId.startsWith(FilterConstant.NEGATION_PREFIX)) {
			param = pluginId.substring(FilterConstant.NEGATION_PREFIX.length());
			negate = true;
		}
		List<SdmlMonitorTypeMasterInfo> list = null;
		if (negate) {
			list = QueryUtil.getSdmlMonitorTypeMstByPluginIdNot(QueryDivergence.escapeLikeCondition(param));
		} else {
			list = QueryUtil.getSdmlMonitorTypeMstByPluginId(QueryDivergence.escapeLikeCondition(param));
		}

		List<String> rtn = new ArrayList<>();
		for (SdmlMonitorTypeMasterInfo info : list) {
			rtn.add(info.getSdmlMonitorTypeId());
		}
		return rtn;
	}
}