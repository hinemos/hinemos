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

package com.clustercontrol.repository.preference;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * リポジトリ管理機能用の設定ページクラス<BR>
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class RepositoryPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/** リポジトリ[ノード]のプログラム実行設定 */
	public static final String P_PROGRAM_EXECUTION = "programExecution";

	private static final String MSG_PROGRAM_EXECUTION = Messages.getString("program.execution") + " : ";

	public RepositoryPreferencePage() {
		super(GRID);
	}

	/**
	 * 初期化します。
	 *
	 * @param workbench ワークベンチオブジェクト
	 */
	@Override
	public void init(IWorkbench workbench) {
		this.setPreferenceStore(ClusterControlPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * 設定フィールドを生成します。
	 */
	@Override
	public void createFieldEditors() {
		Composite parent = this.getFieldEditorParent();
		GridData gridData = null;

		// リポジトリ[ノード]関連
		Group nodeGroup = new Group( parent, SWT.SHADOW_NONE );
		WidgetTestUtil.setTestId( this, null, nodeGroup );
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 10;
		gridData.verticalSpan = 10;
		nodeGroup.setLayoutData( gridData );
		nodeGroup.setText( Messages.getString( "view.repository.node" ) );
		// 実行プログラム
		StringFieldEditor progExec = new StringFieldEditor( P_PROGRAM_EXECUTION, MSG_PROGRAM_EXECUTION, nodeGroup );
		progExec.setTextLimit( DataRangeConstant.VARCHAR_1024 );
		String[] args = { Integer.toString( DataRangeConstant.VARCHAR_1024 ) };
		progExec.setErrorMessage( Messages.getString( "message.hinemos.7", args ) );
		this.addField( progExec );

		// This setting is not available for Web Client. Disable and show message.
		// Note: RAP BUG? If there is no FieldEditor set, label will not be shown.
		if( ClusterControlPlugin.isRAP() ){
			progExec.getTextControl( nodeGroup ).setEnabled( false );

			Label lblNotAvailable = new Label( parent, SWT.LEFT );
			lblNotAvailable.setText( Messages.getString( "preferencepage.notavailable.message" ) );
			lblNotAvailable.setForeground( Display.getCurrent().getSystemColor( SWT.COLOR_RED ) );
			WidgetTestUtil.setTestId( this, "notavailable", nodeGroup );
		}
	}

	/**
	 * 設定内容を各ビューに反映します。
	 *
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		boolean result = super.performOk();

		this.applySetting();

		return result;
	}

	/**
	 * 設定内容を反映します。
	 */
	private void applySetting() {
		IPreferenceStore store = this.getPreferenceStore();

		// 実行プログラムを格納
		store.getString(P_PROGRAM_EXECUTION);
	}

}
