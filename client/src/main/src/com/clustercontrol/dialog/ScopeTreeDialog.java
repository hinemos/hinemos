/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.dialog;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.composite.FacilityTreeComposite;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * スコープツリーからスコープもしくはノードを選択するためのダイアログ<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class ScopeTreeDialog extends CommonDialog {

	// ----- instance フィールド ----- //

	/** 選択されたアイテム */
	private FacilityTreeComposite treeComposite = null;

	/**ノードをツリーに含めるかのフラグ**/
	private boolean scopeOnly;
	/**未登録ノード　スコープを含めるかのフラグ**/
	private boolean unregistered;

	/** ノードのみを選択可能とするフラグ **/
	private boolean selectNodeOnly;

	/** オーナーロールID **/
	private String ownerRoleId = null;

	/** マネージャ名 */
	private String managerName = null;

	// ----- コンストラクタ ----- //

	/**
	 * ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 * @param managerName
	 * @param ownerRoleId
	 *            親とするシェル
	 */
	public ScopeTreeDialog(Shell parent, String managerName, String ownerRoleId) {
		super(parent);
		this.managerName = managerName;
		this.ownerRoleId = ownerRoleId;
		this.scopeOnly = false;
		//未登録ノードスコープはデフォルト非表示
		this.unregistered = false;
	}


	/**
	 * ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親とするシェル
	 * @param managerName
	 * @param scopeOnly
	 */
	public ScopeTreeDialog(Shell parent, String managerName, String ownerRoleId, boolean scopeOnly) {
		super(parent);
		this.managerName = managerName;
		this.ownerRoleId = ownerRoleId;
		this.scopeOnly = scopeOnly;
		//未登録ノードスコープはデフォルト非表示
		this.unregistered = false;
	}

	public ScopeTreeDialog(Shell parent, String managerName, String ownerRoleId, boolean scopeOnly, boolean unregistered){
		super(parent);
		this.managerName = managerName;
		this.ownerRoleId = ownerRoleId;
		this.scopeOnly = scopeOnly;
		//未登録ノードスコープはデフォルト非表示
		this.unregistered = unregistered;

	}

	public void setSelectNodeOnly(boolean flag) {
		this.selectNodeOnly = flag;
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

		if(selectNodeOnly){
			treeComposite = new FacilityTreeComposite(parent, SWT.NONE, this.managerName, this.ownerRoleId, true);
		}else{
			treeComposite = new FacilityTreeComposite(parent, SWT.NONE, this.managerName, this.ownerRoleId, scopeOnly,
					unregistered, false);
		}
		WidgetTestUtil.setTestId(this, null, treeComposite);

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
	}

	public FacilityTreeItem getSelectItem() {
		return this.treeComposite.getSelectItem();
	}

	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;
		FacilityTreeItem item = this.getSelectItem();

		if (this.selectNodeOnly) {
			// ノードのみ選択可能な場合
			if (item == null
					|| item.getData().isNotReferFlg()
					|| item.getData().getFacilityType() == FacilityConstant.TYPE_COMPOSITE
					|| item.getData().getFacilityType() == FacilityConstant.TYPE_MANAGER
					|| item.getData().getFacilityType() == FacilityConstant.TYPE_SCOPE) {
				// 未選択の場合エラー
				// 参照不可のスコープを選択した場合はエラー
				// ルートを選択した場合はエラー
				// スコープを選択するとエラー
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.repository.2"));
			}
		} else {
			// ノード・スコープが選択可能な場合
			if (item == null
					|| item.getData().isNotReferFlg()
					|| item.getData().getFacilityType() == FacilityConstant.TYPE_COMPOSITE
					|| item.getData().getFacilityType() == FacilityConstant.TYPE_MANAGER) {
				// 未選択の場合エラー
				// 参照不可のスコープを選択した場合はエラー
				// ルートを選択した場合はエラー
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.repository.47"));
			}
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
