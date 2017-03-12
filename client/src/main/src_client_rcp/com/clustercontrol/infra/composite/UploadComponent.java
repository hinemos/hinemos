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

package com.clustercontrol.infra.composite;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.infra.dialog.ChangeBackgroundModifyListener;

/**
 * Upload composite
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class UploadComponent{
	private Shell shell;
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
	public UploadComponent(Composite parent, String label, int labelColumns, int inputColumns) {
		this.parent = parent;
		this.shell = parent.getShell();
		this.label = label;
		this.labelColumns = labelColumns;
		this.inputColumns = inputColumns;
		initialize();
	}

	private Text m_fileName;
	private String m_filePath;

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

		Button fileSelectionBtn = new Button(parent,  SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		fileSelectionBtn.setLayoutData(gridData);
		fileSelectionBtn.setText("...");

		fileSelectionBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				String selectedFilePath = fd.open();
				if (selectedFilePath != null) {
					Path p = Paths.get(selectedFilePath);
					if (p == null || p.getFileName() == null) {
						throw new InternalError("Path is null.");
					}
					Path filename = p.getFileName();
					if (filename == null) {
						throw new InternalError("filename is null.");
					}
					m_fileName.setText(filename.toString());
					m_filePath = selectedFilePath;
				}
			}
		});

		m_fileName.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		m_fileName.addModifyListener(new ChangeBackgroundModifyListener());
	}

	public boolean isReady(){
		// Do nothing
		return true;
	}

	public void setFileName( String fileName ){
		m_fileName.setText( fileName );
	}

	public String getFileName(){
		return m_fileName.getText();
	}

	public String getFilePath(){
		return m_filePath;
	}

	public void cleanup(){
		// Do nothing
	}

}
