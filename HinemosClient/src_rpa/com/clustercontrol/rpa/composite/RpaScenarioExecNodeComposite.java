/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.composite;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.GetRpaScenarioExecNodeDataResponse;

import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.rpa.action.GetRpaScenarioExecNodeTableDefine;
import com.clustercontrol.rpa.util.RpaScenarioDialogUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * ノード情報タブ用のコンポジットクラスです。
 */
public class RpaScenarioExecNodeComposite extends Composite {
	/** ログ出力のインスタンス */
	private static Log log = LogFactory.getLog( RpaScenarioExecNodeComposite.class );
	/** テーブルビューア */
	private CommonTableViewer viewer = null;
	
	/** 共通のシナリオチェックボタン */
	private Button commonScenarioCondition = null;
	/** 実行ノード ファシリティID */
	private List<GetRpaScenarioExecNodeDataResponse> execNodeList = null;

	/**
	 * コンストラクタ
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public RpaScenarioExecNodeComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * コンポジットを構築します。
	 * 
	 * @param jobType ジョブタイプ
	 */
	private void initialize() {

		this.setLayout(RpaScenarioDialogUtil.getParentLayout());

		// 共通のシナリオ（チェック）
		this.commonScenarioCondition = new Button(RpaScenarioDialogUtil.getComposite_MarginZero(this), SWT.CHECK);
		this.commonScenarioCondition.setText(Messages.getString("rpa.scenario.common"));
		this.commonScenarioCondition.setLayoutData(new RowData(220, SizeConstant.SIZE_BUTTON_HEIGHT + 5));
		this.commonScenarioCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// 判定対象一覧（ラベル）
		Label tableTitle = new Label(this, SWT.NONE);
		tableTitle.setText(Messages.getString("rpa.scenario.exec.node") + " ： ");

		// 判定対象一覧（テーブル）
		int addTableWidth = 170;

		Table table = new Table(this, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new RowData(430 + addTableWidth, 100));

		this.viewer = new CommonTableViewer(table);
		this.viewer.createTableColumn(GetRpaScenarioExecNodeTableDefine.get(),
				GetRpaScenarioExecNodeTableDefine.SORT_COLUMN_INDEX,
				GetRpaScenarioExecNodeTableDefine.SORT_ORDER);
	}
	
	/**
	 * 実行ノード情報をコンポジットに反映します。
	 */
	public void reflectExecNodeInfo() {
		if (this.execNodeList != null) {
			List<GetRpaScenarioExecNodeDataResponse> list = this.execNodeList;
			ArrayList<Object> tableData = new ArrayList<Object>();
			for (int i = 0; i < list.size(); i++) {
				GetRpaScenarioExecNodeDataResponse info = list.get(i);
				ArrayList<Object> tableLineData = new ArrayList<Object>();
				tableLineData.add(info.getExecNode());
				tableLineData.add(info.getExecNodeName());
				tableData.add(tableLineData);
			}
			viewer.setInput(tableData);
		}
	}

	@Override
	public void update() {
	}

	/**
	 * 共通のシナリオを設定します。
	 *
	 * @param start 共通のシナリオ
	 */
	public void setCommonNodeScenario(Boolean start) {
		this.commonScenarioCondition.setSelection(start);
	}

	/**
	 * 共通のシナリオ情報を返します。
	 *
	 * @return 共通のシナリオ情報
	 */
	public Boolean getCommonNodeScenario() {
		return this.commonScenarioCondition.getSelection();
	}

	/**
	 * 実行ノード ファシリティIDを設定します。
	 *
	 * @param start 実行ノード
	 */
	public void setExecNodeList(List<GetRpaScenarioExecNodeDataResponse> execNodeList) {
		log.debug("execNodeList = " + execNodeList);
		this.execNodeList = execNodeList;
	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		commonScenarioCondition.setEnabled(enabled);
	}
}
