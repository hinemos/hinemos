/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.views;

import static com.clustercontrol.xcloud.common.CloudConstants.bundle_messages;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.FacilityImageConstant;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.util.FacilityTreeCache;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.extensions.ICloudModelContentProvider;
import com.clustercontrol.xcloud.extensions.CloudModelContentProviderExtension;
import com.clustercontrol.xcloud.model.base.ElementBaseModeWatch;
import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.model.cloud.ICloudScopes;
import com.clustercontrol.xcloud.model.cloud.IHinemosManager;
import com.clustercontrol.xcloud.plugin.CloudOptionSourceProvider;
import com.clustercontrol.xcloud.util.CollectionComparator;
import com.clustercontrol.xcloud.util.ControlUtil;
import com.clustercontrol.xcloud.util.TableViewerSorter;


public class CloudScopesView extends AbstractCloudViewPart {
	public static final String Id = "com.clustercontrol.xcloud.ui.views.CloudScopesView";
	
	private static final Log logger = LogFactory.getLog(CloudScopesView.class);

	protected Runnable refreshTask;
	
	protected IHinemosManager currentHinemosManager = null;
	protected ICloudScope currentScope = null;

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
								CloudScopesView.this.update();
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
	
	protected class CloudScopeRoot {
		private List<ICloudScopes> cloudScopes = new ArrayList<>();

		public List<ICloudScopes> getCloudScopes() {
			return cloudScopes;
		}

		public void update(boolean initialize) {
			List<IHinemosManager> managers = ClusterControlPlugin.getDefault().getHinemosManagers();

			List<ICloudScopes> newCloudScopes = new ArrayList<>();
			for (IHinemosManager m: managers) {
				try {
					if (!initialize || (initialize && !m.isInitialized()))
						m.update();
					newCloudScopes.add(m.getCloudScopes());
				} catch (Exception e) {
					logger.warn(e.getMessage(), e);
					ControlUtil.openError(e, CloudStringConstants.msgErrorFinishRefreshView);
				}
			}
			
			CollectionComparator.compareCollection(cloudScopes, newCloudScopes, new CollectionComparator.Comparator<ICloudScopes, ICloudScopes>() {
				@Override
				public boolean match(ICloudScopes o1, ICloudScopes o2) {
					return o1.getHinemosManager().getManagerName().equals(o2.getHinemosManager().getManagerName());
				}
				@Override
				public void afterO1(ICloudScopes o1) {
					o1.getHinemosManager().getModelWatch().removeWatcher(o1, watcher);
					cloudScopes.remove(o1);
				}
				@Override
				public void afterO2(ICloudScopes o2) {
					o2.getHinemosManager().getModelWatch().addWatcher(o2, watcher);
					cloudScopes.add(o2);
				}
			});
			
			if (refreshTask == null) {
				refreshTask = new Runnable() {
					@Override
					public void run() {
						refresh();
						refreshTask = null;
					}
				};
				Display.getCurrent().asyncExec(refreshTask);
			}
		}
	}

	private static class FacilityTreeContentProvider implements ITreeContentProvider{
		public Object[] getChildren(Object element) {
			if (element instanceof ICloudScopes) {
				List<ICloudScope> scopes = new ArrayList<>();
				for (ICloudScope scope: ((ICloudScopes)element).getCloudScopes()) {
					if (scope.getCloudPlatform().getCloudSpec().isBillingAlarmEnabled())
						scopes.add(scope);
				}
				return scopes.toArray();
			}
			return null;
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof ICloudScopes) {
				return ((ICloudScopes)element).getCloudScopes().length != 0;
			}
			return false;	
		}

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof CloudScopeRoot) {
				return ((CloudScopeRoot)inputElement).getCloudScopes().toArray();
			}
			return new Object[]{};
		}

		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	protected ElementBaseModeWatch.AnyPropertyWatcher watcher = new Watcher<ICloudScope>(){
		@Override protected void asyncRefresh() {
			CloudScopesView.this.refresh();
		}
		@Override
		protected void unwatchedOwner(IElement owning, IElement owned) {
			currentHinemosManager = null;
			currentScope = null;
			treeViewer.setInput(null);
		}
	};

	private CloudScopeRoot root = new CloudScopeRoot();
	private TreeViewer treeViewer;
	private Composite composite;

	public CloudScopesView() {
		super();
	}

	@Override
	protected void internalCreatePartControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));

		Composite composite_1 = new Composite(composite, SWT.NONE);
		TreeColumnLayout tcl_composite = new TreeColumnLayout();
		composite_1.setLayout(tcl_composite);

		treeViewer = new TreeViewer(composite_1, SWT.BORDER);
		Tree tree = treeViewer.getTree();
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);

		treeViewer.setContentProvider(new FacilityTreeContentProvider());
		
		for (final ViewColumn column: ViewColumn.values()) {
			TreeViewerColumn treeViewerColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
			TreeColumn trclmn = treeViewerColumn.getColumn();
			trclmn.setText(column.getLabel());
			treeViewerColumn.setLabelProvider(column.getProvider());
			tcl_composite.setColumnData(trclmn, column.getPixelData());
			trclmn.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					treeViewer.setSorter(new TableViewerSorter(treeViewer, column.getProvider()));
				}
			});
		}
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {	
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sselection = (IStructuredSelection)event.getSelection();
				if (sselection.isEmpty()) {
					CloudOptionSourceProvider.setActiveCloudScopeToProvider(null);
					return;
				}
				
				currentHinemosManager = null;
				currentScope = null;
				
				Object selected = sselection.getFirstElement();
				if (selected instanceof IElement) {
					IElement scope = (IElement)selected;
					currentHinemosManager = (IHinemosManager)scope.getAdapter(IHinemosManager.class);
					currentScope = (ICloudScope)scope.getAdapter(ICloudScope.class);
				}

				CloudOptionSourceProvider.setActiveHinemosManagerToProvider(currentHinemosManager);
				CloudOptionSourceProvider.setActiveCloudScopeToProvider(currentScope);
				CloudOptionSourceProvider.setActiveLocationToProvider(null);
			}
		});
		this.getSite().setSelectionProvider(treeViewer);

		treeViewer.setInput(root);
		treeViewer.setComparator(new ViewerComparator(){
			// Set sorting key by element type
			private String getSortingKey(Object element){
				if (element instanceof ICloudScopes)
					return ((ICloudScopes)element).getHinemosManager().getManagerName();
				else if (element instanceof ICloudScope) {
					ICloudScope cloudScope = (ICloudScope)element;
					return cloudScope.getName() + cloudScope.getId();
				}else
					return "";
			}

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return getSortingKey(e1).compareTo(getSortingKey(e2));
			}
		});

		try {
			root.update(true);
		} catch(Exception e) {
			logger.error(e.getMessage(), e);

			ControlUtil.openError(e, CloudStringConstants.msgErrorFinishRefreshView);
		}
	}

	private enum ViewColumn{
		id(
			bundle_messages.getString(""),
			new ColumnPixelData(300, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					if (element instanceof ICloudScopes) {
						return ((ICloudScopes)element).getHinemosManager().getManagerName();
					} else if (element instanceof ICloudScope) {
						ICloudScope cloudScope = (ICloudScope)element;
						return cloudScope.getName() + " (" + cloudScope.getId() + ")";
					}
					return element.toString();
				}
				@Override
				public Image getImage(Object element) {
					if (element instanceof ICloudScopes) {
						return FacilityImageConstant.typeToImage(FacilityConstant.TYPE_COMPOSITE, true);
					} else if (element instanceof ICloudScope) {
						Image defaultImage = FacilityImageConstant.typeToImage(FacilityConstant.TYPE_SCOPE, true);
						String platformId = ((ICloudScope)element).getPlatformId();
						ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(platformId);
						return provider.getImage(element, defaultImage);
					}
					return null;
				}
			}
		),
		RetentionPeriod(
			bundle_messages.getString("word.billing_detail_collection"),
			new ColumnPixelData(100, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					if (element instanceof ICloudScope) {
						if (((ICloudScope)element).getBillingDetailCollectorFlg() != null  && ((ICloudScope)element).getBillingDetailCollectorFlg()) {
							return bundle_messages.getString("word.enable");
						} else {
							return bundle_messages.getString("word.disable");
						}
					}
					return "";
				}
			}
		),
		BillingDetailCollectorFlg(
			bundle_messages.getString("word.retention_period"),
			new ColumnPixelData(100, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					if (element instanceof ICloudScope) {
						if(((ICloudScope)element).getRetentionPeriod() != null){
							return ((ICloudScope)element).getRetentionPeriod().toString() + bundle_messages.getString("word.days");
						} else {
							return "0" + bundle_messages.getString("word.days");
						}
					}
					return "";
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
		for (ICloudScopes cloudScopes: root.getCloudScopes()) {
			cloudScopes.getHinemosManager().getModelWatch().removeWatcher(cloudScopes, watcher);
		}
		
		if (service != null)
			service.dispose();

		getSite().setSelectionProvider(null);

		super.dispose();
	}

	@Override
	public void update() {
		root.update(false);
	}

	protected void refresh() {
		treeViewer.refresh();
		treeViewer.expandToLevel(2);
	}
	
	@Override
	protected StructuredViewer getViewer() {
		return treeViewer;
	}

	@Override
	public String getId() {
		return Id;
	}
	
	@Override
	public void setFocus() {
		super.setFocus();
		CloudOptionSourceProvider.setActiveHinemosManagerToProvider(currentHinemosManager);
		CloudOptionSourceProvider.setActiveCloudScopeToProvider(currentScope);
		CloudOptionSourceProvider.setActiveLocationToProvider(null);
		
		if (service == null)
			service = new FacilityRootUpdateService();

	}
}
