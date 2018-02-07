/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rap.addons.fileupload.FileUploadEvent;
import org.eclipse.rap.addons.fileupload.FileUploadHandler;
import org.eclipse.rap.addons.fileupload.FileUploadListener;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.rap.rwt.widgets.FileUpload;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.client.ui.util.FileDownloader;
import com.clustercontrol.composite.action.StringVerifyListener;
import com.clustercontrol.jobmanagement.dialog.ManagerDistributionDialog;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * ScriptComponent
 * 
 * @version 6.0.0
 * @since 6.0.0
 */
// TODO Encapsulate this class with FileDialog.
public class ScriptComponent {
	// ログ
	private static Log m_log = LogFactory.getLog(ScriptComponent.class);

	private Composite parent;
	private ManagerDistributionDialog dialog;

	/**
	 * コンストラクタ
	 *
	 * @param parent 親コンポジット
	 * @param commandComposite コマンドコンポジット
	 *
	 * @see org.eclipse.swt.SWT
	 * @see #initialize()
	 */
	public ScriptComponent(Composite parent, ManagerDistributionDialog dialog) {
		this.parent = parent;
		this.dialog = dialog;
		initialize();
	}

	/** スクリプト名用テキスト */
	private Text m_scriptName = null;
	/** スクリプト配布用アップロードボタン */
	private FileUpload m_scriptUpload = null;
	/** スクリプト配布用ダウンロードボタン */
	private Button m_scriptDownload = null;
	/** エンコーディング */
	private Text m_scriptEncoding = null;
	/** スクリプト用テキスト */
	private Text m_scriptContent = null;


	/**
	 * コンポジットを構築します
	 */
	private void initialize() {
		// スクリプト配布：スクリプト名（ラベル）
		Label scripNametLabel = new Label(parent, SWT.NONE);
		scripNametLabel.setText(Messages.getString("job.script.name") + ": ");
		scripNametLabel.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// スクリプト配布：スクリプト名（テキスト）
		this.m_scriptName = new Text(parent, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_scriptName", this.m_scriptName);
		this.m_scriptName.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_scriptName.addVerifyListener(new StringVerifyListener(DataRangeConstant.VARCHAR_64));
		this.m_scriptName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				dialog.update();
			}
		});

		// スクリプト配布：アップロード（ボタン）
		m_scriptUpload = new FileUpload(parent, SWT.BORDER | SWT.FLAT | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, "m_scriptUpload", this.m_scriptUpload);
		this.m_scriptUpload.setText(Messages.getString("upload"));
		this.m_scriptUpload.setLayoutData(new GridData(100, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_scriptUpload.addSelectionListener(new ScriptUploadSelectionAdapter());

		// スクリプト配布：ダウンロード（ボタン）
		this.m_scriptDownload = new Button(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_scriptDownload", this.m_scriptDownload);
		this.m_scriptDownload.setText(Messages.getString("download"));
		this.m_scriptDownload.setLayoutData(new GridData(100, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_scriptDownload.addSelectionListener(new ScriptDownloadSelectionAdapter());

		// スクリプト配布：ファイルエンコーディング（ラベル）
		Label encodingLabel = new Label(parent, SWT.LEFT);
		encodingLabel.setText(Messages.getString("job.script.encoding") + " : ");
		encodingLabel.setLayoutData(new GridData(120, SizeConstant.SIZE_LABEL_HEIGHT));

		// スクリプト配布：ファイルエンコーディング（テキスト）
		this.m_scriptEncoding = new Text(parent, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_scriptEncoding", this.m_scriptEncoding);
		this.m_scriptEncoding.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_scriptEncoding.addVerifyListener(new StringVerifyListener(DataRangeConstant.VARCHAR_32));
		this.m_scriptEncoding.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				dialog.update();
			}
		});

		// dummy
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);

		// スクリプト配布：スクリプト（ラベル）
		Label scriptLabel = new Label(parent, SWT.LEFT);
		scriptLabel.setText(Messages.getString("script") + ": ");
		scriptLabel.setLayoutData(new GridData(120, SizeConstant.SIZE_LABEL_HEIGHT));

		// dummy
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);

		// スクリプト配布：スクリプト（テキスト）
		this.m_scriptContent = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "m_scriptContent", this.m_scriptContent);
		GridData sGrid = new GridData(500, 350);
		sGrid.horizontalAlignment = GridData.FILL;
		sGrid.verticalAlignment = GridData.FILL;
		sGrid.grabExcessHorizontalSpace = true;
		sGrid.grabExcessVerticalSpace = true;
		sGrid.horizontalSpan = 4;
		this.m_scriptContent.setLayoutData(sGrid);
		this.m_scriptContent.addVerifyListener(new StringVerifyListener(DataRangeConstant.TEXT));
		this.m_scriptContent.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				dialog.update();
			}
		});
	}

	ScriptUploadReceiver receiver = null;
	ServerPushSession pushSession = null;

	private String startUploadReceiver() {
		receiver = new ScriptUploadReceiver();
		pushSession = new ServerPushSession();
		FileUploadHandler uploadHandler = new FileUploadHandler(receiver);
		uploadHandler.addUploadListener(new FileUploadListener() {
			@Override
			public void uploadProgress(FileUploadEvent event) {
				ScriptComponent.this.showProgress((100 * event.getBytesRead() / event.getContentLength()) + "%");
			}

			@Override
			public void uploadFinished(FileUploadEvent event) {
				ScriptComponent.this.setFree();
				
				m_scriptContent.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						ByteArrayOutputStream baos = receiver.getOutputStream();
						try {
							String scriptContent = baos.toString(m_scriptEncoding.getText());
							m_scriptContent.setText(scriptContent);
						} catch (IOException e) {
							m_log.warn("falied to upload script content " + e.getClass().getSimpleName() + ":" + e.getMessage());
							MessageDialog.openError(null, Messages.getString("failed"),
									Messages.getString("message.job.156"));
						}
					}
				});
				
				// Stop push session after all
				pushSession.stop();
			}

			@Override
			public void uploadFailed(FileUploadEvent event) {
				// Failed. Just set it free
				ScriptComponent.this.setFree();

				// Stop push session after all
				pushSession.stop();
			}
		});
		return uploadHandler.getUploadUrl();
	}
	
	public Text getScriptName() {
		return m_scriptName;
	}

	public FileUpload getScriptUpload() {
		return m_scriptUpload;
	}

	public Button getScriptDownload() {
		return m_scriptDownload;
	}

	public Text getScriptEncoding() {
		return m_scriptEncoding;
	}

	public Text getScriptContent() {
		return m_scriptContent;
	}

	private void setBusy() {
		m_scriptUpload.setEnabled(false);
	}

	private void setFree() {
		m_scriptUpload.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				m_scriptUpload.setEnabled(true);
				m_scriptUpload.setText(Messages.getString("upload"));
			}
		});
	}

	private void showProgress(final String precentageText) {
		m_scriptUpload.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				m_scriptUpload.setText(precentageText);
			}
		});
	}
	
	private class ScriptUploadSelectionAdapter extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			FileUpload fileUpload = (FileUpload) e.widget;
			m_scriptName.setText(fileUpload.getFileName());

			ScriptComponent.this.setBusy();
			
			// Start upload direct
			String url = startUploadReceiver();
			pushSession.start();
			fileUpload.submit(url);
		}
	}

	private class ScriptDownloadSelectionAdapter extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			String scriptName = m_scriptName.getText();
			String scriptEncoding = m_scriptEncoding.getText();
			String scriptContent = m_scriptContent.getText();
			if (scriptName == null || scriptName.isEmpty() ||
					scriptEncoding == null || scriptEncoding.isEmpty() ||
					scriptContent == null || scriptContent.isEmpty()) {
				MessageDialog.openError(null, Messages.getString("failed"), Messages.getString("message.job.159"));
				return;
			}
			
			FileDialog fd = new FileDialog(parent.getShell(), SWT.SAVE);
			// スクリプト名が空の場合はデフォルトファイル名は"script"とする
			if(scriptName.isEmpty()) {
				scriptName = "script";
			}
			fd.setFileName(scriptName);
			String selectedFilePath = fd.open();
			if(selectedFilePath != null) {
				PrintWriter pw = null;
				try {
					pw = new PrintWriter(selectedFilePath, m_scriptEncoding.getText());
					pw.println(scriptContent);
					pw.close();
				} catch (IOException ex) {
					MessageDialog.openError(null, Messages.getString("failed"),
							Messages.getString("message.job.158") + ", " + HinemosMessage.replace(ex.getMessage()));
					return;
				}
				
				FileDownloader.openBrowser(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), selectedFilePath, scriptName);
			}
		}
	}
}
