/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.binary.readingstatus;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.log.MonitorInfoWrapper;
import com.clustercontrol.fault.InvalidSetting;

/**
 * 読込状態管理クラス<br>
 * <br>
 * オブジェクトはThread毎に作成される.<br>
 */
public class RootReadingStatus {

	// クラス共通フィールド.
	/** ロガー */
	private static Log log = LogFactory.getLog(RootReadingStatus.class);

	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	// 読込情報を保存するファイル名等
	/** 全体設定情報のファイル名 */
	protected static final String file_rstatus = "rstatus.json";

	/** ディレクトリ読込状態を書き込むファイル名の先頭に付加 */
	protected static final String dir_prefix = "rs_";

	// 読込情報に書き込む項目名(rstatus.json).
	/** 監視対象ディレクトリ(項目名) */
	protected static final String monRootDirectoryName = "directory";

	/** 監視対象ファイル名(項目名) */
	protected static final String filename = "filename";

	/** 監視設定登録日時(項目名) */
	protected static final String updatedate = "updateDate";

	// 読込情報に書き込む項目名.
	/** 読込状態(項目名) */
	protected static final String readingStatus = "Reading";

	/** 前回のRS更新日時 */
	protected static final String lastUpdateRs = "lastUpdateReadingStatus";

	/** ファイルハンドラのポジション(項目名) */
	protected static final String position = "position";

	/** 前回監視時のサイズ(項目名) */
	protected static final String prevSize = "prevSize";

	/** 前回監視時の最終更新日時(項目名) */
	protected static final String monFileLastModTimeStamp = "lastModifiedTime";

	/** 前回スレッド実行時の最終更新日時(項目名) */
	protected static final String lastModTimeStampByThread = "lastModTimeStampByThread";

	/** 読み込んでいるファイルの先頭バイナリ(項目名) */
	protected static final String prefixString = "prefix";

	/** 初回監視実行済フラグ(true:実行済,false:未実行) */
	protected static final String didFirstRun = "didFirstRun";

	/** 監視実行中 */
	protected static final String runMonitor = "running";

	/** 増分監視の際、初回読込時に既存レコードをスキップするフラグ */
	protected static final String toSkipRecord = "toSkipRecord";

	/** 増分監視の際、レコードをスキップするサイズ */
	protected static final String skipSize = "skipSize";


	// 読込情報に書き込む各種ステータス.
	/** 読込状態_読込中 */
	protected static final String RS_OPEN_STRING = "Open";

	/** 読込状態_読込対象なし(更新されているファイルがない) */
	protected static final String RS_CLOSE_STRING = "Close";

	// 各種ステータス.
	/** 読込状態_読込中 */
	protected static final boolean RS_OPEN_FLAG = true;

	/** 読込状態_読込対象なし(更新されているファイルがない) */
	protected static final boolean RS_CLOSE_FLAG = false;

	// 読込状態管理クラス全体に関する情報.
	/** 読込状態の保存先パス(Agent毎) **/
	private final File storePath;

	// 監視設定毎の読込状態に関する情報.
	/** 監視設定毎の読込状態(キーは監視IDもしくは監視ジョブに紐づくID) */
	private Map<String, MonitorReadingStatus> monitorRSMap = new TreeMap<String, MonitorReadingStatus>();

	// スレッド毎の情報.
	/** 監視処理実行間隔 */
	private int runInterval;

	/**
	 * コンストラクタ<br>
	 * 
	 * @param miList
	 *            監視設定リスト
	 * @param baseDirectory
	 *            ファイルの読込状態を格納するディレクトリパス
	 */
	public RootReadingStatus(List<MonitorInfoWrapper> miList, int runInterval) {
		this.storePath = getRootStoreDirectory();
		this.init(miList, null);
		this.runInterval = runInterval;
	}

	/**
	 * ファイル構成初期化.<br>
	 * <br>
	 * 指定した監視設定に従って、管理するファイル構成を初期化する.<br>
	 * 
	 * @param miList
	 *            監視設定
	 * @param deleteMiList
	 *            削除された監視設定(不要なRSディレクトリ削除用).
	 */
	public void init(List<MonitorInfoWrapper> miList, Collection<MonitorInfoWrapper> deleteMiList) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// 引数ログ出力.
		if (log.isDebugEnabled()) {
			if (miList != null && deleteMiList != null) {
				log.debug(methodName + DELIMITER + String.format("start. miList size=%d, deleteMiList size=%d",
						miList.size(), deleteMiList.size()));
			} else if (miList == null && deleteMiList != null) {
				log.debug(methodName + DELIMITER
						+ String.format("start. miList size=null, deleteMiList size=%d", deleteMiList.size()));
			} else if (miList != null && deleteMiList == null) {
				log.debug(methodName + DELIMITER
						+ String.format("start. miList size=%d, deleteMiList size=null", miList.size()));
			} else {
				log.debug(methodName + DELIMITER + "start. miList size=null, deleteMiList size=null");
			}
		}

		// /opt/hinemos_agent/readingstatus配下の全ファイルのリストを取得.
		List<File> allFileDirList;
		if (this.storePath.exists()) {
			// 取得.
			File[] files = this.storePath.listFiles();
			if (files == null) {
				// 読込状態を管理しているファイルが存在しない場合は0件として初期化
				allFileDirList = Collections.<File> emptyList();
			} else {
				// 読込状態を管理しているファイルが格納されている場合はリストとして持つ.
				allFileDirList = new ArrayList<>(Arrays.asList(files));
			}
		} else {
			// そもそも格納先のディレクトリが存在しない場合は作成.
			allFileDirList = new ArrayList<>();
			if (!this.storePath.mkdirs()) {
				log.warn(this.storePath.getPath() + " is not created.");
				return;
			}
		}

		// 監視設定毎に読込状態管理クラス作成.
		Map<String, MonitorReadingStatus> curStatusMap = new TreeMap<>(this.monitorRSMap);
		Map<String, MonitorReadingStatus> newStatusMap = new TreeMap<>();
		for (MonitorInfoWrapper wrapper : miList) {

			MonitorReadingStatus monrs = curStatusMap.get(wrapper.getId());
			if (monrs == null) {
				// 既存のmonitorRSが存在しない場合(新規追加された監視設定)は追加.
				monrs = new MonitorReadingStatus(wrapper, this);
			} else {
				// 存在する場合は最新の監視設定を元に更新.
				monrs.updateMonRS(wrapper);
			}
			newStatusMap.put(monrs.getMonitorID(), monrs);

		}

		// 削除対象のRSディレクトリ取得.
		List<String> deleteDirList = new ArrayList<String>();
		if (deleteMiList != null) {
			for (MonitorInfoWrapper deleteMonitorInfo : deleteMiList) {
				deleteDirList.add(dir_prefix + deleteMonitorInfo.getId());
			}
		}

		// 新規作成等は終了.
		if (allFileDirList.isEmpty() || newStatusMap.isEmpty()) {
			this.monitorRSMap = newStatusMap;
			return;
		}

		// allFileDirListの内、newStatusMapに存在しないものは不要なので削除.
		// [hinemos_agentのHome]/readingstatus 配下のファイル(ディレクトリ)毎に削除判定.
		for (File fileDir : allFileDirList) {
			if (!fileDir.exists()) {
				// 存在しない場合はスキップ.
				continue;
			} else if (fileDir.isDirectory()) {
				// 監視設定削除された場合は削除(他のスレッドで動く監視設定で生成したディレクトリは削除しない).
				if (deleteDirList.contains(fileDir.getName())) {
					if (!FileUtils.deleteQuietly(fileDir)) {
						log.warn(methodName + DELIMITER + "failed to delete monitor directory = ["
								+ fileDir.getAbsolutePath() + "]");
					} else {
						log.debug(methodName + DELIMITER + "success to delete monitor directory = ["
								+ fileDir.getAbsolutePath() + "]");
					}
					continue;
				}
				// 先頭にプレフィックスついてないディレクトリはゴミなので削除.
				if (!fileDir.getName().contains(dir_prefix)) {
					if (!FileUtils.deleteQuietly(fileDir)) {
						log.warn(methodName + DELIMITER + "failed to delete no prefix directory = ["
								+ fileDir.getAbsolutePath() + "]");
					} else {
						log.debug(methodName + DELIMITER + "success to delete no prefix directory = ["
								+ fileDir.getAbsolutePath() + "]");
					}
				}
			} else {
				// ディレクトリ以外の場合はゴミなので削除.
				if (!fileDir.delete()) {
					log.warn(methodName + DELIMITER + "failed to delete file = [" + fileDir.getAbsolutePath() + "]");
				} else {
					log.debug(methodName + DELIMITER + "success to delete file = [" + fileDir.getAbsolutePath() + "]");
				}
			}
		}

		this.monitorRSMap = newStatusMap;
	}

	// 以下ReadingStatusUtil
	/**
	 * ReadingStatusのString変換.<br>
	 * 
	 * @param rs
	 *            booleanのReadingStatus
	 * @return String型のReadingStatus
	 */
	protected static String rsToString(boolean rs) {
		if (rs) {
			return RS_OPEN_STRING;
		} else {
			return RS_CLOSE_STRING;
		}
	}

	/**
	 * ReadingStatusのboolean変換.<br>
	 * 
	 * @param rs
	 *            String型のReadingStatus
	 * @return 引数不正の場合もtrue(読込対象として返却)
	 */
	protected static boolean rsToFlag(String rs) {
		if (RS_OPEN_STRING.equals(rs)) {
			return RS_OPEN_FLAG;
		} else if (RS_CLOSE_STRING.equals(rs)) {
			return RS_CLOSE_FLAG;
		} else {
			return RS_OPEN_FLAG;
		}
	}

	/**
	 * プロパティ取得.<br>
	 * 
	 * @param props
	 *            読込元
	 * @param propertyName
	 *            読込対象のプロパティ名
	 * @return プロパティ値、取得不可の場合はInvalidSettingをthrow
	 */
	protected static String getPropertyValue(Properties props, String propertyName) throws InvalidSetting {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		String propertyValue = props.getProperty(propertyName);
		if (propertyValue != null) {
			log.debug(methodName + DELIMITER
					+ String.format("success to get the property. name=%s, value=%s", propertyName, propertyValue));
			return propertyValue;
		}
		InvalidSetting e = new InvalidSetting(String.format("propety is not defined. property=[%s]", propertyName));
		throw e;
	}

	/**
	 * RS保存ディレクトリ取得.
	 */
	public static File getRootStoreDirectory() {
		String home = Agent.getAgentHome();
		String storepath = new File(new File(home), "readingstatus").getAbsolutePath();
		return new File(storepath);
	}

	// 以下各フィールドのgetter.
	/** 読込状態の保存先パス(Agent毎) **/
	public File getStorePath() {
		return this.storePath;
	}

	/** 監視設定毎の読込状態(キーは監視IDもしくは監視ジョブに紐づくID) */
	public Map<String, MonitorReadingStatus> getMonitorRSMap() {
		return this.monitorRSMap;
	}

	/** 監視処理実行間隔 */
	public int getRunInterval() {
		return this.runInterval;
	}
}