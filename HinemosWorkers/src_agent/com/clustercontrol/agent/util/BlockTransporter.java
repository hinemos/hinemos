/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 特定オブジェクトのキューへの蓄積と、蓄積したオブジェクトのブロック単位での転送処理を行います。<br/>
 * ただし、本クラスは枠組みのみ提供します。
 * 転送処理の本体は、本クラスを利用する側で {@link TransportProcessor} を implements して定義してください。
 * 
 * @param <T> 蓄積するオブジェクトの型。
 */
public class BlockTransporter<T> {
	private static Log log = LogFactory.getLog(BlockTransporter.class);

	private final String name;
	private final int _queueSize;
	private final int blockSize;
	private final int maxTries;
	private final int sizeThreshold;
	private final long timeThreshold;
	private final long maxTimeThreshold;
	private final long addQueueWaitIntervalMSec;
	private final long addQueueWaitWarnThresholdMSec;
	private final AtomicInteger retryCount;
	private final TransportProcessor<T> processor;

	private final Lock addQueuelock = new ReentrantLock(true);

	private long timeThresholdVariable;
	
	/** 送信リスト（内容データは送信バッファに一旦移動してから送信される）*/
	private final Queue<T> queue;

	/** 送信バッファ（内容データは送信正常完了時に全削除。空の時のみ送信リストからの移動が可能） */
	private final List<T> buffer;

	private final ScheduledThreadPoolExecutor executor;
	private final AtomicInteger activeTaskCount = new AtomicInteger(0);

	/** 送信リクエストID（重複送信防止用。送信バッファにデータを移動するたびに更新 同じバッファの送信ならリトライしても同一） */
	private AgentRequestId requestId;

	/**
	 * 転送処理を定義するためのインターフェイスです。
	 */
	public interface TransportProcessor<T> {

		/**
		 * 転送処理を行います。
		 * 
		 * @param results 転送対象オブジェクトのリスト。
		 * @param requestId 採番したリクエストID。
		 */
		public void accept(List<T> results, AgentRequestId requestId) throws Exception;
	}

	public BlockTransporter(String name, int queueSize, int blockSize, int sizeThreshold, long timeThreshold,
			int maxTries, TransportProcessor<T> resultsProcessor) {
		this.name = name;
		this._queueSize = queueSize;
		this.blockSize = blockSize;
		this.maxTries = maxTries;
		this.sizeThreshold = sizeThreshold;
		this.timeThreshold = timeThreshold;
		this.timeThresholdVariable = timeThreshold;
		this.maxTimeThreshold = -1;
		this.processor = resultsProcessor;
		this.addQueueWaitIntervalMSec = getAddQueueWaitIntervalMSec();
		this.addQueueWaitWarnThresholdMSec = getAddQueueWaitWarnThresholdMSec();

		retryCount = new AtomicInteger(0);
		queue = new LinkedBlockingQueue<>(queueSize);
		buffer = new ArrayList<>();

		executor = new ScheduledThreadPoolExecutor(
				1, // single-thread
				new ThreadFactory() {
					@Override
					public Thread newThread(Runnable r) {
						Thread t = new Thread(r, BlockTransporter.this.name);
						t.setDaemon(true);
						return t;
					}
				});

		if (this.timeThreshold != -1) {
			executor.scheduleWithFixedDelay(new ExecuteTransport(), 0, this.timeThreshold, TimeUnit.MILLISECONDS);
		}

		requestId = null;
	}

	public BlockTransporter(String name, int queueSize, int blockSize, int sizeThreshold, long timeThreshold,
			long maxTimeThreshold, TransportProcessor<T> resultsProcessor) {
		this.name = name;
		this._queueSize = queueSize;
		this.blockSize = blockSize;
		this.maxTries = -1;
		this.sizeThreshold = sizeThreshold;
		this.timeThreshold = timeThreshold;
		this.timeThresholdVariable = timeThreshold;
		this.maxTimeThreshold = maxTimeThreshold;
		this.processor = resultsProcessor;
		this.addQueueWaitIntervalMSec = getAddQueueWaitIntervalMSec();
		this.addQueueWaitWarnThresholdMSec = getAddQueueWaitWarnThresholdMSec();

		retryCount = new AtomicInteger(0);
		queue = new LinkedBlockingQueue<>(queueSize);
		buffer = new ArrayList<>();

		executor = new ScheduledThreadPoolExecutor(
				1, // single-thread
				new ThreadFactory() {
					@Override
					public Thread newThread(Runnable r) {
						Thread t = new Thread(r, BlockTransporter.this.name);
						t.setDaemon(true);
						return t;
					}
				});

		if (this.timeThresholdVariable != -1) {
			scheduleOnce();
		}

		requestId = null;
	}

	private long getAddQueueWaitIntervalMSec() {
		return AgentProperties.getPropertyLongValue("forwarding.queue.wait.interval.msec", 100,
				new AgentProperties.Validator<Long>() {
					@Override
					public boolean validate(Long value) {
						return value >= 0;
					}
				});
	}

	private long getAddQueueWaitWarnThresholdMSec() {
		return AgentProperties.getPropertyLongValue("forwarding.queue.wait.warn.threshold.msec", 10000,
				new AgentProperties.Validator<Long>() {
					@Override
					public boolean validate(Long value) {
						return value >= 0;
					}
				});
	}
	
	public void shutdown(long timeoutMills) {
		executor.shutdown();
		try {
			executor.awaitTermination(timeoutMills, TimeUnit.MICROSECONDS);
		} catch (InterruptedException e) {
			log.warn("shutdown: awaitTermination was interrupted.");
		}
	}

	public boolean waitNoActiveTasks(long timeoutInMillis) {
		long start = System.currentTimeMillis();
		while (activeTaskCount.get() > 0) {
			if (System.currentTimeMillis() - start > timeoutInMillis) return false;
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				log.warn("waitNoActiveTasks: Sleep interrupted.");
			}
		}
		return true;
	}

	public int getSize() {
		return queue.size();
	}

	/**
	 * マネージャへ送信する情報を格納するキューに追加する。キューが溢れた場合は、溢れた分を捨てる。
	 * 
	 * @param object
	 *            送信情報
	 */
	public void addIfSpace(T object) {
		synchronized (queue) {
			try {
				if (_queueSize != -1 && queue.size() >= _queueSize) {
					log.warn(makeLog("add: Rejected. " + object));
					return;
				}

				queue.add(object);

				if (queue.size() != 0) {
					if (sizeThreshold != -1 && queue.size() % sizeThreshold == 0) {
						executor.submit(new ExecuteTransport());
					}
				}
			} catch (Exception e) {
				log.error(makeLog("add: Failed."), e);
			}
		}
	}

	public boolean addWithWait(T object) {
		return addWithWait(object, -1);
	}

	/**
	 * マネージャへ送信する情報を格納するキューに追加する。キューが溢れた場合はキューが空くまで待つ。
	 * 
	 * @param object
	 *            送信情報
	 * @param timeoutInMillis
	 *            キュー追加待ちのタイムアウト(-1で無限待ち)
	 * @return キュー追加待ちでタイムアウトが発生した場合falseを返す
	 */
	public boolean addWithWait(T object, long timeoutInMillis) {
		// v7.1.0/7.0.2のadd()関数ではExceptionをcatchしてエラーログを出力させException自体は握りつぶしていたが、そのcatchは削除した。
		// 例外が発生する見込みは無く、発生した場合は予期せぬエラーとして呼び元に例外をそのままthrowし、呼び元で対処するように方針を変える。
		try {
			// キュー追加待ちのロック(addの呼び出しが並列で行われた場合の依頼順を順守するため)
			addQueuelock.lock();

			long start = System.currentTimeMillis();
			long outputWarnLogTime = System.currentTimeMillis();
			long totalWaitTime = 0;

			while (true) {
				synchronized (queue) {
					if (queue.offer(object)) {
						if (totalWaitTime > 0) {
							// キュー追加の待ちが発生していた場合にDEBUGログ出力を行う
							if (log.isDebugEnabled()) {
								log.debug(makeLog("add SUCCESS: " + object + ", wait totaltime: " + totalWaitTime + "ms"));
							}
							// キュー追加の待ちが一定時間以上かかった場合はWARNログを出力する
							if (totalWaitTime > addQueueWaitWarnThresholdMSec) {
								log.warn(makeLog("add SUCCESS delayed exceed threshold: " + object + ", wait totaltime: " + totalWaitTime + "ms"));
							}
						}

						if (sizeThreshold != -1 && queue.size() % sizeThreshold == 0) {
							executor.submit(new ExecuteTransport());
						}

						return true;

					} else {
						// キューが飽和状態であれば待つ
						// 初回のみデバッグログ出力
						if (log.isDebugEnabled() && totalWaitTime == 0) {
							log.debug(makeLog("add busy: " + object));
						}
					}
				}

				try {
					if (timeoutInMillis >= 0 && totalWaitTime > timeoutInMillis) {
						// タイムアウトの時間が指定されており、その時間以上経過した場合は処理を抜ける
						log.warn(makeLog("add Timeout. " + object + ", totaltime: " + totalWaitTime + "ms"));
						break;
					}

					long now = System.currentTimeMillis();
					if (now - outputWarnLogTime > addQueueWaitWarnThresholdMSec) {
						// 一定期間でログ出力
						log.warn(makeLog("add wait for busy. " + object + ", wait time: " + totalWaitTime + "ms"));
						outputWarnLogTime = now;
					}

					Thread.sleep(addQueueWaitIntervalMSec);
					totalWaitTime = System.currentTimeMillis() - start;
				} catch (InterruptedException e) {
					// エラーで抜ける
					log.warn(makeLog("Thread interrupted while waiting to add: " + object));
					break;
				}
			}

		} finally {
			addQueuelock.unlock();
		}

		log.warn(makeLog("add: Rejected. " + object));
		return false;
	}

	public void transport() {
		synchronized (queue) {
			if (queue.size() == 0 && buffer.size() == 0) {
				timeThresholdVariable = timeThreshold;
				if(maxTimeThreshold != -1 && timeThresholdVariable != -1){
					scheduleOnce();
				}
				return;
			}

			//送信バッファが空なら送信リストからデータを移動（一回のリクエストでの送信のサイズに上限を考慮）
			//リクエストIDを併せて更新する。
			if (buffer.size() == 0) {  // true なら必然的に queue.size() > 0
				// 上限設定
				int transportSize = queue.size();
				if (blockSize != -1 && transportSize > blockSize) {
					transportSize = blockSize;
				}
				// 移動
				for (int i = 0; i < transportSize; ++i) {
					buffer.add(queue.poll());
				}
				//リクエストID 更新依頼
				requestId = new AgentRequestId();
			}
		}

		//送信バッファのデータを全件送信
		try {
			processor.accept(buffer, requestId);

			log.debug(makeLog("forward: Sended Request[%s](%d/%d)",
					requestId,
					buffer.size(), buffer.size() + queue.size()));
		} catch (Exception e) {
			String msg = makeLog("forward: Failed [%d/%d][%d/%d] Request[%s](%d/%d) %s",
					retryCount.get(), maxTries,
					timeThresholdVariable, maxTimeThreshold,
					requestId,
					buffer.size(), buffer.size() + queue.size(),
					buffer.get(0).toString());
			if (log.isDebugEnabled()) {
				log.warn(msg, e);
			} else {
				log.warn(msg + ", " + e.getClass().getName() + ":" + e.getMessage());
			}
			// リトライ判定
			if(maxTimeThreshold == -1){
				if (retryCount.incrementAndGet() >= maxTries && maxTries != -1) {
					msg = makeLog("forward: Gave up. Request[%s](%d/%d) %s",
							requestId,
							buffer.size(), buffer.size() + queue.size(),
							buffer.get(0).toString());
					if (log.isDebugEnabled()) {
						log.warn(msg);	// スタックトレースは出力済み
					} else {
						log.warn(msg, e);
					}
				} else {
					// retry
					return;
				}
			} else {
				if (timeThresholdVariable != -1) {
					if (timeThresholdVariable * 2 <= maxTimeThreshold) {
						timeThresholdVariable = timeThresholdVariable * 2;
					}
					scheduleOnce();
				}
				return;
			}
		}
		buffer.clear();
		retryCount.set(0);
		timeThresholdVariable = timeThreshold;
		requestId = null;

		if(maxTimeThreshold != -1 && timeThresholdVariable != -1){
			scheduleOnce();
		}
	}

	private void scheduleOnce() {
		if(activeTaskCount.get() <= 1){
			executor.schedule(new ExecuteTransport(), timeThresholdVariable, TimeUnit.MILLISECONDS);
		}
	}

	private String makeLog(String format, Object... args) {
		return "<" + name + "> " + String.format(format, args);
	}

	private class ExecuteTransport implements Runnable {

		public ExecuteTransport() {
			activeTaskCount.incrementAndGet();
		}

		@Override
		public void run() {
			try {
				BlockTransporter.this.transport();
			} finally {
				activeTaskCount.decrementAndGet();
			}
		}
	}

}
