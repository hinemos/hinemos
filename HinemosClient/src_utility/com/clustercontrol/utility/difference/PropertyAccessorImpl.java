/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference;

import java.lang.reflect.Method;

import com.clustercontrol.utility.difference.anno.Translate;
import com.clustercontrol.utility.difference.anno.TranslateOverrides;

/**
 * PropertyAccessor の実装クラス。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
public class PropertyAccessorImpl implements PropertyAccessor {
	private Class<?> targetClass;
	private TranslateOverrides to;

	@Override
	public Class<?> getType() {
		return targetClass;
	}

	public PropertyAccessorImpl(Class<?> targetClass, TranslateOverrides to) {
		this.targetClass = targetClass;
		this.to = to;
	}

	@Override
	public PropValue getProperty(Object obj, String propName) {
		PropValue prop = null;
		Method method = DiffUtil.getPropGetMethod(propName, targetClass);
		if (method != null) {
			prop = getProperty(obj, method);
		}

		return prop;
	}
	
	@Override
	public PropValue getProperty(Object obj, Method propGet) {
		Object prop = DiffUtil.getProperty(obj, propGet);
		
		if (prop == null) {
			return null;
		}

		String propName = propGet.getName().substring("get".length());
		return new PropValueImpl(prop, translate(prop, propName, propGet));
	}

	private String translate(Object prop, String propName, Method propGet) {
		Translate t = null;
		if (to != null) {
			for (TranslateOverrides.Value v: to.values) {
				if (propName.compareToIgnoreCase(v.p) == 0) {
					t = v.t;
					break;
				}
			}
		}
		
		if (t == null) {
			t = DiffUtil.getAnnotation(propGet, Translate.class);
		}

		String translated = null;
		if (t != null) {
			translated = translate(prop, propGet.getReturnType(), t);
		}
		
		return translated;
	}

	private String translate(Object prop, Class<?> c, Translate t) {
		for (Translate.Value v : t.values) {
			Object value = null;
			if (int.class == c) {
				value = Integer.valueOf(v.value);
			}
			else if (boolean.class == c) {
				value = Boolean.valueOf(v.value);
			}
			else if (double.class == c) {
				value = v.value;
			}
			else if (String.class == c) {
				value = v.value;
			}
			else {
				throw new IllegalStateException("unexpected");
			}

			if (prop.equals(value)) {
				return v.name;
			}
		}
		
		return null;
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof PropertyAccessorImpl) {
			PropertyAccessorImpl pa = (PropertyAccessorImpl)anObject;
			return targetClass.equals(pa.targetClass) && to.equals(pa.to);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return 31*targetClass.hashCode() + to.hashCode();
	}
}