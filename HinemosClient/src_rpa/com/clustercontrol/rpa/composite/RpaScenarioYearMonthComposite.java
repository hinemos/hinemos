/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa.composite;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.vafada.swtcalendar.SWTMonthChooser;
import org.vafada.swtcalendar.Spinner;

import com.clustercontrol.util.TimezoneUtil;

public class RpaScenarioYearMonthComposite extends Composite {

	private boolean settingDate;

	private Spinner yearChooser;

	private SWTMonthChooser monthChooser;

public RpaScenarioYearMonthComposite(Composite parent, int style) {
		super(parent, style);

		Calendar calendar = Calendar.getInstance(TimezoneUtil.getTimeZone());
		
		{
			final GridLayout gridLayout = new GridLayout();
			gridLayout.marginHeight = 0;
			gridLayout.marginWidth = 0;
			gridLayout.horizontalSpacing = 2;
			gridLayout.verticalSpacing = 2;
			setLayout(gridLayout);
		}

		final Composite header = new Composite(this, SWT.NONE);

		{
			{
				final GridData gridData = new GridData();
				header.setLayoutData(gridData);
				final GridLayout gridLayout = new GridLayout();
				gridLayout.numColumns = 3;
				gridLayout.marginWidth = 0;
				gridLayout.marginHeight = 0;
				header.setLayout(gridLayout);
			}

			final Composite composite = new Composite(header, SWT.NONE);
			composite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL 
					| GridData.HORIZONTAL_ALIGN_CENTER));
			{
				final GridLayout gridLayout = new GridLayout();
				gridLayout.numColumns = 2;
				gridLayout.marginWidth = 0;
				gridLayout.marginHeight = 0;
				composite.setLayout(gridLayout);
			}
			header.setTabList(new Control[] { composite });

			yearChooser = new Spinner(composite, SWT.BORDER);
			yearChooser
			.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL));
			yearChooser.setMinimum(1);
			yearChooser.setMaximum(9999);
			yearChooser.setValue(calendar.get(Calendar.YEAR));
			
			monthChooser = new SWTMonthChooser(composite);
			monthChooser.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		}
		setFont(parent.getFont());
	}

	public RpaScenarioYearMonthComposite(Composite parent) {
		this(parent, SWT.FLAT);
	}

	public void setCalendar(Calendar cal) {
		settingDate = true;
		try {
			refreshYearMonth(cal);
		} finally {
			settingDate = false;
		}
	}

	private void refreshYearMonth(Calendar cal) {
		yearChooser.setValue(cal.get(Calendar.YEAR));
		monthChooser.setMonth(cal.get(Calendar.MONTH));
	}

	public Date getValue() {
		// MonthChooserが1月前の値を取得してしまう為、値に+1する
		String strDate = yearChooser.getValue() + "/" + (monthChooser.getMonth() + 1) + "/01";
		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd");
		Date date;
		try {
			date = sdFormat.parse(strDate);
		} catch (ParseException e) {
			date = new Date();
		}
		return date;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.widgets.Control#setFont(org.eclipse.swt.graphics.Font)
	 */
	@Override
	public void setFont(Font font) {
		super.setFont(font);
		monthChooser.setFont(font);
		yearChooser.setFont(font);
	}

	public boolean isSettingDate() {
		return settingDate;
	}
}
