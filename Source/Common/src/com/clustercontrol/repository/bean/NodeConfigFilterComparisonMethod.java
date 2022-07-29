/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 構成情報検索処理で使用する比較方法を定数として格納するクラス<BR>
 * 
 * @version 6.1.0
 */
public enum NodeConfigFilterComparisonMethod {
	
	GT(">", false),
	GE(">=", false),
	LT("<", false),
	LE("<=", false),
	EQ("=", true),
	NE("<>", true);

	private static final List<String> symbols;
	private static final List<String> symbolsForString;
	private final String symbol;
	private final Boolean isString;

	static {
		final List<String> _values = new ArrayList<>();
		final List<String> _valuesForString = new ArrayList<>();
		for (NodeConfigFilterComparisonMethod method : values()) {
			_values.add(method.symbol);
			if (method.isString) {
				_valuesForString.add(method.symbol);
			}
		}
		symbols = Collections.unmodifiableList(new CopyOnWriteArrayList<String>(_values));
		symbolsForString = Collections.unmodifiableList(new CopyOnWriteArrayList<String>(_valuesForString));
	}
	private NodeConfigFilterComparisonMethod(String symbol, Boolean isString) {
		this.symbol = symbol;
		this.isString = isString;
	}

	// 全ての値を返す
	public static Collection<String> symbols() {
		return symbols;
	}

	// 全ての値を返す(文字列用)
	public static Collection<String> symbolsForString() {
		return symbolsForString;
	}

	// 記号から型を返す
	public static NodeConfigFilterComparisonMethod symbolToType(String symbol) {
		NodeConfigFilterComparisonMethod rtn = null;
		if (symbol == null || symbol.isEmpty()) {
			return rtn;
		}
		for (NodeConfigFilterComparisonMethod method : values()) {
			if (method.symbol.equals(symbol)) {
				rtn = method;
				break;
			}
		}
		return rtn;
	}

	// 記号を返す
	public String symbol() {
		return symbol;
	}

	// 文字列で使用可能か返す
	public Boolean isString() {
		return isString;
	}
}