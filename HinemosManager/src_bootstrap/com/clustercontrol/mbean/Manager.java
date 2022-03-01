/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.mbean;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.persistence.internal.jpa.CacheImpl;
import org.eclipse.persistence.sessions.SessionEventListener;
import org.eclipse.persistence.sessions.server.ConnectionPool;
import org.eclipse.persistence.sessions.server.ServerSession;

import com.clustercontrol.commons.util.DBConnectionPoolStats;
import com.clustercontrol.commons.util.EmptyJpaTransactionCallback;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaSessionEventListener;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.jobmanagement.factory.FullJob;
import com.clustercontrol.jobmanagement.util.JobMultiplicityCache;
import com.clustercontrol.jobmanagement.util.JobSessionNodeRetryController;
import com.clustercontrol.notify.model.MonitorStatusEntity;
import com.clustercontrol.notify.model.NotifyHistoryEntity;
import com.clustercontrol.notify.util.MonitorStatusCache;
import com.clustercontrol.plugin.impl.AsyncWorkerPlugin;
import com.clustercontrol.plugin.impl.RestServiceAgentPlugin;
import com.clustercontrol.plugin.impl.RestServicePlugin;
import com.clustercontrol.plugin.impl.SchedulerInfo;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType;
import com.clustercontrol.plugin.impl.SnmpTrapPlugin;
import com.clustercontrol.plugin.impl.SystemLogPlugin;
import com.clustercontrol.plugin.impl.WebServiceAgentPlugin;
import com.clustercontrol.repository.factory.NodeProperty;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.selfcheck.SelfCheckTaskSubmitter;
import com.clustercontrol.selfcheck.monitor.AsyncTaskQueueMonitor;
import com.clustercontrol.selfcheck.monitor.DBConnectionCountMonitor;
import com.clustercontrol.selfcheck.monitor.DBLongTranMonitor;
import com.clustercontrol.selfcheck.monitor.JVMHeapMonitor;
import com.clustercontrol.selfcheck.monitor.JobRunSessionMonitor;
import com.clustercontrol.selfcheck.monitor.SchedulerMonitor;
import com.clustercontrol.selfcheck.monitor.TableSizeMonitor;

import jakarta.persistence.Cache;

/**
 * ----!注意!----<BR>
 * このクラスおよびManagerMXBeanインターフェイスのMBeanの属性を変更したり追加する時は以下に注意してください。<BR>
 * 
 * 1. JMX監視にHinemosのセルフチェック対象を監視できる監視項目が存在します。<BR>
 * 変更や追加をする場合はJMX監視の監視項目への対応も合わせて検討してください。<BR>
 * TablePhysicalSizeやAsyncTaskQueueCountのキーを追加する場合も同様です。<BR>
 * 
 * 2. hinemos_manager_summaryでこのクラスのMBeanから情報を取得しています。（manager_cli経由）<BR>
 * 変更や追加をする場合はhinemos_manager_summaryとの整合性に注意してください。<BR>
 * --------------
 */
public class Manager implements ManagerMXBean {

	private static final Log log = LogFactory.getLog(Manager.class);

	@Override
	public String getValidAgentStr() {
		StringBuilder str = new StringBuilder();
		String lineSeparator = System.getProperty("line.separator");

		List<String> validAgent = AgentConnectUtil.getValidAgent();
		Collections.sort(validAgent);

		for (String facilityId : validAgent) {
			String agentString = AgentConnectUtil.getAgentString(facilityId);
			if (agentString == null) {
				continue;
			}
			str.append(facilityId + ", " + agentString + lineSeparator);
		}
		return str.toString();
	}

	@Override
	public int getValidAgentCount(){
		return AgentConnectUtil.getValidAgent().size();
	}

	@Override
	public String getSchedulerInfoStr() throws HinemosUnknown {
		StringBuilder str = new StringBuilder();
		String lineSeparator = System.getProperty("line.separator");
		String lineFormat = "%-30s| %-30s| %-20s| %-20s";
		
		for (SchedulerType schedulerType : SchedulerType.values()) {
			// header
			str.append("----- ").append(schedulerType).append(" ").append(SchedulerPlugin.schedulerSummary(schedulerType)).append(" -----").append(lineSeparator);
			str.append(String.format(lineFormat, "name", "group", "last fire time", "next fire time")).append(lineSeparator);
			str.append("------------------------------+-------------------------------+---------------------+---------------------").append(lineSeparator);
			
			// each schedule
			List<SchedulerInfo> schedule = SchedulerPlugin.getSchedulerList(schedulerType);
			for (SchedulerInfo trigger : schedule) {
				String prev = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", trigger.previousFireTime);
				String next = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", trigger.nextFireTime);
				str.append(String.format(lineFormat, trigger.name, trigger.group, prev, next)).append(lineSeparator);
			}
			
			str.append(lineSeparator);
		}
		return str.toString();
	}
	
	@Override
	public String getSelfCheckLastFireTimeStr() {
		return String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", SelfCheckTaskSubmitter.lastMonitorDate);
	}

	@Override
	public String getSyslogStatistics() {
		String str = "";
		str += "[Syslog Statistics]" + System.getProperty("line.separator");
		str += "received : " + SystemLogPlugin.getReceivedCount() + System.getProperty("line.separator");
		str += "queued : " + SystemLogPlugin.getQueuedCount() + System.getProperty("line.separator");
		str += "discarded : " + SystemLogPlugin.getDiscardedCount() + System.getProperty("line.separator");
		str += "notified : " + SystemLogPlugin.getNotifiedCount() + System.getProperty("line.separator");
		return str;
	}

	@Override
	public String getSnmpTrapStatistics() {
		String str = "";
		str += "[SnmpTrap Statistics]" + System.getProperty("line.separator");
		str += "received : " + SnmpTrapPlugin.getReceivedCount() + System.getProperty("line.separator");
		str += "queued : " + SnmpTrapPlugin.getQueuedCount() + System.getProperty("line.separator");
		str += "discarded : " + SnmpTrapPlugin.getDiscardedCount() + System.getProperty("line.separator");
		str += "notified : " + SnmpTrapPlugin.getNotifiedCount() + System.getProperty("line.separator");
		return str;
	}

	@Override
	public String getAsyncWorkerStatistics() throws HinemosUnknown {
		StringBuilder str = new StringBuilder();
		str.append("[AsyncWorker Statistics]").append(System.getProperty("line.separator"));
		for (String worker : AsyncWorkerPlugin.getWorkerList()) {
			str.append(String.format("queued tasks [%s] : %s%s", worker,  AsyncWorkerPlugin.getTaskCount(worker), System.getProperty("line.separator")));
		}
		return str.toString();
	}

	@Override
	public String resetNotificationLogger() throws HinemosUnknown {
		log.info("resetting notification counter...");

		JpaTransactionManager tm = null;
		HinemosEntityManager em = null;

		try {
			tm = new JpaTransactionManager();
			tm.begin();

			// EJB Container内の抑制情報を初期化
			em = tm.getEntityManager();
			em.createNamedQuery("MonitorStatusEntity.deleteAll", MonitorStatusEntity.class).executeUpdate();
			em.createNamedQuery("NotifyHistoryEntity.deleteAll", NotifyHistoryEntity.class).executeUpdate();

			// コミット完了後に、MonitorStatusCache内の抑制情報を初期化
			tm.addCallback(new EmptyJpaTransactionCallback() {
				@Override
				public void postCommit() {
					try {
						MonitorStatusCache.removeAll();
					} catch (Exception e) {
						log.warn("Failed to reset NotifyStatusCache.", e);
					}
				}
			});

			tm.commit();
		} catch (Exception e) {
			log.warn("notify counter reset failure...", e);
			if (tm != null)
				tm.rollback();
			throw new HinemosUnknown("notify counter reset failure...", e);
		} finally {
			if (tm != null) {
				tm.close();
			}
		}

		log.info("notify counter resetted successfully.");
		return null;
	}

	@Override
	public String getDBConnectionPoolInfoStr() {
		log.debug("get DB ConnectionPool Info start.");
		int used = 0;
		StringBuilder str = new StringBuilder();
		String lineSeparator = System.getProperty("line.separator");
		
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			ServerSession ss = em.unwrap(ServerSession.class);
			
			// 登録するイベントリスナーはJpaSessionEventListenerのみ
			List<SessionEventListener> list = ss.getEventManager().getListeners();
			JpaSessionEventListener listener = (JpaSessionEventListener)list.get(0);
			
			str.append("[DB ConnectionPool Info]" + lineSeparator);
			// Hinemos 5.1時点ではデフォルトのコネクションプールしか使用しないが、読込専用プール等、
			// 別のプールを追加する場合は、プール毎に出力するため、プール名も併せて取得すべき。
			for (ConnectionPool pool : ss.getConnectionPools().values()) {
				used = (pool.getTotalNumberOfConnections() - pool.getConnectionsAvailable().size());
				StringBuilder message = new StringBuilder();
				message.append("Pool-name=" + pool.getName());
				message.append(" ,Initial=" + pool.getInitialNumberOfConnections());
				message.append(" ,Max=" + pool.getMaxNumberOfConnections());
				message.append(" ,Min=" + pool.getMinNumberOfConnections());
				message.append(" ,Use=" + used);
				
				str.append(message + lineSeparator);
				log.debug(message);
			}
			// プール使用数の統計データ(1時間毎の最大使用数)を取得(統計データはデフォルトプールのみ取得)
			for (DBConnectionPoolStats queue : listener.getPoolStats()){
				str.append("Max use count:" + String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", queue.getUpdateTime()) + "=" + queue.getMaxUseCount() + lineSeparator);
			}
			log.debug("get DB ConnectionPool Info end.");
			return str.toString();
		}
	}

	@Override
	public void printJpaCacheAll() {
		JpaTransactionManager tm = null;
		HinemosEntityManager em = null;
		try {
			tm = new JpaTransactionManager();
			tm.begin();
			em = tm.getEntityManager();
			Cache cache = em.getEntityManagerFactory().getCache();
			CacheImpl jpaCache = (CacheImpl) cache;
			jpaCache.print();
			tm.commit();
		} catch (Exception e) {
			log.warn("printJpaCache failure...", e);
		} finally {
			if (tm != null) {
				tm.close();
			}
		}
	}

	@Override
	public String getJobQueueStr() {
		return JobMultiplicityCache.getJobQueueStr() + "\n" + JobSessionNodeRetryController.getReport();
	}

	@Override
	public void printFacilityTreeCacheAll() {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			/* メイン処理 */
			FacilityTreeCache.printCache();

			jtm.commit();
		} catch (Exception e) {
			log.warn("printFacilityTreeCacheAll failure...", e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	@Override
	public void refreshFacilityTreeCache() {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			/* メイン処理 */
			FacilityTreeCache.refresh();

			jtm.commit();
		} catch (Exception e) {
			log.warn("refreshFacilityTreeCache failure...", e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	@Override
	public long getJobRunSessionCount() {
		return JobRunSessionMonitor.getJobRunSessionCount();
	}

	@Override
	public int getSnmpTrapQueueCount() {
		return SnmpTrapPlugin.getQueuedCount();
	}

	@Override
	public int getSyslogQueueCount() {
		return SystemLogPlugin.getQueuedCount();
	}
	
	@Override
	public int getWebServiceForAgentQueueCount() {
		return WebServiceAgentPlugin.getAgentQueueSize();
	}
	
	@Override
	public int getWebServiceForAgentHubQueueCount() {
		return WebServiceAgentPlugin.getAgentHubQueueSize();
	}

	@Override
	public int getWebServiceForAgentBinaryQueueCount() {
		return WebServiceAgentPlugin.getAgentBinaryQueueSize();
	}

	@Override
	public int getWebServiceForAgentNodeConfigQueueCount() {
		return WebServiceAgentPlugin.getAgentNodeConfigQueueSize();
	}

	@Override
	public int getRestServiceForAgentQueueCount() {
		return RestServiceAgentPlugin.getAgentQueueSize();
	}

	@Override
	public int getRestServiceForAgentHubQueueCount() {
		return RestServiceAgentPlugin.getAgentHubQueueSize();
	}

	@Override
	public int getRestServiceForAgentBinaryQueueCount() {
		return RestServiceAgentPlugin.getAgentBinaryQueueSize();
	}

	@Override
	public int getRestServiceForAgentNodeConfigQueueCount() {
		return RestServiceAgentPlugin.getAgentNodeConfigQueueSize();
	}

	@Override
	public int getRestServiceQueueCount() {
		return RestServicePlugin.getQueueSize();
	}

	@Override
	public TablePhysicalSizes getTablePhysicalSize() {
		// 各テーブルの物理テーブルサイズを取得する
		long log_cc_collect_data_raw = TableSizeMonitor.getTableSize("log.cc_collect_data_raw");
		long log_cc_collect_data_string = TableSizeMonitor.getTableSize("log.cc_collect_data_string");
		long log_cc_collect_data_tag = TableSizeMonitor.getTableSize("log.cc_collect_data_tag");
		long log_cc_collect_summary_day = TableSizeMonitor.getTableSize("log.cc_collect_summary_day");
		long log_cc_collect_summary_hour = TableSizeMonitor.getTableSize("log.cc_collect_summary_hour");
		long log_cc_collect_summary_month = TableSizeMonitor.getTableSize("log.cc_collect_summary_month");
		long log_cc_event_log = TableSizeMonitor.getTableSize("log.cc_event_log");
		long log_cc_job_info = TableSizeMonitor.getTableSize("log.cc_job_info");
		long log_cc_job_param_info = TableSizeMonitor.getTableSize("log.cc_job_param_info");
		long log_cc_job_session = TableSizeMonitor.getTableSize("log.cc_job_session");
		long log_cc_job_session_job = TableSizeMonitor.getTableSize("log.cc_job_session_job");
		long log_cc_job_session_node = TableSizeMonitor.getTableSize("log.cc_job_session_node");
		long log_cc_job_wait_group_info = TableSizeMonitor.getTableSize("log.cc_job_wait_group_info");
		long log_cc_status_info = TableSizeMonitor.getTableSize("log.cc_status_info");

		TablePhysicalSizes tablePhysicalSizes = new TablePhysicalSizes(
				log_cc_collect_data_raw,
				log_cc_collect_data_string,
				log_cc_collect_data_tag,
				log_cc_collect_summary_day,
				log_cc_collect_summary_hour,
				log_cc_collect_summary_month,
				log_cc_event_log,
				log_cc_job_info,
				log_cc_job_param_info,
				log_cc_job_session,
				log_cc_job_session_job,
				log_cc_job_session_node,
				log_cc_job_wait_group_info,
				log_cc_status_info);
		return tablePhysicalSizes;
	}

	@Override
	public long getTableRecordCount(String tableName) {
		return TableSizeMonitor.getTableCount(tableName);
	}

	@Override
	public int getDBConnectionCount() {
		return DBConnectionCountMonitor.getDBConnectionCount();
	}

	@Override
	public AsyncTaskQueueCounts getAsyncTaskQueueCount() throws HinemosUnknown {
		// 各非同期タスクのキュー数を取得する
		int notifyStatusTaskFactory = AsyncTaskQueueMonitor.getTaskCount(AsyncWorkerPlugin.NOTIFY_STATUS_TASK_FACTORY);
		int notifyEventTaskFactory = AsyncTaskQueueMonitor.getTaskCount(AsyncWorkerPlugin.NOTIFY_EVENT_TASK_FACTORY);
		int notifyMailTaskFactory = AsyncTaskQueueMonitor.getTaskCount(AsyncWorkerPlugin.NOTIFY_MAIL_TASK_FACTORY);
		int notifyCommandTaskFactory = AsyncTaskQueueMonitor.getTaskCount(AsyncWorkerPlugin.NOTIFY_COMMAND_TASK_FACTORY);
		int notifyLogEscalationTaskFactory = AsyncTaskQueueMonitor.getTaskCount(AsyncWorkerPlugin.NOTIFY_LOG_ESCALATION_TASK_FACTORY);
		int notifyJobTaskFactory = AsyncTaskQueueMonitor.getTaskCount(AsyncWorkerPlugin.NOTIFY_JOB_TASK_FACTORY);
		int createJobSessionTaskFactory = AsyncTaskQueueMonitor.getTaskCount(AsyncWorkerPlugin.CREATE_JOB_SESSION_TASK_FACTORY);
		int notifyInfraTaskFactory = AsyncTaskQueueMonitor.getTaskCount(AsyncWorkerPlugin.NOTIFY_INFRA_TASK_FACTORY);
		int notifyRestTaskFactory = AsyncTaskQueueMonitor.getTaskCount(AsyncWorkerPlugin.NOTIFY_REST_TASK_FACTORY);
		int notifyCloudTaskFactory = AsyncTaskQueueMonitor.getTaskCount(AsyncWorkerPlugin.NOTIFY_CLOUD_TASK_FACTORY);
		int agentRestartTaskFactory = AsyncTaskQueueMonitor.getTaskCount(AsyncWorkerPlugin.AGENT_RESTART_TASK_FACTORY);
		int agentUpdateTaskFactory = AsyncTaskQueueMonitor.getTaskCount(AsyncWorkerPlugin.AGENT_UPDATE_TASK_FACTORY);
		int agentBroadcastAwakeTaskFactory = AsyncTaskQueueMonitor.getTaskCount(AsyncWorkerPlugin.AGENT_BROADCAST_AWAKE_TASK_FACTORY);
		int notifyMessageTaskFactory = AsyncTaskQueueMonitor.getTaskCount(AsyncWorkerPlugin.NOTIFY_MESSAGE_TASK_FACTORY);
		
		AsyncTaskQueueCounts asyncTaskQueueCounts = new AsyncTaskQueueCounts(
				notifyStatusTaskFactory,
				notifyEventTaskFactory, 
				notifyMailTaskFactory,
				notifyCommandTaskFactory,
				notifyLogEscalationTaskFactory,
				notifyJobTaskFactory,
				createJobSessionTaskFactory,
				notifyInfraTaskFactory,
				notifyRestTaskFactory,
				notifyCloudTaskFactory,
				agentRestartTaskFactory,
				agentUpdateTaskFactory,
				agentBroadcastAwakeTaskFactory,
				notifyMessageTaskFactory);
		return asyncTaskQueueCounts;
	}

	@Override
	public int getJVMHeapSize() {
		return JVMHeapMonitor.getJVMHeapSize();
	}

	@Override
	public double getDBLongTransactionTime() {
		return DBLongTranMonitor.getDBLongTransactionTime();
	}

	@Override
	public SchedulerDelayTimes getSchedulerDelayTime() throws HinemosUnknown {
		long delayMillisecDbmsJob = 0L;
		long delayMillisecDbmsDel = 0L;
		long delayMillisecDbms = 0L;
		long delayMillisecRamMon = 0L;
		long delayMillisecRamJob = 0L;
		
		// 指定したスケジューラの中で、最も遅延しているものを取得
		delayMillisecDbmsJob = SchedulerMonitor.getSchedulerDelayTime(SchedulerPlugin.SchedulerType.DBMS_JOB);
		delayMillisecDbmsDel = SchedulerMonitor.getSchedulerDelayTime(SchedulerPlugin.SchedulerType.DBMS_DEL);
		delayMillisecDbms = SchedulerMonitor.getSchedulerDelayTime(SchedulerPlugin.SchedulerType.DBMS);
		delayMillisecRamMon = SchedulerMonitor.getSchedulerDelayTime(SchedulerPlugin.SchedulerType.RAM_MONITOR);
		delayMillisecRamJob = SchedulerMonitor.getSchedulerDelayTime(SchedulerPlugin.SchedulerType.RAM_JOB);
		
		SchedulerDelayTimes schedulerDelayTimes = new SchedulerDelayTimes(delayMillisecDbmsJob, delayMillisecDbmsDel, 
				delayMillisecDbms, delayMillisecRamMon, delayMillisecRamJob);
		return schedulerDelayTimes;
	}

	/*
	@Override
	public void startHinemosSchedulerTest() {
		log.info("startHinemosSchedulerTest start.");
		HinemosSchedulerTest obj = new HinemosSchedulerTest();
		obj.activate();
		log.info("startHinemosSchedulerTest end.");
	}
	
	@Override
	public void startHinemosSchedulerStressTest() {
		log.info("startHinemosSchedulerStressTest start.");
		HinemosSchedulerTest obj = new HinemosSchedulerTest();
		obj.activateStressTest();
		log.info("startHinemosSchedulerStressTest end.");
	}
	
	@Override
	public void stopHinemosSchedulerTest() {
		log.info("stopHinemosSchedulerTest start.");
		HinemosSchedulerTest obj = new HinemosSchedulerTest();
		obj.deactivate();
		log.info("stopHinemosSchedulerTest end.");
	}
	*/
	
	@Override
	public void initNodeCache() {
		try {
			NodeProperty.init();
		} catch (Throwable t) {
			log.error("NodeProperty initialisation error. " + t.getMessage(), t);
		}
	}
	
	@Override
	public void initJobCache() {
		try {
			FullJob.initJobMstCache();
			FullJob.initJobInfoCache();
		} catch (Throwable t) {
			log.error("FullJob initialisation error. " + t.getMessage(), t);
		}
	}
}
