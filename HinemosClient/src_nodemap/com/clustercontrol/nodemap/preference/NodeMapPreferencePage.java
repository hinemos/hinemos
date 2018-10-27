/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.preference;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.nodemap.view.NodeMapView;

/**
 * NodeMapの設定ページクラス。
 * @since 1.0.0
 */
public class NodeMapPreferencePage extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage {

	/** ノードマップビューの自動更新フラグ */
	public static final String P_HISTORY_UPDATE_FLG = "historyUpdateFlg";

	/** ノードマップビューの自動更新周期 */
	public static final String P_HISTORY_UPDATE_CYCLE = "historyUpdateCycle";

	/** グリッドスナップフラグ **/
	public static final String P_GRID_SNAP_FLG = "gridSnapFlg";

	/** グリッド幅 **/
	public static final String P_GRID_WIDTH = "gridWidth";

	/** ノードマップビューのアイコンの背景の判断基準(ステータス) */
	public static final String P_ICON_BG_STATUS_FLG = "iconBgStatusFlg";

	/** ノードマップビューのアイコンの背景の判断基準(イベント) */
	public static final String P_ICON_BG_EVENT_FLG = "iconBgEventFlg";

	/**
	 * 初期値が設定されたインスタンスを返します。
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#setPreferenceStore(org.eclipse.jface.preference.IPreferenceStore)
	 * @see #initializeDefaults()
	 */
	public NodeMapPreferencePage() {
		super(GRID);
		this.setPreferenceStore(ClusterControlPlugin.getDefault()
				.getPreferenceStore());

		this.initializeDefaults();
	}

	/**
	 * 初期値を設定します。
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#getPreferenceStore()
	 * @see org.eclipse.jface.preference.IPreferenceStore
	 */
	private void initializeDefaults() {
		IPreferenceStore store = this.getPreferenceStore();

		store.setDefault(P_HISTORY_UPDATE_FLG, true);
		store.setDefault(P_HISTORY_UPDATE_CYCLE, 10);
		store.setDefault(P_GRID_SNAP_FLG, true);
		store.setDefault(P_GRID_WIDTH, 10);
		store.setDefault(P_ICON_BG_STATUS_FLG, true);
		store.setDefault(P_ICON_BG_EVENT_FLG, false);
	}

	/**
	 * 設定フィールドを生成します。
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#addField(org.eclipse.jface.preference.FieldEditor)
	 */
	@Override
	public void createFieldEditors() {
		Composite parent = this.getFieldEditorParent();
		GridData gridData = null;
		// ノードマップビュー関連
		Group group = new Group(parent, SWT.SHADOW_NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 10;
		gridData.verticalSpan = 10;
		group.setLayoutData(gridData);
		group.setText(com.clustercontrol.nodemap.messages.Messages.getString("nodemap"));

		// 自動更新フラグ
		this.addField(new BooleanFieldEditor(P_HISTORY_UPDATE_FLG,
				Messages.getString("autoupdate.enable"), group));

		// 自動更新周期
		IntegerFieldEditor cycle =
			new IntegerFieldEditor(
					P_HISTORY_UPDATE_CYCLE,
					Messages.getString("autoupdate.cycle") + " : ", group);
		cycle.setValidRange(1, DataRangeConstant.SMALLINT_HIGH);
		String[] argsCycle = {
				Integer.toString(1),
				Integer.toString(DataRangeConstant.SMALLINT_HIGH) };
		cycle.setErrorMessage(Messages.getString("message.hinemos.8", argsCycle ));
		this.addField(cycle);

		// スナップフラグ
		this.addField(new BooleanFieldEditor(P_GRID_SNAP_FLG,
				com.clustercontrol.nodemap.messages.Messages.getString("grid.snap"), group));

		// グリッド幅
		IntegerFieldEditor gridSnap =
			new IntegerFieldEditor(
					P_GRID_WIDTH,
					com.clustercontrol.nodemap.messages.Messages.getString("grid.width") + " : ", group);
		gridSnap.setValidRange(1, DataRangeConstant.SMALLINT_HIGH);
		String[] argsGridSnap = {
				Integer.toString(1),
				Integer.toString(DataRangeConstant.SMALLINT_HIGH) };
		gridSnap.setErrorMessage(Messages.getString("message.hinemos.8", argsGridSnap ));
		this.addField(gridSnap);

		// アイコン背景色(ステータス)
		this.addField(new BooleanFieldEditor(P_ICON_BG_STATUS_FLG,
				com.clustercontrol.nodemap.messages.Messages.getString("preference.status"), group));

		// アイコン背景色(イベント)
		this.addField(new BooleanFieldEditor(P_ICON_BG_EVENT_FLG,
				com.clustercontrol.nodemap.messages.Messages.getString("preference.event"), group));
	}

	/**
	 * 設定内容をビューに反映します。
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
	 * 設定内容をノードマップビューに反映します。
	 */
	protected void applySetting() {
		// 存在するNodeMapView全てに設定を適応する。
		IWorkbench workbench = ClusterControlPlugin.getDefault().getWorkbench();
		for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
			for (IWorkbenchPage page : window.getPages()) {
				for (IViewReference reference : page.getViewReferences()) {
					// eclipseの仕様によりgetId()の結果が「NodemapView.ID +":" + secondalyId」になってしまうので、startsWithを使用する
					if (reference.getId().startsWith(NodeMapView.ID)) {
						NodeMapView view = (NodeMapView)reference.getView(true);
						view.applySetting();
					}
				}
			}
		}
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
	}
}