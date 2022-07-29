/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.binary;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtBinaryCheckInfoResponse;
import org.openapitools.client.model.AgtMonitorInfoResponse;
import org.openapitools.client.model.AgtOutputBasicInfoRequest;
import org.openapitools.client.model.AgtRunInstructionInfoRequest;
import org.openapitools.client.model.AgtRunInstructionInfoResponse;
import org.pcap4j.core.PcapHandle;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.agent.SendQueue.MessageSendableObject;
import com.clustercontrol.agent.binary.packet.PacketCapture;
import com.clustercontrol.agent.binary.packet.PacketListenerImpl;
import com.clustercontrol.agent.binary.readingstatus.DirectoryReadingStatus;
import com.clustercontrol.agent.binary.readingstatus.FileReadingStatus;
import com.clustercontrol.agent.binary.readingstatus.MonitorReadingStatus;
import com.clustercontrol.agent.binary.readingstatus.RootReadingStatus;
import com.clustercontrol.agent.binary.thread.EveryIntervalMonitorThread;
import com.clustercontrol.agent.binary.thread.IncrementMonitorThread;
import com.clustercontrol.agent.binary.thread.PacketCaptureThread;
import com.clustercontrol.agent.binary.thread.WholeFileMonitorThread;
import com.clustercontrol.agent.log.MonitorInfoWrapper;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.binary.bean.BinaryConstant;
import com.clustercontrol.util.FileUtil;
import com.clustercontrol.util.HinemosTime;

/**
 * バイナリファイル転送スレッド管理クラス.<br>
 * <br>
 * 転送対象のバイナリファイルに関する情報の受取と<br>
 * 転送スレッドの制御を行う.<br>
 * 
 * @since 6.1.0
 * @version 6.1.0
 */
public class BinaryMonitorManager {

	/** ロガー */
	private static Log log = LogFactory.getLog(BinaryMonitorManager.class);

	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	/**
	 * 監視対象ファイルのキャッシュ情報 <br>
	 * <br>
	 * 親マップ<スレッドID,子マップ> <br>
	 * 子マップ<監視ID＋ファイルパス,ファイルの読込状態><br>
	 */
	public static Map<String, Map<String, BinaryMonitor>> binMonCacheMap = new ConcurrentHashMap<String, Map<String, BinaryMonitor>>();

	/** マネージャ送信用Queue */
	private static SendQueue sendQueue;

	/** 読込状態 <スレッドID,読込状態> */
	public static Map<String, RootReadingStatus> rootRSMap = new ConcurrentHashMap<String, RootReadingStatus>();

	/** バイナリ監視スレッド */
	private static BinaryThread binaryThread;

	/** 親スレッド名 */
	private static final String parentThreadName = "BinaryThread";

	/** 監視設定一覧 */
	private static List<MonitorInfoWrapper> monitorList;

	/** 監視設定一覧のロック用オブジェクト */
	private static final Object monitorListLock = new Object();

	/** スレッド種別 **/
	private static enum ThreadType {
		/** パケットキャプチャ **/
		PACKET_CAPTURE,
		/** ファイル全体 **/
		WHOLE_FILE,
		/** 時間区切り **/
		CUT_INTERVAL,
		/** レコード長区切り **/
		CUT_LENGTH,
		/** エラー(処理種別に必要な情報が存在しない等) **/
		ERROR
	}

	/**
	 * クラス初期化処理.
	 */
	static {

		// ファイル全体監視の一時ファイルが残っているようであれば削除する.
		deleteTemporaryFiles: {
			File rsDirectory = RootReadingStatus.getRootStoreDirectory();
			if (!rsDirectory.exists()) {
				log.debug(String.format(
						"skip to delete temporary files, because directory of RS isn't exist." + " directory=[%s]",
						rsDirectory.getAbsolutePath()));
				break deleteTemporaryFiles;
			}
			ArrayList<File> allFileList = new ArrayList<File>();
			FileUtil.addFileList(rsDirectory, allFileList, null, 500, true);
			if (allFileList.isEmpty()) {
				log.debug("skip to delete temporary files, because directory of RS has no file.");
				break deleteTemporaryFiles;
			}
			ArrayList<File> tempFileList = new ArrayList<File>();
			for (File fileDir : allFileList) {
				if (!fileDir.exists()) {
					// 削除された場合はスキップ.
					continue;
				}
				if (BinaryMonitor.matchTmpFileName(fileDir.getName())) {
					// 一時ファイルの名前に合致した場合.
					tempFileList.add(fileDir);
				}
			}
			allFileList = null;
			// 削除対象の一時ファイルが存在しない場合は削除しない.
			if (tempFileList.isEmpty()) {
				log.debug("skip to delete temporary files, because temporary file isn't exist.");
				break deleteTemporaryFiles;
			}

			log.debug(String.format("prepared to delete temporary files." + " size(tmpFiles)=%d", tempFileList.size()));
			for (File tempFile : tempFileList) {
				if (!tempFile.exists()) {
					// 削除された場合はスキップ.
					log.debug("skip to delete temporary file, because temporary file or fileRS isn't exist.");
					continue;
				}
				// 一時ファイルのファイル名から監視対象ファイル名を取得.
				String monitorFileName = BinaryMonitor.getMonNameFromTmp(tempFile.getName());
				if (monitorFileName == null) {
					continue;
				}

				// 対応ファイルRSのパスを生成.
				if (tempFile.getParentFile() == null) {
					log.warn(String.format(
							"skip to delete temporary file, because failed to get parent." + " tempFile=[%s]",
							tempFile.getAbsolutePath()));
					continue;
				}
				File fileRs = new File(tempFile.getParentFile(), monitorFileName + ".json");

				// 対応するファイルRSが存在しない場合はスキップ.
				if (!fileRs.exists()) {
					log.warn(String.format(
							"skip to delete temporary file, because file RS isn't exist."
									+ " tempFile=[%s], fileRS=[%s]",
							tempFile.getAbsolutePath(), fileRs.getAbsolutePath()));
				}

				// 一時ファイルの削除.
				if (!tempFile.delete()) {
					log.warn(String.format("skip to delete temporary file." + " tempFile=[%s]",
							tempFile.getAbsolutePath()));
				} else {
					log.info(String.format("success to delete temporary file." + " tempFile=[%s]",
							tempFile.getAbsolutePath()));
				}

				// ファイルRSの更新(runMonitorをfalseにする).
				FileReadingStatus.closeRumMonitor(fileRs, monitorFileName);
			}

		}

	}

	/**
	 * マネージャへの返信キューを設定.<br>
	 * <br>
	 * ジョブ実行結果（チェックや開始含む）をQueue送信する際に<br>
	 * 他の監視やジョブ実行結果と共に送信するため共通のQueueをTopic上で設定.
	 * 
	 * @param sendQueue
	 *			  監視管理Queue送信
	 */
	public static void setSendQueue(SendQueue sendQueue) {
		BinaryMonitorManager.sendQueue = sendQueue;
	}

	/**
	 * バイナリ監視スレッド開始.<br>
	 * <br>
	 * ※スレッド処理内容はインナークラスにて定義
	 *
	 */
	public static void start() {
		synchronized (BinaryMonitorManager.class) {

			if (binaryThread != null) {
				log.info("start() : " + parentThreadName + "  is already started.");
				return;
			}

			if (binaryThread == null) {
				// 定期的な処理が必要な監視スレッド.
				binaryThread = new BinaryThread();
				binaryThread.setName(parentThreadName);
				binaryThread.start();
				log.info("start() : " + parentThreadName + " is started.");
			}
		}
	}

	/**
	 * バイナリ監視スレッド終了.<br>
	 *
	 */
	public static void terminate() {
		synchronized (BinaryMonitorManager.class) {
			if (binaryThread == null) {
				log.info("terminate() : " + parentThreadName + " is not started.");
				return;
			}

			if (binaryThread != null) {
				// ファイル増分監視(時間区切り以外)のスレッド.
				binaryThread.terminate();
				binaryThread = null;
				log.info("terminate() : " + parentThreadName + " is terminated.");
			}

			// OutOfMemoryError防止用に不要なオブジェクトをクリアしておく.
			if (binaryThread == null) {
				BinaryMonitorManager.rootRSMap.clear();
				BinaryMonitorManager.binMonCacheMap.clear();
				log.info("terminate() : " + parentThreadName + " is not started.");
			}
		}
	}

	/**
	 * バイナリ監視スレッド定義クラス.<br>
	 * <br>
	 * 監視設定を取得し、監視設定別に必要な子スレッドを作成・設定変更する.<br>
	 */
	private static class BinaryThread extends Thread {

		/** スレッド繰返し(trueだとrun()の処理内容が繰返される). */
		private boolean loop = true;

		/** 子スレッド(時間区切り監視用)(キー:監視ID,値：スレッド). */
		private Map<String, EveryIntervalMonitorThread> everyIntMonThreadMap = new ConcurrentHashMap<String, EveryIntervalMonitorThread>();

		/** 子スレッド(パケットキャプチャ)(キー:監視ID,値：スレッド). */
		private PacketCaptureThread pcapThread;

		/** 子スレッド(時間区切り以外の増分監視用・自動生成pcapファイル含む). */
		private IncrementMonitorThread incrementMonitorThread;

		/** 子スレッド(任意バイナリファイル監視用). */
		private WholeFileMonitorThread wholeFileMonitorThread;

		/** シグナル(キューへの要素挿入)を待機するための blocking queue */
		private BlockingQueue<Object> signalQueue = new ArrayBlockingQueue<>(1);

		/** {@link #signalQueue}へ送るシグナル */
		private static final Object WAKE_UP_SIGNAL = new Object();

		/**
		 * スレッド実行処理.
		 */
		@Override
		public void run() {
			log.info("run " + parentThreadName);
			while (loop) {
				try {
					// 処理時間計測用
					long startTime = System.currentTimeMillis();

					// 最新の監視設定を取得.
					List<MonitorInfoWrapper> newMonList = popMonitorInfoList();

					// 取得した監視設定をログ出力.
						if (newMonList == null) {
						log.info("run: " + parentThreadName + " got new monitor info list. size=null");
						} else {
						StringBuilder monIds = new StringBuilder();
						String delim = "";
						for (MonitorInfoWrapper monInfo : newMonList) {
							monIds.append(delim).append(monInfo.getId());
							delim =", ";
								}
						log.info("run: " + parentThreadName + " got new monitor info list. size=" + newMonList.size()
								+ ", monitorId=[" + monIds + "]");
					}

					// 監視設定ファイル毎に必要なスレッドを生成.
					if (newMonList != null) {

						// スレッドに紐付ける監視設定の初期化.
						List<MonitorInfoWrapper> wholeFileMonList = null;
						Map<String, PacketListenerImpl> pcapListenerMap = null;
						Map<String, MonitorInfoWrapper> pcapMonitorMap = null;
						Map<String, MonitorInfoWrapper> incrementMonMap = null;

						// スレッドが完了している場合はnullで初期化しとく.
						if (incrementMonitorThread != null && !incrementMonitorThread.isAlive()) {
							incrementMonitorThread = null;
						}
						if (wholeFileMonitorThread != null && !wholeFileMonitorThread.isAlive()) {
							wholeFileMonitorThread = null;
						}

						// ↓ループここから--------------------------------------------
						// 定期監視用のスレッドを作成・スレッドに紐づく監視設定を更新.
						for (MonitorInfoWrapper moninfo : newMonList) {
							// 監視ジョブ以外で監視フラグと収集フラグがoffの場合は監視処理走らせないのでskip
							if ((!moninfo.monitorInfo.getMonitorFlg().booleanValue()
									&& !moninfo.monitorInfo.getCollectorFlg().booleanValue())
									&& moninfo.runInstructionInfo == null) {
								log.debug(String.format(
										"run() : skip to create thread. monitorID=%s, monitorFlg=%b,  collectorFlg=%b",
										moninfo.getId(),
										moninfo.monitorInfo.getMonitorFlg(),
										moninfo.monitorInfo.getCollectorFlg()));
								continue;
							}

							ThreadType threadType = this.getThreadType(moninfo);

							switch (threadType) {

							case PACKET_CAPTURE:
								// パケットキャプチャ用のスレッドに紐づくListener生成.
								if (pcapListenerMap == null) {
									pcapListenerMap = new ConcurrentHashMap<String, PacketListenerImpl>();
								}
								moninfo = this.addNewListenerMap(moninfo, pcapListenerMap);
								log.debug(String.format(
										"run() : added new LiseneMap. monitor fileName=%s, monitor directory=%s",
										moninfo.monitorInfo.getBinaryCheckInfo().getFileName(),
										moninfo.monitorInfo.getBinaryCheckInfo().getDirectory()));

								// パケットキャプチャ自動生成ファイルの監視準備.
								if (pcapMonitorMap == null) {
									pcapMonitorMap = new ConcurrentHashMap<String, MonitorInfoWrapper>();
								}
								// 新しい監視設定として追加(スレッド紐付けマップのキーで上書きされる).
								pcapMonitorMap.put(moninfo.getId(), moninfo);
								log.debug(IncrementMonitorThread.threadName + "is added monitorInfo. monitorID="
										+ moninfo.getId());
								break;

							case WHOLE_FILE:
								// ファイル全体監視のスレッドに設定する監視設定リストのセット.
								if (wholeFileMonList == null) {
									wholeFileMonList = new ArrayList<MonitorInfoWrapper>();
								}
								// 新しい監視設定として追加(スレッド紐付けリストを総入替え).
								wholeFileMonList.add(moninfo);
								log.debug("wholeFileMonList is added monitorInfo. monitorID=" + moninfo.getId());
								break;

							case CUT_INTERVAL:
								// 処理中のスレッドに対象の監視設定が存在するか判定.
								boolean createNewThread = false;
								if (everyIntMonThreadMap.keySet().contains(moninfo.getId())) {
									// 定期的に監視処理を行うスレッドが存在する場合.
									EveryIntervalMonitorThread runningThread = everyIntMonThreadMap
											.get(moninfo.getId());
									if (runningThread.isAlive()) {
										// スレッド紐付け監視設定をリフレッシュ.
										runningThread.refreshMonInfo(moninfo);
										log.debug(EveryIntervalMonitorThread.threadName + " is refreshed. monitorID="
												+ moninfo.getId());
										createNewThread = false;
									} else {
										// 停止してる場合はマップからオブジェクト削除して新しくスレッド生成.
										everyIntMonThreadMap.remove(moninfo.getId());
										createNewThread = true;
										log.debug(EveryIntervalMonitorThread.threadName
												+ " is new created because old is terminated. monitorID="
												+ moninfo.getId());
									}
								} else {
									// 存在しない場合は単純に定期的に監視処理を行うスレッドを作成する.
									createNewThread = true;
									log.debug(EveryIntervalMonitorThread.threadName + " is new created. monitorID="
											+ moninfo.getId());
								}
								// 時間区切りファイル監視スレッドの生成(監視設定の単位で生成).
								if (createNewThread) {
									EveryIntervalMonitorThread newThread = new EveryIntervalMonitorThread(moninfo);
									newThread.setName(EveryIntervalMonitorThread.threadName + "" + moninfo.getId());
									newThread.start();
									everyIntMonThreadMap.put(moninfo.getId(), newThread);
									log.info("start() : " + EveryIntervalMonitorThread.threadName + " is started for "
											+ moninfo.getId());
								}
								break;

							case CUT_LENGTH:
								// ファイル増分監視の監視設定.
								if (incrementMonMap == null) {
									incrementMonMap = new ConcurrentHashMap<String, MonitorInfoWrapper>();
								}
								// 新しい監視設定として追加(スレッド紐付けマップのキーで上書きされる).
								incrementMonMap.put(moninfo.getId(), moninfo);
								log.debug(IncrementMonitorThread.threadName + "is added monitorInfo. monitorID="
										+ moninfo.getId());
								break;

							case ERROR:
								// スレッド種別取得時にログ出力済.
								break;

							default:
								// 想定外のパターンなのでログ出力.
								log.warn("it's unexpected threadType for binary monitor. threadType=[" + threadType
										+ "]");
								break;
							}
						}
						// ↑ループここまで--------------------------------------------

						// ファイル増分監視のスレッド用に増分監視とパケットキャプチャの監視設定をセットする.
						Map<String, MonitorInfoWrapper> allMonMap = new ConcurrentHashMap<String, MonitorInfoWrapper>();
						if (incrementMonMap != null && !incrementMonMap.isEmpty()) {
							allMonMap.putAll(incrementMonMap);
						}
						if (pcapMonitorMap != null && !pcapMonitorMap.isEmpty()) {
							allMonMap.putAll(pcapMonitorMap);
						}
						// スレッドの作成・紐付き監視設定の更新.
						this.createThread(wholeFileMonList, pcapListenerMap, allMonMap);

						// 監視設定から削除されたスレッドの停止.
						this.removeThreadMap(newMonList);

						// 取得された監視設定から不要なRSディレクトリの削除
						RootReadingStatus.refreshRootStoreDirectory(newMonList);
					}
					// 設定反映に掛かった時間をログ出力
					log.info("run: " + parentThreadName + " spent " + (System.currentTimeMillis() - startTime) + "ms.");

				} catch (Exception e) {
					log.warn(parentThreadName + " : " + e.getClass().getCanonicalName() + ", " + e.getMessage(), e);
				} catch (Throwable e) {
					log.error(parentThreadName + " : " + e.getClass().getCanonicalName() + ", " + e.getMessage(), e);
				}

				try {
					// シグナルが来るまで待機する (戻り値はなんでもよい)
					signalQueue.take();
				} catch (InterruptedException e) {
					log.info(parentThreadName + " for is Interrupted");
					break;
				}
			} // while (loop)
			this.close();
		}

		/**
		 * スレッド種別取得.
		 * 
		 * @return スレッド種別.
		 */
		private ThreadType getThreadType(MonitorInfoWrapper moninfo) {
			String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

			// 監視種別で判定.
			String monitorTypeId = moninfo.monitorInfo.getMonitorTypeId();
			if (monitorTypeId == null || monitorTypeId.isEmpty()) {
				// 取得不可.
				log.warn(methodName + DELIMITER + "faild to get monitor type Id. monitorID=" + moninfo.getId());
				return ThreadType.ERROR;
			}
			if (HinemosModuleConstant.MONITOR_PCAP_BIN.equals(monitorTypeId)) {
				// パケットキャプチャ監視.
				log.debug(methodName + DELIMITER
						+ String.format("get thread type. threadType=%s, monitorID=%s, monitorTypeId=%s",
								ThreadType.PACKET_CAPTURE, moninfo.getId(), monitorTypeId));
				return ThreadType.PACKET_CAPTURE;
			}
			if (!HinemosModuleConstant.MONITOR_BINARYFILE_BIN.equals(monitorTypeId)) {
				// バイナリ監視に存在しない監視種別IDの場合はエラー(想定外).
				log.warn(methodName + DELIMITER + String.format(
						"invalid monitor type ID. monitorID=%s, monitorTypeId=%s", moninfo.getId(), monitorTypeId));
				return ThreadType.ERROR;
			}

			// 収集対象で判定.
			String collectType = moninfo.monitorInfo.getBinaryCheckInfo().getCollectType();
			if (collectType == null || collectType.isEmpty()) {
				log.warn(methodName + DELIMITER + "faild to get collectType. monitorID=" + moninfo.getId());
				return ThreadType.ERROR;
			}
			if (BinaryConstant.COLLECT_TYPE_WHOLE_FILE.equals(collectType)) {
				// ファイル全体.
				log.debug(methodName + DELIMITER
						+ String.format("get thread type. threadType=%s, monitorID=%s, collectType=%s",
								ThreadType.WHOLE_FILE, moninfo.getId(), collectType));
				return ThreadType.WHOLE_FILE;
			}
			if (!BinaryConstant.COLLECT_TYPE_ONLY_INCREMENTS.equals(collectType)) {
				// 存在しない収集対象の場合はエラー(想定外).
				log.warn(methodName + DELIMITER + String.format("invalid collect type. monitorID=%s, collectType=%s",
						moninfo.getId(), collectType));
				return ThreadType.ERROR;
			}

			// レコード分割方法で判定.
			String cutType = moninfo.monitorInfo.getBinaryCheckInfo().getCutType();
			if (cutType == null || cutType.isEmpty()) {
				log.warn(methodName + DELIMITER + "faild to get cutType. monitorID=" + moninfo.getId());
				return ThreadType.ERROR;
			}
			if (BinaryConstant.CUT_TYPE_INTERVAL.equals(cutType)) {
				// 時間区切り.
				log.debug(methodName + DELIMITER
						+ String.format("get thread type. threadType=%s, monitorID=%s, cutType=%s",
								ThreadType.CUT_INTERVAL, moninfo.getId(), cutType));
				return ThreadType.CUT_INTERVAL;
			}
			if (!BinaryConstant.CUT_TYPE_LENGTH.equals(cutType)) {
				// 存在しない分割方法の場合はエラー(想定外).
				log.warn(methodName + DELIMITER
						+ String.format("invalid collect type. monitorID=%s, cutType=%s", moninfo.getId(), cutType));
				return ThreadType.ERROR;
			}

			// レコード長指定.
			log.debug(methodName + DELIMITER + String.format("get thread type. threadType=%s, monitorID=%s, cutType=%s",
					ThreadType.CUT_LENGTH, moninfo.getId(), cutType));
			return ThreadType.CUT_LENGTH;
		}

		/**
		 * パケットキャプチャスレッド作成.
		 * 
		 * @return 自動生成dumpを監視対象とするためのディレクトリ・ファイル名を設定して返却
		 */
		private MonitorInfoWrapper addNewListenerMap(MonitorInfoWrapper moninfo,
				Map<String, PacketListenerImpl> newListnerMap) {
			String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
			log.debug(methodName + DELIMITER + "start. monitorID=" + moninfo.getId());

			// メモリリーク等でスレッドが完了している場合は初期化しとく.
			if (pcapThread != null && !pcapThread.isAlive()) {
				pcapThread = null;
			}

			// パケットキャプチャスレッドに取得した監視設定が含まれている場合は除去.
			Map<String, PacketListenerImpl> listnerMap = null;
			if (pcapThread != null) {
				String newUpDate = "no exist";
				String oldUpDate = "no exist";
				listnerMap = pcapThread.readListnerMap();
				MonitorInfoWrapper oldMonitorInfo = null;
				// 現在動いているスレッドに紐づくリスナー(監視設定×IP単位)毎に監視設定更新チェック.
				for (Entry<String, PacketListenerImpl> entry : listnerMap.entrySet()) {
					MonitorInfoWrapper oldPcapMonitorInfo = entry.getValue().getMonInfo();
					String oldPcapMonId = oldPcapMonitorInfo.getId();
					if (oldPcapMonId.equals(moninfo.getId())) {
						// 存在した場合は監視設定が更新されてるかチェック.
						if (!(moninfo.monitorInfo.getUpdateDate() > oldPcapMonitorInfo.monitorInfo.getUpdateDate())) {
							// 更新されていないので前の監視設定をそのまま更新用にセット
							newListnerMap.put(entry.getKey(), entry.getValue());
							oldMonitorInfo = oldPcapMonitorInfo;
						}
						newUpDate = new Timestamp(moninfo.monitorInfo.getUpdateDate()).toString();
						oldUpDate = new Timestamp(oldPcapMonitorInfo.monitorInfo.getUpdateDate()).toString();
					}
				}
				// 監視設定に紐づくIP全量分マップにセットしてから、更新されてない旧監視設定を返却
				if (oldMonitorInfo != null) {
					log.debug(methodName + DELIMITER + PacketCaptureThread.threadName
							+ String.format(
									" is added old listener and monitor info. monitorID=%s, newUpDate=%s, oldUpDate=%s",
									moninfo.getId(), newUpDate, oldUpDate));
					return oldMonitorInfo;
				} else {
					// 旧監視設定に存在しない場合は no exist.
					log.debug(methodName + DELIMITER + PacketCaptureThread.threadName
							+ String.format(
									" is added new listener and monitor info. monitorID=%s, newUpDate=%s, oldUpDate=%s",
									moninfo.getId(), newUpDate, oldUpDate));
				}
			}

			// 監視処理・パケットキャプチャスレッド用に必要な監視設定をセット.
			moninfo.monitorInfo.setBinaryCheckInfo(getNewBinaryCheckInfo(moninfo.getId()));

			// 一意となるIPリストを取得(Handle生成時に統一したフォーマットでさらに絞り込む).
			List<String> addressList = this.getAddressByNif();
			// ネットワークデバイスに応じたIPアドレスの分だけリスナーを生成してスレッドに追加.
			if (addressList != null && (!addressList.isEmpty())) {
				// 取得できたIPアドレス毎にリスナーを作成する.
				listnerMap = new TreeMap<String, PacketListenerImpl>();
				for (String address : addressList) {
					// パケットキャプチャ制御用のオブジェクト.
					PacketCapture packetCapture = new PacketCapture();
					PcapHandle handle = packetCapture.getPcapHandle(moninfo, address);
					PacketListenerImpl listener = packetCapture.getListener(address, moninfo, handle);
					if (listener != null) {
						// パケットキャプチャ制御用オブジェクトが作成できた場合.
						listnerMap.put(moninfo.getId() + address, listener);
						log.debug(String.format(methodName + DELIMITER + "listener is added. monitorID=%s, IP=%s",
								moninfo.getId(), address));
					} else {
						log.debug(String.format(methodName + DELIMITER + "listener isn't added. monitorID=%s, IP=%s",
								moninfo.getId(), address));
					}
				}

				newListnerMap.putAll(listnerMap);
				log.debug(methodName + DELIMITER + PacketCaptureThread.threadName + " is added new listener. monitorID="
						+ moninfo.getId());
			} else {
				log.debug(methodName + DELIMITER + "monitorId is skipped to add to " + PacketCaptureThread.threadName
						+ ", because of no ip. monitorID=" + moninfo.getId());
			}

			return moninfo;

		}

		/**
		 * パケットキャプチャ向けのバイナリファイル監視情報を設定.<br>
		 * <br>
		 * パケットキャプチャ監視はパケットキャプチャ後ダンプファイルを出力し、<br>
		 * ダンプファイルに対してバイナリファイル監視を実施する.<br>
		 * 
		 */
		private AgtBinaryCheckInfoResponse getNewBinaryCheckInfo(String monitorId) {
			AgtBinaryCheckInfoResponse returnNewInfo = new AgtBinaryCheckInfoResponse();

			// パケットキャプチャ出力ダンプファイルに関する情報.
			String storeDir = BinaryMonitorConfig.getPcapExportDir();
			returnNewInfo.setDirectory(storeDir);
			returnNewInfo.setFileName(monitorId + ".*\\.pcap");

			// 他.pcapファイル向けの設定(デフォルトプリセットファイルpcapと同等の内容).
			returnNewInfo.setCollectType(BinaryConstant.COLLECT_TYPE_ONLY_INCREMENTS);
			returnNewInfo.setCutType(BinaryConstant.CUT_TYPE_LENGTH);
			returnNewInfo.setTagType(BinaryConstant.TAG_TYPE_PCAP);
			returnNewInfo.setFileHeadSize(24L);

			returnNewInfo.setLengthType(BinaryConstant.LENGTH_TYPE_VARIABLE);
			returnNewInfo.setRecordSize(0);
			returnNewInfo.setRecordHeadSize(16);
			returnNewInfo.setSizePosition(9);
			returnNewInfo.setSizeLength(4);

			returnNewInfo.setHaveTs(true);
			returnNewInfo.setTsPosition(1);
			returnNewInfo.setTsType(BinaryConstant.TS_TYPE_SEC_AND_USEC);

			returnNewInfo.setLittleEndian(true);

			return returnNewInfo;
		}

		/**
		 * ネットワークインターフェース(NIF)単位でアドレスを取得する.
		 */
		private List<String> getAddressByNif() {
			// AgentInfoから取得(AgentInfoの更新含む)(IPv4/IPv6 設定されてれば両方取得).
			List<String> addressListDouble = Agent.getAgentInfoRequest().getIpAddressList();
			List<String> addressList = new ArrayList<String>();

			// AgentInfoから取得したアドレスをNIFで一意にする
			try {
				Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
				if (null != networkInterfaces) {
					// Agentサーバーに紐づくNIFでループ.
					while (networkInterfaces.hasMoreElements()) {
						NetworkInterface ni = networkInterfaces.nextElement();
						Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
						// NIFに紐づくアドレスでループ.
						while (inetAddresses.hasMoreElements()) {
							InetAddress in = inetAddresses.nextElement();
							String hostAddress = in.getHostAddress();
							if (addressListDouble.contains(hostAddress)) {
								// NIFのアドレスセットしたら次のNIF.
								addressList.add(hostAddress);
								break;
							}
						}
					}
				}
			} catch (SocketException e) {
				log.error(e, e);
			}
			return addressList;
		}

		/**
		 * スレッド作成・スレッドに紐づく監視設定の更新.
		 * 
		 * @param スレッド紐付け用の監視設定等
		 */
		private void createThread(List<MonitorInfoWrapper> wholeFileMonList,
				Map<String, PacketListenerImpl> pcapListenerMap, Map<String, MonitorInfoWrapper> incrementMonMap) {
			// パケットキャプチャ全体監視のスレッド作成.
			if (pcapListenerMap == null || pcapListenerMap.isEmpty()) {
				if (pcapThread != null) {
					// リスト生成されていない場合は削除された監視設定なのでスレッド停止.
					pcapThread.terminate();
					pcapThread = null;
					log.debug(PacketCaptureThread.threadName + "is terminated because monitor setting is deleted.");
				}
			} else {
				if (pcapThread == null) {
					pcapThread = new PacketCaptureThread(pcapListenerMap);
					pcapThread.setName(PacketCaptureThread.threadName);
					pcapThread.start();
					log.info("start() : " + PacketCaptureThread.threadName + " is started.");
				} else {
					// 次回スレッド実行時に新しい監視設定を適用するようにセット.
					pcapThread.addListnerMap(pcapListenerMap);
					log.debug(PacketCaptureThread.threadName + " is refreshed monitor list.");
				}
			}

			// ファイル増分監視のスレッド作成.
			if (incrementMonMap.isEmpty()) {
				if (incrementMonitorThread != null) {
					// リスト生成されていない場合は削除された監視設定なのでスレッド停止.
					incrementMonitorThread.deleteRootRsDir();
					incrementMonitorThread.terminate();
					incrementMonitorThread = null;
					log.debug(IncrementMonitorThread.threadName + " is terminated because monitor setting is deleted.");
				}
			} else {
				if (incrementMonitorThread == null) {
					incrementMonitorThread = new IncrementMonitorThread(incrementMonMap);
					incrementMonitorThread.setName(IncrementMonitorThread.threadName);
					incrementMonitorThread.start();
					log.info("start() : " + IncrementMonitorThread.threadName + " is started.");
				} else {
					// 新しい監視設定を追加.
					incrementMonitorThread.addMonitorMap(incrementMonMap);
					log.debug(IncrementMonitorThread.threadName + " is added monitor list for pcap.");
				}
			}

			// ファイル全体監視のスレッド作成.
			if (wholeFileMonList == null || wholeFileMonList.isEmpty()) {
				if (wholeFileMonitorThread != null) {
					// リスト生成されていない場合は削除された監視設定なのでスレッド停止.
					wholeFileMonitorThread.deleteRootRsDir();
					wholeFileMonitorThread.terminate();
					wholeFileMonitorThread = null;
					log.debug(WholeFileMonitorThread.threadName + " is terminated because monitor setting is deleted.");
				}
			} else {
				if (wholeFileMonitorThread == null) {
					wholeFileMonitorThread = new WholeFileMonitorThread(wholeFileMonList);
					wholeFileMonitorThread.setName(WholeFileMonitorThread.threadName);
					wholeFileMonitorThread.start();
					log.info("start() : BinaryEventMonThread is started.");
				} else {
					// 次回スレッド実行時に新しい監視設定を適用するようにセット.
					wholeFileMonitorThread.setRefreshMonInfoList(wholeFileMonList);
					log.debug(WholeFileMonitorThread.threadName + " is refreshed monitor list.");
				}
			}
		}

		/**
		 * 個別生成している不要なスレッドや監視設定削除.
		 * 
		 * @param 最新監視設定の全量.
		 */
		private void removeThreadMap(List<MonitorInfoWrapper> newMonList) {

			// 取得したnewMonListに監視設定が含まれていない場合は削除された設定なので除去する.
			boolean toDelete = true;

			// 時間区切り監視スレッド.
			if (everyIntMonThreadMap != null && !everyIntMonThreadMap.isEmpty()) {
				Iterator<Entry<String, EveryIntervalMonitorThread>> everyIntMonThreadMapIt = everyIntMonThreadMap
						.entrySet().iterator();
				while (everyIntMonThreadMapIt.hasNext()) {
					toDelete = true;
					Entry<String, EveryIntervalMonitorThread> entry = everyIntMonThreadMapIt.next();
					for (MonitorInfoWrapper moninfo : newMonList) {
						if (entry.getKey().equals(moninfo.getId())) {
							// 取得した監視設定に含まれてる.
							if (moninfo.monitorInfo.getCollectorFlg().booleanValue()
									|| moninfo.monitorInfo.getMonitorFlg().booleanValue()
									|| moninfo.runInstructionInfo != null) {
								// 有効なら問題なし.
								toDelete = false;
								break;
							} else {
								// 無効なら削除.
								toDelete = true;
								break;
							}
						}
					}
					if (toDelete) {
						entry.getValue().deleteRootRsDir();
						entry.getValue().terminate();
						everyIntMonThreadMapIt.remove();
						log.info(String.format(EveryIntervalMonitorThread.threadName + " for [%s] is removed.",
								entry.getKey()));
					}
				}
				everyIntMonThreadMapIt = null;
			}
		}

		/**
		 * メインループが待機状態にある場合、その待機状態を解除して、処理を実行させます。
		 */
		public void wakeUp() {
			// キューがいっぱいになっていても構わない(空でなくすればよい)
			// findbus対応 戻り値をチェックしてログを出力とした。
			boolean ret = signalQueue.offer(WAKE_UP_SIGNAL);
			if(!ret){
				log.debug("wakeUp: signalQueue.offer is failed.");
			}
			log.debug("wakeUp: Sent a signal.");
		}

		/**
		 * スレッド停止.
		 */
		public void terminate() {
			// メインループを抜けるようにフラグをセット
			loop = false;
			// 待機状態を解除する
			wakeUp();
		}

		/**
		 * スレッドクローズ(スレッド紐づきオブジェクト削除・ログ出力).
		 */
		private void close() {
			// 子スレッドの停止.
			if (everyIntMonThreadMap != null && (!everyIntMonThreadMap.isEmpty())) {
				// 時間区切り監視スレッドの停止.
				for (EveryIntervalMonitorThread childThread : everyIntMonThreadMap.values()) {
					childThread.terminate();
				}
				everyIntMonThreadMap.clear();
				everyIntMonThreadMap = null;
				log.debug(
						EveryIntervalMonitorThread.threadName + " is terminated because parent thread is terminated.");
			}

			if (pcapThread != null) {
				// パケットキャプチャ用スレッドの停止.
				pcapThread.terminate();
				pcapThread = null;
				log.debug(PacketCaptureThread.threadName + " is terminated because parent thread is terminated.");
			}

			if (wholeFileMonitorThread != null) {
				// ファイル全体監視スレッドの停止.
				wholeFileMonitorThread.terminate();
				wholeFileMonitorThread = null;
				log.debug(WholeFileMonitorThread.threadName + " is terminated because parent thread is terminated.");
			}

			log.info("terminate() : " + parentThreadName + " is terminated.");
		}

	}

	/**
	 * 読込状態の更新処理.<br>
	 * <br>
	 * 最新の監視設定を元に読込状態を更新する.<br>
	 * 
	 * @param monitorList
	 *			  監視設定のリスト.
	 * 
	 */
	public static void refreshReadingStatus(String threadID, List<MonitorInfoWrapper> monList, int runInterval,
			Collection<MonitorInfoWrapper> deleteMonList) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");

		RootReadingStatus rootReadingStatus = rootRSMap.get(threadID);

		if (monList != null) {
			if (rootReadingStatus == null) {
				// 他の監視設定含め初回読込の場合.
				rootReadingStatus = new RootReadingStatus(monList, runInterval);
				rootRSMap.put(threadID, rootReadingStatus);
				log.debug(methodName + DELIMITER + "RootReadingStatus is initialized on first reading.");
			} else {
				// 前回thread実行時の読込み状態が存在する場合.
				rootReadingStatus.init(monList, deleteMonList);
				log.debug(methodName + DELIMITER + "RootReadingStatus is initialized on and after second reading.");
			}
		} else {
			if (rootReadingStatus != null) {
				// 監視設定は前回thread実行時と変わらないためファイル読込み状態のみ更新.
				Map<String, MonitorReadingStatus> monitorRSMap = rootReadingStatus.getMonitorRSMap();
				for (MonitorReadingStatus monRS : monitorRSMap.values()) {
					monRS.initMonitorRS();
					monRS.initDirectoryRS();
				}
				log.debug(methodName + DELIMITER + "MonitorReadingStatus and others are initialized.");
			} else {
				// 監視設定の更新なし.
				log.debug(methodName + DELIMITER + "RootReadingStatus is not initialized.");
			}
		}
	}

	/**
	 * 監視用オブジェクトの更新処理.<br>
	 * <br>
	 * 最新の読込状態を元に監視対象ファイル毎で監視用オブジェクトを生成.<br>
	 * 
	 */
	public static void refreshMonitorObject(String threadID) {

		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// スレッドに紐づく情報取得.
		RootReadingStatus rootReadingStatus = rootRSMap.get(threadID);

		// 監視設定用オブジェクトをつめるMapの初期化
		Map<String, BinaryMonitor> binaryMonitorCache = null;
		binaryMonitorCache = BinaryMonitorManager.binMonCacheMap.get(threadID);
		if (binaryMonitorCache == null) {
			// スレッドで初回の場合の初期化.
			binaryMonitorCache = new TreeMap<String, BinaryMonitor>();
			BinaryMonitorManager.binMonCacheMap.put(threadID, binaryMonitorCache);
		}

		// RSが生成されてない場合は監視設定がない状態なので処理飛ばす.
		if (rootReadingStatus == null) {
			return;
		}

		// 監視対象ファイルのキーを格納するセット.
		Set<String> newBinaryMonitorCacheKeySet = new HashSet<String>();

		// 監視設定毎にログ出力.
		for (MonitorReadingStatus miDir : rootReadingStatus.getMonitorRSMap().values()) {
			MonitorInfoWrapper monInfoWrapper = miDir.getMonInfoWrapper();

			// ログ出力.
			String monitorId = monInfoWrapper.monitorInfo.getMonitorId();
			String monitorTypeId = monInfoWrapper.monitorInfo.getMonitorTypeId();
			String collectType = monInfoWrapper.monitorInfo.getBinaryCheckInfo().getCollectType();
			String cutType = monInfoWrapper.monitorInfo.getBinaryCheckInfo().getCutType();
			String directoryPath = monInfoWrapper.monitorInfo.getBinaryCheckInfo().getDirectory();
			String fileNamePattern = monInfoWrapper.monitorInfo.getBinaryCheckInfo().getFileName();
			log.debug(methodName + DELIMITER + "monitorId=" + monitorId + ", monitorTypeId=" + monitorTypeId
					+ ", collectType=" + collectType + ", cutType=" + cutType + ", directory=" + directoryPath
					+ ", filenamePattern=" + fileNamePattern);

			// 監視対象ディレクトリ内の監視対象ファイル毎に監視用オブジェクトを生成.
			for (DirectoryReadingStatus dirRS : miDir.getDirectoryRSMap().values()) {
				if (dirRS.isReadingFlag()) {
					// 監視対象ディレクトリに読込中ファイルが存在する場合.
					String cacheKey = null;
					BinaryMonitor binaryMonitor = null;
					for (FileReadingStatus fileRS : dirRS.getFileRSMap().values()) {
						log.debug(methodName + DELIMITER + "filePath=" + fileRS.getMonFileName());
						if (fileRS.isReadingFlag()) {
							// 監視対象として読込中の場合.
							if (BinaryConstant.COLLECT_TYPE_WHOLE_FILE.equals(collectType)) {
								if (!fileRS.isRunWholeFileMonitor()) {
									// 書込み中ファイル等は監視対象外とするのでskip.
									log.debug(methodName + DELIMITER
											+ String.format(
													"skipped monitor for [%s], because it may be under writing.",
													fileRS.getMonFileName()));
									continue;
								}
								// ファイル全体監視は監視時にキー使って取り出すので下記の名前.
								cacheKey = monitorId + fileRS.getMonFileName();
							} else {
								// ファイル増分監視は最終更新日時の順で監視処理を実行するため、キー名下記でtreeMap.
								cacheKey = monitorId + fileRS.getMonFileLastModTimeStamp() + fileRS.getMonFileName();
							}
							binaryMonitor = binaryMonitorCache.get(cacheKey);
							if (binaryMonitor == null) {
								// 前回thread実行時に生成したキャッシュがない場合はファイル監視オブジェクトを生成.
								binaryMonitor = new BinaryMonitor(monInfoWrapper, fileRS);
								// 生成したオブジェクトをキャッシュに保存.
								binaryMonitorCache.put(cacheKey, binaryMonitor);
								log.debug(methodName + DELIMITER
										+ String.format("BinaryMonitor Object is created. key=[%s]", cacheKey));
							} else {
								// キャッシュが存在する場合は監視設定のみ更新.
								binaryMonitor.setMonitor(monInfoWrapper);
								binaryMonitorCache.put(cacheKey, binaryMonitor);
								log.debug(methodName + DELIMITER
										+ String.format("BinaryMonitor Object is refreshed. key=[%s]", cacheKey));
							}
							// キャッシュに監視対象として保持するキーセットに追加.
							newBinaryMonitorCacheKeySet.add(cacheKey);
						} else {
							// 監視対象外の場合はログ出力のみ.
							log.debug(methodName + DELIMITER + "[" + fileRS.getMonFileName() + "] is closed.");
						}
					}
				} else {
					// ディレクトリに読込中ファイルが存在しない場合はログ出力のみ.
					log.debug(methodName + DELIMITER + "[" + dirRS.getMonDirName() + "] has no file to monitor.");
				}
			}
		}

		// 生成したオブジェクトを設定.
		BinaryMonitorManager.binMonCacheMap.put(threadID, binaryMonitorCache);

		// もう監視対象じゃないファイルをクローズしてから削除.
		Iterator<Entry<String, BinaryMonitor>> it = binaryMonitorCache.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, BinaryMonitor> entry = it.next();
			if (!newBinaryMonitorCacheKeySet.contains(entry.getKey())) {
				entry.getValue().closeFileChannel();
				it.remove();
			}
		}
	}

	/**
	 * ローテーションチェック.<br>
	 * <br>
	 * 生成した監視対象オブジェクトがローテーションで生成されたファイルに紐づくものかチェック<br>
	 * 
	 * @throws IOException
	 * 
	 */
	public static void checkRotatedObject(Map<String, BinaryMonitor> monitorObjectMap) throws IOException {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");

		boolean rotated = false;
		Map<String, FileReadingStatus> rotatedRsMap = null;
		BinaryMonitor skipBinaryMonitor = null;

		for (BinaryMonitor binaryMonitor : monitorObjectMap.values()) {
			log.debug(
					methodName + DELIMITER
							+ String.format("prepared to check rotated before monitor. monitorId=%s, monitorFile=[%s]",
									binaryMonitor.getM_wrapper().getId(),
									binaryMonitor.getReadingStatus().getMonFileName()));
			// ローテーションチェック.
			if (binaryMonitor.callRotatedCheck()) {
				// 前回チェック時点から一定時間(AgentProperty)経過している場合はファイル変更チェック.
				log.debug(methodName + DELIMITER + String.format("call rotated check. monitorId=%s, monitorFile=[%s]",
						binaryMonitor.getM_wrapper().getId(), binaryMonitor.getReadingStatus().getMonFileName()));
				rotated = binaryMonitor.checkPrefix();
			} else {
				// チェック対象でない場合はskip.
				log.debug(methodName + DELIMITER + String.format(
						"skip to set flag to skip monitor because of call time. monitorId=%s, monitorFile=[%s]",
						binaryMonitor.getM_wrapper().getId(), binaryMonitor.getReadingStatus().getMonFileName()));
				continue;
			}

			if (!rotated) {
				continue;
			}
			// ローテーションされている場合は、ローテーションまでに生成されたファイルのRS更新.
			binaryMonitor.rotate();

			// ローテーションファイルRS取得.
			rotatedRsMap = binaryMonitor.getRotatedRSMap();
			if (rotatedRsMap == null || rotatedRsMap.isEmpty()) {
				log.debug(methodName + DELIMITER + String.format(
						"skip to set flag to skip monitor because rotatedRsMap is empty. monitorId=%s, monitorFile=[%s]",
						binaryMonitor.getM_wrapper().getId(), binaryMonitor.getReadingStatus().getMonFileName()));
				continue;
			}

			// 生成されたローテーションファイルRSに紐づく監視オブジェクトの監視処理スキップ.
			for (String key : rotatedRsMap.keySet()) {
				skipBinaryMonitor = monitorObjectMap.get(key);
				if (skipBinaryMonitor == null) {
					log.debug(methodName + DELIMITER + String.format(
							"skip to set flag to skip monitor. key=[%s], monitorId=%s, monitorFile=[%s]", key,
							binaryMonitor.getM_wrapper().getId(), binaryMonitor.getReadingStatus().getMonFileName()));
					continue;
				}
				skipBinaryMonitor.setSkipForRotate(true);
				log.debug(methodName + DELIMITER
						+ String.format("set flag to skip monitor. key=[%s], monitorId=%s, monitorFile=[%s]", key,
								binaryMonitor.getM_wrapper().getId(),
								binaryMonitor.getReadingStatus().getMonFileName()));
			}
		}
	}

	/**
	 * 監視設定のスレッド反映.<br>
	 * <br>
	 * マネージャーから取得した監視設定をスレッド内に保持.
	 * 
	 * @param monitorList
	 *			  監視設定一覧からの設定情報
	 * @param monitorMap
	 *			  ジョブ設定一覧(監視ジョブ)からの設定情報
	 */
	public static void pushMonitorInfoList(List<AgtMonitorInfoResponse> monList,
			Map<AgtRunInstructionInfoResponse, AgtMonitorInfoResponse> monitorMap) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");

		synchronized (monitorListLock) {
			List<MonitorInfoWrapper> wrapperList = new ArrayList<MonitorInfoWrapper>();

			// 監視設定一覧.
			StringBuilder fileNameSb = new StringBuilder();
			for (AgtMonitorInfoResponse info : monList) {

				AgtBinaryCheckInfoResponse check = info.getBinaryCheckInfo();
				String monitorTypeId = info.getMonitorTypeId();

				// 返却用監視設定リストに追加.
				wrapperList.add(new MonitorInfoWrapper(info, null));

				if (HinemosModuleConstant.MONITOR_BINARYFILE_BIN.equals(monitorTypeId)) {
					// ログ出力用のファイル名に追加.
					fileNameSb.append("[" + check.getDirectory() + "," + check.getFileName() + "]");
					// 監視設定の対象ディレクトリ存在チェック.
					File directory = new File(check.getDirectory());
					log.debug("setBinaryMonitor() : directoryExistsMap put monitorId = " + check.getMonitorId()
							+ ", directoryStr = " + check.getDirectory() + ", exists = " + directory.isDirectory());
				} else {
					// ログ出力用のファイル名に追加.
					fileNameSb.append("[(packetCapture)monitorId=" + info.getMonitorId() + "]");
				}
			}
			log.info("setBinaryMonitor() : m_monitorList=" + fileNameSb.toString());

			// ジョブ設定一覧.
			for (Map.Entry<AgtRunInstructionInfoResponse, AgtMonitorInfoResponse> entry : monitorMap.entrySet()) {
				AgtBinaryCheckInfoResponse check = entry.getValue().getBinaryCheckInfo();
				String monitorTypeId = entry.getValue().getMonitorTypeId();

				wrapperList.add(new MonitorInfoWrapper(entry.getValue(), entry.getKey()));
				if (HinemosModuleConstant.MONITOR_BINARYFILE_BIN.equals(monitorTypeId)) {
					// ログ出力用のファイル名に追加.
					fileNameSb.append("[" + check.getDirectory() + "," + check.getFileName() + "]");
					// 監視設定の対象ディレクトリ存在チェック
					File directory = new File(check.getDirectory());
					log.debug("setMonitorInfoListForMonitorJob() : directoryExistsMap put monitorId = "
							+ check.getMonitorId() + ", directoryStr = " + check.getDirectory() + ", exists = "
							+ directory.isDirectory());
				} else {
					// ログ出力用のファイル名に追加.
					fileNameSb.append("[(packetCapture)job id=" + entry.getKey().getJobId() + "]");
				}

			}
			log.info("setBinaryMonitor() : m_monitorList=" + fileNameSb.toString());
			BinaryMonitorManager.monitorList = wrapperList;
		}
		// ファイル監視を行っているスレッドへ設定を反映させる処理を起動する
		synchronized (BinaryMonitorManager.class) {
			if (binaryThread != null) {
				binaryThread.wakeUp();
			}
		}
	}

	/**
	 * 監視設定の取得.<br>
	 * <br>
	 * monitorListはマネージャーからの指示で定期的に読込む.<br>
	 * ※pushMonitorInfoListメソッドをReceiveTopicクラスでTopic受信時に呼び出してる.
	 * 
	 * @return 読込済の監視設定リスト
	 */
	private static List<MonitorInfoWrapper> popMonitorInfoList() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");

		synchronized (monitorListLock) {
			// クラス生成時もしくはpushされたmonitorList.
			List<MonitorInfoWrapper> list = BinaryMonitorManager.monitorList;

			if (list == null) {
				log.debug(methodName + DELIMITER + "get empty list.");
				return null;
			} else {
				log.debug(methodName + DELIMITER + String.format("get monitor list. size=%d", list.size()));
			}

			// 取り出した分クラスで保持している監視設定を減らす.
			BinaryMonitorManager.monitorList = null;

			return list;
		}
	}

	/**
	 * Hinemosマネージャへ情報を通知します。<BR>
	 */
	public static void sendMessage(int priority, String app, String msg, String msgOrg, String monitorId,
			AgtRunInstructionInfoRequest runInstructionInfo, String monitorType) {
		// ログ出力情報
		MessageSendableObject sendme = new MessageSendableObject();
		sendme.body = new AgtOutputBasicInfoRequest();
		sendme.body.setPluginId(monitorType);
		sendme.body.setPriority(priority);
		sendme.body.setApplication(app);
		sendme.body.setMessage(msg);
		sendme.body.setMessageOrg(msgOrg);
		sendme.body.setGenerationDate(HinemosTime.getDateInstance().getTime());
		sendme.body.setMonitorId(monitorId);
		sendme.body.setFacilityId(""); // マネージャがセット.
		sendme.body.setScopeText(""); // マネージャがセット.
		sendme.body.setRunInstructionInfo(runInstructionInfo);

		sendQueue.put(sendme);
	}

	/**
	 * ファイルのクローズ処理.<br>
	 * <br>
	 * スレッドをクローズする際、監視用オブジェクトを削除する前に全てのファイルをクローズする<br>
	 * 
	 */
	public static void closeFileChannels(String threadID) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");
		
		Map<String, BinaryMonitor> binaryMonitorCache = BinaryMonitorManager.binMonCacheMap.get(threadID);
		if (binaryMonitorCache == null) {
			log.debug(methodName + DELIMITER + "skip to close File Channels. threadID=" + threadID);
			return;
		}
		
		// スレッドに紐づくファイルチャンネルを全てクローズしてから削除.
		Iterator<Entry<String, BinaryMonitor>> it = binaryMonitorCache.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, BinaryMonitor> entry = it.next();
			entry.getValue().closeFileChannel();
			it.remove();
		}
	}
}
