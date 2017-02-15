/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.util.UserRoleCache;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.calendar.util.CalendarCache;
import com.clustercontrol.calendar.util.CalendarPatternCache;
import com.clustercontrol.commons.bean.SettingUpdateInfo;
import com.clustercontrol.commons.util.JpaPersistenceConfig;
import com.clustercontrol.custom.factory.SelectCustom;
import com.clustercontrol.jobmanagement.factory.FullJob;
import com.clustercontrol.jobmanagement.util.JobMultiplicityCache;
import com.clustercontrol.logfile.session.MonitorLogfileControllerBean;
import com.clustercontrol.maintenance.factory.HinemosPropertyInfoCache;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.notify.util.NotifyCache;
import com.clustercontrol.notify.util.NotifyRelationCache;
import com.clustercontrol.performance.util.CollectorMasterCache;
import com.clustercontrol.plugin.HinemosPluginService;
import com.clustercontrol.process.factory.ProcessMasterCache;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.repository.factory.NodeProperty;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.systemlog.util.SystemlogCache;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.StdOutErrLog;
import com.clustercontrol.util.apllog.AplLogger;
import com.clustercontrol.winevent.session.MonitorWinEventControllerBean;

/**
 * Hinemos ManagerのMainクラス<br/>
 */
public class HinemosManagerMain {

	public static final Log log = LogFactory.getLog(HinemosManagerMain.class);

	// shutdownHookが呼ばれるまでmainスレッドを待機させるためのLockオブジェクトおよびフラグ
	private static final Object shutdownLock = new Object();
	private static boolean shutdown = false;

	// 起動モード(この値を参照して、各HinemosPluginは起動状態をswitchする)
	public enum StartupMode { NORMAL, MAINTENANCE };
	public static volatile StartupMode _startupMode;
	
	private static List<StartupTask> startupTaskList = new ArrayList<StartupTask>();
	
	public static final String _hostname;
	
	public static final Path _homeDir;
	public static final Path _etcDir;
	public static final Path _logDir;
	
	public static final int _instanceId;
	public static final int _instanceCount;
	public static final boolean _isClustered;
	
	static {
		// 最初にSNMP4jのAPIを叩くよりも前に、下記コマンドを実行する必要がある。
		org.snmp4j.log.LogFactory.setLogFactory(new org.snmp4j.log.Log4jLogFactory());
		log.info("setLogFactory(Log4jLogFactory)");
		
		_hostname = System.getProperty("hinemos.manager.hostname", "");
		
		_homeDir = Paths.get(System.getProperty("hinemos.manager.home.dir", "/opt/hinemos"));
		_etcDir = Paths.get(System.getProperty("hinemos.manager.etc.dir", "/opt/hinemos/etc"));
		_logDir = Paths.get(System.getProperty("hinemos.manager.log.dir", "/opt/hinemos/var/log"));
		
		_isClustered = Files.exists(_homeDir.resolve("_version_ha"));
		
		int instanceId = 0;
		try {
			instanceId = Integer.parseInt(System.getProperty("hinemos.instance.id", "0"));
		} catch (NumberFormatException e) {
			log.warn("System environment value \"hinemos.instance.id\" is not correct.");
		} finally {
			_instanceId = instanceId;
		}
		
		int instanceCount = 1;
		try {
			instanceCount = Integer.parseInt(System.getProperty("hinemos.instance.count", "1"));
		} catch (NumberFormatException e) {
			log.warn("System environment value \"hinemos.instance.count\" is not correct.");
		} finally {
			_instanceCount = instanceCount;
		}
		
		// java引数からパラメータを読み込む
		String startupModeStr = System.getenv("STARTUP_MODE");
		StartupMode startupMode = StartupMode.NORMAL;

		try {
			startupMode = StartupMode.valueOf(startupModeStr);
		} catch (IllegalArgumentException e) {
			log.warn("System environment value is not correct. (STARTUP_MODE = " + startupModeStr);
		} finally {
			_startupMode = startupMode;
		}
		
		switch (_startupMode) {
		case MAINTENANCE :
			new Thread(new StartupWaitThread(), "StarupWaitingThread").start();
			break;
		case NORMAL :
		default:
			break;
		}
		
		// 標準出力
		StdOutErrLog.initialize();
	}

	/**
	 * Hinemos Managerのmainメソッド<br/>
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) {

		try{
			try {
				if (System.getProperty("systime.iso") != null) {
					String oldVmName = System.getProperty("java.vm.name");
					
					//Systemのメソッドをmockするために、HotSpotを含む名前に変更することが必要
					System.setProperty("java.vm.name", "HotSpot 64-Bit Server VM");

					Class.forName("com.clustercontrol.util.SystemTimeShifter");
					
					System.setProperty("java.vm.name", oldVmName);
				}
			} catch (Exception e) {
				log.error(e);
			} catch (Error e) {
				log.error(e);
			}
			
			long bootTime = System.currentTimeMillis();
			log.info("Hinemos Manager is starting." +
					" (startupMode=" + _startupMode +
					", clustered=" + _isClustered + 
					", locale=" + Locale.getDefault() +
					")");
			
			
			// Hinemos時刻(スケジューラが管理している現在時刻)の設定は、各プラグインサービス起動前に行う。
			// (各プラグインの処理で現在時刻を取得する場合があるため、事前に設定しておく必要がある)
			long offset = HinemosPropertyUtil.getHinemosPropertyNum("common.time.offset", Long.valueOf(0));
			HinemosTime.setTimeOffsetMillis(offset);
			
			// Hinemos独自のタイムゾーン(UTCからのオフセット)をプロパティから取得/設定(ミリ秒単位)
			int timeZoneOffset = HinemosPropertyUtil.getHinemosPropertyNum("common.timezone", Long.valueOf(TimeZone.getDefault().getRawOffset())).intValue();
			HinemosTime.setTimeZoneOffset(timeZoneOffset);
			
			// 参照可能なHinemosPluginを全て生成(create)する
			HinemosPluginService.getInstance().create();

			// 参照可能なHinemosPluginを全て活性化(activate)する
			HinemosPluginService.getInstance().activate();

			// Hinemos Mangerの停止処理を定義する
			Runtime.getRuntime().addShutdownHook(
					new Thread() {
						@Override
						public void run() {
							log.info("shutdown hook called.");
							synchronized (shutdownLock) {
								// Hinemos Managerの停止開始を通知する
								String[] msgArgsShutdown = {_hostname};
								AplLogger.put(PriorityConstant.TYPE_INFO, HinemosModuleConstant.HINEMOS_MANAGER_MONITOR, MessageConstant.MESSAGE_SYS_002_MNG,  msgArgsShutdown);

								// 参照可能なHinemosPluginを全て非活性化(deactivate)する
								HinemosPluginService.getInstance().deactivate();

								// 参照可能なHinemosPluginを全て破棄(destroy)する
								HinemosPluginService.getInstance().destroy();

								log.info("Hinemos Manager is stopped.");

								shutdown = true;
								shutdownLock.notify();
							}
						}
					});

			// 起動所要時間をログ出力する
			long startupTime = System.currentTimeMillis();
			long initializeSec = (startupTime - bootTime) / 1000;
			long initializeMSec = (startupTime - bootTime) % 1000;
			log.info("Hinemos Manager Started in " + initializeSec + "s:" + initializeMSec + "ms");

			// Hinemos Managerの起動完了を通知する
			String[] msgArgsStart = {_hostname};
			AplLogger.put(PriorityConstant.TYPE_INFO, HinemosModuleConstant.HINEMOS_MANAGER_MONITOR, MessageConstant.MESSAGE_SYS_001_MNG, msgArgsStart);

			// Hinemos Managerの停止が完了するまで待機する
			synchronized (shutdownLock) {
				while (! shutdown) {
					try {
						shutdownLock.wait();
					} catch (InterruptedException e) {
						log.warn("shutdown lock interrupted.", e);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException sleepE) { };
					}
				}
			}

			System.exit(0);

		} catch (Throwable e) {
			log.error("unknown error occured.", e);
		}
	}
	
	public interface StartupTask {
		void init();
	}
	
	public static synchronized void addStartupTask(StartupTask task) {
		startupTaskList.add(task);
	}
	
	public static synchronized void executeStartupTask() {
		// reset connection pool
		if (JpaPersistenceConfig.getHinemosEMFactory().isOpen()) {
			log.info("re-initializing database connection...");
			JpaPersistenceConfig.getHinemosEMFactory().close();
		}
		
		// refresh cache (DBと等価にマップされたキャッシュを再構成)
		UserRoleCache.refresh();
		CalendarCache.init();
		CalendarPatternCache.init();
		SettingUpdateInfo.init();
		SelectCustom.refreshCache();
		FullJob.init();
		JobMultiplicityCache.refresh();
		MonitorLogfileControllerBean.refreshCache();
		HinemosPropertyInfoCache.refresh();
		NotifyCache.refresh();
		NotifyRelationCache.refresh();
		CollectorMasterCache.refresh();
		ProcessMasterCache.refresh();
		FacilitySelector.initCacheFacilityTree();
		NodeProperty.init();
		FacilityTreeCache.refresh();
		SystemlogCache.refresh();
		MonitorWinEventControllerBean.refreshCache();
		
		// execute promotion task
		log.info("executing startup tasks...");
		for (StartupTask task : startupTaskList) {
			try {
				task.init();
			} catch (Exception e) {
				log.warn("unknown error occured.", e);
			}
		}
		startupTaskList.clear();
		
		_startupMode = StartupMode.NORMAL;
		log.info("startup mode is changed. (startupMode = " + _startupMode + ")");
	}
	
	private static class StartupWaitThread implements Runnable {
		
		@Override
		public void run() {
			String lockFileName = ".startup.lock";
			
			WatchService watcher = null;
			try {
				String homeDirStr = System.getProperty("hinemos.manager.home.dir", "/opt/hinemos/");
				Path dir = Paths.get(homeDirStr);
				Path lockFile = Paths.get(homeDirStr + "/" + lockFileName);
				
				if (! Files.exists(lockFile)) {
					log.info("creating lock file " + lockFile.toAbsolutePath().toString() + " for proactive features.");
					Files.createFile(lockFile);
				}
				
				watcher = FileSystems.getDefault().newWatchService();
				dir.register(watcher, StandardWatchEventKinds.ENTRY_DELETE);
				
				log.info("waiting removal of lock file " + lockFile.toAbsolutePath() + " for proactive features.");
				
				while (true) {
					boolean deleted = false;
					
					WatchKey key = watcher.take();
					for (WatchEvent<?> event : key.pollEvents()) {
						if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
							@SuppressWarnings("unchecked")
							WatchEvent<Path> eventPath = (WatchEvent<Path>)event;
							Path path = eventPath.context();
							
							if (lockFile.endsWith(path)) {
								log.info(lockFile.toAbsolutePath() + " file is deleted.");
								deleted = true;
							}
						}
					}
					key.reset();
					
					if (deleted) {
						executeStartupTask();
						break;
					}
				}
			} catch (Exception e) {
				log.warn("unknown error occured.", e);
			} finally {
				if (watcher != null) {
					try {
						watcher.close();
					} catch (IOException e) {
						log.warn("unknown error occured.", e);
					}
				}
			}
		}
	}
}