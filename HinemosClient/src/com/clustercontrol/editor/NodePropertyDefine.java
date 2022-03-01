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
import com.clustercontrol.repository.util.FacilityTreeItemResponse;

/**
 * ノードツリープロパティ定義クラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class NodePropertyDefine extends PropertyDefine implements Serializable {

	private static final long serialVersionUID = 3563671541514184052L;

	/**
	 * コンストラクタ
	 */
	public NodePropertyDefine() {
		m_cellEditor = new ScopeDialogCellEditor(true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.clustercontrol.bean.PropertyDefine#getColumnText(java.lang.Object)
	 */
	@Override
	public String getColumnText(Object value) {
		//プロパティ値がファシリティツリーならば、スコープパスを表示
		if (value instanceof FacilityTreeItemResponse) {
			FacilityPath path = new FacilityPath(ClusterControlPlugin
					.getDefault().getSeparator());
			return path.getPath((FacilityTreeItemResponse) value);
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
		if (value instanceof FacilityTreeItemResponse) {
			FacilityPath path = new FacilityPath(ClusterControlPlugin
					.getDefault().getSeparator());
			return path.getPath((FacilityTreeItemResponse) value);
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
		if (value instanceof FacilityTreeItemResponse) {
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
