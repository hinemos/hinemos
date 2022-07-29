/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.dialog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.JobRpaScreenshotResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.client.ui.util.FileDownloader;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.jobmanagement.action.GetRpaScreenshotTableDefine;
import com.clustercontrol.jobmanagement.composite.NodeDetailComposite;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.util.DateTimeStringConverter;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * RPAシナリオジョブ スクリーンショットダウンロードダイアログ用のコンポジットクラスです
 */
public class RpaScreenshotDownloadDialog extends Dialog {
	/** ロガー */
	private static Log m_log = LogFactory.getLog(RpaScreenshotDownloadDialog.class);
	/** スクリーンショット一覧テーブルビューア */
	private CommonTableViewer m_screenshotListViewer = null;
	/** 保存ファイル名 */
	private String m_fileName;
	/** マネージャ名 */
	private String m_managerName = null;
	/** セッションID */
	private String m_sessionId = null;
	/** ジョブユニットID */
	private String m_jobunitId = null;
	/** ジョブID */
	private String m_jobId = null;
	/** ファシリティID */
	private String m_facilityId = null;
	/**
	 * APIリクエスト用の日付フォーマット（ミリ秒あり）
	 */
	private final String m_requestDateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
	/**
	 * ファイル名用の日付フォーマット
	 */
	private final String m_fileNameDateFormat = "yyyyMMddHHmmssSSS";

	/**
	 * コンストラクタ
	 * 
	 * @param parent
	 *            親コンポジット
	 * @param nodeDetailComposite
	 *            ノード詳細コンポジット
	 */
	public RpaScreenshotDownloadDialog(Shell parent, NodeDetailComposite nodeDetailComposite) {
		super(parent);
		m_managerName = nodeDetailComposite.getManagerName();
		m_sessionId = nodeDetailComposite.getSessionId();
		m_jobunitId = nodeDetailComposite.getJobunitId();
		m_jobId = nodeDetailComposite.getJobId();
		m_facilityId = nodeDetailComposite.getFacilityId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		container.setLayout(layout);

		Table table = new Table(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(400, 150));
		this.m_screenshotListViewer = new CommonTableViewer(table);
		this.m_screenshotListViewer.createTableColumn(GetRpaScreenshotTableDefine.get(),
				GetRpaScreenshotTableDefine.SORT_COLUMN_INDEX, GetRpaScreenshotTableDefine.SORT_ORDER);
		// スクリーンショット情報を取得
		JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(m_managerName);
		List<JobRpaScreenshotResponse> retDtoList;
		try {
			retDtoList = wrapper.getJobRpaScreenshot(m_sessionId, m_jobunitId, m_jobId, m_facilityId);
			List<Object> tableData = new ArrayList<Object>();
			for (JobRpaScreenshotResponse dto : retDtoList) {
				ArrayList<Object> tableLineData = new ArrayList<Object>();
				// 日付を画面ではミリ秒無しで表示する（CommonTableLabelProviderで変換するためDateにする）
				tableLineData.add(DateTimeStringConverter.parseDateString(dto.getOutputDate(), m_requestDateFormat));
				tableLineData.add(HinemosMessage.replace(dto.getDescription()));
				tableData.add(tableLineData);
			}
			this.m_screenshotListViewer.setInput(tableData);
		} catch (InvalidUserPass | InvalidRole | RestConnectFailed | HinemosUnknown e) {
			m_log.warn("createDialogArea() : request failed, " + e.getMessage(), e);
			openErrorDialog(e.getMessage());
		}
		return container;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.
	 * swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, Messages.getString("download"), true);
		createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("cancel"), false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.
	 * Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		// ダイアログタイトル
		newShell.setText(Messages.getString("dialog.job.download.rpa.screenshot"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.dialog.CommonDialog#okPressed()
	 */
	@Override
	protected void okPressed() {

		@SuppressWarnings("unchecked")
		List<Object> selectedRow = (List<Object>) ((StructuredSelection) m_screenshotListViewer.getSelection())
				.getFirstElement();
		if (selectedRow == null) {
			MessageDialog.openWarning(null, Messages.getString("dialog.job.download.rpa.screenshot"),
					Messages.getString("message.job.rpa.23"));
			return;
		}
		Date selectedDate = (Date) selectedRow.get(GetRpaScreenshotTableDefine.DATE);
		String extension = ".png";
		// 日付の書式を変換しファイル名に含める
		m_fileName = String.join("_", "Screenshot", m_sessionId, m_jobunitId, m_jobId, m_facilityId,
				DateTimeStringConverter.formatDate(selectedDate, m_fileNameDateFormat)) + extension;
		// ファイルダイアログを開く
		FileDialog fileDialog = new FileDialog(this.getShell(), SWT.SAVE);
		fileDialog.setFileName(m_fileName);
		fileDialog.setFilterExtensions(new String[] { "*" + extension });
		fileDialog.setOverwrite(true);
		String selectedFilePath = fileDialog.open();
		// path is null when dialog cancelled
		if (selectedFilePath != null) {
			File file = new File(selectedFilePath);
			// スクリーンショットをダウンロード
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(m_managerName);
			try {
				File downloadFile = wrapper.downloadJobRpaScreenshotFile(m_sessionId, m_jobunitId, m_jobId,
						m_facilityId, DateTimeStringConverter.formatDate(selectedDate, m_requestDateFormat));
				// RCPの場合は一時ファイルを指定された保存先へ移動
				// RAPの場合は一時ファイルをリネームした後にブラウザでダウンロード
				Files.move(downloadFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
				if (ClusterControlPlugin.isRAP()) {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					FileDownloader.openBrowser(window.getShell(), selectedFilePath, m_fileName);
				}
				// ダウンロードが成功したらダイアログを閉じる
				super.okPressed();
			} catch (InvalidUserPass | InvalidRole | RestConnectFailed | HinemosUnknown e) {
				m_log.warn("okPressed() : request failed, " + e.getMessage(), e);
				openErrorDialog(e.getMessage());
			} catch (IOException e) {
				m_log.warn("okPressed() : move file failed, " + e.getMessage(), e);
				openErrorDialog(e.getMessage());
			}
		}
	}

	/**
	 * エラーダイアログを表示します。
	 * 
	 * @param errorMessage
	 *            エラーメッセージ
	 */
	private void openErrorDialog(String errorMessage) {
		MessageDialog.openError(null, Messages.getString("error"), errorMessage);
	}

	/**
	 * 出力先ファイル名を返します。
	 *
	 * @return 出力先ファイル名
	 */
	public String getFileName() {
		return this.m_fileName;
	}
}
