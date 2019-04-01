/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.dialog;

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

import com.clustercontrol.accesscontrol.composite.ObjectPrivilegeEditComposite;
import com.clustercontrol.accesscontrol.util.AccessEndpointWrapper;
import com.clustercontrol.accesscontrol.util.ObjectBean;
import com.clustercontrol.accesscontrol.util.ObjectPrivilegeBean;
import com.clustercontrol.accesscontrol.util.RoleObjectPrivilegeUtil;
import com.clustercontrol.bean.HinemosModuleMessage;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.access.InvalidRole_Exception;
import com.clustercontrol.ws.access.JobMasterNotFound_Exception;
import com.clustercontrol.ws.access.ObjectPrivilegeInfo;
import com.clustercontrol.ws.access.UsedObjectPrivilege_Exception;

/**
 * オブジェクト権限編集ダイアログクラス<BR>
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class ObjectPrivilegeEditDialog extends CommonDialog{

	// ログ
	private static Log m_log = LogFactory.getLog( ObjectPrivilegeEditDialog.class );
	/** オブジェクト(オブジェクト種別、オブジェクトID) */
	private java.util.List<ObjectBean> m_objects = null;
	private Composite m_listRoleComposite = null;
	/** ロール一覧 */
	private List m_listRole = null;
	/** ロールID コンポジット */
	private RoleIdListComposite m_roleIdComposite = null;
	/** オブジェクト権限編集コンポジット*/
	private ObjectPrivilegeEditComposite m_objectPrivEditComposite = null;
	/** 編集用オブジェクト権限マップ */
	private HashMap<String, ObjectPrivilegeBean> m_modObjPrivMap = null;

	private String m_selectRoleId = "";

	// オーナーロールID
	private String m_ownerRoleId = "";

	/** 追加ボタン。 */
	private Button m_buttonAdd = null;
	/** 削除ボタン。 */
	private Button m_buttonDel = null;


	// ----- 共通メンバ変数 ----- //
	private Shell shell = null;

	// ----- コンストラクタ ----- //
	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public ObjectPrivilegeEditDialog(Shell parent) {
		super(parent);
	}
	/**
	 * オブジェクト権限一覧ダイアログのインスタンスを返します。
	 * @param parent
	 * @param objects オブジェクトタイプ、オブジェクトIDの配列
	 * @param ownerRoleId オーナID
	 * @param objPrivMap オブジェクト権限情報
	 */
	public ObjectPrivilegeEditDialog(Shell parent, java.util.List<ObjectBean> objects, String ownerRoleId, HashMap<String, ObjectPrivilegeBean> objPrivMap) {
		super(parent);
		this.m_objects = objects;
		this.m_ownerRoleId = ownerRoleId;
		if (objPrivMap == null) {
			this.m_modObjPrivMap = new HashMap<String, ObjectPrivilegeBean>();
		} else {
			this.m_modObjPrivMap = objPrivMap;
		}
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
		shell = this.getShell();

		// オブジェクト名取得
		String objectName = "";
		if (m_objects.size() == 1) {
			objectName = HinemosModuleMessage.nameToString(this.m_objects.get(0).getObjectType());
		}

		// タイトル
		shell.setText(objectName + Messages.getString("dialog.accesscontrol.object.privilege.edit"));
		GridData gridData = new GridData();
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 15;
		parent.setLayout(layout);

		/*
		 * ロールIDリスト
		 */
		// ラベル
		Label lblRoleList = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "rolelist", lblRoleList);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblRoleList.setLayoutData(gridData);
		lblRoleList.setText(Messages.getString("role.id.list"));
		// 登録ロールリスト一覧用コンポジットの作成
		m_listRoleComposite = new Composite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "role", m_listRoleComposite);
		layout = new GridLayout(1, true);
		layout.numColumns = 1;
		layout.marginWidth = 10;
		layout.marginHeight = 0;
		m_listRoleComposite.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 15;
		gridData.verticalSpan = 2;
		m_listRoleComposite.setLayoutData(gridData);
		// コンポジットの設定
		this.m_listRole = new List(m_listRoleComposite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		WidgetTestUtil.setTestId(this, "role", m_listRole);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = this.m_listRole.getItemHeight() * 6;
		this.m_listRole.setLayoutData(gridData);
		// ロールID が選択された際の挙動
		this.m_listRole.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// 選択されたロールID のオブジェクト権限情報をオブジェクト権限編集コンポジットに渡す
				String roleId = m_listRole.getSelection()[0];
				m_objectPrivEditComposite.setObjectPrivilege(m_modObjPrivMap.get(roleId));

				// このタイミングでオブジェクト権限編集 コンポジットの操作を有効にする
				m_objectPrivEditComposite.getTable().setEnabled(true);

			}
		});

		/*
		 * ロール追加用プルダウンメニュー
		 */
		Label labelRoleId = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "roleid", labelRoleId);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelRoleId.setLayoutData(gridData);
		labelRoleId.setText(Messages.getString("role.id") + " : ");
		this.m_roleIdComposite = new RoleIdListComposite(parent, SWT.NONE,
				this.m_objects.get(0).getManagerName(), true, Mode.ROLE);
		WidgetTestUtil.setTestId(this, "roleList", m_roleIdComposite);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_roleIdComposite.setLayoutData(gridData);

		Label labelSpace = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "space", labelSpace);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelSpace.setLayoutData(gridData);
		labelSpace.setText("");

		// m_listRole の初期化
		// m_listRoleに表示するロールはロール追加用プルダウンから削除する。
		for(Map.Entry<String, ObjectPrivilegeBean> keyValue : m_modObjPrivMap.entrySet()) {
			this.m_listRole.add(keyValue.getKey());
			this.m_roleIdComposite.delete(keyValue.getKey());

		}
		// ロール追加用プルダウンからオーナーロールIDを削除する。
		if (m_ownerRoleId != null && !m_ownerRoleId.isEmpty()) {
			this.m_roleIdComposite.delete(m_ownerRoleId);
		}
		// ロール追加ボタン
		this.m_buttonAdd = new Button(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "add", m_buttonAdd);

		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_buttonAdd.setLayoutData(gridData);
		this.m_buttonAdd.setText(Messages.getString("add"));
		this.m_buttonAdd.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e) {
				String roleId = m_roleIdComposite.getText();
				if("".equals(roleId)){
					return;
				}
				// リストに追加
				m_listRole.add(roleId);

				// 編集用オブジェクト権限情報のマップにも追加
				ObjectPrivilegeBean bean = new ObjectPrivilegeBean();
				bean.setRoleId(roleId);
				bean.setReadPrivilege(true);
				m_modObjPrivMap.put(roleId, bean);

				// 追加したロールIDをリストから削除する
				m_roleIdComposite.delete(roleId);
			}
		});

		// 空白
		Label label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space", label);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// ロール削除ボタン
		this.m_buttonDel = new Button(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "del", m_buttonDel);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_buttonDel.setLayoutData(gridData);
		this.m_buttonDel.setText(Messages.getString("delete"));
		this.m_buttonDel.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				if (m_listRole.getSelection() == null || m_listRole.getSelection().length == 0) {
					return;
				}
				// ロールID をリストから削除
				String roleId = m_listRole.getSelection()[0];
				m_listRole.remove(roleId);

				// オブジェクト権限編集コンポジットをクリアし、操作不可に設定する
				m_objectPrivEditComposite.setObjectPrivilege(new ObjectPrivilegeBean());
				m_objectPrivEditComposite.getTable().setEnabled(false);

				// 編集用オブジェクト権限情報のマップからも削除
				m_modObjPrivMap.remove(roleId);

				// 削除したロールID をプルダウンメニューに追加
				m_roleIdComposite.add(roleId);

			}

		});


		// 空行
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space", label);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 *  オブジェクト権限編集
		 */
		// ラベル
		Label lblObjectPrivilegeEdit = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "objectprivilegeedit", lblObjectPrivilegeEdit);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblObjectPrivilegeEdit.setLayoutData(gridData);
		if(!this.m_selectRoleId.isEmpty())
			lblObjectPrivilegeEdit.setText(this.m_selectRoleId + " : " + Messages.getString("object.privilege.setting"));
		else
			lblObjectPrivilegeEdit.setText(Messages.getString("object.privilege.setting"));

		//オブジェクト権限編集コンポジット
		this.m_objectPrivEditComposite = new ObjectPrivilegeEditComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, m_objectPrivEditComposite);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = 150;
		this.m_objectPrivEditComposite.setLayoutData(gridData);
		this.m_objectPrivEditComposite.getTable().setEnabled(false);	// 初期値は false

		Display objPrivEditDisplay = shell.getDisplay();
		shell.setLocation((objPrivEditDisplay.getBounds().width - shell.getSize().x) / 2,
				(objPrivEditDisplay.getBounds().height - shell.getSize().y) / 2);

		// ダイアログを調整
		this.adjustDialog();
		// 必須入力項目を可視化
		//		this.update();
	}


	/**
	 * ダイアログエリアを調整します。
	 *
	 */
	private void adjustDialog(){
		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		shell.pack();
		shell.setSize(new Point(600, shell.getSize().y));

		// 画面中央に配置
		Display objPrivEditAdjustDisplay = shell.getDisplay();
		shell.setLocation((objPrivEditAdjustDisplay.getBounds().width - shell.getSize().x) / 2,
				(objPrivEditAdjustDisplay.getBounds().height - shell.getSize().y) / 2);
	}

	/**
	 * 更新処理
	 *
	 */
	public void update(){

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
		boolean result = true;

		java.util.List<ObjectPrivilegeInfo> inputdata = createInputData();
		StringBuffer confirmList = new StringBuffer();
		StringBuffer successList = new StringBuffer();
		StringBuffer failureList = new StringBuffer();
		String[] args = null;

		// 一括でオブジェクト権限を変更する場合は、確認ダイアログを表示する
		if (m_objects.size() > 1) {
			for (ObjectBean objectBean : m_objects) {
				confirmList.append(objectBean.getObjectId() + "\n");
			}

			args = new String[]{ confirmList.toString() } ;
			if (!MessageDialog.openConfirm(
					null,
					Messages.getString("confirmed"),
					Messages.getString("message.accesscontrol.53", args)))
			{
				return false;
			}
		}

		for (ObjectBean objectBean : m_objects) {
			String objectPrivilegeParam = Messages.getString("object.privilege.param",
					new String[]{ HinemosModuleMessage.nameToString(objectBean.getObjectType()), objectBean.getObjectId()});
			String managerName = objectBean.getManagerName();
			AccessEndpointWrapper wrapper = AccessEndpointWrapper.getWrapper(managerName);
			try {
				wrapper.replaceObjectPrivilegeInfo(
						objectBean.getObjectType(),
						objectBean.getObjectId(),
						inputdata);
				successList.append(objectPrivilegeParam + "(" + managerName + ")\n");
			} catch (UsedObjectPrivilege_Exception e) {
				args = new String[]{
						HinemosModuleMessage.nameToString(e.getFaultInfo().getObjectType()),
						e.getFaultInfo().getObjectId()
				};
				// 他機能で参照しているため、削除できない。
				failureList.append(objectPrivilegeParam + " (" + Messages.getString("message.accesscontrol.36", args) + ")\n");
			} catch (InvalidRole_Exception e) {
				// 権限なし
				failureList.append(objectPrivilegeParam + " (" + Messages.getString("message.accesscontrol.16") + ")\n");
			} catch (JobMasterNotFound_Exception e) {
				// ジョブが登録前の場合
				failureList.append(objectPrivilegeParam + " (" + Messages.getString("message.accesscontrol.46") + ")\n");
			} catch (Exception e) {
				// 上記以外の例外
				m_log.warn("getOwnUserList(), " + HinemosMessage.replace(e.getMessage()), e);
				failureList.append(objectPrivilegeParam + " (" + Messages.getString("message.hinemos.failure.unexpected") 
						+ ", " + HinemosMessage.replace(e.getMessage()) + ")\n");
			}
		}

		// 成功ダイアログ
		if(successList.length() != 0){
			args = new String[]{ successList.toString() } ;
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.accesscontrol.49", args));
		}

		// 失敗ダイアログ
		if(failureList.length() != 0){
			args = new String[]{ failureList.toString() } ;
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.accesscontrol.50", args));
		}

		return result;
	}

	private java.util.List<ObjectPrivilegeInfo> createInputData() {

		java.util.List<ObjectPrivilegeInfo> list = null;
		list = RoleObjectPrivilegeUtil.beanMap2dtoList(m_modObjPrivMap);

		return list;
	}
}
