/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.dialog;

import java.util.Map;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.HinemosPropertyResponse.TypeEnum;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.maintenance.HinemosPropertyTypeMessage;
import com.clustercontrol.maintenance.composite.HinemosPropertyTypeListComposite;
import com.clustercontrol.maintenance.view.HinemosPropertyView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 共通設定種別一覧を表示するダイアログクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class HinemosPropertyTypeDialog extends CommonDialog {

	// ----- instance フィールド ----- //
	// 後でpackするためsizeXはダミーの値。
	private static final int sizeX = 300;
	private static final int sizeY = 300;

	// 共通設定種別一覧用コンポジット
	private HinemosPropertyTypeListComposite listComposite = null;

	// 共通設定種別リスト用ビュー(listComposite内のオブジェクト)
	private ListViewer HinemosPropertyTypeList = null;

	// 呼び出し元ビュー
	private HinemosPropertyView view = null;

	/** マネージャ名 */
	private String managerName = null;

	// ----- コンストラクタ ----- //

	/**
	 * ダイアログのインスタンスを返します。
	 *
	 * @param parent 親とするシェル
	 */
	public HinemosPropertyTypeDialog(Shell parent, HinemosPropertyView view, String managerName) {
		super(parent);
		this.view = view;
		this.managerName = managerName;
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
		parent.getShell().setText(Messages.getString("hinemos.property.type"));

		GridLayout layout = new GridLayout(5, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		listComposite = new HinemosPropertyTypeListComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, listComposite);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 5;
		listComposite.setLayoutData(gridData);

		HinemosPropertyTypeList = listComposite.getHinemosPropertyTypeList();

		HinemosPropertyTypeList.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return element.toString();
			}
		});
		// 共通設定タイプ定義のインスタンスを登録する
		for (Map.Entry<TypeEnum, String> entry : HinemosPropertyTypeMessage
				.getList().entrySet()) {
			HinemosPropertyTypeList.add(entry.getValue());
		}

		// アイテムをダブルクリックした場合、それを選択したこととする。
		HinemosPropertyTypeList
				.addDoubleClickListener(new IDoubleClickListener() {
					@Override
					public void doubleClick(DoubleClickEvent event) {
						okPressed();
					}
				});

		// ダイアログのサイズ調整（pack:resize to be its preferred size）
		shell.pack();
		shell.setSize(new Point(shell.getSize().x, shell.getSize().y));

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);
	}

	/**
	 * 選択されたアイテム(共通設定種別マスタの定義)の取得
	 *
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
	 * 本処理の中で、共通設定作成ダイアログを表示させる。
	 */
	@Override
	protected void okPressed() {
		// 選択項目のnullチェック
		if (this.getSelectItem() == null) {
			ValidateResult result = null;
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.hinemos.property.1"));
			displayError(result);

			return;
		}

		// 選択された値種別を取得する
		TypeEnum valueType = HinemosPropertyTypeMessage.stringToType(this
				.getSelectItem());
		// ダイアログの生成
		HinemosPropertyDialog dialog = new HinemosPropertyDialog(
				getParentShell(), managerName, valueType, PropertyDefineConstant.MODE_ADD, null);

		dialog.open();

		// 共通設定後に、共通設定種別一覧は閉じない。
		// super.okPressed();

		// 共通設定後に共通設定ビューを更新
		this.view.update();
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
