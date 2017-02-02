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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.addons.fileupload.FileDetails;
import org.eclipse.rap.addons.fileupload.FileUploadEvent;
import org.eclipse.rap.addons.fileupload.FileUploadHandler;
import org.eclipse.rap.addons.fileupload.FileUploadListener;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.rap.rwt.widgets.FileUpload;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.infra.dialog.ChangeBackgroundModifyListener;

/**
 * Upload composite
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
// TODO Encapsulate this class with FileDialog.
public class UploadComponent{
	// ログ
	private static Log m_log = LogFactory.getLog( UploadComponent.class );

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
		this.label = label;
		this.labelColumns = labelColumns;
		this.inputColumns = inputColumns;
		initialize();
	}

	private Text m_fileName;
	private FileUpload fileUpload;

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
		fileUpload = new FileUpload(parent, SWT.BORDER | SWT.FLAT | SWT.SINGLE);
		fileUpload.setText("...");
		fileUpload.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Cleanup the temporary file at first
				cleanup();

				FileUpload fileUpload = (FileUpload) e.widget;
				m_fileName.setText(fileUpload.getFileName());

				UploadComponent.this.setBusy();

				// Start upload direct
				pushSession.start();
				fileUpload.submit(url);
			}
		});
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		fileUpload.setLayoutData(gridData);

		url = startUploadReceiver();
	}

	InfraFileUploadReceiver receiver = null;
	ServerPushSession pushSession;
	String url;

	private String startUploadReceiver() {
		receiver = new InfraFileUploadReceiver();
		pushSession = new ServerPushSession();
		FileUploadHandler uploadHandler = new FileUploadHandler(receiver);
		uploadHandler.addUploadListener(new FileUploadListener() {
			@Override
			public void uploadProgress(FileUploadEvent event){
				UploadComponent.this.showProgress( ( 100 * event.getBytesRead() / event.getContentLength() ) + "%" );
			}

			@Override
			public void uploadFinished(FileUploadEvent event) {
				FileDetails[] fileDetails = event.getFileDetails();
				if( 0 < fileDetails.length ){
					m_log.debug( "uploadFinished : " + fileDetails[0].getFileName() );
				}
				UploadComponent.this.setFree();

				// Stop push session after all
				pushSession.stop();
			}
			@Override
			public void uploadFailed(FileUploadEvent event) {
				// Failed. Just set it free
				UploadComponent.this.setFree();

				// Stop push session after all
				pushSession.stop();
			}
		});
		return uploadHandler.getUploadUrl();
	}

	/**
	 * Ready if file uploaded
	 * @return
	 */
	public boolean isReady(){
		return fileUpload.getEnabled() && ((null != receiver.getTargetFile()) && receiver.getTargetFile().canRead() );
	}

	public void setFileName( String fileName ){
		m_fileName.setText( fileName );
	}

	public String getFileName(){
		return m_fileName.getText();
	}

	public String getFilePath(){
		return null != receiver && null != receiver.getTargetFile() ? receiver.getTargetFile().getAbsolutePath() : null;
	}

	private void setBusy(){
		fileUpload.setEnabled( false );
	}

	private void setFree(){
		fileUpload.getDisplay().asyncExec( new Runnable() {
			@Override
			public void run() {
				fileUpload.setEnabled( true );
				fileUpload.setText( "..." );
			}
		});
	}

	private void showProgress(final String precentageText){
		fileUpload.getDisplay().asyncExec( new Runnable() {
			@Override
			public void run() {
				fileUpload.setText( precentageText );
			}
		});
	}

	public void cleanup(){
		// Remove temporary file
		if( null != receiver.getTargetFile() && receiver.getTargetFile().exists() ){
			if( receiver.getTargetFile().delete() ){
				m_log.debug( "UploadComponent - Deleted : " + receiver.getTargetFile().getAbsolutePath() );
			}else{
				m_log.error( "UploadComponent - Fail to delete " + receiver.getTargetFile().getAbsolutePath() );
			}
		}
	}

}
