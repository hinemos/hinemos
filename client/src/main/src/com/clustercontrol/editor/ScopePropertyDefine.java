/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.editor;

import java.io.Serializable;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.Property;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * スコープツリープロパティ定義クラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class ScopePropertyDefine extends PropertyDefine implements Serializable {
	private static final long serialVersionUID = -55095307970824890L;

	/**
	 * コンストラクタ
	 */
	public ScopePropertyDefine() {
		m_cellEditor = new ScopeDialogCellEditor(false);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.clustercontrol.bean.PropertyDefine#getColumnText(java.lang.Object)
	 */
	@Override
	public String getColumnText(Object value) {
		//プロパティ値がファシリティツリーならば、スコープパスを表示
		if (value instanceof FacilityTreeItem) {
			FacilityPath path = new FacilityPath(ClusterControlPlugin
					.getDefault().getSeparator());
			return path.getPath((FacilityTreeItem) value);
		} else if (value instanceof String) {
			// 文字列の場合は、そのまま表示する。
			return (String) value;
		} else {
			return "";
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.clustercontrol.bean.PropertyDefine#getValue(com.clustercontrol.bean.Property)
	 */
	@Override
	public Object getValue(Property element) {
		//プロパティ値がファシリティツリーならば、スコープパスを表示
		Object value = element.getValue();
		if (value instanceof FacilityTreeItem) {
			FacilityPath path = new FacilityPath(ClusterControlPlugin
					.getDefault().getSeparator());
			return path.getPath((FacilityTreeItem) value);
		} else if (value instanceof String) {
			// 文字列の場合は、そのまま表示する。
			return value;
		} else {
			return "";
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.clustercontrol.bean.PropertyDefine#modify(com.clustercontrol.bean.Property,
	 *      java.lang.Object)
	 */
	@Override
	public void modify(Property element, Object value) {
		//変更値がファシリティツリーならば、プロパティ値に設定する
		if (value instanceof FacilityTreeItem) {
			element.setValue(value);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.clustercontrol.bean.PropertyDefine#initEditer()
	 */
	@Override
	public void initEditer() {

	}
}
