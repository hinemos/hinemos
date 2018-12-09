/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.composite.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.PropertySheet;

/**
 * プロパティを削除するクライアント側アクションクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class DeletePropertyAction extends Action {
	PropertySheet m_viewer;

	/**
	 * コンストラクタ
	 * 
	 * @param viewer
	 */
	public DeletePropertyAction(PropertySheet viewer) {
		super();

		setText(Messages.getString("delete"));

		//PropertySheetを保持
		m_viewer = viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		//選択されたプロパティを取得
		StructuredSelection selection = (StructuredSelection) m_viewer
				.getSelection();
		Property property = (Property) selection.getFirstElement();

		if (property.getCopy() == PropertyDefineConstant.COPY_OK) {
			//選択されたプロパティを削除
			property.getParent().removeChildren(property);
		}

		//PropertySheet更新
		m_viewer.refresh();
	}
}
