/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.plugin.monitor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.HFacilityResponse;
import org.openapitools.client.model.HRepositoryResponse;
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;
import org.openapitools.client.model.HFacilityResponse.TypeEnum;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.composite.FacilityTreeComposite;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.bean.CloudConstant;
import com.clustercontrol.xcloud.common.CloudConstants;
import com.clustercontrol.xcloud.model.InvalidStateException;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;

/**
 * クラウドスコープ（第3階層まで）のスコープツリーからスコープを選択するためのダイアログ<BR>
 * v.7.0時点ではクラウドログ監視、クラウド通知から使用されています。
 */
public class ScopeTreeDialogForServiceMonitors extends CommonDialog {
	private static Log m_log = LogFactory.getLog(ScopeTreeDialogForServiceMonitors.class);

	/** 選択されたアイテム */
	private FacilityTreeComposite treeComposite = null;

	/** オーナーロールID **/
	private String ownerRoleId = null;

	private String manager = null;

	private Map<String, HFacilityResponse> hScopeMap = new ConcurrentHashMap<>();

	// ----- コンストラクタ ----- //

	/**
	 * ダイアログのインスタンスを返します。
	 * 
	 * @param parent
	 * @param ownerRoleId
	 *            親とするシェル
	 */
	public ScopeTreeDialogForServiceMonitors(Shell parent, String manager, String ownerRoleId) {
		super(parent);
		this.ownerRoleId = ownerRoleId;
		// 未登録ノードスコープはデフォルト非表示
		this.manager = manager;
	}

	// ----- instance メソッド ----- //

	@Override
	protected Point getInitialSize() {
		return new Point(400, 400);
	}

	@Override
	protected void customizeDialog(Composite parent) {
		Field treeComposite = null;
		boolean treeCompositeAccesible = false;
		try {
			treeComposite = ScopeTreeDialogForServiceMonitors.class.getDeclaredField("treeComposite");
			treeCompositeAccesible = treeComposite.isAccessible();
			treeComposite.setAccessible(true);

			// タイトル
			parent.getShell().setText(Messages.getString("select.scope"));

			GridLayout layout = new GridLayout(5, true);
			parent.setLayout(layout);
			layout.marginHeight = 0;
			layout.marginWidth = 0;

			treeComposite.set(this, new FacilityTreeComposite(parent, SWT.NONE, manager, ownerRoleId, true) {
				/**
				 * ビューの表示内容を更新します。
				 */
				@Override
				public void update() {
					final CloudRestClientWrapper wrapper = ClusterControlPlugin.getDefault().getHinemosManager(manager)
							.getWrapper();
					HRepositoryResponse cloudRepository;
					try {
						cloudRepository = wrapper.getRepository(ownerRoleId);
					} catch (CloudManagerException | InvalidUserPass | InvalidRole | RestConnectFailed
							| HinemosUnknown e) {
						m_log.warn(e.getMessage(), e);
						throw new InvalidStateException(e.getMessage(), e);
					}

					FacilityTreeItemResponse treeItem;
					try {
						treeItem = RepositoryRestClientWrapper.getWrapper(manager).getFacilityTree(ownerRoleId);
						if (treeItem != null && treeItem.getChildren() != null
								&& treeItem.getChildren().get(0) != null) {
							Collections.sort(treeItem.getChildren().get(0).getChildren(),
									new Comparator<FacilityTreeItemResponse>() {
										@Override
										public int compare(FacilityTreeItemResponse o1, FacilityTreeItemResponse o2) {
											FacilityInfoResponse info1 = ((FacilityTreeItemResponse) o1).getData();
											FacilityInfoResponse info2 = ((FacilityTreeItemResponse) o2).getData();
											int order1 = info1.getDisplaySortOrder();
											int order2 = info2.getDisplaySortOrder();
											if (order1 == order2) {
												String object1 = info1.getFacilityId();
												String object2 = info2.getFacilityId();
												return object1.compareTo(object2);
											} else {
												return (order1 - order2);
											}
										}
									});
						}
					} catch (HinemosUnknown | InvalidRole | InvalidUserPass | RestConnectFailed e) {
						m_log.warn(e.getMessage(), e);
						throw new InvalidStateException(e.getMessage(), e);
					}

					final FacilityTreeItemResponse treeItemWrapper = addEmptyParent(
							createTreeItemWrapper(cloudRepository, treeItem));

					if (!this.isDisposed()) {
						m_log.trace("FacilityTreeComposite.checkAsyncExec() is true");
						getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								m_log.trace("FacilityTreeComposite.checkAsyncExec() do runnnable");

								FacilityTreeItemResponse oldTreeItem = (FacilityTreeItemResponse) treeViewer.getInput();
								m_log.debug("run() oldTreeItem=" + oldTreeItem);
								if (null != oldTreeItem) {
									if (!oldTreeItem.equals(treeItemWrapper)) {
										ArrayList<String> expandIdList = new ArrayList<String>();
										for (Object item : treeViewer.getExpandedElements()) {
											expandIdList
													.add(((FacilityTreeItemResponse) item).getData().getFacilityId());
										}
										treeViewer.setInput(treeItemWrapper);
										treeViewer.refresh();
										expand(treeItemWrapper, expandIdList);
									}
								} else {
									m_log.info("oldTreeItem is null");
									treeViewer.setInput(treeItemWrapper);
									List<FacilityTreeItemResponse> selectItem = treeItemWrapper.getChildren();
									if (!selectItem.isEmpty()) {
										treeViewer.setSelection(new StructuredSelection(selectItem.get(0)), true);
										// スコープのレベルまで展開
										treeViewer.expandToLevel(4);
									}
								}
							}

							private void expand(FacilityTreeItemResponse item, List<String> expandIdList) {
								if (expandIdList.contains(item.getData().getFacilityId())) {
									treeViewer.expandToLevel(item, 1);
								}
								for (FacilityTreeItemResponse child : item.getChildren()) {
									expand(child, expandIdList);
								}
							}
						});
					} else {
						m_log.trace("FacilityTreeComposite.checkAsyncExec() is false");
					}
				}
			});

			FacilityTreeComposite w = (FacilityTreeComposite) treeComposite.get(this);
			WidgetTestUtil.setTestId(this, null, w);

			GridData gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.verticalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.grabExcessVerticalSpace = true;
			gridData.horizontalSpan = 5;
			w.setLayoutData(gridData);

			// アイテムをダブルクリックした場合、それを選択したこととする。
			w.getTreeViewer().addDoubleClickListener(new IDoubleClickListener() {

				@Override
				public void doubleClick(DoubleClickEvent event) {
					okPressed();
				}
			});
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException |

				IllegalAccessException e1) {
			e1.printStackTrace();
		} finally {
			if (treeComposite != null)
				treeComposite.setAccessible(treeCompositeAccesible);
		}
	}

	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;
		FacilityTreeItemResponse item = this.getSelectItem();

		// ノード・スコープが選択可能な場合
		if (item == null || item.getData().getNotReferFlg()
				|| item.getData().getFacilityType() == FacilityTypeEnum.COMPOSITE
				|| item.getData().getFacilityType() == FacilityTypeEnum.MANAGER) {
			// 未選択の場合エラー
			// 参照不可のスコープを選択した場合はエラー
			// ルートを選択した場合はエラー
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.repository.47"));
		} else if (item instanceof FacilityTreeItemWrapper) {
			FacilityTreeItemWrapper wrapper = (FacilityTreeItemWrapper) item;
			// ルートクラウドスコープは選択不可
			if (wrapper.getHScope() != null && wrapper.getHScope().getType() == TypeEnum.ROOT) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(CloudConstants.bundle_messages.getString("message.cant_select_cloud_root_scope"));
			} else if (wrapper.getParent() != null
					&& wrapper.getParent().getData().getFacilityId().equals(CloudConstants.PUBLIC_CLOUD_SCOPE_ID)) {
				// 親がルートの場合も選択不可
				// ※選択可能はリージョン（第3階層のみ）
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(CloudConstants.bundle_messages.getString("message.select.region"));
			}

		}
		return result;
	}

	public class FacilityTreeItemWrapper extends FacilityTreeItemResponse {
		private HFacilityResponse scope;
		private FacilityTreeItemResponse item;

		public FacilityTreeItemWrapper(HFacilityResponse scope, FacilityTreeItemResponse item) {
			this.scope = scope;
			this.item = item;
		}

		public List<FacilityTreeItemResponse> getChildren() {
			if (super.getChildren().isEmpty()) {
				List<FacilityTreeItemResponse> items = new ArrayList<FacilityTreeItemResponse>(item.getChildren());
				Iterator<HFacilityResponse> hFacilityIter = new ArrayList<>(scope.getFacilities()).iterator();
				while (hFacilityIter.hasNext()) {
					HFacilityResponse f = hFacilityIter.next();
					if (f.getType() == TypeEnum.CLOUDSCOPE || f.getType() == TypeEnum.LOCATION) {
						// AWSもしくはAzureの場合のみツリーに追加
						if (f.getPlatformId().equals(CloudConstant.platform_AWS)
								|| f.getPlatformId().equals(CloudConstant.platform_Azure)
								|| f.getPlatformId().equals(CloudConstant.platform_GCP)) {

							Iterator<FacilityTreeItemResponse> itemIter = items.iterator();
							while (itemIter.hasNext()) {
								FacilityTreeItemResponse i = (FacilityTreeItemResponse) itemIter.next();
								if (f.getId().equals(i.getData().getFacilityId())) {
									hScopeMap.put(f.getId(), f);

									FacilityTreeItemResponse wrapper = new FacilityTreeItemWrapper(f, i);
									wrapper.setParent(this);
									super.getChildren().add(wrapper);
									hFacilityIter.remove();
									itemIter.remove();
									break;
								}
							}
						}
					}
				}
			}
			return super.getChildren();
		}

		public FacilityInfoResponse getData() {
			return item.getData();
		}

		public HFacilityResponse getHScope() {
			return scope;
		}

	};

	protected FacilityTreeItemResponse createTreeItemWrapper(final HRepositoryResponse cloudRepository,
			final FacilityTreeItemResponse treeItem) {
		hScopeMap.clear();
		return new FacilityTreeItemResponse() {
			public List<FacilityTreeItemResponse> getChildren() {
				if (super.getChildren().isEmpty()) {
					List<FacilityTreeItemResponse> items = new ArrayList<FacilityTreeItemResponse>(
							treeItem.getChildren());
					Iterator<HFacilityResponse> hFacilityIter = new ArrayList<>(cloudRepository.getFacilities())
							.iterator();
					while (hFacilityIter.hasNext()) {
						HFacilityResponse f = hFacilityIter.next();
						if (f.getType() == TypeEnum.ROOT) {
							Iterator<FacilityTreeItemResponse> itemIter = items.iterator();
							while (itemIter.hasNext()) {
								FacilityTreeItemResponse i = (FacilityTreeItemResponse) itemIter.next();
								if (f.getId().equals(i.getData().getFacilityId())) {
									FacilityTreeItemResponse wrapper = new FacilityTreeItemWrapper(f, i);
									wrapper.setParent(this);
									super.getChildren().add(wrapper);
									hFacilityIter.remove();
									itemIter.remove();
									break;
								}
							}
						}
					}
				}
				return super.getChildren();
			}

			public FacilityInfoResponse getData() {
				return treeItem.getData();
			}
		};
	}

	public FacilityTreeItemResponse getSelectItem() {
		return this.treeComposite.getSelectItem();
	}

	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}
}
