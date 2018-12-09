/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.dialog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.UISession;
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
import org.eclipse.swt.widgets.Tree;

import com.clustercontrol.bean.Property;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.monitor.action.GetStatusFilterProperty;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.viewer.PropertySheet;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 監視[ステータスのフィルタ処理]ダイアログクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class StatusFilterDialog extends CommonDialog {

	/** プロパティシート。 */
	private PropertySheet propertySheet = null;

	// 後でpackするためsizeXはダミーの値。
	private static final int sizeX = 500;
	private static final int sizeY = 600;

	/** Cache map of filter properties for each UI session */
	private static Map<UISession, Property> filterPropertyCache = new ConcurrentHashMap<>();

	/**
	 * ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public StatusFilterDialog(Shell parent) {
		super(parent);
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
	 * @param parent
	 *            親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.monitor.filter.status"));

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);

		/*
		 * 属性プロパティシート
		 */

		// ラベル
		Label label = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "attribute", label);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("attribute") + " : ");

		// プロパティシート
		Tree table = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		WidgetTestUtil.setTestId(this, null, table);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		table.setLayoutData(gridData);

		createPropertySheet(table);

		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		WidgetTestUtil.setTestId(this, "line", line);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		line.setLayoutData(gridData);

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

		//ダイアログのサイズ調整（pack:resize to be its preferred size）
		shell.pack();
		shell.setSize(new Point(shell.getSize().x, sizeY));
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
	 * 入力値を保持したデータモデルを生成します。
	 *
	 * @return データモデル
	 */
	public Property getInputData() {
		Property property = getOrInitFilterProperty();
		return PropertyUtil.copy( property );
	}

	/**
	 * 既存のボタンに加え、クリアボタンを追加します。
	 *
	 * @param parent
	 *            ボタンバーコンポジット
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// クリアボタン
		this.createButton(parent, IDialogConstants.OPEN_ID, Messages
				.getString("clear"), false);
		this.getButton(IDialogConstants.OPEN_ID).addSelectionListener(
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						// プロパティ設定
						resetPropertySheet();
					}
				});

		super.createButtonsForButtonBar(parent);
	}

	/**
	 * ＯＫボタンのテキストを返します。
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンのテキストを返します。
	 *
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

	/**
	 * 検索条件用プロパティ設定
	 * 前回ダイアログオープン時の情報を保持するために使用します。
	 */
	private void createPropertySheet( Tree table ){
		propertySheet = new PropertySheet(table);
		Property filterProperty = getOrInitFilterProperty();
		propertySheet.setInput(filterProperty);
		propertySheet.expandAll();
	}

	/**
	 * Reset property sheet
	 * The filter properties will be cleared
	 */
	private void resetPropertySheet(){
		Property filterProperty = this.initFilterProperty();
		propertySheet.setInput(filterProperty);
		propertySheet.expandAll();
	}

	/**
	 * Initialize a filter property
	 */
	private Property initFilterProperty() {
		Property property = new GetStatusFilterProperty().getProperty();
		filterPropertyCache.put(RWT.getUISession(), property);
		return property;
	}

	/**
	 * Get the cached filter property if existed,
	 * or initialize one while not.
	 */
	private Property getOrInitFilterProperty() {
		Property property = filterPropertyCache.get(RWT.getUISession());
		if( null == property ){
			property = initFilterProperty();
		}
		return property;
	}
}
