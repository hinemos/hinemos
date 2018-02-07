/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.binary.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.AgentBinaryEndPointWrapper;
import com.clustercontrol.agent.binary.BinaryMonitorConfig;
import com.clustercontrol.ws.agentbinary.BinaryFileDTO;
import com.clustercontrol.ws.agentbinary.BinaryRecordDTO;
import com.clustercontrol.ws.agentbinary.MessageInfo;
import com.clustercontrol.ws.jobmanagement.RunInstructionInfo;
import com.clustercontrol.ws.monitor.BinaryResultDTO;
import com.clustercontrol.ws.monitor.MonitorInfo;

public class BinaryForwarder {

	// ログ出力関連
	/** ロガー */
	private static Log log = LogFactory.getLog(BinaryForwarder.class);

	/** 自身のインスタンス(クラス1に対してインスタンス1) */
	private static final BinaryForwarder _instance = new BinaryForwarder();

	private final ScheduledExecutorService _scheduler;

	public final int _queueMaxSize;

	public final int _transportMaxTries;
	public final int _transportMaxSize;
	public final int _transportIntervalSize;
	public final long _transportIntervalMSec;

	private AtomicInteger transportTries = new AtomicInteger(0);

	private List<BinaryResult> forwardList = new ArrayList<BinaryResult>();

	/**
	 * コンストラクタ.
	 */
	private BinaryForwarder() {
		_queueMaxSize = BinaryMonitorConfig.getBinForwaringQueueMaxSize();
		_transportMaxSize = BinaryMonitorConfig.getBinForwaringTransportMaxSize();
		_transportMaxTries = BinaryMonitorConfig.getBinForwaringTransportMaxTry();
		_transportIntervalSize = BinaryMonitorConfig.getBinForwaringTransportInterval();
		_transportIntervalMSec = BinaryMonitorConfig.getBinForwaringTransportIntervalMsec();

		// 周期的に実行する単一スレッド作成.
		_scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
			private volatile int _count = 0;

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, BinaryForwarder.class.getSimpleName() + _count++);
				t.setDaemon(true);
				return t;
			}
		});

		if (_transportIntervalMSec != -1) {
			_scheduler.scheduleWithFixedDelay(new ScheduledTask(), 0, _transportIntervalMSec, TimeUnit.MILLISECONDS);
		}
	}

	/** 自身のインスタンス(クラス1に対してインスタンス1) */
	public static BinaryForwarder getInstance() {
		return _instance;
	}

	/**
	 * 
	 * マネージャへの送信対象として追加.
	 * 
	 * @param binaryFile
	 *            監視バイナリファイル情報
	 * @param binaryRecords
	 *            監視バイナリレコード
	 * @param msgInfo
	 *            sys_log情報
	 * @param monitorInfo
	 *            監視結果
	 * @param monitorStrValueInfo
	 *            監視対象文字列パターン
	 * @param runInstructionInfo
	 *            ジョブ設定
	 */
	public void add(BinaryFileDTO binaryFile, List<BinaryRecordDTO> binaryRecords, MessageInfo msgInfo,
			MonitorInfo monitorInfo, RunInstructionInfo runInstructionInfo) {
		try {
			ForwardListLock.writeLock();

			if (_queueMaxSize != -1 && forwardList.size() >= _queueMaxSize) {
				log.warn("rejected new binary monitor's result. queue is full : " + binaryRecords.get(0).getOxStr());
				return;
			}

			// 送信リストに追加.
			forwardList.add(new BinaryResult(binaryFile, binaryRecords, msgInfo, monitorInfo, runInstructionInfo));

			if (forwardList.size() != 0) {
				if (_transportIntervalSize != -1 && forwardList.size() % _transportIntervalSize == 0) {
					// マネージャーへの送信処理を周期的に実行するタスクとして送信.
					_scheduler.submit(new ScheduledTask());
				}
			}
		} finally {
			ForwardListLock.writeUnlock();
		}
	}

	/**
	 * 送信処理.
	 */
	private void forward() {
		try {
			ForwardListLock.writeLock();

			while (forwardList.size() > 0) {
				// JAX-WSの一時ファイル肥大化(/tmp/jaxwsXXX)へのワークアラウンド実装(リクエストサイズに上限を設ける)
				int transportSize = _transportMaxSize != -1 && forwardList.size() > _transportMaxSize
						? _transportMaxSize : forwardList.size();
				// 送信失敗直後は1メッセージずつ送信(SOAPのアーキテクチャ上、timeoutなどでメッセージの重複受信は回避できないが、その重複数を最小化する）
				transportSize = transportTries.get() == 0 ? transportSize : 1;

				List<BinaryResult> forwardListPart = Collections
						.unmodifiableList(forwardList.subList(0, transportSize));
				if (forwardListPart.size() > 0) {
					try {
						List<BinaryResultDTO> dtoList = new ArrayList<BinaryResultDTO>(forwardListPart.size());
						for (BinaryResult result : forwardListPart) {
							BinaryResultDTO dto = new BinaryResultDTO();
							dto.setBinaryFile(result._binaryFile);
							dto.getBinaryRecords().addAll(result._binaryRecords);
							dto.setMsgInfo(result._msgInfo);
							dto.setMonitorInfo(result._monitorInfo);
							dto.setRunInstructionInfo(result._runInstructionInfo);
							dtoList.add(dto);
						}
						//
						AgentBinaryEndPointWrapper.forwardBinaryResult(dtoList);

						log.debug(String.format("forward() : sended %d lines.", forwardListPart.size()));
					} catch (Throwable t) {
						String msg = String.format(
								"[%d/%d] failed forwarding binary monitor's result (%d of %d) : %s ...",
								transportTries.get(), _transportMaxTries, forwardListPart.size(), forwardList.size(),
								forwardListPart.get(0)._binaryRecords.get(0).getOxStr());
						if (log.isDebugEnabled()) {
							log.warn(msg, t);
						} else {
							log.warn(msg);
						}
						if (transportTries.incrementAndGet() >= _transportMaxTries && _transportMaxTries != -1) {
							// 最大再送回数を超えている場合.
							msg = String.format(
									"[%d/%d] give up forwarding binary monitor's result (%d of %d) : %s ...",
									transportTries.get(), _transportMaxTries, forwardListPart.size(),
									forwardList.size(), forwardListPart.get(0)._binaryRecords.get(0).getOxStr());
							log.warn(msg, t);
						} else {
							// 再送するためforwardListからremoveする前に処理終了.
							return;
						}
					}

					forwardList.removeAll(forwardListPart);
					transportTries.set(0);
				}
			}
		} catch (RuntimeException e) {
			log.warn("failed forwarding result.", e);
		} finally {
			ForwardListLock.writeUnlock();
		}
	}

	/**
	 * スケジュール実行タスク.
	 */
	private static class ScheduledTask implements Runnable {

		/**
		 * 実行タスク定義.
		 */
		@Override
		public void run() {
			_instance.forward();
		}

	}

	/**
	 * マネージャー送信用バイナリ取得結果.<br>
	 * <br>
	 * 下記と同一構成.<br>
	 * /HinemosManager/src_binary/com/clustercontrol/binary/bean/BinaryResultDTO
	 * .java
	 * 
	 */
	private static class BinaryResult {

		public final BinaryFileDTO _binaryFile;
		public final List<BinaryRecordDTO> _binaryRecords;
		public final MessageInfo _msgInfo;
		public final MonitorInfo _monitorInfo;
		public final RunInstructionInfo _runInstructionInfo;

		public BinaryResult(BinaryFileDTO binaryFile, List<BinaryRecordDTO> binaryRecords, MessageInfo msgInfo,
				MonitorInfo monitorInfo, RunInstructionInfo runInstructionInfo) {
			this._binaryFile = binaryFile;
			this._binaryRecords = binaryRecords;
			this._msgInfo = msgInfo;
			this._monitorInfo = monitorInfo;
			this._runInstructionInfo = runInstructionInfo;
		}
	}

	/**
	 * 送信対象ロック.
	 */
	private static class ForwardListLock {

		private static final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock();

		public static void writeLock() {
			_lock.writeLock().lock();
		}

		public static void writeUnlock() {
			_lock.writeLock().unlock();
		}
	}

}
