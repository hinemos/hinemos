/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view.action;

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

import com.clustercontrol.jobmap.util.JobmapIconImageCache;
import com.clustercontrol.jobmap.view.JobMapImageListView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.ws.calendar.InvalidRole_Exception;

/**
 * ジョブマップ用イメージファイルの削除を行うクライアント側アクションクラス<BR>
 *
 * @version 6.0.a
 * @since 2.0.0
 */
public class JobMapImageDeleteAction extends AbstractHandler implements IElementUpdater{
	private static Log m_log = LogFactory.getLog(JobMapImageDeleteAction.class);
	public static final String ID = BaseAction.ActionIdBase + JobMapImageDeleteAction.class.getSimpleName();

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
		if (!(viewPart instanceof JobMapImageListView)) {
			return null;
		}

		// ジョブマップ用アイコンファイル一覧より、選択されているアイコンIDを取得
		JobMapImageListView listView = null;
		try {
			listView = (JobMapImageListView) this.viewPart
					.getAdapter(JobMapImageListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + HinemosMessage.replace(e.getMessage())); 
			return null; 
		}
		Map<String, List<String>> map = listView.getSelectedItem();

		String[] args = new String[1];
		String msg = null;

		int i = 0;
		String iconId = null;
		for(Map.Entry<String, List<String>> entry : map.entrySet()) {
			for(String id : entry.getValue()) {
				iconId = id;
				i++;
			}
		}

		if (i > 0) {
			// 確認ダイアログにて変更が選択された場合、削除処理を行う。
			if(i == 1) {
				msg = "message.job.144";
				args[0] = iconId;
			} else {
				msg = "message.job.145";
				args[0] = Integer.toString(i);
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
					if(i > 0) {
						messageArg.append(", ");
					}
					messageArg.append(managerName);
					try {
						JobmapIconImageCache.deleteJobmapIconImage(managerName, entry.getValue());
					} catch (Exception e) {
						if (e instanceof InvalidRole_Exception) {
							// 権限なし
							errorMsgs.put(managerName, Messages.getString("message.accesscontrol.16"));
						} else {
							errorMsgs.put(managerName, Messages.getString("message.job.146") +
									", " + HinemosMessage.replace(e.getMessage()));
						}
					}
					i++;
				}

				//メッセージ表示
				if( 0 < errorMsgs.size() ){
					UIManager.showMessageBox(errorMsgs, true);
				} else {
					args[0] = messageArg.toString();
					// 成功報告ダイアログを生成
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.job.147", args));
				}

				// ビューを更新
				listView.update();
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

				if( part instanceof JobMapImageListView  ){
					// Enable button when 1 item is selected
					this.setBaseEnabled( 0 < ((JobMapImageListView) part).getSelectedNum() );
				}else{
					this.setBaseEnabled( false );
				}
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
