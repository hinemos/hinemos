/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.util;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.hub.util.DataId.GeneratorFor;

/**
 * 
 * 収集(バイナリ)をDBに格納する際に使用する ID を生成
 * 
 * @version 6.1.0
 * @since 6.1.0
 *
 */
public class DataIdGenerator {

	private static Log m_log = LogFactory.getLog(DataIdGenerator.class);

	public static synchronized void init(GeneratorFor type) {
		if (DataId.generatorMap.get(type) != null) {
			return;
		}

		try {
			//本クラスは汎用的に使用できそうな実装に見えるが
			//以下のロジックをテーブル毎に実装する必要があるため、実際はcc_collect_data_binaryの採番にのみ使用可
			Long maxId = QueryUtil.getMaxCollectBinaryDataId();

			if (maxId == null) {
				m_log.info("init() : Not found id, so start from 0.");
				DataId.generatorMap.put(type, new AtomicLong());
			} else {
				m_log.info(String.format("init() : Max of id is %d, so start from %d.", maxId, maxId));
				DataId.generatorMap.put(type, new AtomicLong(maxId));
			}
		} catch (Exception e) {
			m_log.warn("init() : Fail to get max of id, so start from 0.", e);
			DataId.generatorMap.put(type, new AtomicLong());
		}
	}

	public static Long getNext(GeneratorFor type) {
		if (DataId.generatorMap.get(type) == null) {
			init(type);
		}
		return DataId.generatorMap.get(type).incrementAndGet();
	}

	public static Long getCurrent(GeneratorFor type) {
		if (DataId.generatorMap.get(type) == null) {
			init(type);
		}
		return DataId.generatorMap.get(type).get();
	}

	public static Long getMax() {
		return Long.MAX_VALUE;
	}
}
