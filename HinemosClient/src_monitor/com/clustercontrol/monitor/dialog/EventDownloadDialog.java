/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.dialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.DownloadEventFileRequest;
import org.openapitools.client.model.EventLogInfoRequest;
import org.openapitools.client.model.EventSelectionRequest;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.client.ui.util.FileDownloader;
import com.clustercontrol.dialog.ApiResultDialog;
import com.clustercontrol.filtersetting.bean.EventFilterContext;
import com.clustercontrol.monitor.util.MonitorResultRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * 監視[イベントのダウンロード]ダイアログ
 */
public class EventDownloadDialog extends EventFilterDialog {
	public static String DOWNLOAD_FILE_NAME = "EventReport-%s";
	public static String DOWNLOAD_FILE_TIMESTAMP = "yyyyMMddHHmmssSSS";
	public static String DOWNLOAD_FILE_EXTENSION = ".csv";

	private static Log logger = LogFactory.getLog(EventDownloadDialog.class);

	private EventFilterContext context;

	private String defaultManagerName;
	private String defaultFacilityId;
	
	private List<EventLogInfoRequest> selectedEvents;
	private String fileName;
	private String filePath;

	private Button rdoSelected;
	private Button rdoFilter;

	public EventDownloadDialog(
			Shell parent,
			EventFilterContext context,
			List<EventLogInfoRequest> selectedEvents,
			String facilityId) {
		super(parent, context);
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(context.getManagerName(), "context.managerName");
		Objects.requireNonNull(facilityId, "facilityId");

		this.context = context;
		this.selectedEvents = selectedEvents == null ? new ArrayList<>() : selectedEvents;
		this.defaultManagerName = context.getManagerName();
		this.defaultFacilityId = facilityId;
		this.fileName = null;
		this.filePath = null;
	}

	@Override
	protected void createHeaderComposite(Composite parent) {
		// イベントのソースグループ
		Group grpSource = new Group(parent, SWT.NONE);
		GridLayout lyoSource = new GridLayout(1, true);
		lyoSource.marginWidth = 5;
		lyoSource.marginHeight = 5;
		lyoSource.numColumns = 15;
		grpSource.setLayout(lyoSource);
		grpSource.setText(Messages.getString("download"));
		grpSource.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 15, 1));

		rdoSelected = new Button(grpSource, SWT.RADIO);
		rdoSelected.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 15, 1));
		rdoSelected.setText(Messages.getString("dialog.monitor.events.download.selectevent") + "   " +
				Messages.getString("dialog.monitor.events.download.count") + " : " +
				String.valueOf(selectedEvents.size()));

		rdoFilter = new Button(grpSource, SWT.RADIO);
		rdoFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 15, 1));
		rdoFilter.setText(Messages.getString("dialog.monitor.events.download.filter"));

		// イベントリスナー
		rdoSelected.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabled(false);
			}
		});

		rdoFilter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabled(true);
			}
		});
	}

	@Override
	protected void initializeAfterCustomizeDialog() {
		// 初期選択
		switch (selectedEvents.size()) {
		case 0:
			// 1件も選択されていない場合、選択イベントは非活性
			rdoFilter.setSelection(true);
			rdoSelected.setEnabled(false);
			break;
		case 1:
			// 1件のみ選択されている場合、フィルタ
			rdoFilter.setSelection(true);
			break;
		default:
			// 複数兼選択されている場合、選択イベント
			rdoSelected.setSelection(true);
			break;
		}

		setEnabled(rdoFilter.getSelection());
	}

	@Override
	protected String getTitle() {
		return Messages.getString("dialog.monitor.events.download");
	}

	@Override
	protected boolean actionWithUpdatedContext() {
		// ファイルダイアログを開く
		FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
		fileDialog.setFilterExtensions(new String[] { "*" + DOWNLOAD_FILE_EXTENSION });

		// クライアントで出力するファイル名の日時情報はクライアントのタイムゾーンの現在時刻とする(マネージャのタイムゾーン時刻に補正しない)
		SimpleDateFormat sdf = new SimpleDateFormat(DOWNLOAD_FILE_TIMESTAMP);
		fileName = String.format(DOWNLOAD_FILE_NAME, sdf.format(new Date())) + DOWNLOAD_FILE_EXTENSION;
		fileDialog.setFileName(fileName);

		filePath = fileDialog.open();
		if (filePath == null || filePath.trim().length() == 0) return false;

		// Get specified new filename on RCP
		if (!ClusterControlPlugin.isRAP()) {
			fileName = fileDialog.getFileName();
		}

		try {
			return download(); // tryブロックが長くなるのでダウンロード処理は別メソッドへ
		} catch (Exception e) {
			logger.warn("Failed to download. " + e.getMessage());
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected")
							+ ", " + HinemosMessage.replace(e.getMessage()));
			return false;
		} finally {
			if (ClusterControlPlugin.isRAP()) {
				// Clean up temporary file
				try {
					FileDownloader.cleanup(filePath);
				} catch (Exception e) {
					logger.warn("Failed to clean up, filePath=" + filePath + ", error=" + e.getMessage());
				}
			}
		}
	}

	private boolean download() throws IOException {
		// 出力ファイル作成
		File file = new File(filePath);
		if (!file.createNewFile()) {
			logger.warn("File [" + filePath + "] is already existing.");
		}

		// リクエストDTOの構築
		DownloadEventFileRequest downloadEventFileRequest = new DownloadEventFileRequest();
		downloadEventFileRequest.setFilename(fileName);

		String targetManager;
		if (rdoSelected.getSelection()) {
			// 選択イベントのダウンロード
			targetManager = defaultManagerName;
			for (EventLogInfoRequest ev : selectedEvents) {
				EventSelectionRequest item = new EventSelectionRequest();
				item.setOutputDate(ev.getOutputDate());
				item.setPluginId(ev.getPluginId());
				item.setMonitorId(ev.getMonitorId());
				item.setMonitorDetailId(ev.getMonitorDetailId());
				item.setFacilityId(ev.getFacilityId());
				downloadEventFileRequest.addSelectedEventsItem(item);
			}
		} else {
			// フィルタ結果のダウンロード
			if (context.getFilter().getFacilityId() == null) {
				targetManager = defaultManagerName;
				context.getFilter().setFacilityId(defaultFacilityId);
			} else {
				targetManager = context.getManagerName();
			}
			downloadEventFileRequest.setFilter(context.getFilter());
		}

		// (Hinemosクライアントへの)ダウンロード実施
		File downloadFile;
		try {
			MonitorResultRestClientWrapper wrapper = MonitorResultRestClientWrapper.getWrapper(targetManager);
			downloadFile = wrapper.downloadEventFile(downloadEventFileRequest);
		} catch (Exception e) {
			new ApiResultDialog().addFailure(targetManager, e).show();
			return false;
		}

		FileDataSource source = new FileDataSource(downloadFile);
		DataHandler handler = new DataHandler(source);
		try (FileOutputStream fos = new FileOutputStream(file)) {
			handler.writeTo(fos);
		}

		// RAPならブラウザダウンロード実施
		if (ClusterControlPlugin.isRAP()) {
			FileDownloader.openBrowser(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					filePath,
					fileName);
		}
		return true;
	}

	public String getFilePath() {
		return filePath;
	}

	public String getFileName() {
		return fileName;
	}

}
