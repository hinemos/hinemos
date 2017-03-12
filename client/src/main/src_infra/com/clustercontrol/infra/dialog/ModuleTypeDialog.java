/*

Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.dialog;

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

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.infra.bean.ModuleTypeMessage;
import com.clustercontrol.infra.composite.ModuleTypeListComposite;
import com.clustercontrol.infra.view.InfraModuleView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
/**
 * モジュール種別一覧を表示するダイアログクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class ModuleTypeDialog extends CommonDialog {

	// ----- instance フィールド ----- //
	// 後でpackするためsizeXはダミーの値。
	private static final int sizeX = 300;
	private static final int sizeY = 300;

	// モジュール種別一覧用コンポジット
	private ModuleTypeListComposite listComposite = null;

	// モジュール種別リスト用ビュー(listComposite内のオブジェクト)
	private ListViewer moduleTypeList = null;

	private String managerName = null;

	private String managementId =null;

	private InfraModuleView view = null;

	// ----- コンストラクタ ----- //

	/**
	 * ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親とするシェル
	 */
	public ModuleTypeDialog(Shell parent) {
		super(parent);
	}

	public ModuleTypeDialog(Shell parent, String managerName, String managementId, InfraModuleView view) {
		super(parent);
		this.managerName = managerName;
		this.managementId = managementId;
		this.view = view;
	}
	// ----- instance メソッド ----- //

	@Override
	protected Point getInitialSize() {
		return new Point(sizeX, sizeY);
	}

	/**
	 * ダイアログ作成のメイン処理
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		parent.getShell().setText(Messages.getString("infra.module.type"));

		GridLayout layout = new GridLayout(5, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		listComposite = new ModuleTypeListComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId( this, null, listComposite );
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 5;
		listComposite.setLayoutData(gridData);

		moduleTypeList = listComposite.getMonitorTypeList();

		moduleTypeList.setLabelProvider(new LabelProvider(){
			@Override
			public String getText(Object element) {
				return (String)element;
			}
		});

		for(String moduleType: ModuleTypeMessage.getAllStrings()){
			moduleTypeList.add(moduleType);
		}

		// アイテムをダブルクリックした場合、それを選択したこととする。
		moduleTypeList.addDoubleClickListener(
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

	/**
	 * 選択されたアイテム(モジュール種別マスタの定義)の取得
	 * @return
	 */
	public String getSelectItem() {
		return this.listComposite.getSelectItem();
	}

	/**
	 * OK ボタンの表示テキスト設定
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("next");
	}

	/**
	 * OK ボタン押下<BR>
	 * 本処理の中で、指定されたモジュール機能の作成ダイアログを表示させる。
	 */
	@Override
	protected void okPressed() {
		// 選択項目のnullチェック
		if(this.getSelectItem() == null){
			ValidateResult result = null;
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.infra.specify.item", new Object[]{Messages.getString("infra.module.type")}));
			displayError(result);
		}

		CommonDialog dialog = null;
		if(getSelectItem().equals(ModuleTypeMessage.STRING_COMMAND)){
			dialog = new CommandModuleDialog(getShell(), this.managerName, this.managementId);
		} else if (getSelectItem().equals(ModuleTypeMessage.STRING_FILETRANSFER)){
			dialog = new FileTransferModuleDialog(getShell(), this.managerName, this.managementId);
		} else {
			throw new InternalError("dialog is null.");
		}

		dialog.open();

		view.update(this.managerName, this.managementId);

		// モジュール設定後に、モジュール種別一覧は閉じない。
		// super.okPressed();
	}

	@Override
	protected void cancelPressed() {
		super.cancelPressed();
	}

	/**
	 * キャンセルボタンの表示テキスト設定
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}
}
