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

import java.util.ArrayList;
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

import com.clustercontrol.accesscontrol.util.ObjectBean;
import com.clustercontrol.calendar.util.CalendarEndpointWrapper;
import com.clustercontrol.calendar.view.CalendarPatternView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.ws.calendar.InvalidRole_Exception;

/**
 * カレンダ[カレンダパターン]の削除を行うクライアント側アクションクラス<BR>
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class CalendarPatternDeleteAction extends AbstractHandler implements IElementUpdater {
	private static Log m_log = LogFactory.getLog(CalendarPatternDeleteAction.class);
	public static final String ID = CalendarPatternDeleteAction.class.getName();

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

		List<ObjectBean> objList = calendarPatternView.getSelectedObjectBeans();

		String[] args = new String[1];
		String msg = null;

		if(objList == null || objList.isEmpty()) {
			return null;
		}

		Map<String, List<String>> map = new ConcurrentHashMap<String, List<String>>();
		int size = 0;
		String patternId = "";
		for(ObjectBean obj : objList) {
			String managerName = obj.getManagerName();
			if(map.get(managerName) == null) {
				map.put(managerName, new ArrayList<String>());
			}
			map.get(managerName).add(obj.getObjectId());
			patternId = obj.getObjectId();
			size++;
		}

		if (size > 0) {
			// 確認ダイアログにて変更が選択された場合、削除処理を行う。
			if(objList.size() == 1) {
				msg = "message.calendar.49";
				args[0] = patternId;
			} else {
				msg = "message.calendar.72";
				args[0] = Integer.toString(size);
			}

			if (MessageDialog.openConfirm(
					null,
					Messages.getString("confirmed"),
					Messages.getString(msg, args)) == false) {
				return null;
			}
		}

		Map<String, String> errMsg = new ConcurrentHashMap<String, String>();
		StringBuffer messageArg = new StringBuffer();
		int i = 0;
		for(Map.Entry<String, List<String>> entry : map.entrySet()) {
			String managerName = entry.getKey();
			List<String> idList = entry.getValue();

			if(i > 0) {
				messageArg.append(", ");
			}
			messageArg.append(managerName);

			try {
				CalendarEndpointWrapper wrapper = CalendarEndpointWrapper.getWrapper(managerName);
				wrapper.deleteCalendarPattern(idList);
			} catch (Exception e) {
				if (e instanceof InvalidRole_Exception) {
					errMsg.put(managerName, Messages.getString("message.accesscontrol.16"));
				} else {
					errMsg.put(managerName, HinemosMessage.replace(e.getMessage()));
				}
			}
			i++;
		}

		if(errMsg.isEmpty()) {
			Object[] arg = {messageArg.toString()};
			// 成功報告ダイアログを生成
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.calendar.38", arg));
		} else {
			// 失敗報告ダイアログを生成
			UIManager.showMessageBox(errMsg, true);
		}

		// ビューを更新
		calendarPatternView.update();
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
					this.setBaseEnabled( 0 < ((CalendarPatternView) part).getSelectedNum() );
				}else{
					this.setBaseEnabled( false );
				}
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
