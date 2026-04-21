/*
 * Copyright (c) 2026 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.composite;

import java.net.URL;
import java.text.MessageFormat;
import java.util.Date;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.dialog.DateTimeDialog;
import com.clustercontrol.util.TimezoneUtil;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/*
 * クリア機能を追加した日時指定エディタ
 * 
 * DateTimeDialogCellEditorの実装をベースにツールバーでクリアとエディタ表示のボタンを追加
 */
@SuppressFBWarnings(
	value = "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR",
	justification = "defaultLabel and toolbar are initialized in createControl() before SWT layout callbacks are used."
)
public class ClearableDateTimeDialogCellEditor extends CellEditor {

	public static final String CELL_EDITOR_IMG_DOTS = "cell_editor_dots_image";
	public static final String CELL_EDITOR_IMG_CLEAR = "cell_editor_clear_image";

	protected Composite editor;

	protected Label defaultLabel;
	protected ToolBar toolbar;
	protected ToolItem chooseItem;
	protected ToolItem clearItem;

	protected Object value = null;

	protected FocusListener buttonFocusListener;

	static {
		ImageRegistry reg = JFaceResources.getImageRegistry();
		reg.put(CELL_EDITOR_IMG_DOTS, imageDescriptor("icons/dots.gif"));
		reg.put(CELL_EDITOR_IMG_CLEAR, imageDescriptor("icons/delete_obj.gif"));
	}

	protected class DialogCellLayout extends Layout {
		public void layout(Composite editor, boolean force) {
			Rectangle bounds = editor.getClientArea();

			Point toolbarSize = toolbar.computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
			Point defaultLabelSize = defaultLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, force);

			defaultLabel.setBounds(2, (bounds.height - defaultLabelSize.y) / 2, bounds.width - toolbarSize.x - 2,
					defaultLabelSize.y);
			toolbar.setBounds(bounds.width - toolbarSize.x, 0, toolbarSize.x, bounds.height);
		}

		public Point computeSize(Composite editor, int wHint, int hHint, boolean force) {
			if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT) {
				return new Point(wHint, hHint);
			}
			Point contentsSize = defaultLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
			Point buttonSize = toolbar.computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
			Point result = new Point(buttonSize.x, Math.max(contentsSize.y, buttonSize.y));
			return result;
		}
	}

	public ClearableDateTimeDialogCellEditor() {
		super();
	}

	@SuppressFBWarnings(
		value = "SIC_INNER_SHOULD_BE_STATIC_ANON",
		justification = "Anonymous listener is intentionally kept local for readability."
	)
	@Override
	protected Control createControl(Composite parent) {
		editor = new Composite(parent, SWT.NONE);
		editor.setBackground(parent.getBackground());
		editor.setBackgroundMode(SWT.INHERIT_DEFAULT);
		editor.setLayout(new DialogCellLayout());

		defaultLabel = new Label(editor, SWT.LEFT | SWT.CENTER);
		defaultLabel.setBackground(parent.getBackground());

		toolbar = new ToolBar(editor, SWT.FLAT);
		toolbar.setBackground(parent.getBackground());

		chooseItem = new ToolItem(toolbar, SWT.PUSH);
		chooseItem.setImage(JFaceResources.getImageRegistry().get(CELL_EDITOR_IMG_DOTS));
		chooseItem.setToolTipText("Select...");
		chooseItem.addListener(SWT.Selection, e -> {
			toolbar.removeFocusListener(getButtonFocusListener());

			Object newValue = openDialogBox(editor);

			toolbar.addFocusListener(getButtonFocusListener());

			if (newValue != null) {
				boolean newValidState = isCorrect(newValue);
				if (newValidState) {
					markDirty();
					doSetValue(newValue);
				} else {
					setErrorMessage(MessageFormat.format(getErrorMessage(), new Object[] { newValue.toString() }));
				}
				fireApplyEditorValue();
			}
		});

		clearItem = new ToolItem(toolbar, SWT.PUSH);
		clearItem.setImage(JFaceResources.getImageRegistry().get(CELL_EDITOR_IMG_CLEAR));
		clearItem.setToolTipText("claer");
		clearItem.addListener(SWT.Selection, e -> {
			markDirty();
			doSetValue(null);
			fireApplyEditorValue();
		});

		toolbar.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				if (e.childID == 0) {
					e.result = "Select...";
				}
				if (e.childID == 1) {
					e.result = "Clear";
				}
			}
		});

		return editor;
	}

	protected void updateContents(Object value) {
		if (defaultLabel != null && !defaultLabel.isDisposed()) {
			if (value instanceof Date) {
				defaultLabel.setText(TimezoneUtil.getSimpleDateFormat().format((Date) value));
			} else {
				defaultLabel.setText("");
			}
		}
		if (clearItem != null && !clearItem.isDisposed()) {
			clearItem.setEnabled(value instanceof Date);
		}
	}

	@Override
	protected void doSetValue(Object value) {
		this.value = value;
		updateContents(value);
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	protected Object openDialogBox(Control cellEditorWindow) {
		// 日時ダイアログを表示
		DateTimeDialog dialog = new DateTimeDialog(cellEditorWindow.getShell());

		if (getValue() instanceof Date) {
			dialog.setDate((Date) getValue());
		}

		// 選択した日時を取得する
		dialog.open();

		return dialog.getDate();
	}

	protected static ImageDescriptor imageDescriptor(String pluguinRelationPath) {
		try {
			URL url = new URL(ClusterControlPlugin.getDefault().getBundle().getEntry("/"), pluguinRelationPath);
			return ImageDescriptor.createFromURL(url);
		} catch (Exception e) {
		}
		return null;
	}

	@Override
	protected Object doGetValue() {
		return value;
	}

	@Override
	protected void doSetFocus() {
		toolbar.setFocus();
		toolbar.addFocusListener(getButtonFocusListener());
	}

	public void deactivate() {
		if (toolbar != null && !toolbar.isDisposed()) {
			toolbar.removeFocusListener(getButtonFocusListener());
		}
		super.deactivate();
	}

	private FocusListener getButtonFocusListener() {
		if (buttonFocusListener == null) {
			buttonFocusListener = new FocusListener() {
				public void focusGained(FocusEvent e) {
				}

				public void focusLost(FocusEvent e) {
					ClearableDateTimeDialogCellEditor.this.focusLost();
				}
			};
		}
		return buttonFocusListener;
	}
}
