/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.cloud.log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.logs.model.AWSLogsException;
import com.amazonaws.services.logs.model.FilterLogEventsRequest;
import com.amazonaws.services.logs.model.FilterLogEventsResult;
import com.amazonaws.services.logs.model.FilteredLogEvent;
import com.amazonaws.services.logs.model.InvalidParameterException;
import com.amazonaws.services.logs.model.ResourceNotFoundException;
import com.amazonaws.services.logs.model.UnrecognizedClientException;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.util.EC2MetadataUtils;
import com.amazonaws.util.EC2MetadataUtils.IAMInfo;
import com.clustercontrol.agent.cloud.log.util.CloudLogRawObject;
import com.clustercontrol.agent.cloud.log.util.CloudLogfileMonitorConfig;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.util.MessageConstant;

/**
 * クラウドログ監視で対象がAWSの場合にAWSと通信し、ログを取得するクラスです。
 */
public class CloudLogMonitorRunAWS extends AbstractCloudLogMonitorRun {

	private static Log log = LogFactory.getLog(CloudLogMonitorRunAWS.class);
	public static final Pattern iamPattern = Pattern.compile("^arn:aws:iam::(\\d+):(.+)$");

	public CloudLogMonitorRunAWS(CloudLogMonitorConfig config) {
		super(config);
		rawObject = new ArrayList<CloudLogRawObject>();
	}

	/**
	 * AWSへのポーリングを実施します。
	 * 
	 * @return
	 */
	@Override
	public void run() {
		log.debug("CloudLogMonitor for AWS Started: " + config.getMonitorId());
		successFlg = false;

		/*
		 * AWSLogsクライアントを作成する
		 */
		AWSLogs client = null;
		AWSLogsClientBuilder builder = AWSLogsClientBuilder.standard();
		// 認証情報毎にCredentialプロバイダーを作成
		try {
			builder.setCredentials(getCredentialProvider(config.getAccess(), config.getSecret()));
		} catch (Exception e1) {
			internalNotify(e1);
		}

		builder.setClientConfiguration(props.createClientConfiguration());
		builder.setRegion(config.getLocation());
		client = builder.build();

		/*
		 * Eventbridgeに送信用のリクエストを作成する
		 */
		FilterLogEventsRequest fer = new FilterLogEventsRequest();
		fer.setLogGroupName(config.getLogGroup());
		if (config.getLogSreams() != null && !config.getLogSreams().isEmpty()) {
			if (config.isPrefix) {
				fer.setLogStreamNamePrefix(config.getLogSreams().get(0));
			} else {
				fer.setLogStreamNames(config.getLogSreams());
			}
		} else {
			// ログストリームの指定がない場合は、ロググループ内すべての
			// ログストリームが監視対象になる。
		}
		
		// ログ取得遅延を反映
		long offsetInMills = config.getOffset() * 1000;
		long timeFilteredTo = CloudLogMonitorUtil.getTimeWithOffset() - offsetInMills;
		log.debug("runAWS(); Offset :" + offsetInMills);
		if (lastFireTime >= timeFilteredTo) {
			// ログ取得期間が0もしくはマイナスになる場合は、今回の監視間隔では何もしない
			log.info("runAWS(): Skipped Monitoring because log monitor duration is <= 0. LastFireTime: " + lastFireTime
					+ " timeFilteredTo: " + timeFilteredTo);
			// 一応監視成功扱い
			// フラグを立てておくと、次回lastFireTimeが更新されず、
			// 次回監視タイミングで同じlastFireTimeから監視が始まる
			successFlg = true;
			return;
		}

		// 通信障害時に前回の範囲も含めてログを取得するか確認
		if (shoudRetryMissing) {
			log.info("runAWS(): Retry to get missing log. Retry count: " + CNFcount + "Start time: " + lastFailedTime);
			// 0になることはないはずだが念のため
			if (lastFailedTime != 0) {
				lastFireTime = lastFailedTime;
			} else {
				// ここに来たとすると何らかのバグ
				log.warn("runAWS(): Logical error. LastFailedTime is 0 when it should not be");
			}
		}

		log.debug("runAWS(): filter duration. Start: " + lastFireTime + " End: " + timeFilteredTo);
		fer.setStartTime(lastFireTime);
		fer.setEndTime(timeFilteredTo);
		FilterLogEventsResult res = null;
		int resSize = 0;

		/*
		 * AWSへのポーリングを実施
		 */
		try {
			res = client.filterLogEvents(fer);
		} catch (Exception e) {
			// INTERNAL
			internalNotify(e);
			return;
		}

		// responseを文字列に変換
		try {
			resSize += getResult(config, res);
		} catch (HinemosUnknown e) {
			if (!hasNotifiedTPE) {
				CloudLogMonitorUtil.sendMessage(config, PriorityConstant.TYPE_WARNING,
						MessageConstant.AGENT.getMessage(),
						MessageConstant.MESSAGE_CLOUD_LOG_MONITOR_FAILED_TMP_FILE.getMessage(), e.getMessage());
				hasNotifiedTPE = true;
			}
			return;
		}

		// AWSの1レスポンスは256KBなので通常設定ではまず超えないが、
		// 念のため最大値を超えていないかチェック
		if (!checkLogSize(resSize)) {
			// 最大値を超えていた場合は、今取得した分までは監視
		} else {
			// まだ取得可能なログがある場合は、取得を継続
			while (true) {
				log.debug("runAWS(): Next Token exists");

				String nextT = res.getNextToken();
				if (nextT == null) {
					log.debug("runAWS(): Response end. Exit loop");
					break;
				}
				fer.setNextToken(nextT);
				try {
					res = client.filterLogEvents(fer);
				} catch (Exception e) {
					// INTERNAL
					internalNotify(e);
					break;
				}
				// 取得結果の書き込み
				try {
					resSize += getResult(config, res);
				} catch (HinemosUnknown e) {
					if (!hasNotifiedTPE) {
						CloudLogMonitorUtil.sendMessage(config, PriorityConstant.TYPE_WARNING,
								MessageConstant.AGENT.getMessage(),
								MessageConstant.MESSAGE_CLOUD_LOG_MONITOR_FAILED_TMP_FILE.getMessage(), e.getMessage());
						hasNotifiedTPE = true;
					}
					return;
				}

				// 最大取得量を超えたら終了
				if (!checkLogSize(resSize)) {
					break;
				}
			}
		}

		// ファイル監視を実行
		execFileMonitor(rawObject, resumeFlg);
		// rawObjectの切りつめ
		truncateRawObjectCarryOver();
		
		// どこまでログを取得したかを記録
		lastFireTime = timeFilteredTo + 1;

		// ここに到達したら監視成功
		successFlg = true;

		// INTERNALを出力済みの場合は、復旧をINTERNAL出力
		CloudLogMonitorUtil.notifyRecovery(config,
				hasNotifiedRNF | hasNotifiedINC | hasNotifiedBDC | hasNotifiedOthers | hasNotifiedTPE | hasNotifiedCNF);
		hasNotifiedRNF = false;
		hasNotifiedINC = false;
		hasNotifiedOthers = false;
		hasNotifiedTPE = false;
		hasNotifiedBDC = false;
		hasNotifiedCNF = false;
		shoudRetryMissing = false;
		lastFailedTime = 0;
		CNFcount = 0;
		
		return;
	}

	/**
	 * FilterLogEventsResultからログを取り出し、ファイルに書き込む
	 * 
	 * return 書き出したバイト数
	 * 
	 * @throws HinemosUnknown
	 **/
	private int getResult(CloudLogMonitorConfig config, FilterLogEventsResult res) throws HinemosUnknown {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		String retCode = "";
		int length = 0;

		// レコード内で区切られるメッセージの改行コードを指定
		retCode = getReturnCodeString(config.getReturnCode());

		StringBuilder sb = new StringBuilder();
		boolean hasRotated = false;

		for (FilteredLogEvent e : res.getEvents()) {

			// 前回取得のメッセージでローテーションされた場合は、
			// ローテーションを検知するため一度ファイル監視を実施
			// ここでローテートを検知できないと、次の一時ファイル書き込み時にローテートが発生した場合に
			// 書き込みに失敗する
			if (hasRotated) {
				log.info("getResult(): File Rotate occured while writing logs to file. Exec File Monitor.");
				try {
					// 負荷軽減とローテートを確実に検知するため
					// monitor.cloudlogfile.filter.interval指定秒数スリープ（デフォルト1秒）
					Thread.sleep(CloudLogfileMonitorConfig.getInstance().getRunInterval());
				} catch (InterruptedException e1) {
					log.warn("getResult():", e1);
				}
				execFileMonitor(rawObject, resumeFlg);
				hasRotated = false;
			}

			Date date = new Date(e.getTimestamp());
			// the format of your date
			log.debug("getResult(): New Message Date: " + sdf.format(date));
			log.debug("getResult(): New Message: " + e.getMessage());
			// 行内で改行するメッセージだった場合、
			// 今後の処理のため、改行した状態で書き込む
			// (一時ファイルの改行コードはLFで固定のため）
			// 区切り条件が改行コード以外の場合でも一時ファイルに改行コードが
			// 混在してしまわないように、処理を行う
			log.debug("getResult(): split message with " + config.getReturnCode());
			for (String mes : e.getMessage().split(retCode)) {
				String splitStr = mes + "\n";
				sb.append(splitStr);
				rawObject.add(new CloudLogRawObject(date, splitStr, e.getLogStreamName()));
			}

			// 一時ファイルへの書き込み
			// ローテートしたかを記録
			hasRotated = writeToFile(config, sb.toString(), e.getLogStreamName());
			// 読み込んだバイト数の記録
			length += sb.toString().getBytes().length;
			sb.setLength(0);
		}
		
		// ローテート直後の場合、今回のファイル監視では
		// ローテート前ファイルの最後までの読込になるので、
		// 一度ファイル監視を走らせておく
		// (そうするとローテート後ファイルへの書き出し分の監視がrun()の方で実行される
		// execFileMonitorで検知される)
		if (hasRotated) {
			log.info("getResult(): File Rotate occured after writing logs to file. Exec File Monitor.");
			execFileMonitor(rawObject, resumeFlg);
			try {
				// 負荷軽減のため、
				// monitor.cloudlogfile.filter.interval指定秒数スリープ（デフォルト1秒）
				Thread.sleep(CloudLogfileMonitorConfig.getInstance().getRunInterval());
			} catch (InterruptedException e1) {
				log.warn("getResult():", e1);
			}
		}
		return length;
	}

	/**
	 * ログのサイズを確認。最大値を超えていた場合はFalse、それ以外はTrue
	 * 
	 * @param resSize
	 * @return
	 */
	private boolean checkLogSize(int resSize) {
		// 最大取得量を超えたら終了
		// ここまで取得したログは監視対象になるが、以降は破棄
		if (resSize > props.getMaxLogsize()) {
			log.warn("runAWS(): too many logs length" + resSize);
			String[] args = { "" + resSize, "" + props.getMaxLogsize() };
			CloudLogMonitorUtil.sendMessage(config, PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
					MessageConstant.MESSAGE_CLOUD_LOG_MONITOR_FAILED_TOO_MANY_LOGS.getMessage(args), "");
			return false;
		}
		return true;
	}

	/**
	 * 適切なAWSCredentialsProviderを返却します。
	 * 
	 * @param accessKey
	 * @param secretKey
	 * @return
	 * @throws UnrecognizedClientException
	 */
	private AWSCredentialsProvider getCredentialProvider(String accessKey, String secretKey)
			throws UnrecognizedClientException {

		// インスタンスのIAMロールで接続
		if (accessKey.equals("@LOCAL")) {
			if (EC2MetadataUtils.getIAMInstanceProfileInfo() == null) {
				throw new UnrecognizedClientException("No IAM role has been assigned to this instance.");
			}
			return new InstanceProfileCredentialsProvider(false);
			// 指定したアカウントのIAMロールで接続（クロスアカウント）
		} else if (accessKey.startsWith("@")) {
			Matcher iam = Pattern.compile("^@(\\d+):(.+)$").matcher(accessKey);
			String arn = "";
			if (iam.matches()) {
				// IAM ARN = arn:aws:iam::[accountid]:role/[roleid]
				arn = "arn:aws:iam::" + iam.group(1) + ":role/" + iam.group(2);
			} else {
				// 解析できない形式の場合は後続の処理ができないため、例外を出力して終了する
				throw new UnrecognizedClientException(
						"AccessKey is invalid pattern. @AccountID:RoleID (accessKey you have typed=" + accessKey + ")");
			}

			String roleSessionName = EC2MetadataUtils.getInstanceId() + "@";
			IAMInfo iamInfo = EC2MetadataUtils.getIAMInstanceProfileInfo();
			if (iamInfo == null) {
				throw new UnrecognizedClientException("No IAM role has been assigned to this instance.");
			}
			Matcher iamArn = iamPattern.matcher(iamInfo.instanceProfileArn);
			if (iamArn.matches()) {
				roleSessionName += iamArn.group(1);
			}
			AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
					.withCredentials(new InstanceProfileCredentialsProvider(false))
					.withClientConfiguration(props.createClientConfiguration()).build();

			AssumeRoleRequest roleRequest = new AssumeRoleRequest().withRoleArn(arn)
					.withRoleSessionName(roleSessionName);
			Credentials tmpCredential = stsClient.assumeRole(roleRequest).getCredentials();

			BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(
					tmpCredential.getAccessKeyId(), tmpCredential.getSecretAccessKey(),
					tmpCredential.getSessionToken());
			// endpoint が有効な限り一時的な認証情報を更新する
			return new AWSStaticCredentialsProvider(basicSessionCredentials);
			// 永続的なアクセスキーとシークレットキーを利用する
		} else {
			BasicAWSCredentials awsCledential = new BasicAWSCredentials(accessKey, secretKey);
			return new AWSStaticCredentialsProvider(awsCledential);
		}
	}

	/**
	 * 例外の種別により送信するINTERNALを振り分けます
	 * 
	 * @param e
	 */
	private void internalNotify(Exception e) {

		if (e instanceof ResourceNotFoundException) {
			// 失敗時にはマネージャに通知
			log.warn("runAWS() resource not found " + e.getMessage());
			// マネージャに通知
			if (!hasNotifiedRNF) {
				String[] args = { config.getLogGroup(), Arrays.toString(config.getLogSreams().toArray()) };
				CloudLogMonitorUtil.sendMessage(config, PriorityConstant.TYPE_WARNING,
						MessageConstant.AGENT.getMessage(),
						MessageConstant.MESSAGE_CLOUD_LOG_MONITOR_FAILED_NO_RESOURCE.getMessage(args), e.getMessage());
				hasNotifiedRNF = true;
			}

		} else if (e instanceof InvalidParameterException) {
			log.warn("runAWS(): Invalid setting:", e);
			if (!hasNotifiedBDC) {
				CloudLogMonitorUtil.sendMessage(config, PriorityConstant.TYPE_WARNING,
						MessageConstant.AGENT.getMessage(),
						MessageConstant.MESSAGE_CLOUD_LOG_MONITOR_BAD_CONFIG.getMessage(), e.getMessage());
				hasNotifiedBDC = true;
			}

		} else if (e instanceof AWSLogsException) {
			// 認証情報に誤りがあるか確認
			// UnrecognizedClientExceptionは、アクセスキーなどに誤りがある際に発生
			// 適切な権限がない場合、AWSのAPIからのError CodeはAccessDeniedExceptionになるが、
			// AWSLogsExceptionにはAccessDeniedExceptionが用意されていないので、
			// メッセージのError CodeにAccessDeniedExceptionが記載されているか確認
			if (e instanceof UnrecognizedClientException
					|| (e.getMessage() != null && e.getMessage().contains("AccessDeniedException"))) {
				log.warn("runAWS() invalid credential ", e);
				// マネージャに通知
				if (!hasNotifiedINC) {
					CloudLogMonitorUtil.sendMessage(config, PriorityConstant.TYPE_WARNING,
							MessageConstant.AGENT.getMessage(),
							MessageConstant.MESSAGE_CLOUD_LOG_MONITOR_FAILED_INVALID_CREDENTIAL.getMessage(),
							e.getMessage());
					hasNotifiedINC = true;
				}
			} else {
				// ここにたどり着いた場合、一時的にAWSのサービスがリクエストを受け付けられない状態や、
				// quotaに引っかかっている可能性があるので、リトライ
				// AWSに接続できなかった扱いとする
				log.warn("runAWS() Failed to connect to AWS ", e);
				shouldNotifyFailure(e);
			}

		} else if (e instanceof SdkClientException) {
			// 通知とリトライ判断
			log.warn("runAWS() Failed to connect to AWS ", e);
			shouldNotifyFailure(e);
		} else {
			log.warn("runAWS() unknown error ", e);
			// マネージャに通知
			if (!hasNotifiedOthers) {
				CloudLogMonitorUtil.sendMessage(config, PriorityConstant.TYPE_WARNING,
						MessageConstant.AGENT.getMessage(),
						MessageConstant.MESSAGE_CLOUD_LOG_MONITOR_FAILED_UNKNOWN.getMessage(), e.getMessage());
				hasNotifiedOthers = true;
			}

		}
	}

}
