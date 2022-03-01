/*
 * SWTDayChooser.java - A day chooser component for SWT Author: Mark Bryan Yu
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

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.eclipse.draw2d.ColorConstantsWrapper;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.openapitools.client.model.YMDResponse;

import com.clustercontrol.client.swt.SWT;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.WidgetTestUtil;

public class SWTDayChooser extends Composite implements MouseListener,
FocusListener, TraverseListener, KeyListener {
	/**
	 * Style constant for making Sundays red.
	 */
	public static final int RED_SUNDAY = 1 << 24; // == SWT.EMBEDDED

	/**
	 * Style constant for making Saturdays red.
	 */
	public static final int RED_SATURDAY = 1 << 28; // == SWT.VIRTUAL

	/**
	 * Style constant for making weekends red.
	 */
	public static final int RED_WEEKEND = RED_SATURDAY | RED_SUNDAY;

	private Label[] dayTitles;

	private DayControl[] days;

	private int dayOffset;

	private Color activeSelectionBackground;

	private Color inactiveSelectionBackground;

	private Color activeSelectionForeground;

	private Color inactiveSelectionForeground;

	private Color otherMonthColor;

	private Calendar calendar;

	private Calendar today;

	private Locale locale;

	private List<SWTCalendarListener> listeners;

	private int style;

	private ArrayList<YMDResponse> dateList;// = new ArrayList<Ymd>();

	public SWTDayChooser(Composite parent, int style) {
		super(parent, style & ~RED_WEEKEND);
		this.style = style;
		listeners = new ArrayList<SWTCalendarListener>(3);

		Pattern.compile("[^\\w\\.]").matcher(this.getClass().getName()).replaceAll("-");

		setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

		otherMonthColor = new Color(getDisplay(), 128, 128, 128);
		activeSelectionBackground = getDisplay().getSystemColor(
				SWT.COLOR_LIST_SELECTION);
		inactiveSelectionBackground = getDisplay().getSystemColor(
				SWT.COLOR_GRAY);
		activeSelectionForeground = getDisplay().getSystemColor(
				SWT.COLOR_LIST_SELECTION_TEXT);
		inactiveSelectionForeground = getForeground();

		locale = Locale.getDefault();

		GridLayout gridLayout = new GridLayout();
		gridLayout.makeColumnsEqualWidth = true;
		gridLayout.numColumns = 7;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		setLayout(gridLayout);

		dayTitles = new Label[7];
		for (int i = 0; i < dayTitles.length; i++) {
			Label label = new Label(this, SWT.CENTER);
			WidgetTestUtil.setTestId(this, "daytitles" + i, label);
			dayTitles[i] = label;
			label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
			label.addMouseListener(this);
			WidgetTestUtil.setTestId(this, "label" + i, label);
		}
		{
			final Composite spacer = new Composite(this, SWT.NO_FOCUS);
			WidgetTestUtil.setTestId(this, "spacer", spacer);
			spacer.setBackground(getBackground());
			final GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.heightHint = 2;
			gridData.horizontalSpan = 7;
			spacer.setLayoutData(gridData);
			spacer.setLayout(new GridLayout());
			spacer.addMouseListener(this);
		}

		{
			final Label label = new Label(this, SWT.HORIZONTAL | SWT.SEPARATOR);
			WidgetTestUtil.setTestId(this, null, label);
			final GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 7;
			label.setLayoutData(gridData);
		}

		days = new DayControl[42];
		for (int i = 0; i < days.length; i++) {
			DayControl day = new DayControl(this, i);
			WidgetTestUtil.setTestId(this, "day" + i, day);
			days[i] = day;
			day.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
					| GridData.VERTICAL_ALIGN_FILL));
			day.addMouseListener(this);
		}

		setTabList(new Control[0]);

		setFont(parent.getFont());

		init();

		addMouseListener(this);
		addFocusListener(this);
		addTraverseListener(this);
		addKeyListener(this);

		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent event) {
				otherMonthColor.dispose();
			}
		});
	}

	protected void init() {
		calendar = Calendar.getInstance(TimezoneUtil.getTimeZone(), locale);
		calendar.setLenient(true);
		today = (Calendar) calendar.clone();
		int firstDayOfWeek = calendar.getFirstDayOfWeek();
		DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(locale);
		String[] dayNames = dateFormatSymbols.getShortWeekdays();
		int minLength = Integer.MAX_VALUE;
		for (int i = 0; i < dayNames.length; i++) {
			int len = dayNames[i].length();
			if (len > 0 && len < minLength) {
				minLength = len;
			}
		}
		if (minLength > 2) {
			for (int i = 0; i < dayNames.length; i++) {
				if (dayNames[i].length() > 0) {
					dayNames[i] = dayNames[i].substring(0, 1);
				}
			}
		}

		int d = firstDayOfWeek;
		for (int i = 0; i < dayTitles.length; i++) {
			Label label = dayTitles[i];
			label.setText(dayNames[d]);
			label.setBackground(getBackground());
			if (d == Calendar.SUNDAY && (style & RED_SUNDAY) != 0
					|| d == Calendar.SATURDAY && (style & RED_SATURDAY) != 0) {
				label.setForeground(getDisplay().getSystemColor(
						SWT.COLOR_DARK_RED));
			} else {
				label.setForeground(getForeground());
			}

			d++;
			if (d > dayTitles.length) {
				d -= dayTitles.length;
			}
		}

		drawDays();
	}

	protected void drawDays() {
		calendar.get(Calendar.DAY_OF_YEAR); // Force calendar update
		Calendar cal = (Calendar) calendar.clone();
		int firstDayOfWeek = cal.getFirstDayOfWeek();
		cal.set(Calendar.DAY_OF_MONTH, 1);

		dayOffset = firstDayOfWeek - cal.get(Calendar.DAY_OF_WEEK);
		if (dayOffset >= 0) {
			dayOffset -= 7;
		}
		cal.add(Calendar.DAY_OF_MONTH, dayOffset);

		Color foregroundColor = getForeground();
		for (int i = 0; i < days.length; cal.add(Calendar.DAY_OF_MONTH, 1)) {
			DayControl dayControl = days[i++];
			dayControl
			.setText(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
			if (isSameDay(cal, today)) {
				dayControl.setBorderColor(getDisplay().getSystemColor(
						SWT.COLOR_BLACK));
			} else {
				dayControl.setBorderColor(getBackground());
			}

			if (isSameMonth(cal, calendar)) {
				int d = cal.get(Calendar.DAY_OF_WEEK);
				if (d == Calendar.SUNDAY && (style & RED_SUNDAY) != 0
						|| d == Calendar.SATURDAY
						&& (style & RED_SATURDAY) != 0) {
					dayControl.setForeground(getDisplay().getSystemColor(
							SWT.COLOR_DARK_RED));
				} else {
					dayControl.setForeground(foregroundColor);
				}
			} else {
				dayControl.setForeground(otherMonthColor);
			}

			if (isSameDay(cal, calendar)) {
				dayControl.setBackground(getSelectionBackgroundColor());
				dayControl.setForeground(getSelectionForegroundColor());
			} else {
				dayControl.setBackground(getBackground());
			}
		}
	}

	/**
	 * 描画処理
	 * 複数選択用
	 * dateListに格納されている日時と一致したら、色変更<BR>
	 * @param dateList
	 * @since 4.1.0
	 */
	protected void drawMultipleDays() {
		calendar.get(Calendar.DAY_OF_YEAR); // Force calendar update
		Calendar cal = (Calendar) calendar.clone();
		int firstDayOfWeek = cal.getFirstDayOfWeek();
		cal.set(Calendar.DAY_OF_MONTH, 1);

		dayOffset = firstDayOfWeek - cal.get(Calendar.DAY_OF_WEEK);
		if (dayOffset >= 0) {
			dayOffset -= 7;
		}
		cal.add(Calendar.DAY_OF_MONTH, dayOffset);

		Color foregroundColor = getForeground();
		for (int i = 0; i < days.length; cal.add(Calendar.DAY_OF_MONTH, 1)) {
			DayControl dayControl = days[i++];
			dayControl
			.setText(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
			if (isSameDay(cal, today)) {
				dayControl.setBorderColor(getDisplay().getSystemColor(
						SWT.COLOR_BLACK));
			} else {
				dayControl.setBorderColor(getBackground());
			}

			if (isSameMonth(cal, calendar)) {
				int d = cal.get(Calendar.DAY_OF_WEEK);
				if (d == Calendar.SUNDAY && (style & RED_SUNDAY) != 0
						|| d == Calendar.SATURDAY
						&& (style & RED_SATURDAY) != 0) {
					dayControl.setForeground(getDisplay().getSystemColor(
							SWT.COLOR_DARK_RED));
				} else {
					dayControl.setForeground(foregroundColor);
				}
			} else {
				dayControl.setForeground(otherMonthColor);
			}
			dayControl.setBackground(getBackground());
			//すでに、選択済みの日付の色を変更する
			if(dateList == null){
				continue;
			}
			for(YMDResponse ymd : dateList){
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M");
				//月までの表示データでもマネージャのタイムゾーン反映させる
				sdf.setTimeZone(TimezoneUtil.getTimeZone());
				//「現在表示されている年月」＋「選択された日にち」を「yyyy/M/d」の形式に変換
				String selectYMD = sdf.format(calendar.getTime()) + "/"+ dayControl.getText();
				String ymdYMD = yyyyMMdd(ymd);
				/*
				 * 選択された日付か否か
				 * 前後の月の日付と現在月の日付を区別するため
				 * ラベルの色により判定する
				 */
				if(selectYMD.equals(ymdYMD)
						&& !(dayControl.label.getForeground().equals(otherMonthColor))){
					dayControl.setBackground(ColorConstantsWrapper.lightBlue());
					dayControl.setForeground(getSelectionForegroundColor());
					break;
				}
			}
		}
	}
	/**
	 * Ymd(
	 * Integer year
	 * Integer month
	 * Integer day
	 * )をyyyy/M/dのString型へ変換する
	 * @param ymd
	 * @return yyyy/M/d
	 */
	private String yyyyMMdd(YMDResponse ymd){
		return ymd.getYearNo() + "/" + ymd.getMonthNo() + "/" + ymd.getDayNo();
	}
	/**
	 * 複数選択SWTカレンダを更新
	 * @param dateList
	 * @since 4.1.0
	 */
	protected void update(ArrayList<YMDResponse> dateList){
		this.dateList = dateList;
		drawMultipleDays();
	}
	protected static boolean isSameDay(Calendar cal1, Calendar cal2) {
		return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
				&& cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
	}

	private static boolean isSameMonth(Calendar cal1, Calendar cal2) {
		return cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
				&& cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
	}

	public void setMonth(int month) {
		int differenceMonth = month - calendar.get(Calendar.MONTH);
		calendar.add(Calendar.MONTH, differenceMonth);
		//if(dateList.isEmpty()){
		if(dateList == null){
			drawDays();
			dateChanged();
		}else {
			drawMultipleDays();
		}
	}

	public void setYear(int year) {
		int differenceYear = year - calendar.get(Calendar.YEAR);
		calendar.add(Calendar.YEAR, differenceYear);
		if(dateList == null){
			drawDays();
			dateChanged();
		}else {
			drawMultipleDays();
		}
	}

	public void setCalendar(Calendar cal) {
		calendar = (Calendar) cal.clone();
		calendar.setLenient(true);
		if(dateList == null){
			drawDays();
			dateChanged();
		}else {
			drawMultipleDays();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public void mouseDown(MouseEvent event) {
		if (event.button == 1) { // Left click
			setFocus();

			if (event.widget instanceof DayControl) {
				int index = findDay(event.widget);
				selectDay(index + 1 + dayOffset);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public void mouseDoubleClick(MouseEvent event) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public void mouseUp(MouseEvent event) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
	 */
	@Override
	public void focusGained(FocusEvent event) {
		DayControl selectedDay = getSelectedDayControl();
		WidgetTestUtil.setTestId(this, "selected", selectedDay);
		selectedDay.setBackground(getSelectionBackgroundColor());
		selectedDay.setForeground(getSelectionForegroundColor());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
	 */
	@Override
	public void focusLost(FocusEvent event) {
		DayControl selectedDay = getSelectedDayControl();
		WidgetTestUtil.setTestId(this, "selected", selectedDay);
		selectedDay.setBackground(getSelectionBackgroundColor());
		selectedDay.setForeground(getSelectionForegroundColor());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.events.TraverseListener#keyTraversed(org.eclipse.swt.events.TraverseEvent)
	 */
	@Override
	public void keyTraversed(TraverseEvent event) {
		switch (event.detail) {
		case SWT.TRAVERSE_ARROW_PREVIOUS:
		case SWT.TRAVERSE_ARROW_NEXT:
		case SWT.TRAVERSE_PAGE_PREVIOUS:
		case SWT.TRAVERSE_PAGE_NEXT:
			event.doit = false;
			break;
		case SWT.TRAVERSE_TAB_NEXT:
		case SWT.TRAVERSE_TAB_PREVIOUS:
			event.doit = true;
			break;
		default:
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
	 */
	@Override
	public void keyPressed(KeyEvent event) {
		switch (event.keyCode) {
		case SWT.ARROW_LEFT:
			selectDay(calendar.get(Calendar.DAY_OF_MONTH) - 1);
			break;

		case SWT.ARROW_RIGHT:
			selectDay(calendar.get(Calendar.DAY_OF_MONTH) + 1);
			break;

		case SWT.ARROW_UP:
			selectDay(calendar.get(Calendar.DAY_OF_MONTH) - 7);
			break;

		case SWT.ARROW_DOWN:
			selectDay(calendar.get(Calendar.DAY_OF_MONTH) + 7);
			break;

		case SWT.PAGE_UP:
			setMonth(calendar.get(Calendar.MONTH) - 1);
			break;

		case SWT.PAGE_DOWN:
			setMonth(calendar.get(Calendar.MONTH) + 1);
			break;
		default:
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
	 */
	@Override
	public void keyReleased(KeyEvent event) {
	}

	/**
	 * Finds position of a control in <code>days</code> array.
	 *
	 * @param dayControl
	 *            a control to find.
	 * @return an index of <code>dayControl</code> in <code>days</code>
	 *         array, or -1 if not found.
	 */
	private int findDay(Widget dayControl) {
		for (int i = 0; i < days.length; i++) {
			if (days[i] == dayControl) {
				return i;
			}
		}

		return -1;
	}

	private void selectDay(int day) {
		calendar.get(Calendar.DAY_OF_YEAR); // Force calendar update
		if (day >= calendar.getActualMinimum(Calendar.DAY_OF_MONTH)
				&& day <= calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
			int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
			// Stay on the same month.
			DayControl selectedDay = getSelectedDayControl();
			WidgetTestUtil.setTestId(this, "selected", selectedDay);
			selectedDay.setBackground(getBackground());
			if (dayOfWeek == Calendar.SUNDAY) {
				selectedDay.setForeground(getDisplay().getSystemColor(
						SWT.COLOR_DARK_RED));
			} else {
				selectedDay.setForeground(getForeground());
			}

			calendar.set(Calendar.DAY_OF_MONTH, day);

			selectedDay = getSelectedDayControl();
			selectedDay.setBackground(getSelectionBackgroundColor());
			selectedDay.setForeground(getSelectionForegroundColor());

		} else {
			// Move to a different month.
			calendar.set(Calendar.DAY_OF_MONTH, day);
			drawDays();
		}

		dateChanged();
	}

	private DayControl getSelectedDayControl() {
		return days[calendar.get(Calendar.DAY_OF_MONTH) - 1 - dayOffset];
	}

	private Color getSelectionBackgroundColor() {
		return isFocusControl() ? activeSelectionBackground
				: inactiveSelectionBackground;
	}

	private Color getSelectionForegroundColor() {
		return isFocusControl() ? activeSelectionForeground
				: inactiveSelectionForeground;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.widgets.Control#isFocusControl()
	 */
	@Override
	public boolean isFocusControl() {
		for (Control control = getDisplay().getFocusControl(); control != null; control = control
				.getParent()) {
			if (control == this) {
				return true;
			}
		}

		return false;
	}

	public void addSWTCalendarListener(SWTCalendarListener listener) {
		this.listeners.add(listener);
	}

	public void removeSWTCalendarListener(SWTCalendarListener listener) {
		this.listeners.remove(listener);
	}

	private void dateChanged() {
		if (!listeners.isEmpty()) {
			SWTCalendarListener[] listenersArray = new SWTCalendarListener[listeners.size()];
			listeners.toArray(listenersArray);
			for (int i = 0; i < listenersArray.length; i++) {
				Event event = new Event();
				event.widget = this;
				event.display = getDisplay();
				event.time = (int) System.currentTimeMillis();
				event.data = calendar.clone();
				listenersArray[i].dateChanged(new SWTCalendarEvent(event));
			}
		}
	}

	public Calendar getCalendar() {
		return (Calendar) calendar.clone();
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
		init();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.widgets.Control#setFont(org.eclipse.swt.graphics.Font)
	 */
	@Override
	public void setFont(Font font) {
		super.setFont(font);

		for (int i = 0; i < dayTitles.length; i++) {
			dayTitles[i].setFont(font);
		}

		for (int i = 0; i < days.length; i++) {
			days[i].setFont(font);
		}
	}

	static private class DayControl extends Composite implements Listener {
		private Composite filler;

		private Label label;

		public DayControl(Composite parent, int suffix) {
			super(parent, SWT.NO_FOCUS);
			{
				final GridLayout gridLayout = new GridLayout();
				gridLayout.marginWidth = 1;
				gridLayout.marginHeight = 1;
				setLayout(gridLayout);
			}

			Pattern.compile("[^\\w\\.]").matcher(this.getClass().getName()).replaceAll("-");

			filler = new Composite(this, SWT.NO_FOCUS);
			WidgetTestUtil.setTestId(this, "filler", filler);
			filler.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				final GridLayout gridLayout = new GridLayout();
				gridLayout.marginWidth = 2;
				gridLayout.marginHeight = 0;
				filler.setLayout(gridLayout);
			}
			filler.addListener(SWT.MouseDown, this);
			filler.addListener(SWT.MouseUp, this);
			filler.addListener(SWT.MouseDoubleClick, this);

			label = new DayLabel(filler, SWT.RIGHT | SWT.NO_FOCUS);
			WidgetTestUtil.setTestId(this, null, label);
			label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
					| GridData.HORIZONTAL_ALIGN_CENTER));
			label.addListener(SWT.MouseDown, this);
			label.addListener(SWT.MouseUp, this);
			label.addListener(SWT.MouseDoubleClick, this);

			setBorderColor(parent.getBackground());
			setBackground(parent.getBackground());
			setFont(parent.getFont());
		}

		public void setText(String text) {
			label.setText(text);
		}

		public String getText() {
			return label.getText();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.swt.widgets.Control#setFont(org.eclipse.swt.graphics.Font)
		 */
		@Override
		public void setFont(Font font) {
			super.setFont(font);
			filler.setFont(font);
			label.setFont(font);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.swt.widgets.Control#setBackground(org.eclipse.swt.graphics.Color)
		 */
		@Override
		public void setBackground(Color color) {
			filler.setBackground(color);
			label.setBackground(color);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.swt.widgets.Control#setForeground(org.eclipse.swt.graphics.Color)
		 */
		@Override
		public void setForeground(Color color) {
			label.setForeground(color);
		}

		public void setBorderColor(Color color) {
			super.setBackground(color);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
		 */
		@Override
		public void handleEvent(Event event) {
			notifyListeners(event.type, event);
		}
	}

	static private class DayLabel extends Label {
		public DayLabel(Composite parent, int style) {
			super(parent, style);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.swt.widgets.Control#computeSize(int, int, boolean)
		 */
		@Override
		public Point computeSize(int wHint, int hHint, boolean changed) {
			if (wHint == SWT.DEFAULT) {
				GC gc = new GC(this);
				wHint = gc.textExtent("22").x; //$NON-NLS-1$
				gc.dispose();
			}

			return super.computeSize(wHint, hHint, changed);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.swt.widgets.Widget#checkSubclass()
		 */
		@Override
		protected void checkSubclass() {
		}
	}
}
