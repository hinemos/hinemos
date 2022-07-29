/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.view;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.openapitools.client.model.GetPlanListRequest;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.jobmanagement.composite.JobPlanComposite;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.view.CommonViewPart;

/**
 * ジョブ[スケジュール予定]ビュークラスです。
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class JobPlanListView extends CommonViewPart {

	// ログ
	private static Log m_log = LogFactory.getLog( JobPlanListView.class );

	/** ビューID */
	public static final String ID = JobPlanListView.class.getName();
	/** ジョブ[スケジュール予定]ビュー用のコンポジット */
	private JobPlanComposite m_plan = null;
	/** フィルタ条件 */
	private GetPlanListRequest m_filter = null;
	private String m_managerName = null;

	protected String getViewName() {
		return this.getClass().getName();
	}

	/**
	 * ビューを構築します。
	 *
	 * @param parent 親コンポジット
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

		m_plan = new JobPlanComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, m_plan);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_plan.setLayoutData(gridData);

		//ビューを更新
		this.update();
	}

	/**
	 * フィルタ条件を設定します。
	 *
	 * @param condition フィルタ条件
	 */
	public void setFilterCondition(String managerName, GetPlanListRequest filter) {
		m_managerName = managerName;
		m_filter = filter;
	}

	/**
	 * ビューを更新します。
	 *
	 * @see com.clustercontrol.jobmanagement.composite.JobPlanComposite#update(String, JobPlanFilter)
	 */
	public void update() {
		try {
			m_log.debug("update : " + m_filter);
			if (m_filter == null) {
				m_plan.update(null, null);
			} else {
				Collection<String> activemanagerNames = RestConnectManager.getActiveManagerSet();
				if(!activemanagerNames.contains(m_managerName)){
					m_managerName = "";
				}
				m_plan.update(m_managerName, m_filter);
			}
		} catch (Exception e) {
			m_log.warn("update(), " + e.getMessage(), e);
		}
	}
}
