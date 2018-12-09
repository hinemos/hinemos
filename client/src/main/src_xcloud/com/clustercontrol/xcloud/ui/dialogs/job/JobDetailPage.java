/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.dialogs.job;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.util.ControlUtil;

public class JobDetailPage extends WizardPage implements CloudStringConstants {
	public static final long serialVersionUID = 1L;
	public static final String pageName = JobDetailPage.class.getName();

	private Text txtJobId;
	private Text txtJobName;

	/**
	 * Create the wizard.
	 */
	public JobDetailPage() {
		super(pageName);
		setTitle(msgSetingJob);
		setDescription(msgInputJobDetail);
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		ScrolledComposite scroll = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);

		Composite container = new Composite(scroll, SWT.NULL);
		
		setControl(scroll);
		container.setLayout(new GridLayout(3, false));
		
		Label lblNewLabel = new Label(container, SWT.RIGHT);
		lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText(strJobId + strSeparator);
		
		txtJobId = new Text(container, SWT.BORDER);
		txtJobId.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getContainer().updateButtons();
			}
		});
		txtJobId.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		Label lblNewLabel_1 = new Label(container, SWT.RIGHT);
		lblNewLabel_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText(strJobName + strSeparator);
		
		txtJobName = new Text(container, SWT.BORDER);
		txtJobName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getContainer().updateButtons();
			}
		});
		txtJobName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		ControlUtil.setRequired(txtJobId, txtJobName);
		
		container.setSize(430, 64);
		
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		scroll.setContent(container);
		scroll.setMinSize(container.getSize().x, container.getSize().y);
	}
	
	public String getJobId() {
		return txtJobId.getText();
	}
	public void setJobId(String jobId) {
		txtJobId.setText(jobId);
	}
	
	public String getJobName() {
		return txtJobName.getText();
	}
	public void setJobName(String jobName) {
		txtJobName.setText(jobName);
	}
	
	@Override
	public boolean isPageComplete() {
		return super.isPageComplete() && txtJobName.getText() != null && !txtJobName.getText().isEmpty() && txtJobId.getText() != null && !txtJobId.getText().isEmpty();
	}
}
