/**********************************************************************
 * Copyright (C) 2014 NTT DATA Corporation
 * This program is free software; you can redistribute it and/or
 * Modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2.
 * 
 * This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *********************************************************************/

package com.clustercontrol.jobmanagement.composite;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
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

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.StringVerifyListener;
import com.clustercontrol.jobmanagement.dialog.ManagerDistributionDialog;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * Upload composite
 * 
 * @version 6.0.0
 * @since 6.0.0
 */
public class ScriptComponent {
	
	private static Log m_log = LogFactory.getLog( ScriptComponent.class );
	
	private Composite parent;
	private ManagerDistributionDialog dialog;

	/**
	 * コンストラクタ
	 *
	 * @param parent 親のコンポジット
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int
	 *      style)
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
	private Button m_scriptUpload = null;
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
		this.m_scriptUpload = new Button(parent, SWT.NONE);
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
		new Label(parent, SWT.LEFT);
		new Label(parent, SWT.LEFT);

		// スクリプト配布：スクリプト（ラベル）
		Label scriptLabel = new Label(parent, SWT.LEFT);
		scriptLabel.setText(Messages.getString("script") + ": ");
		scriptLabel.setLayoutData(new GridData(120, SizeConstant.SIZE_LABEL_HEIGHT));

		// dummy
		new Label(parent, SWT.LEFT);
		new Label(parent, SWT.LEFT);
		new Label(parent, SWT.LEFT);

		// スクリプト配布：スクリプト（テキスト）
		this.m_scriptContent = new Text(parent,
				SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "m_scriptContent", this.m_scriptContent);
		GridData sGrid = new GridData(550, SizeConstant.SIZE_TEXTFIELD_HEIGHT);
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

	public Text getScriptName() {
		return m_scriptName;
	}

	public Button getScriptUpload() {
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

	public boolean isReady() {
		// Do nothing
		return true;
	}
	
	public void cleanup() {
		// Do nothing
	}

	private class ScriptUploadSelectionAdapter extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			FileDialog fd = new FileDialog(parent.getShell(), SWT.OPEN);
			String selectedFilePath = fd.open();
			if (selectedFilePath != null) {
				Path p = Paths.get(selectedFilePath);
				if (p == null || p.getFileName() == null) {
					MessageDialog.openError(null, Messages.getString("failed"), Messages.getString("message.job.156"));
				}
				Path filename = p.getFileName();
				if (filename == null) {
					MessageDialog.openError(null, Messages.getString("failed"), Messages.getString("message.job.156"));
				}
				m_scriptName.setText(filename.toString());
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					Files.copy(p, baos);
					m_scriptContent.setText(baos.toString(m_scriptEncoding.getText()));
				} catch (IOException ex) {
					m_log.warn("falied to upload script content " + ex.getClass().getSimpleName() + ":" + ex.getMessage());
					MessageDialog.openError(null, Messages.getString("failed"),
							Messages.getString("message.job.156"));
				}
			}
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
			fd.setOverwrite(true);
			fd.setFileName(scriptName);
			String selectedFilePath = fd.open();
			if (selectedFilePath != null) {
				PrintWriter pw = null;
				try {
					pw = new PrintWriter(selectedFilePath, m_scriptEncoding.getText());
					pw.print(m_scriptContent.getText());
					
					MessageDialog.openInformation(null, Messages.getString("confirmed"),
								Messages.getString("message.job.157"));
					
				} catch (Exception ex) {
					MessageDialog.openError(null, Messages.getString("failed"),
							Messages.getString("message.job.158") + ", " + HinemosMessage.replace(ex.getMessage()));
				} finally {
					if (pw != null) {
						pw.close();
					}
				}
			}
		}
	}

}
