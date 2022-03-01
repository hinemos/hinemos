/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.view;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.clustercontrol.bean.Property;
import com.clustercontrol.jobmanagement.bean.JobLinkMessageFilterPropertyConstant;
import com.clustercontrol.jobmanagement.composite.JobLinkMessageComposite;
import com.clustercontrol.util.FilterPropertyUpdater;
import com.clustercontrol.view.CommonViewPart;

/**
 * ジョブ履歴[受信ジョブ連携メッセージ一覧]ビュークラスです。
 *
 */
public class JobLinkMessageView extends CommonViewPart {

	// ログ
	private static Log m_log = LogFactory.getLog(JobLinkMessageView.class);

	/** ビューID */
	public static final String ID = JobLinkMessageView.class.getName();
	/** ジョブ履歴[ジョブ連携メッセージ一覧]ビュー用のコンポジット */
	private JobLinkMessageComposite m_composite = null;
	/** フィルタ条件 */
	private Property m_condition = null;

	/**
	 * コンストラクタ
	 */
	public JobLinkMessageView() {
		super();
	}

	protected String getViewName() {
		return this.getClass().getName();
	}

	/**
	 * ビューを構築します。
	 *
	 * @param parent
	 *            親コンポジット
	 *
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 * @see com.clustercontrol.view.AutoUpdateView#setInterval(int)
	 * @see com.clustercontrol.view.AutoUpdateView#startAutoReload()
	 * @see #createContextMenu()
	 * @see #update()
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		m_composite = new JobLinkMessageComposite(parent, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_composite.setLayoutData(gridData);
		m_composite.setView(this);

		// ビューを更新
		this.update();
	}

	/**
	 * フィルタ条件を設定します。
	 *
	 * @param condition
	 *            フィルタ条件
	 */
	public void setFilterCondition(Property condition) {
		FilterPropertyUpdater.getInstance().addFilterProperty(getClass(), condition,
				JobLinkMessageFilterPropertyConstant.MANAGER);
		m_condition = condition;
	}

	/**
	 * ビューを更新します。
	 *
	 */
	public void update() {
		try {
			if (m_condition == null) {
				m_composite.update();
			} else {
				m_composite.update(m_condition);
			}
		} catch (Exception e) {
			m_log.warn("update(), " + e.getMessage(), e);
		}
	}
}
