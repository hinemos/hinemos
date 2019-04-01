/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.CheckBoxSelectionAdapter;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.constant.YesNoConstant;
import com.clustercontrol.utility.settings.ui.action.CommandAction;
import com.clustercontrol.viewer.CommonTableViewer;


/**
 * インポート/エクスポート対象のジョブユニットを選択するダイアログ
 * 
 * @version 6.1.0
 * @since 1.0.0
 * 
 * 
 *
 */
public class JobunitListDialog extends CommonDialog {
	
	private static final int SELECTION = 0;
	private static final int JOBUNIT_ID = 1;
//	private final int JOBUNIT_NAME = 2;
//	private final int JOBUNIT_DESCRIPTION = 3;
	
	private static final int SORT_COLUMN_INDEX = 1;
	private static final int SORT_ORDER = 1;
	
	private static final int SIZE_WIDTH = 700;
	private static final int SIZE_HEIGHT = 400;
	
	/** テーブル */
	protected Table table = null;
	/** テーブルビューア */
	protected CommonTableViewer m_viewer = null;
	/** シェル */
	private Shell m_shell = null;
	/** ジョブユニットID情報 */
	private List<String> m_jobunitIdList = null;
	
	private String m_command = null;
	
	private String m_fileName = null;
	
	/**
	 * コンストラクタ
	 * 
	 * @param parent 親シェル
	 */
	public JobunitListDialog(Shell parent, String command, String fileName) {
		super(parent);
		m_command = command;
		m_fileName = fileName;
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
	}
	
	
	
	/**
	 * ダイアログエリアを生成します。
	 * 
	 * @param parent 親コンポジット
	 * 
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		m_shell = this.getShell();
		
		parent.getShell().setText(Messages.getString("dialog.select.jobunit"));
		
		// レイアウト
		GridLayout gridLayout = new GridLayout(8, true);
		gridLayout.marginWidth = 10;
		gridLayout.marginHeight = 10;
		gridLayout.numColumns = 8;
		parent.setLayout(gridLayout);
		
		Composite composite = null;
		
		// ジョブユニット覧を表示するテーブル
		Label tableTitle = new Label(parent, SWT.NONE);
		tableTitle.setText(Messages.getString("message.select.jobunit"));
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 8;
		tableTitle.setLayoutData(gridData);
		
		table = new Table(parent, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 7;
		table.setLayoutData(gridData);
		
		composite = new Composite(parent, SWT.NONE);
		gridLayout = new GridLayout(1, true);
		gridLayout.numColumns = 1;
		composite.setLayout(gridLayout);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		composite.setLayoutData(gridData);
		
		//すべて選択ボタン
		Button allSelectButton = this.createButton(composite, Messages.getString("string.select.all"));
		allSelectButton.addSelectionListener(new SelectionAdapter() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<Object> al = null;
				TableItem[] ti = table.getItems();
				for (int i = 0; i<ti.length; i++){
					al = (List<Object>)ti[i].getData();
					al.set(SELECTION, YesNoConstant.BOOLEAN_YES);
				}
				m_viewer.refresh();
			}
		});
		
		//クリアボタン
		Button clearButton = this.createButton(composite, Messages.getString("string.select.clear"));
		clearButton.addSelectionListener(new SelectionAdapter() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<Object> al = null;
				TableItem[] ti = table.getItems();
				for (int i = 0; i<ti.length; i++){
					al = (List<Object>)ti[i].getData();
					al.set(SELECTION, YesNoConstant.BOOLEAN_NO);
				}
				m_viewer.refresh();
			}
		});
		
		ArrayList<TableColumnInfo> column_defs = new ArrayList<TableColumnInfo>();
		TableColumnInfo column_def;

		column_def = new TableColumnInfo("", TableColumnInfo.CHECKBOX, 60, SWT.LEFT);
		column_defs.add(column_def);
		// カラムの表示名を定義
		column_def = new TableColumnInfo(Messages.getString("job.jobunit.id"), TableColumnInfo.NONE, 100, SWT.LEFT);
		column_defs.add(column_def);
		column_def = new TableColumnInfo(Messages.getString("job.jobunit.name"), TableColumnInfo.NONE, 100, SWT.LEFT);
		column_defs.add(column_def);
		column_def = new TableColumnInfo(Messages.getString("job.jobunit.description"), TableColumnInfo.NONE, 240, SWT.LEFT);
		column_defs.add(column_def);
		
		m_viewer = new CommonTableViewer(table);
		m_viewer.createTableColumn(column_defs, SORT_COLUMN_INDEX, SORT_ORDER);
		
		reflectJobunitList(m_command, m_fileName);
		
		//初期サイズをセット
		m_shell.setSize(new Point(SIZE_WIDTH, SIZE_HEIGHT));
		
		// チェックボックスの選択を制御するリスナー 
		SelectionAdapter adapter =
				new CheckBoxSelectionAdapter(this.getParentShell(), this.m_viewer, SELECTION);
		table.addSelectionListener(adapter);
	}

	/**
	 * ジョブユニット一覧をテーブルに反映する
	 */
	private void reflectJobunitList(String command, String fileName) {

		List<Object> tableData = new ArrayList<Object>();
		List<List<String>> jobunitList = null;
		
		// importの場合、XMLファイルから読み込む
		if (command.equals("import")) {
			jobunitList = new CommandAction().getJobunitListFromXML(fileName);
		}
		// exportおよびclearの場合、マネージャから取得する
		else {
			jobunitList = new CommandAction().getJobunitList();
		}
		
		if (jobunitList == null)
			jobunitList = new ArrayList<>();
		
		for (List<String> jobunit : jobunitList) {
			List<Object> tableLineData = new ArrayList<Object>();
			
			if (alreadyChecked(jobunit.get(0).toString()))
				tableLineData.add(YesNoConstant.BOOLEAN_YES);
			else
				tableLineData.add(YesNoConstant.BOOLEAN_NO);
			
			tableLineData.add(jobunit.get(0).toString());
			tableLineData.add(jobunit.get(1).toString());
			tableLineData.add(jobunit.get(2).toString());
			tableData.add(tableLineData);
		}
		
		m_viewer.setInput(tableData);
		m_viewer.refresh();
	}
	
	/**
	 * 既にチェックされているか確認
	 * @param jobunitId
	 * @return
	 */
	private boolean alreadyChecked(String jobunitId) {
		boolean ret = false;
		if(m_jobunitIdList != null){
			for (int i = 0; i < m_jobunitIdList.size(); i++) {
				String id = m_jobunitIdList.get(i);
				if(id.equals(jobunitId)){
					ret = true;
					break;
				}
			}
		}
		return ret;
	}

	/**
	 * チェックがONになっているユーザ一覧を作成して返却
	 * @return
	 */
	public List<String> getSelectionData() {
		return m_jobunitIdList;
	}
	
	/**
	 * ＯＫボタンテキスト取得
	 * 
	 * @return ＯＫボタンのテキスト
	 * @since 2.1.0
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}
	
	/**
	 * キャンセルボタンテキスト取得
	 * 
	 * @return キャンセルボタンのテキスト
	 * @since 2.1.0
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}
	
	/**
	 * 入力値チェックをします。
	 * 
	 * @return 検証結果
	 * 
	 * @see com.clustercontrol.dialog.CommonDialog#validate()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;
		
		// 選択されたユーザリストを作成
		List<String> data = new ArrayList<String>();
		List<Object> al;
		TableItem[] ti = table.getItems();	
		for (int i = 0; i<ti.length; i++){
			al = (List<Object>)ti[i].getData();
			if((Boolean)al.get(SELECTION)){
				data.add((String)al.get(JOBUNIT_ID));
			}
		}
		
		//選択していない場合、入力を促す
		if(data.size() == 0){
            result = new ValidateResult();
            result.setValid(false);
            result.setID(Messages.getString("message.hinemos.1"));
            result.setMessage(Messages.getString("message.select.jobunit"));
            return result;
		} else {
			m_jobunitIdList = data;
		}
		
		return result;
	}
	
	
    /**
	 * 共通のボタンを生成します。
	 * 
	 * @param parent
	 *            親のコンポジット
	 * @param label
	 *            ボタンのラベル
	 * @return 生成されたボタン
	 */
	private Button createButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.NONE);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		button.setLayoutData(gridData);

		button.setText(label);

		return button;
	}
	
	
}
