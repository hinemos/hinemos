/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.session;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.binary.bean.BinaryConstant;
import com.clustercontrol.binary.bean.BinaryQueryInfo;
import com.clustercontrol.binary.bean.BinaryResultDTO;
import com.clustercontrol.binary.factory.DownloadBinary;
import com.clustercontrol.binary.factory.RunMonitorBinary;
import com.clustercontrol.binary.model.BinaryCheckInfo;
import com.clustercontrol.calendar.model.CalendarInfo;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.collect.util.ZipCompressor;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.BinaryRecordNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.hub.model.CollectStringDataPK;
import com.clustercontrol.jobmanagement.bean.MonitorJobEndNode;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.logfile.session.MonitorLogfileControllerBean;
import com.clustercontrol.monitor.run.factory.SelectMonitor;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.util.MonitorOnAgentUtil;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.util.NotifyCallback;
import com.clustercontrol.platform.HinemosPropertyDefault;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.rest.util.RestDownloadFile;
import com.clustercontrol.rest.util.RestTempFileType;
import com.clustercontrol.rest.util.RestTempFileUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * バイナリファイル監視・収集機能の管理を行う Session Bean <BR>
 * クライアントからの Entity Bean へのアクセスは、Session Bean を介して行う.
 * 
 * @version 6.1.0
 * @since 6.1.0
 * 
 */
public class BinaryControllerBean {

	/** ログ出力用インスタンス */
	private static Log m_log = LogFactory.getLog(BinaryControllerBean.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	/** クラス全体ロック */
	private static final ILock _lock;

	// プリセット用プロパティ項目名.
	/** プリセット表示名(タグ種別) */
	private static final String PRESET_NAME = "preset.name";
	/** ファイルヘッダサイズ */
	private static final String PRESET_FILE_HEAD_SIZE = "file.head.size";
	/** レコード長 */
	private static final String PRESET_LENGTH_TYPE = "length.type";
	/** レコードサイズ(固定長レコード) */
	private static final String PRESET_FIXED_RECORD_SIZE = "fixed.record.size";
	/** レコードヘッダサイズ(可変長レコード) */
	private static final String PRESET_VARIABLE_RECORD_HEAD_SIZE = "variable.record.head.size";
	/** レコードサイズ位置(可変長レコード) */
	private static final String PRESET_VARIABLE_RECORD_SIZE_POSITION = "variable.record.size.position";
	/** レコードサイズ表現バイト長(可変長レコード) */
	private static final String PRESET_VARIABLE_RECORD_SIZE_LENGTH = "variable.record.size.length";
	/** タイムスタンプ */
	private static final String PRESET_HAVE_TIMESTAMP = "have.timestamp";
	/** タイムスタンプ位置 */
	private static final String PRESET_TIMESTAMP_POSITION = "timestamp.position";
	/** タイムスタンプ種類 */
	private static final String PRESET_TIMESTAMP_TYPE = "timestamp.type";
	/** リトルエンディアン方式 */
	private static final String PRESET_LITTLE_ENDIAN = "little.endian";

	/** プリセットファイルのメッセージ */
	private static final String PRESET_MESSAGE = "the property [%s] isn't exist. Please set on data structure dialog. file=[%s].";
	/** プリセットレコード長の設定値の接頭後 */
	private static final String PRESET_LENGTH_TYPE_PREFIX = "binary." + PRESET_LENGTH_TYPE + ".";
	/** プリセットタイムスタンプ種類の設定値の接頭後 */
	private static final String PRESET_TS_TYPE_PREFIX = "timestamp.";

	// クラス初期化処理.
	static {
		// クラス全体ロックの初期化.
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(MonitorLogfileControllerBean.class.getName());

		try {
			_lock.writeLock();

			ArrayList<MonitorInfo> cache = getCache();
			if (cache == null) { // not null when clustered
				refreshCache();
			}
		} finally {
			_lock.writeUnlock();
			m_log.info("Static Initialization [Thread : " + Thread.currentThread() + ", User : "
					+ (String) HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID) + "]");
		}
	}

	/**
	 * 
	 * <注意！> このメソッドはAgentユーザ以外で呼び出さないこと！
	 * <注意！> キャッシュの都合上、Agentユーザ以外から呼び出すと、正常に動作しません。
	 * 
	 * facilityIDごとのバイナリ監視一覧リストを返します。
	 * withCalendarをtrueにするとMonitorInfoのcalendarDTOに値が入ります。
	 * 
	 * 
	 * @return Objectの2次元配列
	 * @throws HinemosUnknown
	 * @throws MonitorNotFound
	 * 
	 */
	public ArrayList<MonitorInfo> getBinaryListForFacilityId(String facilityId, boolean withCalendar)
			throws MonitorNotFound, HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		ArrayList<MonitorInfo> ret = new ArrayList<MonitorInfo>();
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
			// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
			ArrayList<MonitorInfo> monitorList = getCache();

			for (MonitorInfo monitorInfo : monitorList) {
				String scope = monitorInfo.getFacilityId();

				if (new RepositoryControllerBean().containsFacilityIdWithoutList(scope, facilityId, monitorInfo.getOwnerRoleId())) {
					if (withCalendar) {
						String calendarId = monitorInfo.getCalendarId();
						try {
							CalendarInfo calendar = new CalendarControllerBean().getCalendarFull(calendarId);
							monitorInfo.setCalendar(calendar);
						} catch (Exception e) {
							m_log.warn(methodName + DELIMITER + e.getClass().getSimpleName() + ", " + e.getMessage(),
									e);
							throw new HinemosUnknown(e.getMessage(), e);
						}
					}
					ret.add(monitorInfo);
					m_log.debug(methodName + DELIMITER
							+ String.format(
									"added binary monitor list for agent."
											+ " monitorId=%s, facilityId(Agent)=%s, scope(Monitor)=%s",
									monitorInfo.getMonitorId(), facilityId, scope));

				} else {
					m_log.debug(methodName + DELIMITER
							+ String.format(
									"skip to add binary monitor list for agent."
											+ " monitorId=%s, facilityId(Agent)=%s, scope(Monitor)=%s",
									monitorInfo.getMonitorId(), facilityId, scope));
				}
			}

			jtm.commit();
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn(methodName + DELIMITER + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;
	}

	/**
	 * バイナリ用のキャッシュ取得.
	 */
	@SuppressWarnings("unchecked")
	private static ArrayList<MonitorInfo> getCache() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_BINARY);
		if (m_log.isDebugEnabled())
			m_log.debug(methodName + DELIMITER + "get cache " + AbstractCacheManager.KEY_BINARY + " : " + cache);
		return cache == null ? null : (ArrayList<MonitorInfo>) cache;
	}

	/**
	 * バイナリ用のキャッシュ更新.
	 */
	public static void refreshCache() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");

		long startTime = HinemosTime.currentTimeMillis();
		try {
			_lock.writeLock();

			try (JpaTransactionManager jtm = new JpaTransactionManager()) {
				HinemosEntityManager em = jtm.getEntityManager();
				em.clear();
				ArrayList<MonitorInfo> binaryCache = new BinaryControllerBean().getBinaryList();
				storeCache(binaryCache);
				m_log.info(methodName + DELIMITER + (HinemosTime.currentTimeMillis() - startTime) + "ms. size="
						+ binaryCache.size());
			}
		} catch (Exception e) {
			m_log.warn(methodName + DELIMITER + "failed refreshing binaryCache.", e);
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * バイナリ用のキャッシュ保存.
	 */
	private static void storeCache(ArrayList<MonitorInfo> newCache) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled())
			m_log.debug(methodName + DELIMITER + "store cache " + AbstractCacheManager.KEY_BINARY + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_BINARY, newCache);
	}

	/**
	 * バイナリ監視一覧リストを返します。
	 * 
	 * 
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getBinaryList() throws MonitorNotFound, InvalidRole, HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");

		JpaTransactionManager jtm = null;
		ArrayList<MonitorInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectMonitor selectMonitor = new SelectMonitor();
			ArrayList<MonitorInfo> binaryFileList = selectMonitor
					.getMonitorListObjectPrivilegeModeNONE(HinemosModuleConstant.MONITOR_BINARYFILE_BIN);
			ArrayList<MonitorInfo> pcapList = selectMonitor
					.getMonitorListObjectPrivilegeModeNONE(HinemosModuleConstant.MONITOR_PCAP_BIN);
			list = new ArrayList<MonitorInfo>(binaryFileList);
			list.addAll(pcapList);
			jtm.commit();
			m_log.debug(methodName + DELIMITER + "binaryFileList size = " + binaryFileList.size() + ", pcapList size = "
					+ pcapList.size());
		} catch (Exception e) {
			m_log.warn(methodName + DELIMITER + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**
	 * バイナリファイル監視結果を収集・通知する.
	 * 
	 * @param results
	 *            バイナリファイル監視結果のリスト
	 * @throws HinemosUnknown
	 */
	public void run(String facilityId, List<BinaryResultDTO> results) throws HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		JpaTransactionManager jtm = null;
		List<OutputBasicInfo> notifyInfoList = new ArrayList<>();
		List<MonitorJobEndNode> monitorJobEndNodeList = new ArrayList<>();

		// 性能測定用に受信時刻をこの時点として取得.
		long receiveMsec = HinemosTime.getDateInstance().getTime();
		StringBuilder monitorIdSb = new StringBuilder();
		StringBuilder filenameSb = new StringBuilder();

		try {
			// トランザクション開始.
			jtm = new JpaTransactionManager();
			jtm.begin();

			if (results != null) {
				RunMonitorBinary runMonitor = null;
				String id = null;

				// 監視結果毎に収集・通知等のDB登録処理.
				for (BinaryResultDTO result : results) {

					// 性能測定用に転送にかかった時間を出力(デバッガモード).
					id = MonitorOnAgentUtil.getId(result.runInstructionInfo, result.monitorInfo);
					if (m_log.isDebugEnabled()) {
						long transferMsec = receiveMsec - result.msgInfo.getGenerationDate().longValue();
						m_log.debug(methodName + DELIMITER + String.format(
								"received result of monitor binary from agent."
										+ " transfer time=%dmsec, facilityId=%s, settingId=%s, fileName=[%s]",
								transferMsec, facilityId, id, result.monitorInfo.getBinaryCheckInfo().getBinaryfile()));
						monitorIdSb.append("[" + id + "]");
						filenameSb.append("[" + result.monitorInfo.getBinaryCheckInfo().getBinaryfile() + "]");
					}

					// 監視設定に紐づくFacilityIDかチェック.
					boolean associatedFacilityId = MonitorOnAgentUtil.checkFacilityId(facilityId,
							result.runInstructionInfo, result.monitorInfo);
					if (!associatedFacilityId) {
						m_log.debug(methodName + DELIMITER
								+ String.format(
										"skip to run monitor because facility id isn't associated with settings."
												+ "facilityId=%s, settingId=%s",
										facilityId, id));
						continue;
					}

					// 処理実行.
					runMonitor = new RunMonitorBinary();
					// 収集処理.
					runMonitor.runCollect(facilityId, result);
					// 通知情報取得.
					notifyInfoList.addAll(runMonitor.runAlert(facilityId, result));
					monitorJobEndNodeList.addAll(runMonitor.getMonitorJobEndNodeList());
				}
			}

			// 通知設定
			jtm.addCallback(new NotifyCallback(notifyInfoList));

			jtm.commit();
		} catch (HinemosUnknown e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn(methodName + DELIMITER + "failed storeing result.", e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} finally {
			if (jtm != null)
				// トランザクション終了.
				jtm.close(this.getClass().getName());
		}

		// 監視ジョブEndNode処理.
		try {
			if (monitorJobEndNodeList != null && monitorJobEndNodeList.size() > 0) {
				/* 
				 * 監視ジョブの対象はMonitorJobWorker.monitorJobMapに含まれるもの。
				 * また、MonitorJobWorker.endMonitorJob()では、MonitorJobWorker.monitorJobMapから
				 * 対象を削除しているため、
				 * MonitorJobWorker.monitorJobMap()のキーであるMonitorJobWorker.getKey()が
				 * 一致する情報は複数回実行する必要はない。
				 */
				HashMap<String, MonitorJobEndNode> endMonitorJobEndNodes = new HashMap<>();
				for (MonitorJobEndNode monitorJobEndNode : monitorJobEndNodeList) {
					String monitorJobKey = MonitorJobWorker.getKey(monitorJobEndNode.getRunInstructionInfo());
					m_log.trace("run() : endMonitorJob before check=" + monitorJobKey);
					if (!endMonitorJobEndNodes.containsKey(monitorJobKey)) {
						m_log.debug("run() : endMonitorJob target=" + monitorJobKey);
						endMonitorJobEndNodes.put(monitorJobKey, monitorJobEndNode);
					}
				}
				for (Map.Entry<String, MonitorJobEndNode> entry : endMonitorJobEndNodes.entrySet()) {
					MonitorJobWorker.endMonitorJob(
							entry.getValue().getRunInstructionInfo(),
							entry.getValue().getMonitorTypeId(),
							entry.getValue().getMessage(),
							entry.getValue().getErrorMessage(),
							entry.getValue().getStatus(),
							entry.getValue().getEndValue());
				}
			}
		} catch (Exception e) {
			m_log.warn(methodName + DELIMITER + "MonitorJobWorker.endMonitorJob() : " + e.getClass().getSimpleName()
					+ ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		// 性能測定用にManager処理時間を出力.
		long processMsec = HinemosTime.getDateInstance().getTime() - receiveMsec;
		m_log.debug(methodName + DELIMITER
				+ String.format(
						"received result of monitor binary after received."
								+ " process time=%dmsec, facilityId=%s, settingId=%s, fileName=[%s]",
						processMsec, facilityId, monitorIdSb.toString(), filenameSb.toString()));
	}

	/**
	 * バイナリファイルの収集済レコードをファイルとしてダウンロード.
	 * 
	 * @param recordTime
	 *            収集蓄積の表示時刻.
	 * @param recordKey
	 *            ダウンロード対象のレコードキー(内部変数でクライアント表示なし)
	 * 
	 * @return 添付ファイルとして取得したバイナリレコード
	 * @throws HinemosDbTimeout
	 * @throws BinaryRecordNotFound
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	public RestDownloadFile downloadBinaryRecord(BinaryQueryInfo queryInfo, CollectStringDataPK primaryKey, String filename,
			String tempDir) throws HinemosDbTimeout, BinaryRecordNotFound, InvalidSetting, HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		long beforeMsec = HinemosTime.getDateInstance().getTime();
		long processMsec = 0;
		m_log.debug(methodName + DELIMITER + "start. time=" + beforeMsec);

		// 入力チェック
		if (primaryKey == null) {
			InvalidSetting e = new InvalidSetting(methodName + DELIMITER + "primaryKey is not defined.");
			m_log.info(methodName + DELIMITER + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		JpaTransactionManager jtm = null;
		DownloadBinary download = new DownloadBinary(queryInfo);
		try {
			// トランザクション制御開始.
			jtm = new JpaTransactionManager();
			jtm.begin();

			// レコードデータを取得する.
			download.getOneRecord(primaryKey);
			if (m_log.isDebugEnabled()) {
				processMsec = HinemosTime.getDateInstance().getTime() - beforeMsec;
				beforeMsec = HinemosTime.getDateInstance().getTime();
				m_log.debug(
						methodName + DELIMITER + String.format("getOneRecord. processing time=%dmsec", processMsec));
			}
			if (BinaryConstant.COLLECT_TYPE_WHOLE_FILE.equals(download.getCollectType())) {
				download.getDataIds();
			}
			
			download.createTemporaryFile(filename, tempDir);
			// コミット処理.
			jtm.commit();
		} catch (HinemosDbTimeout e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosDbTimeout(MessageConstant.MESSAGE_HUB_SEARCH_TIMEOUT.getMessage());
		} catch (BinaryRecordNotFound | HinemosUnknown e) {
			if (jtm != null)
				jtm.rollback();
			throw e;
		} catch (RuntimeException e) {
			m_log.warn(methodName + DELIMITER + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				// トランザクション境界ここまで.
				jtm.close();
			}
		}

		if (m_log.isDebugEnabled()) {
			processMsec = HinemosTime.getDateInstance().getTime() - beforeMsec;
			beforeMsec = HinemosTime.getDateInstance().getTime();
			m_log.debug(methodName + DELIMITER + String.format("getOneRecord. processing time=%dmsec", processMsec));
		}

		if (BinaryConstant.COLLECT_TYPE_WHOLE_FILE.equals(download.getCollectType())) {
			boolean isTop = true;
			for (CollectStringDataPK pk : download.getPkList()) {
				long startM = HinemosTime.getDateInstance().getTime();

				try {
					// トランザクション制御開始.
					jtm = new JpaTransactionManager();
					jtm.begin();
					download.outputTmpFileAll(isTop, pk);
					isTop = false;
					// コミット処理.
					jtm.commit();
				} catch (HinemosDbTimeout | HinemosUnknown e) {
					if (jtm != null) {
						jtm.rollback();
					}
					// エクセプション発生した場合は書込み途中のファイルを削除する.
					if (download.getTmpFile().exists()) {
						this.deleteDownloadedBinaryRecord(download.getFileName(), tempDir);
					}
					throw e;
				} catch (RuntimeException e) {
					if (jtm != null) {
						jtm.rollback();
					}
					// エクセプション発生した場合は書込み途中のファイルを削除する.
					if (download.getTmpFile().exists()) {
						this.deleteDownloadedBinaryRecord(download.getFileName(), tempDir);
					}
					m_log.warn(e.getMessage(), e);
					throw new HinemosUnknown(e.getMessage(), e);
				} finally {
					if (jtm != null) {
						// トランザクション境界ここまで.
						jtm.close();
					}
				}

				if (m_log.isDebugEnabled()) {
					long msec = HinemosTime.getDateInstance().getTime() - startM;
					m_log.debug(methodName + DELIMITER
							+ String.format("outputTmpFileAll by record. processing time=%dmsec", msec));
				}
			}
			m_log.debug(methodName + DELIMITER
					+ String.format(
							"success write binary from records as a file. dirName=[%s], fileName=[%s], collectType=%s",
							download.getDirName(), download.getFileName(), download.getCollectType()));

		} else {
			try {
				// トランザクション制御開始.
				jtm = new JpaTransactionManager();
				jtm.begin();
				download.outputTmpFileRecord();
				// コミット処理.
				jtm.commit();
			} catch (HinemosUnknown e) {
				if (jtm != null) {
					jtm.rollback();
				}
				// エクセプション発生した場合は書込み途中のファイルを削除する.
				if (download.getTmpFile().exists()) {
					this.deleteDownloadedBinaryRecord(download.getFileName(), tempDir);
				}
				throw e;
			} catch (RuntimeException e) {
				if (jtm != null) {
					jtm.rollback();
				}
				// エクセプション発生した場合は書込み途中のファイルを削除する.
				if (download.getTmpFile().exists()) {
					this.deleteDownloadedBinaryRecord(download.getFileName(), tempDir);
				}
				m_log.warn(e.getMessage(), e);
				throw new HinemosUnknown(e.getMessage(), e);
			} finally {
				if (jtm != null) {
					// トランザクション境界ここまで.
					jtm.close();
				}
			}
		}

		if (m_log.isDebugEnabled()) {
			processMsec = HinemosTime.getDateInstance().getTime() - beforeMsec;
			beforeMsec = HinemosTime.getDateInstance().getTime();
			m_log.debug(methodName + DELIMITER + String.format("outputTmpFile. processing time=%dmsec", processMsec));
		}

		return new RestDownloadFile(download.getTmpFile(), download.getFileName());

	}

	/**
	 * バイナリファイルの収集済レコードをマネージャに一時ファイルとして出力.<br>
	 * <br>
	 * 収集済レコードをZIPにまとめるため一時ファイルとして出力.
	 * 
	 * @param recordTime
	 *            収集蓄積の表示時刻.
	 * @param recordKey
	 *            ダウンロード対象のレコードキー(内部変数でクライアント表示なし)
	 * @param intoZipList
	 *            ZIPにまとめるファイルのリスト、出力したファイルが一意になるようにaddしてreturn.
	 * 
	 * @return 添付ファイルとして取得したバイナリレコード
	 * @throws BinaryRecordNotFound
	 * @throws HinemosDbTimeout
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	public ArrayList<String> createTmpRecords(BinaryQueryInfo queryInfo, CollectStringDataPK primaryKey,
			ArrayList<String> intoZipList, String tempDir)
			throws BinaryRecordNotFound, HinemosDbTimeout, InvalidSetting, HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");

		// 入力チェック
		if (primaryKey == null) {
			InvalidSetting e = new InvalidSetting(methodName + DELIMITER + "primaryKey is not defined.");
			m_log.info(methodName + DELIMITER + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		JpaTransactionManager jtm = null;
		DownloadBinary download = new DownloadBinary(queryInfo);
		try {
			// トランザクション制御開始.
			jtm = new JpaTransactionManager();
			jtm.begin();

			// レコードデータを取得する.
			download.getOneRecord(primaryKey);
			if (BinaryConstant.COLLECT_TYPE_WHOLE_FILE.equals(download.getCollectType())) {
				download.getDataIds();
			}
			
			download.createTemporaryFile(null, tempDir);
			// コミット処理.
			jtm.commit();
		} catch (HinemosDbTimeout e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosDbTimeout(MessageConstant.MESSAGE_HUB_SEARCH_TIMEOUT.getMessage());
		} catch (BinaryRecordNotFound e) {
			if (jtm != null)
				jtm.rollback();
			throw e;
		} catch (RuntimeException e) {
			m_log.warn(methodName + DELIMITER + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				// トランザクション境界ここまで.
				jtm.close();
			}
		}

		if (BinaryConstant.COLLECT_TYPE_WHOLE_FILE.equals(download.getCollectType())) {
			boolean isTop = true;
			for (CollectStringDataPK pk : download.getPkList()) {
				try {
					// トランザクション制御開始.
					jtm = new JpaTransactionManager();
					jtm.begin();
					download.outputTmpFileAll(isTop, pk);
					isTop = false;
					// コミット処理.
					jtm.commit();
				} catch (HinemosDbTimeout | HinemosUnknown e) {
					if (jtm != null) {
						jtm.rollback();
					}
					// エクセプション発生した場合は書込み途中のファイルを削除する.
					if (download.getTmpFile().exists()) {
						this.deleteDownloadedBinaryRecord(download.getFileName(), tempDir);
					}
					throw e;
				} catch (RuntimeException e) {
					if (jtm != null) {
						jtm.rollback();
					}
					// エクセプション発生した場合は書込み途中のファイルを削除する.
					if (download.getTmpFile().exists()) {
						this.deleteDownloadedBinaryRecord(download.getFileName(), tempDir);
					}
					m_log.warn(e.getMessage(), e);
					throw new HinemosUnknown(e.getMessage(), e);
				} finally {
					if (jtm != null) {
						// トランザクション境界ここまで.
						jtm.close();
					}
				}
			}
			m_log.debug(methodName + DELIMITER
					+ String.format(
							"success write binary from records as a file. dirName=[%s], fileName=[%s], collectType=%s",
							download.getDirName(), download.getFileName(), download.getCollectType()));

		} else {
			try {
				// トランザクション制御開始.
				jtm = new JpaTransactionManager();
				jtm.begin();
				download.outputTmpFileRecord();
				// コミット処理.
				jtm.commit();
			} catch (HinemosUnknown e) {
				if (jtm != null)
					jtm.rollback();
				// エクセプション発生した場合は書込み途中のファイルを削除する.
				if (download.getTmpFile().exists()) {
					this.deleteDownloadedBinaryRecord(download.getFileName(), tempDir);
				}
				throw e;
			} catch (RuntimeException e) {
				if (jtm != null)
					jtm.rollback();
				// エクセプション発生した場合は書込み途中のファイルを削除する.
				if (download.getTmpFile().exists()) {
					this.deleteDownloadedBinaryRecord(download.getFileName(), tempDir);
				}
				m_log.warn(e.getMessage(), e);
				throw new HinemosUnknown(e.getMessage(), e);
			} finally {
				if (jtm != null) {
					// トランザクション境界ここまで.
					jtm.close();
				}
			}
		}

		ArrayList<String> returnList = new ArrayList<String>(intoZipList);
		m_log.debug(methodName + DELIMITER
				+ String.format("prepared to add the intoZipList for [%s]. addWrite=%b listSize=%d",
						download.getTmpFile().getAbsolutePath(), download.isAddWrite(), returnList.size()));
		try {
			// トランザクション制御開始.
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 初回作成ファイルの場合のみzipリストに追加(ログはレコードをまとめて1ファイルに統合).
			if (!(download.isAddWrite() || intoZipList.contains(download.getTmpFile().getAbsolutePath()))) {
				returnList.add(download.getTmpFile().getAbsolutePath());
				m_log.debug(methodName + DELIMITER
						+ String.format("add the intoZipList. filename=[%s]", download.getTmpFile().getAbsolutePath()));
			} else {
				m_log.debug(methodName + DELIMITER + String.format("skip to add the intoZipList. filename=[%s]",
						download.getTmpFile().getAbsolutePath()));
			}
			m_log.debug(methodName + DELIMITER + String.format("add the intoZipLis for [%s]. addWrite=%b, listSize=%d",
					download.getTmpFile().getAbsolutePath(), download.isAddWrite(), returnList.size()));

			// コミット処理.
			jtm.commit();
		} catch (RuntimeException e) {
			m_log.warn(methodName + DELIMITER + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				// トランザクション境界ここまで.
				jtm.close();
			}
		}

		return returnList;

	}

	/**
	 * ZIPファイルにまとめてクライアント送信用添付ファイル返却.
	 * 
	 * @param intoZipList
	 *            ZIPにまとめるファイルリスト
	 * @param ouputZipName
	 *            出力ZIPファイル名.
	 * @return 作成したZIPの添付ファイル
	 * @throws HinemosUnknown
	 */
	public RestDownloadFile createZipHandler(ArrayList<String> intoZipList, String tempDir, String ouputZipName)
			throws HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");
		m_log.debug(String.format("intoZipList=[%s], tempDir=[%s], ouputZipName=[%s]", intoZipList, tempDir, ouputZipName));

		// マネージャーの出力先ファイルを生成.
		File ouputZip = new File(tempDir, ouputZipName);
		m_log.debug(methodName + DELIMITER
				+ String.format("create file object to outpu zip. file=[%s]", ouputZip.getAbsolutePath()));

		try {
			// ZIP形式に圧縮.
			ZipCompressor.archive(intoZipList, ouputZip.getAbsolutePath());
			m_log.debug(
					methodName + DELIMITER + String.format("create the zip. file=[%s]", ouputZip.getAbsolutePath()));
		} catch (HinemosUnknown e) {
			if (ouputZip.exists()) {
				// zipファイル生成済の場合削除.
				this.deleteDownloadedBinaryRecord(ouputZipName, tempDir);
			}
			m_log.warn(methodName + DELIMITER + String.format("faild to create the zip. file=[%s], message=%s",
					ouputZip.getAbsolutePath(), e.getMessage()), e);
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			// 圧縮前のファイルを削除.
			for (String intoZipFile : intoZipList) {
				String fileName = new File(intoZipFile).getName();
				this.deleteDownloadedBinaryRecord(fileName, tempDir);
			}
		}

		m_log.debug(
				methodName + DELIMITER + String.format("return the DataHandler for [%s].", ouputZip.getAbsolutePath()));

		return new RestDownloadFile(ouputZip, ouputZipName);
	}

	/**
	 * バイナリファイルのダウンロード済レコードを削除.
	 * 
	 * @param fileName
	 *            削除対象ファイル名(絶対パスではないので注意).
	 * @param tempDir
	 *            クライアント名(一時ファイル出力先フォルダ名特定用).
	 */
	public void deleteDownloadedBinaryRecord(String fileName, String tempDir) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		File directory = new File(tempDir);
		String dirName = directory.getAbsolutePath();
		File file = new File(dirName, fileName);
		if (!file.delete()) {
			m_log.warn(methodName + DELIMITER + String.format("Fail to delete file[%s].", file.getAbsolutePath()));
		} else {
			m_log.debug(methodName + DELIMITER + String.format("Success to delete file[%s].", file.getAbsolutePath()));
		}
		try {
			String[] fileDirArray = directory.list();
			if (fileDirArray == null || fileDirArray.length == 0) {
				if (!directory.delete()) {
					m_log.warn(methodName + DELIMITER + String.format("Fail to delete directory[%s].", dirName));
				} else {
					m_log.debug(methodName + DELIMITER + String.format("Success to delete directory[%s].", dirName));
				}
			} else {
				m_log.debug(methodName + DELIMITER + String.format("Skip to delete directory[%s].", dirName));
			}
		} catch (Exception e) {
			m_log.warn(methodName + DELIMITER + String.format("Fail to delete directory[%s].", dirName), e);
		}
	}

	/**
	 * Fetch BinaryCheckInfo from preset properties
	 * 
	 * Send internal error if failed to read
	 * 
	 * @param propertyFile
	 * @return
	 * @throws IOException
	 * @throws InvalidSetting
	 */
	private BinaryCheckInfo fetchPreset(File propertyFile) throws IOException, InvalidSetting {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");

		BinaryCheckInfo info = null;
		try (FileInputStream inputStream = new FileInputStream(propertyFile)) {

			// プロパティから取得した値を返却用のBeanにセット.
			Properties props = new Properties();
			info = new BinaryCheckInfo();
			props.load(inputStream);

			// プロパティの内容確認(デバッガ向け).
			if (m_log.isDebugEnabled()) {
				StringBuilder msgStr = new StringBuilder("preset = " + propertyFile.getName());
				for (Object key : props.keySet()) {
					msgStr = msgStr.append(
							msgStr.toString() + ", " + key.toString() + ":" + props.getProperty(key.toString()));
				}
				m_log.debug(msgStr.toString());
			}

			String property = null;

			// 表示プリセット名.
			property = props.getProperty(PRESET_NAME);
			if (property == null) {
				// - Necessary property not existed error
				InvalidSetting e = new InvalidSetting(
						String.format("the property [%s] is to be required for preset. file=[%s].", PRESET_NAME,
								propertyFile.getAbsolutePath()));
				m_log.info(methodName + DELIMITER + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			info.setTagType(property);

			// 以下はセットされてなかったらダイアログ入力とする.
			String errMsg = "";
			property = props.getProperty(PRESET_FILE_HEAD_SIZE);
			if (property == null) {
				errMsg = errMsg + "\n"
						+ String.format(PRESET_MESSAGE, PRESET_FILE_HEAD_SIZE, propertyFile.getAbsolutePath());
			} else {
				info.setFileHeadSize(Integer.parseInt(property));
			}
			property = props.getProperty(PRESET_LENGTH_TYPE);
			if (property == null) {
				errMsg = errMsg + "\n"
						+ String.format(PRESET_MESSAGE, PRESET_LENGTH_TYPE, propertyFile.getAbsolutePath());
			} else {
				property = PRESET_LENGTH_TYPE_PREFIX + property;
				info.setLengthType(property);
			}
			property = props.getProperty(PRESET_FIXED_RECORD_SIZE);
			if (property == null) {
				errMsg = errMsg + "\n"
						+ String.format(PRESET_MESSAGE, PRESET_FIXED_RECORD_SIZE, propertyFile.getAbsolutePath());
			} else {
				info.setRecordSize(Integer.parseInt(property));
			}
			property = props.getProperty(PRESET_VARIABLE_RECORD_HEAD_SIZE);
			if (property == null) {
				errMsg = errMsg + "\n" + String.format(PRESET_MESSAGE, PRESET_VARIABLE_RECORD_HEAD_SIZE,
						propertyFile.getAbsolutePath());
			} else {
				info.setRecordHeadSize(Integer.parseInt(property));
			}
			property = props.getProperty(PRESET_VARIABLE_RECORD_SIZE_POSITION);
			if (property == null) {
				errMsg = errMsg + "\n" + String.format(PRESET_MESSAGE, PRESET_VARIABLE_RECORD_SIZE_POSITION,
						propertyFile.getAbsolutePath());
			} else {
				info.setSizePosition(Integer.parseInt(property));
			}
			property = props.getProperty(PRESET_VARIABLE_RECORD_SIZE_LENGTH);
			if (property == null) {
				errMsg = errMsg + "\n" + String.format(PRESET_MESSAGE, PRESET_VARIABLE_RECORD_SIZE_LENGTH,
						propertyFile.getAbsolutePath());
			} else {
				info.setSizeLength(Integer.parseInt(property));
			}
			property = props.getProperty(PRESET_HAVE_TIMESTAMP);
			if (property == null) {
				errMsg = errMsg + "\n"
						+ String.format(PRESET_MESSAGE, PRESET_HAVE_TIMESTAMP, propertyFile.getAbsolutePath());
			} else {
				info.setHaveTs(Boolean.valueOf(property));
			}
			property = props.getProperty(PRESET_TIMESTAMP_POSITION);
			if (property == null) {
				errMsg = errMsg + "\n"
						+ String.format(PRESET_MESSAGE, PRESET_TIMESTAMP_POSITION, propertyFile.getAbsolutePath());
			} else {
				info.setTsPosition(Integer.parseInt(property));
			}
			property = props.getProperty(PRESET_TIMESTAMP_TYPE);
			if (property == null) {
				errMsg = errMsg + "\n"
						+ String.format(PRESET_MESSAGE, PRESET_TIMESTAMP_TYPE, propertyFile.getAbsolutePath());
			} else {
				property = PRESET_TS_TYPE_PREFIX + property;
				info.setTsType(property);
			}
			property = props.getProperty(PRESET_LITTLE_ENDIAN);
			if (property == null) {
				errMsg = errMsg + "\n"
						+ String.format(PRESET_MESSAGE, PRESET_LITTLE_ENDIAN, propertyFile.getAbsolutePath());
			} else {
				info.setLittleEndian(Boolean.valueOf(property));
			}

			if (!errMsg.isEmpty()) {
				info.setErrMsg(errMsg);
			} else {
				info.setErrMsg(null);
			}

		} catch (FileNotFoundException e) {
			m_log.warn(methodName + DELIMITER + String.format("failed to find [%s]", propertyFile.getAbsolutePath())
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e;
		} catch (IOException e) {
			// - File read error
			m_log.warn(
					methodName + DELIMITER + String.format("failed to input from [%s]", propertyFile.getAbsolutePath())
							+ e.getClass().getSimpleName() + ", " + e.getMessage(),
					e);
			throw e;
		} catch (NumberFormatException e) {
			// - Properties format error
			m_log.warn(methodName + DELIMITER
					+ String.format("failed to convert from [%s]", propertyFile.getAbsolutePath())
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		return info;
	}

	/**
	 * プリセット配置ディレクトリからプリセットの一覧を取得し、返す
	 * 
	 * @param ownerRoleId
	 * @return
	 * @throws IOException
	 * @throws InvalidSetting
	 */
	public List<BinaryCheckInfo> getPresetList() throws InvalidSetting {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");

		List<BinaryCheckInfo> list = new ArrayList<>();

		String dir = System.getProperty("hinemos.manager.etc.dir") + File.separator + "binary_preset";
		File[] presetFiles = new File(dir).listFiles();

		if (presetFiles == null || presetFiles.length == 0) {
			m_log.info("no preset exists.");
			InvalidSetting e = new InvalidSetting(
					methodName + DELIMITER + String.format("no preset exists in [%s].", dir));
			m_log.info(methodName + DELIMITER + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		BinaryCheckInfo errorInfo = null;

		for (File presetFile : presetFiles) {
			if (presetFile.isFile()) {
				try {
					list.add(fetchPreset(presetFile));
				} catch (Exception e) {
					// 設定不正のファイルはクライアントでエラーメッセージ表示(正常に取得できたファイルはプルダウン表示させる).
					errorInfo = new BinaryCheckInfo();
					errorInfo.setTagType("");
					errorInfo.setFileName(presetFile.getAbsolutePath());
					errorInfo.setErrMsg(e.toString());
					list.add(errorInfo);
				}
				m_log.debug("load preset: " + presetFile.getName());
			}
		}

		// ファイル種別(表示プリセット名)で比較してソート.
		Collections.sort(list, new Comparator<BinaryCheckInfo>() {
			@Override
			public int compare(BinaryCheckInfo info1, BinaryCheckInfo info2) {
				// ファイル種別で比較した結果を返却.
				return info1.getTagType().compareTo(info2.getTagType());
			}
		});

		return list;
	}

}
