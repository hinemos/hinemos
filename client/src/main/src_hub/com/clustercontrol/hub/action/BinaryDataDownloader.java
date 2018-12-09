/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.activation.DataHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceContext;

import com.clustercontrol.binary.util.BinaryEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.hub.BinaryDownloadDTO;
import com.clustercontrol.ws.hub.BinaryQueryInfo;
import com.clustercontrol.ws.hub.StringData;

/**
 * 選択したバイナリレコードのダウンロードスレッド.
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
public class BinaryDataDownloader implements Runnable {

	// ログ出力関連.
	/** ログ */
	private static Log m_log = LogFactory.getLog(BinaryDataDownloader.class);
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
	/** クライアント名(マネージャ出力一時ファイル管理用) */
	private String clientName;
	/** 単ファイルダウンロードフラグ(true:単ファイル,false複数ファイル) */
	private boolean singleFile;
	/** マネージャー */
	private String manager;
	/** ダウンロード開始時間(性能出力用) */
	private long startMsec;
	/** クライアント出力ファイル名 */
	private String fileName;
	/** ファイル選択ダイアログ選択ファイルパス */
	private String selectedFilePath;
	// 単ファイル向けフィールド.
	/** 選択データ */
	private StringData selectBinaryData;
	// 複数ファイル向けフィールド
	/** 選択データに紐づくダウンロード条件リスト */
	private List<BinaryDownloadDTO> binaryDownloadList;

	/**
	 * 単ファイルダウンロード向けコンストラクタ
	 * 
	 * @param clientName
	 *            クライアント名(マネージャ出力一時ファイル管理用)
	 * @param manager
	 *            接続マネージャー
	 * @param startMsec
	 *            ダウンロード開始時間(性能出力用)
	 * @param defaultFileName
	 *            ファイル選択ダイアログデフォルトファイル名
	 * @param selectedFilePath
	 *            ファイル選択ダイアログ選択ファイルパス
	 * @param selectBinaryData
	 *            選択データ
	 * @param recordKey
	 *            選択データのレコードキー
	 */
	public BinaryDataDownloader(String clientName, String manager, long startMsec, String fileName,
			String selectedFilePath, StringData selectBinaryData) {
		this.clientName = clientName;
		this.singleFile = true;
		this.manager = manager;
		this.startMsec = startMsec;
		this.fileName = fileName;
		this.selectedFilePath = selectedFilePath;
		this.selectBinaryData = selectBinaryData;
	}

	/**
	 * 複数ファイルダウンロード向けコンストラクタ
	 * 
	 * @param clientName
	 *            クライアント名(マネージャ出力一時ファイル管理用)
	 * @param manager
	 *            接続マネージャー
	 * @param startMsec
	 *            ダウンロード開始時間(性能出力用)
	 * @param defaultFileName
	 *            ファイル選択ダイアログデフォルトファイル名
	 * @param selectedFilePath
	 *            ファイル選択ダイアログ選択ファイルパス
	 */
	public BinaryDataDownloader(String clientName, String manager, long startMsec, String fileName,
			String selectedFilePath, List<BinaryDownloadDTO> binaryDownloadList) {
		this.clientName = clientName;
		this.singleFile = false;
		this.manager = manager;
		this.startMsec = startMsec;
		this.fileName = fileName;
		this.selectedFilePath = selectedFilePath;
		this.binaryDownloadList = binaryDownloadList;
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

		if (singleFile) {
			this.singleFileDownload();
		} else {
			this.multipleFileDownload();
		}

	}

	/**
	 * 単ファイルダウンロード.
	 */
	private void singleFileDownload() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");

		BinaryEndpointWrapper wrapper = BinaryEndpointWrapper.getWrapper(manager);
		FileOutputStream fos = null;
		String action = Messages.getString("download");
		try {
			// キー情報をセット.
			BinaryQueryInfo queryInfo = new BinaryQueryInfo();
			queryInfo.setFacilityId(selectBinaryData.getFacilityId());
			queryInfo.setMonitorId(selectBinaryData.getMonitorId());
			this.progress = 10; // マネージャ送信前(10%)

			DataHandler dh = wrapper.downloadBinaryRecord(queryInfo, selectBinaryData.getPrimaryKey(), fileName,
					clientName);
			this.progress = 40; // マネージャサイドの動作完了(40%)

			fos = new FileOutputStream(new File(selectedFilePath));
			dh.writeTo(fos);
			this.progress = 90; // クライアントにダウンロード完了(90%)

			// マネージャーの一時保存ファイルを削除.
			wrapper.deleteDownloadedBinaryRecord(fileName, clientName);
			this.progress = 100; // マネージャー一時ファイル削除完了(100%)

			// 性能測定結果の出力.
			if (m_log.isDebugEnabled()) {
				long downloadMsec = HinemosTime.getDateInstance().getTime() - startMsec;
				m_log.debug(methodName + DELIMITER + String.format(
						"end download a single record. processing time=%d, facilityId=%s, monitorId=%s, fileName=[%s]",
						downloadMsec, selectBinaryData.getFacilityId(), selectBinaryData.getMonitorId(), fileName));
			}
		} catch (Exception e) {
			m_log.warn(e);
			Object[] args = new Object[] { Messages.getString("file"), action, Messages.getString("failed"),
					HinemosMessage.replace(e.getMessage()) };
			this.cancelMessage = Messages.getString("message.infra.action.result", args);
			this.canceled = true;
			this.progress = 100;
			return;
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * 複数ファイルダウンロード.
	 */
	private void multipleFileDownload() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");

		FileOutputStream fos = null;
		String action = Messages.getString("download");

		try {
			BinaryEndpointWrapper wrapper = BinaryEndpointWrapper.getWrapper(manager);
			this.progress = 10; // マネージャ送信前(10%)

			DataHandler dh = wrapper.downloadBinaryRecords(fileName, binaryDownloadList, clientName);
			this.progress = 40; // マネージャサイドの動作完了(40%)

			// データハンドラー取得エラー.
			if (dh == null) {
				String errorMessage = "failed to get DataHandler.";
				// データ不正.
				Object[] args = new Object[] { Messages.getString("file"), action, Messages.getString("failed"),
						errorMessage };
				this.cancelMessage = Messages.getString("message.infra.action.result", args);
				this.canceled = true;
				this.progress = 100;
				return;
			}

			// 書込みストリーム生成.
			fos = new FileOutputStream(new File(selectedFilePath));
			dh.writeTo(fos);
			this.progress = 90; // クライアントにダウンロード完了(90%)

			// 書込み完了したファイル削除.
			wrapper.deleteDownloadedBinaryRecord(fileName, clientName);
			this.progress = 100; // マネージャー一時ファイル削除完了(100%)

			// 性能測定結果ログ出力.
			if (m_log.isDebugEnabled()) {
				long downloadMsec = HinemosTime.getDateInstance().getTime() - startMsec;
				m_log.debug(methodName + DELIMITER + String.format(
						"end download multiple records. processing time=%d, fileName=[%s]", downloadMsec, fileName));
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
