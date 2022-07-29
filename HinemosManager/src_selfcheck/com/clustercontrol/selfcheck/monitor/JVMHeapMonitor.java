/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.selfcheck.monitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * Java VMの利用可能なヒープ容量を確認する処理の実装クラス
 */
public class JVMHeapMonitor extends SelfCheckMonitorBase {

	private static Log m_log = LogFactory.getLog( JVMHeapMonitor.class );

	public final String monitorId = "SYS_JVM_HEAP";
	public final String subKey = "";
	public final String application = "SELFCHECK (Java VM Heap)";

	
	/**
	 * セルフチェック処理名
	 */
	@Override
	public String toString() {
		int jvmMinHeapThreshold = HinemosPropertyCommon.selfcheck_monitoring_jvm_freeheap_threshold.getIntegerValue();
		return "monitoring jvm free heap (threshold = " + jvmMinHeapThreshold + " [mbyte] )";
	}

	/**
	 * 監視項目ID
	 */
	@Override
	public String getMonitorId() {
		return monitorId;
	}

	/**
	 * Java VMの利用可能なヒープ容量が最小値以上であるかを確認する処理
	 * @return 通知情報（アプリケーション名は未格納）
	 */
	@Override
	public void execute() {
		if(!HinemosPropertyCommon.selfcheck_monitoring_jvm_freeheap.getBooleanValue()) {
			m_log.debug("skip");
			return;
		}
		
		/** ローカル変数 */
		int freeHeapMByte = 0;
		boolean warn = true;

		/** メイン処理 */
		if (m_log.isDebugEnabled()) m_log.debug("monitoring java vm heap size.");

		// 利用可能なヒープ容量をMByte単位で取得する
		freeHeapMByte = getJVMHeapSize();
		int jvmMinHeapThreshold = HinemosPropertyCommon.selfcheck_monitoring_jvm_freeheap_threshold.getIntegerValue();

		if (jvmMinHeapThreshold >= 0 && freeHeapMByte >= jvmMinHeapThreshold) {
			m_log.debug("size of java vm's free heap is enough. (free heap's size = " + freeHeapMByte + " [MByte], threshold = " + jvmMinHeapThreshold + " [MByte])");
			warn = false;
		}

		if (warn) {
			m_log.info("size of java vm's free heap is low. (free heap's size = " + freeHeapMByte + " [MByte], threshold = " + jvmMinHeapThreshold + " [MByte])");
		}
		if (!isNotify(subKey, warn)) {
			return;
		}

		String[] msgAttr1 = { Integer.toString(freeHeapMByte),
				Integer.toString(jvmMinHeapThreshold) };
		AplLogger.put(InternalIdCommon.SYS_SFC_SYS_003, msgAttr1,
				"free heap of jvm (" +
						freeHeapMByte +
						" [mbyte]) is not enough (threshold " +
						jvmMinHeapThreshold +
				" [mbyte]).");

		return;
	}
	
	/**
	 * 利用可能なヒープ容量をMByte単位で取得する<br/>
	 * @return 利用可能なヒープ容量
	 */
	public static int getJVMHeapSize() {
		return (int)(Runtime.getRuntime().freeMemory() / 1024 / 1024);
	}

}
