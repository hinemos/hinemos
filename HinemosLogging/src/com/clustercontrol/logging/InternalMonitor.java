/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.logging;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.clustercontrol.log.control.ControlLogManager;
import com.clustercontrol.log.internal.InternalLogManager;
import com.clustercontrol.log.monitoring.MonitoringLogManager;
import com.clustercontrol.logging.constant.MessageConstant;
import com.clustercontrol.logging.constant.PropertyConstant;
import com.clustercontrol.logging.property.LoggingProperty;
import com.clustercontrol.logging.util.ProcessInfo;
import com.sun.management.OperatingSystemMXBean;

/**
 * 内部監視を行うクラスです 各監視種別ごとに別プロセスを起動します。
 */
public class InternalMonitor {
	private static final InternalMonitor INSTANCE = new InternalMonitor();
	private static final String INFO_TASK_START = "_Task_Start";
	private static final String INFO_TASK_END = "_Task_End";
	private static final String DEAD_LOCK = "Deadlock";
	private static final String HEAP = "JVM Heap";
	private static final String GC = "JVM GC";
	private static final String CPU = "CPU Usage";

	// プロセス内部監視連続失敗時の最大許容回数
	private static final Integer FAILD_MAX_COUNT = LoggingProperty.getInstance()
			.getIntProperty(PropertyConstant.FAILD_MAX_COUNT);

	private static ControlLogManager.Logger controlLog = ControlLogManager.getLogger(InternalMonitor.class);
	private static MonitoringLogManager.Logger monitoringLog = MonitoringLogManager.getLogger(InternalMonitor.class);
	private static InternalLogManager.Logger internalLog;
	// 監視タスクの実行スケジューラ
	private static ScheduledExecutorService scheduler;
	// タスクsubmit用クラスのエグゼキュータ
	private static ExecutorService exec;
	// GC回数のカウンター
	private ConcurrentHashMap<String, Long> gccCountMap = new ConcurrentHashMap<String, Long>();
	// デットロック監視の有効無効
	private static boolean enableDlk;
	// ヒープ未使用量監視の有効無効
	private static boolean enableHpr;
	// GC発生頻度監視監視の有効無効
	private static HashMap<Integer, Boolean> enableGccMap;
	// CPU使用率監視の有効無効
	private static boolean enableCpu;
	// デットロック監視でのエラー回数のカウンター
	private static int dlkErrorCount;
	// ヒープ未使用量監視でのエラー回数のカウンター
	private static int hprErrorCount;
	// GC発生頻度監視でのエラー回数のカウンター(複数の累計)
	private static int gccErrorCount;
	// CPU使用率監視でのエラー回数のカウンター
	private static int cpuErrorCount;

	private InternalMonitor() {
		try {
			internalLog = InternalLogManager.getLogger(InternalMonitor.class);
			// 監視、収集どちらかが有効となっているプロセス内部監視を判定する
			int result = monitorJudgement();

			// 有効なプロセス内部監視が存在する場合はエグゼキュータにスレッド数を設定する。
			if (result != 0) {
				exec = Executors.newFixedThreadPool(result);
				internalLog.debug("The number of exec threads is " + result);
			} else {
				internalLog.debug("No internal monitoring enabled. result =" + result);
			}

			// スケジューラにはinfoの定期実行分のスレッドを追加する
			int schedulerThread = result + 1;
			scheduler = Executors.newScheduledThreadPool(schedulerThread);
			internalLog.debug("The number of scheduler threads is " + schedulerThread);
		} catch (Exception e) {
			internalLog.error(e.getMessage(), e);
			throw e;
		}
	}

	public static InternalMonitor getInstance() {
		return INSTANCE;
	}

	public void start() {
		LoggingProperty prop = LoggingProperty.getInstance();
		internalLog.info("start : internal monitor start");

		// デッドロック監視
		if (enableDlk) {
			int dlkInterval = prop.getIntProperty(PropertyConstant.INT_DLK_INTERVAL);
			scheduler.scheduleWithFixedDelay(new DeadLockTaskSubmitter(), 1, dlkInterval, TimeUnit.SECONDS);
			internalLog.info("start : Start monitoring the " + DEAD_LOCK);
		}

		// ヒープ未使用量監視
		if (enableHpr) {
			int hprInterval = prop.getIntProperty(PropertyConstant.INT_HPR_INTERVAL);
			scheduler.scheduleWithFixedDelay(new HeapRemainingTaskSubmitter(), 1, hprInterval, TimeUnit.SECONDS);
			internalLog.info("start : Start monitoring the " + HEAP);
		}

		// GC発生頻度監視 複数項目のため、項目番号ごとに処理を行う
		for (Integer gccPropNum : prop.getGccNumbers()) {
			if (gccPropNum == null) {
				break;
			}

			if (enableGccMap.get(gccPropNum)) {
				String intervalKey = String.format(PropertyConstant.INT_GCC_INTERVAL, gccPropNum);
				Integer interval = prop.getIntProperty(intervalKey);

				scheduler.scheduleWithFixedDelay(new GCCountTaskSubmitter(gccPropNum), 1, interval, TimeUnit.SECONDS);
				internalLog.info("start : Start monitoring the " + GC + gccPropNum);
			}
		}

		// CPU使用率監視
		if (enableCpu) {
			int cpuInterval = prop.getIntProperty(PropertyConstant.INT_CPU_INTERVAL);
			scheduler.scheduleWithFixedDelay(new CpuUsageTaskSubmitter(), 1, cpuInterval, TimeUnit.SECONDS);
			internalLog.info("start : Start monitoring the " + CPU);
		}

		// 定期的なINFORMの実行
		int notifyInterval = prop.getIntProperty(PropertyConstant.INFORM_INTERVAL);
		scheduler.scheduleAtFixedRate(new InfoTask(), 1, notifyInterval, TimeUnit.MINUTES);
	}

	public void stop() {
		exec.shutdown();
		scheduler.shutdownNow();
		internalLog.info("stop : internal monitor stop");
	}

	private void faild(String type, int count, Exception e) {
		if (count > FAILD_MAX_COUNT) {
			controlLog.error(MessageConstant.getFaildMonitorError(type, count));
			if (e == null) {
				internalLog.error("faild : " + MessageConstant.getFaildMonitorError(type, count));
			} else {
				internalLog.error("faild : " + MessageConstant.getFaildMonitorError(type, count), e);
			}
			// エラーの許容回数を超えたため機能を停止する
			LoggingConfigurator.stop();
		} else {
			controlLog.warn(MessageConstant.getFaildMonitorWarn(type));
			if (e == null) {
				internalLog.warn("faild : " + MessageConstant.getFaildMonitorWarn(type));
			} else {
				internalLog.warn("faild : " + MessageConstant.getFaildMonitorWarn(type), e);
			}
		}
	}

	// デッドロック監視タスク
	private static class DeadLockMonitorTask implements Runnable {

		@Override
		public void run() {
			internalLog.debug("run : " + DEAD_LOCK + INFO_TASK_START);
			try {
				ThreadMXBean tMXBean = ManagementFactory.getThreadMXBean();
				long[] lockedThreadIds = tMXBean.findDeadlockedThreads();
				if (lockedThreadIds == null) {
					internalLog.debug("run : " + DEAD_LOCK + INFO_TASK_END + " is safe");
					return;
				}
				for (long id : lockedThreadIds) {
					ThreadInfo info = tMXBean.getThreadInfo(id);
					monitoringLog.logDlk("[" + DEAD_LOCK + "] detected. Detail=" + info);
				}
				dlkErrorCount = 0;
			} catch (Exception e) {
				throw e;
			}
			internalLog.debug("run : " + DEAD_LOCK + INFO_TASK_END);
		}
	}

	// ヒープ未使用量監視タスク
	private static class HeapRemainingMonitorTask implements Runnable {

		@Override
		public void run() {
			LoggingProperty prop = LoggingProperty.getInstance();

			internalLog.debug("run : " + HEAP + INFO_TASK_START);
			try {
				int freeHeapMByte = (int) (Runtime.getRuntime().freeMemory() / 1024 / 1024);
				Integer hprThreshold = prop.getIntProperty(PropertyConstant.INT_HPR_THRESHOLD);

				if (hprThreshold == null) {
					monitoringLog.logHpr("[" + HEAP + "] Remaining=" + freeHeapMByte + "MB");
				} else {
					if (hprThreshold < freeHeapMByte) {
						monitoringLog.logHpr(
								"[" + HEAP + "] Remaining=" + freeHeapMByte + "MB, Threshold=" + hprThreshold + "MB");
					}
				}
				hprErrorCount = 0;
			} catch (Exception e) {
				throw e;
			}
			internalLog.debug("run : " + HEAP + INFO_TASK_END);
		}
	}

	// GC発生頻度監視タスク
	private class GCCountMonitorTask implements Runnable {
		private Integer gccPropNum;

		private GCCountMonitorTask(Integer gccPropNum) {
			this.gccPropNum = gccPropNum;
		}

		private List<String> getGcMethodNames(List<GarbageCollectorMXBean> beans) {
			List<String> methodNames = new ArrayList<String>();
			for (GarbageCollectorMXBean b : beans) {
				methodNames.add(b.getName());
			}
			return methodNames;
		}

		@Override
		public void run() {
			LoggingProperty prop = LoggingProperty.getInstance();
			String methodKey = String.format(PropertyConstant.INT_GCC_METHOD, gccPropNum);
			String thresholdKey = String.format(PropertyConstant.INT_GCC_THRESHOLD, gccPropNum);
			String methodName = prop.getProperty(methodKey);
			Integer threshold = prop.getIntProperty(thresholdKey);

			internalLog.debug("run : " + GC + INFO_TASK_START + ", MethodName=" + methodName);
			try {
				List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();
				if (!getGcMethodNames(beans).contains(methodName)) {
					gccErrorCount++;
					faild(GC, gccErrorCount, null);
					internalLog.debug("run : " + GC + INFO_TASK_END + " Method=" + methodName
							+ " is not found, get_result=" + getGcMethodNames(beans));
					return;
				}
				for (GarbageCollectorMXBean bean : beans) {
					if (bean.getName().equals(methodName)) {
						long lastGccCount;
						long gccCount;
						long difference;

						// 今回のタイミングで取得した値
						gccCount = bean.getCollectionCount();

						// 前回のタイミングで取得した値が存在した場合、差分を出す
						if (gccCountMap.containsKey(methodName)) {
							lastGccCount = gccCountMap.get(methodName);
							difference = gccCount - lastGccCount;
						} else {
							difference = gccCount;
						}

						if (threshold == null) {
							monitoringLog.logGcc("[" + GC + "] GCName=" + methodName + ", Count=" + difference);
						} else {
							if (threshold < difference) {
								monitoringLog.logGcc("[" + GC + "] GCName=" + methodName + ", Count=" + difference
										+ ", Threshold=" + threshold);
							}
						}
						gccCountMap.put(methodName, gccCount);
					}
				}
				gccErrorCount = 0;
			} catch (Exception e) {
				throw e;
			}
			internalLog.debug("run : " + GC + INFO_TASK_END + ", MethodName=" + methodName);
		}
	}

	// CPU使用率監視タスク
	private static class CpuUsageMonitorTask implements Runnable {

		@Override
		public void run() {
			LoggingProperty prop = LoggingProperty.getInstance();

			internalLog.debug("run : " + CPU + INFO_TASK_START);
			try {
				Integer cpuThreshold = prop.getIntProperty(PropertyConstant.INT_CPU_THRESHOLD);
				Integer pid = ProcessInfo.getInstance().getPid();

				OperatingSystemMXBean osMx = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
				Double val = osMx.getProcessCpuLoad();
				int cpuUsage = (int) ((val * 1000) / 10.0);
				if (cpuThreshold == null) {
					monitoringLog.logCpu("[" + CPU + "] PID=" + pid + ", Usage=" + cpuUsage + "%");
				} else {
					if (cpuThreshold < cpuUsage) {
						monitoringLog.logCpu("[" + CPU + "] PID=" + pid + ", Usage=" + cpuUsage + "%, Threshold="
								+ cpuThreshold + "%");
					}
				}
				cpuErrorCount = 0;
			} catch (Exception e) {
				throw e;
			}
			internalLog.debug("run : " + CPU + INFO_TASK_END);
		}
	}

	// タスクsubmit用クラス
	private class DeadLockTaskSubmitter implements Runnable {
		public void run() {
			LoggingProperty prop = LoggingProperty.getInstance();
			int dlkTimeout = prop.getIntProperty(PropertyConstant.INT_DLK_TIMEOUT);
			// 実際の処理を別スレッドで実行
			Future<?> f = exec.submit(new DeadLockMonitorTask());
			try {
				if (dlkTimeout > 0) {
					f.get(dlkTimeout, TimeUnit.SECONDS);
				} else {
					f.get();
				}
			} catch (TimeoutException | ExecutionException e) {
				// Loggingのエラーとしてカウントする
				dlkErrorCount++;
				faild(DEAD_LOCK, dlkErrorCount, e);
			} catch (InterruptedException e) {
				internalLog.error("DeadLockTaskSubmitter :", e);
			}
		}
	}

	// タスクsubmit用クラス
	private class HeapRemainingTaskSubmitter implements Runnable {
		public void run() {
			LoggingProperty prop = LoggingProperty.getInstance();
			int hprTimeout = prop.getIntProperty(PropertyConstant.INT_HPR_TIMEOUT);
			// 実際の処理を別スレッドで実行
			Future<?> f = exec.submit(new HeapRemainingMonitorTask());
			try {
				if (hprTimeout > 0) {
					f.get(hprTimeout, TimeUnit.SECONDS);
				} else {
					f.get();
				}
			} catch (TimeoutException | ExecutionException e) {
				// Loggingのエラーとしてカウントする
				hprErrorCount++;
				faild(HEAP, hprErrorCount, e);
			} catch (InterruptedException e) {
				internalLog.error("HeapRemainingTaskSubmitter :", e);
			}
		}
	}

	// タスクsubmit用クラス
	private class GCCountTaskSubmitter implements Runnable {

		private Integer gccPropNum;

		private GCCountTaskSubmitter(Integer gccPropNum) {
			this.gccPropNum = gccPropNum;
		}

		public void run() {
			LoggingProperty prop = LoggingProperty.getInstance();
			String timeoutKey = String.format(PropertyConstant.INT_GCC_TIMEOUT, gccPropNum);
			int gccTimeout = prop.getIntProperty(timeoutKey);
			// 実際の処理を別スレッドで実行
			Future<?> f = exec.submit(new GCCountMonitorTask(gccPropNum));
			try {
				if (gccTimeout > 0) {
					f.get(gccTimeout, TimeUnit.SECONDS);
				} else {
					f.get();
				}
			} catch (TimeoutException | ExecutionException e) {
				// Loggingのエラーとしてカウントする
				gccErrorCount++;
				faild(GC, gccErrorCount, e);
			} catch (InterruptedException e) {
				internalLog.error("GCCountTaskSubmitter :", e);
			}
		}
	}

	// タスクsubmit用クラス
	private class CpuUsageTaskSubmitter implements Runnable {
		public void run() {
			LoggingProperty prop = LoggingProperty.getInstance();
			int cpuTimeout = prop.getIntProperty(PropertyConstant.INT_CPU_TIMEOUT);
			// 実際の処理を別スレッドで実行
			Future<?> f = exec.submit(new CpuUsageMonitorTask());
			try {
				if (cpuTimeout > 0) {
					f.get(cpuTimeout, TimeUnit.SECONDS);
				} else {
					f.get();
				}
			} catch (TimeoutException | ExecutionException e) {
				// Loggingのエラーとしてカウントする
				cpuErrorCount++;
				faild(CPU, cpuErrorCount, e);
			} catch (InterruptedException e) {
				internalLog.error("CpuUsageTaskSubmitter :", e);
			}
		}
	}

	private static class InfoTask implements Runnable {
		@Override
		public void run() {
			controlLog.info(MessageConstant.getInfoWorking());
		}
	}

	/**
	 * 設定ファイルから監視、収集どちらかが有効かを判断する 結果として有効なプロセス内部監視の件数を返す
	 * 
	 * @return 有効な監視の件数
	 */
	private int monitorJudgement() {
		LoggingProperty prop = LoggingProperty.getInstance();
		int result = 0;

		if (Boolean.valueOf(prop.getProperty(PropertyConstant.INT_DLK_MONITOR))
				|| Boolean.valueOf(prop.getProperty(PropertyConstant.INT_DLK_COLLECT))) {
			enableDlk = true;
			result++;
		}

		if (Boolean.valueOf(prop.getProperty(PropertyConstant.INT_HPR_MONITOR))
				|| Boolean.valueOf(prop.getProperty(PropertyConstant.INT_HPR_COLLECT))) {
			enableHpr = true;
			result++;
		}

		enableGccMap = new HashMap<Integer, Boolean>();
		for (Integer gccPropNum : prop.getGccNumbers()) {
			if (gccPropNum == null) {
				break;
			}
			String monitorKey = String.format(PropertyConstant.INT_GCC_MONITOR, gccPropNum);
			String collectKey = String.format(PropertyConstant.INT_GCC_COLLECT, gccPropNum);
			if (Boolean.valueOf(prop.getProperty(monitorKey)) || Boolean.valueOf(prop.getProperty(collectKey))) {
				enableGccMap.put(gccPropNum, true);
				result++;
			} else {
				enableGccMap.put(gccPropNum, false);
			}
		}

		if (Boolean.valueOf(prop.getProperty(PropertyConstant.INT_CPU_MONITOR))
				|| Boolean.valueOf(prop.getProperty(PropertyConstant.INT_CPU_COLLECT))) {
			enableCpu = true;
			result++;
		}
		return result;
	}
}
