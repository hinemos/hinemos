/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.dialog.CommonDialog;

public abstract class DetailDialog extends CommonDialog {
	protected String input;
	protected String dialogTitle;

	public DetailDialog(Shell parentShell, String dialogTitle) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.RESIZE | SWT.TITLE | SWT.APPLICATION_MODAL);
		this.dialogTitle = dialogTitle;
	}
	
	protected abstract void customizeDialog(Composite parent);
	
	@Override
	protected Point getInitialSize() {return new Point(475, 285);}

	public void setInput(String input) {this.input = input;}
	public String getInput() {return input;}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(dialogTitle);
	}
}
