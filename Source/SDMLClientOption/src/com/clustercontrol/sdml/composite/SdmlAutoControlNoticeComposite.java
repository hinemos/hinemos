/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.openapitools.client.model.SdmlControlSettingInfoResponse;
import org.openapitools.client.model.SdmlControlSettingInfoResponse.AutoControlFailedPriorityEnum;
import org.openapitools.client.model.SdmlControlSettingInfoResponse.AutoCreateSuccessPriorityEnum;
import org.openapitools.client.model.SdmlControlSettingInfoResponse.AutoDisableSuccessPriorityEnum;
import org.openapitools.client.model.SdmlControlSettingInfoResponse.AutoEnableSuccessPriorityEnum;
import org.openapitools.client.model.SdmlControlSettingInfoResponse.AutoUpdateSuccessPriorityEnum;

import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.sdml.util.SdmlClientConstant;
import com.clustercontrol.sdml.util.SdmlUiUtil;
import com.clustercontrol.util.Messages;

public class SdmlAutoControlNoticeComposite extends Composite {
	/** 作成完了 重要度 */
	private Combo createFinishedPriorityCombo = null;
	/** 有効化完了 重要度 */
	private Combo enableFinishedPriorityCombo = null;
	/** 無効化完了 重要度 */
	private Combo disableFinishedPriorityCombo = null;
	/** 更新完了 重要度 */
	private Combo updateFinishedPriorityCombo = null;
	/** 異常時 重要度 */
	private Combo failedPriorityCombo = null;

	/** デフォルト値 作成完了 重要度 */
	private static final String DEFAULT_CREATE_FINISHED_SELECT = PriorityMessage.STRING_INFO;
	/** デフォルト値 有効化完了 重要度 */
	private static final String DEFAULT_ENABLE_FINISHED_SELECT = PriorityMessage.STRING_INFO;
	/** デフォルト値 無効化完了 重要度 */
	private static final String DEFAULT_DISABLE_FINISHED_SELECT = PriorityMessage.STRING_INFO;
	/** デフォルト値 更新完了 重要度 */
	private static final String DEFAULT_UPDATE_FINISHED_SELECT = PriorityMessage.STRING_INFO;
	/** デフォルト値 異常時 重要度 */
	private static final String DEFAULT_FAILED_SELECT = PriorityMessage.STRING_CRITICAL;

	/**
	 * コンストラクタ
	 * 
	 * @param parent
	 * @param style
	 */
	public SdmlAutoControlNoticeComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
		Label label = null;
		GridData gridData = null;

		this.setLayout(new GridLayout(SdmlClientConstant.DIALOG_WIDTH, true));

		// 重要度(タイトル)
		Label dummy = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.TITLE_WIDTH;
		dummy.setLayoutData(gridData);
		dummy.setVisible(false);
		label = new Label(this, SWT.LEFT);
		label.setText(Messages.getString("priority"));
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.FORM_WIDTH;
		label.setLayoutData(gridData);

		// 作成完了(ラベル)
		label = new Label(this, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("sdml.auto.create.finished") + " : ");
		// 作成完了(テキスト)
		this.createFinishedPriorityCombo = new Combo(this, SWT.CENTER | SWT.READ_ONLY);
		for (String priority : SdmlUiUtil.PRIORITY_LIST) {
			this.createFinishedPriorityCombo.add(priority);
		}
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.FORM_WIDTH;
		this.createFinishedPriorityCombo.setLayoutData(gridData);

		// 有効化完了(ラベル)
		label = new Label(this, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("sdml.auto.enable.finished") + " : ");
		// 有効化完了(テキスト)
		this.enableFinishedPriorityCombo = new Combo(this, SWT.CENTER | SWT.READ_ONLY);
		for (String priority : SdmlUiUtil.PRIORITY_LIST) {
			this.enableFinishedPriorityCombo.add(priority);
		}
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.FORM_WIDTH;
		this.enableFinishedPriorityCombo.setLayoutData(gridData);

		// 無効化完了(ラベル)
		label = new Label(this, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("sdml.auto.disable.finished") + " : ");
		// 無効化完了(テキスト)
		this.disableFinishedPriorityCombo = new Combo(this, SWT.CENTER | SWT.READ_ONLY);
		for (String priority : SdmlUiUtil.PRIORITY_LIST) {
			this.disableFinishedPriorityCombo.add(priority);
		}
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.FORM_WIDTH;
		this.disableFinishedPriorityCombo.setLayoutData(gridData);

		// 更新完了(ラベル)
		label = new Label(this, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("sdml.auto.update.finished") + " : ");
		// 更新完了(テキスト)
		this.updateFinishedPriorityCombo = new Combo(this, SWT.CENTER | SWT.READ_ONLY);
		for (String priority : SdmlUiUtil.PRIORITY_LIST) {
			this.updateFinishedPriorityCombo.add(priority);
		}
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.FORM_WIDTH;
		this.updateFinishedPriorityCombo.setLayoutData(gridData);

		// 異常時(ラベル)
		label = new Label(this, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("sdml.auto.control.failed") + " : ");
		// 異常時(テキスト)
		this.failedPriorityCombo = new Combo(this, SWT.CENTER | SWT.READ_ONLY);
		for (String priority : SdmlUiUtil.PRIORITY_LIST) {
			this.failedPriorityCombo.add(priority);
		}
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.FORM_WIDTH;
		this.failedPriorityCombo.setLayoutData(gridData);
	}

	/**
	 * 引数で受け取った情報から各項目に設定します
	 * 
	 * @param info
	 */
	public void setInputData(SdmlControlSettingInfoResponse info) {
		if (info == null) {
			// デフォルト値
			SdmlUiUtil.setSelectPriority(createFinishedPriorityCombo, DEFAULT_CREATE_FINISHED_SELECT);
			SdmlUiUtil.setSelectPriority(enableFinishedPriorityCombo, DEFAULT_ENABLE_FINISHED_SELECT);
			SdmlUiUtil.setSelectPriority(disableFinishedPriorityCombo, DEFAULT_DISABLE_FINISHED_SELECT);
			SdmlUiUtil.setSelectPriority(updateFinishedPriorityCombo, DEFAULT_UPDATE_FINISHED_SELECT);
			SdmlUiUtil.setSelectPriority(failedPriorityCombo, DEFAULT_FAILED_SELECT);
		} else {
			if (info.getAutoCreateSuccessPriority() != null) {
				String select = PriorityMessage.enumToString(info.getAutoCreateSuccessPriority(),
						AutoCreateSuccessPriorityEnum.class);
				SdmlUiUtil.setSelectPriority(createFinishedPriorityCombo, select);
			}
			if (info.getAutoEnableSuccessPriority() != null) {
				String select = PriorityMessage.enumToString(info.getAutoEnableSuccessPriority(),
						AutoEnableSuccessPriorityEnum.class);
				SdmlUiUtil.setSelectPriority(enableFinishedPriorityCombo, select);
			}
			if (info.getAutoDisableSuccessPriority() != null) {
				String select = PriorityMessage.enumToString(info.getAutoDisableSuccessPriority(),
						AutoDisableSuccessPriorityEnum.class);
				SdmlUiUtil.setSelectPriority(disableFinishedPriorityCombo, select);
			}
			if (info.getAutoUpdateSuccessPriority() != null) {
				String select = PriorityMessage.enumToString(info.getAutoUpdateSuccessPriority(),
						AutoUpdateSuccessPriorityEnum.class);
				SdmlUiUtil.setSelectPriority(updateFinishedPriorityCombo, select);
			}
			if (info.getAutoControlFailedPriority() != null) {
				String select = PriorityMessage.enumToString(info.getAutoControlFailedPriority(),
						AutoControlFailedPriorityEnum.class);
				SdmlUiUtil.setSelectPriority(failedPriorityCombo, select);
			}
		}
		update();
	}

	/**
	 * 更新処理
	 */
	@Override
	public void update() {
		// なし
	}

	/**
	 * 引数で受け取った情報に入力値を設定します
	 * 
	 * @param info
	 */
	public void createInputData(SdmlControlSettingInfoResponse info) {
		if (info != null) {
			info.setAutoCreateSuccessPriority(PriorityMessage.stringToEnum(createFinishedPriorityCombo.getText(),
					AutoCreateSuccessPriorityEnum.class));
			info.setAutoEnableSuccessPriority(PriorityMessage.stringToEnum(enableFinishedPriorityCombo.getText(),
					AutoEnableSuccessPriorityEnum.class));
			info.setAutoDisableSuccessPriority(PriorityMessage.stringToEnum(disableFinishedPriorityCombo.getText(),
					AutoDisableSuccessPriorityEnum.class));
			info.setAutoUpdateSuccessPriority(PriorityMessage.stringToEnum(updateFinishedPriorityCombo.getText(),
					AutoUpdateSuccessPriorityEnum.class));
			info.setAutoControlFailedPriority(
					PriorityMessage.stringToEnum(failedPriorityCombo.getText(), AutoControlFailedPriorityEnum.class));
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		createFinishedPriorityCombo.setEnabled(enabled);
		enableFinishedPriorityCombo.setEnabled(enabled);
		disableFinishedPriorityCombo.setEnabled(enabled);
		updateFinishedPriorityCombo.setEnabled(enabled);
		failedPriorityCombo.setEnabled(enabled);
	}
}
