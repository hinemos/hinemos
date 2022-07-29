/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.JobRpaEndValueConditionInfoResponse;
import org.openapitools.client.model.JobRpaEndValueConditionInfoResponse.ConditionTypeEnum;

import com.clustercontrol.jobmanagement.action.GetRpaDirectEndValueConditionTableDefine;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * RPAシナリオ 直接実行 終了値判定条件テーブル用のコンポジットクラスです
 */
public class RpaDirectEndValueConditionComposite extends Composite {

	/** テーブルビューアー。 */
	private CommonTableViewer m_tableViewer = null;

	/** 終了値判定情報 */
	private List<JobRpaEndValueConditionInfoResponse> m_endValueConditionList = new ArrayList<>();

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int
	 *      style)
	 * @see #initialize()
	 */
	public RpaDirectEndValueConditionComposite(Composite parent, int style) {
		super(parent, style);

		this.initialize(parent);
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize(Composite parent) {
		GridLayout layout = new GridLayout(1, true);
		// テーブルとボタンの上部を揃える
		layout.marginHeight = 0;
		this.setLayout(layout);

		Table table = new Table(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(440, 100));

		// テーブルビューアの作成
		this.m_tableViewer = new CommonTableViewer(table);
		this.m_tableViewer.createTableColumn(GetRpaDirectEndValueConditionTableDefine.get(),
				GetRpaDirectEndValueConditionTableDefine.SORT_COLUMN_INDEX, GetRpaDirectEndValueConditionTableDefine.SORT_ORDER);
	}

	/**
	 * コンポジットを更新します。<BR>
	 * 判定情報一覧を取得し、テーブルビューアーにセットします。
	 *
	 * @see com.clustercontrol.monitor.run.util.StringValueInfoManager#get()
	 * @see com.clustercontrol.monitor.run.viewer.StringValueListTableViewer
	 */
	@Override
	public void update() {
		List<Object> listAll = new ArrayList<Object>();
		int order = 1;
		for (JobRpaEndValueConditionInfoResponse condition : m_endValueConditionList) {
			List<Object> list = new ArrayList<Object>();
			// 順序
			list.add(order); // 順序は常に1から振り直す
			condition.setOrderNo(order);
			order++;
			// 判定対象
			list.add(condition.getConditionType());
			if (condition.getConditionType() == ConditionTypeEnum.LOG) {
				// ファイルの内容で判定する場合
				// 判定条件
				// ファイルによる判定の場合は固定文字列
				list.add(Messages.getString("matching"));
				// 判定値
				// パターンマッチ表現
				list.add(condition.getPattern());
				// 終了値
				list.add(condition.getEndValue());
				// 処理
				if (condition.getProcessType()) {
					list.add(Messages.getString("rpa.condition.matched"));
				} else {
					list.add(Messages.getString("rpa.condition.not.matched"));
				}
				// 大文字・小文字を区別しない
				list.add(condition.getCaseSensitivityFlg());
			} else if (condition.getConditionType() == ConditionTypeEnum.RETURN_CODE) {
				// RPAツールのリターンコードで判定する場合
				// 判定条件
				// リターンコード判定条件
				list.add(condition.getReturnCodeCondition());
				// 判定値
				// リターンコード
				list.add(condition.getReturnCode());
				// 終了値
				if (condition.getUseCommandReturnCodeFlg()) {
					list.add(Messages.getString("rpa.use.return.code.as.end.value.2"));
				} else {
					list.add(condition.getEndValue());
				}
				// 処理
				list.add("");
				// 大文字・小文字を区別しない
				list.add(null);
			}
			// 説明
			list.add(condition.getDescription());
			listAll.add(list);
		}
		m_tableViewer.setInput(listAll);
	}

	/**
	 * 設定項目のリストを返します。
	 *
	 */
	public List<JobRpaEndValueConditionInfoResponse> getInputData() {
		return m_endValueConditionList;
	}

	/**
	 * 引数で指定された情報の値を、各項目に設定します。
	 *
	 */
	public void setInputData(List<JobRpaEndValueConditionInfoResponse> conditionList) {
		// テーブル更新
		m_endValueConditionList = conditionList;
		update();
	}

	/**
	 * テーブルビューワーを返します。
	 * @return テーブルビューワー
	 */
	public CommonTableViewer getTableViewer() {
		return m_tableViewer;
	}

	/**
	 * テーブルの行を選択します。
	 * @param index 選択行のインデックス
	 */
	public void setSelection(int index) {
		m_tableViewer.getTable().setSelection(index);
	}

	/**
	 * ログファイルによる終了値判定条件が設定されているかどうかを返します。
	 * @return true : 設定あり / false : 設定なし
	 */
	public boolean isLogConditionExisting() {
		return m_endValueConditionList.stream().filter(c -> c.getConditionType() == ConditionTypeEnum.LOG).findFirst()
				.isPresent();
	}
}
