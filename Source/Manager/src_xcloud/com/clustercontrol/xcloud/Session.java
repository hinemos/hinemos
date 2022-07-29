/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jakarta.persistence.RollbackException;

import org.apache.log4j.Logger;

import com.clustercontrol.commons.util.EmptyJpaTransactionCallback;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.xcloud.persistence.TransactionException;
import com.clustercontrol.xcloud.util.CloudUtil;

public class Session {
	public static class SessionScope implements AutoCloseable {
		enum Option {
			Required,
			RequiredNew
		};
		
		private boolean isolated;
		private HinemosEntityManager backupedEm;
		private List<JpaTransactionCallback> backupedCallbacks;
		
		private boolean acruired;
		
		private Session prev = null;
		
		public SessionScope(boolean isolate) {
			this(Option.Required, isolate);
		}
		
		@SuppressWarnings("unchecked")
		public SessionScope(Option option, boolean isolate) {
			if (!Session.isExist() && isolate) {
				HinemosEntityManager em = (HinemosEntityManager)HinemosSessionContext.instance().getProperty(JpaTransactionManager.EM);
				if (em != null) {
					isolated = true;
					backupedEm = (HinemosEntityManager)HinemosSessionContext.instance().getProperty(JpaTransactionManager.EM);
					backupedCallbacks = (List<JpaTransactionCallback>)HinemosSessionContext.instance().getProperty(JpaTransactionManager.CALLBACKS);
					HinemosSessionContext.instance().setProperty(JpaTransactionManager.EM, null);
					HinemosSessionContext.instance().setProperty(JpaTransactionManager.CALLBACKS, null);
				}
			}
			
			switch(option) {
			case Required:
				if (!Session.isExist()) {
					prev = Session.present();
					Session.offer();
					acruired = true;
				}
				break;
			case RequiredNew:
				prev = Session.present();
				Session.offer();
				acruired = true;
				break;
			}
		}
		
		@SuppressWarnings("unchecked")
		public SessionScope(ContextBean context, boolean isolate) {
			if (!Session.isExist() && isolate) {
				HinemosEntityManager em = (HinemosEntityManager)HinemosSessionContext.instance().getProperty(JpaTransactionManager.EM);
				if (em != null) {
					isolated = true;
					backupedEm = (HinemosEntityManager)HinemosSessionContext.instance().getProperty(JpaTransactionManager.EM);
					backupedCallbacks = (List<JpaTransactionCallback>)HinemosSessionContext.instance().getProperty(JpaTransactionManager.CALLBACKS);
					HinemosSessionContext.instance().setProperty(JpaTransactionManager.EM, null);
					HinemosSessionContext.instance().setProperty(JpaTransactionManager.CALLBACKS, null);
				}
			}
			
			prev = Session.present();
			Session.offer(context);
			acruired = true;
		}

		public Session previous() {
			return prev;
		}
		
		public static SessionScope open() {
			return new SessionScope(true);
		}
		
		public static SessionScope merge() {
			return new SessionScope(false);
		}
		
		public static SessionScope open(Option option) {
			return new SessionScope(option, true);
		}
		
		public static SessionScope open(ContextBean context) {
			return new SessionScope(context, true);
		}
		
		@Override
		public void close() {
			if (acruired) Session.poll();
			
			if (isolated) {
				HinemosSessionContext.instance().setProperty(JpaTransactionManager.EM, backupedEm);
				HinemosSessionContext.instance().setProperty(JpaTransactionManager.CALLBACKS, backupedCallbacks);
			}
		}
	}
	
	private interface TransactionCore {
		void store();
		void restore();
		HinemosEntityManager getEntityManagerEx();
		void beginTransaction();
		void commitTransaction();
		void rollbackTransaction();
		void flushPersistenceContext();
		void closeTransactionManager();
		boolean isTransaction();
		boolean isTransactionInitiate();
		void addPreCommitAction(PreCommitAction action);
		void addPostCommitAction(PostCommitAction action);
		void addRollbackAction(RolebackAction action);
	}
	
	private static class Primary implements TransactionCore {
		private interface TransactionOperator {
			void beginTransaction();
			void commitTransaction();
			void rollbackTransaction();
			void flushPersistenceContext();
		}
		
		private TransactionOperator operator;
		
		private JpaTransactionManager hinemosTM;
		
		private HinemosEntityManager backupedEm;
		private List<JpaTransactionCallback> backupedCallbacks;
		
		public Primary() {
		}
		protected JpaTransactionManager getJpaTransactionManager() {
			if (hinemosTM == null) {
				Logger.getLogger(Session.class).debug("new JpaTransactionManager.");
				hinemosTM = new JpaTransactionManager();
				Logger.getLogger(Session.class).debug(hinemosTM.isNestedEm() ? String.format("JpaTransactionManager(%d) is nested.", hinemosTM.hashCode()): "JpaTransactionManager is not nested.");
			}
			return hinemosTM;
		}
		@Override
		public HinemosEntityManager getEntityManagerEx() {
			return getJpaTransactionManager().getEntityManager();
		}
		@Override
		public void beginTransaction() {
			Logger.getLogger(Session.class).debug("bigin transaction.");
			
			if (!getJpaTransactionManager().getEntityManager().getTransaction().isActive()) {
				operator = new TransactionOperator() {
					@Override
					public void beginTransaction() {
						getJpaTransactionManager().begin();
					}
					@Override
					public void commitTransaction() {
						if (getJpaTransactionManager().getEntityManager().getTransaction().isActive()) {
							try {
								try {
									getJpaTransactionManager().commit();
								} catch (Exception e1) {
									if (!(e1 instanceof RollbackException)) {
										try {
											getJpaTransactionManager().rollback();
										} catch (Exception e2) {
											Logger.getLogger(this.getClass()).error(e2.getMessage(), e2);
										}
									}
									throw e1;
								}
							} finally {
//								hinemosTM.close();
//								hinemosTM = null;
							}
						}
					}
					@Override
					public void rollbackTransaction() {
						if (getJpaTransactionManager().getEntityManager().getTransaction().isActive()) {
							try {
								getJpaTransactionManager().rollback();
							}
							finally {
//								hinemosTM.close();
//								hinemosTM = null;
							}
						}
					}
					@Override
					public void flushPersistenceContext() {
						if (getJpaTransactionManager().getEntityManager().getTransaction().isActive()) {
							try {
								getJpaTransactionManager().flush();
							} catch (Exception e1) {
								try {
									throw e1;
									
								} finally {
//									hinemosTM.close();
//									hinemosTM = null;
								}
							}
						}
					}
				};
				operator.beginTransaction();
			} else {
				throw new InternalManagerError();
			}
		}
		@Override
		public void commitTransaction() {
			if (operator != null) {
				operator.commitTransaction();
			} else {
				throw new InternalManagerError();
			}
		}
		@Override
		public void rollbackTransaction() {
			if (operator != null) {
				operator.rollbackTransaction();
			} else {
				throw new InternalManagerError();
			}
		}
		@Override
		public void flushPersistenceContext() {
			if (operator != null) {
				operator.flushPersistenceContext();
			} else {
				throw new InternalManagerError();
			}
		}
		
		@Override
		public void closeTransactionManager() {
			if (hinemosTM != null) {
				Logger.getLogger(Session.class).debug("close JpaTransactionManager");
				Logger.getLogger(Session.class).debug(hinemosTM.isNestedEm() ? String.format("JpaTransactionManager(%d) is nested.", hinemosTM.hashCode()): "JpaTransactionManager is not nested.");
				hinemosTM.close();
				hinemosTM = null;
				operator = null;
			}
		}

		@Override
		public boolean isTransaction() {
			return getJpaTransactionManager().getEntityManager().getTransaction().isActive();
		}
		@Override
		public void addPreCommitAction(final PreCommitAction action) {
			if (!isTransaction())
				throw new UnsupportedOperationException();
			getJpaTransactionManager().addCallback(new EmptyJpaTransactionCallback() {
				@Override
				public void preCommit() {
					action.preCommit();
				}			
			});
		}
		@Override
		public void addPostCommitAction(final PostCommitAction action) {
			if (!isTransaction())
				throw new UnsupportedOperationException();
			getJpaTransactionManager().addCallback(new EmptyJpaTransactionCallback() {
				@Override
				public void postCommit() {
					action.postCommit();
				}
			});
		}
		@Override
		public void addRollbackAction(final RolebackAction action) {
			if (!isTransaction())
				throw new UnsupportedOperationException();

			getJpaTransactionManager().addCallback(new EmptyJpaTransactionCallback() {
				@Override
				public void postRollback() {
					action.rollback();;
				}
			});
		}
		@SuppressWarnings("unchecked")
		@Override
		public void store() {
			backupedEm = (HinemosEntityManager)HinemosSessionContext.instance().getProperty(JpaTransactionManager.EM);
			backupedCallbacks = (List<JpaTransactionCallback>)HinemosSessionContext.instance().getProperty(JpaTransactionManager.CALLBACKS);
			HinemosSessionContext.instance().setProperty(JpaTransactionManager.EM, null);
			HinemosSessionContext.instance().setProperty(JpaTransactionManager.CALLBACKS, null);
		}
		@Override
		public void restore() {
			HinemosSessionContext.instance().setProperty(JpaTransactionManager.EM, backupedEm);
			HinemosSessionContext.instance().setProperty(JpaTransactionManager.CALLBACKS, backupedCallbacks);
		}
		@Override
		public boolean isTransactionInitiate() {
			return operator != null ? isTransaction(): false;
		}
	}
	
	
	public interface PreCommitAction {
		RolebackAction preCommit() throws TransactionException;
	}

	public interface PostCommitAction {
		void postCommit() throws TransactionException;
	}

	public interface RolebackAction {
		void rollback() throws TransactionException;
	}

	public interface ISessionInitializer {
		void initialize(Session session);
		void close(Session session);
	}

	public static class ContextBean {
		private String hinemosUser;
		private boolean isAdministrator;
		private Map<Object, Boolean> states = new HashMap<>();
		private Map<Class<?>, Object> objects = new HashMap<>();
		private Map<Object, Object> properties = new HashMap<>();
		private HinemosCredential credential = new HinemosCredential();
	}

	private static Logger logger = Logger.getLogger(Session.class);

	private Map<Object, Boolean> states = new HashMap<>();
	private Map<Class<?>, Object> objects = new HashMap<>();
	private Map<Object, Object> properties = new HashMap<>();

	private HinemosCredential credential = new HinemosCredential();
	
	private TransactionCore transactionCore;
	
	private Session() {
	}

	private Session(ContextBean context) {
		this();
		HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, context.hinemosUser);
		HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, context.isAdministrator);

		this.states.putAll(context.states);
		this.objects.putAll(context.objects);
		this.properties.putAll(context.properties);
		this.credential = context.credential;
	}

	public Object getProperty(Object key) {
		return properties.get(key);
	}

	public void setProperty(Object key, Object value) {
		properties.put(key, value);
	}

	public <T> T get(Class<T> clazz) {
		return clazz.cast(objects.get(clazz));
	}

	public <T> void set(Class<? extends T> clazz, T object) {
		objects.put(clazz, object);
	}

	public void beginTransaction() {
		getTransactionCore().beginTransaction();
	}

	public void commitTransaction() {
		getTransactionCore().commitTransaction();
	}

	public void rollbackTransaction() {
		getTransactionCore().rollbackTransaction();
	}

	public void flushPersistenceContext() {
		getTransactionCore().flushPersistenceContext();
	}

	public void closeTransactionManager() {
		getTransactionCore().closeTransactionManager();
	}

	public HinemosCredential getHinemosCredential() {
		return credential;
	}

	public void setHinemosCredential(HinemosCredential credential) {
		this.credential = credential;
	}

	public Map<Object, Object> getProperties() {
		return properties;
	}

	public Map<Class<?>, ?> getObjects() {
		return objects;
	}

	public HinemosEntityManager getEntityManager() {
		return getTransactionCore().getEntityManagerEx();
	}
	
	protected TransactionCore getTransactionCore() {
		if (transactionCore == null)
			transactionCore = new Primary();
		return transactionCore;
	}

	public boolean isDebugEnbled() {
		return logger.isDebugEnabled();
	}

	public boolean isTransaction() {
		return getTransactionCore().isTransaction();
	}
	
	boolean isTransactionInitiate() {
		return getTransactionCore().isTransactionInitiate();
	}


	public void addPreCommitAction(PreCommitAction action) {
		getTransactionCore().addPreCommitAction(action);
	}

	public void addRollbackAction(RolebackAction action) {
		getTransactionCore().addRollbackAction(action);
	}

	public void addPostCommitAction(PostCommitAction action) {
		getTransactionCore().addPostCommitAction(action);
	}
	
	public ContextBean getContext() {
		// スレッドローカルの情報を詰め込む可能性があるので、その問題が出たら対処。
		ContextBean data = new ContextBean();

		data.states.putAll(getStates());
		data.objects.putAll(getObjects());
		data.properties.putAll(getProperties());
		data.credential = getHinemosCredential();
		data.hinemosUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		data.isAdministrator = Boolean.TRUE.equals(HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR)) ? Boolean.TRUE: Boolean.FALSE;

		return data;
	}

	public void setState(Object key, boolean state) {
		states.put(key, state);
	}

	public boolean isState(Object key) {
		Boolean state = states.get(key);
		return state != null ? state: false;
	}

	public Map<Object, Boolean> getStates() {
		return states;
	}

	protected void release() {
		closeTransactionManager();
		
		synchronized (initializers) {
			for (ISessionInitializer initializer: initializers) {
				initializer.close(this);
			}
		}
	}

	private static ThreadLocal<Deque<Session>> deque  = new ThreadLocal<Deque<Session>>() {
		protected Deque<Session> initialValue() {
			return new LinkedList<Session>();
		}
	};

	private static List<ISessionInitializer> initializers = Collections.synchronizedList(new ArrayList<ISessionInitializer>());
	
	public static Session current() {
		if (!Session.isExist()) {
			String stack = CloudUtil.getStackTrace(Thread.currentThread());
			logger.warn("Session.current() is called on illegally." + stack);
		}
		Session session = deque.get().peekFirst();
		if (session != null) return session;
		offer();
		return deque.get().peekFirst();
	}
	
	public static Session present() {
		return deque.get().peekFirst();
	}
	
	public static boolean isExist() {
		return !deque.get().isEmpty();
	}
	
	public static void offer() {
		Session session = deque.get().peekFirst();
		offer(session != null ? session.getContext(): null);
	}

	public static void offer(ContextBean context) {
		Logger.getLogger(Session.class).debug("offer session.");
		Session newSession = context == null ? new Session(): new Session(context);
		logger.debug(String.format("offer : new=%d", newSession.hashCode()));
		synchronized (initializers) {
			for (ISessionInitializer initializer: initializers) {
				initializer.initialize(newSession);
			}
		}
		
		Session oldSession = deque.get().peekFirst();
		if (oldSession != null && oldSession.transactionCore != null) {
			logger.debug(String.format("offer : old=%d", oldSession.hashCode()));
			oldSession.transactionCore.store();
		}

		deque.get().offerFirst(newSession);
	}

	public static void poll() {
		Logger.getLogger(Session.class).debug("poll session.");
		Session session = deque.get().peekFirst();
		if (session != null) {
			logger.debug(String.format("poll : current=%d", session.hashCode()));
			if (session.isTransactionInitiate()) throw new InternalManagerError("Transaction is active.");

			Logger.getLogger(Session.class).debug("session release.");
			session.release();
			Session.deque.get().pollFirst();

			session = deque.get().peekFirst();
			if (session != null && session.transactionCore != null) {
				logger.debug(String.format("poll : restore=%d", session.hashCode()));
				session.transactionCore.restore();
			}
		} else {
			logger.debug("poll : session not exist.");
		}
	}

	public static void addInitializer(ISessionInitializer initializer) {
		initializers.add(initializer);
	}
}
