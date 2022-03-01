/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.rpa;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtRunInstructionInfoResponse;
import org.openapitools.client.model.ForwardRpaScreenshotRequest;
import org.openapitools.client.model.SetJobResultRequest;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.agent.SendQueue.JobResultSendableObject;
import com.clustercontrol.agent.job.AgentThread;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.rpa.bean.RoboScreenshotInfo;
import com.clustercontrol.jobmanagement.rpa.util.RoboFileManager;
import com.clustercontrol.util.HinemosTime;

public class ScreenshotThread extends AgentThread {

	/** ロガー */
	static private Log m_log = LogFactory.getLog(ScreenshotThread.class);
	/** RPAツールエグゼキューター連携用ファイル出力先フォルダ */
	private static String roboFileDir = Agent.getAgentHome() + "var/rpa";
	/** RPAツールエグゼキューター連携用ファイル管理オブジェクト */
	private RoboFileManager roboFileManager;
	/** 実行結果ファイルチェック間隔 */
	private static int checkInterval = 10000; // 10sec
	/** スクリーンショットファイル名プレフィックス */
	private static String screenshotFileNamePrefix = "screenshot";
	/**
	 * スクリーンショットファイル名<br>
	 * ファイル名に時刻を入れる。
	 */
	private String screenshotFileName = screenshotFileNamePrefix + "-%s.png";
	/** スレッド名 */
	private String threadName = "ScreenshotThread";

	static {
		String key1 = "job.rpa.result.check.interval";
		try {
			String checkIntervalStr = AgentProperties.getProperty(key1, Integer.toString(checkInterval));
			checkInterval = Integer.parseInt(checkIntervalStr);
		} catch (Exception e) {
			m_log.warn("ScreenshotThread : " + e.getMessage(), e);
		}
		m_log.info(key1 + "=" + checkInterval);
	}

	/**
	 * コンストラクタ
	 * 
	 * @param info
	 * @param sendQueue
	 */
	public ScreenshotThread(AgtRunInstructionInfoResponse info, SendQueue sendQueue) {
		super(info, sendQueue);
		this.setName(threadName);
		this.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				m_log.error("uncaughtException() : " + e.getMessage(), e);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		m_log.info("run() start");

		// 開始メッセージ送信
		sendMessage(RunStatusConstant.START);

		// スクリーンショット取得の開始
		takeScreenshot();

		// 終了メッセージ送信
		sendMessage(RunStatusConstant.END);
	}

	/**
	 * スクリーンショットを取得します。<br>
	 * <dl>
	 * <dt>run()が呼び出される場合</dt>
	 * <dd>マネージャからのスクリーンショット取得指示があった場合に呼び出されます。
	 * <ul>
	 * <li>終了遅延による取得</li>
	 * <li>手動実行による取得</li>
	 * </ul>
	 * <dt>takeScreenshot()が呼び出される場合</dt>
	 * <dd>RPAシナリオジョブの実行で終了値が条件を満たした場合に呼び出されます。</dd>
	 * </dl>
	 */
	public void takeScreenshot() {
		// スクリーンショット取得指示ファイルの生成
		String screenshotFileName = String.format(this.screenshotFileName, HinemosTime.currentTimeMillis());
		RoboScreenshotInfo screenshotInfo = new RoboScreenshotInfo();
		screenshotInfo.setDatetime(m_info.getRpaRoboRunInfo().getDatetime());
		screenshotInfo.setSessionId(m_info.getRpaRoboRunInfo().getSessionId());
		screenshotInfo.setJobunitId(m_info.getRpaRoboRunInfo().getJobunitId());
		screenshotInfo.setJobId(m_info.getRpaRoboRunInfo().getJobId());
		screenshotInfo.setFacilityId(m_info.getRpaRoboRunInfo().getFacilityId());
		screenshotInfo.setScreenshotFileName(screenshotFileName);
		m_log.debug("run() : screenshotInfo=" + screenshotInfo);
		roboFileManager = new RoboFileManager(roboFileDir);
		// RPAツールエグゼキューターへの実行指示
		try {
			roboFileManager.write(screenshotInfo);
		} catch (IOException e1) {
			// RPAツールエグゼキューター連携用ファイル入出力エラーが発生
			sendMessage(RunStatusConstant.ERROR);
		}

		// スクリーンショットが生成するまで待機
		File file = new File(roboFileDir, screenshotFileName);
		while (!file.exists()) {
			try {
				Thread.sleep(checkInterval);
			} catch (InterruptedException e) {
				m_log.warn("run() : thread interrupted");
				sendMessage(RunStatusConstant.ERROR);
			}
		}

		// スクリーンショットが生成したらマネージャへ転送
		sendScreenshot(m_info, file,
				ForwardRpaScreenshotRequest.TriggerTypeEnum.fromValue(m_info.getRpaScreenshotTriggerType().getValue()));
	}

	/**
	 * メッセージを送信します。
	 * 
	 * @param status
	 *            スクリーンショット取得処理のステータス
	 */
	private void sendMessage(int status) {
		JobResultSendableObject result = new JobResultSendableObject();
		result.sessionId = m_info.getSessionId();
		result.jobunitId = m_info.getJobunitId();
		result.jobId = m_info.getJobId();
		result.facilityId = m_info.getFacilityId();
		result.body = new SetJobResultRequest();
		result.body.setCommand(m_info.getCommand());
		result.body.setCommandType(m_info.getCommandType());
		result.body.setStatus(status);
		result.body.setTime(HinemosTime.getDateInstance().getTime());
		m_sendQueue.put(result);
	}

	/**
	 * マネージャにスクリーンショットを転送します。
	 * 
	 * @param info
	 * @param file
	 * @param triggerType
	 */
	public static void sendScreenshot(AgtRunInstructionInfoResponse info, File file,
			ForwardRpaScreenshotRequest.TriggerTypeEnum triggerType) {
		ForwardRpaScreenshotRequest request = new ForwardRpaScreenshotRequest();
		m_log.info("sendScreenshot()");
		m_log.debug("sendScreenshot() : AgtRunInstructionInfoResponse=" + info);
		request.setSessionId(info.getRpaRoboRunInfo().getSessionId());
		request.setJobunitId(info.getRpaRoboRunInfo().getJobunitId());
		request.setJobId(info.getRpaRoboRunInfo().getJobId());
		request.setFacilityId(info.getRpaRoboRunInfo().getFacilityId());
		request.setTriggerType(triggerType);
		long outputDate = 0L;
		try {
			// ファイルの生成時刻を使用する
			outputDate = ((FileTime) Files.getAttribute(file.toPath(), "creationTime")).toMillis();
		} catch (IOException e) {
			m_log.warn("sendScreenshot() : failed to get creationTime, " + e.getMessage(), e);
		}
		request.setOutputDate(outputDate);
		RpaScreenshotForwarder.getInstance().add(request, file);
	}

	/**
	 * スクリーンショットのファイルを削除します。<br>
	 * エージェント起動時に残ったままになっているファイルを削除するために使用します。
	 */
	public static void deleteScreenshotFiles() {
		File screenshotDir = new File(roboFileDir);

		// フォルダ未作成の場合、listFiles()はnullが返るので、チェック
		File[] files = screenshotDir.listFiles();
		if (files == null) {
			return;
		}

		for (File f : files) {
			if (f.getName().startsWith(screenshotFileNamePrefix)) {
				try {
					Files.delete(f.toPath());
					m_log.debug("delete screenshot : " + f.getName());
				} catch (IOException e) {
					m_log.warn("delete screenshot error : " + f.getName(), e);
				}
			}
		}
	}
}
