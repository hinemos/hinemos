/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.accesscontrol.composite.ObjectPrivilegeListComposite;
import com.clustercontrol.accesscontrol.util.ObjectBean;
import com.clustercontrol.accesscontrol.util.ObjectPrivilegeBean;
import com.clustercontrol.accesscontrol.util.RoleObjectPrivilegeUtil;
import com.clustercontrol.bean.HinemosModuleMessage;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * オブジェクト権限設定一覧ダイアログクラス<BR>
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class ObjectPrivilegeListDialog extends CommonDialog{

	/** マネージャ名 */
	private String m_managerName = "";
	/** オブジェクトID */
	private String m_objectId = "";
	/** オブジェクトタイプ */
	private String m_objectType = "";
	/** ロールID */
	private String m_ownerRoleId = "";
	/** オブジェクト一覧表示用コンポジット */
	private ObjectPrivilegeListComposite m_objPrivListComposite = null;
	/** 操作用のオブジェクト権限 HashMap */
	private HashMap<String, ObjectPrivilegeBean> m_objPrivMap = null;

	// ----- 共通メンバ変数 ----- //
	private Shell shell = null;

	// ----- コンストラクタ ----- //
	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public ObjectPrivilegeListDialog(Shell parent) {
		super(parent);
	}
	/**
	 * オブジェクト権限一覧ダイアログのインスタンスを返します。
	 * @param parent
	 * @param objectId オブジェクトID
	 * @param objectType オブジェクトタイプ
	 * @param ownerId オーナID
	 * @param mode モード
	 */
	public ObjectPrivilegeListDialog(Shell parent, String managerName, String objectId, String objectType, String m_ownerRoleId) {
		super(parent);
		this.m_managerName = managerName;
		this.m_objectId = objectId;
		this.m_objectType = objectType;
		this.m_ownerRoleId = m_ownerRoleId;
		this.m_objPrivMap = RoleObjectPrivilegeUtil.dto2beanMap(managerName, m_objectId, m_objectType);
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
		String objectName = HinemosModuleMessage.nameToString(this.m_objectType);

		// タイトル
		shell.setText(objectName + Messages.getString("dialog.accesscontrol.object.privilege.list"));
		GridData gridData = new GridData();
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);

		/*
		 * オブジェクトID
		 */
		Label lblObjID = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "objid", lblObjID);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblObjID.setLayoutData(gridData);
		lblObjID.setText(objectName + Messages.getString("id") + " : " + this.m_objectId);

		/*
		 * オーナID
		 */
		Label lblOwnerId = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "ownerid", lblOwnerId);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblOwnerId.setLayoutData(gridData);
		lblOwnerId.setText(Messages.getString("owner.role.id") + " : " + this.m_ownerRoleId);

		/*
		 *  オブジェクト権限情報リスト
		 */

		//オブジェクト権限情報テーブルカラム取得
		this.m_objPrivListComposite = new ObjectPrivilegeListComposite(parent, SWT.NONE, this.m_objPrivMap);
		WidgetTestUtil.setTestId(this, null, m_objPrivListComposite);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = 220;
		m_objPrivListComposite.setLayoutData(gridData);

		Display objPrivListDisplay = shell.getDisplay();
		shell.setLocation((objPrivListDisplay.getBounds().width - shell.getSize().x) / 2,
				(objPrivListDisplay.getBounds().height - shell.getSize().y) / 2);

		// ダイアログを調整
		this.adjustDialog();
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
		Display objPrivListAdjustDisplay = shell.getDisplay();
		shell.setLocation((objPrivListAdjustDisplay.getBounds().width - shell.getSize().x) / 2,
				(objPrivListAdjustDisplay.getBounds().height - shell.getSize().y) / 2);
	}

	/**
	 * 更新処理
	 *   DB から情報を再取得する
	 */
	public void update(){

		// オブジェクト権限一覧情報をDBより再取得し、再描画する
		this.m_objPrivMap = RoleObjectPrivilegeUtil.dto2beanMap(this.m_managerName, m_objectId, m_objectType);
		this.m_objPrivListComposite.objectPrivilegeRefresh(this.m_objPrivMap);
	}


	/**
	 * 既存のボタンに加え、編集ボタンを追加します。
	 *
	 * @param parent
	 *            ボタンバーコンポジット
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// 編集ボタン
		this.createButton(parent, IDialogConstants.OPEN_ID, Messages
				.getString("edit"), false);
		this.getButton(IDialogConstants.OPEN_ID).addSelectionListener(
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						List<ObjectBean> objectBeans = new ArrayList<ObjectBean>();
						objectBeans.add(new ObjectBean(m_managerName, m_objectType, m_objectId));
						ObjectPrivilegeEditDialog dialog =
								new ObjectPrivilegeEditDialog(getParentShell(),
										objectBeans, m_ownerRoleId, m_objPrivMap);
						dialog.open();

						// 編集した内容でアップデート
						update();
					}
				});

		// 閉じるボタン
		this.createButton( parent, IDialogConstants.CANCEL_ID, Messages.getString("close"), false );
	}

}
