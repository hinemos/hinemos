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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.StatusNotifyDetailInfoResponse;
import org.openapitools.client.model.StatusNotifyDetailInfoResponse.StatusInvalidFlgEnum;
import org.openapitools.client.model.StatusNotifyDetailInfoResponse.StatusUpdatePriorityEnum;

import com.clustercontrol.bean.PriorityColorConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.monitor.bean.StatusValidPeriodConstant;
import com.clustercontrol.monitor.bean.StatusValidPeriodMessage;
import com.clustercontrol.notify.action.AddNotify;
import com.clustercontrol.notify.action.GetNotify;
import com.clustercontrol.notify.action.ModifyNotify;
import com.clustercontrol.notify.dialog.bean.NotifyInfoInputData;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 通知（ステータス）作成・変更ダイアログクラス<BR>
 *
 * @version 4.0.0
 * @since 3.0.0
 */
public class NotifyStatusCreateDialog extends NotifyBasicCreateDialog {

	/** カラム数（重要度）。 */
	private static final int WIDTH_PRIORITY 	= 2;

	/** カラム数（チェックボックス）。 */
	private static final int WIDTH_CHECK 		= 2;

	/** カラム数（空欄）。 */
	private static final int WIDTH_BLANK 		= 11;


	// ----- instance フィールド ----- //

	/** 通知タイプ
	 * @see com.clustercontrol.bean.NotifyTypeConstant
	 */
	private static final int TYPE_STATUS = 0;

	/** 入力値の正当性を保持するオブジェクト。 */
	protected ValidateResult validateResult = null;

	/** ステータス 通知（重要度：通知） チェックボックス。 */
	private Button m_checkStatusNormalInfo = null;
	/** ステータス 通知（重要度：警告） チェックボックス。 */
	private Button m_checkStatusNormalWarning = null;
	/** ステータス 通知（重要度：危険） チェックボックス。 */
	private Button m_checkStatusNormalCritical = null;
	/** ステータス 通知（重要度：不明） チェックボックス。 */
	private Button m_checkStatusNormalUnknown = null;

	/**  ステータス通知 ステータス情報の存続期間 コンボボックス。 */
	private Combo m_comboStatusValidPeriod = null;

	/** ステータス通知 存続期間経過後の処理（情報を削除する） ラジオボタン。  */
	private Button m_radioStatusDelete = null;

	/** ステータス通知 存続期間経過後の処理（更新されていない旨のメッセージに置換える） ラジオボタン。 */
	private Button m_radioStatusUpdate = null;

	/**  ステータス通知 更新時の重要度 コンボボックス。 */
	private Combo m_comboStatusPriority = null;


	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public NotifyStatusCreateDialog(Shell parent) {
		super(parent);
		parentDialog = this;
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 * @param notifyId 変更する通知情報の通知ID
	 * @param managerName マネージャ名
	 * @param updateFlg 更新フラグ（true:更新する）
	 */
	public NotifyStatusCreateDialog(Shell parent, String managerName, String notifyId, boolean updateFlg) {
		super(parent, managerName, notifyId, updateFlg);
		parentDialog = this;
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 *
	 * @see com.clustercontrol.notify.dialog.NotifyBasicCreateDialog#customizeDialog(Composite)
	 * @see com.clustercontrol.notify.action.GetNotify#getNotify(String)
	 * @see #setInputData(NotifyInfoInputData)
	 */
	@Override
	protected void customizeDialog(Composite parent) {

		super.customizeDialog(parent);

		// 通知IDが指定されている場合、その情報を初期表示する。
		NotifyInfoInputData inputData;
		if(this.notifyId != null){
			inputData = new GetNotify().getStatusNotify(this.managerName, this.notifyId);
		} else {
			inputData = new NotifyInfoInputData();
		}
		this.setInputData(inputData);

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
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.notify.status.create.modify"));

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
		 * ステータス通知
		 */
		// ステータス通知グループ
		Group groupStatus1 = new Group(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "status1", groupStatus1);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupStatus1.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupStatus1.setLayoutData(gridData);
		groupStatus1.setText(Messages.getString("notifies.status"));

		/*
		 * 重要度 ごとの設定
		 */
		// ラベル（重要度）
		label = new Label(groupStatus1, SWT.NONE);
		WidgetTestUtil.setTestId(this, "priority", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_PRIORITY;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("priority"));

		// ラベル（通知する）
		label = new Label(groupStatus1, SWT.NONE);
		WidgetTestUtil.setTestId(this, "attribute", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_CHECK;
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("notify.attribute"));

		// 空白
		label = new Label(groupStatus1, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space2", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_BLANK;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);


		// ステータス 重要度：通知
		label = this.getLabelPriority(groupStatus1, Messages.getString("info"),PriorityColorConstant.COLOR_INFO);
		this.m_checkStatusNormalInfo = this.getCheckStatusNormal(groupStatus1);
		WidgetTestUtil.setTestId(this, "normalinfo", m_checkStatusNormalInfo);
		this.m_checkStatusNormalInfo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabledForStatusDuration();
			}
		});

		// 空白
		label = new Label(groupStatus1, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space3", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_BLANK;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);


		// ステータス 重要度：警告
		label = this.getLabelPriority(groupStatus1, Messages.getString("warning"),PriorityColorConstant.COLOR_WARNING);
		this.m_checkStatusNormalWarning = this.getCheckStatusNormal(groupStatus1);
		WidgetTestUtil.setTestId(this, "normalwarning", m_checkStatusNormalWarning);
		this.m_checkStatusNormalWarning.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabledForStatusDuration();
			}
		});

		// 空白
		label = new Label(groupStatus1, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space4", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_BLANK;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);


		// ステータス 重要度：危険
		label = this.getLabelPriority(groupStatus1, Messages.getString("critical"),PriorityColorConstant.COLOR_CRITICAL);
		this.m_checkStatusNormalCritical = this.getCheckStatusNormal(groupStatus1);
		WidgetTestUtil.setTestId(this, "normalcritical", m_checkStatusNormalCritical);
		this.m_checkStatusNormalCritical.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabledForStatusDuration();
			}
		});

		// 空白
		label = new Label(groupStatus1, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space5", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_BLANK;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);


		// ステータス 重要度：不明
		label = this.getLabelPriority(groupStatus1, Messages.getString("unknown"),PriorityColorConstant.COLOR_UNKNOWN);
		this.m_checkStatusNormalUnknown = this.getCheckStatusNormal(groupStatus1);
		WidgetTestUtil.setTestId(this, "normalunknown", m_checkStatusNormalUnknown);
		this.m_checkStatusNormalUnknown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabledForStatusDuration();
			}
		});

		// 空白
		label = new Label(groupStatus1, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space6", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_BLANK;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 空行
		label = new Label(groupStatus1, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space7", label);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * ステータス情報の存続期間（分）
		 */
		// ラベル
		label = new Label(groupStatus1, SWT.NONE);
		WidgetTestUtil.setTestId(this, "validperiod", label);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("notify.status.valid.period") + " : ");
		// コンボボックス
		this.m_comboStatusValidPeriod = new Combo(groupStatus1, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "validpriod", m_comboStatusValidPeriod);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboStatusValidPeriod.setLayoutData(gridData);
		this.m_comboStatusValidPeriod.add(StatusValidPeriodMessage.STRING_UNLIMITED);
		this.m_comboStatusValidPeriod.add(StatusValidPeriodMessage.STRING_MIN_10);
		this.m_comboStatusValidPeriod.add(StatusValidPeriodMessage.STRING_MIN_20);
		this.m_comboStatusValidPeriod.add(StatusValidPeriodMessage.STRING_MIN_30);
		this.m_comboStatusValidPeriod.add(StatusValidPeriodMessage.STRING_HOUR_1);
		this.m_comboStatusValidPeriod.add(StatusValidPeriodMessage.STRING_HOUR_3);
		this.m_comboStatusValidPeriod.add(StatusValidPeriodMessage.STRING_HOUR_6);
		this.m_comboStatusValidPeriod.add(StatusValidPeriodMessage.STRING_HOUR_12);
		this.m_comboStatusValidPeriod.add(StatusValidPeriodMessage.STRING_DAY_1);
		this.m_comboStatusValidPeriod.setText(StatusValidPeriodMessage.STRING_MIN_10);

		// 空白
		label = new Label(groupStatus1, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space8", label);
		gridData = new GridData();
		gridData.horizontalSpan = 9;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// ラベル
		label = new Label(groupStatus1, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space9", label);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("notify.status.invalid.period.treatment") + " : ");

		/*
		 * 中グループ
		 */
		// ステータス情報有効期間経過後の扱い通知グループ
		Group groupStatus2 = new Group(groupStatus1, SWT.NONE);
		WidgetTestUtil.setTestId(this, "status2", groupStatus2);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupStatus2.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupStatus2.setLayoutData(gridData);

		// 情報を削除する
		this.m_radioStatusDelete = new Button(groupStatus2, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "delete", m_radioStatusDelete);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		this.m_radioStatusDelete.setLayoutData(gridData);
		this.m_radioStatusDelete.setText(Messages.getString("notify.status.invalid.period.delete"));

		// 更新されていない旨のメッセージに置換える。
		this.m_radioStatusUpdate = new Button(groupStatus2, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "update", m_radioStatusUpdate);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_radioStatusUpdate.setLayoutData(gridData);
		this.m_radioStatusUpdate.setText(Messages.getString("notify.status.invalid.period.updatet"));
		this.m_radioStatusUpdate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_comboStatusPriority.setEnabled(m_radioStatusUpdate.getSelection());
			}
		});

		/*
		 * ステータス情報 重要度
		 */
		// ラベル
		label = new Label(groupStatus2, SWT.NONE);
		WidgetTestUtil.setTestId(this, "priority2", label);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("priority") + " : ");
		// コンボボックス
		this.m_comboStatusPriority = new Combo(groupStatus2, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "priority", m_comboStatusPriority);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboStatusPriority.setLayoutData(gridData);
		this.m_comboStatusPriority.add(PriorityMessage.STRING_CRITICAL);
		this.m_comboStatusPriority.add(PriorityMessage.STRING_WARNING);
		this.m_comboStatusPriority.add(PriorityMessage.STRING_INFO);
		this.m_comboStatusPriority.add(PriorityMessage.STRING_UNKNOWN);
		this.m_comboStatusPriority.setText(PriorityMessage.STRING_WARNING);
		// 空白
		label = new Label(groupStatus2, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space10", label);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
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
	public NotifyInfoInputData getInputData() {
		return this.inputData;
	}

	/**
	 * 引数で指定された通知情報の値を、各項目に設定します。
	 *
	 * @param notify 設定値として用いる通知情報
	 */
	@Override
	protected void setInputData(NotifyInfoInputData notify) {
		super.setInputData(notify);

		// コマンド情報
		StatusNotifyDetailInfoResponse info = notify.getNotifyStatusInfo();
		if (info != null) {
			this.setInputData(info);
		} else {
			// 新規追加の場合
			this.m_radioStatusDelete.setSelection(true);
		}
		this.m_comboStatusPriority.setEnabled(m_radioStatusUpdate.getSelection());
	}

	private void setInputData(StatusNotifyDetailInfoResponse status) {
		if (status.getStatusValidPeriod() != null) {
			this.m_comboStatusValidPeriod.setText(StatusValidPeriodMessage.typeToString(status.getStatusValidPeriod().intValue()));
		}
		if (status.getStatusInvalidFlg() != null && status.getStatusInvalidFlg() == StatusInvalidFlgEnum.DELETE) {
			this.m_radioStatusDelete.setSelection(true);
		} else {
			this.m_radioStatusUpdate.setSelection(true);
		}
		this.m_comboStatusPriority.setText(PriorityMessage.enumToString(status.getStatusUpdatePriority(), StatusUpdatePriorityEnum.class));

		Button[] checkStatusNormals = new Button[] {
				this.m_checkStatusNormalInfo,
				this.m_checkStatusNormalWarning,
				this.m_checkStatusNormalCritical,
				this.m_checkStatusNormalUnknown
		};
		Boolean[] validFlgs = getValidFlgs(status);
		for (int i = 0; i < validFlgs.length; i++) {
			boolean valid = validFlgs[i].booleanValue();
			checkStatusNormals[i].setSelection(valid);
			WidgetTestUtil.setTestId(this, "checkStatusNormals" + i, checkStatusNormals[i]);
		}

		this.m_comboStatusPriority.setEnabled(m_radioStatusUpdate.getSelection());
	}


	/**
	 * 引数で指定された通知ステータス情報の値を、各項目に設定します。
	 *
	 * @param info 設定値として用いる通知ステータス情報
	 * @param checkStatusNormal 通知チェックボックス
	 * @param checkStatusInhibition 抑制チェックボックス
	 */
	protected void setInputDataForStatus(StatusNotifyDetailInfoResponse info,
			Button checkStatusNormal,
			Button checkStatusInhibition
			) {
	}

	/**
	 * 入力値を設定した通知情報を返します。<BR>
	 * 入力値チェックを行い、不正な場合は<code>null</code>を返します。
	 *
	 * @return 通知情報
	 *
	 * @see #createInputDataForStatus(ArrayList, int, Button, Button)
	 */
	@Override
	protected NotifyInfoInputData createInputData() {
		NotifyInfoInputData info = super.createInputData();

		// 通知タイプの設定
		info.setNotifyType(TYPE_STATUS);

		// イベント情報
		StatusNotifyDetailInfoResponse status = createNotifyInfoDetail();
		info.setNotifyStatusInfo(status);

		return info;
	}

	private StatusNotifyDetailInfoResponse createNotifyInfoDetail() {
		StatusNotifyDetailInfoResponse status = new StatusNotifyDetailInfoResponse();

		status.setStatusInvalidFlg(StatusInvalidFlgEnum.DELETE);
		status.setStatusUpdatePriority(StatusUpdatePriorityEnum.WARNING);
		status.setStatusValidPeriod(StatusValidPeriodConstant.TYPE_MIN_10);

		// ステータス通知
		status.setInfoValidFlg(m_checkStatusNormalInfo.getSelection());
		status.setWarnValidFlg(m_checkStatusNormalWarning.getSelection());
		status.setCriticalValidFlg(m_checkStatusNormalCritical.getSelection());
		status.setUnknownValidFlg(m_checkStatusNormalUnknown.getSelection());

		// ステータス通知の共通内容（重要度に関係なく全て同じものを設定する）
		// ステータス情報の存続期間
		if (isNotNullAndBlank(this.m_comboStatusValidPeriod.getText())) {
			status.setStatusValidPeriod(StatusValidPeriodMessage.stringToType(this.m_comboStatusValidPeriod.getText()));
		}

		// 存続期間経過後の処理
		if (this.m_radioStatusDelete.getSelection()) {
			status.setStatusInvalidFlg(StatusInvalidFlgEnum.DELETE);
		} else {
			status.setStatusInvalidFlg(StatusInvalidFlgEnum.UPDATE);
		}

		// 更新されていない場合に通知する際の重要度
		if (isNotNullAndBlank(this.m_comboStatusPriority.getText())) {
			status.setStatusUpdatePriority(PriorityMessage.stringToEnum(this.m_comboStatusPriority.getText(), StatusUpdatePriorityEnum.class));
		}

		return status;
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

		NotifyInfoInputData info = this.getInputData();
		if(info != null){
			if (!this.updateFlg) {
				// 作成の場合
				result = new AddNotify().addStatusNotify(managerName, info);
			}
			else{
				// 変更の場合
				result = new ModifyNotify().modifyStatusNotify(managerName, info);
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
	 * コンポジットの選択可/不可を設定します。
	 *
	 * @param enable 選択可の場合、<code> true </code>
	 */
	@Override
	protected void setEnabled(boolean enable) {

		super.m_notifyBasic.setEnabled(enable);
		super.m_notifyInhibition.setEnabled(enable);
		this.setEnabledForStatuses(enable);
		this.setEnabledForStatusDuration();

	}

	/**
	 * ステータスの通知関連コンポジットの選択可/不可を設定します。
	 *
	 * @param enable 選択可の場合、<code> true </code>
	 *
	 * @see #setEnabledForStatuses(boolean, Button, Button)
	 */
	protected void setEnabledForStatuses(boolean enable) {
		// 通知チェックボックス
		this.m_checkStatusNormalInfo.setEnabled(enable);
		this.m_checkStatusNormalWarning.setEnabled(enable);
		this.m_checkStatusNormalCritical.setEnabled(enable);
		this.m_checkStatusNormalUnknown.setEnabled(enable);
	}

	/**
	 * ステータスの通知情報の選択可/不可を設定します。
	 *
	 */
	protected void setEnabledForStatusDuration() {

		boolean enable = false;

		// 全重要度の「通知」のチェックボックスのチェック内容と表示/非表示を確認する。
		if ((this.m_checkStatusNormalInfo.getSelection() && this.m_checkStatusNormalInfo.getEnabled()) ||
				(this.m_checkStatusNormalWarning.getSelection() && this.m_checkStatusNormalWarning.getEnabled()) ||
				(this.m_checkStatusNormalCritical.getSelection() && this.m_checkStatusNormalCritical.getEnabled()) ||
				(this.m_checkStatusNormalUnknown.getSelection() && this.m_checkStatusNormalUnknown.getEnabled())) {

			enable = true;
		}
		else {
			enable = false;
		}

		this.m_comboStatusValidPeriod.setEnabled(enable);
		this.m_radioStatusDelete.setEnabled(enable);
		this.m_radioStatusUpdate.setEnabled(enable);
		if (enable) {
			this.m_comboStatusPriority.setEnabled(this.m_radioStatusUpdate.getSelection());
		} else {
			this.m_comboStatusPriority.setEnabled(enable);
		}

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
			this.createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("cancel"), false);
		}
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
	 * ステータスの通知のチェックボックスを返します。
	 *
	 * @param parent 親のコンポジット
	 * @return 生成されたチェックボックス
	 */
	private Button getCheckStatusNormal(Composite parent) {
		// チェックボックス（通知する）
		Button notifyStatusCreateNormalCheckbox = new Button(parent, SWT.CHECK);
		GridData gridData = new GridData();
		gridData.horizontalSpan = WIDTH_CHECK;
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		notifyStatusCreateNormalCheckbox.setLayoutData(gridData);

		return notifyStatusCreateNormalCheckbox;
	}

	private Boolean[] getValidFlgs(StatusNotifyDetailInfoResponse info) {
		Boolean[] validFlgs = new Boolean[] {
				info.getInfoValidFlg(),
				info.getWarnValidFlg(),
				info.getCriticalValidFlg(),
				info.getUnknownValidFlg()
		};
		return validFlgs;
	}
}
