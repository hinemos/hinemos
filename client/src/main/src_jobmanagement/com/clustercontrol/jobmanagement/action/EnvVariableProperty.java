/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
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

/**
 * 環境変数用プロパティを作成するクライアント側アクションクラス<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class EnvVariableProperty {

	/** 環境変数ID */
	public static final String ID_ENV_VARIABLE_ID = "envVariableId";

	/** 値 */
	public static final String ID_VALUE = "value";

	/** 説明 */
	public static final String ID_DESCRIPTION = "description";

	/**
	 * 環境変数用プロパティを返します。
	 *
	 * <p>
	 * <ol>
	 *  <li>環境変数の設定項目毎にID, 名前, 処理定数（{@link com.clustercontrol.bean.PropertyDefineConstant}）を指定し、
	 *      プロパティ（{@link com.clustercontrol.bean.Property}）を生成します。</li>
	 *  <li>各設定項目のプロパティをツリー状に定義します。</li>
	 * </ol>
	 *
	 * <p>プロパティに定義する環境変数設定項目は、下記の通りです。
	 * <p>
	 * <ul>
	 *  <li>プロパティ（親。ダミー）
	 *  <ul>
	 *     <li>名前（子。テキスト）
	 *     <li>値（子。テキスト）
	 *     <li>説明（子。テキスト）
	 *    </ul>
	 *   </ul>
	 *  </ul>
	 * </ul>
	 *
	 * @return 環境変数用プロパティ
	 */
	public Property getProperty() {
		//プロパティ項目定義
		Property envVariableId = new Property(ID_ENV_VARIABLE_ID, Messages.getString("name"),
				PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		Property value = new Property(ID_VALUE, Messages.getString("value"),
				PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		Property description = new Property(ID_DESCRIPTION, Messages.getString("description"),
				PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);

		//値を初期化
		envVariableId.setValue("");
		value.setValue("");
		description.setValue("");

		//変更の可/不可を設定
		envVariableId.setModify(PropertyDefineConstant.MODIFY_OK);
		value.setModify(PropertyDefineConstant.MODIFY_OK);
		description.setModify(PropertyDefineConstant.MODIFY_OK);

		Property property = new Property(null, null, null);

		// 初期表示ツリーを構成。
		property.removeChildren();
		property.addChildren(envVariableId);
		property.addChildren(value);
		property.addChildren(description);

		return property;
	}
}
