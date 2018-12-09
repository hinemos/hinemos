/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.composite;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.calendar.dialog.CalendarDetailDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.calendar.CalendarDetailInfo;

/**
 * カレンダ詳細情報グループのコンポジットクラス<BR>
 * <p>
 * <dl>
 *  <dt>コンポジット</dt>
 *  <dd>値取得の成功時</dd>
 *  <dd>　カレンダ詳細情報一覧コンポジット</dd>
 *  <dd>　「追加」ボタン</dd>
 *  <dd>　「変更」ボタン</dd>
 *  <dd>　「削除」ボタン</dd>
 *  <dd>　「コピー」ボタン</dd>
 *  <dd>　「上へ」ボタン</dd>
 *  <dd>　「下へ」ボタン</dd>
 *  <dd>値取得の失敗時</dd>
 *  <dd>　「重要度」 コンボボックス</dd>
 * </dl>
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class CalendarDetailInfoComposite extends Composite {

	/** カレンダ詳細情報一覧 コンポジット。 */
	private CalendarDetailListComposite calInfoListComposite = null;

	/** 追加 ボタン。 */
	private Button calDetailInfoAddButton = null;

	/** 変更 ボタン。 */
	private Button calDetailInfoModifyButton = null;

	/** 削除 ボタン。 */
	private Button calDetailInfoDeleteButton = null;

	/** コピー ボタン。 */
	private Button calDetailInfoCopyButton = null;

	/** 上へ ボタン。 */
	private Button calDetailInfoUpButton = null;

	/** 下へ ボタン。 */
	private Button calDetailInfoDownButton = null;

	/** マネージャ名 */
	private String managerName = null;

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param managerName マネージャ名
	 * @param tableDefine カレンダ詳細情報一覧のテーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see com.clustercontrol.bean.TableColumnInfo#TableColumnInfo(java.lang.String, int, int, int)
	 * @see com.clustercontrol.monitor.run.action.GetStringFilterTableDefine
	 * @see #initialize(ArrayList)
	 */
	public CalendarDetailInfoComposite(Composite parent, int style, String managerName){
		super(parent, style);
		this.managerName = managerName;
		this.initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize(){

		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 15;
		this.setLayout(layout);

		/*
		 * カレンダ詳細情報一覧
		 */
		this.calInfoListComposite = new CalendarDetailListComposite(this, SWT.BORDER, this.managerName);
		WidgetTestUtil.setTestId(this, "list", calInfoListComposite);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 13;
		this.calInfoListComposite.setLayoutData(gridData);
		/*
		 * 操作ボタン
		 */
		Composite calDetailInfoButtonComposite = new Composite(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "button", calDetailInfoButtonComposite);
		layout = new GridLayout(1, true);
		layout.numColumns = 1;
		calDetailInfoButtonComposite.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 2;
		calDetailInfoButtonComposite.setLayoutData(gridData);

		// 追加ボタン
		this.calDetailInfoAddButton = this.createButton(calDetailInfoButtonComposite, Messages.getString("add"));
		WidgetTestUtil.setTestId(this, "add", calDetailInfoAddButton);
		this.calDetailInfoAddButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				//FIXME
				//CalendarDetailDialog dialog = new CalendarDetailDialog(shell, m_ownerRoleId);
				CalendarDetailDialog dialog = new CalendarDetailDialog(shell, calInfoListComposite.getManagerName(), calInfoListComposite.getOwnerRoleId());
				if (dialog.open() == IDialogConstants.OK_ID) {
					calInfoListComposite.getDetailList().add(dialog.getInputData());
					calInfoListComposite.update();
				}
			}
		});

		// 変更ボタン
		this.calDetailInfoModifyButton = this.createButton(calDetailInfoButtonComposite, Messages.getString("modify"));
		WidgetTestUtil.setTestId(this, "modify", calDetailInfoModifyButton);
		this.calDetailInfoModifyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Integer order = calInfoListComposite.getSelection();
				if (order != null) {
					// シェルを取得
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					//FIXME
					//CalendarDetailDialog dialog = new CalendarDetailDialog(shell,m_infoListComposite.getDetailList().get(order - 1), m_ownerRoleId);
					CalendarDetailDialog dialog = new CalendarDetailDialog(shell, calInfoListComposite.getManagerName(), calInfoListComposite.getDetailList().get(order - 1), calInfoListComposite.getOwnerRoleId());
					if (dialog.open() == IDialogConstants.OK_ID) {
						calInfoListComposite.getDetailList().remove(calInfoListComposite.getDetailList().get(order - 1));
						calInfoListComposite.getDetailList().add(order - 1,dialog.getInputData());
						calInfoListComposite.setSelection();
					}
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});

		// 削除ボタン
		this.calDetailInfoDeleteButton = this.createButton(calDetailInfoButtonComposite, Messages.getString("delete"));
		WidgetTestUtil.setTestId(this, "delete", calDetailInfoDeleteButton);
		this.calDetailInfoDeleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Integer order = calInfoListComposite.getSelection();
				if (order != null) {
					calInfoListComposite.getDetailList().remove(order - 1);
					calInfoListComposite.update();
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});

		// コピーボタン
		this.calDetailInfoCopyButton = this.createButton(calDetailInfoButtonComposite, Messages.getString("copy"));
		WidgetTestUtil.setTestId(this, "copy", calDetailInfoCopyButton);
		this.calDetailInfoCopyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Integer order = calInfoListComposite.getSelection();
				if (order != null) {
					// シェルを取得
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					CalendarDetailDialog dialog = new CalendarDetailDialog(shell, calInfoListComposite.getManagerName(), calInfoListComposite.getDetailList().get(order - 1), calInfoListComposite.getOwnerRoleId());
					if (dialog.open() == IDialogConstants.OK_ID) {
						calInfoListComposite.getDetailList().add(dialog.getInputData());
						calInfoListComposite.setSelection();
					}
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});

		// 上へボタン
		this.calDetailInfoUpButton = this.createButton(calDetailInfoButtonComposite, Messages.getString("up"));
		WidgetTestUtil.setTestId(this, "up", calDetailInfoUpButton);
		this.calDetailInfoUpButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Integer order = calInfoListComposite.getSelection();
				if (order != null) {
					calInfoListComposite.up();
					calInfoListComposite.update();
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});

		// 下へボタン
		this.calDetailInfoDownButton = this.createButton(calDetailInfoButtonComposite, Messages.getString("down"));
		WidgetTestUtil.setTestId(this, "down", calDetailInfoDownButton);
		this.calDetailInfoDownButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Integer order = calInfoListComposite.getSelection();
				if (order != null) {
					calInfoListComposite.down();
					calInfoListComposite.update();
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});
	}

	/**
	 *
	 * @return
	 */
	public ArrayList<CalendarDetailInfo> getDetailList(){
		return this.calInfoListComposite.getDetailList();
	}
	/**
	 * カレンダ詳細情報をコンポジット内リストに反映させる
	 * @param detailList
	 */
	public void setDetailList(ArrayList<CalendarDetailInfo> detailList){
		if (detailList != null) {
			this.calInfoListComposite.setDetailList(detailList);
		}
		this.update();
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.calInfoListComposite.setEnabled(enabled);
		this.calDetailInfoAddButton.setEnabled(enabled);
		this.calDetailInfoModifyButton.setEnabled(enabled);
		this.calDetailInfoDeleteButton.setEnabled(enabled);
		this.calDetailInfoUpButton.setEnabled(enabled);
		this.calDetailInfoDownButton.setEnabled(enabled);
	}

	/**
	 * ボタンを返します。
	 *
	 * @param parent 親のコンポジット
	 * @param label ボタンに表示するテキスト
	 * @return ボタン
	 */
	private Button createButton(Composite parent, String label) {
		Button calDetailInfoCommonButton = new Button(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, calDetailInfoCommonButton);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calDetailInfoCommonButton.setLayoutData(gridData);

		calDetailInfoCommonButton.setText(label);

		return calDetailInfoCommonButton;
	}

	/**
	 * 無効な入力値の情報を設定します。
	 *
	 * @param id ID
	 * @param message メッセージ
	 * @return 認証結果
	 */
	protected ValidateResult setValidateResult(String id, String message) {

		ValidateResult validateResult = new ValidateResult();
		validateResult.setValid(false);
		validateResult.setID(id);
		validateResult.setMessage(message);

		return validateResult;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.calInfoListComposite.setOwnerRoleId(ownerRoleId);
		//this.m_ownerRoleId = ownerRoleId;
	}

	public void changeOwnerRoleId(String ownerRoleId) {

		//if (ownerRoleId == null
		//		|| !ownerRoleId.equals(this.m_ownerRoleId)) {
		if (ownerRoleId == null
				|| !ownerRoleId.equals(this.calInfoListComposite.getOwnerRoleId())) {

			//Iterator<CalendarDetailInfo> iter = m_info.getCalendarDetailList().iterator();
			Iterator<CalendarDetailInfo> iter = calInfoListComposite.getDetailList().iterator();
			while (iter.hasNext()) {
				CalendarDetailInfo composite = iter.next();
				composite.setCalPatternId(null);
				composite.setCalPatternInfo(null);
			}
			this.calInfoListComposite.update();
		}
		setOwnerRoleId(ownerRoleId);
	}

	public void setManagerName(String managerName) {
		this.calInfoListComposite.setManagerName(managerName);
	}

	public void changeManagerName(String managerName, boolean clear) {
		if (managerName == null
				|| !managerName.equals(this.calInfoListComposite.getManagerName())) {

			Iterator<CalendarDetailInfo> iter = calInfoListComposite.getDetailList().iterator();
			while (iter.hasNext() && clear) {
				CalendarDetailInfo composite = iter.next();
				composite.setCalPatternId(null);
				composite.setCalPatternInfo(null);
			}
			this.calInfoListComposite.update();
		}
		setManagerName(managerName);
	}
}
