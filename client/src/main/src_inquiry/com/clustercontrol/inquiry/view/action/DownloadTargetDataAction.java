/*
* Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
*
* Hinemos (http://www.hinemos.info/)
*
* See the LICENSE file for licensing information.
*/

package com.clustercontrol.inquiry.view.action;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.client.ui.util.FileDownloader;
import com.clustercontrol.inquiry.action.GetInquiryTableDefine;
import com.clustercontrol.inquiry.util.InquiryEndpointWrapper;
import com.clustercontrol.inquiry.util.InquiryUtil;
import com.clustercontrol.inquiry.view.InquiryView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.ws.inquiry.HinemosUnknown_Exception;
import com.clustercontrol.ws.inquiry.InquiryTarget;
import com.clustercontrol.ws.inquiry.InquiryTargetNotFound_Exception;
import com.clustercontrol.ws.inquiry.InvalidRole_Exception;
import com.clustercontrol.ws.inquiry.InvalidUserPass_Exception;

/**
 * 選択した項目のダウンロードを行うアクションクラス
 *
 * @version 6.1.0
 * @since 6.1.0
 */

public class DownloadTargetDataAction extends AbstractHandler implements IElementUpdater {
	public static final String ID ="com.clustercontrol.enterprise.inquiry.view.action.DownloadTargetDataAction"; //$NON-NLS-1$
	private static Log m_log = LogFactory.getLog( DownloadTargetDataAction.class );

	/** ビュー */
	private IWorkbenchPart viewPart;
	private IWorkbenchWindow window;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.window = null;
		this.viewPart = null;
	}
	
	/**
	 * 選択されているターゲットのデータをエクスポートします。
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( window == null || !isEnabled() ){
			return null;
		}
		
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		
		InquiryView view = null;
		try {
			view = (InquiryView) this.viewPart.getAdapter(InquiryView.class);
		} catch (Exception e) { 
			m_log.info("execute " + HinemosMessage.replace(e.getMessage())); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}
		
		String id = this.getClass().getName();
		m_log.debug("execute id=" + id);

		StructuredSelection selection = null;
		if (view.getComposite().getTableViewer().getSelection() instanceof StructuredSelection) {
			selection = (StructuredSelection) view.getComposite().getTableViewer().getSelection();
		}
		
		List<?> selectionData = null;
		if(selection != null && selection.isEmpty() == false){
			selectionData = (ArrayList<?>) selection.getFirstElement();
		} else {
			m_log.debug("selection is empty or null."); 
			return null;
		}
		FileDialog dialog = new FileDialog(window.getShell(), SWT.SAVE);
		
		String managerName = (String) selectionData.get(GetInquiryTableDefine.MANAGER_NAME);
		InquiryEndpointWrapper wrapper = InquiryEndpointWrapper.getWrapper(managerName);
		
		String targetId = (String) selectionData.get(GetInquiryTableDefine.ID);
		InquiryTarget target;
		try {
			target = wrapper.getInquiryTarget(targetId);
		} catch(HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception | InquiryTargetNotFound_Exception e) {
			Map<String, String> errorMsg = new HashMap<>();
			errorMsg.put(managerName, HinemosMessage.replace(e.getMessage()));
			UIManager.showMessageBox(errorMsg, true);
			return null;
		}
		
		String targetFileName = target.getFileName();
		
		String name;
		String extension;
		if (targetFileName.endsWith(".tar.gz")) {
			extension = ".tar.gz";
			name = targetFileName.substring(0, targetFileName.length() - extension.length());
		} else {
			int lastSeparatorIndex = targetFileName.lastIndexOf(".");
			if (lastSeparatorIndex != -1) {
				name = targetFileName.substring(0, lastSeparatorIndex);
				extension = targetFileName.substring(lastSeparatorIndex);
			} else {
				name = targetFileName;
				extension = "";
			}
		}
		
		String fileName;
		if( ClusterControlPlugin.isRAP() ){
			fileName = name + "_" + extension;
		}else{
			fileName = targetFileName;
		}
		
		// ファイル名に空白があると+に置き換わってしまうため、空白を削除
		fileName = fileName.replaceAll(" ", "_");
		
		dialog.setFileName(fileName);
		dialog.setFilterExtensions(new String[]{"*" + extension});
		dialog.setOverwrite(true);
		
		String selectedFilePath = dialog.open();
		
		// path is null when dialog cancelled
		if( null != selectedFilePath ){
			Map<String, String> errorMsg = new HashMap<>();
			try {
				DataHandler dh = wrapper.download(targetId);
				try (FileOutputStream fos = new FileOutputStream(new File(selectedFilePath))) {
					dh.writeTo(fos);
				}
				
				if( ClusterControlPlugin.isRAP() ){
					FileDownloader.openBrowser(window.getShell(), selectedFilePath, dialog.getFileName());
				}else{
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.inquiry.target.data.download.success") + dialog.getFileName());

				}
			} catch (Exception e) {
				m_log.info(HinemosMessage.replace(e.getMessage()), e);
				errorMsg.put(managerName, HinemosMessage.replace(e.getMessage()));
			} finally {
				if (errorMsg.size() > 0)
					UIManager.showMessageBox(errorMsg, true);
			}
		}
		return null;
	}
	
	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();

				boolean enable = false;
				if (part instanceof InquiryView) {
					InquiryView view = (InquiryView) part.getAdapter(InquiryView.class);

					if (view == null) {
						m_log.info("execute: view is null");
						return;
					}

					StructuredSelection selection = null;
					if (view.getComposite().getTableViewer().getSelection() instanceof StructuredSelection) {
						selection = (StructuredSelection) view.getComposite().getTableViewer().getSelection();
					}

					if (selection != null && selection.size() == 1) {
						String status = (String) ((List<?>) selection.getFirstElement()).get(GetInquiryTableDefine.STATUS);
						if (InquiryUtil.isDownloadable(status))
							enable = true;
					}
				}
				this.setBaseEnabled(enable);
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}