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

package com.clustercontrol.jobmanagement.composite;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.jobmanagement.action.GetNodeDetailTableDefine;
import com.clustercontrol.jobmanagement.composite.action.NodeDetailSelectionChangedListener;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.jobmanagement.util.TimeToANYhourConverter;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.JobNodeDetail;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * ジョブ[ノード詳細]ビュー用のコンポジットクラスです。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class NodeDetailComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( NodeDetailComposite.class );

	/** テーブルビューアー */
	private CommonTableViewer m_viewer = null;
	/** セッションID */
	private String m_sessionId = null;
	/** 所属ジョブユニットのジョブID */
	private String m_jobunitId = null;
	/** ジョブID */
	private String m_jobId = null;
	/** ファシリティID */
	private String m_facilityId = null;
	/** ID用ラベル */
	private Label m_idLabel = null;
	/** マネージャ名 */
	private String m_managerName = null;

	/**
	 * コンストラクタ
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public NodeDetailComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * コンポジットを構築します。
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		//セッションID・ジョブIDラベル作成
		m_idLabel = new Label(this, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "idlabel", m_idLabel);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		m_idLabel.setLayoutData(gridData);

		//ノード詳細テーブル作成
		Table table = new Table(this, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, null, table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		table.setLayoutData(gridData);

		m_viewer = new CommonTableViewer(table);
		m_viewer.createTableColumn(GetNodeDetailTableDefine.get(),
				GetNodeDetailTableDefine.SORT_COLUMN_INDEX,
				GetNodeDetailTableDefine.SORT_ORDER);
		// 列移動が可能に設定
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumn(i).setMoveable(true);
		}

		m_viewer
		.addSelectionChangedListener(new NodeDetailSelectionChangedListener(
				this));

	}

	/**
	 * テーブルビューアーを更新します。<BR>
	 * 引数で指定されたセッションIDとジョブIDのノード詳細一覧情報を取得し、
	 * 共通テーブルビューアーにセットします。
	 * <p>
	 * <ol>
	 * <li>引数で指定されたセッションIDとジョブIDのノード詳細一覧情報を取得します。</li>
	 * <li>共通テーブルビューアーにノード詳細一覧情報をセットします。</li>
	 * </ol>
	 *
	 * @param managerName マネージャ名
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 *
	 * @see com.clustercontrol.jobmanagement.action.GetNodeDetail#getNodeDetail(String, String)
	 */
	public void update(String managerName, String sessionId, String jobunitId, String jobId) {
		List<JobNodeDetail> nodeDetailInfo = null;

		//ノード詳細情報取得
		if (sessionId != null && jobId != null) {
			try {
				JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);
				nodeDetailInfo = wrapper.getNodeDetailList(sessionId, jobunitId, jobId);
			} catch (InvalidRole_Exception e) {
				if(ClientSession.isDialogFree()){
					ClientSession.occupyDialog();
					MessageDialog.openInformation(null, Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
					ClientSession.freeDialog();
				}
			} catch (Exception e) {
				m_log.warn("update() getNodeDetailList, " + e.getMessage(), e);
				if(ClientSession.isDialogFree()){
					ClientSession.occupyDialog();
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
					ClientSession.freeDialog();
				}
			}
		}
		if (nodeDetailInfo == null) {
			nodeDetailInfo = new ArrayList<JobNodeDetail>();
		}

		ArrayList<Object> listInput = new ArrayList<Object>();
		for (JobNodeDetail info : nodeDetailInfo) {
			ArrayList<Object> a = new ArrayList<Object>();
			a.add(info.getStatus());
			a.add(info.getEndValue());
			a.add(info.getFacilityId());
			a.add(info.getNodeName());
			a.add(info.getStartDate() == null ? "":new Date(info.getStartDate()));
			a.add(info.getEndDate() == null ? "":new Date(info.getEndDate()));
			a.add(TimeToANYhourConverter.toDiffTime(info.getStartDate(), info.getEndDate()));
			a.add(HinemosMessage.replace(info.getMessage()));
			listInput.add(a);
		}
		m_viewer.setInput(listInput);
		m_sessionId = sessionId;
		m_jobunitId = jobunitId;
		m_jobId = jobId;
		m_managerName = managerName;

		//セッションID・ジョブIDを表示
		if (m_sessionId != null && m_jobId != null) {
			m_idLabel.setText(Messages.getString("session.id") + " : "
					+ m_sessionId + ",   " + Messages.getString("job.id")
					+ " : " + m_jobId);
		} else {
			m_idLabel.setText(Messages.getString("session.id") + " : " + ",   "
					+ Messages.getString("job.id") + " : ");
		}
	}

	/**
	 * このコンポジットが利用するテーブルビューアを返します。
	 *
	 * @return テーブルビューア
	 */
	public TableViewer getTableViewer() {
		return m_viewer;
	}

	/**
	 * このコンポジットが利用するテーブルを返します。
	 *
	 * @return テーブル
	 */
	public Table getTable() {
		return m_viewer.getTable();
	}

	/**
	 * セッションIDを返します。
	 *
	 * @return セッションID
	 */
	public String getSessionId() {
		return m_sessionId;
	}

	/**
	 * セッションIDを設定します。
	 *
	 * @param sessionId セッションID
	 */
	public void setSessionId(String sessionId) {
		m_sessionId = sessionId;
	}

	/**
	 * ジョブIDを返します。
	 *
	 * @return ジョブID
	 */
	public String getJobId() {
		return m_jobId;
	}

	/**
	 * ジョブIDを設定します。
	 *
	 * @param jobId ジョブID
	 */
	public void setJobId(String jobId) {
		m_jobId = jobId;
	}

	/**
	 * ファシリティIDを返します。
	 *
	 * @return ファシリティID
	 */
	public String getFacilityId() {
		return m_facilityId;
	}

	/**
	 * ファシリティIDを設定します。
	 *
	 * @param facilityId ファシリティID
	 */
	public void setFacilityId(String facilityId) {
		m_facilityId = facilityId;
	}

	/**
	 * 所属ジョブユニットのジョブIDを取得します。
	 *
	 * @return 所属ジョブユニットのジョブID
	 */
	public String getJobunitId() {
		return m_jobunitId;
	}

	/**
	 * 所属ジョブユニットのジョブIDを設定します。
	 *
	 * @param jobunitId 所属ジョブユニットのジョブID
	 */
	public void setJobunitId(String jobunitId) {
		m_jobunitId = jobunitId;
	}

	/**
	 * @return the m_managerName
	 */
	public String getManagerName() {
		return m_managerName;
	}

	/**
	 * @param m_managerName the m_managerName to set
	 */
	public void setManagerName(String m_managerName) {
		this.m_managerName = m_managerName;
	}

}
