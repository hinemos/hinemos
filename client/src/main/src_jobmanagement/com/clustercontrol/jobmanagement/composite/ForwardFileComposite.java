/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
import com.clustercontrol.jobmanagement.action.GetForwardFileTableDefine;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.jobmanagement.util.TimeToANYhourConverter;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.JobForwardFile;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * ジョブ[ファイル転送]ビュー用のコンポジットクラスです。
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class ForwardFileComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( ForwardFileComposite.class );

	/** テーブルビューアー */
	private CommonTableViewer m_viewer = null;
	/** セッションID */
	private String m_sessionId = null;
	/** 所属ジョブユニットのジョブID */
	private String m_jobunitId = null;
	/** ジョブID */
	private String m_jobId = null;
	/** ID用ラベル */
	private Label m_idLabel = null;

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
	public ForwardFileComposite(Composite parent, int style) {
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

		//セッションID・ジョブIDラベル作成
		m_idLabel = new Label(this, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "idlabel", m_idLabel);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		m_idLabel.setLayoutData(gridData);

		//ファイル転送テーブル作成
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
		m_viewer.createTableColumn(GetForwardFileTableDefine.get(),
				GetForwardFileTableDefine.SORT_COLUMN_INDEX,
				GetForwardFileTableDefine.SORT_ORDER);
		// 列移動が可能に設定
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumn(i).setMoveable(true);
		}
	}

	/**
	 * テーブルビューアーを更新します。<BR>
	 * 引数で指定されたセッションIDとジョブIDのファイル転送一覧情報を取得し、
	 * 共通テーブルビューアーにセットします。
	 * <p>
	 * <ol>
	 * <li>引数で指定されたセッションIDとジョブIDのファイル転送一覧情報を取得します。</li>
	 * <li>共通テーブルビューアーにファイル転送一覧情報をセットします。</li>
	 * </ol>
	 *
	 * @param managerName マネージャ名
	 * @param sessionId セッションID
	 * @param jobId ジョブID
	 *
	 * @see com.clustercontrol.jobmanagement.action.GetForwardFile#get(String, String)
	 */
	public void update(String managerName, String sessionId, String jobunitId, String jobId) {
		List<JobForwardFile> list = null;

		//ファイル転送情報取得
		if (sessionId != null && jobId != null) {
			try {
				JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);
				list = wrapper.getForwardFileList(sessionId, jobunitId, jobId);
			} catch (InvalidRole_Exception e) {
				if(ClientSession.isDialogFree()){
					ClientSession.occupyDialog();
					MessageDialog.openInformation(null, Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
					ClientSession.freeDialog();
				}
			} catch (Exception e) {
				if(ClientSession.isDialogFree()){
					ClientSession.occupyDialog();
					m_log.warn("update() getForwardFileList, " + e.getMessage(), e);
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
					ClientSession.freeDialog();
				}
			}
		}
		if (list == null) {
			list = new ArrayList<JobForwardFile>();
		}

		ArrayList<Object> listInput = new ArrayList<Object>();
		for (JobForwardFile info : list) {
			ArrayList<Object> a = new ArrayList<Object>();
			a.add(info.getStatus());
			a.add(info.getEndStatus());
			a.add(info.getFile());
			a.add(info.getSrcFacility());
			a.add(info.getSrcFacilityName());
			a.add(info.getDstFacilityId());
			a.add(info.getDstFacilityName());
			a.add(info.getStartDate() == null ? "":new Date(info.getStartDate()));
			a.add(info.getEndDate() == null ? "":new Date(info.getEndDate()));
			a.add(TimeToANYhourConverter.toDiffTime(info.getStartDate(), info.getEndDate()));
			listInput.add(a);
		}
		m_viewer.setInput(listInput);

		m_sessionId = sessionId;
		m_jobunitId = jobunitId;
		m_jobId = jobId;

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
	 *
	 * 所属ジョブユニットのジョブIDを返します。
	 *
	 * @return 所属ジョブユニットのジョブID
	 */
	public String getJobunitId() {
		return m_jobunitId;
	}

	/**
	 *
	 * 所属ジョブユニットのジョブIDを設定します。
	 *
	 * @param jobunitId 所属ジョブユニットのジョブID
	 */
	public void setJobunitId(String jobunitId) {
		m_jobunitId = jobunitId;
	}




}
