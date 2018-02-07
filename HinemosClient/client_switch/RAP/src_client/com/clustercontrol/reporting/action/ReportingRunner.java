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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.swt.widgets.Display;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.client.ui.util.FileDownloader;
import com.clustercontrol.reporting.preference.ReportingPreferencePage;
import com.clustercontrol.reporting.util.ReportingEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.reporting.HinemosUnknown_Exception;
import com.clustercontrol.ws.reporting.InvalidRole_Exception;
import com.clustercontrol.ws.reporting.InvalidUserPass_Exception;
import com.clustercontrol.ws.reporting.NotifyNotFound_Exception;
import com.clustercontrol.ws.reporting.ReportingInfo;
import com.clustercontrol.ws.reporting.ReportingNotFound_Exception;

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

	private ReportingInfo reportingInfo;

	private ReportingEndpointWrapper wrapper;
	
	final private Display display;
	
	
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
	public ReportingRunner(String reportId, ReportingInfo info, 
			ReportingEndpointWrapper wrapper, Display display) {
		super();
		this.reportId = reportId;
		this.reportingInfo = info;
		this.wrapper = wrapper;
		this.display = display;

		m_log.debug("ReportingRunner() " + "reportId = " + reportId);
		
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
			throws HinemosUnknown_Exception, InvalidRole_Exception,
			InvalidUserPass_Exception, ReportingNotFound_Exception,
			NotifyNotFound_Exception {
		ReportingEndpointWrapper wrapper = ReportingEndpointWrapper.getWrapper(managerName);
		downloadFileList = wrapper.createReportingFileWithParam(this.reportId,
						this.reportingInfo);

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

		FileOutputStream fileOutputStream = null;
		DataHandler handler = null;
		try {
			// 指定回数だけファイル存在確認をする
			m_log.info("download report file = " + fileName + ", waitCount = "
					+ waitCount);
			for (int i = 0; i < waitCount; i++) {

				if (!this.canceled) {
					Thread.sleep(waitSleep);
					m_log.debug("download report file = " + fileName
							+ ", create check. count = " + i);

					// クライアントのヒープが小さい場合は下記の行で落ちる。(out of memory)
					if(wrapper == null) {
						throw new Exception("failed to download");
					}
					handler = wrapper.downloadReportingFile(fileName);
					if (handler != null) {
						m_log.info("download report file = " + fileName
								+ ", created !");
						break;
					}
				}
			}
			if (handler == null) {
				m_log.info("download handler is null");
				setCancelMessage(Messages
						.getString("message.reporting.25")
						+ ": "
						+ Messages
								.getString("message.reporting.26"));
				setCanceled(true);
				return;
			}

			// OSが異なると、セパレータが違うので正しくファイル名が取得できないので
			// クライアントのPSのセパレータに置換する
			fileName = fileName.replace("\\", File.separator);
			fileName = fileName.replace("/", File.separator);
			m_log.debug("fileName = " + fileName);
			final String exactFileName = new File(fileName).getName();
			
			m_log.info("exactFileName = " + exactFileName);
			
			final File file = new File(exactFileName);
			file.createNewFile();
			fileOutputStream = new FileOutputStream(file);
			handler.writeTo(fileOutputStream);

			m_log.info("download report file  = " + exactFileName + ", succeed !");
			m_log.debug("download() succeed!");
			
			if (ClusterControlPlugin.isRAP()) {
				
				UISession uiSession = RWT.getUISession();
				uiSession.exec(new Runnable() {
					public void run() {
						display.asyncExec( new Runnable() {
							public void run() {
								FileDownloader.openBrowser(display.getActiveShell(), file.getAbsolutePath(), exactFileName);
								m_log.info("FileDownloader.openBrowser finish.");
								FileDownloader.cleanup(file.getAbsolutePath());
								m_log.info("FileDownloader.cleanup finish.");
							}
						});
					}
				});
			}
			
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
			m_log.warn("canceled:" + getCancelMessage());
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