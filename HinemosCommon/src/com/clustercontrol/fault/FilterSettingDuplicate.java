/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * フィルタIDが重複している場合に投げます。
 */
public class FilterSettingDuplicate extends HinemosDuplicate {
	private static final long serialVersionUID = 7427945000355508609L;

	public FilterSettingDuplicate() {
		super();
	}

	public FilterSettingDuplicate(String messages) {
		super(messages);
	}

	public FilterSettingDuplicate(Throwable e) {
		super(e);
	}

	public FilterSettingDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

}
