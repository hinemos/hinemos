/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.openapitools.client.model.JobInfoResponse;

import com.clustercontrol.jobmanagement.util.JobInfoWrapper;

import com.clustercontrol.bean.EndStatusColorConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.notify.composite.NotifyIdListComposite;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 通知先の指定タブ用のコンポジットクラスです。
 *
 * @version 3.0.0
 * @since 2.0.0
 */
public class NotificationsComposite extends Composite {
	/** 正常重要度用コンボボックス */
	private Combo m_normalPriority = null;
	/** 警告重要度用コンボボックス */
	private Combo m_warningPriority = null;
	/** 異常重要度用コンボボックス */
	private Combo m_abnormalPriority = null;
	/** 開始重要度用コンボボックス */
	private Combo m_startPriority = null;
	/** 通知ID */
	private NotifyIdListComposite m_notifyId = null;
	private JobInfoWrapper m_jobInfo = null;

	/** マネージャ名 */
	private String managerName = null;

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
	public NotificationsComposite(Composite parent, int style, String managerName) {
		super(parent, style);
		this.managerName = managerName;
		initialize();
	}

	/**
	 * コンポジットを構築します。
	 */
	private void initialize() {

		this.setLayout(JobDialogUtil.getParentLayout());

		// Composite
		Composite notificationsComposite = new Composite(this, SWT.NONE);
		notificationsComposite.setLayout(new GridLayout(2, false));

		// dummy
		new Label(notificationsComposite, SWT.NONE);

		// 重要度（ラベル）
		Label importanceDegreeTitle = new Label(notificationsComposite, SWT.CENTER);
		importanceDegreeTitle.setText(Messages.getString("priority"));

		// 開始（ラベル）
		Label beginningTitle = new Label(notificationsComposite, SWT.CENTER);
		beginningTitle.setText(EndStatusMessage.STRING_BEGINNING + " : ");
		beginningTitle.setLayoutData(new GridData(60,
				SizeConstant.SIZE_LABEL_HEIGHT));

		// 開始（コンボ）
		this.m_startPriority = new Combo(notificationsComposite, SWT.CENTER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_startPriority", this.m_startPriority);
		this.m_startPriority.setLayoutData(new GridData(100,
				SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_startPriority.add(PriorityMessage.STRING_INFO);
		this.m_startPriority.add(PriorityMessage.STRING_WARNING);
		this.m_startPriority.add(PriorityMessage.STRING_CRITICAL);
		this.m_startPriority.add(PriorityMessage.STRING_UNKNOWN);
		this.m_startPriority.add(PriorityMessage.STRING_NONE);

		// 正常（ラベル）
		Label normalTitle = new Label(notificationsComposite, SWT.CENTER);
		normalTitle.setText(EndStatusMessage.STRING_NORMAL + " : ");
		normalTitle.setLayoutData(new GridData(60,
				SizeConstant.SIZE_LABEL_HEIGHT));
		normalTitle.setBackground(EndStatusColorConstant.COLOR_NORMAL);

		// 正常（コンボ）
		this.m_normalPriority = new Combo(notificationsComposite, SWT.CENTER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_normalPriority", this.m_normalPriority);
		this.m_normalPriority.setLayoutData(new GridData(100,
				SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_normalPriority.add(PriorityMessage.STRING_INFO);
		this.m_normalPriority.add(PriorityMessage.STRING_WARNING);
		this.m_normalPriority.add(PriorityMessage.STRING_CRITICAL);
		this.m_normalPriority.add(PriorityMessage.STRING_UNKNOWN);
		this.m_normalPriority.add(PriorityMessage.STRING_NONE);

		// 警告（ラベル）
		Label warningTitle = new Label(notificationsComposite, SWT.CENTER);
		warningTitle.setText(EndStatusMessage.STRING_WARNING + " : ");
		warningTitle.setLayoutData(new GridData(60,
				SizeConstant.SIZE_LABEL_HEIGHT));
		warningTitle.setBackground(EndStatusColorConstant.COLOR_WARNING);

		// 警告（コンボ）
		this.m_warningPriority = new Combo(notificationsComposite, SWT.CENTER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_warningPriority", this.m_warningPriority);
		this.m_warningPriority.setLayoutData(new GridData(100,
				SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_warningPriority.add(PriorityMessage.STRING_INFO);
		this.m_warningPriority.add(PriorityMessage.STRING_WARNING);
		this.m_warningPriority.add(PriorityMessage.STRING_CRITICAL);
		this.m_warningPriority.add(PriorityMessage.STRING_UNKNOWN);
		this.m_warningPriority.add(PriorityMessage.STRING_NONE);

		// 異常（ラベル）
		Label abnormalTitle = new Label(notificationsComposite, SWT.CENTER);
		abnormalTitle.setText(EndStatusMessage.STRING_ABNORMAL + " : ");
		abnormalTitle.setLayoutData(new GridData(60,
				SizeConstant.SIZE_LABEL_HEIGHT));
		abnormalTitle.setBackground(EndStatusColorConstant.COLOR_ABNORMAL);

		// 異常（コンボ）
		this.m_abnormalPriority = new Combo(notificationsComposite, SWT.CENTER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_abnormalPriority", this.m_abnormalPriority);
		this.m_abnormalPriority.setLayoutData(new GridData(100,
				SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_abnormalPriority.add(PriorityMessage.STRING_INFO);
		this.m_abnormalPriority.add(PriorityMessage.STRING_WARNING);
		this.m_abnormalPriority.add(PriorityMessage.STRING_CRITICAL);
		this.m_abnormalPriority.add(PriorityMessage.STRING_UNKNOWN);
		this.m_abnormalPriority.add(PriorityMessage.STRING_NONE);

		// 通知ID（ラベル）
		Label notifyIdTitle = new Label(notificationsComposite, SWT.CENTER);
		notifyIdTitle.setText(Messages.getString("notify.id") + " : ");
		notifyIdTitle.setLayoutData(new GridData(100,
				SizeConstant.SIZE_LABEL_HEIGHT));

		// 通知ID（NotifiyIdListComposite）
		this.m_notifyId = new NotifyIdListComposite(notificationsComposite, SWT.CENTER, false);
		this.m_notifyId.setManagerName(this.managerName);
		WidgetTestUtil.setTestId(this, "m_notifyId", this.m_notifyId);
	}

	/**
	 * ジョブ通知情報をコンポジットに反映します。
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobNotificationsInfo
	 */
	public void reflectNotificationsInfo() {

		// 初期値
		m_normalPriority.select(0);
		m_warningPriority.select(1);
		m_abnormalPriority.select(2);
		m_startPriority.select(0);
		//  m_notifyId.setText("");

		if (this.m_jobInfo != null) {
			if (m_jobInfo.getBeginPriority() != null) {
				setSelectPriority(m_startPriority, m_jobInfo.getBeginPriority() , JobInfoResponse.BeginPriorityEnum.class );
			}
			if (m_jobInfo.getNormalPriority() != null) {
				setSelectPriority(m_normalPriority, m_jobInfo.getNormalPriority() , JobInfoResponse.NormalPriorityEnum.class );
			}
			if (m_jobInfo.getWarnPriority() != null) {
				setSelectPriority(m_warningPriority, m_jobInfo.getWarnPriority() , JobInfoResponse.WarnPriorityEnum.class );
			}
			if (m_jobInfo.getAbnormalPriority() != null) {
				setSelectPriority(m_abnormalPriority,m_jobInfo.getAbnormalPriority() , JobInfoResponse.AbnormalPriorityEnum.class );
			}
			if (m_jobInfo.getNotifyRelationInfos() != null) {
				m_notifyId.setNotify(m_jobInfo.getNotifyRelationInfos());
			}
		}
	}

	/**
	 * ジョブ情報を返します。
	 *
	 * @return ジョブ通知情報のリスト
	 */
	public JobInfoWrapper getJobInfo() {
		return m_jobInfo;
	}

	/**
	 * コンポジットの情報から、ジョブ通知情報を作成します。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobNotificationsInfo
	 */
	public ValidateResult createNotificationsInfo() {
		m_jobInfo = JobTreeItemUtil.createJobInfoWrapper();
		m_jobInfo.setBeginPriority(PriorityMessage.stringToEnum(m_startPriority.getText(), JobInfoResponse.BeginPriorityEnum.class));
		m_jobInfo.setNormalPriority(PriorityMessage.stringToEnum(m_normalPriority.getText(), JobInfoResponse.NormalPriorityEnum.class));
		m_jobInfo.setWarnPriority(PriorityMessage.stringToEnum(m_warningPriority.getText(), JobInfoResponse.WarnPriorityEnum.class));
		m_jobInfo.setAbnormalPriority(PriorityMessage.stringToEnum(m_abnormalPriority.getText(), JobInfoResponse.AbnormalPriorityEnum.class));
		if (m_notifyId.getNotify() != null) {
			m_jobInfo.getNotifyRelationInfos().addAll(m_notifyId.getNotify());
		}
		return null;
	}

	/**
	 * 指定した重要度に該当する重要度用コンボボックスの項目を選択します。
	 *
	 * @param combo 重要度用コンボボックスのインスタンス
	 * @param priorityEnum 重要度
	 *
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public <T extends Enum<T>> void setSelectPriority(Combo combo, T priorityEnum, Class<T> enumType) {
		String select = PriorityMessage.enumToString(priorityEnum,enumType);

		combo.select(0);
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (select.equals(combo.getItem(i))) {
				combo.select(i);
				break;
			}
		}
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
		m_normalPriority.setEnabled(enabled);
		m_warningPriority.setEnabled(enabled);
		m_abnormalPriority.setEnabled(enabled);
		m_startPriority.setEnabled(enabled);
		m_notifyId.setButtonEnabled(enabled);
	}

	public void setJobInfo(JobInfoWrapper info) {
		this.m_jobInfo = info;
	}
}
