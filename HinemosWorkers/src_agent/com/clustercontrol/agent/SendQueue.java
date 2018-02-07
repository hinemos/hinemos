/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.ws.agent.AgentOutputBasicInfo;
import com.clustercontrol.ws.agent.JobInfoNotFound_Exception;
import com.clustercontrol.ws.agent.OutputBasicInfo;
import com.clustercontrol.ws.jobmanagement.RunResultInfo;


/**
 * ジョブ実行結果（チェック、開始含む）をQueue送信するクラス<BR>
 *
 * エージェントからの戻りメッセージはこのメソッドを用いて
 * マネージャに返送されます。
 *
 */
public class SendQueue {

	//ロガー
	static private Log m_log = LogFactory.getLog(SendQueue.class);

	private static final long SEND_TIMEOUT = 60 * 1000l;

	private long m_sendQueueReconnectionInterval = 10 * 1000l;

	/**
	 * コンストラクタ
	 * @param props プロパティファイル情報
	 */
	public SendQueue() {
		super();
	}

	/**
	 * メッセージを送信します。<BR>
	 * 
	 * マネージャからの実行に対する応答メッセージを送信します。<BR>
	 * 処理失敗は再試行します。
	 * @param msg
	 */
	public boolean put(Object info) {
		m_log.debug("put() start : " + info.getClass().getCanonicalName());

		while (!ReceiveTopic.isHistoryClear()) {
			m_log.debug("put() while (!ReceiveTopic.isHistoryClear()) is true");

			boolean sendQueueStatus = false;
			ExecutorService es = null;
			Future<Boolean> task = null;
			try {
				String id = "";
				// Executorオブジェクトの生成
				SenderThreadFactory threadFactory = new SenderThreadFactory(id);
				es = Executors.newSingleThreadExecutor(threadFactory);

				// Queue送信タスクを実行する。
				// 別スレッドでQueue送信処理を実行することで、送信処理に無限の時間がかかっても、
				// Future.get()のタイムアウトにより、本スレッドに制御が戻るようにする。
				m_log.debug("put() submit");
				task = es.submit(new Sender(info));
				sendQueueStatus = task.get(SEND_TIMEOUT, TimeUnit.MILLISECONDS);

			} catch (Exception e) {
				// Queue送信処理で例外が発生した場合、もしくは、Future.get()でタイムアウトが発生した場合に、
				// ここに制御が移る

				// ログファイルにログ出力
				m_log.warn("put() : Failed to connect to MGR " + e.getMessage(), e);

			} finally {
				// タスクをキャンセル
				if (task != null) {
					task.cancel(true);
				}

				if (es != null) {
					es.shutdown();
				}
				m_log.debug("put() end    : " + info.getClass().getCanonicalName());
			}

			// 送信が成功していない場合はsleep後に再送を試みる
			// Queue送信に成功した場合はループを抜ける
			if(sendQueueStatus){
				m_log.debug("put() return true : " + info.getClass().getCanonicalName());
				return true;
			} else {
				// 一定時間sleepした後、QueueConnection、QueueSession を再接続する。
				try {
					m_log.debug("put() reput interval sleep: " + m_sendQueueReconnectionInterval + " sec");
					Thread.sleep(m_sendQueueReconnectionInterval);
				} catch (InterruptedException e1) {
					m_log.error("put() reput interval sleep: ", e1);
				}
			}
		} // End While Loop
		return false;
	}

	/**
	 * Queueメッセージ送信処理を実行するタスク用クラス
	 */
	private static class Sender implements Callable<Boolean> {
		//private RunResultInfo m_info;
		private Object m_info;

		public Sender(Object info){
			m_info = info;
		}

		@Override
		public Boolean call() throws Exception {
			try {
				if(m_info instanceof RunResultInfo) {
					RunResultInfo resultInfo = (RunResultInfo)m_info;
					m_log.info("Sender Send RunResultInfo : SessionID=" + resultInfo.getSessionId() +
							", JobID=" + resultInfo.getJobId() +
							", CommandType=" + resultInfo.getCommandType() +
							", Status=" + resultInfo.getStatus());
					AgentEndPointWrapper.jobResult(resultInfo);
				} else if (m_info instanceof OutputBasicInfo) {
					OutputBasicInfo message = (OutputBasicInfo)m_info;
					m_log.info("Sender Send Message : message =" + message);
					AgentOutputBasicInfo info = new AgentOutputBasicInfo();
					info.setOutputBasicInfo(message);
					AgentEndPointWrapper.sendMessage(info);
				} else {
					m_log.error("Sender Send Object is not unknown = " + m_info.getClass().getName());
					return false;
				}
				return true;
			} catch (JobInfoNotFound_Exception e) {
				m_log.info("call() : JobInfo has been already deleted");
				return true;
			} catch (Exception e) {
				throw e;
			}
		}
	}

	/**
	 * Queueメッセージ送信処理を実行するタスク用のThreadFactory
	 */
	private static class SenderThreadFactory implements ThreadFactory {
		private final String m_threadName;

		public SenderThreadFactory(String threadName){
			m_threadName = threadName;
		}

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "Sender-" + m_threadName);
		}
	}
}
