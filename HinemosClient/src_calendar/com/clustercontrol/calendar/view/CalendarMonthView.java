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

package com.clustercontrol.calendar.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.clustercontrol.calendar.composite.CalendarMonthComposite;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 月間カレンダサマリビュークラス<BR>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class CalendarMonthView extends CommonViewPart {
	public static final String ID = CalendarMonthView.class.getName();
	/** CalendarMonthViewコンポジット */
	private CalendarMonthComposite m_calendarMonthComposite = null;

	private Composite m_parent = null;
	private String orgViewName = null;

	/**
	 * コンストラクタ
	 *
	 */
	public CalendarMonthView() {
		super();
	}

	protected String getViewName() {
		return this.getClass().getName();
	}

	/**
	 * ViewPartへのコントロール作成処理
	 *
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		m_parent = parent;
		m_calendarMonthComposite = new CalendarMonthComposite(m_parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, m_calendarMonthComposite);
		orgViewName = this.getPartName();
	}

	/**
	 * カレンダ曜日別一覧ビュー更新
	 */
	public void update(String managerName, String calendarId) {
		if(managerName == null || managerName.equals("")) {
			return;
		}
		m_calendarMonthComposite.init(managerName, calendarId);
		String viewName = orgViewName + "(" + managerName + ")";
		setPartName(viewName);
	}
}
