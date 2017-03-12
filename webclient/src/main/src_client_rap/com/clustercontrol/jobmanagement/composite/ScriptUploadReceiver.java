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
