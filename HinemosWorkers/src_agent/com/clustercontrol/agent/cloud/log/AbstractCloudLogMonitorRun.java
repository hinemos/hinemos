/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.cloud.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.cloud.log.util.CloudLogRawObject;
import com.clustercontrol.agent.cloud.log.util.CloudLogfileMonitor;
import com.clustercontrol.agent.cloud.log.util.CloudLogfileMonitorManager;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.logfile.bean.LogfileLineSeparatorConstant;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * クラウドログ監視の処理実行のアブストラクトクラスです。
 */
public abstract class AbstractCloudLogMonitorRun implements Runnable {

	protected CloudLogMonitorConfig config;
	List<CloudLogRawObject> rawObject;
	protected long lastFireTime;
	private static Log log = LogFactory.getLog(AbstractCloudLogMonitorRun.class);
	protected CloudLogMonitorProperty props;
	protected boolean successFlg = false;
	protected boolean shoudRetryMissing = false;
	protected boolean hasNotifiedCNF = false;
	protected boolean hasNotifiedBDC = false;
	protected boolean hasNotifiedRNF = false;
	protected boolean hasNotifiedINC = false;
	protected boolean hasNotifiedOthers = false;
	protected boolean hasNotifiedTPE = false;
	protected int CNFcount = 0;
	protected long lastFailedTime = 0;
	protected boolean resumeFlg = false;

	public AbstractCloudLogMonitorRun(CloudLogMonitorConfig config) {
		this.config = config;
		props = CloudLogMonitorProperty.getInstance();
	}

	public void setConfig(CloudLogMonitorConfig config) {
		this.config = config;
	}

	public CloudLogMonitorConfig getConfig() {
		return this.config;
	}

	public void setLastFireTime(long lastFireTime) {
		this.lastFireTime = lastFireTime;
	}

	public long getLastFireTime() {
		return this.lastFireTime;
	}

	public boolean isSucceed() {
		return successFlg;
	}
	
	public void setResumeFlg(boolean resumeFlg){
		this.resumeFlg = resumeFlg;
	}

	/**
	 * ログの取得結果を一時ファイルに書き込み CloudLogMonitorRunAWSとRunAzureから呼ばれる
	 * 
	 * @param config
	 * @param rs
	 * @throws HinemosUnknown
	 */
	protected boolean writeToFile(CloudLogMonitorConfig config, String rs, String streamPath) throws HinemosUnknown {
		File file;
		
		// 何らかの理由で一時ファイルのディレクトリが削除されてしまっている場合再作成
		CloudLogMonitorUtil.createTmpFileDir(config.getFilePath());

		// AWSの場合はログストリームごとにファイルを分ける
		if (streamPath == null) {
			streamPath = "";
		} else {
			// ログストリーム名は:と*以外何でも許可されているので、ハッシュ化
			streamPath = "_" + DigestUtils.md5Hex(streamPath);
		}

		String filepath = String.format("%s/%s%s.tmp", config.getFilePath(), config.getMonitorId(), streamPath);
		String newFilePath = filepath + ".1";

		file = new File(filepath);
		boolean rotate = false;

		try {
			rotate = writeToFileInner(file, rs, filepath, newFilePath);
		} catch (IOException e) {
			// 以下の条件を満たすと稀にファイル監視がローテーションを検知できず、
			// ファイルの参照を持ちっぱなしになる場合がある。
			// ・1度のログ取得で1回以上ローテーションが発生する
			// ・ローテーション前とローテーション後のファイルサイズが同じ
			// ・ファイルの先頭256byteが完全一致
			// これらの条件を満たす場合にFileAlreadyExistsExceptionが出力され、ローテートに失敗するので、
			// 一度ファイル監視の参照をクローズしてから再度ローテートを試す
			// この処理によりローテートは成功するが、ファイル監視側の制約により、
			// 今回書き出された文字列によりファイルサイズが変わる、もしくは
			// ファイルの先頭が変化しない限りは監視が行われない。
			// ただし、Agent.propertiesの設定がデフォルトの場合ほぼ発生しないので、これ以上のケアは行わない。
			// (一時ファイル上限=5MBなので、1回のログ取得で10MBの全く同じ内容ログを取得しない限り発生しない）
			if (e instanceof FileAlreadyExistsException) {
				log.warn("writeToFile(): tmp file rotation failed. Clean File Monitor and try again.", e);
				CloudLogfileMonitorManager.getInstance().cleanCloudLogMonitorFiles(config.getMonitorId());
				try {
					rotate = writeToFileInner(file, rs, filepath, newFilePath);
				} catch (IOException e1) {
					log.warn("writeToFile(): tmp file rotation failed", e);
					throw new HinemosUnknown(e1);
				}
			} else {
				log.warn("writeToFile(): failed writing file", e);
				throw new HinemosUnknown(e);
			}
		}

		return rotate;

	}
	
	private boolean writeToFileInner(File file, String rs, String filepath, String newFilePath)
			throws IOException, HinemosUnknown {
		boolean rotate = false;
		boolean isSuccess = false;
		FileWriter fw = null;

		try {
			if (file.exists()) {
				// 一時ファイルの最大サイズに到達していた場合はローテート
				if (file.length() + rs.getBytes().length > CloudLogMonitorProperty.getInstance().getMaxFileSize()) {
					log.info("writeToFile: File size will exceeds maximum. Lotate");
					// 既にファイルが移動先のファイルが存在している場合は上書き
					Files.move(Paths.get(filepath), Paths.get(newFilePath), REPLACE_EXISTING);
					// 同名の新しいファイルを作成
					isSuccess = file.createNewFile();
					if (!isSuccess) {
						throw new HinemosUnknown("writeToFile(): Rotation Failed.");
					}
					rotate = true;
				}
			} else {
				isSuccess = file.createNewFile();
				if (!isSuccess) {
					throw new HinemosUnknown("writeToFile(): failed writing file");
				}
			}
			// ファイルの書き込みは常に新しいファイルに対して行う
			fw = new FileWriter(file, true);
			fw.write(rs);
			fw.flush();
		} finally {
			if (fw != null) {
				fw.close();
			}
		}
		return rotate;
	}

	/**
	 * 対応するファイル監視を取得して、監視を実行します。
	 * 
	 * @param obj
	 */
	public void execFileMonitor(List<CloudLogRawObject> obj, boolean resumeFlg) {
		List<CloudLogfileMonitor> logMngList = CloudLogfileMonitorManager.getInstance()
				.getCloudLogFileMonitor(config.getMonitorId());
		for (CloudLogfileMonitor logMng : logMngList) {
			logMng.setRawObject(obj);
			logMng.setResumeFlg(resumeFlg);
			logMng.run();
		}
	}

	/**
	 * 通信障害時の通知とリトライの管理を行います。
	 * 
	 * @param e
	 */
	protected void shouldNotifyFailure(Exception e) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

		// 規定回数以内なら次回に今回の取得範囲も含めて取得
		if (CNFcount < props.getRetryThreshold()) {
			shoudRetryMissing = true;
			lastFailedTime = lastFireTime;
			String[] args = { "" + CNFcount, "" + props.getRetryThreshold() };
			CloudLogMonitorUtil.sendMessage(config, PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
					MessageConstant.MESSAGE_CLOUD_LOG_MONITOR_CONNECTION_RETRY.getMessage(args), e.getMessage());
		} else {
			// 規定回数を超えた場合
			shoudRetryMissing = false;
			// 通知は一回のみ
			if (!hasNotifiedCNF) {
				sdf.setTimeZone(HinemosTime.getTimeZone());
				Date lastFailedDate = new Date(lastFailedTime);
				String[] args = { sdf.format(lastFailedDate) };
				CloudLogMonitorUtil.sendMessage(config, PriorityConstant.TYPE_CRITICAL,
						MessageConstant.AGENT.getMessage(),
						MessageConstant.MESSAGE_CLOUD_LOG_MONITOR_FAILED_CONNECTION.getMessage(args), e.getMessage());
				hasNotifiedCNF = true;
			}
		}
		CNFcount++;
	}
	
	/**
	 * 次回監視に持ち越すrawObjectの量を決め、切り詰めめます。
	 * 
	 */
	protected void truncateRawObjectCarryOver() {
		// 1ログが複数行にまたがり、かつ複数回クラウドからのログ取得に
		// またがるパターンを考慮し、今回取得分の最後15行分はrawObjectのまま
		// 次回監視に持ち越す
		if (props.getCarryoverLength() <= 0) {
			// 0以下が指定されていた場合は、持ち越さない
			rawObject = new ArrayList<CloudLogRawObject>();
		} else if (0 >= (rawObject.size() - props.getCarryoverLength())) {
			// 行数がプロパティの指定以下のため、すべて持ち越す
		} else {
			// プロパティの指定行数に切り詰めて持ち越す
			rawObject = new ArrayList<>(
					rawObject.subList(rawObject.size() - props.getCarryoverLength(), rawObject.size()));
		}
		
		// 持ち越されたログに対してcarryOverフラグを立てておく
		for (CloudLogRawObject r : rawObject){
			r.setCarryOver(true);
		}
		
	}

	/**
	 * 改行コードを返却します。
	 * @param returnCode
	 * @return
	 */
	protected String getReturnCodeString(String returnCode) {
		// レコード内で区切られるメッセージの改行コードを指定
		if (LogfileLineSeparatorConstant.CRLF.equals(returnCode)) {
			return "\r\n";
		} else if (LogfileLineSeparatorConstant.CR.equals(returnCode)) {
			return "\r";
		} else if (LogfileLineSeparatorConstant.LF.equals(returnCode)) {
			return "\n";
		} else {
			log.warn("getReturnCodeString(): return code specified not exists. Use LF. Specified: " + returnCode);
			return "\n";
		}
	}
	
	/**
	 * クラウドサービスへのポーリングを実施します。
	 * 
	 * @return
	 */
	@Override
	public abstract void run();

}
