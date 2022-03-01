/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.views;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.TableViewerSorter;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.extensions.ICloudModelContentProvider;
import com.clustercontrol.xcloud.extensions.CloudModelContentProviderExtension;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.base.ElementBaseModeWatch;
import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.model.cloud.IInstance;
import com.clustercontrol.xcloud.model.cloud.ILocation;
import com.clustercontrol.xcloud.model.cloud.INetwork;
import com.clustercontrol.xcloud.model.repository.ICloudScopeScope;
import com.clustercontrol.xcloud.model.repository.IEntityNode;
import com.clustercontrol.xcloud.model.repository.IFacility;
import com.clustercontrol.xcloud.model.repository.IInstanceNode;
import com.clustercontrol.xcloud.model.repository.IScope;
import com.clustercontrol.xcloud.util.ControlUtil;


/**
 */
public class NetworksView extends AbstractCloudViewPart implements CloudStringConstants {
	public static final String Id = "com.clustercontrol.xcloud.ui.views.NetworksView";
	
	private static final Log logger = LogFactory.getLog(NetworksView.class);
	
	protected ElementBaseModeWatch.AnyPropertyWatcher watcher = new Watcher<INetwork>(){
		@Override protected void asyncRefresh() {
			NetworksView.this.refresh();
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
	
	public NetworksView() {
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
		tableViewer.setComparator(new ViewerComparator(){
			// Set sorting key by element type
			private String getSortingKey(Object element){
				String key = (element instanceof INetwork)? ((INetwork)element).getNetworkType(): null;
				return (null == key) ? "" : key;
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
			strNetworkId,
			new ColumnPixelData(160, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((INetwork)element).getId();
				}
			}
		),
		computer_name(
			strNetworkName,
			new ColumnPixelData(200, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					INetwork network = (INetwork)element;
					if (network.getName() == null)
						return "";
					return network.getName();
				}
			}),
		facility_id(
			strType,
			new ColumnPixelData(200, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					INetwork network = (INetwork)element;
					if (network.getNetworkType() == null)
						return "";
					return network.getNetworkType();
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
			currentLocation.getComputeResources().updateNetworks();
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

		currentCloudScope = null;
		currentLocation = null;
		Set<INetwork> networks = new HashSet<>();
		try {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection sselection = (IStructuredSelection)selection;
				if (!sselection.isEmpty()) {
					Object selected = sselection.getFirstElement();
					if (selected instanceof ICloudScopeScope) {
						ICloudScopeScope scope = (ICloudScopeScope)selected;
						currentCloudScope = scope.getCloudScope();
						if (scope.getLocation() != null) {
							currentLocation = scope.getLocation();
							ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(currentCloudScope.getPlatformId());
							networks.addAll(Arrays.asList(provider.getChildren(selected, currentLocation.getComputeResources().getNetworksWithInitializing())));
							collectNetworks(scope, networks);
						}
					} else if (selected instanceof IScope) {
						IScope scope = (IScope)selected;
						
						ICloudScopeScope cloudScopeScope = scope.getCloudScopeScope();
						if (cloudScopeScope != null && cloudScopeScope.getLocation() != null) {
							currentCloudScope = cloudScopeScope.getCloudScope();
							currentLocation = cloudScopeScope.getLocation();
						} else if (scope.getLocationScope() != null) {
							currentCloudScope = scope.getCloudScopeScope().getCloudScope();
							currentLocation = scope.getLocationScope().getLocation();
						}
						
						if (currentCloudScope != null) {
							ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(currentCloudScope.getPlatformId());
							networks.addAll(Arrays.asList(provider.getChildren(selected, currentLocation.getComputeResources().getNetworksWithInitializing())));
							collectNetworks(scope, networks);
						}
					} else if (selected instanceof IInstanceNode) {
						IInstanceNode node = (IInstanceNode)selected;
						currentCloudScope = node.getCloudScopeScope().getCloudScope();
						currentLocation = node.getLocationScope() != null ? node.getLocationScope().getLocation(): node.getCloudScopeScope().getLocation();
						for (INetwork network: currentLocation.getComputeResources().getNetworksWithInitializing()) {
							if (network.getAttachedInstances().contains(node.getInstance().getId())) {
								networks.add(network);
								break;
							}
						}
					} else if (selected instanceof IEntityNode) {
						IEntityNode entity = (IEntityNode)selected;
						currentCloudScope = entity.getCloudScopeScope().getCloudScope();
						currentLocation = entity.getLocationScope() != null ? entity.getLocationScope().getLocation(): entity.getCloudScopeScope().getLocation();
						for (INetwork network: currentLocation.getComputeResources().getNetworksWithInitializing()) {
							if (network.getId().equals(HinemosMessage.replace(entity.getName()))) {
								networks.add(network);
								break;
							}
						}
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
		} catch(Exception e) {
			currentCloudScope = null;
			currentLocation = null;
			networks = Collections.emptySet();
			
			logger.error(e.getMessage(), e);
			
			Display.getCurrent().asyncExec(new Runnable() {
				@Override
				public void run() {
					// 失敗報告ダイアログを生成
					ControlUtil.openError(e, msgErrorFinishRefreshView);
				}
			});
		}
		
		tableViewer.setInput(networks);
		getViewSite().getActionBars().updateActionBars();
		getViewSite().getActionBars().getToolBarManager().update(false);
		
		footerComposite.getRightControl().setText(strFooterTitle + networks.size());
	}
	
	protected Set<INetwork> collectNetworks(IScope target, Set<INetwork> networks) {
		for (IFacility facility: target.getFacilities()) {
			if (facility instanceof IInstanceNode) {
				IInstance instance = ((IInstanceNode)facility).getInstance();
				
				for (INetwork network: currentLocation.getComputeResources().getNetworksWithInitializing()) {
					if (network.getAttachedInstances().contains(instance.getId())) {
						networks.add(network);
						break;
					}
				}
			} else if (facility instanceof IScope) {
				collectNetworks((IScope)facility, networks);
			}
		}
		return networks;
	}
}
