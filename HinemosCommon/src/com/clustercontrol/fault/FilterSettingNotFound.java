/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * フィルタ設定が存在しない場合に投げます。
 */
public class FilterSettingNotFound extends HinemosNotFound {
	private static final long serialVersionUID = 6619115754758542057L;

	public FilterSettingNotFound() {
		super();
	}

	public FilterSettingNotFound(String message, Throwable e) {
		super(message, e);
	}

	public FilterSettingNotFound(String message) {
		super(message);
	}

	public FilterSettingNotFound(Throwable e) {
		super(e);
	}
}
