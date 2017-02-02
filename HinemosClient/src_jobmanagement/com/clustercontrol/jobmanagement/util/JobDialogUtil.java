/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

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

}
