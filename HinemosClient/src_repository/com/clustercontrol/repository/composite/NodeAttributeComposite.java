/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.repository.action.GetNodeProperty;
import com.clustercontrol.viewer.PropertySheet;

/**
 * ノード属性一覧コンポジットクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class NodeAttributeComposite extends Composite {

	// ----- instance フィールド ----- //

	/** テーブルビューア */
	private PropertySheet propertySheet = null;

	// ----- コンストラクタ ----- //

	/**
	 * インスタンスを返します。
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 */
	public NodeAttributeComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	// ----- instance メソッド ----- //

	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		Tree table = new Tree(this, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.MULTI);
		WidgetTestUtil.setTestId(this, null, table);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		table.setLayoutData(gridData);

		this.propertySheet = new PropertySheet(table);
	}

	/**
	 * PropertySheetを返します。
	 *
	 * @return PropertySheet
	 */
	public PropertySheet getPropertySheet() {
		return this.propertySheet;
	}

	/**
	 * 指定されたノードの情報を表示します。
	 *
	 * @param facilityId
	 *            ファシリティID
	 */
	public void update(String managerName, String facilityId) {
		Property property = null;
		GetNodeProperty getNodeProperty = new GetNodeProperty(managerName, facilityId,
				PropertyDefineConstant.MODE_SHOW);
		property = getNodeProperty.getProperty(false);

		this.propertySheet.setInput(property);


		/*プロパティシートの展開を指定します。*/

		//レベル1までの展開
		this.propertySheet.expandToLevel(1);

		//サーバ基本情報
		Object element = this.propertySheet.getTree().getItem(5).getData();
		this.propertySheet.expandToLevel(element, 2);

		//構成情報
		element = this.propertySheet.getTree().getItem(6).getData();
		this.propertySheet.expandToLevel(element, 1);

		//サービス
		element =  this.propertySheet.getTree().getItem(7).getData();
		this.propertySheet.expandToLevel(element, 1);

		//デバイス
		element =  this.propertySheet.getTree().getItem(8).getData();
		this.propertySheet.expandToLevel(element, 1);

	}
}
