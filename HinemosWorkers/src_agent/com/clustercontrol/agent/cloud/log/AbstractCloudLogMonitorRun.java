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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.cloud.log.util.CloudLogfileMonitor;
import com.clustercontrol.agent.cloud.log.util.CloudLogfileMonitorConfig;
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
	protected Map<String, CloudLogMonitorStatus> statusMap = new HashMap<String, CloudLogMonitorStatus>();

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
	public void execFileMonitor() {
		List<CloudLogfileMonitor> logMngList = CloudLogfileMonitorManager.getInstance()
				.getCloudLogFileMonitor(config.getMonitorId());

		// 日時情報取得エラー用
		String orgMessage = "";
		for (CloudLogfileMonitor logMng : logMngList) {
			
			setStreamNameForCloudLogfileMonitor(logMng);
			logMng.run();
			// 日時情報取得失敗のINTERNAL用
			if (!logMng.getOrgMessageForDateNotFound().isEmpty()) {
				orgMessage += logMng.getOrgMessageForDateNotFound() + "\n";
			}
			logMng.resetDateNotFoundNotify();
		}

		if (!orgMessage.isEmpty()) {
			String msg = "execFileMonitor(): date info not found for file monitor.";
			// 通知を行う際の上限は4096文字を上限にする
			if (orgMessage.length() > 4096) {
				orgMessage = orgMessage.substring(0, 4096);
				msg += " orgMessage truncated at length of 4096.";
				
			}
			log.warn(msg);
			CloudLogMonitorUtil.sendMessage(config, PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
					MessageConstant.MESSAGE_CLOUD_LOG_MONITOR_DATE_NOT_FOUND.getMessage(), orgMessage);
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
	 * 取得した文字列を区切り条件に従って区切ります。
	 * 区切り条件に合致しない文字列は区切り条件に合致するまで
	 * CloudLogMonitorStatusに保存されます。
	 * 
	 * @param splitStr 改行コードで区切られたログ
	 * @param timestamp ログのタイムスタンプ
	 * @param fileKey ログの取得元(AWSならログストリーム名、Azureの場合監視設定ID)
	 * @return 区切り条件で区切られた文字列
	 */
	protected String getSplitStr(String splitStr, String timestamp, String fileKey) {

		log.debug("getSplitStr(): original message :" + splitStr);

		String appendbuf = "";
		CloudLogMonitorStatus status = statusMap.get(fileKey);

		// プロパティファイルが存在しない場合は初期化
		if (status == null) {
			String fileName = fileKey;
			// プロパティファイル名を取得
			fileName = getPropFileName(fileKey);
			
			File filepath = new File(CloudLogMonitorUtil.getPropFileStorePath(config.getMonitorId()), fileName);
			status = new CloudLogMonitorStatus(config.getMonitorId(), fileKey, filepath);

			// #16879パッチ適用直後にはファイル監視側のキャリーオーバが残っている可能性があるため、
			// ここで処理を行う
			boolean isInitializedWithCarryover = false;
			isInitializedWithCarryover = isInitializedWithCarryover(status, fileKey, fileName);
			status.setInitializedWithCarryover(isInitializedWithCarryover);
			status.setInitializedWithFileCreation(false);

			statusMap.put(fileKey, status);
		}
		// キャリーオーバに今回取得文字列をアペンド
		appendbuf = status.getCarryOver() + splitStr;
		log.debug("getSplitStr(): message with carryover appended :" + appendbuf);

		// 後続処理用にステータス管理側に保存されていたキャリーオーバを保存
		String orgCarryover = status.getCarryOver();

		// 日時情報がない場合は現在のログの日時情報を追加
		if (status.getDate().isEmpty()) {
			status.setLongest(splitStr);
			status.setDate(timestamp);
		}

		if (log.isDebugEnabled()) {
			log.debug("getSplitStr(): longest line :" + status.getLongest());
			log.debug("getSplitStr(): longest date :" + status.getDate());
		}
		
		// 区切り文字に該当するか？
		// 改行コードはここに来る前に区切られているので、ここではLF固定
		CloudLogLineSeparator sep = new CloudLogLineSeparator(LogfileLineSeparatorConstant.LF, config.getPatternHead(),
				config.getPatternTail());
		boolean lfFlag = false;
		if ((config.getPatternTail() == null || config.getPatternTail().isEmpty())
				&& (config.getPatternHead() == null || config.getPatternHead().isEmpty())) {
			lfFlag = true;
		}

		StringBuilder splittedStr = new StringBuilder();
		boolean found = false;

		// 今回のログが区切り条件に一致する場合、
		// 今回取得のログが一番長い行の可能性があるので、区切り文字で区切って保持しておく
		int orgInd = sep.searchInitialMatch(splitStr);
		String splittedOrgStr = "";
		// 区切りが先頭パターンで今回取得ログの先頭がマッチした場合は無視(-1になる)
		if (orgInd != -1) {
			splittedOrgStr = splitStr.substring(0, orgInd);
		}

		// 一行が複数の区切り条件に分かれる場合を考慮し、区切り条件が見つかる限りループ
		while (true) {
			int ind = sep.search(appendbuf);
			String toWrite = "";

			// 区切り条件が見つからなかった場合
			if (ind == -1) {
				// 区切り条件に合致しないログの中で一番長い行になる場合、日時情報を更新
				if (status.getLongest().length() < splitStr.length()) {
					status.setLongest(splitStr);
					status.setDate(timestamp);
				}
				break;
			} else {
				// 区切り条件に一致した場合
				String matchedStr = appendbuf.substring(0, ind);
				log.debug("getSplitStr(): splitted message :" + matchedStr);

				if (lfFlag) {
					if (!status.isInitializedWithCarryover()) {
						// 区切り条件がLFの場合、区切り条件変更直後に
						// キャリーオーバーに区切り条件変更前の情報が残っている可能性がある。
						// 残っていた場合は日時情報は過去取得時のものにする
						if (orgCarryover.contains(matchedStr)) {
							toWrite = status.getDate() + "," + matchedStr;
							// キャリーオーバ分の処理が完了したかを確認するため、マッチした分は削除しておく
							orgCarryover = orgCarryover.replace(matchedStr, "");
						} else {
							toWrite = timestamp + "," + matchedStr;
						}
					} else {
						// パッチ適用後の初回ログ取得時は日時情報を追記しない
						toWrite = "," + matchedStr;
						status.setInitializedWithCarryover(false);
						log.info("getSplitStr(): date info omitted since this is first log after patch applied. Line :"
								+ toWrite);
					}
				} else {
					if (found) {
						// 区切り条件内で2度目の場合、キャリーオーバー分の処理は完了しているはずなので、
						// 今回取得したログのタイムスタンプを使用する。
						status.setLongest(splitStr);
						status.setDate(timestamp);
					} else {
						// 過去取得のログと今回取得のログどちらが長いか確認しておく
						// 今回取得のログを区切った要素のほうが長ければ、今回取得分を使用
						log.debug("getSplitStr(): original message splitted :" + splittedOrgStr);
						if (status.getLongest().length() < splittedOrgStr.length()) {
							status.setLongest(splittedOrgStr);
							status.setDate(timestamp);
						}
					}
					if (!status.isInitializedWithCarryover()) {
						// 日時情報を書き出す文字列に付与
						toWrite = status.getDate() + "," + appendbuf.substring(0, ind);
					} else {
						// パッチ適用後の初回ログ取得時は日時情報を追記しない
						toWrite = "," + appendbuf.substring(0, ind);
						status.setInitializedWithCarryover(false);
						log.info("getSplitStr(): date info omitted since this is first log after patch applied. Line :"
								+ toWrite);

					}

				}
				// 改行コードが残っている可能性があるので除去して末尾にLFを付与
				splittedStr.append(toWrite.replaceAll("\\r\\n|\\r|\\n", "") + "\n");

				// 区切り条件後に残る文字列を保存
				appendbuf = appendbuf.substring(ind, appendbuf.length());
				log.debug("getSplitStr(): remained message :" + appendbuf);

				found = true;

			}
		}

		// 区切り条件に一致しなかった文字列をキャリーオーバに設定
		status.setCarryOver(appendbuf);
		// キャリーオーバーが上限値を超えていたら切り詰め
		truncateCarryOverLength(status, fileKey);

		if (found) {
			// 区切りが見つかったらキャリーオーバに残っている最長行で、
			// 日時情報を更新する
			String longest = "";
			// 改行文字を含めて区切る
			for (String s : status.getCarryOver().split("(?<=\n)")) {
				if (s.length() > longest.length()) {
					longest = s;
				}
			}
			status.setLongest(longest);
			status.setDate(timestamp);

		}

		return splittedStr.toString();
	}
	
	/**
	 *  CloudLogMonitorStatusのキャリーオーバの長さが上限に達していないかを確認します。
	 *  上限に達していた場合は上限値で切り詰めます。
	 *  キャリーオーバの上限はファイル監視のキャリーオーバの上限と共通です。
	 * @param status
	 * @param fileKey
	 */
	private void truncateCarryOverLength(CloudLogMonitorStatus status, String fileKey) {

		if (status.getCarryOver().length() > CloudLogfileMonitorConfig.getInstance().getFileReadCarryOverLength()) {
			String message = "truncateCarryOverLength() : " + fileKey + " carryOverBuf size = "
					+ status.getCarryOver().length()
					+ ". carryOverBuf is too long. it cut down .(see monitor.cloudlogfile.read.carryover.length)";

			log.info(message);

			status.setCarryOver(status.getCarryOver().substring(0,
					CloudLogfileMonitorConfig.getInstance().getFileReadCarryOverLength()));
		}
	}
	
	private boolean isInitializedWithCarryover(CloudLogMonitorStatus status, String fileKey, String fileName) {
		if (status.isInitializedWithFileCreation()) {
			log.debug("getSplitStr(): initial run for file key: " + fileKey);
			List<CloudLogfileMonitor> monList = CloudLogfileMonitorManager.getInstance()
					.getCloudLogFileMonitor(config.getMonitorId());

			// 今回ログを取得した設定（ログストリーム）に対応するファイル監視を検索
			for (CloudLogfileMonitor mon : monList) {

				// プロパティファイルがないのにもかかわらずファイル監視側のキャリーオーバが
				// 存在する場合、パッチ適用前のキャリーオーバが残っていると判断
				if (mon.getFilePathForStream().contains(fileName)) {
					String carryover = mon.getCarryover();

					if (carryover.isEmpty()) {
						// ファイル監視は存在するがキャリーオーバがない場合
						// パッチ適用前の区切り条件が改行か、もしくはステータス管理ファイルが破損していた場合に
						// ここに到達する可能性あり
						log.debug("getSplitStr(): File Monitor exists but without carryover. ");
						return false;
					}

					log.info("getSplitStr(): initial run with carryover. Use current log timestamp. file key: "
							+ fileKey);
					log.debug("getSplitStr(): Appended carryover: " + carryover);

					// ファイル監視側のキャリーオーバをプロパティファイルのキャリーオーバにセット
					// これにより今回監視時に適切な区切り文字でパッチ適用前のキャリーオーバが処理される
					// ただし、必ず正しい日時情報が取得できるとは限らないので、日時情報は取得しない。
					status.setCarryOver(carryover);
					mon.clearCarryover();
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * クラウドサービスへのポーリングを実施します。
	 * 
	 * @return
	 */
	@Override
	public abstract void run();
	
	protected abstract String getPropFileName(String fileKey);
	
	protected abstract void setStreamNameForCloudLogfileMonitor(CloudLogfileMonitor mon);

}
