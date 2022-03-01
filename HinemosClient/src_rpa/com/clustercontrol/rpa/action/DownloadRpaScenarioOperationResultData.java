/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.action;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.DownloadRpaScenarioOperationResultRecordsRequest;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.client.ui.util.FileDownloader;
import com.clustercontrol.util.DateTimeStringConverter;
import com.clustercontrol.util.FileUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Messages;

/**
 * シナリオ実績分析用データをダウンロード
 */
public class DownloadRpaScenarioOperationResultData {

	/** ログ */
	private static Log m_log = LogFactory.getLog(DownloadRpaScenarioOperationResultData.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	/** クライアント内連番(クライアントサーバー管理) */
	private static int clientSequeance = 0;

	/** クライアント内連番(インスタンス保持用) */
	private int clientSeq = 0;

	/**
	 * クライアント内連番increment.
	 */
	private static synchronized int addSequeance() {
		return clientSequeance++;
	}

	/**
	 * シナリオ実績レコードをユーザー指定したフォルダにダウンロード. 
	 */
	public void executeRecords(Shell parent, String manager, DownloadRpaScenarioOperationResultRecordsRequest request) throws UnknownHostException {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");

		// 表示レコードがない場合は処理せず終了
		if (request == null) {
			return;
		}
		
		String fileName = null;

		// 出力用のファイル名取得できたかチェック.
		String action = Messages.getString("download");

		// ユーザーに出力先ファイルを選択させるためのダイアログ生成(zip形式で出力).
		String selectedFilePath = null;
		FileDialog saveDialog = new FileDialog(parent.getShell(), SWT.SAVE);
		long startMsec = 0;

		// zipファイルの初期名称を設定.
		String fileNamePrefix = Messages.getString("rpa.scenario.operation.result.download.csv.name.prefix");
		// 対象ファイル名に含めるID(日付)を生成
		String defaultDateStr = DateTimeStringConverter.formatLongDate(System.currentTimeMillis(), "yyyyMMddHHmmssSSS");
		String defaultFileName = fileNamePrefix + defaultDateStr;
		// ファイル名に空白があると+に置き換わってしまうため、空白を削除
		defaultFileName = this.renameFileName(defaultFileName);
		saveDialog.setFilterExtensions(new String[] { "*.zip" });
		defaultFileName = defaultFileName + ".zip";
		saveDialog.setFileName(defaultFileName);

		try {
			// ダイアログオープン.
			selectedFilePath = saveDialog.open();
			// ダウンロード性能測定用に時刻取得.
			startMsec = HinemosTime.getDateInstance().getTime();
		} catch (Exception e) {
			m_log.error(e);
			Object[] args = new Object[] { Messages.getString("file"), action, Messages.getString("failed"),
					HinemosMessage.replace(e.getMessage()) };
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.infra.action.result", args));
			return;
		}

		// path is null when dialog cancelled
		if (selectedFilePath != null) {
			try {
				// マネージャーにてZipファイル生成してダウンロード.
				if (ClusterControlPlugin.isRAP()) {
					fileName = defaultFileName;
				} else {
					fileName = new File(selectedFilePath).getName();
				}
				request.setFilename(fileName);
				
				// マネージャ側で一時ファイルの出力先を識別するためクライアント名設定.
				this.clientSeq = addSequeance();
				String clientName = FileUtil.fittingFileName(InetAddress.getLocalHost().getHostName(), "-") + "_"
						+ Integer.toString(this.clientSeq);
				request.setClientName(clientName);
				// 別スレッドでダウンロード処理走らせるためのオブジェクト生成.
				RpaScenarioOperationResultDataDownloader downloader = 
						new RpaScenarioOperationResultDataDownloader(manager, startMsec, selectedFilePath, request);
				IRunnableWithProgress op = new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						// エクスポートを開始
						ServiceContext context = ContextProvider.getContext();
						ContextProvider.releaseContextHolder();
						ContextProvider.setContext(context);

						downloader.setContext(context);
						Thread exportThread = new Thread(downloader);
						exportThread.start();
						Thread.sleep(3000);
						monitor.beginTask(action, 100);

						int progress = 0;
						int buff = 0;
						while (progress < 100) {
							progress = downloader.getProgress();

							if (monitor.isCanceled()) {
								Object[] args = new Object[] { action };
								throw new InterruptedException(Messages.getString("message.common.13", args));
							}
							if (downloader.isCanceled()) {
								throw new InterruptedException(downloader.getCancelMessage());
							}
							Thread.sleep(50);
							monitor.worked(progress - buff);
							buff = progress;
						}
						monitor.done();
					}
				};

				// プロセスバーダイアログの表示.
				new ProgressMonitorDialog(parent.getShell()).run(true, true, op);

				// 完了ダイアログ.
				if (ClusterControlPlugin.isRAP()) {
					FileDownloader.openBrowser(parent.getShell(), selectedFilePath, fileName);
				} else {
					Object[] args = new Object[] { Messages.getString("file"), action, Messages.getString("successful"),
							fileName };
					MessageDialog.openInformation(null, Messages.getString("confirmed"),
							Messages.getString("message.infra.action.result", args));
				}
			} catch (Exception e) {
				m_log.error(e);
				MessageDialog.openError(null, Messages.getString("failed"), e.getMessage());
			}
		}
		return;
	}

	/**
	 * 通信エラーの原因になる文字を削除.
	 */
	private String renameFileName(String fileName) {
		fileName = fileName.replaceAll(" ", "");
		fileName = fileName.replaceAll("%", "");
		return fileName;
	}

}
