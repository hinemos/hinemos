/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.view.action;

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

import com.clustercontrol.reporting.view.action.ReportingDeleteAction;
import com.clustercontrol.reporting.util.ReportingEndpointWrapper;
import com.clustercontrol.reporting.view.ReportingScheduleView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.ws.reporting.InvalidRole_Exception;

/**
 * レポーティング[スケジュール]ビューの削除アクションクラス<BR>
 * 
 * @version 5.0.a
 * @since 4.1.2
 */
public class ReportingDeleteAction extends AbstractHandler implements IElementUpdater{

	// ログ
	private static Log m_log = LogFactory.getLog(ReportingDeleteAction.class);

	/** アクションID */
	public static final String ID = ReportingDeleteAction.class.getName();

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
		ReportingScheduleView view = (ReportingScheduleView) this.viewPart
				.getAdapter(ReportingScheduleView.class);
		
		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}
		
		Map<String, List<String>> map = view.getSelectedItem();
		
		String[] args = new String[1];
		String msg = null;

		int i = 0;
		String scheduleId = null;
		for(Map.Entry<String, List<String>> entry : map.entrySet()) {
			for(String id : entry.getValue()) {
				scheduleId = id;
				i++;
			}
		}
		
		if (i > 0) {
			
			if(i == 1) {
				msg = "message.reporting.7";
				args[0] = scheduleId;
			} else {
				msg = "message.reporting.23";
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
					ReportingEndpointWrapper wrapper = ReportingEndpointWrapper.getWrapper(managerName);
					
					if(i > 0) {
						messageArg.append(", ");
					}
					messageArg.append(managerName);
					try {
						for (String entryScheduleId : entry.getValue()) {
							wrapper.deleteReporting(entryScheduleId);
						}
					} catch (Exception e) {
						if (e instanceof InvalidRole_Exception) {
							// 権限なし
							errorMsgs.put(managerName, Messages.getString("message.accesscontrol.16"));
						} else {
							String errMessage = HinemosMessage.replace(e.getMessage());
							errorMsgs.put(managerName, Messages.getString("message.reporting.6") +
									", " + errMessage);
						}
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
							Messages.getString("message.reporting.5", args));
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

				if( part instanceof ReportingScheduleView ){
					// Enable button when 1 item is selected
					this.setBaseEnabled( 0 < ((ReportingScheduleView) part).getSelectedNum() );
				}else{
					this.setBaseEnabled( false );
				}
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
