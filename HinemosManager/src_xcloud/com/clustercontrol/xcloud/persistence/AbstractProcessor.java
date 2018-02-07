/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.persistence;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public abstract class AbstractProcessor<A extends Annotation> implements Processor<A> {
	protected A annotation;
	protected Method getMethod;
	protected Method setMethod;
	
	@Override
	public void init(A annotation, Method getMethod, Method setMethod) {
		this.annotation = annotation;
		this.getMethod = getMethod;
		this.setMethod = setMethod;
	}

	@Override
	public void postLoad(Object entity) throws Exception {
	}

	@Override
	public void prePersist(Object entity) throws Exception {
	}

	@Override
	public void postPersist(Object entity) throws Exception {
	}

	@Override
	public void preUpdate(Object entity) throws Exception {
	}

	@Override
	public void postUpdate(Object entity) throws Exception {
	}

	public A getAnnotation() {
		return annotation;
	}

	public Method getGetMethod() {
		return getMethod;
	}

	public Method getSetMethod() {
		return setMethod;
	}
}
