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
import java.util.Locale;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.openapitools.client.model.MonitorInfoResponse;

import com.clustercontrol.bean.RunInterval;
import com.clustercontrol.monitor.run.bean.MonitorTypeMessage;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TableViewerSorter;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.base.ElementBaseModeWatch;
import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.cloud.BillingMonitor;
import com.clustercontrol.xcloud.model.cloud.IBillingMonitors;
import com.clustercontrol.xcloud.model.cloud.IHinemosManager;

/**
 */
public class BillingAlarmsView extends AbstractCloudViewPart {
	public static final String Id = BillingAlarmsView.class.getName();
	
	private static final Log logger = LogFactory.getLog(BillingAlarmsView.class);
	
	protected ElementBaseModeWatch.AnyPropertyWatcher watcher = new Watcher<BillingMonitor>(){
		@Override protected void asyncRefresh() {
			BillingAlarmsView.this.refresh();
		}
		@Override
		protected void unwatchedOwner(IElement owning, IElement owned) {
			currentBillingAlarms = null;
			tableViewer.setInput(null);
		}
	};
	
	protected ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (!part.getSite().getId().equals(CloudScopesView.Id))
				return;
			refresh(selection);
		}
	};
	
	private Table table;
	private TableViewer tableViewer;
	private Label lblFooder;
	
	private IBillingMonitors currentBillingAlarms;
	 

	private enum ViewColumn{
		manager_name(
			Messages.getString("facility.manager", "Manager", Locale.getDefault()),
			new ColumnPixelData(100, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((BillingMonitor)element).getBillingMonitors().getHinemosManager().getManagerName();
				}
			}
		),
		billing_monitor_id(
			Messages.getString("monitor.id", Locale.getDefault()),
			new ColumnPixelData(100, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((BillingMonitor)element).getMonitorInfo().getMonitorId();
				}
			}
		),
		plugin_id(
			Messages.getString("plugin.id",Locale.getDefault()),
			new ColumnPixelData(100, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((BillingMonitor)element).getMonitorInfo().getMonitorTypeId();
				}
			}
		),
		monitor_type(
			Messages.getString("monitor.type",Locale.getDefault()),
			new ColumnPixelData(100, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					String code = ((BillingMonitor)element).getMonitorInfo().getMonitorType().toString();
					return MonitorTypeMessage.codeToString(code);
				}
			}
		),
		description(
			Messages.getString("description",Locale.getDefault()),
			new ColumnPixelData(100, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((BillingMonitor)element).getMonitorInfo().getDescription();
				}
			}
		),
		facility_id(
			Messages.getString("facility.id",Locale.getDefault()),
			new ColumnPixelData(100, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((BillingMonitor)element).getMonitorInfo().getFacilityId();
				}
			}
		),
		scope(
			Messages.getString("scope",Locale.getDefault()),
			new ColumnPixelData(100, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return HinemosMessage.replace(((BillingMonitor)element).getMonitorInfo().getScope());
				}
			}
		),
		calendar(
			Messages.getString("calendar",Locale.getDefault()),
			new ColumnPixelData(100, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					if (((BillingMonitor)element).getMonitorInfo().getCalendarId() != null) {
						return ((BillingMonitor)element).getMonitorInfo().getCalendarId();
					} else {
						return "";
					}
				}
			}
		),
		run_interval(
			Messages.getString("run.interval",Locale.getDefault()),
			new ColumnPixelData(100, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					MonitorInfoResponse info = (MonitorInfoResponse)((BillingMonitor)element).getMonitorInfo();
					return RunInterval.enumToString(
							info.getRunInterval(), MonitorInfoResponse.RunIntervalEnum.class);
				}
			}
		),
		monitor_valid_name(
			Messages.getString("monitor.valid.name",Locale.getDefault()),
			new ColumnPixelData(100, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					if (((BillingMonitor)element).getMonitorInfo().getMonitorFlg()) {
						return Messages.getString("valid");
					} else {
						return Messages.getString("invalid");
					}
				}
			}
		),
		collector_valid_name(
			Messages.getString("collect",Locale.getDefault()),
			new ColumnPixelData(100, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					if (((BillingMonitor)element).getMonitorInfo().getCollectorFlg()) {
						return Messages.getString("valid");
					} else {
						return Messages.getString("invalid");
					}
				}
			}
		),
		owner_role_id(
			Messages.getString("owner.role.id",Locale.getDefault()),
			new ColumnPixelData(100, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((BillingMonitor)element).getMonitorInfo().getOwnerRoleId();
				}
			}
		),
		reg_date(
			Messages.getString("create.time",Locale.getDefault()),
			new ColumnPixelData(100, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					if(((BillingMonitor)element).getMonitorInfo().getRegDate() != null){
						return ((BillingMonitor)element).getMonitorInfo().getRegDate();
					}
					return "----/--/--";
				}
			}
		),
		reg_user(
			Messages.getString("creator.name",Locale.getDefault()),
			new ColumnPixelData(100, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((BillingMonitor)element).getMonitorInfo().getRegUser();
				}
			}
		),
		update_date(
			Messages.getString("update.time",Locale.getDefault()),
			new ColumnPixelData(100, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					if(((BillingMonitor)element).getMonitorInfo().getUpdateDate() != null){
						return ((BillingMonitor)element).getMonitorInfo().getUpdateDate();
					}
					return "----/--/--";
				}
			}
		),
		update_user(
			Messages.getString("modifier.name",Locale.getDefault()),
			new ColumnPixelData(100, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((BillingMonitor)element).getMonitorInfo().getUpdateUser();
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

 	public BillingAlarmsView() {
 		super();
	}

	@Override
	protected void internalCreatePartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		
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
		
		GridLayout gl_composite = new GridLayout(1, true);
		gl_composite.horizontalSpacing = 0;
		gl_composite.marginHeight = 0;
		gl_composite.marginWidth = 0;
		gl_composite.verticalSpacing = 0;
		composite.setLayout(gl_composite);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		composite_1.setLayoutData(gridData);

		lblFooder = new Label(composite, SWT.NONE);
		lblFooder.setAlignment(SWT.RIGHT);
		lblFooder.setSize(lblFooder.getSize().x, 80);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		lblFooder.setLayoutData(gridData);

		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setComparator(new ViewerComparator(){
			// Set sorting key by element type
			private String getSortingKey(Object element){
				if (element instanceof MonitorInfoResponse) {
					return ((MonitorInfoResponse)element).getMonitorId();
				} else {
					return "";
				}
			}

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return getSortingKey(e1).compareTo(getSortingKey(e2));
			}
		});

		getSite().setSelectionProvider(tableViewer);
		
		getSite().getPage().addSelectionListener(CloudScopesView.Id, selectionListener);
	}
	
	@Override
	public void dispose() {
		if (currentBillingAlarms != null)
			currentBillingAlarms.getHinemosManager().getModelWatch().removeWatcher(currentBillingAlarms, watcher);

		getSite().getPage().removeSelectionListener(CloudScopesView.Id, selectionListener);
		getSite().setSelectionProvider(null);
		super.dispose();
	}

	@Override
	protected StructuredViewer getViewer() {
		return tableViewer;
	}

	@Override
	public String getId() {
		return Id;
	}
	
	@Override
	public void update() {
		if (currentBillingAlarms != null) {
			try {
				currentBillingAlarms.updateBillingMonitors();
			} catch (CloudModelException e) {
				logger.warn(e.getMessage(), e);
				currentBillingAlarms = null;
			}
		}
	}

	protected void refresh() {
		for (IViewReference ref: getSite().getPage().getViewReferences()) {
			if (CloudScopesView.Id.equals(ref.getId())) {
				IViewPart part = ref.getView(false);
				if (part != null) {
					refresh(part.getViewSite().getSelectionProvider().getSelection());
				}
				break;
			}
		}
	}

	protected void refresh(ISelection selection) {
		if (currentBillingAlarms != null) {
			try {
				currentBillingAlarms.getHinemosManager().getModelWatch().removeWatcher(currentBillingAlarms, watcher);
			} catch (CloudModelException e) {
				logger.warn(e.getMessage(), e);
			}
		}

		currentBillingAlarms = null;

		List<BillingMonitor> alarms = new ArrayList<>();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sselection = (IStructuredSelection)selection;
			if (!sselection.isEmpty()) {
				Object selected = sselection.getFirstElement();
				if (selected instanceof IElement) {
					currentBillingAlarms = ((IHinemosManager)((IElement)selected).getAdapter(IHinemosManager.class)).getBillingAlarms();
				}
				
				if (currentBillingAlarms != null) {
					alarms = Arrays.asList(currentBillingAlarms.getBillingMonitorsWithInitializing());

					try {
						currentBillingAlarms.getHinemosManager().getModelWatch().addWatcher(currentBillingAlarms, watcher);
					} catch (CloudModelException e) {
						logger.warn(e.getMessage(), e);
					}
				} else {
					currentBillingAlarms = null;
				}
			}
		}
		
		tableViewer.setInput(alarms);
		getViewSite().getActionBars().updateActionBars();
		getViewSite().getActionBars().getToolBarManager().update(false);
		if (currentBillingAlarms == null) {
			lblFooder.setText(CloudStringConstants.strFooterTitle + 0);
		} else {
			lblFooder.setText(CloudStringConstants.strFooterTitle + currentBillingAlarms.getBillingMonitors().length);
		}

	}
}
