/*
 * SWTCalendar.java - A calendar component for SWT Author: Mark Bryan Yu
 * Modified by: Sergey Prigogin swtcalendar.sourceforge.net
 * Modified by: NTT DATA INTELLILINK Corporation <http://www.hinemos.info/>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.vafada.swtcalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.calendar.Ymd;

public class SWTCalendar extends Composite {
	/**
	 * Style constant for making Sundays red.
	 */
	public static final int RED_SUNDAY = SWTDayChooser.RED_SUNDAY;

	/**
	 * Style constant for making weekends red.
	 */
	public static final int RED_WEEKEND = SWTDayChooser.RED_WEEKEND;

	private boolean settingDate;

	private Spinner yearChooser;

	private SWTMonthChooser monthChooser;

	private SWTDayChooser dayChooser;

	private boolean settingYearMonth;

	/**
	 * Constructs a calendar control.
	 *
	 * @param parent
	 *            a parent container.
	 * @param style
	 *            FLAT to make the buttons flat, or NONE.
	 */
	public SWTCalendar(Composite parent, int style) {
		super(parent, (style & ~(SWT.FLAT | RED_WEEKEND)));

		Pattern.compile("[^\\w\\.]").matcher(this.getClass().getName()).replaceAll("-");

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
		WidgetTestUtil.setTestId(this, "header", header);

		{
			{
				final GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
				header.setLayoutData(gridData);
				final GridLayout gridLayout = new GridLayout();
				gridLayout.numColumns = 3;
				gridLayout.marginWidth = 0;
				gridLayout.marginHeight = 0;
				header.setLayout(gridLayout);
			}

			final Button prevMonthButton = new Button(header, SWT.ARROW
					| SWT.LEFT | SWT.CENTER | (style & SWT.FLAT));
			WidgetTestUtil.setTestId(this, "prevMonth", prevMonthButton);
			prevMonthButton.setLayoutData(new GridData(
					GridData.VERTICAL_ALIGN_FILL));
			prevMonthButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					previousMonth();
				}
			});

			final Composite composite = new Composite(header, SWT.NONE);
			WidgetTestUtil.setTestId(this, null, composite);
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

			monthChooser = new SWTMonthChooser(composite);
			WidgetTestUtil.setTestId(this, null, monthChooser);
			monthChooser.setLayoutData(new GridData(GridData.FILL_VERTICAL));
			monthChooser.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!settingYearMonth) {
						dayChooser.setMonth(monthChooser.getMonth());
					}
				}
			});

			yearChooser = new Spinner(composite, SWT.BORDER);
			WidgetTestUtil.setTestId(this, null, yearChooser);
			yearChooser
			.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL));
			yearChooser.setMinimum(1);
			yearChooser.setMaximum(9999);
			yearChooser.setValue(calendar.get(Calendar.YEAR));
			yearChooser.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					if (!settingYearMonth) {
						dayChooser.setYear(yearChooser.getValue());
					}
				}
			});

			final Button nextMonthButton = new Button(header, SWT.ARROW
					| SWT.RIGHT | SWT.CENTER | (style & SWT.FLAT));
			WidgetTestUtil.setTestId(this, "nextButton", nextMonthButton);
			nextMonthButton.setLayoutData(new GridData(
					GridData.VERTICAL_ALIGN_FILL));
			nextMonthButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					nextMonth();
				}
			});
		}

		{
			dayChooser = new SWTDayChooser(this, SWT.BORDER
					| (style & RED_WEEKEND));
			WidgetTestUtil.setTestId(this, null, dayChooser);
			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 3;
			dayChooser.setLayoutData(gridData);
			dayChooser.addSWTCalendarListener(new SWTCalendarListener() {
				@Override
				public void dateChanged(SWTCalendarEvent event) {
					refreshYearMonth(event.getCalendar());
				}
			});
		}

		setTabList(new Control[] { header, dayChooser });

		setFont(parent.getFont());
	}

	/**
	 * 複数選択用SWTカレンダ更新
	 * @since 4.1.0
	 * @see com.clustercontrol.calendar.dialog.CalendarPatternDialog#customizeDialog
	 * @param dateList
	 */
	public void updateCalendar(ArrayList<Ymd> dateList){
		dayChooser.update(dateList);
	}


	public SWTCalendar(Composite parent) {
		this(parent, SWT.FLAT);
	}

	public void setCalendar(Calendar cal) {
		settingDate = true;
		try {
			refreshYearMonth(cal);
			dayChooser.setCalendar(cal);
		} finally {
			settingDate = false;
		}
	}

	private void refreshYearMonth(Calendar cal) {
		settingYearMonth = true;
		yearChooser.setValue(cal.get(Calendar.YEAR));
		monthChooser.setMonth(cal.get(Calendar.MONTH));
		settingYearMonth = false;
	}

	public void nextMonth() {
		Calendar cal = dayChooser.getCalendar();
		cal.add(Calendar.MONTH, 1);
		refreshYearMonth(cal);
		dayChooser.setCalendar(cal);
	}

	public void previousMonth() {
		Calendar cal = dayChooser.getCalendar();
		cal.add(Calendar.MONTH, -1);
		refreshYearMonth(cal);
		dayChooser.setCalendar(cal);
	}

	public Calendar getCalendar() {
		return dayChooser.getCalendar();
	}

	public void addSWTCalendarListener(SWTCalendarListener listener) {
		dayChooser.addSWTCalendarListener(listener);
	}

	public void removeSWTCalendarListener(SWTCalendarListener listener) {
		dayChooser.removeSWTCalendarListener(listener);
	}

	public void setLocale(Locale locale) {
		monthChooser.setLocale(locale);
		dayChooser.setLocale(locale);
		yearChooser.setValue(getCalendar().get(Calendar.YEAR));
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
		dayChooser.setFont(font);
	}

	public boolean isSettingDate() {
		return settingDate;
	}
}
