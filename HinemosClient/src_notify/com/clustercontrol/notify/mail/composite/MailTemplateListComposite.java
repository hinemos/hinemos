/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.mail.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.MailTemplateInfoResponse;

import com.clustercontrol.notify.mail.action.GetMailTemplate;
import com.clustercontrol.notify.mail.action.GetMailTemplateListTableDefine;
import com.clustercontrol.notify.mail.composite.actioin.MailTemplateDoubleClickListener;
import com.clustercontrol.util.DateTimeStringConverter;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;
;

/**
 * メールテンプレート一覧コンポジットクラス<BR>
 *
 * @version 2.4.0
 * @since 2.4.0
 */
public class MailTemplateListComposite extends Composite {

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
	public MailTemplateListComposite(Composite parent, int style) {
		super(parent, style);

		this.initialize();
	}

	/**
	 * コンポジットを配置します。
	 *
	 * @see com.clustercontrol.notify.action.GetMailTemplateListTableDefine#get()
	 * @see #update()
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		Table table = new Table(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
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
		this.tableViewer.createTableColumn(GetMailTemplateListTableDefine.get(),
				GetMailTemplateListTableDefine.SORT_COLUMN_INDEX1,
				GetMailTemplateListTableDefine.SORT_COLUMN_INDEX2,
				GetMailTemplateListTableDefine.SORT_ORDER);

		for (int i = 0; i < table.getColumnCount(); i++){
			table.getColumn(i).setMoveable(true);
		}

		// ダブルクリックリスナの追加
		this.tableViewer.addDoubleClickListener(new MailTemplateDoubleClickListener(this));

		// 合計ラベルの作成
		this.totalLabel = new Label(this, SWT.RIGHT);
		WidgetTestUtil.setTestId(this, "total", totalLabel);
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
	 * メールテンプレート一覧情報を取得し、テーブルビューアーにセットします。
	 *
	 * @see com.clustercontrol.notify.mail.action.GetMailTemplate#getMailTemplateList()
	 */
	@Override
	public void update() {
		// データ取得
		Map<String, List<MailTemplateInfoResponse>> dispDataMap = new GetMailTemplate().getMailTemplateList();
		ArrayList<ArrayList<Object>> listInput = new ArrayList<ArrayList<Object>>();

		int cnt = 0;
		for(Map.Entry<String, List<MailTemplateInfoResponse>> entrySet : dispDataMap.entrySet()) {
			List<MailTemplateInfoResponse> list = entrySet.getValue();
			if(list == null){
				list = new ArrayList<MailTemplateInfoResponse>();
			}

			// tableViewer にセットするための詰め替え
			for (MailTemplateInfoResponse mailTemplateInfo : list) {
				ArrayList<Object> a = new ArrayList<Object>();
				a.add(entrySet.getKey());
				a.add(mailTemplateInfo.getMailTemplateId());
				a.add(mailTemplateInfo.getDescription());
				a.add(mailTemplateInfo.getOwnerRoleId());
				a.add(mailTemplateInfo.getRegUser());
				a.add(mailTemplateInfo.getRegDate() == null ? null : DateTimeStringConverter.parseDateString(mailTemplateInfo.getRegDate()));
				a.add(mailTemplateInfo.getUpdateUser());
				a.add(mailTemplateInfo.getUpdateDate() == null ? null : DateTimeStringConverter.parseDateString(mailTemplateInfo.getUpdateDate()));
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
}
