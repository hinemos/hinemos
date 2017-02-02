/*

Copyright (C) 2013 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
import com.clustercontrol.calendar.dialog.CalendarPatternDialog;
import com.clustercontrol.calendar.view.CalendarPatternView;

/**
 * カレンダ[カレンダパターン]の作成・変更ダイアログによる、
 * カレンダパターンのコピーを行うクライアント側アクションクラス<BR>
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class CalendarPatternCopyAction extends AbstractHandler implements IElementUpdater {
	private static Log m_log = LogFactory.getLog(CalendarPatternCopyAction.class);
	public static final String ID = CalendarPatternCopyAction.class.getName();

	private IWorkbenchWindow window;
	private IWorkbenchPart viewPart;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		this.viewPart = HandlerUtil.getActivePart(event);
		// カレンダ[カレンダパターン]より、選択されているカレンダパターンIDを取得

		CalendarPatternView calendarPatternView = null; 
		try { 
			calendarPatternView = (CalendarPatternView) this.viewPart.getAdapter(CalendarPatternView.class); 
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (calendarPatternView == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		String managerName = calendarPatternView.getSelectedManagerNameList().get(0);
		String id = calendarPatternView.getSelectedIdList().get(0);

		if (id != null) {
			// ダイアログを生成
			CalendarPatternDialog dialog = new CalendarPatternDialog(
					this.viewPart.getSite().getShell(), managerName, id,
					PropertyDefineConstant.MODE_COPY);

			// ダイアログにて変更が選択された場合、入力内容をもって更新を行う。
			if (dialog.open() == IDialogConstants.OK_ID) {
				calendarPatternView.update();
			}
		}
		return null;
	}

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
		this.window = null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();

				if( part instanceof CalendarPatternView  ){
					// Enable button when 1 item is selected
					this.setBaseEnabled( 1 == ((CalendarPatternView) part).getSelectedNum() );
				}else{
					this.setBaseEnabled( false );
				}
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
