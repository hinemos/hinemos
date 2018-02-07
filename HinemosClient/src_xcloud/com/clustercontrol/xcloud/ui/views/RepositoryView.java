/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.FacilityImageConstant;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.util.FacilityTreeCache;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.extensions.ICloudModelContentProvider;
import com.clustercontrol.xcloud.extensions.CloudModelContentProviderExtension;
import com.clustercontrol.xcloud.model.base.ElementBaseModeWatch;
import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.model.cloud.IHinemosManager;
import com.clustercontrol.xcloud.model.cloud.ILocation;
import com.clustercontrol.xcloud.model.repository.ICloudRepository;
import com.clustercontrol.xcloud.model.repository.ICloudScopeRootScope;
import com.clustercontrol.xcloud.model.repository.ICloudScopeScope;
import com.clustercontrol.xcloud.model.repository.IFacility;
import com.clustercontrol.xcloud.model.repository.IInstanceNode;
import com.clustercontrol.xcloud.model.repository.ILocationScope;
import com.clustercontrol.xcloud.model.repository.INode;
import com.clustercontrol.xcloud.model.repository.IScope;
import com.clustercontrol.xcloud.plugin.CloudOptionSourceProvider;
import com.clustercontrol.xcloud.util.CollectionComparator;
import com.clustercontrol.xcloud.util.ControlUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;


public class RepositoryView extends AbstractCloudViewPart {
	public static final String Id = "com.clustercontrol.xcloud.ui.views.RepositoryView";
	
	private static final Log logger = LogFactory.getLog(RepositoryView.class);
	
	protected Runnable refreshTask;

	protected IHinemosManager currentHinemosManager = null;
	protected ICloudScope currentScope = null;
	protected ILocation currentLocation = null;

	protected class CloudRepositories {
		private List<ICloudRepository> repositories = new ArrayList<>();

		public List<ICloudRepository> getCloudRepositories() {
			return repositories;
		}

		public void update(boolean initialize) {
			List<IHinemosManager> managers = ClusterControlPlugin.getDefault().getHinemosManagers();

			List<ICloudRepository> newRepositories = new ArrayList<>();
			for (IHinemosManager m: managers) {
				try {
					if (!initialize || (initialize && !m.isInitialized()))
						m.update();
					newRepositories.add(m.getCloudRepository());
				} catch (Exception e) {
					logger.warn(e.getMessage(), e);
				}
			}
			
			CollectionComparator.compareCollection(repositories, newRepositories, new CollectionComparator.Comparator<ICloudRepository, ICloudRepository>() {
				@Override
				public boolean match(ICloudRepository o1, ICloudRepository o2) {
					return o1.getHinemosManager().getManagerName().equals(o2.getHinemosManager().getManagerName());
				}
				@Override
				public void afterO1(ICloudRepository o1) {
					o1.getHinemosManager().getModelWatch().removeWatcher(o1, watcher);
					repositories.remove(o1);
				}
				@Override
				public void afterO2(ICloudRepository o2) {
					o2.getHinemosManager().getModelWatch().addWatcher(o2, watcher);
					repositories.add(o2);
				}
			});
			
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

	private class FacilityRootUpdateService {
		private com.clustercontrol.composite.FacilityTreeComposite cacheComposite;

		public FacilityRootUpdateService() {
			cacheComposite = new com.clustercontrol.composite.FacilityTreeComposite(rootCompo, SWT.None, null, null, false) {
				@Override
				public void update() {
					rootCompo.getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							RepositoryView.this.update();
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
			cacheComposite.dispose();
			FacilityTreeCache.addComposite(cacheComposite);
		}

		public void dispose() {
			FacilityTreeCache.delComposite(cacheComposite);
		}
	}

	private static class FacilityTreeContentProvider implements ITreeContentProvider{
		public Object[] getChildren(Object element) {
			if (element instanceof ICloudRepository) {
				return ((ICloudRepository)element).getRootScopes();
			} if (element instanceof ICloudScopeRootScope) {
				ICloudScopeRootScope facility = (ICloudScopeRootScope)element;
				return facility.getFacilities();
			} else if (element instanceof IScope) {
				IScope scope = (IScope)element;
				String platformId = ((IScope)element).getCloudScopeScope().getCloudScope().getPlatformId();

				ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(platformId);
				if (provider != null)
					return provider.getChildren(scope, (Object[])scope.getFacilities());

				return scope.getFacilities();
			}
			return null;
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof ICloudRepository) {
				return ((ICloudRepository)element).getRootScopes().length != 0;
			} else if (element instanceof ICloudScopeRootScope) {
				return ((ICloudScopeRootScope)element).getFacilities().length != 0;
			} else if (element instanceof IScope){
				IScope scope = (IScope)element;
				String platformId = ((IScope)element).getCloudScopeScope().getCloudScope().getPlatformId();

				ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(platformId);
				if (provider != null)
					return provider.getChildren(scope, scope.getFacilities()).length != 0;

				return scope.getFacilities().length != 0;
			}
			return false;	
		}

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof CloudRepositories) {
				return ((CloudRepositories)inputElement).getCloudRepositories().toArray();
			}
			return new Object[]{};
		}

		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private static class FacilityLabelProvider extends LabelProvider {
		@Override
		public Image getImage(Object element) {
			if (element instanceof ICloudRepository) {
				return FacilityImageConstant.typeToImage(FacilityConstant.TYPE_COMPOSITE, true);
			} else if (element instanceof ICloudScopeRootScope) {
				return FacilityImageConstant.typeToImage(FacilityConstant.TYPE_SCOPE, true);
			} else if (element instanceof ICloudScopeScope) {
				Image defaultImage = ClusterControlPlugin.getDefault().getImageRegistry().get("cloudscope");
				String platformId = ((IFacility)element).getCloudScopeScope().getCloudScope().getPlatformId();
				ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(platformId);
				if (provider != null)
					return provider.getImage(element, defaultImage);
				return defaultImage;
			} else if (element instanceof ILocationScope) {
				Image defaultImage = ClusterControlPlugin.getDefault().getImageRegistry().get("location");
				String platformId = ((IFacility)element).getCloudScopeScope().getCloudScope().getPlatformId();
				ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(platformId);
				if (provider != null)
					return provider.getImage(element, defaultImage);
				return defaultImage;
			} else if (element instanceof IScope) {
				Image defaultImage = FacilityImageConstant.typeToImage(FacilityConstant.TYPE_SCOPE, true);
				String platformId = ((IFacility)element).getCloudScopeScope().getCloudScope().getPlatformId();
				ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(platformId);
				if (provider != null)
					return provider.getImage(element, defaultImage);
				return defaultImage;
			} else if (element instanceof IInstanceNode) {
				IInstanceNode instanceNode = (IInstanceNode)element;

				Image defaultImage = ClusterControlPlugin.getDefault().getImageRegistry().get("instance");
				if (!instanceNode.getInstance().getStatus().equals("running")) {
					defaultImage = ClusterControlPlugin.getDefault().getImageRegistry().get("instance2");
				}
				
				String platformId = instanceNode.getCloudScopeScope().getCloudScope().getPlatformId();
				ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(platformId);
				if (provider != null)
					return provider.getImage(element, defaultImage);
				return defaultImage;
			} else if (element instanceof INode) {
				Image defaultImage = FacilityImageConstant.typeToImage(FacilityConstant.TYPE_NODE, true);
				String platformId = ((IFacility)element).getCloudScopeScope().getCloudScope().getPlatformId();
				ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(platformId);
				if (provider != null)
					return provider.getImage(element, defaultImage);
				return defaultImage;
			} else {
				return FacilityImageConstant.typeToImage(FacilityConstant.TYPE_NODE, true);
			}
		}
		@Override
		public String getText(Object element) {
			if (element instanceof ICloudRepository) {
				return ((ICloudRepository)element).getHinemosManager().getManagerName();
			} else if (element instanceof ICloudScopeRootScope) {
				IFacility facility = (IFacility)element;
				return HinemosMessage.replace(facility.getName()) + "(" + facility.getFacilityId() + ")";
			} else if (element instanceof IFacility) {
				IFacility facility = (IFacility)element;
				String platformId = facility.getCloudScopeScope().getCloudScope().getPlatformId();
				ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(platformId);
				if (provider != null)
					return provider.getText(element, HinemosMessage.replace(facility.getName()));
				else
					return HinemosMessage.replace(facility.getName()) + "(" + facility.getFacilityId() + ")";
			}
			return element.toString();
		}
	}

	private static class TreeViewerComparator extends ViewerComparator {

		/**
		 * Set sorting key by element type
		 * @param element
		 * @return
		 */
		private String getSortingKey(Object element){
			String key = null;
			if (element instanceof ICloudRepository)
				key = ((ICloudRepository)element).getHinemosManager().getManagerName();
			else if(element instanceof IFacility)
				key = HinemosMessage.replace(((IFacility)element).getName());
			return (null==key)? "" : key;
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			return getSortingKey(e1).compareTo(getSortingKey(e2));
		}
	}

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
							refresh();
						} finally {
							refreshTask = null;
						}
					}
				};
				Display.getCurrent().asyncExec(refreshTask);
			}
		}
	};

	private FacilityRootUpdateService service;

	private CloudRepositories cloudRepositories = new CloudRepositories();
	private TreeViewer treeViewer;
	private Composite rootCompo;
	
	private List<List<String>> initPaths = Collections.emptyList();
	
	public RepositoryView() {
		super();
	}

	@Override
	protected void internalCreatePartControl(Composite parent) {
		rootCompo = new Composite(parent, SWT.NONE);
		rootCompo.setLayout(new FillLayout(SWT.HORIZONTAL));

		Composite composite_1 = new Composite(rootCompo, SWT.NONE);
		TreeColumnLayout tcl_composite = new TreeColumnLayout();
		composite_1.setLayout(tcl_composite);

		treeViewer = new TreeViewer(composite_1, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

		treeViewer.setContentProvider(new FacilityTreeContentProvider());
		treeViewer.setLabelProvider(new FacilityLabelProvider());
		treeViewer.setInput(cloudRepositories);
		treeViewer.setComparator(new TreeViewerComparator());

		this.getSite().setSelectionProvider(treeViewer);

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
				currentLocation = null;
				
				Object selected = sselection.getFirstElement();
				if (selected instanceof IElement) {
					IElement scope = (IElement)selected;
					currentHinemosManager = (IHinemosManager)scope.getAdapter(IHinemosManager.class);
					currentScope = (ICloudScope)scope.getAdapter(ICloudScope.class);
					currentLocation = (ILocation)scope.getAdapter(ILocation.class);
				}
				
				CloudOptionSourceProvider.setActiveHinemosManagerToProvider(currentHinemosManager);
				CloudOptionSourceProvider.setActiveCloudScopeToProvider(currentScope);
				CloudOptionSourceProvider.setActiveLocationToProvider(currentLocation);
			}
		});
		
		try {
			cloudRepositories.update(true);
			
			List<TreePath> treePaths = new ArrayList<>();
			for (List<String> path: initPaths) {
				String manager = path.get(0);
				ICloudRepository repository = null;
				List<Object> segments = new ArrayList<>();
				for (ICloudRepository r: cloudRepositories.getCloudRepositories()) {
					if (r.getHinemosManager().getUrl().equals(manager)) {
						segments.add(r);
						repository = r;
						break;
					}
				}
				
				if (repository == null)
					continue;
				
				IFacility[] facilities = repository.getRootScopes();
				loop_end:
				for (int i = 1; i < path.size(); ++i) {
					String facilityId = path.get(i);
					for (IFacility f: facilities) {
						if (f.getFacilityId().equals(facilityId)) {
							segments.add(f);
							if (f instanceof IScope) {
								facilities = ((IScope)f).getFacilities();
								break;
							} else {
								break loop_end;
							}
						}
					}
				}
				treePaths.add(new TreePath(segments.toArray()));
			}
			treeViewer.setExpandedTreePaths(treePaths.toArray(new TreePath[treePaths.size()]));
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
			
			ControlUtil.openError(e, CloudStringConstants.msgErrorFinishRefreshView);
		}
	}

	@Override
	public void dispose() {
		for (ICloudRepository repository: cloudRepositories.getCloudRepositories()) {
			repository.getHinemosManager().getModelWatch().removeWatcher(repository, watcher);
		}
		
		if (service != null)
			service.dispose();
		
		getSite().setSelectionProvider(null);

		super.dispose();
	}

	@Override
	public void update() {
		cloudRepositories.update(false);
	}

	protected void refresh() {
		treeViewer.refresh();
		// Expand tree to level 3 (account)
		treeViewer.expandToLevel(3);
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
		CloudOptionSourceProvider.setActiveLocationToProvider(currentLocation);
		
		if (service == null)
			service = new FacilityRootUpdateService();
	}
	
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		
		if (memento == null)
			return;
		
		String pathString = memento.getString("path");
		if (pathString != null) {
			ObjectMapper om = new ObjectMapper();
			ObjectReader or = om.readerFor(new TypeReference<List<List<String>>>(){});

			try {
				initPaths = or.readValue(pathString);
			} catch (IOException e) {
				logger.warn(e.getMessage(), e);
			}
		}
	}

	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		TreePath[] path = treeViewer.getExpandedTreePaths();
		
		List<List<String>> pathList = new ArrayList<>();
		for (TreePath p: path) {
			List<String> pl = new ArrayList<>();
			for (int i = 0; i <  p.getSegmentCount(); ++i) {
				Object o = p.getSegment(i);
				if (o instanceof ICloudRepository) {
					ICloudRepository r = (ICloudRepository)o;
					pl.add(r.getHinemosManager().getUrl());
				} else if (o instanceof IScope) {
					IFacility f = (IFacility)o;
					pl.add(f.getFacilityId());
				} else if (o instanceof INode) {
					IFacility f = (IFacility)o;
					pl.add(f.getFacilityId());
				}
			}
			pathList.add(pl);
		}
		
		ObjectMapper om = new ObjectMapper();
		ObjectWriter ow = om.writer();
		try {
			String pathString = ow.writeValueAsString(pathList);
			memento.putString("path", pathString);
		} catch (JsonProcessingException e) {
			logger.warn(e.getMessage(), e);
		}
	}
}
