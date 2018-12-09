/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.persistence;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.xcloud.InternalManagerError;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.bean.Filter;
import com.clustercontrol.xcloud.persistence.Transactional.TransactionOption;


public class PersistenceUtil {
	public static class TransactionScope implements AutoCloseable {
		private boolean completed = false;
		private boolean transactional = false;
		private boolean requiredNew = false;
		
		public TransactionScope() {
			this(Transactional.TransactionOption.Required);
		}

		public TransactionScope(Transactional.TransactionOption transactionType) {
			Session session = Session.present();
			switch (transactionType) {
			case Required:
				if (session == null) {
					Session.offer();
					session = Session.current();
					transactional = true;
					requiredNew = true;
				} else {
					transactional = !session.isTransaction();
				}
				break;
			case RequiredNew:
				if (session == null) {
					Session.offer();
					session = Session.current();
					transactional = true;
					requiredNew = true;
				} else {
					if (session.isTransaction()) {
						if (session.isPostCommitting())
							throw new InternalManagerError("postcommitting now");
						
						Session.offer();
						session = Session.current();
						transactional = true;
						requiredNew = true;
					} else {
						transactional = true;
					}
				}
				break;
			default:
				break;
			}
			
			if (transactional) {
				if (session.isPostCommitting())
					throw new InternalManagerError("postcommitting now");
				
				session.beginTransaction();
			}
		}
		
		public void complete() {
			completed = true;
		}
		
		@Override
		public void close() {
			try {
				if (!completed) {
					Session.current().rollbackTransaction();
				} else {
					if (transactional) Session.current().commitTransaction();
				}
			} finally {
				if (requiredNew) Session.poll();
			}
		}
	}
	
	private static class TransactionalInvoker2 implements InvocationHandler {
		private Object implementor;
		
		public TransactionalInvoker2(Object implementor) {
			this.implementor = implementor;
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			boolean requiredNew = false;
			try {
				boolean transactional = false;
				
				TransactionOption merged = null;
				Transactional ct = implementor.getClass().getAnnotation(Transactional.class);
				if (ct != null)
					merged = ct.value();

				Method m = implementor.getClass().getMethod(method.getName(), method.getParameterTypes());
				Transactional mt = m.getAnnotation(Transactional.class);
				if (mt != null)
					merged = mt.value();
				
				Session session = Session.present();
				if (merged != null) {
					switch (merged) {
					case Required:
						if (session == null) {
							Session.offer();
							session = Session.current();
							transactional = true;
							requiredNew = true;
						} else {
							transactional = !session.isTransaction();
						}
						break;
					case RequiredNew:
						if (session == null) {
							Session.offer();
							session = Session.current();
							transactional = true;
							requiredNew = true;
						} else {
							if (session.isTransaction()) {
								if (session.isPostCommitting())
									throw new InternalManagerError("postcommitting now");
								
								Session.offer();
								session = Session.current();
								transactional = true;
								requiredNew = true;
							} else {
								transactional = true;
							}
						}
						break;
					default:
						break;
					}
				}
				
				if (transactional) {
					if (session.isPostCommitting())
						throw new InternalManagerError("postcommitting now");
					
					session.beginTransaction();
					
					ClassLoader loader = Thread.currentThread().getContextClassLoader();
					Object result = null;
					try {
						Thread.currentThread().setContextClassLoader(implementor.getClass().getClassLoader());
						result = method.invoke(implementor, args);
					}
					catch (Exception e) {
						session.rollbackTransaction();
						throw e;
					} finally {
						Thread.currentThread().setContextClassLoader(loader);
					}
					
					session.commitTransaction();
					
					return result;
				}
				else {
					return method.invoke(implementor, args);
				}
			}
			catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
			catch (IllegalAccessException | IllegalArgumentException e) {
				throw e;
			}
			catch (RuntimeException e) {
				throw e;
			}
			catch (Exception e) {
				throw new InternalManagerError(e);
			}
			finally {
				if (requiredNew) Session.poll();
			}
		}
	}
	
	public static <T> T decorateTransactional(Class<T> interfaceClass, T implementer) {
		return 	interfaceClass.cast(Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, new TransactionalInvoker2(implementer)));
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> findAll(HinemosEntityManager em, Class<T> type) {
		Logger logger = Logger.getLogger(PersistenceUtil.class);
		logger.debug("findAll() start");
		
		try {
			Query q = em.getEntityManager().createQuery("select c from " + type.getSimpleName() + " c");
			return (List<T>)q.getResultList();
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}	
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> findByFilter(HinemosEntityManager em, Class<T> type, Filter... filters) {
		Logger logger = Logger.getLogger(PersistenceUtil.class);
		logger.debug("findByFilter() start");
		
		StringBuilder sb = new StringBuilder();
		sb.append("select c from ");
		sb.append(type.getSimpleName());
		sb.append(" c where ");
		
		boolean subsequent = false;
		for (int i = 0; i < filters.length; ++i) {
			Filter f = filters[i];
			if (subsequent) {
				sb.append(" and ");
			}
			
			if (f.getValues().size() > 1) {
				sb.append('(');
			}
			boolean subsequent2 = false;
			for (int j = 0; j < f.getValues().size(); ++j) {
				if (subsequent2) {
					sb.append(" or ");
				}
				sb.append("c.")
					.append(f.getName().replaceAll("''", "''"))
					.append(" like ")
					.append(String.format(":value_%d_%d", i, j));
				subsequent2 = true;
			}
			if (f.getValues().size() > 1) {
				sb.append(')');
			}
			
			subsequent = true;
		}
		
		String query = sb.toString();
		logger.debug(String.format("findByFilter() : query = \"%s\"", query));
		Query q = em.getEntityManager().createQuery(query);
		
		for (int i = 0; i < filters.length; ++i) {
			Filter f = filters[i];
			for (int j = 0; j < f.getValues().size(); ++j) {
				String v = f.getValues().get(j);
				q.setParameter(String.format("value_%d_%d", i, j), v);
			}
		}
		
		List<?> result = q.getResultList();
		if (result != null) {
			return (List<T>)result;
		}
		else {
			return Collections.emptyList();
		}
	}

	public static void persist(HinemosEntityManager em, Object entity) {
		if (entity instanceof IDHolder) {
			persist(em, (IDHolder)entity);
		} else {
			em.persist(entity);
		}
	}

	public static void persist(HinemosEntityManager em, IDHolder entity) {
		Object managed = em.find(entity.getClass(), entity.getId(), ObjectPrivilegeMode.READ);
		if (managed == null) {
			em.persist(entity);
		} else {
			// 重複エラー
			throw new EntityExistsException("{" + entity.getClass().getSimpleName() + ", " + entity.getId().toString() + "}");
		}
	}
}
