/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.platform.rap;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ServiceHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;

public class Downloader {
	public interface DownloadHandler {
		void download( HttpServletResponse response) throws ServletException, IOException;
	}
	
	public final static String DOWNLOAD_HANDLER = "xcloudDownloadHandler";

	static {
		RWT.getServiceManager().registerServiceHandler(DOWNLOAD_HANDLER,
		new ServiceHandler(){
			@Override
			public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
				DownloadHandler handler = (DownloadHandler)RWT.getUISession().getAttribute(DOWNLOAD_HANDLER);
				handler.download(response);
			}
		});
	}

	private static String makeUrl() {
		final StringBuilder url = new StringBuilder();
		url.append(RWT.getServiceManager().getServiceHandlerUrl(DOWNLOAD_HANDLER));
		url.append("&nocache=").append(System.currentTimeMillis());

		return RWT.getResponse().encodeURL(url.toString());
	}

	public static boolean download(Composite parent, DownloadHandler handler){
		// Browser for sending file
		Browser browser = new Browser(parent, SWT.NONE);
		browser.setSize( 0, 0 );
		
		RWT.getUISession().setAttribute(DOWNLOAD_HANDLER, handler);
		return browser.setUrl(makeUrl());
	}
}
