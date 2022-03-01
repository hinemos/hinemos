/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.platform.rcp;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.BillingResultResponse.TypeEnum;

import com.clustercontrol.xcloud.platform.PlatformDependent;
import com.clustercontrol.xcloud.ui.dialogs.DetailDialog;

public class RCPPlatformDependent extends PlatformDependent {

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
 				
				StyledText detail = new StyledText(parent, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP);
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
	public void downloadBillingDetail(Shell parent, TypeEnum type, String targetId, int year, int month, File file) throws Exception {
		FileDialog dialog = new FileDialog(parent, SWT.SAVE);
		String filename = null;
		switch (type) {
		case FACILITY:
			filename = String.format("hinemos_cloud_billing_detail_facility_%s_%04d%02d.csv.zip", targetId, year, month);
			break;
		case CLOUDSCOPE:
			filename = String.format("hinemos_cloud_billing_detail_account_%s_%04d%02d.csv.zip", targetId, year, month);
			break;
		}
		
		dialog.setFileName(filename);
		dialog.setFilterExtensions(new String[]{"*.zip"});
		dialog.setOverwrite(true);
		String filePath = dialog.open();
		
		if (filePath == null) return;

		try (FileInputStream is = new FileInputStream(file)) {
			Files.copy(is, new File(filePath).toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	@Override
	public boolean isRapPlatfome() {
		return false;
	}
}
