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
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 監視対象ディレクトリ読込状態管理クラス<br>
 * <br>
 * オブジェクトは監視対象のディレクトリ毎に作成される.<br>
 */
public class DirectoryReadingStatus {

	// クラス共通フィールド.
	/** ロガー */
	private static Log log = LogFactory.getLog(DirectoryReadingStatus.class);

	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	// ディレクトリ毎の読込状態に関する情報.
	/** 監視毎の読込状態に関する情報 */
	private MonitorReadingStatus parentMonRS;

	/** 監視対象ファイル毎の読込状態 */
	private Map<String, FileReadingStatus> fileRSMap = new TreeMap<String, FileReadingStatus>();

	/** 監視対象ディレクトリ */
	private File monDir;

	/** 監視対象ディレクトリの相対パス(基底は監視設定で指定したディレクトリ) */
	private String relative;

	/** ディレクトリ読込状態を格納するディレクトリ */
	private File storeDirRSDir;

	/** ディレクトリ読込状態を出力するファイル */
	private File storeDirRSFile;

	/** ファイル読込状態を格納するディレクトリ */
	private File storeFileRSDir;

	/** 読込状態 */
	private boolean readingFlag;

	// ファイル出力対象の項目.
	/** 監視対象ディレクトリ名(絶対パス)(コメント出力)(マップキー) */
	private String monDirName;

	/** 読込状態 */
	private String readingStatus;

	/**
	 * コンストラクタ.<br>
	 * 
	 * @param monDir
	 *            監視対象のディレクトリ
	 * @param parentMonRS
	 *            親となる読込情報
	 */
	public DirectoryReadingStatus(File monDir, MonitorReadingStatus parentMonRS) {
		this.parentMonRS = parentMonRS;
		this.monDir = monDir;
		this.monDirName = monDir.getAbsolutePath();
		File parentMonRoot = null;
		if (parentMonRS.getMonRootDirectory().getParent() != null) {
			parentMonRoot = new File(parentMonRS.getMonRootDirectory().getParent());
		} else {
			parentMonRoot = parentMonRS.getMonRootDirectory();
		}
		this.relative = parentMonRoot.toURI().relativize(monDir.toURI()).toString();
		this.storeFileRSDir = new File(parentMonRS.getStoreDir(), relative);
		this.storeDirRSDir = storeFileRSDir.getParentFile();

		this.initStoreDir();
		this.initDirRS();
		this.initFileRS();
	}

	/**
	 * 読込状態を保存するディレクトリの初期化.<br>
	 * 
	 */
	protected void initStoreDir() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");

		if (!storeDirRSDir.exists()) {
			if (!storeDirRSDir.mkdir()) {
				log.warn(methodName + DELIMITER + storeDirRSDir.getPath() + " is not created.");
				return;
			} else {
				log.debug(methodName + DELIMITER + storeDirRSDir.getPath() + " is created.");
			}
		}

		if (!storeFileRSDir.exists()) {
			if (!storeFileRSDir.mkdir()) {
				log.warn(methodName + DELIMITER + storeFileRSDir.getPath() + " is not created.");
				return;
			} else {
				log.debug(methodName + DELIMITER + storeFileRSDir.getPath() + " is created.");
			}
		}
	}

	/**
	 * DirRSの値を初期化.<br>
	 * <br>
	 * 前回出力済ファイルから取得もしくは初期値を設定.<br>
	 * 
	 */
	protected void initDirRS() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");

		File rstatus = new File(storeDirRSDir, RootReadingStatus.dir_prefix + monDir.getName());
		this.storeDirRSFile = rstatus;

		Properties props = new Properties();

		if (rstatus.exists()) {
			// 前回ファイル出力した読込情報が存在する場合.
			try (FileInputStream fi = new FileInputStream(rstatus)) {
				// ファイルを読み込む
				props.load(fi);
				this.readingStatus = props.getProperty(RootReadingStatus.readingStatus);
			} catch (FileNotFoundException e) {
				log.warn(methodName + DELIMITER + e.getMessage(), e);
			} catch (IOException | NumberFormatException e) {
				log.warn(methodName + DELIMITER + e.getMessage(), e);
			}
		} else {
			// ファイル出力された読込情報が存在しない場合は、読込中として読込状態保存.
			try (FileOutputStream fo = new FileOutputStream(rstatus)) {
				this.readingStatus = RootReadingStatus.RS_OPEN_STRING;
				this.outputRS();
			} catch (IOException e) {
				log.warn(methodName + DELIMITER + e.getMessage(), e);
			}
		}

		this.readingFlag = RootReadingStatus.rsToFlag(readingStatus);

	}

	/**
	 * ファイル毎の読込状態管理クラス作成.<br>
	 * 
	 */
	protected void initFileRS() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");

		Map<String, FileReadingStatus> curFileRS = new TreeMap<String, FileReadingStatus>(fileRSMap);
		Map<String, FileReadingStatus> newFileRS = new TreeMap<String, FileReadingStatus>();

		// 監視対象ファイル検索用のパターン作成
		Pattern pattern = null;
		try {
			pattern = Pattern.compile(parentMonRS.getFilename(), Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		} catch (Exception e) {
			// 不正ファイルパターン、処理が継続できないので、処理を戻す。
			log.warn(methodName + DELIMITER + e.getMessage(), e);
			return;
		}

		// 検索対象のファイルを抽出.
		File[] seekFiles = monDir.listFiles();
		if (seekFiles == null || seekFiles.length <= 0) {
			log.info(methodName + DELIMITER + monDirName + " does not have a reference permission");
			return;
		}

		// 検索対象ファイルのチェックを行いOKなら監視対象としてマップに追加.
		for (File file : seekFiles) {
			// ファイル存在チェック.
			log.debug(methodName + DELIMITER + parentMonRS.getMonitorID() + ", file=" + file.getName());
			if (!file.isFile()) {
				log.debug(file.getName() + " is not file");
				continue;
			}
			// ファイルパターン一致チェック.
			Matcher matcher = pattern.matcher(file.getName());
			if (!matcher.matches()) {
				log.debug(methodName + DELIMITER + "don't match. filename=" + file.getName() + ", pattern="
						+ parentMonRS.getFilename());
				continue;
			}
			// 最大監視対象ファイル数チェック.
			if (!parentMonRS.incrementCounter()) {
				log.info(methodName + DELIMITER + "too many files for binary monitor. not-monitoring file="
						+ file.getName());
				continue;
			}

			// 前回検知分として存在するかマップ取得.
			FileReadingStatus oldFileRS = curFileRS.get(file.getName());

			if (oldFileRS == null) {
				// 新規に検知したファイルなら、ファイル状態情報を新たに追加.
				FileReadingStatus tmpRS = new FileReadingStatus(file, this);
				newFileRS.put(tmpRS.getMonFileName(), tmpRS);
			} else {
				// 存在してた場合はFileRSを初期化してから追加.
				oldFileRS.initFileRS();
				newFileRS.put(oldFileRS.getMonFileName(), oldFileRS);
			}
		}

		// 不要なファイル読込状態の削除.
		// ※curFileRSの内、newFileRSにないRSは、監視設定のパスが更新されて監視対象外となったファイル読込状態
		FileReadingStatus tmpRS = null;
		FileReadingStatus rmvRS = null;
		if (!curFileRS.isEmpty()) {
			for (Map.Entry<String, FileReadingStatus> curRS : curFileRS.entrySet()) {
				tmpRS = newFileRS.get(curRS.getKey());
				if (tmpRS == null) {
					// 新規の監視対象に存在しない場合はRSの物理ファイル削除.
					rmvRS = curRS.getValue();
					if (rmvRS.getStoreFileRSFile().exists()) {
						if (!rmvRS.getStoreFileRSFile().delete()) {
							// RSファイル削除失敗の場合はログ出力.
							log.warn(methodName + DELIMITER + "failed to delete reading status file = ["
									+ rmvRS.getStoreFileRSFile().getAbsolutePath() + "]");
						}
					}
				}
			}
		}

		if (newFileRS.isEmpty()) {
			// 監視対象のファイルがないということなので、監視対象ディレクトリの読込状態をクローズする.
			this.closeDirRS();
		} else {
			this.readingStatus = RootReadingStatus.RS_OPEN_STRING;
			this.outputRS();
			this.readingFlag = RootReadingStatus.rsToFlag(readingStatus);
		}

		// パターンマッチしたファイルの読込状態をRSとして保存.
		this.fileRSMap = newFileRS;

	}

	/**
	 * ファイル出力.<br>
	 */
	private void outputRS() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");

		try (FileOutputStream fo = new FileOutputStream(storeDirRSFile)) {
			Properties props = new Properties();
			props.put(RootReadingStatus.readingStatus, readingStatus);
			props.store(fo, monDirName);
			log.debug(methodName + DELIMITER + "output Directory Reading Status : monDirName = " + monDirName + ", "
					+ RootReadingStatus.readingStatus + " = " + readingStatus);
		} catch (IOException e) {
			log.warn(methodName + DELIMITER + e.getMessage(), e);
		}
	}

	/**
	 * DirRSの読込状態をクローズ.<br>
	 * <br>
	 * DirRSのフィールドとファイル出力内容をクローズとして更新.<br>
	 */
	private void closeDirRS() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");

		this.readingStatus = RootReadingStatus.RS_CLOSE_STRING;
		this.outputRS();
		this.readingFlag = RootReadingStatus.rsToFlag(readingStatus);
	}

	/**
	 * DirRSのクローズ判定.<br>
	 * <br>
	 * DirRS内のファイルが全てクローズしている場合はクローズする.<br>
	 */
	protected void checkCloseDirRS() {
		boolean readingFlg = RootReadingStatus.RS_CLOSE_FLAG;
		for (FileReadingStatus fileRS : this.fileRSMap.values()) {
			if (fileRS.isReadingFlag()) {
				readingFlg = RootReadingStatus.RS_OPEN_FLAG;
				break;
			}
		}
		if (!readingFlg) {
			// 読込中のFileが存在しない場合はディレクトリクローズ処理.
			this.closeDirRS();
		}
	}

	// 以下各フィールドのgetter.
	/** フォルダ読込状態を格納するディレクトリ */
	public File getStoreDirRSDir() {
		return this.storeDirRSDir;
	}

	/** ファイル読込状態を格納するディレクトリ */
	public File getStoreFileRSDir() {
		return this.storeFileRSDir;
	}

	/** 監視対象ディレクトリ名(絶対パス)(マップキー) */
	public String getMonDirName() {
		return this.monDirName;
	}

	/** 監視毎の読込状態に関する情報(キーは監視対象ディレクトリ絶対パス) */
	public MonitorReadingStatus getParentMonRS() {
		return this.parentMonRS;
	}

	/** ディレクトリ読込状態を出力するファイル */
	public File getStoreDirRSFile() {
		return this.storeDirRSFile;
	}

	/** 監視対象ファイル毎の読込状態 */
	public Map<String, FileReadingStatus> getFileRSMap() {
		return this.fileRSMap;
	}

	/** 読込状態 */
	public boolean isReadingFlag() {
		return this.readingFlag;
	}

	/** 監視対象ディレクトリ */
	public File getMonDir() {
		return this.monDir;
	}
}