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

import com.clustercontrol.calendar.action.GetCalendarPatternTableDefine;
import com.clustercontrol.calendar.composite.CalendarPatternComposite;
import com.clustercontrol.calendar.view.CalendarPatternView;
/**
 * カレンダ[カレンダパターン]テーブルのSelectionChangedListenerクラス<BR>
 * 
 * @version 4.1.0
 * @since 4.1.0
 */
public class CalendarPatternSelectionChangedListener implements ISelectionChangedListener {
	/** ログ */
	private static Log m_log = LogFactory.getLog(CalendarPatternSelectionChangedListener.class);
	private CalendarPatternComposite m_composite;

	/**
	 * コンストラクタ
	 * 
	 * @param composite
	 * @since 4.1.0
	 */
	public CalendarPatternSelectionChangedListener(CalendarPatternComposite composite) {
		m_composite = composite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		String id = null;

		StructuredSelection selection = (StructuredSelection) event.getSelection();
		if (selection == null)
			throw new InternalError("selection is null.");

		//カレンダパターンIDを取得
		if ( selection.getFirstElement() != null) {
			ArrayList<?> info = (ArrayList<?>) selection.getFirstElement();
			id = (String) info.get(GetCalendarPatternTableDefine.CAL_PATTERN_ID);
			//カレンダパターンIDを設定
			m_composite.setCalendarPatternId(id);
		}

		//アクティブページを手に入れる
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();

		//カレンダ[カレンダパターン]ビューのボタン（アクション）の使用可/不可を設定する
		IViewPart viewPart = page.findView(CalendarPatternView.ID);
		if (viewPart != null) {
			CalendarPatternView view =
					(CalendarPatternView) viewPart.getAdapter(CalendarPatternView.class);
			if (view == null) {
				m_log.info("selection changed: view is null"); 
				return;
			}
			//ボタン（アクション）の使用可/不可を設定する
			view.setEnabledAction(selection.size(), selection);
		}
	}
}

