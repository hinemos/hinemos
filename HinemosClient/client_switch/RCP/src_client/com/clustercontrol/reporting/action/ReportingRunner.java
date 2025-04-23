/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.CreateReportingFileResponse;
import org.openapitools.client.model.CreateReportingFileRequest;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.ReportFileCreateFailed;
import com.clustercontrol.fault.ReportFileNotFound;
import com.clustercontrol.fault.ReportingNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.reporting.preference.ReportingPreferencePage;
import com.clustercontrol.reporting.util.ReportingRestClientWrapper;
import com.clustercontrol.rest.util.RestCodecUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;

/**
 * レポート作成を実行するクラス
 * 
 * @version 1.0.0
 * @since 1.0.0
 * 
 */
public class ReportingRunner implements Runnable {
	private static Log m_log = LogFactory.getLog(ReportingRunner.class);

	// input
	private String reportId;

	private String folderName = null;

	private CreateReportingFileRequest reportingInfo;

	private ReportingRestClientWrapper wrapper;
	
	
	// ダウンロードを試みる時間間隔（ミリ秒）
	private int waitSleep = 60000;
	private int waitCount;

	// レポートダウンロード待ち時間（分）
	private final int DOWNLOAD_MAX_WAIT = 30;
	private int dlMaxWait = DOWNLOAD_MAX_WAIT;

	// ダウンロードファイル名
	List<String> downloadFileList = null;

	private boolean canceled;
	private String cancelMessage = null;

	/**
	 * デフォルトコンストラクタ
	 * 
	 * @param reportId
	 *            レポートID
	 * @param folderName
	 *            フォルダ名
	 * @param info
	 *            レポーティング情報
	 */
	public ReportingRunner(String reportId, String folderName,
			CreateReportingFileRequest info, ReportingRestClientWrapper wrapper) {
		super();
		this.reportId = reportId;
		this.folderName = folderName;
		this.reportingInfo = info;
		this.wrapper = wrapper;

		m_log.debug("ReportingRunner() " + "reportId = " + reportId
				+ " folderName = " + folderName);
		
		// レポートダウンロード待ち時間とチェック間隔をプレファレンスページから取得する
		dlMaxWait = ClusterControlPlugin.getDefault().getPreferenceStore().getInt(
				ReportingPreferencePage.P_DL_MAX_WAIT);
		int waitSleepMin = ClusterControlPlugin.getDefault().getPreferenceStore().getInt(
				ReportingPreferencePage.P_DL_CHECK_INTREVAL);
		
		waitCount = dlMaxWait / waitSleepMin;
		
		// 分をミリ秒に変更
		waitSleep = waitSleepMin * 60 * 1000; 
		
		m_log.debug("ReportingRunner() " + "dlMaxWait = " + dlMaxWait
				+ ", waitCount = " + waitCount);
	}

	/**
	 * コンストラクタで指定された条件で、マネージャサーバにレポートをファイルとして作成する
	 * 
	 * @return ファイル名リスト
	 */
	public List<String> create(String managerName)
			throws HinemosUnknown, InvalidRole, InvalidUserPass, ReportingNotFound, NotifyNotFound, InvalidSetting, RestConnectFailed {
		ReportingRestClientWrapper wrapper = ReportingRestClientWrapper.getWrapper(managerName);
		CreateReportingFileResponse listRes = wrapper.createReportingFileManual(this.reportId, this.reportingInfo);
		downloadFileList = listRes.getFileNameList();

		// for debug
		if (m_log.isDebugEnabled()) {
			if (downloadFileList == null || downloadFileList.size() == 0) {
				m_log.debug("create() downloadFileList is null");
			} else {
				for (String fileName : downloadFileList) {
					m_log.debug("create() downloadFileName = " + fileName);
				}
			}
		}

		return downloadFileList;
	}

	/**
	 * マネージャサーバで作成したレポートファイルをダウンロードして指定のフォルダに配置します
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	public void download(String managerName, String fileName) {
		m_log.debug("download() downloadFileName = " + fileName);
		m_log.info("download report file  = " + fileName);
		
		String downloadFileEncoded = RestCodecUtil.stringEncode(fileName); 

		FileOutputStream fileOutputStream = null;
		DataHandler handler = null;
		File file = null;
		boolean isClientTimeout = true;
		try {
			// 指定回数だけファイル存在確認をする
			m_log.info("download report file = " + fileName + ", waitCount = "
					+ waitCount);
			for (int i = 0; i < waitCount; i++) {

				if (!this.canceled) {
					Thread.sleep(waitSleep);
					m_log.debug("download report file = " + fileName
							+ ", create check. count = " + i);

					if(wrapper == null) {
						throw new Exception("failed to download");
					}
					try {
						file = wrapper.downloadReportingFile(downloadFileEncoded);
						m_log.info("download report file = " + fileName
								+ ", created !");
						break;
					} catch (ReportFileNotFound e) {
						m_log.debug("download report file =" + e.getMessage() + "," + e.getClass().getSimpleName());
						continue;
					} catch (ReportFileCreateFailed e) {
						m_log.debug("download report file =" + e.getMessage() + "," + e.getClass().getSimpleName());
						isClientTimeout = false;
						break;
					}
				}
			}
			if (file == null) {
				m_log.info("download file is null");
				if(isClientTimeout) {
					setCancelMessage(Messages
							.getString("message.reporting.25")
							+ ": "
							+ Messages
							.getString("message.reporting.26"));
				} else {
					setCancelMessage(Messages
							.getString("message.reporting.25")
							+ ": "
							+ Messages
							.getString("message.reporting.44"));
				}
				setCanceled(true);
				return;
			}
			
			FileDataSource source = new FileDataSource(file);
			handler = new DataHandler(source);

			final String exactFileName = new File(fileName).getName();
			final File createFile = new File(this.folderName + "\\" + exactFileName);
			boolean rtn = createFile.createNewFile();
			if (!rtn) {
				throw new HinemosUnknown("File creation failed. fileName=" + this.folderName + "\\" + exactFileName);
			}
			fileOutputStream = new FileOutputStream(createFile);
			handler.writeTo(fileOutputStream);

			m_log.info("download report file  = " + exactFileName + ", succeed !");
			m_log.debug("download() succeed!");
			
		} catch (IOException e) {
			String errMessage = HinemosMessage.replace(e.getMessage());
			setCancelMessage(Messages
					.getString("message.reporting.27")
					+ ":"
					+ errMessage);
			setCanceled(true);
			m_log.warn("download()", e);

		} catch (Exception e) {
			String errMessage = HinemosMessage.replace(e.getMessage());
			setCancelMessage(Messages
					.getString("message.reporting.25")
					+ ":"
					+ errMessage);
			setCanceled(true);
			m_log.warn("download()", e);
		} finally {
			try {
				if (fileOutputStream != null) {
					fileOutputStream.close();
				}
			} catch (IOException e) {
				String errMessage = HinemosMessage.replace(e.getMessage());
				setCancelMessage(Messages
						.getString("message.reporting.27")
						+ ":"
						+ errMessage);
				setCanceled(true);
				m_log.warn("download()", e);
			}
		}
	}

	/**
	 * ファイルへのエクスポートを実行します。
	 */
	@Override
	public void run() {
		try{
			if (isCanceled()) {
				m_log.warn("canceled:" + getCancelMessage());
				return;
			}
	
			if (downloadFileList == null || downloadFileList.size() == 0) {
				if (!isCanceled()) {
					setCancelMessage(Messages.getString("message.reporting.30"));
					setCanceled(true);
				}
				m_log.warn("canceled:" + getCancelMessage());
				return;
			}
			
			String managerName = "";
	
			// ダウンロード
			download(managerName, downloadFileList.get(0));
			if (isCanceled()) {
				//エラーダイアログを表示
				UIManager.checkAsyncExec(new Runnable(){
					@Override
					public void run() {
						MessageDialog.openError(
								null,
								Messages.getString("message.reporting.25"),
								Messages.getString("message.reporting.27") + "\r\n" + downloadFileList.get(0) + "\r\n"
										+ getCancelMessage());
					}}
				);
				m_log.warn("canceled:" + getCancelMessage());
			}
		}catch(Throwable e){
			// might be out of memory
			m_log.error(e);
		}
	}

	/**
	 * レポーティング実行処理を中止します。
	 * 
	 * @param 処理を中止したい場合にはtrueを設定します
	 * 
	 */
	public void setCanceled(boolean b) {
		this.canceled = b;
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

	/**
	 * キャンセル時のメッセージの設定
	 * 
	 * @param cancelMessage
	 */
	public void setCancelMessage(String cancelMessage) {
		this.cancelMessage = cancelMessage;
	}
}