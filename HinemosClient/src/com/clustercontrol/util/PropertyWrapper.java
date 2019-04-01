/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import com.clustercontrol.bean.Property;

/**
 * 特定のPropertyオブジェクトに対して、連続して操作を行う場合のコードを簡略化します。
 * 
 * @since 6.2.0
 */
public class PropertyWrapper {
	
	private Property target;

	public PropertyWrapper(Property target) {
		this.target = target;
	}

	public String findString(String id) {
		return PropertyUtil.findStringValue(target, id);
	}
	
	public Integer findInteger(String id) {
		return PropertyUtil.findIntegerValue(target, id);
	}
	
	public Long findTime(String id) {
		return PropertyUtil.findTimeValue(target, id);
	}

	public Long findEndTime(String id) {
		return PropertyUtil.findEndTimeValue(target, id);
	}
}
