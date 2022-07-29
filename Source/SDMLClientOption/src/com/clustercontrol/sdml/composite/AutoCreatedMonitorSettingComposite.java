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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.openapitools.client.model.NotifyRelationInfoResponse;
import org.openapitools.client.model.SdmlControlSettingInfoResponse;

import com.clustercontrol.calendar.composite.CalendarIdListComposite;
import com.clustercontrol.notify.composite.NotifyIdListComposite;
import com.clustercontrol.sdml.util.SdmlClientConstant;
import com.clustercontrol.util.Messages;

public class AutoCreatedMonitorSettingComposite extends Composite {
	/** マネージャ */
	private String managerName = null;
	/** オーナーロールID */
	private String ownerRoleId = null;
	/** アプリケーション停止時に監視設定を削除する */
	private Button autoDeleteValidButton = null;
	/** カレンダID */
	private CalendarIdListComposite calendarComposite = null;
	/** 種別共通通知ID */
	private NotifyIdListComposite commonNotifySettingComposite = null;
	/** 個別通知設定 */
	private IndividualNotifySettingListComposite individualNotifySettingComposite = null;

	/**
	 * コンストラクタ
	 * 
	 * @param parent
	 * @param style
	 * @param managerName
	 */
	public AutoCreatedMonitorSettingComposite(Composite parent, int style, String managerName, String ownerRoleId) {
		super(parent, style);
		this.managerName = managerName;
		this.ownerRoleId = ownerRoleId;
		initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
		GridData gridData = null;
		GridLayout layout = null;

		this.setLayout(new GridLayout(SdmlClientConstant.DIALOG_WIDTH, true));

		// 監視 グループ
		Group groupMonitor = new Group(this, SWT.NONE);
		layout = new GridLayout(SdmlClientConstant.DIALOG_WIDTH, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		groupMonitor.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupMonitor.setLayoutData(gridData);
		groupMonitor.setText(Messages.getString("sdml.monitor"));

		// 監視設定自動削除フラグ
		this.autoDeleteValidButton = new Button(groupMonitor, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.autoDeleteValidButton.setLayoutData(gridData);
		this.autoDeleteValidButton.setText(Messages.getString("sdml.auto.monitor.delete.valid"));

		// TODO ver7.0.1では暫定対処として常に削除する設定とする
		autoDeleteValidButton.setSelection(true);
		autoDeleteValidButton.setEnabled(false);

		// カレンダID（コンボボックス）
		this.calendarComposite = new CalendarIdListComposite(groupMonitor, SWT.NONE, true);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calendarComposite.setLayoutData(gridData);

		// 種別共通通知ID Composite
		this.commonNotifySettingComposite = new NotifyIdListComposite(groupMonitor, SWT.NONE,
				Messages.getString("sdml.auto.monitor.common.notify.id"));
		this.commonNotifySettingComposite.setManagerName(this.managerName);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.commonNotifySettingComposite.setLayoutData(gridData);

		// 個別通知設定 グループ
		Group groupIndividualNotify = new Group(groupMonitor, SWT.NONE);
		layout = new GridLayout(SdmlClientConstant.DIALOG_WIDTH, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		groupIndividualNotify.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupIndividualNotify.setLayoutData(gridData);
		groupIndividualNotify.setText(Messages.getString("sdml.auto.monitor.indivisual.notify"));
		// テーブル
		this.individualNotifySettingComposite = new IndividualNotifySettingListComposite(groupIndividualNotify,
				SWT.NONE, this.managerName, this.ownerRoleId);
		gridData = new GridData();
		gridData.horizontalSpan = SdmlClientConstant.DIALOG_WIDTH;
		this.individualNotifySettingComposite.setLayoutData(gridData);
	}

	/**
	 * 引数で受け取った情報から各項目に設定します
	 * 
	 * @param info
	 */
	public void setInputData(SdmlControlSettingInfoResponse info) {
		if (info == null) {
			// デフォルト値
			// autoDeleteValidButton.setSelection(false);

		} else {
			// autoDeleteValidButton.setSelection(info.getAutoMonitorDeleteFlg());
			if (info.getAutoMonitorCalendarId() != null) {
				calendarComposite.setText(info.getAutoMonitorCalendarId());
			}
			// 種別共通通知
			if (info.getAutoMonitorCommonNotifyRelationList() != null
					&& info.getAutoMonitorCommonNotifyRelationList().size() > 0) {
				List<NotifyRelationInfoResponse> notifyList = new ArrayList<>();
				for (NotifyRelationInfoResponse src : info.getAutoMonitorCommonNotifyRelationList()) {
					NotifyRelationInfoResponse dst = new NotifyRelationInfoResponse();
					dst.setNotifyId(src.getNotifyId());
					dst.setNotifyType(src.getNotifyType());
					notifyList.add(dst);
				}
				commonNotifySettingComposite.setNotify(notifyList);
			}
		}
		// 子Compositeに情報を設定
		individualNotifySettingComposite.setInputData(info);
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
			info.setAutoMonitorDeleteFlg(autoDeleteValidButton.getSelection());
			if (calendarComposite.getText().length() > 0) {
				info.setAutoMonitorCalendarId(calendarComposite.getText());
			} else {
				info.setAutoMonitorCalendarId(null);
			}
			// 種別共通通知
			if (commonNotifySettingComposite.getNotify() != null) {
				List<NotifyRelationInfoResponse> notifyRelationList = new ArrayList<>();
				for (NotifyRelationInfoResponse src : commonNotifySettingComposite.getNotify()) {
					NotifyRelationInfoResponse dst = new NotifyRelationInfoResponse();
					dst.setNotifyId(src.getNotifyId());
					dst.setNotifyType(src.getNotifyType());
					notifyRelationList.add(dst);
				}
				info.setAutoMonitorCommonNotifyRelationList(notifyRelationList);
			}
		}
		// 子Compositeから情報反映
		individualNotifySettingComposite.createInputData(info);
	}

	/**
	 * 子Compositeに情報を反映します
	 * 
	 * @param managerName
	 * @param ownerRoleId
	 */
	public void reflect(String managerName, String ownerRoleId) {
		this.managerName = managerName;
		calendarComposite.createCalIdCombo(managerName, ownerRoleId);
		commonNotifySettingComposite.setManagerName(managerName);
		commonNotifySettingComposite.setOwnerRoleId(ownerRoleId, false);
		individualNotifySettingComposite.reflect(managerName, ownerRoleId);
	}

	@Override
	public void setEnabled(boolean enabled) {
		// autoDeleteValidButton.setEnabled(enabled);
		calendarComposite.setEnabled(enabled);
		commonNotifySettingComposite.setEnabled(enabled);
	}
}
