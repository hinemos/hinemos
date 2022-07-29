/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.ui.dialog;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.ui.preference.SettingToolsXMLPreferencePage;
import com.clustercontrol.utility.util.MultiManagerPathUtil;



public class OpenFolderDialog  {
	public OpenFolderDialog(Shell parent) {
	
		DirectoryDialog openDialog = new DirectoryDialog(parent);
		
		openDialog.setText(Messages.getString("dialog.message1"));
		openDialog.setMessage(Messages.getString("message.xmlselect"));
		
		String filePath = MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML);
		openDialog.setFilterPath(filePath);
		String openDir = openDialog.open();
		SettingToolsXMLPreferencePage.writeXmlProperties(openDir);
	}
}
