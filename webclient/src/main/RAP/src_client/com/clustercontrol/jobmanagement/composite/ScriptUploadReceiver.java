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
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.rap.addons.fileupload.FileDetails;
import org.eclipse.rap.addons.fileupload.FileUploadReceiver;

/**
 * ScriptUploadReceiver
 * 
 * @see org.eclipse.rap.addons.fileupload.DiskFileUploadReceiver
 * 
 * @version 6.0.0
 * @since 6.0.0
 */
public class ScriptUploadReceiver extends FileUploadReceiver{

	public static int BUFFER_SIZE = 1024;

	private ByteArrayOutputStream baos = new ByteArrayOutputStream();

	public ScriptUploadReceiver(){
	}

	@Override
	public void receive(InputStream dataStream, FileDetails details) throws IOException {
		try{
			copy(dataStream, baos);
		}finally{
			dataStream.close();
		}
	}
	public ByteArrayOutputStream getOutputStream(){
		return baos;
	}

	private static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		boolean finished = false;
		while(!finished) {
			int bytesRead = inputStream.read(buffer);
			if(bytesRead != -1) {
				outputStream.write(buffer, 0, bytesRead);
			} else {
				finished = true;
			}
		}
	}
}
