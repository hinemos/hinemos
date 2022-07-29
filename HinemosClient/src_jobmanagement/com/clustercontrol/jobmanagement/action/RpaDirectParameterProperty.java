/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.action;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.util.Messages;

public class RpaDirectParameterProperty {
	/** パラメータ */
	public static final String ID_PARAMETER = "parameter";

	/** 説明 */
	public static final String ID_DESCRIPTION = "description";

	public Property getProperty() {
		Property parameter = new Property(ID_PARAMETER, Messages.getString("parameter"),
				PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property description = new Property(ID_DESCRIPTION, Messages.getString("description"),
				PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);

		// 値を初期化
		parameter.setValue("");
		description.setValue("");

		// 変更の可/不可を設定
		parameter.setModify(PropertyDefineConstant.MODIFY_OK);
		description.setModify(PropertyDefineConstant.MODIFY_OK);

		Property property = new Property(null, null, null);
		property.addChildren(parameter);
		property.addChildren(description);
		return property;
	}
}
