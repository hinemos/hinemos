/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.util;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;

import com.clustercontrol.infra.action.GetInfraFileManagerTableDefine;
import com.clustercontrol.infra.view.InfraFileManagerView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.infra.InfraFileInfo;

public class InfraFileUtil {
	public static String joinStringList(List<String> strList, String separator) {
		StringBuilder builder = new StringBuilder();
		for (String str : strList) {
			builder.append(str);
			builder.append(separator);
		}
		if (builder.length() != 0) {
			builder.delete(builder.length() - separator.length(),
					builder.length());
		}

		return builder.toString();
	}

	public static void showSuccessDialog(String action, String extraArg) {
		MessageDialog
				.openInformation(
						null,
						Messages.getString("confirmed"),
						Messages.getString(
								"message.infra.action.result",
								new Object[] {
										Messages.getString("file"),
										action,
										Messages.getString("successful"),
										extraArg }));
	}

	public static void showFailureDialog(String action, String extraArg) {
		MessageDialog
				.openError(
						null,
						Messages.getString("failed"),
						Messages.getString(
								"message.infra.action.result",
								new Object[] {
										Messages.getString("file"),
										action,
										Messages.getString("failed"), extraArg }));
	}
	
	public static String getManagerName(InfraFileManagerView view) {
		StructuredSelection selection = null;
		if(view.getComposite().getTableViewer().getSelection() instanceof StructuredSelection){
			selection = (StructuredSelection) view.getComposite().getTableViewer().getSelection();
		}
	
		return getManagerName(selection);
	}
	
	public static InfraFileInfo getSelectedInfraFileInfo(InfraFileManagerView view) {
		StructuredSelection selection = null;
		if(view.getComposite().getTableViewer().getSelection() instanceof StructuredSelection){
			selection = (StructuredSelection) view.getComposite().getTableViewer().getSelection();
		}
	
		return getSelectedInfraFileInfo(selection);
	}

	public static String getManagerName(StructuredSelection selection) {
		if(selection == null){
			return null;
		}
		
		@SuppressWarnings("unchecked")
		List<String> itemList = (List<String>)selection.toList().get(0);
		return itemList.get(GetInfraFileManagerTableDefine.MANAGER_NAME);
	}
	
	public static InfraFileInfo getSelectedInfraFileInfo(StructuredSelection selection) {
		if(selection == null){
			return null;
		}
		
		InfraFileInfo info = new InfraFileInfo();
		
		@SuppressWarnings("unchecked")
		List<String> itemList = (List<String>)selection.toList().get(0);
		info.setFileId(itemList.get(GetInfraFileManagerTableDefine.FILE_ID));
		info.setFileName(itemList.get(GetInfraFileManagerTableDefine.FILE_NAME));
		info.setOwnerRoleId(itemList.get(GetInfraFileManagerTableDefine.OWNER_ROLE));
		
		return info;
	}
}
