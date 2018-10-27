/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.action;

import java.io.File;
import java.io.FileWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceContext;

import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Messages;

/**
 * ログレコード(文字列)のダウンロードスレッド.
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
public class TextDataDownloader implements Runnable {

	// ログ出力関連.
	/** ログ */
	private static Log m_log = LogFactory.getLog(TextDataDownloader.class);
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

	// BinaryDataDownloaderと共通のフィールド.
	/** ダウンロード開始時間(性能出力用) */
	private long startMsec;
	/** クライアント出力ファイル名 */
	private String fileName;
	/** ファイル選択ダイアログ選択ファイルパス */
	private String selectedFilePath;

	// TextDataDownloader独自フィールド.
	/** ファイル出力メッセージ */
	private String expMsg;

	/**
	 * 単ファイルダウンロード向けコンストラクタ
	 * 
	 * @param startMsec
	 *            ダウンロード開始時間(性能出力用)
	 * @param fileName
	 *            ファイル選択ダイアログデフォルトファイル名
	 * @param selectedFilePath
	 *            ファイル選択ダイアログ選択ファイルパス
	 * @param expMsg
	 *            ファイル出力メッセージ
	 */
	public TextDataDownloader(long startMsec, String fileName, String selectedFilePath, String expMsg) {
		this.startMsec = startMsec;
		this.fileName = fileName;
		this.selectedFilePath = selectedFilePath;
		this.expMsg = expMsg;
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

		this.originMsgExecute();

	}

	/**
	 * オリジナルメッセージ出力.
	 */
	private void originMsgExecute() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");

		String action = Messages.getString("download");

		try (FileWriter fw = new FileWriter(new File(selectedFilePath))) {
			this.progress = 10; // ファイル書込み前(10%)
			fw.write(this.expMsg);
			this.progress = 100; // 出力完了(100%)
			// 性能測定結果ログ出力.
			if (m_log.isDebugEnabled()) {
				long downloadMsec = HinemosTime.getDateInstance().getTime() - startMsec;
				m_log.debug(methodName + DELIMITER + String.format(
						"end execute original message. processing time=%d, fileName=[%s]", downloadMsec, fileName));
			}
		} catch (Exception e) {
			m_log.error(e);
			Object[] args = new Object[] { Messages.getString("file"), action, Messages.getString("failed"),
					HinemosMessage.replace(e.getMessage()) };
			this.cancelMessage = Messages.getString("message.infra.action.result", args);
			this.canceled = true;
			this.progress = 100;
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
