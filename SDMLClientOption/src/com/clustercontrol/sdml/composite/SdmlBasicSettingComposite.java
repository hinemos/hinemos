/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.NotifyRelationInfoResponse;
import org.openapitools.client.model.SdmlControlSettingInfoResponse;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.notify.composite.NotifyInfoComposite;
import com.clustercontrol.sdml.util.SdmlClientConstant;
import com.clustercontrol.sdml.util.SdmlUiUtil;
import com.clustercontrol.util.Messages;

public class SdmlBasicSettingComposite extends Composite {
	/** マネージャ */
	private String managerName = null;
	/** ディレクトリ */
	private Text directoryText = null;
	/** ファイル名 */
	private Text filenameText = null;
	/** 収集 */
	private Button collectButton = null;
	/** 通知 */
	private NotifyInfoComposite notifyComposite;
	/** アクションの種別 (default: MODE_ADD) */
	private int mode = PropertyDefineConstant.MODE_ADD;

	/** デフォルト値 SDML制御ログ収集 */
	private static final boolean DEFAULT_COLLECT_FLG = true;

	/**
	 * コンストラクタ
	 * 
	 * @param parent
	 * @param style
	 * @param managerName
	 * @param mode
	 */
	public SdmlBasicSettingComposite(Composite parent, int style, String managerName, int mode) {
		super(parent, style);
		this.managerName = managerName;
		this.mode = mode;
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

		// SDML制御ログ グループ
		Group groupControlLog = new Group(this, SWT.NONE);
		layout = new GridLayout(SdmlClientConstant.DIALOG_WIDTH, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		groupControlLog.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupControlLog.setLayoutData(gridData);
		groupControlLog.setText(Messages.getString("sdml.control.log"));

		// ディレクトリ(ラベル)
		label = new Label(groupControlLog, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("sdml.directory") + " : ");
		// ディレクトリ(テキスト)
		this.directoryText = new Text(groupControlLog, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.FORM_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.directoryText.setLayoutData(gridData);
		this.directoryText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ファイル名(ラベル)
		label = new Label(groupControlLog, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("sdml.file.name") + " : ");
		// ファイル名(テキスト)
		this.filenameText = new Text(groupControlLog, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.FORM_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.filenameText.setLayoutData(gridData);
		this.filenameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 収集フラグ
		collectButton = new Button(groupControlLog, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		collectButton.setLayoutData(gridData);
		collectButton.setText(Messages.getString("sdml.collect"));

		// 通知 グループ
		Group groupNotifyAttribute = new Group(this, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		groupNotifyAttribute.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupNotifyAttribute.setLayoutData(gridData);
		groupNotifyAttribute.setText(Messages.getString("notify.attribute"));
		// 通知 Composite（アプリケーションつき）
		this.notifyComposite = new NotifyInfoComposite(groupNotifyAttribute, SWT.NONE);
		this.notifyComposite.setManagerName(this.managerName);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.notifyComposite.setLayoutData(gridData);

	}

	/**
	 * 引数で受け取った情報から各項目に設定します
	 * 
	 * @param info
	 */
	public void setInputData(SdmlControlSettingInfoResponse info) {
		if (info == null) {
			// デフォルト値
			collectButton.setSelection(DEFAULT_COLLECT_FLG);
		} else {
			if (info.getControlLogDirectory() != null) {
				directoryText.setText(info.getControlLogDirectory());
			}
			if (info.getControlLogFilename() != null) {
				filenameText.setText(info.getControlLogFilename());
			}
			collectButton.setSelection(info.getControlLogCollectFlg());

			// 通知
			if (info.getNotifyRelationList() != null && info.getNotifyRelationList().size() > 0) {
				List<NotifyRelationInfoResponse> notifyList = new ArrayList<>();
				for (NotifyRelationInfoResponse src : info.getNotifyRelationList()) {
					NotifyRelationInfoResponse dst = new NotifyRelationInfoResponse();
					dst.setNotifyId(src.getNotifyId());
					dst.setNotifyType(src.getNotifyType());
					notifyList.add(dst);
				}
				notifyComposite.setNotify(notifyList);
			}
			if (info.getApplication() != null) {
				this.notifyComposite.setApplication(info.getApplication());
				this.notifyComposite.update();
			}

			// 設定変更時は変更不可
			if (mode == PropertyDefineConstant.MODE_MODIFY) {
				directoryText.setEnabled(false);
				filenameText.setEnabled(false);
			}
		}
		update();
	}

	/**
	 * 更新処理
	 */
	@Override
	public void update() {
		SdmlUiUtil.setColorRequired(directoryText);
		SdmlUiUtil.setColorRequired(filenameText);
	}

	/**
	 * 引数で受け取った情報に入力値を設定します
	 * 
	 * @param info
	 */
	public void createInputData(SdmlControlSettingInfoResponse info) {
		if (info != null) {
			info.setControlLogDirectory(directoryText.getText());
			info.setControlLogFilename(filenameText.getText());
			info.setControlLogCollectFlg(collectButton.getSelection());

			// 通知
			if (this.notifyComposite.getNotify() != null) {
				List<NotifyRelationInfoResponse> notifyRelationList = new ArrayList<>();
				for (NotifyRelationInfoResponse src : this.notifyComposite.getNotify()) {
					NotifyRelationInfoResponse dst = new NotifyRelationInfoResponse();
					dst.setNotifyId(src.getNotifyId());
					dst.setNotifyType(src.getNotifyType());
					notifyRelationList.add(dst);
				}
				info.setNotifyRelationList(notifyRelationList);
			}
			info.setApplication(this.notifyComposite.getApplication());
		}
	}

	/**
	 * 子Compositeに情報を反映します
	 * 
	 * @param managerName
	 * @param ownerRoleId
	 */
	public void reflect(String managerName, String ownerRoleId) {
		this.managerName = managerName;
		this.notifyComposite.setManagerName(managerName);
		this.notifyComposite.setOwnerRoleId(ownerRoleId, false);
	}

	@Override
	public void setEnabled(boolean enabled) {
		directoryText.setEnabled(enabled);
		filenameText.setEnabled(enabled);
		collectButton.setEnabled(enabled);
		notifyComposite.setEnabled(enabled);
	}
}
