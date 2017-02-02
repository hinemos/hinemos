/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.hub.view.action;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

import com.clustercontrol.hub.util.HubEndpointWrapper;
import com.clustercontrol.hub.view.LogFormatView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.ws.hub.InvalidRole_Exception;
import com.clustercontrol.ws.hub.LogFormatUsed_Exception;

/**
 * ログフォーマットの削除を行うクライアント側アクションクラス<BR>
 *
 */
public class LogFormatDeleteAction extends AbstractHandler implements IElementUpdater{
	/** ログ */
	private static Log m_log = LogFactory.getLog(LogFormatDeleteAction.class);
	public static final String ID = LogFormatDeleteAction.class.getName();

	private IWorkbenchWindow window;
	private IWorkbenchPart viewPart;

	/**
	 * Handler execution
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		this.viewPart = HandlerUtil.getActivePart(event);

		// ログ[フォーマット]一覧より、選択されているフォーマットIDを取得
		LogFormatView view = (LogFormatView) this.viewPart
				.getAdapter(LogFormatView.class);

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		Map<String, List<String>> map = view.getSelectedItem();

		String[] args = new String[1];
		String msg = null;
		String endMsg = null;

		int i = 0;
		int deleteSize = 0;
		String formatId = null;
		for(Map.Entry<String, List<String>> entry : map.entrySet()) {
			for(String id : entry.getValue()) {
				formatId = id;
				deleteSize++;
			}
		}

		if (deleteSize > 0) {
			// 確認ダイアログにて変更が選択された場合、削除処理を行う。
			if(deleteSize == 1) {
				msg = "message.hub.log.format.delete.single";
				endMsg = "message.hub.log.format.delete.single.finish";
				args[0] = formatId;
			} else {
				msg = "message.hub.log.format.delete.multi";
				endMsg = "message.hub.log.format.delete.multi.finish";
				args[0] = String.valueOf(deleteSize);
			}

			Map<String, String> errorMsgs = new ConcurrentHashMap<>();
			StringBuffer messageArg = new StringBuffer();
			i = 0;

			if (MessageDialog.openConfirm(
					null,
					Messages.getString("confirmed"),
					Messages.getString(msg, args))) {
				for(Map.Entry<String, List<String>> entry : map.entrySet()) {
					String managerName = entry.getKey();
					HubEndpointWrapper wrapper = HubEndpointWrapper.getWrapper(managerName);
					if(i > 0) {
						messageArg.append(", ");
					}
					messageArg.append(managerName);
					try {
						wrapper.deleteLogFormat(entry.getValue());
					} catch (Exception e) {
						if (e instanceof InvalidRole_Exception) {
							// 権限なし
							errorMsgs.put(managerName, Messages.getString("message.accesscontrol.16"));
						} else if (e instanceof LogFormatUsed_Exception){
							errorMsgs.put(managerName, HinemosMessage.replace(e.getMessage()));
						} else {
							errorMsgs.put(managerName,
									Messages.getString("message.hub.log.format.delete.failed")
									+ ", " + e.getMessage());
						}
					}
					i++;
				}

				//メッセージ表示
				if( 0 < errorMsgs.size() ){
					UIManager.showMessageBox(errorMsgs, true);
				} else {
					// 成功報告ダイアログを生成
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString(endMsg, args));
				}

				// ビューを更新
				view.update();
			}
		}
		return null;
	}

	/**
	 * Dispose
	 */
	@Override
	public void dispose(){
		this.viewPart = null;
		this.window = null;
	}

	/**
	 * Update handler status
	 */
	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();

				if( part instanceof LogFormatView  ){
					// Enable button when 1 item is selected
					this.setBaseEnabled( 0 < ((LogFormatView) part).getSelectedNum() );
				}else{
					this.setBaseEnabled( false );
				}
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
