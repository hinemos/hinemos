/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.ui.composite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.addons.fileupload.FileDetails;
import org.eclipse.rap.addons.fileupload.FileUploadEvent;
import org.eclipse.rap.addons.fileupload.FileUploadHandler;
import org.eclipse.rap.addons.fileupload.FileUploadListener;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.rap.rwt.widgets.FileUpload;
import org.eclipse.swt.widgets.Composite;

import com.clustercontrol.utility.ui.settings.composite.UtilityFileUploadReceiver;

public class UtilityFileUpload extends FileUpload {
	/** ログ出力用 */
	private static Log log = LogFactory.getLog(UtilityFileUpload.class);
	
	public UtilityFileUpload(Composite parent, int style) {
		super(parent, style);
		url = startUploadReceiver();
	}

	private UtilityFileUploadReceiver receiver;
	private ServerPushSession pushSession;
	private String url;

	private String startUploadReceiver() {
		receiver = new UtilityFileUploadReceiver();
		pushSession = new ServerPushSession();
		FileUploadHandler uploadHandler = new FileUploadHandler(receiver);
		uploadHandler.addUploadListener(new FileUploadListener() {
			@Override
			public void uploadProgress(FileUploadEvent event){
				UtilityFileUpload.this.showProgress( ( 100 * event.getBytesRead() / event.getContentLength() ) + "%" );
			}

			@Override
			public void uploadFinished(FileUploadEvent event) {
				FileDetails[] fileDetails = event.getFileDetails();
				if( 0 < fileDetails.length ){
					log.debug( "uploadFinished : " + fileDetails[0].getFileName() );
				}
				UtilityFileUpload.this.setFree();

				// Stop push session after all
				pushSession.stop();
			}
			@Override
			public void uploadFailed(FileUploadEvent event) {
				// Failed. Just set it free
				UtilityFileUpload.this.setFree();

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
		return getEnabled() && receiver.getTargetFile().canRead();
	}

	public String getFileName(){
		return super.getFileName();
	}

	public String getFilePath(){
		return null != receiver && null != receiver.getTargetFile() ? receiver.getTargetFile().getAbsolutePath() : null;
	}

	public void setBusy(){
		setEnabled( false );
	}

	private void setFree(){
		getDisplay().asyncExec( new Runnable() {
			@Override
			public void run() {
				setEnabled( true );
				if(label != null && !"".equals(label)){
					setText(label);
				}
			}
		});
	}

	private void showProgress(final String precentageText){
		getDisplay().asyncExec( new Runnable() {
			@Override
			public void run() {
				UtilityFileUpload.super.setText( precentageText );
			}
		});
	}

	private String label;
	public void setText( String text ) {
		super.setText(text);
		label = text;
	}

	public void cleanup(){
		// Remove temporary file
		if( null != receiver.getTargetFile() && receiver.getTargetFile().exists() ){
			if( receiver.getTargetFile().delete() ){
				log.debug( "UtilityFileUpload - Deleted : " + receiver.getTargetFile().getAbsolutePath() );
			}else{
				log.error( "UtilityFileUpload - Fail to delete " + receiver.getTargetFile().getAbsolutePath() );
			}
		}
	}

	public void startSession(){
		pushSession.start();
	}
	
	public void submit(){
		submit(url);
	}
}
