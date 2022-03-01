/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceContext;
import org.openapitools.client.model.DownloadRpaScenarioOperationResultRecordsRequest;

import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Messages;

/**
 * シナリオ実績レコードのダウンロードスレッド.
 */
public class RpaScenarioOperationResultDataDownloader implements Runnable {

	// ログ出力関連.
	/** ログ */
	private static Log m_log = LogFactory.getLog(RpaScenarioOperationResultDataDownloader.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	// RecordDataWriterを参照にしたフィールド.
	/** 進捗（%表記 0～100） */
	private int progress;
	/** サービスコンテキスト */
	private ServiceContext context = null;
	/** エラー発生等により終了 */
	private boolean canceled = false;
	/** キャンセル事由 */
	private String cancelMessage = null;

	// BinaryDataDownloader独自フィールド.
	/** マネージャー */
	private String manager;
	/** ダウンロード開始時間(性能出力用) */
	private long startMsec;
	/** ファイル選択ダイアログ選択ファイルパス */
	private String selectedFilePath;
	/** 選択データに紐づくダウンロード条件リスト */
	private DownloadRpaScenarioOperationResultRecordsRequest downloadRecordsRequest;

	/**
	 * コンストラクタ
	 */
	public RpaScenarioOperationResultDataDownloader(String manager, long startMsec,
			String selectedFilePath, DownloadRpaScenarioOperationResultRecordsRequest downloadRecordsRequest) {
		this.manager = manager;
		this.startMsec = startMsec;
		this.selectedFilePath = selectedFilePath;
		this.downloadRecordsRequest = downloadRecordsRequest;
	}

	/**
	 * スレッドメイン処理.
	 */
	@Override
	public void run() {
		// 開始
		this.progress = 0; // スレッド開始(0%)

		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");

		ContextProvider.releaseContextHolder();
		ContextProvider.setContext(context);

		this.multipleFileDownload();
	}

	/**
	 * 複数ファイルダウンロード.
	 */
	private void multipleFileDownload() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");

		// File
		File file = new File( this.selectedFilePath );
		// 書込みストリーム生成.
		FileOutputStream fos = null;
		String action = Messages.getString("download");

		try {
			fos = new FileOutputStream(file);
			
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(manager);
			
			this.progress = 10; // マネージャ送信前(10%)

			File downloadFile = wrapper.downloadRecords(this.downloadRecordsRequest);
			
			FileDataSource source = new FileDataSource(downloadFile);
			DataHandler dh = new DataHandler(source);

			this.progress = 40; // マネージャサイドの動作完了(40%)

			dh.writeTo(fos);
			this.progress = 90; // クライアントにダウンロード完了(90%)

			this.progress = 100; // マネージャー一時ファイル削除完了(100%)

			// 性能測定結果ログ出力.
			if (m_log.isDebugEnabled()) {
				long downloadMsec = HinemosTime.getDateInstance().getTime() - startMsec;
				m_log.debug(methodName + DELIMITER + String.format(
						"end download multiple records. processing time=%d, fileName=[%s]", downloadMsec, this.downloadRecordsRequest.getFilename()));
			}
		} catch (Exception e) {
			m_log.error(e);
			Object[] args = new Object[] { Messages.getString("file"), action, Messages.getString("failed"),
					HinemosMessage.replace(e.getMessage()) };
			this.cancelMessage = Messages.getString("message.infra.action.result", args);
			this.canceled = true;
			this.progress = 100;
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
	}

	// setter.
	/**
	 * サービスコンテキスト設定.
	 */
	public void setContext(ServiceContext context) {
		this.context = context;
	}

	// getter.
	/**
	 * 現在までに何％のダウンロードが完了したのかを取得します。
	 *
	 * @return 進捗（%表記 0～100）
	 */
	public int getProgress() {
		return progress;
	}

	/**
	 * キャンセルの有無を取得
	 * 
	 * @return
	 */
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * キャンセル時のメッセージの取得
	 * 
	 * @return
	 */
	public String getCancelMessage() {
		return cancelMessage;
	}

}
