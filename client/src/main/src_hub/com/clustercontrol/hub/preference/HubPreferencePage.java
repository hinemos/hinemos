/*

 Copyright (C) 2016 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 */
package com.clustercontrol.hub.preference;



import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.util.Messages;

public class HubPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/** 検索結果 1ページ表示件数 */
	public static final String P_SIZE_POS = "pageMax";
	private static final String MSG_SIZE_POS =  Messages.getString("number.of.display.list");

	public static final int PAGE_MAX_DEFAULT = 100; // 検索結果 1ページ表示件数

	
	public HubPreferencePage() {
		super(GRID);
		this.setPreferenceStore(ClusterControlPlugin.getDefault().getPreferenceStore());
		initializeDefaults();
	}
	
	private void initializeDefaults(){
		IPreferenceStore store =this.getPreferenceStore();
		store.setDefault(HubPreferencePage.P_SIZE_POS,PAGE_MAX_DEFAULT);
	}
	
	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(ClusterControlPlugin.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		Composite parent = this.getFieldEditorParent();
		GridData gridData = null;
		
		// ログ検索ビュー 検索結果
		Group resultGroup = new Group(parent, SWT.SHADOW_NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 10;
		gridData.verticalSpan = 10;
		resultGroup.setLayoutData(gridData);
		resultGroup.setText(Messages.getString("view.hub.preference.search"));

		// 検索結果 1ページ表示件数
		IntegerFieldEditor pageMax = new IntegerFieldEditor(
				P_SIZE_POS,
				MSG_SIZE_POS,
				resultGroup);
		pageMax.setValidRange(1, DataRangeConstant.SMALLINT_HIGH);
		String[] args1 = { Integer.toString(1),
				Integer.toString(DataRangeConstant.SMALLINT_HIGH) };
		pageMax.setErrorMessage(Messages
				.getString("message.hinemos.8", args1));
		this.addField(pageMax);
	}
	/**
	 * ボタン押下時に設定反映
	 */
	@Override
	public boolean performOk() {
		applySetting();
		return super.performOk();
	}

	/**
	 * 設定内容を反映します。
	 */
	private void applySetting() {
	}
}
