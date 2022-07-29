/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.views;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import com.clustercontrol.util.TableViewerSorter;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.base.ElementBaseModeWatch;
import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.cloud.IStorage;
import com.clustercontrol.xcloud.model.cloud.IStorageBackupEntry;
import com.clustercontrol.xcloud.util.ControlUtil;

/**
 */
public class StorageBackupsView extends AbstractCloudViewPart implements CloudStringConstants {
	public static final String Id = "com.clustercontrol.xcloud.ui.views.StorageBackupsView";
	
	private static final Log logger = LogFactory.getLog(StorageBackupsView.class);
	
	protected ElementBaseModeWatch.AnyPropertyWatcher watcher = new Watcher<IStorageBackupEntry>(){
		@Override protected void asyncRefresh() {
			StorageBackupsView.this.refresh();
		}
		@Override
		protected void unwatchedOwner(IElement owning, IElement owned) {
			currentStorage = null;
			tableViewer.setInput(null);
		}
	};
	
	protected ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (!part.getSite().getId().equals(StoragesView.Id))
				return;
			refresh(selection);
		}
	};
	
	private FooterComposite footerComposite;
	private Table table;
	private TableViewer tableViewer;
	
	private IStorage currentStorage;
	
	private static SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd H:mm:ss");

	public StorageBackupsView() {
 		super();
	}

	@Override
	protected void internalCreatePartControl(Composite arg0) {
		Composite composite = new Composite(arg0, SWT.NONE);
		GridLayout gl_composite = new GridLayout(1, true);
		gl_composite.horizontalSpacing = 0;
		gl_composite.marginHeight = 0;
		gl_composite.marginWidth = 0;
		gl_composite.verticalSpacing = 0;
		composite.setLayout(gl_composite);

//		lblHeader = new Label(composite, SWT.NONE);
//		lblHeader.setSize(lblHeader.getSize().x, 80);
//		GridData gridData = new GridData();
//		gridData.horizontalAlignment = GridData.FILL;
//		gridData.verticalAlignment = GridData.FILL;
//		lblHeader.setLayoutData(gridData);

		Composite composite_1 = new Composite(composite, SWT.NONE);
		TableColumnLayout tcl_composite_1 = new TableColumnLayout();
		composite_1.setLayout(tcl_composite_1);

		tableViewer = new TableViewer(composite_1, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		for(final ViewColumn column: ViewColumn.values()){
			TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			TableColumn tableColumn = tableViewerColumn.getColumn();
			tcl_composite_1.setColumnData(tableColumn, column.getPixelData());
			tableColumn.setText(column.getLabel());
			tableViewerColumn.setLabelProvider(column.getProvider());
			tableColumn.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					tableViewer.setSorter(new TableViewerSorter(tableViewer, column.getProvider()));
				}
			});
		}

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		composite_1.setLayoutData(gridData);

		footerComposite = new FooterComposite(composite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		footerComposite.setSize(footerComposite.getLeftControl().getSize().x, 80);
		footerComposite.setLayoutData(gridData);
		footerComposite.getRightControl().setText(strFooterTitle + 0);

		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setComparator(new ViewerComparator() {
			// Set sorting key by element type
			private String getSortingKey(Object element){
				return (element instanceof IStorageBackupEntry)? ((IStorageBackupEntry)element).getId() : "";
			}

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return getSortingKey(e1).compareTo(getSortingKey(e2));
			}
		});

		this.getSite().setSelectionProvider(tableViewer);
		this.getSite().getPage().addSelectionListener(StoragesView.Id, selectionListener);
	}
	
	@Override
	protected StructuredViewer getViewer() {
		return tableViewer;
	}
	
	private enum ViewColumn{
		status(
			strState,
			new ColumnPixelData(60, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((IStorageBackupEntry)element).getStatus();
				}
			}
		),
		status_detail(
			strStateDetail,
			new ColumnPixelData(100, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((IStorageBackupEntry)element).getStatusAsPlatform();
				}
			}
		),
		snapshot_id(
			strSnapshotId,
			new ColumnPixelData(160, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((IStorageBackupEntry)element).getId();
				}
			}
		),
		snapshot_name(
			strSnapshotName,
			new ColumnPixelData(160, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((IStorageBackupEntry)element).getName();
				}
			}
		),
		create_time(
			strCreateDate,
			new ColumnPixelData(150, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return format.format(((IStorageBackupEntry)element).getCreateTime());
				}
			}
		),
		detail(
			strDescription,
			new ColumnPixelData(200, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((IStorageBackupEntry)element).getDescription();
				}
			}
		);

		private String label;
		private ColumnLabelProvider provider;
		private ColumnPixelData pixelData;
		
		
		ViewColumn(String label, ColumnPixelData pixelData, ColumnLabelProvider provider){
			this.label = label;
			this.pixelData = pixelData;
			this.provider = provider;
		}

		public String getLabel() {
			return label;
		}

		public ColumnPixelData getPixelData() {
			return pixelData;
		}

		public ColumnLabelProvider getProvider() {
			return provider;
		}
	}

	@Override
	public void dispose() {
		if (currentStorage != null)
			currentStorage.getCloudScope().getCloudScopes().getHinemosManager().getModelWatch().removeWatcher(currentStorage.getBackup(), watcher);

		getSite().getPage().removeSelectionListener(StoragesView.Id, selectionListener);
		getSite().setSelectionProvider(null);
		super.dispose();
	}

	@Override
	public String getId() {
		return Id;
	}

	@Override
	public void update() {
		if (currentStorage != null)
			currentStorage.getBackup().update();
	}
	
	protected void refresh() {
		ISelection selection = getSite().getPage().getSelection(StoragesView.Id);
		refresh(selection);
	}
	
	protected void refresh(ISelection selection) {
		if (currentStorage != null) {
			try {
				currentStorage.getCloudScope().getCloudScopes().getHinemosManager().getModelWatch().removeWatcher(currentStorage.getBackup(), watcher);
			} catch (CloudModelException e) {
				logger.warn(e.getMessage(), e);
				ControlUtil.openError(e, msgErrorFinishRefreshView);
			}
		}
		
		currentStorage = null;
		List<IStorageBackupEntry> entries = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sselection = (IStructuredSelection)selection;
			if (!sselection.isEmpty()) {
				if (sselection.getFirstElement() instanceof IStorage) {
					currentStorage = (IStorage)sselection.getFirstElement();
					currentStorage.getCloudScope().getCloudScopes().getHinemosManager().getModelWatch().addWatcher(currentStorage.getBackup(), watcher);
					entries = Arrays.asList(currentStorage.getBackup().getEntriesWithInitializing());
				}
			}
		}
		
		tableViewer.setInput(entries);
		getViewSite().getActionBars().updateActionBars();
		getViewSite().getActionBars().getToolBarManager().update(false);
		
		footerComposite.getRightControl().setText(strFooterTitle + (entries != null ? entries.size(): 0));
	}
}
