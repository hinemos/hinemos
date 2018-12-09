/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class FooterComposite extends Composite {
	public static final long serialVersionUID = 1L;
	private Label leftControl;
	private Label rightControl;

	public FooterComposite(Composite parent, int style) {
		super(parent, style);
		
		GridLayout footerLayout = new GridLayout(1, true);
		footerLayout.horizontalSpacing = 0;
		footerLayout.marginHeight = 0;
		footerLayout.marginWidth = 0;
		footerLayout.verticalSpacing = 0;
		footerLayout.numColumns = 3;
		this.setLayout(footerLayout);

		leftControl = new Label(this, SWT.NONE);
		leftControl.setAlignment(SWT.LEFT);
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		leftControl.setLayoutData(gridData);

		rightControl = new Label(this, SWT.NONE);
		rightControl.setAlignment(SWT.RIGHT);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		rightControl.setLayoutData(gridData);
	}

	public Label getLeftControl() {
		return leftControl;
	}

	public Label getRightControl() {
		return rightControl;
	}
}
