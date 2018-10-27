/*
* Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
*
* Hinemos (http://www.hinemos.info/)
*
* See the LICENSE file for licensing information.
*/

package com.clustercontrol.inquiry.view.action;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.inquiry.action.GetInquiryTableDefine;
import com.clustercontrol.inquiry.util.InquiryEndpointWrapper;
import com.clustercontrol.inquiry.view.InquiryView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.ws.inquiry.HinemosUnknown_Exception;
import com.clustercontrol.ws.inquiry.InquiryTargetCreating_Exception;
import com.clustercontrol.ws.inquiry.InquiryTargetCommandNotFound_Exception;
import com.clustercontrol.ws.inquiry.InquiryTargetNotFound_Exception;
import com.clustercontrol.ws.inquiry.InvalidRole_Exception;
import com.clustercontrol.ws.inquiry.InvalidUserPass_Exception;

/**
 * 遠隔管理ビューの作成アクションクラス<BR>
 *
 * @version 6.1.0
 * @since 6.1.0
 */
public class CreateTargetDataAction extends AbstractHandler implements IElementUpdater {
	/** ログ */
	private static Log m_log = LogFactory.getLog(CreateTargetDataAction.class);
	
	/** アクションID */
	public static final String ID = "com.clustercontrol.enterprise.inquiry.view.action.CreateTargetDataAction"; //$NON-NLS-1$

	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		InquiryView view = null;
		try {
			view = (InquiryView) this.viewPart.getAdapter(InquiryView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}
		
		if (!(view.getComposite().getTableViewer().getSelection() instanceof StructuredSelection)) {
			m_log.info("execute: selection is not StructuredSelection class."); 
			return null;
		}
		
		StructuredSelection selection = (StructuredSelection) view.getComposite().getTableViewer().getSelection();
		List<?> data = (List<?>) selection.getFirstElement();
		
		String targetId = (String) data.get(GetInquiryTableDefine.ID);
		String managerName = (String) data.get(GetInquiryTableDefine.MANAGER_NAME);
		if (MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				MessageFormat.format(Messages.getString("message.inquiry.target.data.create.question"), targetId))) {
			Map<String, String> errorMsg = new HashMap<>();
			try {
				//create endpoint 実行
				InquiryEndpointWrapper wrapper = InquiryEndpointWrapper.getWrapper(managerName);
				wrapper.prepare(targetId);
			
				// 成功報告ダイアログを生成
				MessageDialog.openInformation(
						null,
						Messages.getString("successful"),
						MessageFormat.format(Messages.getString("message.inquiry.target.data.create.success"), targetId));
				
				view.update();
				
			//マネージャからエラー、エンドポイントへのアクセス失敗時
			} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception | InquiryTargetCreating_Exception | InquiryTargetNotFound_Exception | InquiryTargetCommandNotFound_Exception e) {
				m_log.info(e.getMessage());
				
				errorMsg.put(managerName, HinemosMessage.replace(e.getMessage()));
				UIManager.showMessageBox(errorMsg, true);
				return null;
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