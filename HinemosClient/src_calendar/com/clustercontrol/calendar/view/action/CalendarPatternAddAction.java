/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
import com.clustercontrol.calendar.dialog.CalendarPatternDialog;
import com.clustercontrol.calendar.view.CalendarPatternView;
import com.clustercontrol.util.RestConnectManager;

/**
 * カレンダ[カレンダパターン]の作成・変更ダイアログによる、
 * カレンダパターン登録を行うクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 4.1.0
 */
public class CalendarPatternAddAction extends AbstractHandler {
	private static Log m_log = LogFactory.getLog(CalendarPatternAddAction.class);
	public static final String ID = CalendarPatternAddAction.class.getName();

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
		
		String managerName = RestConnectManager.getActiveManagerNameList().get(0);
		
		// ダイアログを生成
		CalendarPatternDialog dialog = new CalendarPatternDialog(this.viewPart.getSite().getShell(),
				managerName, null, PropertyDefineConstant.MODE_ADD);

		// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
		dialog.open();

		calendarPatternView.update();
		return null;
	}
}
