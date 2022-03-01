/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.viewer;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.Item;

import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;

/**
 * プロパティシートクラス用のModifierクラス<BR>
 */
public class RpaScenarioExecNodePropertySheetModifier implements ICellModifier {
	private RpaScenarioExecNodePropertySheet viewer;

	/**
	 * コンストラクタ
	 * 
	 * @param viewer
	 */
	public RpaScenarioExecNodePropertySheetModifier(RpaScenarioExecNodePropertySheet viewer) {
		this.viewer = viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object,
	 *      java.lang.String)
	 */
	@Override
	public boolean canModify(Object element, String property) {
		if (RpaScenarioExecNodePropertySheet.CLMN_VALUE.equals(property)) {
			if (element instanceof Property) {

				Property nodeProperty = (Property) element;

				if (nodeProperty.getModify() == PropertyDefineConstant.MODIFY_OK) {
					// ここでエディターを定義体から取り出して、エディターを切り替える。
					CellEditor cellEditor = nodeProperty.getCellEditor();

					if (cellEditor.getControl() == null || cellEditor.getControl().isDisposed()) {
						cellEditor.dispose();
						cellEditor.create(viewer.getTree());
						nodeProperty.initEditer();
					}

					CellEditor[] editors = new CellEditor[] { null, cellEditor };
					viewer.setCellEditors(editors);

					return true;
				}
				else{
					if(nodeProperty.getEditor().compareTo(
							PropertyDefineConstant.EDITOR_TEXTAREA) == 0){
						// ここでエディターを定義体から取り出して、エディターを切り替える。
						CellEditor cellEditor = nodeProperty.getCellEditor();

						if (cellEditor.getControl() == null || cellEditor.getControl().isDisposed()) {
							cellEditor.dispose();
							cellEditor.create(viewer.getTree());
							nodeProperty.initEditer();
						}

						CellEditor[] editors = new CellEditor[] { null, cellEditor };
						viewer.setCellEditors(editors);

						return true;
					}
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object,
	 *      java.lang.String)
	 */
	@Override
	public Object getValue(Object element, String property) {
		if (RpaScenarioExecNodePropertySheet.CLMN_VALUE.equals(property)) {
			//定義体から値を取り出す
			if (element instanceof Property) {
				Property nodeProperty = (Property) element;
				return nodeProperty.getModifyValue();
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object,
	 *      java.lang.String, java.lang.Object)
	 */
	@Override
	public void modify(Object element, String property, Object value) {
		if (RpaScenarioExecNodePropertySheet.CLMN_VALUE.equals(property)) {

			//定義体からの値更新用メソッドを呼び出す
			if (element instanceof Item) {
				element = ((Item) element).getData();

				if (element instanceof Property) {

					//値の更新用メソッドを呼び出す
					Property nodeProperty = (Property) element;
					nodeProperty.modify(value);
					viewer.expandToLevel(nodeProperty, 1);

					// ビューワを更新
					viewer.refresh();
				}
			}
		}
	}
}
