/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.composite;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.FacilityInfoResponseP1;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.repository.action.GetNodeScopeTableDefine;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * ノードの割当スコープコンポジットクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class NodeScopeComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( NodeScopeComposite.class );

	// ----- instance フィールド ----- //

	/** ファシリティIDラベル */
	private Label facilityIdLabel = null;

	/** ファシリティ名ラベル */
	private Label facilityNameLabel = null;

	/** テーブルビューア */
	private CommonTableViewer tableViewer = null;

	// ----- コンストラクタ ----- //

	/**
	 * インスタンスを返します。
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 */
	public NodeScopeComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	// ----- instance メソッド ----- //

	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		this.facilityIdLabel = new Label(this, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "facilityid", facilityIdLabel);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		this.facilityIdLabel.setLayoutData(gridData);

		this.facilityNameLabel = new Label(this, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "facilityname", facilityNameLabel);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		this.facilityNameLabel.setLayoutData(gridData);

		Table table = new Table(this, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, null, table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);

		// テーブルビューアの作成
		this.tableViewer = new CommonTableViewer(table);
		this.tableViewer.createTableColumn(GetNodeScopeTableDefine.get(),
				GetNodeScopeTableDefine.SORT_COLUMN_INDEX,
				GetNodeScopeTableDefine.SORT_ORDER);
		for (int i = 0; i < table.getColumnCount(); i++){
			table.getColumn(i).setMoveable(true);
		}

	}

	/**
	 * tableViewerを返します。
	 *
	 * @return tableViewer
	 */
	public CommonTableViewer getTableViewer() {
		return this.tableViewer;
	}

	/**
	 * 指定されたノードの情報を表示します。
	 *
	 * @param facilityId
	 *            ファシリティID
	 * @param facilityName
	 *            ファシリティ名
	 */
	public void update(String managerName, String facilityId, String facilityName) {
		List<String> data = new ArrayList<>();

		if (facilityId != null) {
			try {
				RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(managerName);
				List<FacilityInfoResponseP1> dtoList = wrapper.getNodeScopeList(facilityId);
				for (FacilityInfoResponseP1 dto : dtoList) {
					data.add(dto.getFacilityId());
				}
			} catch (InvalidRole e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (Exception e) {
				m_log.warn("update(), " + e.getMessage(), e);
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}

			this.facilityIdLabel.setText(Messages.getString("facility.id")
					+ " : " + facilityId);
		} else {
			this.facilityIdLabel.setText(Messages.getString("facility.id")
					+ " : ");
		}

		if (facilityName != null) {
			this.facilityNameLabel.setText(Messages.getString("facility.name")
					+ " : " + facilityName);
		} else {
			this.facilityNameLabel.setText(Messages.getString("facility.name")
					+ " : ");
		}

		ArrayList<ArrayList<String>> dataInput = new ArrayList<ArrayList<String>>();
		for (String path : data) {
			ArrayList<String> a = new ArrayList<String>();
			a.add(HinemosMessage.replace(path));
			a.add(null);
			dataInput.add(a);
		}

		this.tableViewer.setInput(dataInput);
	}
}
