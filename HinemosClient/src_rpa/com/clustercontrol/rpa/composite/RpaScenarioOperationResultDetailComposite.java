/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.composite;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.RpaScenarioOperationResultDetailResponse;
import org.openapitools.client.model.RpaScenarioOperationResultDetailResponse.PriorityEnum;
import org.openapitools.client.model.RpaScenarioOperationResultWithDetailResponse;

import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.rpa.action.GetRpaScenarioOperationResultDetailTableDefine;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * シナリオ実績詳細一覧コンポジットクラス<BR>
 */
public class RpaScenarioOperationResultDetailComposite extends Composite {

	/** テーブルビューアー */
	private CommonTableViewer tableViewer = null;
	/** マネージャ名 */
	private String managerName = null;
	/** シナリオ実績 */
	private RpaScenarioOperationResultWithDetailResponse result = null;

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public RpaScenarioOperationResultDetailComposite(Composite parent, int style, RpaScenarioOperationResultWithDetailResponse result) {
		super(parent, style);
		this.result = result;
		this.initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		final Table table = new Table(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION);
		WidgetTestUtil.setTestId(this, null, table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);

		// テーブルビューアの作成
		this.tableViewer = new CommonTableViewer(table);
		
		this.tableViewer.createTableColumn(GetRpaScenarioOperationResultDetailTableDefine.get(),
				GetRpaScenarioOperationResultDetailTableDefine.SORT_COLUMN_INDEX,
				GetRpaScenarioOperationResultDetailTableDefine.SORT_ORDER);
	}
	
	/**
	 * このコンポジットが利用するテーブルビューアーを返します。
	 *
	 * @return テーブルビューアー
	 */
	public CommonTableViewer getTableViewer() {
		return this.tableViewer;
	}

	/**
	 * このコンポジットが利用するテーブルを返します。
	 *
	 * @return テーブル
	 */
	public Table getTable() {
		return this.tableViewer.getTable();
	}

	/**
	 * コンポジットを更新します。<BR>
	 * タグ一覧情報を取得し、テーブルビューアーにセットします。
	 *
	 * @see com.clustercontrol.notify.action.GetNotify#getNotifyList()
	 */
	@Override
	public void update() {
		ArrayList<Object> listInput = new ArrayList<Object>();
		SimpleDateFormat df = TimezoneUtil.getSimpleDateFormat();
		
		for (RpaScenarioOperationResultDetailResponse info : result.getOperationResultDetail()) {
			ArrayList<Object> obj = new ArrayList<Object>();
			obj.add(df.format(new Date(info.getLogTime())));
			obj.add(PriorityMessage.enumToString(info.getPriority(), PriorityEnum.class));
			obj.add(info.getLog());
			obj.add(RpaScenarioOperationResultSearchComposite.convertTimeToHMS(info.getRunTime()));
			String coefficientCost = "";
			if (info.getCoefficientCost() != null){
				coefficientCost = info.getCoefficientCost().toString() + "%";
			} else {
				coefficientCost = "-";
			}
			obj.add(coefficientCost);
			obj.add(null);
			listInput.add(obj);
		}
		this.tableViewer.setInput(listInput);
	}

	/**
	 * @return the managerName
	 */
	public String getManagerName() {
		return managerName;
	}

	/**
	 * @param managerName the managerName to set
	 */
	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}
}
