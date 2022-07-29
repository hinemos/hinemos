/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Files;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.rpa.util.RpaWindowsUtil;

/**
 * RPAシナリオエグゼキューターのPID（プロセスID）ファイルのクラス
 */
public class PidFile {
	/** ロガー */
	private static Log m_log = LogFactory.getLog(PidFile.class);

	/** インスタンス */
	private static PidFile singleton = new PidFile();


	/**
	 * コンストラクタ
	 */
	private PidFile() {
		// nothing
	}

	/**
	 * PIDファイルクラスのインスタンスを取得します。
	 *
	 * @return インスタンス
	 */
	public static PidFile getInstance() {
		return singleton;
	}

	/**
	 * PIDファイルを生成します。
	 *
	 * @return 成否
	 */
	public boolean create() {
		m_log.debug("create() : start.");

		File pidFile = getFile();
		if (pidFile == null) {
			m_log.debug("create() : failed, pidFile is null.");
			return false;
		}

		// pidファイル存在チェック
		if (pidFile.exists()) {
			// 存在する場合はエラー（通常はバッチで削除される）
			m_log.error("create() : pid file already exists, " + pidFile);
			return false;
		}

		if (!writePidToFile(pidFile)) {
			m_log.debug("create() : failed to write file. pidFile=" + pidFile);
			return false;
		}

		m_log.debug("create() : done.");
		return true;
	}

	/**
	 * PIDファイルを削除します。
	 *
	 * @return 成否
	 */
	public boolean delete() {
		m_log.debug("delete() : start.");

		File pidFile = getFile();
		if (pidFile == null) {
			m_log.debug("delete() : failed, pidFile is null.");
			return false;
		}

		try {
			m_log.debug("delete() : delete pidFile=" + pidFile);
			Files.delete(pidFile.toPath());
		} catch (IOException e) {
			m_log.error("delete() : failed to delete. pidFile=" + pidFile + ", " + e.getMessage(), e);
			return false;
		}

		m_log.debug("delete() : done.");
		return true;
	}
	
	/**
	 * PIDファイルを取得します。
	 *
	 * @return PIDファイル
	 */
	private File getFile() {
		m_log.debug("getFile() : start.");

		String useername = RpaWindowsUtil.getUseername();
		if (useername == null || useername.isEmpty()) {
			m_log.error("getFile() : useername is null or empty.");
			return null;
		}

		File pidFile = null;
		try {
			pidFile = RpaWindowsUtil.getPidFile(useername);
		} catch (HinemosUnknown | InterruptedException e) {
			m_log.error("getFile() : error occurred, " + e.getMessage(), e);
		}

		m_log.debug("getFile() : done. pidfile=" + pidFile);
		return pidFile;
	}

	/**
	 * PIDファイルにプロセスIDを書き込みます。
	 *
	 * @param pidFile PIDファイル
	 * @return 成否
	 */
	private boolean writePidToFile(File pidFile) {
		m_log.debug("writePidToFile() : start. pidFile=" + pidFile);

		// PID取得
		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		String vm = bean.getName();
		m_log.debug("writePidToFile() : vm=" + vm);
		if (vm == null || vm.length() == 0) {
			m_log.error("writePidToFile() : vm is null or empty, vm=" + vm);
			return false;
		}
		String pid = vm.split("@")[0];
		m_log.debug("writePidToFile() : pid=" + pid);

		// 書き込み
		m_log.debug("writePidToFile() : writing pid file. pidFile=" + pidFile);
		try (FileWriter fw = new FileWriter(pidFile, false)) {
			fw.write(pid);
		} catch (IOException e) {
			m_log.error("writePidToFile() : failed to write pid=" + pid + " to pid file. " + e.getMessage(), e);
			return false;
		}

		m_log.debug("writePidToFile() : done.");
		return true;
	}

}
