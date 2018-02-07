/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.composite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.addons.fileupload.FileDetails;
import org.eclipse.rap.addons.fileupload.FileUploadReceiver;

/**
 * FileUploadReceiver for Infra File
 * 
 * @see org.eclipse.rap.addons.fileupload.DiskFileUploadReceiver
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class InfraFileUploadReceiver extends FileUploadReceiver{

	private static Log m_log = LogFactory.getLog( InfraFileUploadReceiver.class );

	private static final String TEMP_FILE_PREFIX = "infra_";
	public static int BUFFER_SIZE = 1024;

	private File targetFile;

	public InfraFileUploadReceiver(){
	}

	@Override
	public void receive( InputStream dataStream, FileDetails details ) throws IOException{
		targetFile = createTargetFile( details );
		FileOutputStream outputStream = new FileOutputStream( targetFile );
		try{
			copy( dataStream, outputStream );
		}finally{
			dataStream.close();
			outputStream.close();
		}
	}

	/**
	 * Returns file that the received data has been saved to.
	 *
	 * @return the target file or null if no files have been stored yet
	 */
	public File getTargetFile(){
		return targetFile;
	}

	/**
	 * Creates a file to save the received data to. Subclasses may override.
	 *
	 * @param details
	 *            the details of the uploaded file like file name, content-type
	 *            and size
	 * @return the file to store the data in
	 */
	protected File createTargetFile( FileDetails details ) throws IOException{
		File result = File.createTempFile( TEMP_FILE_PREFIX, "" );
		boolean b = result.createNewFile();
		if (!b) {
			m_log.info("file exists.");
		}
		return result;
	}

	private static void copy( InputStream inputStream, OutputStream outputStream ) throws IOException{
		byte[] buffer = new byte[BUFFER_SIZE];
		boolean finished = false;
		while( !finished ){
			int bytesRead = inputStream.read( buffer );
			if( bytesRead != -1 ){
				outputStream.write( buffer, 0, bytesRead );
			}else{
				finished = true;
			}
		}
	}
}
