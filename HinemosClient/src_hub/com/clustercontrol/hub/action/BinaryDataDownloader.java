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
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceContext;
import org.openapitools.client.model.DownloadBinaryRecordKeyRequest;
import org.openapitools.client.model.DownloadBinaryRecordRequest;
import org.openapitools.client.model.DownloadBinaryRecordsKeyRequest;
import org.openapitools.client.model.DownloadBinaryRecordsRequest;
import org.openapitools.client.model.QueryCollectBinaryDataRequest;

import com.clustercontrol.collect.util.CollectRestClientWrapper;
import com.clustercontrol.hub.dto.DataResponse;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Messages;

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
	private DataResponse selectBinaryData;
	// 複数ファイル向けフィールド
	/** 選択データに紐づくダウンロード条件リスト */
	private DownloadBinaryRecordsRequest binaryDownloadList;

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
	public BinaryDataDownloader(String manager, long startMsec, String fileName,
			String selectedFilePath, DataResponse selectBinaryData) {
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
	public BinaryDataDownloader(String manager, long startMsec, String fileName,
			String selectedFilePath, DownloadBinaryRecordsRequest binaryDownloadList) {
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
		
		// File
		File file = new File( this.selectedFilePath );
		FileOutputStream fos = null;

		CollectRestClientWrapper wrapper = CollectRestClientWrapper.getWrapper(manager);
		
		String action = Messages.getString("download");
		try {
			fos = new FileOutputStream(file);

			// バイナリ収集の検索条件をセット.
			List<QueryCollectBinaryDataRequest> queryCollectBinaryDataRequestList = new ArrayList<>();
			QueryCollectBinaryDataRequest queryCollectBinaryDataRequest = new QueryCollectBinaryDataRequest();
			queryCollectBinaryDataRequest.setFacilityId(selectBinaryData.getFacilityId());
			queryCollectBinaryDataRequest.setMonitorId(selectBinaryData.getMonitorId());
			queryCollectBinaryDataRequestList.add(queryCollectBinaryDataRequest);
			
			//Request
			DownloadBinaryRecordRequest downloadBinaryRecordRequest = new DownloadBinaryRecordRequest();
			downloadBinaryRecordRequest.setQueryCollectBinaryDataRequest(queryCollectBinaryDataRequestList);
			downloadBinaryRecordRequest.setFilename(this.fileName);
			
			// キー情報セット
			List<DownloadBinaryRecordKeyRequest> recordList = new ArrayList<>();
			DownloadBinaryRecordKeyRequest record = new DownloadBinaryRecordKeyRequest();
			record.setCollectId(selectBinaryData.getCollectId());
			record.setDataId(selectBinaryData.getDataId());
			recordList.add(record);
			downloadBinaryRecordRequest.setRecords(recordList);

			this.progress = 10; // マネージャ送信前(10%)

			File downloadFile = wrapper.downloadBinaryRecord(downloadBinaryRecordRequest);
			FileDataSource source = new FileDataSource(downloadFile);
			DataHandler dh = new DataHandler(source);

			this.progress = 40; // マネージャサイドの動作完了(40%)

			dh.writeTo(fos);

			this.progress = 90; // クライアントにダウンロード完了(90%)

			this.progress = 100; // マネージャー一時ファイル削除完了(100%)

			// 性能測定結果の出力.
			if (m_log.isDebugEnabled()) {
				long downloadMsec = HinemosTime.getDateInstance().getTime() - startMsec;
				m_log.debug(methodName + DELIMITER + String.format(
						"end download a single record. processing time=%d, facilityId=%s, monitorId=%s, fileName=[%s]",
						downloadMsec, selectBinaryData.getFacilityId(), selectBinaryData.getMonitorId(), fileName));
			}
		} catch (RuntimeException e) {
			// findbugs対応 RuntimeExceptionのcatchを明示化
			m_log.warn(e);
			Object[] args = new Object[] { Messages.getString("file"), action, Messages.getString("failed"),
					HinemosMessage.replace(e.getMessage()) };
			this.cancelMessage = Messages.getString("message.infra.action.result", args);
			this.canceled = true;
			this.progress = 100;
			return;
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

		// File
		File file = new File( this.selectedFilePath );
		// 書込みストリーム生成.
		FileOutputStream fos = null;
		String action = Messages.getString("download");

		try {
			fos = new FileOutputStream(file);
			
			CollectRestClientWrapper wrapper = CollectRestClientWrapper.getWrapper(manager);
			
			DownloadBinaryRecordsRequest downloadBinaryRecordsRequest = binaryDownloadList;
			downloadBinaryRecordsRequest.setFilename(fileName);
			
			this.progress = 10; // マネージャ送信前(10%)

			File downloadFile = wrapper.downloadBinaryRecords(downloadBinaryRecordsRequest);
			
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
