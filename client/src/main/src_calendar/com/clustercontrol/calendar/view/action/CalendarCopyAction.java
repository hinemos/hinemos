/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.view.action;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.calendar.dialog.CalendarDialog;
import com.clustercontrol.calendar.view.CalendarListView;

/**
 * カレンダの作成・変更ダイアログによる、カレンダコピーを行うクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 2.0.0
 */
public class CalendarCopyAction extends AbstractHandler implements IElementUpdater{
	private static Log m_log = LogFactory.getLog(CalendarCopyAction.class);
	public static final String ID = CalendarCopyAction.class.getName();

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

		// カレンダ一覧より、選択されているカレンダIDを取得

		CalendarListView calendarListView = null; 
		try { 
			calendarListView = (CalendarListView) this.viewPart.getAdapter(CalendarListView.class); 
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (calendarListView == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		String managerName = calendarListView.getSelectedManagerNameList().get(0);
		String id = calendarListView.getSelectedIdList().get(0);

		if (id != null) {
			// ダイアログを生成
			CalendarDialog dialog = new CalendarDialog(this.viewPart.getSite()
					.getShell(), managerName, id,
					PropertyDefineConstant.MODE_COPY);

			// ダイアログにて変更が選択された場合、入力内容をもって更新を行う。
			if (dialog.open() == IDialogConstants.OK_ID) {
				calendarListView.update();
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

				if( part instanceof CalendarListView  ){
					// Enable button when 1 item is selected
					this.setBaseEnabled( 1 == ((CalendarListView) part).getSelectedNum() );
				}else{
					this.setBaseEnabled( false );
				}
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
