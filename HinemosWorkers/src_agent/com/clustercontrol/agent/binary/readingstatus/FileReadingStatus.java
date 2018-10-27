/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.binary.readingstatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.binary.BinaryMonitorConfig;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.util.BinaryUtil;

/**
 * 監視対象ファイル読込状態管理クラス<br>
 * <br>
 * オブジェクトは監視対象のファイル毎に作成される.<br>
 */
public class FileReadingStatus {

	// クラス共通フィールド.
	/** ロガー */
	private static Log log = LogFactory.getLog(FileReadingStatus.class);

	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	// ファイル毎の読込状態に関する情報.
	/** ディレクトリ毎の読込状態に関する情報 */
	private DirectoryReadingStatus parentDirRS;

	/** 監視対象ファイル */
	private final File monFile;

	/** ファイル読込状態を出力するファイル */
	private File storeFileRSFile;

	/** 読込状態 */
	private boolean readingFlag;

	/** 読み込んでいるファイルの先頭バイナリ */
	private List<Byte> prefix = new ArrayList<Byte>();

	/** 初期化済 */
	private boolean initialized;

	/** 比較用先頭バイナリのサイズ */
	private int prefixSize = 0;

	/** レコードスキップフラグ */
	private boolean toSkipRecord = false;

	/** レコードスキップサイズ （ファイルヘッダーは除外しているので注意） */
	private long skipSize = 0;

	// ファイル出力対象の項目.
	/** 監視対象ファイル名(絶対パス)(コメント出力)(マップキー) */
	private String monFileName;

	/** 読込状態 */
	private String readingStatus;

	/** ファイルハンドラのポジション */
	private long position = 0;

	/** ファイルサイズ(ReadingStatus作成時点)(byte) */
	private long prevSize = 0;

	/** 監視対象ファイルの最終更新タイムスタンプ(監視処理完了時に更新) */
	private long monFileLastModTimeStamp;

	/** 初回監視実行済フラグ(true:実行済,false:未実行) */
	private Boolean didFirstRun;

	/** 読み込んでいるファイルの先頭バイナリ文字列(16進数表記) */
	private String prefixString;

	// ファイル全体監視向けフィールド.
	/** スレッド開始時の最終更新タイムスタンプ(RSのrefresh時に更新)(ファイル全体監視向け) */
	private long lastModTimeStampByThread = 0;

	/** ファイル全体監視実行フラグ(true:実行,false:監視skip) */
	private boolean runWholeFileMonitor = false;

	/** 監視実行中フラグ(ファイル全体監視向け) */
	private Boolean runMonitor;

	/**
	 * コンストラクタ.<br>
	 * 
	 * @param monFile
	 *            監視対象のファイル
	 * @param parentDirRS
	 *            親となる読込情報
	 */
	public FileReadingStatus(File monFile, DirectoryReadingStatus parentDirRS) {
		this.monFile = monFile;
		this.monFileName = monFile.getAbsolutePath();
		this.monFileName = monFile.getAbsolutePath();
		this.parentDirRS = parentDirRS;

		this.initFileRS();
	}

	/**
	 * FileRSの値を初期化.<br>
	 * <br>
	 * 前回出力済ファイルから取得もしくは初期値を設定.<br>
	 * 
	 * @param refresh
	 *            前回ファイル出力した情報を更新する場合.
	 * 
	 */
	public void initFileRS() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		File rstatus = new File(parentDirRS.getStoreFileRSDir(), monFile.getName() + ".json");
		log.debug(methodName + DELIMITER + "rstatus = " + rstatus.toString());
		this.storeFileRSFile = rstatus;

		// プロパティセット前に初期化.
		this.skipSize = 0;
		this.toSkipRecord = false;

		Properties props = new Properties();
		boolean toCreate = false;

		if (rstatus.exists()) {
			// 前回ファイル出力した読込情報が存在する場合.
			try (FileInputStream fi = new FileInputStream(rstatus)) {
				// ファイルから各プロパティを読込む.
				props.load(fi);
				this.position = Long.parseLong(RootReadingStatus.getPropertyValue(props, RootReadingStatus.position));
				this.prevSize = Long.parseLong(RootReadingStatus.getPropertyValue(props, RootReadingStatus.prevSize));
				this.monFileLastModTimeStamp = Long.parseLong(
						RootReadingStatus.getPropertyValue(props, RootReadingStatus.monFileLastModTimeStamp));
				this.lastModTimeStampByThread = Long.parseLong(
						RootReadingStatus.getPropertyValue(props, RootReadingStatus.lastModTimeStampByThread));
				this.didFirstRun = Boolean
						.valueOf(RootReadingStatus.getPropertyValue(props, RootReadingStatus.didFirstRun));
				this.runMonitor = Boolean
						.valueOf(RootReadingStatus.getPropertyValue(props, RootReadingStatus.runMonitor));
				this.prefixString = RootReadingStatus.getPropertyValue(props, RootReadingStatus.prefixString);
				// 読込状態は前回監視時点からファイルの更新時刻が変更されてたらopen.
				if (this.monFileLastModTimeStamp != monFile.lastModified()) {
					this.readingStatus = RootReadingStatus.RS_OPEN_STRING;
				} else {
					this.readingStatus = RootReadingStatus.getPropertyValue(props, RootReadingStatus.readingStatus);
				}

				this.setRunWholeFileMonitor();

				this.initialized = true;
			} catch (FileNotFoundException e) {
				log.debug(methodName + DELIMITER + e.getMessage(), e);
			} catch (IOException e) {
				log.warn(methodName + DELIMITER + e.getMessage(), e);
			} catch (InvalidSetting | NumberFormatException e) {
				log.warn(methodName + DELIMITER + e.getMessage(), e);
				// RSファイルが壊れてるので削除.
				if (!rstatus.delete()) {
					log.warn(methodName + DELIMITER + "failed to delete file = [" + rstatus.getAbsolutePath() + "]");
				} else {
					log.info(methodName + DELIMITER + "deleted file = [" + rstatus.getAbsolutePath() + "]");
					// 削除したので新規作成.
					toCreate = true;
				}
			}
		} else {
			// ファイル出力された読込情報が存在しない場合は、実際にファイルを読込んで情報出力.
			toCreate = true;
		}

		// fileRS新規作成.
		if (toCreate) {
			log.debug(methodName + DELIMITER + String.format("fileRS isn't exist. monitorID=%s ,rs=[%s]",
					this.parentDirRS.getParentMonRS().getMonitorID(), rstatus.toString()));
			this.readingStatus = RootReadingStatus.RS_OPEN_STRING;

			// 前回読込ファイルサイズ
			this.prevSize = monFile.length();

			// 読込完了位置.
			this.position = 0;
			// 前回ファイルRS作成日時を取得して監視設定更新日時と比較.
			if (!(this.parentDirRS.getParentMonRS().getLastUpdateRs() >= this.parentDirRS.getParentMonRS()
					.getUpdateDate().longValue())) {
				// 監視設定の更新日時より前の場合は、前回は監視対象ではなかったファイルなので、
				// 現時点のファイルサイズより後の増分を読込むように、レコード分割時に位置調整するフラグを立てる.
				this.toSkipRecord = true;
				if( log.isDebugEnabled() ){
					log.debug(methodName + DELIMITER
							+ String.format(
									"toSkipRecord is true . monitorID=%s ,rs=[%s]",
									this.parentDirRS.getParentMonRS().getMonitorID(), rstatus.toString()));
				}
			}
			if( log.isDebugEnabled() ){
				log.debug(methodName + DELIMITER
						+ String.format(
								"init rerading position. position=%d, lastUpdateRsTime=%d, monitorInfoUpdated=%d, monitorID=%s ,rs=[%s]",
								this.position, this.parentDirRS.getParentMonRS().getLastUpdateRs(),
								this.parentDirRS.getParentMonRS().getUpdateDate(),
								this.parentDirRS.getParentMonRS().getMonitorID(), rstatus.toString()));
			}

			// 最終更新タイムスタンプ取得.
			this.monFileLastModTimeStamp = monFile.lastModified();
			// 実際にファイルを読込んで先頭バイナリを文字列変換して格納.
			this.prefixString = this.getPrefixString();
			// ファイル全体監視の監視中フラグはBinaryMonitorから更新する.
			this.runMonitor = Boolean.valueOf(false);
			// 初回なので必ず未実行とする.
			this.didFirstRun = Boolean.valueOf(false);
			// スレッド実行時のタイムスタンプは初回なので0.
			this.monFileLastModTimeStamp = 0L;
			this.setRunWholeFileMonitor();

			this.initialized = true;
		}

		// クラス保持している値のファイル出力.
		this.outputRS();

		// ファイル出力しないフィールドの初期化.
		if (this.prefixString == null || this.prefixString.isEmpty()) {
			this.prefix = new ArrayList<Byte>();
			log.debug(methodName + DELIMITER + "prefixString is empty");
		} else {
			this.prefix = BinaryUtil.stirngToList(prefixString, 1, 1);
			log.debug(methodName + DELIMITER + "prefix size = " + this.prefix.size());
		}
		this.prefixSize = this.prefix.size();
		this.readingFlag = RootReadingStatus.rsToFlag(this.readingStatus);

	}

	/**
	 * ファイル全体監視の実行是非を判定.<br>
	 */
	private void setRunWholeFileMonitor() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");

		// ファイル全体監視の実行是非を判定(前回スレッド実行時点の更新時刻と現在のファイル更新時刻を比較).
		long lastModified = monFile.lastModified();
		if (this.lastModTimeStampByThread == 0) {
			// 初回監視なので実行なし.
			this.runWholeFileMonitor = false;
		} else if (this.lastModTimeStampByThread == lastModified) {
			// ファイル書込みが完了してる状態.
			if (this.monFileLastModTimeStamp != lastModified) {
				// 前回監視時点から更新されてるので実行.
				this.runWholeFileMonitor = true;
			} else if (!this.didFirstRun) {
				// 初回監視未実施の場合は実行対象
				this.runWholeFileMonitor = true;
			} else {
				// 更新なし.
				this.runWholeFileMonitor = false;
			}
		} else {
			// ファイル書込み中の可能性があるため実行なし.
			this.runWholeFileMonitor = false;
		}
		log.debug(methodName + DELIMITER
				+ String.format(
						"set runWholeFileMonitor. value=%b,"
								+ " lastModTimeStampByThread=%d, lastModified=%d, lastMonitorLastModified=%d, didFirstRun=%b",
						this.runWholeFileMonitor, this.lastModTimeStampByThread, lastModified,
						this.monFileLastModTimeStamp, this.didFirstRun));
		this.lastModTimeStampByThread = monFile.lastModified();
	}

	/**
	 * ファイルRS出力.<br>
	 */
	public void outputRS() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");

		// ディレクトリない場合は作成.
		if (!parentDirRS.getStoreDirRSDir().exists() || !parentDirRS.getStoreFileRSDir().exists()) {
			log.debug(methodName + DELIMITER + "storeFileRSFile is not found.");
			parentDirRS.initStoreDir();
		}

		// ファイルRS出力.
		try (FileOutputStream fo = new FileOutputStream(storeFileRSFile)) {
			Properties props = new Properties();
			props.put(RootReadingStatus.readingStatus, this.readingStatus);
			props.put(RootReadingStatus.didFirstRun, this.didFirstRun.toString());
			props.put(RootReadingStatus.prevSize, Long.valueOf(this.prevSize).toString());
			props.put(RootReadingStatus.position, Long.valueOf(this.position).toString());
			props.put(RootReadingStatus.monFileLastModTimeStamp, Long.valueOf(this.monFileLastModTimeStamp).toString());
			props.put(RootReadingStatus.lastModTimeStampByThread,
					Long.valueOf(this.lastModTimeStampByThread).toString());
			props.put(RootReadingStatus.prefixString, this.prefixString);
			props.put(RootReadingStatus.runMonitor, this.runMonitor.toString());
			props.store(fo, this.monFileName);

			// ファイル出力内容をログ出力.
			if (log.isDebugEnabled()) {
				log.debug(methodName + DELIMITER + "output file reading status, " + RootReadingStatus.readingStatus
						+ "=" + this.readingStatus + ", " + RootReadingStatus.didFirstRun + "="
						+ this.didFirstRun.toString() + ", " + RootReadingStatus.prevSize + "="
						+ Long.valueOf(this.prevSize).toString() + ", " + RootReadingStatus.position + "="
						+ Long.valueOf(this.position).toString() + ", " + RootReadingStatus.monFileLastModTimeStamp
						+ "=" + Long.valueOf(this.monFileLastModTimeStamp).toString() + ", "
						+ RootReadingStatus.prefixString + "=" + this.prefixString + ", " + RootReadingStatus.runMonitor
						+ "=" + this.runMonitor.toString());

				// 調査用.
				String didFirstRunProps = props.getProperty(RootReadingStatus.didFirstRun);
				log.debug(methodName + DELIMITER + String.format("didFirstRun is... boolean=%s, props=%s",
						this.didFirstRun.toString(), didFirstRunProps));
			}
		} catch (IOException e) {
			// 出力エラー(ユーザー).
			log.info(methodName + DELIMITER + e.getMessage(), e);
		}
	}

	/**
	 * 読み込み状態の保存.<br>
	 * <br>
	 * マネージャー送信後にこのメソッドを呼び出し 読込状態を更新・ファイル出力する.<br>
	 * 
	 * @param prevSize
	 *            読込時のファイルサイズ.
	 * @param position
	 *            読込完了位置.
	 * @param monFileLastModTimeStamp
	 *            読込時の最終更新日時.
	 * @param prefix
	 *            読込時のファイル先頭文字列
	 * 
	 */
	public void storeRS(long prevSize, long position, long monFileLastModTimeStamp, List<Byte> prefix) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");

		// 読込中として保存する.
		this.readingFlag = RootReadingStatus.RS_OPEN_FLAG;
		this.didFirstRun = Boolean.valueOf(true);
		this.readingStatus = RootReadingStatus.rsToString(readingFlag);

		// 引数に従ってフィールド設定.
		this.prevSize = prevSize;
		this.position = position;
		this.monFileLastModTimeStamp = monFileLastModTimeStamp;
		this.prefix = prefix;
		this.prefixSize = prefix.size();
		this.prefixString = BinaryUtil.listToString(prefix, 1);

		// ファイル出力.
		this.outputRS();
	}

	/**
	 * 読込位置の保存.<br>
	 * <br>
	 * 監視処理をスキップした場合等にfile channelのposition設定とセットでセットする.<br>
	 * 
	 * @param position
	 *            読込完了位置.
	 */
	public void storePosition(long prevSize, long position) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");

		// 読込中として保存する.
		this.readingFlag = RootReadingStatus.RS_OPEN_FLAG;
		this.readingStatus = RootReadingStatus.rsToString(readingFlag);

		// 引数に従って位置指定、他のフィールドはそのまま.
		this.prevSize = prevSize;
		this.position = position;

		// ファイル出力.
		this.outputRS();
	}

	/**
	 * 読み込み状態の保存.<br>
	 * <br>
	 * マネージャー送信後にこのメソッドを呼び出し 読込状態を更新・ファイル出力する.<br>
	 * ※ローテーションなしのファイル全体監視向けメソッド.<br>
	 * 
	 * @param prevSize
	 *            読込時のファイルサイズ.
	 * @param monFileLastModTimeStamp
	 *            読込時の最終更新日時.
	 * 
	 */
	public void storeRS(long prevSize, long monFileLastModTimeStamp) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");

		// 読込中として保存する.
		this.readingFlag = RootReadingStatus.RS_OPEN_FLAG;
		this.didFirstRun = Boolean.valueOf(true);
		this.readingStatus = RootReadingStatus.rsToString(readingFlag);

		// 引数に従ってフィールド設定.
		this.prevSize = prevSize;
		this.monFileLastModTimeStamp = monFileLastModTimeStamp;

		// 任意バイナリファイルとしては不要なフィールドなので初期化.
		this.position = 0;
		this.prefixString = "";
		this.prefix = new ArrayList<Byte>();
		this.prefixSize = 0;

		// ファイル出力.
		this.outputRS();
	}

	/**
	 * 監視対象ファイルから現在の先頭バイナリ列取得.<br>
	 * <br>
	 * agentpropertiesで設定しているチェックサイズ分カット.
	 * 
	 * @return 取得できなかった場合はnull返却.
	 * 
	 */
	public List<Byte> getCurrenPrefix() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");

		// 指定バイト数だけ読込むため配列長をAgentPropertiesで指定.
		byte[] monFileByteArray = new byte[BinaryMonitorConfig.getFirstPartDataCheckSize()];
		List<Byte> firstPartOfFile = new ArrayList<Byte>();
		try (FileInputStream fi = new FileInputStream(monFile)) {
			// 監視対象ファイルの先頭から作成したバイト配列長分だけ読込む.
			while (true) {
				int readed = fi.read(monFileByteArray);
				if (readed == monFileByteArray.length) {
					break;
				} else if (readed == monFile.length()) {
					break;
				} else if (readed < 0) {
					break;
				} else {
					fi.reset();
				}
			}
			firstPartOfFile = BinaryUtil.arrayToList(monFileByteArray);
		} catch (IOException e) {
			log.warn(methodName + DELIMITER + e.getMessage(), e);
		}
		log.debug(methodName + DELIMITER + "firstPartOfFile size = " + firstPartOfFile.size());
		return firstPartOfFile;
	}

	/**
	 * 監視対象ファイルから先頭バイナリ文字列取得.<br>
	 * <br>
	 * agentpropertiesで設定しているチェックサイズ分カット.<br>
	 * 設定サイズよりファイルサイズの方が小さい場合はそのサイズのまま.
	 * 
	 * @return 取得できなかった場合はnull返却.
	 * 
	 */
	public String getPrefixString() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");

		String returnString = null;
		List<Byte> firstPartOfFile = this.getCurrenPrefix();
		returnString = BinaryUtil.listToString(firstPartOfFile, 1);
		log.debug(methodName + DELIMITER + "returnString = " + returnString);
		return returnString;
	}

	/**
	 * ローテーション時の初期化.<br>
	 * <br>
	 * 監視対象ファイルがローテーションで切り替わった場合に<br>
	 * 各フィールドを初期化してファイル出力する.<br>
	 * 
	 */
	public void rotate() {
		this.prefixString = this.getPrefixString();
		this.prefix = BinaryUtil.stirngToList(prefixString, 1, 1);
		this.prefixSize = prefix.size();
		this.position = 0;
		this.prevSize = 0;
		this.monFileLastModTimeStamp = monFile.lastModified();
		this.outputRS();
	}

	/**
	 * FilerRSの読込状態をクローズ.<br>
	 * <br>
	 * DirRSのフィールドとファイル出力内容をクローズとして更新.<br>
	 */
	public void closeFileRS() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");

		this.readingStatus = RootReadingStatus.RS_CLOSE_STRING;
		this.outputRS();
		this.readingFlag = RootReadingStatus.rsToFlag(readingStatus);
		this.parentDirRS.checkCloseDirRS();
	}

	/**
	 * 読み込み中フラグをクローズ.<br>
	 * <br>
	 * ファイルRS作成前に呼ぶ想定なのでstatic.
	 */
	public static void closeRumMonitor(File fileRs, String monitorFileName) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		String readingStatus = null;
		Boolean didFirstRun = null;
		long prevSize = 0;
		long position = 0;
		long monFileLastModTimeStamp = 0;
		long lastModTimeStampByThread = 0;
		String prefixString = null;

		// 読込中フラグ以外をファイルから読込んだ値のままにするため読込む.
		Properties props = new Properties();
		try (FileInputStream fi = new FileInputStream(fileRs)) {
			props.load(fi);
			position = Long.parseLong(RootReadingStatus.getPropertyValue(props, RootReadingStatus.position));
			prevSize = Long.parseLong(RootReadingStatus.getPropertyValue(props, RootReadingStatus.prevSize));
			monFileLastModTimeStamp = Long
					.parseLong(RootReadingStatus.getPropertyValue(props, RootReadingStatus.monFileLastModTimeStamp));
			lastModTimeStampByThread = Long
					.parseLong(RootReadingStatus.getPropertyValue(props, RootReadingStatus.lastModTimeStampByThread));
			didFirstRun = Boolean.valueOf(RootReadingStatus.getPropertyValue(props, RootReadingStatus.didFirstRun));
			prefixString = RootReadingStatus.getPropertyValue(props, RootReadingStatus.prefixString);
			readingStatus = RootReadingStatus.getPropertyValue(props, RootReadingStatus.readingStatus);
		} catch (FileNotFoundException e) {
			log.warn(String.format(methodName + DELIMITER + "skip to update fileRS." + " fileRS=[%s] : ",
					fileRs.getAbsolutePath()) + e.getMessage(), e);
			return;
		} catch (IOException e) {
			log.warn(String.format(methodName + DELIMITER + "skip to update fileRS." + " fileRS=[%s] : ",
					fileRs.getAbsolutePath()) + e.getMessage(), e);
			return;
		} catch (InvalidSetting | NumberFormatException e) {
			log.warn(String.format(methodName + DELIMITER + "skip to update fileRS." + " fileRS=[%s] : ",
					fileRs.getAbsolutePath()) + e.getMessage(), e);
			// RSファイルが壊れてるので削除.
			if (!fileRs.delete()) {
				log.warn(methodName + DELIMITER + "failed to delete file = [" + fileRs.getAbsolutePath() + "]");
			} else {
				log.info(methodName + DELIMITER + "deleted file = [" + fileRs.getAbsolutePath() + "]");
			}
			return;
		}

		// プロパティを書き込む.
		try (FileOutputStream fo = new FileOutputStream(fileRs)) {
			props.put(RootReadingStatus.readingStatus, readingStatus);
			props.put(RootReadingStatus.didFirstRun, didFirstRun.toString());
			props.put(RootReadingStatus.prevSize, Long.valueOf(prevSize).toString());
			props.put(RootReadingStatus.position, Long.valueOf(position).toString());
			props.put(RootReadingStatus.monFileLastModTimeStamp, Long.valueOf(monFileLastModTimeStamp).toString());
			props.put(RootReadingStatus.lastModTimeStampByThread, Long.valueOf(lastModTimeStampByThread).toString());
			props.put(RootReadingStatus.prefixString, prefixString);
			// 読込中フラグは固定値.
			props.put(RootReadingStatus.runMonitor, Boolean.valueOf(false).toString());
			props.store(fo, monitorFileName);
			// 更新内容の出力(prefixString以外).
			log.info(methodName + DELIMITER
					+ String.format(
							"success to update in flag of running in fileRS." + " fileRS=[%s], "
									+ RootReadingStatus.readingStatus + "=[%s], " + RootReadingStatus.didFirstRun
									+ "=[%s], " + RootReadingStatus.prevSize + "=[%d], " + RootReadingStatus.position
									+ "=[%d], " + RootReadingStatus.monFileLastModTimeStamp + "=[%d], "
									+ RootReadingStatus.lastModTimeStampByThread + "=[%d]",
							fileRs.getAbsolutePath(), readingStatus, didFirstRun.toString(), prevSize, position,
							monFileLastModTimeStamp, lastModTimeStampByThread));
		} catch (FileNotFoundException e) {
			log.warn(String.format(methodName + DELIMITER + "skip to update fileRS." + " fileRS=[%s] : ",
					fileRs.getAbsolutePath()) + e.getMessage(), e);
		} catch (IOException e) {
			log.warn(String.format(methodName + DELIMITER + "skip to update fileRS." + " fileRS=[%s] : ",
					fileRs.getAbsolutePath()) + e.getMessage(), e);
		}
	}

	// 以下各フィールドのsetter.
	/** ファイルハンドラのポジション */
	public void setPosition(long position) {
		this.position = position;
	}

	/** 監視中フラグをセットしてファイル出力 */
	public void setRunMonitor(boolean run) {
		this.runMonitor = Boolean.valueOf(run);
		this.outputRS();
	}

	/** レコードスキップフラグ */
	public void setToSkipRecord(boolean toSkipRecord) {
		this.toSkipRecord = toSkipRecord;
	}

	/** レコードスキップサイズ */
	public void setSkipSize(long skipSize) {
		this.skipSize = skipSize;
	}

	// 以下各フィールドのgetter.
	/** 監視対象ファイル名(絶対パス)(マップキー) */
	public String getMonFileName() {
		return this.monFileName;
	}

	/** ファイル読込状態を出力するファイル */
	public File getStoreFileRSFile() {
		return this.storeFileRSFile;
	}

	/** 読込状態(true:読込中 false:読込対象外) */
	public boolean isReadingFlag() {
		return this.readingFlag;
	}

	/** 読込状態 */
	public String getReadingStatus() {
		return this.readingStatus;
	}

	/** 初期化済 */
	public boolean isInitialized() {
		return this.initialized;
	}

	/** 読み込んでいるファイルの先頭バイナリ */
	public List<Byte> getPrefix() {
		return this.prefix;
	}

	/** 監視対象ファイル */
	public File getMonFile() {
		return this.monFile;
	}

	/** 比較用先頭バイナリのサイズ */
	public int getPrefixSize() {
		return this.prefixSize;
	}

	/** ファイルハンドラのポジション */
	public long getPosition() {
		return this.position;
	}

	/** ファイルサイズ(ReadingStatus作成時点)(byte) */
	public long getPrevSize() {
		return this.prevSize;
	}

	/** 監視対象ファイルの最終更新タイムスタンプ(監視処理完了時に更新) */
	public long getMonFileLastModTimeStamp() {
		return this.monFileLastModTimeStamp;
	}

	/** ディレクトリ毎の読込状態に関する情報 */
	public DirectoryReadingStatus getParentDirRS() {
		return this.parentDirRS;
	}

	/** ファイル全体監視実行フラグ(true:実行,false:監視skip) */
	public boolean isRunWholeFileMonitor() {
		return this.runWholeFileMonitor;
	}

	/** 監視中フラグ(ファイル全体監視向け) */
	public boolean isRunMonitor() {
		return this.runMonitor.booleanValue();
	}

	/** 初回監視実行済フラグ(true:実行済,false:未実行) */
	public boolean isDidFirstRun() {
		return this.didFirstRun.booleanValue();
	}

	/** レコードスキップフラグ */
	public boolean isToSkipRecord() {
		return this.toSkipRecord;
	}

	/** レコードスキップサイズ */
	public long getSkipSize() {
		return this.skipSize;
	}

}