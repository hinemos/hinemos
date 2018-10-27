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
import com.clustercontrol.calendar.dialog.CalendarPatternDialog;
import com.clustercontrol.calendar.view.CalendarPatternView;

/**
 * カレンダ[カレンダパターン]の作成・変更ダイアログによる、
 * カレンダ[カレンダパターン]変更を行うクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 4.1.0
 */
public class CalendarPatternModifyAction extends AbstractHandler implements IElementUpdater{
	private static Log m_log = LogFactory.getLog(CalendarPatternModifyAction.class);
	public static final String ID = CalendarPatternModifyAction.class.getName();

	private IWorkbenchWindow window;
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
		this.window = null;
	}

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
					PropertyDefineConstant.MODE_MODIFY);

			// ダイアログにて変更が選択された場合、入力内容をもって更新を行う。
			if (dialog.open() == IDialogConstants.OK_ID) {
				calendarPatternView.update();
			}
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
