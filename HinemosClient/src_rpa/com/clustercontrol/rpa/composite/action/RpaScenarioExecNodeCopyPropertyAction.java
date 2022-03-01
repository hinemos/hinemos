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
import com.clustercontrol.util.PropertyUtil;

/**
 * プロパティをコピーするクライアント側アクションクラス<BR>
 */
public class RpaScenarioExecNodeCopyPropertyAction extends Action {
	RpaScenarioExecNodePropertySheet viewer;

	/**
	 * コンストラクタ
	 * 
	 * @param viewer
	 */
	public RpaScenarioExecNodeCopyPropertyAction(RpaScenarioExecNodePropertySheet viewer) {
		super();

		setText(Messages.getString("copy"));

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
			//選択されたプロパティのコピーを作成
			Property clone = PropertyUtil.copy(property);

			//コピーしたプロパティを、選択したプロパティの直後に追加する
			int index = PropertyUtil.getPropertyIndex((Property) property
					.getParent(), property);
			if (index != -1) {
				property.getParent().addChildren(clone, index + 1);
			} else {
				property.getParent().addChildren(clone);
			}
		}

		//PropertySheet更新
		this.viewer.refresh();
	}
}
