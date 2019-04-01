/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.dialog;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.bean.PriorityColorConstant;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.monitor.bean.EventConfirmConstant;
import com.clustercontrol.monitor.bean.EventConfirmMessage;
import com.clustercontrol.notify.action.AddNotify;
import com.clustercontrol.notify.action.GetNotify;
import com.clustercontrol.notify.action.ModifyNotify;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.notify.NotifyEventInfo;
import com.clustercontrol.ws.notify.NotifyInfo;

/**
 * 通知（イベント）作成・変更ダイアログクラス<BR>
 *
 * @version 4.0.0
 * @since 3.0.0
 */
public class NotifyEventCreateDialog extends NotifyBasicCreateDialog {

	/** カラム数（重要度）。 */
	private static final int WIDTH_PRIORITY 		= 2;

	/** カラム数（通知）。 */
	private static final int WIDTH_NOTIFY	 		= 2;

	/** カラム数（状態）。 */
	private static final int WIDTH_STATE		= 3;

	/** カラム数（抑制）。 */
	private static final int WIDTH_INHIBITION_FLG 	= 2;

	/** カラム数（空欄）。 */
	private static final int WIDTH_BLANK 			= 3;

	// ----- instance フィールド ----- //

	/** 通知タイプ
	 * @see com.clustercontrol.bean.NotifyTypeConstant
	 */
	private static final int TYPE_EVENT = 1;

	/** 入力値の正当性を保持するオブジェクト。 */
	protected ValidateResult validateResult = null;

	/** 通知（通知） チェックボックス。 */
	private Button m_checkEventNormalInfo = null;
	/** 通知（警告） チェックボックス。 */
	private Button m_checkEventNormalWarning = null;
	/** 通知（異常） チェックボックス。 */
	private Button m_checkEventNormalCritical = null;
	/** 通知（不明） チェックボックス。 */
	private Button m_checkEventNormalUnknown = null;

	/** 通知状態（通知） ボタン。 */
	private Combo m_comboEventNormalInfo = null;
	/** 通知状態（警告） ボタン。 */
	private Combo m_comboEventNormalWarning = null;
	/** 通知状態（異常） ボタン。 */
	private Combo m_comboEventNormalCritical = null;
	/** 通知状態（不明） ボタン。 */
	private Combo m_comboEventNormalUnknown = null;

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public NotifyEventCreateDialog(Shell parent) {
		super(parent);
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param managerName マネージャ名
	 * @param notifyId 変更する通知情報の通知ID
	 * @param updateFlg 更新フラグ（true:更新する）
	 */
	public NotifyEventCreateDialog(Shell parent, String managerName, String notifyId, boolean updateFlg) {
		super(parent, managerName, notifyId, updateFlg);
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 *
	 * @see com.clustercontrol.notify.dialog.NotifyBasicCreateDialog#customizeDialog(Composite)
	 * @see com.clustercontrol.notify.action.GetNotify#getNotify(String)
	 * @see #setInputData(NotifyInfo)
	 */
	@Override
	protected void customizeDialog(Composite parent) {

		super.customizeDialog(parent);

		// 通知IDが指定されている場合、その情報を初期表示する。
		NotifyInfo info = null;
		if(this.notifyId != null){
			info = new GetNotify().getNotify(this.managerName, this.notifyId);
		}else{
			info = new NotifyInfo();
		}
		this.setInputData(info);

	}

	/**
	 * 親のクラスから呼ばれ、各通知用のダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 *
	 * @see com.clustercontrol.notify.dialog.NotifyBasicCreateDialog#customizeDialog(Composite)
	 */
	@Override
	protected void customizeSettingDialog(Composite parent) {
		final Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.notify.event.create.modify"));

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
		 * イベント
		 */
		// イベントグループ
		Group groupEvent = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "event", groupEvent);

		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupEvent.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupEvent.setLayoutData(gridData);
		groupEvent.setText(Messages.getString("notifies.event"));

		/*
		 * 重要度 ごとの設定
		 */
		// ラベル（重要度）
		label = new Label(groupEvent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "priority", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_PRIORITY;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("priority"));

		// ラベル（通知）
		label = new Label(groupEvent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "notifyattribute", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_NOTIFY;
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("notify.attribute"));

		// ラベル（状態）
		label = new Label(groupEvent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "status", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_STATE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("status"));

		// 空欄
		label = new Label(groupEvent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space1", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_BLANK + WIDTH_INHIBITION_FLG + WIDTH_STATE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		//　イベント　重要度：通知
		label = this.getLabelPriority(groupEvent, Messages.getString("info"),PriorityColorConstant.COLOR_INFO);
		this.m_checkEventNormalInfo = this.getCheckEventNormal(groupEvent);
		WidgetTestUtil.setTestId(this, "normalinfo", m_checkEventNormalInfo);
		this.m_comboEventNormalInfo = this.getComboEventNormal(groupEvent);
		WidgetTestUtil.setTestId(this, "normalinfo", m_comboEventNormalInfo);

		// 空欄
		label = new Label(groupEvent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space2", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_BLANK + WIDTH_INHIBITION_FLG + WIDTH_STATE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		//　イベント　重要度：警告
		label = this.getLabelPriority(groupEvent, Messages.getString("warning"),PriorityColorConstant.COLOR_WARNING);
		this.m_checkEventNormalWarning = this.getCheckEventNormal(groupEvent);
		WidgetTestUtil.setTestId(this, "normalwarning", m_checkEventNormalWarning);
		this.m_comboEventNormalWarning = this.getComboEventNormal(groupEvent);
		WidgetTestUtil.setTestId(this, "normalwarning", m_comboEventNormalWarning);

		// 空欄
		label = new Label(groupEvent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space3", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_BLANK + WIDTH_INHIBITION_FLG + WIDTH_STATE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		//　イベント　重要度：危険
		label = this.getLabelPriority(groupEvent, Messages.getString("critical"),PriorityColorConstant.COLOR_CRITICAL);
		this.m_checkEventNormalCritical = this.getCheckEventNormal(groupEvent);
		WidgetTestUtil.setTestId(this, "normalcritical", m_checkEventNormalCritical);
		this.m_comboEventNormalCritical = this.getComboEventNormal(groupEvent);
		WidgetTestUtil.setTestId(this, "normalcritical", m_comboEventNormalCritical);

		// 空欄
		label = new Label(groupEvent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space4", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_BLANK + WIDTH_INHIBITION_FLG + WIDTH_STATE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		//　イベント　重要度：不明
		label = this.getLabelPriority(groupEvent, Messages.getString("unknown"),PriorityColorConstant.COLOR_UNKNOWN);
		this.m_checkEventNormalUnknown = this.getCheckEventNormal(groupEvent);
		WidgetTestUtil.setTestId(this, "normalunknown", m_checkEventNormalUnknown);
		this.m_comboEventNormalUnknown = this.getComboEventNormal(groupEvent);
		WidgetTestUtil.setTestId(this, "normalunknown", m_comboEventNormalUnknown);

		// 空欄
		label = new Label(groupEvent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space5", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_BLANK + WIDTH_INHIBITION_FLG + WIDTH_STATE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
	}

	/**
	 * 入力値を保持した通知情報を返します。
	 *
	 * @return 通知情報
	 */
	@Override
	public NotifyInfo getInputData() {
		return this.inputData;
	}

	/**
	 * 引数で指定された通知情報の値を、各項目に設定します。
	 *
	 * @param notify 設定値として用いる通知情報
	 */
	@Override
	protected void setInputData(NotifyInfo notify) {
		super.setInputData(notify);

		// コマンド情報
		NotifyEventInfo info = notify.getNotifyEventInfo();
		if (info != null) {
			this.setInputDatal(info);
		}
	}

	private void setInputDatal(NotifyEventInfo info) {
		Button[] checkEventNormals = new Button[] {
				this.m_checkEventNormalInfo,
				this.m_checkEventNormalWarning,
				this.m_checkEventNormalCritical,
				this.m_checkEventNormalUnknown
		};
		Combo[] comboEventNormals = new Combo[] {
				this.m_comboEventNormalInfo,
				this.m_comboEventNormalWarning,
				this.m_comboEventNormalCritical,
				this.m_comboEventNormalUnknown
		};
		Integer[] eventNormalStates = new Integer[] {
				info.getInfoEventNormalState(),
				info.getWarnEventNormalState(),
				info.getCriticalEventNormalState(),
				info.getUnknownEventNormalState()
		};

		Boolean[] validFlgs = this.getValidFlgs(info);
		for (int i = 0; i < validFlgs.length; i++) {
			// イベント通知
			boolean valid = validFlgs[i].booleanValue();
			checkEventNormals[i].setSelection(valid);
			WidgetTestUtil.setTestId(this, "checkEventNormals" + i, checkEventNormals[i]);


			// イベント画面出力時の通知状態
			if (eventNormalStates[i] != null) {
				comboEventNormals[i].setText(EventConfirmMessage.typeToString(eventNormalStates[i]));
				WidgetTestUtil.setTestId(this, "comboEventNormals" + i, comboEventNormals[i]);
			}

		}
	}

	/**
	 * 入力値を設定した通知情報を返します。<BR>
	 * 入力値チェックを行い、不正な場合は<code>null</code>を返します。
	 *
	 * @return 通知情報
	 *
	 * @see #createInputDataForEvent(ArrayList, int, Button, Combo, Button, Combo)
	 */
	@Override
	protected NotifyInfo createInputData() {
		NotifyInfo info = super.createInputData();

		// 通知タイプの設定
		info.setNotifyType(TYPE_EVENT);

		// イベント情報
		NotifyEventInfo event = createNotifyInfoDetail();
		info.setNotifyEventInfo(event);

		return info;
	}

	/**
	 * 入力値を設定した通知イベント情報を返します。<BR>
	 * 入力値チェックを行い、不正な場合は<code>null</code>を返します。
	 *
	 * @return 通知イベント情報
	 *
	 */
	private NotifyEventInfo createNotifyInfoDetail() {
		NotifyEventInfo event = new NotifyEventInfo();

		// イベント通知
		event.setInfoValidFlg(m_checkEventNormalInfo.getSelection());
		event.setWarnValidFlg(m_checkEventNormalWarning.getSelection());
		event.setCriticalValidFlg(m_checkEventNormalCritical.getSelection());
		event.setUnknownValidFlg(m_checkEventNormalUnknown.getSelection());

		// イベント通知時の状態
		event.setInfoEventNormalState(EventConfirmConstant.TYPE_UNCONFIRMED);
		event.setWarnEventNormalState(EventConfirmConstant.TYPE_UNCONFIRMED);
		event.setCriticalEventNormalState(EventConfirmConstant.TYPE_UNCONFIRMED);
		event.setUnknownEventNormalState(EventConfirmConstant.TYPE_UNCONFIRMED);

		if (isNotNullAndBlank(m_comboEventNormalInfo.getText())) {
			event.setInfoEventNormalState(EventConfirmMessage.stringToType(m_comboEventNormalInfo.getText()));
		}
		if (isNotNullAndBlank(m_comboEventNormalWarning.getText())) {
			event.setWarnEventNormalState(EventConfirmMessage.stringToType(m_comboEventNormalWarning.getText()));
		}
		if (isNotNullAndBlank(m_comboEventNormalCritical.getText())) {
			event.setCriticalEventNormalState(EventConfirmMessage.stringToType(m_comboEventNormalCritical.getText()));
		}
		if (isNotNullAndBlank(m_comboEventNormalUnknown.getText())) {
			event.setUnknownEventNormalState(EventConfirmMessage.stringToType(m_comboEventNormalUnknown.getText()));
		}

		return event;
	}

	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 */
	@Override
	protected ValidateResult validate() {
		// 入力値生成
		this.inputData = this.createInputData();

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
		boolean result = false;

		NotifyInfo info = this.getInputData();

		if(info != null){
			if (!this.updateFlg) {
				// 作成の場合
				result = new AddNotify().add(this.getInputManagerName(), info);
			}
			else{
				// 変更の場合
				result = new ModifyNotify().modify(this.getInputManagerName(), info);
			}
		}

		return result;
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
	 * 無効な入力値の情報を設定します。
	 *
	 * @param id ID
	 * @param message メッセージ
	 */
	@Override
	protected void setValidateResult(String id, String message) {

		this.validateResult = new ValidateResult();
		this.validateResult.setValid(false);
		this.validateResult.setID(id);
		this.validateResult.setMessage(message);
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

		if(!this.referenceFlg){
			super.createButtonsForButtonBar(parent);
		}
		else{
			// 閉じるボタン
			this.createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("close"), false);
		}
	}
	/**
	 * コンポジットの選択可/不可を設定します。
	 *
	 * @param enable 選択可の場合、<code> true </code>
	 */
	@Override
	protected void setEnabled(boolean enable) {
		super.m_notifyBasic.setEnabled(enable);
		super.m_notifyInhibition.setEnabled(enable);
	}

	/**
	 * 重要度のラベルを返します。
	 *
	 * @param parent 親のコンポジット
	 * @param text ラベルに表示するテキスト
	 * @param background ラベルの背景色
	 * @return 生成されたラベル
	 */
	private Label getLabelPriority(Composite parent,
			String text,
			Color background
			) {

		// ラベル（重要度）
		Label label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "labelpriority", label);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_PRIORITY;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(text + " : ");
		label.setBackground(background);

		return label;
	}

	/**
	 * イベント通知の通知のチェックボックスを返します。
	 *
	 * @param parent 親のコンポジット
	 * @return 生成されたチェックボックス
	 */
	private Button getCheckEventNormal(Composite parent) {

		// チェックボックス（通知する）
		Button button = new Button(parent, SWT.CHECK);
		WidgetTestUtil.setTestId(this, null, button);

		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_NOTIFY;
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		button.setLayoutData(gridData);

		return button;
	}

	/**
	 * イベント通知の通知状態コンボボックスを返します。
	 *
	 * @param parent 親のコンポジット
	 * @return 生成されたコンボボックス
	 */
	private Combo getComboEventNormal(Composite parent) {

		// コンボボックス（通知状態）
		Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, null, combo);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_STATE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		combo.setLayoutData(gridData);
		combo.add(EventConfirmMessage.STRING_UNCONFIRMED);
		combo.add(EventConfirmMessage.STRING_CONFIRMING);
		combo.add(EventConfirmMessage.STRING_CONFIRMED);
		combo.setText(EventConfirmMessage.STRING_UNCONFIRMED);

		return combo;
	}
}
