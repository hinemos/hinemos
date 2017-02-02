/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.http.dialog;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.http.action.AddHttp;
import com.clustercontrol.http.action.GetHttp;
import com.clustercontrol.http.action.ModifyHttp;
import com.clustercontrol.http.composite.PageCompositeDefine;
import com.clustercontrol.monitor.bean.HttpAuthTypeConstant;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.composite.MonitorBasicScopeComposite;
import com.clustercontrol.monitor.run.composite.MonitorRuleComposite;
import com.clustercontrol.monitor.run.composite.TableItemInfoComposite;
import com.clustercontrol.monitor.run.dialog.CommonMonitorDialog;
import com.clustercontrol.notify.composite.NotifyInfoComposite;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.monitor.HttpScenarioCheckInfo;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.Page;

/**
 * Http監視（シナリオ）作成・変更ダイアログクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class HttpScenarioCreateDialog extends CommonMonitorDialog {

	// ----- static フィールド ----- //
	private static final String HTTP_PREFIX = "http://";
	private static final String HTTPS_PREFIX = "https://";
	private static final String DEFAULT_USER_AGENT = "Internet Explorer 11.0";

	// ----- instance フィールド ----- //

	/** コネクションタイムアウト用テキストボックス */
	private Text m_textConnectionTimeout = null;

	/** リクエストタイムアウト用テキストボックス */
	private Text m_textRequestTimeout = null;

	/** User-Agent */
	private Text m_textUserAgent = null;

	/** 認証 */
	private Combo m_comboAuth = null;

	/** 認証：ユーザ */
	private Text m_textAuthUser = null;

	/** 認証：パスワード */
	private Text m_textAuthPassword = null;

	/** プロキシ：有効無効 */
	private Button m_chkbxProxy = null;

	/** プロキシ：URL */
	private Text m_textProxyUrl = null;

	/** プロキシ：URL */
	private Text m_textProxyPort = null;

	/** プロキシ：ユーザ */
	private Text m_textProxyUser = null;

	/** プロキシ：パスワード */
	private Text m_textProxyPassword = null;

	/** 収集時にページ単位の情報も収集する： */
	private Button m_chkbxCollectEachPage = null;

	/** ページリストコンポジット */
	private TableItemInfoComposite<Page> m_pageInfoComposite = null;

	/** 収集グループ */
	private Group groupCollect = null;

	/** 収集を有効にする */
	private Button confirmCollectValid = null;

	/** 収集値表示名 */
	protected Text itemName = null;

	/** 収集値単位 */
	protected Text measure = null;


	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public HttpScenarioCreateDialog(Shell parent) {
		super(parent, null);
	}

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param monitorType 監視判定タイプ
	 */
	public HttpScenarioCreateDialog(Shell parent, String managerName, int monitorType) {
		super(parent, managerName);
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param monitorId 変更する監視項目ID
	 * @param updateFlg 更新するか否か（true:変更、false:新規登録）
	 */
	public HttpScenarioCreateDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
		super(parent, managerName);

		this.monitorId = monitorId;
		this.updateFlg = updateFlg;
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {

//		super.customizeDialog(parent);

		shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.http.create.modify"));

		// 変数として利用されるラベル
		Label label = null;

		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 15;
		parent.setLayout(layout);

		// 監視基本情報
		m_monitorBasic = new MonitorBasicScopeComposite(parent, SWT.NONE, m_unregistered, this);
		WidgetTestUtil.setTestId(this, null, m_monitorBasic);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_monitorBasic.setLayoutData(gridData);
		if(this.managerName != null) {
			m_monitorBasic.getManagerListComposite().setText(this.managerName);
		}

		/*
		 * 条件グループ
		 */
		groupRule = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "rule", groupRule);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupRule.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupRule.setLayoutData(gridData);
		groupRule.setText(Messages.getString("monitor.rule"));

		m_monitorRule = new MonitorRuleComposite(groupRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "rule", m_monitorRule);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_monitorRule.setLayoutData(gridData);

		/*
		 * 監視グループ
		 */
		groupMonitor = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "monitor", groupMonitor);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupMonitor.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupMonitor.setLayoutData(gridData);
		groupMonitor.setText(Messages.getString("monitor.run"));


		// 監視（有効／無効）
		this.confirmMonitorValid = new Button(groupMonitor, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "confirmMonitorValid", confirmMonitorValid);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.confirmMonitorValid.setLayoutData(gridData);
		this.confirmMonitorValid.setText(Messages.getString("monitor.run"));
		this.confirmMonitorValid.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 判定、通知部分を有効/無効化
				if(confirmMonitorValid.getSelection()){
					setMonitorEnabled(true);
				}else{
					setMonitorEnabled(false);
				}
			}
		});

		/*
		 * 通知グループ（監視グループの子グループ）
		 */
		groupNotifyAttribute = new Group(groupMonitor, SWT.NONE);
		WidgetTestUtil.setTestId(this, "notifyAttribute", groupNotifyAttribute);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 1;
		groupNotifyAttribute.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 13;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupNotifyAttribute.setLayoutData(gridData);
		groupNotifyAttribute.setText(Messages.getString("notify.attribute"));
		this.m_notifyInfo = new NotifyInfoComposite(groupNotifyAttribute, SWT.NONE);
		WidgetTestUtil.setTestId(this, "notifyinfo", m_notifyInfo);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = 110;
		this.m_notifyInfo.setLayoutData(gridData);

		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		WidgetTestUtil.setTestId(this, "line", line);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 15;
		line.setLayoutData(gridData);


		/*
		 * 収集グループ
		 */
		groupCollect = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "collect", groupCollect);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupCollect.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupCollect.setLayoutData(gridData);
		groupCollect.setText(Messages.getString("collection.run"));

		// 収集（有効／無効）
		this.confirmCollectValid = new Button(groupCollect, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "collectcheck", confirmCollectValid);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.confirmCollectValid.setLayoutData(gridData);
		this.confirmCollectValid.setText(Messages.getString("collection.run"));
		this.confirmCollectValid.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 収集エリアを有効/無効化
				if(confirmCollectValid.getSelection()){
					setCollectorEnabled(true);
				}else{
					setCollectorEnabled(false);
				}
			}
		});

		// ラベル（収集値表示名）
		label = new Label(groupCollect, SWT.NONE);
		WidgetTestUtil.setTestId(this, "collectiondisplayname", label);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("collection.display.name") + " : ");

		// テキスト（収集値表示名）
		this.itemName = new Text(groupCollect, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "item", itemName);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.itemName.setLayoutData(gridData);
		this.itemName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(groupCollect, SWT.NONE);
		WidgetTestUtil.setTestId(this, "blank", label);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// ラベル（収集値単位）
		label = new Label(groupCollect, SWT.NONE);
		WidgetTestUtil.setTestId(this, "collectionunit", label);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("collection.unit") + " : ");
		// テキスト（収集値単位）
		this.measure = new Text(groupCollect, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "unit", measure);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.measure.setLayoutData(gridData);
		this.measure.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});


		/*
		 * チェック設定グループ（条件グループの子グループ）
		 */
		Group groupCheckRule = new Group(groupRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "checkrule", groupCheckRule);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupCheckRule.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupCheckRule.setLayoutData(gridData);
		groupCheckRule.setText(Messages.getString("check.rule"));

		/*
		 * コネクションタイムアウト
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "connectiontimeout", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("monitor.http.scenario.connect.timeout") + " : ");

		// テキスト
		this.m_textConnectionTimeout = new Text(groupCheckRule, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "connectiontimeout", m_textConnectionTimeout);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_VALUE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textConnectionTimeout.setLayoutData(gridData);
		this.m_textConnectionTimeout.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ラベル（単位）
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "millisec", label);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("milli.sec"));

		/*
		 * リクエストタイムアウト
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "requesttimeout", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("monitor.http.scenario.request.timeout") + " : ");

		// テキスト
		this.m_textRequestTimeout = new Text(groupCheckRule, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "requesttimeout", m_textRequestTimeout);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_VALUE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textRequestTimeout.setLayoutData(gridData);
		this.m_textRequestTimeout.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ラベル（単位）
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "millisec", label);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("milli.sec"));


		/*
		 * User-Agent
		 */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "useragent", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("monitor.http.scenario.user.agent") + " : ");
		// テキスト
		this.m_textUserAgent = new Text(groupCheckRule, SWT.BORDER | SWT.LEFT | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, "useragent", m_textUserAgent);
		gridData = new GridData();
		gridData.horizontalSpan = 11;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textUserAgent.setLayoutData(gridData);
		this.m_textUserAgent.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});


		/*
		 * 認証グループ
		 */
		Group groupAuthentication = new Group(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "authentication", groupAuthentication);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupAuthentication.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupAuthentication.setLayoutData(gridData);
		groupAuthentication.setText(Messages.getString("monitor.http.scenario.authentication"));


		// コンボ
		this.m_comboAuth = new Combo(groupAuthentication, SWT.BORDER | SWT.LEFT | SWT.SINGLE | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, null, m_comboAuth);

		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboAuth.setLayoutData(gridData);
		this.m_comboAuth.add("");
		this.m_comboAuth.add(HttpAuthTypeConstant.BASIC);
		this.m_comboAuth.add(HttpAuthTypeConstant.DIGEST);
		this.m_comboAuth.add(HttpAuthTypeConstant.NTLM);
		this.m_comboAuth.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(groupAuthentication, SWT.NONE);
		WidgetTestUtil.setTestId(this, "blank", label);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 認証：ユーザ名
		// ラベル
		label = new Label(groupAuthentication, SWT.NONE);
		WidgetTestUtil.setTestId(this, "user", label);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("user") + " : ");

		// テキスト
		this.m_textAuthUser = new Text(groupAuthentication, SWT.BORDER | SWT.LEFT | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, null, m_textAuthUser);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textAuthUser.setLayoutData(gridData);
		this.m_textAuthUser.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(groupAuthentication, SWT.NONE);
		WidgetTestUtil.setTestId(this, "blankpassword", label);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 認証：パスワード

		// ラベル
		label = new Label(groupAuthentication, SWT.NONE);
		WidgetTestUtil.setTestId(this, "password", label);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("password") + " : ");

		// テキスト
		this.m_textAuthPassword = new Text(groupAuthentication, SWT.BORDER | SWT.LEFT | SWT.SINGLE | SWT.PASSWORD);
		WidgetTestUtil.setTestId(this, null, m_textAuthPassword);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textAuthPassword.setLayoutData(gridData);
		this.m_textAuthPassword.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * 認証グループ
		 */
		Group groupProxy = new Group(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "proxy", groupAuthentication);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupProxy.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupProxy.setLayoutData(gridData);
		groupProxy.setText(Messages.getString("monitor.http.scenario.proxy"));

		/*
		 * プロキシ:
		 */
		// チェック
		m_chkbxProxy = new Button(groupProxy, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "chkbxproxy", m_chkbxProxy);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_chkbxProxy.setLayoutData(gridData);
		m_chkbxProxy.setText(Messages.getString("monitor.http.scenario.proxy"));
		this.m_chkbxProxy.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e){
				update();
			}
		});

		// プロキシ：ポート
		// ラベル
		label = new Label(groupProxy, SWT.NONE);
		WidgetTestUtil.setTestId(this, "requesturl", label);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("request.url") + " : ");

		// テキスト
		this.m_textProxyUrl = new Text(groupProxy, SWT.BORDER | SWT.LEFT | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, null, m_textProxyUrl);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textProxyUrl.setLayoutData(gridData);
		this.m_textProxyUrl.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});


		// プロキシ：ポート
		// ラベル
		label = new Label(groupProxy, SWT.NONE);
		WidgetTestUtil.setTestId(this, "port", label);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 20;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("port") + " : ");

		// テキスト
		this.m_textProxyPort = new Text(groupProxy, SWT.BORDER | SWT.LEFT | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, null, m_textProxyPort);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textProxyPort.setLayoutData(gridData);
		this.m_textProxyPort.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(groupProxy, SWT.NONE);
		WidgetTestUtil.setTestId(this, "blankuser", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// プロキシ：ユーザ名
		// ラベル
		label = new Label(groupProxy, SWT.NONE);
		WidgetTestUtil.setTestId(this, "user", label);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("user") + " : ");

		// テキスト
		this.m_textProxyUser = new Text(groupProxy, SWT.BORDER | SWT.LEFT | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, null, m_textProxyUser);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textProxyUser.setLayoutData(gridData);
		this.m_textProxyUser.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 空白
		label = new Label(groupProxy, SWT.NONE);
		WidgetTestUtil.setTestId(this, "blankpassword", label);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// プロキシ：パスワード

		// ラベル
		label = new Label(groupProxy, SWT.NONE);
		WidgetTestUtil.setTestId(this, "password", label);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("password") + " : ");

		// テキスト
		this.m_textProxyPassword = new Text(groupProxy, SWT.BORDER | SWT.LEFT | SWT.SINGLE | SWT.PASSWORD);
		WidgetTestUtil.setTestId(this, null, m_textProxyPassword);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textProxyPassword.setLayoutData(gridData);
		this.m_textProxyPassword.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});


		/*
		 * 収集時にページ単位の情報も収集する:
		 */
		// チェック
		m_chkbxCollectEachPage = new Button(groupCheckRule, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "collecteachpage", m_chkbxCollectEachPage);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE + 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_chkbxCollectEachPage.setLayoutData(gridData);
		m_chkbxCollectEachPage.setText(Messages.getString("monitor.http.scenario.collect.each.page"));


		/** ページ */
		// ラベル
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "scenariopage", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("monitor.http.scenario.page") + " : ");

		// テーブルコンポジット
		m_pageInfoComposite = new TableItemInfoComposite<>(groupCheckRule, SWT.NONE, new PageCompositeDefine());
		WidgetTestUtil.setTestId(this, "pageinfo", m_pageInfoComposite);
		gridData = new GridData();
		gridData.horizontalSpan = 11;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = 110;
		m_pageInfoComposite.setLayoutData(gridData);


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
			info = new GetHttp().getHttp(this.managerName, this.monitorId);
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

		// 必須項目を明示
		if(this.m_textConnectionTimeout.getEnabled() && "".equals(this.m_textConnectionTimeout.getText())){
			this.m_textConnectionTimeout.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textConnectionTimeout.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(this.m_textRequestTimeout.getEnabled() && "".equals(this.m_textRequestTimeout.getText())){
			this.m_textRequestTimeout.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textRequestTimeout.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_textUserAgent.getText().trim())){
			this.m_textUserAgent.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textUserAgent.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		boolean isAuthUse = !"".equals(this.m_comboAuth.getText().trim());
		this.m_textAuthUser.setEnabled(isAuthUse);
		this.m_textAuthPassword.setEnabled(isAuthUse);

		if(this.m_textAuthUser.isEnabled() && "".equals(this.m_textAuthUser.getText().trim())){
			this.m_textAuthUser.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textAuthUser.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if(this.m_textAuthPassword.isEnabled() && "".equals(this.m_textAuthPassword.getText().trim())){
			this.m_textAuthPassword.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_textAuthPassword.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		this.m_textProxyUrl.setEnabled(this.m_chkbxProxy.getSelection());
		this.m_textProxyPort.setEnabled(this.m_chkbxProxy.getSelection());
		this.m_textProxyUser.setEnabled(this.m_chkbxProxy.getSelection());
		this.m_textProxyPassword.setEnabled(this.m_chkbxProxy.getSelection());

		if(this.m_textProxyUrl.isEnabled() && (HTTP_PREFIX.equals(this.m_textProxyUrl.getText()) || !(this.m_textProxyUrl.getText().startsWith(HTTP_PREFIX) || this.m_textProxyUrl.getText().startsWith(HTTPS_PREFIX)))){
			this.m_textProxyUrl.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textProxyUrl.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		if(this.m_textProxyPort.isEnabled() && "".equals(this.m_textProxyPort.getText().trim())){
			this.m_textProxyPort.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textProxyPort.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 各項目に入力値を設定します。
	 *
	 * @param monitor 設定値として用いる監視情報
	 */
	@Override
	protected void setInputData(MonitorInfo monitor) {

		super.setInputData(monitor);

		this.inputData = monitor;

		this.m_textUserAgent.setText(monitor.getHttpScenarioCheckInfo().getUserAgent());

		this.m_textConnectionTimeout.setText(String.valueOf(monitor.getHttpScenarioCheckInfo().getConnectTimeout()));

		this.m_textRequestTimeout.setText(String.valueOf(monitor.getHttpScenarioCheckInfo().getRequestTimeout()));

		if(monitor.getHttpScenarioCheckInfo().getAuthType() != null){
			this.m_comboAuth.setText(monitor.getHttpScenarioCheckInfo().getAuthType());
			this.m_textAuthUser.setText(monitor.getHttpScenarioCheckInfo().getAuthUser());
			this.m_textAuthPassword.setText(monitor.getHttpScenarioCheckInfo().getAuthPassword());
		}

		if(monitor.getHttpScenarioCheckInfo().getProxyUrl() == null){
			monitor.getHttpScenarioCheckInfo().setProxyUrl(HTTP_PREFIX);
		}
		this.m_textProxyUrl.setText(monitor.getHttpScenarioCheckInfo().getProxyUrl());

		if(monitor.getHttpScenarioCheckInfo().isProxyFlg()){
			this.m_chkbxProxy.setSelection(true);
		}
		if(monitor.getHttpScenarioCheckInfo().getProxyPort() != null) {
			this.m_textProxyPort.setText(String.valueOf(monitor.getHttpScenarioCheckInfo().getProxyPort()));
		}
		if(monitor.getHttpScenarioCheckInfo().getProxyUser() != null){
			this.m_textProxyUser.setText(monitor.getHttpScenarioCheckInfo().getProxyUser());
		}
		if(monitor.getHttpScenarioCheckInfo().getProxyPassword() != null){
			this.m_textProxyPassword.setText(monitor.getHttpScenarioCheckInfo().getProxyPassword());
		}

		this.m_chkbxCollectEachPage.setSelection(monitor.getHttpScenarioCheckInfo().isMonitoringPerPageFlg());

		if(monitor.getHttpScenarioCheckInfo().getPages() != null){
			this.m_pageInfoComposite.setInputData(monitor.getHttpScenarioCheckInfo().getPages());
		}

		// 収集
		if (monitor.isCollectorFlg()) {
			this.confirmCollectValid.setSelection(true);
		}else{
			this.setCollectorEnabled(false);
		}

		// 収集値表示名
		if (monitor.getItemName() != null){
			this.itemName.setText(monitor.getItemName());
		} else {
			this.itemName.setText(Messages.getString("response.time"));
		}

		// 収集値単位
		if (monitor.getMeasure() != null){
			this.measure.setText(monitor.getMeasure());
		} else {
			this.measure.setText("msec");
		}

		// 必須項目を明示
		this.update();
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

		// HttpScenario監視（文字列）固有情報を設定
		monitorInfo.setMonitorTypeId(HinemosModuleConstant.MONITOR_HTTP_SCENARIO);
		monitorInfo.setMonitorType(MonitorTypeConstant.TYPE_SCENARIO);

		// 監視条件 HttpScenario監視情報
		HttpScenarioCheckInfo httpScenarioCheckInfo = new HttpScenarioCheckInfo();
		httpScenarioCheckInfo.setMonitorTypeId(monitorInfo.getMonitorTypeId());
		httpScenarioCheckInfo.setMonitorId(monitorInfo.getMonitorId());

		httpScenarioCheckInfo.setUserAgent(this.m_textUserAgent.getText());

		httpScenarioCheckInfo.setConnectTimeout(Integer.valueOf(this.m_textConnectionTimeout.getText().trim()));
		httpScenarioCheckInfo.setRequestTimeout(Integer.valueOf(this.m_textRequestTimeout.getText().trim()));

		if(!"".equals(this.m_comboAuth.getText().trim())){
			httpScenarioCheckInfo.setAuthType(this.m_comboAuth.getText());
			httpScenarioCheckInfo.setAuthUser(this.m_textAuthUser.getText());
			httpScenarioCheckInfo.setAuthPassword(this.m_textAuthPassword.getText());
		}

		httpScenarioCheckInfo.setProxyFlg(this.m_chkbxProxy.getSelection());
		httpScenarioCheckInfo.setProxyUrl(this.m_textProxyUrl.getText());
		httpScenarioCheckInfo.setProxyPort(Integer.valueOf(this.m_textProxyPort.getText()));
		httpScenarioCheckInfo.setProxyUser(this.m_textProxyUser.getText());
		httpScenarioCheckInfo.setProxyPassword(this.m_textProxyPassword.getText());

		httpScenarioCheckInfo.setMonitoringPerPageFlg(this.m_chkbxCollectEachPage.getSelection());

		List<Page> pages = httpScenarioCheckInfo.getPages();
		pages.clear();
		pages.addAll(this.m_pageInfoComposite.getItems());

		monitorInfo.setHttpScenarioCheckInfo(httpScenarioCheckInfo);

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

		// 収集
		monitorInfo.setCollectorFlg(this.confirmCollectValid.getSelection());

		if(this.itemName.getText() != null){
			monitorInfo.setItemName(this.itemName.getText());
		}

		if(this.measure.getText() != null){
			monitorInfo.setMeasure(this.measure.getText());
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
		if(info != null){
			String managerName = this.getManagerName();
			if(!this.updateFlg){
				// 作成の場合
				result = new AddHttp().add(managerName, info);
			}
			else{
				// 変更の場合
				result = new ModifyHttp().modify(managerName, info);
			}
		}

		return result;
	}

	/**
	 * MonitorInfoに初期値を設定します
	 *
	 * @see com.clustercontrol.dialog.CommonMonitorDialog#setInfoInitialValue()
	 */
	@Override
	protected void setInfoInitialValue(MonitorInfo monitor) {

		super.setInfoInitialValue(monitor);

		HttpScenarioCheckInfo httpScenarioCheckInfo = new HttpScenarioCheckInfo();
		// URL置換
		httpScenarioCheckInfo.setUserAgent(DEFAULT_USER_AGENT);
		// コネクションタイムアウト（ミリ秒）
		httpScenarioCheckInfo.setConnectTimeout(TIMEOUT_SEC);
		// リクエストタイムアウト（ミリ秒）
		httpScenarioCheckInfo.setRequestTimeout(TIMEOUT_SEC);

		// プロキシ
		httpScenarioCheckInfo.setProxyFlg(false);
		httpScenarioCheckInfo.setProxyUrl(HTTP_PREFIX);
		httpScenarioCheckInfo.setProxyPort(8080);
		httpScenarioCheckInfo.setMonitoringPerPageFlg(false);

		monitor.setHttpScenarioCheckInfo(httpScenarioCheckInfo);
	}


	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;

		if ("".equals((this.m_textConnectionTimeout.getText()).trim())) {
			this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.http.scenario.required.numeric", new Object[]{Messages.getString("monitor.http.scenario.connect.timeout")}));
			return this.validateResult;
		} else {
			try{
				Integer.valueOf(this.m_textConnectionTimeout.getText().trim());
			}
			catch(NumberFormatException e){
				this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.http.scenario.required.numeric", new Object[]{Messages.getString("monitor.http.scenario.connect.timeout")}));
				return this.validateResult;
			}
		}

		if ("".equals((this.m_textRequestTimeout.getText()).trim())) {
			this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.http.scenario.required.numeric", new Object[]{Messages.getString("monitor.http.scenario.request.timeout")}));
			return this.validateResult;
		} else {
			try{
				Integer.valueOf(this.m_textRequestTimeout.getText().trim());
			}
			catch(NumberFormatException e){
				this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.http.scenario.required.numeric", new Object[]{Messages.getString("monitor.http.scenario.request.timeout")}));
				return this.validateResult;
			}
		}

		if (this.m_textUserAgent.getText() == null || "".equals((this.m_textUserAgent.getText()).trim())) {
			this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.http.scenario.required", new Object[]{Messages.getString("monitor.http.scenario.user.agent")}));
			return this.validateResult;
		}

		if(!"".equals(this.m_comboAuth.getText())){
			if (this.m_textAuthUser.getText() == null || "".equals((this.m_textAuthUser.getText()).trim())) {
				this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.http.scenario.required", new Object[]{Messages.getString("user") + "(" + Messages.getString("monitor.http.scenario.authentication") + ")"}));
				return this.validateResult;
			}
			if (this.m_textAuthPassword.getText() == null || "".equals((this.m_textAuthPassword.getText()).trim())) {
				this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.http.scenario.required", new Object[]{Messages.getString("password") + "(" + Messages.getString("monitor.http.scenario.authentication") + ")"}));
				return this.validateResult;
			}
		}

		if(this.m_chkbxProxy.getSelection()){
			if (this.m_textProxyUrl.getText() == null || HTTP_PREFIX.equals(this.m_textProxyUrl.getText()) || !(this.m_textProxyUrl.getText().startsWith(HTTP_PREFIX) || this.m_textProxyUrl.getText().startsWith(HTTPS_PREFIX))) {
				this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.http.scenario.required", new Object[]{Messages.getString("request.url")}));
				return this.validateResult;
			}
			if (this.m_textProxyPort.getText() == null || "".equals((this.m_textProxyPort.getText()).trim())) {
				this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.http.scenario.required", new Object[]{Messages.getString("port")}));
				return this.validateResult;
			}
		}

		if(result == null){
			result = super.validate();
		}
		return result;
	}

	/**
	 * 収集エリアを有効/無効化します。
	 *
	 */
	private void setCollectorEnabled(boolean enabled){
		itemName.setEnabled(enabled);
		measure.setEnabled(enabled);

		update();
	}
}
