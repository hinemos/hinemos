/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.cloud.log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.CharArrayBuffer;

import com.clustercontrol.agent.cloud.log.util.CloudLogRawObject;
import com.clustercontrol.agent.cloud.log.util.CloudLogfileMonitorConfig;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.util.MessageConstant;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * クラウドログ監視で対象がAzureの場合Azureと通信し、ログを取得するクラスです。
 */
public class CloudLogMonitorRunAzure extends AbstractCloudLogMonitorRun {

	public CloudLogMonitorRunAzure(CloudLogMonitorConfig config) {
		super(config);
		rawObject = new ArrayList<CloudLogRawObject>();
	}

	private static Log log = LogFactory.getLog(CloudLogMonitorRunAzure.class);

	private static BasicCredentialsProvider credsProvider = null;
	private static RequestConfig rConfig = null;
	private static CloseableHttpClient client = null;
	private static final int BUFFERSIZE = 1024;

	// Azureへの接続用定数
	private static final String URLID = "/subscriptions/%s/resourceGroups/%s/providers/Microsoft.OperationalInsights/workspaces/%s";
	private static final String URL = "https://management.azure.com%s%s";
	private static final String APIVERSION = "/api/query?api-version=2017-01-01-preview";
	// Azureへのクエリ用定数
	private static final String TIMEGENERATED = "TimeGenerated";
	private static final String INGESTIONTIME = "ingestion_time()";
	private static final String TIMEQUERY = "| where  datetime(%s) < %s and %s <= datetime(%s)";
	private static final String ORDERQUERY = "{\"query\":\"%s%s%s| order by %s asc\"}";

	/**
	 * プロキシの使用有無を確認しHTTPCientを作成
	 */
	static {
		String azure_client_proxy_host = CloudLogMonitorProperty.getInstance().getAzure_client_proxy_host();
		int azure_client_proxy_port = CloudLogMonitorProperty.getInstance().getAzure_client_proxy_port();
		String azure_client_proxy_username = CloudLogMonitorProperty.getInstance().getAzure_client_proxy_username();
		String azure_client_proxy_password = CloudLogMonitorProperty.getInstance().getAzure_client_proxy_password();
		int connectionTimeout = CloudLogMonitorProperty.getInstance().getConnectionTimeout();

		if (azure_client_proxy_host != null && !azure_client_proxy_host.isEmpty()) {
			// Proxyサーバと認証情報のセット
			credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(azure_client_proxy_host, azure_client_proxy_port),
					new UsernamePasswordCredentials(azure_client_proxy_username,
							azure_client_proxy_password.toCharArray()));
			HttpHost proxy = new HttpHost(azure_client_proxy_host, azure_client_proxy_port);

			// httpclientの設定
			rConfig = RequestConfig.custom().setConnectTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
					.setConnectionRequestTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
					.setResponseTimeout(connectionTimeout, TimeUnit.MILLISECONDS).setProxy(proxy).build();
			client = HttpClientBuilder.create().setDefaultRequestConfig(rConfig)
					.setDefaultCredentialsProvider(credsProvider).build();
		} else {
			// httpclientの設定
			rConfig = RequestConfig.custom().setConnectTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
					.setConnectionRequestTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
					.setResponseTimeout(connectionTimeout, TimeUnit.MILLISECONDS).build();
			client = HttpClientBuilder.create().setDefaultRequestConfig(rConfig).build();
		}
	}

	/**
	 * Azureと通信して、ログを取得します。
	 * 
	 * @return
	 */

	@Override
	public void run() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		successFlg = false;

		// 通信障害時に前回の範囲も含めてログを取得するか確認
		if (shoudRetryMissing) {
			log.info("runAzure(): Retry to get missing log. Retry count: " + CNFcount + "Start time: " + lastFailedTime);
			// 0になることはないはずだが念のため
			if (lastFailedTime != 0) {
				lastFireTime = lastFailedTime;
			} else {
				// ここに来たとすると何らかのバグ
				log.warn("runAzure(): Logical error. LastFailedTime is 0 when it should not be");
			}
		}

		// アクセストークンの取得
		AccessToken accessToken = null;
		String url = "";
		try {
			accessToken = getAccessToken(splitIdentity(config.getAccess(), 0), splitIdentity(config.getAccess(), 1),
					config.getSecret());

			// apiアクセス用リクエストのアクセス用
			String id = String.format(URLID, splitIdentity(config.getAccess(), 2), config.getResourceGroup(),
					config.getWorkspaceName());
			url = String.format(URL, id, APIVERSION);
			log.debug("runAzure(): accessURL: " + url);

		} catch (HinemosUnknown e) {
			// 既に処理済みなので何もしない
			log.debug("runAzure();", e);
			return;
		} catch (Exception e) {
			log.warn("runAzure() invalid credential ", e);
			// マネージャに通知
			if (!hasNotifiedINC) {
				CloudLogMonitorUtil.sendMessage(config, PriorityConstant.TYPE_WARNING,
						MessageConstant.AGENT.getMessage(),
						MessageConstant.MESSAGE_CLOUD_LOG_MONITOR_FAILED_INVALID_CREDENTIAL.getMessage(),
						e.getMessage());
				hasNotifiedINC = true;
			}
			return;
		}

		HttpPost requestPost = new HttpPost(url);
		requestPost.addHeader("Content-Type", "application/json; charset=utf-8");
		requestPost.addHeader("Authorization", accessToken.getTokenType() + " " + accessToken.getAccessToken());

		// 取得先のテーブル、カラムの指定
		String tableName = config.getTable();
		// TimeGeneratedは必ず含めるようにする
		boolean containTimeGenerated = false;
		String userCols = config.getCol().replaceAll(" ", "");
		if (userCols.contains(TIMEGENERATED)) {
			containTimeGenerated = true;
			userCols = userCols.replace(TIMEGENERATED + ",", "");
			userCols = userCols.replace("," + TIMEGENERATED, "");
		}

		String cols = String.format("%s,%s,%s", INGESTIONTIME, TIMEGENERATED, userCols);
		String queryStr = String.format("|project %s", cols);

		// 取得先時刻（現在時刻を取得）
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date nowDate = new Date(CloudLogMonitorUtil.getTimeWithOffset());
		String nowTime = sdf.format(nowDate);
		log.debug("runAzure(): get log from " + lastFireTime + " to " + nowDate.getTime());

		// 日時指定用のクエリ作成
		String timeStr = String.format(TIMEQUERY, sdf.format(lastFireTime), INGESTIONTIME, INGESTIONTIME, nowTime);
		String e = String.format(ORDERQUERY, tableName, queryStr, timeStr, INGESTIONTIME);
		log.debug("runAzure(): access url: " + e);

		// Azureへ送信
		// 通信エラーの場合は再送を試みる
		requestPost.setEntity(new StringEntity(e));
		ClassicHttpResponse response = null;

		try {
			response = execPost(requestPost);
		} catch (IOException e3) {
			// 既に通知などは行っているので何もしない
			log.debug("runAzure();", e3);
			return;
		}

		// レスポンスが得られた場合はレスポンスをチェック
		HttpEntity entity = response.getEntity();
				
		String responseBody = "";
		boolean isOverflow = false;
		int overflowLength = 0;
		Reader reader = null;
		try {
			// バッファを利用してレスポンスを読み込む
			final CharArrayBuffer buf = new CharArrayBuffer(BUFFERSIZE);
			reader = new InputStreamReader(entity.getContent());
			final char[] tmp = new char[BUFFERSIZE];
			int chReadCount;
			while ((chReadCount = reader.read(tmp)) != -1) {
				buf.append(tmp, 0, chReadCount);
				// 読み込んだバッファのサイズが最大ログ取得量を超えたら中断
				if (buf.length() > props.getMaxLogsize()) {
					isOverflow = true;
					break;
				}
			}
			// ログが多すぎた場合は、現在読み込んだ量を記録しておく
			if (!isOverflow) {
				responseBody = buf.toString();
				log.debug("runAzure(): read length: " + buf.length());
			} else {
				overflowLength = buf.length();
			}
		} catch (Exception ex) {
			// ここにたどり着いた場合、ユーザに見せて利益のある情報はないので、
			// 詳細はログに出力するのみ。(レスポンスコードに応じてINTERNALは出力される)
			log.warn("runAzure(): Response does not contain any info.", ex);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
					log.warn("runAzure(): Failed closing reader.", e1);
				}
			}
		}
		// レスポンスコードを確認し、200以外なら適切なINTERNALを出力
		if (response.getCode() == 400) {
			log.warn("runAzure(): Invalid setting:" + responseBody);
			if (!hasNotifiedBDC) {
				String[] args = { config.getTable(), config.getCol() };
				CloudLogMonitorUtil.sendMessage(config, PriorityConstant.TYPE_WARNING,
						MessageConstant.AGENT.getMessage(),
						MessageConstant.MESSAGE_CLOUD_LOG_MONITOR_FAILED_NO_RESOURCE.getMessage(args), responseBody);
				hasNotifiedBDC = true;
			}
			return;
		} else if (response.getCode() == 401 || response.getCode() == 403) {
			log.warn("runAzure(): Wrong Auth Info:" + responseBody);
			if (!hasNotifiedINC) {
				CloudLogMonitorUtil.sendMessage(config, PriorityConstant.TYPE_WARNING,
						MessageConstant.AGENT.getMessage(),
						MessageConstant.MESSAGE_CLOUD_LOG_MONITOR_FAILED_INVALID_CREDENTIAL.getMessage(), responseBody);
				hasNotifiedINC = true;
			}
			return;
		} else if (response.getCode() == 204 || response.getCode() == 404) {
			log.warn("runAzure():" + responseBody);
			if (!hasNotifiedRNF) {
				String[] args = { config.getWorkspaceName(), config.getTable() };
				CloudLogMonitorUtil.sendMessage(config, PriorityConstant.TYPE_WARNING,
						MessageConstant.AGENT.getMessage(),
						MessageConstant.MESSAGE_CLOUD_LOG_MONITOR_FAILED_NO_RESOURCE.getMessage(args), responseBody);
				hasNotifiedRNF = true;
			}
			return;

		} else if (response.getCode() == 200) {
			log.debug("run(): Succeed:" + responseBody);
		} else {
			log.warn("runAzure(): Unknown error:" + responseBody);

			if (!hasNotifiedOthers) {
				CloudLogMonitorUtil.sendMessage(config, PriorityConstant.TYPE_WARNING,
						MessageConstant.AGENT.getMessage(),
						MessageConstant.MESSAGE_CLOUD_LOG_MONITOR_FAILED_UNKNOWN.getMessage(), responseBody);
				hasNotifiedOthers = true;
			}
			return;
		}

		// レスポンスが大きすぎる場合
		if (isOverflow) {
			log.warn("runAzure(): Response size is exceeds maximum. Size:" + overflowLength);
			String[] args = { "" + overflowLength, "" + props.getMaxLogsize() };

			CloudLogMonitorUtil.sendMessage(config, PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
					MessageConstant.MESSAGE_CLOUD_LOG_MONITOR_FAILED_TOO_MANY_LOGS.getMessage(args), "");
			return;
		}

		// jsonをリストに変換
		ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("rawtypes")
		HashMap tmpMap = null;
		try {
			tmpMap = mapper.readValue(responseBody, HashMap.class);
		} catch (JsonProcessingException e2) {
			log.warn("runAzure(): Unknown error:" + responseBody);

			if (!hasNotifiedOthers) {
				CloudLogMonitorUtil.sendMessage(config, PriorityConstant.TYPE_WARNING,
						MessageConstant.AGENT.getMessage(),
						MessageConstant.MESSAGE_CLOUD_LOG_MONITOR_FAILED_UNKNOWN.getMessage(), e2.getMessage());
				hasNotifiedOthers = true;
			}
			return;
		}

		// テーブルを探索し、指定したカラム情報を持っているテーブルのインデックスを見つける
		@SuppressWarnings("unchecked")
		HashMap<String, ArrayList<HashMap<String, ArrayList<Object>>>> obj = tmpMap;
		int tableIndex = getTableIndex(obj, cols);
		
		// カラム情報をフォーマットして、
		// 一時ファイルに書き出し
		try {
			 findAndFormatMessages(obj, tableIndex, containTimeGenerated, rawObject);
		} catch (HinemosUnknown e1) {
			if (!hasNotifiedTPE) {
				CloudLogMonitorUtil.sendMessage(config, PriorityConstant.TYPE_WARNING,
						MessageConstant.AGENT.getMessage(),
						MessageConstant.MESSAGE_CLOUD_LOG_MONITOR_FAILED_TMP_FILE.getMessage(), e1.getMessage());
				hasNotifiedTPE = true;
			}
			return;
		}

		// ファイル監視を実行
		execFileMonitor(rawObject, resumeFlg);
		// rawObjectの切りつめ
		truncateRawObjectCarryOver();

		// 取得した日時を記録
		lastFireTime = nowDate.getTime() + 1;
		log.debug("runAzure(): run ended. LastFireTime:" + lastFireTime);

		successFlg = true;

		// INTERNALが発生していた場合は回復通知
		CloudLogMonitorUtil.notifyRecovery(config,
				hasNotifiedCNF | hasNotifiedINC | hasNotifiedBDC | hasNotifiedRNF | hasNotifiedOthers | hasNotifiedTPE);
		shoudRetryMissing = false;
		hasNotifiedINC = false;
		hasNotifiedBDC = false;
		hasNotifiedRNF = false;
		hasNotifiedOthers = false;
		hasNotifiedTPE = false;
		hasNotifiedCNF = false;

		return;
	}

	/**
	 * レスポンステーブルを探索し、 メッセージを含むインデックスを返します。
	 * 
	 * @param tmpMap
	 * @param cols
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private int getTableIndex(HashMap<String, ArrayList<HashMap<String, ArrayList<Object>>>> obj, String cols) {
		// テーブル一覧を取得
		ArrayList<HashMap<String, ArrayList<Object>>> tableList = obj.get("Tables");

		int tableIndex = 0;
		boolean found = false;
		for (HashMap<String, ArrayList<Object>> tableMap : tableList) {
			for (int i = 0; i < tableMap.get("Columns").size(); i++) {
				HashMap<String, String> colMap = (HashMap<String, String>) tableMap.get("Columns").get(i);
				for (String key : cols.split(",")) {
					if (colMap.containsValue(key)) {
						// 一致すれば終了
						found = true;
						break;
					}
				}
				if (found) {
					break;
				}
			}
			if (found) {
				break;
			}
			// テーブル内に一致するものが無ければ次へ
			tableIndex++;
		}

		return tableIndex;
	}

	/**
	 * テーブルからカラム情報を抽出し、一時ファイルに書き出す形でフォーマット
	 * 
	 * @param obj
	 * @param tableIndex
	 * @param containTimeGenerated
	 * @param rawObject
	 * @throws HinemosUnknown 
	 */
	private void findAndFormatMessages(HashMap<String, ArrayList<HashMap<String, ArrayList<Object>>>> obj, int tableIndex,
			boolean containTimeGenerated, List<CloudLogRawObject> rawObject) throws HinemosUnknown {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		// Azure logsのタイムスタンプはUTCなので、UTCで変換
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		// テーブルを見つけたら、カラムをフォーマット
		ArrayList<Object> t = obj.get("Tables").get(tableIndex).get("Rows");
		StringBuilder sb = new StringBuilder();
		boolean hasRotated = false;
		String retCode = getReturnCodeString(config.getReturnCode());

		for (int i = 0; i < t.size(); i++) {
			// 一度のログ取得で一時ファイルが１回以上ローテーションされた場合は、
			// ローテーションを検知するため一度ファイル監視を実施
			// ここでローテートを検知できないと、次の一時ファイル書き込み時にローテートが発生した場合に
			// 書き込みに失敗する
			if (hasRotated) {
				log.info("findAndFormatMessages(): File Rotate occured while writing logs to file. Exec File Monitor.");
				try {
					// 負荷軽減とローテートを確実に検知するため
					// monitor.cloudlogfile.filter.interval指定秒数スリープ（デフォルト1秒）
					Thread.sleep(CloudLogfileMonitorConfig.getInstance().getRunInterval());
				} catch (InterruptedException e1) {
					log.warn("findAndFormatMessages():", e1);
				}
				execFileMonitor(rawObject, resumeFlg);
				hasRotated = false;
			}

			@SuppressWarnings("unchecked")
			ArrayList<String> tmp = (ArrayList<String>) t.get(i);

			Date parsedDate = null;
			for (int y = 0; y < tmp.size(); y++) {
				if (y == 0) {
					// dispose time received
				} else if (y == 1) {
					// timeGeratedのパース
					try {
						parsedDate = sdf.parse(tmp.get(y));
					} catch (java.text.ParseException e1) {
					}
					// TimeGeneratedがユーザ指定で含まれる場合は、パース前の文字列を含む
					if (containTimeGenerated) {
						sb.append(tmp.get(y) + " ");
					}
				} else if (y == tmp.size() - 1) {
					// 最後にスペースをつけない
					sb.append(tmp.get(y));
				} else {
					sb.append(tmp.get(y) + " ");
				}
			}
			// 行内で改行するメッセージだった場合、
			// 今後の処理のため、改行した状態で書き込む
			// (一時ファイルの改行コードはLFで固定のため）
			// 区切り条件が改行コード以外の場合でも一時ファイルに改行コードが
			// 混在してしまわないように、処理を行う
			log.debug("findAndFormatMessages(): split message with " + config.getReturnCode());
			StringBuilder sbRes = new StringBuilder();
			for (String mes :sb.toString().split(retCode)) {
				String splitStr = mes + "\n";
				sbRes.append(splitStr);
				rawObject.add(new CloudLogRawObject(parsedDate, splitStr, null));
			}
			log.debug("findAndFormatMessages(): find Message :" + sb.toString());
			hasRotated = writeToFile(config, sbRes.toString(), null);
			sb.setLength(0);
		}

		// ローテート直後の場合、今回のファイル監視では
		// ローテート前ファイルの最後までの読込になるので、
		// 一度ファイル監視を走らせておく
		// (そうするとローテート後ファイルへの書き出し分の監視がrun()の方で実行される
		// execFileMonitorで検知される)
		if (hasRotated) {
			log.info("findAndFormatMessages(): File Rotate occured after writing logs to file. Exec File Monitor.");
			execFileMonitor(rawObject, resumeFlg);
			try {
				// 負荷軽減とローテートを確実に検知するため
				// monitor.cloudlogfile.filter.interval指定秒数スリープ（デフォルト1秒）
				Thread.sleep(CloudLogfileMonitorConfig.getInstance().getRunInterval());
			} catch (InterruptedException e1) {
				log.warn("findAndFormatMessages():", e1);
			}
		}
	}

	/**
	 * Azure用のアクセストークンの取得
	 * 
	 * @param tenantId
	 * @param applicationId
	 * @param key
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws HinemosUnknown
	 * @throws ParseException
	 */
	protected AccessToken getAccessToken(String tenantId, String applicationId, String key)
			throws ClientProtocolException, IOException, HinemosUnknown, ParseException {
		String url = String.format("https://login.microsoftonline.com/%s/oauth2/token", tenantId);
		String target = "resource=" + URLEncoder.encode("https://management.core.windows.net/", "UTF-8") + "&"
				+ "scope=openid" + "&" + "client_id=" + applicationId + "&" + "client_secret="
				+ URLEncoder.encode(key, "UTF-8") + "&" + "grant_type=client_credentials";

		HttpPost requestPost = new HttpPost(url);
		requestPost.addHeader("Accept", "application/json");
		requestPost.addHeader("content-type", "application/x-www-form-urlencoded");
		requestPost.addHeader("return-client-request-id", "true");
		requestPost.setEntity(new StringEntity(target));

		ClassicHttpResponse response = null;
		try {
			response = execPost(requestPost);
		} catch (IOException e) {
			throw new HinemosUnknown();
		}

		HttpEntity entity = response.getEntity();

		ObjectMapper om = new ObjectMapper();
		AccessToken token = null;
		try {
			token = om.readValue(EntityUtils.toString(entity).trim(), AccessToken.class);
		} catch (ParseException e) {
			log.error("getAccessToken(): failed json parsing.", e);
			throw e;
		}

		return token;
	}

	public String splitIdentity(String identity, int sel) throws HinemosUnknown {

		String[] words = identity.split("/", 3);
		if (words.length != 3) {
			String errmsg = "identity does not contains sufficient information. Identity: " + identity;
			log.error("splitIdentity(): " + errmsg);
			throw new HinemosUnknown(errmsg);
		}

		// 0tenantId. 1 String applicationId .2 String subscriptionId

		return words[sel];
	}

	/**
	 * HTTPPOSTを実行します。
	 * 
	 * @param requestPost
	 * @return
	 * @throws IOException
	 */
	private ClassicHttpResponse execPost(HttpPost requestPost) throws IOException {
		ClassicHttpResponse response = null;
		boolean isSuccess = false;
		IOException e = null;
		for (int i = 0; i < props.getAzureClientRetry(); i++) {
			try {
				response = client.execute(requestPost);
				isSuccess = true;
				break;

			} catch (IOException c) {
				log.warn("runAzure(): Error occurs during API call. Retry (current: " + (i + 1) + " threashold: "
						+ props.getAzureClientRetry(), c);
				try {
					Thread.sleep(props.getAzure_retry_interval());
				} catch (InterruptedException e1) {
					log.warn(e1);
				}
				e = c;
			}
		}

		if (!isSuccess) {
			shouldNotifyFailure(e);
			throw e;
		}

		return response;
	}

	private static class AccessToken {
		protected String tokenType;
		protected long expiresIn;
		protected long extExpiresIn;
		protected long expiresOn;
		protected long notBefore;
		protected String resource;
		protected String accessToken;

		@JsonProperty("token_type")
		public String getTokenType() {
			return tokenType;
		}

		@JsonProperty("token_type")
		public void setTokenType(String tokenType) {
			this.tokenType = tokenType;
		}

		@JsonProperty("expires_in")
		public long getExpiresIn() {
			return expiresIn;
		}

		@SuppressWarnings("unused")
		public void setExpiresIn(long expiresIn) {
			this.expiresIn = expiresIn;
		}

		@JsonProperty("ext_expires_in")
		public long getExtExpiresIn() {
			return extExpiresIn;
		}

		@SuppressWarnings("unused")
		public void setExtExpiresIn(long extExpiresIn) {
			this.extExpiresIn = extExpiresIn;
		}

		@JsonProperty("expires_on")
		public long getExpiresOn() {
			return expiresOn;
		}

		@SuppressWarnings("unused")
		public void setExpiresOn(long expiresOn) {
			this.expiresOn = expiresOn;
		}

		@JsonProperty("not_before")
		public long getNotBefore() {
			return notBefore;
		}

		@SuppressWarnings("unused")
		public void setNotBefore(long notBefore) {
			this.notBefore = notBefore;
		}

		@JsonProperty("resource")
		public String getResource() {
			return resource;
		}

		@SuppressWarnings("unused")
		public void setResource(String resource) {
			this.resource = resource;
		}

		@JsonProperty("access_token")
		public String getAccessToken() {
			return accessToken;
		}

		@SuppressWarnings("unused")
		public void setAccessToken(String accessToken) {
			this.accessToken = accessToken;
		}
	}

}
