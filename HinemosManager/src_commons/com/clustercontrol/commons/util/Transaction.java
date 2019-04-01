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

/**
 * try-with-resource構文によって、{@link JpaTransactionManager}の利用を簡略化できるようにします。
 * <p>
 * また、{@link JobQueueTx}のように、本クラスの派生クラスを作ってそこへクエリを集約しておくと、
 * ユニットテスト時のモック置換の助けになります。
 * <p>
 * 基本的な使用方法は以下のとおりです。
 * <pre>
 * try (Transaction tx = new Transaction()) {
 * 
 *     // データアクセスを行う
 *     
 *    tx.commit();
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
}
