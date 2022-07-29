/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.restaccess.composite;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.RestAccessInfoResponse;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.notify.restaccess.action.GetRestAccessInfo;
import com.clustercontrol.notify.restaccess.action.GetRestAccessInfoListTableDefine;
import com.clustercontrol.notify.restaccess.composite.action.RestAccessInfoDoubleClickListener;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.util.WidgetTestUtil;
;

/**
 * RESTアクセス情報一覧コンポジットクラス<BR>
 *
 */
public class RestAccessInfoListComposite extends Composite {

	/** テーブルビューアー。 */
	private CommonTableViewer tableViewer = null;

	/** 合計ラベル */
	private Label totalLabel = null;

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
	public RestAccessInfoListComposite(Composite parent, int style) {
		super(parent, style);

		this.initialize();
	}

	/**
	 * コンポジットを配置します。
	 *
	 * @see com.clustercontrol.notify.action.GetRestAccessInfoListTableDefine#get()
	 * @see #update()
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		Table table = new Table(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION);
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
		this.tableViewer.createTableColumn(GetRestAccessInfoListTableDefine.get(),
				GetRestAccessInfoListTableDefine.SORT_COLUMN_INDEX1,
				GetRestAccessInfoListTableDefine.SORT_COLUMN_INDEX2,
				GetRestAccessInfoListTableDefine.SORT_ORDER);

		for (int i = 0; i < table.getColumnCount(); i++){
			table.getColumn(i).setMoveable(true);
		}

		// ダブルクリックリスナの追加
		this.tableViewer.addDoubleClickListener(new RestAccessInfoDoubleClickListener(this));

		// 合計ラベルの作成
		this.totalLabel = new Label(this, SWT.RIGHT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		this.totalLabel.setLayoutData(gridData);

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
	 * RESTアクセス情報一覧情報を取得し、テーブルビューアーにセットします。
	 *
	 * @see com.clustercontrol.notify.restaccess.action.GetRestAccessInfo#getRestAccessInfoList()
	 */
	@Override
	public void update() {
		// データ取得
		Map<String, List<RestAccessInfoResponse>> dispDataMap = new GetRestAccessInfo().getRestAccessInfoList();
		ArrayList<ArrayList<Object>> listInput = new ArrayList<ArrayList<Object>>();

		int cnt = 0;
		for(Map.Entry<String, List<RestAccessInfoResponse>> entrySet : dispDataMap.entrySet()) {
			List<RestAccessInfoResponse> list = entrySet.getValue();
			if(list == null){
				list = new ArrayList<RestAccessInfoResponse>();
			}

			// tableViewer にセットするための詰め替え
			for (RestAccessInfoResponse RestAccessInfo : list) {
				ArrayList<Object> a = new ArrayList<Object>();
				a.add(entrySet.getKey());
				a.add(RestAccessInfo.getRestAccessId());
				a.add(RestAccessInfo.getDescription());
				a.add(RestAccessInfo.getOwnerRoleId());
				a.add(RestAccessInfo.getRegUser());
				a.add(RestAccessInfo.getRegDate());
				a.add(RestAccessInfo.getUpdateUser());
				a.add(RestAccessInfo.getUpdateDate());
				a.add(null);
				listInput.add(a);
				cnt++;
			}
		}

		// テーブル更新
		this.tableViewer.setInput(listInput);

		// 合計欄更新
		String[] args = { Integer.toString(cnt) };
		String message = null;
		message = Messages.getString("filtered.records", args);
		this.totalLabel.setText(message);
	}

	private static void setRequiredColor(Object target) {
		if(target instanceof Text ){
			Text tagText = (Text)target;
			if (tagText.getText() == null || tagText.getText().isEmpty()) {
				tagText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			}else{
				tagText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		}
		
	}
}
