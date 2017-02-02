/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.view;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.jobmanagement.composite.JobPlanComposite;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.ws.jobmanagement.JobPlanFilter;

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
	private JobPlanFilter m_filter = null;
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
	public void setFilterCondition(String managerName, JobPlanFilter filter) {
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
				m_plan.update(m_managerName, m_filter);
			}
		} catch (Exception e) {
			m_log.warn("update(), " + e.getMessage(), e);
		}
	}
}
