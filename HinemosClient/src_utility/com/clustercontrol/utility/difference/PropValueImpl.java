/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference;

/**
 * プロパティ値へのアクセサークラス。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 * 
 */
public class PropValueImpl implements PropValue {
	private Object real;
	private String translated;
	
	public PropValueImpl(Object real, String translated) {
		this.real = real;
		this.translated = translated;
	}
	
	@Override
	public Object getRealValue() {
		return real;
	}

	@Override
	public String getTranslatedString() {
		return translated;
	}

	@Override
	public String getResourceString() {
		return translated == null ? null: CSVUtil.getString(translated);
	}
	
	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof PropValueImpl) {
			PropValueImpl value = (PropValueImpl)anObject;
			return real.equals(value.real) && (translated == null ? translated == value.translated: translated.equals(value.translated));
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int h = 0;
		h = 31*h + real.hashCode();
		h = 31*h + (translated == null ? 0: translated.hashCode());
		return h;
	}
	
	@Override
	public String toString() {
		String toString = null;
		if (translated == null) {
			toString = real.toString();
		}
		else {
			toString = CSVUtil.getString(translated);
			if (toString == null) {
				toString = translated;
			}
		}
		return toString;
	}
}