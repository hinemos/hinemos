/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.dialog;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.filtersetting.composite.FilterSettingSelectorComposite;
import com.clustercontrol.rest.endpoint.filtersetting.dto.enumtype.FilterCategoryEnum;
import com.clustercontrol.util.Messages;

/**
 * フィルタ設定に対応したフィルタダイアログの基本となるクラスです。
 * フィルタ分類ごとにサブクラスがあります。
 */
public abstract class FilterDialog extends CommonDialog {

	/** true:入力欄を全体的に無効化 */
	private boolean enabled;

	/**
	 * 配置されているコントロールの有効状態を保存します。
	 * この情報は、本ダイアログを無効化する際に保存され、有効化する際に参照されます
	 */
	private Map<Control, Boolean> savedEnabledFlags;

	private FilterSettingSelectorComposite cmpSelector;
	private Group grpLower;
	private Button btnClear;

	public FilterDialog(Shell parent) {
		super(parent);
		enabled = true;
		savedEnabledFlags = new HashMap<>();
	}

	@Override
	protected Point getInitialSize() {
		// 後でpackするので実サイズは変わる
		return new Point(ClusterControlPlugin.WINDOW_INIT_SIZE.width, ClusterControlPlugin.WINDOW_INIT_SIZE.height);
	}

	@Override
	protected final void customizeDialog(Composite parent) {
		// タイトル
		this.getShell().setText(getTitle());

		// レイアウト
		GridLayout lyoParent = new GridLayout(1, false);
		lyoParent.marginWidth = 10;
		lyoParent.marginHeight = 10;
		parent.setLayout(lyoParent);

		// サブクラス独自のコンポジットがあれば生成
		createHeaderComposite(parent);

		// フィルタ設定
		Group grpUpper = new Group(parent, SWT.NONE);
		grpUpper.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grpUpper.setText(Messages.getString("fltset.edit_group.setting"));
		GridLayout lyoUpper = new GridLayout(1, false);
		lyoUpper.marginWidth = 5;
		lyoUpper.marginHeight = 5;
		grpUpper.setLayout(lyoUpper);

		cmpSelector = new FilterSettingSelectorComposite(grpUpper, getFilterCategory());
		cmpSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		// サブクラス独自のコンポジットがあれば生成
		createMiddleComposite(parent);

		// フィルタ条件
		grpLower = new Group(parent, SWT.NONE);
		grpLower.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpLower.setText(Messages.getString("fltset.edit_group.condition"));
		GridLayout lyoLower = new GridLayout(1, false);
		lyoLower.marginWidth = 5;
		lyoLower.marginHeight = 5;
		grpLower.setLayout(lyoLower);

		Composite cmpFilter = createFilterComposite(grpLower, cmpSelector);
		cmpFilter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// サブクラス独自のコンポジットがあれば生成
		createFooterComposite(parent);

		// 選択リスト更新
		cmpSelector.reload();

		// サブクラスで初期化処理があれば
		initializeAfterCustomizeDialog();

		// ダイアログのサイズ調整
		adjustPosition();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// クリアボタンを追加する
		// - キャンセルボタンがショートカットキー"C"を使っているので、そちらと被らないように
		//   メッセージリソース"clear"ではなく"clear.2"を使用する。
		btnClear = this.createButton(parent, IDialogConstants.OPEN_ID, Messages.getString("clear.2"), false);
		btnClear.addSelectionListener(
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						cmpSelector.unselect();
						resetFilterComposite();
					}
				});

		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

	/**
	 * サブクラスが対応するフィルタカテゴリを返します。
	 */
	protected abstract FilterCategoryEnum getFilterCategory();

	/**
	 * ダイアログのタイトルバーへ表示する文字列を返します。
	 */
	protected abstract String getTitle();

	/**
	 * サブクラスがダイアログの最上部(フィルタ設定選択の上)に独自のコンポーネントを配置したい場合は、本メソッドを使用してください。
	 * 親のレイアウトは{@link GridLayout}で、カラム数は 1 です。
	 */
	protected void createHeaderComposite(Composite parent) {
		// NOP
	}

	/**
	 * サブクラスがダイアログの中央(フィルタ設定選択とフィルタ条件の間)に独自のコンポーネントを配置したい場合は、本メソッドを使用してください。
	 * 親のレイアウトは{@link GridLayout}で、カラム数は 1 です。
	 */
	protected void createMiddleComposite(Composite parent) {
		// NOP
	}

	/**
	 * サブクラスがダイアログの最下部(フィルタ条件の下)に独自のコンポーネントを配置したい場合は、本メソッドを使用してください。
	 * 親のレイアウトは{@link GridLayout}で、カラム数は 1 です。
	 */
	protected void createFooterComposite(Composite parent) {
		// NOP
	}

	/**
	 * サブクラス特有のフィルタ条件入力コンポジットを生成して返します。
	 * @param parent 生成するコンポジットの親とするコンポジット。
	 * @param cmpSelector フィルタ設定選択コンポジット。
	 */
	protected abstract Composite createFilterComposite(Composite parent, FilterSettingSelectorComposite cmpSelector);

	/**
	 * {@link #customizeDialog(Composite)} 終了直前に呼ばれます。
	 * サブクラスでコントロール類の初期化を行いたい場合は、本メソッドを使用してください。 
	 */
	protected void initializeAfterCustomizeDialog() {
		// NOP
	}

	/**
	 * サブクラス特有のフィルタ条件入力コンポジットの内容をリセットします。
	 */
	protected abstract void resetFilterComposite();

	/**
	 * 入力を有効化/無効化します。
	 * @param enabled 有効にする場合は true、無効にする場合は false。
	 */
	public void setEnabled(boolean enabled) {
		if (this.enabled != enabled) {
			setEnabledRecursive(cmpSelector, enabled);
			setEnabledRecursive(grpLower, enabled);
			btnClear.setEnabled(enabled);
			this.enabled = enabled;
		}
	}

	/** {@link #setEnabled(boolean)}の下請けです。 */
	private void setEnabledRecursive(Control control, boolean enabled) {
		if (control instanceof Composite) {
			for (Control c : ((Composite) control).getChildren()) {
				setEnabledRecursive(c, enabled);
			}
		}
		if (enabled) {
			// 状態をリストア
			Boolean flag = savedEnabledFlags.get(control);
			if (flag == null) flag = enabled;
			control.setEnabled(flag);
		} else {
			// 状態を保存してから無効化
			savedEnabledFlags.put(control, control.getEnabled());
			control.setEnabled(false);
		}
	}

}
