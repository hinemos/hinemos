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

package com.clustercontrol.calendar.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.clustercontrol.calendar.composite.CalendarWeekComposite;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * カレンダ[週間予定]ビュークラス<BR>
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class CalendarWeekView extends CommonViewPart {
	public static final String ID = CalendarWeekView.class.getName();
	/** CalendarWeekViewコンポジット*/
	private CalendarWeekComposite calWeekComposite = null;
	private Composite calWeekParentComposite = null;
	private String orgViewName = null;

	/**
	 * コンストラクタ
	 *
	 */
	public CalendarWeekView() {
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
		calWeekParentComposite = parent;
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		calWeekComposite = new CalendarWeekComposite(calWeekParentComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, calWeekComposite);
		orgViewName = this.getPartName();
	}

	/**
	 * カレンダ[週間予定]ビュー更新
	 */
	public void update(String managerName, String calendarId, int year, int month, int day) {
		if(managerName == null || managerName.equals("")) {
			return;
		}
		calWeekComposite.update(managerName, calendarId, year, month, day);
		String viewName = orgViewName + "(" + managerName + ")";
		setPartName(viewName);
	}

}
