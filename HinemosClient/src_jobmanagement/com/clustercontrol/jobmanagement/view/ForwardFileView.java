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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.clustercontrol.jobmanagement.composite.ForwardFileComposite;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * ジョブ[ファイル転送]ビュークラスです。
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class ForwardFileView extends CommonViewPart {
	/** ビューID */
	public static final String ID = ForwardFileView.class.getName();
	/** ジョブ[ファイル転送]ビュー用のコンポジット */
	private ForwardFileComposite m_composite = null;

	private String orgViewName = null;

	/**
	 * コンストラクタ
	 */
	public ForwardFileView() {
		super();
	}

	protected String getViewName() {
		return this.getClass().getName();
	}

	/**
	 * ビューを構築します。
	 *
	 * @param parent 親コンポジット
	 *
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 * @see #update(String, String)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		m_composite = new ForwardFileComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, m_composite);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_composite.setLayoutData(gridData);

		//ビューの更新
		this.update(null, null, null, null);
		orgViewName = this.getPartName();
	}

	/**
	 * ビューを更新します。
	 *
	 * @param managerName マネージャ名
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 *
	 * @see com.clustercontrol.jobmanagement.composite.ForwardFileComposite#update(String, String, String)
	 */
	public void update(String managerName, String sessionId, String jobunitId, String jobId) {
		if(managerName == null || managerName.equals("")) {
			return;
		}
		m_composite.update(managerName, sessionId, jobunitId, jobId);
		String viewName = orgViewName + "(" + managerName + ")";
		setPartName(viewName);
	}

	/**
	 * ジョブ[ファイル転送]ビュー用のコンポジットを返します。
	 *
	 * @return ジョブ[ファイル転送]ビュー用のコンポジット
	 */
	public ForwardFileComposite getComposite() {
		return m_composite;
	}
}
