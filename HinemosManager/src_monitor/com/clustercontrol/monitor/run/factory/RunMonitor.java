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

package com.clustercontrol.monitor.run.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.collect.bean.Sample;
import com.clustercontrol.collect.util.CollectDataUtil;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.hub.bean.StringSample;
import com.clustercontrol.hub.util.CollectStringDataUtil;
import com.clustercontrol.monitor.run.bean.MonitorRunResultInfo;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorJudgementInfo;
import com.clustercontrol.monitor.run.util.MonitorExecuteTask;
import com.clustercontrol.monitor.run.util.NodeMonitorPollerController;
import com.clustercontrol.monitor.run.util.NodeToMonitorCache;
import com.clustercontrol.monitor.run.util.ParallelExecution;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.performance.bean.CollectedDataErrorTypeConstant;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * 監視を実行する抽象クラス<BR>
 * <p>
 * 監視種別（真偽値，数値，文字列）の各クラスで継承してください。
 *
 * @version 4.0.0
 * @since 2.0.0
 */
abstract public class RunMonitor {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( RunMonitor.class );

	/** 通知のメッセージ。 */
	private static final String MESSAGE_INFO = MessageConstant.MESSAGE_PRIORITY_SCOPE_INFO.getMessage();
	/** 警告のメッセージ。 */
	private static final String MESSAGE_WARNING = MessageConstant.MESSAGE_PRIORITY_SCOPE_WARNING.getMessage();
	/** 危険のメッセージ。 */
	private static final String MESSAGE_CRITICAL = MessageConstant.MESSAGE_PRIORITY_SCOPE_CRITICAL.getMessage();
	/** 不明のメッセージ。 */
	private static final String MESSAGE_UNKNOWN = MessageConstant.UNKNOWN.getMessage();

	/** 監視情報のローカルコンポーネント。 */
	protected MonitorInfo m_monitor;

	/** 監視対象ID。 */
	protected String m_monitorTypeId;

	/** 監視項目ID。 */
	protected String m_monitorId;

	/** 通知ID */
	private String m_notifyGroupId;

	/** 監視対象ファシリティID。 */
	protected String m_facilityId;
	
	/**
	 * 値取得の失敗時の重要度。
	 * 初期値は値取得失敗を示す値(-1)として、
	 * setMonitorInfoがコールされた際に、値取得失敗時の重要度がセットされる。
	 *
	 * @see #setMonitorInfo(String, String)
	 */
	protected int m_failurePriority = PriorityConstant.TYPE_FAILURE;

	/** 収集間隔 */
	protected int m_runInterval;

	/** 監視開始時刻。 */
	protected Date m_now;

	/** スコープ 監視結果取得時刻。 */
	protected long m_scopeDate;

	/** ノード 監視結果取得時刻。 */
	protected long m_nodeDate;

	/** 監視単位。 */
	protected int m_monitorBlock;

	/** ノードフラグ。 */
	protected boolean m_isNode;

	/** ノード情報一覧 */
	protected volatile Map<String, NodeInfo> nodeInfo;

	/** 重要度別ファシリティ名マップ。 */
	protected HashMap<Integer, ArrayList<String>> m_priorityMap;

	/** カレンダの期間内か否かのフラグ */
	protected boolean m_isInCalendarTerm;

	/** 次の監視タイミングがカレンダの期間内か否かのフラグ */
	protected boolean m_isInNextCalendarTerm;

	/** ジョブ監視からの遷移か否か（true：ジョブ監視からの遷移、false:ジョブ監視以外からの遷移） */
	protected boolean m_isMonitorJob = false;

	/** 実行結果（ジョブ監視で使用） */
	protected MonitorRunResultInfo m_monitorRunResultInfo;

	/** 今回の実行結果（ジョブ監視で使用） */
	protected Object m_curData = null;

	/** 前回の実行結果（ジョブ監視で使用） */
	protected Object m_prvData = null;

	/**
	 * 判定情報マップ。
	 * <p>
	 * <dl>
	 *  <dt>キー</dt>
	 *  <dd>真偽値監視：真偽値定数（{@link com.clustercontrol.monitor.run.bean.TruthConstant}）</dd>
	 *  <dd>数値監視：重要度定数（{@link com.clustercontrol.bean.PriorityConstant}）</dd>
	 *  <dd>文字列監視：順序（{@link com.clustercontrol.monitor.run.ejb.entity.MonitorStringValueInfoBean#getOrder_no()}）</dd>
	 * </dl>
	 */
	protected TreeMap<Integer, MonitorJudgementInfo> m_judgementInfoList;

	/** 監視取得値 */
	protected Double m_value = null;

	/**
	 * 引数で指定された監視情報の監視を実行します。
	 * （監視項目ごとに処理を行う監視を実行します）
	 * 監視ジョブでのみ使用します。
	 *
	 * @param monitorTypeId 監視対象ID
	 * @param monitorId 監視項目ID
	 * @param facilityId ファシリティID
	 * @param prvData 前回の実施結果
	 * @throws FacilityNotFound
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see #runMonitorInfo()
	 */
	public MonitorRunResultInfo runMonitor(String monitorTypeId, String monitorId, String facilityId, Object prvData) throws FacilityNotFound, MonitorNotFound, InvalidRole, HinemosUnknown {

		this.initialize(monitorTypeId);

		m_isMonitorJob = true;
		m_facilityId = facilityId;
		m_monitorTypeId = monitorTypeId;
		m_monitorId = monitorId;
		m_prvData = prvData;

		boolean isSuccess = false;

		try
		{
			// 監視実行
			isSuccess = runMonitorInfo();
		} finally {
			if(!isSuccess){
				String[] args = {m_monitorTypeId,""};
				AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.MONITOR, MessageConstant.MESSAGE_SYS_012_MON, args);
			}
			// 終了処理
			this.terminate();
			
		}
		// 処理結果を返す。
		return m_monitorRunResultInfo;
	}

	/**
	 * 引数で指定された監視情報の監視を実行します。
	 * （監視項目ごとに処理を行う監視を実行します）
	 *
	 * @param monitorTypeId 監視対象ID
	 * @param monitorId 監視項目ID
	 * @throws FacilityNotFound
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see #runMonitorInfo()
	 */
	public void runMonitor(String monitorTypeId, String monitorId) throws FacilityNotFound, MonitorNotFound, InvalidRole, HinemosUnknown {

		this.initialize(monitorTypeId);

		m_monitorTypeId = monitorTypeId;
		m_monitorId = monitorId;

		boolean isSuccess = false;

		try
		{
			// 監視実行
			isSuccess = runMonitorInfo();
		} finally {
			if(!isSuccess){
				String[] args = {m_monitorTypeId,m_monitorId};
				AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.MONITOR, MessageConstant.MESSAGE_SYS_012_MON, args);
			}
			// 終了処理
			this.terminate();
		}
	}

	/**
	 * 監視を実行します。（並列処理）
	 * <p>
	 * <ol>
	 * <li>監視情報を取得し、保持します（{@link #setMonitorInfo(String, String)}）。</li>
	 * <li>判定情報を取得し、判定情報マップに保持します（{@link #setJudgementInfo()}）。</li>
	 * <li>チェック条件情報を取得し、保持します（{@link #setCheckInfo()}）。</li>
	 * <li>ファシリティ毎に並列に監視を実行し、値を収集します。 （{@link #collect(String)}）。</li>
	 * <li>監視結果から、判定結果を取得します。 （{@link #getCheckResult(boolean)}）。</li>
	 * <li>監視結果から、重要度を取得します（{@link #getPriority(int)}）。</li>
	 * <li>監視結果を通知します（{@link #notify(boolean, String, int, Date)}）。</li>
	 * </ol>
	 *
	 * @return 実行に成功した場合、</code> true </code>
	 * @throws FacilityNotFound
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 *
	 * @see #setMonitorInfo(String, String)
	 * @see #setJudgementInfo()
	 * @see #setCheckInfo()
	 * @see #collect(String)
	 * @see #getCheckResult(boolean)
	 * @see #getPriority(int)
	 * @see #notify(boolean, String, int, Date)
	 */
	protected boolean runMonitorInfo() throws FacilityNotFound, MonitorNotFound, InvalidRole, EntityExistsException, HinemosUnknown {

		m_now = HinemosTime.getDateInstance();

		m_priorityMap = new HashMap<Integer, ArrayList<String>>();
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_INFO),		new ArrayList<String>());
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_WARNING),	new ArrayList<String>());
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_CRITICAL),	new ArrayList<String>());
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_UNKNOWN),	new ArrayList<String>());
		List<Sample> sampleList = new ArrayList<Sample>();
		List<StringSample> collectedSamples = new ArrayList<>();
		
		try
		{
			// 監視基本情報を設定
			boolean run = this.setMonitorInfo(m_monitorTypeId, m_monitorId);
			if(!run){
				// 処理終了
				return true;
			}

			// 判定情報を設定
			setJudgementInfo();

			// チェック条件情報を設定
			setCheckInfo();

			ArrayList<String> facilityList = null;
			ExecutorCompletionService<MonitorRunResultInfo> ecs = new ExecutorCompletionService<MonitorRunResultInfo>(ParallelExecution.instance().getExecutorService());
			int taskCount = 0;

			if (!m_isMonitorJob) {
				// 監視ジョブ以外の場合
				// ファシリティIDの配下全ての一覧を取得
				// 有効/無効フラグがtrueとなっているファシリティIDを取得する
				facilityList = new RepositoryControllerBean().getExecTargetFacilityIdList(m_facilityId, m_monitor.getOwnerRoleId());
				if (facilityList.size() == 0) {
					return true;
				}

				m_isNode = new RepositoryControllerBean().isNode(m_facilityId);

				// 監視対象となっているノードの変数を取得
				nodeInfo = new HashMap<String, NodeInfo>();
				for (String facilityId : facilityList) {
					try {
						synchronized (this) {
							nodeInfo.put(facilityId, new RepositoryControllerBean().getNode(facilityId));
						}
					} catch (FacilityNotFound e) {
						// 何もしない
					}
				}

				m_log.debug("monitor start : monitorTypeId : " + m_monitorTypeId + ", monitorId : " + m_monitorId);

				/**
				 * 監視の実行
				 */
				// ファシリティIDの数だけ、各監視処理を実行する
				Iterator<String> itr = facilityList.iterator();
				while(itr.hasNext()){
					String facilityId = itr.next();
					if(facilityId != null && !"".equals(facilityId)){

						// マルチスレッド実行用に、RunMonitorのインスタンスを新規作成する
						// インスタンスを新規作成するのは、共通クラス部分に監視結果を保持するため
						RunMonitor runMonitor = this.createMonitorInstance();

						// 監視実行に必要な情報を再度セットする
						runMonitor.m_monitorTypeId = this.m_monitorTypeId;
						runMonitor.m_monitorId = this.m_monitorId;
						runMonitor.m_now = this.m_now;
						runMonitor.m_priorityMap = this.m_priorityMap;
						runMonitor.setMonitorInfo(runMonitor.m_monitorTypeId, runMonitor.m_monitorId);
						runMonitor.setJudgementInfo();
						runMonitor.setCheckInfo();
						runMonitor.nodeInfo = this.nodeInfo;

						ecs.submit(new MonitorExecuteTask(runMonitor, facilityId));
						taskCount++;
						
						if (m_log.isDebugEnabled()) {
							m_log.debug("starting monitor result : monitorId = " + m_monitorId + ", facilityId = " + facilityId);
						}
					}
					else {
						facilityList.remove(facilityId);
					}
				}

			} else {
				// 監視ジョブの場合
				// ファシリティIDの配下全ての一覧を取得
				// 有効/無効フラグがtrueとなっているファシリティIDを取得する
				facilityList = new RepositoryControllerBean().getExecTargetFacilityIdList(m_facilityId, m_monitor.getOwnerRoleId());
				if (facilityList.size() != 1
						|| !facilityList.get(0).equals(m_facilityId) ) {
					return true;
				}

				m_isNode = true;

				// 監視対象となっているノードの変数を取得
				nodeInfo = new HashMap<String, NodeInfo>();
				try {
					synchronized (this) {
						nodeInfo.put(m_facilityId, new RepositoryControllerBean().getNode(m_facilityId));
					}
				} catch (FacilityNotFound e) {
					// 何もしない
				}
				m_log.debug("monitor start : monitorTypeId : " + m_monitorTypeId + ", monitorId : " + m_monitorId);

				/**
				 * 監視の実行
				 */
				// マルチスレッド実行用に、RunMonitorのインスタンスを新規作成する
				// インスタンスを新規作成するのは、共通クラス部分に監視結果を保持するため
				RunMonitor runMonitor = this.createMonitorInstance();

				// 監視実行に必要な情報を再度セットする
				runMonitor.m_isMonitorJob = this.m_isMonitorJob;
				runMonitor.m_monitorTypeId = this.m_monitorTypeId;
				runMonitor.m_monitorId = this.m_monitorId;
				runMonitor.m_now = this.m_now;
				runMonitor.m_priorityMap = this.m_priorityMap;
				runMonitor.setMonitorInfo(runMonitor.m_monitorTypeId, runMonitor.m_monitorId);
				runMonitor.setJudgementInfo();
				runMonitor.setCheckInfo();
				runMonitor.nodeInfo = this.nodeInfo;
				runMonitor.m_prvData = this.m_prvData;

				ecs.submit(new MonitorExecuteTask(runMonitor, m_facilityId));
				taskCount++;

				if (m_log.isDebugEnabled()) {
					m_log.debug("starting monitor result : monitorId = " + m_monitorId + ", facilityId = " + m_facilityId);
				}
			}

			/**
			 * 監視結果の集計
			 */
			MonitorRunResultInfo result = new MonitorRunResultInfo();	// 監視結果を格納

			m_log.debug("total start : monitorTypeId : " + m_monitorTypeId + ", monitorId : " + m_monitorId);

			// 収集値の入れ物を作成
			StringSample strSample = null;
			Sample sample = null;
			if(m_monitor.getCollectorFlg()){
				//収集 - 文字列
				if (m_monitor.getMonitorType() == MonitorTypeConstant.TYPE_STRING 
						|| m_monitor.getMonitorType() == MonitorTypeConstant.TYPE_TRAP ) {
					strSample = new StringSample(HinemosTime.getDateInstance(), m_monitor.getMonitorId());
				}
				//収集 - 数値
				else {
					sample = new Sample(HinemosTime.getDateInstance(), m_monitor.getMonitorId());
				}
			}
			
			for (int i = 0; i < taskCount; i++) {
				Future<MonitorRunResultInfo> future = ecs.take();
				result = future.get();	// 監視結果を取得
				
				String facilityId = result.getFacilityId();
				m_nodeDate = result.getNodeDate();
				
				if (m_log.isDebugEnabled()) {
					m_log.debug("finished monitor : monitorId = " + m_monitorId + ", facilityId = " + facilityId);
				}
				
				//文字列は、処理しないものでも収集する。
				if (m_monitor.getMonitorType() == MonitorTypeConstant.TYPE_STRING 
						|| m_monitor.getMonitorType() == MonitorTypeConstant.TYPE_TRAP ) {
					if (strSample != null) {
						strSample.set(facilityId, m_monitor.getMonitorTypeId(), result.getMessageOrg());
					}
				}
				
				if (!m_isMonitorJob) {
					// 処理する場合
					if(result.getProcessType().booleanValue()){
						// 監視結果を通知
						notify(true, facilityId, result.getCheckResult(), new Date(m_nodeDate), result);
						// 個々の収集値の登録
						if(sample != null){
							int errorType = -1;
							if(result.isCollectorResult()){
								errorType = CollectedDataErrorTypeConstant.NOT_ERROR;
							}else{
								errorType = CollectedDataErrorTypeConstant.UNKNOWN;
							}
							sample.set(facilityId, m_monitor.getItemName(), result.getValue(), errorType);
						}
					}
				} else {
					m_monitorRunResultInfo = new MonitorRunResultInfo();
					m_monitorRunResultInfo.setPriority(result.getPriority());
					m_monitorRunResultInfo.setCheckResult(result.getCheckResult());
					m_monitorRunResultInfo.setNodeDate(m_nodeDate);
					m_monitorRunResultInfo.setMessageOrg(makeJobOrgMessage(result.getMessageOrg(), result.getMessage()));
					m_monitorRunResultInfo.setCurData(result.getCurData());
				}
			}

			// 収集値をまとめて登録
			if (m_monitor.getMonitorType() == MonitorTypeConstant.TYPE_STRING 
					|| m_monitor.getMonitorType() == MonitorTypeConstant.TYPE_TRAP ) {
				//収集 - 文字列をまとめて格納
				if (strSample != null) {
					collectedSamples.add(strSample);
				}
				if (!collectedSamples.isEmpty()) {
					CollectStringDataUtil.store(collectedSamples);
				}
			} else {
				if(sample != null){
					sampleList.add(sample);
				}
				if(!sampleList.isEmpty()){
					CollectDataUtil.put(sampleList);
				}
			}
			
			m_log.debug("monitor end : monitorTypeId : " + m_monitorTypeId + ", monitorId : " + m_monitorId);

			return true;

		} catch (FacilityNotFound e) {
			throw e;
		} catch (InterruptedException e) {
			m_log.info("runMonitorInfo() monitorTypeId = " + m_monitorTypeId + ", monitorId  = " + m_monitorId + " : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			return false;
		} catch (ExecutionException e) {
			m_log.info("runMonitorInfo() monitorTypeId = " + m_monitorTypeId + ", monitorId  = " + m_monitorId + " : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			return false;
		}
	}

	/**
	 * 引数で指定された監視情報の監視を実行します。
	 * （監視対象ノードごとに処理を行う監視を実行します）
	 * 
	 * 本メソッドを呼び出す必要がある場合、この派生クラスにおいて必ず runMonitorInfo をオーバーライドすること。
	 * 本メソッドは、FacilityIdごとに集約する形で監視を行うことが効率的な監視の場合に呼ぶものであり、
	 * 基底クラスで実装された監視項目IDごとに監視をするためのrunMonitorInfoは目的に合致しない。
	 *
	 * @param monitorTypeId 監視対象ID
	 * @param facilityId ファシリティID
	 * @throws FacilityNotFound
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see #runMonitorInfo()
	 */
	public void runMonitorAggregateByNode(String monitorTypeId, String facilityId) throws FacilityNotFound, MonitorNotFound, InvalidRole, HinemosUnknown {

		this.initialize(monitorTypeId);

		m_monitorTypeId = monitorTypeId;
		m_facilityId = facilityId;
		m_now = HinemosTime.getDateInstance();

		boolean isSuccess = false;

		try
		{
			// 監視実行
			isSuccess = runMonitorInfoAggregateByNode();
		} finally {
			if(!isSuccess){
				String[] args = {m_monitorTypeId,facilityId}; 
				AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.MONITOR, MessageConstant.MESSAGE_SYS_013_MON, args);
			}
			// 終了処理
			this.terminate();
		}
	}
	
	/**
	 * FacilityIdごとに処理する監視を実行します
	 * 
	 * @return 実行に成功した場合、</code> true </code>
	 * @throws FacilityNotFound
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 *
	 */
	private boolean runMonitorInfoAggregateByNode() throws FacilityNotFound, MonitorNotFound, InvalidRole, EntityExistsException, HinemosUnknown {
		
		if (!m_isMonitorJob) {
			// 現在時刻から、今はどの監視間隔の監視を行なえばよいのかを計算し、タスクリストに追加する
			final RepositoryControllerBean repository = new RepositoryControllerBean();
			final NodeInfo targetNode = repository.getNode(m_facilityId);
			final Set<Integer> currentTask = NodeMonitorPollerController.calcCurrentExecMonitorIntervals(targetNode);
			
			final Set<Integer> plannedTask = getPlannedTasksForNodeAggregateMonitor(m_monitorTypeId, m_facilityId);
			synchronized (plannedTask) {
				plannedTask.addAll(currentTask);
			}
			
			final NodeToMonitorCache node2monitor = NodeToMonitorCache.getInstance(m_monitorTypeId);
			final Semaphore execSingleThreadSemaphore = getSemaphoreForNodeAggregateMonitor(m_monitorTypeId, m_facilityId);
			
			// 次のtryブロック内は実際のポーリング処理等を行うため、各ノードあたり1つのスレッドしか走行してはならない。
			// Semaphoreを取得できない場合は先行して走っているスレッドの処理が遅れているため、
			// その旨メッセージを出して、この関数を終了する
			if (execSingleThreadSemaphore.tryAcquire() == false) {
				for (Map.Entry<Integer, Set<MonitorInfo>> entry : node2monitor.getMonitorsWithCalendar(m_facilityId, currentTask).entrySet()) {
					
					// それぞれ遅延していることを通知する
					final Set<MonitorInfo> monitors = entry.getValue();
					for (MonitorInfo monitor : monitors) {
						String notifyGroupId = monitor.getNotifyGroupId();
						
						// 通知情報を設定
						OutputBasicInfo notifyInfo = new OutputBasicInfo();
						notifyInfo.setPluginId(m_monitorTypeId);
						notifyInfo.setMonitorId(monitor.getMonitorId());
						notifyInfo.setApplication(monitor.getApplication());

						String facilityPath = new RepositoryControllerBean().getFacilityPath(m_facilityId, null);
						notifyInfo.setFacilityId(m_facilityId);
						notifyInfo.setScopeText(facilityPath);

						// 重要度は不明
						int priority = PriorityConstant.TYPE_UNKNOWN;
						
						String message = MessageConstant.MESSAGE_MONITOR_UNCOMPLETED.getMessage();
						String messageOrg = MessageConstant.MESSAGE_MONITOR_UNCOMPLETED.getMessage();
						notifyInfo.setPriority(priority);
						notifyInfo.setMessage(message);
						notifyInfo.setMessageOrg(messageOrg);
						notifyInfo.setGenerationDate(m_now.getTime());

						// for debug
						if (m_log.isDebugEnabled()) {
							m_log.debug("notify() priority = " + priority
									+ " , message = " + message
									+ " , messageOrg = " + messageOrg
									+ ", generationDate = " + m_now);
						}
	
						// ログ出力情報を送信
						if (m_log.isDebugEnabled()) {
							m_log.debug("sending message"
									+ " : priority=" + notifyInfo.getPriority()
									+ " generationDate=" + notifyInfo.getGenerationDate() + " pluginId=" + notifyInfo.getPluginId()
									+ " monitorId=" + notifyInfo.getMonitorId() + " facilityId=" + notifyInfo.getFacilityId()
									+ " subKey=" + notifyInfo.getSubKey()
									+ ")");
						}

						new NotifyControllerBean().notify(notifyInfo, notifyGroupId);
					}
				}

				// ここでログを出しているので、戻った先でログを出さないように成功としてtrueを返す
				return true;
			}
			
			// 以下の処理はノードあたり1スレッドのみが進入可能。最後に必ずSemaphoreをリリースすること
			try {
				// 事前に予定されているタスク（監視間隔の集合）を取得し、予定されたタスクリストは空にする
				Set<Integer> execTargetInterval = null;
				synchronized (plannedTask) {
					execTargetInterval = new HashSet<>(plannedTask);
					plannedTask.clear();
				}
				
				// 実際の収集処理を行なう（派生クラスのpreCollectにて、管理対象から必要となる値を収集してくる。つまりpreCollectには時間がかかる可能性がある）
				final Object preCollectData = preCollect(execTargetInterval);
				
				// 以下では各監視項目に応じたRunMonitorを作成し、runMonitorListに格納していく
				final List<RunMonitor> runMonitorList = new ArrayList<>();
				Set<MonitorInfo> monitorInfos = new HashSet<>();
				for (Set<MonitorInfo> addMonitors : node2monitor.getMonitorsWithCalendar(m_facilityId, execTargetInterval).values()) {
					monitorInfos.addAll(addMonitors);
				}
				// このノードの変数を取得（監視項目集約型の場合、複数のFacilityIdが存在するのでMapとなっているが、ここでは1個しか登録しない）
				final Map<String, NodeInfo> nodeinfoMap = new HashMap<>();
				nodeinfoMap.put(m_facilityId, targetNode);
				for (final MonitorInfo monitorInfo : monitorInfos) {
					// 対象監視項目IDでrunMonitorを生成し、各種値を設定する
					RunMonitor runMonitor = this.createMonitorInstance();
					runMonitor.m_monitorTypeId = m_monitorTypeId;
					runMonitor.m_monitorId = monitorInfo.getMonitorId();
					runMonitor.m_now = m_now;
					if (runMonitor.setMonitorInfo(m_monitorTypeId, monitorInfo.getMonitorId())) {
						runMonitorList.add(runMonitor);
						
						// setMonitorInfoによって監視設定から自動的にFacilityIdがセットされるが
						// この関数がキックされる契機となった対象ノードそのものを指定する必要があるため設定しなおす
						runMonitor.m_facilityId = m_facilityId;
						
						// その他、runMonitorに対して、監視項目集約型のrunMonitorInfoと同様の前処理を行う
						// TODO 監視項目集約型には m_priorityMap の設定があるが、これについてはどのように処理をするか不明
						runMonitor.setJudgementInfo();
						runMonitor.nodeInfo = nodeinfoMap;
						runMonitor.setCheckInfo();
						runMonitor.m_isNode = true;
					}
				}
				
				// 収集した値を元に閾値判定を行なう
				checkMultiMonitorInfoData(preCollectData, runMonitorList);
			} finally {
				execSingleThreadSemaphore.release();
			}
		} else {
			// 実際の収集処理を行なう
			final Object preCollectData = preCollect(null);

			// 対象監視項目IDでrunMonitorを生成し、各種値を設定する
			setJudgementInfo();
			final RepositoryControllerBean repository = new RepositoryControllerBean();
			final NodeInfo targetNode = repository.getNode(m_facilityId);
			final Map<String, NodeInfo> nodeinfoMap = new HashMap<>();
			nodeinfoMap.put(m_facilityId, targetNode);
			nodeInfo = nodeinfoMap;
			setCheckInfo();
			m_isNode = true;
				
			// 収集した値を元に閾値判定を行なう
			checkMultiMonitorInfoData(preCollectData, Arrays.asList(this));
		}
		return true;
	}

	/**
	 * 引数で指定された監視情報の監視を実行します。
	 * （監視対象ノードごとに処理を行う監視を実行します）
	 * 監視ジョブでのみ使用します。
	 * 
	 * 本メソッドを呼び出す必要がある場合、この派生クラスにおいて必ず runMonitorInfo をオーバーライドすること。
	 * 本メソッドは、FacilityIdごとに集約する形で監視を行うことが効率的な監視の場合に呼ぶものであり、
	 * 基底クラスで実装された監視項目IDごとに監視をするためのrunMonitorInfoは目的に合致しない。
	 *
	 * @param monitorTypeId 監視対象ID
	 * @param monitorId 監視項目ID
	 * @param facilityId ファシリティID
	 * @param prvData 前回の実施結果
	 * @throws FacilityNotFound
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see #runMonitorInfo()
	 */
	public MonitorRunResultInfo runMonitorAggregateByNode(String monitorTypeId, String monitorId, String facilityId, Object prvData) throws FacilityNotFound, MonitorNotFound, InvalidRole, HinemosUnknown {

		this.initialize(monitorTypeId);

		m_isMonitorJob = true;
		m_monitorTypeId = monitorTypeId;
		m_monitorId = monitorId;
		m_facilityId = facilityId;
		m_now = HinemosTime.getDateInstance();
		m_prvData = prvData;
		
		// 監視基本情報を取得
		m_monitor = QueryUtil.getMonitorInfoPK_NONE(monitorId);

		boolean isSuccess = false;

		try
		{
			// 監視実行
			isSuccess = runMonitorInfoAggregateByNode();
		} finally {
			if(!isSuccess){
				String[] args = {m_monitorTypeId, m_facilityId}; 
				AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.MONITOR, MessageConstant.MESSAGE_SYS_013_MON, args);
			}
			// 終了処理
			this.terminate();
		}
		// 処理結果を返す。
		return m_monitorRunResultInfo;
	}
	
	/**
	 * 特定の監視タイプ・ノードにおける、現時点までにつまれているタスク
	 * （正確には、実行すべき「監視間隔」のSet）を返す。
	 * 
	 * ここで返されるSetを変更する場合、必ずこのSet自身でsynchronizedしなくてはならない
	 * 
	 * @param monitorType
	 * @param facilityId
	 * @return
	 */
	private static Set<Integer> getPlannedTasksForNodeAggregateMonitor(String monitorType, String facilityId) {
		// モニタタイプごとのインスタンスを取得（無ければ作成して登録）
		ConcurrentMap<String, Set<Integer>> node2task = plannedTaskForNodeAggregateMonitor.get(monitorType);
		if (node2task == null) {
			ConcurrentMap<String, Set<Integer>> newNode2task = new ConcurrentHashMap<>();
			node2task = plannedTaskForNodeAggregateMonitor.putIfAbsent(monitorType, newNode2task);
			if (node2task == null) {
				node2task = newNode2task;
			}
		}
		// さらにその中の各ノードのインスタンスを取得（無ければ作成して登録）
		Set<Integer> tasks = node2task.get(facilityId);
		if (tasks == null) {
			Set<Integer> newTasks = new HashSet<>();
			tasks = node2task.putIfAbsent(facilityId, newTasks);
			if (tasks == null) {
				tasks = newTasks;
			}
		}
		return tasks;
	}
	
	private static final ConcurrentMap<String, ConcurrentMap<String, Set<Integer>>> plannedTaskForNodeAggregateMonitor = new ConcurrentHashMap<>();
	
	/**
	 * 監視タイプ＋ノードごとに1スレッドのみ処理可能とするためのSemaphoreを返す
	 * @param monitorType 対象となる監視タイプ（リソースやプロセス）
	 * @param facilityId 対象となるノードのFacilityId
	 * @return
	 */
	private static Semaphore getSemaphoreForNodeAggregateMonitor(String monitorType, String facilityId) {
		if (nodeSemaphore.containsKey(monitorType) == false) {
			nodeSemaphore.putIfAbsent(monitorType, new ConcurrentHashMap<String, Semaphore>());
		}
		final Semaphore semaphore = nodeSemaphore.get(monitorType).get(facilityId);
		if (semaphore != null) {
			return semaphore;
		}
		
		final Semaphore newSemaphore = new Semaphore(1);
		final Semaphore oldSemaphore = nodeSemaphore.get(monitorType).putIfAbsent(facilityId, newSemaphore);
		if (oldSemaphore != null) {
			return oldSemaphore;
		} else {
			return newSemaphore;
		}
	}
	
	/**
	 * キーが監視タイプ（リソースやプロセス）、値がFacilityIdとそのノード用のSemaphoreのマップ
	 */
	private static final ConcurrentMap<String, ConcurrentMap<String, Semaphore>> nodeSemaphore = new ConcurrentHashMap<>();
	

	/**
	 * 複数の監視項目に対する、閾値判定処理・通知・収集値登録を行なう。
	 * 本関数は、ノード集約型監視（プロセス監視・リソース監視）でのみ利用すること。
	 * 
	 * @param preCollectData
	 * @param runMonitorList
	 */
	protected void checkMultiMonitorInfoData(final Object preCollectData, final List<RunMonitor> runMonitorList) {
		// 複数の監視項目について並行して閾値判定するメリットはほとんど無いため、全監視項目の判定・通知を直列に実施する
		List<Sample> sampleList = new ArrayList<Sample>();
		for (final RunMonitor targetMonitor : runMonitorList) {
			final String facilityId = targetMonitor.m_facilityId;
			targetMonitor.preCollectData = preCollectData;
			try {
				// 対象から収集済みのデータから判定のための情報を作る処理（プロセス監視のカウント処理）
				// 監視項目集約型においてはexecutorに渡すCallableタスククラスのほうにいくつかの処理があるため、
				// ここでは直接runMonitor.collectを起動せず、Callableタスクからcallを呼び出している。
				final MonitorRunResultInfo result = new MonitorExecuteTask(targetMonitor, m_facilityId).call();
				if (m_log.isDebugEnabled())
					m_log.debug("runMonitorInfoAggregatedFacilityId() : finished RunMonitor.collect." +
							" facilityId = " + facilityId + 
							", monitorTypeId = " + m_monitorTypeId +
							", monitorId = " + targetMonitor.m_monitorId);
				
				// 通知処理と性能収集処理
				if(result.getProcessType().booleanValue()){
					// 監視結果を通知
					if (!m_isMonitorJob) {
						targetMonitor.notify(true, facilityId, result.getCheckResult(), new Date(result.getNodeDate()), result);
					} else {
						m_monitorRunResultInfo = new MonitorRunResultInfo();
						m_monitorRunResultInfo.setNodeDate(m_nodeDate);
						m_monitorRunResultInfo.setCurData(result.getCurData());
						m_monitorRunResultInfo.setPriority(result.getPriority());
						m_monitorRunResultInfo.setMessageOrg(makeJobOrgMessage(result.getMessage(), null));
					}
					
					// 収集が有効な場合、収集値を登録
					if(targetMonitor.m_monitor.getCollectorFlg().booleanValue()) {
						int errorType = 0;
						Sample sample = new Sample(HinemosTime.getDateInstance(), targetMonitor.m_monitor.getMonitorId());
						if(result.isCollectorResult()){
							errorType = CollectedDataErrorTypeConstant.NOT_ERROR;
						}else{
							errorType = CollectedDataErrorTypeConstant.UNKNOWN;
						}
						sample.set(facilityId, targetMonitor.getMonitorInfo().getItemName(), result.getValue(), errorType);
						sampleList.add(sample);
					}
				}
				if (m_log.isDebugEnabled()) 
					m_log.debug("runMonitorInfoAggregatedFacilityId() : finished notify and store perf data." +
							" facilityId = " + facilityId + 
							", monitorTypeId = " + targetMonitor.m_monitorTypeId +
							", monitorId = " + targetMonitor.m_monitorId);
			} catch (Exception e) {
				m_log.warn("runMonitorInfoAggregatedFacilityId() : facilityId = " + facilityId +
						", monitorTypeId = " + targetMonitor.m_monitorTypeId +
						", monitorId  = " + targetMonitor.m_monitorId, e);
			}
		}
		if(!sampleList.isEmpty()){
			CollectDataUtil.put(sampleList);
		}
	}
	
	/**
	 * ノード集約型監視（ノードごとに複数の監視項目を一度に監視することで効率が上がる監視、例えばプロセス・リソース監視等）において、
	 * 各監視項目の閾値判定処理を行う前に、監視対象からデータを取得してデータの塊を返却する。
	 * 
	 * ノード集約型監視を実装する場合、派生クラスにてこの関数をオーバーライドすること。
	 * 
	 * @return
	 * @throws HinemosUnknown 
	 * @throws FacilityNotFound 
	 */
	protected Object preCollect(Set<Integer> execMonitorIntervals) throws HinemosUnknown, FacilityNotFound {
		// 必要に応じて派生クラスでオーバーライド
		return null;
	}
	
	protected Object preCollectData;
	
	/**
	 * 判定情報を設定します。
	 * <p>
	 * 各監視種別（真偽値，数値，文字列）のサブクラスで実装します。
	 * 監視情報より判定情報を取得し、判定情報マップに保持します。
	 */
	protected abstract void setJudgementInfo();

	/**
	 * チェック条件情報を設定します。
	 * <p>
	 * 各監視管理のサブクラスで実装します。
	 * 監視情報よりチェック条件情報を取得し、保持します。
	 *
	 * @throws MonitorNotFound
	 */
	protected abstract void setCheckInfo() throws MonitorNotFound ;

	/**
	 * 監視対象に対する監視を実行し、値を収集します。
	 * <p>
	 * 各監視管理のサブクラスで実装します。
	 * 引数で指定されたファシリティIDの監視を実行して値を収集し、
	 * 各監視種別（真偽値，数値，文字列）のサブクラスの監視取得値にセットします。
	 *
	 * @param facilityId 監視対象のファシリティID
	 * @return 値取得に成功した場合、</code> true </code>
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 */
	public abstract boolean collect(String facilityId) throws FacilityNotFound, HinemosUnknown;

	/**
	 * マルチスレッドを実現するCallableTaskに渡すためのインスタンスを作成するメソッドです。
	 * 監視を実装するクラスでは、RunMonitorクラス（すべての監視実装クラスの親クラス）のインスタンスを返すために、
	 * このメソッドを実装してください。
	 * このメソッドで生成されたインスタンスは、監視実行スレッドごとの監視結果を保持するために利用されます。
	 *
	 * すべての監視はマルチスレッドで動作しており、監視設定単位でRunMonitorクラスのインスタンスを共有しています。
	 * 監視結果（収集値）は、数値監視、文字列監視、真偽値監視のレベルで共有される変数に格納されるため、
	 * 同一監視設定で複数ノードに対して監視を実行する場合、監視結果に不整合が生じる可能性があります。
	 *
	 * したがって、本メソッドによって新たにインスタンスを生成し、マルチスレッドを実現するCallableTaskに渡す必要があります。
	 *
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#runMonitorInfo()
	 * @see com.clustercontrol.monitor.run.factory.RunMonitorNumericValueType
	 * @see com.clustercontrol.monitor.run.factory.RunMonitorStringValueType
	 * @see com.clustercontrol.monitor.run.factory.RunMonitorTruthValueType
	 * @see com.clustercontrol.monitor.run.util.MonitorExecuteTask
	 *
	 * @return
	 * @throws HinemosUnknown
	 */
	protected abstract RunMonitor createMonitorInstance() throws HinemosUnknown;

	/**
	 * 判定結果を返します。
	 * <p>
	 * 各監視種別（真偽値，数値，文字列）のサブクラスで実装します。
	 * {@link #collect(String)}メソッドで監視を実行した後に呼ばれます。
	 * 監視取得値と判定情報から、判定結果を返します。
	 * <p>
	 * <dl>
	 *  <dt>判定結果の値</dt>
	 *  <dd>真偽値監視：真偽値定数（{@link com.clustercontrol.monitor.run.bean.TruthConstant}）</dd>
	 *  <dd>数値監視：重要度定数（{@link com.clustercontrol.bean.PriorityConstant}）</dd>
	 *  <dd>文字列監視：順序（{@link com.clustercontrol.monitor.run.ejb.entity.MonitorStringValueInfoBean#getOrder_no()}）</dd>
	 * </dl>
	 *
	 * @param ret 監視の実行が成功した場合、</code> true </code>
	 * @return 判定結果
	 */
	public abstract int getCheckResult(boolean ret);

	/**
	 * 重要度を返します。
	 * <p>
	 * 引数で指定された判定結果のキーに対応する重要度を、判定情報マップから取得します。
	 *
	 * @param key 各監視種別（真偽値，数値，文字列）の判定結果のキー
	 * @return 重要度
	 * @since 2.0.0
	 */
	public int getPriority(int key) {

		MonitorJudgementInfo info = m_judgementInfoList.get(key);
		if(info != null){
			return info.getPriority();
		}
		else{
			return m_failurePriority;
		}
	}

	/**
	 * 通知グループIDを返します。
	 *
	 * @return 通知グループID
	 * @since 2.1.0
	 */
	public String getNotifyGroupId(){
		return m_notifyGroupId;
	}
	
	/**
	 * ノード用メッセージを設定します。
	 * 
	 * プロセス監視・リソース監視でのみ利用します。
	 */
	public void setMessage(String message) {
		// Do nothing
	}

	/**
	 * ノード用メッセージを返します。
	 * <p>
	 * 引数で指定された判定結果のキーに対応するメッセージを、判定情報マップから取得します。
	 *
	 * @param key 各監視種別（真偽値，数値，文字列）の判定結果のキー
	 * @return メッセージ
	 * @since 2.0.0
	 */
	public String getMessage(int key) {

		MonitorJudgementInfo info = m_judgementInfoList.get(Integer.valueOf(key));
		if(info != null){
			if(info.getMessage() != null){
				return info.getMessage();
			}
		}
		return "";
	}

	/**
	 * ノード用オリジナルメッセージを返します。
	 * <p>
	 * 各監視管理のサブクラスで実装します。
	 *
	 * @param key 各監視種別（真偽値，数値，文字列）の判定結果のキー
	 * @return オリジナルメッセージ
	 * @since 2.0.0
	 */
	public abstract String getMessageOrg(int key);

	/**
	 * スコープ用メッセージを返します。
	 * <p>
	 * 引数で指定された重要度に対応するメッセージを返します。
	 *
	 * @param priority 重要度
	 * @return メッセージ
	 */
	protected String getMessageForScope(int priority){

		if(priority == PriorityConstant.TYPE_INFO){
			return MESSAGE_INFO;
		}
		else if(priority == PriorityConstant.TYPE_WARNING){
			return MESSAGE_WARNING;
		}
		else if(priority == PriorityConstant.TYPE_CRITICAL){
			return MESSAGE_CRITICAL;
		}
		else{
			return MESSAGE_UNKNOWN;
		}
	}

	/**
	 * スコープ用オリジナルメッセージを返します。
	 * <p>
	 * 重要度別の件数，ファシリティ名を表示する文字列を作成し、返します。
	 *
	 * <dl>
	 *  <dt>通知:X件, 警告:X件, 危険:X件, 不明:X件</dt>
	 *  <dt>通知:</dt>
	 * 	 <dd>NODE1</dd>
	 *   <dd>NODE2</dd>
	 *  <dt>警告:</dt>
	 *   <dd>NODE3</dd>
	 *  <dt>危険:</dt>
	 * 	 <dd>NODE4</dd>
	 *  <dt>不明:</dt>
	 *   <dd>NODE5</dd>
	 * </dl>
	 *
	 * @param priority 重要度
	 * @return オリジナルメッセージ
	 */
	protected String getMessageOrgForScope(int priority){

		ArrayList<String> info = m_priorityMap.get(Integer.valueOf(PriorityConstant.TYPE_INFO));
		ArrayList<String> warning = m_priorityMap.get(Integer.valueOf(PriorityConstant.TYPE_WARNING));
		ArrayList<String> critical = m_priorityMap.get(Integer.valueOf(PriorityConstant.TYPE_CRITICAL));
		ArrayList<String> unknown = m_priorityMap.get(Integer.valueOf(PriorityConstant.TYPE_UNKNOWN));

		// 重要度別の件数
		StringBuffer summary = new StringBuffer();
		summary.append(MessageConstant.INFO.getMessage() + ":" + info.size() + MessageConstant.RECORD.getMessage() + ", ");
		summary.append(MessageConstant.WARNING.getMessage() + ":" + warning.size() + MessageConstant.RECORD.getMessage() + ", ");
		summary.append(MessageConstant.CRITICAL.getMessage() + ":" + critical.size() + MessageConstant.RECORD.getMessage() + ", ");
		summary.append(MessageConstant.UNKNOWN.getMessage() + ":" + unknown.size() + MessageConstant.RECORD.getMessage());

		// 重要度別のファシリティ名
		StringBuffer detail = new StringBuffer();
		detail.append(getItemListString("\n" + MessageConstant.INFO.getMessage(), info));
		detail.append(getItemListString("\n" + MessageConstant.WARNING.getMessage(), warning));
		detail.append(getItemListString("\n" + MessageConstant.CRITICAL.getMessage(), critical));
		detail.append(getItemListString("\n" + MessageConstant.UNKNOWN.getMessage(), unknown));
		return summary.toString() + detail.toString();
	}

	/**
	 * ノードの監視結果取得時刻を返します。
	 *
	 * @return ノードの監視結果取得時刻
	 * @since 3.0.0
	 */
	public long getNodeDate() {
		return m_nodeDate;
	}


	/**
	 * 監視値を返します。
	 *
	 * @return 監視値
	 * @since 4.0.0
	 */
	public Double getValue() {
		return m_value;
	}


	/**
	 * 今回の監視結果を返します。
	 *
	 * @return 前回の監視結果
	 * @since 4.0.0
	 */
	public Object getCurData() {
		return m_curData;
	}

	/**
	 * 監視情報を設定します。
	 * <p>
	 * 引数で指定された監視対象と監視項目の監視情報を取得し、保持します。<BR>
	 * 通知IDが指定されていない場合は、処理を終了します。<BR>
	 * カレンダIDが指定されていた場合は、稼動日か否かチェックします（{@link com.clustercontrol.calendar.session.CalendarControllerBean#isRun(java.lang.String, java.util.Date)}）。非稼動日の場合は、処理を終了します。
	 *
	 * @param monitorTypeId 監視対象ID
	 * @param monitorId 監視項目ID
	 * @return 監視を実行する場合、</code> true </code>
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 *
	 * @since 2.0.0
	 *
	 * @see com.clustercontrol.calendar.session.CalendarControllerBean#isRun(java.lang.String, java.util.Date)
	 */
	protected boolean setMonitorInfo(String monitorTypeId, String monitorId) throws MonitorNotFound, HinemosUnknown {
		m_isInCalendarTerm = true;
		m_isInNextCalendarTerm = false;

		// 監視基本情報を取得
		m_monitor = QueryUtil.getMonitorInfoPK_NONE(monitorId);

		if (!m_isMonitorJob) {
			// 監視ジョブ以外の場合

			// 通知情報を取得
			List<NotifyRelationInfo> notifyRelationList = 
					new NotifyControllerBean().getNotifyRelation(m_monitor.getNotifyGroupId());
			m_notifyGroupId = m_monitor.getNotifyGroupId();
			m_monitor.setNotifyRelationList(notifyRelationList);
			if(m_notifyGroupId == null || "".equals(m_notifyGroupId)){
				if(m_monitor.getMonitorType().intValue() != MonitorTypeConstant.TYPE_STRING){
					// 通知しない場合は、処理終了
					return false;
				}
			}

			// 収集間隔
			m_runInterval = m_monitor.getRunInterval().intValue();

			// カレンダID
			String calendarId = m_monitor.getCalendarId();
			if(calendarId != null && !"".equals(calendarId)){
				// 稼働日か否かチェック
				try {
					CalendarControllerBean calendar = new CalendarControllerBean();
					if(!calendar.isRun(calendarId, m_now==null?null:m_now.getTime()).booleanValue()){
						// 非稼働日の場合は、処理終了
						m_log.debug("setMonitorInfo() monitorTypeId = " + m_monitorTypeId + ", monitorId = " + m_monitorId  + ", calenderId = " + calendarId
								+ ". The monitor is not executed because of non-operating day.");
						m_isInCalendarTerm = false;

						// 次回の監視契機が稼働日か
						if(calendar.isRun(calendarId, m_now==null?null:(m_now.getTime() + (m_runInterval*1000))).booleanValue()){
							m_log.debug("setMonitorInfo() : monitorTypeId:" + m_monitorTypeId + ",monitorId:" + m_monitorId  + ",calenderId:" + calendarId + ". Next term is operating day.");
							m_isInNextCalendarTerm = true;
						}

						return false;
					}
				} catch (InvalidRole e) {
					// 指定されたカレンダIDの参照権限がない場合は、処理終了
					// 手動でない限りここは通らない。
					// （ADMINISTRATORSロールのユーザ、もしくはユーザ未指定の場合はオブジェクト権限をしないため）
					return false;
				} catch (CalendarNotFound e) {
					// 指定されたカレンダIDがすでに存在しない場合は、処理終了
					return false;
				}
			}
			// 監視対象ファシリティID
			m_facilityId = m_monitor.getFacilityId();
		}

		// 値取得失敗時の重要度
		m_failurePriority = m_monitor.getFailurePriority().intValue();

		return true;
	}

	/**
	 * 監視管理に通知します。
	 * <p>
	 * 通知に必要となる情報をログ出力情報にセットし、キューへメッセージを送信します。
	 * <ol>
	 * <li>通知IDを取得し、<code>null</code>の場合は、処理を終了します。</li>
	 * <li>各情報を取得し、ログ出力情報（{@link com.clustercontrol.monitor.message.LogOutputNotifyInfo }）にセットします。
	 * 監視単位がノードの場合とスコープの場合では、通知する情報が異なります。
	 * </li>
	 * <li>ログ出力情報を、ログ出力キューへ送信します。</li>
	 * </ol>
	 *
	 * @param isNode ノードの場合、</code> true </code>
	 * @param facilityId 通知対象のファシリティID
	 * @param result 判定結果
	 * @param generationDate ログ出力日時（監視を実行した日時）
	 * @throws HinemosUnknown
	 * @since 2.0.0
	 *
	 * @see com.clustercontrol.monitor.message.LogOutputNotifyInfo
	 * @see com.clustercontrol.repository.session.RepositoryControllerBean#getFacilityPath(java.lang.String, java.lang.String)
	 * @see #getNotifyRelationInfos()
	 * @see #getPriority(int)
	 * @see #getMessage(int)
	 * @see #getMessageOrg(int)
	 * @see #getJobRunInfo(int)
	 * @see #getMessageForScope(int)
	 * @see #getMessageOrgForScope(int)
	 * @see #getJobRunInfoForScope(int)
	 */
	protected void notify(
			boolean isNode,
			String facilityId,
			int result,
			Date generationDate) throws HinemosUnknown {
		// for debug
		if (m_log.isDebugEnabled()) {
			m_log.debug("notify() isNode = " + isNode + ", facilityId = " + facilityId
					+ ", result = " + result + ", generationDate = " + generationDate.toString());
		}

		// 監視無効の場合、通知しない
		if(!m_monitor.getMonitorFlg()){
			m_log.debug("notify() isNode = " + isNode + ", facilityId = " + facilityId
					+ ", result = " + result + ", generationDate = " + generationDate.toString()
					+ ", monitorFlg is false");
			return;
		}

		// 通知IDが指定されていない場合、通知しない
		String notifyGroupId = getNotifyGroupId();
		List<NotifyRelationInfo> notifyRelationList = m_monitor.getNotifyRelationList();
		if(notifyRelationList == null || notifyRelationList.size() == 0){
			return;
		}

		// 通知情報を設定
		OutputBasicInfo notifyInfo = new OutputBasicInfo();
		notifyInfo.setPluginId(m_monitorTypeId);
		notifyInfo.setMonitorId(m_monitorId);
		notifyInfo.setApplication(m_monitor.getApplication());

		String facilityPath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
		notifyInfo.setFacilityId(facilityId);
		notifyInfo.setScopeText(facilityPath);

		int priority = -1;
		String message = "";
		String messageOrg = "";

		if(isNode){
			// ノードの場合
			priority = getPriority(result);
			message = getMessage(result);
			messageOrg = getMessageOrg(result);
		}
		else{
			// スコープの場合
			priority = result;
			message = getMessageForScope(result);
			messageOrg = getMessageOrgForScope(result);
		}
		notifyInfo.setPriority(priority);
		notifyInfo.setMessage(message);
		notifyInfo.setMessageOrg(messageOrg);
		if (generationDate != null) {
			notifyInfo.setGenerationDate(generationDate.getTime());
		}
		// for debug
		if (m_log.isDebugEnabled()) {
			m_log.debug("notify() priority = " + priority
					+ " , message = " + message
					+ " , messageOrg = " + messageOrg
					+ ", generationDate = " + generationDate);
		}

		// ログ出力情報を送信
		if (m_log.isDebugEnabled()) {
			m_log.debug("sending message"
					+ " : priority=" + notifyInfo.getPriority()
					+ " generationDate=" + notifyInfo.getGenerationDate() + " pluginId=" + notifyInfo.getPluginId()
					+ " monitorId=" + notifyInfo.getMonitorId() + " facilityId=" + notifyInfo.getFacilityId()
					+ " subKey=" + notifyInfo.getSubKey()
					+ ")");
		}

		new NotifyControllerBean().notify(notifyInfo, notifyGroupId);
	}

	/**
	 * 監視管理に通知します。(並列処理時)
	 * <p>
	 * 通知に必要となる情報をログ出力情報にセットし、キューへメッセージを送信します。
	 * <ol>
	 * <li>通知IDを取得し、<code>null</code>の場合は、処理を終了します。</li>
	 * <li>各情報を取得し、ログ出力情報（{@link com.clustercontrol.monitor.message.LogOutputNotifyInfo }）にセットします。
	 * 監視単位がノードの場合とスコープの場合では、通知する情報が異なります。
	 * </li>
	 * <li>ログ出力情報を、ログ出力キューへ送信します。</li>
	 * </ol>
	 *
	 * @param isNode ノードの場合、</code> true </code>
	 * @param facilityId 通知対象のファシリティID
	 * @param result 判定結果
	 * @param generationDate ログ出力日時（監視を実行した日時）
	 * @param resultList 並列実行時の情報
	 * @throws HinemosUnknown
	 * @since 2.0.0
	 *
	 * @see com.clustercontrol.monitor.message.LogOutputNotifyInfo
	 * @see com.clustercontrol.repository.session.RepositoryControllerBean#getFacilityPath(java.lang.String, java.lang.String)
	 * @see #getNotifyRelationInfos()
	 * @see #getPriority(int)
	 * @see #getMessage(int)
	 * @see #getMessageOrg(int)
	 * @see #getJobRunInfo(int)
	 * @see #getMessageForScope(int)
	 * @see #getMessageOrgForScope(int)
	 * @see #getJobRunInfoForScope(int)
	 */
	protected void notify(
			boolean isNode,
			String facilityId,
			int result,
			Date generationDate,
			MonitorRunResultInfo resultInfo) throws HinemosUnknown {

		// for debug
		if (m_log.isDebugEnabled()) {
			m_log.debug("notify() isNode = " + isNode + ", facilityId = " + facilityId
					+ ", result = " + result + ", generationDate = " + generationDate.toString()
					+ ", resultInfo = " + resultInfo.getMessage());
		}

		boolean monitorFlg = false;

		monitorFlg = m_monitor.getMonitorFlg();

		// 監視無効の場合、通知しない
		if(!monitorFlg){
			m_log.debug("notify() isNode = " + isNode + ", facilityId = " + facilityId
					+ ", result = " + result + ", generationDate = " + generationDate.toString()
					+ ", resultInfo = " + resultInfo.getMessage() + ", monitorFlg is false");
			return;
		}

		// 通知IDが指定されていない場合、通知しない
		String notifyGroupId = resultInfo.getNotifyGroupId();
		if(notifyGroupId == null || "".equals(notifyGroupId)){
			return;
		}

		// 通知情報を設定
		OutputBasicInfo notifyInfo = new OutputBasicInfo();
		notifyInfo.setPluginId(m_monitorTypeId);
		notifyInfo.setMonitorId(m_monitorId);
		notifyInfo.setApplication(m_monitor.getApplication());

		String facilityPath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
		notifyInfo.setFacilityId(facilityId);
		notifyInfo.setScopeText(facilityPath);

		int priority = -1;
		String message = "";
		String messageOrg = "";

		if(isNode){
			// ノードの場合
			priority = resultInfo.getPriority();
			message = resultInfo.getMessage();
			messageOrg = resultInfo.getMessageOrg();
		}
		else{
			// スコープの場合
			priority = result;
			message = getMessageForScope(result);
			messageOrg = getMessageOrgForScope(result);
		}
		notifyInfo.setPriority(priority);
		// 通知抑制用のサブキーを設定。
		if(resultInfo.getDisplayName() != null && !"".equals(resultInfo.getDisplayName())){
			// 監視結果にデバイス名を含むものは、デバイス名をサブキーとして設定。
			notifyInfo.setSubKey(resultInfo.getDisplayName());
		} else if(resultInfo.getPatternText() != null){
			// 監視結果にパターンマッチ文字列を含むものは、デバイス名をサブキーとして設定。
			notifyInfo.setSubKey(resultInfo.getPatternText());
		}
		notifyInfo.setMessage(message);
		notifyInfo.setMessageOrg(messageOrg);
		if (generationDate != null) {
			notifyInfo.setGenerationDate(generationDate.getTime());
		}
		// for debug
		if (m_log.isDebugEnabled()) {
			m_log.debug("notify() priority = " + priority
					+ " , message = " + message
					+ " , messageOrg = " + messageOrg
					+ ", generationDate = " + generationDate);
		}

		// ログ出力情報を送信
		if (m_log.isDebugEnabled()) {
			m_log.debug("sending message"
					+ " : priority=" + notifyInfo.getPriority()
					+ " generationDate=" + notifyInfo.getGenerationDate() + " pluginId=" + notifyInfo.getPluginId()
					+ " monitorId=" + notifyInfo.getMonitorId() + " facilityId=" + notifyInfo.getFacilityId()
					+ " subKey=" + notifyInfo.getSubKey()
					+ ")");
		}
		new NotifyControllerBean().notify(notifyInfo, notifyGroupId);
	}

	/**
	 * 重要度別ファシリティ名を設定します。
	 * <p>
	 * <ol>
	 * <li>引数で指定されたファシリティIDのファシリティ名を取得します。</li>
	 * <li>重要度別のファシリティマップの引数で指定された重要度に、ファシリティ名を追加します。</li>
	 *
	 * @param priority 重要度
	 * @param facilityId ファシリティ名
	 * @throws HinemosUnknown
	 */
	protected void setPriorityMap(Integer priority, String facilityId) throws HinemosUnknown {

		ArrayList<String> list = m_priorityMap.get(priority);
		if(list != null){

			// ファシリティ名を取得
			String facilityName = new RepositoryControllerBean().getFacilityPath(facilityId, null);

			list.add(facilityName);
			m_priorityMap.put(priority, list);
		}
	}

	/**
	 * 項目一覧の文字列を返します。
	 *
	 * <dl>
	 *  <dt>項目名:</dt>
	 * 	 <dd>リスト[0]</dd>
	 *   <dd>リスト[1]</dd>
	 *   <dd>リスト[2]</dd>
	 *   <dd>　 ：　</dd>
	 * </dl>
	 *
	 * @param item 項目名
	 * @param list リスト
	 * @return 項目一覧の文字列
	 */
	private String getItemListString(String item, ArrayList<String> list){

		int length = list.size();
		if(length > 0){
			StringBuffer result = new StringBuffer();
			result.append(item + ":" + "\n");
			for (int i = 0; i < length; i++) {
				result.append("\t" + list.get(i));
				if(i < length-1){
					result.append("\n");
				}
			}
			return result.toString();
		}
		else{
			return "";
		}
	}

	/**
	 * 監視実行の初期処理を行います。
	 *
	 * 監視を実行するrunメソッドの最初の部分でcallしてください。
	 * runメソッド終了部分で、terminatteメソッドをcallし、キューのコネクションをクローズする必要があります。
	 *
	 */
	private void initialize(String monitorTypeId) {
	}

	/**
	 * 監視実行の終了処理を行います。
	 *
	 */
	private void terminate() {
	}
	
	public MonitorInfo getMonitorInfo() {
		return m_monitor;
	}

	/**
	 * ジョブ監視用のオリジナルメッセージを返す。
	 * 
	 * @param orgMsg 監視用のオリジナルメッセージ
	 * @param msg 監視用のメッセージ
	 * @return ジョブ監視用のオリジナルメッセージ
	 */
	protected String makeJobOrgMessage(String orgMsg, String msg){
		return "";
	}
}
