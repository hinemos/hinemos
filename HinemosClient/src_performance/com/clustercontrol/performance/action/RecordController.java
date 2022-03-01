/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.action;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.CollectorItemCodeMstMapResponse;
import org.openapitools.client.model.CollectorItemTreeItemResponse;

import com.clustercontrol.collect.util.CollectRestClientWrapper;

/**
 * 収集した性能情報を取得を行うアクションクラス
 *
 * @version 4.0.0
 * @since 1.0.0
 *
 */
public class RecordController {
	private static Log log = LogFactory.getLog(RecordController.class);

	private int errorCount = 1;

	/**
	 * コンストラクタ
	 */
	public RecordController() {
	}

	/**
	 * 収集項目コード情報を取得します。
	 *
	 * @param ファシリティID
	 * @return デバイス情報セット
	 */
	public Map<String, CollectorItemTreeItemResponse> getItemCodeTreeMap(String managerName) {
		log.debug("getItemCodeTreeMap() : managerName=" + managerName);
		for (int i = 0; i <= this.errorCount; i++) {
			try {
				CollectRestClientWrapper wrapper = CollectRestClientWrapper.getWrapper(managerName);
				CollectorItemCodeMstMapResponse res = wrapper.getItemCodeMap();
				// findbugs対応 不要な初期化を削除
				Map<String, CollectorItemTreeItemResponse> rtnMap = res.getItemCodeMap();
				log.debug("getItemCodeTreeMap() : size=" + rtnMap.size());
				return rtnMap;
			} catch (Exception e){
				log.error("getItemCodeTreeMap()", e);
			}
		}

		return null;
	}
}
