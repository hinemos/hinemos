/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.approval.preference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
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
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 承認[一覧]ビューの設定ページクラスです。
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class ApprovalPreferencePage extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage {

	private static Log m_log = LogFactory.getLog(ApprovalPreferencePage.class);

	/** 承認[一覧]ビューのメッセージ表示 */
	public static final String P_APPROVAL_MESSAGE_FLG = "approvalMessageFlg";

	/** 承認[一覧]ビューの表示件数 */
	public static final String P_APPROVAL_MAX_LIST= "approvalMaxList";

	public ApprovalPreferencePage() {
		super(GRID);
	}

	/**
	 * 初期値が設定されたインスタンスを返します。
	 *
	 * @see org.eclipse.jface.preference.PreferencePage#setPreferenceStore(org.eclipse.jface.preference.IPreferenceStore)
	 * @see #initializeDefaults()
	 */
	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(ClusterControlPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * 設定フィールドを生成します。
	 *
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#addField(org.eclipse.jface.preference.FieldEditor)
	 */
	@Override
	public void createFieldEditors() {
		m_log.trace("ApprovalPreferencePage.createFieldEditors()");
		
		Composite parent = this.getFieldEditorParent();
		GridData gridData = null;
		// 承認ビュー関連
		Group group = new Group(parent, SWT.SHADOW_NONE);
		WidgetTestUtil.setTestId(this, null, group);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 10;
		gridData.verticalSpan = 10;
		group.setLayoutData(gridData);
		group.setText(Messages.getString("view.approval.list"));

		// メッセージ表示
		this.addField(new BooleanFieldEditor(P_APPROVAL_MESSAGE_FLG,
				Messages.getString("over.limit.message"), group));

		// 表示履歴数
		String[] args = {
				Integer.toString(1),
				Integer.toString(DataRangeConstant.SMALLINT_HIGH) };
		
		IntegerFieldEditor histories =
				new IntegerFieldEditor(
						P_APPROVAL_MAX_LIST,
						Messages.getString("number.of.display.list") + " : ", group);
		histories.setValidRange(1, DataRangeConstant.SMALLINT_HIGH);
		histories.setErrorMessage(Messages.getString("message.hinemos.8", args ));
		this.addField(histories);
	}

	/**
	 * ボタン押下時に設定反映します。
	 *
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		m_log.trace("ApprovalPreferencePage.performOk()");
		applySetting();
		return super.performOk();
	}

	/**
	 * 設定内容を反映します。
	 */
	private void applySetting() {
	}
}
