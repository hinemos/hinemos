/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.RequiredFieldColorConstant;

/**
 * SWTウィジェット関連のユーティリティです。
 */
public class WidgetUtil {

	/**
	 * 指定された {@link Text} が未入力の場合に必須を強調するスタイル(背景色ハイライト)になるようにします。
	 * <p>
	 * コントロールのイベントリスナーにより実現していますので、
	 * コントロールを所有しているコンポジットなどでスタイルの変更タイミングを管理する必要はありません。
	 */
	public static void applyRequiredStyleOnChange(Text textControl) {
		applyRequiredStyle(textControl);

		textControl.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				applyRequiredStyle((Text) event.widget);
			}
		});
	}

	/**
	 * 指定された {@link Text} が未入力の場合、必須を強調するスタイル(背景色ハイライト)にします。
	 */
	public static void applyRequiredStyle(Text textControl) {
		String v = textControl.getText();
		if (v == null || v.length() == 0) {
			textControl.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			textControl.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

}
