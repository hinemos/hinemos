/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.util.Calendar;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.persistence.sessions.SessionEvent;
import org.eclipse.persistence.sessions.SessionEventAdapter;
import org.eclipse.persistence.sessions.server.ConnectionPool;
import org.eclipse.persistence.sessions.server.ServerSession;

import com.clustercontrol.commons.util.DBConnectionPoolStats;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.util.HinemosTime;

/**
 * eclipselinkのSessionEventに紐づくcallback APIを実装するクラス
 * SessionEventAdapterはSessionEventListenerインタフェースを空実装した抽象クラス
 *
 * @see org.eclipse.persistence.sessions.SessionEventListener
 * @see org.eclipse.persistence.sessions.SessionEventAdapter
 */
public class JpaSessionEventListener extends SessionEventAdapter {

	private static final Log m_log = LogFactory.getLog( JpaSessionEventListener.class );
	
	private static final LinkedList<DBConnectionPoolStats> queue = new LinkedList<DBConnectionPoolStats>();
	private static final Object queueUpdateLock = new Object();
	private static int maxQueueSize = 12;
	private static long lastAddTime  = 0;
	
	
	public JpaSessionEventListener() {
		if(queue.peekLast() == null){
			setLastAddTime(HinemosTime.currentTimeMillis());
			queue.addLast(new DBConnectionPoolStats(lastAddTime, 0));
			m_log.debug("initialize queue.");
		}
	}
	
	private static void setLastAddTime(long time) {
		lastAddTime = time;
	}
	
	public LinkedList<DBConnectionPoolStats> getPoolStats() {
		LinkedList<DBConnectionPoolStats> copiedQueue = null;
		synchronized(queueUpdateLock) {
			copiedQueue = new LinkedList<DBConnectionPoolStats>(queue);
		}
		return copiedQueue;
	}
	
	/**
	 * PUBLIC:
	 * This event is raised on when using the server/client sessions.
	 * This event is raised after a connection is acquired from a connection pool.
	 */
	public void postAcquireConnection(SessionEvent event) {
		m_log.debug("Connection acquired from connection pool");
		
		if(m_log.isTraceEnabled()){
			StackTraceElement[] elements = Thread.currentThread().getStackTrace();
			for(StackTraceElement element : elements){
				m_log.trace("postAcquireConnection():" + element);
			}
		}
	}
	
	private static void setMaxQueueSize(int size) {
		maxQueueSize = size;
	}
	
	/**
	 * PUBLIC:
	 * This event is raised on when using the server/client sessions.
	 * This event is raised before a connection is released into a connection pool.
	 */
	public void preReleaseConnection(SessionEvent event) {
		m_log.debug("Connection release to connection pool");
		
		HinemosEntityManager em = null;
		em = (HinemosEntityManager)HinemosSessionContext.instance().getProperty(JpaTransactionManager.EM);
		
		if (em != null) {
			setMaxQueueSize(HinemosPropertyCommon.common_db_connectionpool_stats_threshold.getIntegerValue());
			
			ServerSession ss = em.unwrap(ServerSession.class);
			// Hinemos 6.0時点ではデフォルトのコネクションプールしか使用しないが、読込専用プール等、
			// 別のプールが追加された場合は、プール毎にデータ保持するように、別のキューを用意すべき。
			for (ConnectionPool pool : ss.getConnectionPools().values()) {
				int used = (pool.getTotalNumberOfConnections() - pool.getConnectionsAvailable().size());
				if (m_log.isDebugEnabled()) {
					m_log.debug("Pool name=" + pool.getName() + ", Used size=" + used + ", Free size=" + pool.getConnectionsAvailable().size());
					m_log.debug("Initial=" + pool.getInitialNumberOfConnections() + ":Min=" + pool.getMinNumberOfConnections() + ":Max=" + pool.getMaxNumberOfConnections());
				}
				
				// Minを現在使用値へ増加させる更新
				if(used > pool.getMinNumberOfConnections()){
					if (m_log.isDebugEnabled())
						m_log.info("update min setting:" + pool.getMinNumberOfConnections() + "->" + used + "/max(" + pool.getMaxNumberOfConnections() + ")");
					pool.setMinNumberOfConnections(used);
				}
				
				// 1時間毎のデータを保持するため、"X時0分"以降で初めてプールの増減があった際にキューへ新規追加する
				// 併せて、保持データにおける最大使用数にMinを更新する(使用数が大きく減った場合の対応)
				Calendar now = HinemosTime.getCalendarInstance();
				Calendar comparetime = (Calendar)now.clone();
				// "分"以降の値を切捨て
				comparetime.clear();
				comparetime.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DATE), now.get(Calendar.HOUR_OF_DAY), 0, 0);
				// 動作確認用に、1分間毎にデータ保持させる場合↑から↓へ変更のこと(秒以降切捨て)
//				comparetime.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DATE), now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), 0);
				
				synchronized(queueUpdateLock) {
					// 直近のキュー追加時間が"X時0分"の時間以降ではない("X時0分"以降キュー追加してない)場合にキュー新規追加
					if(lastAddTime < comparetime.getTimeInMillis()){
						if (m_log.isDebugEnabled())
							m_log.debug("add queue:used=" + used);
						queue.addLast(new DBConnectionPoolStats(now.getTimeInMillis(), used));
						lastAddTime = now.getTimeInMillis();
						
						// 古いものから削除
						while (queue.size() > maxQueueSize) {
							if (m_log.isDebugEnabled())
								m_log.debug("remove queue: currentSize=" + queue.size() + ", maxSize=" + maxQueueSize);
							queue.removeFirst();
						}
						
						// Minを(現在値含む)過去データにおける最大使用数へ更新
						int max = used;
						for (DBConnectionPoolStats stats: queue) {
							if(stats.getMaxUseCount() > max) max = stats.getMaxUseCount();
						}
						if (m_log.isDebugEnabled()) m_log.debug("max use:" + max);
						if(pool.getMinNumberOfConnections() > max){
							if (m_log.isDebugEnabled())
								m_log.info("update min setting(from stats):" + pool.getMinNumberOfConnections() + "->" + used + "/max(" + pool.getMaxNumberOfConnections() + ")");
							pool.setMinNumberOfConnections(max);
						}
					} // 通常時は使用数が増えた場合に直近の保持データを更新
					else if(used > queue.peekLast().getMaxUseCount()){
						if (m_log.isDebugEnabled()) m_log.debug("update queue:used=" + used);
						queue.peekLast().setMaxUseInfo(now.getTimeInMillis(), used);
					}
				}
			}
		}
		
		if(m_log.isTraceEnabled()){
			StackTraceElement[] elements = Thread.currentThread().getStackTrace();
			for(StackTraceElement element : elements){
				m_log.trace("preReleaseConnection():" + element);
			}
		}
	}

}
