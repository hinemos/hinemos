/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.persistence;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.apache.log4j.Logger;


public class EntityExtension {
	private static class EntityContext {
		public Map<String, Property> processors = new HashMap<>();
	}

	private static class Property {
		public Class<?> propType;
		public Method getMetthod;
		public Method setMetthod;
		public List<Annotation> annotations = new ArrayList<>();
	}

	private static Map<Class<?>, EntityContext> contextMap = Collections.synchronizedMap(new HashMap<Class<?>, EntityContext>());

	private static EntityContext getEntityContext(Class<?> type) {
		EntityContext context = contextMap.get(type);
		if (context == null) {
			Logger logger = Logger.getLogger(EntityExtension.class);
			logger.debug("checking : " + type.getName());

			context = new EntityContext();
			contextMap.put(type, context);

			Map<String, Property> bufMap = new HashMap<>();
			for (Class<?> clazz = type; clazz != Object.class; clazz = clazz.getSuperclass()) {
				for (Method m: clazz.getMethods()) {
					logger.debug("methodName : " + m.getName());

					boolean isGet = false;
					for (Annotation anno: m.getAnnotations()) {
						logger.debug("annotation : " + anno.annotationType().getName());
						ProcessedBy pb = anno.annotationType().getAnnotation(ProcessedBy.class);
						if (pb != null) {
							String propName = null;
							Class<?> returnType = null;

							if (!isGet) {
								// get 関数か判定。
								// static 定義されているのは、対象外。
								if (Modifier.isStatic(m.getModifiers())) {
									throw new IllegalStateException();
								}

								// 共変は、対象外。
								if (m.isSynthetic()) {
									throw new IllegalStateException();
								}

								// 引数が 0 個。
								if (m.getParameterTypes().length != 0) {
									throw new IllegalStateException();
								}

								// 関数名の先頭は、get がつく。
								if (!m.getName().startsWith("get")) {
									throw new IllegalStateException();
								}

								// プロパティの型を抽出。
								returnType = m.getReturnType();

								// 戻り値は、void でない。
								if (Void.class.isAssignableFrom(returnType) || void.class.isAssignableFrom(returnType)) {
									throw new IllegalStateException();
								}

								propName = m.getName().substring("get".length());
								propName = Character.toLowerCase(propName.charAt(0)) + propName.substring(1);

								logger.debug("found GetMethod : " + m.getName());

								isGet = true;
							}

							Property buf = bufMap.get(propName);
							if (buf == null) {
								buf = new Property();
								buf.propType = returnType;
								bufMap.put(propName, buf);
							}
							else {
								if (!buf.propType.equals(returnType)) {
									throw new IllegalStateException();
								}
							}
							buf.getMetthod = m;
							buf.annotations.add(anno);
						}
					}

					if (!isGet) {
						// set メソッドの可能性があるので確認。
						if (m.getName().startsWith("set")) {
							logger.debug("found SetMethod : " + m.getName());

							// static 定義されているのは、対象外。
							if (Modifier.isStatic(m.getModifiers())) {
								continue;
							}

							// 引数が 1 個。
							if (m.getParameterTypes().length != 1) {
								continue;
							}

							// プロパティ名抽出。
							String propName = m.getName().substring("set".length());
							propName = Character.toLowerCase(propName.charAt(0)) + propName.substring(1);

							// パラメーターの型を取得。
							Class<?> paramType = m.getParameterTypes()[0];

							Property buf = bufMap.get(propName);
							if (buf == null) {
								buf = new Property();
								buf.propType = paramType;
								bufMap.put(propName, buf);
							}
							else {
								if (!buf.propType.equals(paramType)) {
									continue;
								}
							}
							buf.setMetthod = m;
						}
					}
				}
			}

			for (Map.Entry<String, Property> e: bufMap.entrySet()) {
				if (e.getValue().getMetthod == null) {
					continue;
				}
				if (e.getValue().setMetthod == null) {
					throw new IllegalStateException();
				}
				context.processors.put(e.getKey(), e.getValue());
			}
		}
		return context;
	}

	private static Map<Class<?>, List<Processor<?>>> processorsMap = new HashMap<>();

	private static synchronized List<Processor<?>> getProcessores(Class<?> type) {
		List<Processor<?>> processors = processorsMap.get(type);
		if (processors == null) {
			Method initMethod = null;
			try {
				initMethod = Processor.class.getMethod("init", Annotation.class, Method.class, Method.class);
			} catch (NoSuchMethodException | SecurityException e2) {
				throw new IllegalStateException(e2);
			}

			processors = new ArrayList<>();
			EntityContext context = getEntityContext(type);
			for (Map.Entry<String, Property> e: context.processors.entrySet()) {
				for (Annotation a: e.getValue().annotations) {
					ProcessedBy pb = a.annotationType().getAnnotation(ProcessedBy.class);
					try {
						Processor<?> p = pb.value().newInstance();
						initMethod.invoke(p, a, e.getValue().getMetthod, e.getValue().setMetthod);
						processors.add(p);
					}
					catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
						throw new IllegalStateException(e1);
					}
				}
			}
			processorsMap.put(type, processors);
		}
		return processors;
	}

	@PreRemove
	public void preRemove(Object entity) {
	}

	@PrePersist
	public void prePersist(Object entity) {
		List<Processor<?>> processors = getProcessores(entity.getClass());
		for (Processor<?> p: processors) {
			try {
				p.prePersist(entity);
			}
			catch (Exception e) {
				Logger.getLogger(this.getClass()).error(e.getMessage(), e);
			}
		}
	}

	@PostPersist
	public void postPersist(Object entity) {
		List<Processor<?>> processors = getProcessores(entity.getClass());
		for (Processor<?> p: processors) {
			try {
				p.postPersist(entity);
			}
			catch (Exception e) {
				Logger.getLogger(this.getClass()).error(e.getMessage(), e);
			}
		}
	}

	@PreUpdate
	public void preUpdate(Object entity) {
		List<Processor<?>> processors = getProcessores(entity.getClass());
		for (Processor<?> p: processors) {
			try {
				p.preUpdate(entity);
			}
			catch (Exception e) {
				Logger.getLogger(this.getClass()).error(e.getMessage(), e);
			}
		}
	}
	
	@PostUpdate
	public void postUpdate(Object entity) {
		List<Processor<?>> processors = getProcessores(entity.getClass());
		for (Processor<?> p: processors) {
			try {
				p.postUpdate(entity);
			}
			catch (Exception e) {
				Logger.getLogger(this.getClass()).error(e.getMessage(), e);
			}
		}
	}
	
	@PostLoad
	public void postLoad(Object entity) throws Exception {
		List<Processor<?>> processors = getProcessores(entity.getClass());
		for (Processor<?> p: processors) {
			try {
				p.postLoad(entity);
			}
			catch (Exception e) {
				Logger.getLogger(this.getClass()).error(e.getMessage(), e);
			}
		}
	}
}
