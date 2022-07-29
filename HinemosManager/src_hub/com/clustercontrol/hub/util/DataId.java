/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * DataIdGeneratorで生成するID種類を管理するクラス<br>
 *
 * @version 6.1.0
 * @since 6.1.0
 */
public class DataId {
	
	// DataIdGenerator以外から呼び出さないこと
	// IDを一意とするためstatic.
	/** ID取出し用Map */
	protected static Map<GeneratorFor, AtomicLong> generatorMap = new HashMap<GeneratorFor, AtomicLong>();
	
	// ID種類増やす場合は下記Map初期化とEnum追加すること.
	static {
		generatorMap.put(GeneratorFor.BINARY, null);
	}
	
	/** ID種類 */
	public enum GeneratorFor {
		BINARY
	}
}
