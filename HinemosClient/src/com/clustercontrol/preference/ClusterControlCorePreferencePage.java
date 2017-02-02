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

package com.clustercontrol.preference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.LoginManager;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PasswordFieldEditor;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * アクセス機能の設定ページクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class ClusterControlCorePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	// ログ
	private static Log m_log = LogFactory.getLog( ClusterControlCorePreferencePage.class );

	/** 接続先URL */
	public static final String URL = LoginManager.KEY_URL;

	/** 接続の死活監視間隔 */
	public static final String KEY_INTERVAL = LoginManager.KEY_INTERVAL;
	public static final String KEY_HTTP_REQUEST_TIMEOUT = LoginManager.KEY_HTTP_REQUEST_TIMEOUT;
	public static final int VALUE_INTERVAL_MAX = 60;
	public static final int VALUE_INTERVAL_MIN = 1;
	public static final int VALUE_HTTP_TIMEOUT_MAX = 600000; // ms = 600 s
	public static final int VALUE_HTTP_TIMEOUT_MIN = 1; // ms = 1 s

	/** Proxy */
	public static final String KEY_PROXY_ENABLE = LoginManager.KEY_PROXY_ENABLE;
	public static final String KEY_PROXY_HOST = LoginManager.KEY_PROXY_HOST;
	public static final String KEY_PROXY_PORT = LoginManager.KEY_PROXY_PORT;
	public static final String KEY_PROXY_USER = LoginManager.KEY_PROXY_USER;
	public static final String KEY_PROXY_PASSWORD = LoginManager.KEY_PROXY_PASSWORD;
	public static final int VALUE_PROXY_PORT_MAX = 65535;
	public static final int VALUE_PROXY_PORT_MIN = 0;

	/** 接続先チェック間隔 */
	IntegerFieldEditor managerPollingInterval = null;

	/** HTTPリクエストタイムアウト */
	IntegerFieldEditor httpRequestTimeout = null;

	/** Proxyを利用するか否か */
	BooleanFieldEditor proxyEnable = null;
	
	/** Proxyホスト */
	StringFieldEditor proxyHost = null;

	/** Proxyポート */
	IntegerFieldEditor proxyPort = null;

	/** Proxyユーザ */
	StringFieldEditor proxyUser = null;

	/** Proxyパスワード */
	PasswordFieldEditor proxyPassword = null;

	/** HTTPSキーストアパス */
	StringFieldEditor keyStorePath = null;

	/** HTTPSキーストアパス ワード*/
	PasswordFieldEditor keyStorePassword = null;

	/**
	 * Set style
	 */
	public ClusterControlCorePreferencePage() {
		super(GRID);
	}

	/**
	 * 初期値が設定されたインスタンスを返します。
	 */
	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(ClusterControlPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * 設定フィールドを生成します。
	 */
	@Override
	public void createFieldEditors() {
		Composite parent = this.getFieldEditorParent();
		GridData gridData = null;
		// 接続関連
		Group group = new Group(parent, SWT.SHADOW_NONE);
		WidgetTestUtil.setTestId(this, "group", group);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 10;
		gridData.verticalSpan = 10;
		group.setLayoutData(gridData);

		group.setText(Messages.getString("connection.setting"));

		// 接続死活間隔
		managerPollingInterval = new IntegerFieldEditor(KEY_INTERVAL, Messages.getString("manager.polling.interval"), group);
		managerPollingInterval.setValidRange(VALUE_INTERVAL_MIN, VALUE_INTERVAL_MAX);
		String[] args1 = {
				Integer.toString(VALUE_INTERVAL_MIN),
				Integer.toString(VALUE_INTERVAL_MAX) };
		managerPollingInterval.setErrorMessage(Messages.getString("message.hinemos.8", args1 ));
		this.addField(managerPollingInterval);

		// HTTPタイムアウト
		String[] args2 = {
				Integer.toString(VALUE_HTTP_TIMEOUT_MIN),
				Integer.toString(VALUE_HTTP_TIMEOUT_MAX) };
		httpRequestTimeout = new IntegerFieldEditor(KEY_HTTP_REQUEST_TIMEOUT, Messages.getString("connection.request.timeout"), group);
		httpRequestTimeout.setValidRange(VALUE_HTTP_TIMEOUT_MIN, VALUE_HTTP_TIMEOUT_MAX);
		httpRequestTimeout.setErrorMessage(Messages.getString("message.hinemos.8", args2 ));
		this.addField(httpRequestTimeout);

		// Proxy接続関連
		Group proxyGroup = new Group(parent, SWT.SHADOW_NONE);
		WidgetTestUtil.setTestId(this, "proxygroup", proxyGroup);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 10;
		gridData.verticalSpan = 10;
		proxyGroup.setLayoutData(gridData);

		proxyGroup.setText(Messages.getString("proxy.connection.setting"));

		// Proxy有効/無効
		proxyEnable = new BooleanFieldEditor(KEY_PROXY_ENABLE, Messages.getString("proxy.connection.enable"), proxyGroup);
		this.addField(proxyEnable);

		// Proxyホスト
		proxyHost = new StringFieldEditor(KEY_PROXY_HOST, Messages.getString("proxy.connection.host"), proxyGroup);
		proxyHost.setTextLimit(DataRangeConstant.VARCHAR_256);
		this.addField(proxyHost);

		// Proxyポート
		proxyPort = new IntegerFieldEditor(KEY_PROXY_PORT, Messages.getString("proxy.connection.port"), proxyGroup);
		proxyPort.setValidRange(VALUE_PROXY_PORT_MIN, VALUE_PROXY_PORT_MAX);
		String[] args3 = {
				Integer.toString(VALUE_PROXY_PORT_MIN),
				Integer.toString(VALUE_PROXY_PORT_MAX) };
		proxyPort.setErrorMessage(Messages.getString("message.hinemos.8", args3 ));
		this.addField(proxyPort);

		// Proxyユーザ
		proxyUser = new StringFieldEditor(KEY_PROXY_USER, Messages.getString("proxy.connection.user"), proxyGroup);
		proxyUser.setTextLimit(DataRangeConstant.VARCHAR_256);
		this.addField(proxyUser);

		// Proxyパスワード
		proxyPassword = new PasswordFieldEditor(KEY_PROXY_PASSWORD, Messages.getString("proxy.connection.password"), proxyGroup);
		proxyPassword.setTextLimit(DataRangeConstant.VARCHAR_256);
		this.addField(proxyPassword);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		m_log.trace("ClusterControlCorePreferencePage.performOk() start");

		// 接続死活間隔の更新
		ClientSession.restartChecktask(managerPollingInterval.getIntValue());

		// JAX-WSタイムアウト値の更新
		EndpointManager.setHttpRequestTimeout( httpRequestTimeout.getIntValue() );
		m_log.info("request.timeout=" + EndpointManager.getHttpRequestTimeout());

		boolean result = super.performOk();

		//セッションに接続先URLを格納
		IPreferenceStore store = this.getPreferenceStore();
		store.getString(URL);

		LoginManager.setup();
		return result;
	}
}
