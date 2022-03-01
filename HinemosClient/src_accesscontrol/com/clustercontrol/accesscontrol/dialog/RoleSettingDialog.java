/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.AssignUserWithRoleRequest;
import org.openapitools.client.model.RoleInfoResponse;
import org.openapitools.client.model.UserInfoResponse;

import com.clustercontrol.accesscontrol.util.AccessRestClientWrapper;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.UnEditableRole;
import com.clustercontrol.fault.UserDuplicate;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * アカウント[ユーザの選択]ダイアログクラスです。
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class RoleSettingDialog extends CommonDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( RoleSettingDialog.class );

	/** ロールID */
	private String roleId = "";

	/** マネージャ名 */
	private String managerName = "";

	/** 全ユーザ一覧　リストボックス */
	private List listNotRoleUser = null;

	/** 所属ユーザ一覧　リストボックス */
	private List listRoleUser = null;

	/** 所属　ボタン */
	private Button buttonRoleUser = null;

	/** 所属解除　ボタン */
	private Button buttonNotRoleUser = null;

	/** カラム数 */
	public static final int WIDTH	 = 15;

	/** カラム数（ラベル）。 */
	public static final int WIDTH_LABEL = 4;

	/** カラム数（テキスト）。 */
	public static final int WIDTH_TEXT = 10;

	/** 入力値を保持するオブジェクト。 */
	private RoleInfoResponse inputData = null;

	/** 全ユーザ一覧の表示名をキーとし、ユーザIDを格納しているハッシュマップ */
	private HashMap<String, String> mapNotRoleUser = null;

	/** 所属ユーザ一覧の表示名をキーとし、ユーザIDを格納しているハッシュマップ */
	private HashMap<String, String> mapRoleUser = null;

	/**
	 * コンストラクタ
	 *
	 * @param parent 親シェル
	 * @paramn managerName マネージャ名
	 * @param uid ユーザID
	 */
	public RoleSettingDialog(Shell parent, String managerName, String roleId) {
		super(parent);
		this.managerName = managerName;
		this.roleId = roleId;
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
				.getString("dialog.accesscontrol.role.setting"));

		// ロール情報の取得
		RoleInfoResponse info = null;
		try {
			AccessRestClientWrapper wrapper = AccessRestClientWrapper.getWrapper(this.managerName);
			info = wrapper.getRoleInfo(this.roleId);
		} catch (InvalidRole e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
			throw new InternalError(e.getMessage());
		} catch (Exception e) {
			m_log.warn("customizeDialog(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + e.getMessage());
			throw new InternalError(e.getMessage());
		}

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = WIDTH;
		parent.setLayout(layout);

		/*
		 * ロール名
		 */
		// ラベル
		Label label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "role", label);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("role.name") + " : " + info.getRoleName());

		/*
		 * 全ユーザ用一覧用コンポジット
		 */
		Composite compositeNotRole = new Composite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "all", compositeNotRole);

		layout = new GridLayout(1, true);
		layout.numColumns = 1;
		compositeNotRole.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 6;
		gridData.verticalSpan = 2;
		compositeNotRole.setLayoutData(gridData);

		// 全ユーザ一覧 ラベル
		label = new Label(compositeNotRole, SWT.NONE);
		WidgetTestUtil.setTestId(this, "alluserlist", label);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("RoleSettingDialog.not_role_user"));

		// 全ユーザ一覧 リスト
		this.listNotRoleUser = new List(compositeNotRole, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		WidgetTestUtil.setTestId(this, "all", compositeNotRole);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = this.listNotRoleUser.getItemHeight() * 12;
		this.listNotRoleUser.setLayoutData(gridData);

		// 全ユーザ一覧 ハッシュマップ
		this.mapNotRoleUser = new HashMap<String, String>();

		/*
		 * 操作ボタン用コンポジット
		 */
		Composite compositeButton = new Composite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "composite", compositeButton);
		layout = new GridLayout(1, true);
		layout.numColumns = 1;
		compositeButton.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 3;
		compositeButton.setLayoutData(gridData);

		// 空
		label = new Label(compositeButton, SWT.NONE);
		WidgetTestUtil.setTestId(this, "blank1", label);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 所属ボタン
		Label dummy = new Label(compositeButton, SWT.NONE);
		WidgetTestUtil.setTestId(this, "dummy", dummy);
		this.buttonRoleUser = this.createButton(compositeButton, Messages.getString("RoleSettingDialog.role_user_button"));
		WidgetTestUtil.setTestId(this, "assign", buttonRoleUser);
		this.buttonRoleUser.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e) {
				String[] items = listNotRoleUser.getSelection();
				for (String item : items) {
					listNotRoleUser.remove(item);
					listRoleUser.add(item);

					// 表示名をキーとしたユーザIDのHashMapを内容の遷移に合わせて移動
					mapRoleUser.put(item, mapNotRoleUser.get(item));
					mapNotRoleUser.remove(item);
				}
			}
		});

		// 所属解除ボタン
		dummy = new Label(compositeButton, SWT.NONE);
		WidgetTestUtil.setTestId(this, "unassign", dummy);
		this.buttonNotRoleUser = this.createButton(compositeButton, Messages.getString("RoleSettingDialog.not_role_user_button"));
		WidgetTestUtil.setTestId(this, "unassign", buttonNotRoleUser);
		this.buttonNotRoleUser.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e) {
				String[] items = listRoleUser.getSelection();
				for (String item : items) {
					listRoleUser.remove(item);
					listNotRoleUser.add(item);

					// 表示名をキーとしたユーザIDのHashMapを内容の遷移に合わせて移動
					mapNotRoleUser.put(item, mapRoleUser.get(item));
					mapRoleUser.remove(item);
				}
			}
		});

		// 空
		label = new Label(compositeButton, SWT.NONE);
		WidgetTestUtil.setTestId(this, "blank", label);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * 所属ユーザ用一覧用コンポジット
		 */
		Composite compositeRole = new Composite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "role", compositeRole);
		layout = new GridLayout(1, true);
		layout.numColumns = 1;
		compositeRole.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 6;
		gridData.verticalSpan = 2;
		compositeRole.setLayoutData(gridData);

		// 所属ユーザ一覧 ラベル
		label = new Label(compositeRole, SWT.NONE);
		WidgetTestUtil.setTestId(this, "userlist", label);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("RoleSettingDialog.role_user"));

		// 所属ユーザ一覧 リスト
		this.listRoleUser = new List(compositeRole, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		WidgetTestUtil.setTestId(this, "roleuserlist", listRoleUser);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = this.listRoleUser.getItemHeight() * 12;
		this.listRoleUser.setLayoutData(gridData);

		// 所属ユーザ一覧 ハッシュマップ
		this.mapRoleUser = new HashMap<String, String>();

		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		shell.pack();
		shell.setSize(new Point(550, shell.getSize().y));

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

		this.setInputData(this.managerName, info);
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

		RoleInfoResponse roleInfo = this.inputData;
		if(roleInfo == null){
			return result;
		}

		try {
			AccessRestClientWrapper wrapper = AccessRestClientWrapper.getWrapper(managerName);
			AssignUserWithRoleRequest userList = new AssignUserWithRoleRequest();
			for( UserInfoResponse rec : roleInfo.getUserInfoList()){
				userList.addUserIdListItem(rec.getUserId());
			}
			wrapper.assignUserWithRole(roleInfo.getRoleId(),userList);
			result = true;

			Object[] arg = {managerName};
			// 完了メッセージ
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.accesscontrol.34", arg));

		} catch (Exception e) {
			String errMessage = "";
			if (e instanceof InvalidRole) {
				// 権限なし
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} else if (e instanceof UnEditableRole) {
				// ユーザの割り当て不可のロールの場合はエラー（ALL_USERS）
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.43"));
			} else {
				errMessage = ", " + e.getMessage();
			}
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.accesscontrol.35") + errMessage);

		}

		return result;
	}

	/**
	 * ロール用プロパティを設定します。
	 *
	 * @param roleInfo 設定値として用いるロール情報
	 */
	protected void setInputData(String managerName, RoleInfoResponse roleInfo) {

		this.inputData = roleInfo;

		// 各項目に反映

		java.util.List<UserInfoResponse> allUserList = null;
		// 全ユーザを取得
		try {
			AccessRestClientWrapper wrapper = AccessRestClientWrapper.getWrapper(managerName);
			allUserList = wrapper.getUserInfoList();
			//昇順ソート
			Collections.sort(allUserList, new Comparator<UserInfoResponse>(){
				@Override
				public int compare(UserInfoResponse o1, UserInfoResponse o2) {
					return o1.getUserId().compareTo(o2.getUserId());
				}
			});
		} catch (InvalidRole e) {
			// 権限なし
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));

		} catch (Exception e) {
			// 上記以外の例外
			m_log.warn("getOwnUserList(), " +e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + e.getMessage());
		}
		
		if (allUserList == null)
			throw new InternalError("allUserList is null");

		// リストを振り分ける
		String listName = null;
		java.util.List<String> roleAssignUserList = new java.util.ArrayList<String>();
		for (UserInfoResponse userInfo : roleInfo.getUserInfoList()) {
			roleAssignUserList.add(userInfo.getUserId());
		}
		for (UserInfoResponse userInfo : allUserList) {

			//リストの表示名を生成する
			listName = userInfo.getUserName() + "(" + userInfo.getUserId() + ")";
			
			if (roleAssignUserList.contains(userInfo.getUserId())) {
				this.listRoleUser.add(listName);
				this.mapRoleUser.put(listName, userInfo.getUserId());
			} else {
				this.listNotRoleUser.add(listName);
				this.mapNotRoleUser.put(listName, userInfo.getUserId());
			}
		}
	}

	/**
	 * 入力値を設定したロール情報を返します。<BR>
	 * 入力値チェックを行い、不正な場合は<code>null</code>を返します。
	 *
	 * @return ロール情報
	 */
	private RoleInfoResponse createInputData() {
		RoleInfoResponse info = this.inputData;

		// 所属ユーザ一覧からユーザ情報を取得する
		java.util.List<UserInfoResponse> roleUserList = info.getUserInfoList();
		roleUserList.clear();
		if (this.listRoleUser.getItemCount() > 0) {
			for( String userId : getUserIdList(this.mapRoleUser) ){
				// 入力値としては ユーザIDStringのListでいいのだが、持ち回り用の型の都合上、UserInfoResponseに変更
				UserInfoResponse rec= new UserInfoResponse();
				rec.setUserId(userId);
				roleUserList.add( rec );
			}
		}

		return info;
	}

	/**
	 * ＯＫボタンのテキストを返します。
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("setup");
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
		WidgetTestUtil.setTestId(this, null, button);

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		button.setLayoutData(gridData);

		button.setText(label);

		return button;
	}

	/**
	 * 表示名をキーとしたハッシュマップからユーザのリストを返す
	 *
	 * @param userMap HashMap
	 * @return ユーザIDのリスト
	 */
	private java.util.List<String> getUserIdList(java.util.HashMap<String,String> userMap) {
		java.util.List<String> resultList = new ArrayList<String>();
		for(Map.Entry<String, String> entry : userMap.entrySet()) {
			resultList.add(entry.getValue());
		}
		return resultList;
	}

}
