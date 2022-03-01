/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.views;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.util.FacilityTreeCache;
import com.clustercontrol.util.TableViewerSorter;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.base.ElementBaseModeWatch;
import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.model.cloud.ICloudScopes;
import com.clustercontrol.xcloud.model.cloud.IHinemosManager;
import com.clustercontrol.xcloud.model.cloud.ILoginUser;
import com.clustercontrol.xcloud.util.CollectionComparator;
import com.clustercontrol.xcloud.util.ControlUtil;


/**
 */
public class LoginUsersView extends AbstractCloudViewPart implements CloudStringConstants {
	public static final String Id = "com.clustercontrol.xcloud.ui.views.LoginUsersView";
	
	private static final Log logger = LogFactory.getLog(LoginUsersView.class);
	
	private FacilityRootUpdateService service;
	
	private class FacilityRootUpdateService {
		private boolean disposed;
		private com.clustercontrol.composite.FacilityTreeComposite listener;

		public FacilityRootUpdateService() {
			listener = new com.clustercontrol.composite.FacilityTreeComposite(composite, SWT.None, null, null, false) {
				@Override
				public void update() {
					composite.getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							if (!disposed)
								LoginUsersView.this.update();
						}
					});
				}
				@Override
				public boolean isDisposed () {
					return false;
				}
				@Override
				protected void checkWidget() {
				}
			};
			listener.dispose();
			FacilityTreeCache.addComposite(listener);
		}

		public void dispose() {
			disposed = true;
			FacilityTreeCache.delComposite(listener);
		}
	}
	private Table table;
	private TableViewer tableViewer;
	private Label lblFooter;
	private Composite composite;
	
	private List<ICloudScopes> cloudScopeRoots = new ArrayList<>(); 
	
	protected ElementBaseModeWatch.AnyPropertyWatcher watcher = new ElementBaseModeWatch.AnyPropertyWatcher() {
		@Override
		public void elementAdded(ElementAddedEvent event) {
			refreshView();
		}

		@Override
		public void elementRemoved(ElementRemovedEvent event) {
			refreshView();
		}

		@Override
		public void propertyChanged(ValueChangedEvent event) {
			refreshView();
		}

		@Override
		public void unwatched(IElement owning, IElement owned) {
			refreshView();
		}
		
		public void refreshView() {
			registerRefreshTask();
		}
	};
	
	protected Runnable refreshTask;

	public LoginUsersView() {
 		super();
	}
	
 	@Override
	protected void internalCreatePartControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		GridData gridData;

		GridLayout gl_composite = new GridLayout(1, true);
		gl_composite.horizontalSpacing = 0;
		gl_composite.marginHeight = 0;
		gl_composite.marginWidth = 0;
		gl_composite.verticalSpacing = 0;
		composite.setLayout(gl_composite);
		
		Composite composite_1 = new Composite(composite, SWT.NONE);
		TableColumnLayout tcl_composite_1 = new TableColumnLayout();
		composite_1.setLayout(tcl_composite_1);
		
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		composite_1.setLayoutData(gridData);

		lblFooter = new Label(composite, SWT.NONE);
		lblFooter.setAlignment(SWT.RIGHT);
		lblFooter.setSize(lblFooter.getSize().x, 80);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		lblFooter.setLayoutData(gridData);
		lblFooter.setText(strFooterTitle + 0);
		
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

		this.getSite().setSelectionProvider(tableViewer);

		tableViewer.setContentProvider(new ArrayContentProvider());
		// Sorting by Manager > Scope name > Account
		tableViewer.setComparator(new ViewerComparator(){
			// Set sorting key by element type
			private String getSortingKey(Object element){
				if(element instanceof ICloudScope){
					ICloudScope castedElem = (ICloudScope)element;
					return castedElem.getCloudScopes().getHinemosManager().getManagerName()
							+ castedElem.getName()
							+ castedElem.getAccountId();
				}else
					return "";
			}

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return getSortingKey(e1).compareTo(getSortingKey(e2));
			}
		});

		try {
			for (IHinemosManager manager: ClusterControlPlugin.getDefault().getHinemosManagers()) {
				if (!manager.isInitialized()){
					//マネージャ毎に状態更新を行っているが、
					//マルチマネージャ接続時にクラウド/ＶＭが有効になってないマネージャの混在がありえる（endpoint通信で異常が出る）ので
					//異常発生時は該当の警告ログのみを表示する。
					try{
						manager.update();
					} catch(CloudModelException e) {
						logger.warn("internalCreatePartControl() . Failed to update the status of the manager's cloud function. Manager="+manager.getManagerName() );
					}
				}
			}
			
			List<ICloudScopes> newCloudScopes = new ArrayList<>();
			for (IHinemosManager manager: ClusterControlPlugin.getDefault().getHinemosManagers()) {
				newCloudScopes.add(manager.getCloudScopes());
			}
			
			CollectionComparator.compareCollection(cloudScopeRoots, newCloudScopes,
				new CollectionComparator.Comparator<ICloudScopes, ICloudScopes>() {
					@Override
					public boolean match(ICloudScopes o1, ICloudScopes o2) {
						return o1.getHinemosManager().getManagerName().equals(o1.getHinemosManager().getManagerName());
					}
					@Override
					public void afterO1(ICloudScopes o1) {
						cloudScopeRoots.remove(o1);
						o1.getHinemosManager().getModelWatch().removeWatcher(o1, watcher);
					}
					@Override
					public void afterO2(ICloudScopes o2) {
						cloudScopeRoots.add(o2);
						o2.getHinemosManager().getModelWatch().addWatcher(o2, watcher);
					}
				});
			
			refresh();
		} catch(Exception e) {
			logger.error(e.getMessage(), e);

			ControlUtil.openError(e, msgErrorFinishRefreshView);
		}
	}
 	
	public void update() {
		try {
			List<ICloudScopes> newCloudScopes = new ArrayList<>();
			for (IHinemosManager manager: ClusterControlPlugin.getDefault().getHinemosManagers()) {
				newCloudScopes.add(manager.getCloudScopes());
			}
			
			CollectionComparator.compareCollection(cloudScopeRoots, newCloudScopes,
				new CollectionComparator.Comparator<ICloudScopes, ICloudScopes>() {
					@Override
					public boolean match(ICloudScopes o1, ICloudScopes o2) {
						if(o1.getHinemosManager().getManagerName().equals(o1.getHinemosManager().getManagerName())){
							return o1.getCloudScopes().length == o2.getCloudScopes().length;
						} else {
							logger.info("update() : number of login managers changed.");
						}
						return false;
					}
					@Override
					public void afterO1(ICloudScopes o1) {
						cloudScopeRoots.remove(o1);
						o1.getHinemosManager().getModelWatch().removeWatcher(o1, watcher);
						registerRefreshTask();
					}
					@Override
					public void afterO2(ICloudScopes o2) {
						cloudScopeRoots.add(o2);
						o2.getHinemosManager().getModelWatch().addWatcher(o2, watcher);
						registerRefreshTask();
					}
				});
			
			for (IHinemosManager manager: ClusterControlPlugin.getDefault().getHinemosManagers()) {
				//マネージャ毎に状態更新を行っているが、
				//マルチマネージャ接続時にクラウド/ＶＭが有効になってないマネージャの混在がありえる（endpoint通信で異常が出る）ので
				//異常発生時は該当の警告ログのみを表示する。
				try{
					manager.update();
				} catch(CloudModelException e) {
					logger.warn("refresh() . Failed to update the status of the manager's cloud function. Manager="+manager.getManagerName() );
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			Display.getCurrent().asyncExec(new Runnable() {
				@Override
				public void run() {
					// 失敗報告ダイアログを生成
					ControlUtil.openError(e, msgErrorFinishRefreshView);
				}
			});
		}
	}
	
	protected void refresh() {
		List<ICloudScope> roots = new ArrayList<>();
		for (ICloudScopes cloudScopes: cloudScopeRoots) {
			roots.addAll(Arrays.asList(cloudScopes.getCloudScopes()));
		}
		
		Collections.sort(roots, new Comparator<ICloudScope>() {
			@Override
			public int compare(ICloudScope o1, ICloudScope o2) {
				int compare = o1.getCloudScopes().getHinemosManager().getManagerName().compareTo(o2.getCloudScopes().getHinemosManager().getManagerName());
				if (compare == 0) {
					return o1.getId().compareTo(o2.getId());
				}
				return compare;
			}
		});
		
		tableViewer.setInput(roots);
		tableViewer.refresh();
		lblFooter.setText(strFooterTitle + roots.size());
	}
	
	private enum ViewColumn{
		manager(
			strManager,
			new ColumnPixelData(150, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((ICloudScope)element).getCloudScopes().getHinemosManager().getManagerName();
				}
			}
		),
		cloud_platform(
			strCloudPlatform,
			new ColumnPixelData(150, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((ICloudScope)element).getCloudPlatform().getName();
				}
			}
		),
		cloud_scope_name(
			strCloudScope + "(" + strCloudScopeId + ")",
			new ColumnPixelData(200, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element){ 
					return ((ICloudScope)element).getName() + " (" + ((ICloudScope)element).getId() + ")";
				}
			}
		),
		cloud_admin_user_name(
			strAccount + "(" + strAccountId + ")",
			new ColumnPixelData(200, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					ICloudScope cloudScope = (ICloudScope)element;
					ILoginUser loginUser = cloudScope.getLoginUsers().getLoginUser(cloudScope.getAccountId());
					return String.format("%s (%s)", loginUser.getName(), loginUser.getId());
				}
			}
		),
		description(
			strDescription,
			new ColumnPixelData(200, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((ICloudScope)element).getDescription();
				}
			}
		),
		reg_user(
			strRegUser,
			new ColumnPixelData(100, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((ICloudScope)element).getRegUser();
				}
			}
		),
		reg_date(
			strRegDate,
			new ColumnPixelData(150, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					if(((ICloudScope)element).getRegDate() != null){
						return format.format(((ICloudScope)element).getRegDate());
					}
					return "";
				}
			}
		),
		update_user(
			strUpdateUser,
			new ColumnPixelData(100, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((ICloudScope)element).getUpdateUser();
				}
			}
		),
		update_date(
			strUpdateDate,
			new ColumnPixelData(150, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					if(((ICloudScope)element).getUpdateDate() != null){
						return format.format(((ICloudScope)element).getUpdateDate());
					}
					return "";
				}
			}
		);

		private String label;
		private ColumnLabelProvider provider;
		private ColumnPixelData pixelData;
		private static SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd H:mm:ss");
		
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
	protected StructuredViewer getViewer() {
		return tableViewer;
	}

	@Override
	public String getId() {
		return Id;
	}

	@Override
	public void dispose() {
		for (ICloudScopes root: cloudScopeRoots) {
			root.getHinemosManager().getModelWatch().removeWatcher(root, watcher);
		}
		cloudScopeRoots.clear();
		
		if (service != null)
			service.dispose();
		
		getSite().setSelectionProvider(null);
		
		super.dispose();
	}

	@Override
	public void setFocus() {
		super.setFocus();
		
		if (service == null)
			service = new FacilityRootUpdateService();
	}

	protected void registerRefreshTask() {
		if (refreshTask == null) {
			refreshTask = new Runnable() {
				@Override
				public void run() {
					try {
						refresh();
					} finally {
						refreshTask = null;
					}
				}
			};
			Display.getCurrent().asyncExec(refreshTask);
		}
	}
}
