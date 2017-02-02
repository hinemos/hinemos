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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.bean.Property;
import com.clustercontrol.jobmanagement.action.GetHistoryTableDefine;
import com.clustercontrol.jobmanagement.bean.JobTriggerTypeMessage;
import com.clustercontrol.jobmanagement.composite.action.HistorySelectionChangedListener;
import com.clustercontrol.jobmanagement.composite.action.SessionJobDoubleClickListener;
import com.clustercontrol.jobmanagement.preference.JobManagementPreferencePage;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.jobmanagement.util.JobPropertyUtil;
import com.clustercontrol.jobmanagement.util.TimeToANYhourConverter;
import com.clustercontrol.jobmanagement.view.JobHistoryView;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.JobHistory;
import com.clustercontrol.ws.jobmanagement.JobHistoryList;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * ジョブ[履歴]ビュー用のコンポジットクラスです。
 *
 * @version 2.1.1
 * @since 1.0.0
 */
public class HistoryComposite extends Composite {
	private static Log m_log = LogFactory.getLog( HistoryComposite.class );

	/** テーブルビューア */
	private CommonTableViewer m_viewer = null;
	/** ヘッダ用ラベル */
	private Label m_labelType = null;
	/** 件数用ラベル */
	private Label m_labelCount = null;
	/** セッションID */
	private String m_sessionId = null;
	/** 所属ジョブユニットのジョブID */
	private String m_jobunitId = null;
	/** ジョブID */
	private String m_jobId = null;
	/** マネージャ名 */
	private String m_managerName = null;

	private JobHistoryView m_view = null;

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
	public HistoryComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	public void setView(JobHistoryView view) {
		m_view = view;
	}

	public JobHistoryView getView() {
		return m_view;
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		m_labelType = new Label(this, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "labeltype", m_labelType);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		m_labelType.setLayoutData(gridData);

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

		m_labelCount = new Label(this, SWT.RIGHT);
		WidgetTestUtil.setTestId(this, "labelcount", m_labelCount);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		m_labelCount.setLayoutData(gridData);

		m_viewer = new CommonTableViewer(table);
		m_viewer.createTableColumn(GetHistoryTableDefine.get(),
				GetHistoryTableDefine.SORT_COLUMN_INDEX1,
				GetHistoryTableDefine.SORT_COLUMN_INDEX2,
				GetHistoryTableDefine.SORT_ORDER);

		// 列移動が可能に設定
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumn(i).setMoveable(true);
		}
		m_viewer.addSelectionChangedListener(
				new HistorySelectionChangedListener(this));

		m_viewer.addDoubleClickListener(
				new SessionJobDoubleClickListener(this));
	}

	/**
	 * テーブルビューアーを更新します。<BR>
	 * ジョブ履歴一覧情報を取得し、共通テーブルビューアーにセットします。
	 * <p>
	 * <ol>
	 * <li>監視管理のプレファレンスページより、ジョブ[履歴]ビューの表示履歴数を取得します。</li>
	 * <li>ジョブ履歴一覧情報を、表示履歴数分取得します。</li>
	 * <li>表示履歴数を超える場合、メッセージダイアログを表示します。</li>
	 * <li>共通テーブルビューアーにジョブ履歴一覧情報をセットします。</li>
	 * </ol>
	 *
	 * @see com.clustercontrol.jobmanagement.action.GetHistory#getHistory(int)
	 * @see #selectHistory(ArrayList)
	 */
	@Override
	public void update() {
		update(null);
	}

	/**
	 * テーブルビューアを更新します。<BR>
	 * 引数で指定された条件に一致するジョブ履歴一覧情報を取得し、共通テーブルビューアーにセットします。
	 * <p>
	 * <ol>
	 * <li>監視管理のプレファレンスページより、ジョブ[履歴]ビューの表示履歴数を取得します。</li>
	 * <li>引数で指定された条件に一致するジョブ履歴一覧情報を、表示履歴数分取得します。</li>
	 * <li>表示履歴数を超える場合、メッセージダイアログを表示します。</li>
	 * <li>共通テーブルビューアーにジョブ履歴一覧情報をセットします。</li>
	 * </ol>
	 *
	 * @param condition 検索条件
	 *
	 * @see com.clustercontrol.jobmanagement.action.GetHistory#getHistory(Property, int)
	 * @see #selectHistory(ArrayList)
	 */
	public void update(Property condition) {
		Map<String, JobHistoryList> dispDataMap = new ConcurrentHashMap<String, JobHistoryList>();

		//ジョブ履歴情報取得
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		int total = 0;
		int size = 0;
		String conditionManager = null;
		if(condition != null) {
			conditionManager = JobPropertyUtil.getManagerName(condition);
		}

		if(conditionManager == null || conditionManager.equals("")) {
			for(String managerName : EndpointManager.getActiveManagerSet()) {
				int[] ret = getHistoryList(managerName, condition, dispDataMap, errorMsgs);
				total += ret[0];
				size += ret[1];
			}
		} else {
			int[] ret = getHistoryList(conditionManager, condition, dispDataMap, errorMsgs);
			total = ret[0];
			size = ret[1];
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}

		if(ClusterControlPlugin.getDefault().getPreferenceStore().getBoolean(
				JobManagementPreferencePage.P_HISTORY_MESSAGE_FLG)){
			if(total > size){
				if(ClientSession.isDialogFree()){
					ClientSession.occupyDialog();
					// 最大表示件数を超える場合、エラーダイアログを表示する
					MessageDialogWithToggle.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.job.33"),
							Messages.getString("message.will.not.be.displayed"),
							false,
							ClusterControlPlugin.getDefault().getPreferenceStore(),
							JobManagementPreferencePage.P_HISTORY_MESSAGE_FLG);
					ClientSession.freeDialog();
				}
			}
		}
		
		List<JobHistory> historyList = jobHistoryDataMap2SortedList(dispDataMap);
		size = historyList.size();

		ArrayList<Object> listInput = new ArrayList<Object>();
		for (JobHistory history : historyList) {
			ArrayList<Object> a = new ArrayList<Object>();
			a.add(history.getManagerName());
			a.add(history.getStatus());
			a.add(history.getEndStatus());
			a.add(history.getEndValue());
			a.add(history.getSessionId());
			m_log.trace("sessionId=" + history.getSessionId());
			a.add(history.getJobId());
			a.add(history.getJobName());
			a.add(history.getJobunitId());
			a.add(history.getJobType());
			a.add(history.getFacilityId());
			a.add(HinemosMessage.replace(history.getScope()));
			a.add(history.getOwnerRoleId());
			a.add(history.getScheduleDate() == null ? "":new Date(history.getScheduleDate()));
			a.add(history.getStartDate() == null ? "":new Date(history.getStartDate()));
			a.add(history.getEndDate() == null ? "":new Date(history.getEndDate()));
			a.add(TimeToANYhourConverter.toDiffTime(history.getStartDate(), history.getEndDate()));
			a.add(JobTriggerTypeMessage.typeToString(history.getJobTriggerType()));
			a.add(history.getTriggerInfo());
			a.add(null);
			listInput.add(a);
		}
		m_viewer.setInput(listInput);

		selectHistory(listInput);

		if (condition != null) {
			m_labelType.setText(Messages.getString("filtered.list"));
			Object[] args = null;
			if(total > size){
				args = new Object[]{ size };
			} else {
				args = new Object[]{ total };
			}
			m_labelCount.setText(Messages.getString("filtered.records", args));
		} else {
			// 表示件数をセット(最大件数以上に達しているか否かの分岐)
			m_labelType.setText("");
			Object[] args = null;
			if(total > size){
				args = new Object[]{ size };
			} else {
				args = new Object[]{ total };
			}
			m_labelCount.setText(Messages.getString("records", args));
		}
	}
	
	private List<JobHistory> jobHistoryDataMap2SortedList(Map<String, JobHistoryList> dispDataMap) {
		List<JobHistory> ret = new ArrayList<JobHistory>();
		
		for (Entry<String, JobHistoryList> historyEntry : dispDataMap.entrySet()) {
			JobHistoryList list = historyEntry.getValue();
			for (JobHistory history : list.getList()) {
				history.setManagerName(historyEntry.getKey());
				ret.add(history);
			}
		}
		
		// Sort - OutputDate, 降順で並べ替え
		Collections.sort(ret, new Comparator<JobHistory>() {
			@Override
			public int compare(JobHistory o1, JobHistory o2) {
				return o2.getSessionId().compareTo(o1.getSessionId());
			}
		});
		
		// Slice array
		int max = ClusterControlPlugin.getDefault().getPreferenceStore().getInt(JobManagementPreferencePage.P_HISTORY_MAX_HISTORIES);
		int len = ret.size();
		if( len > max ){
			ret.subList(max, len).clear();
		}
		return ret;
	}

	private int[] getHistoryList(String managerName, Property condition,
			Map<String, JobHistoryList> dispDataMap,
			Map<String, String> errorMsgs) {
		JobHistoryList historyInfo = null;

		int total = 0;
		int size = 0;
		try {
			int histories = ClusterControlPlugin.getDefault().getPreferenceStore().getInt(
					JobManagementPreferencePage.P_HISTORY_MAX_HISTORIES);
			JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);
			if (condition == null) {
				historyInfo = wrapper.getJobHistoryList(null, histories);
			} else {
				historyInfo = wrapper.getJobHistoryList(
						JobPropertyUtil.property2jobHistoryFilter(condition), histories);
			}
			total = historyInfo.getTotal();
			size = historyInfo.getList().size();
			dispDataMap.put(managerName, historyInfo);
		} catch (InvalidRole_Exception e) {
			errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
		} catch (Exception e) {
			errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		int[] ret = {total, size};
		return ret;
	}

	/**
	 * ジョブ履歴の行を選択します。<BR>
	 * 前回選択したセッションIDとジョブIDが一致する行を選択します。
	 *
	 * @param historyInfo ジョブ履歴情報
	 */
	public void selectHistory(ArrayList<Object> historyInfo) {
		if (m_sessionId != null && m_sessionId.length() > 0) {
			int index = -1;
			for (int i = 0; i < historyInfo.size(); i++) {
				ArrayList<?> line = (ArrayList<?>) historyInfo.get(i);
				String sessionId = (String)line.get(GetHistoryTableDefine.SESSION_ID);

				if (m_sessionId.compareTo(sessionId) == 0) {
					index = i;
					break;
				}
			}
			if (index == -1) {
				m_sessionId = null;
				m_jobId = null;
			} else {
				m_viewer.setSelection(new StructuredSelection(historyInfo
						.get(index)), true);
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
