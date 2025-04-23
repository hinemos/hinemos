/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.view.action;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.openapitools.client.model.InfraFileInfoResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.client.ui.util.FileDownloader;
import com.clustercontrol.infra.action.GetInfraFileManagerTableDefine;
import com.clustercontrol.infra.util.InfraFileUtil;
import com.clustercontrol.infra.util.InfraRestClientWrapper;
import com.clustercontrol.infra.view.InfraFileManagerView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;


public class DownloadInfraFileAction extends InfraFileManagerBaseAction {
	// ログ
	private static Log m_log = LogFactory.getLog( DownloadInfraFileAction.class );

	/** アクションID */
	public static final String ID = DownloadInfraFileAction.class.getName();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		InfraFileManagerView view = getView(event);
		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		InfraFileInfoResponse info = InfraFileUtil.getSelectedInfraFileInfo(view);
		if (info == null) {
			return null;
		}

		String action = Messages.getString("download");

		FileDialog fd = new FileDialog(window.getShell(), SWT.SAVE);
		String fileName = info.getFileName();
		fd.setFileName(fileName);
		String selectedFilePath = null;
		try{
			selectedFilePath = fd.open();
		}catch( Exception e ){
			m_log.error(e);
 			InfraFileUtil.showFailureDialog(action, HinemosMessage.replace(e.getMessage()));
 			return null;
		}

		// path is null when dialog cancelled
		if( null != selectedFilePath ){
			StructuredSelection selection = null;
			if(view.getComposite().getTableViewer().getSelection() instanceof StructuredSelection){
				selection = (StructuredSelection) view.getComposite().getTableViewer().getSelection();
			}
			String managerName = null;
			if(selection != null && selection.isEmpty() == false){
				managerName = (String)((ArrayList<?>)selection.getFirstElement()).get(GetInfraFileManagerTableDefine.MANAGER_NAME);
			}
			InfraRestClientWrapper wrapper = InfraRestClientWrapper.getWrapper(managerName);
			try {
				File downloadFile = wrapper.downloadInfraFile(info.getFileId());
				File file = new File(selectedFilePath);
				Files.move(downloadFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
				if( ClusterControlPlugin.isRAP() ){
					FileDownloader.openBrowser(window.getShell(), selectedFilePath, fileName);
				}else{
					InfraFileUtil.showSuccessDialog(action, info.getFileId());
				}
			} catch (Exception e) {
				m_log.error(e);
				InfraFileUtil.showFailureDialog(action, HinemosMessage.replace(e.getMessage()));
			} finally {
				FileDownloader.cleanup(selectedFilePath);
			}
		}

		return null;
	}
}
