/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.rpa.util.ReturnCodeConditionChecker;

/**
 * ジョブ作成ダイアログ作成用ユーティリティクラス
 *
 * @version 5.1.0
 */
public class JobDialogUtil {

	/**
	 * ジョブダイアログで使用されるコンポジットの親Layoutを返す
	 * 
	 * @return RowLayout
	 */
	public static RowLayout getParentLayout() {
		RowLayout layout = new RowLayout();
		layout.type = SWT.VERTICAL;
		layout.spacing = 1;
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.fill = true;
		return layout;
	}

	/**
	 * マージンに0が設定されたCompositeを返す
	 * 
	 * @param parent 親Composite
	 * @return Composite
	 */
	public static Composite getComposite_MarginZero(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		RowLayout rowLayout = new RowLayout();
		rowLayout.marginTop = 0;
		rowLayout.marginRight = 0;
		rowLayout.marginLeft = 0;
		rowLayout.marginBottom = 0;
		composite.setLayout(rowLayout);
		return composite;
	}

	/**
	 * 指定したサイズの空Compositeを返す
	 * 
	 * @param parent 親Composite
	 * @param width 幅
	 * @param height 高さ
	 * @return Composite
	 */
	public static Composite getComposite_Space(Composite parent, int width, int height) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new RowData(width, height));
		return composite;
	}

	/**
	 * 項目の区切りに使用する空ラベルを返す
	 * 
	 * @param parent 親Composite
	 * @return 空のLabel
	 */
	public static Label getSeparator(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new RowData(10, 5));
		return label;
	}

	/**
	 * 項目の区切りに使用する空ラベルを返す
	 * 
	 * @param parent 親Composite
	 * @return 空のLabel
	 */
	public static Label getGridSeparator(Composite parent, int horizontalSpan) {
		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(10, 5));
		((GridData)label.getLayoutData()).horizontalSpan = horizontalSpan;
		return label;
	}
	
	public static ValidateResult getValidateResult(String id, String message) {
		ValidateResult result = new ValidateResult();
		result.setValid(false);
		result.setID(id);
		result.setMessage(message);
		return result;
	}
	
	public static boolean validateText(Text text) {
		return text.getText() != null && !text.getText().isEmpty();
	}
	
	public static boolean validateNumberText(Text text) {
		if (validateText(text)) {
			try {
				Integer.valueOf(text.getText()); 
				return true;
			} catch(NumberFormatException e) {
				return false;
			}
		}
		return false;
	}
	
	public static boolean validateReturnCodeText(Text text) {
		if (validateText(text)) {
			// 単一指定、範囲指定にマッチする正規表現
			return text.getText().matches(ReturnCodeConditionChecker.CONDITION_REGEX);
		}
		return false;
	}
}
