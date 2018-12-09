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

import com.clustercontrol.composite.FacilityTreeComposite;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.repository.FacilityTreeItem;
import com.clustercontrol.ws.xcloud.CloudEndpoint;
import com.clustercontrol.ws.xcloud.CloudManagerException;
import com.clustercontrol.ws.xcloud.CloudScope;
import com.clustercontrol.ws.xcloud.InvalidRole_Exception;
import com.clustercontrol.ws.xcloud.InvalidUserPass_Exception;
import com.clustercontrol.xcloud.common.CloudConstants;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.cloud.IHinemosManager;

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
		
		
		CloudEndpoint endpoint = manager.getEndpoint(CloudEndpoint.class);
		List<CloudScope> cloudScopes;
		try {
			if ("ADMINISTRATORS".equals(this.ownerRoleId)) {
				cloudScopes = endpoint.getAllCloudScopes();
			} else {
				cloudScopes = endpoint.getCloudScopesByRole(this.ownerRoleId);
			}
		} catch (CloudManagerException | InvalidRole_Exception | InvalidUserPass_Exception e) {
			throw new CloudModelException(e);
		}
		
		treeComposite = new FacilityTreeComposite(parent, SWT.NONE, this.manager.getManagerName(), this.ownerRoleId, true, false, false, "") {
			
			@Override
			protected void createContents() {
				super.createContents();
				treeViewer.setContentProvider(new ITreeContentProvider() {
					@Override
					public Object getParent(Object element) {
						return ((FacilityTreeItem) element).getParent();
					}
					@Override
					public Object[] getElements(Object inputElement) {
						return getChildren(inputElement);
					}
					@Override
					public Object[] getChildren(Object parentElement) {
						List<FacilityTreeItem> children = new ArrayList<>();
						for (FacilityTreeItem child: ((FacilityTreeItem) parentElement).getChildren()) {
							if (child.getData().getFacilityType() == FacilityConstant.TYPE_COMPOSITE || checkValidScope(child))
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
					
					protected boolean checkValidScope(final FacilityTreeItem target) {
						if (!CloudConstants.PRIVATE_CLOUD_SCOPE_ID.equals(target.getData().getFacilityId()) &&
							!CloudConstants.PUBLIC_CLOUD_SCOPE_ID.equals(target.getData().getFacilityId()) &&
							target.getData().isBuiltInFlg()) {
							return false;
						}
						
						// クラウドスコープが関連するノードで絞り込み
						for (CloudScope cloudScope: cloudScopes) {
							String cloudScopeNodeId = String.format("_%s_%s_Node", cloudScope.getPlatformId(), cloudScope.getId());
							if (cloudScopeNodeId.equals(target.getData().getFacilityId()))
								return true;
						}
						for (FacilityTreeItem child: target.getChildren()) {
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

	public FacilityTreeItem getSelectItem() {
		return this.treeComposite.getSelectItem();
	}
	
	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;
		FacilityTreeItem item = this.getSelectItem();

		if (item == null
			|| item.getData().isNotReferFlg()
			|| item.getData().getFacilityType() == FacilityConstant.TYPE_COMPOSITE
			|| item.getData().getFacilityType() == FacilityConstant.TYPE_MANAGER) {
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
