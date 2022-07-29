/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.rpa.scenariojob;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtRunInstructionInfoResponse;
import org.openapitools.client.model.ForwardRpaScreenshotRequest;
import org.openapitools.client.model.SetJobResultRequest;
import org.openapitools.client.model.SetJobResultRequest.RpaJobErrorTypeEnum;

import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.agent.SendQueue.JobResultSendableObject;
import com.clustercontrol.agent.job.AgentThread;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.rpa.bean.RoboScreenshotInfo;
import com.clustercontrol.jobmanagement.rpa.util.RoboFileManager;
import com.clustercontrol.jobmanagement.rpa.util.RpaWindowsUtil;
import com.clustercontrol.util.HinemosTime;

public class ScreenshotThread extends AgentThread {

	/** ロガー */
	private static Log m_log = LogFactory.getLog(ScreenshotThread.class);

	/** RPAツールエグゼキューター連携用ファイル管理オブジェクト */
	private static RoboFileManager roboFileManager;

	/** スクリーンショット作成タイムアウト（ミリ秒） */
	private static int screenshotTimeout = 60000; // 60sec

	/** セッションがアクティブ（デスクトップログイン中）なユーザー名 **/
	private String activeUserName = null;

	static {
		// スクリーンショット作成タイムアウト設定
		try {
			String v = AgentProperties.getProperty("job.rpa.screenshot.timeout", Integer.toString(screenshotTimeout));
			screenshotTimeout = Integer.parseInt(v);
		} catch (Exception e) {
			m_log.warn("ScreenshotThread : " + e.getMessage(), e);
		}
		m_log.info("screenshotTimeout=" + screenshotTimeout);
	}


	/**
	 * コンストラクタ
	 * 
	 * @param info
	 * @param sendQueue
	 */
	public ScreenshotThread(AgtRunInstructionInfoResponse info, SendQueue sendQueue, String userName) {
		super(info, sendQueue);
		this.activeUserName = userName;
		this.setName(this.getClass().getSimpleName());
		this.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				m_log.error("uncaughtException() : " + e.getMessage(), e);
			}
		});

		// ディレクトリ確認、作成
		File dir = null;
		try {
			dir = new File(RpaWindowsUtil.getScreenshotDirPath());
		} catch (HinemosUnknown | InterruptedException e) {
			m_log.warn("ScreenshotThread() : error occurred. e=" + e.getMessage(), e);
		}
		if (dir != null && !dir.exists()) {
			m_log.warn("ScreenshotThread() : make directories. dir=" + dir);
			dir.mkdirs();
		}
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
		if (takeScreenshot()) {
			m_log.debug("run() : success to take screenshot");
		} else {
			m_log.debug("run() : fail to take screenshot");
			sendMessage(RunStatusConstant.ERROR);
		}

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
	 * 
	 * @return 実行成否
	 */
	public boolean takeScreenshot() {
		// スクリーンショット取得指示ファイルの生成
		String screenshotFileName = String.format(RpaWindowsUtil.getScreenshotFileName(), HinemosTime.currentTimeMillis());
		RoboScreenshotInfo screenshotInfo = new RoboScreenshotInfo();
		screenshotInfo.setDatetime(m_info.getRpaRoboRunInfo().getDatetime());
		screenshotInfo.setSessionId(m_info.getRpaRoboRunInfo().getSessionId());
		screenshotInfo.setJobunitId(m_info.getRpaRoboRunInfo().getJobunitId());
		screenshotInfo.setJobId(m_info.getRpaRoboRunInfo().getJobId());
		screenshotInfo.setFacilityId(m_info.getRpaRoboRunInfo().getFacilityId());
		screenshotInfo.setUserName(this.activeUserName);
		screenshotInfo.setScreenshotFileName(screenshotFileName);
		m_log.debug("takeScreenshot() : screenshotInfo=" + screenshotInfo);

		try {
			roboFileManager = new RoboFileManager();
			// RPAツールエグゼキューターへの実行指示
			// Everyoneのフルアクセスで設定
			roboFileManager.writeWithEveryoneFullAccess(screenshotInfo);
			// 指示受付確認待ち
			boolean retShotInst = roboFileManager.confirmDelete(RoboScreenshotInfo.class, true,
					ScenarioMonitorThread.instConfirmInterval, ScenarioMonitorThread.instConfirmTimeout);
			if (!retShotInst) {
				m_log.warn("takeScreenshot() : The screen shot instruction may not have been communicated to the executor. session=" + m_info.getSessionId());
			}

			// スクリーンショットが生成するまで待機
			long start = HinemosTime.currentTimeMillis();
			File file = new File(RpaWindowsUtil.getScreenshotDirPath(), screenshotFileName);
			m_log.debug("takeScreenshot() : screen shot file=" + file);
			while (!file.exists()) {
				m_log.debug("takeScreenshot() : file not exists, file=" + file);
				long elapsedTime = HinemosTime.currentTimeMillis() - start;
				m_log.debug("takeScreenshot() : waiting " + elapsedTime + "ms for login until " + screenshotTimeout + "ms");
				if (elapsedTime > screenshotTimeout) {
					// 待機時間を超えた場合は終了する
					m_log.error("takeScreenshot() : failed to take screen shot, waiting " + elapsedTime + "ms for login over " + screenshotTimeout + "ms");
					sendErrorMessage(RpaJobErrorTypeEnum.SCREENSHOT_FAILED);
					return false;	
				}
				Thread.sleep(ScenarioMonitorThread.checkInterval);
			}

			// スクリーンショットが生成したらマネージャへ転送
			sendScreenshot(m_info, file,
					ForwardRpaScreenshotRequest.TriggerTypeEnum.fromValue(m_info.getRpaScreenshotTriggerType().getValue()));
		} catch (HinemosUnknown | IOException e) {
			m_log.error("takeScreenshot() : error occurred. e=" + e.getMessage(), e);
			sendErrorMessage(RpaJobErrorTypeEnum.SCREENSHOT_FAILED);
			return false;
		} catch (InterruptedException e) {
			// 中断された場合
			m_log.debug("takeScreenshot() : interrupted. e=" + e.getMessage(), e);
			return false;
		}
		
		return true;
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
	 * エラーメッセージを送信します。
	 * 
	 * @param errorType
	 */
	private void sendErrorMessage(SetJobResultRequest.RpaJobErrorTypeEnum errorType) {
		sendErrorMessage(errorType, "");
	}

	/**
	 * エラーメッセージを送信します。
	 * 
	 * @param errorType
	 * @param errorMessage
	 */
	private void sendErrorMessage(SetJobResultRequest.RpaJobErrorTypeEnum errorType, String errorMessage) {
		JobResultSendableObject jobResult = new JobResultSendableObject();
		jobResult.sessionId = m_info.getSessionId();
		jobResult.jobunitId = m_info.getJobunitId();
		jobResult.jobId = m_info.getJobId();
		jobResult.facilityId = m_info.getFacilityId();
		jobResult.body = new SetJobResultRequest();
		jobResult.body.setCommand(m_info.getCommand());
		jobResult.body.setCommandType(m_info.getCommandType());
		jobResult.body.setStatus(RunStatusConstant.ERROR);
		jobResult.body.setRpaJobErrorType(errorType);
		jobResult.body.setTime(HinemosTime.getDateInstance().getTime());
		jobResult.body.setErrorMessage(errorMessage);

		m_log.info("sendJobErrorMessage() : SessionID=" + jobResult.sessionId + ", JobID=" + jobResult.jobId + ", ErrorType=" + errorType);
		// 送信
		m_sendQueue.put(jobResult);
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
		File screenshotDir;
		try {
			screenshotDir = new File(RpaWindowsUtil.getScreenshotDirPath());
		} catch (HinemosUnknown | InterruptedException e) {
			m_log.warn("deleteScreenshotFiles() : error occurred. e=" + e.getMessage(), e);
			return;
		}

		// フォルダ未作成の場合、listFiles()はnullが返るので、チェック
		File[] files = screenshotDir.listFiles();
		if (files == null) {
			m_log.debug("deleteScreenshotFiles() : files is null");
			return;
		}

		for (File f : files) {
			if (f.getName().startsWith(RpaWindowsUtil.getScreenshotFileNamePrefix())) {
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
