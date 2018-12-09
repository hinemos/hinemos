/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.analytics.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 収集値統合監視で使用する比較方法を定数として格納するクラス<BR>
 * 
 * @version 6.1.0
 */
public enum IntegrationComparisonMethod {
	
	GT(">"),
	GE(">="),
	LT("<"),
	LE("<="),
	EQ("=");

	private static final List<String> symbols;
	private final String symbol;

	static {
		final List<String> values = new ArrayList<String>();
		for (IntegrationComparisonMethod method : values()) {
			values.add(method.symbol);
		}
		symbols = Collections.unmodifiableList(new CopyOnWriteArrayList<String>(values));
	}
	private IntegrationComparisonMethod(String symbol) {
		this.symbol = symbol;
	}

	// 全ての値を返す
	public static Collection<String> symbols() {
		return symbols;
	}

	// 記号を返す
	public String symbol() {
		return symbol;
	}
}