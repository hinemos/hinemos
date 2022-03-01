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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.GetJobKickListByConditionRequest;
import org.openapitools.client.model.JobKickFilterInfoRequest;
import org.openapitools.client.model.JobKickResponse;

import com.clustercontrol.bean.DayOfWeekConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.jobmanagement.action.GetJobKickTableDefine;
import com.clustercontrol.jobmanagement.bean.JobKickTypeMessage;
import com.clustercontrol.jobmanagement.composite.action.JobKickDoubleClickListener;
import com.clustercontrol.jobmanagement.composite.action.JobKickSelectionChangedListener;
import com.clustercontrol.jobmanagement.util.JobKickFilterPropertyUtil;
import com.clustercontrol.jobmanagement.util.JobPropertyUtil;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * ジョブ[実行契機]ビュー用のコンポジットクラスです。
 *
 * @version 4.1.0
 * @since 1.0.0
 */
public class JobKickListComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( JobKickListComposite.class );

	/** テーブルビューア */
	private CommonTableViewer m_viewer = null;
	/** 選択アイテム */
	private ArrayList<ArrayList<?>> m_selectItem = null;

	/** 表示内容ラベル */
	private Label m_statuslabel = null;

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
	public JobKickListComposite(Composite parent, int style) {
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

		Table table = new Table(this, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.MULTI);
		WidgetTestUtil.setTestId(this, null, table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		this.m_statuslabel = new Label(this, SWT.RIGHT);
		WidgetTestUtil.setTestId(this, "m_statuslabel", m_statuslabel);
		this.m_statuslabel.setText("");
		GridData statuslabelGridData = new GridData();
		statuslabelGridData.horizontalAlignment = GridData.FILL;
		statuslabelGridData.verticalAlignment = GridData.FILL;
		this.m_statuslabel.setLayoutData(statuslabelGridData);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		table.setLayoutData(gridData);

		m_viewer = new CommonTableViewer(table);
		m_viewer.createTableColumn(GetJobKickTableDefine.get(),
				GetJobKickTableDefine.SORT_COLUMN_INDEX1,
				GetJobKickTableDefine.SORT_COLUMN_INDEX2,
				GetJobKickTableDefine.SORT_ORDER);
		// 列移動が可能に設定
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumn(i).setMoveable(true);
		}

		m_viewer
		.addSelectionChangedListener(new JobKickSelectionChangedListener(
				this));
		// ダブルクリックリスナの追加
		m_viewer.addDoubleClickListener(new JobKickDoubleClickListener(this));
	}

	/**
	 * テーブルビューアーを更新します。<BR>
	 * ジョブ[実行契機]一覧情報を取得し、共通テーブルビューアーにセットします。
	 * <p>
	 * <ol>
	 * <li>スケジュール一覧情報を取得します。</li>
	 * <li>共通テーブルビューアーにジョブ[実行契機]一覧情報をセットします。</li>
	 * </ol>
	 */
	@Override
	public void update() {
		update(null);
	}

	/**
	 * テーブルビューアーを更新します。<BR>
	 * ジョブ[実行契機]一覧情報を取得し、共通テーブルビューアーにセットします。
	 * <p>
	 * <ol>
	 * <li>スケジュール一覧情報を取得します。</li>
	 * <li>共通テーブルビューアーにジョブ[実行契機]一覧情報をセットします。</li>
	 * </ol>
	 * 
	 * @param condition 検索条件
	 */
	public void update(Property condition) {
		Map<String, List<JobKickResponse>> dispDataMap = new ConcurrentHashMap<String, List<JobKickResponse>>();
		ArrayList<Object> listInput = new ArrayList<Object>();

		// ジョブ実行契機情報取得
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		String conditionManager = null;
		if (condition != null) {
			conditionManager = JobPropertyUtil.getManagerName(condition);
		}

		if (conditionManager == null || conditionManager.equals("")) {
			for (String managerName : RestConnectManager.getActiveManagerSet()) {
				if (condition != null) {
					getJobKickWithCondition(managerName, condition, dispDataMap, errorMsgs);
				} else {
					getJobKick(managerName, dispDataMap, errorMsgs);
				}
			}
			
		} else {
			getJobKickWithCondition(conditionManager, condition, dispDataMap, errorMsgs);
		}
		
		// メッセージ表示
		if (errorMsgs.size() > 0) {
			UIManager.showMessageBox(errorMsgs, true);
		}

		for (Map.Entry<String, List<JobKickResponse>> set : dispDataMap.entrySet()) {
			for(JobKickResponse jobKick : set.getValue()) {
				ArrayList<Object> a = new ArrayList<Object>();
				a.add(set.getKey());
				a.add(JobKickTypeMessage.typeEnumValueToString(jobKick.getType().getValue()));
				a.add(jobKick.getId());
				a.add(jobKick.getName());
				a.add(jobKick.getJobId());
				a.add(jobKick.getJobName());
				a.add(jobKick.getJobunitId());
				a.add(new JobKickDetail(jobKick));
				a.add(jobKick.getCalendarId());
				a.add(jobKick.getValid());
				a.add(jobKick.getOwnerRoleId());
				a.add(jobKick.getCreateUser());
				if(jobKick.getCreateTime() == null){
					a.add(null);
				}
				else{
					a.add(jobKick.getCreateTime());
				}
				a.add(jobKick.getUpdateUser());
				if(jobKick.getUpdateTime() == null){
					a.add(null);
				}
				else{
					a.add(jobKick.getUpdateTime());
				}
				listInput.add(a);
			}
		}
		m_viewer.setInput(listInput);

		// 合計欄更新
		String[] args = { String.valueOf(listInput.size()) };
		String message = null;
		if (condition == null) {
			message = Messages.getString("records", args);
		} else {
			message = Messages.getString("filtered.records", args);
		}
		this.m_statuslabel.setText(message);
	}

	private static class JobKickDetail {
		JobKickResponse jobKick;
		private JobKickDetail(JobKickResponse jobKick) {
			this.jobKick = jobKick;
		}
		
		@Override
		public String toString() {
			if (jobKick.getType() == JobKickResponse.TypeEnum.SCHEDULE) {
				String ret = "";
				if (jobKick.getScheduleType() ==  JobKickResponse.ScheduleTypeEnum.DAY) {
					if (jobKick.getHour() == null) {
						ret += "*";
					} else {
						ret += String.format("%02d", jobKick.getHour());
					}
					ret += ":" + String.format("%02d", jobKick.getMinute());
				} else if (jobKick.getScheduleType() == JobKickResponse.ScheduleTypeEnum.WEEK) {
					ret += DayOfWeekConstant.typeToString(jobKick.getWeek()) + " ";
					if (jobKick.getHour() == null) {
						ret += "*";
					} else {
						ret += String.format("%02d", jobKick.getHour());
					}
					ret += ":" + String.format("%02d", jobKick.getMinute());
				} else if (jobKick.getScheduleType() == JobKickResponse.ScheduleTypeEnum.REPEAT) {
					ret += String.format("%02d", jobKick.getFromXminutes()) +
							Messages.getString("schedule.min.start.time") +
							String.format("%02d", jobKick.getEveryXminutes()) +
							Messages.getString("schedule.min.execution.interval");
				} else if (jobKick.getScheduleType() == JobKickResponse.ScheduleTypeEnum.INTERVAL) {
					ret += String.format("%02d", jobKick.getHour()) +
							Messages.getString("hr") +
							String.format("%02d", jobKick.getMinute()) +
							Messages.getString("schedule.min.start.time") +
							String.format("%02d", jobKick.getEveryXminutes()) +
							Messages.getString("schedule.min.execution.interval");
				}
				return ret;
			} else if (jobKick.getType() == JobKickResponse.TypeEnum.FILECHECK) {
				return jobKick.getFileName();
			} else if (jobKick.getType() == JobKickResponse.TypeEnum.JOBLINKRCV) {
				return jobKick.getJoblinkMessageId();
			} else if (jobKick.getType() == JobKickResponse.TypeEnum.MANUAL) {
				//マニュアル実行契機の場合は何も表示しない
				return "";
			} else {
				m_log.warn("unknown type " + jobKick.getType());
			}
			return "";
		}
	}

	/**
	 * 実行契機一覧情報を取得します。
	 *
	 * @param managerName マネージャ名
	 * @param dispDataMap 検索結果
	 * @param errorMsgs エラー情報
	 */
	private void getJobKick(
			String managerName,
			Map<String, List<JobKickResponse>> dispDataMap, 
			Map<String, String> errorMsgs) {

		//実行契機情報取得
		try {
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
			dispDataMap.put(managerName, wrapper.getJobKickList());
		} catch (InvalidRole e) {
			errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
		} catch (Exception e) {
			m_log.warn("getJobKick(), " + e.getMessage(), e);
			errorMsgs.put(managerName,
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
	}


	/**
	 * 実行契機一覧情報を取得します。
	 *
	 * @param managerName マネージャ名
	 * @param condition 検索条件
	 * @param dispDataMap 検索結果
	 * @param errorMsgs エラー情報
	 */
	private void getJobKickWithCondition(
			String managerName, 
			Property condition, 
			Map<String, List<JobKickResponse>> dispDataMap, 
			Map<String, String> errorMsgs) {

		//実行契機情報取得
		try {
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
			JobKickFilterInfoRequest jobKickFilterInfo = JobKickFilterPropertyUtil.property2dto(condition);
			GetJobKickListByConditionRequest getJobKickListByCondition = new GetJobKickListByConditionRequest();
			getJobKickListByCondition.setJobKickFilterInfo(jobKickFilterInfo);

			dispDataMap.put(managerName, wrapper.getJobKickListByCondition(getJobKickListByCondition));
		} catch (InvalidRole e) {
			errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
		} catch (Exception e) {
			m_log.warn("getJobKick(), " + e.getMessage(), e);
			errorMsgs.put(managerName,
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
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
	 * 選択アイテムを返します。
	 *
	 * @return 選択アイテム
	 */
	public ArrayList<ArrayList<?>> getSelectItem() {
		return m_selectItem;
	}

	/**
	 * 選択アイテムを設定します。
	 *
	 * @param selectItem 選択アイテム
	 */
	public void setSelectItem(ArrayList<ArrayList<?>> selectItem) {
		this.m_selectItem = selectItem;
	}
}
