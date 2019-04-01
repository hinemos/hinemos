/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.util.ObjectPrivilegeCallback;
import com.clustercontrol.fault.HinemosUnknown;
/*
 * JPA用のトランザクション制御機能
 */
public class JpaTransactionManager implements AutoCloseable {

	private static Log m_log = LogFactory.getLog(JpaTransactionManager.class);

	public final static String EM = "entityManager";
	public final static String CALLBACKS = "callbackImpl";
	public final static String IS_CALLBACKED = "isCallbacked";

	// HinemosEntityManager
	private HinemosEntityManager em = null;
	// HinemosEntityManagerが既に起動されているか
	private boolean nestedEm = false;
	// EntityTransaction
	private EntityTransaction tx = null;
	// EntityTransactionが既に起動されているか
	private boolean nestedTx = false;

	/**
	 * コンストラクタ
	 */
	public JpaTransactionManager() {
		// EntityManager生成
		em = (HinemosEntityManager)HinemosSessionContext.instance().getProperty(EM);
		if (em == null) {
			em = new HinemosEntityManager();
			em.setEntityManager(JpaPersistenceConfig.getHinemosEMFactory().createEntityManager());
			HinemosSessionContext.instance().setProperty(EM, em);
			
			clearCallbacks(); 
			unsetCallbacked();
		} else {
			nestedEm = true;
		}
	}

	/**
	 * トランザクション開始
	 * 
	 * @param abortIfTxBegined trueかつ既にトランザクションが開始されていればException
	 */
	public void begin(boolean abortIfTxBegined) throws HinemosUnknown {
		// EntityTransaction開始
		tx = em.getTransaction();
		nestedTx = tx.isActive();

		if (!nestedTx) {
			//callbackがcommitの度に呼ばれないようにここでclearしておく
			clearTransactionCallbacks(); 
			addCallback(new ObjectPrivilegeCallback());
			
			tx.begin();
			
		} else {
			if (abortIfTxBegined) {
				HinemosUnknown e = new HinemosUnknown("transaction has already started.");
				m_log.info("begin() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
	}

	/**
	 * トランザクション開始
	 * (トランザクションを引き継ぐ)
	 */
	public void begin() {
		try {
			begin(false);
		} catch (HinemosUnknown e) {
			m_log.debug(e.getMessage(), e);
		}
	}

	/**
	 * コミット処理
	 * 
	 * @param isGetTransaction true：EntityManagerよりトランザクションを取得する
	 */
	public void commit(boolean isGetTransaction) {
		if (isGetTransaction) {
			tx = em.getTransaction();
		}
		if (!nestedTx) {
			if (! isCallbacked()) {
				List<JpaTransactionCallback> callbacks = getCallbacks();
				for (JpaTransactionCallback callback : callbacks) {
					if (m_log.isDebugEnabled()) {
						m_log.debug("executing callback preCommit : " + callback.getClass().getName());
					}
					try {
						setCallbacked();
						
						callback.preCommit();
					} catch (Throwable t) {
						m_log.warn("callback execution failure : " + callback.getClass().getName());
						throw t;
					} finally {
						unsetCallbacked();
					}
				}
			}

			tx.commit();
			
			if (! isCallbacked()) {
				List<JpaTransactionCallback> callbacks = getCallbacks();
				for (JpaTransactionCallback callback : callbacks) {
					if (m_log.isDebugEnabled()) {
						m_log.debug("executing callback postCommit : " + callback.getClass().getName());
					}
					try {
						setCallbacked();
						
						callback.postCommit();
					} catch (Throwable t) {
						m_log.warn("callback execution failure : " + callback.getClass().getName());
						throw t;
					} finally {
						unsetCallbacked();
					}
				}
			}
		}
	}

	/**
	 * コミット処理
	 * （begin()時に取得したトランザクションを使用する）
	 */
	public void commit() {
		commit(false);
	}

	/**
	 * フラッシュ処理
	 * (JPAではクエリの順序性が保証されないため、INSERT -> DELETEという順序性が求められる場合、
	 * INSERT(persist) -> flush -> DELETE(remove)という処理が必要である。
	 * flushしないと、DELETE -> INSERTという順序となり、SQLExceptionが生じる可能性がある。
	 */
	public void flush() {
		if (! isCallbacked()) {
			List<JpaTransactionCallback> callbacks = getCallbacks();
			for (JpaTransactionCallback callback : callbacks) {
				if (m_log.isDebugEnabled()) {
					m_log.debug("executing callback preFlush : " + callback.getClass().getName());
				}
				try {
					setCallbacked();
					
					callback.preFlush();
				} catch (Throwable t) {
					m_log.warn("callback execution failure : " + callback.getClass().getName(), t);
				} finally {
					unsetCallbacked();
				}
			}
		}
		
		em.flush();
		
		if (! isCallbacked()) {
			List<JpaTransactionCallback> callbacks = getCallbacks();
			for (JpaTransactionCallback callback : callbacks) {
				if (m_log.isDebugEnabled()) {
					m_log.debug("executing callback postFlush : " + callback.getClass().getName());
				}
				try {
					setCallbacked();
					
					callback.postFlush();
				} catch (Throwable t) {
					m_log.warn("callback execution failure : " + callback.getClass().getName(), t);
				} finally {
					unsetCallbacked();
				}
			}
		}
	}

	/**
	 * ロールバック処理
	 */
	public void rollback() {
		if (!nestedTx) {
			if (tx != null && tx.isActive()) {
				m_log.debug("session is rollback.");
				
				if (! isCallbacked()) {
					List<JpaTransactionCallback> callbacks = getCallbacks();
					for (JpaTransactionCallback callback : callbacks) {
						if (m_log.isDebugEnabled()) {
							m_log.debug("executing callback preRollback : " + callback.getClass().getName());
						}
						try {
							setCallbacked();
							
							callback.preRollback();
						} catch (Throwable t) {
							m_log.warn("callback execution failure : " + callback.getClass().getName(), t);
						} finally {
							unsetCallbacked();
						}
					}
				}

				tx.rollback();
				
				if (! isCallbacked()) {
					List<JpaTransactionCallback> callbacks = getCallbacks();
					for (JpaTransactionCallback callback : callbacks) {
						if (m_log.isDebugEnabled()) {
							m_log.debug("executing callback postRollback : " + callback.getClass().getName());
						}
						try {
							setCallbacked();
							
							callback.postRollback();
						} catch (Throwable t) {
							m_log.warn("callback execution failure : " + callback.getClass().getName(), t);
						} finally {
							unsetCallbacked();
						}
					}
				}
			}
		}
	}

	/**
	 * クローズ処理
	 */
	public void close() {
		if(!nestedEm && em != null) {
			if(em.isOpen()) {
				try {
					List<JpaTransactionCallback> callbacks = getCallbacks();
					
					if (! isCallbacked()) {
						for (JpaTransactionCallback callback : callbacks) {
							if (m_log.isDebugEnabled()) {
								m_log.debug("executing callback preClose : " + callback.getClass().getName());
							}
							try {
								setCallbacked();
								
								callback.preClose();
							} catch (Throwable t) {
								m_log.warn("callback execution failure : " + callback.getClass().getName(), t);
							} finally {
								unsetCallbacked();
							}
						}
					}
					
					// commit or rollbackを発行せずにcloseに到達した悪しき実装は喝(rollback)！
					// （中途半端な状態のconnectionをプールに戻してはいけません）
					EntityTransaction tx = em.getTransaction();
					if (tx.isActive()) {
						if (m_log.isDebugEnabled()) {
							StackTraceElement[] eList = Thread.currentThread().getStackTrace();
							StringBuilder trace = new StringBuilder();
							for (StackTraceElement e : eList) {
								if (trace.length() > 0) {
									trace.append("\n");
								}
								trace.append(String.format("%s.%s(%s:%d)", e.getClassName(), e.getMethodName(), e.getFileName(), e.getLineNumber()));
							}
							m_log.debug("closing uncompleted transaction. this transaction will be rollbacked before closing : " + trace);
						}
						tx.rollback();
					}
					
					em.close();
					HinemosSessionContext.instance().setProperty(JpaTransactionManager.EM, null);
					
					// postCloseのみはinnerTransactionとならないため、例外的にcallbackを実行可能とする
					for (JpaTransactionCallback callback : callbacks) {
						if (m_log.isDebugEnabled()) {
							m_log.debug("executing callback postClose : " + callback.getClass().getName());
						}
						try {
							callback.postClose();
						} catch (Throwable t) {
							m_log.warn("callback execution failure : " + callback.getClass().getName(), t);
						}
					}
				} finally {
					HinemosSessionContext.instance().setProperty(JpaTransactionManager.EM, null);
				}
			}
			HinemosSessionContext.instance().setProperty(EM, null);
		}
	}

	/**
	 * クローズ処理
	 * エージェントを利用する、または、システムログ監視を利用する場合のみ利用する
	 */
	public void close(String monitor) {
		if(!nestedEm && em != null) {
			if(em.isOpen()) {
				try {
					List<JpaTransactionCallback> callbacks = getCallbacks();
					
					if (! isCallbacked()) {
						for (JpaTransactionCallback callback : callbacks) {
							if (m_log.isDebugEnabled()) {
								m_log.debug("executing callback preClose : " + callback.getClass().getName());
							}
							try {
								setCallbacked();
								
								callback.preClose();
							} catch (Throwable t) {
								m_log.warn("callback execution failure : " + callback.getClass().getName(), t);
							} finally {
								unsetCallbacked();
							}
						}
					}
					
					// commit or rollbackを発行せずにcloseに到達した悪しき実装は喝(rollback)！
					// （中途半端な状態のconnectionをプールに戻してはいけません）
					EntityTransaction tx = em.getTransaction();
					if (tx.isActive()) {
						if (m_log.isDebugEnabled()) {
							StackTraceElement[] eList = Thread.currentThread().getStackTrace();
							StringBuilder trace = new StringBuilder();
							for (StackTraceElement e : eList) {
								if (trace.length() > 0) {
									trace.append("\n");
								}
								trace.append(String.format("%s.%s(%s:%d)", e.getClassName(), e.getMethodName(), e.getFileName(), e.getLineNumber()));
							}
							m_log.debug("closing uncompleted transaction. this transaction will be rollbacked before closing : " + trace);
						}
						tx.rollback();
					}
					
					em.close();
					HinemosSessionContext.instance().setProperty(JpaTransactionManager.EM, null);
					
					// postCloseのみはinnerTransactionとならないため、例外的にcallbackを実行可能とする
					for (JpaTransactionCallback callback : callbacks) {
						if (m_log.isDebugEnabled()) {
							m_log.debug("executing callback postClose : " + callback.getClass().getName());
						}
						try {
							callback.postClose();
						} catch (Throwable t) {
							m_log.warn("callback execution failure : " + callback.getClass().getName() + ", monitor name : " + monitor, t);
							throw new RuntimeException(t.getMessage(), t);
						}
					}
				} finally {
					HinemosSessionContext.instance().setProperty(JpaTransactionManager.EM, null);
				}
			}
			HinemosSessionContext.instance().setProperty(EM, null);
		}
	}

	/**
	 * EntityManager取得
	 */
	public HinemosEntityManager getEntityManager() {
		return em;
	}

	/**
	 * トランザクションが既に開始されているか
	 */
	public boolean isNestedEm() {
		return nestedEm;
	}

	/**
	 * 重複チェック
	 *   重複エラーの場合、EntityExistsException発生
	 * 
	 *  Eclipselink(2.4.1以前)では、persist()時にEntityExistsExceptionが
	 *  発生しないため、本メソッドを使用し重複チェックをする。
	 * 
	 *  Eclipselink(2.4.1以前)では、In-memory上でCascade.removeを行うと、
	 *  DB・キャッシュ間で差異が発生するため、ヒント句を設定している。
	 *
	 * @param clazz 検索対象のEntityクラス
	 * @param primaryKey 検索対象のPrimaryKey
	 * @throws EntityExistsException
	 */
	public <T> void checkEntityExists(Class<T> clazz, Object primaryKey) throws EntityExistsException {
		String strPk = "";
		if (primaryKey instanceof String) {
			strPk = "primaryKey = " + primaryKey;
		} else {
			strPk = primaryKey.toString();
		}
		Object obj = em.find(clazz, primaryKey, JpaPersistenceConfig.JPA_EXISTS_CHECK_HINT_MAP, ObjectPrivilegeMode.NONE);
		if (obj != null) {
			// 重複エラー
			EntityExistsException e = new EntityExistsException(clazz.getSimpleName()
					+ ", " + strPk);
			throw e;
		}
	}

	/**
	 * トランザクションAPIの前処理・後処理に関するcallbackクラス一覧を取得する。<br />
	 * @return callbackクラスのリスト
	 */
	@SuppressWarnings("unchecked")
	public List<JpaTransactionCallback> getCallbacks() {
		Object callbacksProp = em.getProperties().get(CALLBACKS);
		if (callbacksProp != null) {
			return new ArrayList<JpaTransactionCallback>((List<JpaTransactionCallback>)callbacksProp);
		} else {
			return new ArrayList<JpaTransactionCallback>();
		}
	}

	/**
	 * EntityManagerとトランザクション処理のcallbackクラスの関連を消去する。<br/>
	 */
	@SuppressWarnings("unchecked")
	private void clearTransactionCallbacks() {
		Object callbacksProp = em.getProperties().get(CALLBACKS);
		if (callbacksProp != null) {
			List<JpaTransactionCallback> callbacks = (List<JpaTransactionCallback>)callbacksProp;
			for (Iterator<JpaTransactionCallback> iter = callbacks.iterator(); iter.hasNext();) {
				JpaTransactionCallback callback = iter.next();
				if (callback.isTransaction()) {
					iter.remove();
				}
			}
		} else {
			em.setProperty(CALLBACKS, new ArrayList<JpaTransactionCallback>());
		}
	}

	/**
	 * EntityManagerとcallbackクラスの関連を消去する。<br/>
	 */
	private void clearCallbacks() {
		em.setProperty(CALLBACKS, new ArrayList<JpaTransactionCallback>());
	}
	
	/**
	 * 現在のスレッドに割り当てられたトランザクションをcallback処理呼び出し状態としてセットする。<br/>
	 * callback処理内のDBアクセスにてcallback処理が発動しなくなる。<br/>
	 */
	private void setCallbacked() {
		em.setProperty(IS_CALLBACKED, Boolean.TRUE);
	}
	
	/**
	 * 現在のスレッドに割り当てられたトランザクションをcallback処理呼び出し状態から解除する。<br/>
	 */
	private void unsetCallbacked() {
		em.setProperty(IS_CALLBACKED, Boolean.FALSE);
	}
	
	/**
	 * 現在のスレッドに割り当てられたトランザクションがcallback処理中かどうかを返却する。<br/>
	 * @return callback処理中の場合はtrue, それ以外はfalse
	 */
	private boolean isCallbacked() {
		Object isCallbacksProp = em.getProperties().get(IS_CALLBACKED);
		if (isCallbacksProp != null) {
			return (Boolean)isCallbacksProp;
		} else {
			return false;
		}
	}
	
	/**
	 * トランザクションAPIの前処理・後処理に関するcallbackクラスを追加する。<br />
	 * 本メソッドにより追加された順序でcallbackクラスは実行される。<br />
	 * ただし、無限ループを避ける安全性対策として、callbackにて呼び出されたトランザクションに対してcallbackは行われない。<br />
	 * 
	 * @param callback 追加するcallbackクラス
	 */
	public void addCallback(JpaTransactionCallback callback) {
		if (callback == null) {
			m_log.warn("skipped callback addition : null");
			return;
		}
		for (JpaTransactionCallback obj : getCallbacks()) {
			if (callback.equals(obj)) {
				if (m_log.isDebugEnabled()) {
					m_log.debug("skipped callback addition : " + callback.getClass().getName());
				}
				return;
			}
		}
		
		List<JpaTransactionCallback> callbacks = getCallbacks();
		callbacks.add(callback);
		if (m_log.isDebugEnabled()) {
			m_log.debug("adding callback : " + callback.getClass().getName());
		}
		em.setProperty(CALLBACKS, callbacks);
	}

	public int sizeCallback() {
		return getCallbacks().size();
	}

}
