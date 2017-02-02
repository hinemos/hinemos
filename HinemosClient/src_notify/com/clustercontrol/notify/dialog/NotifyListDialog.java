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

package com.clustercontrol.notify.dialog;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.notify.action.DeleteNotify;
import com.clustercontrol.notify.action.GetNotify;
import com.clustercontrol.notify.action.GetNotifyTableDefineCheckBox;
import com.clustercontrol.notify.action.ModifyNotify;
import com.clustercontrol.notify.composite.NotifyListComposite;
import com.clustercontrol.notify.view.action.NotifyModifyAction;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.notify.NotifyInfo;
import com.clustercontrol.ws.notify.NotifyRelationInfo;

/**
 * 通知[一覧]ダイアログクラス<BR>
 *
 * @version 3.0.0
 * @since 2.0.0
 */
public class NotifyListDialog extends CommonDialog {

	// 後でpackするためsizeXはダミーの値。
	private static final int sizeX = 800;
	private static final int sizeY = 450;

	/** 通知一覧 コンポジット。 */
	private NotifyListComposite notifyListComposite = null;

	/** 追加 ボタン。 */
	private Button buttonAdd = null;

	/** 変更 ボタン。 */
	private Button buttonModify = null;

	/** 削除 ボタン。 */
	private Button buttonDelete = null;

	/** 有効ボタン */
	private Button buttonValid = null;

	/** 無効ボタン */
	private Button buttonInvalid = null;

	/*通知を選択するかのフラグ*/
	boolean isSelect = false;

	String ownerRoleId = null;

	private String managerName = null;

	/***/
	List<NotifyRelationInfo> notify;

	/**
	 * ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public NotifyListDialog(Shell parent) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
	}

	public NotifyListDialog(Shell parent, String managerName, boolean isSelect, int notifyIdType, String ownerRoleId) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);

		this.managerName = managerName;
		/**監視などから呼ばれる場合にはtrue*/
		this.isSelect = isSelect;
		this.ownerRoleId = ownerRoleId;
	}

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(sizeX, sizeY);
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 *
	 * @see com.clustercontrol.notify.composite.NotifyListComposite
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.notify.list"));

		// レイアウト
		GridLayout layout = new GridLayout(8, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 8;
		parent.setLayout(layout);

		/*
		 * ログ一覧
		 */
		this.notifyListComposite = new NotifyListComposite(parent, SWT.BORDER, isSelect, this.ownerRoleId);
		this.notifyListComposite.setManagerName(this.managerName);
		WidgetTestUtil.setTestId(this, "notifylist", notifyListComposite);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 7;
		gridData.heightHint = SWT.MIN;
		this.notifyListComposite.setLayoutData(gridData);
		this.notifyListComposite.setSelectNotify(this.notify);
		this.notifyListComposite.update();
		// アクセス権限がなく、通知一覧が取得できなかった場合は、本ダイアログを閉じる
		if(!this.notifyListComposite.isShowFlg()) {
			this.close();
			return ;
		}


		/*
		 * 操作ボタン
		 */

		Composite composite = new Composite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "button", composite);
		layout = new GridLayout(1, true);
		layout.numColumns = 1;
		composite.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		composite.setLayoutData(gridData);

		// 追加ボタン
		this.buttonAdd = this.createButton(composite, Messages.getString("add"));
		WidgetTestUtil.setTestId(this, "add", buttonAdd);
		this.buttonAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				NotifyTypeDialog dialog = new NotifyTypeDialog(getParentShell(), notifyListComposite, managerName);
				dialog.open();
			}
		});

		// 変更ボタン
		Label dummy = new Label(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "dummy", dummy);
		this.buttonModify = this.createButton(composite, Messages.getString("modify"));
		WidgetTestUtil.setTestId(this, "modify", buttonModify);
		this.buttonModify.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				String notifyId = null;
				Integer notifyType = null;

				Table notifyListModifyTable = notifyListComposite.getTableViewer().getTable();
				WidgetTestUtil.setTestId(this, "modify", notifyListModifyTable);
				TableItem[] item = notifyListModifyTable.getSelection();

				if(item != null && item.length>0){
					try{
						notifyId = (String)((ArrayList<?>)item[0].getData()).get(GetNotifyTableDefineCheckBox.NOTIFY_ID);
						notifyType = (Integer)((ArrayList<?>)item[0].getData()).get(GetNotifyTableDefineCheckBox.NOTIFY_TYPE);
					}
					catch(Exception ex){
						Logger.getLogger(this.getClass()).debug(ex.getMessage(), ex);
					}
				}

				if (notifyId != null && notifyType != null) {
					NotifyModifyAction action = new NotifyModifyAction();
					if (action.openDialog(getParentShell(), NotifyListDialog.this.managerName, notifyId, notifyType) ==
							IDialogConstants.OK_ID) {
						int selectIndex = notifyListModifyTable.getSelectionIndex();
						notifyListComposite.update();
						notifyListModifyTable.setSelection(selectIndex);
					}
				} else{
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.notify.8"));
				}
			}
		});

		// 削除ボタン
		dummy = new Label(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "dummydelete", dummy);
		this.buttonDelete = this.createButton(composite, Messages
				.getString("delete"));
		WidgetTestUtil.setTestId(this, "delete", buttonDelete);
		this.buttonDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				String notifyId = null;

				Table notifyListDeleteTable = notifyListComposite.getTableViewer().getTable();
				WidgetTestUtil.setTestId(this, "delete", notifyListDeleteTable);
				TableItem[] item = notifyListDeleteTable.getSelection();
				List<String> notifyIdList = new ArrayList<String>();

				if(item == null || item.length == 0){
					return;
				}

				for(int i=0; i<item.length; i++) {
					WidgetTestUtil.setTestId(this, "item" + i, item[i]);
					notifyId = (String)((ArrayList<?>)item[i].getData()).get(GetNotifyTableDefineCheckBox.NOTIFY_ID);
					notifyIdList.add(notifyId);
				}

				String[] args = new String[1];
				String msg = null;
				if (notifyIdList.isEmpty() == false) {
					if (notifyIdList.size() == 1) {
						args[0] = notifyIdList.get(0);
						msg = "message.notify.7";
					} else {
						args[0] = Integer.toString(notifyIdList.size());
						msg = "message.notify.51";
					}

					DeleteNotify deleteNotify = new DeleteNotify();

					if(deleteNotify.useCheck(managerName, notifyIdList) == Window.OK){ // 対象の通知IDがどの監視で使用されているかを確認
						if (MessageDialog.openConfirm(
								null,
								Messages.getString("confirmed"),
								Messages.getString(msg, args))) {

							boolean result = deleteNotify.delete(managerName, notifyIdList);
							if(result){
								notifyListComposite.update();
							}
							//            			else{
							//                        	MessageDialog.openError(
							//                        			null,
							//                        			Messages.getString("failed"),
							//                        			Messages.getString("message.notify.6", args));
							//            			}
						}
					}
				}
				else{
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.notify.9"));
				}
			}
		});

		// 有効ボタン
		dummy = new Label(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "dummy1", dummy);
		dummy = new Label(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "dummy2", dummy);
		this.buttonValid = this.createButton(composite, Messages
				.getString("valid"));
		WidgetTestUtil.setTestId(this, "valid", buttonValid);
		this.buttonValid.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//一括で有効に変更
				setValid(true);
			}
		});

		// 無効ボタン
		this.buttonInvalid = this.createButton(composite, Messages
				.getString("invalid"));
		WidgetTestUtil.setTestId(this, "invalid", buttonInvalid);
		this.buttonInvalid.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//一括で無効に変更
				setValid(false);
			}
		});

		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		WidgetTestUtil.setTestId(this, "line", line);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 8;
		line.setLayoutData(gridData);

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

		//ダイアログのサイズ調整（pack:resize to be its preferred size）
		shell.pack();
		shell.setSize(new Point(sizeX, sizeY ));

	}

	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 */
	@Override
	protected ValidateResult validate() {
		return super.validate();
	}

	/**
	 * ボタンを返します。
	 *
	 * @param parent 親のコンポジット
	 * @param label ラベル文字列
	 * @return 生成されたボタン
	 */
	private Button createButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, button);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		button.setLayoutData(gridData);

		button.setText(label);

		return button;
	}

	/**
	 * ボタンを生成します。<BR>
	 * 閉じるボタンを生成します。
	 *
	 * @param parent ボタンバーコンポジット
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// TODO Remove the following hard-codes. IDialogConstants.*_LABEL will cause IncompatibleClassChangeError in RAP.
		if(!isSelect){
			// 閉じるボタン
			this.createButton(parent, IDialogConstants.CANCEL_ID, "Close", false);
		}else{
			// 閉じるボタン
			this.createButton(parent, IDialogConstants.CANCEL_ID, "OK", false);
		}
	}

	/**
	 * 監視にもともと設定されていた通知を反映します。
	 *
	 * @param notify
	 */
	public void setSelectNotify(List<NotifyRelationInfo> notify){

		this.notify = notify;

	}

	/**
	 *
	 */
	public List<NotifyRelationInfo> getSelectNotify(){
		return this.notifyListComposite.getSelectNotify();
	}

	/**
	 * 閉じる、キャンセルボタンが押された場合に呼ばれるメソッドで、
	 * 監視から呼ばれた場合にのみ動作させます。
	 * <p>
	 *
	 * エラーの場合、ダイアログを閉じずにエラー内容を通知します。
	 */
	@Override
	protected void cancelPressed() {
		//監視から呼ばれた場合のみ動作
		if(isSelect){

			if(!notifyListComposite.makeNotifyData()){
				MessageDialog.openWarning(
						null,
						Messages.getString("warning"),
						Messages.getString("message.notify.25"));
				return;
			}

		}

		//上位のcancelPressで
		super.cancelPressed();
	}

	/**
	 * 有効・無効変更処理
	 *
	 * @param valid
	 */
	public void setValid(boolean valid) {

		//選択された通知IDを取得
		ArrayList<String> list = notifyListComposite.getSelectionData();
		if (list != null && list.size() > 0) {

			StringBuilder notifyIds = new StringBuilder();
			for(int i = 0; i < list.size(); i++){
				if(i > 0){
					notifyIds.append(", ");
				}
				notifyIds.append(list.get(i));
			}

			String[] confirmArgs = { notifyIds.toString() };
			String message;
			if(valid)
				message = Messages.getString("message.notify.34",confirmArgs);
			else
				message = Messages.getString("message.notify.37",confirmArgs);
			if (!MessageDialog.openConfirm(
					null,
					Messages.getString("confirmed"),
					message)) {
				return;
			}

			for(int i = 0; i < list.size(); i++){
				String notifyId = list.get(i);

				if(notifyId != null && !notifyId.equals("")){
					//通知情報を取得
					NotifyInfo info = new GetNotify().getNotify(this.managerName, notifyId);

					//有効・無効を設定
					info.setValidFlg(valid);

					//通知情報を更新
					new ModifyNotify().modify(this.managerName, info);
				}
			}

			int selectIndex = notifyListComposite.getTableViewer().getTable().getSelectionIndex();
			notifyListComposite.update();
			notifyListComposite.getTableViewer().getTable().setSelection(selectIndex);
		}
		else{
			MessageDialog.openWarning(
					null,
					Messages.getString("warning"),
					Messages.getString("message.notify.10"));
		}
	}

}
