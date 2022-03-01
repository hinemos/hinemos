/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.composite;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.JobRpaInfoResponse;
import org.openapitools.client.model.RpaManagementToolEndStatusResponse;

import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.jobmanagement.action.RpaIndirectEndValueTableViewer;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

public class RpaIndirectEndValueComposite extends Composite {

	/** ロガー */
	private static final Log m_log = LogFactory.getLog(RpaIndirectEndValueComposite.class);

	/** 終了値判定条件テーブルビューア */
	private RpaIndirectEndValueTableViewer m_endValueTableViewer = null;

	/** RPAシナリオジョブ情報 */
	private JobRpaInfoResponse m_rpa = null;

	/** マネージャ名 */
	private String m_managerName = null;

	public RpaIndirectEndValueComposite(Composite parent, int style, String managerName) {
		super(parent, style);
		this.m_managerName = managerName;
		initialize();
	}

	private void initialize() {
		this.setLayout(JobDialogUtil.getParentLayout());

		// 終了値判定条件
		Table table = new Table(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new RowData(450, 100));
		m_endValueTableViewer = new RpaIndirectEndValueTableViewer(table);
	}

	public void reflectRpaJobInfo() {
		if (this.m_rpa != null) {
			m_endValueTableViewer.setEndValueInfos(m_rpa.getRpaJobCheckEndValueInfos());
		}
	}

	public void createRpaJobInfo() {
		this.m_rpa.setRpaJobCheckEndValueInfos(m_endValueTableViewer.getEndValueInfos());
	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		m_endValueTableViewer.setEditable(enabled);
	}

	/**
	 * RPA管理ツールに応じた終了値のデフォルト値を取得し、テーブルに表示します。
	 * @param rpaManagementToolId RPA管理ツールID
	 */
	public void refresh(String rpaManagementToolId) {
		List<RpaManagementToolEndStatusResponse> rpaManagementToolEndStatusList;
		try {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(this.m_managerName);
			rpaManagementToolEndStatusList = wrapper.getRpaManagementToolEndStatus(rpaManagementToolId);
			this.m_endValueTableViewer.setInput(rpaManagementToolEndStatusList);
		} catch (InvalidRole e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			m_log.warn("initialize() : " + e.getMessage(), e);
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", "
							+ HinemosMessage.replace(e.getMessage()));
		}

	}

	/**
	 * @return the m_rpa
	 */
	public JobRpaInfoResponse getRpaJobInfo() {
		return m_rpa;
	}

	/**
	 * @param m_rpa
	 *            the m_rpa to set
	 */
	public void setRpaJobInfo(JobRpaInfoResponse rpa) {
		this.m_rpa = rpa;
	}
}
