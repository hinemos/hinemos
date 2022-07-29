/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.view.action;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.monitor.action.GetEventListTableDefine;
import com.clustercontrol.monitor.composite.EventListComposite;
import com.clustercontrol.monitor.dialog.EventInfoDialog;
import com.clustercontrol.monitor.view.EventView;

/**
 * 監視[イベントの詳細]ダイアログの表示を行うクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 2.1.0
 */
public class EventDetailAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(EventDetailAction.class);

	/** アクションID */
	public static final String ID = EventDetailAction.class.getName();

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
	 * 監視[イベント]ビューの選択されたアイテムのイベント情報を取得し、
	 * 監視[イベントの詳細]ダイアログに表示します。
	 * <p>
	 * <ol>
	 * <li>監視[イベント]ビューで、選択されているアイテムを取得します。</li>
	 * <li>取得したイベント情報の監視[イベントの詳細]ダイアログを表示します。</li>
	 * </ol>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.monitor.view.EventView
	 * @see com.clustercontrol.monitor.dialog.EventInfoDialog
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

		EventListComposite composite =
				(EventListComposite)view.getListComposite();

		StructuredSelection selection =
				(StructuredSelection)composite.getTableViewer().getSelection();

		List<?> list = (List<?>)selection.getFirstElement();

		if(list != null){
			EventInfoDialog dialog = new EventInfoDialog(this.viewPart.getSite().getShell(), list, view.getEventDspSetting());
			if (dialog.open() == IDialogConstants.OK_ID){
				String managerName = (String) list.get(GetEventListTableDefine.MANAGER_NAME);
				dialog.okButtonPress(managerName);
				view.update(false);
			}
		}
		return null;
	}
}
