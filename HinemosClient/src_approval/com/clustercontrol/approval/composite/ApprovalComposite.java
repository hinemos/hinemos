/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.approval.composite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.GetApprovalJobListRequest;
import org.openapitools.client.model.JobApprovalInfoResponse;

import com.clustercontrol.approval.util.JobApprovalInfoWrapper;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.approval.action.GetApprovalTableDefine;
import com.clustercontrol.approval.bean.ApprovalFilterPropertyConstant;
import com.clustercontrol.approval.dialog.ApprovalDetailDialog;
import com.clustercontrol.approval.preference.ApprovalPreferencePage;
import com.clustercontrol.approval.view.ApprovalView;
import com.clustercontrol.bean.Property;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.jobmanagement.bean.JobApprovalResultMessage;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * 承認一覧コンポジットクラス<BR>
 *
 * @version 5.1.0
 * @since 5.1.0
 */
public class ApprovalComposite extends Composite implements ISelectionChangedListener, IDoubleClickListener {
	
	// ログ
	private static Log m_log = LogFactory.getLog( ApprovalComposite.class );
	
	/** テーブルビューアー。 */
	private CommonTableViewer tableViewer = null;
	
	/** ヘッダ用ラベル */
	private Label labelType = null;
	
	/** 件数表示用ラベル */
	private Label labelCount = null;
	
	/** セッションID */
	private String selectedSessionId = null;
	
	/** フィルタ条件 */
	private Property condition = null;

	public ApprovalComposite(Composite parent, int style) {
		super(parent, style);
		this.initialize();
	}
	/**
	 * コンポジットを配置します。
	 *
	 * @see com.clustercontrol.notify.action.GetNotifyTableDefineCheckBox#get()
	 * @see #update()
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		
		labelType = new Label(this, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "labelType", labelType);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		labelType.setLayoutData(gridData);
		
		final Table table = new Table(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION);
		WidgetTestUtil.setTestId(this, null, table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);
		
		labelCount = new Label(this, SWT.RIGHT);
		WidgetTestUtil.setTestId(this, "labelcount", labelCount);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		labelCount.setLayoutData(gridData);
		
		tableViewer = new CommonTableViewer(table);
		tableViewer.createTableColumn(GetApprovalTableDefine.get(),
				GetApprovalTableDefine.SORT_COLUMN_INDEX1,
				GetApprovalTableDefine.SORT_COLUMN_INDEX2,
				GetApprovalTableDefine.SORT_ORDER);
		
		// 列移動が可能に設定
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumn(i).setMoveable(true);
		}
		
		tableViewer.addSelectionChangedListener(this);
		tableViewer.addDoubleClickListener(this);
		
	}

	/**
	 * このコンポジットが利用するテーブルを返します。
	 *
	 * @return テーブル
	 */
	public Table getTable() {
		return this.tableViewer.getTable();
	}

	/**
	 * このコンポジットが利用するテーブルビューアーを返します。
	 *
	 * @return テーブルビューアー
	 */
	public CommonTableViewer getTableViewer() {
		return this.tableViewer;
	}

	/**
	 * テーブルビューアーを更新します。<BR>
	 * 共通テーブルビューアーにセットします。
	 *
	 */
	@Override
	public void update() {
			condition = null;
			update(condition);
		}
	
	public void update(Property property) {
		condition = property;
		
		List<JobApprovalInfoWrapper> infoList = null;
		Map<String, List<JobApprovalInfoWrapper>> dispDataMap= new ConcurrentHashMap<String, List<JobApprovalInfoWrapper>>();
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		
		String conditionManager = null;
		GetApprovalJobListRequest filter = null;
		
		int total = 0;
		int limit = ClusterControlPlugin.getDefault().getPreferenceStore().getInt(
				ApprovalPreferencePage.P_APPROVAL_MAX_LIST);
		
		if(condition != null) {
			conditionManager = getManagerName(condition);
			filter = property2jobApprovalFilter(condition);
			filter.setSize(limit);
		} else {
			// フィルタ無しの場合はデフォルトとして承認待状態のみ表示の条件とする
			filter = new GetApprovalJobListRequest();
			filter.getTargetStatusList().add(GetApprovalJobListRequest.TargetStatusListEnum.PENDING);
			filter.setSize(limit);
		}
		
		// 承認一覧情報取得
		for(String managerName : RestConnectManager.getActiveManagerSet()) {
			try {
				if(conditionManager != null && !conditionManager.equals(managerName)){
					continue;
				}
				
				JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
				List<JobApprovalInfoResponse> infoListRes = wrapper.getApprovalJobList(filter);
				if( infoListRes != null ){
					infoList = new ArrayList<JobApprovalInfoWrapper>();
					for(JobApprovalInfoResponse dtoRec : infoListRes){
						JobApprovalInfoWrapper wrapperRec = new JobApprovalInfoWrapper();
						RestClientBeanUtil.convertBean(dtoRec, wrapperRec);
						infoList.add(wrapperRec);
					}
				}
				
			} catch (InvalidRole e) {
				// 権限なし
				errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );

			} catch (Exception e) {
				// 上記以外の例外
				m_log.warn("update(), " + e.getMessage(), e);
				errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}
			
			if(infoList != null){
				dispDataMap.put(managerName, infoList);
				total += infoList.size();
			}
		}
		
		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}
		
		if(ClusterControlPlugin.getDefault().getPreferenceStore().getBoolean(
				ApprovalPreferencePage.P_APPROVAL_MESSAGE_FLG)){
			if(total > limit){
				if(ClientSession.isDialogFree()){
					ClientSession.occupyDialog();
					// 最大表示件数を超える場合、エラーダイアログを表示する
					MessageDialogWithToggle.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.approval.7"),
							Messages.getString("message.will.not.be.displayed"),
							false,
							ClusterControlPlugin.getDefault().getPreferenceStore(),
							ApprovalPreferencePage.P_APPROVAL_MESSAGE_FLG);
					ClientSession.freeDialog();
				}
			}
		}
		
		//複数マネージャ分をマージしてソート
		List<JobApprovalInfoWrapper> infolist = dispDataMap2SortedList(dispDataMap, limit);
		
		// JobApprovalInfo を tableViewer にセットするための詰め替え
		ArrayList<Object> listInput = new ArrayList<Object>();
		for (JobApprovalInfoWrapper info : infolist) {
			ArrayList<Object> obj = new ArrayList<Object>();
			obj.add(info.getManagerName()); 
			obj.add(info.getStatus());
			obj.add(info.getResult());
			obj.add(info.getSessionId());
			obj.add(info.getJobunitId());
			obj.add(info.getJobId());
			obj.add(info.getJobName());
			obj.add(info.getRequestUser());
			obj.add(info.getApprovalUser());
			try {
				obj.add(info.getStartDate() == null ? "": TimezoneUtil.getSimpleDateFormat().parse(info.getStartDate()));
			} catch (Exception e) {
				//日付変換失敗は何もしない
			}
			try {
				obj.add(info.getEndDate() == null ? "": TimezoneUtil.getSimpleDateFormat().parse(info.getEndDate()));
			} catch (Exception e) {
				//日付変換失敗は何もしない
			}
			obj.add(info.getRequestSentence());
			obj.add(info.getComment());
			obj.add(null);
			listInput.add(obj);
		}
		tableViewer.setInput(listInput);
		
		selectList(listInput);
		
		Integer count = listInput.size();
		Object[] args = null;
		args = new Object[]{ count.toString() };
		
		if (condition != null) {
			labelType.setText(Messages.getString("filtered.list"));
			labelCount.setText(Messages.getString("filtered.records", args));
		} else {
			labelType.setText("");
			labelCount.setText(Messages.getString("records", args));
		}
	}

	private List<JobApprovalInfoWrapper> dispDataMap2SortedList(Map<String, List<JobApprovalInfoWrapper>> dispDataMap, int limit) {
		List<JobApprovalInfoWrapper> ret = new ArrayList<JobApprovalInfoWrapper>();
		
		for(Map.Entry<String, List<JobApprovalInfoWrapper>> map : dispDataMap.entrySet()){
			for (JobApprovalInfoWrapper info : map.getValue()) {
				info.setManagerName(map.getKey());
				ret.add(info);
			}
		}
		
		// Sort - approvalStatus, 降順で並べ替え
		Collections.sort(ret, new Comparator<JobApprovalInfoWrapper>() {
			@Override
			public int compare(JobApprovalInfoWrapper o1, JobApprovalInfoWrapper o2) {
				return o2.getSessionId().compareTo(o1.getSessionId());
			}
		});
		
		// Slice array
		int len = ret.size();
		if( len > limit ){
			ret.subList(limit, len).clear();
			m_log.debug("limit over:" + len);
		}
		return ret;
	}

	private String getManagerName (Property property) {
		ArrayList<?> values = null;

		String managerName = null;
		//マネージャ名取得
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.MANAGER);
		if(values.get(0) instanceof String && ((String)values.get(0)).length() > 0){
			managerName = (String)values.get(0);
		}

		return managerName;
	}

	private GetApprovalJobListRequest property2jobApprovalFilter (Property property) {
		GetApprovalJobListRequest filter = new GetApprovalJobListRequest();
		ArrayList<?> values = null;

		//承認依頼日時（開始）取得
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.START_FROM_DATE);
		if(values.get(0) != null && values.get(0) instanceof Date){
			filter.setStartFromDate(TimezoneUtil.getSimpleDateFormat().format((Date)values.get(0)));
		}
		//承認依頼日時（終了）取得
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.START_TO_DATE);
		if(values.get(0) != null && values.get(0) instanceof Date){
			filter.setStartToDate(TimezoneUtil.getSimpleDateFormat().format((Date)values.get(0)));
		}
		//承認完了日時（開始）取得
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.END_FROM_DATE);
		if(values.get(0) != null && values.get(0) instanceof Date){
			filter.setEndFromDate(TimezoneUtil.getSimpleDateFormat().format((Date)values.get(0)));
		}
		//承認完了日時（終了）取得
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.END_TO_DATE);
		if(values.get(0) != null && values.get(0) instanceof Date){
			filter.setEndToDate(TimezoneUtil.getSimpleDateFormat().format((Date)values.get(0)));
		}

		//承認状態取得
		//承認待
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.APPROVAL_STATUS_PENDING);
		if (!"".equals(values.get(0))) {
			if ((Boolean)values.get(0)) {
				filter.getTargetStatusList().add(GetApprovalJobListRequest.TargetStatusListEnum.PENDING);
			}
		}
		//未承認
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.APPROVAL_STATUS_STILL);
		if (!"".equals(values.get(0))) {
			if ((Boolean)values.get(0)) {
				filter.getTargetStatusList().add(GetApprovalJobListRequest.TargetStatusListEnum.STILL);
			}
		}
		//中断中
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.APPROVAL_STATUS_SUSPEND);
		if (!"".equals(values.get(0))) {
			if ((Boolean)values.get(0)) {
				filter.getTargetStatusList().add(GetApprovalJobListRequest.TargetStatusListEnum.SUSPEND);
			}
		}
		//停止(取り下げ)
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.APPROVAL_STATUS_STOP);
		if (!"".equals(values.get(0))) {
			if ((Boolean)values.get(0)) {
				filter.getTargetStatusList().add(GetApprovalJobListRequest.TargetStatusListEnum.STOP);
			}
		}
		//承認済
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.APPROVAL_STATUS_FINISHED);
		if (!"".equals(values.get(0))) {
			if ((Boolean)values.get(0)) {
				filter.getTargetStatusList().add(GetApprovalJobListRequest.TargetStatusListEnum.FINISHED);
			}
		}
		
		//承認結果取得
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.APPROVAL_RESULT);
		GetApprovalJobListRequest.ResultEnum result = null;
		if(values.get(0) instanceof String){
			String resultString = (String)values.get(0);
			result = JobApprovalResultMessage.stringToEnum(resultString);
			filter.setResult(result);
		}

		//セッションID取得
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.SESSION_ID);
		if(values.get(0) instanceof String && ((String)values.get(0)).length() > 0){
			filter.setSessionId((String)values.get(0));
		}

		//ジョブユニットID取得
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.JOBUNIT_ID);
		if(values.get(0) instanceof String && ((String)values.get(0)).length() > 0){
			filter.setJobunitId((String)values.get(0));
		}

		//ジョブID取得
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.JOB_ID);
		if(values.get(0) instanceof String && ((String)values.get(0)).length() > 0){
			filter.setJobId((String)values.get(0));
		}

		//ジョブ名取得
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.JOB_NAME);
		if(values.get(0) instanceof String && ((String)values.get(0)).length() > 0){
			filter.setJobName((String)values.get(0));
		}

		//実行ユーザ取得
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.RQUEST_USER);
		if(values.get(0) instanceof String && ((String)values.get(0)).length() > 0){
			filter.setRequestUser((String)values.get(0));
		}

		//承認ユーザ取得
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.APPROVAL_USER);
		if(values.get(0) instanceof String && ((String)values.get(0)).length() > 0){
			filter.setApprovalUser((String)values.get(0));
		}

		//承認依頼文取得
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.RQUEST_SENTENCE);
		if(values.get(0) instanceof String && ((String)values.get(0)).length() > 0){
			filter.setRequestSentence((String)values.get(0));
		}

		//コメント取得
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.COMMENT);
		if(values.get(0) instanceof String && ((String)values.get(0)).length() > 0){
			filter.setComment((String)values.get(0));
		}

		return filter;
	}
	
	/**
	 * 前回選択したセッションIDと一致する行を選択します。
	 *
	 * @param lsit 承認ジョブ一覧情報
	 */
	private void selectList(ArrayList<Object> lsit) {
		if (selectedSessionId != null && selectedSessionId.length() > 0) {
			int index = -1;
			for (int i = 0; i < lsit.size(); i++) {
				ArrayList<?> line = (ArrayList<?>) lsit.get(i);
				String sessionId = (String)line.get(GetApprovalTableDefine.SESSION_ID);

				if (selectedSessionId.compareTo(sessionId) == 0) {
					index = i;
					break;
				}
			}
			if (index == -1) {
				selectedSessionId = null;
			} else {
				tableViewer.setSelection(new StructuredSelection(lsit.get(index)), true);
			}
		}
	}
	
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();

		StructuredSelection selection = (StructuredSelection) event.getSelection();
		IViewPart viewPart = page.findView(ApprovalView.ID);
		if ( viewPart != null && selection != null ) {
			ApprovalView view = (ApprovalView) viewPart.getAdapter(ApprovalView.class);
			if (view == null) {
				m_log.info("selection changed: view is null");
			} else {
				view.setEnabledAction(selection.size(), selection);
			}
		}
		//セッションIDを取得
		if (selection != null&& selection.getFirstElement() != null) {
			ArrayList<?> info = (ArrayList<?>) selection.getFirstElement();
			selectedSessionId = (String) info.get(GetApprovalTableDefine.SESSION_ID);
		}
	
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();

		StructuredSelection selection = (StructuredSelection) event.getSelection();
		IViewPart viewPart = page.findView(ApprovalView.ID);
		if (viewPart == null || selection == null) {
			return;
		}
		
		//詳細ダイアログを開く
		ApprovalDetailDialog dialog = new ApprovalDetailDialog(viewPart.getSite().getShell(), getSelectedApprovalInfo());
		dialog.open();

		// 更新
		this.update(condition);
	}
	
	/**
	 * 選択中の承認ジョブ情報を返します。
	 *
	 * @return 承認ジョブ情報
	 */
	public JobApprovalInfoWrapper getSelectedApprovalInfo() {
		JobApprovalInfoWrapper info = null;
		
		StructuredSelection selection = (StructuredSelection) tableViewer.getSelection();
		ArrayList<?> itemList = (ArrayList<?>) selection.getFirstElement();
		if(itemList != null){
			info = new JobApprovalInfoWrapper();
			info.setManagerName((String) itemList.get(GetApprovalTableDefine.MANAGER_NAME));
			info.setStatus((JobApprovalInfoResponse.StatusEnum) itemList.get(GetApprovalTableDefine.APPROVAL_STATUS));
			info.setResult((JobApprovalInfoResponse.ResultEnum) itemList.get(GetApprovalTableDefine.APPROVAL_RESULT));
			info.setSessionId((String) itemList.get(GetApprovalTableDefine.SESSION_ID));
			info.setJobunitId((String) itemList.get(GetApprovalTableDefine.JOBUNIT_ID));
			info.setJobId((String) itemList.get(GetApprovalTableDefine.JOB_ID));
			info.setJobName((String) itemList.get(GetApprovalTableDefine.JOB_NAME));
			info.setRequestUser((String) itemList.get(GetApprovalTableDefine.APPROVAL_REQUEST_USER));
			info.setApprovalUser((String) itemList.get(GetApprovalTableDefine.APPROVAL_USER));
			if(itemList.get(GetApprovalTableDefine.APPROVAL_REQUEST_TIME) instanceof Date){
				Date start = (Date) itemList.get(GetApprovalTableDefine.APPROVAL_REQUEST_TIME);
				info.setStartDate(TimezoneUtil.getSimpleDateFormat().format(new Date(start.getTime())));
			}
			if(itemList.get(GetApprovalTableDefine.APPROVAL_COMPLETION_TIME) instanceof Date){
				Date end = (Date) itemList.get(GetApprovalTableDefine.APPROVAL_COMPLETION_TIME);
				info.setEndDate(TimezoneUtil.getSimpleDateFormat().format(new Date(end.getTime())));
			}
			info.setRequestSentence((String) itemList.get(GetApprovalTableDefine.APPROVAL_REQUEST_SENTENCE));
			info.setComment((String) itemList.get(GetApprovalTableDefine.COMMENT));
		}
		
		return info;
	}
	
}
