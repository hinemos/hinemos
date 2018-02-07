/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
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
import com.clustercontrol.ws.xcloud.ModifyBillingSettingRequest;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.util.ControlUtil;

public class ModifyBillingSettingDialog extends CommonDialog implements CloudStringConstants {
	public static final long serialVersionUID = 1L;
	
	protected ModifyBillingSettingRequest output = new ModifyBillingSettingRequest();
	protected ICloudScope cloudScope;
	
	protected Text txtRetentionPeriod;
	protected Button btnBillingDetailCollectorFlg;
	
	private VerifyListener numberVelifylistener = (new VerifyListener() {
		public void verifyText(VerifyEvent e) {
			try {
				Text text = (Text)e.getSource();
				String t = text.getText().substring(0, e.start) + e.text + text.getText().substring(e.end);
				if (t.length() != 0) {
					Integer.valueOf(t);
				}
			} catch(Exception e1) {
				e.doit = false;
			}
		}
	});
	
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public ModifyBillingSettingDialog(Shell parentShell, ICloudScope cloudScope) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.RESIZE | SWT.TITLE | SWT.APPLICATION_MODAL);
		this.cloudScope = cloudScope;
	}

	@Override
	protected void customizeDialog(Composite parent) {
		GridLayout gl_parent = new GridLayout(2, true);
		gl_parent.marginBottom = 10;
		gl_parent.marginTop = 10;
		gl_parent.marginRight = 10;
		gl_parent.marginLeft = 10;
		
		parent.setLayout(gl_parent);
		GridData gd_parent = new GridData(SWT.FILL, SWT.FILL, true, false);
		parent.setLayoutData(gd_parent);
		
		
		Composite container = new Composite(parent, SWT.FILL);
		GridLayout gl_container = new GridLayout(2, false);
		gl_container.verticalSpacing = 15;
		container.setLayout(gl_container);
		GridData gd_container = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd_container.horizontalSpan = 2;
		container.setLayoutData(gd_container);
		
		Composite cmpBillingDetailCollectorFlg = new Composite(container, SWT.NONE);
		GridLayout gl_billingDetailCollectorFlg = new GridLayout(2, false);
		cmpBillingDetailCollectorFlg.setLayout(gl_billingDetailCollectorFlg);
		GridData gd_cmpBillingDetailCollectorFlg = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_cmpBillingDetailCollectorFlg.horizontalSpan = 2;
		cmpBillingDetailCollectorFlg.setLayoutData(gd_cmpBillingDetailCollectorFlg);
		btnBillingDetailCollectorFlg = new Button(cmpBillingDetailCollectorFlg, SWT.CHECK);
		btnBillingDetailCollectorFlg.setText(strEnableBillingDetailCollection);
		btnBillingDetailCollectorFlg.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		new Label(cmpBillingDetailCollectorFlg, SWT.NONE);
		
		//初期化ブロック1
		btnBillingDetailCollectorFlg.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				txtRetentionPeriod.setEnabled(btnBillingDetailCollectorFlg.getSelection());
			}
		});
		btnBillingDetailCollectorFlg.setSelection(cloudScope.getBillingDetailCollectorFlg() == null ? false: cloudScope.getBillingDetailCollectorFlg());
		
		new Label(parent, SWT.NONE);
		Composite cmpRetentionPeriod = new Composite(parent, SWT.NONE);
		GridData gd_cmpRetentionPeriod = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_cmpRetentionPeriod.widthHint = 230;
		cmpRetentionPeriod.setLayoutData(gd_cmpRetentionPeriod);
		GridLayout gl_retentionPeriod = new GridLayout(3, false);
		cmpRetentionPeriod.setLayout(gl_retentionPeriod);
		Label lblRetentionPeriod = new Label(cmpRetentionPeriod, SWT.NONE);
		lblRetentionPeriod.setText(strRetentionPeriod + strSeparator);
		lblRetentionPeriod.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		txtRetentionPeriod = new Text(cmpRetentionPeriod, SWT.BORDER | SWT.RIGHT);
		txtRetentionPeriod.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		txtRetentionPeriod.addVerifyListener(numberVelifylistener);
		Label lblDays = new Label(cmpRetentionPeriod, SWT.NONE);
		lblDays.setText(strDays);
		lblDays.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		//初期化ブロック2
		if(!btnBillingDetailCollectorFlg.getSelection()){
			txtRetentionPeriod.setEnabled(false);
		}
		txtRetentionPeriod.setText(cloudScope.getRetentionPeriod() == null ? Integer.toString(0): cloudScope.getRetentionPeriod().toString());

		ControlUtil.setRequired(new Control[]{txtRetentionPeriod});
		
		//pack:resize to be its preferred size
		getShell().pack();
		getShell().setSize(new Point(getShell().getSize().x, getShell().getSize().y));
		
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
				false);
		createButton(parent, DialogConstants.CANCEL_ID,
				DialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(399, 220);
	}

	@Override
	protected void okPressed() {
		output.setCloudScopeId(cloudScope.getId());
		output.setRetentionPeriod(Integer.valueOf(txtRetentionPeriod.getText()));
		output.setBillingDetailCollectorFlg(btnBillingDetailCollectorFlg.getSelection());

		super.okPressed();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(dlgBillingDetailCollectSetting);
	}

	public ModifyBillingSettingRequest getOutput() {
		return output;
	}
}
