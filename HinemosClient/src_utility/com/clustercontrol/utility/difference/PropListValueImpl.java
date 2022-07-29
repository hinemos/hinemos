/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference;

import java.util.Arrays;

public class PropListValueImpl implements PropValue {
	private Object[] real;
	private String[] translated;
	
	public PropListValueImpl(Object[] real, String...translated) {
		this.real = real;
		this.translated = translated;
	}
	
	@Override
	public Object getRealValue() {
		return real;
	}

	@Override
	public String getTranslatedString() {
		return Arrays.toString(translated);
	}

	@Override
	public String getResourceString() {
		if (translated == null) {
			return null;
		} else {
			StringBuffer rs = new StringBuffer();
			for (String t : translated) {
				rs.append(CSVUtil.getString(t));
			}
			return rs.toString();
		}
	}
	
	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof PropListValueImpl) {
			PropListValueImpl value = (PropListValueImpl)anObject;
			
			boolean isRealEquaos = false;
			isRealEquaos = Arrays.asList(real).containsAll(Arrays.asList(value.real))
						&& Arrays.asList(value.real).containsAll(Arrays.asList(real)); 
			boolean isTranslatedEquals = false;
			if (translated == null) {
				isTranslatedEquals = translated == value.translated;
			} else if (translated.length == 0) {
				isTranslatedEquals = translated.length == value.translated.length;
			} else {
				isTranslatedEquals = Arrays.asList(translated).containsAll(Arrays.asList(value.translated))
						&& Arrays.asList(value.translated).containsAll(Arrays.asList(translated));
			}
			return isRealEquaos && isTranslatedEquals;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int h = 0;
		h = 31*h + Arrays.hashCode(real);
		h = 31*h + (translated == null ? 0: Arrays.hashCode(translated));
		return h;
	}
	
	@Override
	public String toString() {
		String toString = null;
		if (translated == null) {
			toString = Arrays.toString(real);
		}
		else {
			StringBuffer rs = new StringBuffer();
			rs.append("[");
			for (String t : translated) {
				if (rs.length() > 1) {
					rs.append(":");
				}
				rs.append("\"");
				rs.append(CSVUtil.getString(t));
				rs.append("\"");
			}
			rs.append("]");
			toString = rs.toString();
		}
		return toString;
	}
}