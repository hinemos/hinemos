/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.view.action;

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

import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.rpa.view.RpaManagementToolAccountView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;

/**
 * RPA設定[RPA管理ツールアカウント]]ビューの削除アクションクラス
 * 
 */
public class AccountDeleteAction extends AbstractHandler implements IElementUpdater{

	// ログ
	private static Log m_log = LogFactory.getLog(AccountDeleteAction.class);

	/** アクションID */
	public static final String ID = AccountDeleteAction.class.getName();

	/** ビュー */
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
		
		// 選択アイテムの取得
		RpaManagementToolAccountView view = (RpaManagementToolAccountView) this.viewPart
				.getAdapter(RpaManagementToolAccountView.class);
		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}
		Map<String, List<String>> map = view.getSelectedItem();
		
		String[] args = new String[1];
		String msg = null;

		int i = 0;
		String rpaScopeId = null;
		for(Map.Entry<String, List<String>> entry : map.entrySet()) {
			for(String id : entry.getValue()) {
				rpaScopeId = id;
				i++;
			}
		}
		
		if (i > 0) {
			
			if(i == 1) {
				msg = "message.rpa.account.delete.confirm";
				args[0] = rpaScopeId;
			} else {
				msg = "message.rpa.accounts.delete.confirm";
				args[0] = Integer.toString(i);
			}
			
			Map<String, String> errorMsgs = new ConcurrentHashMap<String, String>();
			StringBuffer messageArg = new StringBuffer();
			i = 0;
			
			// 選択アイテムがある場合に、削除処理を呼び出す
			if (MessageDialog.openConfirm(null,
					Messages.getString("confirmed"),
					Messages.getString(msg, args))) {
				for (Map.Entry<String, List<String>> entry : map.entrySet()) {
					String managerName = entry.getKey();
					RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
					
					if(i > 0) {
						messageArg.append(", ");
					}
					messageArg.append(managerName);

					// 削除処理
					try {
						wrapper.deleteRpaManagementToolAccount(String.join(",", entry.getValue()));
					} catch (Exception e) {
						errorMsgs.put(managerName, e.getMessage());
					}
					i++;
				}
				
				//メッセージ表示
				if(errorMsgs.size() > 0 ){
					UIManager.showMessageBox(errorMsgs, true);
				} else {
					args[0] = messageArg.toString();
					// 成功報告ダイアログを生成
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.rpa.account.delete", args));
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

				if( part instanceof RpaManagementToolAccountView ){
					// Enable button when 1 item is selected
					this.setBaseEnabled( 0 < ((RpaManagementToolAccountView) part).getSelectedNum() );
				}else{
					this.setBaseEnabled( false );
				}
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
