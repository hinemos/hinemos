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

package com.clustercontrol.notify.composite;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.notify.dialog.NotifyListDialog;
import com.clustercontrol.notify.util.NotifyTypeUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.notify.NotifyRelationInfo;
import com.clustercontrol.util.WidgetTestUtil;


/**
 * 通知ID一覧コンポジットクラス<BR>
 * <p>
 * <dl>
 *  <dt>コンポジット</dt>
 *  <dd>「通知ID」 ラベル</dd>
 *  <dd>「通知ID一覧」 フィールド</dd>
 *  <dd>「選択」 ボタン</dd>
 * </dl>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class NotifyIdListComposite extends Composite {

	private static Log m_log = LogFactory.getLog( NotifyIdListComposite.class ); 

	/** 通知ID ラベル文字列。 */
	private String m_text = null;

	/** 通知ID ラベル。 */
	private Label m_labelNotifyId = null;

	private Table m_NotifyIdTable = null;

	/**選　択 ボタン。 */
	private Button m_buttonRefer = null;

	/**　通知ID一覧　フィールド*/
	private List<NotifyRelationInfo> notify ;

	private int notifyIdType = 0;

	private String m_ownerRoleId = null;

	private String m_managerName = null;

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param labelFlg 通知IDラベル表示フラグ（表示する場合、<code> true </code>）。
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize(Composite, boolean)
	 */
	public NotifyIdListComposite(Composite parent, int style, boolean labelFlg) {
		super(parent, style);
		m_text = Messages.getString("notify.id"); //$NON-NLS-1$

		this.initialize(parent, labelFlg);
	}

	public NotifyIdListComposite(Composite parent, int style, boolean labelFlg, int notifyIdType) {
		super(parent, style);
		m_text = Messages.getString("notify.id"); //$NON-NLS-1$
		this.notifyIdType = notifyIdType;

		this.initialize(parent, labelFlg);
	}

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 * 通知IDラベルの文字列を指定します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param text 通知ID ラベル文字列
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize(Composite, boolean)
	 */
	public NotifyIdListComposite(Composite parent, int style, String text) {
		super(parent, style);
		m_text = text;

		this.initialize(parent, true);
	}


	/**
	 * コンポジットを配置します。
	 *
	 * @see #update()
	 */
	private void initialize(Composite parent, boolean labelFlg) {

		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		if(labelFlg){
			layout.numColumns = 15;
		}
		else{
			layout.numColumns = 10;
		}

		this.setLayout(layout);

		/*
		 * 通知ID
		 */
		if(labelFlg){
			// ラベル
			this.m_labelNotifyId = new Label(this, SWT.NONE);
			WidgetTestUtil.setTestId(this, "notifyid", m_labelNotifyId);
			gridData = new GridData();
			gridData.horizontalSpan = 4;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			this.m_labelNotifyId.setLayoutData(gridData);
			this.m_labelNotifyId.setText(m_text + " : "); //$NON-NLS-1$
		}

		//通知設定一覧
		this.m_NotifyIdTable = new Table(this, SWT.BORDER
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, "table", m_NotifyIdTable);
		m_NotifyIdTable.setHeaderVisible(true);
		m_NotifyIdTable.setLinesVisible(true);
		m_NotifyIdTable.setLayoutData(new RowData(200, 100));


		TableColumn col = new TableColumn(m_NotifyIdTable,SWT.LEFT);
		WidgetTestUtil.setTestId(this, "column1", col);
		col.setText(Messages.getString("NotifyIdListComposite.notifyId")); //$NON-NLS-1$
		col.setWidth(130);
		TableColumn col2 = new TableColumn(m_NotifyIdTable,SWT.LEFT);
		WidgetTestUtil.setTestId(this, "column2", col2);
		col2.setText(Messages.getString("NotifyIdListComposite.notifyType")); //$NON-NLS-1$
		col2.setWidth(100);

		gridData = new GridData();
		gridData.horizontalSpan = 8 ;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.heightHint = 100; //通知IDのテーブルの高さ
		this.m_NotifyIdTable.setLayoutData(gridData);


		// 参照ボタン
		this.m_buttonRefer = new Button(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "refer", m_buttonRefer);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_buttonRefer.setLayoutData(gridData);
		this.m_buttonRefer.setText(Messages.getString("select")); //$NON-NLS-1$
		this.m_buttonRefer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

				// ダイアログ表示及び終了処理
				NotifyListDialog dialog = new NotifyListDialog(shell, m_managerName, true, notifyIdType, m_ownerRoleId);
				if (notify != null) {
					dialog.setSelectNotify(notify);
				}
				dialog.open();

				// ダイアログからデータを取得して通知IDを設定する
				setNotify(dialog.getSelectNotify());

				// コンポジットを更新する
				update();
			}
		});
	}

	/**
	 * コンポジットを更新します。<BR>
	 * 通知ID一覧情報を取得し、通知ID一覧コンポジットにセットします。
	 *
	 * @see com.clustercontrol.notify.action.GetNotify#getNotifyIdList()
	 */
	@Override
	public void update() {
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.m_buttonRefer.setEnabled(enabled);
		this.m_NotifyIdTable.setEnabled(enabled);
	}

	public void setButtonEnabled(boolean enabled) {
		this.m_buttonRefer.setEnabled(enabled);
	}

	/**
	 * 通知IDを返します。
	 *
	 * @return 通知ID
	 *
	 * @see org.eclipse.swt.widgets.Combo#getText()
	 */
	public List<NotifyRelationInfo> getNotify() {
		return notify;
	}

	/**
	 * 通知IDを設定します。
	 *
	 * @param notifyList 通知ID
	 *
	 * @see org.eclipse.swt.widgets.Combo#setText(java.lang.String)
	 */
	public void setNotify(List<NotifyRelationInfo> notifyList) {

		//フィールドに追加
		this.notify = notifyList;

		m_NotifyIdTable.removeAll();

		// 表示に追加
		m_log.info("notifyList.size=" + notifyList.size());
		for (NotifyRelationInfo notify : notifyList) {
			m_log.info("notify=" + notify.getNotifyGroupId() + ", " + notify.getNotifyId() + ", " + notify.getNotifyType());
			TableItem ti = new TableItem(m_NotifyIdTable, 0);
			WidgetTestUtil.setTestId(this, null, ti);
			String notifyType = NotifyTypeUtil.typeToString(notify.getNotifyType());

			String[] repData = { notify.getNotifyId(), notifyType };

			ti.setText(repData);
		}
		m_NotifyIdTable.update();
	}

	/**
	 * 通知グループIDを設定します。
	 *
	 * @param string
	 */
	public boolean setNotifyGroupId(String string){

		if(notify != null && notify.size() != 0) {
			NotifyRelationInfo nri ;

			for(int i = 0; i < notify.size(); i++){
				nri =((ArrayList<NotifyRelationInfo>)notify).get(i);
				nri.setNotifyGroupId(string);
			}
			return true;
		}

		return false;
	}

	public String getOwnerRoleId() {
		return m_ownerRoleId;
	}

	/**
	 * resetNotifyListはジョブから呼ばれたときのみfalseとなる。
	 * @param ownerRoleId
	 * @param resetNotifyList
	 */
	public void setOwnerRoleId(String ownerRoleId, boolean resetNotifyList) {
		this.m_ownerRoleId = ownerRoleId;
		if (resetNotifyList) {
			m_log.debug("setOwnerRoleId : removeAll notifyIdTable");
			m_NotifyIdTable.removeAll();
			this.notify = null;
		}
	}

	/**
	 * @return the managerName
	 */
	public String getManagerName() {
		return m_managerName;
	}

	/**
	 * @param managerName the managerName to set
	 */
	public void setManagerName(String managerName) {
		this.m_managerName = managerName;
		m_log.debug("setManagerName : removeAll notifyIdTable");
		m_NotifyIdTable.removeAll(); 
		this.notify = null; 
	}
}
