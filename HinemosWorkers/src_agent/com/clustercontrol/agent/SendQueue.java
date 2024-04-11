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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtOutputBasicInfoRequest;
import org.openapitools.client.model.SendMessageRequest;
import org.openapitools.client.model.SetJobResultRequest;

import com.clustercontrol.agent.sdml.SdmlMessageSendableObject;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.agent.util.AgentRequestId;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.util.HinemosTime;

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

	/** メッセージ通知の同時実行制御用スレッドプール */
	private static ThreadPoolExecutor m_internalMessageSendExecutor;

	/** メッセージ通知の同時実行制御用依頼キュー */
	private static LinkedBlockingQueue<Runnable> m_internalMessageSendQueue;

	/** メッセージ通知用スレッドプールのスレッド数向けプロパティ名 */
	private static final String MAX_THREADS = "common.message.send.threads";

	/** メッセージ通知用スレッドプールの依頼受付キュー最大数向けプロパティ名 */
	private static final String MAX_QUEUE = "common.message.send.queue.maxsize";

	/** メッセージ通知用スレッドプールのスレッド数*/
	private static int m_internalMessageSendThreadMax  = 10 ;

	/** メッセージ通知用スレッドプールのキュー最大数*/
	private static int m_internalMessageSendQueueMax  = Integer.MAX_VALUE ;

	static {
		//内部メッセージ送信用Executerの初期化
		initMessageSendExecuter();
	}

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
	 * マネージャへ各種メッセージを送信します。<BR>
	 * 処理失敗は再試行します。
	 * @param msg
	 */
	public boolean put(SendableObject info) {
		if (info instanceof JobResultSendableObject) {
			return put((JobResultSendableObject)info); 
		}else{
			return putForMessage(info); 
		}
	}
	
	/**
	 * メッセージを送信します。<BR>
	 * 
	 * マネージャからの実行に対する応答メッセージを送信します。<BR>
	 * 処理失敗は再試行します。
	 * @param msg
	 */
	public boolean put(JobResultSendableObject info) {
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
	 * マネージャへエージェント内部メッセージを送信します。<BR>
	 * 
	 * 処理失敗時は再試行し、別途のチェック処理にてマネージャとの接続が消失と判定されるまで再試行を継続します。<BR>
	 * 
	 * 送信API呼び出しのタイムアウト時の打ち切り制御と利用リソース制限のために、
	 * スレッドプールを用いて送信用API呼出部の同時実行数制限とスレッドの再利用を行っています。<BR>
	 * 
	 * 送信は依頼順にて行われますが、タイムアウトもしくは送信エラーとなった場合
	 * 待ち順は並び直しになります。
	 * 
	 * @param info
	 */
	public boolean putForMessage(SendableObject info) {
		if( m_log.isDebugEnabled() ){
			m_log.debug("putForInternal() : start " + info.getClass().getCanonicalName() + ",hashCode=" + Integer.toHexString(info.hashCode()) );
		}
		AgentRequestId agentRequestId = new AgentRequestId();
		
		// 同時実行の制御用Executerは メッセージ送信用のプールを利用する。
		ThreadPoolExecutor targetExecuter = m_internalMessageSendExecutor;

		while (!ReceiveTopic.isHistoryClear()) { // マネージャ接続中チェック
			m_log.debug("putForInternal() : while (!ReceiveTopic.isHistoryClear()) is true");
			boolean sendQueueStatus = false;
			
			// Queue送信タスクをExecuterに依頼する。
			//  利用リソース制限のために同時実行数の制限とスレッドの使いまわし（ApiClient再利用向け）を
			//  スレッドプールを用いて実装する。
			Sender sender = new Sender(info, agentRequestId);
			Future<Boolean> task = null;
			try{
				task = targetExecuter.submit(sender);
				if( m_log.isDebugEnabled() ){
					m_log.debug("putForInternal() : submit,hashCode=" + Integer.toHexString(info.hashCode()) );
				}
			} catch (RejectedExecutionException e) {
				// キュー満杯等にて 依頼が拒否された場合は送信失敗とする。
				m_log.warn("putForInternal() : submit to executer is failed . : "+ e.getMessage());
				return false;
			}

			// Queue送信タスクが成否に関わらず終了するか、マネージャと不通と判断されるまでは待機。
			//  送信開始後、タイムアウト時間を超過した場合はタスクに対してキャンセルを依頼（スレッドがソケットでI/O待ちの場合、そちらが優先される）
			//  単純なタスク完了待ちタイムアウトでは、送信開始と経過時間が不明なのでsender.getCallStartTime()を参照して送信開始後、タイムアウト時間超過を判断する。
			while (!ReceiveTopic.isHistoryClear()) { // マネージャ接続中チェック
				boolean isTimeout = false;
				try {
					// 別スレッドでQueue送信処理を実行することで、送信処理に無限の時間がかかっても、
					// Future.get()のタイムアウトにより、本スレッドに制御が戻るようにする。
					sendQueueStatus = task.get(AgentRestConnectManager.getHttpRequestTimeout(), TimeUnit.MILLISECONDS);
					break;
				} catch (Exception e) {
					if( e instanceof TimeoutException ){
						//Future.get()でタイムアウトが発生時
						m_log.warn("putForInternal() : Timeout to connect to MGR. ,hashCode=" + Integer.toHexString(info.hashCode()) );
						// 送信タスク完了待ち タイムアウト時、
						// 送信タスクが開始済み かつ 開始からタイムアウト期間が経過ならスレッドにキャンセル依頼して待機継続（ただし ソケットi/o待ちならキャンセルより優先される）
						if( !(isTimeout) ){
							if (sender.getCallStartTime() != null
									&& (HinemosTime.currentTimeMillis() - sender.getCallStartTime()) > AgentRestConnectManager.getHttpRequestTimeout()) {
								if( m_log.isDebugEnabled() ){
									m_log.debug("putForInternal() : The timeout period has passed since the sending task started. hashCode=" + Integer.toHexString(info.hashCode()) );
								}
								if (task != null) {
									task.cancel(true);
								}
								isTimeout = true;
							}
						}
					}else{
						// タイムアウト以外のエラーなら待機終了(リトライ制御へ)
						m_log.warn("putForInternal() : Failed to connect to MGR. Exception=" + e.getClass().getSimpleName()
								+ ",message=" + e.getMessage() + ",hashCode=" + Integer.toHexString(info.hashCode()));
						break;
					}
				}
			}
			if (task != null) {
				//待機終了時は無条件にタスクをキャンセルしておく(重複でキャンセルされても問題はない)
				task.cancel(true);
			}
			// Queue送信に成功した場合はループを抜ける
			// 送信が成功していない場合はsleep後に再送を依頼して待機
			if(sendQueueStatus){
				if( m_log.isDebugEnabled() ){
					m_log.debug("putForInternal() : return true : " + info.getClass().getCanonicalName() + ",hashCode=" + Integer.toHexString(info.hashCode()));
				}
				return true;
			} else {
				try {
					if( m_log.isDebugEnabled() ){
						m_log.debug("putForInternal() : reput interval sleep: " + m_sendQueueReconnectionInterval + " sec,hashCode=" + Integer.toHexString(info.hashCode()));
					}
					Thread.sleep(m_sendQueueReconnectionInterval);
				} catch (InterruptedException e1) {
					m_log.error("putForInternal() : reput interval sleep: ", e1);
				}
			}
		}
		return false;
	}
	
	
	/**
	 * Queueメッセージ送信処理を実行するタスク用クラス
	 */
	private static class Sender implements Callable<Boolean> {
		private SendableObject m_info;
		private AgentRequestId m_agentRequestId;
		private volatile Long m_callStartTime = null ;

		public Sender(SendableObject info, AgentRequestId agentRequestId) {
			m_info = info;
			m_agentRequestId = agentRequestId;
		}

		@Override
		public Boolean call() throws Exception {
			m_callStartTime = HinemosTime.currentTimeMillis();
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
				m_log.info("Sender Send Message : message =" + o.body.getMessage() + ",hashCode=" + Integer.toHexString(m_info.hashCode()) );

				SendMessageRequest req = new SendMessageRequest();
				req.setAgentInfo(Agent.getAgentInfoRequest());
				req.setOutputBasicInfo(o.body);
				try {
					AgentRestClientWrapper.sendMessageToInternalEvent(req, m_agentRequestId.toRequestHeaderValue());
				} catch (Exception e) {
					m_log.warn("call() : sendMessageToInternalEvent ocuur Exception  Exception=" + e.getClass().getSimpleName()
								+ ",message=" + e.getMessage() + ",hashCode=" + Integer.toHexString(m_info.hashCode()));
					throw e;
				}
				if(m_log.isDebugEnabled()){
					m_log.debug("Sender Send Message complete,hashCode=" + Integer.toHexString(m_info.hashCode()));
				}
				return true;

			} else if (m_info instanceof SdmlMessageSendableObject){
				if(m_log.isDebugEnabled()){
					m_log.debug("Sender Send SdmlMessage start,hashCode=" + Integer.toHexString(m_info.hashCode()));
				}
				try {
					((SdmlMessageSendableObject)m_info).sendMessage(m_agentRequestId.toRequestHeaderValue());
				} catch (Exception e) {
					m_log.warn("call() : SdmlMessageSendableObject#sendMessage ocuur Exception  Exception=" + e.getClass().getSimpleName()
								+ ",message=" + e.getMessage() + ",hashCode=" + Integer.toHexString(m_info.hashCode()));
					throw e;
				}
				
				return true;
			} else {
				m_log.error("Unknown sendable object = " + m_info.getClass().getName());
				return false;
			}
		}
		public Long getCallStartTime() {
			return m_callStartTime;
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

	/**
	 * 内部メッセージ送信用Executerの初期化用メソッド
	 */
	private static void initMessageSendExecuter( )  {
		// メッセージ通知用スレッドプール スレッド数
		String maxThreadsStr = AgentProperties.getProperty(MAX_THREADS, Integer.toString(m_internalMessageSendThreadMax));
		m_log.info("initMessageSendExecuter(): "+ MAX_THREADS + " = " + maxThreadsStr );
		try {
			int value = Integer.parseInt(maxThreadsStr);
			if (value < 1) {
				throw new NumberFormatException("invalid value: " + maxThreadsStr);
			}
			m_internalMessageSendThreadMax = value;
		} catch (NumberFormatException e) {
			m_log.warn("initMessageSendExecuter(): " + MAX_THREADS + ", " + e.getMessage());
		} catch (Exception e) {
			m_log.warn("initMessageSendExecuter(): " + MAX_THREADS + ", " + e.getMessage(), e);
		}
		m_log.debug("initMessageSendExecuter(): m_internalMessageSendThreadMax =" + m_internalMessageSendThreadMax);

		// メッセージ通知用スレッドプール キュー最大数
		String maxQuesuStr = AgentProperties.getProperty(MAX_QUEUE, Integer.toString(m_internalMessageSendQueueMax));
		m_log.info("initMessageSendExecuter(): "+ MAX_QUEUE + " = " + maxQuesuStr );
		try {
			int value = Integer.parseInt(maxQuesuStr);
			if (value < 1) {
				throw new NumberFormatException("invalid value: " + maxQuesuStr);
			}
			m_internalMessageSendQueueMax = value;
		} catch (NumberFormatException e) {
			m_log.warn("initMessageSendExecuter(): " + MAX_QUEUE + ", " + e.getMessage());
		} catch (Exception e) {
			m_log.warn("initMessageSendExecuter(): " + MAX_QUEUE + ", " + e.getMessage(), e);
		}
		m_log.debug("initMessageSendExecuter():  m_internalMessageSendQueueMax = " + m_internalMessageSendQueueMax);

		m_internalMessageSendQueue = new LinkedBlockingQueue<Runnable>(m_internalMessageSendQueueMax);

		// プロパティの値を元に、メッセージ通知用スレッドプール 初期設定
		m_internalMessageSendExecutor = new ThreadPoolExecutor(m_internalMessageSendThreadMax, m_internalMessageSendThreadMax, 0L, TimeUnit.MILLISECONDS,
				m_internalMessageSendQueue, new ThreadFactory() {
				//Internalメッセージ送信処理を実行するタスク用のThreadFactory
				private volatile int _count = 0;
				@Override
				public Thread newThread(Runnable r) {
					return new Thread(r, "InternalMessageSendExecutor-" + _count++);
				}
			}
		);

	}
	/**
	 * 内部メッセージ送信用Executerの終了用メソッド
	 */
	public static void termMessageSendExecuter( long timeoutSec )  {

		// 同時実行の制御用スレッドプールをセット
		ThreadPoolExecutor targetExecuter = m_internalMessageSendExecutor;

		m_log.trace("termMessageSendExecuter(): call executor.shutdown(), executor=" + m_internalMessageSendExecutor);
		targetExecuter.shutdown();
		// すべてのタスクが実行を完了するか、または終了タイムアウトに達するまで待つ。 
		// タイムアウトした場合は実行中および実行待ちのタスクを破棄して終了する。
		m_log.debug("termMessageSendExecuter(): end. executor=" + targetExecuter);
		try {
			if (!targetExecuter.awaitTermination(timeoutSec,TimeUnit.SECONDS)) {
				m_log.warn("termMessageSendExecuter(): shutdownNow was executed by timeout. some messages were discarded ");
				targetExecuter.shutdownNow();
			}
		} catch (InterruptedException ex) {
			targetExecuter.shutdownNow();
			m_log.warn("termMessageSendExecuter(): shutdownNow was executed by InterruptedException. some messages were discarded");
			Thread.currentThread().interrupt();
		}		
		
	}
	
}
