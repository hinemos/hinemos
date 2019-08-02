/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.session;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.monitor.bean.EventCustomCommandInfo;
import com.clustercontrol.monitor.bean.EventCustomCommandInfoData;
import com.clustercontrol.monitor.bean.EventCustomCommandResultRoot;
import com.clustercontrol.monitor.bean.EventDataInfo;
import com.clustercontrol.monitor.bean.EventHinemosPropertyConstant;
import com.clustercontrol.monitor.factory.SelectEventHinemosProperty;
import com.clustercontrol.monitor.run.util.EventCustomCommandTask;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * イベントカスタムコマンド機能の管理を行う Session Bean です。<BR>
 * クライアントからの Entity Bean へのアクセスは、Session Bean を介して行います。
 * 
 */
public class EventCustomCommandBean {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( EventCustomCommandBean.class );
	
	private static Map<Integer, CommandThreadPoolManager> threadPoolManagerMap = null;
	private static UniqueKeyManager<EventCustomCommandResultRoot> commandResultManager = null;
	private static ExitResultRemover exitResultRemover = null;
	
	/**
	 * 初期化処理
	 */
	public static void init() {
		threadPoolManagerMap = new ConcurrentHashMap<>();
		commandResultManager = new UniqueKeyManager<>();
		exitResultRemover = new ExitResultRemover(commandResultManager);
		exitResultRemover.start();
		
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			Map<Integer, EventCustomCommandInfo> customCommandMap = 
					SelectEventHinemosProperty.getEventCustomCommandInfo();
			
			//Hinemosプロパティで有効となっているindexのThreadPoolを作成する
			for (int i = 1; i <= EventHinemosPropertyConstant.COMMAND_SIZE; i++) {
				if (!customCommandMap.get(i).getEnable()) {
					continue;
				}
				
				int poolSize = customCommandMap.get(i).getThread().intValue();
				int capacity = customCommandMap.get(i).getQueue().intValue();
				
				CommandThreadPoolManager manager = new CommandThreadPoolManager(commandResultManager, i, poolSize, capacity);
				threadPoolManagerMap.put(i, manager);
				
			}
			
		} catch (Exception e) {
			m_log.warn("init() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw e;
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}
	
	/**
	 * 終了処理
	 */
	public static void terminate() {
		
		if (threadPoolManagerMap != null) {
			//ThreadPool
			for (CommandThreadPoolManager threadPoolManager : threadPoolManagerMap.values()) {
				threadPoolManager.shutdown();
			}
		}
		
		if (exitResultRemover != null) {
			exitResultRemover.shutdown();
		}
		
		if (threadPoolManagerMap != null) {
			while (true) {
				
				//ThreadPoolの終了待ち
				boolean isTerminate = true;
				for (CommandThreadPoolManager threadPoolManager : threadPoolManagerMap.values()) {
					if (!threadPoolManager.isTerminate()) {
						isTerminate = false;
						break;
					}
				}
				if (isTerminate) {
					break;
				}
				
				try {
					Thread.sleep(1 * 1000);
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
	}
	
	public static class ExitResultRemover extends Thread {
		
		private static Log m_log = LogFactory.getLog( ExitResultRemover.class );
		private static final long CHECK_TIME = 60 * 1000;
		
		boolean terminate = false;
		UniqueKeyManager<EventCustomCommandResultRoot> commandResultManager = null;
		
		public ExitResultRemover(UniqueKeyManager<EventCustomCommandResultRoot> commandResultManager) {
			this.commandResultManager = commandResultManager;
		}
		
		@Override
		public void run() {
			while (true) {
				try {
					if (terminate) {
						return;
					}
					
					try {
						Thread.sleep(CHECK_TIME);
					} catch (InterruptedException e) {
						// ignore
					}
				} catch (Exception e) {
					m_log.warn(e);
				}
			}
		}
		
		public void removeExitTask() {
			
			
			long now = HinemosTime.currentTimeMillis();
			
			Collection<String> col = this.commandResultManager.getKeys();
			
			for (String key : col) {
				EventCustomCommandResultRoot result = this.commandResultManager.uniqueKeyObjectMap.get(key);
				
				if (result == null) {
					//処理中に他の処理から削除される場合
					continue;
				}
				
				long keeptime = HinemosPropertyCommon.monitor_event_customcmd_cmd$_result_keeptime.getIntegerValue(result.getCommandNo().toString());
				
				if (result.getCommandEndTime() == null) {
					//まだ処理が終了していない場合
					continue;
				}
				
				if (result.getCommandEndTime() + keeptime < now ) {
					//保存期間を経過している場合
					this.commandResultManager.removeObject(key);
				}
			}
		}
		
		public void shutdown() {
			this.interrupt();
			this.terminate = true;
		}
	}
	
	/**
	 * イベントカスタムコマンドの設定を取得します。<BR><BR> 
	 * 
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public EventCustomCommandInfoData getEventCustomCommandSettingInfo() throws InvalidRole, HinemosUnknown{
		
		EventCustomCommandInfoData info = null;
		
		JpaTransactionManager jtm = null;
		
		//入力チェック
		try{
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			info = new EventCustomCommandInfoData();
			
			info.setEvemtCustomCommandMap(SelectEventHinemosProperty.getEventCustomCommandInfo());
			
			jtm.commit();
			

		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getEventCustomCommandSettingInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		
		return info;
	}
	
	/**
	 * イベントカスタムコマンド実行
	 * 
	 * @param commandNo 実行するコマンドのindex
	 * @param eventList コマンド実行対象のイベント情報リスト
	 * @return 実行したコマンドに対するユニークキー（結果の取得に使用）
	 * @throws HinemosUnknown
	 */
	public String execEventCustomCommand(int commandNo, List<EventDataInfo> eventList) throws HinemosUnknown {
		
		if (eventList == null) {
			throw new HinemosUnknown("eventList is null");
		}
		
		if (!threadPoolManagerMap.containsKey(commandNo)) {
			//ThreadPoolが作成されていないとき
			//（Hinemos Manager起動時に対象のコマンドがenable = trueでないとき）
			m_log.info("command " + commandNo + " not enable." );
			throw new HinemosUnknown(
					MessageConstant.MESSAGE_EVENT_CUSTOM_COMMAND_EVENT_NOT_ENABLE.getMessage(String.valueOf(commandNo))
					);
		}
		
		Long userExecuteTime = HinemosTime.currentTimeMillis();
		
		CommandThreadPoolManager threadPoolManager = threadPoolManagerMap.get(commandNo);
		
		String username = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		
		//コマンドをThreadPoolに登録する
		String commandResultID = threadPoolManager.addCustomEventComamnd(eventList, username, userExecuteTime); 
		m_log.info("event custom command regist commandNo:" + commandNo + " commandResultID : " + commandResultID );
		return commandResultID;
	}
	
	/**
	 * イベントカスタムコマンド実行結果取得.
	 * 
	 * @param commandResultID コマンド実行結果ＩＤ
	 * @return イベントカスタムコマンド実行結果 カスタムイベントコマンドの実行中の場合、null
	 * @throws HinemosUnknown 
	 */
	public EventCustomCommandResultRoot getEventCustomCommandResult(String commandResultID) throws HinemosUnknown {
		
		if (commandResultID == null) {
			throw new HinemosUnknown("commandResultID is null");
		}
		
		EventCustomCommandResultRoot result = commandResultManager.getObject(commandResultID);
		
		if (result == null) {
			//実行結果が存在しない場合
			//以下のパターンがありえる
			//誤ったIDを指定／すでに１回取得されたため、削除済／生存期間を過ぎたため、削除された
			m_log.info("init() : commandResultID:" + commandResultID);
			throw new HinemosUnknown(MessageConstant.MESSAGE_EVENT_CUSTOM_COMMAND_EXCUTING_COMMAND_NOT_FOUND.getMessage());
		}
		
		if (result.getCommandEndTime() == null) {
			//コマンドが終了していない場合
			return null;
		}
		
		//コマンドが終了している場合、返却と同時に削除
		commandResultManager.removeObject(commandResultID);
		
		return result;
	}
	
	/**
	 * スレッドプールを管理するクラス（カスタムイベントコマンドの定義単位にインスタンスが生成される）
	 *
	 */
	public static class CommandThreadPoolManager {
		/** 実行結果を保持するオブジェクト（インスタンスはHinemosMagaerで１件のみ） */
		private UniqueKeyManager<EventCustomCommandResultRoot> commandResultManager = null;
		/** レコード単位の排他制御を行うためのオブジェクト */
		private LockManager lockManaer = new LockManager();
		/** イベントカスタムコマンド実行用のThreadPool */
		private ThreadPoolExecutor threadPool;
		/** イベントカスタムコマンドIndex */
		private int commandNo;
		
		/**
		 * コンストラクタ
		 * 
		 * @param commandResultManager 実行中タスク管理クラス
		 * @param commandNo イベントカスタムコマンドIndex
		 * @param poolSize ThreadPoolの同時実行Thread数
		 * @param capacity キューの最大容量
		 */
		public CommandThreadPoolManager(
				UniqueKeyManager<EventCustomCommandResultRoot> commandResultManager,
				int commandNo,
				int poolSize,
				int capacity
				) {
			this.lockManaer = new LockManager();
			
			this.commandResultManager = commandResultManager;
			this.commandNo = commandNo;
			
			this.threadPool = new ThreadPoolExecutor(poolSize, poolSize,
					0L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>(capacity),
					new ThreadFactory() {
				
				private volatile int _count = 0;
				private int commandNo;
				
				@Override
				public Thread newThread(Runnable r) {
					return new Thread(r, "EventCustomCommandWorker[" + commandNo + "] - " + _count++);
				}
				
				public ThreadFactory setCommandNo(int commandNo) {
					this.commandNo = commandNo;
					return this;
				}
				
			}.setCommandNo(commandNo), new ThreadPoolExecutor.AbortPolicy());
		}
		
		/**
		 * ThreadPoolに引数で渡されたカスタムイベントコマンドを実行登録する
		 * 
		 * @param eventList イベント情報の一覧
		 * @param operationUser カスタムイベントコマンド実行ユーザ
		 * @return 実行登録されたカスタムイベントコマンドのユニークキー
		 * @throws HinemosUnknown
		 */
		public String addCustomEventComamnd(List<EventDataInfo> eventList, String operationUser, Long userExecuteTime) throws HinemosUnknown {
			
			//イベントカスタムコマンド実行用のタスクを作成
			EventCustomCommandInfo info = SelectEventHinemosProperty.getEventCustomCommandInfo().get(commandNo);
			EventCustomCommandTask task = new EventCustomCommandTask(this.lockManaer ,commandNo, info, eventList, operationUser, userExecuteTime);
			EventCustomCommandResultRoot result = task.getResult();
			
			//タスクを実行中として登録し、ユニークキーを受けとる
			String resultId = commandResultManager.addObject(result);
			task.setResultId(resultId);
			
			try {
				//ThreadPoolにタスクを登録
				threadPool.execute(task);
			} catch (RejectedExecutionException e) {
				//キュー上限をオーバした時
				//以下のログは実行タイミングによっては正確な値が反映されない場合がある
				m_log.info("Queue capacity over." 
					+ " Command:" + info.getDisplayName()
					+ " thread(setting): " + threadPool.getMaximumPoolSize()
					+ " thread(now): " + threadPool.getActiveCount()
					+ " queue(setting): " + (threadPool.getQueue().size() + threadPool.getQueue().remainingCapacity())
					+ " queue(now): " + threadPool.getQueue().size());
				
				//実行できなかったため、実行中タスクから削除する
				commandResultManager.removeObject(resultId);
				
				throw new HinemosUnknown(MessageConstant.MESSAGE_EVENT_CUSTOM_COMMAND_QUEUE_SIZE_OVER.getMessage(
						info.getDisplayName(), String.valueOf(threadPool.getQueue().size())));
			}
			
			return resultId;
		}
		
		public void shutdown() {
			if (this.threadPool != null) {
				this.threadPool.shutdown();
				List<Runnable> taskList = this.threadPool.shutdownNow();
				for (Runnable runnable : taskList) {
					if (runnable instanceof EventCustomCommandTask) {
						((EventCustomCommandTask) runnable).cancelAll();
					}
				}
			}
		}
		
		public boolean isTerminate() {
			if (this.threadPool == null) {
				return true;
			}
			
			return this.threadPool.isTerminated();
			
		}
	}
	
	/**
	 * ユニークキーとオブジェクトの組み合わせを管理するクラス.（ThreadSafe)
	 *
	 */
	public static class UniqueKeyManager<T> {
		private Map<String, T> uniqueKeyObjectMap = null;
		
		public UniqueKeyManager() {
			this.uniqueKeyObjectMap = new ConcurrentHashMap<>();
		}
		
		public String addObject(T object) {
			String genarateKey = null;
			
			final int retryCount = 100;
			
			for (int i = 0; i < retryCount; i++) {
				genarateKey = UUID.randomUUID().toString();
				
				if (addObjectImpl(genarateKey, object)) {
					return genarateKey;
				}
				m_log.info("duplicate uniqueKey genarateKey = " + genarateKey);
			}
			
			//UUIDの衝突自体が超レアケースなので、ここに到達することは基本的にないはず
			//リトライは無制限でも問題ないと思うが、何らかの理由で無限ループになることを避けるため、リトライ上限を定めておく
			m_log.warn("duplicate uniqueKey retry over");
			
			return null;
		}
		
		
		private boolean addObjectImpl(String execKey ,T task) {
			synchronized (this) {
				if (uniqueKeyObjectMap.containsKey(execKey)) {
					//すでに存在する場合
					return false;
				}
				uniqueKeyObjectMap.put(execKey, task);
				return true;
			}
		}
		
		public T getObject(String uniqueKey) {
			synchronized (this) {
				return uniqueKeyObjectMap.get(uniqueKey);
			}
		}
		
		public void removeObject(String uniqueKey) {
			synchronized (this) {
				uniqueKeyObjectMap.remove(uniqueKey);
			}
		}
		public Collection<String> getKeys() {
			synchronized (this) {
				return uniqueKeyObjectMap.keySet();
			}
		}
		
	}
	
	/**
	 * ロックを管理するクラス
	 * 
	 * 同一キーに対して、処理開始前にロック、処理終了時にリリースをすることで
	 * 同一レコードに対する排他処理を行う
	 *
	 */
	public static class LockManager {
		private Map<String, Semaphore> semaphoreMap = new ConcurrentHashMap<>();
		private Map<String, Long> countMap = new ConcurrentHashMap<>();
		
		/**
		 * ロックを取得する
		 */
		public void getLock(String key) {
			Semaphore semaphore = null;
			
			synchronized(this) {
				Long count = countMap.get(key);
				if (count != null) {
					semaphore = semaphoreMap.get(key);
					countMap.put(key, count + 1);
				} else {
					countMap.put(key, 1L);
					//1多重、FIFOのセマフォ
					semaphore = new Semaphore(1, true);
					semaphoreMap.put(key, semaphore);
				}
			}
			
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				// ignore
			}
		}
		
		/**
		 * ロックを開放する
		 */
		public void releaseLock(String key) {
			Semaphore semaphore = null;
			
			synchronized(this) {
				semaphore = semaphoreMap.get(key);
				Long count = countMap.get(key);
				if (count == null) {
					//発生しない
				} else {
					if (count<= 1) {
						//1件しか実行されていないとき
						countMap.remove(key);
						semaphoreMap.remove(key);
					} else {
						//他にも実行されている時
						countMap.put(key, count -1);
					}
				}
			}
			
			semaphore.release();
		}
	}
		
}
