/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.binary.thread;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pcap4j.core.PcapHandle;

import com.clustercontrol.agent.binary.BinaryMonitorConfig;
import com.clustercontrol.agent.binary.packet.PacketCapture;
import com.clustercontrol.agent.binary.packet.PacketListenerImpl;
import com.clustercontrol.util.HinemosTime;

public class PacketCaptureThread extends Thread {

	/** ロガー */
	private static Log log = LogFactory.getLog(PacketCaptureThread.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	/** スレッド名 */
	public static final String threadName = "PacketCaptureThread";

	/** パケットキャプチャ間隔 */
	private int capInterval;

	/** スレッド繰返し(trueだとrun()の処理内容が繰返される). */
	private boolean loop = true;

	/** リスナーマップ(キー:MonitorID+IP,値:リスナー). */
	private Map<String, PacketListenerImpl> listenerMap = new ConcurrentHashMap<String, PacketListenerImpl>();

	/** 更新用リスナーマップ(キー:MonitorID+IP,値:リスナー). */
	private Map<String, PacketListenerImpl> refreshListenerMap = new ConcurrentHashMap<String, PacketListenerImpl>();

	/** スレッド開始時間(性能検証用). */
	private long startmsec = 0L;

	/** コンストラクタ. */
	public PacketCaptureThread(Map<String, PacketListenerImpl> listenerMap) {
		this.listenerMap = listenerMap;
		this.capInterval = BinaryMonitorConfig.getCapInterval();
	}

	/**
	 * スレッド実行処理.
	 */
	@Override
	public void run() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.info("run " + threadName);
		while (loop) {
			if (log.isDebugEnabled()) {
				long msec = 0L;
				if (this.startmsec > 0) {
					msec = HinemosTime.getDateInstance().getTime() - this.startmsec;
				}
				log.debug(String.format(
						methodName + DELIMITER + threadName + " is on top of run(). thread interval time=%dmsec",
						msec));
				this.startmsec = HinemosTime.getDateInstance().getTime();
			}
			// 更新用のリスナーを処理対象として設定.
			this.refreshListener();
			try {
				int loopCnt = BinaryMonitorConfig.getCapLoopCount();
				log.debug(methodName + DELIMITER + "loopCnt =[" + loopCnt + "]");

				for (Entry<String, PacketListenerImpl> entry : this.listenerMap.entrySet()) {
					if (!entry.getValue().getHandle().isOpen()) {
						// スレッド再開等でパケットキャプチャ操作オブジェクトがクローズしている場合は再生成.
						PacketCapture packetCapture = new PacketCapture();
						PcapHandle handle = packetCapture.getPcapHandle(entry.getValue().getMonInfo(), entry.getKey());
						PacketListenerImpl listener = packetCapture.getListener(entry.getKey(),
								entry.getValue().getMonInfo(), handle);
						this.listenerMap.put(entry.getKey(), listener);
						log.info(String.format(
								methodName + DELIMITER
										+ "regenerate the new handle and listener for packet capture. monitorId=%s, ip=%s",
								entry.getValue().getMonInfo().getId(), entry.getKey()));
					}
					// loopはlibpcapのnativeコードをwrapperしたメソッド.
					// 詳細は以下参照.
					// https://www.tcpdump.org/manpages/pcap_loop.3pcap.html
					entry.getValue().getHandle().loop(loopCnt, entry.getValue());
				}

			} catch (Exception e) {
				log.warn(threadName + " : " + e.getClass().getCanonicalName() + ", " + e.getMessage(), e);
			} catch (Throwable e) {
				log.error(threadName + " : " + e.getClass().getCanonicalName() + ", " + e.getMessage(), e);
			}
			try {
				Thread.sleep(capInterval);
			} catch (InterruptedException e) {
				log.info(threadName + " for is Interrupted");
				break;
			}
		}
		this.close();
	}

	/** リスナーマップ(キー:IP,値:リスナー). */
	public Map<String, PacketListenerImpl> readListnerMap() {
		return new ConcurrentHashMap<String, PacketListenerImpl>(this.listenerMap);
	}

	/**
	 * リスナーマップ追加.
	 */
	public void addListnerMap(Map<String, PacketListenerImpl> newListnerMap) {
		this.refreshListenerMap.putAll(newListnerMap);
	}

	/**
	 * スレッド停止.
	 */
	public void terminate() {
		this.loop = false;
	}

	/**
	 * パケットキャプチャ用のリスナー更新.
	 */
	private void refreshListener() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		// 更新用のリスナーが存在しない場合は実施しない.
		if (this.refreshListenerMap.isEmpty()) {
			return;
		}

		// 削除された監視設定に紐づくリスナーについてはクローズ処理を実施する.
		PacketListenerImpl oldListener = null;
		for (Entry<String, PacketListenerImpl> entry : this.listenerMap.entrySet()) {
			oldListener = entry.getValue();
			// 更新用のリスナーに含まれてるかチェックして含まれてなかった場合はクローズ処理.
			if (!this.refreshListenerMap.keySet().contains(entry.getKey())) {
				oldListener.close();
				log.info(methodName + DELIMITER + String.format("close listener. monitorId=%s, address=%s",
						oldListener.getMonInfo().getId(), oldListener.getAddress()));
			} else {
				log.debug(methodName + DELIMITER + String.format("skip to close listener. monitorId=%s, address=%s",
						oldListener.getMonInfo().getId(), oldListener.getAddress()));
			}
		}

		// リスナー更新.
		this.listenerMap = new ConcurrentHashMap<String, PacketListenerImpl>(this.refreshListenerMap);
		this.refreshListenerMap.clear();
	}

	/**
	 * スレッドクローズ(スレッド紐づきオブジェクト削除・ログ出力).
	 */
	private void close() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		for (PacketListenerImpl listener : this.listenerMap.values()) {
			listener.close();
		}
		this.listenerMap.clear();
		for (PacketListenerImpl refreshListener : this.refreshListenerMap.values()) {
			refreshListener.close();
		}
		this.refreshListenerMap.clear();
		log.info(methodName + DELIMITER + threadName + " is terminated");
	}

}
