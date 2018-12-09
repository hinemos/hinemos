/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.views;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.FacilityImageConstant;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.util.FacilityTreeCache;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.access.AccessEndpoint;
import com.clustercontrol.ws.access.HinemosUnknown_Exception;
import com.clustercontrol.ws.access.InvalidRole_Exception;
import com.clustercontrol.ws.access.InvalidUserPass_Exception;
import com.clustercontrol.ws.access.RoleInfo;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.base.ElementBaseModeWatch;
import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.model.cloud.IHinemosManager;
import com.clustercontrol.xcloud.model.cloud.ILoginUser;
import com.clustercontrol.xcloud.model.cloud.RoleRelation;
import com.clustercontrol.xcloud.util.CollectionComparator;
import com.clustercontrol.xcloud.util.TableViewerSorter;

/**
 */
public class RoleManagementView extends AbstractCloudViewPart implements CloudStringConstants {
	public static final String Id = "com.clustercontrol.xcloud.ui.views.RoleManagementView";
	
	private static final Log logger = LogFactory.getLog(RoleManagementView.class);
	
	private TableViewer tableViewer;
	private Label lblFooter;
	
	private List<IHinemosManager> managers;
	
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
								RoleManagementView.this.update();
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
	
//	private String footerTitle = bundle_messages.getString("word.view_item_count") + bundle_messages.getString("caption.title_separator");

	private TreeViewer treeViewer;
	private IHinemosManager currentManager;
	private RoleInfo currentRole;
	private Composite composite;
	
	private List<Object[]> loginUsers = new ArrayList<>(); 
	
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
			if (refreshTask == null) {
				refreshTask = new Runnable() {
					@Override
					public void run() {
						try {
							refresh(true);
						} finally {
							refreshTask = null;
						}
					}
				};
				Display.getCurrent().asyncExec(refreshTask);
			}
		}
	};
	
	protected Runnable refreshTask;
	
	protected ITreeContentProvider roleTreeContentProvider = new ITreeContentProvider() {
		public Object[] getChildren(Object element) {
			if (element instanceof IHinemosManager) {
				return getRoleInfos((IHinemosManager)element);
			}
			return null;
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof IHinemosManager) {
				return getRoleInfos((IHinemosManager)element).length != 0;
			}
			return false;	
		}

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof Object[]) {
				return (Object[]) inputElement;
			} else if (inputElement instanceof Collection) {
				return ((Collection<?>) inputElement).toArray();
			}
			return new Object[0];
		}

		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	};
	
	protected ILabelProvider roleLabelProvider = new LabelProvider() {
		@Override
		public Image getImage(Object element) {
			if (element instanceof IHinemosManager) {
				return FacilityImageConstant.typeToImage(FacilityConstant.TYPE_COMPOSITE, true);
			} else if (element instanceof HinemosRole) {
				return ClusterControlPlugin.getDefault().getImageRegistry().get(ClusterControlPlugin.IMG_ROLESETTING_ROLE);
			}
			return null;
		}
		@Override
		public String getText(Object element) {
			if (element instanceof IHinemosManager) {
				return ((IHinemosManager)element).getManagerName();
			} else if (element instanceof HinemosRole) {
				HinemosRole role = (HinemosRole)element;
				return String.format("%s (%s)", role.roleInfo.getRoleName(), role.roleInfo.getRoleId());
			}
			return element.toString();
		}
	};
	
	protected HinemosRole[] getRoleInfos(IHinemosManager manager) {
		HinemosRole[] roles = manager.getData("roles", HinemosRole[].class);
		if (roles == null) {
			try {
				List<RoleInfo> roleList = manager.getEndpoint(AccessEndpoint.class).getRoleInfoList();
				
				roles = new HinemosRole[roleList.size()];
				for (int i = 0; i < roleList.size(); ++i) {
					HinemosRole role = new HinemosRole();
					role.manager = manager;
					role.roleInfo = roleList.get(i);
					roles[i] = role;
				}
				manager.setData("roles", roles);
				return roles;
			} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception e) {
				logger.warn(e.getMessage(), e);
				return new HinemosRole[]{};
			}
		}
		return roles;
	}
	
	protected void updateRoleInfos(final IHinemosManager manager) {
		try {
			List<RoleInfo> newRoleList = manager.getEndpoint(AccessEndpoint.class).getRoleInfoList();
			HinemosRole[] roles = manager.getData("roles", HinemosRole[].class);
			final List<HinemosRole> oldRoleList = new ArrayList<>();
			if (roles != null)
				oldRoleList.addAll(Arrays.asList(roles));
			
			CollectionComparator.compareCollection(oldRoleList, newRoleList, new CollectionComparator.Comparator<HinemosRole, RoleInfo>(){
				@Override public boolean match(HinemosRole o1, RoleInfo o2) {
					return o1.roleInfo.getRoleId().equals(o2.getRoleId());
				}
				@Override public void matched(HinemosRole o1, RoleInfo o2) {
					o1.roleInfo.setRoleName(o2.getRoleName());
					o1.roleInfo.setCreateDate(o2.getCreateDate());
					o1.roleInfo.setCreateUserId(o2.getCreateUserId());
					o1.roleInfo.setDescription(o2.getDescription());
					o1.roleInfo.setModifyDate(o2.getModifyDate());
					o1.roleInfo.setModifyUserId(o2.getModifyUserId());
				}
				@Override public void afterO1(HinemosRole o1) {
					oldRoleList.remove(o1);
				}
				@Override public void afterO2(RoleInfo o2) {
					HinemosRole role = new HinemosRole();
					role.manager = manager;
					role.roleInfo = o2;
					oldRoleList.add(role);
				}
			});
			manager.setData("roles", oldRoleList.toArray(new HinemosRole[oldRoleList.size()]));
		} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception e) {
			logger.warn(e.getMessage(), e);
		}
	}
	
	public RoleManagementView() {
 		super();
	}
	
 	@Override
	protected void internalCreatePartControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);

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
		tableViewer = new TableViewer(composite_1, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
		Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		lblFooter.setText(strFooterTitle + 0);

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
		
		tableViewer.setContentProvider(new ArrayContentProvider());

		treeViewer = new TreeViewer(composite_2, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

		treeViewer.setContentProvider(roleTreeContentProvider);
		treeViewer.setLabelProvider(roleLabelProvider);
		
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				currentManager = null;
				currentRole = null;
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					if (!selection.isEmpty()) {
						Object selected = selection.getFirstElement();
						if (selected instanceof HinemosRole) {
							HinemosRole role = (HinemosRole)selected;
							currentManager = role.manager;
							currentRole = role.roleInfo;
						}
					}
					reflectTreeSelectionToView(selection);
				}
			}
		});
		// Sorting by Platform > Scope name
		tableViewer.setComparator(new ViewerComparator() {
			// Set sorting key by element type
			private String getSortingKey(Object element){
				return (element instanceof ViewData)? ((ViewData)element).getCloudPlatformName()+((ViewData)element).getCloudScopeName() : "";
			}

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return getSortingKey(e1).compareTo(getSortingKey(e2));
			}
		});

		this.getSite().setSelectionProvider(treeViewer);
		
		//Sashの境界を調整 左部40% 右部60%
		sash.setWeights(new int[] { 40, 60 });
		
		refresh(true);
	}
 	
	public void update() {
		refresh(true);
	}
	
	protected void refresh(boolean update) {
		managers = ClusterControlPlugin.getDefault().getHinemosManagers();
		Collections.sort(managers, new Comparator<IHinemosManager>() {
			@Override
			public int compare(IHinemosManager o1, IHinemosManager o2) {
				return o1.getManagerName().compareTo(o2.getManagerName());
			}
		});
		if (update) {
			
			List<Object[]> newLoginUsers = new ArrayList<>();
			
			try {
			
				for (IHinemosManager manager: managers) {
					manager.update();
					for (ICloudScope cloudscope: manager.getCloudScopes().getCloudScopes()) {
						cloudscope.getLoginUsers().update();
						for (ILoginUser user: cloudscope.getLoginUsers().getLoginUsers()) {
							newLoginUsers.add(new Object[]{manager, user});
						}
					}
					updateRoleInfos(manager);
				}
				
				CollectionComparator.compareCollection(loginUsers, newLoginUsers,
					new CollectionComparator.Comparator<Object[], Object[]>() {
					
						@Override
						public boolean match(Object[] o1, Object[] o2) {
							IHinemosManager man1 = (IHinemosManager)o1[0];
							IHinemosManager man2 = (IHinemosManager)o2[0];
							ILoginUser usr1 = (ILoginUser)o1[1];
							ILoginUser usr2 = (ILoginUser)o2[1];
							
							return man1.getManagerName().equals(man2.getManagerName()) &&
									usr1.getId().equals(usr2.getId());
						}
						@Override
						public void afterO1(Object[] o1) {
							loginUsers.remove(o1);
							((IHinemosManager)o1[0]).getModelWatch().removeWatcher((IElement)o1[1], watcher);
						}
						@Override
						public void afterO2(Object[] o2) {
							loginUsers.add(o2);
							((IHinemosManager)o2[0]).getModelWatch().addWatcher((IElement)o2[1], watcher);
						}
					});

			} catch (Exception e) {
				logger.error(e.getMessage(), e);

				String m = e.getMessage();
				if (m == null) {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					PrintStream ps = new PrintStream(bos, true);
					e.printStackTrace(ps);
					m = bos.toString();
				}
				
				final String message = m;
				Display.getCurrent().asyncExec(new Runnable() {
					@Override
					public void run() {
						// 失敗報告ダイアログを生成
						MessageDialog.openError(null, Messages.getString("failed"), message);
					}
				});
			}
			
		}
		treeViewer.setInput(managers);
		treeViewer.refresh();
		
		reflectTreeSelectionToView(treeViewer.getSelection());
	}

	private void reflectTreeSelectionToView(ISelection treeSelection) {
		//一覧にツリー選択の内容を反映
		if (treeSelection instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection)treeSelection;
			if (!selection.isEmpty()) {
				Object selected = selection.getFirstElement();
				if (selected instanceof HinemosRole) {
					
					List<ViewData> data = getViewData();
					tableViewer.setInput(getViewData());
					tableViewer.refresh();
					lblFooter.setText(strFooterTitle + data.size());
				}
			} else {
				tableViewer.setInput(null);
				lblFooter.setText(strFooterTitle + 0);
			}
		}
	}
	
	private static class ViewData {
		private String cloudScopedId;
		private String cloudPlatformName;
		private String cloudScopeName;
		private String assignedUser;
		private Object object;
		public void setCloudScopeId(String cloudScopedId){this.cloudScopedId = cloudScopedId;}
		public void setCloudPlatformName(String cloudPlatformName){this.cloudPlatformName = cloudPlatformName;}
		public void setCloudScopeName(String cloudScopeName){this.cloudScopeName = cloudScopeName;}
		public void setAssignedUser(String assignedUser){this.assignedUser = assignedUser;}
		public void setObject(Object object){this.object = object;}
		public String getCloudScopeId(){return cloudScopedId;}
		public String getCloudPlatformName(){return cloudPlatformName;}
		public String getCloudScopeName(){return cloudScopeName;}
		public String getAssignedUser(){return assignedUser;}
		@SuppressWarnings("unused")
		public Object getObject(){return object;}
	}
	
	private enum ViewColumn{
		cloud_platform(
			strCloudPlatform,
			new ColumnPixelData(150, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((ViewData)element).getCloudPlatformName();
				}
			}
		),
		cloud_scope_name(
			strCloudScopeName,
			new ColumnPixelData(200, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					ViewData data = (ViewData)element;
					return String.format("%s (%s)", data.getCloudScopeName(), data.getCloudScopeId());
				}
			}
		),
		login_user_attached_to_role(
			strAssignedUser,
			new ColumnPixelData(350, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((ViewData)element).getAssignedUser();
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
		public String getLabel() {return label;}
		public ColumnPixelData getPixelData() {return pixelData;}
		public ColumnLabelProvider getProvider() {return provider;}
	}

	@Override
	protected StructuredViewer getViewer() {
		return tableViewer;
	}
	
	private List<ViewData> getViewData() {
		List<ViewData> datas = new ArrayList<>();
		if(currentManager != null && currentRole != null){
			for(ICloudScope scope: currentManager.getCloudScopes().getCloudScopes()){
				ViewData viewData = new ViewData();
				viewData.setObject(scope);
				viewData.setCloudScopeId(scope.getId());
				viewData.setCloudPlatformName(scope.getCloudPlatform().getName());
				viewData.setCloudScopeName(scope.getName());
				
				for (ILoginUser user: scope.getLoginUsers().getLoginUsers()) {
					for(RoleRelation relation: user.getRoleRelations()){
						if(relation.getId().equals(currentRole.getRoleId())){
							StringBuilder sb = new StringBuilder();
							sb.append(user.getName())
							.append(" (")
							.append(user.getId())
							.append(")");
							
							if (scope.getAccountId().equals(user.getId()))
								sb.append(" [" + strMain + "]");
							
							viewData.setAssignedUser(sb.toString());
						}
					}
				}
				datas.add(viewData);
			}
		}
		return datas;
	}

	@Override
	public String getId() {
		return Id;
	}

	@Override
	public void dispose() {
		for (Object[] obj: loginUsers) {
			((IHinemosManager)obj[0]).getModelWatch().removeWatcher((IElement)obj[1], watcher);
		}
		
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
}
