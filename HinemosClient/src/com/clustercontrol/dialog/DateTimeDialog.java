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

package com.clustercontrol.dialog;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.vafada.swtcalendar.SWTCalendar;

import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 日時ダイアログクラス<BR>
 *
 * @version 2.0.0
 * @since 1.0.0
 */
public class DateTimeDialog extends CommonDialog {
	private SWTCalendar swtcal = null;

	private Date dateBefore;

	private Date date;

	private Combo comboHours = null;

	private Combo comboMinutes = null;

	private Combo comboSecond = null;

	/**
	 * コンストラクタ
	 *
	 * @param parent
	 * @since 1.0.0
	 */
	public DateTimeDialog(Shell parent) {
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
		composite.getShell().setText(Messages.getString("time"));

		Pattern.compile("[^\\w\\.]").matcher(this.getClass().getName()).replaceAll("-");

		GridLayout gridLayout = new GridLayout(2, false);
		composite.setLayout(gridLayout);
		gridLayout.marginWidth = 10;
		gridLayout.marginHeight = 10;

		GridData gridData;

		swtcal = new SWTCalendar(composite);
		WidgetTestUtil.setTestId(this, null, swtcal);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		swtcal.setLayoutData(gridData);

		Composite timeComposite = new Composite(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "time", timeComposite);

		gridLayout = new GridLayout(5, false);
		timeComposite.setLayout(gridLayout);
		gridLayout.marginWidth = 10;
		gridLayout.marginHeight = 10;

		Label labelTime = new Label(timeComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "time", labelTime);

		labelTime.setText(Messages.getString("timestamp"));
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 5;
		labelTime.setLayoutData(gridData);

		comboHours = new Combo(timeComposite, SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "hours", comboHours);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		comboHours.setLayoutData(gridData);
		comboHours.setTextLimit(2);
		comboHours.setVisibleItemCount(10);
		DecimalFormat format = new DecimalFormat("00");
		for (int hour = 0; hour < 24; hour++) {
			comboHours.add(format.format(hour));
		}

		Label labelHours = new Label(timeComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "hours", labelTime);
		labelHours.setText(Messages.getString(":"));

		comboMinutes = new Combo(timeComposite, SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "minutes", comboMinutes);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		comboMinutes.setLayoutData(gridData);
		comboMinutes.setTextLimit(2);
		comboMinutes.setVisibleItemCount(10);
		for (int minutes = 0; minutes < 60; minutes++) {
			comboMinutes.add(format.format(minutes));
		}

		Label labelMinutes = new Label(timeComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "minutes", labelTime);
		labelMinutes.setText(Messages.getString(":"));

		comboSecond = new Combo(timeComposite, SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "second", comboSecond);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		comboSecond.setLayoutData(gridData);
		comboSecond.setTextLimit(2);
		comboSecond.setVisibleItemCount(10);
		for (int second = 0; second < 60; second++) {
			comboSecond.add(format.format(second));
		}

		if (dateBefore != null) {
			Calendar calendar = Calendar.getInstance(TimezoneUtil.getTimeZone());
			calendar.setTime(dateBefore);
			swtcal.setCalendar(calendar);

			//時を設定
			comboHours.setText(format.format(calendar
					.get(Calendar.HOUR_OF_DAY)));

			//分を設定
			comboMinutes
			.setText(format.format(calendar.get(Calendar.MINUTE)));

			//秒を設定
			comboSecond.setText(format.format(calendar.get(Calendar.SECOND)));
		} else {
			Calendar calendar = Calendar.getInstance(TimezoneUtil.getTimeZone());

			//時を設定
			comboHours.setText(format.format(calendar
					.get(Calendar.HOUR_OF_DAY)));

			//分を設定
			comboMinutes
			.setText(format.format(calendar.get(Calendar.MINUTE)));

			//秒を設定
			comboSecond.setText(format.format(calendar.get(Calendar.SECOND)));
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
		date = swtcal.getCalendar().getTime();

		//時を取得
		Integer hours = Integer.parseInt(comboHours.getText());

		//分を取得
		Integer minutes = Integer.parseInt(comboMinutes.getText());

		//秒を取得
		Integer second = Integer.valueOf(comboSecond.getText());

		Calendar work = Calendar.getInstance(TimezoneUtil.getTimeZone());
		work.setTime(date);
		work.set(Calendar.HOUR_OF_DAY, hours.intValue());
		work.set(Calendar.MINUTE, minutes.intValue());
		work.set(Calendar.SECOND, second.intValue());
		work.set(Calendar.MILLISECOND, 0);
		date = work.getTime();

		super.okPressed();
	}

	@Override
	protected ValidateResult validate() {
		return null;
	}
}
