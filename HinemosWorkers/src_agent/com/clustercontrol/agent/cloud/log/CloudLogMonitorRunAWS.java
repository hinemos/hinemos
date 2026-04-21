/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.cloud.log;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtMonitorInfoResponse;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.document.Document;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.imds.Ec2MetadataClient;
import software.amazon.awssdk.imds.Ec2MetadataResponse;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.AccessDeniedException;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilteredLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.UnrecognizedClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.internal.util.EC2MetadataUtils;

import com.clustercontrol.agent.cloud.log.util.CloudLogfileMonitor;
import com.clustercontrol.agent.cloud.log.util.CloudLogfileMonitorConfig;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.util.MessageConstant;

/**
 * クラウドログ監視で対象がAWSの場合にAWSと通信し、ログを取得するクラスです。
 */
public class CloudLogMonitorRunAWS extends AbstractCloudLogMonitorRun {

	private static Log log = LogFactory.getLog(CloudLogMonitorRunAWS.class);
	private static final String EXPIRED_TOKEN_ERRORCODE = "ExpiredTokenException";

	public CloudLogMonitorRunAWS(CloudLogMonitorConfig config) {
		super(config);
	}

	@Override
	public void setConfig(CloudLogMonitorConfig config) {
		// 新しく指定された監視設定が、古い監視設定と証明書のキャッシュのキーを構成する情報が異なる場合、作成済みのトークンを削除する
		CredentialsProviderCache.ScopeAccessKey key = providerCache.getScopeAccessKey(this.config.getMonitorId());
		if (key != null) {
			if (config.getMonInfo() != null) {
				if (!key.scope().equals(config.getMonInfo().getScope())) {
					providerCache.remove(this.config.getMonitorId());
				} else {
					if (!key.accessKey().equals(config.getAccess())) {
						providerCache.remove(this.config.getMonitorId());
					}
				}
			} else {
				providerCache.remove(this.config.getMonitorId());
			}
		}
		this.config = config;
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
		 * CloudWatchLogsClientクライアントを作成する
		 */
		CloudWatchLogsClient client = null;
		try {
			client = buildAWSLogsClient();
		} catch (Exception e) {
			internalNotify(e);
			return;
		}

		/*
		 * Eventbridgeに送信用のリクエストを作成する
		 */
		FilterLogEventsRequest.Builder ferBuild = FilterLogEventsRequest.builder()
				.logGroupName(config.getLogGroup());
		if (config.getLogSreams() != null && !config.getLogSreams().isEmpty()) {
			if (config.isPrefix) {
				ferBuild.logStreamNamePrefix(config.getLogSreams().get(0));
			} else {
				ferBuild.logStreamNames(config.getLogSreams());
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
		ferBuild.startTime(lastFireTime);
		ferBuild.endTime(timeFilteredTo);
		FilterLogEventsResponse res = null;
		int resSize = 0;

		/*
		 * AWSへのポーリングを実施
		 */
		try {
			res = filterLogEvents(client, ferBuild.build());
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

				String nextT = res.nextToken();
				if (nextT == null) {
					log.debug("runAWS(): Response end. Exit loop");
					break;
				}
				ferBuild.nextToken(nextT);
				try {
					res = filterLogEvents(client, ferBuild.build());
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
		execFileMonitor();
		
		// ステータス管理ファイルの更新
		for (CloudLogMonitorStatus status : statusMap.values()) {
			status.store();
		}

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
	private int getResult(CloudLogMonitorConfig config, FilterLogEventsResponse res) throws HinemosUnknown {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		String retCode = "";
		int length = 0;

		// レコード内で区切られるメッセージの改行コードを指定
		retCode = getReturnCodeString(config.getReturnCode());

		StringBuilder sb = new StringBuilder();
		boolean hasRotated = false;
		
		for (FilteredLogEvent e : res.events()) {

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
				execFileMonitor();
				hasRotated = false;
			}

			Date date = new Date(e.timestamp());
			// the format of your date
			log.debug("getResult(): New Message Date: " + sdf.format(date));
			log.debug("getResult(): New Message: " + e.message());
			// 行内で改行するメッセージだった場合、
			// 今後の処理のため、改行した状態で書き込む
			// (一時ファイルの改行コードはLFで固定のため）
			// 区切り条件が改行コード以外の場合でも一時ファイルに改行コードが
			// 混在してしまわないように、処理を行う
			log.debug("getResult(): split message with " + config.getReturnCode());
			
			for (String mes : e.message().split(retCode)) {
				String splitStr = mes + "\n";
				String timestamp = e.timestamp() + "";
				sb.append(getSplitStr(splitStr, timestamp, e.logStreamName()));
				
			}
			
			// 一時ファイルへの書き込み
			// ローテートしたかを記録
			hasRotated = writeToFile(config, sb.toString(), e.logStreamName());
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
			execFileMonitor();
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
	 * CloudWatchLogsClientクライアントを作成する。認証エラーとなったらnullを返す。
	 * 
	 * @return CloudWatchLogsClientクライアント
	 */
	private CloudWatchLogsClient buildAWSLogsClient() {
		CloudWatchLogsClient client = CloudWatchLogsClient.builder()
				.credentialsProvider(getCredentialProvider(config.getAccess(), config.getSecret()))
				.region(Region.of(config.getLocation()))
				.httpClient(props.createHttpClient())
				.overrideConfiguration(props.createOverrideConfiguration())
				.build();

		return client;
	}

	/**
	 * 適切なAWSCredentialsProviderを返却します。
	 * 
	 * @param accessKey
	 * @param secretKey
	 * @return
	 * @throws UnrecognizedClientException
	 */
	private AwsCredentialsProvider getCredentialProvider(String accessKey, String secretKey)
			throws UnrecognizedClientException {

		// インスタンスのIAMロールで接続
		if (accessKey.equals("@LOCAL")) {
		    try {
		        String instanceProfileArn = getInstanceProfileArn();
		        if (instanceProfileArn == null || instanceProfileArn.isEmpty()) {
		            throw UnrecognizedClientException.builder()
		                    .message("No IAM role has been assigned to this instance.")
		                    .build();
		        }
		    } catch (SdkClientException e) {
		        throw UnrecognizedClientException.builder()
		                .message("No IAM role has been assigned to this instance.")
		                .build();
		    }
			return InstanceProfileCredentialsProvider.create();
		// 指定したアカウントのIAMロールで接続（クロスアカウント）
		} else if (accessKey.startsWith("@")) {
			StsAssumeRoleCredentialsProvider awsCledential = 
					providerCache.getCredentialsProvider(config, props.createHttpClient(), props.createOverrideConfiguration());
			return awsCledential;
		// 永続的なアクセスキーとシークレットキーを利用する
		} else {
			return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
		}
	}

	public static String getInstanceProfileArn() {
		
		Ec2MetadataClient imdc = Ec2MetadataClient.create();
		Ec2MetadataResponse response = imdc.get("/latest/meta-data/iam/info");
		Document instanceInfo = response.asDocument();
		
		return instanceInfo.asMap().get("InstanceProfileArn").asString();
	}
	
	/*
	 * filterLogEvents()を実行し、トークンの有効期限のエラーであれば再実行する。
	 * 
	 * @param client
	 * @param request
	 * @return
	 */
	private FilterLogEventsResponse filterLogEvents(CloudWatchLogsClient client, FilterLogEventsRequest request) {
		FilterLogEventsResponse res;
		try {
			res = client.filterLogEvents(request);
		} catch (ResourceNotFoundException e) {
			// ResourceNotFoundExceptionは監視対象のログストリームが存在しない場合に発生し、認証エラーではないためトークンの削除はしない
			throw e;
		} catch (CloudWatchLogsException e) {
			// 例外発生時は、トークンのキャッシュを削除
			providerCache.forceRemove(config.getMonitorId());

			// トークンの期限切れの場合、トークンを強制削除した後、再実行
			if (EXPIRED_TOKEN_ERRORCODE.equals(e.awsErrorDetails().errorCode())) {
				log.info("runAWS(): Credential expired, scope : " + config.getMonInfo().getScope());
				try {
					res = buildAWSLogsClient().filterLogEvents(request);
				} catch (Exception e1) {
					// 再び例外が発生時したので、トークンのキャッシュから削除する
					providerCache.forceRemove(config.getMonitorId());
					throw e1;
				}
			} else {
				// トークン期限切れ以外は再実行しない
				throw e;
			}
		} catch (Exception e) {
			// 例外発生時は、トークンのキャッシュを削除
			providerCache.forceRemove(config.getMonitorId());
			throw e;
		}
		return res;

	}

	/*
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

		} else if (e instanceof CloudWatchLogsException) {
			// 認証情報に誤りがあるか確認
			// UnrecognizedClientExceptionは、アクセスキーなどに誤りがある際に発生
			// 適切な権限がない場合、AWSのAPIからのError CodeはAccessDeniedExceptionになるが、
			// AWSLogsExceptionにはAccessDeniedExceptionが用意されていないので、
			// メッセージのError CodeにAccessDeniedExceptionが記載されているか確認
			if (e instanceof UnrecognizedClientException
					|| e instanceof AccessDeniedException) {
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

	/**
	 * プロパティファイル名を取得するメソッド
	 * AWSの場合、ログストリーム名をMD5でハッシュしたものを返す
	 */
	@Override
	protected String getPropFileName(String fileKey) {
		return DigestUtils.md5Hex(fileKey);
	}

	/**
	 * CloudLogfileMonitorにログストリーム名を設定するメソッド
	 */
	@Override
	protected void setStreamNameForCloudLogfileMonitor(CloudLogfileMonitor mon) {
		String streamName = "";

		for (String key : statusMap.keySet()) {
			if (mon.getFilePathForStream().contains(DigestUtils.md5Hex(key))) {
				streamName = key;
				break;
			}
		}
		mon.setLogStreamName(streamName);

	}

	@Override
	public void shutdown() {
		providerCache.remove(config.getMonitorId());
	}
	
	/*
	 * CredentialsProviderCache内でStsAssumeRoleCredentialsProviderを作成
	 * CredentialsProviderCacheでStsAssumeRoleCredentialsProviderの作成のためのロック取得前に、時間がかかるEC2MetadataUtilsの呼出しを分離しておく
	 */
	private static class Factory implements CredentialsProviderCache.CredentialProviderFactory {
		private static final Pattern iamArnPattern = Pattern.compile("^arn:aws:iam::(\\d+):.+$");
		private static final Pattern accessKeyPattern = Pattern.compile("^@(\\d+):(.+)$");

		/*
		 * StsAssumeRoleCredentialsProviderの作成
		 */
		private static class Plan implements CredentialsProviderCache.StsProviderPlan {
			private final String roleArn;
			private final String roleSessionName;
			private final SdkHttpClient sdkHttpClient;
			private final ClientOverrideConfiguration clientOverrideConfiguration;

			private Plan(String roleArn, String roleSessionName, SdkHttpClient sdkHttpClient, ClientOverrideConfiguration clientOverrideConfiguration) {
				this.roleArn = roleArn;
				this.roleSessionName = roleSessionName;
				this.sdkHttpClient = sdkHttpClient;
				this.clientOverrideConfiguration = clientOverrideConfiguration;
			}

			@Override
			public StsAssumeRoleCredentialsProvider instantiate() {
				AssumeRoleRequest req = AssumeRoleRequest.builder().roleArn(roleArn).roleSessionName(roleSessionName)
						.build();
				StsClient sts = StsClient.builder()
						.credentialsProvider(InstanceProfileCredentialsProvider.create())
						.httpClient(sdkHttpClient)
						.overrideConfiguration(clientOverrideConfiguration)
						.build();
				return StsAssumeRoleCredentialsProvider.builder()
						.refreshRequest(req)
						.stsClient(sts)
						.build();
			}
		}

		@Override
		public CredentialsProviderCache.StsProviderPlan plan(String accessKey,
				SdkHttpClient sdkHttpClient, ClientOverrideConfiguration clientOverrideConfiguration) {
			// 以下の処理は、CredentialsProviderCacheのロック外で実施されることを想定
			// EC2MetadataUtilsは外部への呼出しになる
			String roleSessionName = EC2MetadataUtils.getInstanceId() + "@";
			String instanceProfileArn = "";
			try {
				instanceProfileArn = getInstanceProfileArn();
			} catch (SdkClientException e) {
				throw UnrecognizedClientException.builder().message("No IAM role has been assigned to this instance.")
						.build();
			}

			Matcher m = iamArnPattern.matcher(instanceProfileArn);
			if (m.matches()) {
				roleSessionName += m.group(1);
			}

			String roleArn = buildIAMArnForAccessKey(accessKey);

			// この関数内で生成した情報を実際にStsAssumeRoleCredentialsProvider作成を移譲するクラスに渡す
			return new Plan(roleArn, roleSessionName, sdkHttpClient, clientOverrideConfiguration);
		}

		private static String buildIAMArnForAccessKey(String accessKey) {
			accessKey = Objects.requireNonNull(accessKey);

			Matcher m = accessKeyPattern.matcher(accessKey);
			if (m.matches()) {
				// IAM ARN = arn:aws:iam::[accountid]:role/[roleid]
				return "arn:aws:iam::" + m.group(1) + ":role/" + m.group(2);
			} else {
				// 解析できない形式の場合は後続の処理ができないため、例外を出力して終了する
				throw UnrecognizedClientException.builder().message(
						"AccessKey is invalid pattern. @AccountID:RoleID (accessKey you have typed=" + accessKey + ")").build();
			}
		}
	}
	
	private static CredentialsProviderCache providerCache = new CredentialsProviderCache(new Factory());

	/*
	 * StsAssumeRoleCredentialsProviderのキャッシュ管理
	 */
	private static class CredentialsProviderCache {

		private interface CredentialProviderFactory {
			StsProviderPlan plan(String accessKey, SdkHttpClient sdkHttpClient, ClientOverrideConfiguration clientOverrideConfiguration);
		}

		private interface StsProviderPlan {
			StsAssumeRoleCredentialsProvider instantiate();
		}

		private static class ScopeAccessKey {
			private final String scope;
			private final String accessKey;

			private ScopeAccessKey(String scope, String accessKey) {
				this.scope = Objects.requireNonNull(scope);
				this.accessKey = Objects.requireNonNull(accessKey);
			}

			public String scope() {
				return scope;
			}

			public String accessKey() {
				return accessKey;
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) {
					return true;
				}
				if (!(o instanceof ScopeAccessKey)) {
					return false;
				}

				ScopeAccessKey other = (ScopeAccessKey) o;
				return scope.equals(other.scope) && accessKey.equals(other.accessKey);
			}

			@Override
			public int hashCode() {
				return 31 * scope.hashCode() + accessKey.hashCode();
			}

			@Override
			public String toString() {
				return scope + ":" + accessKey;
			}
		}

		/*
		 * キャッシュ内でStsAssumeRoleCredentialsProviderの情報を保持
		 */
		private static class ProviderInfo {
			private final StsAssumeRoleCredentialsProvider provider;
			private final Set<String> monitorIds = Collections.synchronizedSet(new LinkedHashSet<String>());

			public ProviderInfo(StsAssumeRoleCredentialsProvider provider) {
				this.provider = provider;
			}

			public StsAssumeRoleCredentialsProvider getProvider() {
				return provider;
			}

			public Set<String> getMonitorIds() {
				// unmodifiedでくるんでもiterarorの原子性が担保されないので、コピーを渡す
				// Collections.synchronizedSetの同期オブジェクトは自身になる
				synchronized (monitorIds) {
					return new LinkedHashSet<>(monitorIds);
				}
			}

			public boolean add(String monitorId) {
				return monitorIds.add(monitorId);
			}

			public boolean remove(String monitorId) {
				return monitorIds.remove(monitorId);
			}

			public boolean isEmpty() {
				return monitorIds.isEmpty();
			}
		}

		private static final Log log = LogFactory.getLog(CredentialsProviderCache.class);

		private final Map<ScopeAccessKey, ProviderInfo> credentialsProviderMap = new HashMap<>();

		private final Map<String, ScopeAccessKey> scopeAccessKeyMap = new HashMap<>();

		private final CredentialProviderFactory factory;
		private final Object lock = new Object();

		private CredentialsProviderCache(CredentialProviderFactory factory) {
			this.factory = Objects.requireNonNull(factory);
		}

		public StsAssumeRoleCredentialsProvider getCredentialsProvider(CloudLogMonitorConfig config,
				SdkHttpClient sdkHttpClient, ClientOverrideConfiguration clientOverrideConfiguration) {
			config = Objects.requireNonNull(config);
			sdkHttpClient = Objects.requireNonNull(sdkHttpClient);
			clientOverrideConfiguration = Objects.requireNonNull(clientOverrideConfiguration);
			AgtMonitorInfoResponse monitorInfo = Objects.requireNonNull(config.getMonInfo());
			return getCredentialsProvider(config.getMonitorId(), monitorInfo.getScope(), config.getAccess(),
					sdkHttpClient, clientOverrideConfiguration);
		}

		public StsAssumeRoleCredentialsProvider getCredentialsProvider(String monitorId, String scope,
				String accessKey, SdkHttpClient sdkHttpClient, ClientOverrideConfiguration clientOverrideConfiguration) {
			scope = Objects.requireNonNull(scope);
			accessKey = Objects.requireNonNull(accessKey);
			monitorId = Objects.requireNonNull(monitorId);

			ScopeAccessKey key = new ScopeAccessKey(scope, accessKey);

			// 最初のロックは、存在している場合にStsAssumeRoleCredentialsProviderを返すまで
			synchronized (lock) {
				ProviderInfo pi = credentialsProviderMap.get(key);
				if (pi != null) {
					pi.add(monitorId);
					scopeAccessKeyMap.put(monitorId, key);
					return pi.getProvider();
				}
			}

			// 以下の呼出しは、ロックにまぜない
			// EC2MetadataUtilsに対する時間がかかるかもしれない呼出しを実行するので。
			StsProviderPlan plan = factory.plan(accessKey, sdkHttpClient, clientOverrideConfiguration);

			// あらためてロックして、StsAssumeRoleCredentialsProviderを作成する
			synchronized (lock) {
				ProviderInfo info = credentialsProviderMap.get(key);
				if (info == null) {
					StsAssumeRoleCredentialsProvider cp = plan.instantiate();

					info = new ProviderInfo(cp);
					info.add(monitorId);

					credentialsProviderMap.put(key, info);
					scopeAccessKeyMap.put(monitorId, key);

					log.info(String.format("Cached new credentials. scope=%s, monitorId=%s", scope, monitorId));
					return cp;
				} else {
					info.add(monitorId);
					return info.getProvider();
				}
			}
		}

		public ScopeAccessKey getScopeAccessKey(String monitorId) {
			synchronized (lock) {
				return scopeAccessKeyMap.get(monitorId);
			}
		}

		public void remove(String monitorId) {
			synchronized (lock) {
				ScopeAccessKey key = scopeAccessKeyMap.remove(monitorId);
				if (key == null) {
					return;
				}

				ProviderInfo pi = credentialsProviderMap.get(key);
				if (pi != null) {
					if (pi.remove(monitorId)) {
						log.info(String.format("Removed reference to cached credential. scope=%s, monitorId=%s",
								key.scope(), monitorId));
					} else {
						log.info(String.format("Not referencing the cached credential. scope=%s, monitorId=%s",
								key.scope(), monitorId));
					}

					if (pi.isEmpty()) {
						credentialsProviderMap.remove(key);
						log.info(String.format(
								"All references to the credential have been removed, so the credential was removed from the cache. scope=%s, monitorId=%s",
								key.scope(), monitorId));

						try {
							pi.getProvider().close();
						} catch (Exception e) {
							log.warn("Failed to close provider.", e);
						}
					}
				}
			}
		}

		public void forceRemove(String monitorId) {
			ScopeAccessKey key = getScopeAccessKey(monitorId);
			if (key == null) {
				return;
			}
			forceRemove(key);
		}

		public void forceRemove(ScopeAccessKey key) {
			synchronized (lock) {
				ProviderInfo info = credentialsProviderMap.remove(key);
				if (info != null) {
					for (String monitorId : info.getMonitorIds()) {
						scopeAccessKeyMap.remove(monitorId);
					}

					log.info(String.format(
							"Forcefully removed the credential from the cache. scope=%s, monitorIdsAtRemoval=%s",
							key.scope(), info.getMonitorIds().toString()));

					try {
						info.getProvider().close();
					} catch (Exception e) {
						log.warn("Failed to close provider.", e);
					}
				}
			}
		}
	}
}
