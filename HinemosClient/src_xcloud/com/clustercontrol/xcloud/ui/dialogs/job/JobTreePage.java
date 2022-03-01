/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.dialogs.job;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.clustercontrol.jobmanagement.util.JobInfoWrapper;

import com.clustercontrol.jobmanagement.composite.JobTreeComposite;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmanagement.viewer.JobTreeContentProvider;
import com.clustercontrol.jobmanagement.viewer.JobTreeViewer;
import com.clustercontrol.xcloud.common.CloudStringConstants;

public class JobTreePage extends WizardPage implements CloudStringConstants {
	public static final long serialVersionUID = 1L;
	public static final String pageName = JobTreePage.class.getName();
	
	private static final Log logger = LogFactory.getLog(JobTreePage.class);

	protected String ownerRoleId;

	protected String managerName;
	/** ジョブツリー用のコンポジット */
	protected JobTreeComposite treeComposite = null;

	/**
	 * Create the wizard.
	 */
	public JobTreePage(String managerName, String ownerRoleId) {
		super(pageName);
		setTitle(msgSelectJobnetSummary);
		setDescription(msgSelectJobnet);
		
		this.ownerRoleId = ownerRoleId;
		this.managerName = managerName;
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		ScrolledComposite scroll = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);

		Composite container = new Composite(scroll, SWT.NULL);

		setControl(container);
		container.setLayout(new GridLayout(1, false));
		
		treeComposite = new JobTreeComposite(container, SWT.NONE, managerName, ownerRoleId, false, false);
		
		treeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		try {
			Field v = JobTreeComposite.class.getDeclaredField("m_viewer");
			boolean va = v.isAccessible();
			try {
				v.setAccessible(true);
				JobTreeViewer viewer = (JobTreeViewer)v.get(treeComposite);
				
				viewer.setContentProvider(new JobTreeContentProvider() {
					@Override
					public Object[] getChildren(Object parentElement) {
						Object[] obj = null;
						if(parentElement instanceof List<?>) {
							List<?> list = (List<?>)parentElement;
							obj = ((JobTreeItemWrapper)list.get(0)).getChildren().toArray();
						} else if (parentElement instanceof JobTreeItemWrapper){
							List<JobTreeItemWrapper> treeItems = new ArrayList<>();
							for (JobTreeItemWrapper item: ((JobTreeItemWrapper)parentElement).getChildren()) {
								if (item.getData().getType() != JobInfoWrapper.TypeEnum.RESOURCEJOB) {
									treeItems.add(item);
								}
							}
							obj = treeItems.toArray();
						}
						return obj;
					}

					@Override
					public boolean hasChildren(Object element) {
						JobTreeItemWrapper item = (JobTreeItemWrapper)element;
						for (JobTreeItemWrapper child: item.getChildren()) {
							if (child.getData().getType() != JobInfoWrapper.TypeEnum.RESOURCEJOB)
								return true;
						}
						return false;
					}
				});
				viewer.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						getContainer().updateButtons();
					}
				});
			} finally {
				v.setAccessible(va);
			}
		} catch(RuntimeException e) {
			logger.warn(e.getMessage(), e);
		} catch (NoSuchFieldException e) {
			logger.warn(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			logger.warn(e.getMessage(), e);
		}

		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		scroll.setContent(container);
		scroll.setMinSize(520, 180);
		setControl(scroll);
	}

	@Override
	public boolean isPageComplete() {
		if (!treeComposite.getSelectItemList().isEmpty()) {
			JobTreeItemWrapper selected = treeComposite.getSelectItemList().get(0);
			if (selected.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT || selected.getData().getType() == JobInfoWrapper.TypeEnum.JOBNET)
				return super.isPageComplete();
		}
		return false;
	}
	
	public JobTreeItemWrapper getSelectedItem() {
		if (!treeComposite.getSelectItemList().isEmpty()) {
			JobTreeItemWrapper selected = treeComposite.getSelectItemList().get(0);
			if (selected.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT || selected.getData().getType() == JobInfoWrapper.TypeEnum.JOBNET)
				return selected;
		}
		return null;
	}
}
