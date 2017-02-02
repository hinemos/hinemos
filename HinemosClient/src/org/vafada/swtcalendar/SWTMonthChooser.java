/*
 * SWTMonthChooser.java - A month chooser component for SWT Author: Mark Bryan
 * Yu Modified by: Sergey Prigogin swtcalendar.sourceforge.net
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
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.WidgetTestUtil;


public class SWTMonthChooser extends Composite {
	private Combo comboBox;

	private Locale locale;

	public SWTMonthChooser(Composite parent) {
		super(parent, SWT.NONE);

		Pattern.compile("[^\\w\\.]").matcher(this.getClass().getName()).replaceAll("-");

		locale = Locale.getDefault();
		setLayout(new FillLayout());
		comboBox = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, null, comboBox);

		initNames();

		setMonth(Calendar.getInstance(TimezoneUtil.getTimeZone()).get(Calendar.MONTH));
		
		setFont(parent.getFont());
	}

	private void initNames() {
		DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(locale);
		String[] monthNames = dateFormatSymbols.getMonths();

		int month = comboBox.getSelectionIndex();
		if (comboBox.getItemCount() > 0) {
			comboBox.removeAll();
		}

		for (int i = 0; i < monthNames.length; i++) {
			String name = monthNames[i];
			if (name.length() > 0) {
				comboBox.add(name);
			}
		}

		if (month < 0) {
			month = 0;
		} else if (month >= comboBox.getItemCount()) {
			month = comboBox.getItemCount() - 1;
		}

		comboBox.select(month);
	}

	public void addSelectionListener(SelectionListener listener) {
		comboBox.addSelectionListener(listener);
	}

	public void removeSelectionListener(SelectionListener listener) {
		comboBox.removeSelectionListener(listener);
	}

	public void setMonth(int newMonth) {
		comboBox.select(newMonth);
	}

	public int getMonth() {
		return comboBox.getSelectionIndex();
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
		initNames();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.widgets.Control#setFont(org.eclipse.swt.graphics.Font)
	 */
	@Override
	public void setFont(Font font) {
		super.setFont(font);
		comboBox.setFont(getFont());
	}
}
