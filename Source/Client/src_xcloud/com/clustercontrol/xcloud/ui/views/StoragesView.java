/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.views;

import java.util.ArrayList;
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

import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.util.TableViewerSorter;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.extensions.ICloudModelContentProvider;
import com.clustercontrol.xcloud.extensions.CloudModelContentProviderExtension;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.base.ElementBaseModeWatch;
import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.model.cloud.ILocation;
import com.clustercontrol.xcloud.model.cloud.IStorage;
import com.clustercontrol.xcloud.model.repository.ICloudScopeScope;
import com.clustercontrol.xcloud.model.repository.IInstanceNode;
import com.clustercontrol.xcloud.model.repository.IScope;
import com.clustercontrol.xcloud.plugin.CloudOptionSourceProvider;
import com.clustercontrol.xcloud.util.ControlUtil;
import com.clustercontrol.xcloud.util.CloudUtil;


/**
 */
public class StoragesView extends AbstractCloudViewPart implements CloudStringConstants {
	public static final String Id = "com.clustercontrol.xcloud.ui.views.StoragesView";
	
	private static final Log logger = LogFactory.getLog(StoragesView.class);
	
	protected ElementBaseModeWatch.AnyPropertyWatcher watcher = new Watcher<IStorage>(){
		@Override protected void asyncRefresh() {
			StoragesView.this.refresh();
		}
		@Override
		protected void unwatchedOwner(IElement owning, IElement owned) {
			currentCloudScope = null;
			currentLocation = null;
			tableViewer.setInput(null);
		}
	};

	protected FooterComposite footerComposite;
	protected Table table;
	protected TableViewer tableViewer;
	
	protected ICloudScope currentCloudScope;
	protected ILocation currentLocation;

	protected ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (!part.getSite().getId().equals(RepositoryView.Id))
				return;
			refresh(selection);
		}
	};
	
	public StoragesView() {
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
				return (element instanceof IStorage)? ((IStorage)element).getId() : "";
			}

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return getSortingKey(e1).compareTo(getSortingKey(e2));
			}
		});

		getSite().setSelectionProvider(tableViewer);

		getSite().getPage().addSelectionListener(RepositoryView.Id, selectionListener);
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
					return ((IStorage)element).getStatus();
				}
			}
		),
		storage_id(
				strStorageId,
				new ColumnPixelData(160, true, true),
				new ColumnLabelProvider(){
					@Override
					public String getText(Object element) {
						return ((IStorage)element).getId();
					}
				}
			),
		storage_name(
			strStorageName,
			new ColumnPixelData(160, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((IStorage)element).getName();
				}
			}
		),
		storage_type(
				strType,
				new ColumnPixelData(80, true, true),
				new ColumnLabelProvider(){
					@Override
					public String getText(Object element) {
						return ((IStorage)element).getStorageType();
					}
				}
			),
		computer_id(
			strComputeId,
			new ColumnPixelData(160, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					IStorage storage = (IStorage)element;
					if (storage.getTargetInstanceId() == null)
						return "";
					return storage.getTargetInstanceId();
				}
			}
		),
		computer_name(
			strComputeName,
			new ColumnPixelData(160, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					IStorage storage = (IStorage)element;
					if (storage.getTargetInstanceId() == null)
						return "";
					try {
						return storage.getCloudComputeManager().getInstance(storage.getTargetInstanceId()).getName();
					} catch(CloudModelException e) {
						logger.warn(e.getMessage());
						return "";
					}
				}
			}),
		facility_id(
			strFacilityId,
			new ColumnPixelData(260, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((IStorage)element).getFacilityId();
				}
			}
		),
		facility_name(
			strFacilityName,
			new ColumnPixelData(160, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					IStorage storage = (IStorage)element;
					List<FacilityTreeItemResponse> items = CloudUtil.collectScopes(storage.getCloudScope().getCloudScopes().getHinemosManager().getManagerName(), storage.getFacilityId());
					if (!items.isEmpty()) {
						return items.get(0).getData().getFacilityName();
					} else {
						return "";
					}
				}
			}
		);
		
		private String label;
		private transient ColumnLabelProvider provider;
		private transient ColumnPixelData pixelData;
		
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
		if (currentCloudScope != null && currentLocation != null)
			currentCloudScope.getCloudScopes().getHinemosManager().getModelWatch().removeWatcher(currentLocation.getComputeResources(), watcher);

		getSite().getPage().removeSelectionListener(RepositoryView.Id, selectionListener);
		getSite().setSelectionProvider(null);
		super.dispose();
	}

	@Override
	public String getId() {
		return Id;
	}
	
	@Override
	public void update() {
		if (currentLocation != null) {
			currentLocation.getComputeResources().updateStorages();
		}
	}
	
	protected void refresh() {
		refresh(getSite().getPage().getSelection(RepositoryView.Id));
	}

	protected void refresh(ISelection selection) {
		if (currentCloudScope != null && currentLocation != null) {
			try {
				currentCloudScope.getCloudScopes().getHinemosManager().getModelWatch().removeWatcher(currentLocation.getComputeResources(), watcher);
			} catch (CloudModelException e) {
				logger.warn(e.getMessage(), e);
			}
		}

		List<IStorage> storages = new ArrayList<>();
		currentCloudScope = null;
		currentLocation = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sselection = (IStructuredSelection)selection;
			if (!sselection.isEmpty()) {
				Object selected = sselection.getFirstElement();
				if (selected instanceof ICloudScopeScope) {
					ICloudScopeScope scope = (ICloudScopeScope)selected;
					if (scope.getLocation() != null) {
						storages.addAll(Arrays.asList(scope.getLocation().getComputeResources().getStorages()));
					}
				} else if (selected instanceof IScope) {
					IScope scope = (IScope)selected;
					
					ICloudScopeScope cloudScopeScope = scope.getCloudScopeScope();
					if (cloudScopeScope != null && cloudScopeScope.getLocation() != null) {
						ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(cloudScopeScope.getCloudScope().getPlatformId());
						storages = Arrays.asList(provider.getChildren(selected, cloudScopeScope.getLocation().getComputeResources().getStorages()));
					} else if (scope.getLocationScope() != null) {
						ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(cloudScopeScope.getCloudScope().getPlatformId());
						storages = Arrays.asList(provider.getChildren(selected, scope.getLocationScope().getLocation().getComputeResources().getStorages()));
					}
				} else if (selected instanceof IInstanceNode) {
					IInstanceNode node = (IInstanceNode)selected;
					ILocation l = node.getLocationScope() != null ? node.getLocationScope().getLocation(): node.getCloudScopeScope().getLocation();
					for (IStorage storage: l.getComputeResources().getStorages()) {
						if (storage.getTargetInstanceId() != null) {
							if (node.getInstance().getId().equals(storage.getTargetInstanceId()))
								storages.add(storage);
						}
					}
				}
				
				if (selected instanceof IElement) {
					IElement node = (IElement)selected;
					currentCloudScope = (ICloudScope)node.getAdapter(ICloudScope.class);
					currentLocation = (ILocation)node.getAdapter(ILocation.class);
				}

				if (currentCloudScope != null && currentLocation != null) {
					try {
						currentCloudScope.getCloudScopes().getHinemosManager().getModelWatch().addWatcher(currentLocation.getComputeResources(), watcher);
					} catch (CloudModelException e) {
						logger.warn(e.getMessage(), e);
						ControlUtil.openError(e, msgErrorFinishRefreshView);
					}
				} else {
					currentCloudScope = null;
					currentLocation = null;
				}
			}
		}
		tableViewer.setInput(storages);
		getViewSite().getActionBars().updateActionBars();
		getViewSite().getActionBars().getToolBarManager().update(false);
		
		footerComposite.getRightControl().setText(strFooterTitle + storages.size());
	}
	
	@Override
	public void setFocus() {
		super.setFocus();
		CloudOptionSourceProvider.setActiveHinemosManagerToProvider(currentCloudScope != null ? currentCloudScope.getCloudScopes().getHinemosManager(): null);
		CloudOptionSourceProvider.setActiveCloudScopeToProvider(currentCloudScope);
		CloudOptionSourceProvider.setActiveLocationToProvider(currentLocation);
	}
}
