/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.AgentNodeConfigEndPointWrapper;
import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.agent.repository.NodeConfigConstant.Function;
import com.clustercontrol.agent.repository.NodeConfigConstant.Result;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.agent.util.CalendarWSUtil;
import com.clustercontrol.agent.util.CollectorId;
import com.clustercontrol.agent.util.CollectorTask;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PluginConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.repository.bean.NodeConfigSettingItem;
import com.clustercontrol.repository.bean.NodeRegisterFlagConstant;
import com.clustercontrol.util.CommandCreator;
import com.clustercontrol.util.CommandExecutor;
import com.clustercontrol.util.DateUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.CommandExecutor.CommandResult;
import com.clustercontrol.ws.agent.OutputBasicInfo;
import com.clustercontrol.ws.agentnodeconfig.FacilityNotFound_Exception;
import com.clustercontrol.ws.agentnodeconfig.HinemosUnknown_Exception;
import com.clustercontrol.ws.agentnodeconfig.InvalidRole_Exception;
import com.clustercontrol.ws.agentnodeconfig.InvalidUserPass_Exception;
import com.clustercontrol.ws.agentnodeconfig.NodeNetworkInterfaceInfo;
import com.clustercontrol.ws.repository.NodeConfigCustomInfo;
import com.clustercontrol.ws.repository.NodeConfigSetting;
import com.clustercontrol.ws.repository.NodeCpuInfo;
import com.clustercontrol.ws.repository.NodeCustomInfo;
import com.clustercontrol.ws.repository.NodeDiskInfo;
import com.clustercontrol.ws.repository.NodeFilesystemInfo;
import com.clustercontrol.ws.repository.NodeHostnameInfo;
import com.clustercontrol.ws.repository.NodeInfo;
import com.clustercontrol.ws.repository.NodeMemoryInfo;
import com.clustercontrol.ws.repository.NodeNetstatInfo;
import com.clustercontrol.ws.repository.NodePackageInfo;
import com.clustercontrol.ws.repository.NodeProcessInfo;

/**
 * Collector using command (register one Collector against one Command
 * Monitoring)
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
public class NodeConfigCollector implements CollectorTask, Runnable {

	private static Log log = LogFactory.getLog(NodeConfigCollector.class);
	private static final String DELIMITER = "() : ";

	public static final int _collectorType = PluginConstant.TYPE_REPOSITORY;

	// scheduler thread for configuration
	private ScheduledExecutorService _scheduler;

	// スレッド名.
	private String threadName = "";
	// 中断対象スレッド名.
	private String interruptThreadName = null;

	// 開始時刻(出力日時計算用).
	private long startedMillis = 0;

	private NodeConfigSetting config;

	private final Function function;
	
	private List<NodeInfo> nodeInfoList;

	// Managerメッセージ通知用の送信Queue
	private static SendQueue sendQueue;

	// collector's thread pool
	public static final String _commandMode;
	public static final String _commandModeDefault = "auto";
	public static boolean _commandLogin = false;

	public static final int _bufferSize;
	public static final int _bufferSizeDefault = 512;
	public static final int _customBufferSize;
	public static final int _customBufferSizeDefault = 1024;

	public static final Charset _charset;
	public static final Charset _charsetDefault = Charset.forName("UTF-8");

	public static final long _timeoutInterval;
	public static final long _timeoutIntervalDefault = 3600000L;

	static {
		// read command mode
		String commandModeStr = AgentProperties.getProperty("repository.cmdb.command.mode");
		if (commandModeStr == null) {
			log.info("repository.cmdb.command.mode uses default value " + _commandModeDefault);
			commandModeStr = _commandModeDefault;
		} else if (!("windows".equals(commandModeStr) || "unix".equals(commandModeStr) || "unix.su".equals(commandModeStr)
				|| "compatible".equals(commandModeStr) || "regacy".equals(commandModeStr)
				|| "auto".equals(commandModeStr))) {
			log.warn("repository.cmdb.command.mode uses " + _commandModeDefault + ". (" + commandModeStr + " is not collect)");
			commandModeStr = _commandModeDefault;
		}
		_commandMode = commandModeStr;

		// read command login
		String loginFlagStr = AgentProperties.getProperty("repository.cmdb.command.login");
		if (loginFlagStr == null) {
			log.info("repository.cmdb.command.login uses default value false");
		} else {
			_commandLogin = Boolean.parseBoolean(loginFlagStr);
			if (!"false".equals(loginFlagStr) && !"true".equals(loginFlagStr)) {
				log.warn("repository.cmdb.command.login uses false. (" + loginFlagStr + " is not collect)");
			}
		}

		// read buffer size
		String bufferSizeStr = AgentProperties.getProperty("repository.cmdb.buffer");
		int bufferSize = _bufferSizeDefault;
		if (bufferSizeStr == null) { 
			log.info("repository.cmdb.buffer uses default value " + _bufferSizeDefault);
		} else {
			try {
				bufferSize = Integer.parseInt(bufferSizeStr);
			} catch (NumberFormatException e) {
				log.warn("repository.cmdb.buffer uses " + _bufferSizeDefault + ". (" + bufferSizeStr + " is not collect)");
			}
			if (bufferSize < 0) {
				bufferSize = _bufferSizeDefault;
				log.warn("repository.cmdb.buffer uses " + _bufferSizeDefault + ". (" + bufferSizeStr + " is negative, it is invalid)");
			}
		}
		_bufferSize = bufferSize;

		// read custom buffer size
		String custombufferSizeStr = AgentProperties.getProperty("repository.cmdb.custom.buffer");
		int customBufferSize = _customBufferSizeDefault;
		if (custombufferSizeStr == null) {
			log.info("repository.cmdb.custom.buffer uses default value " + _customBufferSizeDefault);
		} else {
			try {
				customBufferSize = Integer.parseInt(custombufferSizeStr);
			} catch (NumberFormatException e) {
				log.warn("repository.cmdb.custom.buffer uses " + _customBufferSizeDefault + ". (" + custombufferSizeStr
						+ " is not collect)");
			}
			if (customBufferSize < 0) {
				customBufferSize = _customBufferSizeDefault;
				log.warn("repository.cmdb.custom.buffer uses " + _customBufferSizeDefault + ". (" + custombufferSizeStr
						+ " is negative, it is invalid)");
			}
		}
		_customBufferSize = customBufferSize;

		// read charset
		String charsetStr = AgentProperties.getProperty("repository.cmdb.charset");
		Charset charset = _charsetDefault;
		if (charsetStr == null) {
			log.info("repository.cmdb.charset uses default value " + _charsetDefault);
		} else {
			try {
				charset = Charset.forName(charsetStr);
			} catch (Exception e) {
				log.warn("repository.cmdb.charset uses " + _charsetDefault.toString() + ". (" + charsetStr
						+ " is not collect)");
			}
		}
		_charset = charset;

		// timeout time
		String timeoutIntervalStr = AgentProperties.getProperty("repository.cmdb.timeout");
		long timeoutInterval = _timeoutIntervalDefault;
		if (timeoutIntervalStr == null) {
			log.info("repository.cmdb.timeout uses default value " + _timeoutIntervalDefault);
		} else {
			try {
				timeoutInterval = Long.parseLong(timeoutIntervalStr);
			} catch (Exception e) {
				log.warn("repository.cmdb.timeout uses " + _timeoutIntervalDefault + ". (" + timeoutIntervalStr
						+ " is not collect)");
			}
			if (timeoutInterval < 0) {
				timeoutInterval = _timeoutIntervalDefault;
				log.warn("repository.cmdb.timeout uses " + _timeoutIntervalDefault + ". (" + timeoutIntervalStr
						+ " is negative, it is invalid)");
			}
		}
		_timeoutInterval = timeoutInterval;

	}

	// スクリプトファイル、TSVファイルの共通パス
	private static String commonPath = "";

	// 取得対象フラグ
	private Map<String, Boolean> collectFlgMap = new HashMap<String, Boolean>();

	// Windowsフラグ
	private boolean winFlg = false;

	private static volatile int _threadCount = 0;

	/** 即時実行フラグ. */
	private boolean runOnce = false;
	
	// --コンストラクタ.
	/**
	 * 収集設定指定コンストラクタ.<br>
	 * <br>
	 * CMDB収集設定が存在する場合はこちらを使うこと<br>
	 */
	public NodeConfigCollector(NodeConfigSetting config, Function function, List<NodeInfo> nodeInfoList) {
		this.config = config;
		this.function = function;
		this.nodeInfoList = nodeInfoList;
		
	}

	/**
	 * 機能指定コンストラクタ.<br>
	 * <br>
	 * ノード自動登録などCMDB機能と異なる機能で紐づく設定がない機能で利用する場合に、<br>
	 * TSVファイル生成用の最低限の情報をセットするためのコンストラクタ.<br>
	 */
	public NodeConfigCollector(Function function) {
		this.function = function;
		switch (function) {
		case NODE_REGISTER:
			NodeConfigSetting dto = new NodeConfigSetting();
			dto.setSettingId("NodeRegister");
			dto.setFacilityId("Agent");
			this.config = dto;
			break;
		default:
			break;
		}
	}

	// --開始メソッド郡.
	/**
	 * 通常の定期的な構成情報収集の開始メソッド.
	 **/
	@Override
	public void start() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// ■構成情報の取得対象を設定
		setTargetConfigFlg(config);
		if (log.isDebugEnabled()) {
			log.debug("config : " + config);
		}

		// 収集の基準となる時間範囲を取得.
		Long rangeMillisLong = TimeUnit.MINUTES.toMillis(this.config.getLoadDistributionRange());
		int rangeMillis = 0;
		if (Integer.MAX_VALUE < rangeMillisLong.longValue()) {
			rangeMillis = Integer.MAX_VALUE;
		} else {
			rangeMillis = rangeMillisLong.intValue();
		}
		String message = "";
		String originMsg = "";
		String[] args = null;
		String rangeMinStr = "";
		String intervalMinStr = "";
		if (rangeMillis >= this.config.getRunInterval()) {
			log.info(methodName + DELIMITER + "Invalid range of load distribution defined as Hinemos property,"//
					+ " because it is over the interval on setting." //
					+ " settingID=[" + this.config.getSettingId() + "]" // 設定ID.
					+ ", range=" + rangeMillis + " [msec]" // 負荷分散範囲.
					+ ", interval=" + this.config.getRunInterval() + " [msec]"); // 間隔.

			message = MessageConstant.MESSAGE_NODE_CONFIG_SETTING_OVER_INTERNAL.getMessage();
			rangeMinStr = Long.toString(TimeUnit.MILLISECONDS.toMinutes(rangeMillis));
			intervalMinStr = Long.toString(TimeUnit.MILLISECONDS.toMinutes(this.config.getRunInterval()));
			args = new String[] { this.config.getSettingId(), intervalMinStr, rangeMinStr,
					this.config.getReferenceTime() };
			originMsg = MessageConstant.MESSAGE_NODE_CONFIG_SETTING_TIME_DETAIL.getMessage(args);
			sendMessage(PriorityConstant.TYPE_WARNING, message, originMsg, this.config.getSettingId());

			rangeMillis = -1;
		}

		// 基準となる時刻を取得.
		long refTimemillis = 0;
		long now = HinemosTime.currentTimeMillis();
		try {
			// 実行日の基準時刻を取得する.
			String todayStr = DateUtil.millisToString(now, "yyyyMMdd");
			refTimemillis = DateUtil.dateStrToMillis(todayStr + this.config.getReferenceTime(), "yyyyMMddHH:mm");

		} catch (InvalidSetting e) {
			// Managerで設定取得時に形式チェックしてるのでここは入らない想定.
			log.warn(methodName + DELIMITER + "Invalid reference time defined as Hinemos property."//
					+ " reference time=[" + this.config.getReferenceTime() + "]");

			message = MessageConstant.MESSAGE_NODE_CONFIG_INVALID_REFERENCE_TIME.getMessage();
			rangeMinStr = Long.toString(TimeUnit.MILLISECONDS.toMinutes(rangeMillis));
			intervalMinStr = Long.toString(TimeUnit.MILLISECONDS.toMinutes(this.config.getRunInterval()));
			args = new String[] { this.config.getSettingId(), rangeMinStr, intervalMinStr,
					this.config.getReferenceTime() };
			originMsg = MessageConstant.MESSAGE_NODE_CONFIG_SETTING_TIME_DETAIL.getMessage(args);
			sendMessage(PriorityConstant.TYPE_WARNING, message, originMsg, this.config.getSettingId());

			rangeMillis = -1;
		}

		// 基準時刻を直近の未来時刻になるよう調整.
		if (rangeMillis < 0) {
			log.debug(methodName + DELIMITER + "skipped reference time calcultaion." // 基準時刻は無視.
					+ " settingID=[" + this.config.getSettingId() + "]"); // 設定ID.

		} else if (refTimemillis > now) {
			// 現在時刻より未来の場合は、一番直近の時刻になるように計算.
			long pastMillis = refTimemillis;
			while (pastMillis > now) {
				log.trace(methodName + DELIMITER + "to subtract reference time." // 基準時刻を元に開始時刻を直近の未来時刻として引き算.
						+ " settingID=[" + this.config.getSettingId() + "]" // 設定ID.
						+ ", now=[" + new Date(now).toString() + "]" // 現在時刻.
						+ ", reference time=[" + new Date(refTimemillis).toString() + "]" // 基準時刻.
						+ ", subtractedMillis=[" + new Date(refTimemillis).toString() + "]"); // 計算した時刻.
				refTimemillis = pastMillis;
				pastMillis = pastMillis - this.config.getRunInterval();
			}
			log.debug(methodName + DELIMITER + "subtracted reference time." // 基準時刻を元に開始時刻を直近の未来時刻として引き算.
					+ " settingID=[" + this.config.getSettingId() + "]" // 設定ID.
					+ ", now=[" + new Date(now).toString() + "]"// 現在時刻.
					+ ", reference time=[" + new Date(refTimemillis).toString() + "]"); // 基準時刻.

		} else {
			// 現在時刻より前の場合は、未来になるように計算.
			while (refTimemillis <= now) {
				log.trace(methodName + DELIMITER + "to add reference time." // 基準時刻を元に開始時刻を直近の未来時刻として足し算.
						+ " settingID=[" + this.config.getSettingId() + "]" // 設定ID.
						+ ", now=[" + new Date(now).toString() + "]"// 現在時刻.
						+ ", reference time=[" + new Date(refTimemillis).toString() + "]"); // 基準時刻.
				refTimemillis = refTimemillis + this.config.getRunInterval();
			}
			log.debug(methodName + DELIMITER + "added reference time." // 基準時刻を元に開始時刻を直近の未来時刻として足し算.
					+ " settingID=[" + this.config.getSettingId() + "]" // 設定ID.
					+ ", now=[" + new Date(now).toString() + "]"// 現在時刻.
					+ ", reference time=[" + new Date(refTimemillis).toString() + "]"); // 基準時刻.
		}

		// determine startup delay (using monitorId for random seed)
		int delay = 0;
		// 設定が書き換えられたタイミングで乱数seedがずれてしまわないよう考慮.
		long seed = this.config.getFacilityId().hashCode() + this.config.getSettingId().hashCode();
		long startup = 0L;
		Random random = new Random(seed);
		if (rangeMillis < 0) {
			// determine startup time
			//
			// example)
			// delay : 15 [sec]
			// interval : 300 [sec]
			// now : 2000-1-1 00:00:10
			// best startup : 2000-1-1 00:00:15
			delay = random.nextInt(this.config.getRunInterval());
			// 設定が書き換えられたタイミングで実行タイミングがずれてしまわないように考慮.
			startup = config.getRunInterval() * (now / config.getRunInterval()) + delay;
			if (startup <= now) {
				startup = startup + config.getRunInterval();
			}
		} else if (rangeMillis == 0) {
			startup = refTimemillis;
		} else {
			delay = random.nextInt(rangeMillis);
			startup = refTimemillis + delay;
		}

		log.info(methodName + DELIMITER + "scheduled to acquire configuration of node regularly."//
				+ " settingID=[" + this.config.getSettingId() + "]" // 設定ID.
				+ ", start=[" + String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", new Date(startup)) + "]" // 開始時刻.
				+ ", interval=" + config.getRunInterval() + " [msec]" // 間隔.
				+ ", delay=" + delay + " [msec]" // 遅延時間(乱数取得結果).
				+ ", reference time(setting)=[" + this.config.getReferenceTime() + "]" // 基準時刻(設定).
				+ ", reference time(calculated)=["
				+ String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", new Date(refTimemillis)) + "]" // 基準時刻(計算結果).
				+ ", range(setting)=" + rangeMillisLong + " [msec]" // 負荷分散範囲(設定).
				+ ", range(calculated)=" + rangeMillis + " [msec]"); // 負荷分散範囲(計算).

		// initialize scheduler thread
		_scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				_threadCount++;
				threadName = "NodeConfigCollectorScheduler-" + _threadCount;
				return new Thread(r, threadName);
			}
		});

		// start scheduler
		// when agent startup. this is called twice. first execution is after
		// interval to avoid double execution.
		now = HinemosTime.currentTimeMillis();
		long startMillis = startup - now;
		log.trace(methodName + DELIMITER + "calculated to 'startMillis'." // ミリ秒後に開始のミリ秒を計算.
				+ " settingID=[" + this.config.getSettingId() + "]" // 設定ID.
				+ ", startMillis=" + startMillis + "[msec]");// 現在時刻.

		while (startMillis < 0) {
			startup = startup + config.getRunInterval();
			log.info(methodName + DELIMITER + "added time to start acquireing configuration of node regularly." // ギリギリ過去日時になったので調整.
					+ " settingID=[" + this.config.getSettingId() + "]" // 設定ID.
					+ ", now=[" + String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", new Date(now)) + "]" // 現在時刻.
					+ ", start=[" + String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", new Date(startup)) + "]"); // 開始時刻.

			startMillis = startup - now;
			log.trace(methodName + DELIMITER + "recalculated to 'startMillis'." // ミリ秒後に開始のミリ秒を再計算.
					+ " settingID=[" + this.config.getSettingId() + "]" // 設定ID.
					+ ", startMillis=" + startMillis + "[msec]");// 現在時刻.
		}
		_scheduler.scheduleWithFixedDelay(this, startMillis, config.getRunInterval(), TimeUnit.MILLISECONDS);
		this.startedMillis = startup;
	}

	/**
	 * NIC情報のみ取得する
	 * 
	 * @return List<NodeNetworkInterfaceInfo>
	 */
	public List<NodeNetworkInterfaceInfo> getNICList() {
		if (log.isDebugEnabled()) {
			log.debug("getNICList() : start");
		}
		// ■構成情報の取得対象を設定（NIC情報のみ）
		this.initializeColletFlgMap();
		this.collectFlgMap.put(NodeConfigSettingItem.HW_NIC.name(), Boolean.TRUE);

		// ■構成情報取);
		NodeInfo nInfo = getRecord(new NodeInfo(), true);
		List<NodeNetworkInterfaceInfo> nicInfoList = nInfo.getNodeNetworkInterfaceInfo();

		if (log.isDebugEnabled()) {
			log.debug("getNICList() : end");
		}

		return nicInfoList;
	}

	/**
	 * 即時実行用の開始メソッド.
	 * 
	 * @param instructedDate
	 *            Managerが即時実行の指示を受けた日時
	 * @param loadDistributionTime
	 *            Manager負荷分散間隔
	 **/
	public void runCollect(Long instructedDate, Long loadDistributionTime) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		this.runOnce = true;

		// 遅延時間の算出.
		long now = HinemosTime.currentTimeMillis();
		Long maxDelayTime = Long.valueOf(instructedDate.longValue() + loadDistributionTime.longValue());
		Long maxDelayMillis = Long.valueOf(maxDelayTime.longValue() - now);
		int maxDelayMillisInt = 0;
		if (Integer.MAX_VALUE < maxDelayMillis.longValue()) {
			maxDelayMillisInt = Integer.MAX_VALUE;
		} else if (maxDelayMillisInt < 1) {
			maxDelayMillisInt = 1;
		} else {
			maxDelayMillisInt = maxDelayMillis.intValue();
		}

		int delay = new SecureRandom().nextInt(maxDelayMillisInt);

		// 収集実行予定時刻をログ出力.
		long startup = now + delay;
		String startupTime = new Date(startup).toString();
		String instructedTime = new Date(instructedDate.longValue()).toString();
		log.info(methodName + DELIMITER + "scheduled to run collecting configuration of node."//
				+ " settingID=[" + this.config.getSettingId() + "]" // 設定ID.
				+ ", start=[" + startupTime + "]"// 開始時刻.
				+ ", instructed=[" + instructedTime + "]"); // 即時実行の指示を受けた日時.

		// 構成情報の取得対象を設定.
		setTargetConfigFlg(config);
		if (log.isDebugEnabled()) {
			log.debug("config : " + config);
		}

		// 指定遅延時間後に収集処理開始(1回のみ).
		_scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				_threadCount++;
				threadName = "NodeConfigCollectorScheduler-" + _threadCount;
				return new Thread(r, threadName);
			}
		});
		_scheduler.schedule(this, delay, TimeUnit.MILLISECONDS);
	}

	/**
	 * 取得対象チェック
	 * 
	 * @param config
	 *            Managerから渡された収集設定.
	 */
	private void setTargetConfigFlg(NodeConfigSetting config) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		this.initializeColletFlgMap();
		StringBuilder logSettingItemIds = new StringBuilder();
		boolean isTop = true;
		for (String settingItemId : config.getNodeConfigSettingItemList()) {
			if (!isTop) {
				logSettingItemIds.append(", ");
			}
			this.collectFlgMap.put(settingItemId, Boolean.TRUE);
			logSettingItemIds.append(settingItemId);
			isTop = false;
		}
		log.info(methodName + DELIMITER
				+ String.format("put as target to collect. configId=[%s]", logSettingItemIds.toString()));
	}

	/**
	 * 収集対象マップの初期化.
	 */
	private void initializeColletFlgMap() {
		this.collectFlgMap = new HashMap<String, Boolean>();
		this.collectFlgMap.put(NodeConfigSettingItem.PROCESS.name(), Boolean.FALSE);
		this.collectFlgMap.put(NodeConfigSettingItem.PACKAGE.name(), Boolean.FALSE);
		this.collectFlgMap.put(NodeConfigSettingItem.HW_NIC.name(), Boolean.FALSE);
		this.collectFlgMap.put(NodeConfigSettingItem.HW_CPU.name(), Boolean.FALSE);
		this.collectFlgMap.put(NodeConfigSettingItem.HW_DISK.name(), Boolean.FALSE);
		this.collectFlgMap.put(NodeConfigSettingItem.HW_FILESYSTEM.name(), Boolean.FALSE);
		this.collectFlgMap.put(NodeConfigSettingItem.OS.name(), Boolean.FALSE);
		this.collectFlgMap.put(NodeConfigSettingItem.HOSTNAME.name(), Boolean.FALSE);
		this.collectFlgMap.put(NodeConfigSettingItem.HW_MEMORY.name(), Boolean.FALSE);
		this.collectFlgMap.put(NodeConfigSettingItem.NETSTAT.name(), Boolean.FALSE);
		this.collectFlgMap.put(NodeConfigSettingItem.CUSTOM.name(), Boolean.FALSE);
	}

	// --外部からの操作用のメソッド郡.
	/**
	 * Manager送信用のQueueセット(他の機能と一緒に送るので外部操作).
	 */
	public static void setSendQueue(SendQueue sendQueue) {
		NodeConfigCollector.sendQueue = sendQueue;
	}

	@Override
	public synchronized void update(CollectorTask task) {
		if (!(task instanceof NodeConfigCollector)) {
			log.warn("this is not instance of NodeConfigCollector : " + task);
			return;
		}

		setConfig(((NodeConfigCollector) task).getConfig(),((NodeConfigCollector) task).getNodeInfoList());
	}

	public synchronized NodeConfigSetting getConfig() {
		return config;
	}
	
	public synchronized List<NodeInfo> getNodeInfoList(){
		return nodeInfoList;
	}

	public synchronized void setConfig(NodeConfigSetting newConfig, List<NodeInfo> nodeInfoList) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		NodeConfigSetting currentConfig = config;
		this.config = newConfig;
		this.nodeInfoList = nodeInfoList;
		setTargetConfigFlg(config);
		if (log.isDebugEnabled()) {
			log.debug("config : " + config);
		}

		if (currentConfig.getRunInterval().intValue() != newConfig.getRunInterval().intValue()
				|| !currentConfig.getReferenceTime().equals(newConfig.getReferenceTime())
				|| !currentConfig.getLoadDistributionRange().equals(newConfig.getLoadDistributionRange())) {
			log.info(methodName + DELIMITER + "interval or reference-time are changed." //
					+ " settingID=[" + this.config.getSettingId() + "]" // 設定ID.
					+ ", interval(old)=[" + currentConfig.getRunInterval() + "]millis" // 間隔(旧設定)
					+ ", interval(new)=[" + newConfig.getRunInterval() + "]millis" // 間隔(新設定)
					+ ", reference-time(old)=[" + currentConfig.getReferenceTime() + "](24-hour notation)" // 基準時刻(旧設定)
					+ ", reference-time(new)=[" + newConfig.getReferenceTime() + "](24-hour notation)" // 基準時刻(新設定)
					+ ", load-distribution-range(old)=[" + currentConfig.getLoadDistributionRange() + "]min" // 負荷分散範囲(旧設定)
					+ ", load-distribution-range(new)=[" + newConfig.getLoadDistributionRange() + "]min" // 負荷分散範囲(新設定)
					+ ")");
			shutdown();
			start();
		}
	}

	/**
	 * string to identify Collector
	 */
	@Override
	public CollectorId getCollectorId() {
		String key = config.getFacilityId() + config.getSettingId();
		return new CollectorId(_collectorType, key);
	}

	@Override
	public void shutdown() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.info(methodName + DELIMITER + "shutdown nodeconfig acquirere. (" + this + ")");
		// 受付済・実行中のタスクも強制終了する.
		// ※_scheduler.shutdown()だと受付済・実行中のタスクは処理続行となる.
		if (_scheduler != null) {
			this.interruptThreadName = threadName;
			_scheduler.shutdownNow();
			log.info(methodName + DELIMITER + "shutdown the thread of nodeconfig acquirere. (" + threadName + ")");
		}

		// 即時実行の場合はManager側のステータスを変更する.
		if (Function.RUN_COLLECT == this.function) {
			try {
				AgentNodeConfigEndPointWrapper.stopNodeConfigRunCollect();
			} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception e) {
				log.warn(methodName + DELIMITER + "failed to stop running.", e);
			} catch (FacilityNotFound_Exception e) {
				log.warn(methodName + DELIMITER + "failed to stop running. " + e.getMessage());
			}
		}

	}

	@Override
	public String toString() {
		return "NodeConfigCollector [config = " + config.getSettingId() + "]";
	}

	// --メインメソッド郡.
	/**
	 * process called by scheduler thread
	 */
	@Override
	public void run() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// ■取得日時を設定(Managerで出力日時・登録日時となる)
		Date aquireDateObj = HinemosTime.getDateInstance();
		long aquireDateMillis = aquireDateObj.getTime();
		String aquireDateStr = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", aquireDateObj); // 取得日時.
		log.debug(methodName + DELIMITER + "set date to aquire." // 取得日時を設定.
				+ " settingID=[" + this.config.getSettingId() + "]" // 設定ID.
				+ ", acquire=[" + aquireDateStr + "]"); // 日付.
		if (!runOnce) {
			long fromStarted = aquireDateMillis - this.startedMillis;
			long surplus = fromStarted % this.config.getRunInterval();
			aquireDateMillis = aquireDateMillis - surplus;
			aquireDateObj = new Date(aquireDateMillis);
			aquireDateStr = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", aquireDateObj);
			log.debug(methodName + DELIMITER + "adjusted date to acquire according to interval on setting." // 設定間隔にあわせて取得日時を調整.
					+ " settingID=[" + this.config.getSettingId() + "]" // 設定ID.
					+ ", acquire(adjusted)=[" + aquireDateStr + "]" // 調整済の取得日時.
					+ ", fromStarted=" + fromStarted + "[msec]" // 開始時刻からのミリ秒.
					+ ", interval=" + this.config.getRunInterval() + "[msec]" // 取得間隔.
					+ ", surplus=" + surplus + "[msec]"); // 半端なミリ秒.
		}

		// ■カレンダーチェック(即時実行は関係なく実行).
		if (!this.runOnce && config.getCalendar() != null && !CalendarWSUtil.isRun(config.getCalendar())) {
			// do nothing because now is not allowed
			if (log.isDebugEnabled()) {
				log.debug(methodName + DELIMITER + "command execution is skipped because of calendar. ["
						+ config.getSettingId() + ", " + config.getCalendar() + "]");
			}
			return;
		}

		// ■初期化
		NodeInfo nInfo = new NodeInfo();
		nInfo.setFacilityId(config.getFacilityId());
		nInfo.setNodeConfigSettingId(config.getSettingId());
		nInfo = initNodeInfo(nInfo);
		// ■構成情報取得
		nInfo = getRecord(nInfo, false);

		if (log.isDebugEnabled()) {
			log.debug("call() nInfo.getNodeProcessInfo().size()          : " + nInfo.getNodeProcessInfo().size());
			log.debug("call() nInfo.getNodePackageInfo().size()          : " + nInfo.getNodePackageInfo().size());
			log.debug("call() nInfo.getNodeNetworkInterfaceInfo().size() : "
					+ nInfo.getNodeNetworkInterfaceInfo().size());
		}

		// ■処理中断チェック
		if (this.interruptThreadName != null) {
			if (this.interruptThreadName.equals(this.threadName)) {
				// 中断された場合は送信せず、InternalError通知のみ.
				log.info(methodName + DELIMITER + "interrupted to aquire because the setting was changed or the like."//
						+ " settingID=[" + this.config.getSettingId() + "]" // 設定ID.
						+ ", thread=[" + this.interruptThreadName + "]" // 中断対象のスレッド名.
						+ ", aquire date=[" + aquireDateStr + "]"); // 実行時刻.
				String message = MessageConstant.MESSAGE_NODE_CONFIG_INTERRUPTED.getMessage();
				String[] args = new String[] { this.config.getSettingId(), aquireDateStr };
				String originMsg = MessageConstant.MESSAGE_NODE_CONFIG_INTERRUPTED_DETAIL.getMessage(args);
				sendMessage(PriorityConstant.TYPE_INFO, message, originMsg, this.config.getSettingId());
				interruptThreadName = null;
				return;
			}
			log.debug(methodName + DELIMITER + "skip to interrupt aquireing."//
					+ " settingID=[" + this.config.getSettingId() + "]" // 設定ID.
					+ ", thread(now)=[" + this.threadName + "]" // 実行中のスレッド名.
					+ ", thread(interrupt)=[" + this.interruptThreadName + "]" // 中断対象のスレッド名.
					+ ", aquire date=[" + aquireDateStr + "]"); // 実行時刻.
			interruptThreadName = null;
		}

		// ■マネージャに送る
		nInfo.setNodeConfigAcquireOnce(Boolean.valueOf(this.runOnce));
		NodeConfigResult result = new NodeConfigResult(aquireDateMillis, nInfo);
		NodeConfigResultForwarder.getInstance().add(result);

		// logにだして確認
		if (log.isDebugEnabled()) {
			log.debug("call() completed.");
		}
		return;
	}

	/**
	 * 引数から対象を判別し、recordを取得する。
	 * 
	 * @param target_int
	 * @param isAutoRegist ノード自動登録フラグ
	 * @return
	 */
	private NodeInfo getRecord(NodeInfo nInfo, boolean isAutoRegist) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		long start = HinemosTime.currentTimeMillis();
		log.info("run getRecord() : " + config.getSettingId());

		// 実行対象のスクリプト設定.
		List<String> executeDoubleScript = new ArrayList<String>();
		for (Entry<String, Boolean> collectFlgEntry : this.collectFlgMap.entrySet()) {
			String collectTarget = collectFlgEntry.getKey();
			if (!collectFlgEntry.getValue().booleanValue()) {
				log.debug(methodName + DELIMITER
						+ String.format("this key is not target to collect. key=[%s]", collectTarget));
				continue;
			}

			// 収集対象と紐づく実行対象のスクリプトファイルをセット.
			if (NodeConfigSettingItem.PROCESS.name().equals(collectTarget)) {
				executeDoubleScript.add(NodeConfigConstant.SCRIPT_NAME_PROCESS);
			} else if (NodeConfigSettingItem.PACKAGE.name().equals(collectTarget)) {
				executeDoubleScript.add(NodeConfigConstant.SCRIPT_NAME_PACKAGE);
			} else if (NodeConfigSettingItem.HW_NIC.name().equals(collectTarget)) {
				executeDoubleScript.add(NodeConfigConstant.SCRIPT_NAME_HW_NIC);
			} else if (NodeConfigSettingItem.HW_CPU.name().equals(collectTarget)) {
				executeDoubleScript.add(NodeConfigConstant.SCRIPT_NAME_HW_CPU);
			} else if (NodeConfigSettingItem.HW_DISK.name().equals(collectTarget)) {
				executeDoubleScript.add(NodeConfigConstant.SCRIPT_NAME_HW_DISK);
			} else if (NodeConfigSettingItem.HW_FILESYSTEM.name().equals(collectTarget)) {
				executeDoubleScript.add(NodeConfigConstant.SCRIPT_NAME_HW_FSYSTEM);
			} else if (NodeConfigSettingItem.NETSTAT.name().equals(collectTarget)) {
				executeDoubleScript.add(NodeConfigConstant.SCRIPT_NAME_NETSTAT);
			} else if (NodeConfigSettingItem.OS.name().equals(collectTarget)
					|| NodeConfigSettingItem.HOSTNAME.name().equals(collectTarget)
					|| NodeConfigSettingItem.HW_MEMORY.name().equals(collectTarget)) {
				executeDoubleScript.add(NodeConfigConstant.SCRIPT_NAME_OTHER);
			} else if (NodeConfigSettingItem.CUSTOM.name().equals(collectTarget)) {
				log.trace(methodName + DELIMITER
						+ String.format("skipped to add name of script file. target=[%s]", collectTarget));
				continue;
			} else {
				log.warn(methodName + DELIMITER
						+ String.format("failed to add name of script file. target=[%s]", collectTarget));
			}

		}
		// 重複を取り除く.
		List<String> executeScript = new ArrayList<String>(new HashSet<>(executeDoubleScript));

		// スクリプトファイル単位でコマンド実行・TSVファイル読み込み.
		this.initializeWinFlg();
		String filePrefix = getConfig().getSettingId() + "_" + getConfig().getFacilityId() + "_";
		if (this.runOnce) {
			filePrefix = "RUN_" + filePrefix;
		}
		for (String scriptName : executeScript) {
			String scriptFileName = "";
			if (this.winFlg) {
				scriptFileName = scriptName + NodeConfigConstant.SCRIPT_EXTENSION_WIN;
			} else {
				scriptFileName = scriptName + NodeConfigConstant.SCRIPT_EXTENSION_LINUX;
			}
			//Agentが対応するNodeInfoの中から、今回実行対象となっているNodeInfoを抽出する
			NodeInfo nodeInfo=null;
			//ノード自動登録フラグ
			if (nodeInfoList != null){
				for(int i=0;i<nodeInfoList.size();i++){
					if(nodeInfoList.get(i).getFacilityId().equals(config.getFacilityId())){
						nodeInfo =nodeInfoList.get(i);
						break;
					}
				}
			}else{
				log.debug("getRecord(): nodeInfoList is null");
			}

			// コマンド引数を追加.
			String tsvFileName = filePrefix + scriptName + NodeConfigConstant.TSV_EXTENSION;
			String args = " " + NodeConfigConstant.OPTION_FILENAME + " " + tsvFileName;
			if (NodeConfigConstant.SCRIPT_NAME_OTHER.equals(scriptName)) {
				// 実行対象のオプション.
				if (this.collectFlgMap.get(NodeConfigSettingItem.OS.name()).booleanValue()) {
					args = args + " " + NodeConfigConstant.EXEC_OPTION_OS;
				}
				if (this.collectFlgMap.get(NodeConfigSettingItem.HOSTNAME.name()).booleanValue()) {
					args = args + " " + NodeConfigConstant.EXEC_OPTION_HOST;
				}
				if (this.collectFlgMap.get(NodeConfigSettingItem.HW_MEMORY.name()).booleanValue()) {
					args = args + " " + NodeConfigConstant.EXEC_OPTION_MEMORY;
				}
			//ノードのSNMP情報を引数として設定する
			} else if(NodeConfigConstant.SCRIPT_NAME_HW_CPU.equals(scriptName)||NodeConfigConstant.SCRIPT_NAME_HW_DISK.equals(scriptName)||
					NodeConfigConstant.SCRIPT_NAME_HW_FSYSTEM.equals(scriptName)||NodeConfigConstant.SCRIPT_NAME_HW_NIC.equals(scriptName)){
				log.debug("getRecord():SNMP args has generated for specific scripts");
				args=args + getSnmpArgs(nodeInfo, isAutoRegist);
			}else
			{
				log.debug(methodName + DELIMITER
						+ String.format("no particular arguments. scriptFileName=[%s]", scriptFileName));
			}
			
			log.debug("getRecord(): Script argument is:  "+args);
			// コマンド実行.
			boolean toRetry = this.executeCommand(args, nInfo, scriptFileName, tsvFileName, true);
			// 特定のエラーコードに対して条件変更してコマンドリトライさせる.
			if (toRetry) {
				this.executeCommand(args, nInfo, scriptFileName, tsvFileName, false);
			}
		}

		// ユーザ任意コマンド実行.
		if (nInfo != null) {
			List<NodeCustomInfo> customResultList = this.executeCustomCommand();
			if(customResultList != null){
				nInfo.getNodeCustomInfo().addAll(customResultList);
			}else{
				log.debug("getRecord(): No Custom Result returned. Skip forwarding custom result");
			}
		}

		long end = HinemosTime.currentTimeMillis();
		log.info("getRecord () : done. " + (end - start) + "ms.");
		return nInfo;
	}
	/**
	 * NodeInfoからSNMP情報を抽出し、スクリプト引数の文字列を返す
	 * 
	 * @param NodeInfo nodeInfo
	 * @param int version
	 * @return スクリプト引数
	 */
	private String getSnmpArgs(NodeInfo nodeInfo, boolean isAutoRegist){
		String args="";
		//nullチェック
		//ノード自動登録の場合もnullになる
		if (nodeInfo == null){
			//Autoregist
			if(isAutoRegist){
				log.info("getSnmpArgs(): Called by Auto Regist");
				return " -z auto";
			}else{
				log.warn("getSnmpArgs(): NodeInfo is empty");
				return args;
			}
		}
		
		int version = nodeInfo.getSnmpVersion();
		
		if(this.winFlg){
			log.debug("getSnmpArgs(): Node platform: WINDOWS");
		}else{
			log.debug("getSnmpArgs(): Node platform: LINUX");
		}
		
		//winFlgを使用し、作成する引数をプラットフォームごとに変更
		if(this.winFlg){
			//windowsの場合は、コミュニティ名のみ指定する
			if(!nodeInfo.getSnmpCommunity().equals("")){
				args=args+" "+NodeConfigConstant.SNMP_COMMUNITY+" "+nodeInfo.getSnmpCommunity();
			}else{
				args=args+" "+NodeConfigConstant.SNMP_COMMUNITY+" "+"public";
				log.warn("getSnmpArgs(): No SNMP community defind. Use default value");
			}
		}else{
			if(version==3){
				//ver 3
				args=args+" "+NodeConfigConstant.SNMP_VERSION+" "+nodeInfo.getSnmpVersion();
				//port
				if(nodeInfo.getSnmpPort()!=null){
					args=args+" "+NodeConfigConstant.SNMP_PORT+" "+nodeInfo.getSnmpPort();
				}else{
					args=args+" "+NodeConfigConstant.SNMP_PORT+" "+"161";
					log.warn("getSnmpArgs(): No SNMP port defind. Use default value");
				}
				//user
				if(nodeInfo.getSnmpUser() != null&&!nodeInfo.getSnmpUser().equals("")){
					args=args+" "+NodeConfigConstant.SNMP_USER+" "+nodeInfo.getSnmpUser();
				}else{
					args=args+" "+NodeConfigConstant.SNMP_USER+" "+"root";
					log.warn("getSnmpArgs(): No SNMP user defind. Use default value");
				}
				//security level
				if(nodeInfo.getSnmpSecurityLevel() != null&&!nodeInfo.getSnmpSecurityLevel().equals("")){
					//snmpwalkのオプションにあわせる
					if(nodeInfo.getSnmpSecurityLevel().equals("noauth_nopriv")){
						args=args+" "+NodeConfigConstant.SNMP_LEVEL+" "+"noAuthNoPriv";
					}else if(nodeInfo.getSnmpSecurityLevel().equals("auth_nopriv")){
						args=args+" "+NodeConfigConstant.SNMP_LEVEL+" "+"authNoPriv";
					}else{
						args=args+" "+NodeConfigConstant.SNMP_LEVEL+" "+"authPriv";
					}
				}
				//auth password
				if(nodeInfo.getSnmpAuthPassword() != null&&!nodeInfo.getSnmpAuthPassword().equals("")){
					args=args+" "+NodeConfigConstant.SNMP_AUTH_PASSPHRASE+" "+nodeInfo.getSnmpAuthPassword();
				}
				//auth protocol
				if(nodeInfo.getSnmpAuthProtocol() != null&&!nodeInfo.getSnmpAuthProtocol().equals("")){
					args=args+" "+NodeConfigConstant.SNMP_AUTH_PROTOCOL+" "+nodeInfo.getSnmpAuthProtocol();
				}
				//priv protocol
				if(nodeInfo.getSnmpPrivProtocol() != null&&!nodeInfo.getSnmpPrivProtocol().equals("")){
					args=args+" "+NodeConfigConstant.SNMP_PRIV_PROTOCOL+" "+nodeInfo.getSnmpPrivProtocol();
				}
				//priv password
				if(nodeInfo.getSnmpPrivPassword() != null&&!nodeInfo.getSnmpPrivPassword().equals("")){
					args=args+" "+NodeConfigConstant.SNMP_PRIV_PASSPHRASE+" "+nodeInfo.getSnmpPrivPassword();
				}
			}else{
				//version 3以外は、2cに統一する
				args=args+" "+NodeConfigConstant.SNMP_VERSION+" "+"2c";
				//port
				if(nodeInfo.getSnmpPort()!=null){
					args=args+" "+NodeConfigConstant.SNMP_PORT+" "+nodeInfo.getSnmpPort();
				}else{
					args=args+" "+NodeConfigConstant.SNMP_PORT+" "+"161";
					log.warn("getSnmpArgs(): No SNMP port defind. Use default value");
				}
				//community
				if(nodeInfo.getSnmpUser() != null&&!nodeInfo.getSnmpCommunity().equals("")){
					args=args+" "+NodeConfigConstant.SNMP_COMMUNITY+" "+nodeInfo.getSnmpCommunity();
				}else{
					args=args+" "+NodeConfigConstant.SNMP_COMMUNITY+" "+"public";
					log.warn("getSnmpArgs(): No SNMP community defind. Use default value");
				}
			}
		}
		//version,platform共通
		if(nodeInfo.getSnmpTimeout()!=null){
			//snmpwalkのタイムアウト指定は秒のため、変換
			long t_sec = TimeUnit.MILLISECONDS.toSeconds(nodeInfo.getSnmpTimeout());
			args=args+" "+NodeConfigConstant.SNMP_TIMEOUT+" "+t_sec;
		}
		if(nodeInfo.getSnmpRetryCount()!=null){
			args=args+" "+NodeConfigConstant.SNMP_RETRY+" "+nodeInfo.getSnmpRetryCount();
		}
		
		log.debug("getSnmpArgs(): SNMP args construction succeeded");
		return args;
	}

	/**
	 * コマンドを生成して実行する(スクリプト単位).
	 * 
	 * @param args
	 *            コマンド引数.
	 * @param nInfo
	 *            実行結果を格納するノード情報オブジェクト.
	 * @param scriptName
	 *            実行対象のスクリプト名(パス無し・拡張子付き).
	 * @param fName
	 *            コマンド実行後に生成するTSVファイル名(パス無し・拡張子付き).
	 * @param firstTry
	 *            初回実行フラグ(true:初回実行、false:リトライ).
	 * @retrun リトライフラグ(true:要リトライ、false:リトライ対象外)
	 */
	private boolean executeCommand(String args, NodeInfo nInfo, String scriptName, String fName, boolean firstTry) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		// コマンドを設定
		String commandBinded = commonPath + scriptName + args;
		if (this.winFlg) {
			String psPath = "powershell";
			// リトライの場合は32bit版のPowerShellで動かしてみる.
			if (!firstTry) {

				List<File> roots = Arrays.asList(File.listRoots());
				File rootDrive = null;
				for (File root : roots) {
					if ("C:".equals(root.getAbsolutePath())) {
						rootDrive = root;
						break;
					}
				}
				File psExeFile = null;
				if (rootDrive != null) {
					psExeFile = new File(rootDrive, "Windows");
					psExeFile = new File(psExeFile, "SysWOW64");
					psExeFile = new File(psExeFile, "WindowsPowerShell");
					psExeFile = new File(psExeFile, "v1.0");
					psExeFile = new File(psExeFile, "powershell.exe");
				}

				String psPathDefault = "C:\\Windows\\SysWOW64\\WindowsPowerShell\\v1.0\\powershell.exe";
				// 32bitOSの場合は上記パスはないので普通のコマンド.
				if (psExeFile == null || !psExeFile.exists()) {
					psPathDefault = "powershell";
				}
				String psPathKey = "repository.cmdb.command.path";
				psPath = AgentProperties.getProperty(psPathKey, psPathDefault);
			}

			commandBinded = psPath + " -inputformat none \"& '" + commonPath + scriptName + "'" + args + "\"";
		}

		// プラットフォームに対応したコマンドに変換して実行.
		CommandResult ret = null;
		try {
			// プラットフォーム判定
			CommandCreator.PlatformType platform = CommandCreator.convertPlatform(_commandMode);
			// コマンド作成：commandBindedをプラットフォームに対応したコマンドに変換
			String[] command = CommandCreator.createCommand("", commandBinded, platform, false, false);

			// コマンド実行
			CommandExecutor cmdExecutor = new CommandExecutor(command, _charset, _timeoutInterval, _bufferSize);
			cmdExecutor.execute();
			ret = cmdExecutor.getResult();
			Result scriptResult = null;
			if (ret != null && ret.exitCode.intValue() == NodeConfigConstant.EXCD_SUCCESS) {
				if (ret.stderr != null && !ret.stderr.isEmpty()) {
					log.warn(methodName + DELIMITER + //
							"succeeded to execute configuration script, but exported stderr. " + //
							"script=[" + scriptName + "]\n" + //
							"result-exitCode : " + ret.exitCode.intValue() + "\n" + //
							"result-stdout :\n" + ret.stdout + "\n" + //
							"result-stderr :\n" + ret.stderr);
				} else if (ret.stdout != null
						&& (ret.stdout.toUpperCase().contains("WARN") || ret.stdout.toUpperCase().contains("ERROR"))) {
					log.warn(methodName + DELIMITER + //
							"succeeded to execute configuration script, but occured warning or error on the script. " + //
							"script=[" + scriptName + "]\n" + //
							"result-exitCode : " + ret.exitCode.intValue() + "\n" + //
							"result-stdout :\n" + ret.stdout + "\n" + //
							"result-stderr :\n" + ret.stderr);
				} else {
					log.trace(methodName + DELIMITER + String.format("succeeded to execute [%s].", scriptName));
				}
				scriptResult = Result.SUCCESS;
			} else if (ret != null && firstTry && this.winFlg
					&& ret.exitCode.intValue() == NodeConfigConstant.EXCD_SYSTEM_WIN_AVEXCEP) {
				// Windowsでステータスコード255(System.AccessViolationException発生)は、リトライ対象.
				log.info(methodName + DELIMITER + "Configration getting script is failed, but to retry. script=["
						+ scriptName + "]\n" + //
						"result-exitCode : " + ret.exitCode.intValue() + "\n" + //
						"result-stdout :\n" + ret.stdout + "\n" + //
						"result-stderr :\n" + ret.stderr);
				return true;
			} else if (ret != null && scriptName.startsWith(NodeConfigConstant.SCRIPT_NAME_OTHER)
					&& ret.exitCode.intValue() > NodeConfigConstant.EXCD_HINEMOS_CONTINUE
					&& ret.exitCode.intValue() <= NodeConfigConstant.EXCD_HINEMOS_CONTINUE_MAX) {
				// 複数種類の情報をまとめて取得するスクリプトが部分的に失敗した状態.
				log.warn(methodName + DELIMITER + //
						"partially failed to execute configuration script. script=[" + scriptName + "]\n" + //
						"result-exitCode : " + ret.exitCode.intValue() + "\n" + //
						"result-stdout :\n" + ret.stdout + "\n" + //
						"result-stderr :\n" + ret.stderr);
				scriptResult = Result.PARTIALLY_SUCCESS;
			} else if (ret == null) {
				log.warn(methodName + DELIMITER + //
						"failed to execute configuration script. script=[" + scriptName + "]\n" + //
						"result : null");
				this.setResult(scriptName, Integer.valueOf(NodeRegisterFlagConstant.GET_FAILURE), nInfo);
				return false;
			} else {
				// スクリプト実行後のステータスコードが異常終了.
				log.warn(methodName + DELIMITER + //
						"failed to execute configuration script. script=[" + scriptName + "]\n" + //
						"result-exitCode : " + ret.exitCode.intValue() + "\n" + //
						"result-stdout :\n" + ret.stdout + "\n" + //
						"result-stderr :\n" + ret.stderr);
				this.setResult(scriptName, Integer.valueOf(NodeRegisterFlagConstant.GET_FAILURE), nInfo);
				return false;
			}

			// スクリプト実行後に生成されるTSVファイルの読み込み.
			int readResult = readFile(nInfo, fName);

			// スクリプト実行結果とTSV読込結果を元に登録フラグを設定する.
			if (readResult == NodeConfigConstant.EXCD_SUCCESS) {
				if (scriptResult == Result.SUCCESS) {
					this.setResult(scriptName, Integer.valueOf(NodeRegisterFlagConstant.GET_SUCCESS), nInfo);
				} else if (scriptResult == Result.PARTIALLY_SUCCESS) {
					this.setResultPartially(scriptName, nInfo, ret.exitCode.intValue(), readResult);
				} else {
					this.setResult(scriptName, Integer.valueOf(NodeRegisterFlagConstant.GET_FAILURE), nInfo);
					log.warn(methodName + DELIMITER + String.format("failed to execute [%s].", scriptName));
				}
			} else if (readResult >= NodeConfigConstant.FAILED_OS && // 読み込み失敗最小値.
					readResult <= (NodeConfigConstant.FAILED_OS + NodeConfigConstant.FAILED_HOST
							+ NodeConfigConstant.FAILED_MEMORY)) {
				if (scriptResult == Result.SUCCESS) {
					this.setResultPartially(scriptName, nInfo, ret.exitCode.intValue(), readResult);
				} else if (scriptResult == Result.PARTIALLY_SUCCESS) {
					this.setResultPartially(scriptName, nInfo, ret.exitCode.intValue(), readResult);
				} else {
					this.setResult(scriptName, Integer.valueOf(NodeRegisterFlagConstant.GET_FAILURE), nInfo);
					log.warn(methodName + DELIMITER + String.format("failed to execute [%s].", scriptName));
				}
			} else {
				this.setResult(scriptName, Integer.valueOf(NodeRegisterFlagConstant.GET_FAILURE), nInfo);
				log.warn(methodName + DELIMITER + String.format("failed to read executef [%s].", scriptName));
			}
		} catch (HinemosUnknown e) {
			// コマンド作成時・スクリプト実行時・TSVファイル読み込み時の予期せぬエラー.
			// throw元でログ出力してるのでここではしない、次のスクリプトを実行するためcatchして処理続行.
			this.setResult(scriptName, Integer.valueOf(NodeRegisterFlagConstant.GET_FAILURE), nInfo);
			return false;
		}

		return false;
	}

	/**
	 * スクリプトを判定して実行結果を適切なフィールドにセット.
	 * 
	 * @param scriptName
	 *            実行対象のスクリプト名(拡張子込).
	 * @param result
	 *            スクリプト実行結果.
	 * @param nInfo
	 *            Manager返却用オブジェクト.
	 */
	private void setResult(String scriptName, Integer result, NodeInfo nInfo) {

		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		int extension = scriptName.indexOf(".");
		String name = scriptName.substring(0, extension);

		/// スクリプトの種別毎に実行結果をセット.
		switch (name) {
		case NodeConfigConstant.SCRIPT_NAME_PROCESS:
			nInfo.setNodeProcessRegisterFlag(result);
			return;
		case NodeConfigConstant.SCRIPT_NAME_PACKAGE:
			nInfo.setNodePackageRegisterFlag(result);
			return;
		case NodeConfigConstant.SCRIPT_NAME_HW_NIC:
			nInfo.setNodeNetworkInterfaceRegisterFlag(result);
			return;
		case NodeConfigConstant.SCRIPT_NAME_HW_CPU:
			nInfo.setNodeCpuRegisterFlag(result);
			return;
		case NodeConfigConstant.SCRIPT_NAME_HW_DISK:
			nInfo.setNodeDiskRegisterFlag(result);
			return;
		case NodeConfigConstant.SCRIPT_NAME_HW_FSYSTEM:
			nInfo.setNodeFilesystemRegisterFlag(result);
			return;
		case NodeConfigConstant.SCRIPT_NAME_NETSTAT:
			nInfo.setNodeNetstatRegisterFlag(result);
			return;

		// 単行データ同時実行用スクリプト：実行対象に一律同じ結果をセットする.
		case NodeConfigConstant.SCRIPT_NAME_OTHER:
			if (this.collectFlgMap.get(NodeConfigSettingItem.OS.name()).booleanValue()) {
				nInfo.setNodeOsRegisterFlag(result);
			}
			if (this.collectFlgMap.get(NodeConfigSettingItem.HOSTNAME.name()).booleanValue()) {
				nInfo.setNodeHostnameRegisterFlag(result);
			}
			if (this.collectFlgMap.get(NodeConfigSettingItem.HW_MEMORY.name()).booleanValue()) {
				nInfo.setNodeMemoryRegisterFlag(result);
			}
			return;

		default:
			log.warn(methodName + DELIMITER + "no flag of executing script.");
			return;
		}
	}

	/**
	 * スクリプトが部分的に成功/失敗した場合にスクリプトの実行結果を判定して適切なフィールドにセットする.
	 * 
	 * @param scriptName
	 *            実行対象のスクリプト名(拡張子込).
	 * @param nInfo
	 *            Manager返却用オブジェクト.
	 * @param exitCode
	 *            スクリプトの実行結果.
	 * @param readResult
	 *            TSVファイルの読み込み結果.
	 */
	private void setResultPartially(String scriptName, NodeInfo nInfo, int exitCode, int readResult) {

		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(
				methodName + DELIMITER + String.format("start. exitCode=[%d], readResult=[%d]", exitCode, readResult));

		// 対象外のスクリプトの失敗コードなので一律失敗として設定.
		if (!scriptName.startsWith(NodeConfigConstant.SCRIPT_NAME_OTHER)) {
			log.warn(methodName + DELIMITER + String.format("failed to exit [%s].", scriptName));
			this.setResult(scriptName, Integer.valueOf(NodeRegisterFlagConstant.GET_FAILURE), nInfo);
			return;
		}

		// 失敗対象を算出する.
		int mergedResult = 0;
		if (exitCode == NodeConfigConstant.EXCD_SUCCESS) {
			// TSV読込みのみ失敗.
			mergedResult = readResult;
		} else {
			int scriptResult = exitCode - NodeConfigConstant.EXCD_HINEMOS_CONTINUE;
			if (readResult == NodeConfigConstant.EXCD_SUCCESS) {
				// スクリプト実行のみ失敗.
				mergedResult = scriptResult;
			} else {
				// 両方何かしらが失敗してるので算出.
				int allFailure = NodeConfigConstant.EXCD_HINEMOS_CONTINUE_MAX
						- NodeConfigConstant.EXCD_HINEMOS_CONTINUE;

				// スクリプトの実行結果とTSVファイルの読み込みの失敗対象を統合する.
				if (scriptResult == readResult) {
					// まったく同じ組み合わせ.
					mergedResult = scriptResult;
				} else if (scriptResult == allFailure || readResult == allFailure
						|| scriptResult + readResult == allFailure) {
					// どちらかが全失敗、もしくは組み合わせて全失敗.
					mergedResult = allFailure;
				} else if (scriptResult > NodeConfigConstant.FAILED_MEMORY
						|| readResult > NodeConfigConstant.FAILED_MEMORY) {
					if (scriptResult <= NodeConfigConstant.FAILED_MEMORY
							|| readResult <= NodeConfigConstant.FAILED_MEMORY) {
						// 2つと1つの組み合わせ、必ず1つは重複しているので大きい方をセット.
						if (scriptResult > readResult) {
							mergedResult = scriptResult;
						} else {
							mergedResult = readResult;
						}
					} else {
						// 2つと2つの組み合わせ、どういう組み合わせでも全失敗.
						mergedResult = allFailure;
					}
				} else {
					// どちらも1つの組み合わせで異なるので単純に足す.
					mergedResult = scriptResult + readResult;
				}
			}
		}

		log.debug(methodName + DELIMITER + String.format("calculated. mergedResult=[%d].", mergedResult));
		// 統合したコードから失敗対象を判定する.
		List<String> failureTargets = new LinkedList<String>();
		List<String> successTargets = new LinkedList<String>();
		switch (mergedResult) {

		// 失敗対象がOSのみの場合.
		case NodeConfigConstant.FAILED_OS:
			failureTargets.add(NodeConfigSettingItem.OS.name());
			successTargets.add(NodeConfigSettingItem.HW_MEMORY.name());
			successTargets.add(NodeConfigSettingItem.HOSTNAME.name());
			break;

		// 失敗対象がホストのみの場合.
		case NodeConfigConstant.FAILED_HOST:
			successTargets.add(NodeConfigSettingItem.OS.name());
			failureTargets.add(NodeConfigSettingItem.HOSTNAME.name());
			successTargets.add(NodeConfigSettingItem.HW_MEMORY.name());
			break;

		// 失敗対象がメモリのみの場合.
		case NodeConfigConstant.FAILED_MEMORY:
			successTargets.add(NodeConfigSettingItem.OS.name());
			successTargets.add(NodeConfigSettingItem.HOSTNAME.name());
			failureTargets.add(NodeConfigSettingItem.HW_MEMORY.name());
			break;

		// 失敗対象がOSとホストの場合.
		case NodeConfigConstant.FAILED_OS + NodeConfigConstant.FAILED_HOST:
			failureTargets.add(NodeConfigSettingItem.OS.name());
			failureTargets.add(NodeConfigSettingItem.HOSTNAME.name());
			successTargets.add(NodeConfigSettingItem.HW_MEMORY.name());
			break;

		// 失敗対象がOSとメモリの場合.
		case NodeConfigConstant.FAILED_OS + NodeConfigConstant.FAILED_MEMORY:
			failureTargets.add(NodeConfigSettingItem.OS.name());
			successTargets.add(NodeConfigSettingItem.HOSTNAME.name());
			failureTargets.add(NodeConfigSettingItem.HW_MEMORY.name());
			break;

		// 失敗対象がホストとメモリの場合.
		case NodeConfigConstant.FAILED_HOST + NodeConfigConstant.FAILED_MEMORY:
			successTargets.add(NodeConfigSettingItem.OS.name());
			failureTargets.add(NodeConfigSettingItem.HOSTNAME.name());
			failureTargets.add(NodeConfigSettingItem.HW_MEMORY.name());
			break;

		// 全て失敗の場合.
		case NodeConfigConstant.FAILED_OS + NodeConfigConstant.FAILED_HOST + NodeConfigConstant.FAILED_MEMORY:
			failureTargets.add(NodeConfigSettingItem.OS.name());
			failureTargets.add(NodeConfigSettingItem.HOSTNAME.name());
			failureTargets.add(NodeConfigSettingItem.HW_MEMORY.name());
			break;

		// 失敗コードが定義されていない場合.
		default:
			// 全て失敗としてセットする.
			log.warn(methodName + DELIMITER
					+ String.format("[%d] is undefined as exit code of failure partially.", mergedResult));
			this.setResult(scriptName, Integer.valueOf(NodeRegisterFlagConstant.GET_FAILURE), nInfo);
			return;
		}

		// 収集対象外なのにスクリプト実行されてるかチェック.
		if (this.isntCollectTarget(failureTargets, nInfo)) {
			return;
		}

		// ステータスセット.
		this.setRegisterFlags(failureTargets, NodeRegisterFlagConstant.GET_FAILURE, nInfo);
		this.setRegisterFlags(successTargets, NodeRegisterFlagConstant.GET_SUCCESS, nInfo);
		return;
	}

	/**
	 * 
	 * 収集対象外なのかどうかをチェックする
	 * 
	 * @param checkTargets
	 *            チェック対象 .
	 * @param nInfo
	 *            Manager返却用オブジェクト.
	 * 
	 * @return チェック結果、true:収集対象外(一律失敗として登録)、 false:収集対象
	 */
	private boolean isntCollectTarget(List<String> checkTargets, NodeInfo nInfo) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		if (checkTargets == null || checkTargets.isEmpty()) {
			log.warn(methodName + DELIMITER
					+ "failed to check which is target to collect, because 'checkTargets' are empty.");
			return false;
		}

		List<String> allFailuer = new LinkedList<String>();
		allFailuer.add(NodeConfigSettingItem.OS.name());
		allFailuer.add(NodeConfigSettingItem.HOSTNAME.name());
		allFailuer.add(NodeConfigSettingItem.HW_MEMORY.name());

		StringBuilder logSb = new StringBuilder();
		boolean isFirst = true;
		for (String target : checkTargets) {

			if (!this.collectFlgMap.get(target).booleanValue()) {
				log.warn(methodName + DELIMITER
						+ String.format("failed to exit [%s] excepted from collecting.", target));
				this.setRegisterFlags(allFailuer, NodeRegisterFlagConstant.GET_FAILURE, nInfo);
				return true;
			}

			if (!isFirst) {
				logSb.append("], [");
			} else {
				logSb.append("[");
			}
			logSb.append(target);
			isFirst = false;
		}

		log.debug(methodName + DELIMITER
				+ String.format("succeeded to check which is target to collect. targets=%s", logSb.toString()));
		return false;
	}

	/**
	 * 
	 * Agent収集結果をまとめて登録(複数実行スクリプト用).
	 * 
	 * @param targets
	 *            登録対象.
	 * @param nInfo
	 *            Manager返却用オブジェクト.
	 * @param exitCode
	 *            スクリプトの実行結果.
	 */
	private void setRegisterFlags(List<String> targets, int registerStatus, NodeInfo nInfo) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		if (targets == null || targets.isEmpty()) {
			log.warn(methodName + DELIMITER + "failed to set register-flag, because 'targets' are empty.");
			return;
		}

		StringBuilder logSb = new StringBuilder();
		boolean isFirst = true;
		for (String target : targets) {
			if (!this.collectFlgMap.get(target).booleanValue()) {
				log.trace(methodName + DELIMITER + String.format("skipped to set register-flag. flag=[%s]", target));
				continue;
			}

			if (NodeConfigSettingItem.OS.name().equals(target)) {
				nInfo.setNodeOsRegisterFlag(Integer.valueOf(registerStatus));
			} else if (NodeConfigSettingItem.HOSTNAME.name().equals(target)) {
				nInfo.setNodeHostnameRegisterFlag(Integer.valueOf(registerStatus));
			} else if (NodeConfigSettingItem.HW_MEMORY.name().equals(target)) {
				nInfo.setNodeMemoryRegisterFlag(Integer.valueOf(registerStatus));
			} else {
				log.warn(methodName + DELIMITER + String.format("failed to set register-flag. flag=[%s]", target));
				continue;
			}

			if (!isFirst) {
				logSb.append("], [");
			} else {
				logSb.append("[");
			}
			logSb.append(target);
			isFirst = false;
		}
		if (!isFirst) {
			log.debug(methodName + DELIMITER
					+ String.format("succeeded to set register-flags. flags=%s", logSb.toString()));
		}
	}

	/**
	 * スクリプト、TSVファイルの共通パスの設定、OSチェック
	 * 
	 * @return
	 */
	private void initializeWinFlg() {
		// ■スクリプト、TSVファイルの共通パスの設定

		// Agent_Homeのパス取得
		String agent_home = Agent.getAgentHome();
		String fileDirName = "lib";

		// OSチェック
		this.winFlg = false;
		String osName = System.getProperty("os.name");

		String os = "LINUX";
		if (osName != null && osName.startsWith("Windows")) {
			this.winFlg = true;
			os = "WINDOWS";
		}
		commonPath = agent_home + fileDirName + File.separator + os + File.separator + "script" + File.separator;

		if (log.isDebugEnabled()) {
			log.debug("getPreparation() windows os flg : " + this.winFlg);
			log.debug("getPreparation() agent_home : " + agent_home);
			log.debug("getPreparation() commonPath : " + commonPath);
		}
	}

	/**
	 * TSVファイルの読み込み
	 * 
	 * @param nInfo
	 *            Manager返却用ノード情報オブジェクト.
	 * @param fName
	 * @param winChk
	 *            true:windows, false:windows以外
	 * @return CMDBRecord
	 * @throws HinemosUnknown
	 */
	private int readFile(NodeInfo nInfo, String fName) throws HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		int readResult = NodeConfigConstant.EXCD_SUCCESS;
		try {
			// ファイルの読み込み
			String tsvCharset = AgentProperties.getProperty("repository.cmdb.tsv.charset", "UTF-8");
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new FileInputStream(commonPath + fName), tsvCharset));

			// 読み込んだファイルを処理する
			String line;
			String[] token;

			List<NodeProcessInfo> proList = nInfo.getNodeProcessInfo(); // プロセス
			HashMap<String, NodePackageInfo> pkgMap = new HashMap<>(); // パッケージ一時格納用
			List<NodePackageInfo> pkgList = nInfo.getNodePackageInfo(); // パッケージ
			List<NodeNetworkInterfaceInfo> nicList = nInfo.getNodeNetworkInterfaceInfo(); // HW-NIC
			List<NodeCpuInfo> cpuList = nInfo.getNodeCpuInfo(); // HW-CPU
			List<NodeDiskInfo> diskList = nInfo.getNodeDiskInfo(); // HW-Disk
			List<NodeFilesystemInfo> fsystemList = nInfo.getNodeFilesystemInfo(); // HW-ファイルシステム
			List<NodeNetstatInfo> netstatList = nInfo.getNodeNetstatInfo(); // ネットワーク接続情報
			List<NodeHostnameInfo> hostList = nInfo.getNodeHostnameInfo(); // HW-ホスト
			List<NodeMemoryInfo> memoryList = nInfo.getNodeMemoryInfo(); // HW-メモリ

			NodeConfigTsvReplaser replacer = new NodeConfigTsvReplaser(this.winFlg);
			boolean isFirst = true;

			// TSVファイルを1行ずつ読込んで処理する.
			while ((line = br.readLine()) != null) {
				log.trace(methodName + DELIMITER + String.format("to read this line. line=[%s]", line));
				// 区切り文字"\t(タブ)"で分割する
				token = line.split("\t",-1);
				String head = token[0];

				// プロセス情報.
				if (this.collectFlgMap.get(NodeConfigSettingItem.PROCESS.name()).booleanValue() //
						&& token.length > 0 && NodeConfigConstant.LINE_HEADER_PROCESS.equals(head)) {
					proList.add(replacer.setRecordProcess(token));
					log.trace("readFile() : succeeded to add proList.");
				}
				// パッケージ情報取得フラグ、TSVの１列目がpackageかの判定
				else if (this.collectFlgMap.get(NodeConfigSettingItem.PACKAGE.name()).booleanValue() //
						&& 0 < token.length && NodeConfigConstant.LINE_HEADER_PACKAGE.equals(head)) {
					NodePackageInfo pkg = null;
					pkg = replacer.setRecordPackage(token);

					String pkgId = pkg.getPackageId();
					if (pkgMap.containsKey(pkgId)) {
						NodePackageInfo nowPkg = pkgMap.get(pkgId);
						if (pkg.getInstallDate() > nowPkg.getInstallDate()) {
							pkgMap.put(pkgId, pkg);
						}
					} else {
						pkgMap.put(pkgId, pkg);
					}
					log.trace(methodName + DELIMITER + "succeeded to add pkgList.");
				}
				// HW NIC情報.
				else if (this.collectFlgMap.get(NodeConfigSettingItem.HW_NIC.name()).booleanValue() //
						&& 0 < token.length && NodeConfigConstant.LINE_HEADER_NIC.equals(head)) {
					nicList.add(replacer.setRecordNic(token));
					log.trace(methodName + DELIMITER + "succeeded to add nicList.");
				}
				// HW CPU情報.
				else if (this.collectFlgMap.get(NodeConfigSettingItem.HW_CPU.name()).booleanValue() //
						&& 0 < token.length && NodeConfigConstant.LINE_HEADER_CPU.equals(head)) {
					cpuList.add(replacer.setRecordCpu(token));
					log.trace(methodName + DELIMITER + "succeeded to add cpuList.");
				}
				// HW Disk情報.
				else if (this.collectFlgMap.get(NodeConfigSettingItem.HW_DISK.name()).booleanValue() //
						&& 0 < token.length && NodeConfigConstant.LINE_HEADER_DISK.equals(head)) {
					diskList.add(replacer.setRecordDisk(token));
					log.trace(methodName + DELIMITER + "succeeded to add diskList.");
				}
				// HW ファイルシステム情報.
				else if (this.collectFlgMap.get(NodeConfigSettingItem.HW_FILESYSTEM.name()).booleanValue() //
						&& 0 < token.length && NodeConfigConstant.LINE_HEADER_FSYSTEM.equals(head)) {
					fsystemList.add(replacer.setRecordFsystem(token));
					log.trace(methodName + DELIMITER + "succeeded to add fsystemList.");
				}
				// ネットワーク接続情報.
				else if (this.collectFlgMap.get(NodeConfigSettingItem.NETSTAT.name()).booleanValue() //
						&& 0 < token.length && NodeConfigConstant.LINE_HEADER_NETSTAT.equals(head)) {
					netstatList.add(replacer.setRecordNetstat(token));
					log.trace(methodName + DELIMITER + "succeeded to add netstatList.");
				}
				// OS情報.
				else if (this.collectFlgMap.get(NodeConfigSettingItem.OS.name()).booleanValue() //
						&& 0 < token.length && NodeConfigConstant.LINE_HEADER_OS.equals(head)) {
					try {
						nInfo.setNodeOsInfo(replacer.setRecordOs(token));
						log.trace(methodName + DELIMITER + "succeeded to set NodeOsInfo.");
					} catch (HinemosUnknown e) {
						readResult = readResult + NodeConfigConstant.FAILED_OS;
					}
				}
				// ホスト.
				else if (this.collectFlgMap.get(NodeConfigSettingItem.HOSTNAME.name()).booleanValue() //
						&& 0 < token.length && NodeConfigConstant.LINE_HEADER_HOST.equals(head)) {
					try {
						hostList.add(replacer.setRecordHost(token));
						log.trace(methodName + DELIMITER + "succeeded to add hostList.");
					} catch (HinemosUnknown e) {
						readResult = readResult + NodeConfigConstant.FAILED_HOST;
					}
				}
				// メモリ.
				else if (this.collectFlgMap.get(NodeConfigSettingItem.HW_MEMORY.name()).booleanValue() //
						&& 0 < token.length && NodeConfigConstant.LINE_HEADER_MEMORY.equals(head)) {
					try {
						memoryList.add(replacer.setRecordMemory(token));
						log.trace(methodName + DELIMITER + "succeeded to add memoryList.");
					} catch (HinemosUnknown e) {
						readResult = readResult + NodeConfigConstant.FAILED_MEMORY;
					}
				}
				// Windowsの場合はBOMバイナリを回避するため1行目空行.
				else if (this.winFlg && isFirst) {
					log.trace(methodName + DELIMITER + "skipped to read top line on windows.");
				}
				// 想定外.
				else {
					log.warn(String.format(methodName + DELIMITER + "no flag or token[0] is invalid. token[0]=[%s]",
							head));
				}
				isFirst = false;
			}
			// パッケージは最後に格納する
			pkgList.addAll(pkgMap.values());

			// 終了処理
			br.close();
			log.debug(methodName + DELIMITER + String.format("succeeded to read TSV file. file=[%s]", fName));
			return readResult;

		} catch (FileNotFoundException fne) {
			log.warn(methodName + DELIMITER + "file not found.", fne);
			throw new HinemosUnknown(fne.getMessage(), fne);
		} catch (IOException e) {
			log.warn(methodName + DELIMITER + "get file failure.", e);
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (HinemosUnknown e) {
			// TSVファイル変換エラー、ログ出力済.
			throw e;
		} catch (Exception e) {
			log.warn(methodName + DELIMITER + "failed to get by unknown exception.", e);
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (log.isDebugEnabled()) {
				log.debug(methodName + DELIMITER + "TSV file name : " + commonPath + fName);
			}
		}
	}

	/**
	 * ユーザ任意コマンドの実行.
	 * 
	 * @return コマンド実行結果.
	 */
	private List<NodeCustomInfo> executeCustomCommand() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// 実行対象なのにユーザ任意情報が存在しない.
		List<NodeConfigCustomInfo> customSettingList = this.config.getNodeConfigSettingCustomList();
		if (customSettingList == null || customSettingList.isEmpty()) {
			log.debug(methodName + DELIMITER + "customized settings are empty."//
					+ " settingID=[" + this.config.getSettingId() + "]");
			return null;
		}

		// ユーザ任意情報の設定単位でコマンド実行.
		List<NodeCustomInfo> returnList = new LinkedList<NodeCustomInfo>();
		for (NodeConfigCustomInfo customSetting : customSettingList) {
			if (customSetting == null || customSetting.getSettingCustomId() == null) {
				log.warn(methodName + DELIMITER + "required property ('customSetting' or 'settingCustomId') is null."//
						+ " settingID=[" + this.config.getSettingId() + "]");
				continue;
			}

			// 失敗結果も格納.
			NodeCustomInfo returnCustomInfo = new NodeCustomInfo();
			String errorMsg = "";
			returnCustomInfo.setSettingCustomId(customSetting.getSettingCustomId());
			if (customSetting.getCommand() == null || customSetting.getCommand().isEmpty()
					|| customSetting.getDisplayName() == null || customSetting.getDisplayName().isEmpty()) {
				errorMsg = "the setting is invalid. required property ('command' or 'displayName') in customized setting is null or empty.";
				log.warn(methodName + DELIMITER + errorMsg//
						+ " settingID=[" + this.config.getSettingId() + "]"//
						+ " customID=[" + customSetting.getSettingCustomId() + "]");
				returnCustomInfo.setValue(errorMsg);
				returnCustomInfo.setRegisterFlag(NodeRegisterFlagConstant.GET_FAILURE);
				returnCustomInfo.setCommand("");
				returnCustomInfo.setDisplayName("");
				returnList.add(returnCustomInfo);
				continue;
			}
			returnCustomInfo.setCommand(customSetting.getCommand());
			returnCustomInfo.setDisplayName(customSetting.getDisplayName());

			if (customSetting.isValidFlg() == null) {
				errorMsg = "the setting is invalid. 'validFlg' is null or empty.";
				log.warn(methodName + DELIMITER + errorMsg//
						+ " settingID=[" + this.config.getSettingId() + "]"//
						+ ", customID=[" + customSetting.getSettingCustomId() + "]");
				returnCustomInfo.setValue(errorMsg);
				returnCustomInfo.setRegisterFlag(NodeRegisterFlagConstant.GET_FAILURE);
				returnList.add(returnCustomInfo);
				continue;
			}

			// 有効な設定のみ実施.
			if (!customSetting.isValidFlg().booleanValue()) {
				log.trace(methodName + DELIMITER + "flag of customized settings is invalid."//
						+ " settingID=[" + this.config.getSettingId() + "]"//
						+ ", customID=[" + customSetting.getSettingCustomId() + "]");
				returnCustomInfo.setValue(null);
				returnCustomInfo.setRegisterFlag(NodeRegisterFlagConstant.NOT_GET);
				returnList.add(returnCustomInfo);
				continue;
			}

			// ユーザチェック.
			if (customSetting.isSpecifyUser() == null || //
					(customSetting.isSpecifyUser().booleanValue() && //
							(customSetting.getEffectiveUser() == null || customSetting.getEffectiveUser().isEmpty()))) {
				errorMsg = "the setting is invalid. 'specifyUser' or 'effectiveUser' in customized setting is null or empty.";
				log.warn(methodName + DELIMITER + errorMsg//
						+ " settingID=[" + this.config.getSettingId() + "]"//
						+ ", customID=[" + customSetting.getSettingCustomId() + "]");
				returnCustomInfo.setValue(errorMsg);
				returnCustomInfo.setRegisterFlag(NodeRegisterFlagConstant.GET_FAILURE);
				returnList.add(returnCustomInfo);
				continue;
			}
			String effectiveUser = "";
			if (customSetting.isSpecifyUser().booleanValue()) {
				effectiveUser = customSetting.getEffectiveUser();
			}

			// カスタムコマンド実行.
			CommandResult ret = null;
			try {
				CommandCreator.PlatformType platform = CommandCreator.convertPlatform(_commandMode);
				String[] command = CommandCreator.createCommand(effectiveUser, customSetting.getCommand(), platform,
						customSetting.isSpecifyUser().booleanValue(), _commandLogin);

				// コマンド実行
				CommandExecutor cmdExecutor = new CommandExecutor(command, _charset, _timeoutInterval,
						_customBufferSize);
				cmdExecutor.execute();
				ret = cmdExecutor.getResult();
				if (ret != null && NodeConfigConstant.EXCD_SUCCESS == ret.exitCode.intValue()) {
					log.trace(methodName + DELIMITER + "succeeded to execute customized command."//
							+ " settingID=[" + this.config.getSettingId() + "]"//
							+ ", customID=[" + customSetting.getSettingCustomId() + "]");
				} else if (ret == null) {
					errorMsg = "failed to execute customized command. result=null";
					log.warn(methodName + DELIMITER + errorMsg//
							+ " settingID=[" + this.config.getSettingId() + "]"//
							+ ", customID=[" + customSetting.getSettingCustomId() + "]");
					returnCustomInfo.setValue(errorMsg);
					returnCustomInfo.setRegisterFlag(NodeRegisterFlagConstant.GET_FAILURE);
					returnList.add(returnCustomInfo);
					continue;
				} else {
					errorMsg = "failed to execute customized command.";
					String result = "result-exitCode : " + ret.exitCode.intValue() + "\n" //
							+ "result-stdout :\n" + ret.stdout + "\n" //
							+ "result-stderr :\n" + ret.stderr;
					log.info(methodName + DELIMITER + "failed to execute customized command."//
							+ " settingID=[" + this.config.getSettingId() + "]"//
							+ " customID=[" + customSetting.getSettingCustomId() + "]" //
							+ result);
					returnCustomInfo.setValue(errorMsg + "\n" + result);
					returnCustomInfo.setRegisterFlag(NodeRegisterFlagConstant.GET_FAILURE);
					returnList.add(returnCustomInfo);
					continue;
				}
			} catch (HinemosUnknown e) {
				errorMsg = "failed to execute customized command.";
				log.warn(methodName + DELIMITER + "failed to execute customized command."//
						+ " settingID=[" + this.config.getSettingId() + "]"//
						+ ", customID=[" + customSetting.getSettingCustomId() + "]");
				String stackTrace = Arrays.toString(e.getStackTrace());
				returnCustomInfo.setValue(errorMsg + "\n" + e.getMessage() + "\n" + stackTrace);
				returnCustomInfo.setRegisterFlag(NodeRegisterFlagConstant.GET_FAILURE);
				returnList.add(returnCustomInfo);
				continue;
			}

			// 成功した分を返却用にセット.
			log.trace(methodName + DELIMITER + "succeeded to execute customized commands."//
					+ " settingID=[" + this.config.getSettingId() + "]"//
					+ ", customID=[" + customSetting.getSettingCustomId() + "]");
			returnCustomInfo.setValue(ret.stdout);
			returnCustomInfo.setRegisterFlag(NodeRegisterFlagConstant.GET_SUCCESS);
			returnList.add(returnCustomInfo);
		}
		if (returnList.isEmpty()) {
			log.warn(methodName + DELIMITER + "failed to execute all of customized command."//
					+ " settingID=[" + this.config.getSettingId() + "]");
			return null;
		}

		log.trace(methodName + DELIMITER + "finished to execute all of customized command."//
				+ " settingID=[" + this.config.getSettingId() + "]");
		return returnList;
	}

	/**
	 * NodeInfo初期化
	 * 
	 * @param nInfo
	 * @return
	 */
	private static NodeInfo initNodeInfo(NodeInfo nInfo) {

		// 初期化
		// 登録フラグ
		nInfo.setNodeCpuRegisterFlag(NodeRegisterFlagConstant.GET_FAILURE);
		nInfo.setNodeDiskRegisterFlag(NodeRegisterFlagConstant.GET_FAILURE);
		nInfo.setNodeFilesystemRegisterFlag(NodeRegisterFlagConstant.GET_FAILURE);
		nInfo.setNodeHostnameRegisterFlag(NodeRegisterFlagConstant.GET_FAILURE);
		nInfo.setNodeMemoryRegisterFlag(NodeRegisterFlagConstant.GET_FAILURE);
		nInfo.setNodeNetworkInterfaceRegisterFlag(NodeRegisterFlagConstant.GET_FAILURE);
		nInfo.setNodeOsRegisterFlag(NodeRegisterFlagConstant.GET_FAILURE);
		nInfo.setNodeProcessRegisterFlag(NodeRegisterFlagConstant.GET_FAILURE);
		nInfo.setNodePackageRegisterFlag(NodeRegisterFlagConstant.GET_FAILURE);

		return nInfo;
	}

	/**
	 * Managerへの通知情報送信.<br>
	 * <br>
	 * 
	 * @param priority
	 *            重要度
	 * @param msg
	 *            メッセージ
	 * @param msgOrg
	 *            オリジナルメッセージ
	 */
	protected static void sendMessage(int priority, String msg, String msgOrg, String settingId) {
		OutputBasicInfo output = new OutputBasicInfo();
		output.setPluginId(HinemosModuleConstant.NODE_CONFIG_SETTING);
		output.setPriority(priority);
		output.setApplication(MessageConstant.AGENT.getMessage());
		output.setMessage(msg);
		output.setMessageOrg(msgOrg);
		output.setGenerationDate(HinemosTime.getDateInstance().getTime());
		output.setMonitorId(settingId);
		output.setFacilityId(""); // マネージャがセット.
		output.setScopeText(""); // マネージャがセット.
		sendQueue.put(output);
	}
}
