/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.monitor.dialog;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.rpa.monitor.composite.RpaFacilityTreeComposite;
import com.clustercontrol.rpa.util.RpaConstants;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * RPA管理ツールを表すスコープを表示するダイアログ
 * RPA管理ツールサービス監視向け
 */
public class RpaScopeTreeDialog extends ScopeTreeDialog {

	public RpaScopeTreeDialog(Shell parent, String managerName, String ownerRoleId) {
		super(parent, managerName, ownerRoleId, true, false);
	}

	@Override
	protected void customizeDialog(Composite parent) {
		// タイトル
		parent.getShell().setText(Messages.getString("select.scope"));

		GridLayout layout = new GridLayout(5, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		// RPAスコープ向けのFacilityTreeCompositeを表示する。
		treeComposite = new RpaFacilityTreeComposite(parent, SWT.NONE, getManagerName(), getOwnerRoleId());
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
	
	@Override
	protected ValidateResult validate() {
		ValidateResult result = super.validate();
		FacilityTreeItemResponse item = this.getSelectItem();
		if (item.getData().getFacilityId().equals(RpaConstants.RPA)) {
			// RPAルートスコープは選択不可
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.rpa.cant.select.root.scope"));
		}
		
		return result;
	}
}
