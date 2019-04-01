/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.composite;

import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.PropertyFieldColorConstant;
import com.clustercontrol.repository.action.GetNodeProperty;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.PropertySheet;
import com.clustercontrol.ws.repository.NodeInfo;

/**
 * ノード属性一覧コンポジットクラス<BR>
 *
 * @version 6.2.0
 */
public class NodeListAttributeComposite extends Composite {

	// ----- instance フィールド ----- //

	/** テーブルビューア */
	private PropertySheet m_propertySheet = null;

	/** ラベル */
	private Label m_titleLabel = null;

	/** ツリー */
	Tree m_tree = null;

	// ----- コンストラクタ ----- //

	/**
	 * インスタンスを返します。
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 */
	public NodeListAttributeComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	// ----- instance メソッド ----- //

	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		// ラベルを設定
		m_titleLabel = new Label(this, SWT.NONE);
		m_titleLabel.setText(Messages.getString("node.config") + " : ");
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_titleLabel.setLayoutData(gridData);

		m_tree = new Tree(this, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.MULTI);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_tree.setLayoutData(gridData);

		this.m_propertySheet = new PropertySheet(m_tree);

		this.m_propertySheet.addTreeListener(new ITreeViewerListener() {

			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				// シートの展開と色表示
				update();
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				// シートの展開と色表示
				update();
			}
		});
	}

	/**
	 * PropertySheetを返します。
	 *
	 * @return PropertySheet
	 */
	public PropertySheet getPropertySheet() {
		return this.m_propertySheet;
	}

	/**
	 * 指定されたノードの情報を表示します。
	 *
	 * @param facilityId ファシリティID
	 * @param targetDatetime 対象日時
	 * @param facilityId ファシリティID
	 */
	public void update(String managerName, String facilityId, Long targetDatetime, NodeInfo nodeFilterInfo) {
		Property property = null;
		GetNodeProperty getNodeProperty = new GetNodeProperty(managerName, facilityId,
				PropertyDefineConstant.MODE_SHOW, targetDatetime, nodeFilterInfo);
		property = getNodeProperty.getProperty(true);

		this.m_propertySheet.setInput(property);

		//レベル1までの展開
		this.m_propertySheet.expandToLevel(1);

		// ラベルの設定
		if (managerName != null && !managerName.isEmpty()
				&& facilityId != null && !facilityId.isEmpty()) {
			m_titleLabel.setText(Messages.getString("node.config") + " : " + facilityId + " (" + managerName + ")");
		} else {
			m_titleLabel.setText("");
		}

		for (TreeItem item : m_tree.getItems()) {
			setForegroundColor(item);
		}
	}

	/**
	 * 指定されたノードの情報を表示します。
	 *
	 * @param facilityId ファシリティID
	 * @param targetDatetime 対象日時
	 * @param facilityId ファシリティID
	 */
	public void update() {

		for (TreeItem item : m_tree.getItems()) {
			setForegroundColor(item);
		}
	}

	/**
	 * 該当箇所を強調表示します。
	 * @param item ツリー
	 */
	private void setForegroundColor(TreeItem item) {
		if (item == null) {
			return;
		}
		Property element = (Property)item.getData();
		if (element != null 
				&& element.getStringHighlight() != null
				&& element.getStringHighlight()) {
			item.setForeground(1, new Color(null, 255, 0, 0));
		} else {
			item.setForeground(1, PropertyFieldColorConstant.COLOR_FILLED);
		}

		for (TreeItem child : item.getItems()) {
			setForegroundColor(child);
		}
	}
}
