/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.dialog;

import java.util.Locale;

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
import org.eclipse.swt.widgets.Tree;

import com.clustercontrol.bean.Property;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.monitor.bean.MonitorFilterConstant;
import com.clustercontrol.sdml.util.SdmlControlSettingFilterPropertyUtil;
import com.clustercontrol.util.FilterPropertyCache;
import com.clustercontrol.util.FilterPropertyUpdater;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.viewer.PropertySheet;

/**
 * SDML制御設定一覧のフィルタ処理ダイアログクラス
 *
 */
public class SdmlControlSettingFilterDialog extends CommonDialog {
	/** プロパティシート */
	private PropertySheet propertySheet = null;

	// 後でpackするためsizeXはダミーの値。
	private static final int sizeX = 500;
	private static final int sizeY = 600;

	/** プロパティのキャッシュ用クラス */
	private static FilterPropertyCache filterPropertyCache = null;

	/**
	 * コンストラクタ
	 * 
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public SdmlControlSettingFilterDialog(Shell parent) {
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
	 *            親のコンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.sdml.control.setting.filter"));

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
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("attribute") + " : ");

		// プロパティシート
		Tree table = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
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
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		line.setLayoutData(gridData);

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

		// ダイアログのサイズ調整（pack:resize to be its preferred size）
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
	 * 入力値を保持したプロパティを返します。<BR>
	 * プロパティオブジェクトのコピーを返します。
	 *
	 * @return プロパティ
	 */
	public Property getInputData() {
		Property property = getOrInitFilterProperty();
		return PropertyUtil.copy(property);
	}

	/**
	 * 既存のボタンに加え、クリアボタンを追加します。<BR>
	 * クリアボタンがクリックされた場合、 プロパティを再取得します。
	 *
	 * @param parent
	 *            親のコンポジット（ボタンバー）
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// クリアボタン
		this.createButton(parent, IDialogConstants.OPEN_ID, Messages.getString("clear"), false);
		this.getButton(IDialogConstants.OPEN_ID).addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// プロパティ設定
				resetPropertySheet();
			}
		});

		super.createButtonsForButtonBar(parent);
	}

	/**
	 * 検索条件用プロパティ設定<br>
	 * 前回ダイアログオープン時の情報を保持するために使用します。
	 */
	private void createPropertySheet(Tree table) {
		propertySheet = new PropertySheet(table);
		Property filterProperty = getOrInitFilterProperty();
		propertySheet.setInput(filterProperty);
		propertySheet.expandAll();
	}

	/**
	 * Reset property sheet<br>
	 * The filter properties will be cleared
	 */
	private void resetPropertySheet() {
		Property filterProperty = initFilterProperty();
		propertySheet.setInput(filterProperty);
		propertySheet.expandAll();
	}

	/**
	 * Initialize a filter property
	 */
	private Property initFilterProperty() {
		Property property = SdmlControlSettingFilterPropertyUtil.getProperty(Locale.getDefault());
		FilterPropertyUpdater.getInstance().addFilterProperty(getClass(), property, MonitorFilterConstant.MANAGER);
		filterPropertyCache.initFilterPropertyCache(FilterPropertyCache.SDML_CONTROL_SETTING_FILTER_DIALOG_PROPERTY,
				property);
		return property;
	}

	/**
	 * Get the cached filter property if existed,<br>
	 * or initialize one while not.
	 */
	private Property getOrInitFilterProperty() {
		Property property = null;
		if (null == filterPropertyCache) {
			filterPropertyCache = new FilterPropertyCache();
		}
		if (null == filterPropertyCache
				.getFilterPropertyCache(FilterPropertyCache.SDML_CONTROL_SETTING_FILTER_DIALOG_PROPERTY)) {
			property = initFilterProperty();
		} else {
			property = (Property) filterPropertyCache
					.getFilterPropertyCache(FilterPropertyCache.SDML_CONTROL_SETTING_FILTER_DIALOG_PROPERTY);
		}
		return property;
	}
}
