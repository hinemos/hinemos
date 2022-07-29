/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

import com.clustercontrol.ClusterControlPlugin;

/**
 * 共通入力チェック<BR>
 *
 * @version 6.1.0
 * @since 6.1.0
 */
public class CommonVerifyListener {

	// staticフィールド.
	/** 数値のみ入力リスナー */
	public static VerifyListener onlyNumelicListener = null;
	/** 16進数文字列のみ入力リスナー(先頭0x) */
	public static VerifyListener onlyHexStrListener = null;

	/** staticフィールド定義 */
	static {
		VerifyListener onlyNumelic = new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				String text = e.text;
				if (e.character == SWT.BS || e.character == SWT.DEL) {
					return;
				}
				if (e.text.equals("")) {
					return;
				}
				if (!text.matches("^[0-9]+$")) {
					e.doit = false;
				}
			}
		};
		onlyNumelicListener = onlyNumelic;

		VerifyListener onlyHexStr = new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				String text = e.text;
				if (e.character == SWT.BS || e.character == SWT.DEL) {
					return;
				}
				if (e.text.equals("")) {
					return;
				}
				if (ClusterControlPlugin.isRAP()) {
					if (!text.matches("^0x[a-fA-F0-9]*$") && !text.matches("^0x$") && !text.matches("^0$")) {
						e.doit = false;
					}
				} else {
					if (!text.matches("^[a-fA-F0-9]+$")) {
						e.doit = false;
					}
				}
			}
		};
		onlyHexStrListener = onlyHexStr;
	}
}
