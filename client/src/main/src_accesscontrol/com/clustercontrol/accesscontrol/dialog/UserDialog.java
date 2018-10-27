/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.dialog;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.accesscontrol.util.AccessEndpointWrapper;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.access.InvalidRole_Exception;
import com.clustercontrol.ws.access.UnEditableUser_Exception;
import com.clustercontrol.ws.access.UserDuplicate_Exception;
import com.clustercontrol.ws.access.UserInfo;

/**
 * アカウント[ユーザの作成・変更]ダイアログクラスです。
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class UserDialog extends CommonDialog {

	/** モード */
	private int mode = 0;

	/** 変更用ダイアログ判別フラグ */
	private boolean isModifyDialog = false;

	/** ユーザID　テキストボックス */
	private Text textUserId = null;

	/** ユーザ名　テキストボックス */
	private Text textUserName = null;

	/** 説明　テキストボックス */
	private Text textDescription = null;

	/** メールアドレス　テキストボックス */
	private Text textMailAddress = null;

	/** カラム数 */
	public static final int WIDTH	 = 15;

	/** カラム数（ラベル）。 */
	public static final int WIDTH_LABEL = 4;

	/** カラム数（テキスト）。 */
	public static final int WIDTH_TEXT = 10;

	/** 入力値を保持するオブジェクト。 */
	private UserInfo inputData;

	private boolean permission = false;		// 現在のユーザが変更権限をもつか否か

	/** マネージャ名コンボボックス用コンポジット */
	private ManagerListComposite m_managerComposite = null;

	private String managerName = null;

	/**
	 * コンストラクタ
	 *
	 * @param parent 親シェル
	 * @param managerName マネージャ名
	 * @param uid ユーザID
	 * @param isModifyDialog 変更用ダイアログとして利用する場合は、true
	 */
	public UserDialog(Shell parent, String managerName, UserInfo userInfo, boolean isModifyDialog) {
		super(parent);

		this.managerName = managerName;
		this.inputData = userInfo;
		this.isModifyDialog = isModifyDialog;

	}

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 *
	 * @see org.eclipse.jface.window.Window#getInitialSize()
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(600, 600);
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親コンポジット
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#customizeDialog(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages
				.getString("dialog.accesscontrol.user.create.modify")); //$NON-NLS-1$

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 15;
		parent.setLayout(layout);

		/*
		 * マネージャ
		 */
		Label labelManager = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "manager", labelManager);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = WIDTH_LABEL;
		labelManager.setLayoutData(gridData);
		labelManager.setText(Messages.getString("facility.manager") + " : ");
		if(this.isModifyDialog){
			this.m_managerComposite = new ManagerListComposite(parent, SWT.NONE, false);
		} else {
			this.m_managerComposite = new ManagerListComposite(parent, SWT.NONE, true);
		}
		WidgetTestUtil.setTestId(this, "managerComposite", this.m_managerComposite);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.grabExcessHorizontalSpace = true;
		this.m_managerComposite.setLayoutData(gridData);

		if(this.managerName != null) {
			this.m_managerComposite.setText(this.managerName);
		}

		/*
		 * ユーザID
		 */
		// ラベル
		Label label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "userid", label);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = WIDTH_LABEL;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("user.id") + " : ");
		// テキスト
		this.textUserId = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "id", textUserId);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.textUserId.setLayoutData(gridData);
		this.textUserId.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// プロパティの取得及び設定
		if (this.isModifyDialog) {
			if (permission) {
				this.mode = PropertyDefineConstant.MODE_MODIFY;
			}else{
				this.mode = PropertyDefineConstant.MODE_SHOW;
			}
		} else {
			this.mode = PropertyDefineConstant.MODE_ADD;
		}

		/*
		 * ユーザ名
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "username", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("user.name") + " : ");
		// テキスト
		this.textUserName = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "name", textUserName);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.textUserName.setLayoutData(gridData);
		this.textUserName.addModifyListener(new ModifyListener(){
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
		WidgetTestUtil.setTestId(this, "description", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("description") + " : ");
		// テキスト
		this.textDescription = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "description", textDescription);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.textDescription.setLayoutData(gridData);
		this.textDescription.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * メールアドレス
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "mailaddress", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("mail.address") + " : ");
		// テキスト
		this.textMailAddress = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "mailaddress", textMailAddress);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.textMailAddress.setLayoutData(gridData);
		this.textMailAddress.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		shell.pack();
		shell.setSize(new Point(550, shell.getSize().y));

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

		this.setInputData();
	}

	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#validate()
	 */
	@Override
	protected ValidateResult validate() {
		// 入力値生成
		this.inputData = this.createInputData();
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

		UserInfo userInfo = this.inputData;
		if(userInfo == null){
			return result;
		}

		AccessEndpointWrapper wrapper = AccessEndpointWrapper.getWrapper(m_managerComposite.getText());
		if(!this.isModifyDialog){
			// 作成の場合
			try {
				wrapper.addUserInfo(userInfo);
				result = true;

				Object[] arg = {this.m_managerComposite.getText()};
				// 完了メッセージ
				MessageDialog.openInformation(
						null,
						Messages.getString("successful"),
						Messages.getString("message.accesscontrol.7", arg));

			} catch (UserDuplicate_Exception e) {
				//ユーザID取得
				String args[] = { userInfo.getUserId() };

				// ユーザIDが重複している場合、エラーダイアログを表示する
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.20", args));

			} catch (Exception e) {
				String errMessage = "";
				if (e instanceof InvalidRole_Exception) {
					// 権限なし
					MessageDialog.openInformation(null, Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
				} else {
					errMessage = ", " + HinemosMessage.replace(e.getMessage());
				}
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.accesscontrol.8") + errMessage);

			}
		} else{
			// 変更の場合
			try {
				wrapper.modifyUserInfo(userInfo);
				result = true;

				Object[] arg = {this.m_managerComposite.getText()};
				// 完了メッセージ
				MessageDialog.openInformation(
						null,
						Messages.getString("successful"),
						Messages.getString("message.accesscontrol.9", arg));

			} catch (Exception e) {
				String errMessage = "";
				if (e instanceof InvalidRole_Exception) {
					// 権限なし
					MessageDialog.openInformation(null, Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
				} else if (e instanceof UnEditableUser_Exception) {
					// 変更できないユーザの場合（システムユーザ、内部モジュール用ユーザ）
					MessageDialog.openInformation(null, Messages.getString("message"),
							Messages.getString("message.accesscontrol.38"));
				} else {
					errMessage = ", " + HinemosMessage.replace(e.getMessage());
				}
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.accesscontrol.10") + errMessage);

			}
		}

		return result;
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){
		// 必須項目を明示

		// ユーザID
		if("".equals(this.textUserId.getText())){
			this.textUserId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.textUserId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// ユーザ名
		if("".equals(this.textUserName.getText())){
			this.textUserName.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.textUserName.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 指定されたユーザ情報を元に、ダイアログのテキストボックスにユーザID、ユーザ名、説明をセットする
	 *
	 * @param userInfo 設定値として用いるユーザ情報
	 */
	protected void setInputData() {
		// 各項目に反映
		// ユーザID
		if (inputData.getUserId() != null) {
			this.textUserId.setText(inputData.getUserId());
		}
		// ユーザ名
		if (inputData.getUserName() != null) {
			this.textUserName.setText(inputData.getUserName());
		}
		// 説明
		if (inputData.getDescription() != null) {
			this.textDescription.setText(inputData.getDescription());
		}
		// メールアドレス
		if (inputData.getMailAddress() != null) {
			this.textMailAddress.setText(inputData.getMailAddress());
		}

		// 入力制御
		if(this.mode == PropertyDefineConstant.MODE_SHOW
				|| this.mode == PropertyDefineConstant.MODE_MODIFY){
			this.textUserId.setEnabled(false);
		} else {
			this.textUserId.setEnabled(true);
		}

		/* *** 仮おき *** start ***
		List<String> roleList = new ArrayList<String>();
		roleList.add("AccessControlRead");
		roleList.add("AccessControlWrite");
		roleList.add("CalendarRead");
		roleList.add("CalendarWrite");
		roleList.add("HinemosUser");
		roleList.add("InfraExecute");
		roleList.add("InfraRead");
		roleList.add("InfraWrite");
		roleList.add("JobManagementExecute");
		roleList.add("JobManagementRead");
		roleList.add("JobManagementWrite");
		roleList.add("MaintenanceRead");
		roleList.add("MaintenanceWrite");
		roleList.add("MonitorResultRead");
		roleList.add("MonitorResultWrite");
		roleList.add("MonitorSettingRead");
		roleList.add("MonitorSettingWrite");
		roleList.add("NotifyRead");
		roleList.add("NotifyWrite");
		roleList.add("PerformanceExecute");
		roleList.add("PerformanceRead");
		roleList.add("RepositoryExecute");
		roleList.add("RepositoryRead");
		roleList.add("RepositoryWrite");
		roleList.add("VmManagementExecute");
		roleList.add("VmManagementRead");
		roleList.add("VmManagementWrite");
		*** 仮おき ***  end  *** */



		// 必須入力項目を可視化
		this.update();
	}

	/**
	 * ダイアログ中で入力されたユーザID・ユーザ名・説明を読みだし、ユーザ情報として返却する。<BR>
	 * 入力値チェックは行わないため、null文字などが入っている場合もありうる。
	 *
	 * @return ユーザ情報
	 */
	private UserInfo createInputData() {
		final UserInfo info = new UserInfo();
		info.setUserId(this.textUserId.getText());
		info.setUserName(this.textUserName.getText());
		info.setDescription(this.textDescription.getText());
		info.setMailAddress(this.textMailAddress.getText());
		return info;
	}

	/**
	 * ＯＫボタンのテキストを返します。
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		if (this.isModifyDialog) {
			return Messages.getString("modify"); //$NON-NLS-1$
		} else {
			return Messages.getString("register"); //$NON-NLS-1$
		}
	}

	/**
	 * キャンセルボタンのテキストを返します。
	 *
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel"); //$NON-NLS-1$
	}
}
