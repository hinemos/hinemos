/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.dialog;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.notify.composite.NotifyBasicComposite;
import com.clustercontrol.notify.composite.NotifyInhibitionComposite;
import com.clustercontrol.notify.composite.NotifyInitialComposite;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.notify.NotifyInfo;
import com.clustercontrol.ws.notify.NotifyInfoDetail;

/**
 * 通知共通情報ダイアログクラス<BR>
 *
 * @version 4.0.0
 * @since 3.0.0
 */
public class NotifyBasicCreateDialog extends CommonDialog {

	// ----- instance フィールド ----- //

	/** 参照フラグ。 */
	protected boolean referenceFlg = false;

	/** マネージャ名 */
	protected String managerName = null;

	/** 変更対象の通知ID。 */
	protected String notifyId = null;

	/** 変更するかどうかのフラグ（true：変更する） modify、copy時に使用  */
	protected boolean updateFlg = false;

	/** 通知基本情報 */
	protected NotifyBasicComposite m_notifyBasic = null;

	/** 初回条件 */
	protected NotifyInitialComposite m_notifyInitial = null;

	/** 抑制条件 */
	protected NotifyInhibitionComposite m_notifyInhibition = null;

	/** この設定を有効にする */
	protected Button m_confirmValid = null;

	/** 入力値を保持するオブジェクト。 */
	protected NotifyInfo inputData = null;

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public NotifyBasicCreateDialog(Shell parent) {
		super(parent);
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param managerName マネージャ名
	 * @param notifyId 変更する通知情報の通知ID
	 * @param updateFlg 更新するか否か（true:更新する）
	 */
	public NotifyBasicCreateDialog(Shell parent, String managerName, String notifyId, boolean updateFlg) {
		super(parent);

		this.managerName = managerName;
		this.notifyId = notifyId;
		this.updateFlg = updateFlg;
		this.referenceFlg = false;
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 *
	 * @see com.clustercontrol.notify.action.GetNotify#getNotify(String)
	 * @see #setInputData(NotifyInfo)
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 15;
		parent.setLayout(layout);

		/*
		 * 通知基本情報
		 */
		m_notifyBasic = new NotifyBasicComposite(parent, SWT.NONE, this.managerName, this.notifyId, !this.updateFlg);
		WidgetTestUtil.setTestId(this, null, m_notifyBasic);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_notifyBasic.setLayoutData(gridData);

		/*
		 * 初回条件
		 */
		m_notifyInitial = new NotifyInitialComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "initial", m_notifyInitial);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_notifyInitial.setLayoutData(gridData);

		/*
		 * 抑制情報
		 */
		m_notifyInhibition = new NotifyInhibitionComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "inhibition", m_notifyInhibition);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_notifyInhibition.setLayoutData(gridData);

		// 空行
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space1", label);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * 継承先で各種通知の設定を行う
		 */
		customizeSettingDialog(parent);

		/*
		 * 有効／無効
		 */
		// 空行
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space2", label);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		this.m_confirmValid = new Button(parent, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "confirmvalid", m_confirmValid);

		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_confirmValid.setLayoutData(gridData);
		this.m_confirmValid.setText(Messages.getString("setting.valid.confirmed"));

		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		WidgetTestUtil.setTestId(this, "line", line);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		line.setLayoutData(gridData);

		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		shell.pack();
		shell.setSize(new Point(780, shell.getSize().y));

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

	}

	/**
	 * 入力値を保持した通知情報を返します。
	 *
	 * @return 通知情報
	 */
	public NotifyInfo getInputData() {
		return null;
	}

	/** 入力されたマネージャ名を返します。
	 *
	 * @return マネージャ名
	 */
	public String getInputManagerName() {
		return this.m_notifyBasic.getManagerListComposite().getText();
	}

	/**
	 * 引数で指定された通知情報の値を、各項目に設定します。
	 *
	 * @param notify 設定値として用いる通知情報
	 */
	protected void setInputData(NotifyInfo notify) {
		this.inputData = notify;

		// 通知基本情報
		this.m_notifyBasic.setInputData(notify, this.updateFlg);

		// 初回通知情報
		this.m_notifyInitial.setInputData(notify);

		// 抑制情報
		this.m_notifyInhibition.setInputData(notify);

		// 有効／無効
		if (notify.isValidFlg() != null
				&& !notify.isValidFlg().booleanValue()) {
			this.m_confirmValid.setSelection(false);
		} else {
			this.m_confirmValid.setSelection(true);
		}
	}

	/**
	 * 入力値を設定した通知情報を返します。<BR>
	 * 入力値チェックを行い、不正な場合は<code>null</code>を返します。
	 *
	 * @return 通知情報
	 *
	 * @see #createInputDataForEvent(ArrayList, int, Button, Combo, Button, Combo, Button, Text)
	 */
	protected NotifyInfo createInputData() {
		NotifyInfo info = new NotifyInfo();

		// 通知基本情報
		this.m_notifyBasic.createInputData(info, this.notifyId);
		
		// 初回通知情報
		this.m_notifyInitial.createInputData(info);

		// 抑制情報
		this.m_notifyInhibition.createInputData(info);

		// 有効/無効
		if (this.m_confirmValid.getSelection()) {
			info.setValidFlg(true);
		} else {
			info.setValidFlg(false);
		}

		return info;
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
	 * 入力値をマネージャに登録します。
	 *
	 * @return true：正常、false：異常
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#action()
	 */
	@Override
	protected boolean action() {
		return true;
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
	 * コンポジットの選択可/不可を設定します。
	 *
	 * @param enable 選択可の場合、<code> true </code>
	 */
	protected void setEnabled(boolean enable) {

	}

	/**
	 * 無効な入力値の情報を設定します。
	 *
	 * @param id ID
	 * @param message メッセージ
	 */
	protected void setValidateResult(String id, String message) {

	}

	/**
	 * ボタンを生成します。<BR>
	 * 参照フラグが<code> true </code>の場合は閉じるボタンを生成し、<code> false </code>の場合は、デフォルトのボタンを生成します。
	 *
	 * @param parent ボタンバーコンポジット
	 *
	 * @see #createButtonsForButtonBar(Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
	}

	/**
	 * 各種通知の設定を設定します。
	 */
	protected void customizeSettingDialog(Composite parent){

	}

	/**
	 * オーナーロールIDを設定します。
	 * 継承先クラスにてオーナーロールIDに関連するオブジェクト権限を持つ入力項目をクリアします。
	 */
	public void setOwnerRoleId(String ownerRoleId) {
		m_notifyBasic.setOwnerRoleId(ownerRoleId);
	}

	public String getOwnerRoleId(){
		return m_notifyBasic.getOwnerRoleId();
	}

	public Combo getComboOwnerRoleId() {
		return m_notifyBasic.getRoleIdList().getComboRoleId();
	}

	public Combo getComboManagerName() {
		return m_notifyBasic.getManagerListComposite().getComboManagerName();
	}
	public boolean getUpdateFlg(){
		return updateFlg;
	}

	protected boolean isNotNullAndBlank(String str) {
		return str != null && !str.trim().isEmpty();
	}

	protected Boolean[] getValidFlgs(NotifyInfoDetail info) {
		Boolean[] validFlgs = new Boolean[] {
				info.isInfoValidFlg(),
				info.isWarnValidFlg(),
				info.isCriticalValidFlg(),
				info.isUnknownValidFlg()
		};
		return validFlgs;
	}

}
