/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.util.filemonitor;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtRunInstructionInfoRequest;

import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.agent.util.RestCalendarUtil;
import com.clustercontrol.util.HinemosTime;

/**
 * ログ転送スレッドを管理するクラス<BR>
 * 
 * 転送対象ログファイル情報を受け取り、ログ転送スレッドを制御します。
 * 
 */
public abstract class AbstractFileMonitorManager<T extends AbstractFileMonitorInfoWrapper> {

	/** ロガー */
	private static Log log = LogFactory.getLog(AbstractFileMonitorManager.class);

	/**
	 * ファイルの読み込み状態を保持しているマップ
	 *
	 * キー：監視項目ID＋ファイルパス
	 * 値：ファイル監視オブジェクト
	 */
	protected Map<String, AbstractFileMonitor<T>> logfileMonitorCache =
			new HashMap<String, AbstractFileMonitor<T>>();

	/**
	 * 各ファイル監視の実行フラグを保持するマップ
	 *
	 * キー：logfileMonitorCacheと同じ（監視項目ID＋ファイルパス）
	 * 値：フラグ（スレッドプールに登録後からタスク終了まではtrue、それ以外はfalse）
	 */
	protected Map<String, Boolean> logfileMonitorRunningFlagMap = new HashMap<String, Boolean>();

	/**
	 * 各監視項目の更新日時を保持するマップ
	 * ファイル毎に監視を行うが、場合によっては設定変更後に監視が実行されてしまう。
	 * このため、監視設定の更新日時を保持し、監視タスクからチェックを行う。
	 *
	 * キー：監視項目ID
	 * 値：更新日時（unixtime）
	 */
	protected Map<String, Long> monitorInfoUpdateDateMap = new HashMap<String, Long>();

	/** ファイル監視スレッドプール */
	private ThreadPoolExecutor executor;

	/** Queue送信  */
	protected SendQueue sendQueue;

	/** 読み込み状態 */
	protected AbstractReadingStatusRoot<T> statusRoot;

	/** ログファイル監視スレッド */
	private FileMonitorThread thread;

	/** ログファイル監視設定リスト(監視ジョブ含む) */
	private List<T> monitorList;

	/** 前回監視実行時のログファイル監視設定リスト(読込状態ディレクトリの削除用) */
	private List<T> beforeMonitorList;

	/** 実行中フラグ */
	private boolean isRunning = false;

	/** ファイル監視設定 */
	private FileMonitorConfig fileMonitorConfig;

	/** 監視設定一覧のロック用オブジェクト */
	private final Object monitorListLock = new Object();

	/** logfileMonitorRunningFlagMapのロック用オブジェクト */
	private final Object logfileMonitorRunningFlagLock = new Object();

	/** monitorInfoUpdateDateMap用ロック */
	private ReadWriteLock monitorInfoUpdateDateLock = new ReentrantReadWriteLock();


	/**
	 * コンストラクタ
	 * 
	 * @param fileMonitorConfig
	 */
	protected AbstractFileMonitorManager(FileMonitorConfig fileMonitorConfig){
		this.fileMonitorConfig = fileMonitorConfig;

		int nThreads = 0;
		try {
			nThreads = fileMonitorConfig.getMaxThreads();
		} catch (UnsupportedOperationException e) {
			// 独自起動方法をとっている機能は例外発生するが、その場合は終了する。
			return;
		}
		// UnsupportedOperationException の可能性があるが、getMaxThreads()でチェック済みなのでtry-catchしない。
		final String name = fileMonitorConfig.getThreadName();

		executor = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
					private volatile int _count = 0;

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, name + _count++);
					}
				});
	}

	/**
	 * sendQueue を設定する
	 * 
	 * @param sendQueue
	 */
	public void setSendQueue(SendQueue sendQueue) {
		this.sendQueue = sendQueue;
	}

	/**
	 * 監視設定をスレッドに反映します。<BR>
	 * 
	 * @param list 転送対象ログファイル情報一覧
	 */
	public void pushMonitorInfoList(List<T> monitorInfoList) {
		synchronized (monitorListLock) {
			this.monitorList = monitorInfoList;
		}
	}

	/**
	 * 登録された監視設定を取り出す。<BR>
	 */
	private List<T> popMonitorInfoList() {
		synchronized (monitorListLock) {
			List<T> list = this.monitorList;
			this.monitorList = null;
			return list;
		}
	}

	/**
	 * ファイル監視対象をリフレッシュします。
	 * モニタリストが更新されている場合、ファイル監視インスタンスを変更します。
	 * ディレクトリ・ファイル名変更の場合は、ファイル監視インスタンスを新たに生成します。
	 */
	protected void refresh() {
		log.debug("refresh() : start. monitorInfoUpdateDateMap=" + monitorInfoUpdateDateMap
				+ ", logfileMonitorRunningFlagMap=" + logfileMonitorRunningFlagMap);

		List<T> monitorList = popMonitorInfoList();
		List<T> beforeMonitorList = this.beforeMonitorList;
		// 新たに監視設定が登録されているなら、読み取るファイルおよび読み取り状態を更新する
		if (monitorList != null) {
			log.debug("refresh() : m_monitorList.size=" + monitorList.size());
			// ファイルの読み込み状態の復元
			if (statusRoot == null) {
				statusRoot = createReadingStatusRoot(monitorList, getReadingStatusStorePath(), fileMonitorConfig);
				log.debug("refresh() : ReadingStatusRoot is initialized.");
			} else {
				statusRoot.update(monitorList, beforeMonitorList);
				log.debug("refresh() : ReadingStatusRoot is updated.");
			}
			this.beforeMonitorList = monitorList;
		} else {
			// 監視対象のファイルに対する更新
			if (statusRoot != null)
				statusRoot.update();
		}
		if (statusRoot == null) {
			log.debug("refresh() : ReadingStatusRoot is not initialized.");
			return;
		}

		log.debug("refresh() : monitoring files.");
		try {
			// 更新が中途半端な状態とならないようにロックする
			monitorInfoUpdateDateLock.writeLock().lock();
			synchronized (logfileMonitorRunningFlagLock) {
				// 対象となる監視項目のキーセット
				Set<String> newLogfileMonitorIdKeySet = new HashSet<String>();
				// 対象となるファイル監視のキーセット
				Set<String> newLogfileMonitorCacheKeySet = new HashSet<String>();
				// 監視項目毎にループ
				for (AbstractReadingStatusDir<T> miDir: statusRoot.getReadingStatusDirList()) {
					T wrapper = miDir.getMonitorInfo();
					String id = wrapper.getId();
					String directoryPath = wrapper.getDirectory();
					String osName = System.getProperty("os.name");
					String separateReplace = File.separator;
					if (osName != null && osName.startsWith("Windows")) {
						// Windowsはファイルパスの「/」を許可しているので、比較用に置換しておく
						directoryPath = directoryPath.replace("/", File.separator);
						// Windowsはセパレータ文字が「\」なので、エスケープ文字を追加する
						separateReplace = "\\" + separateReplace;
					}
					directoryPath = directoryPath.replaceAll(separateReplace + "{2,}", separateReplace);
					String fileNamePattern = wrapper.getFileName();
					String fileEncoding = wrapper.getFileEncoding();
					String fileReturnCode = wrapper.getFileReturnCode();
					log.debug("refresh() : monitor info, id=" + id +
							", directory=" + directoryPath +
							", filenamePattern=" + fileNamePattern + 
							", fileEncoding=" + fileEncoding +
							", fileReturnCode=" + fileReturnCode);

					// ファイル毎にループ
					for (AbstractReadingStatus<T> status : miDir.list()) {
						log.debug("refresh() : filePath=" + status.filePath.getPath());
						String cacheKey = id + status.getFilePath().getPath();
						AbstractFileMonitor<T> logfileMonitor = logfileMonitorCache.get(cacheKey);
						if(logfileMonitor == null){
							// ファイル監視オブジェクトを生成。
							logfileMonitor = createFileMonitor(wrapper, status, fileMonitorConfig);
							logfileMonitorCache.put(cacheKey, logfileMonitor);
							log.info("refresh() : LogfileMonitor is created. cacheKey=" + cacheKey);
							// フラグ追加
							log.debug("refresh() : set task running flag as false. id=" + id + ", cacheKey=" + cacheKey);
							logfileMonitorRunningFlagMap.put(cacheKey, false);	// ここでは登録のみ
							// 更新日時追加
							monitorInfoUpdateDateMap.put(id, wrapper.getUpdateDate());
						} else {
							// getFilePath には正式なディレクトリおよびファイル名が記録されているがwrapper のファイル名については
							// 正規表現が入力されている可能性があるので、ファイルパスで前方一致を行う
							if (logfileMonitor.getFilePath().startsWith(directoryPath)) { 
								// 既にキャッシュされている場合、何もしない
								log.debug("refresh() : LogfileMonitor is being cached. cacheKey=" + cacheKey);
							} else {
								// ディレクトリが一致していない場合は再作成
								// ファイル監視オブジェクトをクリーンする
								logfileMonitor.clean();
								// ファイル監視オブジェクトを生成。
								logfileMonitor = createFileMonitor(wrapper, status, fileMonitorConfig);
								logfileMonitorCache.put(cacheKey, logfileMonitor);
								log.info("refresh() : LogfileMonitor is created. Because the directory has been changed. cacheKey=" + cacheKey);
								// 更新日時変更
								monitorInfoUpdateDateMap.put(id, wrapper.getUpdateDate());
							}
						}
						logfileMonitor.setMonitor(wrapper);

						newLogfileMonitorIdKeySet.add(id);
						newLogfileMonitorCacheKeySet.add(cacheKey);
					}
				}
				// 監視対象でないlogfileMonitorをクリーン後、削除
				cleanLogfileMonitorCache(newLogfileMonitorCacheKeySet);
				// 実行フラグマップから削除
				cleanLogfileMonitorRunningFlagMap(newLogfileMonitorCacheKeySet);
				// 更新日時マップから削除
				cleanMonitorInfoUpdateDateMap(newLogfileMonitorIdKeySet);
			}
		} finally {
			monitorInfoUpdateDateLock.writeLock().unlock();
		}
		log.debug("refresh() : end. monitorInfoUpdateDateMap=" + monitorInfoUpdateDateMap
				+ ", logfileMonitorRunningFlagMap=" + logfileMonitorRunningFlagMap);
	}

	/**
	 * ファイル監視マネージャのスレッドを生成、開始します。
	 */
	public synchronized void start() {
		String name = this.getClass().getSimpleName();

		if (thread != null) {
			log.info("start() : FileMonitorThread(" + name  + ") thread is already started.");
			return;
		}

		thread = new FileMonitorThread();
		thread.setName(name);
		thread.start();
		log.info("start() : FileMonitorThread(" + name  + ") thread is started.");
	}

	/**
	 * ファイル監視マネージャのスレッドを終了します。
	 */
	public synchronized void terminate() {
		String name = this.getClass().getSimpleName();

		if (thread == null) {
			log.info("terminate() : FileMonitorThread(" + name  + ") thread is not running.");
			return;
		}

		thread.terminate();
		thread = null;
		log.info("terminate() : FileMonitorThread(" + name  + ") thread is terminated.");
	}

	/**
	 * ファイル監視マネージャのスレッド
	 */
	private class FileMonitorThread extends Thread {
		/** ループフラグ */
		private boolean loop = true;

		/**
		 * 実行メソッド
		 * 指定した定周期でファイル監視のスレッドをスレッドプールに登録します。
		 * スレッドプールの管理、スレッドの実行は {@link ThreadPoolExecutor} で行います。
		 */
		@Override
		public void run() {
			log.info("run FileMonitorThread");
			while (loop) {
				isRunning = true;
				long start = HinemosTime.currentTimeMillis();
				try {
					refresh();

					// ファイル毎にスレッド登録
					for (String cacheKey : logfileMonitorCache.keySet()) {
						log.debug("run(): task submitting to thread pool. cacheKey: " + cacheKey);
						if (!loop) {
							// terminateされた場合次の監視は実行しない
							log.debug("run(): break, loop=" + loop);
							break;
						}

						synchronized (logfileMonitorRunningFlagLock) {
							if (logfileMonitorRunningFlagMap.get(cacheKey)) {
								// 実行中の場合はスレッドプールに登録しない
								log.info("run(): thread is running, so skip to submit thread pool. cacheKey: " + cacheKey);
								continue;
							}
							// スレッドステータスを実行状態に変更
							log.debug("run(): change task running flag to true. cacheKey=" + cacheKey);
							logfileMonitorRunningFlagMap.put(cacheKey, true);
						}

						// スレッドプールに登録し、実行
						log.debug("run(): submit to thread pool. cacheKey=" + cacheKey);
						final AbstractFileMonitor<T> logfileMonitor = logfileMonitorCache.get(cacheKey);
						executor.submit(new Runnable() {
							public void run() {
								logfileMonitor.run();
							}
						});
					}
				} catch (Exception e) {
					log.warn("FileMonitorThread : " + e.getClass().getCanonicalName() + ", " + e.getMessage(), e);
				} catch (Throwable e) {
					log.error("FileMonitorThread : " + e.getClass().getCanonicalName() + ", " + e.getMessage(), e);
				} finally {
					isRunning = false;
				}
				log.debug("run(): thread pool, executor=" + executor);

				if (!loop) {
					log.debug("run(): break, loop=" + loop);
					break;
				}

				try {
					log.debug(String.format("FileMonitorThread run() : elapsed=%d ms.", System.currentTimeMillis() - start));
					Thread.sleep(fileMonitorConfig.getRunInterval());
				} catch (InterruptedException e) {
					log.info("FileMonitorThread is Interrupted");
					break;
				}
			}
			isRunning = false;
			log.info("terminate FileMonitorThread");
		}

		/**
		 * スレッドを終了する。
		 * スレッドプールの実行中タスクは処理が終わるまで続行する。
		 */
		public void terminate() {
			loop = false;

			// スレッドプールに対して終了させる。
			log.trace("terminate(): call executor.shutdown(), executor=" + executor);
			executor.shutdown();
			log.debug("terminate(): end. executor=" + executor);
		}
	}

	/**
	 * Hinemosマネージャへ情報を通知します。<BR>
	 */
	public void sendMessage(String filePath, int priority, String app, String msg, String msgOrg, String monitorId,
			AgtRunInstructionInfoRequest runInstructionInfo) {

		// 監視が無効 or カレンダ非稼働なら通知はしない
		if (!shouldSendMessage(monitorId)) {
			log.info("sendMessage: Suppressed. " + filePath + "," + priority + "," + app + "," + msg + "," + msgOrg + "," + monitorId);
			return;
		}

		// ログ出力情報
		sendMessageInner(filePath, priority, app, msg, msgOrg, monitorId, runInstructionInfo);
	}

	/**
	 * Hinemosマネージャへ情報を通知します。<BR>
	 */
	public void sendMessage(String filePath, int priority, String app, String msg, String msgOrg, T fileMonitorInfoWrapper) {

		// 監視が無効 or カレンダ非稼働なら通知はしない
		if (!shouldSendMessage(fileMonitorInfoWrapper.getId())) {
			log.info("sendMessage: Suppressed. " + filePath + "," + priority + "," + app + "," + msg + "," + msgOrg + "," + fileMonitorInfoWrapper.getId());
			return;
		}

		// ログ出力情報
		sendMessageInner(filePath, priority, app, msg, msgOrg, fileMonitorInfoWrapper);
	}

	protected abstract void sendMessageInner(String filePath, int priority, String app, String msg, String msgOrg,
			String monitorId, AgtRunInstructionInfoRequest runInstructionInfo);

	protected abstract void sendMessageInner(String filePath, int priority, String app, String msg, String msgOrg, T fileMonitorInfoWrapper);

	/**
	 * 指定されたIDの監視設定について、マネージャへの情報通知をすべきなら true、すべきでないなら false を返します。
	 * <p>
	 * 判定仕様について
	 * <ul>
	 * <li>監視ジョブの場合は、常に通知すべきと判定します。
	 * <li>現在時刻がカレンダ非稼働期間の場合は、通知すべきでないと判定します。
	 * <li>監視と収集のいずれかが有効な場合は、通知すべきと判定します。<br/>
	 *     つまり、監視が無効で収集のみ有効な場合でも、通知すべきと判定します。
	 *     収集のみ有効な場合であっても、ログファイルを読み取り、必要ならマネージャへその情報を送信しており、
	 *     その過程で生じたエラーなどは通知すべきだろうという根拠によるものです。
	 * <li>判定に必要な情報が存在しない場合(何らかの理由により未初期化になってしまっている場合など)は、
	 *     通知すべきと判定します。<br/>
	 *     これはできる限り情報を握り潰すべきではないという根拠によるものです。
	 * </ul>
	 */
	protected boolean shouldSendMessage(String monitorId) {
		// 状況的にありえないケースもあるかもしれないが、念のためにしっかりと null チェックする。
		// 判定に必要な情報が null の場合は、情報を握り潰さないように true とする。
		if (monitorId == null) {
			log.debug("shouldSendMessage: True; monitorId is null.");
			return true;
		}
		if (statusRoot == null) {
			log.debug("shouldSendMessage: True; statusRoot is null. monitorId=" + monitorId);
			return true;
		}
		AbstractReadingStatusDir<T> rsDir = statusRoot.getReadingStatusDir(monitorId);
		if (rsDir == null) {
			log.debug("shouldSendMessage: True; ReadingStatusDir is null. monitorId=" + monitorId);
			return true;
		}
		AbstractFileMonitorInfoWrapper wrapper = rsDir.getMonitorInfo();
		if (wrapper == null) {
			log.debug("shouldSendMessage: True; MonitorInfoWrapper is null. monitorId=" + monitorId);
			return true;
		}
		// 監視ジョブなら、有効/無効やカレンダは関係なく true
		if (wrapper.isMonitorJob()) {
			log.debug("shouldSendMessage: True; Monitor-Job. monitorId=" + monitorId);
			return true;
		}
		// カレンダ非稼働なら、有効/無効は関係なく false
		if (wrapper.getCalendar() != null && !RestCalendarUtil.isRun(wrapper.getCalendar())) {
			log.debug("shouldSendMessage: False; Disabled by the calendar. monitorId=" + monitorId + ", calendarId=" + wrapper.getCalendarId());
			return false;
		}
		// 監視か収集のいずれかが有効なら true、そうでなければ false
		if (wrapper.getMonitorFlg() || wrapper.getCollectorFlg()) {
			log.debug("shouldSendMessage: True; Enabled. monitorId=" + monitorId + ", flags=" + wrapper.getMonitorFlg() + ", " + wrapper.getCollectorFlg());
			return true;
		} else {
			log.debug("shouldSendMessage: False; Disabled. monitorId=" + monitorId);
			return false;
		}
	}

	/**
	 * 実行中かどうかを返します。
	 * 
	 * @return ファイル監視マネージャのスレッドが実行中、かつ、全ファイル監視スレッドが終了していない場合true。それ以外はfalse。
	 */
	public boolean isRunning() {
		// スレッドプールの実行中の全タスクが終了すると isTerminated は true となる。
		boolean isTerminated = executor.isTerminated();
		log.debug("isRunning():  isRunning=" + isRunning + ", isTerminated=" + isTerminated + ", executor=" + executor);
		return isRunning && !isTerminated;
	}

	/**
	 * ファイル監視の完了設定
	 * AbstractFileMonitorが終了した場合に設定します。
	 * 
	 * @param id 監視項目ID
	 * @param filePath ファイルパス
	 */
	public void doneFileMonitor(String id, String filePath) {
		String cacheKey = id + filePath;
		synchronized (logfileMonitorRunningFlagLock) {
			// スレッドステータスを変更
			log.debug("doneFileMonitor(): change task running flag to false. cacheKey=" + cacheKey);
			logfileMonitorRunningFlagMap.put(cacheKey, false);
		}
	}

	/**
	 * 監視項目の更新日時を取得します
	 * 
	 * @param id 監視項目ID
	 * @return 更新日時
	 */
	public Long getMonitorInfoUpdateDate(String id) {
		try {
			monitorInfoUpdateDateLock.readLock().lock();
			return monitorInfoUpdateDateMap.get(id);
		} finally {
			monitorInfoUpdateDateLock.readLock().unlock();
		}
	}

	public abstract String getReadingStatusStorePath();

	public abstract AbstractFileMonitor<T> createFileMonitor(T monitorInfo, AbstractReadingStatus<T> status,
			FileMonitorConfig fileMonitorConfig);

	public abstract AbstractReadingStatusRoot<T> createReadingStatusRoot(List<T> miList,
			String baseDirectory, FileMonitorConfig fileMonitorConfig);

	/**
	 * 監視対象でないlogfileMonitorをクリーン後、削除
	 *
	 * @param logfileMonitorCacheKeySet 監視対象logfileMonitorのキーセット
	 */
	private void cleanLogfileMonitorCache(Set<String> logfileMonitorCacheKeySet) {
		log.debug("cleanLogfileMonitorCache() : start");
		Iterator<Entry<String, AbstractFileMonitor<T>>> it = logfileMonitorCache.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, AbstractFileMonitor<T>> entry = it.next();
			if (!logfileMonitorCacheKeySet.contains(entry.getKey())) {
				log.info("cleanLogfileMonitorCache() : LogfileMonitor is removed from cache. cacheKey=" + entry.getKey());
				// クリーン
				entry.getValue().clean();
				it.remove();
			}
		}
	}

	/**
	 * 実行フラグマップの対象外を削除
	 *
	 * @param logfileMonitorCacheKeySet 監視対象logfileMonitorのキーセット
	 */
	private void cleanLogfileMonitorRunningFlagMap(Set<String> logfileMonitorCacheKeySet) {
		log.debug("cleanLogfileMonitorRunningFlagMap() : start");
		Iterator<Entry<String, Boolean>> it = logfileMonitorRunningFlagMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Boolean> entry = it.next();
			if (!logfileMonitorCacheKeySet.contains(entry.getKey())) {
				log.info("cleanLogfileMonitorRunningFlagMap() : remove from logfileMonitorRunningFlagMap. cacheKey=" + entry.getKey());
				it.remove();
			}
		}
	}

	/**
	 * 更新日時マップの対象外を削除
	 *
	 * @param logfileMonitorIdKeySet 監視対象の監視項目キーセット
	 */
	private void cleanMonitorInfoUpdateDateMap(Set<String> logfileMonitorIdKeySet) {
		log.debug("cleanMonitorInfoUpdateDateMap() : start");
		Iterator<Entry<String, Long>> it = monitorInfoUpdateDateMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Long> entry = it.next();
			if (!logfileMonitorIdKeySet.contains(entry.getKey())) {
				log.info("cleanMonitorInfoUpdateDateMap() : remove from monitorInfoUpdateDateMap. id=" + entry.getKey());
				it.remove();
			}
		}
	}

}
