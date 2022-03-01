/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.restaccess.composite;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.RestAccessInfoResponse;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.composite.action.LongNumberVerifyListener;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.Messages;

/**
 * 
 * RESTアクセス情報 制御設定部分（タブ向け）
 *
 */
public class RestAccessControlComposite extends Composite {

	/** カラム数 */
	public static final int WIDTH = 15;

	/** カラム数（ラベル）。 */
	public static final int WIDTH_LABEL = 5;

	/** カラム数（テキストFULL） */
	public static final int WIDTH_TEXT_FULL = WIDTH - WIDTH_LABEL;

	/** カラム数（数値テキスト） */
	public static final int WIDTH_TEXT_NUMBER_INPUT = 4;
	
	/** default of http connect timeout(ms) */
	private static final long HTTP_CONNECT_TIMEOUT_DEFAULT = 60 * 1000;
	
	/** default of http request timeout(ms) */
	private static final long HTTP_REQUEST_TIMEOUT_DEFAULT = 60 * 1000;

	/** http retry num */
	private static final long HTTP_RETRY_NUM_DEFAULT = 0;

	/** コネクションタイムアウト */
	private Text m_textConnectTimeout = null;

	/** リクエスト タイムアウト */
	private Text m_textRequestTimeout = null;

	/** 失敗時リトライ回数 */
	private Text m_textRetryNumber = null;

	/** プロキシ：有効無効 */
	private Button m_chkbxProxy = null;

	/** Webプロキシ URL */
	private Text m_textProxyUrl = null;

	/** Webプロキシ ポート番号 */
	private Text m_textProxyPort = null;

	/** Webプロキシ 認証ユーザ */
	private Text m_textProxyUser = null;

	/** Webプロキシ 認証パスワード */
	private Text m_textProxyPassword = null;

	/** httpリクエスト グループ */
	private Group m_groupHttpRequest = null;

	/** Webプロキシ グループ */
	private Group m_groupWebproxy = null;

	
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
	public RestAccessControlComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {

		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = WIDTH;
		this.setLayout(layout);
		
		setHttpRequestGroup();
		setWebProxyGroup();
	}

	private void setHttpRequestGroup(){
		GridData gridData = null;
		Label label = null;
		/*
		 * httpリクエスト グループ
		 */
		m_groupHttpRequest = new Group(this, SWT.NONE);
		GridLayout layoutHttpRequest = new GridLayout(1, true);
		layoutHttpRequest.marginWidth = 5;
		layoutHttpRequest.marginHeight = 5;
		layoutHttpRequest.numColumns = WIDTH;
		m_groupHttpRequest.setLayout(layoutHttpRequest);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_groupHttpRequest.setLayoutData(gridData);
		m_groupHttpRequest.setText(Messages.getString("restaccess.http.request"));
		
		// コネクション タイムアウト
		Label connectionTimeoutTitle = new Label(m_groupHttpRequest, SWT.CENTER);
		connectionTimeoutTitle.setText(Messages.getString("restaccess.http.connect.timeout"));
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		connectionTimeoutTitle.setLayoutData(gridData);
		this.m_textConnectTimeout = new Text(m_groupHttpRequest, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT_NUMBER_INPUT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textConnectTimeout.setLayoutData(gridData);
		this.m_textConnectTimeout.addVerifyListener(
				new LongNumberVerifyListener(1, DataRangeConstant.LONG_HIGH));
		this.m_textConnectTimeout.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		label = new Label(m_groupHttpRequest, SWT.BEGINNING);
		label.setText(Messages.getString("milli.sec"));
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = WIDTH_TEXT_FULL - WIDTH_TEXT_NUMBER_INPUT;
		label.setLayoutData(gridData);
		
		// リクエスト タイムアウト
		Label requestTimeoutTitle = new Label(m_groupHttpRequest, SWT.CENTER);
		requestTimeoutTitle.setText(Messages.getString("restaccess.http.request.timeout"));
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		requestTimeoutTitle.setLayoutData(gridData);
		this.m_textRequestTimeout = new Text(m_groupHttpRequest, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT_NUMBER_INPUT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textRequestTimeout.setLayoutData(gridData);
		this.m_textRequestTimeout.addVerifyListener(
				new LongNumberVerifyListener(1, DataRangeConstant.LONG_HIGH));
		this.m_textRequestTimeout.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		label = new Label(m_groupHttpRequest, SWT.BEGINNING);
		label.setText(Messages.getString("milli.sec"));
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = WIDTH_TEXT_FULL - WIDTH_TEXT_NUMBER_INPUT;
		label.setLayoutData(gridData);

		// 失敗時リトライ回数
		Label retryNumberTitle = new Label(m_groupHttpRequest, SWT.CENTER);
		retryNumberTitle.setText(Messages.getString("restaccess.http.request.retry.num"));
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		retryNumberTitle.setLayoutData(gridData);
		this.m_textRetryNumber = new Text(m_groupHttpRequest, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT_NUMBER_INPUT ;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textRetryNumber.setLayoutData(gridData);
		this.m_textRetryNumber.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH));
		this.m_textRetryNumber.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		label = new Label(m_groupHttpRequest, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = WIDTH_TEXT_FULL - WIDTH_TEXT_NUMBER_INPUT;
		label.setLayoutData(gridData);
	}

	private void setWebProxyGroup(){
		Label label =null;
		GridData gridData = null;
		/*
		 * Webプロキシ グループ
		 */
		m_groupWebproxy = new Group(this, SWT.NONE);
		GridLayout layoutHttpRequest = new GridLayout(1, true);
		layoutHttpRequest.marginWidth = 5;
		layoutHttpRequest.marginHeight = 5;
		layoutHttpRequest.numColumns = WIDTH;
		m_groupWebproxy.setLayout(layoutHttpRequest);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_groupWebproxy.setLayoutData(gridData);
		m_groupWebproxy.setText(Messages.getString("restaccess.proxy"));


		// 利用する チェック
		m_chkbxProxy = new Button(m_groupWebproxy, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = SWT.RIGHT ;
		gridData.grabExcessHorizontalSpace = true;
		m_chkbxProxy.setLayoutData(gridData);
		this.m_chkbxProxy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});
		label = new Label(m_groupWebproxy, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH -1;
		gridData.horizontalAlignment = GridData.BEGINNING ;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("restaccess.proxy.use"));

		// URL
		label = new Label(m_groupWebproxy, SWT.CENTER);
		label.setText(Messages.getString("restaccess.http.url"));
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		this.m_textProxyUrl = new Text(m_groupWebproxy, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT_FULL;
		gridData.horizontalAlignment = GridData.FILL ;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textProxyUrl.setLayoutData(gridData);
		this.m_textProxyUrl.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		
		// ポート番号
		label = new Label(m_groupWebproxy, SWT.CENTER);
		label.setText(Messages.getString("port.number"));
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL ;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		this.m_textProxyPort = new Text(m_groupWebproxy, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT_NUMBER_INPUT;
		gridData.horizontalAlignment = GridData.FILL ;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textProxyPort.setLayoutData(gridData);
		this.m_textProxyPort.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH));
		this.m_textProxyPort.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		label = new Label(m_groupWebproxy, SWT.CENTER);
		label.setText("");//dummy
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT_FULL - WIDTH_TEXT_NUMBER_INPUT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 認証ユーザー
		label = new Label(m_groupWebproxy, SWT.CENTER);
		label.setText(Messages.getString("restaccess.user"));
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL ;
		gridData.horizontalAlignment = GridData.FILL ;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		this.m_textProxyUser = new Text(m_groupWebproxy, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT_FULL - 5;
		gridData.horizontalAlignment = GridData.FILL ;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textProxyUser.setLayoutData(gridData);
		this.m_textProxyUser.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		label = new Label(m_groupWebproxy, SWT.CENTER);
		label.setText("");//dummy
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		
		// 認証パスワード
		label = new Label(m_groupWebproxy, SWT.CENTER);
		label.setText(Messages.getString("restaccess.password"));
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL ;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		this.m_textProxyPassword = new Text(m_groupWebproxy, SWT.BORDER | SWT.PASSWORD);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT_FULL -5 ;
		gridData.horizontalAlignment = GridData.FILL ;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textProxyPassword.setLayoutData(gridData);
		this.m_textProxyPassword.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		label = new Label(m_groupWebproxy, SWT.CENTER);
		label.setText("");//dummy
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		// 必須項目を明示
		setRequiredColor(this.m_textRequestTimeout);
		setRequiredColor(this.m_textConnectTimeout);
		setRequiredColor(this.m_textRetryNumber);
		if( this.m_chkbxProxy.getSelection()){
			setRequiredColor(this.m_textProxyUrl);
			setRequiredColor(this.m_textProxyPort);
		}else{
			this.m_textProxyUrl.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			this.m_textProxyPort.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}


	/**
	 * RESTアクセス情報をコンポジットに反映します。
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobEndStatusInfo
	 */
	public void reflectRestAccessInfo(RestAccessInfoResponse info) {
		// 初期値connect
		this.m_textConnectTimeout.setText(String.valueOf(HTTP_CONNECT_TIMEOUT_DEFAULT));
		this.m_textRequestTimeout.setText(String.valueOf(HTTP_REQUEST_TIMEOUT_DEFAULT));
		this.m_textRetryNumber.setText(String.valueOf(HTTP_RETRY_NUM_DEFAULT));

		// 設定
		if (info.getHttpConnectTimeout() != null) {
			this.m_textConnectTimeout.setText(String.valueOf(info.getHttpConnectTimeout()));
		}
		if (info.getHttpRequestTimeout() != null) {
			this.m_textRequestTimeout.setText(String.valueOf(info.getHttpRequestTimeout()));
		}
		if (info.getHttpRetryNum() != null) {
			this.m_textRetryNumber.setText(String.valueOf(info.getHttpRetryNum()));
		}
		if (info.getUseWebProxy() != null) {
			this.m_chkbxProxy.setSelection(info.getUseWebProxy());
		}
		if (info.getWebProxyUrlString() != null) {
			this.m_textProxyUrl.setText(info.getWebProxyUrlString());
		}
		if (info.getWebProxyPort() != null) {
			this.m_textProxyPort.setText(String.valueOf(info.getWebProxyPort()));
		}
		if (info.getWebProxyAuthUser() != null) {
			this.m_textProxyUser.setText(info.getWebProxyAuthUser());
		}
		if (info.getWebProxyAuthPassword() != null) {
			this.m_textProxyPassword.setText(info.getWebProxyAuthPassword());
		}
	}

	/**
	 * コンポジットの情報を、RESTアクセス情報に反映する。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobEndStatusInfo
	 */
	public ValidateResult setRestAccessInfo( RestAccessInfoResponse info) {
		if (!(this.m_textConnectTimeout.getText().trim().isEmpty())) {
			info.setHttpConnectTimeout(Long.valueOf(this.m_textConnectTimeout.getText()));
		}
		if (!(this.m_textRequestTimeout.getText().trim().isEmpty())) {
			info.setHttpRequestTimeout(Long.valueOf(this.m_textRequestTimeout.getText()));
		}
		if (!(this.m_textRetryNumber.getText().trim().isEmpty())) {
			info.setHttpRetryNum(Integer.valueOf(this.m_textRetryNumber.getText()));
		}
		info.setUseWebProxy(m_chkbxProxy.getSelection());
		info.setWebProxyUrlString(m_textProxyUrl.getText());
		if (!(this.m_textProxyPort.getText().trim().isEmpty())) {
			info.setWebProxyPort(Integer.valueOf(this.m_textProxyPort.getText()));
		}
		info.setWebProxyAuthUser(m_textProxyUser.getText());
		info.setWebProxyAuthPassword(m_textProxyPassword.getText());
		return null;
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
