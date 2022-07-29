/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.editor;

import java.io.Serializable;
import java.util.Date;

import com.clustercontrol.bean.Property;
import com.clustercontrol.util.TimezoneUtil;

/**
 * 日時プロパティ定義クラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class DateTimePropertyDefine extends PropertyDefine implements Serializable {
	private static final long serialVersionUID = 2848655498726180960L;

	/**
	 * コンストラクタ
	 */
	public DateTimePropertyDefine() {
		m_cellEditor = new DateTimeDialogCellEditor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.bean.PropertyDefine#getColumnText(java.lang.Object)
	 */
	@Override
	public String getColumnText(Object value) {
		if (value instanceof Date) {
			return TimezoneUtil.getSimpleDateFormat().format((Date) value);
		}
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.bean.PropertyDefine#getValue(com.clustercontrol.bean.Property)
	 */
	@Override
	public Object getValue(Property element) {
		Object value = element.getValue();
		if (value instanceof Date) {
			return TimezoneUtil.getSimpleDateFormat().format((Date) value);
		}
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.clustercontrol.bean.PropertyDefine#modify(com.clustercontrol.bean.Property,
	 *      java.lang.Object)
	 */
	@Override
	public void modify(Property element, Object value) {
		if (value instanceof Date) {
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
