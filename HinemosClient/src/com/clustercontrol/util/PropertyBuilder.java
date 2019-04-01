/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.util.Date;

import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.editor.PropertyDefine;

/**
 * Propertyオブジェクトの生成コードを簡略化します。
 *
 * @since 6.2.0
 */
public class PropertyBuilder {
	
	private Property prop;

	private String id;
	private String name;
	private String editor;
	private Object value;
	private PropertyDefine define;
	private Boolean modifiable;
	private Boolean copiable;
	private Object options[][];
	private Long upperBound;
	private Long lowerBound;
	
	public PropertyBuilder(String id, String nameKey, String editor) {
		this.id = id;
		this.name = Messages.get(nameKey);
		this.editor = editor;
	}

	public PropertyBuilder setValue(Object value) {
		this.value = value;
		return this;
	}

	public PropertyBuilder setDefine(PropertyDefine define) {
		this.define = define;
		return this;
	}

	public PropertyBuilder setModifiable(boolean modifiable) {
		this.modifiable = modifiable;
		return this;
	}

	public PropertyBuilder setCopiable(boolean copiable) {
		this.copiable = copiable;
		return this;
	}

	public PropertyBuilder setOptions(Object[][] options) {
		this.options = options;
		return this;
	}

	public PropertyBuilder setOptions(Object[] options) {
		this.options = new Object[][] { options, options };
		return this;
	}

	public PropertyBuilder setUpperBound(long upperBound) {
		this.upperBound = upperBound;
		return this;
	}

	public PropertyBuilder setLowerBound(long lowerBound) {
		this.lowerBound = lowerBound;
		return this;
	}

	public Property build() {
		prop = new Property(id, name, editor);
		
		if (define != null) prop.setDefine(define);
		if (modifiable != null) prop.setModify(
				modifiable.booleanValue() ? PropertyDefineConstant.MODIFY_OK : PropertyDefineConstant.MODIFY_NG);
		if (copiable != null) prop.setCopy(
				copiable.booleanValue() ? PropertyDefineConstant.COPY_OK : PropertyDefineConstant.COPY_NG);
		if (options != null) prop.setSelectValues(options);
		if (upperBound != null) {
			if (PropertyDefineConstant.EDITOR_TEXT.equals(editor) || PropertyDefineConstant.EDITOR_TEXTAREA.equals(editor)) {
				prop.setStringUpperValue(upperBound.intValue());
			} else {
				prop.setUpperBound(upperBound.longValue());
			}
		}
		if (lowerBound != null) prop.setLowerBound(lowerBound.longValue());

		// 値を変換して設定する
		Object v = value;
		if (value == null) {
			v = "";
		} else {
			// Longを日時値として設定
			if (PropertyDefineConstant.EDITOR_DATETIME.equals(editor) && value instanceof Long) {
				v = new Date(((Long) value).longValue());
			}
		}
		prop.setValue(v);

		return prop;
	}
}
