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

public interface Processor<A extends Annotation> {
	void init(A annotation, Method getMethod, Method setMethod) throws Exception;

	void postLoad(Object entity) throws Exception;
	void prePersist(Object entity) throws Exception;
	void postPersist(Object entity) throws Exception;
	void preUpdate(Object entity) throws Exception;
	void postUpdate(Object entity) throws Exception;
}
