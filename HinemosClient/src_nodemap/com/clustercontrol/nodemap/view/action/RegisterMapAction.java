/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.view.action;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.nodemap.view.NodeMapView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.nodemap.InvalidRole_Exception;

/**
 * ビューの更新を行うクライアント側アクションクラス<BR>
 * @since 1.0.0
 */
public class RegisterMapAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog( RegisterMapAction.class );

	/** アクションID */
	public static final String ID = OpenNodeMapAction.ActionIDBase + RegisterMapAction.class.getSimpleName();

	/** ビュー */
	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == window || !isEnabled() ){
			return null;
		}

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		
		NodeMapView view = (NodeMapView) viewPart.getAdapter(NodeMapView.class);
		if (!MessageDialog.openQuestion(
				null, com.clustercontrol.util.Messages.getString("confirmed"),
				com.clustercontrol.nodemap.messages.Messages.getString("regist.question"))) {
			return null;
		}
		try {
			view.registerNodeMap();
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					com.clustercontrol.nodemap.messages.Messages.getString("regist.success"));
		} catch (InvalidRole_Exception e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			m_log.warn("run() registerNodeMap, " + e.getMessage(), e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					com.clustercontrol.nodemap.messages.Messages.getString("regist.fail") + ", " + e.getMessage());
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

				boolean editEnable = false;
				if(part instanceof NodeMapView){
					// Enable button when 1 item is selected
					NodeMapView view = (NodeMapView)part;
					editEnable = view.isEditableMode();
				}

				this.setBaseEnabled(editEnable);
			}
		}
	}
}
