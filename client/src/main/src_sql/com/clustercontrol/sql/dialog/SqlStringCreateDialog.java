/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.sql.dialog;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.composite.TextWithParameterComposite;
import com.clustercontrol.monitor.run.dialog.CommonMonitorStringDialog;
import com.clustercontrol.sql.action.AddSql;
import com.clustercontrol.sql.action.GetJdbc;
import com.clustercontrol.sql.action.GetSql;
import com.clustercontrol.sql.action.ModifySql;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.SqlCheckInfo;

/**
 * SQL監視（文字列）作成・変更ダイアログクラス<BR>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class SqlStringCreateDialog extends CommonMonitorStringDialog {

	/** 接続文字列 */
	private TextWithParameterComposite textUrl = null;

	/** JDBCドライバ */
	private Combo textJdbcDriver = null;

	/** ユーザ */
	private Text textUser = null;

	/** パスワード */
	private Text textPassword = null;

	/** クエリ */
	private Text textQuery = null;

	/** マネージャ名 */
	private String managerName = null;
	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public SqlStringCreateDialog(Shell parent) {
		super(parent, null);
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param マネージャ名
	 * @param monitorId 変更する監視項目ID
	 * @param updateFlg 更新するか否か（true:変更、false:新規登録）
	 */
	public SqlStringCreateDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
		super(parent, managerName);

		this.managerName = managerName;
		this.monitorId = monitorId;
		this.updateFlg = updateFlg;
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {

		super.customizeDialog(parent);

		// タイトル
		shell.setText(Messages.getString("dialog.sql.create.modify"));

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		/*
		 * チェック設定グループ（条件グループの子グループ）
		 */
		Group groupCheckRule = new Group(groupRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "checkrule", groupCheckRule);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = BASIC_UNIT;
		groupCheckRule.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupCheckRule.setLayoutData(gridData);
		groupCheckRule.setText(Messages.getString("check.rule"));

		// 接続文字列
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "connectionurl", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("connection.url") + " : ");
		// テキスト
		this.textUrl = new TextWithParameterComposite(groupCheckRule, SWT.BORDER | SWT.LEFT | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, "url", textUrl);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.textUrl.setLayoutData(gridData);
		this.textUrl.setText("jdbc:");
		this.textUrl.setToolTipText(Messages.getString("connection.url.tooltip"));
		this.textUrl.setColor(new Color(parent.getDisplay(), new RGB(0, 0, 255)));
		this.textUrl.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space1", label);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 接続先DB
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "connectiondb", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("connection.db") + " : ");
		// コンボ
		this.textJdbcDriver = new Combo(groupCheckRule, SWT.BORDER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "jdbcdriver", textJdbcDriver);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.textJdbcDriver.setLayoutData(gridData);
		List<List<String>> list = getJdbcDriver();
		if(list != null){
			for(int i = 0; i < list.size(); i++){
				List<String> driver = list.get(i);
				String name = driver.get(0);
				this.textJdbcDriver.add(name);
			}
			this.textJdbcDriver.select(0);
		}
		this.textJdbcDriver.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});


		// 空白
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space2", label);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// ユーザ
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "user", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("user.id") + " : ");
		// テキスト
		this.textUser = new Text(groupCheckRule, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "user", textUser);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.textUser.setLayoutData(gridData);
		this.textUser.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space3", label);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// パスワード
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "password", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("password") + " : ");
		// テキスト
		this.textPassword = new Text(groupCheckRule, SWT.BORDER | SWT.LEFT | SWT.PASSWORD);
		WidgetTestUtil.setTestId(this, "password", textPassword);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.textPassword.setLayoutData(gridData);
		this.textPassword.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space4", label);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// SQL文
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "sqlstring", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("sql.string") + " : ");
		// テキスト
		this.textQuery= new Text(groupCheckRule, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "query", textQuery);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.textQuery.setLayoutData(gridData);
		this.textQuery.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space5", label);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// ダイアログを調整
		this.adjustDialog();

		// 初期表示
		MonitorInfo info = null;
		if(this.monitorId == null){
			// 作成の場合
			info = new MonitorInfo();
			this.setInfoInitialValue(info);
		}
		else{
			// 変更の場合、情報取得
			info = new GetSql().getSql(this.managerName, this.monitorId);
		}
		this.setInputData(info);


	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		super.update();

		// 各項目が必須項目であることを明示
		if("jdbc:".equals(this.textUrl.getText()) || !(this.textUrl.getText().startsWith("jdbc:"))){
			this.textUrl.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.textUrl.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.textJdbcDriver.getText())){
			this.textJdbcDriver.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.textJdbcDriver.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.textUser.getText())){
			this.textUser.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.textUser.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.textPassword.getText())){
			this.textPassword.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.textPassword.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.textQuery.getText())){
			this.textQuery.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.textQuery.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 各項目に入力値を設定します。
	 *
	 * @param monitor
	 *            設定値として用いる監視情報
	 */
	@Override
	protected void setInputData(MonitorInfo monitor) {

		super.setInputData(monitor);

		this.inputData = monitor;

		// 監視条件 SQL監視情報
		SqlCheckInfo sqlInfo = monitor.getSqlCheckInfo();
		if(sqlInfo != null){
			if (sqlInfo.getConnectionUrl() != null) {
				this.textUrl.setText(sqlInfo.getConnectionUrl());
			}
			if (sqlInfo.getUser() != null) {
				this.textUser.setText(sqlInfo.getUser());
			}
			if (sqlInfo.getPassword() != null) {
				this.textPassword.setText(sqlInfo.getPassword());
			}
			if (sqlInfo.getJdbcDriver() != null) {
				List<List<String>> list = getJdbcDriver();
				if(list != null){
					for(int i = 0; i < list.size(); i++){
						List<String> driver = list.get(i);
						String className = driver.get(1);
						if(className.equals(sqlInfo.getJdbcDriver())){
							this.textJdbcDriver.setText(driver.get(0));
							break;
						}
					}
				}
			}
			if (sqlInfo.getQuery() != null) {
				this.textQuery.setText(sqlInfo.getQuery());
			}
		}

		// 各項目が必須項目であることを明示
		this.update();

		// 文字列監視情報
		m_stringValueInfo.setInputData(monitor);

	}

	/**
	 * 入力値を用いて通知情報を生成します。
	 *
	 * @return 入力値を保持した通知情報
	 */
	@Override
	protected MonitorInfo createInputData() {
		super.createInputData();
		if(validateResult != null){
			return null;
		}

		// SQL監視（文字列）固有情報を設定
		monitorInfo.setMonitorTypeId(HinemosModuleConstant.MONITOR_SQL_S);

		// 監視条件 SQL監視情報
		SqlCheckInfo sqlInfo = new SqlCheckInfo();
		sqlInfo.setMonitorTypeId(HinemosModuleConstant.MONITOR_SQL_S);
		sqlInfo.setMonitorId(monitorInfo.getMonitorId());

		//接続先URL
		if (this.textUrl.getText() != null
				&& !"".equals((this.textUrl.getText()).trim())) {
			//テキストボックスから文字列を取得
			sqlInfo.setConnectionUrl(this.textUrl.getText());
		}
		//ユーザ
		if (this.textUser.getText() != null
				&& !"".equals((this.textUser.getText()).trim())) {
			sqlInfo.setUser(this.textUser.getText());
		}
		//パスワード
		if (this.textPassword.getText() != null
				&& !"".equals((this.textPassword.getText()).trim())) {
			sqlInfo.setPassword(this.textPassword.getText());
		}
		//SQL文
		if (this.textQuery.getText() != null
				&& !"".equals((this.textQuery.getText()).trim())) {
			sqlInfo.setQuery(this.textQuery.getText());
		}
		//JDBCドライバ
		if (this.textJdbcDriver.getText() != null
				&& !"".equals((this.textJdbcDriver.getText()).trim())) {

			List<List<String>> list = getJdbcDriver();
			if(list != null){
				for(int i = 0; i < list.size(); i++){
					List<String> driver = list.get(i);
					String name = driver.get(0);
					if(name.equals(this.textJdbcDriver.getText())){
						sqlInfo.setJdbcDriver(driver.get(1));
						break;
					}
				}
			}
		}
		monitorInfo.setSqlCheckInfo(sqlInfo);

		// 結果判定の定義
		validateResult = m_stringValueInfo.createInputData(monitorInfo);
		if(validateResult != null){
			return null;
		}

		// 通知関連情報とアプリケーションの設定
		validateResult = m_notifyInfo.createInputData(monitorInfo);
		if (validateResult != null) {
			if(validateResult.getID() == null){	// 通知ID警告用出力
				if(!displayQuestion(validateResult)){
					validateResult = null;
					return null;
				}
			}
			else{	// アプリケーション未入力チェック
				return null;
			}
		}

		return monitorInfo;
	}

	/**
	 * 入力値をマネージャに登録します。
	 *
	 * @return true：正常、false：異常
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#action()
	 */
	@Override
	protected boolean action() {
		boolean result = false;

		MonitorInfo info = this.inputData;
		String managerName = this.getManagerName();
		if(info != null){
			if(!this.updateFlg){
				// 作成の場合
				result = new AddSql().add(managerName, info);
			}
			else{
				// 変更の場合
				result = new ModifySql().modify(managerName, info);
			}
		}

		return result;
	}

	/**
	 * JDBC情報を返却します
	 * @return
	 */
	private List<List<String>> getJdbcDriver(){
		return GetJdbc.getJdbcDriverList(m_monitorBasic.getManagerListComposite().getText());
	}
}
