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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.composite.JobTreeComposite;
import com.clustercontrol.jobmanagement.viewer.JobTreeContentProvider;
import com.clustercontrol.jobmanagement.viewer.JobTreeViewer;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;
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
		Composite container = new Composite(parent, SWT.NULL);

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
							obj = ((JobTreeItem)list.get(0)).getChildren().toArray();
						} else if (parentElement instanceof JobTreeItem){
							List<JobTreeItem> treeItems = new ArrayList<>();
							for (JobTreeItem item: ((JobTreeItem)parentElement).getChildren()) {
								if (item.getData().getType() != JobConstant.TYPE_JOB) {
									treeItems.add(item);
								}
							}
							obj = treeItems.toArray();
						}
						return obj;
					}

					@Override
					public boolean hasChildren(Object element) {
						JobTreeItem item = (JobTreeItem)element;
						for (JobTreeItem child: item.getChildren()) {
							if (child.getData().getType() != JobConstant.TYPE_JOB)
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
	}

	@Override
	public boolean isPageComplete() {
		if (!treeComposite.getSelectItemList().isEmpty()) {
			JobTreeItem selected = treeComposite.getSelectItemList().get(0);
			if (selected.getData().getType() == JobConstant.TYPE_JOBUNIT || selected.getData().getType() == JobConstant.TYPE_JOBNET)
				return super.isPageComplete();
		}
		return false;
	}
	
	public JobTreeItem getSelectedItem() {
		if (!treeComposite.getSelectItemList().isEmpty()) {
			JobTreeItem selected = treeComposite.getSelectItemList().get(0);
			if (selected.getData().getType() == JobConstant.TYPE_JOBUNIT || selected.getData().getType() == JobConstant.TYPE_JOBNET)
				return selected;
		}
		return null;
	}
}
