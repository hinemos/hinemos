/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.util;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;

/**
 * 画面用ユーティリティクラス
 *
 */
public class SdmlUiUtil {
	public static final String[] PRIORITY_LIST = {
			PriorityMessage.STRING_INFO,
			PriorityMessage.STRING_WARNING,
			PriorityMessage.STRING_CRITICAL,
			PriorityMessage.STRING_UNKNOWN,
			PriorityMessage.STRING_NONE };

	/**
	 * 必須入力の背景色を設定
	 * 
	 * @param text
	 */
	public static void setColorRequired(Text text) {
		if (text == null) {
			return;
		}
		if ("".equals(text.getText())) {
			text.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			text.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 指定した重要度に該当する重要度用コンボボックスの項目を選択します。
	 *
	 * @param combo
	 *            重要度用コンボボックスのインスタンス
	 * @param priorityEnumValue
	 *            重要度
	 *
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public static void setSelectPriority(Combo combo, String select) {
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (select.equals(combo.getItem(i))) {
				combo.select(i);
				break;
			}
		}
	}
}
