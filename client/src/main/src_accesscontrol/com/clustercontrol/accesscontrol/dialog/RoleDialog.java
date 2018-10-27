/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.dialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import com.clustercontrol.ws.access.FacilityDuplicate_Exception;
import com.clustercontrol.ws.access.InvalidRole_Exception;
import com.clustercontrol.ws.access.RoleDuplicate_Exception;
import com.clustercontrol.ws.access.RoleInfo;
import com.clustercontrol.ws.access.UnEditableRole_Exception;

/**
 * アカウント[ロールの作成・変更]ダイアログクラスです。
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class RoleDialog extends CommonDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( RoleDialog.class );

	/** ロールID */
	private String roleId = "";

	/** モード */
	private int mode = 0;

	/** 変更用ダイアログ判別フラグ */
	private boolean isModifyDialog = false;

	/** ロールID　テキストボックス */
	private Text textRoleId = null;

	/** ロール名　テキストボックス */
	private Text textRoleName = null;

	/** 説明　テキストボックス */
	private Text textDescription = null;

	/** カラム数 */
	public static final int WIDTH	 = 15;

	/** カラム数（ラベル）。 */
	public static final int WIDTH_LABEL = 4;

	/** カラム数（テキスト）。 */
	public static final int WIDTH_TEXT = 10;

	/** 入力値を保持するオブジェクト。 */
	private RoleInfo inputData = null;

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
	public RoleDialog(Shell parent, String managerName, String roleId, boolean isModifyDialog) {
		super(parent);

		this.managerName = managerName;
		this.roleId = roleId;
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
				.getString("dialog.accesscontrol.role.create.modify"));

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
		 * ロールID
		 */
		// ラベル
		Label label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, label);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = WIDTH_LABEL;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("role.id") + " : ");
		// テキスト
		this.textRoleId = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "id", textRoleId);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.textRoleId.setLayoutData(gridData);
		this.textRoleId.addModifyListener(new ModifyListener(){
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
		 * ロール名
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "role", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_LABEL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("role.name") + " : ");
		// テキスト
		this.textRoleName = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "name", textRoleName);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.textRoleName.setLayoutData(gridData);
		this.textRoleName.addModifyListener(new ModifyListener(){
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

		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		shell.pack();
		shell.setSize(new Point(550, shell.getSize().y));

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

		// ロールIDが指定されている場合、その情報を初期化する。
		RoleInfo info = null;
		if (this.roleId != null) {
			try {
				AccessEndpointWrapper wrapper = AccessEndpointWrapper.getWrapper(m_managerComposite.getText());
				info = wrapper.getRoleInfo(this.roleId);
				this.setInputData(info);
			} catch (InvalidRole_Exception e) {
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (Exception e) {
				m_log.warn("customizeDialog(), " + HinemosMessage.replace(e.getMessage()), e);
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}
		} else {
			info = new RoleInfo();
			this.setInputData(info);
		}
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

		RoleInfo roleInfo = this.inputData;
		if(roleInfo == null){
			return result;
		}

		AccessEndpointWrapper wrapper = AccessEndpointWrapper.getWrapper(this.m_managerComposite.getText());
		if(!this.isModifyDialog){
			// 作成の場合
			try {
				wrapper.addRoleInfo(roleInfo);
				result = true;

				Object[] arg = {this.m_managerComposite.getText()};
				// 完了メッセージ
				MessageDialog.openInformation(
						null,
						Messages.getString("successful"),
						Messages.getString("message.accesscontrol.26", arg));

			} catch (RoleDuplicate_Exception e) {
				//ロールID取得
				String args[] = { roleInfo.getRoleId() };

				// ロールIDが重複している場合、エラーダイアログを表示する
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.33", args));
			} catch (FacilityDuplicate_Exception e) {
				//ロールID取得
				String args[] = { roleInfo.getRoleId() };

				// ロールIDが重複している場合、エラーダイアログを表示する
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.repository.26", args));
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
						Messages.getString("message.accesscontrol.27") + errMessage);

			}
		} else{
			// 変更の場合
			try {
				wrapper.modifyRoleInfo(roleInfo);
				result = true;

				Object[] arg = {this.m_managerComposite.getText()};
				// 完了メッセージ
				MessageDialog.openInformation(
						null,
						Messages.getString("successful"),
						Messages.getString("message.accesscontrol.28", arg));

			} catch (Exception e) {
				String errMessage = "";
				if (e instanceof InvalidRole_Exception) {
					// 権限なし
					MessageDialog.openInformation(null, Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
				} else if (e instanceof UnEditableRole_Exception) {
					//　変更不可なロールの場合はエラー（システムロール、内部モジュール用ロール）
					MessageDialog.openInformation(null, Messages.getString("message"),
							Messages.getString("message.accesscontrol.40"));
				} else {
					errMessage = ", " + HinemosMessage.replace(e.getMessage());
				}
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.accesscontrol.29") + errMessage);

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

		// ロールID
		if("".equals(this.textRoleId.getText())){
			this.textRoleId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.textRoleId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// ロール名
		if("".equals(this.textRoleName.getText())){
			this.textRoleName.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.textRoleName.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 指定されたロール情報を元に、ダイアログのテキストボックスにロールID、ロール名、説明をセットする
	 *
	 * @param roleInfo 設定値として用いるロール情報
	 */
	protected void setInputData(RoleInfo roleInfo) {

		this.inputData = roleInfo;

		// 各項目に反映
		// ロールID
		if (roleInfo.getRoleId() != null) {
			this.textRoleId.setText(roleInfo.getRoleId());
		}
		// ロール名
		if (roleInfo.getRoleName() != null) {
			this.textRoleName.setText(roleInfo.getRoleName());
		}
		// 説明
		if (roleInfo.getDescription() != null) {
			this.textDescription.setText(roleInfo.getDescription());
		}
		// 入力制御
		if(this.mode == PropertyDefineConstant.MODE_SHOW
				|| this.mode == PropertyDefineConstant.MODE_MODIFY){
			this.textRoleId.setEnabled(false);
		} else {
			this.textRoleId.setEnabled(true);
		}

		// 必須入力項目を可視化
		this.update();
	}

	/**
	 * ダイアログ中で入力されたRoleID・Role名・説明を読みだし、ロール情報として返却する。<BR>
	 * 入力値チェックは行わないため、null文字などが入っている場合もありうる。
	 *
	 * @return ロール情報
	 */
	private RoleInfo createInputData() {
		final RoleInfo info = new RoleInfo();
		info.setRoleId(this.textRoleId.getText());
		info.setRoleName(this.textRoleName.getText());
		info.setDescription(this.textDescription.getText());
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
