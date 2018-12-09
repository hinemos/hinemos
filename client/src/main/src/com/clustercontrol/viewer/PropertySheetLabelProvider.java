/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.viewer;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.clustercontrol.bean.CheckBoxImageConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.util.Messages;

/**
 * プロパティシートクラス用のLabelProviderクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class PropertySheetLabelProvider extends LabelProvider implements ITableLabelProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
	 *      int)
	 */
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (element instanceof Property && columnIndex == 1) {
			Property nodeProperty = (Property) element;
			if (nodeProperty.getEditor()
					.compareTo(PropertyDefineConstant.EDITOR_BOOL) == 0) {

				if (nodeProperty.getValue() instanceof Boolean) {
					Boolean value = (Boolean) nodeProperty.getValue();
					if (value.booleanValue()) {
						return CheckBoxImageConstant.typeToImage(true);
					} else {
						return CheckBoxImageConstant.typeToImage(false);
					}
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
	 *      int)
	 */
	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof Property) {
			Property nodeProperty = (Property) element;
			switch (columnIndex) {
			case 0:
				return nodeProperty.getName();
			case 1:
				if (nodeProperty.getModify() == PropertyDefineConstant.MODIFY_NG) {
					// 編集可能でない場合は、設定値を返す
					return nodeProperty.getValueText();
				}

				// 編集可能
				if (nodeProperty.getEditor().equals(PropertyDefineConstant.EDITOR_BOOL)) {
					// チェックボックス
					return nodeProperty.getValueText();
				} else if (nodeProperty.getEditor().equals(PropertyDefineConstant.EDITOR_SELECT)) {
					// コンボボックス
					return nodeProperty.getValueText().equals("") ? Messages.getString("dialog.column.select")  : nodeProperty.getValueText();
				} else {
					return nodeProperty.getValueText().equals("") ? Messages.getString("dialog.column.input")  : nodeProperty.getValueText();
				}
			default:
				break;
			}
		}
		return null;
	}

}
