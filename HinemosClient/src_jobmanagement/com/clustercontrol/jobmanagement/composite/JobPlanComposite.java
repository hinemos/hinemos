/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.GetPlanListRequest;
import org.openapitools.client.model.JobPlanResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.jobmanagement.action.GetPlanTableDefine;
import com.clustercontrol.jobmanagement.composite.action.SessionJobDoubleClickListener;
import com.clustercontrol.jobmanagement.preference.JobManagementPreferencePage;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * ジョブ[スケジュール予定]ビュー用のコンポジットクラスです。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class JobPlanComposite extends Composite {
	
	private static Log m_log = LogFactory.getLog( JobPlanComposite.class );

	/** テーブルビューア */
	private CommonTableViewer m_viewer = null;
	/** ヘッダ用ラベル */
	private Label m_labelType = null;
	/** 件数用ラベル */
	private Label m_labelCount = null;

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
	public JobPlanComposite(Composite parent, int style) {
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
		m_viewer.createTableColumn(GetPlanTableDefine.get(),
				GetPlanTableDefine.SORT_COLUMN_INDEX1,
				GetPlanTableDefine.SORT_COLUMN_INDEX2,
				GetPlanTableDefine.SORT_ORDER);
		// 列移動が可能に設定
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumn(i).setMoveable(true);
		}

		m_viewer.addDoubleClickListener(
				new SessionJobDoubleClickListener(this));
	}

	/**
	 * テーブルビューアを更新します。<BR>
	 * ジョブ[スケジュール予定]一覧情報を取得し、テーブルビューアにセットします。
	 * <p>
	 * <ol>
	 * <li>ジョブ管理のプレファレンスページより、ジョブ[スケジュール予定]ビューの表示件数を取得します。</li>
	 * <li>ジョブ[スケジュール予定]一覧情報を、表示数分取得します。</li>
	 * <li>テーブルビューアにジョブ[スケジュール予定]一覧情報をセットします。</li>
	 * </ol>
	 *
	 */
	@Override
	public void update() {
		
	}

	/**
	 * テーブルビューアを更新します。<BR>
	 * 引数で指定された条件に一致するジョブ[スケジュール予定]情報を取得し、テーブルビューアにセットします。
	 * <p>
	 * <ol>
	 * <li>ジョブ管理のプレファレンスページより、ジョブ[スケジュール予定]ビューの表示数を取得します。</li>
	 * <li>引数で指定された条件に一致するジョブ[スケジュール予定]一覧情報を、表示数分取得します。</li>
	 * <li>テーブルビューアにジョブ[スケジュール予定]一覧情報をセットします。</li>
	 * </ol>
	 *
	 * @param condition 検索条件
	 *
	 */
	public void update(String conditionManager, GetPlanListRequest filter) {
		if (m_log.isDebugEnabled()) {
			String str = "managerName=" + conditionManager;
			if (filter == null) {
				str += ", filter is null";
			} else {
				str += ", filter=[" + filter.getJobKickId() + "," + filter.getFromDate() + "," + filter.getToDate() + "]";
			}
			m_log.debug(str);
		}
		
		Map<String, List<JobPlanResponse>> dispDataMap = new ConcurrentHashMap<String, List<JobPlanResponse>>();
		//ジョブ[スケジュール予定]情報取得
		int plans = ClusterControlPlugin.getDefault().getPreferenceStore().getInt(
				JobManagementPreferencePage.P_PLAN_MAX_SCHEDULE);
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();

		if(conditionManager == null || conditionManager.equals("")) {
			for(String managerName : RestConnectManager.getActiveManagerSet()) {
				getPlanList(managerName, filter, plans, dispDataMap, errorMsgs);
			}
		} else {
			getPlanList(conditionManager, filter, plans, dispDataMap, errorMsgs);
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}

		List<JobPlanResponseEx> planList = jobPlanDataMap2SortedList(dispDataMap);

		ArrayList<Object> listInput = new ArrayList<Object>();
		SimpleDateFormat sdfYmd = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		sdfYmd.setTimeZone(TimezoneUtil.getTimeZone());

		for (JobPlanResponseEx plan : planList) {
			ArrayList<Object> a = new ArrayList<Object>();
			a.add(plan.getManagerName());
			a.add(plan.getDate());
			a.add(plan.getJobKickId());
			a.add(plan.getJobKickName());
			a.add(plan.getJobunitId());
			a.add(plan.getJobId());
			a.add(plan.getJobName());
			listInput.add(a);
		}
		m_viewer.setInput(listInput);

		//selectPlan(listInput);

		if (filter != null) {
			m_labelType.setText(Messages.getString("filtered.list"));
			Object[] args = { listInput.size() };
			m_labelCount.setText(Messages.getString("filtered.records", args));
		}
		else {
			// 表示件数をセット(最大件数以上に達しているか否かの分岐)
			m_labelType.setText("");
			Object[] args = null;
			if(plans > listInput.size()){
				args = new Object[]{ listInput.size() };
			} else {
				args = new Object[]{ plans };
			}
			m_labelCount.setText(Messages.getString("records", args));
		}
	}

	private List<JobPlanResponseEx> jobPlanDataMap2SortedList(Map<String, List<JobPlanResponse>> dispDataMap) {
		List<JobPlanResponseEx> ret = new ArrayList<JobPlanResponseEx>();
		
		for (Entry<String, List<JobPlanResponse>> jobplanEntry : dispDataMap.entrySet()) {
			List<JobPlanResponse> list = jobplanEntry.getValue();
			for (JobPlanResponse plan : list) {
				ret.add(new JobPlanResponseEx(plan,jobplanEntry.getKey()));
			}
		}
		
		// Sort - OutputDate, 降順で並べ替え
		Collections.sort(ret, new Comparator<JobPlanResponseEx>() {
			@Override
			public int compare(JobPlanResponseEx o1, JobPlanResponseEx o2) {
				return o1.getDate().compareTo(o2.getDate());
			}
		});
		
		// Slice array
		int max = ClusterControlPlugin.getDefault().getPreferenceStore().getInt(JobManagementPreferencePage.P_PLAN_MAX_SCHEDULE);
		int len = ret.size();
		if( len > max ){
			ret.subList(max, len).clear();
		}
		return ret;
	}

	
	private void getPlanList(String managerName, GetPlanListRequest filter, int plans,
			Map<String, List<JobPlanResponse>> dispDataMap,
			Map<String, String> errorMsgs) {
		try {
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
			if(filter == null){
				filter = new GetPlanListRequest();
			}
			filter.setSize(plans);
			List<JobPlanResponse> planList = wrapper.getPlanList(filter);
			dispDataMap.put(managerName, planList);
		} catch (InvalidRole e) {
			errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
		} catch (Exception e) {
			errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
	}
	
	/**
	 * 表示順のソート処理の都合上、マネージャ名を付与したJobPlanResponse のListが必要なため
	 * 独自拡張したデータクラス
	 * 
	 */
	private static class JobPlanResponseEx extends JobPlanResponse{
		String managerName ;
		
		public JobPlanResponseEx(JobPlanResponse org ,String managerName){
			this.managerName = managerName;
			try{
				RestClientBeanUtil.convertBean(org, this);
			}catch(Exception e){
				//ここには来ない想定（拡張元クラスから拡張先へのデータ移動のため）
				m_log.error("JobHistoryResponseEx init :"+e.getMessage(),e);
			}
		}
		
		public String getManagerName(){
			return this.managerName;
		}

		@Override 
		public boolean equals( Object target){
			return super.equals(target);
		}
		@Override 
		public int hashCode(){
			return super.hashCode();
		}

	}
}
