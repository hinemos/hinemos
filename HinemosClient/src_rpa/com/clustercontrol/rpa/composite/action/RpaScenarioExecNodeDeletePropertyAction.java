/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.composite.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.rpa.viewer.RpaScenarioExecNodePropertySheet;
import com.clustercontrol.util.Messages;

/**
 * プロパティを削除するクライアント側アクションクラス<BR>
 */
public class RpaScenarioExecNodeDeletePropertyAction extends Action {
	RpaScenarioExecNodePropertySheet viewer;

	/**
	 * コンストラクタ
	 * 
	 * @param viewer
	 */
	public RpaScenarioExecNodeDeletePropertyAction(RpaScenarioExecNodePropertySheet viewer) {
		super();

		setText(Messages.getString("delete"));

		//PropertySheetを保持
		this.viewer = viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		//選択されたプロパティを取得
		StructuredSelection selection = (StructuredSelection) this.viewer
				.getSelection();
		Property property = (Property) selection.getFirstElement();

		if (property.getCopy() == PropertyDefineConstant.COPY_OK) {
			//選択されたプロパティを削除
			property.getParent().removeChildren(property);
		}

		//PropertySheet更新
		this.viewer.refresh();
	}
}
