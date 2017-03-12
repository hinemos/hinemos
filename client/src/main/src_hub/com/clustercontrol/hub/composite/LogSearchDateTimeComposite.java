/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.hub.composite;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;

import com.clustercontrol.util.HinemosTime;

/**
 * 検索画面の時刻表示用コンポジット
 *
 */
public class LogSearchDateTimeComposite extends Composite{
	//ログ
	private static Logger logger  = Logger.getLogger(LogSearchDateTimeComposite.class);
	
	private DateTime dateTime_date;
	private DateTime dateTime_time;
	
	//初期値は現在時刻
	private Long epochMillis = HinemosTime.currentTimeMillis();
	
	public LogSearchDateTimeComposite(Composite parent, int style) {
		super(parent, style);
		initizlize();
	}
	
	public Long getEpochMillis() {
		return epochMillis;
	}
	
	public void setEpochMillis(Long epochMillis) {
		this.epochMillis = epochMillis;
	}
	
	private void initizlize(){
		logger.debug("initizlize() HinemosTime = " + epochMillis);
		
		setLayout(new GridLayout(2, false));

		dateTime_date = new DateTime(this, SWT.BORDER | SWT.DROP_DOWN);
		GridData gd_dateTimeFromDate = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_dateTimeFromDate.widthHint = 60;
		dateTime_date.setLayoutData(gd_dateTimeFromDate);
		dateTime_date.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});
		dateTime_time = new DateTime(this, SWT.BORDER | SWT.TIME);
		GridData gd_dateTimeFromTime = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_dateTimeFromTime.widthHint = 40;
		dateTime_time.setLayoutData(gd_dateTimeFromTime);
		dateTime_time.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});
		reflectDateTime();
		update();
	}
	
	@Override
	public void update(){
		createDateTime();
	}
	
	private void createDateTime(){
		GregorianCalendar calendar = new GregorianCalendar(
				this.dateTime_date.getYear(), 
				this.dateTime_date.getMonth(),
				this.dateTime_date.getDay(), 
				this.dateTime_time.getHours(), 
				this.dateTime_time.getMinutes(), 
				this.dateTime_time.getSeconds());
		this.epochMillis = calendar.getTimeInMillis();
	}
	
	private void reflectDateTime(){
		Long datelong = this.epochMillis;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(datelong);

		setPeriod(calendar);
	}
	
	public void shiftDate(int shift) {
		Long datelong = this.epochMillis;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(datelong);
		
		calendar.add(Calendar.DATE, shift);
		setPeriod(calendar);
	}
	
	public void setPeriod(Calendar calendar){
		// 日付が、31 になると、次の変更で、月が更新されないという不具合を確認。
		// 前月 -> 今日 という流れで、前月の末日が 31 だと現象が発生。
		// しかたがなので、別の日付で一度更新し、その後正しい日付を入力。
		this.dateTime_date.setYear(2000);
		this.dateTime_date.setMonth(0);
		this.dateTime_date.setDay(1);
		
		this.dateTime_date.setYear(calendar.get(Calendar.YEAR));
		this.dateTime_date.setMonth(calendar.get(Calendar.MONTH));
		this.dateTime_date.setDay(calendar.get(Calendar.DATE));
		this.dateTime_time.setHours(calendar.get(Calendar.HOUR_OF_DAY));
		this.dateTime_time.setMinutes(calendar.get(Calendar.MINUTE));
		this.dateTime_time.setSeconds(calendar.get(Calendar.SECOND));
		update();
	}
	
	@Override
	public void setEnabled (boolean enabled) {
		dateTime_date.setEnabled(enabled);
		dateTime_time.setEnabled(enabled);

		super.setEnabled(enabled);
	}
}