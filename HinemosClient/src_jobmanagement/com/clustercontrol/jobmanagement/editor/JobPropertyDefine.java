/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.editor;

import com.clustercontrol.bean.Property;
import com.clustercontrol.editor.PropertyDefine;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

/**
 * ジョブ選択用のプロパティ定義クラスです。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class JobPropertyDefine extends PropertyDefine {
	private static final long serialVersionUID = -5940758348294063798L;

	/**
	 * コンストラクタ
	 *
	 * @see com.clustercontrol.jobmanagement.editor.JobDialogCellEditor#JobDialogCellEditor()
	 */
	public JobPropertyDefine(String managerName) {
		m_cellEditor = new JobDialogCellEditor(managerName, false);
	}

	/**
	 * コンストラクタ
	 *
	 * @param treeOnly true：ツリーのみ、false：ジョブ情報を含む
	 *
	 * @see com.clustercontrol.jobmanagement.editor.JobDialogCellEditor#JobDialogCellEditor(boolean)
	 */
	public JobPropertyDefine(String managerName, boolean treeOnly) {
		m_cellEditor = new JobDialogCellEditor(managerName, treeOnly);
	}

	/**
	 * コンストラクタ
	 *
	 * @param parentJobId 親ジョブID
	 * @param jobId ジョブID
	 *
	 * @see com.clustercontrol.jobmanagement.editor.JobDialogCellEditor#JobDialogCellEditor(String, String)
	 */
	public JobPropertyDefine(JobTreeItem item) {
		m_cellEditor = new JobDialogCellEditor(item);
	}

	/**
	 * コンストラクタ
	 *
	 * @param parentJobId 親ジョブID
	 * @param jobId ジョブID
	 *
	 * @see com.clustercontrol.jobmanagement.editor.JobDialogCellEditor#JobDialogCellEditor(String, String)
	 */
	public JobPropertyDefine(JobTreeItem item, Integer mode) {
		m_cellEditor = new JobDialogCellEditor(item, mode);
	}

	/**
	 * 引数で指定された値がジョブツリーアイテムならば、ジョブIDを返します。
	 *
	 * @param value 値
	 *
	 * @see com.clustercontrol.editor.PropertyDefine#getColumnText(java.lang.Object)
	 */
	@Override
	public String getColumnText(Object value) {
		//プロパティ値がジョブツリーならば、ジョブIDを表示
		if (value instanceof JobTreeItem) {
			return ((JobTreeItem) value).getData().getId();
		} else if (value instanceof String) {
			return (String) value;
		}
		return "";
	}

	/**
	 * 引数で指定されたプロパティの値がジョブツリーアイテムならば、ジョブIDを返します。
	 *
	 * @param element プロパティ
	 *
	 * @see com.clustercontrol.editor.PropertyDefine#getValue(com.clustercontrol.bean.Property)
	 */
	@Override
	public Object getValue(Property element) {
		//プロパティ値がジョブツリーならば、ジョブIDを表示
		Object value = element.getValue();
		if (value instanceof JobTreeItem) {
			return ((JobTreeItem) value).getData().getId();
		} else if (value instanceof String) {
			return value;
		}
		return "";
	}

	/**
	 * 引数で指定された値がジョブツリーアイテムならば、プロパティに値を設定します。
	 *
	 * @param element プロパティ
	 * @param value 値
	 *
	 * @see com.clustercontrol.editor.PropertyDefine#modify(com.clustercontrol.bean.Property, java.lang.Object)
	 */
	@Override
	public void modify(Property element, Object value) {
		//変更値がジョブツリーならば、プロパティ値に設定する
		if (value instanceof JobTreeItem) {
			element.setValue(value);
		}
	}

	/* (non-Javadoc)
	 * @see com.clustercontrol.bean.PropertyDefine#initEditer()
	 */
	@Override
	public void initEditer() {

	}
}
