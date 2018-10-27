/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.dialog;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.vafada.swtcalendar.SWTCalendar;
import org.vafada.swtcalendar.SWTCalendarEvent;
import org.vafada.swtcalendar.SWTCalendarListener;

import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * カレンダーダイアログクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class CalendarDialog extends CommonDialog {
	private SWTCalendar swtcal = null;

	private Date dateBuffer;

	private Date dateBefore;

	private Date date;

	/**
	 * コンストラクタ
	 *
	 * @param parent
	 * @since 1.0.0
	 */
	public CalendarDialog(Shell parent) {
		super(parent);
	}

	/**
	 * ダイアログ作成
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#customizeDialog(org.eclipse.swt.widgets.Composite)
	 * @since 1.0.0
	 */
	@Override
	protected void customizeDialog(Composite composite) {
		composite.getShell().setText(Messages.getString("calendar"));

		Pattern.compile("[^\\w\\.]").matcher(this.getClass().getName()).replaceAll("-");

		GridLayout gridLayout = new GridLayout(1, true);
		composite.setLayout(gridLayout);
		gridLayout.marginWidth = 10;
		gridLayout.marginHeight = 10;

		swtcal = new SWTCalendar(composite);
		WidgetTestUtil.setTestId(this, null, swtcal);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		swtcal.setLayoutData(gridData);

		//カレンダー日付変更時リスナー
		swtcal.addSWTCalendarListener(new SWTCalendarListener() {
			@Override
			public void dateChanged(SWTCalendarEvent calendarEvent) {
				dateBuffer = calendarEvent.getCalendar().getTime();
			}
		});

		if (dateBefore != null) {
			Calendar calendar = Calendar.getInstance(TimezoneUtil.getTimeZone());
			calendar.setTime(dateBefore);
			swtcal.setCalendar(calendar);
		}
	}

	/**
	 * 日付設定
	 *
	 * @param date
	 * @since 1.0.0
	 */
	public void setDate(Date date) {
		dateBefore = date;
	}

	/**
	 * 日付取得
	 *
	 * @return
	 * @since 1.0.0
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * キャンセルボタンクリック時の処理
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 * @since 1.0.0
	 */
	@Override
	protected void cancelPressed() {
		date = dateBefore;

		super.cancelPressed();
	}

	/**
	 * OKボタンクリック時の処理
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 * @since 1.0.0
	 */
	@Override
	protected void okPressed() {
		if (dateBuffer != null) {
			date = dateBuffer;
		}
		super.okPressed();
	}

	@Override
	protected ValidateResult validate() {
		return null;
	}
}
