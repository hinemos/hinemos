/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.utility.difference.anno.AnnoSubstitute;
import com.clustercontrol.utility.difference.anno.Element;

/**
 * 指定したオブジェクトに対して、指定した目印を検出するクラス。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 * 
 */
public class AnnotationSeeker {
	/**
	 * 目印が検出された際のコールバックのインターフェース。
	 * 
	 */
	public interface Listener {
		void found(Object parent, Method method, AnnoSubstitute anno, Class<?> returnType, Object prop);
	}

	private Object obj;
	private List<Class<? extends AnnoSubstitute>> targetAnnos;
	private Class<?> clazz;
	private Listener listener;

	public AnnotationSeeker(Object obj, Class<?> clazz, List<Class<? extends AnnoSubstitute>> targetAnnos, Listener listener) {
		this.obj = obj;
		this.clazz = clazz;
		this.targetAnnos = targetAnnos;
		this.listener = listener;
	}

	public void walk() {
		recursive(obj, clazz);
	}

	private void recursive(Object obj, Class<?> targetClass) {
		try {
			for (Class<?> clazz = targetClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
				DiffChecker.logger.debug("walk: " + clazz.getSimpleName());
				for (Method method : clazz.getDeclaredMethods()) {
					if (!DiffUtil.isPropGet(method)) {
						continue;
					}

					DiffChecker.logger.debug("propget: " + method.getName());
					List<AnnoSubstitute> annos = new ArrayList<AnnoSubstitute>();
					for (Class<? extends AnnoSubstitute> anno : targetAnnos) {
						AnnoSubstitute manno = DiffUtil.getAnnotation(method, anno);
						if (manno != null) {
							DiffChecker.logger.debug("found anno: " + manno.getClass().getSimpleName());
							annos.add(manno);
						}
					}

					if (!annos.isEmpty()) {
						// 戻り値の型を確認。
						Class<?> returnType = method.getReturnType();
						// 戻り値が配列か判定。
						if (returnType.isArray()) {
							Object array = method.invoke(obj);

							// 配列の型を取得。
							Class<?> componentType = returnType.getComponentType();
							for (int i = 0; i < Array.getLength(array); ++i) {
								Object prop = Array.get(array, i);
								for (AnnoSubstitute anno : annos) {
									listener.found(obj, method, anno, componentType, prop);
								}

								if (DiffUtil.getAnnotation(componentType, Element.class) != null) {
									recursive(prop, componentType);
								}
							}
						}
						else {
							Object prop = method.invoke(obj);
							for (AnnoSubstitute anno : annos) {
								listener.found(obj, method, anno, returnType, prop);
							}

							if (DiffUtil.getAnnotation(returnType, Element.class) != null) {
								recursive(prop, returnType);
							}
						}
					}
				}
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			
		} catch (Exception e) {
			throw new IllegalStateException("unexpected", e);
		}
	}
}