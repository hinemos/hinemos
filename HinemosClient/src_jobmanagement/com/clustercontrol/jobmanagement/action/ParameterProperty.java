/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.action;

import java.util.ArrayList;
import java.util.HashMap;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.jobmanagement.bean.JobParamTypeConstant;
import com.clustercontrol.jobmanagement.bean.JobParamTypeMessage;
import com.clustercontrol.util.Messages;

/**
 * ジョブ変数用プロパティを作成するクライアント側アクションクラス<BR>
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class ParameterProperty {

	/** パラメータID（ユーザ変数） */
	public static final String ID_USER_PARAM_ID = "useId";

	/** 種別 */
	public static final String ID_TYPE = "type";

	/** 値 */
	public static final String ID_VALUE = "value";

	/** 説明 */
	public static final String ID_DESCRIPTION = "description";

	/**
	 * ジョブ変数用プロパティを返します。
	 *
	 * <p>
	 * <ol>
	 *  <li>ジョブ変数の設定項目毎にID, 名前, 処理定数（{@link com.clustercontrol.bean.PropertyDefineConstant}）を指定し、
	 *      プロパティ（{@link com.clustercontrol.bean.Property}）を生成します。</li>
	 *  <li>各設定項目のプロパティをツリー状に定義します。</li>
	 * </ol>
	 *
	 * <p>プロパティに定義するジョブ変数設定項目は、下記の通りです。
	 * <p>
	 * <ul>
	 *  <li>プロパティ（親。ダミー）
	 *  <ul>
	 *   <li>種別（子。コンボボックス）
	 *   <ul>
	 *    <li>システム（種別の選択肢）
	 *    <ul>
	 *     <li>名前（孫。コンボボックス）
	 *     <li>説明（孫。テキスト）
	 *    </ul>
	 *    <li>ユーザ（種別の選択肢）
	 *    <ul>
	 *     <li>名前（孫。テキスト）
	 *     <li>値（孫。テキスト）
	 *     <li>説明（孫。テキスト）
	 *    </ul>
	 *   </ul>
	 *  </ul>
	 * </ul>
	 *
	 * @param type ジョブ変数の種別
	 * @return ジョブ変数用プロパティ
	 *
	 * @see com.clustercontrol.bean.Property
	 * @see com.clustercontrol.bean.PropertyDefineConstant
	 * @see com.clustercontrol.bean.JobParamTypeConstant
	 * @see com.clustercontrol.jobmanagement.bean.SystemParameterConstant
	 */
	public Property getProperty(int type) {
		//プロパティ項目定義
		Property useId = new Property(ID_USER_PARAM_ID, Messages.getString("name"),
				PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		Property paramType = new Property(ID_TYPE, Messages.getString("type"),
				PropertyDefineConstant.EDITOR_SELECT);
		Property value = new Property(ID_VALUE, Messages.getString("value"),
				PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property description = new Property(ID_DESCRIPTION, Messages.getString("description"),
				PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);

		//ユーザパラメータ
		ArrayList<Object> userList = new ArrayList<Object>();
		userList.add(useId);
		userList.add(value);
		userList.add(description);

		HashMap<String, Object> userListMap = new HashMap<String, Object>();
		userListMap.put("value", JobParamTypeMessage.STRING_USER);
		userListMap.put("property", userList);

		//種別コンボボックスの選択項目
		Object typeValues[][] = {
				{ JobParamTypeMessage.STRING_USER},
					{ userListMap} };

		paramType.setSelectValues(typeValues);

		//値を初期化
		useId.setValue("");
		paramType.setValue("");
		value.setValue("");
		description.setValue("");

		//変更の可/不可を設定
		useId.setModify(PropertyDefineConstant.MODIFY_OK);
		paramType.setModify(PropertyDefineConstant.MODIFY_OK);
		value.setModify(PropertyDefineConstant.MODIFY_OK);
		description.setModify(PropertyDefineConstant.MODIFY_OK);

		Property property = new Property(null, null, null);

		if (type == JobParamTypeConstant.TYPE_USER) {
			paramType.setValue(JobParamTypeMessage.STRING_USER);

			// 初期表示ツリーを構成。
			property.removeChildren();
			property.addChildren(paramType);

			// 判定対象ツリー
			paramType.removeChildren();
			paramType.addChildren(useId);
			paramType.addChildren(value);
			paramType.addChildren(description);
		}

		return property;
	}
}
