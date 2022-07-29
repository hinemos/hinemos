/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.scenario.factory;

import java.io.File;
import org.apache.log4j.Logger;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.platform.HinemosPropertyDefault;
import com.clustercontrol.rest.endpoint.rpa.dto.DownloadRpaScenarioOperationResultRecordsRequest;

/**
 * シナリオ実績をDBから取得してclientにダウンロード
 * 
 * ※トランザクション制御は呼出元で実施
 */
public class DownloadRpaScenarioOperationResult {

	/** ログ出力用インスタンス */
	private static Logger m_log = Logger.getLogger(DownloadRpaScenarioOperationResult.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	// 共通フィールド_コンストラクタ設定.
	DownloadRpaScenarioOperationResultRecordsRequest queryInfo;

	// 共通フィールド_一時ファイル出力関連.
	/** 一時出力先フォルダ(絶対パス) */
	private String dirName;
	/** 一時出力ファイル名(notパス) */
	private String fileName;
	/** 一時出力ファイルオブジェクト */
	private File tmpFile;

	/**
	 * コンストラクタ
	 */
	public DownloadRpaScenarioOperationResult(DownloadRpaScenarioOperationResultRecordsRequest queryInfo) {
		this.queryInfo = queryInfo;
	}

	/**
	 * 一時出力先のファイル生成
	 */
	public void createTemporaryFile(String clientFileName, String clientName) throws HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + String.format("start."));

		// 一時出力先のファイル名設定.
		if (clientFileName == null || clientFileName.isEmpty()) {
			this.fileName = this.queryInfo.getFilename();
		} else {
			this.fileName = clientFileName;
		}

		// 一時出力先のディレクトリ生成.
		String resultExportDir = HinemosPropertyDefault.rpa_scenario_operation_result_export_dir.getStringValue();
		File tmpDirectory = new File(resultExportDir, clientName);
		this.dirName = tmpDirectory.getAbsolutePath();
		if (!tmpDirectory.exists()) {
			if (!tmpDirectory.mkdir()) {
				String errorMessage = String.format("failed to make temporary directory on manager. directory=[%s]",
						this.dirName);
				m_log.warn(methodName + DELIMITER + errorMessage);
				throw new HinemosUnknown(errorMessage);
			} else {
				m_log.debug(methodName + DELIMITER + String
						.format("successed to make temporary directory on manager. directory=[%s]", this.dirName));
			}
		}
	}

	/** 一時出力先フォルダ */
	public String getDirName() {
		return this.dirName;
	}

	/** 一時出力ファイル名 */
	public String getFileName() {
		return this.fileName;
	}

	/** 一時出力ファイルオブジェクト */
	public File getTmpFile() {
		return this.tmpFile;
	}
}
