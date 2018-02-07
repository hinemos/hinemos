/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.platform.rap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataHandler;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.ws.xcloud.TargetType;
import com.clustercontrol.xcloud.platform.PlatformDependent;
import com.clustercontrol.xcloud.ui.dialogs.DetailDialog;

public class RAPPlatformDependent extends PlatformDependent {

	@Override
	public DetailDialog createDetailDialog(Shell parentShell, String dialogTitle) {
		return new DetailDialog(parentShell, dialogTitle) {
			@Override
			protected void customizeDialog(Composite parent) {
				GridLayout gl_parent = new GridLayout(1, false);
 				gl_parent.marginBottom = 10;
 				gl_parent.marginTop = 10;
 				gl_parent.marginRight = 10;
 				gl_parent.marginLeft = 10;
 				
 				parent.setLayout(gl_parent);
 				GridData gd_parent = new GridData(GridData.FILL_BOTH);
 				parent.setLayoutData(gd_parent);
 				
				Text detail = new Text(parent, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP);
				detail.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				if(this.input != null){
					detail.setText(input);
				}
				
				getShell().setSize(getInitialSize());
 				
 				Display display = getShell().getDisplay();
 				getShell().setLocation((display.getBounds().width - getShell().getSize().x) / 2,
 						(display.getBounds().height - getShell().getSize().y) / 2);
			}
		};
	}

	@Override
	public void downloadBillingDetail(Shell parent, final TargetType type, final String targetId, final int year, final int month, final DataHandler handler) throws Exception {
		Downloader.download(parent, new Downloader.DownloadHandler() {
			@Override
			public void download(HttpServletResponse response) throws ServletException, IOException {
				String filename = null;
				switch (type) {
				case FACILITY:
					filename = String.format("hinemos_cloud_billing_detail_facility_%s_%04d%02d.csv.zip", targetId, year, month);
					break;
				case CLOUD_SCOPE:
					filename = String.format("hinemos_cloud_billing_detail_account_%s_%04d%02d.csv.zip", targetId, year, month);
					break;
				}
				
				response.setContentType("application/octet-stream");
//				response.setContentLength( (int)file.length() );
				response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
				
				try (OutputStream os = response.getOutputStream()) {
					try (InputStream is = handler.getInputStream()) {
						int len = 0;
						byte[] buffer = new byte[1024];
						while ((len = is.read(buffer)) >= 0) {
							os.write(buffer, 0, len);
						}
					}
				}
			}
		});
	}

	@Override
	public boolean isRapPlatfome() {
		return true;
	}
}
