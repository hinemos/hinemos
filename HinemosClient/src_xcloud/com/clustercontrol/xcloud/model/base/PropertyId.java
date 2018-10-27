/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.base;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.clustercontrol.xcloud.model.CloudModelException;

public class PropertyId<L extends PropertyObserver<?>> {
	protected String propertyName;
	protected boolean composite;
	
	public Class<? extends PropertyObserver<?>> observerClass;
	public Type propType;
	
	public PropertyId(String propertyName) {
		this.propertyName = propertyName;

		Type type = ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		checkType(null, null, type);
	}

	public PropertyId(String propertyName, boolean composite) {
		this.propertyName = propertyName;
		this.composite = composite;

		Type type = ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		checkType(null, null, type);
		
		if (composite && !((propType instanceof Class) && (IElement.class.isAssignableFrom((Class<?>)propType))))
			throw new CloudModelException("property must inherit IElement if composite is true.");
	}

	public String getPropertyName() {
		return propertyName;
	}

	public Class<?> getObserverClass() {
		return observerClass;
	}

	public Type getPropertyType() {
		return propType;
	}

	public boolean isComposite() {
		return composite;
	}

	@SuppressWarnings("unchecked")
	protected boolean checkType(Class<?> subClass, Type subParam, Type type) {
		if (type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType)type;
			
			Type arg = pt.getActualTypeArguments()[0];
			if (arg instanceof Class ||
				arg instanceof ParameterizedType) {
				subParam = arg;
			}
			
			Class<?> c = (Class<?>)pt.getRawType();
			if (c == PropertyObserver.class) {
				propType = subParam;
				observerClass = (Class<? extends PropertyObserver<?>>)subClass;
				return true;
			}
			
			if (checkClass(subParam, c))
				return true;
		} else if (type instanceof Class) {
			if (checkClass(subParam, (Class<?>)type))
				return true;
		}
		return false;
	}
	
	protected boolean checkClass(Type subParam, Class<?> c) {
		Type[] ts = c.getGenericInterfaces();
		for (Type t: ts) {
			if (checkType(c, subParam, t))
				return true;
		}
		if (!c.isInterface())
			if (checkType(c, subParam, c.getGenericSuperclass()))
				return true;
		
		return false;
	}
}
