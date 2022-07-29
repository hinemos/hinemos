/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.filecheck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtJobFileCheckResponse;
import org.openapitools.client.model.AgtOutputBasicInfoRequest;
import org.openapitools.client.model.AgtRunInstructionInfoRequest;

import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.agent.SendQueue.MessageSendableObject;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * ファイルチェックを管理するクラスです。<BR>
 * 設定情報を受け取り、監視スレッドを制御します。
 * 
 */
public class FileCheckManager {

	//ロガー
	private static Log m_log = LogFactory.getLog(FileCheckManager.class);

	/** ディレクトリとファイルチェック(ジョブ)の状態を保持しているマップ */
	private static ConcurrentHashMap<String, FileCheck> m_fileCheckCache =
			new ConcurrentHashMap<String, FileCheck>();

	/** ファイルチェック間隔 */
	private static int m_runInterval = 10000; // 10sec
	
	// Queue送信
	private static SendQueue sendQueue;
	
	public static void setSendQueue(SendQueue sendQueue){
		FileCheckManager.sendQueue = sendQueue;
	}
	
	/**
	 * 設定情報を反映します。
	 * 
	 * @param jobFileCheckList ファイルチェック情報一覧。
	 */
	public static void setFileCheck(List<AgtJobFileCheckResponse> jobFileCheckList) {
		Map<String, List<AgtJobFileCheckResponse>> newJobFileCheckMap = new HashMap<>();

		try {
			String runIntervalStr = AgentProperties.getProperty("job.filecheck.interval",
					Integer.toString(m_runInterval));
			m_runInterval = Integer.parseInt(runIntervalStr);
		} catch (Exception e) {
			m_log.warn("FileCheckThread : " + e.getMessage());
		}
		/*
		 * FileCheckはチェック対象のファイルごとにオブジェクトが生成される。
		 * FileCheck.monitorInfoListに監視設定が登録される。
		 * (FileCheckとmonitorInfoは1対多の関係)
		 */
		/*
		 * 1. FileCheckを生成する。
		 */
		for (AgtJobFileCheckResponse jobFileCheck : jobFileCheckList) {
			if (!jobFileCheck.getValid().booleanValue()) {
				continue;
			}
			m_log.info("jobFileCheck " + jobFileCheck.getId() + ", " + jobFileCheck.getDirectory());
			String directory = jobFileCheck.getDirectory();

			FileCheck fileCheck = m_fileCheckCache.get(directory);
			if(fileCheck == null){
				// ファイル監視オブジェクトを生成。
				fileCheck = new FileCheck(directory);
				m_fileCheckCache.put(directory, fileCheck);
			}

			List<AgtJobFileCheckResponse> list = newJobFileCheckMap.get(directory);
			if (list == null){
				list = new ArrayList<>();
				newJobFileCheckMap.put(directory, list);
			}
			list.add(jobFileCheck);
		}

		/*
		 * 2. FileCheck.monitorInfoListを登録する。
		 */
		List<String> noDirectoryList = new ArrayList<>();
		for (Entry<String, FileCheck> directory : m_fileCheckCache.entrySet()) {
			FileCheck fileCheck = directory.getValue();
			List<AgtJobFileCheckResponse> list = newJobFileCheckMap.get(directory.getKey());
			fileCheck.setJobFileCheckList(list);
			Integer size = fileCheck.sizeJobFileCheckList();
			if (size == null || size == 0) {
				noDirectoryList.add(directory.getKey());
			}
		}
		// 利用していないものは消す
		for (String directory : noDirectoryList) {
			m_fileCheckCache.remove(directory);
		}
	}

	public void start() {
		m_log.info("start");
		FileCheckThread thread = new FileCheckThread();
		thread.setName("FileCheck");
		thread.start();
	}

	private static class FileCheckThread extends Thread {
		@Override
		public void run() {
			m_log.info("run FileCheckThread");
			while (true) {
				try {
					ArrayList<String> delList = new ArrayList<String>();
					for (String directory : m_fileCheckCache.keySet()) {
						FileCheck filecheck = m_fileCheckCache.get(directory);
						if (filecheck.sizeJobFileCheckList() == 0) {
							delList.add(directory);
						} else {
							filecheck.run();
						}
					}
					for (String directory : delList) {
						m_fileCheckCache.remove(directory);
					}
				} catch (UnsatisfiedLinkError | NoClassDefFoundError e){
					m_log.error("FileCheckThread : Thread is terminated. " + e.getClass().getCanonicalName() + ", " +
							e.getMessage(), e);
					FileCheckManager.sendMessage(PriorityConstant.TYPE_CRITICAL,
							MessageConstant.MESSAGE_JOBFILECHECK_FAILED_TO_CHECK.getMessage(),
							"Failed to exec FileCheckThread.run(). FileCheckThread is terminated. " + e.getClass().getCanonicalName() + ", " + e.getMessage(), HinemosModuleConstant.SYSYTEM, null);
					break;
				}
				catch (Exception e) {
					m_log.warn("FileCheckThread : " + e.getClass().getCanonicalName() + ", " +
							e.getMessage(), e);
				} catch (Throwable e) {
					m_log.error("FileCheckThread : " + e.getClass().getCanonicalName() + ", " +
							e.getMessage(), e);
				}
				try {
					Thread.sleep(m_runInterval);
				} catch (InterruptedException e) {
					m_log.info("FileCheckThread is Interrupted");
					break;
				}
			}
		}
	}
	
	/**
	 * 通知をマネージャに送信する。
	 * @param priority
	 * @param message
	 * @param messageOrg
	 * @param monitorId
	 */
	public static void sendMessage(int priority, String message, String messageOrg, String monitorId, AgtRunInstructionInfoRequest runInstructionInfo) {
		MessageSendableObject sendme = new MessageSendableObject();
		sendme.body = new AgtOutputBasicInfoRequest();
		sendme.body.setPluginId(HinemosModuleConstant.JOB_KICK);
		sendme.body.setPriority(priority);
		sendme.body.setApplication(MessageConstant.AGENT.getMessage());
		sendme.body.setMessage(message);
		sendme.body.setMessageOrg(messageOrg);
		sendme.body.setGenerationDate(HinemosTime.getDateInstance().getTime());
		sendme.body.setMonitorId(monitorId);
		sendme.body.setFacilityId(""); // マネージャがセットする。
		sendme.body.setScopeText(""); // マネージャがセットする。
		sendme.body.setRunInstructionInfo(runInstructionInfo);
		sendQueue.put(sendme);
	}
}
