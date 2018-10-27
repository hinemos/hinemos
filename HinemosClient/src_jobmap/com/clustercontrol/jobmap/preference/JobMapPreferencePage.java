/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.preference;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
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
import com.clustercontrol.jobmap.view.JobMapEditorView;
import com.clustercontrol.jobmap.view.JobMapHistoryView;
import com.clustercontrol.jobmap.view.JobMapView;

/**
 * NodeMapの設定ページクラス。
 * @since 1.0.0
 */
public class JobMapPreferencePage extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage {
	
	/** ジョブマップビューの最大ジョブ表示数 */
	public static final String P_MAX_DISPLAY_JOB = "maxDisplayJob";
	
	/** ジョブマップビューの最大階層表示数 */
	public static final String P_MAX_DISPLAY_DEPTH = "maxDisplayDepth";

	/** 自動展開 */
	public static final String P_AUTO_EXPAND = "autoExpand";
	
	/** 待ち条件の迂回 */
	public static final String P_DETOUR_CONNECTION = "detourConnection";
	
	/** 待ち条件のコンパクト表示 */
	public static final String P_COMPACT_CONNECTION = "compactConnection";
	
	/** 待ち条件の折り返し */
	public static final String P_TURN_CONNECTION ="turnConnection";
	
	/** 待ち条件の折り返し */
	public static final String P_TURN_WIDTH ="turnWidth";

	/** 矢印の白抜き */
	public static final String P_ARROW_WHITE = "arrowWhite";

	/** ラベル表示 */
	public static final String P_LABELING_ID = "labelingId";

	/** drag&drop時の挙動 */
	public static final String P_DRAGDROP_ID = "dragDropId";

	/**
	 * 初期値が設定されたインスタンスを返します。
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#setPreferenceStore(org.eclipse.jface.preference.IPreferenceStore)
	 * @see #initializeDefaults()
	 */
	public JobMapPreferencePage() {
		super(GRID);
		this.setPreferenceStore(ClusterControlPlugin.getDefault()
				.getPreferenceStore());
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
		group.setText(com.clustercontrol.jobmap.messages.Messages.getString("jobmap"));

		// 最大ジョブ数
		IntegerFieldEditor maxJob =
			new IntegerFieldEditor(
					P_MAX_DISPLAY_JOB,
					com.clustercontrol.jobmap.messages.Messages.getString("max.display.job") + " : ", group);
		maxJob.setValidRange(1, DataRangeConstant.SMALLINT_HIGH);
		String[] argsJob = {
				Integer.toString(1),
				Integer.toString(DataRangeConstant.SMALLINT_HIGH) };
		maxJob.setErrorMessage(Messages.getString("message.hinemos.8", argsJob ));
		this.addField(maxJob);

		// 最大階層数
		IntegerFieldEditor maxDepth =
			new IntegerFieldEditor(
					P_MAX_DISPLAY_DEPTH,
					com.clustercontrol.jobmap.messages.Messages.getString("max.display.depth") + " : ", group);
		maxDepth.setValidRange(1, DataRangeConstant.SMALLINT_HIGH);
		String[] argsDepth = {
				Integer.toString(1),
				Integer.toString(DataRangeConstant.SMALLINT_HIGH) };
		maxDepth.setErrorMessage(Messages.getString("message.hinemos.8", argsDepth ));
		this.addField(maxDepth);

		// 自動展開
		BooleanFieldEditor autoExpand =
			new BooleanFieldEditor(
					P_AUTO_EXPAND,
					com.clustercontrol.jobmap.messages.Messages.getString("auto.expand"), group);
		this.addField(autoExpand);
	
		// 矢印の白抜き
		BooleanFieldEditor arrowWhite = 
			new BooleanFieldEditor(
					P_ARROW_WHITE,
					com.clustercontrol.jobmap.messages.Messages.getString("arrow.white"), group);
		this.addField(arrowWhite);
		
		// 矢印の迂回
		BooleanFieldEditor detourConnection =
			new BooleanFieldEditor(
					P_DETOUR_CONNECTION,
					com.clustercontrol.jobmap.messages.Messages.getString("detour.connection"), group);
		this.addField(detourConnection);
		
		// コンパクト表示
		BooleanFieldEditor compactConnection =
			new BooleanFieldEditor(
					P_COMPACT_CONNECTION,
					com.clustercontrol.jobmap.messages.Messages.getString("compact.connection"), group);
		this.addField(compactConnection);
		
		// 折り返し機能
		BooleanFieldEditor turnConnection =
			new BooleanFieldEditor(
					P_TURN_CONNECTION,
					com.clustercontrol.jobmap.messages.Messages.getString("turn.connection"), group);
		this.addField(turnConnection);
		
		// 折り返し機能の幅
		IntegerFieldEditor turnWidth = 
				new IntegerFieldEditor(P_TURN_WIDTH, com.clustercontrol.jobmap.messages.Messages.getString("turn.width") + " : ", group);
		turnWidth.setValidRange(1, DataRangeConstant.SMALLINT_HIGH);
		String[] argsTurnWidth = {
				Integer.toString(1),
				Integer.toString(DataRangeConstant.SMALLINT_HIGH) };
		turnWidth.setErrorMessage(Messages.getString("message.hinemos.8", argsTurnWidth ));
		this.addField(turnWidth);

		// ジョブID、ジョブ名の切り替え
		RadioGroupFieldEditor labelingId= new RadioGroupFieldEditor(
				P_LABELING_ID,
				com.clustercontrol.jobmap.messages.Messages.getString("labeling") + " : ",
				2,
				new String[][] {
					{com.clustercontrol.jobmap.messages.Messages.getString("job.id"), "true"},
					{com.clustercontrol.jobmap.messages.Messages.getString("job.name"), "false"}
				},
				group);
		this.addField(labelingId);

		// ジョブのdrag&drop時の挙動
		RadioGroupFieldEditor dragDropId= new RadioGroupFieldEditor(
				P_DRAGDROP_ID,
				com.clustercontrol.jobmap.messages.Messages.getString("job.dragdrop") + " : ",
				2,
				new String[][] {
					{com.clustercontrol.jobmap.messages.Messages.getString("job.dragdrop.copy"), "true"},
					{com.clustercontrol.jobmap.messages.Messages.getString("job.dragdrop.references"), "false"}
				},
				group);
		this.addField(dragDropId);
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
	 * 設定内容をジョブマップビューに反映します。
	 */
	protected void applySetting() {
		// 存在するJobMapView全てに設定を適応する。
		IWorkbench workbench = ClusterControlPlugin.getDefault().getWorkbench();
		for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
			for (IWorkbenchPage page : window.getPages()) {
				for (IViewReference reference : page.getViewReferences()) {
					if (JobMapEditorView.ID.equals(reference.getId()) || JobMapHistoryView.ID.equals(reference.getId())) {
						JobMapView view = (JobMapView)reference.getView(true);
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