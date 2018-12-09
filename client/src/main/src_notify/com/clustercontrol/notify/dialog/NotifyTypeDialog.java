/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.notify.bean.NotifyTypeConstant;
import com.clustercontrol.notify.composite.NotifyTypeListComposite;
import com.clustercontrol.notify.util.NotifyTypeUtil;
import com.clustercontrol.notify.view.action.NotifyModifyAction;
import com.clustercontrol.util.Messages;

/**
 * 作成する通知機能を選択するダイアログ<BR>
 *
 * @version 3.0.0
 * @since 3.0.0
 */
public class NotifyTypeDialog extends CommonDialog {

	// ----- instance フィールド ----- //

	// 後でpackするためsizeXはダミーの値。
	private static final int sizeX = 300;
	private static final int sizeY = 300;

	/** 選択されたアイテム */
	private NotifyTypeListComposite listComposite = null;
	private ListViewer notifyTypeList = null;

	private String managerName = null;

	Composite composite = null;

	// ----- コンストラクタ ----- //

	/**
	 * ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親とするシェル
	 */
	public NotifyTypeDialog(Shell parent, Composite composite, String managerName) {
		super(parent);
		this.composite = composite;
		this.managerName = managerName;
	}

	// ----- instance メソッド ----- //

	@Override
	protected Point getInitialSize() {
		return new Point(sizeX, sizeY);
	}

	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();
		// タイトル
		parent.getShell().setText(Messages.getString("notify.type.list"));

		GridLayout layout = new GridLayout(5, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		listComposite = new NotifyTypeListComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, listComposite);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 5;
		listComposite.setLayoutData(gridData);

		notifyTypeList = listComposite.getNotifyTypeList();

		notifyTypeList.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				Integer notifyType = (Integer)element;
				return NotifyTypeUtil.typeToString(notifyType);
			}
		});

		// 通知タイプ定義のインスタンスを登録する
		for (Integer type : NotifyTypeConstant.getList()) {
			notifyTypeList.add(type);
		}

		// アイテムをダブルクリックした場合、それを選択したこととする。
		notifyTypeList.addDoubleClickListener(
				new IDoubleClickListener() {
					@Override
					public void doubleClick(DoubleClickEvent event) {
						okPressed();
					}
				});
		//ダイアログのサイズ調整（pack:resize to be its preferred size）
		shell.pack();
		shell.setSize(new Point(shell.getSize().x, shell.getSize().y));
	}

	public Integer getSelectItem() {
		return this.listComposite.getSelectItem();
	}

	/**
	 * Show create dialog and do not close after OK pressed
	 */
	@Override
	protected void okPressed() {
		ValidateResult result = null;

		// 選択項目のnullチェック
		if(this.getSelectItem() == null){
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.notify.28"));
			displayError(result);
		}

		// DBに登録されているダイアログのクラス名を取得する
		Integer notifyType = this.getSelectItem();
		NotifyModifyAction action = new NotifyModifyAction();
		if (action.openDialog(getParentShell(), this.managerName, null, notifyType) == IDialogConstants.OK_ID) {
			composite.update();
		}
	}

	@Override
	protected String getOkButtonText() {
		return Messages.getString("next");
	}

	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}
}
