/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.restaccess.dialog;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.AddRestAccessInfoRequest;
import org.openapitools.client.model.RestAccessInfoResponse;
import org.openapitools.client.model.ModifyRestAccessInfoRequest;

import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.common.util.CommonRestClientWrapper;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.RestAccessDuplicate;
import com.clustercontrol.notify.restaccess.composite.RestAccessAuthComposite;
import com.clustercontrol.notify.restaccess.composite.RestAccessControlComposite;
import com.clustercontrol.notify.restaccess.composite.RestAccessSendComposite;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;

/**
 * RESTアクセス情報 作成・変更 ダイアログクラス<BR>
 *
 */
public class RestAccessInfoCreateDialog extends CommonDialog {

	// ログ
	private static Log m_log = LogFactory.getLog(RestAccessInfoCreateDialog.class);

	/** カラム数 */
	public static final int WIDTH = 15;

	/** カラム数（ラベル）。 */
	public static final int WIDTH_LABEL = 4;

	/** カラム数（テキスト）。 */
	public static final int WIDTH_TEXT = 10;

	/** 入力値を保持するオブジェクト。 */
	private RestAccessInfoResponse inputData = null;

	/** 入力値の正当性を保持するオブジェクト。 */
	protected ValidateResult validateResult = null;

	/** ダイアログ表示時の処理タイプ */
	private int mode;

	/** マネージャ名 */
	private String managerName = null;

	/** RESTアクセス情報ID  */
	private String RestAccessInfoId = null;

	/** RESTアクセス情報ID テキストボックス。 */
	private Text textRestAccessInfoId = null;

	/** 説明 テキストボックス。 */
	private Text textDescription = null;

	/** オーナーロールID用テキスト */
	private RoleIdListComposite m_ownerRoleId = null;

	/** マネージャリスト用コンポジット */
	private ManagerListComposite m_managerComposite = null;
	
	/** タブフォルダー */
	private TabFolder m_tabFolder = null;

	/** 送信タブ用コンポジット */
	private RestAccessSendComposite m_sendComposite = null;

	/** 制御タブ用コンポジット */
	private RestAccessControlComposite m_controlComposite = null;

	/** 認証タブ用コンポジット */
	private RestAccessAuthComposite m_authComposite = null;

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param RestAccessInfoId
	 *            変更するRESTアクセス情報情報のRESTアクセス情報ID
	 * @param dataOperationType
	 *            データ処理タイプ
	 */
	public RestAccessInfoCreateDialog(Shell parent, String managerName, String RestAccessInfoId, int mode) {
		super(parent);

		this.managerName = managerName;
		this.RestAccessInfoId = RestAccessInfoId;
		this.mode = mode;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のコンポジット
	 *
	 * @see #setInputData(RestAccessInfoData)
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.restaccess.info.create.modify"));

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

		/*
		 * マネージャ
		 */
		label = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("facility.manager") + " : ");
		if (this.mode == PropertyDefineConstant.MODE_MODIFY || this.mode == PropertyDefineConstant.MODE_SHOW) {
			this.m_managerComposite = new ManagerListComposite(parent, SWT.NONE, false);
		} else {
			this.m_managerComposite = new ManagerListComposite(parent, SWT.NONE, true);
		}
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_managerComposite.setLayoutData(gridData);
		this.m_managerComposite.setText(this.managerName);
		if (this.mode != PropertyDefineConstant.MODE_MODIFY && this.mode != PropertyDefineConstant.MODE_SHOW) {
			this.m_managerComposite.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String managerName = m_managerComposite.getText();
					m_ownerRoleId.createRoleIdList(managerName);
				}
			});
		}

		/*
		 * RESTアクセスID
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("restaccess.id") + " : ");
		// テキスト
		this.textRestAccessInfoId = new Text(parent, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.textRestAccessInfoId.setLayoutData(gridData);
		if (this.mode == PropertyDefineConstant.MODE_SHOW || this.mode == PropertyDefineConstant.MODE_MODIFY) {
			this.textRestAccessInfoId.setEnabled(false);
		}
		this.textRestAccessInfoId.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		/*
		 * 説明
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("description") + " : ");
		// テキスト
		this.textDescription = new Text(parent, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.textDescription.setLayoutData(gridData);

		/*
		 * オーナーロールID
		 */
		Label labelRoleId = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelRoleId.setLayoutData(gridData);
		labelRoleId.setText(Messages.getString("owner.role.id") + " : "); 
		if (this.mode == PropertyDefineConstant.MODE_ADD) {
			this.m_ownerRoleId = new RoleIdListComposite(parent, SWT.NONE, managerName, true, Mode.OWNER_ROLE);
		} else {
			this.m_ownerRoleId = new RoleIdListComposite(parent, SWT.NONE, managerName, false, Mode.OWNER_ROLE);
		}
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_ownerRoleId.setLayoutData(gridData);

		
		//タブ レイアウト
		m_tabFolder = new TabFolder(parent, SWT.NONE);
		GridLayout groupLayout = new GridLayout(1, true);
		groupLayout.marginWidth = 5;
		groupLayout.marginHeight = 5;
		groupLayout.numColumns = 15;
		m_tabFolder.setLayout(groupLayout);
		gridData = new GridData();
		gridData.horizontalSpan = 40;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_tabFolder.setLayoutData(gridData);

		// 送信タブ セット
		m_sendComposite = new RestAccessSendComposite(m_tabFolder, SWT.NONE);
		TabItem tabItem1 = new TabItem(m_tabFolder, SWT.NONE);
		tabItem1.setText(Messages.getString("restaccess.setting.send"));//
		tabItem1.setControl(m_sendComposite);
		m_sendComposite.setLayoutData(new GridData());

				
		// 認証タブ セット
		m_authComposite = new RestAccessAuthComposite(m_tabFolder, SWT.NONE);
		TabItem tabItem2 = new TabItem(m_tabFolder, SWT.NONE);
		tabItem2.setText(Messages.getString("restaccess.setting.auth"));//
		tabItem2.setControl(m_authComposite);
		m_authComposite.setLayoutData(new GridData());

		// 制御タブ セット
		m_controlComposite = new RestAccessControlComposite(m_tabFolder, SWT.NONE);
		TabItem tabItem3 = new TabItem(m_tabFolder, SWT.NONE);
		tabItem3.setText(Messages.getString("restaccess.setting.control"));
		tabItem3.setControl(m_controlComposite);
		m_controlComposite.setLayoutData(new GridData());

		m_tabFolder.setSelection(0);

		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		line.setLayoutData(gridData);

		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		shell.pack();
		shell.setSize(new Point(650, shell.getSize().y));

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

		// RESTアクセス情報IDが指定されている場合、その情報を初期表示する。
		RestAccessInfoResponse info = null;
		if (this.RestAccessInfoId != null) {
			try {
				CommonRestClientWrapper wrapper = CommonRestClientWrapper.getWrapper(this.managerName);
				info = wrapper.getRestAccessInfo(this.RestAccessInfoId);
			} catch (InvalidRole e) {
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
				return;
			} catch (Exception e) {
				m_log.warn("customizeDialog() getRestAccessInfo, " + HinemosMessage.replace(e.getMessage()), e);
				MessageDialog.openError(null, Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", "
								+ HinemosMessage.replace(e.getMessage()));
				return;
			}
		} else {
			info = new RestAccessInfoResponse();
		}
		this.setInputData(info);
	}

	/**
	 * 更新処理
	 *
	 */
	public void update() {
		setRequiredColor(this.textRestAccessInfoId);
	}

	/**
	 * 入力値を保持したRESTアクセス情報情報を返します。
	 *
	 * @return RESTアクセス情報情報
	 */
	public RestAccessInfoResponse getInputData() {
		return this.inputData;
	}

	/**
	 * 引数で指定されたRESTアクセス情報情報の値を、各項目に設定します。
	 *
	 * @param restAccessInfo
	 *            設定値として用いるRESTアクセス情報情報
	 */
	protected void setInputData(RestAccessInfoResponse restAccessInfo) {

		this.inputData = restAccessInfo;

		// 各項目に反映
		// RESTアクセス情報ID
		if (restAccessInfo.getRestAccessId() != null) {
			this.textRestAccessInfoId.setText(restAccessInfo.getRestAccessId());
		}
		// 説明
		if (restAccessInfo.getDescription() != null) {
			this.textDescription.setText(restAccessInfo.getDescription());
		}
		// オーナーロールID取得
		if (restAccessInfo.getOwnerRoleId() != null) {
			this.m_ownerRoleId.setText(restAccessInfo.getOwnerRoleId());
		}

		// 入力制御
		if (this.mode == PropertyDefineConstant.MODE_SHOW) {
			this.textRestAccessInfoId.setEnabled(false);
			this.textDescription.setEnabled(false);
		}
		
		this.m_sendComposite.reflectRestAccessInfo(restAccessInfo);
		this.m_authComposite.reflectRestAccessInfo(restAccessInfo);
		this.m_controlComposite.reflectRestAccessInfo(restAccessInfo);

		// 必須入力項目を可視化
		this.update();
	}
	
	private ValidateResult setRestAccessInfo(RestAccessInfoResponse info){
		ValidateResult ret = null;
		
		// RESTアクセスID
		if (this.textRestAccessInfoId.getText() != null && !"".equals((this.textRestAccessInfoId.getText()).trim())) {
			info.setRestAccessId(this.textRestAccessInfoId.getText());
		}
		// 説明
		if (this.textDescription.getText() != null && !"".equals((this.textDescription.getText()).trim())) {
			info.setDescription(this.textDescription.getText());
		}
		// オーナーロールID
		if (this.m_ownerRoleId.getText().length() > 0) {
			info.setOwnerRoleId(this.m_ownerRoleId.getText());
		}
		return ret;
	}

	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 */
	@Override
	protected ValidateResult validate() {
		ValidateResult ret = null;
		// 入力値生成
		RestAccessInfoResponse info = new RestAccessInfoResponse();

		// 本体ダイアログ
		ret = setRestAccessInfo(info);
		if(ret!=null){
			return ret;
		}
		// 送信タブ
		ret = this.m_sendComposite.setRestAccessInfo(info);
		if(ret!=null){
			return ret;
		}
		// 認証タブ
		ret = this.m_authComposite.setRestAccessInfo(info);
		if(ret!=null){
			return ret;
		}
		// 制御タブ
		ret = this.m_controlComposite.setRestAccessInfo(info);
		if(ret!=null){
			return ret;
		}
		
		this.inputData = info;
		return super.validate();
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

		RestAccessInfoResponse info = this.inputData;
		if (info != null) {
			String errMessage = "";
			String managerName = m_managerComposite.getText();
			String[] args = { info.getRestAccessId(), managerName };
			CommonRestClientWrapper wrapper = CommonRestClientWrapper.getWrapper(managerName);
			if (this.mode == PropertyDefineConstant.MODE_ADD) {
				// 作成の場合
				try {
					AddRestAccessInfoRequest request = new AddRestAccessInfoRequest();
					RestClientBeanUtil.convertBean(info, request);
					wrapper.addRestAccessInfo(request);
					result = true;

					MessageDialog.openInformation(null, Messages.getString("successful"),
							Messages.getString("message.restaccess.1", args));

				} catch (RestAccessDuplicate e) {
					// RESTアクセス情報IDが重複している場合、エラーダイアログを表示する
					MessageDialog.openInformation(null, Messages.getString("message"),
							Messages.getString("message.restaccess.10", args));
				} catch (Exception e) {
					if (e instanceof InvalidRole) {
						MessageDialog.openInformation(null, Messages.getString("message"),
								Messages.getString("message.accesscontrol.16"));
					} else {
						errMessage = ", " + HinemosMessage.replace(e.getMessage());
					}
					MessageDialog.openError(null, Messages.getString("failed"),
							Messages.getString("message.restaccess.2", args) + errMessage);
				}
			} else if (this.mode == PropertyDefineConstant.MODE_MODIFY) {
				// 変更の場合
				try {
					ModifyRestAccessInfoRequest request = new ModifyRestAccessInfoRequest();
					RestClientBeanUtil.convertBean(info, request);

					wrapper.modifyRestAccessInfo(info.getRestAccessId(), request);
					result = true;

					MessageDialog.openInformation(null, Messages.getString("successful"),
							Messages.getString("message.restaccess.3", args));

				} catch (Exception e) {
					if (e instanceof InvalidRole) {
						MessageDialog.openInformation(null, Messages.getString("message"),
								Messages.getString("message.accesscontrol.16"));
					} else {
						errMessage = ", " + HinemosMessage.replace(e.getMessage());
					}

					MessageDialog.openError(null, Messages.getString("failed"),
							Messages.getString("message.restaccess.4", args) + errMessage);
				}
			}
		}

		return result;
	}

	/**
	 * ＯＫボタンのテキストを返します。
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンのテキストを返します。
	 *
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

	/**
	 * ボタンを生成します。<BR>
	 * 参照フラグが<code> true </code>の場合は閉じるボタンを生成し、<code> false </code>
	 * の場合は、デフォルトのボタンを生成します。
	 *
	 * @param parent
	 *            ボタンバーコンポジット
	 *
	 * @see #createButtonsForButtonBar(Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		if (this.mode != PropertyDefineConstant.MODE_SHOW) {
			super.createButtonsForButtonBar(parent);
		} else {
			// 閉じるボタン
			this.createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("close"), false);
		}
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
