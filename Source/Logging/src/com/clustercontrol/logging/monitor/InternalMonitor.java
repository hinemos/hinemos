/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.logging.monitor;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ThreadInfo;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
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
import com.clustercontrol.logging.LoggingConfigurator;
import com.clustercontrol.logging.constant.MessageConstant;
import com.clustercontrol.logging.constant.PropertyConstant;
import com.clustercontrol.logging.exception.MonitoringFailureException;
import com.clustercontrol.logging.property.LoggingProperty;

/**
 * 内部監視を行うクラスです 各監視種別ごとに別プロセスを起動します。
 */
public class InternalMonitor {

	private static final String DEAD_LOCK = "Deadlock";
	private static final String HEAP = "JVM Heap";
	private static final String GC = "JVM GC";
	private static final String CPU = "CPU Usage";

	private static ControlLogManager.Logger controlLog = ControlLogManager.getLogger(InternalMonitor.class);
	private static MonitoringLogManager.Logger monitoringLog = MonitoringLogManager.getLogger(InternalMonitor.class);
	private static InternalLogManager.Logger internalLog = InternalLogManager.getLogger(InternalMonitor.class);

	// HinemosLogging設定
	private LoggingProperty prop;
	// 監視タスクの実行スケジューラ
	private ScheduledExecutorService scheduler;
	// タスクsubmit用クラスのエグゼキュータ
	private ExecutorService exec;
	// GC回数のカウンター
	private ConcurrentHashMap<String, Long> gccCountMap = new ConcurrentHashMap<String, Long>();
	// デットロック監視の有効無効
	private boolean enableDlk;
	// ヒープ未使用量監視の有効無効
	private boolean enableHpr;
	// GC発生頻度監視監視の有効無効
	private HashMap<Integer, Boolean> enableGccMap;
	// CPU使用率監視の有効無効
	private boolean enableCpu;

	public InternalMonitor(LoggingProperty prop) {
		this.prop = prop;
		// 監視、収集どちらかが有効となっているプロセス内部監視を判定する
		int result = monitorJudgement();

		// 有効なプロセス内部監視が存在する場合はエグゼキュータにスレッド数を設定する。
		if (result > 0) {
			exec = Executors.newFixedThreadPool(result);
			internalLog.debug("The number of exec threads is " + result);
		} else {
			internalLog.debug("No internal monitoring enabled. result =" + result);
		}

		// スケジューラにはinfoの定期実行分のスレッドを追加する
		int schedulerThread = result + 1;
		scheduler = Executors.newScheduledThreadPool(schedulerThread);
		internalLog.debug("The number of scheduler threads is " + schedulerThread);
	}

	/**
	 * プロセス内部監視を開始します。
	 */
	public void start() {
		internalLog.info("start : internal monitor start");

		// プロセス内部監視連続失敗時の最大許容回数
		Integer failedMaxCount = prop.getIntProperty(PropertyConstant.FAILD_MAX_COUNT);

		// デッドロック監視
		if (enableDlk) {
			int dlkInterval = prop.getIntProperty(PropertyConstant.INT_DLK_INTERVAL);
			int dlkTimeout = prop.getIntProperty(PropertyConstant.INT_DLK_TIMEOUT);

			scheduler.scheduleWithFixedDelay(new InternalMonitorTaskSubmitter<List<ThreadInfo>>(DEAD_LOCK, exec,
					new DeadlockMonitorTask(), dlkTimeout, failedMaxCount) {

				@Override
				protected void normallyProcess(List<ThreadInfo> result) {
					if (result == null) {
						// nullの場合は検知されなかったと判定する
						return;
					}
					internalLog.debug(toString() + " : detected. thread count=" + result.size());
					for (ThreadInfo info : result) {
						monitoringLog.logDlk("[" + DEAD_LOCK + "] detected. Detail=" + info);
						internalLog.trace(toString() + " : " + info);
					}
				}

			}, 1, dlkInterval, TimeUnit.SECONDS);
			internalLog.info("start : Start monitoring the " + DEAD_LOCK);
		}

		// ヒープ未使用量監視
		if (enableHpr) {
			int hprInterval = prop.getIntProperty(PropertyConstant.INT_HPR_INTERVAL);
			int hprTimeout = prop.getIntProperty(PropertyConstant.INT_HPR_TIMEOUT);

			scheduler.scheduleWithFixedDelay(new InternalMonitorTaskSubmitter<Long>(HEAP, exec,
					new HeapRemainingMonitorTask(), hprTimeout, failedMaxCount) {

				@Override
				protected void normallyProcess(Long result) {
					Integer hprThreshold = prop.getIntProperty(PropertyConstant.INT_HPR_THRESHOLD);

					internalLog.debug(toString() + " : Ramaining=" + result);
					if (hprThreshold == null) {
						monitoringLog.logHpr("[" + HEAP + "] Remaining=" + result + "MB");
					} else {
						if (hprThreshold > result) {
							// 閾値を下回った場合
							monitoringLog.logHpr(
									"[" + HEAP + "] Remaining=" + result + "MB, Threshold=" + hprThreshold + "MB");
						}
					}
				}

			}, 1, hprInterval, TimeUnit.SECONDS);
			internalLog.info("start : Start monitoring the " + HEAP);
		}

		// GC発生頻度監視 複数項目のため、項目番号ごとに処理を行う
		for (Integer gccPropNum : prop.getGccNumbers()) {
			if (gccPropNum == null) {
				break;
			}

			if (enableGccMap.get(gccPropNum)) {
				int gccInterval = prop.getIntProperty(PropertyConstant.INT_GCC_INTERVAL, gccPropNum);
				int gccTimeout = prop.getIntProperty(PropertyConstant.INT_GCC_TIMEOUT, gccPropNum);
				String methodName = prop.getProperty(PropertyConstant.INT_GCC_METHOD, gccPropNum);
				String submitterName = GC + "(" + methodName + ")";

				scheduler.scheduleWithFixedDelay(new InternalMonitorTaskSubmitter<List<GarbageCollectorMXBean>>(
						submitterName, exec, new GcCountMonitorTask(), gccTimeout, failedMaxCount) {

					@Override
					protected void normallyProcess(List<GarbageCollectorMXBean> result)
							throws MonitoringFailureException {
						Integer threshold = prop.getIntProperty(PropertyConstant.INT_GCC_THRESHOLD, gccPropNum);

						boolean found = false;
						for (GarbageCollectorMXBean bean : result) {
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

								internalLog.debug(toString() + " : Count=" + difference + ", Total=" + gccCount);
								if (threshold == null) {
									monitoringLog.logGcc("[" + GC + "] GCName=" + methodName + ", Count=" + difference);
								} else {
									if (threshold < difference) {
										// 閾値を上回った場合
										monitoringLog.logGcc("[" + GC + "] GCName=" + methodName + ", Count="
												+ difference + ", Threshold=" + threshold);
									}
								}
								gccCountMap.put(methodName, gccCount);

								found = true;
								break;
							} else {
								internalLog.trace("run : not match. Specified collector=" + methodName
										+ " Current collector=" + bean.getName());
							}
						}

						if (!found) {
							throw new MonitoringFailureException("Collector'" + methodName + "' is not found.");
						}
					}

				}, 1, gccInterval, TimeUnit.SECONDS);
				internalLog.info("start : Start monitoring the " + submitterName + gccPropNum);
			}
		}

		// CPU使用率監視
		if (enableCpu) {
			int cpuInterval = prop.getIntProperty(PropertyConstant.INT_CPU_INTERVAL);
			int cpuTimeout = prop.getIntProperty(PropertyConstant.INT_CPU_TIMEOUT);

			scheduler.scheduleWithFixedDelay(new InternalMonitorTaskSubmitter<Integer>(CPU, exec,
					new CpuUsageMonitorTask(), cpuTimeout, failedMaxCount) {

				@Override
				protected void normallyProcess(Integer result) {
					Integer cpuThreshold = prop.getIntProperty(PropertyConstant.INT_CPU_THRESHOLD);
					Integer pid = LoggingConfigurator.getProcessInfo().getPid();

					internalLog.debug(toString() + " : Usage=" + result);
					if (cpuThreshold == null) {
						monitoringLog.logCpu("[" + CPU + "] PID=" + pid + ", Usage=" + result + "%");
					} else {
						if (cpuThreshold < result) {
							// 閾値を上回った場合
							monitoringLog.logCpu("[" + CPU + "] PID=" + pid + ", Usage=" + result + "%, Threshold="
									+ cpuThreshold + "%");
						}
					}
				}

			}, 1, cpuInterval, TimeUnit.SECONDS);
			internalLog.info("start : Start monitoring the " + CPU);
		}

		// 定期的なINFORMの実行
		int notifyInterval = prop.getIntProperty(PropertyConstant.INFO_INTERVAL);
		scheduler.scheduleAtFixedRate(new InfoTask(), 1, notifyInterval, TimeUnit.MINUTES);
	}

	/**
	 * プロセス内部監視を停止します。
	 */
	public void stop() {
		if (exec != null) {
			exec.shutdown();
		}
		if (scheduler != null) {
			scheduler.shutdownNow();
		}
		internalLog.info("stop : internal monitor stop");
	}

	/**
	 * プロセス内部監視タスクsubmit用クラス
	 *
	 * @param <T>
	 */
	public static abstract class InternalMonitorTaskSubmitter<T> implements Runnable {
		protected final String name;
		protected final ExecutorService executor;
		protected final Callable<T> task;
		protected final int timeout;
		protected final int failedMax;

		protected int errorCount = 0;
		protected Object lock = new Object();

		public InternalMonitorTaskSubmitter(String name, ExecutorService executor, Callable<T> task, int timeout,
				int failedMax) {
			this.name = name;
			this.executor = executor;
			this.task = task;
			this.timeout = timeout;
			this.failedMax = failedMax;
		}

		@Override
		public void run() {
			internalLog.debug("run : " + toString());
			// 実際の処理を別スレッドで実行
			Future<T> f = executor.submit(task);
			try {
				T result = null;
				if (timeout > 0) {
					result = f.get(timeout, TimeUnit.SECONDS);
				} else {
					result = f.get();
				}
				normallyProcess(result);
				synchronized (lock) {
					errorCount = 0;
				}
			} catch (TimeoutException | ExecutionException | MonitoringFailureException e) {
				failed(e);
			} catch (InterruptedException e) {
				internalLog.error(toString(), e);
			}
		}

		protected abstract void normallyProcess(T result) throws MonitoringFailureException;

		protected void failed(Exception e) {
			// 呼び出された時点でカウントアップする
			synchronized (lock) {
				errorCount++;
			}

			if (errorCount > failedMax) {
				controlLog.error(MessageConstant.getFaildMonitorError(name, errorCount));
				if (e == null) {
					internalLog.error("failed : " + MessageConstant.getFaildMonitorError(name, errorCount));
				} else {
					internalLog.error("failed : " + MessageConstant.getFaildMonitorError(name, errorCount), e);
				}
				// エラーの許容回数を超えたため機能を停止する
				LoggingConfigurator.stop(true);
			} else {
				controlLog.warn(MessageConstant.getFaildMonitorWarn(name));
				if (e == null) {
					internalLog.warn("failed : " + MessageConstant.getFaildMonitorWarn(name));
				} else {
					internalLog.warn("failed : " + MessageConstant.getFaildMonitorWarn(name), e);
				}
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(this.getClass().getSimpleName());
			sb.append("(");
			sb.append(name);
			sb.append(")");
			return sb.toString();
		}
	}

	private static class InfoTask implements Runnable {
		@Override
		public void run() {
			internalLog.debug("run : InfoTask");
			controlLog.info(MessageConstant.getInfoWorking());
		}
	}

	/**
	 * 設定ファイルから監視、収集どちらかが有効かを判断する 結果として有効なプロセス内部監視の件数を返す
	 * 
	 * @return 有効な監視の件数
	 */
	private int monitorJudgement() {
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
