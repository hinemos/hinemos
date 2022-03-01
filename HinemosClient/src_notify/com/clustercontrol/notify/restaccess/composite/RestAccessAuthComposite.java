/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.restaccess.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.RestAccessAuthHttpHeaderResponse;
import org.openapitools.client.model.RestAccessInfoResponse;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.composite.TextWithParameterComposite;
import com.clustercontrol.composite.action.LongNumberVerifyListener;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.notify.restaccess.bean.HttpMethodMessage;
import com.clustercontrol.notify.restaccess.dialog.HttpHeaderCreateDialog;
import com.clustercontrol.notify.restaccess.viewer.HttpHeaderTableRecord;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;

/**
 * 
 * RESTアクセス情報 認証設定部分（タブ向け）
 *
 */
public class RestAccessAuthComposite extends Composite {
	/** カラム数（全体） */
	public static final int WIDTH = 15;

	/** カラム数（認証方式ラジオボタン） */
	public static final int WIDTH_AUTH_TYPE_RADIO = 3;

	/** カラム数（Basicラベル） */
	public static final int WIDTH_BASIC_LABEL = 3;

	/** カラム数（Basicテキスト） */
	public static final int WIDTH_BASIC_TEXT = 4;

	/** カラム数（URLラベル） */
	public static final int WIDTH_URL_LABEL = 3;

	/** カラム数（URL入出力） */
	public static final int WIDTH_URL_IO = WIDTH  - WIDTH_URL_LABEL;

	/** カラム数（HTTPメソッドコンボ） */
	public static final int WIDTH_METHOOD_COMBO = 3;

	/** カラム数（HTTPヘッダー一覧） */
	public static final int WIDTH_HTTP_HEADER_LIST = 9;
	
	/** 高さ（HTTPヘッダー一覧） */
	public static final int HIGH_HTTP_HEADER_LIST = 20;
	
	/** 高さ（HTTP BODY） */
	public static final int HIGH_HTTP_BODY = 20;

	/** 認証方式 RADIO */
	private Button buttonAuthNone = null; // 無し
	private Button buttonAuthBasic = null; // BASIC
	private Button buttonAuthUrl = null; // URL

	/** Basic認証 グループ */
	private Group m_groupBasicAuth = null;

	/** Basic認証 user */
	private Text m_textUser = null;

	/**  Basic認証 password */
	private Text m_textPassword = null;

	/** URL認証 グループ */
	private Group m_groupURLAuth = null;

	/** 認証リクエスト グループ */
	private Group m_groupHttpRequest = null;

	/** URL */
	private Text m_textURL = null;

	/** メソッド */
	private Combo m_comboHttpMethod = null;

	/** ヘッダー */
	private HttpHeaderListComposite m_tableHttpHeader = null;
	private Button btnAddHeader;
	private Button btnModHeader;
	private Button btnDelHeader;

	/** ボディ */
	private TextWithParameterComposite m_textHttpBody = null;

	/** Token取得 グループ */
	private Group m_groupGetToken = null;

	/** Token取得用正規表現 */
	private Text m_textGetTokenRegex = null;

	/** Token有効期間 */
	private Text m_textTokenValidTerm = null;

	/**
	 * コンストラクタ
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
	public RestAccessAuthComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {

		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = WIDTH;
		this.setLayout(layout);

		// Composite

		// 認証方式選択（RADIO）
		this.buttonAuthNone = new Button(this, SWT.RADIO);
		this.buttonAuthNone.setText(Messages.getString("restaccess.none"));
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = WIDTH_AUTH_TYPE_RADIO;
		this.buttonAuthNone.setLayoutData(gridData);
		this.buttonAuthNone.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		this.buttonAuthBasic = new Button(this, SWT.RADIO);
		this.buttonAuthBasic.setText(Messages.getString("restaccess.basic.authentication"));
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = WIDTH_AUTH_TYPE_RADIO;
		this.buttonAuthBasic.setLayoutData(gridData);
		this.buttonAuthBasic.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		this.buttonAuthUrl = new Button(this, SWT.RADIO);
		this.buttonAuthUrl.setText(Messages.getString("restaccess.url.authentication"));
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = WIDTH_AUTH_TYPE_RADIO;
		this.buttonAuthUrl.setLayoutData(gridData);
		this.buttonAuthUrl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});
		label = new Label(this, SWT.CENTER);
		label.setText("");
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH - (WIDTH_AUTH_TYPE_RADIO * 3);
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * Basic認証 グループ
		 */
		m_groupBasicAuth = new Group(this, SWT.NONE);
		GridLayout layoutBasicAuth = new GridLayout(1, true);
		layoutBasicAuth.marginWidth = 5;
		layoutBasicAuth.marginHeight = 5;
		layoutBasicAuth.numColumns = WIDTH;
		m_groupBasicAuth.setLayout(layoutBasicAuth);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_groupBasicAuth.setLayoutData(gridData);
		m_groupBasicAuth.setText(Messages.getString("restaccess.basic.authentication"));


		label = new Label(m_groupBasicAuth, SWT.CENTER );
		label.setText(Messages.getString("restaccess.user"));
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_BASIC_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		this.m_textUser = new Text(m_groupBasicAuth, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_BASIC_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textUser.setLayoutData(gridData);
		this.m_textUser.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		label = new Label(m_groupBasicAuth, SWT.CENTER );
		label.setText(Messages.getString("restaccess.password"));
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_BASIC_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		this.m_textPassword = new Text(m_groupBasicAuth, SWT.BORDER | SWT.PASSWORD);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_BASIC_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textPassword.setLayoutData(gridData);
		this.m_textPassword.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * URL認証 グループ
		 */
		m_groupURLAuth = new Group(this, SWT.NONE);
		GridLayout layoutURLAuth = new GridLayout(1, true);
		layoutURLAuth.marginWidth = 5;
		layoutURLAuth.marginHeight = 5;
		layoutURLAuth.numColumns = WIDTH;
		m_groupURLAuth.setLayout(layoutURLAuth);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_groupURLAuth.setLayoutData(gridData);
		m_groupURLAuth.setText(Messages.getString("restaccess.url.authentication"));

		/*
		 * 認証リクエスト グループ
		 */
		m_groupHttpRequest = new Group(m_groupURLAuth, SWT.NONE);
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
		m_groupHttpRequest.setText(Messages.getString("restaccess.authentication.request"));

		// URL
		label = new Label(m_groupHttpRequest, SWT.CENTER );
		label.setText(Messages.getString("restaccess.http.url"));
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_URL_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		this.m_textURL = new Text(m_groupHttpRequest, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 11;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textURL.setLayoutData(gridData);
		this.m_textURL.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// http メソッド
		label = new Label(m_groupHttpRequest, SWT.CENTER );
		label.setText(Messages.getString("restaccess.http.method"));
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_URL_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		this.m_comboHttpMethod = new Combo(m_groupHttpRequest, SWT.BORDER | SWT.LEFT | SWT.SINGLE | SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_METHOOD_COMBO;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboHttpMethod.setLayoutData(gridData);
		this.m_comboHttpMethod.add(HttpMethodMessage.STRING_GET);
		this.m_comboHttpMethod.add(HttpMethodMessage.STRING_POST);
		this.m_comboHttpMethod.add(HttpMethodMessage.STRING_PUT);
		this.m_comboHttpMethod.add(HttpMethodMessage.STRING_DELETE);
		this.m_comboHttpMethod.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});
		label = new Label(m_groupHttpRequest, SWT.CENTER);
		label.setText("");// dummy
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_URL_IO - WIDTH_METHOOD_COMBO;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// httpヘッダー
		initializeHeaderPart(m_groupHttpRequest);

		// ボディ
		label = new Label(m_groupHttpRequest, SWT.CENTER);
		label.setText(Messages.getString("restaccess.http.body"));
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_URL_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalSpan = HIGH_HTTP_BODY;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		label.setLayoutData(gridData);
		this.m_textHttpBody = new TextWithParameterComposite(m_groupHttpRequest,
				SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_URL_IO;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalSpan = HIGH_HTTP_BODY;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		this.m_textHttpBody.setLayoutData(gridData);
		this.m_textHttpBody.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * Toke取得 グループ
		 */
		m_groupGetToken = new Group(m_groupURLAuth, SWT.NONE);
		GridLayout layoutGetToken = new GridLayout(1, true);
		layoutGetToken.marginWidth = 5;
		layoutGetToken.marginHeight = 5;
		layoutGetToken.numColumns = WIDTH;
		m_groupGetToken.setLayout(layoutGetToken);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_groupGetToken.setLayoutData(gridData);
		m_groupGetToken.setText(Messages.getString("restaccess.http.token.get"));

		label = new Label(m_groupGetToken, SWT.CENTER);
		label.setText(Messages.getString("restaccess.http.token.regex"));
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		this.m_textGetTokenRegex = new Text(m_groupGetToken, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 11;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textGetTokenRegex.setLayoutData(gridData);
		this.m_textGetTokenRegex.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		label = new Label(m_groupGetToken, SWT.CENTER);
		label.setText(Messages.getString("restaccess.http.token.valid.term"));
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		this.m_textTokenValidTerm = new Text(m_groupGetToken, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textTokenValidTerm.setLayoutData(gridData);
		this.m_textTokenValidTerm.addVerifyListener(
				new LongNumberVerifyListener(1, DataRangeConstant.LONG_HIGH));
		this.m_textTokenValidTerm.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		label = new Label(m_groupGetToken, SWT.BEGINNING);
		label.setText(Messages.getString("milli.sec"));
		gridData = new GridData();
		gridData.horizontalSpan = 7;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
	}

	private void initializeHeaderPart(Composite parent) {
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		label = new Label(parent, SWT.CENTER );
		label.setText(Messages.getString("restaccess.http.header"));
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_URL_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalSpan = HIGH_HTTP_HEADER_LIST;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		label.setLayoutData(gridData);

		m_tableHttpHeader = new HttpHeaderListComposite(parent, SWT.BORDER, false);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_HTTP_HEADER_LIST ;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalSpan = HIGH_HTTP_HEADER_LIST;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		m_tableHttpHeader.setLayoutData(gridData);

		Composite buttonComposite = new Composite(parent, SWT.NONE);
		GridLayout buttonLayout = new GridLayout(1, true);
		buttonLayout.numColumns = 1;
		buttonComposite.setLayout(buttonLayout);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_URL_IO - WIDTH_HTTP_HEADER_LIST;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalSpan = HIGH_HTTP_HEADER_LIST;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		buttonComposite.setLayoutData(gridData);

		btnAddHeader = new Button(buttonComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.btnAddHeader.setLayoutData(gridData);
		btnAddHeader.setText(Messages.getString("add"));
		btnAddHeader.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

				HttpHeaderCreateDialog dialog = new HttpHeaderCreateDialog(shell, null, false);
				if (dialog.open() == IDialogConstants.OK_ID) {
					if (m_tableHttpHeader.getHeaderList() != null) {
						// 順番号をセットしてリストに追加
						long orderNO = m_tableHttpHeader.getHeaderList().size() + 1;
						dialog.getInputData().setHeaderOrderNo(orderNO);
						m_tableHttpHeader.addHeaderList(dialog.getInputData());
						m_tableHttpHeader.update();
					}
				}
			}
		});

		btnModHeader = new Button(buttonComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		btnModHeader.setLayoutData(gridData);
		btnModHeader.setText(Messages.getString("modify"));
		btnModHeader.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				// キーをもとに、変更対象の情報を引き渡し、必要なら変更結果を反映
				Integer tagIndex = m_tableHttpHeader.getHeaderListIndexAsSelection();
				if (tagIndex == null) {
					String[] args = { Messages.getString("restaccess.http.header") };
					MessageDialog.openError(null, Messages.getString("failed"),
							Messages.getString("validation.required_select.message", args));
					return;
				}
				HttpHeaderTableRecord tagRec = m_tableHttpHeader.getHeaderList().get(tagIndex);
				HttpHeaderCreateDialog dialog = new HttpHeaderCreateDialog(shell, tagRec, false);
				if (dialog.open() == IDialogConstants.OK_ID) {
					HttpHeaderTableRecord newRec = dialog.getInputData();
					newRec.setHeaderOrderNo(tagRec.getHeaderOrderNo());
					m_tableHttpHeader.getHeaderList().set(tagIndex, newRec);
					m_tableHttpHeader.update();
				}
			}
		});

		btnDelHeader = new Button(buttonComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		btnDelHeader.setLayoutData(gridData);
		btnDelHeader.setText(Messages.getString("delete"));
		btnDelHeader.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// 選択したテーブルのキーを取得
				Long orderNo = m_tableHttpHeader.getSelectionOrderNo();
				if (orderNo == null) {
					String[] args = { Messages.getString("restaccess.http.header") };
					MessageDialog.openError(null, Messages.getString("failed"),
							Messages.getString("validation.required_select.message", args));
					return;
				}
				String[] args = new String[] { String.valueOf(orderNo) };
				if (MessageDialog.openConfirm(null, Messages.getString("confirmed"),
						Messages.getString("message.restaccess.12", args))) {
					m_tableHttpHeader.deleteSelectionInfo();
					
					m_tableHttpHeader.update();
				}
			}
		});
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update() {
		// 必須項目を明示
		if(buttonAuthBasic.getSelection() ){
			setRequiredColor(this.m_textUser);
			setRequiredColor(this.m_textPassword);
			m_textURL.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			m_textHttpBody.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			m_textGetTokenRegex.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			m_textTokenValidTerm.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}else if(buttonAuthUrl.getSelection() ){
			m_textUser.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			m_textPassword.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			setRequiredColor(this.m_textURL);
			if(m_comboHttpMethod.getText().equals(HttpMethodMessage.STRING_POST) || m_comboHttpMethod.getText().equals(HttpMethodMessage.STRING_PUT) ){
				setRequiredColor(this.m_textHttpBody);
			}else{
				m_textHttpBody.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
			setRequiredColor(this.m_textGetTokenRegex);
			m_textTokenValidTerm.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}else{
			m_textUser.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			m_textPassword.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			m_textURL.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			m_textHttpBody.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			m_textGetTokenRegex.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			m_textTokenValidTerm.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		
	}

	/**
	 * RESTアクセス情報をコンポジットに反映します。
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobEndStatusInfo
	 */
	public void reflectRestAccessInfo(RestAccessInfoResponse info) {
		// 認証方式
		if (info.getAuthType() != null) {
			switch (info.getAuthType()) {
			case NONE:
				buttonAuthNone.setSelection(true);
				break;
			case BASIC:
				buttonAuthBasic.setSelection(true);
				break;
			case  URL:
				buttonAuthUrl.setSelection(true);
				break;
			default:
				buttonAuthNone.setSelection(true);
			}
		}else{
			buttonAuthNone.setSelection(true);
		}
		
		// Basic User
		if (info.getAuthBasicUser() != null) {
			m_textUser.setText(info.getAuthBasicUser());
		}
		// Basic password
		if (info.getAuthBasicPassword() != null) {
			m_textPassword.setText(info.getAuthBasicPassword());
		}

		// URL
		if (info.getAuthUrlString() != null) {
			m_textURL.setText(info.getAuthUrlString());
		}
		// httpメソッド
		if (info.getAuthUrlMethodType() != null) {
			m_comboHttpMethod.setText(HttpMethodMessage.typeEnumValueToString(info.getAuthUrlMethodType().getValue()));
		}else{
			m_comboHttpMethod.setText(HttpMethodMessage.getHttpMethodDefault());
		}

		// Rest認証用Httpヘッダー
		m_tableHttpHeader.getHeaderList().clear();
		for (RestAccessAuthHttpHeaderResponse headerRec : info.getAuthHttpHeaders()) {
			HttpHeaderTableRecord addObj = new HttpHeaderTableRecord();
			try {
				RestClientBeanUtil.convertBeanSimple(headerRec, addObj);
				m_tableHttpHeader.addHeaderList(addObj);
			} catch (HinemosUnknown e) {
				// エラーは発生し得ないので握りつぶす
			}
		}
		m_tableHttpHeader.update();
		// Body
		if (info.getAuthUrlBody() != null) {
			m_textHttpBody.setText(info.getAuthUrlBody());
		}
		// Token取得用正規表現
		if (info.getAuthUrlGetRegex() != null) {
			m_textGetTokenRegex.setText(info.getAuthUrlGetRegex());
		}
		// Token有効期間
		if (info.getAuthUrlValidTerm() != null) {
			m_textTokenValidTerm.setText(String.valueOf(info.getAuthUrlValidTerm()));
		}

	}

	/**
	 * コンポジットの情報を、RESTアクセス情報に反映する。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobEndStatusInfo
	 */
	public ValidateResult setRestAccessInfo(RestAccessInfoResponse info) {
		ValidateResult result = null;

		
		//認証方式
		if( buttonAuthNone.getSelection() ){
			info.setAuthType(RestAccessInfoResponse.AuthTypeEnum.NONE);
		}else if(buttonAuthBasic.getSelection() ){
			info.setAuthType(RestAccessInfoResponse.AuthTypeEnum.BASIC);
		}else if(buttonAuthUrl.getSelection() ){
			info.setAuthType(RestAccessInfoResponse.AuthTypeEnum.URL);
		}else{
			info.setAuthType(null);
		}
		
		// Basic user
		info.setAuthBasicUser(m_textUser.getText());
		// Basic Password
		info.setAuthBasicPassword(m_textPassword.getText());
		
		// URL
		info.setAuthUrlString(m_textURL.getText());
		// httpメソッド
		info.setAuthUrlMethodType(RestAccessInfoResponse.AuthUrlMethodTypeEnum
				.fromValue(HttpMethodMessage.stringToTypeEnumValue(this.m_comboHttpMethod.getText())));
		// httpヘッダー
		List<RestAccessAuthHttpHeaderResponse> authHeaders = new ArrayList<RestAccessAuthHttpHeaderResponse>();
		for (HttpHeaderTableRecord rec : m_tableHttpHeader.getHeaderList()) {
			RestAccessAuthHttpHeaderResponse setting = new RestAccessAuthHttpHeaderResponse();
			try {
				RestClientBeanUtil.convertBeanSimple(rec, setting);
				authHeaders.add(setting);
			} catch (HinemosUnknown e) {
				// エラーは発生し得ないので握りつぶす
			}
		}
		info.setAuthHttpHeaders(authHeaders);
		// httpボディ
		info.setAuthUrlBody(this.m_textHttpBody.getText());
		// Token取得用正規表現
		info.setAuthUrlGetRegex(m_textGetTokenRegex.getText());
		// Token有効期間
		if (!(this.m_textTokenValidTerm.getText().trim().isEmpty())) {
			try{
				info.setAuthUrlValidTerm(Long.valueOf(m_textTokenValidTerm.getText()));
			}
			catch(NumberFormatException e){
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.common.15", new Object[]{Messages.getString("restaccess.http.token.valid.term")}));
				return result;
			}
		}

		return null;
	}

	private static void setRequiredColor(Text tagText) {
			if (tagText.getText() == null || tagText.getText().isEmpty()) {
				tagText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			}else{
				tagText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
	}
	private static void setRequiredColor(TextWithParameterComposite tagText) {
		if (tagText.getText() == null || tagText.getText().isEmpty()) {
			tagText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			tagText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}
}
