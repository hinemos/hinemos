/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.plugin.job;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.FacilityImageConstant;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.util.FacilityTreeCache;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.xcloud.extensions.CloudModelContentProviderExtension;
import com.clustercontrol.xcloud.extensions.ICloudModelContentProvider;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.base.ElementBaseModeWatch;
import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.model.cloud.IHinemosManager;
import com.clustercontrol.xcloud.model.cloud.IStorage;
import com.clustercontrol.xcloud.model.repository.ICloudRepository;
import com.clustercontrol.xcloud.model.repository.ICloudScopeRootScope;
import com.clustercontrol.xcloud.model.repository.ICloudScopeScope;
import com.clustercontrol.xcloud.model.repository.IFacility;
import com.clustercontrol.xcloud.model.repository.IInstanceNode;
import com.clustercontrol.xcloud.model.repository.ILocationScope;
import com.clustercontrol.xcloud.model.repository.INode;
import com.clustercontrol.xcloud.model.repository.IScope;
import com.clustercontrol.xcloud.util.CollectionComparator;

/**
 * クラウドストレージ選択用のツリークラス
 */
public class StorageTreeComposite extends Composite {

	/** ロガー */
	private static Log m_log = LogFactory.getLog(StorageTreeComposite.class);

	/** ツリー表示するクラウドリポジトリ */
	private CloudRepositories cloudRepositories = new CloudRepositories();

	/** リフレッシュ時のタスク */
	private Runnable refreshTask = null;

	/** マネージャ名 */
	private String managerName = null;

	/** ストレージ選択用ツリー */
	private TreeViewer treeViewer = null;

	/** 選択されたアイテム */
	private IStorage selectItem = null;

	/**
	 * コンストラクタ
	 * @param parent
	 * @param style
	 * @param managerName
	 * @param ownerRoleId
	 */
	StorageTreeComposite(Composite parent, int style, String managerName) {
		super(parent, style);
		this.managerName = managerName;
		createContents();
	}

	/**
	 * 選択されたストレージを取得する<BR>
	 * ストレージ以外を選択している場合はnullを返す
	 * @return
	 */
	public IStorage getSelectItem() {
		return selectItem;
	}

	public TreeViewer getTreeViewer() {
		return this.treeViewer;
	}

	/** 
	 * コンポジットを生成する
	 */
	private void createContents() {

		// コンポジットのレイアウト定義
		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);

		// ツリーのレイアウトデータ定義
		GridData layoutData = new GridData();
		layoutData.horizontalAlignment = GridData.FILL;
		layoutData.verticalAlignment = GridData.FILL;
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;

		// ツリー作成
		Tree tree = new Tree(this, SWT.SINGLE | SWT.BORDER);
		WidgetTestUtil.setTestId(this, null, tree);
		tree.setLayoutData(layoutData);

		// ツリービューア設定
		treeViewer = new TreeViewer(tree);
		treeViewer.setContentProvider(new FacilityTreeContentProvider());
		treeViewer.setLabelProvider(new FacilityLabelProvider());
		treeViewer.setInput(cloudRepositories);
		treeViewer.setComparator(new TreeViewerComparator());

		// 選択アイテム取得イベント定義 （ストレージ選択は単一のみ可能）
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.getFirstElement() instanceof IStorage) {
					selectItem = (IStorage) selection.getFirstElement();
				} else {
					selectItem = null;
				}
			}
		});

		// 表示する
		update();
	}

	@Override
	public void update() {
		cloudRepositories.update(false);
	}

	// --- 以下は RepositoryView クラスのツリー表示をストレージ表示用に改造したもの（※完全コピペではないので注意） ---

	private class CloudRepositories {

		private List<ICloudRepository> cloudRepositories = new ArrayList<>();

		public List<ICloudRepository> getCloudRepositories() {
			return cloudRepositories;
		}

		public void update(boolean initialize) {
			IHinemosManager manager = ClusterControlPlugin.getDefault().getHinemosManager(managerName);
			try {
				if (!initialize || (initialize && !manager.isInitialized())) {
					// マネージャ毎に状態更新を行っているが、
					// マルチマネージャ接続時にクラウド/ＶＭが有効になってないマネージャの混在がありえる（endpoint通信で異常が出る）ので
					// 異常発生時は該当の警告ログのみを表示する。
					try {
						manager.update();
					} catch (CloudModelException e) {
						m_log.warn("update() . Failed to update the status of the manager's cloud function. Manager=" + manager.getManagerName());
					}
				}
				cloudRepositories.add(manager.getCloudRepository());
			} catch (Exception e) {
				m_log.warn(e.getMessage(), e);
			}

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

	private void refresh() {
		treeViewer.refresh();
		treeViewer.expandToLevel(3);
	}

	/**
	 * ストレージツリーアイテム
	 */
	private static class FacilityTreeContentProvider implements ITreeContentProvider {

		public Object[] getChildren(Object element) {
			if (element instanceof ICloudRepository) {
				return ((ICloudRepository) element).getRootScopes();
			}

			if (element instanceof ICloudScopeRootScope) {
				ICloudScopeRootScope facility = (ICloudScopeRootScope) element;
				return facility.getFacilities();
			}

			if (element instanceof ICloudScopeScope || element instanceof ILocationScope) {
				IScope scope = (IScope)element;
				String platformId = ((IScope)element).getCloudScopeScope().getCloudScope().getPlatformId();
				ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(platformId);
				return provider.getChildren(scope, (Object[])scope.getFacilities());
			}

			if (element instanceof IScope) {
				IScope scope = (IScope) element;
				ICloudScopeScope cloudScopeScope = scope.getCloudScopeScope();
				if (cloudScopeScope != null && cloudScopeScope.getLocation() != null) {
					ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(cloudScopeScope.getCloudScope().getPlatformId());
					return provider.getChildren((IScope) element, cloudScopeScope.getLocation().getComputeResources().getStorages());
				} else if (scope.getLocationScope() != null) {
					ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(cloudScopeScope.getCloudScope().getPlatformId());
					return provider.getChildren((IScope) element, scope.getLocationScope().getLocation().getComputeResources().getStorages());
				}
			}
			return null;
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof ICloudRepository) {
				return ((ICloudRepository) element).getRootScopes().length != 0;

			} else if (element instanceof ICloudScopeRootScope) {
				return ((ICloudScopeRootScope) element).getFacilities().length != 0;

			} else if (element instanceof ICloudScopeScope || element instanceof ILocationScope) {
				IScope scope = (IScope)element;
				String platformId = ((IScope)element).getCloudScopeScope().getCloudScope().getPlatformId();
				ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(platformId);
				return provider.getChildren(scope, scope.getFacilities()).length != 0;

			} else if (element instanceof IScope) {
				IScope scope = (IScope) element;
				IStorage[] storages = null;
				ICloudScopeScope cloudScopeScope = scope.getCloudScopeScope();
				if (cloudScopeScope != null && cloudScopeScope.getLocation() != null) {
					ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(cloudScopeScope.getCloudScope().getPlatformId());
					storages = provider.getChildren((IScope) element, cloudScopeScope.getLocation().getComputeResources().getStorages());
				} else if (scope.getLocationScope() != null) {
					ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(cloudScopeScope.getCloudScope().getPlatformId());
					storages = provider.getChildren((IScope) element, scope.getLocationScope().getLocation().getComputeResources().getStorages());
				}
				return (storages != null && storages.length != 0);
			}
			return false;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof CloudRepositories) {
				return ((CloudRepositories) inputElement).getCloudRepositories().toArray();
			}
			return new Object[] {};
		}

		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	/**
	 * ツリー表示アイテムのラベルクラス
	 */
	private static class FacilityLabelProvider extends LabelProvider {
		@Override
		public Image getImage(Object element) {
			if (element instanceof ICloudRepository) {
				return FacilityImageConstant.typeToImage(FacilityConstant.TYPE_COMPOSITE, true);

			} else if (element instanceof ICloudScopeRootScope) {
				return FacilityImageConstant.typeToImage(FacilityConstant.TYPE_SCOPE, true);

			} else if (element instanceof ICloudScopeScope) {
				Image defaultImage = ClusterControlPlugin.getDefault().getImageRegistry().get("cloudscope");
				String platformId = ((IFacility) element).getCloudScopeScope().getCloudScope().getPlatformId();
				ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(platformId);
				return provider.getImage(element, defaultImage);

			} else if (element instanceof ILocationScope) {
				Image defaultImage = ClusterControlPlugin.getDefault().getImageRegistry().get("location");
				String platformId = ((IFacility) element).getCloudScopeScope().getCloudScope().getPlatformId();
				ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(platformId);
				return provider.getImage(element, defaultImage);

			} else if (element instanceof IScope) {
				Image defaultImage = FacilityImageConstant.typeToImage(FacilityConstant.TYPE_SCOPE, true);
				String platformId = ((IFacility) element).getCloudScopeScope().getCloudScope().getPlatformId();
				ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(platformId);
				return provider.getImage(element, defaultImage);

			} else {
				return FacilityImageConstant.typeToImage(FacilityConstant.TYPE_NODE, true);
			}
		}

		@Override
		public String getText(Object element) {
			if (element instanceof ICloudRepository) {
				return ((ICloudRepository) element).getHinemosManager().getManagerName();

			} else if (element instanceof ICloudScopeRootScope) {
				IFacility facility = (IFacility) element;
				return HinemosMessage.replace(facility.getName()) + "(" + facility.getFacilityId() + ")";

			} else if (element instanceof IFacility) {
				IFacility facility = (IFacility) element;
				String platformId = facility.getCloudScopeScope().getCloudScope().getPlatformId();
				ICloudModelContentProvider provider = CloudModelContentProviderExtension.getModelContentProvider(platformId);
				return provider.getText(element, HinemosMessage.replace(facility.getName()));

			} else if (element instanceof IStorage) {
				IStorage storage = (IStorage) element;
				String displayName;
				if (storage.getName() != null && !storage.getName().equals("")) {
					displayName = storage.getName() + "(" + storage.getId() + ")";
				} else {
					displayName = storage.getId();
				}
				return displayName;
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
		private String getSortingKey(Object element) {
			String key = null;
			if (element instanceof ICloudRepository) {
				key = ((ICloudRepository) element).getHinemosManager().getManagerName();

			} else if (element instanceof IFacility) {
				key = HinemosMessage.replace(((IFacility) element).getName());
			}
			return (null == key) ? "" : key;
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			return getSortingKey(e1).compareTo(getSortingKey(e2));
		}
	}
}
