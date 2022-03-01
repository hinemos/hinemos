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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.RestAccessInfoResponse;
import org.openapitools.client.model.RestAccessSendHttpHeaderResponse;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.composite.TextWithParameterComposite;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.notify.restaccess.bean.HttpMethodMessage;
import com.clustercontrol.notify.restaccess.dialog.HttpHeaderCreateDialog;
import com.clustercontrol.notify.restaccess.viewer.HttpHeaderTableRecord;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;

/**
 * 
 * RESTアクセス情報 送信設定部分（タブ向け）
 *
 */
public class RestAccessSendComposite extends Composite {
	/** カラム数 */
	public static final int WIDTH = 15;

	/** カラム数（ラベル）。 */
	public static final int WIDTH_LABEL = 3;

	/** カラム数（入出力部） */
	public static final int WIDTH_IO = WIDTH  - WIDTH_LABEL;

	/** カラム数（HTTPメソッドコンボ） */
	public static final int WIDTH_METHOOD_COMBO = 3;

	/** カラム数（HTTPヘッダー一覧） */
	public static final int WIDTH_HTTP_HEADER_LIST = 9;
	
	/** 高さ（HTTPヘッダー一覧） */
	public static final int HIGH_HTTP_HEADER_LIST = 15;
	
	/** 高さ（HTTP BODY） */
	public static final int HIGH_HTTP_BODY = 15;

	/** 高さ（余白） */
	public static final int HIGH_PADDING = 15;
	
	/** URL */
	private Text m_textURL = null;

	/**メソッド */
	private Combo m_comboHttpMethod = null;

	/** ヘッダー */
	private HttpHeaderListComposite m_tableHttpHeader = null;
	private Button btnAddHeader;
	private Button btnModHeader;
	private Button btnDelHeader;

	
	/** ボディ */
	private TextWithParameterComposite m_textHttpBody = null;

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
	public RestAccessSendComposite(Composite parent, int style ) {
		super(parent, style);
		initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {

		Label label =null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = WIDTH;
		this.setLayout(layout);
		
		//変数置換向けのツールチップメッセージ
		String tooletipTextForRep = Messages.getString("notify.parameter.tooltip")
				+ Messages.getString("replace.parameter.restaccess")
				+ Messages.getString("replace.parameter.notify")
				+ Messages.getString("replace.parameter.node");

		// URL
		label = new Label(this, SWT.CENTER);
		label.setText(Messages.getString("restaccess.http.url"));
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		this.m_textURL = new Text(this, SWT.BORDER );
		this.m_textURL.setEnabled(true);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_IO;
		gridData.horizontalAlignment = GridData.FILL ;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textURL.setLayoutData(gridData);
		this.m_textURL.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		this.m_textURL.setToolTipText(tooletipTextForRep);

		// http メソッド
		label = new Label(this, SWT.CENTER );
		label.setText(Messages.getString("restaccess.http.method"));
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		this.m_comboHttpMethod = new Combo(this, SWT.BORDER | SWT.LEFT | SWT.SINGLE | SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_METHOOD_COMBO;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboHttpMethod.setLayoutData(gridData);
		this.m_comboHttpMethod.add(HttpMethodMessage.STRING_GET) ;
		this.m_comboHttpMethod.add(HttpMethodMessage.STRING_POST) ;
		this.m_comboHttpMethod.add(HttpMethodMessage.STRING_PUT) ;
		this.m_comboHttpMethod.add(HttpMethodMessage.STRING_DELETE) ;
		this.m_comboHttpMethod.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});
		label = new Label(this, SWT.CENTER );
		label.setText("");// dummy
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_IO - WIDTH_METHOOD_COMBO;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);


		// httpヘッダー
		initializeHeaderPart();
		
		// ボディ
		label = new Label(this, SWT.CENTER);
		label.setText(Messages.getString("restaccess.http.body"));//
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalSpan = HIGH_HTTP_BODY;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		label.setLayoutData(gridData);
		this.m_textHttpBody = new TextWithParameterComposite(this, SWT.BORDER |SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_IO;
		gridData.horizontalAlignment = GridData.FILL ;
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
		this.m_textHttpBody.setToolTipText(tooletipTextForRep);
		
		//下部余白パティング
		label = new Label(this, SWT.CENTER);
		label.setText("");// dummy
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalSpan = HIGH_PADDING;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		label.setLayoutData(gridData);
		
		update();
	}

	private void initializeHeaderPart(){
		final boolean setGrabExcessVerticalSpace = false;
		Label label =null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		label = new Label(this, SWT.CENTER );
		label.setText(Messages.getString("restaccess.http.header"));
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalSpan = HIGH_HTTP_HEADER_LIST;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = setGrabExcessVerticalSpace;
		label.setLayoutData(gridData);

		m_tableHttpHeader = new HttpHeaderListComposite(this, SWT.BORDER, true);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_HTTP_HEADER_LIST;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalSpan = HIGH_HTTP_HEADER_LIST;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = setGrabExcessVerticalSpace;
		m_tableHttpHeader.setLayoutData(gridData);

		Composite buttonComposite = new Composite(this, SWT.NONE);
		GridLayout buttonLayout = new GridLayout(1, true);
		buttonLayout.numColumns = 1;
		buttonComposite.setLayout(buttonLayout);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_IO - WIDTH_HTTP_HEADER_LIST;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalSpan = HIGH_HTTP_HEADER_LIST;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = setGrabExcessVerticalSpace;
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
				
				HttpHeaderCreateDialog dialog = new HttpHeaderCreateDialog(shell, null, true);
				if (dialog.open() == IDialogConstants.OK_ID) {
					if( m_tableHttpHeader.getHeaderList() != null) {
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
				Integer tagIndex = m_tableHttpHeader.getHeaderListIndexAsSelection();
				if (tagIndex == null) {
					String[] args = { Messages.getString("restaccess.http.header") };
					MessageDialog.openError(null, Messages.getString("failed"),
							Messages.getString("validation.required_select.message", args));
					return;
				}
				//キーをもとに、変更対象の情報を引き渡し、必要なら変更結果を反映
				HttpHeaderTableRecord tagRec = m_tableHttpHeader.getHeaderList().get(tagIndex);
				HttpHeaderCreateDialog dialog = new HttpHeaderCreateDialog(shell, tagRec, true);
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

				//選択したテーブルのキーを取得
				Long orderNo = m_tableHttpHeader.getSelectionOrderNo();
				if (orderNo == null) {
					String[] args = { Messages.getString("restaccess.http.header") };
					MessageDialog.openError(null, Messages.getString("failed"),
							Messages.getString("validation.required_select.message", args));
					return;
				}
				String[] args = new String[] { String.valueOf(orderNo) };
				if (MessageDialog.openConfirm(
						null,
						Messages.getString("confirmed"),
						Messages.getString("message.restaccess.12", args))) {
					m_tableHttpHeader.deleteSelectionInfo();
					m_tableHttpHeader.update();
				}
			}
		});
	}

	/**
	 * 更新処理
	 */
	public void update(){
		// 必須項目を明示
		setRequiredColor(this.m_textURL);
		if(m_comboHttpMethod.getText().equals(HttpMethodMessage.STRING_POST) || m_comboHttpMethod.getText().equals(HttpMethodMessage.STRING_PUT) ){
			setRequiredColor(this.m_textHttpBody);
		}else{
			m_textHttpBody.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		
	}

	/**
	 * RESTアクセス情報をコンポジットに反映します。
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobEndStatusInfo
	 */
	public void reflectRestAccessInfo( RestAccessInfoResponse info ) {
		// URL
		if (info.getSendUrlString() != null) {
			m_textURL.setText(info.getSendUrlString());
		}
		
		//httpメソッド
		if (info.getSendHttpMethodType() != null) {
			m_comboHttpMethod.setText(HttpMethodMessage.typeEnumValueToString(info.getSendHttpMethodType().getValue()));
		}else{
			m_comboHttpMethod.setText(HttpMethodMessage.getHttpMethodDefault());
		}

		//Rest送信用Httpヘッダー
		m_tableHttpHeader.getHeaderList().clear();
		for(RestAccessSendHttpHeaderResponse headerRec : info.getSendHttpHeaders() ){
			HttpHeaderTableRecord addObj = new HttpHeaderTableRecord();
			try {
				RestClientBeanUtil.convertBeanSimple(headerRec, addObj);
				m_tableHttpHeader.addHeaderList(addObj);
			} catch (HinemosUnknown e) {
				// エラーは発生し得ないので握りつぶす
			}
		}
		m_tableHttpHeader.update();
		
		//Body
		if (info.getSendHttpBody() != null) {
			m_textHttpBody.setText(info.getSendHttpBody());
		}
		update();
	}

	/**
	 * コンポジットの情報を、RESTアクセス情報に反映する。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobEndStatusInfo
	 */
	public ValidateResult setRestAccessInfo( RestAccessInfoResponse info) {
		// URL
		info.setSendUrlString(m_textURL.getText());
		// httpメソッド
		info.setSendHttpMethodType(RestAccessInfoResponse.SendHttpMethodTypeEnum
				.fromValue(HttpMethodMessage.stringToTypeEnumValue(this.m_comboHttpMethod.getText())));
		// httpヘッダー
		List<RestAccessSendHttpHeaderResponse> sendHeaders = new ArrayList<RestAccessSendHttpHeaderResponse>();
		for( HttpHeaderTableRecord rec :m_tableHttpHeader.getHeaderList() ){
			RestAccessSendHttpHeaderResponse setting = new RestAccessSendHttpHeaderResponse();
			try {
				RestClientBeanUtil.convertBeanSimple(rec, setting);
				sendHeaders.add(setting);
			} catch (HinemosUnknown e) {
				// エラーは発生し得ないので握りつぶす
			}
		}
		info.setSendHttpHeaders(sendHeaders);
		// httpボディ
		info.setSendHttpBody(this.m_textHttpBody.getText());
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
