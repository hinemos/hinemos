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
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.util.FacilityTreeCache;
import com.clustercontrol.ws.repository.FacilityTreeItem;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.extensions.ICloudModelContentProvider;
import com.clustercontrol.xcloud.extensions.CloudModelContentProviderExtension;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.base.ElementBaseModeWatch;
import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.model.cloud.IHinemosManager;
import com.clustercontrol.xcloud.model.cloud.IInstance;
import com.clustercontrol.xcloud.model.cloud.ILocation;
import com.clustercontrol.xcloud.model.repository.ICloudScopeScope;
import com.clustercontrol.xcloud.model.repository.IFacility;
import com.clustercontrol.xcloud.model.repository.IInstanceNode;
import com.clustercontrol.xcloud.model.repository.IScope;
import com.clustercontrol.xcloud.platform.PlatformDependent;
import com.clustercontrol.xcloud.plugin.CloudOptionSourceProvider;
import com.clustercontrol.xcloud.ui.dialogs.DetailDialog;
import com.clustercontrol.xcloud.util.CloudUtil;
import com.clustercontrol.xcloud.util.TableViewerSorter;


/**
 */
public class InstancesView extends AbstractCloudViewPart implements CloudStringConstants {
	public static final String Id = "com.clustercontrol.xcloud.ui.views.InstancesView";
	
	private static final Log logger = LogFactory.getLog(InstancesView.class);
	
	protected ElementBaseModeWatch.AnyPropertyWatcher watcher = new Watcher<IInstance>(){
		@Override protected void asyncRefresh() {
			InstancesView.this.refresh();
		}
		@Override
		protected void unwatchedOwner(IElement owning, IElement owned) {
			currentCloudScope = null;
			currentLocation = null;
			tableViewer.setInput(null);
		}
	};
	
	protected FooterComposite footerComposite;
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

	protected static Set<IInstance> collectInstances(IScope target, Set<IInstance> instances) {
		for (IFacility facility: target.getFacilities()) {
			if (facility instanceof IInstanceNode) {
				instances.add(((IInstanceNode)facility).getInstance());
			} else if (facility instanceof IScope) {
				collectInstances((IScope)facility, instances);
			}
		}
		return instances;
	}

	public InstancesView() {
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
		final Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				Point p =new Point(e.x, e.y);
				ViewerCell cell = tableViewer.getCell(p);
				if (cell != null) {
					switch (cell.getColumnIndex()) {
					case 4:
						{
							IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
							IInstance instance = (IInstance)selection.getFirstElement();
							
							StringBuilder sb = new StringBuilder();
							for (String ip: instance.getIpAddresses()) {
								sb.append(ip);
								sb.append("\n");
							}
							
							DetailDialog dialog = PlatformDependent.getPlatformDependent().createDetailDialog(table.getShell(), dlgCompute);
							dialog.setInput(sb.toString());
							dialog.create();
							dialog.open();
						}
						break;
					case 7:
						{
							IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
							final IInstance instance = (IInstance)selection.getFirstElement();
							
							IHinemosManager manager = instance.getLocation().getCloudScope().getCloudScopes().getHinemosManager();
							FacilityTreeItem treeItem =FacilityTreeCache.getTreeItem(manager.getManagerName());
							
							final List<FacilityTreeItem> parents = new ArrayList<>();
							CloudUtil.walkFacilityTree(treeItem, new CloudUtil.IFacilityTreeVisitor() {
								@Override
								public void visitTreeItem(FacilityTreeItem item) {
									for (IInstanceNode node: instance.getCounterNodes()) {
										if (
											item.getData() != null &&
											item.getData().getFacilityId() != null &&
											item.getData().getFacilityId().equals(node.getParent().getFacilityId())) {
											parents.add(item);
										}
									}
								}
							} );
							
							FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());

							StringBuilder sb = new StringBuilder();
							for (FacilityTreeItem parent: parents) {
								sb.append(path.getPath(parent));
								sb.append("\n");
							}

							DetailDialog dialog = PlatformDependent.getPlatformDependent().createDetailDialog(table.getShell(), dlgCompute);
							dialog.setInput(sb.toString());
							dialog.create();
							dialog.open();
						}
						break;
					default:
//						IServiceLocator serviceLocator = PlatformUI.getWorkbench();
//						ICommandService commandService = (ICommandService) serviceLocator.getService(ICommandService.class);
//
//						try  { 
//							Command command = commandService.getCommand("com.clustercontrol.xcloud.ui.instance.modify.config");
//							IEvaluationService evaluationService = (IEvaluationService)getViewSite().getService(IEvaluationService.class);
//							try {
//							Command theCommand = commandService.getCommand("com.foo.the.command");
//							theCommand.executeWithChecks(new ExecutionEvent(theCommand, new HashMap(), control, evaluationService.getCurrentState()));
//						} catch (ExecutionException | NotEnabledException | NotHandledException | NotDefinedException e1) {
//							e1.printStackTrace();
//						}
						break;
					}
				}
			}
		});

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
		footerComposite.setSize(footerComposite.getRightControl().getSize().x, 80);
		footerComposite.setLayoutData(gridData);

		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setComparator(new ViewerComparator() {
			// Set sorting key by element type
			private String getSortingKey(Object element){
				return (element instanceof IInstance)? ((IInstance)element).getId() : "";
			}

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return getSortingKey(e1).compareTo(getSortingKey(e2));
			}
		});

		this.getSite().setSelectionProvider(tableViewer);
		getSite().getPage().addSelectionListener(RepositoryView.Id, selectionListener);

		footerComposite.getRightControl().setText(strFooterTitle + 0);
	}
	
	@Override
	protected StructuredViewer getViewer() {
		return tableViewer;
	}
	
	private enum ViewColumn{
		instance_state(
			strState,
			new ColumnPixelData(40, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return "";
				}
				@Override
				public Image getImage(Object element) {
					switch(((IInstance)element).getStatus()) {
					case "running":
						return runImage;
					case "stopped":
						return stopImage;
					case "suspend":
						return suspendImage;
					case "terminated":
						return terminatedImage;
					default:
						return changeImage;
					}
				}
			}
		),
		instance_state_detail(
			strStateDetail,
			new ColumnPixelData(84, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((IInstance)element).getStatus();
				}
			}
		),
		instance_id(
			strComputeId,
			new ColumnPixelData(160, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((IInstance)element).getId();
				}
			}
		),
		instance_name(
			strComputeName,
			new ColumnPixelData(160, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((IInstance)element).getName();
				}
			}
		),
		ipAddress(
			strIpAddress,
			new ColumnPixelData(150, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					if(((IInstance)element).getIpAddresses() != null){
						StringBuffer sb = new StringBuffer();
						for(String ip: ((IInstance)element).getIpAddresses()){
							sb.append(ip + ", ");
						}
						if(sb.length() > 1){
							sb.setLength(sb.length() - 2);
						}
						return sb.toString();
					}
					return "";
				}
			}
		),
		facility_id(
			strFacilityId,
			new ColumnPixelData(260, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((IInstance)element).getFacilityId();
				}
			}
		),
		facility_name(
			strFacilityName,
			new ColumnPixelData(160, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					IInstance instance = (IInstance)element;
					List<FacilityTreeItem> items = CloudUtil.collectScopes(instance.getCloudScope().getCloudScopes().getHinemosManager().getManagerName(), ((IInstance)element).getFacilityId());
					if (!items.isEmpty()) {
						return items.get(0).getData().getFacilityName();
					} else {
						return "";
					}
				}
			}
		),
		path(
			strFacilityPath,
			new ColumnPixelData(400, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					final IInstance instance = (IInstance)element;
					
					IHinemosManager manager = instance.getLocation().getCloudScope().getCloudScopes().getHinemosManager();
					FacilityTreeItem treeItem =FacilityTreeCache.getTreeItem(manager.getManagerName());
					
					final List<FacilityTreeItem> parents = new ArrayList<>();
					CloudUtil.walkFacilityTree(treeItem, new CloudUtil.IFacilityTreeVisitor() {
						@Override
						public void visitTreeItem(FacilityTreeItem item) {
							for (IInstanceNode node: instance.getCounterNodes()) {
								if (
									item.getData() != null &&
									item.getData().getFacilityId() != null &&
									item.getData().getFacilityId().equals(node.getParent().getFacilityId())) {
									parents.add(item);
								}
							}
						}
					} );
					
					FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());

					StringBuilder sb = new StringBuilder();
					for (FacilityTreeItem parent: parents) {
						sb.append(path.getPath(parent));
						sb.append(" ");
					}
					
					return sb.toString();
				}
			}
		);

		private static Image runImage = ClusterControlPlugin.getDefault().getImageRegistry().getDescriptor("running2").createImage();
		private static Image stopImage = ClusterControlPlugin.getDefault().getImageRegistry().getDescriptor("stopped2").createImage();
		private static Image suspendImage = ClusterControlPlugin.getDefault().getImageRegistry().getDescriptor("suspended2").createImage();
		private static Image terminatedImage = ClusterControlPlugin.getDefault().getImageRegistry().getDescriptor("terminated").createImage();
		private static Image changeImage = ClusterControlPlugin.getDefault().getImageRegistry().getDescriptor("processing").createImage();
		
		private String label;
		private ColumnLabelProvider provider;
		private ColumnPixelData pixelData;
//		private static SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd H:mm:ss");
		
		
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
			try {
				currentLocation.updateLocation();
			} catch (CloudModelException e) {
				logger.warn(e.getMessage(), e);
				currentCloudScope = null;
				currentLocation = null;
			}
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
		
		List<IInstance> instancesList = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sselection = (IStructuredSelection)selection;
			if (!sselection.isEmpty()) {
				Set<IInstance> instances = new TreeSet<>(new Comparator<IInstance>() {
					@Override
					public int compare(IInstance o1, IInstance o2) {
						return o1.getId().compareTo(o2.getId());
					}
				});
				
				Object selected = sselection.getFirstElement();
				if (selected instanceof ICloudScopeScope) {
					ICloudScopeScope cloudScopeScope = (ICloudScopeScope)selected;
					if (cloudScopeScope.getLocation() != null) {
						ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(cloudScopeScope.getCloudScope().getPlatformId());
						instances.addAll((List<IInstance>)Arrays.asList(provider.getChildren(selected, cloudScopeScope.getLocation().getComputeResources().getInstances())));
						collectInstances(cloudScopeScope, instances);
					}
				} else if (selected instanceof IScope) {
					IScope scope = (IScope)selected;
					ICloudScopeScope cloudScopeScope = scope.getCloudScopeScope();
					if (cloudScopeScope != null) {
						ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(cloudScopeScope.getCloudScope().getPlatformId());
						ILocation location = scope.getLocationScope() == null ? scope.getCloudScopeScope().getLocation(): scope.getLocationScope().getLocation();
						instances.addAll((List<IInstance>)Arrays.asList(provider.getChildren(selected, location.getComputeResources().getInstances())));
						collectInstances(scope, instances);
					}
				} else if (selected instanceof IInstanceNode) {
					IInstanceNode node = (IInstanceNode)selected;
					instances.add(node.getInstance());
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
					}
				} else {
					currentCloudScope = null;
					currentLocation = null;
				}
				instancesList = new ArrayList<IInstance>(instances);
			}
		}

		tableViewer.setInput(instancesList);
		getViewSite().getActionBars().updateActionBars();
		getViewSite().getActionBars().getToolBarManager().update(false);
		
		footerComposite.getRightControl().setText(strFooterTitle + (instancesList != null ? instancesList.size(): 0));
	}
	
	@Override
	public void setFocus() {
		super.setFocus();
		CloudOptionSourceProvider.setActiveHinemosManagerToProvider(currentCloudScope != null ? currentCloudScope.getCloudScopes().getHinemosManager(): null);
		CloudOptionSourceProvider.setActiveCloudScopeToProvider(currentCloudScope);
		CloudOptionSourceProvider.setActiveLocationToProvider(currentLocation);
	}
}
