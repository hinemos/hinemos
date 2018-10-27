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
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import com.clustercontrol.bean.FacilityImageConstant;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.extensions.CloudModelContentProviderExtension;
import com.clustercontrol.xcloud.extensions.ICloudModelContentProvider;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.model.cloud.IHinemosManager;
import com.clustercontrol.xcloud.model.cloud.ILocation;
import com.clustercontrol.xcloud.model.cloud.IServiceCondition;
import com.clustercontrol.xcloud.platform.PlatformDependent;
import com.clustercontrol.xcloud.ui.dialogs.DetailDialog;
import com.clustercontrol.xcloud.util.ControlUtil;
import com.clustercontrol.xcloud.util.TableViewerSorter;

/**
 */
public class ServiceStateView extends AbstractCloudViewPart implements CloudStringConstants {
	public static final String Id = "com.clustercontrol.xcloud.ui.views.ServiceStateView";
	
	private static final Log logger = LogFactory.getLog(ServiceStateView.class);
	
	protected ITreeContentProvider treeContentProvider = new ITreeContentProvider() {
		public Object[] getChildren(Object element) {
			if (element instanceof IHinemosManager) {
				return new Object[]{currentCloudScope};
			} if (element instanceof ICloudScope) {
				return ((ICloudScope)element).getLocations();
			}
			return null;
		}

		public Object getParent(Object element) {return null;}

		public boolean hasChildren(Object element) {
			if (element instanceof IHinemosManager) {
				return true;
			} else if (element instanceof ICloudScope) {
				return ((ICloudScope)element).getLocations().length != 0;
			}
			return false;	
		}

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof Object[]) {
				return (Object[]) inputElement;
			}
			if (inputElement instanceof Collection) {
				return ((Collection<?>) inputElement).toArray();
			}
			return new Object[0];
		}

		public void dispose() {}
		@Override public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
	};
	
	protected ILabelProvider treeLabelProvider = new LabelProvider() {
		@Override
		public Image getImage(Object element) {
			if (element instanceof IHinemosManager) {
				return FacilityImageConstant.typeToImage(FacilityConstant.TYPE_COMPOSITE, true);
			} else if (element instanceof ICloudScope) {
				Image defaultImage = FacilityImageConstant.typeToImage(FacilityConstant.TYPE_SCOPE, true);
				String platformId = ((ICloudScope)element).getPlatformId();
				ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(platformId);
				return provider.getImage(element, defaultImage);
			} else if (element instanceof ILocation) {
				Image defaultImage = FacilityImageConstant.typeToImage(FacilityConstant.TYPE_SCOPE, true);
				String platformId = ((ILocation)element).getCloudScope().getPlatformId();
				ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(platformId);
				return provider.getImage(element, defaultImage);
			}
			return null;
		}
		@Override
		public String getText(Object element) {
			if (element instanceof IHinemosManager) {
				return ((IHinemosManager)element).getManagerName();
			} else if (element instanceof ICloudScope) {
				ICloudScope cloudScope = (ICloudScope)element;
				String platformId = cloudScope.getPlatformId();
				ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(platformId);
				return HinemosMessage.replace(provider.getText(element, String.format("%s (%s)", cloudScope.getName(), cloudScope.getId())));
			} else if (element instanceof ILocation) {
				ILocation location = (ILocation)element;
				String platformId = location.getCloudScope().getPlatformId();
				ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(platformId);
				if (provider != null)
					return HinemosMessage.replace(provider.getText(element, location.getName()));
				return HinemosMessage.replace(location.getName());
			}
			return HinemosMessage.replace(element.toString());
		}
	};
	
	protected ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (!part.getSite().getId().equals(LoginUsersView.Id))
				return;
			updateTree(selection, false);
		}
	};
	
	private TreeViewer treeViewer;
	private TableViewer tableViewer;
	private Label lblFooter;
	
	private ICloudScope currentCloudScope;
	
	private Color unknown = new Color(Display.getCurrent(), new RGB(128, 192, 255));
	
	public ServiceStateView() {
 		super();
	}
	
 	@Override
	protected void internalCreatePartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);

		GridLayout gl_composite = new GridLayout(1, true);
		gl_composite.horizontalSpacing = 0;
		gl_composite.marginHeight = 0;
		gl_composite.marginWidth = 0;
		gl_composite.verticalSpacing = 0;
		composite.setLayout(gl_composite);

		SashForm sash = new SashForm(composite, SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		sash.setLayoutData(gridData);
		
		Composite composite_2 = new Composite(sash, SWT.NONE);
		TreeColumnLayout tcl_composite = new TreeColumnLayout();
		composite_2.setLayout(tcl_composite);

		Composite cmpTableSide = new Composite(sash, SWT.NONE);

		gl_composite = new GridLayout(1, true);
		gl_composite.horizontalSpacing = 0;
		gl_composite.marginHeight = 0;
		gl_composite.marginWidth = 0;
		gl_composite.verticalSpacing = 0;
		cmpTableSide.setLayout(gl_composite);
		
		Composite composite_1 = new Composite(cmpTableSide, SWT.NONE);
		TableColumnLayout tcl_composite_1 = new TableColumnLayout();
		composite_1.setLayout(tcl_composite_1);
		
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		composite_1.setLayoutData(gridData);

		lblFooter = new Label(cmpTableSide, SWT.NONE);
		lblFooter.setAlignment(SWT.RIGHT);
		lblFooter.setSize(lblFooter.getSize().x, 80);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		lblFooter.setLayoutData(gridData);
		lblFooter.setText(strFooterTitle + 0);

		tableViewer = new TableViewer(composite_1, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
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
					case 2:
						{
							IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
							IServiceCondition serviceCondition = (IServiceCondition)selection.getFirstElement();
							
							DetailDialog dialog = PlatformDependent.getPlatformDependent().createDetailDialog(table.getShell(), dlgServiceState);
							dialog.setInput(serviceCondition.getDetail());
							p = table.toDisplay(p);
							dialog.create();
							dialog.getShell().setLocation(p.x, p.y);
							dialog.open();
						}
						break;
					default:
						break;
					}
				}
			}
		});
		
		{
			TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			TableColumn tableColumn = tableViewerColumn.getColumn();
			tcl_composite_1.setColumnData(tableColumn, new ColumnPixelData(40, true, true));
			tableColumn.setText(strState);
			ColumnLabelProvider provider = new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					String status = ((IServiceCondition)element).getStatus();
					if(status.equalsIgnoreCase("normal")){
						return strNormal;
					} else if(status.equalsIgnoreCase("warn")){
						return strWarn;
					} else if(status.equalsIgnoreCase("abnormal")){
						return strError;
					}
					return strUnknown;
				}
				@Override
				public Color getBackground(Object element) {
					String status = ((IServiceCondition)element).getStatus();
					if(status.equalsIgnoreCase("normal")){
						return Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
					} else if(status.equalsIgnoreCase("warn")){
						return Display.getDefault().getSystemColor(SWT.COLOR_YELLOW);
					} else if(status.equalsIgnoreCase("abnormal")){
						return Display.getDefault().getSystemColor(SWT.COLOR_RED);
					}
					return unknown;
				}
			};
			tableViewerColumn.setLabelProvider(provider);
			tableColumn.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					tableViewer.setSorter(new TableViewerSorter(tableViewer, provider));
				}
			});
		}
		
		{
			TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			TableColumn tableColumn = tableViewerColumn.getColumn();
			tcl_composite_1.setColumnData(tableColumn, new ColumnPixelData(300, true, true));
			tableColumn.setText(strCloudServiceName);
			ColumnLabelProvider provider = new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((IServiceCondition)element).getName();
				}
			};
			tableViewerColumn.setLabelProvider(provider);
			tableColumn.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					tableViewer.setSorter(new TableViewerSorter(tableViewer, provider));
				}
			});
		}
		
		{
			TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			TableColumn tableColumn = tableViewerColumn.getColumn();
			tcl_composite_1.setColumnData(tableColumn, new ColumnPixelData(400, true, true));
			tableColumn.setText(strDetail);
			ColumnLabelProvider provider = new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((IServiceCondition)element).getDetail();
				}
			};
			tableViewerColumn.setLabelProvider(provider);
			tableColumn.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					tableViewer.setSorter(new TableViewerSorter(tableViewer, provider));
				}
			});
		}
		
		tableViewer.setContentProvider(new ArrayContentProvider());
		this.getSite().setSelectionProvider(tableViewer);

		treeViewer = new TreeViewer(composite_2, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		treeViewer.setContentProvider(treeContentProvider);
		treeViewer.setLabelProvider(treeLabelProvider);
		
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateTable(event.getSelection(), false);
			}
		});
		tableViewer.setComparator(new ViewerComparator() {
			// Set sorting key by element type
			private String getSortingKey(Object element){
				return (element instanceof IServiceCondition)? ((IServiceCondition)element).getName() : "";
			}

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return getSortingKey(e1).compareTo(getSortingKey(e2));
			}
		});
		treeViewer.setComparator(new ViewerComparator() {
			// Set sorting key by element type
			private String getSortingKey(Object element){
				if (element instanceof IHinemosManager) {
					return ((IHinemosManager)element).getManagerName();
				} else if (element instanceof ICloudScope) {
					ICloudScope castedElem = (ICloudScope)element;
					return castedElem.getName() + castedElem.getId();
				} else if (element instanceof ILocation) {
					ILocation castedElem = (ILocation)element;
					return castedElem.getName() + castedElem.getId();
				}else
					return "";
			}

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return getSortingKey(e1).compareTo(getSortingKey(e2));
			}
		});


		this.getSite().getPage().addSelectionListener(LoginUsersView.Id, selectionListener);
		
		//Sashの境界を調整 左部40% 右部60%
		sash.setWeights(new int[] { 40, 60 });
		
		update();
	}
 	
	protected void updateTree(ISelection selection, boolean update) {
		if (!(selection instanceof IStructuredSelection))
			return;

		IStructuredSelection sselection = (IStructuredSelection)selection;
		if (sselection.isEmpty()) {
			treeViewer.setInput(null);
			return;
		}
		
		currentCloudScope = null;
		
		Object selected = sselection.getFirstElement();
		if (selected instanceof ICloudScope) {
			currentCloudScope = (ICloudScope)selected;
			
			if (update)
				currentCloudScope.getCloudScopes().getHinemosManager().update();

			treeViewer.setInput(Arrays.asList(currentCloudScope.getCloudScopes().getHinemosManager()));
		}
		
		treeViewer.refresh();
		treeViewer.expandAll();
	}
 	
	protected void updateTable(ISelection selection, boolean update) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sselection = (IStructuredSelection)selection;
			
			List<IServiceCondition> serviceConditions = new ArrayList<>();
			if (!sselection.isEmpty()) {
				Object selected = sselection.getFirstElement();
				try {
					if (selected instanceof ICloudScope) {
						ICloudScope cloudScope = (ICloudScope)selected;
						if (update)
							cloudScope.updateServiceConditions();

						serviceConditions.addAll(Arrays.asList(cloudScope.getServiceConditionsWithInitializing()));
					} else if (selected instanceof ILocation) {
						ILocation location = (ILocation)selected;
						if (update)
							location.updateServiceConditions();
						
						serviceConditions.addAll(Arrays.asList(location.getServiceConditionsWithInitializing()));
					}
				} catch (CloudModelException e) {
					logger.warn(e.getMessage(), e);
					ControlUtil.openError(e, msgErrorGetPlatformServiceConditions);
				}
			}
			tableViewer.setInput(serviceConditions);
			tableViewer.refresh();
			lblFooter.setText(strFooterTitle + serviceConditions.size());
		}
	}

	public void update() {
		updateTree(getSite().getPage().getSelection(LoginUsersView.Id), true);
		updateTable(treeViewer.getSelection(), true);
	}
	
	public void refresh() {
		updateTree(getSite().getPage().getSelection(LoginUsersView.Id), false);
		updateTable(treeViewer.getSelection(), false);
	}

	@Override
	protected StructuredViewer getViewer() {
		return tableViewer;
	}

	@Override
	public void dispose(){
		getSite().getPage().removeSelectionListener(LoginUsersView.Id, selectionListener);
		getSite().setSelectionProvider(null);
		unknown.dispose();
		super.dispose();
	}

	@Override
	public String getId() {
		return Id;
	}
}
