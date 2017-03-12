/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.calendar.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.calendar.dialog.CalendarDialog;
import com.clustercontrol.calendar.view.CalendarListView;
import com.clustercontrol.util.EndpointManager;

/**
 * カレンダの作成・変更ダイアログによる、カレンダ登録を行うクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 2.0.0
 */
public class CalendarAddAction extends AbstractHandler{
	private static Log m_log = LogFactory.getLog(CalendarAddAction.class);
	
	public static final String ID = CalendarAddAction.class.getName();

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
		CalendarListView calendarListView = null; 
		try { 
			calendarListView = (CalendarListView) this.viewPart.getAdapter(CalendarListView.class); 
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		String managerName = EndpointManager.getActiveManagerNameList().get(0);

		// ダイアログを生成
		CalendarDialog dialog = new CalendarDialog(this.viewPart.getSite()
				.getShell(), managerName, null, PropertyDefineConstant.MODE_ADD);
		// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
		dialog.open();

		if (calendarListView == null) {
			m_log.info("execute: view is null");
			return null;
		}

		calendarListView.update();

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

}
