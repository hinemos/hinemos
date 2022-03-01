/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.jobmanagement.queue.internal.JobQueueTx;

import jakarta.persistence.EntityExistsException;

/**
 * try-with-resource構文によって、{@link JpaTransactionManager}の利用を簡略化できるようにします。
 * <p>
 * また、{@link JobQueueTx}のように、本クラスの派生クラスを作ってそこへクエリを集約しておくと、
 * ユニットテスト時のモック置換の助けになります。
 * <p>
 * 基本的な使用方法は以下のとおりです。
 *
 * <pre>
 * try (Transaction tx = new Transaction()) {
 *     // データアクセスを行う
 *     tx.commit();
 * }
 * </pre>
 * <p>
 * 例外などによって、{@link #commit()}を呼ぶ前にtryブロックを抜けた場合は、
 * 自動的に{@link JpaTransactionManager#rollback()}及び{@link JpaTransactionManager#close()}を呼び出します。
 * <p>
 * 「本クラスによるトランザクションが最も外側にある(トランザクションが開始済みでない)」こと、
 * あるいは「tryブロック内部でデータ更新が行われていない」ことが確実である場合を除き、
 * 本クラスを利用したtry-with-resouce構文において例外をcatchするときは、
 * 例外を外側のトランザクションへ伝搬させる必要があることに注意してください。
 * ネストされたトランザクションにおいて、{@link JpaTransactionManager#rollback()}は、
 * 何も行いません。
 *
 * @since 6.2.0
 */
public class Transaction implements AutoCloseable {
	private static final Log log = LogFactory.getLog(Transaction.class);

	private JpaTransactionManager jtm;
	private boolean committed;

	public Transaction() {
		jtm = new JpaTransactionManager();
		committed = false;

		jtm.begin();
	}

	public void flush() {
		jtm.flush();
	}

	public void clear() {
		jtm.getEntityManager().clear();
	}

	public void commit() {
		jtm.commit();
		committed = true;
	}

	@Override
	public void close() {
		try {
			if (jtm != null) {
				try {
					if (!committed) {
						jtm.rollback();
					}
				} finally {
					jtm.close();
				}
			}
		} catch (Throwable t) {
			log.warn("close: Error.", t);
		}
	}

	public void persist(Object entity) {
		jtm.getEntityManager().persist(entity);
	}

	public <T> T merge(T entity) {
		return jtm.getEntityManager().merge(entity);
	}

	public <T> T mergeIfDetached(T entity) {
		if (jtm.getEntityManager().contains(entity)) {
			return entity;
		} else {
			return jtm.getEntityManager().merge(entity);
		}
	}

	public void remove(Object entity) {
		jtm.getEntityManager().remove(entity);
	}

	protected JpaTransactionManager getJtm() {
		return jtm;
	}

	protected HinemosEntityManager getEm() {
		return jtm.getEntityManager();
	}

	public void addCallback(JpaTransactionCallback callback) {
		jtm.addCallback(callback);
	}

	public Object getScopedValue(String key) {
		return jtm.getScopedValue(key);
	}

	public Object getScopedValue(String key, Object defaultValue) {
		return jtm.getScopedValue(key, defaultValue);
	}

	public void setScopedValue(String key, Object value) {
		jtm.setScopedValue(key, value);
	}

	public <T> void checkEntityExists(Class<T> clazz, Object primaryKey) throws EntityExistsException {
		jtm.checkEntityExists(clazz, primaryKey);
	}

	/**
	 * 同一トランザクションで複数回呼び出された場合にデッドロックなどの危険性があるメソッドに関して、警告をログ出力する。
	 * 原則として、この警告がログに出た場合は、ロジックを修正しなければならない。
	 */
	public void alertMultipleCall(Class<?> clazz, String methodName) {
		String classMethod = clazz.getName() + "#" + methodName;
		String key = "alertMultipleCall:" + classMethod;
		int count = (Integer) getScopedValue(key, 0);
		if (++count > 1) {
			log.warn("UNEXPECTED MULTIPLE CALLS IN THE SAME TRANSACTION: " + classMethod + " x" + count);
		}
		setScopedValue(key, count);
	}

}
