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
import org.openapitools.client.model.AgtOutputBasicInfoRequest;
import org.openapitools.client.model.SendMessageRequest;
import org.openapitools.client.model.SetJobResultRequest;

import com.clustercontrol.agent.sdml.SdmlMessageSendableObject;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.agent.util.AgentRequestId;
import com.clustercontrol.fault.JobInfoNotFound;

/**
 * ジョブ実行結果（チェック、開始含む）をQueue送信するクラス<BR>
 *
 * エージェントからの戻りメッセージはこのメソッドを用いて
 * マネージャに返送されます。
 *
 */
public class SendQueue {

	public interface SendableObject {
		// empty
	}

	public static class JobResultSendableObject implements SendableObject {
		public String sessionId;
		public String jobunitId;
		public String jobId;
		public String facilityId;
		public SetJobResultRequest body;
	}

	public static class MessageSendableObject implements SendableObject {
		public AgtOutputBasicInfoRequest body;
	}

	//ロガー
	static private Log m_log = LogFactory.getLog(SendQueue.class);

	private static final long SEND_TIMEOUT = 60 * 1000l;

	// リトライ間隔
	private long m_sendQueueReconnectionInterval = 30000;

	/**
	 * コンストラクタ
	 * @param props プロパティファイル情報
	 */
	public SendQueue() {
		super();
		// リトライ間隔を取得
		String interval = AgentProperties.getProperty("job.reconnection.interval");
		if (interval != null) {
			try {
				// プロパティファイルにはmsecで記述
				m_sendQueueReconnectionInterval = Long.parseLong(interval);
				m_log.info("job.reconnection.interval = " + m_sendQueueReconnectionInterval + " msec");
			} catch (NumberFormatException e) {
				m_log.error("job.reconnection.interval",e);
			}
		}
	}

	/**
	 * メッセージを送信します。<BR>
	 * 
	 * マネージャからの実行に対する応答メッセージを送信します。<BR>
	 * 処理失敗は再試行します。
	 * @param msg
	 */
	public boolean put(SendableObject info) {
		m_log.debug("put() start : " + info.getClass().getCanonicalName());
		AgentRequestId agentRequestId = new AgentRequestId();

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
				task = es.submit(new Sender(info, agentRequestId));
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
				m_log.debug("put() end	  : " + info.getClass().getCanonicalName());
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
		private SendableObject m_info;
		private AgentRequestId m_agentRequestId;

		public Sender(SendableObject info, AgentRequestId agentRequestId) {
			m_info = info;
			m_agentRequestId = agentRequestId;
		}

		@Override
		public Boolean call() throws Exception {
			if (m_info instanceof JobResultSendableObject) {
				JobResultSendableObject o = (JobResultSendableObject) m_info;
				m_log.info("Sender Send RunResultInfo : " +
						"SessionID=" + o.sessionId +
						", JobID=" + o.jobId +
						", CommandType=" + o.body.getCommandType() +
						", Status=" + o.body.getStatus());
				m_log.debug("Sender Send agentRequestId : " + m_agentRequestId);

				try {
					AgentRestClientWrapper.setJobResult(o.sessionId, o.jobunitId, o.jobId, o.facilityId, o.body, m_agentRequestId.toRequestHeaderValue());
				} catch (JobInfoNotFound ignored) {
					m_log.info("call() : JobInfo has been already deleted");
				}
				return true;

			} else if (m_info instanceof MessageSendableObject) {
				MessageSendableObject o = (MessageSendableObject) m_info;
				m_log.info("Sender Send Message : message =" + o.body.getMessage());

				SendMessageRequest req = new SendMessageRequest();
				req.setAgentInfo(Agent.getAgentInfoRequest());
				req.setOutputBasicInfo(o.body);
				AgentRestClientWrapper.sendMessageToInternalEvent(req, m_agentRequestId.toRequestHeaderValue());

				return true;

			} else if (m_info instanceof SdmlMessageSendableObject){
				((SdmlMessageSendableObject)m_info).sendMessage(m_agentRequestId.toRequestHeaderValue());
				
				return true;
			} else {
				m_log.error("Unknown sendable object = " + m_info.getClass().getName());
				return false;
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
