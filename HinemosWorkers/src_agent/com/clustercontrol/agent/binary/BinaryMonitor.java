/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.binary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtBinaryCheckInfoRequest;
import org.openapitools.client.model.AgtBinaryCheckInfoResponse;
import org.openapitools.client.model.AgtBinaryFileDTORequest;
import org.openapitools.client.model.AgtBinaryPatternInfoResponse;
import org.openapitools.client.model.AgtBinaryRecordDTORequest;
import org.openapitools.client.model.AgtMessageInfoRequest;
import org.openapitools.client.model.AgtMonitorInfoRequest;
import org.openapitools.client.model.AgtMonitorInfoResponse;
import org.openapitools.client.model.AgtRunInstructionInfoRequest;
import org.openapitools.client.model.AgtRunInstructionInfoResponse;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.binary.factory.BinaryAddTags;
import com.clustercontrol.agent.binary.factory.BinaryCollector;
import com.clustercontrol.agent.binary.factory.BinaryFiltering;
import com.clustercontrol.agent.binary.factory.BinaryForwarder;
import com.clustercontrol.agent.binary.factory.BinarySeparator;
import com.clustercontrol.agent.binary.readingstatus.DirectoryReadingStatus;
import com.clustercontrol.agent.binary.readingstatus.FileReadingStatus;
import com.clustercontrol.agent.binary.readingstatus.MonitorReadingStatus;
import com.clustercontrol.agent.binary.result.BinaryFile;
import com.clustercontrol.agent.binary.result.BinaryRecord;
import com.clustercontrol.agent.log.MonitorInfoWrapper;
import com.clustercontrol.agent.util.RestCalendarUtil;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.binary.bean.BinaryConstant;
import com.clustercontrol.util.BinaryUtil;
import com.clustercontrol.util.FileUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * バイナリファイル監視クラス.
 * 
 * @since 6.1.0
 * @version 6.1.0
 */
public class BinaryMonitor {

	// ログ出力関連
	/** ロガー */
	private static Log m_log = LogFactory.getLog(BinaryMonitor.class);

	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	// staticフィールド.
	/** 一時ファイルの接頭語 */
	private static final String TEMPORARY_PREFIX = "tmp_thread";

	/**
	 * 監視対象バイナリファイル初回接続フラグ<br>
	 * <br>
	 * ファイル接続に失敗した場合、<br>
	 * 初回接続の場合のみ「ファイルがありません」というinternalイベントを発生させる.<br>
	 */
	private boolean m_initFlag = true;

	// 監視対象の基本情報.
	/** 監視対象ファイルの読込状態 */
	private FileReadingStatus readingStatus;

	/** 監視対象ファイルとの接続 */
	private FileChannel fileChannel;

	/** 監視設定・監視結果 */
	private MonitorInfoWrapper m_wrapper = null;

	/** 最終詳細チェック（冒頭データ比較）実行時刻 */
	private long m_lastDataCheck;

	// 監視前チェック.
	/** ファイル変更なし回数(最終更新日時で判定) */
	private long m_unchanged_stats = 0L;

	// 監視処理.
	/** 読込サイズ(レコード分割前) */
	private long readedSize = 0L;

	/** 増加分読み込み成功フラグ */
	private boolean readSuccessFlg;

	/** 監視時点のファイルサイズ */
	private long currentFilesize = 0L;

	/** 監視時点のファイル最終更新タイムスタンプ */
	private long currentFileTimeStamp = 0L;

	/** 監視時点のファイルの先頭バイナリ */
	private List<Byte> currentPrefix = new ArrayList<Byte>();

	// ローテーション関連
	/** ローテーションフラグ(true:ローテートあり) */
	private boolean rotateFlag;

	/** ローテーションのため監視処理スキップ(true:監視処理スキップ) */
	private boolean skipForRotate = false;

	/** ローテーション処理で新規作成されたファイルRS */
	private Map<String, FileReadingStatus> rotatedRSMap;

	// ファイル全体監視向け.
	/** 一時ファイル命名用スレッドID */
	private String tmpThreadId;

	/** レコード区切り方法 **/
	private static enum CutProcessType {
		/** 固定長 **/
		FIXED,
		/** 可変長 **/
		VARIABLE,
		/** 時間区切り **/
		INTERVAL,
		/** エラー(判定に必要な情報が存在しない等) **/
		ERROR
	}

	/**
	 * コンストラクタ
	 * 
	 * @param monitorInfo
	 *            監視情報
	 * @param readingStatus
	 *            監視対象ファイルの読込状態
	 */
	public BinaryMonitor(MonitorInfoWrapper monitorInfo, FileReadingStatus readingStatus) {
		this.readingStatus = readingStatus;
		this.m_wrapper = monitorInfo;
		this.rotateFlag = false;
		this.m_lastDataCheck = HinemosTime.currentTimeMillis();
	}

	/**
	 * 監視処理.<br>
	 * 
	 * @return ローテーションで生成されたファイルのみを読込んだ場合true.<br>
	 * 
	 */
	public boolean run() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + String.format("monitor start. monitorId=%s, monitorFile=[%s]",
				m_wrapper.getId(), readingStatus.getMonFileName()));

		// 性能検証用に時刻取得.
		long startMsec = HinemosTime.getDateInstance().getTime();
		boolean onlyRotatedFile = false;

		// 読込状態を初期化.
		if (!readingStatus.isInitialized()) {
			readingStatus.initFileRS();
		}

		// --監視開始チェック.
		// 読込状態を初期化できない場合は終了.
		if (!readingStatus.isInitialized()) {
			return onlyRotatedFile;
		}
		// 読込状態がクローズされている場合は終了.
		if (!readingStatus.isReadingFlag()) {
			return onlyRotatedFile;
		}

		// 前回の監視処理が走ってる場合はスキップ(スレッド制御上WholeFileのみ発生しうる).
		if (this.readingStatus.isRunMonitor()) {
			m_log.info(methodName + DELIMITER
					+ String.format(
							"skip to run monitor because last monitor was incomplete. monitorId=[%s], file=[%s],",
							this.m_wrapper.getId(), this.readingStatus.getMonFileName()));
			return onlyRotatedFile;
		}

		// ローテーションのため他ファイル監視時に読込む場合は監視処理スキップ.
		if (this.skipForRotate) {
			return onlyRotatedFile;
		}

		// カレンダー非稼動チェック.
		AgtMonitorInfoResponse monInfo = m_wrapper.monitorInfo;
		AgtRunInstructionInfoResponse jobInfo = m_wrapper.runInstructionInfo;
		if (jobInfo == null && monInfo.getCalendar() != null && !RestCalendarUtil.isRun(monInfo.getCalendar())) {
			// ジョブではなく監視設定による監視でカレンダー設定されているが非稼動の場合.
			m_log.debug(methodName + DELIMITER + "monitor is skipped because it's out of monitor by calendar");
			return onlyRotatedFile;
		}

		// 監視を開始するタイミングでスレッドIDを設定する
		this.setTmpThreadId(Long.toString(Thread.currentThread().getId()));

		// --監視開始.
		this.readingStatus.setRunMonitor(true);
		// 監視対象のファイル接続.
		if (fileChannel == null) {
			// ファイルなし等で接続エラーの場合は終了.
			if (!openFile()) {
				return onlyRotatedFile;
			}
		}

		// 収集対象に応じた監視実施.
		String collectType = monInfo.getBinaryCheckInfo().getCollectType();
		if (BinaryConstant.COLLECT_TYPE_WHOLE_FILE.equals(collectType)) {
			// ファイル全体.
			this.wholeFileMon();
		} else {
			// 増分のみ(読込最大サイズ：int値の上限で約2MB).
			onlyRotatedFile = this.incrementalDataMon();
		}
		this.readingStatus.setRunMonitor(false);

		// 性能出力(デバッガモード).
		long msec = HinemosTime.getDateInstance().getTime() - startMsec;
		m_log.debug(methodName + DELIMITER
				+ String.format("monitor end. processing time=%d msec, monitorId=%s, monitorFile=[%s]", msec,
						m_wrapper.getId(), readingStatus.getMonFileName()));
		return onlyRotatedFile;
	}

	/**
	 * 監視管理情報へ通知
	 * 
	 * @param priority
	 *            重要度
	 * @param app
	 *            アプリケーション
	 * @param msg
	 *            メッセージ
	 * @param msgOrg
	 *            オリジナルメッセージ
	 */
	private void sendMessage(int priority, String app, String msg, String msgOrg) {
		BinaryMonitorManager.sendMessage(priority, app, msg, msgOrg, m_wrapper.monitorInfo.getMonitorId(),
				m_wrapper.runInstructionInfoReq, m_wrapper.monitorInfo.getMonitorTypeId());
	}

	/**
	 * 転送対象ログファイルクローズ
	 * 
	 */
	protected void closeFileChannel() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		m_log.info(methodName + DELIMITER + "filepath=" + readingStatus.getMonFileName());
		if (fileChannel != null) {
			try {
				fileChannel.close();
				fileChannel = null;
			} catch (IOException e) {
				m_log.warn(methodName + DELIMITER + e.getMessage(), e);
			}
		}
	}

	/**
	 * 監視対象バイナリファイル接続.<br>
	 * 
	 * @return 初回読込で接続エラーの場合false.
	 * 
	 */
	private boolean openFile() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.info(methodName + DELIMITER + "filename=" + readingStatus.getMonFileName());

		this.closeFileChannel();

		FileChannel fc = null;
		FileChannel copyFc = null;

		// ファイルオープン.
		// 処理順 try or catch return前まで ⇒ finally ⇒ return.
		try {

			// 収集対象.
			String collectType = m_wrapper.monitorInfo.getBinaryCheckInfo().getCollectType();
			// 先頭バイナリチェック.
			if ((BinaryConstant.COLLECT_TYPE_ONLY_INCREMENTS.equals(collectType)) && checkPrefix()) {
				// ファイル増分監視の場合に、readingStatus作成時点と監視対象ファイルの先頭バイナリが変わっている.
				this.rotate();
			}

			// 読込接続オープン.
			Path monitorFilePath = readingStatus.getMonFile().toPath();
			fc = FileChannel.open(monitorFilePath, StandardOpenOption.READ);

			if (BinaryConstant.COLLECT_TYPE_WHOLE_FILE.equals(collectType)) {
				// ファイル全体監視の場合、一時ファイルをReadingStatusと同じフォルダに出力.
				File copyFile = new File(readingStatus.getParentDirRS().getStoreFileRSDir(), this.getTmpFileName());
				if (copyFile.exists()) {
					if (copyFile.delete()) {
						// 本来監視処理終了時に削除してる想定なのでinfoレベルでログ出力しとく.
						m_log.info(
								methodName + DELIMITER
										+ String.format(
												"success to delete temporary file on opening file channel. file=[%s]",
												copyFile.getAbsolutePath()));
					} else {
						m_log.warn(
								methodName + DELIMITER
										+ String.format(
												"failed to delete temporary file on opening file channel. file=[%s]",
												copyFile.getAbsolutePath()));
					}
				}
				// 前回監視時の最終更新日時と現状の最終更新日時を比較して同じであれば監視済として完了.
				if (this.readingStatus.getMonFileLastModTimeStamp() == this.readingStatus.getMonFile().lastModified()) {
					m_log.debug(methodName + DELIMITER + String.format(
							"skip to run monitor because completed in past time. file=[%s], last monitor modified time=%d, now modified time=%d",
							this.readingStatus.getMonFileName(), this.readingStatus.getMonFileLastModTimeStamp(),
							this.readingStatus.getMonFile().lastModified()));
					return false;
				}
				// ファイル全体監視の場合、一時ファイルをコピー
				// Files.copyメソッドはコピー中に書込みあった場合はコピー前ファイルを破棄して再取得します.
				Files.copy(monitorFilePath, copyFile.toPath());
				copyFc = FileChannel.open(monitorFilePath, StandardOpenOption.READ);
				copyFc.position(0);
				//コピー元ファイルのチャネルは全体監視では利用しないのでクローズ（閉じないとＯＳ側でハンドルリークする）
				fc.close();
				this.fileChannel = copyFc;
				m_log.debug(methodName + DELIMITER
						+ String.format("copied monitor file. copy file=[%s], size=%dbytes, last modified time=%s",
								copyFile.getAbsolutePath(), copyFc.size(),
								new Timestamp(copyFile.lastModified()).toString()));
			} else {
				// 増分監視の場合は、ファイルポインタ(前回までの読込完了位置)を設定.
				fc.position(readingStatus.getPosition());
				// 監視対象ファイルとして格納.
				this.fileChannel = fc;
				m_log.debug(methodName + DELIMITER
						+ String.format(
								"set file channel from monitor file. file=[%s], size=%dbytes, last modified time=%s",
								readingStatus.getMonFileName(), fc.size(),
								new Timestamp(readingStatus.getMonFile().lastModified()).toString()));
			}

			// ログ出力.
			long filesize = this.fileChannel.size();
			if (filesize > BinaryMonitorConfig.getFileMaxSize()) {
				// ファイルサイズが読込上限サイズより大きい場合、監視管理へ通知
				// message.log.agent.1=ログファイル「{0}」
				// message.log.agent.3=ファイルサイズが上限を超えました
				// message.log.agent.5=ファイルサイズ「{0} byte」
				String[] args1 = { readingStatus.getMonFile().getPath() };
				String[] args2 = { String.valueOf(filesize) };
				sendMessage(PriorityConstant.TYPE_INFO, MessageConstant.AGENT.getMessage(),
						MessageConstant.MESSAGE_LOG_FILE_SIZE_EXCEEDED_UPPER_BOUND.getMessage(),
						MessageConstant.MESSAGE_BINARY_FILE.getMessage(args1) + ", "
								+ MessageConstant.MESSAGE_LOG_FILE_SIZE_BYTE.getMessage(args2));
				this.fileChannel = null;
				return false;
			}

			return true;
		} catch (FileNotFoundException e) {
			m_log.info("openFile : " + e.getMessage(), e);
			if (m_initFlag) {
				// 最初にファイルをチェックする場合、監視管理へ通知
				// message.log.agent.1=ログファイル「{0}」
				// message.log.agent.2=ログファイルがありませんでした
				String[] args = { readingStatus.getMonFile().getPath() };
				sendMessage(PriorityConstant.TYPE_INFO, MessageConstant.AGENT.getMessage(),
						MessageConstant.MESSAGE_BINARY_FILE_NOT_FOUND.getMessage(),
						MessageConstant.MESSAGE_BINARY_FILE.getMessage(args));
			}

			return false;
		} catch (SecurityException e) {
			m_log.info("openFile : " + e.getMessage(), e);
			if (m_initFlag) {
				// 監視管理へ通知
				// message.log.agent.1=ログファイル「{0}」
				// message.log.agent.4=ファイルの読み込みに失敗しました
				String[] args = { readingStatus.getMonFile().getPath() };
				sendMessage(PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
						MessageConstant.MESSAGE_LOG_FAILED_TO_READ_FILE.getMessage(),
						MessageConstant.MESSAGE_BINARY_FILE.getMessage(args) + "\n" + e.getMessage());
			}
			return false;
		} catch (IOException e) {
			m_log.info("openFile : " + e.getMessage(), e);
			if (m_initFlag) {
				// 監視管理へ通知
				// message.log.agent.1=ログファイル「{0}」
				// message.log.agent.4=ファイルの読み込みに失敗しました
				String[] args = { readingStatus.getMonFile().getPath() };
				sendMessage(PriorityConstant.TYPE_INFO, MessageConstant.AGENT.getMessage(),
						MessageConstant.MESSAGE_LOG_FAILED_TO_READ_FILE.getMessage(),
						MessageConstant.MESSAGE_BINARY_FILE.getMessage(args));
			}
			return false;
		} finally {
			// ファイルは開けたが、なんらかの例外が出力された場合に、ファイルをクローズ。
			if (fc != null && fileChannel == null) {
				try {
					fc.close();
				} catch (IOException e) {
					m_log.warn(e.getMessage(), e);
				}
			}
			// ファイルは開けたが、なんらかの例外が出力された場合に、ファイルをクローズ。
			if (copyFc != null && fileChannel == null) {
				try {
					copyFc.close();
				} catch (IOException e) {
					m_log.warn(e.getMessage(), e);
				}
			}
			// 2回目以降はエラー出力しないため必ずfalse.
			m_initFlag = false;
		}
	}

	/**
	 * 先頭バイナリチェック(ローテーション).<br>
	 * <br>
	 * readingStatus作成時点との先頭バイナリを比較することでファイル変更をチェック.<br>
	 * 
	 * @return 別ファイルの場合はtrue(ローテーション)
	 * 
	 */
	protected boolean checkPrefix() throws IOException {

		try {

			if (this.readingStatus.getPrefix().size() > 0) {
				// readingStatus作成時点で比較用ファイルの先頭バイナリが生成されている場合.
				// あらためて監視対象ファイルの最新バイナリを取得する.

				// outOfMemory防止で指定バイト数だけ読込むため配列長をAgentPropertiesで指定.
				byte[] newFile = new byte[BinaryMonitorConfig.getFirstPartDataCheckSize()];
				try (FileInputStream fi = new FileInputStream(this.readingStatus.getMonFile())) {
					// 監視対象ファイルの先頭から作成したバイト配列長分だけ読込む.
					fi.mark(newFile.length);
					while (true) {
						int readed = fi.read(newFile);
						if (readed == newFile.length) {
							break;
						} else if (readed == this.readingStatus.getMonFile().length()) {
							break;
						} else if (readed < 0) {
							break;
						} else {
							fi.reset();
						}
					}
				}
				List<Byte> newFileBinary = new ArrayList<Byte>();
				newFileBinary = BinaryUtil.arrayToList(newFile);
				List<Byte> newFirstPartOfFile = new ArrayList<Byte>(newFileBinary);

				if (this.readingStatus.getPrefixSize() > 0
						&& newFileBinary.size() >= this.readingStatus.getPrefixSize()) {
					// 新しいファイルが比較対象バイナリのサイズより大きい場合、比較対象と同じサイズを格納して比較.
					int prefixSize = this.readingStatus.getPrefixSize();
					if (this.readingStatus.getPrevSize() < prefixSize) {
						prefixSize = (int) this.readingStatus.getPrevSize();
					}
					newFirstPartOfFile = newFirstPartOfFile.subList(0, prefixSize);

					// ログ出力.
					if (m_log.isDebugEnabled()) {
						try {
							String newFirstPartString = BinaryUtil.listToString(newFirstPartOfFile, 1);
							String preFirstPartString = BinaryUtil.listToString(this.readingStatus.getPrefix().subList(0, prefixSize), 1);
							m_log.debug("run() : " + this.readingStatus.getMonFile().toPath() + " newFirstPartOfFile : "
									+ newFirstPartString);
							m_log.debug("run() : " + this.readingStatus.getMonFile().toPath() + " preFirstPartOfFile : "
									+ preFirstPartString);
						} catch (Exception e) {
							m_log.error("run() : " + this.readingStatus.getMonFile().toPath() + " " + e.getMessage(),
									e);
						}
					}

					// readingStatus作成時点のファイルの先頭と現時点のファイルの先頭を比較.
					if (!BinaryUtil.equals(newFirstPartOfFile, this.readingStatus.getPrefix().subList(0, prefixSize))) {
						// 同一ファイル名なのにreadingStatus作成時点と先頭が異なっているので、ローテーションされてると判定.
						m_log.debug("run() : " + readingStatus.getMonFile().toPath() + " log rotation detected");
						this.rotateFlag = true;
						m_log.debug("run() : m_logrotate set true .");
						// チェック実施したので更新.
						this.m_lastDataCheck = HinemosTime.currentTimeMillis();
						this.currentPrefix = newFirstPartOfFile;
					}

				} else if (newFileBinary.size() < this.readingStatus.getPrefixSize()) {
					// RS作成時点よりファイルサイズが縮んでいるのでローテーションしているとみなす.
					m_log.debug("run() : " + this.readingStatus.getMonFile().toPath() + " log rotation detected");
					this.rotateFlag = true;
					m_log.debug("run() : m_logrotate set true .");
					// チェック実施したので更新.
					this.m_lastDataCheck = HinemosTime.currentTimeMillis();
					// Agent Propertiesで設定しているチェックサイズにカット(サイズ満たない場合は丸ごと)
					newFirstPartOfFile = BinaryUtil.cutByteList(newFirstPartOfFile,
							BinaryMonitorConfig.getFirstPartDataCheckSize(), false);
					this.currentPrefix = newFirstPartOfFile;
				} else {
					// チェック用の先頭バイナリが存在しないのでチェック飛ばす.
					return this.rotateFlag;
				}

			}

		} catch (RuntimeException e) {
			m_log.error("run() : " + readingStatus.getMonFile().toPath() + " " + e.getMessage(), e);
		}
		return this.rotateFlag;
	}

	/**
	 * ファイル全体監視.
	 */
	private void wholeFileMon() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");

		try {
			// 任意バイナリファイルはローテーションなし.
			this.rotateFlag = false;

			// バイナリファイルの情報更新.
			this.currentFilesize = fileChannel.size();
			this.currentFileTimeStamp = readingStatus.getMonFile().lastModified();

			if ((!readingStatus.isInitialized())
					&& (currentFileTimeStamp == readingStatus.getMonFileLastModTimeStamp())) {
				// 初回読込ではないのにRSと同じタイムスタンプの場合は、前回監視時点から更新なしなので終了.
				return;
			}

			// ファイル読込用の変数初期化.
			List<BinaryRecord> sendData = new ArrayList<BinaryRecord>();
			long afterFileTimeStamp = 0;
			boolean readSuccess = false;

			// ファイル単位の監視結果情報をセット.
			BinaryFile fileInfo = new BinaryFile();

			// 送信用にファイルを分割して格納.
			BinaryCollector collector = new BinaryCollector();
			while (true) {
				readSuccess = collector.setMonBigData(fileInfo, sendData, this);
				afterFileTimeStamp = readingStatus.getMonFile().lastModified();
				if (currentFileTimeStamp == afterFileTimeStamp) {
					// バイナリ取得中に更新なしのため終わり.
					m_log.debug(methodName + DELIMITER
							+ String.format("success to read. file=[%s]", readingStatus.getMonFileName()));
					break;
				} else {
					// バイナリ取得中に更新されたためやり直し.
					sendData = new ArrayList<BinaryRecord>();
					this.currentFileTimeStamp = readingStatus.getMonFile().lastModified();
					this.currentFilesize = fileChannel.size();
					m_log.debug(methodName + DELIMITER
							+ String.format("reload by update. timeStamp before=%s, after=%s, file=[%s]",
									new Timestamp(currentFileTimeStamp).toString(),
									new Timestamp(afterFileTimeStamp).toString(), readingStatus.getMonFileName()));
					continue;
				}
			}
			collector = null;

			if (!readSuccess) {
				// 読込失敗したら終了.
				m_log.warn(methodName + DELIMITER
						+ String.format("failed to read binary data. monitorId=%s", m_wrapper.getId()));
				return;
			}

			if (sendData.isEmpty()) {
				// 読込サイズ0の場合.
				m_log.info(methodName + DELIMITER
						+ String.format("readed data is empty. monitorId=%s, file=[%s], fileInfo=%s", m_wrapper.getId(),
								readingStatus.getMonFileName(), fileInfo.toString()));
				return;
			}

			m_log.debug(
					methodName + DELIMITER
							+ String.format("read the binary file [%s]. monitorId=%s, sendData size=%d, fileInfo=%s",
									readingStatus.getMonFileName(), m_wrapper.getId(), sendData.size(),
									fileInfo.toString()));

			// 最終更新時間を設定.
			fileInfo.setLastModTime(new Timestamp(currentFileTimeStamp));
			// タグ情報追加.
			BinaryAddTags.addFileTags(readingStatus, fileInfo, m_wrapper.monitorInfo.getBinaryCheckInfo());

			// パターンマッチ用変数.
			List<AgtBinaryPatternInfoResponse> matchInfoList = new ArrayList<AgtBinaryPatternInfoResponse>();
			this.setFilterInfoList(matchInfoList);

			// パターンマッチ表現が存在する場合はフィルタリング実行.
			BinaryRecord endRecord = null;
			if (matchInfoList != null && !matchInfoList.isEmpty()) {
				BinaryFiltering filtering = new BinaryFiltering(matchInfoList);
				// ファイル全体でパターンマッチするかチェック.
				if (filtering.matchPatternBigData(sendData)) {
					// パターンマッチ結果、処理するの場合は送信用にマッチしたパターンをセット.
					for (BinaryRecord data : sendData) {
						data.setMatchBinaryProvision(filtering.getMatchBinaryProvision());
						// 監視ジョブ向けに最終レコードを保持しておく.
						if (BinaryConstant.FILE_POSISION_END.equals(data.getFilePosition())) {
							endRecord = data;
						}
					}
					m_log.debug(methodName + DELIMITER
							+ String.format(
									"matched pattern in big data to send manager. pattern order=%d, pattern String=%s, processType=%b",
									filtering.getmatchKey(), filtering.getMatchBinaryProvision().getGrepString(),
									filtering.getMatchBinaryProvision().getProcessType()));
				} else {
					// パターンマッチしない場合はRS更新.
					StringBuilder patternListSb = new StringBuilder();
					for (AgtBinaryPatternInfoResponse pattern : matchInfoList) {
						patternListSb.append("[");
						patternListSb.append(pattern.getGrepString());
						patternListSb.append("],");
					}
					m_log.debug(methodName + DELIMITER + "monitor data don't match to " + patternListSb.toString());
				}
				filtering = null;
			} else {
				m_log.debug(methodName + DELIMITER + "not filter record to send manager.");
			}

			// 監視ジョブもしくは収集無効の場合は収集なしで通知用のデータだけ送信する.
			if (m_wrapper.runInstructionInfo != null || !m_wrapper.monitorInfo.getCollectorFlg().booleanValue()) {
				if (endRecord == null) {
					// 監視自体は実施したのでファイルRS更新しとく.
					readingStatus.storeRS(currentFilesize, currentFileTimeStamp);
					// フィルタにマッチしない場合、監視ジョブで通知不要なので終了.
					m_log.info(methodName + DELIMITER
							+ String.format("matchdata isn't exist. monitorId=%s, collectFlg=%b, file=[%s]",
									m_wrapper.getId(), m_wrapper.monitorInfo.getCollectorFlg(), readingStatus.getMonFileName()));
					return;
				}
				sendData.clear();
				sendData.add(endRecord);
				m_log.debug(
						methodName + DELIMITER
								+ String.format("matchdata is exist. monitorId=%s, collectFlg=%b, file=[%s]",
										m_wrapper.getId(), m_wrapper.monitorInfo.getCollectorFlg(), readingStatus.getMonFileName()));
			}

			// マネージャーにバイナリ送信.
			this.sendManager(fileInfo, sendData);

			// ファイルRSを更新.
			readingStatus.storeRS(currentFilesize, currentFileTimeStamp);

		} catch (IOException e) {
			// 読込失敗.
			m_log.error("run() : " + e.getMessage());
			String[] args = { readingStatus.getMonFile().getPath() };
			// message.log.agent.1=バイナリファイル「{0}」
			// message.log.agent.4=ファイルの読み込みに失敗しました
			sendMessage(PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
					MessageConstant.MESSAGE_LOG_FAILED_TO_READ_FILE.getMessage(),
					MessageConstant.MESSAGE_BINARY_FILE.getMessage(args) + "\n" + e.getMessage());

			// エラーが発生したのでファイルクローズ
			this.closeFileChannel();
		} finally {
			// ファイル全体監視の場合、一時ファイルをReadingStatusと同じフォルダに出力してるので削除.
			File copyFile = new File(readingStatus.getParentDirRS().getStoreFileRSDir(), this.getTmpFileName());
			if (copyFile.exists()) {
				if (copyFile.delete()) {
					m_log.debug(methodName + DELIMITER + String.format(
							"success to delete temporary file on last monitor. file=[%s]", copyFile.getAbsolutePath()));
				} else {
					m_log.warn(methodName + DELIMITER + String.format(
							"failed to delete temporary file on last monitor. file=[%s]", copyFile.getAbsolutePath()));
				}
			}
		}
	}

	/**
	 * 増分のみ監視処理
	 * 
	 * @return ローテーションで作成された別ファイルのみを読込んだ場合true;
	 */
	private boolean incrementalDataMon() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");

		boolean onlyRotatedFile = false;

		try {

			// 監視対象ファイルの現在の情報を取得.
			this.currentFilesize = fileChannel.size();
			this.currentFileTimeStamp = readingStatus.getMonFile().lastModified();

			// 一定間隔で更新行うファイル種類の場合、ファイル更新判定を行う.
			if (this.checkRotation() || this.unchangedLongTime()) {
				// ローテーション後ファイル再オープン不可、もしくは一定時間更新されていない場合.
				return false;
			}
			// バイナリファイルの情報更新.
			this.currentFilesize = fileChannel.size();
			this.currentFileTimeStamp = readingStatus.getMonFile().lastModified();
			this.currentPrefix = readingStatus.getCurrenPrefix();

			if (readingStatus.getPrevSize() <= currentFilesize) {
				// RS作成時点のファイルサイズより最新ファイルサイズの方が大きい.
				// ※上記チェックでローテーションしてる場合もprevSize = 0 で更新してるためここ.
				m_log.debug("run() : " + readingStatus.getMonFile().getPath() + " filesize "
						+ readingStatus.getPrevSize() + " tmp_filesize " + currentFilesize);

				// ファイル単位の監視結果情報をセット.
				BinaryFile fileInfo = new BinaryFile();
				fileInfo.setLastModTime(new Timestamp(currentFileTimeStamp));

				// 監視対象ファイルからバイナリ取得.
				List<Byte> readedBinary = new ArrayList<Byte>();
				BinaryCollector collector = new BinaryCollector();
				onlyRotatedFile = collector.setMonitorBinary(fileInfo, readedBinary, this);
				collector = null;
				AgtBinaryCheckInfoResponse binaryInfo = m_wrapper.monitorInfo.getBinaryCheckInfo();
				List<AgtBinaryPatternInfoResponse> matchInfoList = m_wrapper.monitorInfo.getBinaryPatternInfo();

				// ファイル種類別に送信用にレコードを区切ってマップに格納する.
				List<BinaryRecord> sendData = new ArrayList<BinaryRecord>();
				BinarySeparator separator = new BinarySeparator();

				// レコード分割.
				CutProcessType cutType = this.getCutProcessType(binaryInfo);
				int skippedSize = 0;
				switch (cutType) {

				case FIXED:
					//スキップサイズの指定があれば範囲内は無視するように考慮しつつレコードを分割
					sendData = separator.separateFixed(this.m_wrapper.getId(), readedBinary, binaryInfo, readingStatus);
					//分割時に無視したレコード長を取得
					skippedSize = separator.getSkippedSize();
					break;

				case VARIABLE:
					//スキップサイズの指定があれば範囲内は無視するように考慮しつつレコードを分割
					sendData = separator.separateVariable(this.m_wrapper.getId(), readedBinary, binaryInfo,
							readingStatus);
					//分割時に無視したレコード長を取得
					skippedSize = separator.getSkippedSize();
					break;

				case INTERVAL:
					// 時間区切りは取れた分を1レコードとして扱う.
					if (readingStatus.isToSkipRecord()) {
						// ただし、スキップサイズの指定があれば範囲内は無視するように考慮(読込み開始位置なども考慮)
						// 時間区切りにヘッダーはないので考慮しない
						if (readedBinary.size() > this.readingStatus.getSkipSize()) {
							// 読み取ったレコードがスキップサイズを超えた場合、範囲外のデータをレコードとする
							readedBinary = readedBinary.subList((int) this.readingStatus.getSkipSize(), readedBinary.size());
							skippedSize = (int) this.readingStatus.getSkipSize();
						} else {
							// スキップサイズ範囲内の場合は全てスキップ
							skippedSize = readedBinary.size();
							break;
						}
					}
					if (readedBinary.size() == 0) {//
						break;
					}

					String monitorTime = new Timestamp(HinemosTime.getDateInstance().getTime()).toString();
					// レコードキーにファイル名(絶対パス)＋監視時刻を設定.
					String key = readingStatus.getMonFileName() + monitorTime;
					BinaryRecord alldData = new BinaryRecord(key, readedBinary);
					alldData.setFilePosition(BinaryConstant.FILE_POSISION_END);
					sendData.add(alldData);
					break;

				case ERROR:
					// ログ出力済なのでクライアント送信用メッセージをセットして終了.
					String[] args = { readingStatus.getMonFile().getPath() };
					// message.log.agent.1=バイナリファイル「{0}」
					// message.log.agent.4=ファイルの読み込みに失敗しました
					sendMessage(PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
							MessageConstant.MESSAGE_LOG_FAILED_TO_READ_FILE.getMessage(),
							MessageConstant.MESSAGE_BINARY_FILE.getMessage(args));
					return false;

				default:
					// 想定外なのでエラーログ出力.
					m_log.warn(methodName + DELIMITER + String
							.format("failed to get cut type. cutType=%s, monitorId=[%s]", cutType, m_wrapper.getId()));
					String[] args2 = { readingStatus.getMonFile().getPath() };
					// message.log.agent.1=バイナリファイル「{0}」
					// message.log.agent.4=ファイルの読み込みに失敗しました
					sendMessage(PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
							MessageConstant.MESSAGE_LOG_FAILED_TO_READ_FILE.getMessage(),
							MessageConstant.MESSAGE_BINARY_FILE.getMessage(args2));
					return false;
				}

				if (sendData.isEmpty()) {
					// 読込サイズ0の場合.
					m_log.info(methodName + DELIMITER
							+ String.format("readed data is empty. monitorId=%s, file=[%s], fileInfo=%s",
									m_wrapper.getId(), readingStatus.getMonFileName(), fileInfo.toString()));

					// 読込サイズが0の場合でもスキップしたデータがある場合は、ファイルRSを更新する
					if (skippedSize > 0) {
						this.updateFileRS(onlyRotatedFile, fileInfo, 0, skippedSize);
					}
					return false;
				}

				long settingSize = binaryInfo.getFileHeadSize();
				if (settingSize > 0) {
					if (fileInfo.getFileHeader() == null) {
						// ファイルヘッダサイズが存在するのに読込ファイルヘッダが存在しない場合.
						m_log.info(methodName + DELIMITER + String.format(
								"skip to monitor because file header is null." + " fileHeaderSize(monitorSetting)=%d",
								settingSize));
						return false;

					}
					int getSize = fileInfo.getFileHeaderSize();
					if (getSize < settingSize) {
						// ファイルヘッダサイズが存在するのに読込ファイルヘッダが短い場合.
						m_log.info(methodName + DELIMITER
								+ String.format(
										"skip to monitor because file header is too short."
												+ " monitorId=%s, file=[%s], fileHeaderSize(monitorSetting)=%d, fileHeaderSize(data)=%d",
										m_wrapper.getId(), readingStatus.getMonFileName(), settingSize, getSize));
						return false;
					}
				}

				m_log.debug(
						methodName + DELIMITER + String.format("read the binary log[%s]. sendData size=%d, fileInfo=%s",
								readingStatus.getMonFileName(), sendData.size(), fileInfo.toString()));

				//以後の処理でsendDataを補正する可能性があるため ここで読み込みサイズを一旦保存（RSの更新に利用予定）
				long readedSize = separator.getReadedSize(sendData);

				// wtmpの場合は単純な固定長ではないので、レコード再編成する.
				String tagType = m_wrapper.monitorInfo.getBinaryCheckInfo().getTagType();
				if (BinaryConstant.TAG_TYPE_WTMP.equals(tagType)) {
					sendData = separator.coordinateWtmp(sendData);
				}

				// タグを追加する.
				BinaryAddTags.addFileTags(readingStatus, fileInfo, binaryInfo);
				BinaryAddTags.addRecordTags(fileInfo, sendData, binaryInfo, matchInfoList);

				// パターンマッチ用変数.
				matchInfoList = new ArrayList<AgtBinaryPatternInfoResponse>();
				this.setFilterInfoList(matchInfoList);

				// パターンマッチ表現が存在する場合はフィルタリング実行.
				BinaryRecord topMonitorResult = null;
				List<BinaryRecord> matchData = new ArrayList<BinaryRecord>();
				if (matchInfoList != null && !matchInfoList.isEmpty()) {
					BinaryFiltering filtering = new BinaryFiltering(matchInfoList);
					// レコード毎にパターンマッチするかチェック.
					for (BinaryRecord record : sendData) {
						if (filtering.matchPatternRecord(record)) {
							record.setMatchBinaryProvision(filtering.getMatchBinaryProvision());
							// 監視ジョブ向けにマッチした先頭レコードをセット.
							if (topMonitorResult == null) {
								topMonitorResult = record;
							}
							// 収集フラグオフの場合はマッチした監視結果だけを送信する.
							if (!m_wrapper.monitorInfo.getCollectorFlg().booleanValue()) {
								matchData.add(record);
							}
							m_log.debug(methodName + DELIMITER + String.format(
									"matched pattern in a record to send manager. pattern order=%d, pattern String=%s, processType=%b",
									filtering.getmatchKey(), filtering.getMatchBinaryProvision().getGrepString(),
									filtering.getMatchBinaryProvision().getProcessType()));
						} else {
							m_log.debug(methodName + DELIMITER + "unmatched pattern in a record to send manager.");
						}
					}
					filtering = null;
				} else {
					m_log.debug(methodName + DELIMITER + "not filter record to send manager.");
				}

				// 監視ジョブの場合は、一度のみ監視結果を送信すればよいので、マッチした先頭の監視結果だけを送信する.
				if (m_wrapper.runInstructionInfo != null) {
					if (topMonitorResult == null) {
						// マッチした監視結果が存在しない場合は終了(監視自体は実施してるのでファイルRSは更新しとく)
						this.updateFileRS(onlyRotatedFile, fileInfo, readedSize, skippedSize);
						m_log.info(methodName + DELIMITER
								+ String.format("match data for monitor job isn't exist. Id=%s, file=[%s]",
										m_wrapper.getId(), readingStatus.getMonFileName()));
						return false;
					}
					sendData.clear();
					sendData.add(topMonitorResult);
					m_log.debug(methodName + DELIMITER
							+ String.format("match data for monitor job ist exist. Id=%s, file=[%s]", m_wrapper.getId(),
									readingStatus.getMonFileName()));
				}

				// 収集フラグオフの場合はマッチした監視結果だけを送信する.
				if (!m_wrapper.monitorInfo.getCollectorFlg().booleanValue()) {
					if (matchData.isEmpty()) {
						// マッチした監視結果が存在しない場合は終了(監視自体は実施してるのでファイルRSは更新しとく)
						this.updateFileRS(onlyRotatedFile, fileInfo, readedSize, skippedSize);
						m_log.info(methodName + DELIMITER
								+ String.format(
										"match data for only monitoring isn't exist. Id=%s, collectFlg=%b, file=[%s]",
										m_wrapper.getId(), m_wrapper.monitorInfo.getCollectorFlg(), readingStatus.getMonFileName()));
						return false;
					}
					sendData = matchData;
					m_log.debug(methodName + DELIMITER
							+ String.format("match data for only monitoring is exist. Id=%s, collectFlg=%b, file=[%s]",
									m_wrapper.getId(), m_wrapper.monitorInfo.getCollectorFlg(), readingStatus.getMonFileName()));
				}

				// マネージャーにバイナリ送信.
				this.sendManager(fileInfo, sendData);

				// ファイルRSの更新.
				this.updateFileRS(onlyRotatedFile, fileInfo, readedSize, skippedSize);

			} else if (currentFileTimeStamp == readingStatus.getMonFileLastModTimeStamp()) {
				// 更新されてない場合.
				if (m_log.isDebugEnabled()) {
					String currentTs = new Timestamp(this.currentFileTimeStamp).toString();
					String lastMonitorTs = new Timestamp(this.readingStatus.getMonFileLastModTimeStamp()).toString();
					m_log.debug(methodName + DELIMITER + String.format(
							"skip to monitor because the file wasn't update."
									+ " monitorID=%s, file=[%s], timeStamp(current)=%s, timeStamp(lastMonitor)=%s",
							this.m_wrapper.getId(), this.readingStatus.getMonFileName(), currentTs, lastMonitorTs));
				}
				return onlyRotatedFile;
			} else {
				// ローテーションされた状態なので、次の監視時に最初から読み込む.
				String currentTs = new Timestamp(this.currentFileTimeStamp).toString();
				String lastMonitorTs = new Timestamp(this.readingStatus.getMonFileLastModTimeStamp()).toString();
				m_log.info(methodName + DELIMITER
						+ String.format(
								"skip to monitor because the file size becomes small."
										+ " it will be monitor from top of file next time."
										+ " monitorID=%s, file=[%s], timeStamp(current)=%s, timeStamp(lastMonitor)=%s",
								this.m_wrapper.getId(), this.readingStatus.getMonFileName(), currentTs, lastMonitorTs));
				this.fileChannel.position(0);
				// スキップフラグのリセット
				this.readingStatus.setToSkipRecord(false);
				this.readingStatus.setSkipSize(0);

				this.readingStatus.storePosition(this.currentFilesize, 0);
			}
		} catch (IOException e) {
			if (readSuccessFlg) {
				// バイナリファイルの読込自体は成功、その後の処理でエラー.
				m_log.warn(methodName + DELIMITER + e.getMessage());
				String[] args = { readingStatus.getMonFile().getPath() };
				// message.log.agent.1=ログファイル「{0}」
				// message.log.agent.4=ファイルの読み込みに失敗しました
				sendMessage(PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
						MessageConstant.MESSAGE_LOG_FAILED_TO_READ_FILE.getMessage(),
						MessageConstant.MESSAGE_BINARY_FILE.getMessage(args) + "\n" + e.getMessage());

				// エラーが発生したのでファイルクローズ
				this.closeFileChannel();
			} else {
				// バイナリファイルの読込でエラーのため、エラー分スキップして読込むようポジションをずらす
				m_log.warn(methodName + DELIMITER + e.getMessage());
				try {
					// 読込対象サイズの分だけ読込完了位置をずらして次回トライ.
					long nextposition = readingStatus.getPosition() + readedSize;
					this.fileChannel.position(nextposition);
					this.readingStatus.storePosition(this.currentFilesize, nextposition);
					m_log.debug(methodName + DELIMITER
							+ String.format(
									"success to set position of file channel for monitor on error."
											+ " monitorID=%s, file=[%s], nextposition=%d",
									this.m_wrapper.getId(), this.readingStatus.getMonFileName(), nextposition));
				} catch (IOException e1) {
					m_log.warn(methodName + DELIMITER + "set file-pointer error : " + e1.getMessage());
				}
			}
		}
		return onlyRotatedFile;
	}

	/**
	 * レコード分割方法取得<br>
	 * 
	 * @return 設定値不正等はERROR返却.
	 */
	private CutProcessType getCutProcessType(AgtBinaryCheckInfoResponse binaryInfo) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// 引数不正.
		if (binaryInfo == null) {
			m_log.warn(methodName + DELIMITER
					+ String.format("failed to get cut type. monitorID=%s, binaryInfo=null", m_wrapper.getId()));
			return CutProcessType.ERROR;
		}

		// 分割方法による判定.
		String cutType = binaryInfo.getCutType();
		if (cutType == null || cutType.isEmpty()) {
			m_log.warn(methodName + DELIMITER
					+ String.format("failed to get cut type. monitorID=%s, cutType=null(empty)", m_wrapper.getId()));
			return CutProcessType.ERROR;
		}
		if (BinaryConstant.CUT_TYPE_INTERVAL.equals(cutType)) {
			m_log.debug(methodName + DELIMITER
					+ String.format("get cut type. cutType(result)=%s, monitorID=%s, cutType(args)=%s",
							CutProcessType.INTERVAL, m_wrapper.getId(), cutType));
			return CutProcessType.INTERVAL;
		}
		if (!BinaryConstant.CUT_TYPE_LENGTH.equals(cutType)) {
			m_log.warn(methodName + DELIMITER
					+ String.format("failed to get cut type. monitorID=%s, cutType=%s", m_wrapper.getId(), cutType));
			return CutProcessType.ERROR;
		}

		// レコード長の種類から判定.
		String lengthType = binaryInfo.getLengthType();
		if (lengthType == null || lengthType.isEmpty()) {
			m_log.warn(methodName + DELIMITER + String
					.format("failed to get length type. monitorID=%s, lengthType=null(empty)", m_wrapper.getId()));
			return CutProcessType.ERROR;
		}
		if (BinaryConstant.LENGTH_TYPE_FIXED.equals(lengthType)) {
			m_log.debug(methodName + DELIMITER + String.format("get cut type. cutType=%s, monitorID=%s, lengthType=%s",
					CutProcessType.FIXED, m_wrapper.getId(), lengthType));
			return CutProcessType.FIXED;
		}
		if (BinaryConstant.LENGTH_TYPE_VARIABLE.equals(lengthType)) {
			m_log.debug(methodName + DELIMITER + String.format("get cut type. cutType=%s, monitorID=%s, lengthType=%s",
					CutProcessType.VARIABLE, m_wrapper.getId(), lengthType));
			return CutProcessType.VARIABLE;
		}

		// 変換できなかった場合は不正データ.
		m_log.warn(methodName + DELIMITER
				+ String.format("failed to length type. monitorID=%s, lengthType=%s", m_wrapper.getId(), lengthType));
		return CutProcessType.ERROR;
	}

	/**
	 * マネージャーへの送信.<br>
	 * <br>
	 * レコード毎に送信用xml形式に対応するDTOへの変換を行い、<br>
	 * 送信用サイズにあわせてレコードをまとめて送信する.<br>
	 * 
	 * @param fileDto
	 *            ファイル単位の監視結果情報.
	 * @param sendData
	 *            送信するレコードリスト
	 * @param matchPattern
	 *            フィルタ条件としてマッチしたパターン(nullはフィルタ条件なし)
	 * 
	 */
	private void sendManager(BinaryFile fileInfo, List<BinaryRecord> sendData) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// 送信用の監視情報をセット.
		// ファイル名に正規表現が使われていた場合、ファイル名が複数存在する場合があるためファイル単位でインスタンスを作成
		// [REST対応] MonitorInfo のプロパティのうち不要と思われる項目を除外している。
		//            後からやっぱり必要となった場合はコメントアウトを解除して、DTOへプロパティを追加する。
		AgtMonitorInfoRequest sendMonInfo = new AgtMonitorInfoRequest();
		sendMonInfo.setApplication(m_wrapper.monitorInfo.getApplication());
		// sendMonInfo.setCalendar(m_wrapper.monitorInfo.getCalendar());
		// sendMonInfo.setCalendarId(m_wrapper.monitorInfo.getCalendarId());
		// sendMonInfo.setChangeAnalysysRange(m_wrapper.monitorInfo.getChangeAnalysysRange());
		// sendMonInfo.setChangeApplication(m_wrapper.monitorInfo.getChangeApplication());
		// sendMonInfo.setChangeFlg(m_wrapper.monitorInfo.isChangeFlg());
		sendMonInfo.setCollectorFlg(m_wrapper.monitorInfo.getCollectorFlg());
		// sendMonInfo.setCorrelationCheckInfo(m_wrapper.monitorInfo.getCorrelationCheckInfo());
		// sendMonInfo.setCustomCheckInfo(m_wrapper.monitorInfo.getCustomCheckInfo());
		// sendMonInfo.setCustomTrapCheckInfo(m_wrapper.monitorInfo.getCustomTrapCheckInfo());
		// sendMonInfo.setDelayTime(m_wrapper.monitorInfo.getDelayTime());
		// sendMonInfo.setDescription(m_wrapper.monitorInfo.getDescription());
		sendMonInfo.setFacilityId(m_wrapper.monitorInfo.getFacilityId());
		// sendMonInfo.setFailurePriority(m_wrapper.monitorInfo.getFailurePriority());
		// sendMonInfo.setHttpCheckInfo(m_wrapper.monitorInfo.getHttpCheckInfo());
		// sendMonInfo.setHttpScenarioCheckInfo(m_wrapper.monitorInfo.getHttpScenarioCheckInfo());
		// sendMonInfo.setIntegrationCheckInfo(m_wrapper.monitorInfo.getIntegrationCheckInfo());
		// sendMonInfo.setItemName(m_wrapper.monitorInfo.getItemName());
		// sendMonInfo.setJmxCheckInfo(m_wrapper.monitorInfo.getJmxCheckInfo());
		// sendMonInfo.setLogcountCheckInfo(m_wrapper.monitorInfo.getLogcountCheckInfo());
		// sendMonInfo.setLogfileCheckInfo(m_wrapper.monitorInfo.getLogfileCheckInfo());
		// sendMonInfo.setLogFormatId(m_wrapper.monitorInfo.getLogFormatId());
		// sendMonInfo.setMeasure(m_wrapper.monitorInfo.getMeasure());
		sendMonInfo.setMonitorFlg(m_wrapper.monitorInfo.getMonitorFlg());
		sendMonInfo.setMonitorId(m_wrapper.monitorInfo.getMonitorId());
		sendMonInfo.setMonitorType(m_wrapper.monitorInfo.getMonitorType());
		sendMonInfo.setMonitorTypeId(m_wrapper.monitorInfo.getMonitorTypeId());
		sendMonInfo.setNotifyGroupId(m_wrapper.monitorInfo.getNotifyGroupId());
		sendMonInfo.setOwnerRoleId(m_wrapper.monitorInfo.getOwnerRoleId());
		// sendMonInfo.setPacketCheckInfo(m_wrapper.monitorInfo.getPacketCheckInfo());
		// sendMonInfo.setPerfCheckInfo(m_wrapper.monitorInfo.getPerfCheckInfo());
		// sendMonInfo.setPingCheckInfo(m_wrapper.monitorInfo.getPingCheckInfo());
		// sendMonInfo.setPluginCheckInfo(m_wrapper.monitorInfo.getPluginCheckInfo());
		// sendMonInfo.setPortCheckInfo(m_wrapper.monitorInfo.getPortCheckInfo());
		// sendMonInfo.setPredictionAnalysysRange(m_wrapper.monitorInfo.getPredictionAnalysysRange());
		// sendMonInfo.setPredictionApplication(m_wrapper.monitorInfo.getPredictionApplication());
		// sendMonInfo.setPredictionFlg(m_wrapper.monitorInfo.isPredictionFlg());
		// sendMonInfo.setPredictionMethod(m_wrapper.monitorInfo.getPredictionMethod());
		// sendMonInfo.setPredictionTarget(m_wrapper.monitorInfo.getPredictionTarget());
		// sendMonInfo.setProcessCheckInfo(m_wrapper.monitorInfo.getProcessCheckInfo());
		sendMonInfo.setRegDate(m_wrapper.monitorInfo.getRegDate());
		sendMonInfo.setRegUser(m_wrapper.monitorInfo.getRegUser());
		sendMonInfo.setRunInterval(m_wrapper.monitorInfo.getRunInterval());
		sendMonInfo.setScope(m_wrapper.monitorInfo.getScope());
		// sendMonInfo.setSnmpCheckInfo(m_wrapper.monitorInfo.getSnmpCheckInfo());
		// sendMonInfo.setSqlCheckInfo(m_wrapper.monitorInfo.getSqlCheckInfo());
		// sendMonInfo.setTrapCheckInfo(m_wrapper.monitorInfo.getTrapCheckInfo());
		sendMonInfo.setTriggerType(m_wrapper.monitorInfo.getTriggerType());
		sendMonInfo.setUpdateDate(m_wrapper.monitorInfo.getUpdateDate());
		sendMonInfo.setUpdateUser(m_wrapper.monitorInfo.getUpdateUser());
		// sendMonInfo.setWinEventCheckInfo(m_wrapper.monitorInfo.getWinEventCheckInfo());
		// sendMonInfo.setWinServiceCheckInfo(m_wrapper.monitorInfo.getWinServiceCheckInfo());
		// sendMonInfo.setSdmlMonitorTypeId(m_wrapper.monitorInfo.getSdmlMonitorTypeId());

		AgtBinaryCheckInfoRequest binaryCheckInfo = new AgtBinaryCheckInfoRequest();
		binaryCheckInfo.setBinaryfile(m_wrapper.monitorInfo.getBinaryCheckInfo().getBinaryfile());
		binaryCheckInfo.setCollectType(m_wrapper.monitorInfo.getBinaryCheckInfo().getCollectType());
		binaryCheckInfo.setCutType(m_wrapper.monitorInfo.getBinaryCheckInfo().getCutType());
		binaryCheckInfo.setDirectory(m_wrapper.monitorInfo.getBinaryCheckInfo().getDirectory());
		binaryCheckInfo.setErrMsg(m_wrapper.monitorInfo.getBinaryCheckInfo().getErrMsg());
		binaryCheckInfo.setFileHeadSize(m_wrapper.monitorInfo.getBinaryCheckInfo().getFileHeadSize());
		binaryCheckInfo.setFileName(m_wrapper.monitorInfo.getBinaryCheckInfo().getFileName());
		binaryCheckInfo.setHaveTs(m_wrapper.monitorInfo.getBinaryCheckInfo().getHaveTs());
		binaryCheckInfo.setLengthType(m_wrapper.monitorInfo.getBinaryCheckInfo().getLengthType());
		binaryCheckInfo.setLittleEndian(m_wrapper.monitorInfo.getBinaryCheckInfo().getLittleEndian());
		binaryCheckInfo.setMonitorId(m_wrapper.monitorInfo.getBinaryCheckInfo().getMonitorId());
		binaryCheckInfo.setMonitorTypeId(m_wrapper.monitorInfo.getBinaryCheckInfo().getMonitorTypeId());
		binaryCheckInfo.setRecordHeadSize(m_wrapper.monitorInfo.getBinaryCheckInfo().getRecordHeadSize());
		binaryCheckInfo.setRecordSize(m_wrapper.monitorInfo.getBinaryCheckInfo().getRecordSize());
		binaryCheckInfo.setSizeLength(m_wrapper.monitorInfo.getBinaryCheckInfo().getSizeLength());
		binaryCheckInfo.setSizePosition(m_wrapper.monitorInfo.getBinaryCheckInfo().getSizePosition());
		binaryCheckInfo.setTagType(m_wrapper.monitorInfo.getBinaryCheckInfo().getTagType());
		binaryCheckInfo.setTsPosition(m_wrapper.monitorInfo.getBinaryCheckInfo().getTsPosition());
		binaryCheckInfo.setTsType(m_wrapper.monitorInfo.getBinaryCheckInfo().getTsType());
		sendMonInfo.setBinaryCheckInfo(binaryCheckInfo);

		AgtRunInstructionInfoRequest sendJobInfo = m_wrapper.runInstructionInfoReq;

		// 送信用のsys_logの情報をセット.
		AgtMessageInfoRequest logmsg = new AgtMessageInfoRequest();
		logmsg.setGenerationDate(HinemosTime.getDateInstance().getTime());
		logmsg.setHostName(Agent.getAgentInfoRequest().getHostname());

		// 送信用にファイル単位の情報をセット.
		if (readingStatus.getParentDirRS().getMonDir() != null) {
			// ディレクトリ名.
			sendMonInfo.getBinaryCheckInfo().setDirectory(readingStatus.getParentDirRS().getMonDirName());
		}
		if (readingStatus.getMonFileName() != null) {
			// ファイル名.
			sendMonInfo.getBinaryCheckInfo().setBinaryfile(readingStatus.getMonFileName());
		}
		AgtBinaryFileDTORequest fileDto = fileInfo.getDTO();

		// 送信ループ処理用の変数初期化.
		int maxSendSize = BinaryMonitorConfig.getSendSize();
		AgtBinaryRecordDTORequest dto = null;
		List<AgtBinaryRecordDTORequest> sendList = new ArrayList<>();
		int nextSendSize = 0;
		int sendSize = 0;

		// レコードを送信サイズでまとめて送信.
		for (BinaryRecord sendRecord : sendData) {
			// 対象バイナリを送信用に変換.
			dto = sendRecord.getDTO();
			sendSize = nextSendSize;
			nextSendSize = sendSize + sendRecord.getAlldata().size();

			// 送信サイズが最大送信サイズを超える前に送信(ただし最低1レコードから送信).
			if (nextSendSize > maxSendSize && !sendList.isEmpty()) {
				m_log.debug(
						methodName + DELIMITER
								+ String.format(
										"prepared to send manager. sendSize=%dbyte, monitorID=%s, monitorFile=[%s]",
										sendSize, m_wrapper.getId(), readingStatus.getMonFileName()));
				// sys_logの情報をセット.
				logmsg.setMessage(sendList.get(0).getOxStr());
				// 送信タスクとして追加.
				BinaryForwarder.getInstance().add(fileDto, sendList, logmsg, sendMonInfo, sendJobInfo);
				// 送信用リストと送信サイズを初期化.
				sendList = new ArrayList<>();
				nextSendSize = sendRecord.getAlldata().size();
			}

			// 次回送信分として追加.
			sendList.add(dto);
		}

		// 残分をまとめて送信.
		if (!sendList.isEmpty()) {
			sendSize = nextSendSize;
			m_log.debug(methodName + DELIMITER
					+ String.format(
							"prepared to send last data manager. sendSize=%dbyte, monitorID=%s, monitorFile=[%s]",
							sendSize, m_wrapper.getId(), readingStatus.getMonFileName()));
			// sys_logの情報をセット.
			logmsg.setMessage(sendList.get(0).getOxStr());
			// 送信タスクとして追加.
			BinaryForwarder.getInstance().add(fileDto, sendList, logmsg, sendMonInfo, sendJobInfo);
		}

	}

	/**
	 * ローテーションチェック.<br>
	 * <br>
	 * ファイルの先頭バイナリでローテーションチェック.<br>
	 * 
	 * @return true:ローテーション後オープン不可<br>
	 * @throws IOException
	 * 
	 */
	private boolean checkRotation() throws IOException {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");

		// ローテーションチェック.
		if (!rotateFlag) {
			// ローテーションチェック未実施の場合.
			m_log.debug(methodName + DELIMITER + readingStatus.getMonFile().getPath() + " check log rotation");
			if (this.callRotatedCheck()) {
				m_log.debug(
						methodName + DELIMITER + readingStatus.getMonFile().getPath() + " check first part of file");
				// 前回チェック時点から一定時間(AgentProperty)経過している場合はファイル変更チェック.
				this.checkPrefix();
			} else {
				m_log.debug(
						methodName + DELIMITER + readingStatus.getMonFile().getPath() + " skip to check log rotation");
			}

			// readingStatus生成時点からローテートされている場合.
			if (rotateFlag) {
				m_log.info(methodName + DELIMITER + readingStatus.getMonFile().getPath() + " : file changed");
				this.closeFileChannel();
				this.rotate();
				// ファイルオープン
				if (!openFile()) {
					m_log.warn(methodName + DELIMITER
							+ String.format("failed to open file channel. file=[%s]", readingStatus.getMonFileName()));
					return true;
				}
				this.currentFilesize = fileChannel.size();
				this.currentFileTimeStamp = readingStatus.getMonFile().lastModified();
			}
		}

		return false;
	}

	/**
	 * ファイル更新停止チェック.<br>
	 * <br>
	 * ファイルが一定秒数以上変更されていない状態かチェックする.<br>
	 * ※先頭バイナリ変わっている場合(ローテーション)は別ファイルであること確定なので先にチェックすること.<br>
	 * 
	 * @return true:長時間更新なし
	 * 
	 */
	private boolean unchangedLongTime() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// 監視対象ファイルの最終更新時間チェック.
		if ((!rotateFlag) && readingStatus.getMonFileLastModTimeStamp() == currentFileTimeStamp) {
			// ローテーションされていない状態(ファイルの先頭バイナリが同一)で
			// 監視対象ファイルがreadingStatus生成時点から一定秒数以上更新されていない場合.
			int runInterval = this.readingStatus.getParentDirRS().getParentMonRS().getParentRootRS().getRunInterval();
			if ((++m_unchanged_stats * runInterval) > BinaryMonitorConfig.getUnchangedStatsPeriod()) {
				// 監視対象外としてクローズ.
				readingStatus.closeFileRS();
				// アプリケーションの更新対象ファイルが切り替わっている状態だと判定して次の監視対象ファイルに移る.
				m_log.info(methodName + DELIMITER + String.format(
						"close monitor because unchanged long time. file=[%s]", readingStatus.getMonFileName()));
				return true;
			}
		} else {
			// ファイル更新してるので変更なし回数初期化.
			this.m_unchanged_stats = 0;
		}
		return false;
	}

	/**
	 * ローテーション処理.<br>
	 * <br>
	 * ローテーションで生成された新規bkファイルのRSを生成してファイル出力し.<br>
	 * 現在の監視対象ファイルのRSを更新する.<br>
	 * ローテーションフラグが立っている場合は、ファイル読込時にローテーションで生成されたファイルも読込む.
	 * 
	 */
	protected void rotate() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// ローテーション前のRSを退避.
		long oldPosition = this.readingStatus.getPosition();
		long oldLastModTimeStamp = this.readingStatus.getMonFileLastModTimeStamp();
		List<Byte> oldPrefix = this.readingStatus.getPrefix();
		String oldPrefixStr = this.readingStatus.getPrefixString();

		// ファイルRSをローテーションさせる.
		this.readingStatus.rotate();

		// 親のRSクラスを取得.
		MonitorReadingStatus parentMonRS = this.readingStatus.getParentDirRS().getParentMonRS();
		Map<String, DirectoryReadingStatus> parentDirMap = parentMonRS.getDirectoryRSMap();

		// 現状存在する監視対象ファイル一覧.
		List<File> allMonFile = new ArrayList<File>();
		FileUtil.addFileList(parentMonRS.getMonRootDirectory(), allMonFile,
				this.readingStatus.getParentDirRS().getParentMonRS().getFilename(),
				BinaryMonitorConfig.getFileMaxFiles(), false);
		List<File> noRsMonFile = new ArrayList<File>(allMonFile);
		List<File> allRsMonDir = new ArrayList<File>();

		// 後続の監視処理でローテーション済のファイルから取得するためのリスト.
		this.rotatedRSMap = new TreeMap<String, FileReadingStatus>();
		int createdCount = 0;

		// 現在作成済のファイルRSに存在するか確認.
		// ※存在していた場合はAgent停止中にローテーション、存在しない場合はRS作成⇒監視までの間にローテーション.
		for (DirectoryReadingStatus dirRS : parentDirMap.values()) {
			// 後続処理用に監視対象ディレクトリをRS作成済として保存.
			allRsMonDir.add(dirRS.getMonDir());
			for (FileReadingStatus fileRS : dirRS.getFileRSMap().values()) {
				this.setRotatedFileRs(fileRS, oldPrefix, oldPrefixStr, oldLastModTimeStamp, oldPosition);
				if (allMonFile.contains(fileRS.getMonFile())) {
					// 存在する場合はRS作成済として除外.
					noRsMonFile.remove(fileRS.getMonFile());
					createdCount++;
				}
			}
		}
		m_log.debug(
				methodName + DELIMITER
						+ String.format(
								"modified reading status for rotation. count=%d, monitorId=%s, monitorFile=[%s]",
								createdCount, m_wrapper.getId(), this.readingStatus.getMonFileName()));

		// RSなしの監視ファイル⇒RS作成～監視処理までの間にローテーションで作成されたファイル
		File parentDir = null;
		if (noRsMonFile != null && !noRsMonFile.isEmpty()) {
			// RSなしの監視ファイル毎.
			m_log.debug(methodName + DELIMITER
					+ String.format(
							"prepared to create reading status for rotation. count=%d, monitorId=%s, monitorFile=[%s]",
							noRsMonFile.size(), m_wrapper.getId(), this.readingStatus.getMonFileName()));
			for (File rotatedMonFile : noRsMonFile) {
				// 親ディレクトリを設定.
				if (rotatedMonFile.getParentFile() != null) {
					parentDir = rotatedMonFile.getParentFile();
				} else {
					parentDir = this.readingStatus.getParentDirRS().getParentMonRS().getMonRootDirectory();
				}
				if (allRsMonDir.contains(parentDir)) {
					// ディレクトリRSが作成済の場合はファイルRSのみ作成.
					DirectoryReadingStatus parentDirRS = parentDirMap.get(parentDir.getAbsolutePath());
					FileReadingStatus tmpFileRS = new FileReadingStatus(rotatedMonFile, parentDirRS);
					boolean created = this.setRotatedFileRs(tmpFileRS, oldPrefix, oldPrefixStr, oldLastModTimeStamp,
							oldPosition);
					if (created) {
						parentDirRS.getFileRSMap().put(tmpFileRS.getMonFileName(), tmpFileRS);
					}
				} else {
					// ディレクトリRSも未作成なのでディレクトリRSから作成.
					DirectoryReadingStatus tmpDirRS = new DirectoryReadingStatus(parentDir, parentMonRS);
					parentDirMap.put(tmpDirRS.getMonDirName(), tmpDirRS);
					// 作成したファイルRS毎.
					for (FileReadingStatus fileRS : tmpDirRS.getFileRSMap().values()) {
						if (fileRS.getMonFile().equals(rotatedMonFile)) {
							this.setRotatedFileRs(fileRS, oldPrefix, oldPrefixStr, oldLastModTimeStamp, oldPosition);
							break;
						}

					}
				}
			}
		}

	}

	/**
	 * ローテートしたファイルのファイルRSをセット.<br>
	 * <br>
	 * 前回監視から今回監視までの間に生成されたファイルかどうか判定してファイルRSをセットする.
	 */
	private boolean setRotatedFileRs(FileReadingStatus fileRS, List<Byte> oldPrefix, String oldPrefixStr,
			long oldLastModTimeStamp, long oldPosition) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		String key = null;
		if (fileRS.getMonFileName().equals(this.readingStatus.getMonFileName())) {
			// 監視対象ファイル→ローテーションファイルではないのでスキップ.
			m_log.debug(methodName + DELIMITER
					+ String.format(
							"skip to add rotated reading status. monitorId=%s, rotatedFile=[%s], monitorFile=[%s]",
							this.m_wrapper.getId(), fileRS.getMonFileName(), this.readingStatus.getMonFileName()));
			return false;
		} else if (BinaryUtil.forwardMatch(fileRS.getPrefix(), oldPrefix)) {
			// 先頭バイナリが一致 ⇒ 前に監視してたファイル.
			fileRS.setPosition(oldPosition);
			fileRS.outputRS();
			key = Long.toString(fileRS.getMonFileLastModTimeStamp()) + fileRS.getMonFileName();
			this.rotatedRSMap.put(key, fileRS);
			m_log.debug(methodName + DELIMITER
					+ String.format("added rotated reading status. monitorId=%s, file=[%s], prefix=%s, oldPrefix=%s",
							this.m_wrapper.getId(), fileRS.getMonFileName(), fileRS.getPrefixString(), oldPrefixStr));
			return true;
		} else if (fileRS.getMonFileLastModTimeStamp() >= oldLastModTimeStamp) {
			// 前回の最終更新日時より新しい更新日時 ⇒ 前回監視から今回監視までの間に作成されたファイル.
			fileRS.setPosition(0);
			fileRS.outputRS();
			key = m_wrapper.getId() + Long.toString(fileRS.getMonFileLastModTimeStamp()) + fileRS.getMonFileName();
			this.rotatedRSMap.put(key, fileRS);
			m_log.debug(methodName + DELIMITER
					+ String.format("added rotated reading status. monitorId=%s, file=[%s], regdate=%d, oldRegDate=%d",
							this.m_wrapper.getId(), fileRS.getMonFileName(), fileRS.getMonFileLastModTimeStamp(),
							oldLastModTimeStamp));
			return true;
		} else {
			// ローテーションと関係ないファイルなのでそのまま.
			m_log.debug(methodName + DELIMITER
					+ String.format("skip to add rotated reading status. monitorId=%s, file=[%s], reading=%s",
							this.m_wrapper.getId(), fileRS.getMonFileName(), fileRS.getReadingStatus()));
			return false;
		}
	}

	/**
	 * パターンセット.<br>
	 * <br>
	 * 引数のリストに有効なフィルタ条件のみを設定する.
	 */
	private void setFilterInfoList(List<AgtBinaryPatternInfoResponse> matchInfoList2) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// 監視設定から全量取得.
		List<AgtBinaryPatternInfoResponse> matchInfoList = m_wrapper.monitorInfo.getBinaryPatternInfo();
		AgtBinaryPatternInfoResponse filter = null;

		if (matchInfoList != null && !matchInfoList.isEmpty()) {
			AgtBinaryPatternInfoResponse matchInfo = null;
			// 取得したフィルタについて有効か判定.
			for (int i = 0; i < matchInfoList.size(); i++) {
				filter = matchInfoList.get(i);
				if (filter.getValidFlg().booleanValue()) {
					matchInfo = matchInfoList.get(i);
					// 有効なフィルタ条件をセットする.
					matchInfoList2.add(matchInfo);
				} else {
					m_log.debug(
							methodName + DELIMITER + String.format("invalid filter. monitorID=%s, filterString=[%s]",
									m_wrapper.getId(), filter.getGrepString()));
				}
			}
		}
	}

	/**
	 * ローテーションチェック実施すべきか判定.
	 * 
	 * @return true:実施、false:実施対象外
	 */
	public boolean callRotatedCheck() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		// ファイルサイズが存在する.
		boolean call = (this.currentFilesize > 0);

		// ローテーションチェック実行間隔が設定されている.
		int propertyPeriod = BinaryMonitorConfig.getFirstPartDataCheckPeriod();
		call = call && (propertyPeriod > 0);

		// ローテーションチェック実行間隔が経過している.
		long interval = HinemosTime.currentTimeMillis() - this.m_lastDataCheck;
		call = call && (interval > propertyPeriod);

		m_log.debug(methodName + DELIMITER
				+ String.format(
						"get flag to call rotated check. call=%b, currentFilesize=%d, propertyPeriod=%d, interval=%d",
						call, this.currentFilesize, propertyPeriod, interval));

		return call;
	}

	/**
	 * 監視処理終了時のファイルRS等更新処理(増分のみ監視向け).
	 * 
	 * @throws IOException
	 */
	private void updateFileRS(boolean onlyRotated, BinaryFile fileResult, long readedSize, int skippedSize) throws IOException {
		// ローテーションファイルのみを読込んだ場合は飛ばす.
		if (onlyRotated) {
			return;
		}

		this.readedSize = readedSize;

		// 次回監視用に読込開始位置をずらす.（現在のポジションは不完全なレコードも含んでいる場合があるので補正する）
		// ヘッダーサイズ と レコードスキップサイズ の取り扱いには注意
		if (m_log.isDebugEnabled()) {
			m_log.debug("updateFileRS () " + DELIMITER +
					"  org setPosition=" + this.fileChannel.position() +
					" startPosition=" + this.readingStatus.getPosition() +
					" readSize=" + this.readedSize +
					" skippedSize=" + skippedSize +
					"  file=" + readingStatus.getStoreFileRSFile().getName());
		}
		long setPosition = 0;
		if (skippedSize > 0) {
			// 監視設定更新などによる読み飛ばしがあった場合
			// 次回の読込み開始位置＝ 今回の読込開始位置 + 無視したサイズ(実際にスキップしたサイズ)＋ レコード読込みサイズ とする
			setPosition = this.readingStatus.getPosition() + skippedSize + this.readedSize;

			if (skippedSize >= this.readingStatus.getSkipSize()) {
				// 読み飛ばしが完了した場合
				if (m_log.isDebugEnabled()) {
					m_log.debug("updateFileRS () " + DELIMITER + " setPosition is included record skip size . SkipSize=" + this.readingStatus.getSkipSize());
				}
				// スキップフラグのリセット
				this.readingStatus.setToSkipRecord(false);
				this.readingStatus.setSkipSize(0);
			} else {
				// 読み飛ばしが完了していない場合(読み飛ばしの部分がが大きい場合)
				if (m_log.isDebugEnabled()) {
					m_log.debug("updateFileRS () " + DELIMITER + " readsize is all skipped. SkipSize=" + this.readingStatus.getSkipSize());
				}
				// skipSizeの再設定
				// 読み飛ばしきれなかったレコードを次回読込の際スキップする
				this.readingStatus.setSkipSize(this.readingStatus.getSkipSize() - skippedSize);
			}
		} else {
			//通常は 次回の読込み開始位置＝ 今回の読込み開始位置 ＋ レコード読込みサイズ とする　
			// ただし、ヘッダーの読み飛ばしを行っている場合はその分を加味する
			setPosition = this.readingStatus.getPosition() + this.readedSize;
			if (this.readingStatus.getPosition() < fileResult.getFileHeaderSize()) {
				setPosition = setPosition + fileResult.getFileHeaderSize() - this.readingStatus.getPosition();
			}
		}
		if (m_log.isDebugEnabled()) {
			m_log.debug("updateFileRS () " + DELIMITER + "  upd setPosition=" + setPosition + " file=" + readingStatus.getStoreFileRSFile().getName());
		}
		this.fileChannel.position(setPosition);

		// 読込状態の更新.
		this.readingStatus.storeRS(this.currentFilesize, this.fileChannel.position(), this.currentFileTimeStamp,
				this.currentPrefix);
	}

	/** 一時ファイル名取得 */
	private String getTmpFileName() {
		return TEMPORARY_PREFIX + this.tmpThreadId + "_" + this.readingStatus.getMonFile().getName();
	}

	/** 一時ファイルの名前にマッチするか */
	public static boolean matchTmpFileName(String fileName) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		if (fileName.matches(TEMPORARY_PREFIX + ".*_.*")) {
			m_log.debug(
					methodName + DELIMITER + String.format("match to name of temporary file. fileName=[%s]", fileName));
			return true;
		}
		m_log.debug(methodName + DELIMITER
				+ String.format("don't match to name of temporary file. fileName=[%s]", fileName));
		return false;
	}

	/** 一時ファイル名から監視ファイル名を取得 */
	public static String getMonNameFromTmp(String tmpFileName) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		if (!matchTmpFileName(tmpFileName)) {
			m_log.warn(methodName + DELIMITER
					+ String.format("don't match to name of temporary file. fileName=[%s]", tmpFileName));
			return null;
		}

		// 接頭語を除去.
		int removeSize = TEMPORARY_PREFIX.length();
		String monitorFileName = tmpFileName.substring(removeSize);
		// "スレッドID_"を除去.
		removeSize = monitorFileName.indexOf("_") + 1;
		monitorFileName = monitorFileName.substring(removeSize);

		// "tmp_thread33_.json"みたいなファイルは対象外
		if (monitorFileName.isEmpty() || monitorFileName.charAt(0) == '.') {
			m_log.info(methodName + DELIMITER
					+ String.format("don't match to name of temporary file. fileName=[%s]", tmpFileName));
			return null;
		}

		m_log.debug(methodName + DELIMITER + String.format(
				"get name of file to monitor from temporary file name." + " name(monitor)=[%s], name(temporary)=[%s]",
				monitorFileName, tmpFileName));

		return monitorFileName;
	}

	// 以下各フィールドsetter.
	/** 監視設定・監視結果 */
	public void setMonitor(MonitorInfoWrapper monitorInfo) {
		this.m_wrapper = monitorInfo;
	}

	/** 読込サイズ(レコード分割前) */
	public void setReadedSize(long readedSize) {
		this.readedSize = readedSize;
	}

	/** 増加分読み込み成功フラグ */
	public void setReadSuccessFlg(boolean readSuccessFlg) {
		this.readSuccessFlg = readSuccessFlg;
	}

	/** 一時ファイル命名用スレッドID */
	public void setTmpThreadId(String tmpThreadId) {
		this.tmpThreadId = tmpThreadId;
	}

	/** ローテーションのため監視処理スキップ(true:監視処理スキップ) */
	protected void setSkipForRotate(boolean skipForRotate) {
		this.skipForRotate = skipForRotate;
	}

	/** ローテーションフラグ(true:ローテートあり) */
	protected void setRotateFlag(boolean rotateFlag) {
		this.rotateFlag = rotateFlag;
	}

	// 以下各フィールドgetter.
	/** 監視対象ファイルの読込状態 */
	public FileReadingStatus getReadingStatus() {
		return readingStatus;
	}

	/** 監視対象ファイルとの接続 */
	public FileChannel getFileChannel() {
		return fileChannel;
	}

	/** 監視設定・監視結果 */
	public MonitorInfoWrapper getM_wrapper() {
		return m_wrapper;
	}

	/** 読込サイズ(レコード分割前) */
	public long getReadedSize() {
		return readedSize;
	}

	/** ローテーションフラグ(true:ローテートあり) */
	public boolean isRotateFlag() {
		return rotateFlag;
	}

	/** ローテーション処理で新規作成されたファイルRS */
	public Map<String, FileReadingStatus> getRotatedRSMap() {
		return rotatedRSMap;
	}

}
