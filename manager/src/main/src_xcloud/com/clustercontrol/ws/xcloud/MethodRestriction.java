/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.ws.xcloud;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class MethodRestriction {
	public static enum Status {
		enable,
		disable,
		none
	}
	
	@Retention(RetentionPolicy.RUNTIME)  
	@Target({ElementType.METHOD})
	public @interface Enable {
	}
	
	@Retention(RetentionPolicy.RUNTIME)  
	@Target({ElementType.METHOD})
	public @interface Disable {
	}
	
	private Class<?> target;
	private Map<Method, Boolean> map = new HashMap<>();
	
	public MethodRestriction(Class<?> target) {
		this.target = target;
	}
	
	public Status check(Method method) {
		Boolean check = map.get(method);
		return check == null ? Status.none: check ? Status.enable: Status.disable;
	}
	
	public void config(Class<?> configuration) {
		loop:
		for (Method method: configuration.getMethods()) {
			for (Annotation anno: method.getAnnotations()) {
				if (anno.annotationType() == Enable.class) {
					try {
						Method targetMethod = target.getMethod(method.getName(), method.getParameterTypes());
						assert targetMethod.getReturnType() == method.getReturnType();
						map.put(targetMethod, true);
						continue loop;
					} catch (NoSuchMethodException | SecurityException e) {
						Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
					}
				} if (anno.annotationType() == Disable.class) {
					try {
						Method targetMethod = target.getMethod(method.getName(), method.getParameterTypes());
						assert targetMethod.getReturnType() == method.getReturnType();
						map.put(targetMethod, false);
						continue loop;
					} catch (NoSuchMethodException | SecurityException e) {
						Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
					}
				}
			}
		}
	}
}
