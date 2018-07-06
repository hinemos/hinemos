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

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.binary.BinaryMonitorConfig;
import com.clustercontrol.agent.log.MonitorInfoWrapper;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.util.HinemosTime;

/**
 * 監視設定毎の読込状態管理クラス<br>
 * <br>
 * オブジェクトは監視設定毎に作成される.<br>
 */
public class MonitorReadingStatus {

	// クラス共通フィールド.
	/** ロガー */
	private static Log log = LogFactory.getLog(MonitorReadingStatus.class);

	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	// 監視設定毎の読込状態に関する情報(ファイル出力対象外).
	/** 読込状態の全体に関する情報 */
	private RootReadingStatus parentRootRS;

	/** 監視対象ディレクトリ毎の読込状態(キーはディレクトリの絶対パス) */
	private Map<String, DirectoryReadingStatus> directoryRSMap = new TreeMap<String, DirectoryReadingStatus>();

	/** 監視設定・監視結果 */
	private MonitorInfoWrapper monInfoWrapper;

	/** 監視設定毎の読込状態を格納するディレクトリ名 */
	private String storeDirName;

	/** 読込状態を格納するディレクトリ */
	private File storeDir;

	/** 監視設定毎の読込状態を書き込むファイル(rstatus.json) */
	private File storeMonRSFile;

	/** 監視対象ディレクトリ(監視設定上) */
	private File monRootDirectory;

	/** 読込回数(監視対象ファイル数が上限に達していないかチェック) **/
	private int counter = 0;

	/** 監視設定更新ステータス. **/
	private int refreshStatus;

	/** 監視設定更新ステータス_監視設定更新なし. **/
	protected static final int REFRESH_MONINF_NO = 0;

	/** 監視設定更新ステータス_監視設定登録日時が更新(再作成されている). **/
	protected static final int REFRESH_MONINF_UPDATE = 1;

	/** 監視設定更新ステータス_監視対象ディレクトリ名更新. **/
	protected static final int REFRESH_MONINF_DIRNAME = 2;

	/** 監視設定更新ステータス_監視対象ファイル名更新. **/
	protected static final int REFRESH_MONINF_FILENAME = 3;

	/** 監視設定更新ステータス_監視設定の初回読込. **/
	protected static final int REFRESH_MONINF_INIT = 5;

	// 監視設定毎の読込状態に関する情報(ファイル出力対象の項目).
	/** 監視ID(コメント出力)(マップキー) */
	private String monitorID;

	/** 監視対象ディレクトリ名(監視設定上) */
	private String monRootDirectoryName;

	/** 監視対象ファイル名(正規表現) */
	private String filename;

	/** 監視設定の更新日時 */
	private Long updateDate;

	/** 前回のReadingStatus更新日時 */
	private long lastUpdateRs;

	/**
	 * コンストラクタ.<br>
	 * 
	 * @param monInfoWrapper
	 *            紐づく監視設定
	 * @param parentRootRS
	 *            親となる読込情報
	 */
	protected MonitorReadingStatus(MonitorInfoWrapper monInfoWrapper, RootReadingStatus parentRootRS) {
		// フィールドの初期化.
		this.monInfoWrapper = monInfoWrapper;
		this.parentRootRS = parentRootRS;
		this.monitorID = monInfoWrapper.getId();
		this.storeDirName = RootReadingStatus.dir_prefix + monitorID;

		// フィールドの初期化(メソッド).
		this.initStoreDir();
		this.initMonitorRS();
		this.initDirectoryRS();
	}

	/**
	 * 取得した監視設定でMonitorReadingStatusを更新.<br>
	 * 
	 * @param monInfoWrapper
	 *            最新の監視設定
	 * 
	 */
	protected void updateMonRS(MonitorInfoWrapper monInfoWrapper) {
		this.monInfoWrapper = monInfoWrapper;
		this.initStoreDir();
		this.initMonitorRS();
		this.initDirectoryRS();
	}

	/**
	 * 監視ID毎の読込情報を格納するディレクトリの作成.<br>
	 * 
	 */
	private void initStoreDir() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");

		File storeDir = new File(this.parentRootRS.getStorePath(), this.storeDirName);
		if (!storeDir.exists()) {
			if (!storeDir.mkdir()) {
				log.warn(methodName + DELIMITER + storeDir.getPath() + " is not created.");
				return;
			} else {
				log.debug(methodName + DELIMITER + storeDir.getPath() + " is created.");
			}
		}
		this.storeDir = storeDir;
	}

	/**
	 * MonitorRSの値を初期化.<br>
	 * <br>
	 * rstatus.jsonもしくは監視設定から取得.<br>
	 * 
	 */
	public void initMonitorRS() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");

		// 監視設定等が更新されているかどうか確認.
		this.refreshStatus = this.getRecentRefreshStatus();
		if (this.refreshStatus == REFRESH_MONINF_UPDATE || this.refreshStatus == REFRESH_MONINF_DIRNAME) {
			// 監視設定あるいはディレクトリ名が更新されていた場合は既存の出力RS削除.
			this.clearMonRsFiles();
		}

		// MonitorRSの初期化.
		File rstatus = new File(new File(this.parentRootRS.getStorePath(), this.storeDirName),
				RootReadingStatus.file_rstatus);
		this.storeMonRSFile = rstatus;
		boolean toCreate = false;

		if (rstatus.exists()) {
			// 前回ファイル出力した読込情報が存在する場合.
			try (FileInputStream fi = new FileInputStream(rstatus)) {
				// ファイルを読み込む
				Properties props = new Properties();
				props.load(fi);

				// 監視対象ディレクトリ名と登録日(監視設定更新されていた場合ファイル削除されてるためelseに入る).
				this.monRootDirectoryName = RootReadingStatus.getPropertyValue(props,
						RootReadingStatus.monRootDirectoryName);
				this.updateDate = Long.valueOf(RootReadingStatus.getPropertyValue(props, RootReadingStatus.updatedate));
				this.lastUpdateRs = Long
						.parseLong(RootReadingStatus.getPropertyValue(props, RootReadingStatus.lastUpdateRs));

				// ファイル名は監視設定更新されていた場合、監視設定から取得してファイル内容更新.
				if (this.refreshStatus == REFRESH_MONINF_FILENAME) {
					this.filename = this.monInfoWrapper.monitorInfo.getBinaryCheckInfo().getFileName();
					this.outputRS();
				} else {
					this.filename = RootReadingStatus.getPropertyValue(props, RootReadingStatus.filename);
				}

			} catch (FileNotFoundException e) {
				log.debug(e.getMessage(), e);
			} catch (IOException e) {
				log.warn(methodName + DELIMITER + e.getMessage(), e);
			} catch (InvalidSetting | NumberFormatException e) {
				log.warn(methodName + DELIMITER + e.getMessage(), e);
				// RSファイルが壊れてるので削除.
				if (!rstatus.delete()) {
					log.warn(methodName + DELIMITER + "failed to delete file = [" + rstatus.getAbsolutePath() + "]");
				} else {
					log.info(methodName + DELIMITER + "deleted file = [" + rstatus.getAbsolutePath() + "]");
					// 削除成功したので作成.
					toCreate = true;
				}
			}

		} else {
			// ファイル出力された読込情報が存在しない場合は、foでrstatus.json作成して監視設定から読込む.
			toCreate = true;
		}

		// RSファイル新規作成.
		if (toCreate) {
			try (FileOutputStream fo = new FileOutputStream(rstatus)) {
				this.monRootDirectoryName = this.monInfoWrapper.monitorInfo.getBinaryCheckInfo().getDirectory();
				this.filename = this.monInfoWrapper.monitorInfo.getBinaryCheckInfo().getFileName();
				this.updateDate = this.monInfoWrapper.monitorInfo.getUpdateDate();
				this.lastUpdateRs = 0;
				this.outputRS();
			} catch (IOException e) {
				log.warn(methodName + DELIMITER + e.getMessage(), e);
			}
		}

		// 監視対象ディレクトリをファイルオブジェクト化.
		this.monRootDirectory = new File(this.monRootDirectoryName);
	}

	/**
	 * 監視設定が更新されているかどうか判定.<br>
	 * 
	 * @return <br>
	 *         REFRESH_MONINF_NO：更新なし <br>
	 *         REFRESH_MONINF_UPDATE：監視設定登録日時が更新(再作成されている) <br>
	 *         REFRESH_MONINF_DIRNAME：監視対象ディレクトリ名更新 <br>
	 *         REFRESH_MONINF_FILENAME：監視対象ファイル名更新 <br>
	 * 
	 */
	private int getRecentRefreshStatus() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		int refresh = REFRESH_MONINF_NO;
		if (monRootDirectory != null && updateDate != null && filename != null) {
			// クラスに監視設定が保持されている場合、最新の監視設定を取得して比較する.
			Long monInfUpdateDate = monInfoWrapper.monitorInfo.getUpdateDate();
			String monInfDir = monInfoWrapper.monitorInfo.getBinaryCheckInfo().getDirectory();
			String monInfDirPath = new File(monInfDir).getAbsolutePath();
			String monInfFileName = monInfoWrapper.monitorInfo.getBinaryCheckInfo().getFileName();

			if (!updateDate.equals(monInfUpdateDate)) {
				// 監視設定の登録日時が異なる場合.
				refresh = REFRESH_MONINF_UPDATE;
			} else if (!monRootDirectory.getAbsolutePath().equals(monInfDirPath)) {
				// 監視設定の監視対象ディレクトリのパスが異なる場合.
				refresh = REFRESH_MONINF_DIRNAME;
			} else if (!filename.equals(monInfFileName)) {
				// 監視対象ファイルパターンが変更されている場合.
				refresh = REFRESH_MONINF_FILENAME;
			} else {
				// 監視設定の更新なし
				refresh = REFRESH_MONINF_NO;
			}
		} else {
			// クラスに監視設定が保持されていない場合はそもそも初回読込等なのでtrue.
			refresh = REFRESH_MONINF_INIT;
		}
		log.debug(methodName + DELIMITER + String.format("monitor refresh status=%d", refresh));
		return refresh;
	}

	/**
	 * ファイル出力.<br>
	 */
	private void outputRS() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");

		Properties props = new Properties();

		try (FileOutputStream fo = new FileOutputStream(this.storeMonRSFile)) {
			props.put(RootReadingStatus.monRootDirectoryName, this.monRootDirectoryName);
			props.put(RootReadingStatus.filename, this.filename);
			props.put(RootReadingStatus.updatedate, this.updateDate.toString());
			props.put(RootReadingStatus.lastUpdateRs, Long.valueOf(this.lastUpdateRs).toString());
			props.store(fo, this.monitorID);
		} catch (IOException e) {
			log.warn(methodName + DELIMITER + e.getMessage(), e);
		}
	}

	/**
	 * ディレクトリ毎の読込情報管理クラス作成.
	 */
	public void initDirectoryRS() {

		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");

		// FileRS生成時にカウントして最大読込ファイル数を超えていないかチェック.
		this.clearCounter();

		Map<String, DirectoryReadingStatus> curDirRS = new TreeMap<String, DirectoryReadingStatus>(directoryRSMap);
		Map<String, DirectoryReadingStatus> newDirRS = new TreeMap<String, DirectoryReadingStatus>();

		// 監視設定で指定されているディレクトリ自身を追加.
		if (!monRootDirectory.exists()) {
			log.info(methodName + DELIMITER + String.format("directory [%s] isn't exist.", monRootDirectory.getPath()));
			return;
		}

		// 取得したディレクトリリスト元にRS生成.
		DirectoryReadingStatus oldDirRS = curDirRS.get(monRootDirectory.getAbsolutePath());
		if (oldDirRS == null) {
			// directoryRSが存在しない場合追加.
			DirectoryReadingStatus tmpDirRS = new DirectoryReadingStatus(monRootDirectory, this);
			newDirRS.put(tmpDirRS.getMonDirName(), tmpDirRS);
		} else {
			oldDirRS.initDirRS();
			oldDirRS.initFileRS();
			newDirRS.put(oldDirRS.getMonDirName(), oldDirRS);
		}

		// 不要なディレクトリ読込状態の削除.
		// ※curDirRSの内、newDirRSに存在しないものは、ゴミなのでRS物理ファイル削除.
		DirectoryReadingStatus tmpRS = null;
		if (!curDirRS.isEmpty() && !newDirRS.isEmpty()) {
			for (Map.Entry<String, DirectoryReadingStatus> curRS : curDirRS.entrySet()) {
				tmpRS = newDirRS.get(curRS.getKey());
				if (tmpRS == null) {
					// 新規の監視対象に存在しない場合はRSの物理ファイル削除.
					this.clearDirRsFiles(curRS.getValue());
				}
			}
		}

		this.directoryRSMap = newDirRS;

		// 一通りRSの更新完了したので現在時刻をセット(新規生成ファイルの判断用).
		this.lastUpdateRs = HinemosTime.getDateInstance().getTime();
		this.outputRS();
	}

	/**
	 * 監視設定毎の読込状態を保持するファイルをクリア.<br>
	 * <br>
	 * storeDir内をサブディレクトリ内のファイル含めて削除.<br>
	 * ※storeDir自身は保持.<br>
	 */
	private void clearMonRsFiles() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		try {
			// サブディレクトリ含めて再帰的にファイル物理削除(引数自身は保持).
			FileUtils.cleanDirectory(storeDir);

			// クリアされた旨をログ出力.
			Long monInfUpdateDate = monInfoWrapper.monitorInfo.getUpdateDate();
			String monInfDir = monInfoWrapper.monitorInfo.getBinaryCheckInfo().getDirectory();
			log.info(String.format(
					methodName + DELIMITER + "ReadingStatus is clear. regdate=%d,dir=%s nextUpdateDate=%d,nextdir=%s",
					updateDate, monRootDirectory, monInfUpdateDate, monInfDir));
		} catch (IOException e) {
			log.warn(methodName + DELIMITER + storeDirName + "isn't cleared." + e.getMessage(), e);
		}

		directoryRSMap.clear();
	}

	/**
	 * ディレクトリ毎の読込状態を保持するファイルをクリア.<br>
	 * <br>
	 * ディレクトリ配下のファイル毎の読込情報含め削除.<br>
	 */
	private void clearDirRsFiles(DirectoryReadingStatus rmvDirRS) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");

		// 配下のファイルRSから削除.
		if (!rmvDirRS.getFileRSMap().isEmpty()) {
			File fileRS = null;
			// 物理ファイル削除.
			for (Map.Entry<String, FileReadingStatus> fileRSMap : rmvDirRS.getFileRSMap().entrySet()) {
				fileRS = fileRSMap.getValue().getStoreFileRSFile();
				if (fileRS.exists()) {
					if (!fileRS.delete()) {
						// RSファイル削除失敗の場合はログ出力.
						log.warn(methodName + DELIMITER + "failed to delete reading status file = ["
								+ fileRS.getAbsolutePath() + "]");
					}
				}
			}
			// ディレクトリRS配下のファイルRSマップ削除.
			rmvDirRS.getFileRSMap().clear();
		}

		// ディレクトリRSの物理ファイル削除.
		File rmvStoreFile = null;
		rmvStoreFile = rmvDirRS.getStoreDirRSFile();
		if (rmvStoreFile.exists()) {
			// ディレクトリ以外の場合はゴミなので削除.
			if (!rmvStoreFile.delete()) {
				// RSファイル削除失敗の場合はログ出力.
				log.warn(methodName + DELIMITER + "failed to delete directory reading status file = ["
						+ rmvStoreFile.getAbsolutePath() + "]");
			}
		}
	}

	/**
	 * 読込み回数初期化.<br>
	 * 
	 */
	private void clearCounter() {
		log.debug("clear counter.");
		counter = 0;
	}

	/**
	 * 読込み回数チェック&カウント.<br>
	 * <br>
	 * 監視対象のファイル数が最大読込ファイル数を超えていないかチェック.<br>
	 * DirectoryReadingStatusクラスにて<br>
	 * FileReadingStatusインスタンスを生成する際にチェック&カウント<br>
	 * 
	 * @return 読込み回数 >= 最大読込みファイル数の場合false
	 */
	protected boolean incrementCounter() throws IllegalStateException {
		if (counter >= BinaryMonitorConfig.getFileMaxFiles()) {
			return false;
		}
		++counter;
		return true;
	}

	/**
	 * RS格納ディレクトリ削除.
	 */
	public void deleteStoreDir() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		if (this.storeDir.exists()) {
			try {
				FileUtils.deleteDirectory(this.storeDir);
				log.debug(methodName + DELIMITER + "success to delete directory reading status file = ["
						+ storeDir.getAbsolutePath() + "]");
			} catch (IOException e) {
				log.warn(methodName + DELIMITER + "failed to delete directory reading status file = ["
						+ storeDir.getAbsolutePath() + "]");
			}
		}
	}

	// 以下各フィールドのgetter.
	/** 監視ID(マップキー) */
	public String getMonitorID() {
		return this.monitorID;
	}

	/** 読込状態を格納するディレクトリ */
	public File getStoreDir() {
		return this.storeDir;
	}

	/** 監視設定毎の読込状態を格納するディレクトリ名(rs_監視ID) */
	public String getStoreDirName() {
		return this.storeDirName;
	}

	/** 監視対象ディレクトリ(監視設定上) */
	public File getMonRootDirectory() {
		return this.monRootDirectory;
	}

	/** 監視対象ファイル名(正規表現) */
	public String getFilename() {
		return this.filename;
	}

	/**
	 * 監視設定更新ステータス.<br>
	 * <br>
	 * REFRESH_MONINF_NO：更新なし <br>
	 * REFRESH_MONINF_UPDATE：監視設定登録日時が更新(再作成されている) <br>
	 * REFRESH_MONINF_DIRNAME：監視対象ディレクトリ名更新 <br>
	 * REFRESH_MONINF_FILENAME：監視対象ファイル名更新 <br>
	 * 
	 **/
	public int getRefreshStatus() {
		return this.refreshStatus;
	}

	/** 監視設定・監視結果 */
	public MonitorInfoWrapper getMonInfoWrapper() {
		return this.monInfoWrapper;
	}

	/** 監視対象ディレクトリ毎の読込状態(キーはディレクトリの絶対パス) */
	public Map<String, DirectoryReadingStatus> getDirectoryRSMap() {
		return this.directoryRSMap;
	}

	/** 監視設定の更新日時 */
	public Long getUpdateDate() {
		return this.updateDate;
	}

	/** 読込状態全体に関する情報(thread毎) */
	public RootReadingStatus getParentRootRS() {
		return this.parentRootRS;
	}

	/** 前回のReadingStatus更新日時 */
	protected long getLastUpdateRs() {
		return this.lastUpdateRs;
	}
}