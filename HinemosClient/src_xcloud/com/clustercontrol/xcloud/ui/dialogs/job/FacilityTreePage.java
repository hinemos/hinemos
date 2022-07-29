/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.dialogs.job;

import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;

import com.clustercontrol.composite.FacilityTreeComposite;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.xcloud.common.CloudStringConstants;

public class FacilityTreePage extends WizardPage implements CloudStringConstants{
	public static final long serialVersionUID = 1L;
	public static final String pageName = FacilityTreePage.class.getName();
	
	private static final Log logger = LogFactory.getLog(FacilityTreePage.class);
	
	protected String managerName;
	protected String ownerRoleId;
	protected FacilityTreeComposite treeComposite = null;
	protected Composite container;

	/**
	 * Create the wizard.
	 */
	public FacilityTreePage(String managerName, String ownerRoleId) {
		super(pageName);
		setTitle(msgSelectScopeSummary);
		setDescription(msgSelectScope);
		
		this.ownerRoleId = ownerRoleId;
		this.managerName = managerName;
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		ScrolledComposite scroll = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);

		container = new Composite(scroll, SWT.NULL);

		setControl(container);
		container.setLayout(new GridLayout(1, false));
		
		createTreeComposite();

		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		scroll.setContent(container);
		scroll.setMinSize(520, 180);
		setControl(scroll);
	}

	@Override
	public boolean isPageComplete() {
		if (treeComposite.getSelectItem() != null) {
			FacilityTreeItemResponse selected = treeComposite.getSelectItem();
			if (selected.getData().getFacilityType() == FacilityTypeEnum.SCOPE)
				return super.isPageComplete();
		}
		return false;
	}
	
	public FacilityTreeItemResponse getSelectedItem() {
		if (treeComposite.getSelectItem() != null) {
			FacilityTreeItemResponse selected = treeComposite.getSelectItem();
			if (selected.getData().getFacilityType() == FacilityTypeEnum.SCOPE)
				return selected;
		}
		return null;
	}

	public void setOwnerRole(String ownerRoleId) {
		if (this.ownerRoleId == null) {
			if (ownerRoleId == null) {
				return;
			}
		} else if (this.ownerRoleId.equals(ownerRoleId)) {
			return;
		}
		
		this.ownerRoleId = ownerRoleId;
		createTreeComposite();
	}

	protected void createTreeComposite() {
		if (treeComposite != null)
			treeComposite.dispose();
		
		treeComposite = new FacilityTreeComposite(container, SWT.NONE, managerName, ownerRoleId, false, false, true);
		treeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		try {
			Field v = FacilityTreeComposite.class.getDeclaredField("treeViewer");
			boolean va = v.isAccessible();
			try {
				v.setAccessible(true);
				TreeViewer viewer = (TreeViewer)v.get(treeComposite);
				viewer.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						getContainer().updateButtons();
					}
				});
				
			} finally {
				v.setAccessible(va);
			}
		} catch(Exception e) {
			logger.warn(e.getMessage(), e);
		}
		container.layout();
	}
}
