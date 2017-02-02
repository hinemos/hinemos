/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.clustercontrol.bean.EndStatusColorConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.notify.composite.NotifyIdListComposite;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.infra.InfraManagementInfo;

/**
 * 通知先の指定タブ用のコンポジットクラスです。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class InfraNoticeComposite extends Composite {
	/** 開始重要度用コンボボックス */
	private Combo m_startPriority = null;
	/** 実行正常重要度用コンボボックス */
	private Combo m_runNormalPriority = null;
	/** 実行異常重要度用コンボボックス */
	private Combo m_runAbnormalPriority = null;
	/** チェック正常重要度用コンボボックス */
	private Combo m_checkNormalPriority = null;
	/** チェック異常重要度用コンボボックス */
	private Combo m_checkAbnormalPriority = null;
	/** 通知ID */
	private NotifyIdListComposite m_notifyId = null;
	/** マネージャ名 */
	private String m_managerName = null;

	/**
	 * コンストラクタ
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param managerName マネージャ名
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public InfraNoticeComposite(Composite parent, int style, String managerName) {
		super(parent, style);
		this.m_managerName = managerName;
		initialize();
	}

	/**
	 * コンポジットを構築します。
	 */
	private void initialize() {
		this.setLayout(new GridLayout(3, true));

		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		//タイトル row
		Label dummy = new Label(this, SWT.NONE);
		dummy.setVisible( false );

		Label importanceDegreeTitle = new Label(this, SWT.LEFT);
		importanceDegreeTitle.setText(Messages.getString("priority"));
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		importanceDegreeTitle.setLayoutData(gridData);

		//開始
		Label startTitle = new Label(this, SWT.CENTER);
		startTitle.setText(EndStatusMessage.STRING_BEGINNING + " : ");
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL);
		startTitle.setLayoutData(gridData);

		m_startPriority = new Combo(this, SWT.CENTER | SWT.READ_ONLY);
		m_startPriority.add(PriorityMessage.STRING_INFO);
		m_startPriority.add(PriorityMessage.STRING_WARNING);
		m_startPriority.add(PriorityMessage.STRING_CRITICAL);
		m_startPriority.add(PriorityMessage.STRING_UNKNOWN);
		m_startPriority.add(PriorityMessage.STRING_NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		m_startPriority.setLayoutData(gridData);

		//実行正常
		Label e_normalTitle = new Label(this, SWT.CENTER);
		e_normalTitle.setText(Messages.getString("infra.run.normal") + " : ");
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL);
		e_normalTitle.setLayoutData(gridData);
		e_normalTitle.setBackground(EndStatusColorConstant.COLOR_NORMAL);

		m_runNormalPriority = new Combo(this, SWT.CENTER | SWT.READ_ONLY);
		m_runNormalPriority.add(PriorityMessage.STRING_INFO);
		m_runNormalPriority.add(PriorityMessage.STRING_WARNING);
		m_runNormalPriority.add(PriorityMessage.STRING_CRITICAL);
		m_runNormalPriority.add(PriorityMessage.STRING_UNKNOWN);
		m_runNormalPriority.add(PriorityMessage.STRING_NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		m_runNormalPriority.setLayoutData(gridData);

		//実行異常
		Label e_abnormalTitle = new Label(this, SWT.CENTER);
		e_abnormalTitle.setText(Messages.getString("infra.run.abnormal") + " : ");
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL);
		e_abnormalTitle.setLayoutData(gridData);
		e_abnormalTitle.setBackground(EndStatusColorConstant.COLOR_ABNORMAL);

		m_runAbnormalPriority = new Combo(this, SWT.CENTER | SWT.READ_ONLY);
		m_runAbnormalPriority.add(PriorityMessage.STRING_INFO);
		m_runAbnormalPriority.add(PriorityMessage.STRING_WARNING);
		m_runAbnormalPriority.add(PriorityMessage.STRING_CRITICAL);
		m_runAbnormalPriority.add(PriorityMessage.STRING_UNKNOWN);
		m_runAbnormalPriority.add(PriorityMessage.STRING_NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		m_runAbnormalPriority.setLayoutData(gridData);

		//チェック正常
		Label c_normalTitle = new Label(this, SWT.CENTER);
		c_normalTitle.setText(Messages.getString("infra.check.normal") + " : ");
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL);
		c_normalTitle.setLayoutData(gridData);
		c_normalTitle.setBackground(EndStatusColorConstant.COLOR_NORMAL);

		m_checkNormalPriority = new Combo(this, SWT.CENTER | SWT.READ_ONLY);
		m_checkNormalPriority.add(PriorityMessage.STRING_INFO);
		m_checkNormalPriority.add(PriorityMessage.STRING_WARNING);
		m_checkNormalPriority.add(PriorityMessage.STRING_CRITICAL);
		m_checkNormalPriority.add(PriorityMessage.STRING_UNKNOWN);
		m_checkNormalPriority.add(PriorityMessage.STRING_NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		m_checkNormalPriority.setLayoutData(gridData);

		//チェック異常
		Label c_abnormalTitle = new Label(this, SWT.CENTER);
		c_abnormalTitle.setText(Messages.getString("infra.check.abnormal") + " : ");
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL);
		c_abnormalTitle.setLayoutData(gridData);
		c_abnormalTitle.setBackground(EndStatusColorConstant.COLOR_ABNORMAL);

		m_checkAbnormalPriority = new Combo(this, SWT.CENTER | SWT.READ_ONLY);
		m_checkAbnormalPriority.add(PriorityMessage.STRING_INFO);
		m_checkAbnormalPriority.add(PriorityMessage.STRING_WARNING);
		m_checkAbnormalPriority.add(PriorityMessage.STRING_CRITICAL);
		m_checkAbnormalPriority.add(PriorityMessage.STRING_UNKNOWN);
		m_checkAbnormalPriority.add(PriorityMessage.STRING_NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		m_checkAbnormalPriority.setLayoutData(gridData);

		//通知ID
		Label notifyIdTitle = new Label(this, SWT.CENTER);
		notifyIdTitle.setText(Messages.getString("notify.id") + " : ");
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL);
		notifyIdTitle.setLayoutData(gridData);

		m_notifyId = new NotifyIdListComposite(this, SWT.CENTER, false);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		m_notifyId.setLayoutData(gridData);
		m_notifyId.setManagerName(this.m_managerName);
		setNotificationsInfo();
	}

	/**
	 * モジュール通知情報をコンポジットに反映します。
	 *
	 * @see com.clustercontrol.infra.bean
	 */
	public void setNotificationsInfo() {
		// 初期値
		setSelectPriority(m_startPriority, PriorityConstant.TYPE_INFO);
		setSelectPriority(m_checkAbnormalPriority, PriorityConstant.TYPE_CRITICAL);
		setSelectPriority(m_runNormalPriority,PriorityConstant.TYPE_INFO);
		setSelectPriority(m_runAbnormalPriority, PriorityConstant.TYPE_CRITICAL);
		setSelectPriority(m_checkNormalPriority, PriorityConstant.TYPE_INFO);
	}

	public void setNotificationsInfo(InfraManagementInfo info) {
		if (info != null) {
			setSelectPriority(m_startPriority, info.getStartPriority());
			setSelectPriority(m_checkAbnormalPriority, info.getAbnormalPriorityCheck());
			setSelectPriority(m_runNormalPriority, info.getNormalPriorityRun());
			setSelectPriority(m_runAbnormalPriority, info.getAbnormalPriorityRun());
			setSelectPriority(m_checkNormalPriority, info.getNormalPriorityCheck());

			if (info.getNotifyRelationList() != null) {
				m_notifyId.setNotify(info.getNotifyRelationList());
			}
		}
	}
	
	public void setOwnerRoleId (String ownerRoleId) {
		m_notifyId.setOwnerRoleId(ownerRoleId, true);
	}

	public int getAbnormalPriorityCheck() {
		return getSelectPriority(m_checkAbnormalPriority);
	}
	public int getNormalPriorityCheck() {
		return getSelectPriority(m_checkNormalPriority);
	}
	public int getAbnormalPriorityRun() {
		return getSelectPriority(m_runAbnormalPriority);
	}
	public int getNormalPriorityRun() {
		return getSelectPriority(m_runNormalPriority);
	}
	public int getStartPriority() {
		return getSelectPriority(m_startPriority);
	}
	public String getManagerName() {
		return m_managerName;
	}
	public void setManagerName(String m_managerName) {
		this.m_managerName = m_managerName;
		this.m_notifyId.setManagerName(m_managerName);
	}

	/**
	 * 指定した重要度に該当する重要度用コンボボックスの項目を選択します。
	 *
	 * @param combo 重要度用コンボボックスのインスタンス
	 * @param priority 重要度
	 *
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public void setSelectPriority(Combo combo, int priority) {
		String select = "";

		if (priority == PriorityConstant.TYPE_CRITICAL) {
			select = PriorityMessage.STRING_CRITICAL;
		} else if (priority == PriorityConstant.TYPE_UNKNOWN) {
			select = PriorityMessage.STRING_UNKNOWN;
		} else if (priority == PriorityConstant.TYPE_WARNING) {
			select = PriorityMessage.STRING_WARNING;
		} else if (priority == PriorityConstant.TYPE_INFO) {
			select = PriorityMessage.STRING_INFO;
		} else if (priority == PriorityConstant.TYPE_NONE) {
			select = PriorityMessage.STRING_NONE;
		}

		combo.select(0);
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (select.equals(combo.getItem(i))) {
				combo.select(i);
				break;
			}
		}
	}

	/**
	 * 重要度用コンボボックスにて選択している重要度を取得します。
	 *
	 * @param combo 重要度用コンボボックスのインスタンス
	 * @return 重要度
	 *
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public int getSelectPriority(Combo combo) {
		String select = combo.getText();

		if (select.equals(PriorityMessage.STRING_CRITICAL)) {
			return PriorityConstant.TYPE_CRITICAL;
		} else if (select.equals(PriorityMessage.STRING_WARNING)) {
			return PriorityConstant.TYPE_WARNING;
		} else if (select.equals(PriorityMessage.STRING_INFO)) {
			return PriorityConstant.TYPE_INFO;
		} else if (select.equals(PriorityMessage.STRING_UNKNOWN)) {
			return PriorityConstant.TYPE_UNKNOWN;
		} else if (select.equals(PriorityMessage.STRING_NONE)) {
			return PriorityConstant.TYPE_NONE;
		}

		return -1;
	}

	public NotifyIdListComposite getNotifyId() {
		return m_notifyId;
	}

	public void setNotifyId(NotifyIdListComposite notifyId) {
		this.m_notifyId = notifyId;
	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		// super.setEnabled(enabled); // スクロールバーを動かせるように、ここはコメントアウト
		m_runNormalPriority.setEnabled(enabled);
		m_runAbnormalPriority.setEnabled(enabled);
		m_checkNormalPriority.setEnabled(enabled);
		m_checkAbnormalPriority.setEnabled(enabled);
		m_notifyId.setButtonEnabled(enabled);
	}
}
