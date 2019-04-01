/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.view.action;

import java.util.ArrayList;
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

import com.clustercontrol.monitor.composite.EventListComposite;
import com.clustercontrol.monitor.dialog.EventCustomCommandRunDialog;
import com.clustercontrol.monitor.util.ConvertListUtil;
import com.clustercontrol.monitor.view.EventView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.monitor.EventDataInfo;

/**
 * 監視履歴[イベント・カスタムコマンドの実行]ビューの確認アクションによるイベントの確認の更新処理を行うアクライアント側アクションクラス<BR>
 */
public class EventCustomCommandAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog( EventCustomCommandAction.class );

	/** アクションID */
	public static final String ID = EventCustomCommandAction.class.getName();

	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;


	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
		this.window = null;
	}

	/**
	 * 監視履歴[イベント・カスタムコマンドの実行]ダイアログを表示します。
	 * <p>
	 * <ol>
	 * <li>監視履歴[イベント・カスタムコマンドの実行]ダイアログを表示します。</li>
	 * </ol>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.monitor.view.EventView
	 * @see com.clustercontrol.monitor.dialog.EventCustomCommandRunDialog
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);

		// 選択アイテムを取得します。
		EventView view = null;
		try {
			view = (EventView) this.viewPart.getAdapter(EventView.class);
		} catch (Exception e) {
			m_log.info("execute " + e.getMessage());
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		EventListComposite composite = (EventListComposite)view.getListComposite();
		StructuredSelection selection = (StructuredSelection)composite.getTableViewer().getSelection();

		List<?> selectionList = (List<?>) selection.toList();

		if (selectionList.size() == 0) {
			//選択されているイベントがないので、何もしない
			return null;
		}
		
		ArrayList<EventDataInfo> selectEventList = ConvertListUtil.listToEventLogDataList(selectionList);
		
		//複数マネージャ選択チェック
		
		String managarName = selectEventList.get(0).getManagerName();
		for (EventDataInfo eventInfo : selectEventList) {
			if (!managarName.equals(eventInfo.getManagerName())) {
				//複数マネージャのイベントが選択されている場合
				MessageDialog.openInformation(
						null,
						Messages.getString("failed"),
						Messages.getString("message.monitor.event.customcommand.multimanager.ng"));
				return null;
			}
		}
		
		EventCustomCommandRunDialog dialog = new EventCustomCommandRunDialog(
				this.viewPart.getSite().getShell(), managarName, selectEventList, view.getEventDspSetting());
			
		dialog.open();
		
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
				if(part instanceof EventView){
					editEnable = true;
				}
				this.setBaseEnabled(editEnable);
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
