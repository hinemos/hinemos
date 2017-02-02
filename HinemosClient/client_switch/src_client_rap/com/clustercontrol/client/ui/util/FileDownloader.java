/**********************************************************************
 * Copyright (C) 2006 NTT DATA Corporation
 * This program is free software; you can redistribute it and/or
 * Modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2.
 * 
 * This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *********************************************************************/

package com.clustercontrol.client.ui.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ServiceHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;

import com.clustercontrol.util.Messages;


/**
 * File download with Browser widget
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class FileDownloader {
	public final static String DOWNLOAD_HANDLER = "filedownload";
	public final static String FILE_KEY="dl";
	public final static String FILENAME_PARAM = "dl";
	public static boolean handlerRegistered = false;
	public static int BUFFER_SIZE = 1024 * 32;

	/**
	 * DownloadService handler
	 * 
	 * One-time download.
	 */
	private static void registerDownloadHandler() {
		if (!handlerRegistered) {
			try{
				RWT.getServiceManager().registerServiceHandler(DOWNLOAD_HANDLER,
						new ServiceHandler(){
							@Override
							public void service(HttpServletRequest request,
									HttpServletResponse response)
									throws IOException, ServletException {
									// Send the file
									outputStream(response, request.getParameter(FILENAME_PARAM));
							}
							private void outputStream( HttpServletResponse response, String filename) throws ServletException, IOException {
								OutputStream outStream = response.getOutputStream();
								File file = (File) RWT.getUISession().getAttribute(filename);
								try {
									FileInputStream fileStream = new FileInputStream(file);
									BufferedInputStream bufferedStream = new BufferedInputStream(fileStream);

									// Response header
									response.setContentType("application/octet-stream");
									response.setContentLength( (int)file.length() );
									response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, "UTF-8"));

									int len = 0;
									byte[] buffer = new byte[FileDownloader.BUFFER_SIZE];
									while ((len = bufferedStream.read(buffer)) >= 0) {
										outStream.write(buffer, 0, len);
									}
									bufferedStream.close();
								} catch (FileNotFoundException e){
									printOutNotFound( response, HttpServletResponse.SC_NOT_FOUND, "Not Found" );
								} catch (IOException e){
									printOutNotFound( response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error" );
								} finally {
									if (outStream != null) {
										try {
											outStream.close();
										} catch (IOException e) {
										} finally {
											outStream = null;
										}
									}
								}
							}

							private void printOutNotFound(HttpServletResponse response, int sc, String msg) throws IOException{
									response.setStatus( sc );
									response.sendError(sc, msg);
							}
				});
				handlerRegistered = true;
			}catch( IllegalArgumentException e ){
			}
		}
	}

	/**
	 * Generate download URL
	 * 
	 * @return URL
	 */
	private static String generateUrl(String filename, File tmpFile){
		final StringBuilder url = new StringBuilder();
		url.append(RWT.getServiceManager().getServiceHandlerUrl( DOWNLOAD_HANDLER) );
		url.append("&").append(FILENAME_PARAM).append("=");
		url.append(filename);
		RWT.getUISession().setAttribute(filename, tmpFile);

		url.append("&nocache=").append(System.currentTimeMillis());

		return RWT.getResponse().encodeURL(url.toString());
	}

	public static boolean openBrowser( Composite parent, String path, String filename ){
		// Register download handler at first
		registerDownloadHandler();

		// Browser for sending file
		Browser browser = new Browser(parent, SWT.NONE);
		browser.setSize( 0, 0 );

		File tmpFile = null;
		tmpFile = new File(path);
		tmpFile.deleteOnExit();
		// Start download
		if (browser.setUrl(generateUrl(filename, tmpFile))) {
			MessageDialog.openInformation(parent.getShell(), Messages.getString("download"), 
					Messages.getString("download.message", new String[] { filename }));
			return true;
		}
		return false;
	}

	/**
	 * Delete the temporary file on server-side
	 * 
	 * @return URL
	 */
	public static void cleanup(String path){
		File tmpFile = new File(path);

		// Delete temporary file
		if( null != tmpFile && tmpFile.exists() ){
			if (!tmpFile.delete()) {
				Logger.getLogger(FileDownloader.class).debug("Fial to delete file. " + tmpFile.getAbsolutePath()); 
			}
		}
	}

}
