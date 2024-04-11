/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;

import java.util.ArrayList;
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
import org.openapitools.client.model.JobInfoResponse;
import org.openapitools.client.model.JobNodeDetailResponse;
import org.openapitools.client.model.JobRpaInfoResponse;
import org.openapitools.client.model.JobTreeItemResponseP3;

import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.action.GetNodeDetailTableDefine;
import com.clustercontrol.jobmanagement.composite.action.NodeDetailSelectionChangedListener;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.TimeToANYhourConverter;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

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
	/** ジョブ名 */
	private String m_jobName = null;
	/** ファシリティID */
	private String m_facilityId = null;
	/** ID用ラベル */
	private Label m_idLabel = null;
	/** マネージャ名 */
	private String m_managerName = null;
	/** ジョブ種別 */
	private JobInfoResponse.TypeEnum m_jobType = null;
	/** 実行状態 */
	private JobNodeDetailResponse.StatusEnum m_status = null;
	/** RPAシナリオジョブ種別 */
	private JobRpaInfoResponse.RpaJobTypeEnum m_rpaJobType = null;

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
	public void update(String managerName, String sessionId, String jobunitId, String jobId, String jobName) {
		List<JobNodeDetailResponse> nodeDetailInfo = null;
		JobTreeItemResponseP3 jobInfo = null;

		//ノード詳細情報取得
		if (sessionId != null && jobId != null) {
			try {
				JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
				nodeDetailInfo = wrapper.getNodeDetailList(sessionId, jobunitId, jobId);
				jobInfo = wrapper.getSessionJobInfo(sessionId, jobunitId, jobId);
			} catch (InvalidRole e) {
				if(ClientSession.isDialogFree()){
					ClientSession.occupyDialog();
					MessageDialog.openInformation(null, Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
					ClientSession.freeDialog();
				}
			} catch (JobInfoNotFound e) {
				// 実行契機削除などでジョブセッション削除のタイミングで履歴情報取得した場合の対策
				// itemはnullのままにする
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
			nodeDetailInfo = new ArrayList<JobNodeDetailResponse>();
		}

		ArrayList<Object> listInput = new ArrayList<Object>();
		for (JobNodeDetailResponse info : nodeDetailInfo) {
			ArrayList<Object> a = new ArrayList<Object>();
			a.add(info.getStatus());
			a.add(info.getEndValue());
			a.add(info.getFacilityId());
			a.add(info.getNodeName());
			a.add(info.getStartDate() == null ? "":info.getStartDate());
			a.add(info.getEndDate() == null ? "":info.getEndDate());
			a.add(TimeToANYhourConverter.toDiffTime(
					JobTreeItemUtil.convertDtStringtoLong(info.getStartDate()),
					JobTreeItemUtil.convertDtStringtoLong(info.getEndDate())));
			a.add(HinemosMessage.replace(info.getMessage()));
			listInput.add(a);
		}
		m_viewer.setInput(listInput);
		m_sessionId = sessionId;
		m_jobunitId = jobunitId;
		m_jobId = jobId;
		m_jobName = jobName;
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
		
		// RPAシナリオジョブ種別をアクションボタンの有効・無効の切り替えに使用する
		if (jobInfo != null && jobInfo.getData() != null ) {
			m_jobType = jobInfo.getData().getType();
			if (jobInfo.getData().getRpa() != null) {
				if (jobInfo.getData().getRpa().getRpaJobType() != null) {
					m_rpaJobType = jobInfo.getData().getRpa().getRpaJobType();
				}
			}
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
	 * ジョブ名を返します。
	 *
	 * @return ジョブ名
	 */
	public String getJobName() {
		return m_jobName;
	}

	/**
	 * ジョブ名を設定します。
	 *
	 * @param jobName ジョブ名
	 */
	public void setJobName(String jobName) {
		m_jobName = jobName;
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
	
	/**
	 * ジョブ種別を返します。
	 * 
	 * @return ジョブ種別
	 */
	public JobInfoResponse.TypeEnum getJobType() {
		return m_jobType;
	}
	
	/**
	 * 実行状態を設定します。
	 * 
	 * @param status 実行状態
	 */
	public void setStatus(JobNodeDetailResponse.StatusEnum status) {
		this.m_status = status;
	}
	
	/**
	 * 実行状態を返します。
	 * @return 実行状態
	 */
	public JobNodeDetailResponse.StatusEnum getStatus() {
		return m_status;
	}
	
	/**
	 * RPAシナリオジョブ種別を設定します。
	 * 
	 * @param rpaJobType RPAシナリオジョブ種別
	 */
	public void setRpaJobType(JobRpaInfoResponse.RpaJobTypeEnum rpaJobType) {
		this.m_rpaJobType = rpaJobType;
	}
	
	/**
	 * RPAシナリオジョブ種別を返します。
	 * @return RPAシナリオジョブ種別
	 */
	public JobRpaInfoResponse.RpaJobTypeEnum getRpaJobType() {
		return m_rpaJobType;
	}
}
