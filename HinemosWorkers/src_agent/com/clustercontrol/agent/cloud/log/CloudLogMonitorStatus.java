/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.cloud.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.cloud.log.util.CloudLogfileMonitorManager;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.util.MessageConstant;

/**
 * クラウドログ監視のログ取得状況管理用のクラス
 * 
 */
public class CloudLogMonitorStatus {
	private static Log log = LogFactory.getLog(CloudLogMonitorStatus.class);

	private static final String CARRYOVER_KEY = "carryover";
	private static final String DATE_KEY = "date";
	private static final String LONGEST_KEY = "longest";

	// ステータス管理ファイルのパス
	protected final File statusFilePath;

	protected boolean initialized;

	protected boolean initializedWithFileCreation = false;

	protected boolean initializedWithCarryover = false;

	protected String monitorId;

	// 読み込んでいる対象のログストリーム名（Azureの場合は監視設定ID）
	protected final String monitorTarget;

	// 次回読み込み時までの持ち越し分
	protected String carryover = "";

	protected String date = "";

	protected String longest = "";

	private boolean internalNotifyFlg = false;

	public CloudLogMonitorStatus(String monitorId, String monitorTarget, File statusFilePath) {

		this.monitorTarget = monitorTarget;
		this.monitorId = monitorId;
		this.statusFilePath = statusFilePath;
		initialize();
	}

	public String getMonitorTarget() {
		return monitorTarget;
	}

	public void setCarryOver(String carryover) {
		this.carryover = carryover;
	}

	public String getCarryOver() {
		return this.carryover;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getDate() {
		return this.date;
	}

	public void setLongest(String longest) {
		this.longest = longest;
	}

	public String getLongest() {
		return this.longest;
	}

	public void setInitializedWithFileCreation(boolean initializedWithFileCreation) {
		this.initializedWithFileCreation = initializedWithFileCreation;
	}

	public void setInitializedWithCarryover(boolean initializedWithCarryover) {
		this.initializedWithCarryover = initializedWithCarryover;
	}

	public boolean isInitializedWithCarryover() {
		return this.initializedWithCarryover;
	}

	protected boolean initialize() {

		// 初回起動時のみステータス管理ファイルの最新情報を取得する。
		organizeStatusFile();
		File statusFile = new File(getStatusTempFilePath());
		Path statusTempFilePath = Paths.get(getStatusTempFilePath());
		Path statusFilePath = Paths.get(this.statusFilePath.getAbsolutePath());

		if (java.nio.file.Files.exists(statusTempFilePath)) {
			try (FileInputStream fi = new FileInputStream(statusFile)) {
				// ファイルを読み込む
				Properties props = new Properties();
				props.load(fi);

				carryover = props.getProperty(CARRYOVER_KEY);
				date = props.getProperty(DATE_KEY);
				longest = props.getProperty(LONGEST_KEY);

				initialized = true;
			} catch (IOException e) {
				log.warn("initialize(): failed reading file.", e);
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("initialize() : reading status file is nothing :" + statusFilePath);
			}
			store();
			initialized = true;
			initializedWithFileCreation = true;

		}
		return initialized;
	}

	/**
	 * ステータス管理ファイルの整理を行う。ファイルをチェックし、破損してれば削除する。
	 *  ファイルが破損している場合はその旨の通知を行う。
	 */
	protected void organizeStatusFile() {
		File statusFile = new File(getStatusTempFilePath());
		boolean isStatusFileInValid = false;
		boolean statusFileExists = java.nio.file.Files.exists(Paths.get(getStatusTempFilePath()));

		if (!statusFileExists) {
			log.info("organizeStatusFlag():" + statusFile.getAbsolutePath() + " is nothing");
		} else {
			// ファイルファイルが破損しているか(0バイトファイルもしくは無効な内容)どうかの判定し、該当なら削除
			if (statusFile.length() == 0 || !(isValidContentFile(statusFile))) {
				log.info("organizeStatusFlag():" + statusFile.getAbsolutePath() + " is 0 bytes or invalid file."
						+ " size=" + statusFile.length() + " content=" + printFile(statusFile));
				if (!statusFile.delete()) {
					log.info("organizeStatus: Failed to delete.");
				}
				isStatusFileInValid = true;
			}
		}

		if (statusFileExists && isStatusFileInValid) {
			// ステータス管理ファイル が不正な場合
			// 現時点からの差分を監視する旨を通知
			String[] messageArgs = { monitorTarget };
			CloudLogfileMonitorManager.getInstance().sendMessage(monitorTarget, PriorityConstant.TYPE_WARNING,
					MessageConstant.AGENT.getMessage(),
					MessageConstant.MESSAGE_CLOUD_LOG_REBUILD_STATUS_FILE.getMessage(messageArgs),
					"statusfile has broken ," + statusFile.getAbsolutePath(), monitorId, null);
		}

	}

	/**
	 * 有効な内容のファイルである確認する
	 * 
	 * @return 有効な場合はtrue。そうでない場合はfalse
	 */
	private boolean isValidContentFile(File f) {
		boolean isValid = true;
		try (FileInputStream fi = new FileInputStream(f)) {
			// ファイルを読み込む
			Properties props = new Properties();
			props.load(fi);

			// 指定のプロパティが見つからない場合無効と判断する
			if (props.getProperty(CARRYOVER_KEY) == null || props.getProperty(DATE_KEY) == null
					|| props.getProperty(LONGEST_KEY) == null) {
				isValid = false;
			}

		} catch (Exception e) {
			isValid = false;
			log.warn(e.getMessage(), e);
		}
		return isValid;
	}

	/**
	 * ファイルの内容を表示する
	 * 
	 * @return ファイルの内容
	 */
	private String printFile(File f) {
		StringBuffer buffer = new StringBuffer();
		try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line + System.getProperty("line.separator"));
			}
		} catch (IOException e) {
			log.warn(f.getAbsolutePath() + "," + e.getMessage());
		}
		return buffer.toString();
	}

	/**
	 * ステータス管理ファイルパスの取得
	 * 
	 * @return 最新のステータス管理ファイルパス
	 */
	private String getStatusTempFilePath() {
		return statusFilePath + ".status";
	}

	/**
	 * ファイル状態の情報を書き出す。
	 */
	public void store() {
		String rsFileTempPath = getStatusTempFilePath();

		File tmpFilePath = new File(rsFileTempPath);
		boolean writeSuccessFlg = true;
		try (FileOutputStream fi = new FileOutputStream(tmpFilePath)) {
			Properties props = new Properties();

			props.put(CARRYOVER_KEY, carryover);
			props.put(DATE_KEY, date);
			props.put(LONGEST_KEY, longest);
			props.store(fi, monitorTarget);
		} catch (IOException e) {
			log.warn("store(): failed writing to file.", e);
			// 初回失敗時のみINTERNALを出力
			if (!internalNotifyFlg) {
				String[] messageArgs = { monitorTarget };
				CloudLogfileMonitorManager.getInstance().sendMessage(monitorTarget, PriorityConstant.TYPE_WARNING,
						MessageConstant.AGENT.getMessage(),
						MessageConstant.MESSAGE_CLOUD_LOG_FAILED_WRITING_STATUS_FILE.getMessage(messageArgs),
						"failed writing status file ," + tmpFilePath.getAbsolutePath(), monitorId, null);
				internalNotifyFlg = true;
			}
			writeSuccessFlg = false;
		}

		// 書き込み失敗からの回復時のみINTERNALを出力
		if (writeSuccessFlg && internalNotifyFlg) {
			String[] messageArgs = { monitorTarget };
			CloudLogfileMonitorManager.getInstance().sendMessage(monitorTarget, PriorityConstant.TYPE_INFO,
					MessageConstant.AGENT.getMessage(),
					MessageConstant.MESSAGE_CLOUD_LOG_RECOVER_WRITING_STATUS_FILE.getMessage(messageArgs),
					"recovered writing status file ," + tmpFilePath.getAbsolutePath(), monitorId, null);
			internalNotifyFlg = false;
		}
	}

	/**
	 * ファイル状態情報を格納しているファイルを削除する
	 */
	public void clear() {
		File statusFile = new File(getStatusTempFilePath());
		boolean statusFileExists = java.nio.file.Files.exists(Paths.get(getStatusTempFilePath()));

		if (statusFileExists) {
			log.debug("statusTrueFile is exists. execute delete.");
			if (!statusFile.delete()) {
				log.warn(String.format("clear() :don't delete file. path=%s", statusFile.getName()));
			}
		}

	}

	/**
	 * ファイル状態情報を初期化する。
	 */
	public void reset() {
		carryover = "";
		store();
	}

	public boolean isInitialized() {
		if (!initialized)
			initialize();
		return initialized;
	}

	public boolean isInitializedWithFileCreation() {
		return initializedWithFileCreation;
	}

}
