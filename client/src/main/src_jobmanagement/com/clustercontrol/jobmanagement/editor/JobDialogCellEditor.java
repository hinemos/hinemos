/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.editor;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.widgets.Control;

import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.dialog.JobTreeDialog;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

/**
 * ジョブ選択用のDialogCellEditorです。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class JobDialogCellEditor extends DialogCellEditor {
	/** ツリーのみフラグ */
	private boolean m_treeOnly = false;

	private JobTreeItem m_jobTreeItem = null;
	
	private Integer m_mode = null;

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
	 * @param parentJobId 親ジョブID
	 * @param jobId ジョブID
	 */
	public JobDialogCellEditor(JobTreeItem jobTreeItem) {
		super();
		m_treeOnly = true;
		m_jobTreeItem = jobTreeItem;
	}

	/**
	 * コンストラクタ
	 *
	 * @param parentJobId 親ジョブID
	 * @param jobId ジョブID
	 */
	public JobDialogCellEditor(JobTreeItem jobTreeItem, Integer mode) {
		super();
		m_treeOnly = true;
		m_jobTreeItem = jobTreeItem;
		m_mode = mode;
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
			//TODO: 修正
			dialog = new JobTreeDialog(cellEditorWindow.getShell(), null, null, m_treeOnly);
		} else if (m_mode != null) {
			dialog = new JobTreeDialog(cellEditorWindow.getShell(), null, m_jobTreeItem, m_mode);
		} else {
			dialog = new JobTreeDialog(cellEditorWindow.getShell(), null, m_jobTreeItem);
		}
		dialog.open();
		//選択したジョブツリーアイテムを取得する
		JobTreeItem item = null;
		if (dialog.getReturnCode() == JobTreeDialog.OK) {
			item = dialog.getSelectItem().isEmpty() ? null : dialog.getSelectItem().get(0);
			if (item != null && item.getData().getType() == JobConstant.TYPE_COMPOSITE) {
				item = null;
			}
		}
		return item;
	}
}
