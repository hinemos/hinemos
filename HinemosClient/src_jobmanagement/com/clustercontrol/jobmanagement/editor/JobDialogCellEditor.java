/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.editor;

import java.util.List;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.widgets.Control;

import com.clustercontrol.jobmanagement.dialog.JobTreeDialog;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;

/**
 * ジョブ選択用のDialogCellEditorです。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class JobDialogCellEditor extends DialogCellEditor {

	private JobTreeItemWrapper m_jobTreeItem = null;
	/** ツリーのみフラグ */
	private boolean m_treeOnly = false;

	/**
	 * 表示ツリーの形式
	 * 値として、JobConstantクラスで定義したものが入る
	 * @see com.clustercontrol.jobmanagement.bean.JobConstant
	 *  null : 選択したユニット、ネットの子のみ表示する
	 *  TYPE_REFERJOB,TYPE_REFERJOBNET		: 選択したユニット、ネットの所属するジョブユニット以下すべて表示する
	 */
	private JobInfoWrapper.TypeEnum m_mode = null;

	/**
	 * 表示するジョブ種別のリスト
	 * 値として、JobConstantクラスで定義したものが入る
	 * @see com.clustercontrol.jobmanagement.bean.JobConstant
	 *  null : 全てのユニット、ネット
	 */
	private List<JobInfoWrapper.TypeEnum> m_targetJobTypeList = null;
	/**
	 * コンストラクタ
	 */
	public JobDialogCellEditor() {
		super();
		m_treeOnly = false;
	}

	/**
	 * コンストラクタ
	 *
	 * @pram managerName マネージャ名
	 * @param treeOnly true：ツリーのみ、false：ジョブ情報を含む
	 */
	public JobDialogCellEditor(String managerName, boolean treeOnly) {
		super();
		m_treeOnly = treeOnly;
		m_jobTreeItem = null;
	}

	/**
	 * コンストラクタ
	 *
	 * @param JobTreeItemWrapper
	 *            ジョブツリー
	 */
	public JobDialogCellEditor(JobTreeItemWrapper jobTreeItem) {
		super();
		m_treeOnly = true;
		m_jobTreeItem = jobTreeItem;
	}

	/**
	 * コンストラクタ
	 * @param jobTreeItem
	 * @param mode
	 *            表示元ジョブ種別
	 * @param targetJobTypeList
	 *            表示対象のジョブ種別
	 */
	public JobDialogCellEditor(JobTreeItemWrapper jobTreeItem, JobInfoWrapper.TypeEnum mode,
			List<JobInfoWrapper.TypeEnum> targetJobTypeList) {
		super();
		m_treeOnly = true;
		m_jobTreeItem = jobTreeItem;
		m_mode = mode;
		m_targetJobTypeList = targetJobTypeList;
	}

	/**
	 * ジョブ選択ダイアログを表示します。
	 *
	 * @see org.eclipse.jface.viewers.DialogCellEditor#openDialogBox(org.eclipse.swt.widgets.Control)
	 */
	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		//ジョブツリーダイアログを表示する
		JobTreeDialog dialog = null;
		if (m_jobTreeItem == null) {
			dialog = new JobTreeDialog(cellEditorWindow.getShell(), null, null, m_treeOnly);
		} else {
			dialog = new JobTreeDialog(cellEditorWindow.getShell(), null, m_jobTreeItem, m_mode, m_targetJobTypeList);
		}
		dialog.open();
		JobTreeItemWrapper item = getSelectJobItem(dialog);
		return item;
	}

	/**
	 * 選択したジョブツリーアイテムを取得する
	 *
	 * @param dialog
	 * @return
	 */
	private JobTreeItemWrapper getSelectJobItem(JobTreeDialog dialog) {
		JobTreeItemWrapper item = null;
		if (dialog.getReturnCode() == JobTreeDialog.OK) {
			item = dialog.getSelectItem().isEmpty() ? null : dialog.getSelectItem().get(0);
			if (item != null && item.getData().getType() == JobInfoWrapper.TypeEnum.COMPOSITE) {
				item = null;
			}
		}
		return item;
	}
}
