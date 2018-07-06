/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.systemlog.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.calendar.factory.SelectCalendar;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.hub.bean.CollectStringTag;
import com.clustercontrol.hub.bean.StringSample;
import com.clustercontrol.hub.bean.StringSampleTag;
import com.clustercontrol.hub.util.CollectStringDataUtil;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfo;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.util.NotifyCallback;
import com.clustercontrol.notify.util.NotifyRelationCache;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.factory.SearchNodeBySNMP;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.systemlog.bean.SyslogMessage;
import com.clustercontrol.systemlog.util.ResponseHandler;
import com.clustercontrol.systemlog.util.SyslogHandler;
import com.clustercontrol.util.HinemosTime;

public class SystemLogMonitor implements SyslogHandler, ResponseHandler<byte[]>{

	private static final Log log = LogFactory.getLog(SystemLogMonitor.class);

	private ExecutorService _executor;
	private SystemLogNotifier _notifier = new SystemLogNotifier();

	private final int _threadSize;
	private final int _queueSize;

	private long receivedCount = 0;
	private long discardedCount = 0;
	private long notifiedCount = 0;
	
	private Charset charset = Charset.defaultCharset();

	public SystemLogMonitor(int threadSize, int queueSize) {
		_threadSize = threadSize;
		_queueSize = queueSize;
	}

	@Override
	public synchronized void syslogReceived(List<SyslogMessage> syslogList) {
		String _receiverId = HinemosPropertyCommon.monitor_systemlog_receiverid.getStringValue();
		countupReceived();
		_executor.execute(new SystemLogMonitorTask(_receiverId, syslogList));
	}
	
	public synchronized void syslogReceivedSync(List<SyslogMessage> syslogList) {
		String _receiverId = HinemosPropertyCommon.monitor_systemlog_receiverid.getStringValue();
		countupReceived();
		new SystemLogMonitorTask(_receiverId, syslogList).run();
	}

	private synchronized void countupReceived() {
		receivedCount = receivedCount >= Long.MAX_VALUE ? 0 : receivedCount + 1;
		int _statsInterval = HinemosPropertyCommon.monitor_systemlog_stats_interval.getIntegerValue();
		if (receivedCount % _statsInterval == 0) {
			log.info("The number of syslog (received) : " + receivedCount);
		}
	}

	private synchronized void countupDiscarded() {
		discardedCount = discardedCount >= Long.MAX_VALUE ? 0 : discardedCount + 1;
		int _statsInterval = HinemosPropertyCommon.monitor_systemlog_stats_interval.getIntegerValue();
		if (discardedCount % _statsInterval == 0) {
			log.info("The number of syslog (discarded) : " + discardedCount);
		}
	}

	private synchronized void countupNotified() {
		notifiedCount = notifiedCount >= Long.MAX_VALUE ? 0 : notifiedCount + 1;
		int _statsInterval = HinemosPropertyCommon.monitor_systemlog_stats_interval.getIntegerValue();
		if (notifiedCount % _statsInterval == 0) {
			log.info("The number of syslog (notified) : " + notifiedCount);
		}
	}

	public long getReceivedCount() {
		return receivedCount;
	}

	public long getDiscardedCount() {
		return discardedCount;
	}

	public long getNotifiedCount() {
		return notifiedCount;
	}

	public int getQueuedCount() {
		return ((ThreadPoolExecutor)_executor).getQueue().size();
	}

	@Override
	public synchronized void start() {
		_executor = new MonitoredThreadPoolExecutor(_threadSize, _threadSize,
				0L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>(_queueSize),
				new ThreadFactory() {
			private volatile int _count = 0;
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "SystemLogFilter-" + _count++);
			}
		}, new SystemLogRejectionHandler());
	}

	private class SystemLogRejectionHandler extends ThreadPoolExecutor.DiscardPolicy {

		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
			if (r instanceof SystemLogMonitorTask) {
				countupDiscarded();
				log.warn("too many syslog. syslog discarded : " + r);
			}
		}
	}

	@Override
	public synchronized void shutdown() {
		_executor.shutdown();
		try {
			long _shutdownTimeoutMsec = HinemosPropertyCommon.monitor_systemlog_shutdown_timeout.getNumericValue();

			if (! _executor.awaitTermination(_shutdownTimeoutMsec, TimeUnit.MILLISECONDS)) {
				List<Runnable> remained = _executor.shutdownNow();
				if (remained != null) {
					log.info("shutdown timeout. runnable remained. (size = " + remained.size() + ")");
				}
			}
		} catch (InterruptedException e) {
			_executor.shutdownNow();
		}
	}

	private class SystemLogMonitorTask implements Runnable {

		public final String receiverId;
		public final List<SyslogMessage> syslogList;

		public SystemLogMonitorTask(String receiverId, List<SyslogMessage> syslogList) {
			this.receiverId = receiverId;
			this.syslogList = syslogList;
		}

		@Override
		public void run() {
			JpaTransactionManager tm = null;
			List<OutputBasicInfo> notifyInfoList = new ArrayList<>();

			if (log.isDebugEnabled()) {
				for (SyslogMessage syslog : syslogList) {
					log.debug("monitoring syslog : " + syslog);
				}
			}

			try {
				tm = new JpaTransactionManager();
				tm.begin();

				Collection<MonitorInfo> monitorList = null;
				try {
					monitorList = new MonitorSettingControllerBean().getSystemlogMonitorCache();
				} catch (MonitorNotFound e) {
					log.debug("monitor configuration (system log) not found.");
					return;
				} catch (InvalidRole e) {
					log.debug("monitor configuration (system log) not found.");
					return;
				}
				if (log.isDebugEnabled()) {
					log.debug("monitor configuration (system log) : count = " + monitorList.size());
				}
				
				if (monitorList.isEmpty()) {
					return;
				}

				// 収集処理
				List<StringSample> collectedSamples = new ArrayList<>();
				for (SyslogMessage syslog : syslogList) {
					Set<String> facilityIdSet = resolveFacilityId(syslog.hostname);
					
					for (MonitorInfo monitor : monitorList) {
						// 管理対象フラグが無効であれば、次の設定の処理へスキップする
						if (!monitor.getCollectorFlg()) {
							continue;
						}
						
						List<String> validFacilityIdList = getValidFacilityIdList(facilityIdSet, monitor, null);
						if (!validFacilityIdList.isEmpty()) {
							for (String facilityId: validFacilityIdList) {
								StringSample sample = new StringSample(new Date(HinemosTime.currentTimeMillis()), monitor.getMonitorId());
								//抽出したタグ
								StringSampleTag tagDate = new StringSampleTag(CollectStringTag.TIMESTAMP_IN_LOG, Long.toString(syslog.date));
								StringSampleTag tagFacility = new StringSampleTag(CollectStringTag.facility, syslog.facility.name());
								StringSampleTag tagSeverity = new StringSampleTag(CollectStringTag.severity, syslog.severity.name());
								StringSampleTag tagHostname = new StringSampleTag(CollectStringTag.hostname, syslog.hostname);
								StringSampleTag tagMessage = new StringSampleTag(CollectStringTag.message, syslog.message);
								
								//ログメッセージ
								sample.set(facilityId, "syslog", syslog.rawSyslog, Arrays.asList(tagDate, tagFacility, tagSeverity, tagHostname, tagMessage));
								
								collectedSamples.add(sample);
							}
						}
					}
				}
				
				if (!collectedSamples.isEmpty()) {
					CollectStringDataUtil.store(collectedSamples);
				}

				// 監視ジョブ以外
				for (MonitorInfo monitor : monitorList) {
					notifyInfoList.addAll(notifySyslog(monitor, null));
				}

				// 監視ジョブ
				for (Map.Entry<RunInstructionInfo, MonitorInfo> entry 
						: MonitorJobWorker.getMonitorJobMap(HinemosModuleConstant.MONITOR_SYSTEMLOG).entrySet()) {
					notifyInfoList.addAll(notifySyslog(entry.getValue(), entry.getKey()));
				}

				// 通知設定
				tm.addCallback(new NotifyCallback(notifyInfoList));

				tm.commit();
			} catch (Exception e) {
				// HinemosException系はthrow元でlog出力するため、何もしない
				// HA構成のため、例外を握りつぶしてはいけない
				throw new RuntimeException("unexpected internal error. : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			} finally {
				if (tm != null) {
					tm.close();
				}
			}
		}

		/**
		 * システムログの通知処理を行う
		 * 
		 * @param monitorInfo 監視情報
		 * @param runInstructionInfo ジョブ実行指示
		 * @return 通知情報リスト
		 * @throws HinemosUnknown
		 */
		private List<OutputBasicInfo> notifySyslog(
				MonitorInfo monitorInfo,
				RunInstructionInfo runInstructionInfo) throws HinemosUnknown {

			List<OutputBasicInfo> rtn = new ArrayList<>();

			if (log.isDebugEnabled()) {
				log.debug("filtering by configuration : " + monitorInfo.getMonitorId());
			}

			if (runInstructionInfo == null) {
				// 監視ジョブ以外の場合
				// 管理対象フラグが無効であれば、次の設定の処理へスキップする
				if (!monitorInfo.getMonitorFlg()) {
					return rtn;
				}
				
				// 関連の通知設定がなければ、スキップ
				List<NotifyRelationInfo> notifyRelationList 
					= NotifyRelationCache.getNotifyList(monitorInfo.getNotifyGroupId());
				if (notifyRelationList == null 
						|| notifyRelationList.size() == 0) {
					return rtn;
				}
			}

			for (SyslogMessage syslog : syslogList) {
				List<SyslogMessage> syslogListBuffer = new ArrayList<SyslogMessage>();
				List<MonitorStringValueInfo> ruleListBuffer = new ArrayList<MonitorStringValueInfo>();
				List<String> facilityIdListBuffer = new ArrayList<String>();

				if (runInstructionInfo == null && isNotInCalendar(monitorInfo, syslog)) {
					continue;
				}

				Set<String> facilityIdSet = resolveFacilityId(syslog.hostname);
				if (facilityIdSet.size() == 0) {
					log.warn("target facility not found: " + syslog.hostname);
					continue;
				}
				List<String> validFacilityIdList = getValidFacilityIdList(facilityIdSet, monitorInfo, runInstructionInfo);

				int orderNo = 0;
				for (MonitorStringValueInfo rule : monitorInfo.getStringValueInfo()) {
					++orderNo;
					if (log.isDebugEnabled()) {
						log.debug(String.format("monitoring (monitorId = %s, orderNo = %d, patten = %s, enabled = %s, casesensitive = %s)",
								monitorInfo.getMonitorId(), orderNo, rule.getPattern(), rule.getValidFlg(), rule.getCaseSensitivityFlg()));
					}

					if (! rule.getValidFlg()) {
						// 無効化されているルールはスキップする
						continue;
					}

					// パターンマッチを実施
					if (log.isDebugEnabled()) {
						log.debug(String.format("filtering syslog (regex = %s, syslog = %s", rule.getPattern(), syslog));
					}

					try {
						Pattern pattern = null;
						if (rule.getCaseSensitivityFlg()) {
							// 大文字・小文字を区別しない場合
							pattern = Pattern.compile(rule.getPattern(), Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
						} else {
							// 大文字・小文字を区別する場合
							pattern = Pattern.compile(rule.getPattern(), Pattern.DOTALL);
						}

						Matcher matcher = pattern.matcher(syslog.message);
						if (matcher.matches()) {
							if (rule.getProcessType()) {
								log.debug(String.format("matched (regex = %s, syslog = %s", rule.getPattern(), syslog));
								for (String facilityId : validFacilityIdList) {
									syslogListBuffer.add(syslog);
									ruleListBuffer.add(rule);
									facilityIdListBuffer.add(facilityId);
									countupNotified();
								}
							} else {
								log.debug(String.format("not matched (regex = %s, syslog = %s", rule.getPattern(), syslog));
							}
							break;
						}
					} catch (Exception e) {
						log.warn("filtering failure. (regex = " + rule.getPattern() + ") . " +
								e.getMessage(), e);
					}
				}

				rtn.addAll(_notifier.createOutputBasicInfoList(
						receiverId, syslogListBuffer, monitorInfo, ruleListBuffer, facilityIdListBuffer, runInstructionInfo));
			}
			return rtn;
		}

		private boolean isNotInCalendar(MonitorInfo monitor, SyslogMessage syslog) {
			boolean notInCalendar = false;
			// カレンダが割り当てられている場合
			if (monitor.getCalendarId() != null && monitor.getCalendarId().length() > 0) {
				try {
					boolean run = new SelectCalendar().isRun(monitor.getCalendarId(), syslog.date);
					notInCalendar = !run;
				} catch (CalendarNotFound e) {
					log.warn("calendar not found (calendarId = " + monitor.getCalendarId() + ")");
				} catch (InvalidRole e) {
					log.warn("calendar not found (calendarId = " + monitor.getCalendarId() + ") ,"
							+ e.getMessage());
				}

				// カレンダの有効期間外の場合
				if (notInCalendar) {
					if (log.isDebugEnabled()) {
						log.debug("skip monitoring because of calendar. (monitorId = " + monitor.getMonitorId()
								+ ", calendarId = " + monitor.getCalendarId() + ")");
					}
				}
			}
			
			return notInCalendar;
		}

		private List<String> getValidFacilityIdList(
				Set<String> facilityIdSet, 
				MonitorInfo monitor, 
				RunInstructionInfo runInstructionInfo) {
			List<String> validFacilityIdList = new ArrayList<String>();
			String monitorFacilityId = "";
			if (runInstructionInfo == null) {
				monitorFacilityId = monitor.getFacilityId();
			} else {
				monitorFacilityId = runInstructionInfo.getFacilityId();
			}
			for (String facilityId : facilityIdSet) {
				if (log.isDebugEnabled()) {
					log.debug("filtering node. (monitorId = " + monitor.getMonitorId()
							+ ", facilityId = " + facilityId + ")");
				}

				if (FacilityTreeAttributeConstant.UNREGISTERED_SCOPE.equals(facilityId)) {
					if (! FacilityTreeAttributeConstant.UNREGISTERED_SCOPE.equals(monitorFacilityId)) {
						// 未登録ノードから送信されたsyslogだが、未登録ノードに対する設定でない場合はスキップする
						continue;
					}
				} else {
						if (! new RepositoryControllerBean().containsFaciliyId(monitorFacilityId, facilityId, monitor.getOwnerRoleId())) {
						// syslogの送信元ノードが、設定のスコープ内に含まれない場合はスキップする
						continue;
					}
				}
				validFacilityIdList.add(facilityId);
			}
			return validFacilityIdList;
		}

		private Set<String> resolveFacilityId(String hostname){
			Set<String> facilityIdSet = null;
			String shortHostname = SearchNodeBySNMP.getShortName(hostname);

			if (log.isDebugEnabled()) {
				log.debug("resolving facilityId from hostname = " + shortHostname);
			}

			// ノード名による一致確認
			facilityIdSet = new RepositoryControllerBean().getNodeListByNodename(shortHostname);

			// ノード名で一致するノードがない場合
			if (facilityIdSet == null) {
				// IPアドレスによる一致確認
				try {
					// IPアドレスだけは、ショートネーム処理されていないもので比較する
					facilityIdSet = new RepositoryControllerBean().getNodeListByIpAddress(InetAddress.getByName(hostname));
				} catch (UnknownHostException e) {
					if (log.isDebugEnabled()) {
						log.debug("unknow host " + hostname + ".", e);
					}
				}

				// ノード名でもIPアドレスでも一致するノードがない場合
				if (facilityIdSet == null) {
					// ホスト名による一致確認
					facilityIdSet = new RepositoryControllerBean().getNodeListByHostname(shortHostname);
				}
			}

			if (facilityIdSet == null) {
				// 指定のノード名、IPアドレスで登録されているノードがリポジトリに存在しないため、
				// 「"UNREGISTEREFD"（FacilityTreeAttributeConstant.UNREGISTEREFD_SCOPE）」だけを
				// セットに含めたものをマップに登録する。
				facilityIdSet = new HashSet<String>();
				facilityIdSet.add(FacilityTreeAttributeConstant.UNREGISTERED_SCOPE);
			}

			if (log.isDebugEnabled()) {
				log.debug("resolved facilityId " + facilityIdSet + "(from hostname = " + shortHostname + ")");
			}

			return facilityIdSet;
		}

		@Override
		public String toString() {
			return String.format("%s [receiverId = %s, syslogList = %s]",
					this.getClass().getSimpleName(), receiverId, syslogList);
		}

	}

	@Override
	public void accept(byte[] message, String senderAddress) {
		if (log.isDebugEnabled()) {
			log.debug( "accept senderAddress="+senderAddress) ;
		}
		try {
			SyslogMessage syslogMessage = byteToSyslog(message, senderAddress);
			List<SyslogMessage> syslogList = new ArrayList<SyslogMessage>();
			syslogList.add(syslogMessage);
			syslogReceived(syslogList);
			
		} catch (HinemosUnknown e) {
			log.info(e.getMessage());
		} catch (ParseException e) {
			log.info(e.getMessage());
		}
	}
	
	public void setCharset(Charset charset) {
		this.charset = charset;
	}
	
	public SyslogMessage byteToSyslog(byte[] syslogRaw, String senderAddress) throws ParseException, HinemosUnknown {
		String syslog = new String(syslogRaw, 0, syslogRaw.length, charset);
		return SyslogMessage.parse(syslog, senderAddress);
	}
}
