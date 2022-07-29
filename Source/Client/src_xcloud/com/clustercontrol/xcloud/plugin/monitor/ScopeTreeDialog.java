/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.plugin.monitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.CloudScopeInfoResponse;
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;

import com.clustercontrol.composite.FacilityTreeComposite;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.util.Messages;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.common.CloudConstants;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.cloud.IHinemosManager;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;

/**
 * スコープツリーからスコープもしくはノードを選択するためのダイアログ<BR>
 * 
 * @version 6.0.0
 * @since 1.0.0
 */
public class ScopeTreeDialog extends CommonDialog {
	/** 選択されたアイテム */
	private FacilityTreeComposite treeComposite = null;

	/** オーナーロールID **/
	private String ownerRoleId = null;

	private IHinemosManager manager = null;

	// ----- コンストラクタ ----- //

	/**
	 * ダイアログのインスタンスを返します。
	 * 
	 * @param parent
	 * @param ownerRoleId
	 *            親とするシェル
	 */
	public ScopeTreeDialog(Shell parent, IHinemosManager manager, String ownerRoleId) {
		super(parent);
		this.ownerRoleId = ownerRoleId;
		//未登録ノードスコープはデフォルト非表示
		this.manager = manager;
	}
	
	public ScopeTreeDialog(Shell parent, IHinemosManager manager, String ownerRoleId, String plaginId) {
		super(parent);
		this.ownerRoleId = ownerRoleId;
		//未登録ノードスコープはデフォルト非表示
		this.manager = manager;
	}

	// ----- instance メソッド ----- //

	@Override
	protected Point getInitialSize() {
		return new Point(400, 400);
	}

	@Override
	protected void customizeDialog(Composite parent) {
		// タイトル
		parent.getShell().setText(Messages.getString("select.scope"));

		GridLayout layout = new GridLayout(5, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		
		
		CloudRestClientWrapper wrapper = manager.getWrapper();
		List<CloudScopeInfoResponse> cloudScopes;
		try {
			if ("ADMINISTRATORS".equals(this.ownerRoleId)) {
				cloudScopes = wrapper.getCloudScopes(null);
			} else {
				cloudScopes = wrapper.getCloudScopes(this.ownerRoleId);
			}
		} catch (CloudManagerException | InvalidUserPass | InvalidRole | InvalidSetting | RestConnectFailed | HinemosUnknown e) {
			throw new CloudModelException(e);
		}
		
		treeComposite = new FacilityTreeComposite(parent, SWT.NONE, this.manager.getManagerName(), this.ownerRoleId, true, false, false, "") {
			
			@Override
			protected void createContents() {
				super.createContents();
				treeViewer.setContentProvider(new ITreeContentProvider() {
					@Override
					public Object getParent(Object element) {
						return ((FacilityTreeItemResponse) element).getParent();
					}
					@Override
					public Object[] getElements(Object inputElement) {
						return getChildren(inputElement);
					}
					@Override
					public Object[] getChildren(Object parentElement) {
						List<FacilityTreeItemResponse> children = new ArrayList<>();
						for (FacilityTreeItemResponse child: ((FacilityTreeItemResponse) parentElement).getChildren()) {
							if (child.getData().getFacilityType() == FacilityTypeEnum.COMPOSITE || checkValidScope(child))
								children.add(child);
						}
						return children.toArray();
					}
					@Override
					public boolean hasChildren(Object element) {
						return getChildren(element).length > 0;
					}

					@Override
					public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
					}

					@Override
					public void dispose() {
					}
					
					protected boolean checkValidScope(final FacilityTreeItemResponse target) {
						if (!CloudConstants.PRIVATE_CLOUD_SCOPE_ID.equals(target.getData().getFacilityId()) &&
							!CloudConstants.PUBLIC_CLOUD_SCOPE_ID.equals(target.getData().getFacilityId()) &&
							target.getData().getBuiltInFlg()) {
							return false;
						}
						
						// クラウドスコープが関連するノードで絞り込み
						for (CloudScopeInfoResponse cloudScope: cloudScopes) {
							String cloudScopeNodeId = String.format("_%s_%s_Node", cloudScope.getEntity().getPlatformId(), cloudScope.getEntity().getCloudScopeId());
							if (cloudScopeNodeId.equals(target.getData().getFacilityId()))
								return true;
						}
						for (FacilityTreeItemResponse child: target.getChildren()) {
							if (checkValidScope(child))
								return true;
						}
						return false;
					}
				});
				super.update();
			}
		};

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 5;
		treeComposite.setLayoutData(gridData);

		// アイテムをダブルクリックした場合、それを選択したこととする。
		treeComposite.getTreeViewer().addDoubleClickListener(
				new IDoubleClickListener() {
					@Override
					public void doubleClick(DoubleClickEvent event) {
						okPressed();
					}
				});
		
		treeComposite.setExpand(true);
	}

	public FacilityTreeItemResponse getSelectItem() {
		return this.treeComposite.getSelectItem();
	}
	
	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;
		FacilityTreeItemResponse item = this.getSelectItem();

		if (item == null
			|| item.getData().getNotReferFlg()
			|| item.getData().getFacilityType() == FacilityTypeEnum.COMPOSITE
			|| item.getData().getFacilityType() == FacilityTypeEnum.MANAGER) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.repository.47"));
		}
		return result;
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
