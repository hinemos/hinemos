/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.util.ControlUtil;

public class SnapshotDialog extends CommonDialog implements CloudStringConstants {
	public static final long serialVersionUID = 1L;
	private String snapshotName;
	private String description;
	private String dialogTitle;

	private Text text;
	private Text text_1;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public SnapshotDialog(Shell parentShell, String dialogTitle) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.RESIZE | SWT.TITLE | SWT.APPLICATION_MODAL);
		this.dialogTitle = dialogTitle;
	}

	@Override
	protected void customizeDialog(Composite parent) {
		GridLayout gl_parent = new GridLayout(2, false);
		gl_parent.marginBottom = 10;
		gl_parent.marginTop = 10;
		gl_parent.marginRight = 10;
		gl_parent.marginLeft = 10;
		
		parent.setLayout(gl_parent);
		GridData gd_parent = new GridData(SWT.FILL, SWT.FILL, true, false);
		parent.setLayoutData(gd_parent);
		
		
		Label lblNewLabel = new Label(parent, SWT.RIGHT);
		lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		lblNewLabel.setText(strSnapshotName + strSeparator);
		
		text = new Text(parent, SWT.BORDER);
		if (snapshotName != null) {
			text.setText(snapshotName);
		}
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				checkFinished();
			}
		});
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		Label lblNewLabel_1 = new Label(parent, SWT.RIGHT);
		lblNewLabel_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		lblNewLabel_1.setText(strDescription + strSeparator);
		
		text_1 = new Text(parent, SWT.BORDER | SWT.MULTI);
		if (description != null) {
			text_1.setText(description);
		}
		text_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));

		ControlUtil.setRequired(new Control[]{text});

		getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				checkFinished();
			}
		});

		//pack:resize to be its preferred size
		getShell().pack();
		getShell().setSize(getInitialSize());
		
		Display display = getShell().getDisplay();
		getShell().setLocation((display.getBounds().width - getShell().getSize().x) / 2,
				(display.getBounds().height - getShell().getSize().y) / 2);
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, DialogConstants.OK_ID, DialogConstants.OK_LABEL,
				true);
		createButton(parent, DialogConstants.CANCEL_ID,
				DialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}
	
	public String getSnapshotName() {
		return snapshotName;
	}
	
	public String getDescription() {
		return description;
	}

	@Override
	protected void okPressed() {
		snapshotName = text.getText();
		description = text_1.getText();
		
		super.okPressed();
	}
	
	protected void checkFinished() {
		Button okButton = getButton(DialogConstants.OK_ID);
		if (okButton == null)
			return;
			
		if (text != null && !text.getText().isEmpty()) {
			getButton(DialogConstants.OK_ID).setEnabled(true);
		} else {
			getButton(DialogConstants.OK_ID).setEnabled(false);
		}
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(dialogTitle);
	}
}
