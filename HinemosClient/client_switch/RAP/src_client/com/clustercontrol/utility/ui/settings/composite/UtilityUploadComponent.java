/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.ui.settings.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.infra.dialog.ChangeBackgroundModifyListener;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.ui.composite.UtilityFileUpload;

/**
 * Upload composite
 * 
 * @version 5.0.a
 * @since 5.0.a
 */
public class UtilityUploadComponent{
	private Composite parent;
	private String label;
	private int labelColumns;
	private int inputColumns;

	/**
	 * コンストラクタ
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param label Label text
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public UtilityUploadComponent(Composite parent, String label, int labelColumns, int inputColumns) {
		this.parent = parent;
		this.label = label;
		this.labelColumns = labelColumns;
		this.inputColumns = inputColumns;
		initialize();
	}

	private Text m_fileName;
	private UtilityFileUpload fileUpload;

	/**
	 * コンポジットを構築します
	 */
	private void initialize() {
		// GridData for common use
		GridData gridData;

		// ファイル名
		Label fileNameTitle = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = labelColumns;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		fileNameTitle.setLayoutData(gridData);
		fileNameTitle.setText(label);

		m_fileName = new Text(parent, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = inputColumns - 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_fileName.setLayoutData(gridData);
		m_fileName.setEditable(false);
		m_fileName.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		m_fileName.addModifyListener(new ChangeBackgroundModifyListener());

		//final String url = FileDialog.startUploadReceiver();
		fileUpload = new UtilityFileUpload(parent, SWT.NONE);
		fileUpload.setText(Messages.getString("string.select"));
		this.fileUpload.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				UtilityFileUpload fileUpload = (UtilityFileUpload) e.widget;
				fileUpload.cleanup();
				
				m_fileName.setText(fileUpload.getFileName());

				fileUpload.setBusy();

				// Start upload direct
				fileUpload.startSession();
				fileUpload.submit();
			}
		});
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		fileUpload.setLayoutData(gridData);
	}

	public void setFileName( String fileName ){
		m_fileName.setText( fileName );
	}

	public String getFileName(){
		return m_fileName.getText();
	}

	public String getFilePath(){
		return null != fileUpload.getFilePath() ? fileUpload.getFilePath() : null;
	}
}
