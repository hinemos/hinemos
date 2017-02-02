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

package com.clustercontrol.calendar.composite.action;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.calendar.action.GetCalendarListTableDefine;
import com.clustercontrol.calendar.composite.CalendarListComposite;
import com.clustercontrol.calendar.view.CalendarListView;
import com.clustercontrol.calendar.view.CalendarMonthView;
/**
 * カレンダテーブルのSelectionChangedListenerクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class CalendarSelectionChangedListener implements ISelectionChangedListener {
	/** ログ */
	private static Log m_log = LogFactory.getLog(CalendarSelectionChangedListener.class);
	private CalendarListComposite m_composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite
	 * @since 1.0.0
	 */
	public CalendarSelectionChangedListener(CalendarListComposite composite) {
		m_composite = composite;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		String managerName = null;
		String calenadarId = null;

		StructuredSelection selection = (StructuredSelection) event.getSelection();
		if (selection == null)
			throw new InternalError("selection is null.");

		//カレンダIDを取得
		if ( selection.getFirstElement() != null) {
			ArrayList<?> info = (ArrayList<?>) selection.getFirstElement();
			managerName = (String) info.get(GetCalendarListTableDefine.MANAGER_NAME);
			calenadarId = (String) info.get(GetCalendarListTableDefine.CALENDAR_ID);
			//カレンダIDを設定
			m_composite.setCalendarId(calenadarId);
		}

		//アクティブページを手に入れる
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();

		//カレンダ[一覧]ビューのボタン（アクション）の使用可/不可を設定する
		IViewPart viewPart = page.findView(CalendarListView.ID);
		if (viewPart != null) {
			CalendarListView view =
					(CalendarListView) viewPart.getAdapter(CalendarListView.class);
			if (view == null) {
				m_log.info("selection changed: calendar list view is null"); 
				return;
			}
			//ボタン（アクション）の使用可/不可を設定する
			view.setEnabledAction(selection.size(), selection);
		}

		//月間カレンダビューを更新する
		viewPart = page.findView(CalendarMonthView.ID);
		if (viewPart != null) {
			CalendarMonthView view =
					(CalendarMonthView) viewPart.getAdapter(CalendarMonthView.class);
			if (view == null) {
				m_log.info("selection changed: calendar month view is null"); 
				return;
			}
			view.update(managerName, calenadarId);
		}
	}
}

