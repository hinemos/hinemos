/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.SdmlControlSettingInfoResponse;
import org.openapitools.client.model.SdmlControlSettingInfoResponse.EarlyStopNotifyPriorityEnum;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.sdml.util.SdmlClientConstant;
import com.clustercontrol.sdml.util.SdmlUiUtil;
import com.clustercontrol.util.Messages;

public class SdmlRunningTimeComposite extends Composite {
	/** 時間(秒) */
	private Text secondText = null;
	/** 通知の重要度 */
	private Combo notifyPriorityCombo = null;

	/** デフォルト値 時間(秒) */
	private static final String DEFAULT_SECOND = "30";
	/** デフォルト値 通知の重要度 */
	private static final String DEFAULT_PRIORITY_SELECT = PriorityMessage.STRING_WARNING;

	/**
	 * コンストラクタ
	 * 
	 * @param parent
	 * @param style
	 */
	public SdmlRunningTimeComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
		Label label = null;
		GridData gridData = null;
		GridLayout layout = null;

		this.setLayout(new GridLayout(SdmlClientConstant.DIALOG_WIDTH, true));

		// 起動から停止までの時間が短い場合の通知 グループ
		Group groupEarlyStopNotify = new Group(this, SWT.NONE);
		layout = new GridLayout(SdmlClientConstant.DIALOG_WIDTH, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		groupEarlyStopNotify.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupEarlyStopNotify.setLayoutData(gridData);
		groupEarlyStopNotify.setText(Messages.getString("sdml.notify.early.stop"));

		// 時間(ラベル)
		label = new Label(groupEarlyStopNotify, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("sdml.time") + " : ");
		// 時間(テキスト)
		this.secondText = new Text(groupEarlyStopNotify, SWT.BORDER);
		this.secondText.setText(Messages.getString("second"));
		this.secondText.addVerifyListener(
				new NumberVerifyListener(0, DataRangeConstant.SMALLINT_HIGH));
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.FORM_WIDTH_1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.secondText.setLayoutData(gridData);
		this.secondText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		// 時間(秒)
		label = new Label(groupEarlyStopNotify, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.FORM_WIDTH_1_SPACE;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("second"));

		// 通知の重要度(ラベル)
		label = new Label(groupEarlyStopNotify, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("sdml.notify.priority") + " : ");
		// 通知の重要度(テキスト)
		this.notifyPriorityCombo = new Combo(groupEarlyStopNotify, SWT.CENTER | SWT.READ_ONLY);
		for (String priority : SdmlUiUtil.PRIORITY_LIST) {
			this.notifyPriorityCombo.add(priority);
		}
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.FORM_WIDTH;
		this.notifyPriorityCombo.setLayoutData(gridData);

	}

	/**
	 * 引数で受け取った情報から各項目に設定します
	 * 
	 * @param info
	 */
	public void setInputData(SdmlControlSettingInfoResponse info) {
		if (info == null) {
			// デフォルト値
			secondText.setText(DEFAULT_SECOND);
			SdmlUiUtil.setSelectPriority(notifyPriorityCombo, DEFAULT_PRIORITY_SELECT);
		} else {
			if (info.getEarlyStopThresholdSecond() != null) {
				secondText.setText(info.getEarlyStopThresholdSecond().toString());
			}
			if (info.getEarlyStopNotifyPriority() != null) {
				String select = PriorityMessage.enumToString(info.getEarlyStopNotifyPriority(),
						EarlyStopNotifyPriorityEnum.class);
				SdmlUiUtil.setSelectPriority(notifyPriorityCombo, select);
			}
		}
		update();
	}

	/**
	 * 更新処理
	 */
	@Override
	public void update() {
		SdmlUiUtil.setColorRequired(secondText);
	}

	/**
	 * 引数で受け取った情報に入力値を設定します
	 * 
	 * @param info
	 */
	public void createInputData(SdmlControlSettingInfoResponse info) {
		if (info != null) {
			if (secondText.getText() != null && !secondText.getText().isEmpty()) {
				info.setEarlyStopThresholdSecond(Integer.parseInt(secondText.getText()));
			}
			info.setEarlyStopNotifyPriority(
					PriorityMessage.stringToEnum(notifyPriorityCombo.getText(), EarlyStopNotifyPriorityEnum.class));
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		secondText.setEnabled(enabled);
		notifyPriorityCombo.setEnabled(enabled);
	}
}
