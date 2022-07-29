/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.openapitools.client.model.JobTreeItemResponseP4;

import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.jobmanagement.action.GetJobDetailTableDefine;
import com.clustercontrol.jobmanagement.composite.action.JobDetailSelectionChangedListener;
import com.clustercontrol.jobmanagement.composite.action.SessionJobDoubleClickListener;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmanagement.viewer.JobTableTreeViewer;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * ジョブ[ジョブ詳細]ビュー用のコンポジットクラスです。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class DetailComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( DetailComposite.class );

	/** テーブルビューアー */
	private JobTableTreeViewer m_viewer = null;
	/** セッションID */
	private String m_sessionId = null;
	/** 所属ジョブユニットのジョブID */
	private String m_jobunitId = null;
	/** ジョブID */
	private String m_jobId = null;
	/** ジョブ名 */
	private String m_jobName = null;
	/** セッションID用ラベル */
	private Label m_sessionIdLabel = null;
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
	public DetailComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		//セッションIDラベル作成
		m_sessionIdLabel = new Label(this, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "sessionid", m_sessionIdLabel);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		m_sessionIdLabel.setLayoutData(gridData);

		//ジョブ詳細テーブル作成
		Tree tree = new Tree(this, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, null, tree);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		tree.setLayoutData(gridData);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		m_viewer = new JobTableTreeViewer(tree);
		m_viewer.createTableColumn(GetJobDetailTableDefine.get(),
				GetJobDetailTableDefine.SORT_COLUMN_INDEX,
				GetJobDetailTableDefine.SORT_ORDER);
		// 列移動が可能に設定
		for (int i = 0; i < tree.getColumnCount(); i++) {
			tree.getColumn(i).setMoveable(true);
		}


		m_viewer.addSelectionChangedListener(
				new JobDetailSelectionChangedListener(this));

		m_viewer.addDoubleClickListener(
				new SessionJobDoubleClickListener(this));

		update(null, null, null);
	}

	/**
	 * テーブルビューアーを更新します。<BR>
	 * 引数で指定されたセッションIDのジョブ詳細一覧情報を取得し、
	 * 共通テーブルビューアーにセットします。
	 * <p>
	 * <ol>
	 * <li>引数で指定されたセッションIDのジョブ詳細一覧情報を取得します。</li>
	 * <li>共通テーブルビューアーにジョブ詳細一覧情報をセットします。</li>
	 * </ol>
	 *
	 * @param managerName マネージャ名
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 *
	 * @see com.clustercontrol.jobmanagement.action.GetJobDetail#getJobDetail(String, String)
	 * @see #setJobId(String)
	 */
	public void update(String managerName, String sessionId, String jobunitId) {
		long start = HinemosTime.currentTimeMillis();
		if (m_log.isDebugEnabled()) {
			m_log.debug("DetailComposite update() is start : m_sessionId=" + sessionId + ", startTime="  + start + "ms.");
		}
		//ジョブ詳細情報取得
		JobTreeItemWrapper item = null;
		if (sessionId != null && sessionId.length() > 0) {
			try {
				JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
				JobTreeItemResponseP4 detail = wrapper.getJobDetailList(sessionId);
				item = JobTreeItemUtil.getItemFromP4(detail);
			} catch (InvalidRole e) {
				if(ClientSession.isDialogFree()){
					ClientSession.occupyDialog();
					MessageDialog.openInformation(null, Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
					ClientSession.freeDialog();
				}
			} catch (Exception e) {
				m_log.warn("update() getJobDetailList, " + e.getMessage(), e);
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
		setItem(managerName, sessionId, jobunitId, item);
		if (m_log.isDebugEnabled()) {
			long end = HinemosTime.currentTimeMillis();
			m_log.debug("DetailComposite update() is end :  m_sessionId=" + sessionId + ", endTime=" + end  + "ms, diffTime="  + (end - start) + "ms.");
		}
	}

	/**
	 * 取得したアイテムをセットします。
	 * @param managerName マネージャ名
	 * @param sessionId セッションID
	 * @param jobunitId ジョブユニットID
	 * @param item アイテム情報
	 */
	public void setItem(String managerName, String sessionId, String jobunitId, JobTreeItemWrapper item) {
		if (item == null) {
			m_viewer.setInput(new JobTreeItemWrapper());
		} else {
			m_viewer.setInput(item);
		}
		m_viewer.expandAll();

		if (m_sessionId != null && m_sessionId.length() > 0
				&& sessionId != null && sessionId.length() > 0
				&& m_sessionId.compareTo(sessionId) == 0) {
			selectDetail(item.getChildren().get(0));
		} else {
			setJobId(null);
		}
		m_managerName = managerName;
		m_sessionId = sessionId;
		m_jobunitId = jobunitId;

		//セッションIDを表示
		if (m_sessionId != null) {
			m_sessionIdLabel.setText(Messages.getString("session.id") + " : "
					+ m_sessionId);
		} else {
			m_sessionIdLabel.setText(Messages.getString("session.id") + " : ");
		}
	}

	/**
	 * ジョブ詳細の行を選択します。<BR>
	 * 前回選択したジョブIDと同じジョブIDの行を選択します。
	 *
	 * @param item テーブルツリーアイテム
	 */
	public void selectDetail(JobTreeItemWrapper item) {
		if (getJobId() != null && getJobId().length() > 0) {
			if (m_viewer.getSelection().isEmpty()) {
				boolean select = false;
				JobInfoWrapper info = item.getData();
				if (info == null) {
					m_log.info("selectDetail info is null");
					return;
				}
				String jobId = info.getId();
				if (getJobId().compareTo(jobId) == 0) {
					select = true;
				}

				if (select) {
					m_viewer.setSelection(new StructuredSelection(item), true);
				} else {
					for (int i = 0; i < item.getChildren().size(); i++) {
						JobTreeItemWrapper children = item.getChildren().get(i);
						selectDetail(children);
					}
				}
			}
		}
	}

	/**
	 * このコンポジットが利用するテーブルツリービューアを返します。
	 *
	 * @return テーブルツリービューア
	 */
	public TreeViewer getTableTreeViewer() {
		return m_viewer;
	}

	/**
	 * このコンポジットが利用するテーブルを返します。
	 *
	 * @return テーブル
	 */
//	public Table getTable() {
//		return m_viewer.getTableTree().getTable();
//	}

	public Tree getTree() {
		return m_viewer.getTree();
	}

	/**
	 * このコンポジットが利用するテーブルツリーを返します。
	 *
	 * @return テーブルツリー
	 */
//	public TableTree getTableTree() {
//		return m_viewer.getTableTree();
//	}

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
	 * 所属ジョブユニットのジョブIDを返します。
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
	 * マネージャ名を取得します。
	 * @return the m_managerName
	 */
	public String getManagerName() {
		return m_managerName;
	}

	/**
	 * マネージャ名を設定します。
	 * @param m_managerName the m_managerName to set
	 */
	public void setManagerName(String managerName) {
		this.m_managerName = managerName;
	}


}
