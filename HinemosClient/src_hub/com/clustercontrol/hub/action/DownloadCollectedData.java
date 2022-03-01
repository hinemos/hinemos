/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.action;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

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
import org.openapitools.client.model.DownloadBinaryRecordsKeyRequest;
import org.openapitools.client.model.DownloadBinaryRecordsRequest;
import org.openapitools.client.model.QueryCollectBinaryDataRequest;
import org.openapitools.client.model.TagResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.binary.bean.BinaryTagConstant;
import com.clustercontrol.client.ui.util.FileDownloader;
import com.clustercontrol.hub.dto.DataResponse;
import com.clustercontrol.util.DateTimeStringConverter;
import com.clustercontrol.util.FileUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Messages;

/**
 * 収集蓄積データ(文字/バイナリ)をダウンロード.
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
public class DownloadCollectedData {

	/** ログ */
	private static Log m_log = LogFactory.getLog(DownloadCollectedData.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	// バイナリデータのダウンロード(検索結果はbinary型で保持してないのでマネージャーから再取得が必要).
	/**
	 * 選択したBinaryレコードをファイルとしてダウンロード.
	 */
	public void executeBinaryRecord(Shell parent, String manager, DataResponse selectBinaryData) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");

		if (selectBinaryData == null) {
			return;
		}

		String action = Messages.getString("download");
		FileDialog fd = new FileDialog(parent.getShell(), SWT.SAVE);

		// タグから必要な値を取得.
		List<TagResponse> tagList = selectBinaryData.getTagList();
		String defaultFileName = null;
		for (TagResponse tag : tagList) {
			if (BinaryTagConstant.CommonTagName.FILE_NAME.equals(tag.getKey())) {
				String filePath = tag.getValue();
				defaultFileName = FileUtil.getFileName(filePath);
				break;
			}
		}

		if (defaultFileName != null) {
			defaultFileName = this.renameFileName(defaultFileName);
		}
		fd.setFileName(defaultFileName);

		String selectedFilePath = null;
		long startMsec = 0;
		String fileName = null;
		try {
			selectedFilePath = fd.open();
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
				// クライアントに出力するファイル名設定.
				if (ClusterControlPlugin.isRAP()) {
					fileName = defaultFileName;
				} else {
					fileName = new File(selectedFilePath).getName();
				}
				// 別スレッドでダウンロード処理走らせるためのオブジェクト生成.
				BinaryDataDownloader downloader = new BinaryDataDownloader(manager, startMsec, fileName,
						selectedFilePath, selectBinaryData);
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
	 * 複数のバイナリレコードをユーザー指定したフォルダにダウンロード.
	 * @throws UnknownHostException 
	 */
	public void executeBinaryRecords(Shell parent, String manager, List<DataResponse> selectBinaryList) throws UnknownHostException {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");

		// 表示レコードない場合は処理せず終了.
		if (selectBinaryList == null || selectBinaryList.isEmpty()) {
			return;
		}

		// 表示されているレコード毎にダウンロードに必要な情報をセット.
		DownloadBinaryRecordsRequest binaryDownloadList = new DownloadBinaryRecordsRequest();
		String fileName = null;
		List<String> fileNameList = new ArrayList<String>();
		for (DataResponse selectBinaryData : selectBinaryList) {
			// タグから必要な値を取得.
			List<TagResponse> tagList = selectBinaryData.getTagList();
			for (TagResponse tag : tagList) {
				if (BinaryTagConstant.CommonTagName.FILE_NAME.equals(tag.getKey())) {
					String filePath = tag.getValue();
					fileName = FileUtil.getFileName(filePath);
					break;
				}
			}
			// ダウンロードに必要な情報をマップにセット.
			// バイナリ収集の検索条件のセット
			QueryCollectBinaryDataRequest queryInfo = new QueryCollectBinaryDataRequest();
			queryInfo.setFacilityId(selectBinaryData.getFacilityId());
			queryInfo.setMonitorId(selectBinaryData.getMonitorId());
			binaryDownloadList.getQueryCollectBinaryDataRequest().add(queryInfo);
			
			// PKのセット
			DownloadBinaryRecordsKeyRequest record = new DownloadBinaryRecordsKeyRequest();
			record.setCollectId(selectBinaryData.getCollectId());
			record.setDataId(selectBinaryData.getDataId());
			// ソート用のキー
			record.setRecordKey(selectBinaryData.getRecordKey());
			binaryDownloadList.getRecords().add(record);
			
			// Filename
			binaryDownloadList.setFilename(fileName);
			
			// ファイル名をリストに追加.
			if (fileNameList.isEmpty() || !fileNameList.contains(fileName)) {
				fileNameList.add(fileName);
			}
		}

		// 出力用のファイル名取得できたかチェック.
		String action = Messages.getString("download");
		if (fileNameList == null || fileNameList.isEmpty()) {
			String errorMessage = "failed to get file name from tags.";
			// データ不正.
			Object[] args = new Object[] { Messages.getString("file"), action, Messages.getString("failed"),
					errorMessage };
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.infra.action.result", args));
			return;
		}

		// ユーザーに出力先ファイルを選択させるためのダイアログ生成(zip形式で出力).
		String selectedFilePath = null;
		FileDialog saveDialog = new FileDialog(parent.getShell(), SWT.SAVE);
		long startMsec = 0;

		// zipファイルの名称を設定.
		String topFileName = fileNameList.get(0);
		// 先頭のファイル名の拡張子より前の部分をzip名称用に取得.
		int extentionIndex = topFileName.indexOf('.');
		if (extentionIndex > 0) {
			topFileName = topFileName.substring(0, extentionIndex);
		}
		// 対象ファイル名に含めるID(日付)を生成
		String defaultDateStr = DateTimeStringConverter.formatLongDate(System.currentTimeMillis(), "yyyyMMddHHmmss");
		String defaultFileName = defaultDateStr + '_' + topFileName;
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
				// 別スレッドでダウンロード処理走らせるためのオブジェクト生成.
				BinaryDataDownloader downloader = new BinaryDataDownloader(manager, startMsec, fileName,
						selectedFilePath, binaryDownloadList);
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

	// 文字列データの出力(検索結果を元に出力するのでマネージャー通信なし).
	/**
	 * 選択された1レコードを1ファイルとして出力.
	 */
	public void executeTextRecord(Shell parent, DataResponse selectTextData) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");

		// 選択データ存在しない場合は処理せず終了.
		if (selectTextData == null) {
			return;
		}

		// デフォルトのファイル名を取得.
		String defaultFileName = null;
		// タグから必要な値を取得.
		List<TagResponse> tagList = selectTextData.getTagList();
		for (TagResponse tag : tagList) {
			if ("filename".equals(tag.getKey())) {
				String filePath = tag.getValue();
				defaultFileName = FileUtil.getFileName(filePath);
				break;
			}
		}

		// ファイル名取得できなかった場合は監視IDを設定(ログファイルはタグにファイル名なしデータも存在する).
		if (defaultFileName == null) {
			defaultFileName = selectTextData.getMonitorId();
			defaultFileName = FileUtil.fittingFileName(defaultFileName, "-");
		}

		// ファイル選択用のダイアログ生成.
		String selectedFilePath = null;
		long startMsec = 0;
		FileDialog saveDialog = new FileDialog(parent.getShell(), SWT.SAVE);
		saveDialog.setFileName(defaultFileName);

		// ダイアログ表示用の操作名設定.
		String action = Messages.getString("download");
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
		String fileName = null;
		if (selectedFilePath != null) {
			try {
				// RAPの場合は選択前に処理が走る.
				if (ClusterControlPlugin.isRAP()) {
					fileName = defaultFileName;
				} else {
					fileName = new File(selectedFilePath).getName();
				}
				// 別スレッドでダウンロード処理走らせるためのオブジェクト生成.
				TextDataDownloader downloader = new TextDataDownloader(startMsec, fileName, selectedFilePath,
						selectTextData.getData());
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
	 * オリジナルメッセージ欄に表示されている文字列を1ファイルとして出力.
	 */
	public void executeTextRecordsToOne(String originMsg, List<DataResponse> selectTextDataList, Shell parent) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");

		// オリジナルメッセージの表示ない場合は処理せず終了.
		if (originMsg == null || originMsg.isEmpty()) {
			return;
		}

		// デフォルトのファイル名を取得.
		String fileName = null;
		boolean toBreak = false;
		for (DataResponse selectTextData : selectTextDataList) {
			// タグから必要な値を取得.
			List<TagResponse> tagList = selectTextData.getTagList();
			for (TagResponse tag : tagList) {
				if ("filename".equals(tag.getKey())) {
					String filePath = tag.getValue();
					fileName = FileUtil.getFileName(filePath);
					toBreak = true;
					break;
				}
			}
			// 先頭データのファイル名取得したらそれでおしまい.
			if (toBreak) {
				break;
			}
		}

		// ファイル名取得できなかった場合は監視IDを設定(ログファイルはタグにファイル名なしデータも存在する).
		if (fileName == null) {
			fileName = selectTextDataList.get(0).getMonitorId();
			fileName = FileUtil.fittingFileName(fileName, "-");
		}

		// ユーザーに出力先ファイルを選択させるためのダイアログ生成.
		String selectedFilePath = null;
		long startMsec = 0;

		// ファイル選択用のダイアログ生成.
		FileDialog saveDialog = new FileDialog(parent.getShell(), SWT.SAVE);
		// 対象ファイル名に含めるID(日付)を生成
		String defaultDateStr = DateTimeStringConverter.formatLongDate(System.currentTimeMillis(), "yyyyMMddHHmmss");
		String defaultFileName = defaultDateStr + '_' + fileName;
		// soap通信エラーの原因になる文字をファイル名から削除.
		defaultFileName = this.renameFileName(defaultFileName);
		saveDialog.setFileName(defaultFileName);

		// ダイアログ表示用の操作名設定.
		String action = Messages.getString("download");
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
				// RAPの場合は選択前に処理が走る.
				if (ClusterControlPlugin.isRAP()) {
					fileName = defaultFileName;
				} else {
					fileName = new File(selectedFilePath).getName();
				}
				// 別スレッドでダウンロード処理走らせるためのオブジェクト生成.
				TextDataDownloader downloader = new TextDataDownloader(startMsec, fileName, selectedFilePath,
						originMsg);
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
	 * soap通信エラーの原因になる文字を削除.
	 */
	private String renameFileName(String fileName) {
		fileName = fileName.replaceAll(" ", "");
		fileName = fileName.replaceAll("%", "");
		return fileName;
	}

}
