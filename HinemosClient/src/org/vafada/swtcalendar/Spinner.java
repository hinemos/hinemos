/*
 *  Spinner.java  - A spinner component
 *  Author: Eclipse.org
 *  Modified by: Sergey Prigogin
 *  swtcalendar.sourceforge.net
 * Modified by: NTT DATA INTELLILINK Corporation <http://www.hinemos.info/>
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.vafada.swtcalendar;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.IdentityHashMap;
import java.util.regex.Pattern;

import org.eclipse.swt.SWTError;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TypedListener;

import com.clustercontrol.client.swt.SWT;
import com.clustercontrol.util.WidgetTestUtil;

/**
 *
 */
public class Spinner extends Composite implements FocusListener {
	private static final int BUTTON_WIDTH = 16;

	private IdentityHashMap<SelectionListener, TypedListener> selectionListeners = new IdentityHashMap<SelectionListener, TypedListener>(3);

	private int minimum;

	private int maximum;

	private boolean cyclic;

	private NumberFormat numberFormat = new DecimalFormat("0");

	private boolean settingValue;

	private boolean inFocus;

	private Text text;

	private RepeatingButton upButton;

	private RepeatingButton downButton;

	public Spinner(Composite parent, int style) {
		super(parent, style);
		setFont(parent.getFont());

		Pattern.compile("[^\\w\\.]").matcher(this.getClass().getName()).replaceAll("-");

		minimum = 0;
		maximum = 9;
		setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		setLayout(gridLayout);

		{
			text = new Text(this, SWT.RIGHT);
			WidgetTestUtil.setTestId(this, null, text);
			text.setFont(getFont());
			final GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			text.setLayoutData(gridData);
			text.addVerifyListener(new VerifyListener() {
				@Override
				public void verifyText(VerifyEvent event) {
					verify(event);
				}
			});
			text.addTraverseListener(new TraverseListener() {
				@Override
				public void keyTraversed(TraverseEvent event) {
					traverse(event);
				}
			});
			text.addFocusListener(this);
		}
		{
			final Composite buttonHolder = new Composite(this, SWT.NO_FOCUS);
			WidgetTestUtil.setTestId(this, "buttonholder", buttonHolder);
			buttonHolder.setFont(getFont());
			final GridData gridData = new GridData(
					GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
			gridData.widthHint = BUTTON_WIDTH;
			buttonHolder.setLayoutData(gridData);
			buttonHolder.setLayout(new FillLayout(SWT.VERTICAL));

			upButton = new RepeatingButton(buttonHolder, SWT.ARROW | SWT.CENTER
					| SWT.UP | SWT.NO_FOCUS);
			WidgetTestUtil.setTestId(this, "up", upButton);
			upButton.setFont(getFont());
			upButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					upInternal();
					text.setFocus();
				}
			});

			downButton = new RepeatingButton(buttonHolder, SWT.ARROW
					| SWT.CENTER | SWT.DOWN | SWT.NO_FOCUS);
			WidgetTestUtil.setTestId(this, "spinnerdown", downButton);
			downButton.setFont(getFont());
			downButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					downInternal();
					text.setFocus();
				}
			});
		}

		setTabList(new Control[] { text });
		setValueInternal(minimum);
	}

	public void up() {
		settingValue = true;
		try {
			upInternal();
		} finally {
			settingValue = false;
		}
	}

	public void down() {
		settingValue = true;
		try {
			downInternal();
		} finally {
			settingValue = false;
		}
	}

	public void setValue(int value) {
		settingValue = true;
		try {
			setValueInternal(value);
		} finally {
			settingValue = false;
		}
	}

	public int getValue() {
		try {
			return numberFormat.parse(text.getText()).intValue();
		} catch (ParseException e) {
			return minimum;
		}
	}

	public void setMaximum(int maximum) {
		this.maximum = maximum;
		setValue(getValue());
	}

	public int getMaximum() {
		return maximum;
	}

	public void setMinimum(int minimum) {
		this.minimum = minimum;
		setValue(getValue());
	}

	public int getMinimum() {
		return minimum;
	}

	/**
	 * Returns <code>true</code> if the Spinner is in cyclic mode, otherwise
	 * <code>false</code>.
	 */
	public boolean isCyclic() {
		return cyclic;
	}

	/**
	 * Sets cyclic mode. In cyclic mode pressing the up arrow button repeatedly
	 * increments the value from <code>minimum</code> to <code>maximum</code>
	 * and then starts from <code>minimum</code> again.
	 *
	 * @param cyclic
	 *            <code>true</code> to set cyclic mode, <code>false</code>
	 *            to turn it off.
	 */
	public void setCyclic(boolean cyclic) {
		this.cyclic = cyclic;
	}

	public void setRange(int minimum, int maximum, boolean cyclic) {
		this.minimum = minimum;
		this.maximum = maximum;
		this.cyclic = cyclic;
		setValueInternal(getValue());
	}

	/**
	 * @return Returns the number format used by the spinner.
	 */
	public NumberFormat getNumberFormat() {
		return numberFormat;
	}

	/**
	 * @param numberFormat
	 *            The number format to set.
	 */
	public void setNumberFormat(NumberFormat numberFormat) {
		int val = getValue();
		this.numberFormat = numberFormat;
		setValue(val);
	}

	/**
	 * @return Returns the initial repeat delay in milliseconds.
	 */
	public int getInitialRepeatDelay() {
		return upButton.getInitialRepeatDelay();
	}

	/**
	 * @param initialRepeatDelay
	 *            The new initial repeat delay in milliseconds.
	 */
	public void setInitialRepeatDelay(int initialRepeatDelay) {
		upButton.setInitialRepeatDelay(initialRepeatDelay);
		downButton.setInitialRepeatDelay(initialRepeatDelay);
	}

	/**
	 * @return Returns the repeat delay in millisecons.
	 */
	public int getRepeatDelay() {
		return upButton.getRepeatDelay();
	}

	/**
	 * @param repeatDelay
	 *            The new repeat delay in milliseconds.
	 */
	public void setRepeatDelay(int repeatDelay) {
		upButton.setRepeatDelay(repeatDelay);
		downButton.setRepeatDelay(repeatDelay);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.widgets.Control#setFont(org.eclipse.swt.graphics.Font)
	 */
	@Override
	public void setFont(Font font) {
		super.setFont(font);
		if (text != null) {
			text.setFont(font);
		}
	}

	public boolean isSettingValue() {
		return settingValue;
	}

	public void addModifyListener(ModifyListener listener) {
		text.addModifyListener(listener);
	}

	public void removeModifyListener(ModifyListener listener) {
		text.removeModifyListener(listener);
	}

	public void addSelectionListener(SelectionListener listener) {
		if (listener == null) {
			throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
		}
		TypedListener typedListener = new TypedListener(listener);
		selectionListeners.put(listener, typedListener);
		addListener(SWT.Selection, typedListener);
	}

	public void removeSelectionListener(SelectionListener listener) {
		if (listener == null) {
			throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
		}
		TypedListener typedListener = (TypedListener) selectionListeners
				.remove(listener);
		if (typedListener != null) {
			removeListener(SWT.Selection, typedListener);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.widgets.Control#computeSize(int, int)
	 */
	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		if (wHint == SWT.DEFAULT) {
			GC gc = new GC(text);
			wHint = Math.max(gc.textExtent(numberFormat.format(maximum)).x, gc
					.textExtent(numberFormat.format(maximum)).x);
			gc.dispose();
		}

		Point size = text.computeSize(wHint, hHint, changed);
		size.x += BUTTON_WIDTH;
		if ((getStyle() & SWT.BORDER) != 0) {
			int border = getBorderWidth();
			size.x += border * 2;
			size.y += border * 2 + 3;
		}
		size.y = (size.y + 1) & ~1; // Round up to an even number.
		return size;
	}

	protected void upInternal() {
		int val = getValue();
		val++;
		if (val > maximum) {
			if (cyclic) {
				val = minimum;
			} else {
				val = maximum;
			}
		}
		setValueInternal(val);
		notifyListeners(SWT.Selection, new Event());
	}

	protected void downInternal() {
		int val = getValue();
		val--;
		if (val < minimum) {
			if (cyclic) {
				val = maximum;
			} else {
				val = minimum;
			}
		}
		setValueInternal(val);
		notifyListeners(SWT.Selection, new Event());
	}

	protected void setValueInternal(int value) {
		if (value < minimum) {
			value = minimum;
		} else if (value > maximum) {
			value = maximum;
		}
		String str = numberFormat.format(value);
		if (!str.equals(text.getText())) {
			text.setText(str);
		}
	}

	private void verify(VerifyEvent event) {
		for (int i = 0; i < event.text.length(); i++) {
			char c = event.text.charAt(i);
			if (!Character.isDigit(c)
					&& !(minimum < 0 && c == '-' && i == 0 && event.start == 0)
					&& numberFormat.format(minimum).indexOf(c) < 0) {
				event.doit = false;
				break;
			}
		}
	}

	private void traverse(TraverseEvent event) {
		switch (event.detail) {
		case SWT.TRAVERSE_ARROW_PREVIOUS:
			if (event.keyCode == SWT.ARROW_UP) {
				event.doit = true;
				event.detail = SWT.NULL;
				upInternal();
			}
			break;
		case SWT.TRAVERSE_ARROW_NEXT:
			if (event.keyCode == SWT.ARROW_DOWN) {
				event.doit = true;
				event.detail = SWT.NULL;
				downInternal();
			}
			break;
		default:
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
	 */
	@Override
	public void focusGained(FocusEvent focusEvent) {
		if (!inFocus) {
			inFocus = true;
			Event event = new Event();
			event.time = focusEvent.time;
			notifyListeners(SWT.FocusIn, event);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
	 */
	@Override
	public void focusLost(FocusEvent focusEvent) {
		if (!isFocusControl()) {
			inFocus = false;
			Event event = new Event();
			event.time = focusEvent.time;
			notifyListeners(SWT.FocusOut, event);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.widgets.Control#isFocusControl()
	 */
	@Override
	public boolean isFocusControl() {
		Control control = getDisplay().getFocusControl();
		return control == this || control == text;
	}
}
